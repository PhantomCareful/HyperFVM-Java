package com.careful.HyperFVM.Activities.Oobe;

import static com.careful.HyperFVM.utils.ForDesign.Blur.MiuiLogoBlurUtil.BLEND_ALPHA_CLAMP;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.MiuiLogoBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Glow.GlowController;
import com.careful.HyperFVM.utils.ForDesign.Glow.RenderViewLayout;

import fan.animation.Folme;
import fan.animation.FolmeEase;
import fan.animation.IStateStyle;
import fan.animation.base.AnimConfig;
import fan.animation.controller.AnimState;
import fan.animation.property.ViewProperty;

/**
 * OOBE 欢迎页（首启全屏第一屏，原 OobeActivity 单页逻辑整体搬迁为 Fragment）。
 *
 * <ul>
 *   <li>背景：glow AGSL 流光（{@link GlowController}，0.2 降采样），开场光圈从 logo 位置扩散；
 *       SDK&lt;33 时 RuntimeShader 不可用，根背景降级为 ?attr/colorSurface 纯色</li>
 *   <li>文字/图标流光：{@link MiuiLogoBlurUtil} cross blur 透出下方 glow（系统模糊不支持时静默静态）</li>
 *   <li>入场动画：Folme logo 缩放两段 + 文字淡入 + 下一步按钮 delay 1340ms（对齐 HC AnimHelper）</li>
 *   <li>点"下一步"→ {@link OobeActivity#goToNextFromButton(View)} 系统级窗口从按钮展开进
 *       {@code OobeIntroductionActivity}（first_run 标记在完毕页点"开始"时才写）</li>
 * </ul>
 *
 * glow 生命周期：onStart 恢复流光（不重放开场光圈）/ onStop 停帧 / onDestroy 彻底释放；
 * 入场编排只在首次获得窗口焦点时播一次（HC StartupFragment 同款时机）。
 */
public class OobeWelcomeFragment extends Fragment implements IOnFocusListener {

    private RenderViewLayout mRenderViewLayout;
    private GlowController mGlowController;
    private View mLogoImage;
    private View mLogoImageWrapper;
    private View mNextLayout;
    private View mNext;

    // 入场编排只在首次获得窗口焦点时播一次（息屏/回前台不再重复）
    private boolean mEntered = false;

    // 进程级首启标记（HC Utils.isFirstBoot 同责）：back 返回欢迎页时重建实例，
    // 不重放入场动画，只恢复无光圈流光（HC onStart start(false) 同款）
    private static boolean sFirstBoot = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_oobe_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogoImage = view.findViewById(R.id.logo_image);
        mLogoImageWrapper = view.findViewById(R.id.logo_image_wrapper);
        mNextLayout = view.findViewById(R.id.next_layout);
        mNext = view.findViewById(R.id.next);
        View textLogo = view.findViewById(R.id.text_logo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // glow 流光：glow View 挂进降采样容器（0.2 尺寸 + 5 倍放大，1/25 像素量）
            mRenderViewLayout = view.findViewById(R.id.render_view_layout);
            View glowView = new View(requireContext());
            mRenderViewLayout.attachView(glowView, 0.2f, Color.BLACK);
            mGlowController = new GlowController(glowView);
            // 预热：shader 解析/编译提前到首帧前完成，避免编译顿挫落在开场光圈扩散期
            mGlowController.warmUp();
        } else {
            // 降级：RuntimeShader 需 Android 13，根背景改用主题 colorSurface 纯色
            // （解析值跟随当前深浅色，与页内 colorOnSurface 文字形成正确对比）
            TypedValue typedValue = new TypedValue();
            if (requireContext().getTheme().resolveAttribute(
                    com.google.android.material.R.attr.colorSurface, typedValue, true)
                    && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                view.setBackgroundColor(typedValue.data);
            }
        }

        if (!sFirstBoot) {
            // back 返回欢迎页（重建实例且不重放入场）：布局初始 alpha=0 的三项直接恢复终态，
            // 否则内容不可见；alpha 终值 BLEND_ALPHA_CLAMP 防 OS1.0 blend 跳过（与入场动画一致）
            mLogoImage.setAlpha(BLEND_ALPHA_CLAMP);
            mLogoImageWrapper.setAlpha(BLEND_ALPHA_CLAMP);
            mNextLayout.setAlpha(BLEND_ALPHA_CLAMP);
        } else {
            // 不可见预热（首启路径）：三项布局初始 alpha=0 会被首帧跳过绘制，
            // HWUI 图形管线（blend/blur 等 key）要等入场动画首绘时才现场编译——
            // 清数据首启 code_cache 的 skia 管线磁盘缓存已被清，编译落在光圈扩散期
            // 会连续卡顿（实测单条 pipeline 最长 127ms、动画期累计 ~180ms）。
            // 改为首帧以 alpha=0.01 完成绘制（需高于 Skia 低透明度跳过阈值 1/255≈0.0039，
            // 否则绘制被整树拒绝、预热失效；黑底上 1% 亮度视觉不可见），
            // 把编译提前到动画前；焦点到达时 Folme setTo(0) 照常接管，动画起点值差异肉眼无感。
            final View decor = view.getRootView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                private boolean mDone;

                @Override
                public boolean onPreDraw() {
                    if (!mDone) {
                        mDone = true;
                        mLogoImage.setAlpha(0.01f);
                        mLogoImageWrapper.setAlpha(0.01f);
                        mNextLayout.setAlpha(0.01f);
                        decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                }
            });
        }

        // logo/文字/按钮的 MIUI cross blur 流光（透出下方 glow，HC OOBE 同款配色，
        // 颜色与层数照抄 HC StartupFragment：logo 与文字三层（基础层 19 + 100 + 106，
        // 固定日间基础层、不做日夜切换），下一步按钮两层（100 + 106）；系统模糊不支持时 applyBlendEffect 内部静默跳过）
        View enterLayout = view.findViewById(R.id.miui_enter_layout);
        MiuiLogoBlurUtil.applyBlendEffect(requireContext(), enterLayout,
                new int[]{-867546550, -11579569, -15011328}, new int[]{19, 100, 106},
                mLogoImage, textLogo);
        MiuiLogoBlurUtil.applyBlendEffect(requireContext(), enterLayout,
                new int[]{-13750738, -15011328}, new int[]{100, 106},
                mNext);

        // 下一步：单击热区含背景圆与箭头（与 HC StartupFragment 三个点击目标同责）→ 软件介绍页；
        // 转场为系统默认动画
        View.OnClickListener nextStep = v -> {
            OobeActivity activity = (OobeActivity) requireActivity();
            activity.goToNextFromButton(mNext);
        };
        mNextLayout.setOnClickListener(nextStep);
        mNext.setOnClickListener(nextStep);
        view.findViewById(R.id.next_arrow).setOnClickListener(nextStep);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || mEntered || !isAdded() || getView() == null) {
            return;
        }
        mEntered = true;

        if (sFirstBoot) {
            sFirstBoot = false;
            // SDK 33 以下不存在 glow 流光（根背景降级为 colorSurface 纯色），控制器必为 null；版本判断同时满足 lint 的 API 级别检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
                // 首启带开场光圈（uShowCircle=1），圆心对齐 logo 实际位置（需在 start 后，painter 已创建）
                mGlowController.start(true);
                mGlowController.setCircleYOffsetWithView(mLogoImageWrapper, mRenderViewLayout);
            }

            // 入场编排（对齐 HC AnimHelper/StartupFragment：logo 两段缩放、按钮延迟到光圈铺完）
            startPageLogoAnim(mLogoImage);
            startPageLogoAnim(mLogoImageWrapper);
            startPageBtnAnim(mNextLayout);
            return;
        }

        // back 返回欢迎页：不重放入场，只恢复无光圈流光（HC isFirstBoot=false 分支同款）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.start(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 恢复场景（息屏/切后台后回前台、页面退栈回欢迎页）：重建流光但不重放开场光圈
        if (mEntered && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.start(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.stop();
            mGlowController = null;
        }
    }

    /**
     * logo/文字入场：scale 0.5 →(sinOut 440ms) 0.95 →(cubicOut 700ms) 1.0，alpha 延迟 60ms 淡入。
     * alpha 终值用 BLEND_ALPHA_CLAMP（0.999）：OS1.0 上 alpha 恰为 1 会跳过 blend 渲染导致
     * 文字流光失效（同卡片名 clamp 的机理），0.999 视觉无差。
     */
    private void startPageLogoAnim(View view) {
        if (view == null) return;
        AnimState start = new AnimState("start");
        AnimState from = start.add(ViewProperty.SCALE_X, 0.5d).add(ViewProperty.SCALE_Y, 0.5d);
        AnimState middle = new AnimState("middle").add(ViewProperty.SCALE_X, 0.95d).add(ViewProperty.SCALE_Y, 0.95d);
        Folme.use(view).state().setTo(from)
                .to(middle, new AnimConfig().setEase(FolmeEase.sinOut(440L)))
                .then(new AnimState("end").add(ViewProperty.SCALE_X, 1.0d).add(ViewProperty.SCALE_Y, 1.0d),
                        new AnimConfig().setEase(FolmeEase.cubicOut(700L)));

        AnimConfig delay = new AnimConfig().setDelay(60L);
        IStateStyle state = Folme.use(view).state();
        state.setTo(ViewProperty.ALPHA, 0.0f).to(ViewProperty.ALPHA, BLEND_ALPHA_CLAMP, delay);
    }

    /**
     * 下一步按钮入场：alpha 0→1 + scale 0.9→1，cubicOut 450ms、delay 1340ms
     * （等开场光圈铺满再出现，与 HC startPageBtnAnim 参数一致）。
     * alpha 终值同样钳在 BLEND_ALPHA_CLAMP：按钮带 cross blur 混色，OS1.0 上
     * alpha 恰为 1 会跳过 blend 渲染（白圆变纯色、流光消失）。
     */
    private void startPageBtnAnim(View view) {
        if (view == null) return;
        AnimState from = new AnimState("start");
        AnimState fromFull = from.add(ViewProperty.ALPHA, 0.0d)
                .add(ViewProperty.SCALE_X, 0.9d).add(ViewProperty.SCALE_Y, 0.9d);
        AnimState end = new AnimState("end").add(ViewProperty.ALPHA, BLEND_ALPHA_CLAMP)
                .add(ViewProperty.SCALE_X, 1.0d).add(ViewProperty.SCALE_Y, 1.0d);
        AnimConfig delay = new AnimConfig().setEase(FolmeEase.cubicOut(450L)).setDelay(1340L);
        Folme.use(view).state().setTo(fromFull).to(end, delay);
    }
}
