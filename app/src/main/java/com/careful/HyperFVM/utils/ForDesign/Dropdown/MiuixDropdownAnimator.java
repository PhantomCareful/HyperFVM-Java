package com.careful.HyperFVM.utils.ForDesign.Dropdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.appcompat.widget.ListPopupWindow;

import java.lang.reflect.Field;
import java.util.Collection;

import fan.animation.Folme;
import fan.animation.IFolme;
import fan.animation.IStateStyle;
import fan.animation.base.AnimConfig;
import fan.animation.controller.AnimState;
import fan.animation.listener.TransitionListener;
import fan.animation.listener.UpdateInfo;
import fan.animation.property.ValueProperty;
import fan.animation.property.ViewProperty;
import fan.animation.utils.EaseManager;

/**
 * Miuix 下拉菜单展开/收起动画（view 级），移植自 fan.popupwidget 的 PopupAnimHelper，
 * 对齐 HyperCeiler 真机实际走的动画路径（HC 在 prepareShow 中关闭了窗口动画，
 * 展开/收起全部由该 helper 承担，此前移植的窗口动画只是它禁用掉的 fallback）：
 * <ul>
 *   <li>展开：菜单内容从贴右缘的窄条（宽 0.15 倍、高宽比 0.2；菜单右缘已由
 *       showAnchored 固定对齐容器右缘，故起点即容器右下角（向下弹）/右上角
 *       （向上弹））以弹簧（damping 0.82 / response 0.33）变形展开到完整尺寸，
 *       alpha 走独立 200ms 渐显（官方 mEnterAlphaConfig 同款时序，与几何弹簧并行
 *       推进），可视圆角 4dp→16dp 平滑过渡，首帧被取消避免完整尺寸闪现；</li>
 *   <li>收起：反向收缩 + alpha 150ms 渐隐，渐隐完成立即结束弹簧并真正关闭窗口
 *       （与 HC 一致，收起总时长约 150ms）。</li>
 * </ul>
 *
 * 工程要点（androidx ListPopupWindow 无动画 API，均经反射其内部 mPopup 取框架 PopupWindow）：
 * <ul>
 *   <li>view 级变形要求窗口背景透明（窗口背景不随 view 变换）：show 前把原背景搬到
 *       listView，由它继续绘制菜单底与 16dp 圆角；</li>
 *   <li>收起拦截：条目点击/外部点击经 {@link #dismissAnimated} 走本动画；返回键由
 *       框架直接 dismiss 无法拦截，为直接关闭（遮罩经 OnDismissListener 淡出）；</li>
 *   <li>窗口动画全程清零（官方 view 级路径从不携带动画 style，窗口不带任何
 *       enter/exit 过场）：show 前 setAnimationStyle(0) 清掉 ListPopupWindow 构造时
 *       经主题 defStyle 带入的窗口动画，show 后把根 view 的 windowAnimations 清零
 *       并经 WindowManager.updateViewLayout 硬提交（与收起同一方法）——展开纯由
 *       view 级弹簧呈现，不叠加窗口级过场；</li>
 *   <li>任一环节异常均静默降级（恢复静态形态或直接关闭），保证菜单始终可弹出、可关闭；</li>
 *   <li>动画驱动全走 Folme 自计时引擎（几何弹簧、展开/收起 alpha、遮罩淡入淡出），
 *       不用 ValueAnimator/ViewPropertyAnimator——后者受开发者选项 "Animator 时长缩放"
 *       影响（10x 下淡入淡显被拖慢十倍），而 HC 与该缩放无关。</li>
 * </ul>
 */
public final class MiuixDropdownAnimator {

    private static final String TAG = "MiuixDrop";

    // 弹簧参数（HC PopupAnimHelper 同款：EaseManager.getStyle(-2, damping, response)）
    private static final float SPRING_DAMPING = 0.82f;
    private static final float SPRING_RESPONSE = 0.33f;
    // 展开渐显时长（HC mEnterAlphaConfig：EaseManager.getStyle(1, 200)，与几何弹簧并行）
    private static final int ENTER_ALPHA_MS = 200;
    // 收起渐隐时长（HC dismissWithAnim 的 150ms，渐隐完成即结束弹簧并关闭窗口）
    private static final int ALPHA_EXIT_MS = 150;
    // 起始窄条几何：宽为菜单宽的 0.15 倍、起始高宽比 0.2（HC SIZE_W / mStartKGB）
    private static final float START_SIZE_W = 0.15f;
    private static final float START_KGB_RATIO = 0.2f;
    // 可视圆角起止值（HC START_RADIUS=4dp → END_RADIUS=16dp，随 fraction 线性过渡）
    private static final float START_RADIUS_DP = 4f;
    private static final float END_RADIUS_DP = 16f;

    /**
     * 展开/收起的几何进度属性（0=起始窄条，1=完整尺寸；弹簧过冲时允许 >1）。
     * 匿名子类直接读写本实例的 mFraction：免字段反射、无需 proguard keep；
     * setMinVisibleChange(-1) 忽略最小变化阈值，过冲区间持续刷新（HC setEdgeExtension(true) 同款）。
     */
    private static final ValueProperty<MiuixDropdownAnimator> POPUP_FRACTION =
            new ValueProperty<>("fraction") {
                @Override
                public float getValue(MiuixDropdownAnimator anim) {
                    return anim.mFraction;
                }

                @Override
                public void setValue(MiuixDropdownAnimator anim, float fraction) {
                    anim.mFraction = fraction;
                }
            };

    static {
        POPUP_FRACTION.setMinVisibleChange(-1f);
    }

    private final ListPopupWindow mPopup;
    private final PopupWindow mFrameworkPopup;
    private final Drawable mMenuBackground;
    private final int mGravity;

    private final IStateStyle mValueStyle;
    private final AnimConfig mAnimConfig;
    private IFolme mContentFolme;

    private View mContent;
    private ScaleListener mScaleListener;
    private ViewTreeObserver.OnPreDrawListener mPreDrawListener;
    private Runnable mOnDismissStart;

    private float mFraction;
    private float mOutlineRadiusPx;
    // outline 可视高度（view 本地坐标，0=完整高度）。收缩期 clipBounds 裁掉底边时须与
    // outline 同步，否则底缘圆弧被 clip 底边裁成直角（HC 改父 bounds 时天然联动）
    private float mOutlineClipHeightPx;
    private boolean mDismissing;
    private boolean mFinished;

    private MiuixDropdownAnimator(ListPopupWindow popup, PopupWindow frameworkPopup,
                                  Drawable menuBackground, int gravity) {
        mPopup = popup;
        mFrameworkPopup = frameworkPopup;
        mMenuBackground = menuBackground;
        mGravity = gravity;
        mValueStyle = Folme.useValue(this);
        mAnimConfig = new AnimConfig()
                .setEase(EaseManager.getStyle(-2, SPRING_DAMPING, SPRING_RESPONSE));
    }

    /**
     * show 前准备动画上下文（须在 popup.show() 之前调用，窗口参数 show 时固化）：
     * 估算弹出方向 → 反射取得框架 PopupWindow → 清零窗口动画 style（对齐官方
     * view 级路径）→ 把菜单背景从窗口搬到透明窗口方案上。
     *
     * @return 动画器；反射失败/无锚点/无背景时返回 null（调用方按无动画降级，保持原状）
     */
    static MiuixDropdownAnimator prepare(ListPopupWindow popup) {
        try {
            View anchor = popup.getAnchorView();
            if (anchor == null) {
                Log.d(TAG, "prepare: no anchor -> skip anim");
                return null;
            }
            Field field = ListPopupWindow.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            Object raw = field.get(popup);
            if (!(raw instanceof PopupWindow frameworkPopup)) {
                Log.d(TAG, "prepare: mPopup is " + (raw == null ? "null" : raw.getClass().getName())
                        + " -> skip anim");
                return null;
            }
            Drawable menuBackground = popup.getBackground();
            if (menuBackground == null) {
                // 无背景不走 view 级动画：窗口透明化后菜单将不可见
                Log.d(TAG, "prepare: no background -> skip anim");
                return null;
            }
            int gravity = estimateGravity(popup, anchor);
            // 窗口动画 style 清零（对齐官方 view 级路径：窗口从不携带动画 style）：
            // ListPopupWindow 构造时经主题 defStyle 带入了含 enter 的窗口动画，
            // show 会把它固化进窗口参数，必须在 show 前清掉
            frameworkPopup.setAnimationStyle(0);
            // 根因修复：框架 PopupWindow 的 content 级 enter/exit transition（主题
            // popupEnterTransition，独立于上面清零的 windowAnimations/animationStyle）
            // 会在 show 后把 PopupBackgroundView 从 (-186,-683) 类偏移平移入场并
            // 收敛到 0，叠加在 view 级窄条弹簧上，导致展开起点总在菜单左上角；
            // 收起期 exit transition 同理会干扰收缩方向。
            // 对齐官方 view 级路径（HC 主题无此 transition、fan PopupWindow 跑不到），
            // enter/exit transition 全清，展开/收起纯由 view 级弹簧呈现
            frameworkPopup.setEnterTransition(null);
            frameworkPopup.setExitTransition(null);
            // 窗口背景透明化：view 级变形只作用于内容 view，窗口背景必须不绘制任何内容
            popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            return new MiuixDropdownAnimator(popup, frameworkPopup, menuBackground, gravity);
        } catch (Throwable t) {
            // 反射失败（字段更名等）：退回无 view 动画现状，不影响弹出流程
            Log.d(TAG, "prepare: FAILED -> skip anim", t);
            return null;
        }
    }

    /**
     * show 后启动动画（须紧随 popup.show() 调用）：搬背景、装动态圆角裁剪、
     * 安装外部点击拦截，并经 OnPreDrawListener 在首帧就绪时启动展开动画。
     * 异常时还原窗口背景并降级为静态形态，保证菜单可用。
     */
    void onShown(ListPopupWindow popup) {
        try {
            ListView listView = popup.getListView();
            if (listView == null) {
                Log.d(TAG, "onShown: listView null -> degrade");
                degrade();
                return;
            }
            mContent = listView;
            listView.setTag(this);
            // 原窗口背景（主题色 + 16dp 圆角 shape）搬到内容 view，视觉零差异
            listView.setBackground(mMenuBackground);
            // 波纹等子项绘制裁进圆角；半径随动画在 view 本地坐标系反算（见 ScaleListener.apply）
            mOutlineRadiusPx = END_RADIUS_DP
                    * listView.getResources().getDisplayMetrics().density;
            applyOutlineProvider(listView);
            mContentFolme = Folme.use(listView);
            // 窗口已添加：把窗口动画经 updateViewLayout 硬清零（与收起同一方法），
            // 展开期间 WMS 侧窗口同样不携带 enter 过场，纯由 view 级弹簧呈现
            disableWindowAnimations();
            installOutsideInterceptor();
            startEnterAnim();
        } catch (Throwable t) {
            Log.d(TAG, "onShown: FAILED -> degrade", t);
            degrade();
        }
    }

    /**
     * 收起动画入口（条目点击/外部点击共用）：反向弹簧收缩 + alpha 渐隐，
     * 完成后真正关闭窗口。重复调用只生效一次。
     */
    void dismissWithAnim() {
        if (mDismissing) {
            return;
        }
        mDismissing = true;
        // 遮罩与菜单同步开始淡出（对齐 HC 的 dim 与菜单同步退出）
        if (mOnDismissStart != null) {
            try {
                mOnDismissStart.run();
            } catch (Throwable ignored) {
            }
        }
        // 清零窗口动画，防止窗口级收起与 view 级收缩叠加
        disableWindowAnimations();
        // 移除尚未触发的进入动画挂点
        if (mPreDrawListener != null && mContent != null) {
            try {
                mContent.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
            } catch (Throwable ignored) {
            }
            mPreDrawListener = null;
        }
        boolean animated = false;
        try {
            if (mContent != null && mContent.isAttachedToWindow() && mScaleListener != null
                    && mContentFolme != null) {
                // alpha 150ms 渐隐，完成/取消即结束几何弹簧（HC 同款时序，收起总时长约 150ms）
                AnimConfig alphaExit = new AnimConfig()
                        .setEase(EaseManager.getStyle(1, ALPHA_EXIT_MS))
                        .addListeners(new TransitionListener() {
                            @Override
                            public void onComplete(Object o) {
                                mValueStyle.end();
                            }

                            @Override
                            public void onCancel(Object o) {
                                mValueStyle.end();
                            }
                        });
                // 打断尚未完成的展开渐显，同步到当前实际 alpha 后再 150ms 渐隐，
                // 否则 to(0) 可能从内部记录的错误状态立即完成、失去渐隐效果
                mContentFolme.resetTo(ViewProperty.ALPHA, mContent.getAlpha());
                mContentFolme.to(ViewProperty.ALPHA, 0f, alphaExit);
                // 几何反向收缩；弹簧结束（或被 end() 终止）后真正关闭窗口，完成/取消双保险
                TransitionListener gate = new TransitionListener() {
                    @Override
                    public void onComplete(Object o) {
                        finishDismiss(this);
                    }

                    @Override
                    public void onCancel(Object o) {
                        finishDismiss(this);
                    }
                };
                mAnimConfig.addListeners(gate);
                mValueStyle.to(new AnimState().add(POPUP_FRACTION, 0f), mAnimConfig);
                animated = true;
            }
        } catch (Throwable t) {
            // 动画启动失败：落到下方直接关闭
            Log.d(TAG, "dismiss: anim start FAILED", t);
        }
        Log.d(TAG, "dismiss: animated=" + animated);
        if (!animated) {
            finishDismiss(null);
        }
    }

    /**
     * 条目点击关闭入口（供各调用点替换直接 popup.dismiss()）：
     * listView tag 上挂有本动画器时走收起动画，否则按原样立即关闭。
     */
    public static void dismissAnimated(ListPopupWindow popup) {
        try {
            ListView listView = popup.getListView();
            if (listView != null && listView.getTag() instanceof MiuixDropdownAnimator) {
                ((MiuixDropdownAnimator) listView.getTag()).dismissWithAnim();
                return;
            }
            Log.d(TAG, "dismissAnimated: no animator tag -> direct dismiss");
        } catch (Throwable t) {
            // 取动画器失败：按原样立即关闭
            Log.d(TAG, "dismissAnimated: get tag FAILED -> direct dismiss", t);
        }
        popup.dismiss();
    }

    /** 由 DropdownPopupHelper 注入：收起动画启动瞬间同步淡出遮罩（替代仅在真关闭后淡出） */
    void setOnDismissStart(Runnable runnable) {
        mOnDismissStart = runnable;
    }

    // ------------------------------------------------------------------ 内部实现

    /** 预估弹出方向（原 DropdownPopupHelper#applyMiuixAnimation 迁入）：show 前按锚点/偏移估算中心偏差 */
    @SuppressLint("RtlHardcoded")
    private static int estimateGravity(ListPopupWindow popup, View anchor) {
        // showAsDropDown：left = anchor.left + xoff，top = anchor.bottom + yoff；
        // 尺寸未设置时按锚点尺寸/0 参与估算
        int popWidth = popup.getWidth() > 0 ? popup.getWidth() : anchor.getWidth();
        int popHeight = Math.max(0, popup.getHeight());
        int dyCenter = anchor.getHeight() / 2 + popup.getVerticalOffset() + popHeight / 2;
        int dxCenter = popup.getHorizontalOffset() + popWidth / 2 - anchor.getWidth() / 2;
        int gravity;
        if (Math.abs(dyCenter) <= 10 || dyCenter > 0) {
            gravity = Gravity.TOP;
        } else {
            gravity = Gravity.BOTTOM;
        }
        if (Math.abs(dxCenter) > 10) {
            gravity |= dxCenter > 0 ? Gravity.LEFT : Gravity.RIGHT;
        }
        return gravity;
    }

    /** 首帧就绪时启动展开动画并取消该帧绘制（菜单不会以完整尺寸闪现） */
    private void startEnterAnim() {
        final View content = mContent;
        mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!content.isAttachedToWindow() || content.getWidth() == 0
                        || content.getHeight() == 0) {
                    return true; // 尚未就绪：保留监听等下一帧
                }
                content.getViewTreeObserver().removeOnPreDrawListener(this);
                mPreDrawListener = null;
                beginEnter(content);
                return false; // 取消首帧，由动画首 tick 接管绘制
            }
        };
        content.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
    }

    /**
     * 启动展开：收起动画的严格逆放——几何走与收起完全相同的 {@link ScaleListener#apply}
     * 路径（f 0→1 即收起的 1→0 反向），先把几何落到收起的终点形态（起始窄条）
     * 再启动弹簧，与 alpha 200ms 渐显并行（官方 showWithAnim 同款）
     */
    private void beginEnter(View content) {
        try {
            mScaleListener = new ScaleListener(
                    new Rect(0, 0, content.getWidth(), content.getHeight()));
            mAnimConfig.addListeners(mScaleListener);
            // 几何最先落地（先于一切动画状态）：首帧即为收起终态的窄条，
            // 消除任何“完整尺寸闪现/起点错位”的时序窗口
            mScaleListener.apply(0f);
            // alpha 独立 200ms 渐显（官方 mEnterAlphaConfig 同款），与几何弹簧并行推进
            mContentFolme.resetTo(ViewProperty.ALPHA, 0f);
            mContentFolme.to(ViewProperty.ALPHA, 1f,
                    new AnimConfig().setEase(EaseManager.getStyle(1, ENTER_ALPHA_MS)));
            mValueStyle.resetTo(new AnimState().add(POPUP_FRACTION, 0f));
            mValueStyle.to(new AnimState().add(POPUP_FRACTION, 1f), mAnimConfig);
        } catch (Throwable t) {
            Log.d(TAG, "enter: FAILED -> degrade", t);
            degrade();
        }
    }

    /**
     * 外部点击拦截：show 后框架 PopupDecorView.dispatchTouchEvent 会先询问
     * touchInterceptor，返回 true 即吞掉事件、不再走框架的 outside/BACK 分支中的
     * outside 分支。拦截 ACTION_OUTSIDE 与落点在窗口外的 ACTION_DOWN，转为带动画收起；
     * 菜单内触摸返回 false 交还正常分发（条目点击/滚动不受影响）。
     * 注意：这会替换 androidx 自带的 PopupTouchInterceptor，其仅承担 IME 弹出时的
     * 列表高度重测（本菜单不涉及输入法），丢失无影响。
     */
    @SuppressLint("ClickableViewAccessibility")
    private void installOutsideInterceptor() {
        mFrameworkPopup.setTouchInterceptor((v, ev) -> {
            if (mDismissing) {
                return true; // 收起动画期间吞掉一切触摸，防止二次触发
            }
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_OUTSIDE) {
                dismissWithAnim();
                return true;
            }
            if (action == MotionEvent.ACTION_DOWN
                    && (ev.getX() < 0 || ev.getY() < 0
                        || ev.getX() > v.getWidth() || ev.getY() > v.getHeight())) {
                dismissWithAnim();
                return true;
            }
            return false;
        });
    }

    /**
     * 清零窗口 enter/exit 动画，保证窗口级过场不与 view 级变形叠加：
     * 1. setAnimationStyle(0) 影响后续 update 重算；
     * 2. 根 view 的 WindowManager.LayoutParams.windowAnimations 清零后经
     *    WindowManager.updateViewLayout 硬提交（HC changeWindowDimAmount 同款路径，
     *    WMS 读取的是最新提交值；show 后调用即覆盖展开期，与收起共用）。
     * 两步皆失败也仅是窗口过场叠加在 view 级动画上，视觉影响有限。
     */
    private void disableWindowAnimations() {
        try {
            mFrameworkPopup.setAnimationStyle(0);
        } catch (Throwable ignored) {
        }
        try {
            View probe = mContent != null ? mContent : mFrameworkPopup.getContentView();
            View root = probe.getRootView();
            ViewGroup.LayoutParams lp = root.getLayoutParams();
            if (lp instanceof WindowManager.LayoutParams wlp) {
                if (wlp.windowAnimations != 0) {
                    wlp.windowAnimations = 0;
                    WindowManager wm = (WindowManager) root.getContext()
                            .getSystemService(Context.WINDOW_SERVICE);
                    wm.updateViewLayout(root, wlp);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** 收起完成（或降级）后的唯一关闭出口：窗口保证关闭，重复回调只生效一次 */
    private void finishDismiss(TransitionListener self) {
        if (mFinished) {
            return;
        }
        mFinished = true;
        Log.d(TAG, "dismiss: FINISHED, closing window");
        if (self != null) {
            try {
                mAnimConfig.removeListeners(self);
            } catch (Throwable ignored) {
            }
        }
        try {
            mPopup.dismiss();
        } catch (Throwable ignored) {
        }
    }

    /** 动态圆角裁剪：半径随动画在 view 本地坐标系被反算（ScaleListener.apply 每帧更新） */
    private void applyOutlineProvider(View target) {
        target.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                // 可视高度跟随 clipBounds（ScaleListener.apply 每帧同步），否则收缩期
                // 底弧落在 clip 区域之外被裁掉，底缘退化为直角；半径钳制到 min(宽,高)/2，
                // 避免收缩末期窄条半径越界（超限同样会退化成非预期形状）
                int h = mOutlineClipHeightPx > 0f
                        ? Math.round(mOutlineClipHeightPx) : view.getHeight();
                float r = Math.min(mOutlineRadiusPx,
                        Math.min(view.getWidth(), h) / 2f);
                outline.setRoundRect(0, 0, view.getWidth(), h, r);
            }
        });
        target.setClipToOutline(true);
    }

    /** 降级出口：还原不透明窗口背景、形态复位、恢复原 helper 的固定 16dp 圆角裁剪 */
    private void degrade() {
        Log.d(TAG, "degrade: restore static form");
        try {
            mPopup.setBackgroundDrawable(mMenuBackground);
        } catch (Throwable ignored) {
        }
        if (mContent == null) {
            return;
        }
        try {
            mContent.setAlpha(1f);
            mContent.setScaleX(1f);
            mContent.setScaleY(1f);
            mContent.setTranslationX(0f);
            mContent.setTranslationY(0f);
            mContent.setClipBounds(null);
            mContent.setLayerType(View.LAYER_TYPE_NONE, null);
            mOutlineRadiusPx = END_RADIUS_DP
                    * mContent.getResources().getDisplayMetrics().density;
            mOutlineClipHeightPx = 0f;
            applyOutlineProvider(mContent);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 几何变形监听器（HC PopupAnimHelper.ScaleListener 复刻，起点改为挂锚点容器的角）：
     * 每帧按 fraction 在起始窄条（锚点容器右下角/右上角）与完整尺寸间插值，
     * 换算成内容 view 的 scale/translation/clipBounds 与屏幕可视圆角。
     * <p>HC 原实现经临时改父容器 bounds + 等比 scale 实现（且父 bounds 修改要求 API 29），
     * 这里用「translation + 等比 scale（pivot 0,0）+ clipBounds」等价复现：
     * 屏幕位置 = [newLeft, newTop]、屏幕尺寸 = 宽 newWidth、裁切高 newHeight。
     */
    private final class ScaleListener extends TransitionListener {

        private final int mEndWidth;
        private final int mEndHeight;
        private final float mEndLeft;
        private final float mEndTop;
        private final float mEndRight;
        private final float mEndBottom;
        private final float mEndKGB;
        private final boolean mTopAligned;
        private final float mStartLeft;
        private final float mStartTop;
        private final float mStartRight;
        private final float mStartBottom;
        private final float mStartRadiusPx;
        private final float mEndRadiusPx;

        ScaleListener(Rect end) {
            mEndLeft = end.left;
            mEndTop = end.top;
            mEndRight = end.right;
            mEndBottom = end.bottom;
            mEndWidth = end.width();
            mEndHeight = end.height();
            mEndKGB = mEndWidth == 0 ? 0f : (float) mEndHeight / mEndWidth;
            int hInset = (int) (mEndWidth * START_SIZE_W);
            int vInset = hInset / 5;
            mTopAligned = (Gravity.getAbsoluteGravity(mGravity, mContent.getLayoutDirection())
                    & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP;
            // HC getStartBounds：水平判定比较 Gravity.START 常量在取绝对 gravity 后恒不成立，
            // 起始窄条实际恒贴右缘；菜单右缘已由 showAnchored 固定对齐容器右缘，
            // 故起点窄条即容器右下角（向下弹）/右上角（向上弹），照抄该行为
            mStartLeft = mEndLeft + (mEndWidth - hInset);
            mStartRight = mEndRight;
            // 垂直：弹出位置在锚点下方（TOP）贴顶缘向上展开，上方（BOTTOM）贴底缘向下展开
            if (mTopAligned) {
                mStartTop = mEndTop;
                mStartBottom = mEndTop + vInset;
            } else {
                mStartTop = mEndBottom - vInset;
                mStartBottom = mEndBottom;
            }
            float density = mContent.getResources().getDisplayMetrics().density;
            mStartRadiusPx = START_RADIUS_DP * density;
            mEndRadiusPx = END_RADIUS_DP * density;
        }

        @Override
        public void onBegin(Object o) {
            // 不再硬件层化：层化后 translation/clipBounds 走纹理合成路径，
            // 展开首帧 snapshot 窄条态时在部分设备上起点渲染错位（视觉偏向左上/左下）；
            // 列表内容轻量，逐帧重绘无性能压力（收起路径同步去层化，展开/收起渲染路径完全一致）
            if (mContent != null) {
                mContent.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        }

        @Override
        public void onUpdate(Object o, Collection<UpdateInfo> updates) {
            UpdateInfo info = UpdateInfo.findByName(updates, "fraction");
            if (info != null) {
                apply(info.getFloatValue());
            }
        }

        @Override
        public void onComplete(Object o) {
            if (mContent != null) {
                // 终态取消裁切（等价全尺寸）；收起路径随即关闭且 alpha 已为 0，无视觉影响
                mContent.setClipBounds(null);
                mOutlineClipHeightPx = 0f;
                mContent.invalidateOutline();
            }
        }

        @Override
        public void onCancel(Object o) {
            // 不动 clipBounds：取消意味着新动画接管，下一帧 onUpdate 会重设
        }

        /** 按 fraction（只钳下界，弹簧过冲 >1 允许外推，与 HC 一致）计算并应用菜单形态 */
        void apply(float rawFraction) {
            if (mEndWidth <= 0 || mEndHeight <= 0 || mContent == null) {
                return;
            }
            float f = Math.max(rawFraction, 0f);
            float newLeft = mStartLeft + (mEndLeft - mStartLeft) * f;
            float newRight = mStartRight + (mEndRight - mStartRight) * f;
            float newWidth = newRight - newLeft;
            // 高度按高宽比插值（HC 公式）：起始 0.2 倍宽 → 目标 宽高比
            float newHeight = (START_KGB_RATIO + (mEndKGB - START_KGB_RATIO) * f)
                    * newWidth + 0.5f;
            float newTop = mTopAligned ? mStartTop : mStartBottom - newHeight;
            float scale = newWidth / mEndWidth;

            View v = mContent;
            v.setPivotX(0f);
            v.setPivotY(0f);
            v.setScaleX(scale);
            v.setScaleY(scale);
            v.setTranslationX(newLeft - mEndLeft);
            v.setTranslationY(newTop - mEndTop);
            // 内容裁切：屏幕裁切区高 newHeight，换算回 view 本地坐标（scale 前）为 newHeight/scale
            int clipHeight = Math.round(newHeight / scale);
            v.setClipBounds(new Rect(0, 0, mEndWidth, clipHeight));
            // 可视圆角 = lerp(4dp, 16dp, f)（屏幕坐标系），本地圆角按 scale 反算
            float screenRadius = mStartRadiusPx + (mEndRadiusPx - mStartRadiusPx) * f;
            mOutlineRadiusPx = scale > 0.01f ? screenRadius / scale : screenRadius;
            // outline 高度与 clipBounds 同步（否则 clip 底边把 outline 底弧裁成直角，
            // 从上方弹出时底缘固定于右下角，收缩全程直角边可见）
            mOutlineClipHeightPx = clipHeight;
            v.invalidateOutline();
            // alpha 不在此处干预：展开由独立 200ms 渐显驱动、收起由 150ms 渐隐驱动
            // （均走 mContentFolme），几何与透明度互不干扰
        }
    }
}
