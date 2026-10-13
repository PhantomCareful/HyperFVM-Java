package com.careful.HyperFVM.utils.ForDesign.Dropdown;

import android.app.Activity;
import android.content.Context;
import android.graphics.Outline;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.BuildConfig;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Scroll.SpringBackLayout;
import com.careful.HyperFVM.utils.ForDesign.Smooth.SmoothCornerTraverser;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

import java.util.function.Consumer;

import fan.animation.Folme;
import fan.animation.base.AnimConfig;
import fan.animation.listener.TransitionListener;
import fan.animation.property.ViewProperty;
import fan.animation.utils.EaseManager;

/**
 * 下拉菜单（ListPopupWindow + popup_dropdown_background 浮层）通用增强：
 * 1. 菜单弹出时给菜单以外的屏幕区域加深灰色遮罩，突出菜单主体（菜单为独立窗口，
 *    天然层叠在遮罩之上）；菜单关闭时遮罩自动移除（与收起动画同步淡出）。
 * 2. 把列表绘制（含首/末条目的按压波纹）裁进浮层圆角，防止波纹溢出 16dp 圆角。
 * 3. 展开/收起采用 Miuix view 级弹簧变形动画（MiuixDropdownAnimator，对齐
 *    HyperCeiler 的 PopupAnimHelper 真实路径）：条目点击/外部点击带动画收起，
 *    窗口级 enter/exit 动画全程清零（展开不叠加窗口过场，纯由 view 级弹簧呈现）。
 * 供 SettingsActivity / CookeryIndexActivity / OddsDisclosureIndexActivity 复用。
 * 另提供 createAdapter/showAnchored 公用构建：选项行渲染（文字高亮/图片/√）与就地弹出
 * 全流程（实测宽度恒为“最长条目+对勾”、四分支展开、右缘固定对齐、居中定位选中项），
 * 供 SettingsActivity（纯文本菜单）与 CardOddsCalculatorActivity（图片/图片+文字/预设预览菜单）复用。
 */
public final class DropdownPopupHelper {

    // 与 drawable/popup_dropdown_background.xml 的 corners radius 保持一致
    private static final int CORNER_RADIUS_DP = 16;
    // 遮罩淡入时长（对齐 HC PopupAnimHelper 的 DIM_ENTER_DURATION=300）
    private static final long DIM_MASK_FADE_IN_MS = 300;
    // 遮罩淡出时长（对齐 HC DIM_EXIT_DURATION=150；view 级收起时与菜单同步启动，
    // 返回键关闭时则在真关闭后才淡出，与窗口 exit 动画同时进行）
    private static final long DIM_MASK_FADE_OUT_MS = 150;

    private DropdownPopupHelper() {
    }

    /**
     * 弹出下拉菜单并附加遮罩、Miuix 展开动画、圆角裁切与长列表越界回弹。须在 setAdapter/
     * setOnItemClickListener 等参数全部设置完成之后调用（本方法内部负责 show，
     * 调用方不要再调 popup.show()）。
     * 注意：会覆盖调用方已设置的 OnDismissListener（当前三处调用方均未设置）。
     */
    public static void show(Activity activity, ListPopupWindow popup) {
        // 遮罩挂在 content 上：随 Activity 销毁自动回收，且不遮挡系统栏图标
        ViewGroup content = activity.findViewById(android.R.id.content);
        View mask = new View(activity);
        // Miuix 窗口遮罩规范：浅色 #4D000000 / 深色 #99000000（night 资源自适应）
        mask.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_window_scrim));
        // modal 模式下触摸由 popup 窗口拦截，此点击兜底仅在非 modal 时防止误触底下控件
        mask.setClickable(true);
        mask.setAlpha(0f);
        content.addView(mask, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fadeMask(mask, 1f, DIM_MASK_FADE_IN_MS, null);
        // 菜单关闭时遮罩先淡出再移除（此时 popup 窗口已关闭，遮罩短暂残留于底层之上无视觉冲突）。
        // 结束/取消均回调（onComplete/onCancel）：避免残留一个透明但可点击的遮罩拦截页面触摸
        popup.setOnDismissListener(() -> {
            if (mask.getAlpha() <= 0.01f) {
                // view 级收起时遮罩已与菜单同步淡出完毕，直接移除即可
                if (mask.getParent() != null) {
                    content.removeView(mask);
                }
                return;
            }
            fadeMask(mask, 0f, DIM_MASK_FADE_OUT_MS, () -> {
                if (mask.getParent() != null) {
                    content.removeView(mask);
                }
            });
        });

        // show 前准备 view 级 Miuix 展开/收起动画（HyperCeiler 同款弹簧变形；
        // 窗口参数 show 时固化，背景透明化与窗口动画清零须在此之前完成）
        MiuixDropdownAnimator animator = MiuixDropdownAnimator.prepare(popup);
        if (animator != null) {
            // 收起动画启动瞬间同步淡出遮罩（与菜单收缩同步，对齐 HC 的 dim 时序）
            animator.setOnDismissStart(() -> fadeMask(mask, 0f, DIM_MASK_FADE_OUT_MS, null));
        }
        popup.show();
        // 长内容菜单越界回弹：把弹出的 ListView 包进竖直 SpringBackLayout
        // （非嵌套 target 走 touch 路径；每次 show 均新建 popup/ListView，无复用冲突）
        wrapListWithSpringBack(activity, popup);

        if (animator != null) {
            animator.onShown(popup);
        } else {
            Log.d("MiuixDrop", "helper: animator null -> static clip path");
            // 降级（反射失败等）：无 view 级动画，列表按固定 16dp 圆角裁剪绘制
            // （波纹等子项绘制一并裁进圆角，保持原行为）；圆角层挂外壳——否则
            // 越界滚动把内容移出列表底边后，圆角被列表直角 bounds 裁成直角
            ListView listView = popup.getListView();
            if (listView != null) {
                float radiusPx = CORNER_RADIUS_DP
                        * activity.getResources().getDisplayMetrics().density;
                View clipTarget = listView.getParent() instanceof SpringBackLayout spring
                        ? spring : listView;
                clipTarget.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
                    }
                });
                clipTarget.setClipToOutline(true);
            }
        }

        // 全组件系统平滑圆角：popup 为独立窗口，在其根视图挂持续遍历，
        // 覆盖菜单行（ListView 滚动时按需 inflate 新行）与浮层背景；
        // 每次 show 均新建 popup/ListView（调用方既有模式），防重入 tag 不会误锁
        ListView popupList = popup.getListView();
        if (popupList != null) {
            SmoothCornerTraverser.install(popupList.getRootView());
        }
    }

    /**
     * 把弹出的 ListView 包进竖直 SpringBackLayout：长菜单滚动到顶/底后再拖动获得
     * 越界回弹（含回弹振动）。包裹失败仅失去回弹，不影响弹出流程。
     * 菜单弹层的越界范围用中阻尼（覆写 getSpringBackRange）：菜单内条目先滚动，
     * 滚到头后橡皮筋上限 = range/3 ≈ 120dp，接近页面列表观感但不至像屏高/3 那样
     * 把整菜单大幅移位（早期“菜单本身在滚动”的观感问题即来自完全不限幅）。
     */
    private static void wrapListWithSpringBack(Activity activity, ListPopupWindow popup) {
        try {
            ListView listView = popup.getListView();
            if (listView == null || listView.getParent() instanceof SpringBackLayout
                    || !(listView.getParent() instanceof ViewGroup parent)) {
                return;
            }
            int index = parent.indexOfChild(listView);
            ViewGroup.LayoutParams lp = listView.getLayoutParams();
            if (index < 0 || lp == null) {
                return;
            }
            parent.removeView(listView);
            SpringBackLayout spring = new SpringBackLayout(activity) {
                @Override
                protected int getSpringBackRange(int dir) {
                    // 360dp 阻尼范围 → 最大越界 120dp（obtainDampingDistance(1)=range/3）
                    return DensityUtil.dpToPx(activity, 360);
                }
            };
            parent.addView(spring, index, lp);
            spring.addView(listView, new ViewGroup.LayoutParams(lp.width, lp.height));
            // 菜单列表 fling 撞顶/底也不硬停：滚动监听补一次撞界回弹（与 touch 路径同观感）
            spring.setEdgeFlingBounceEnable(true);
            if (BuildConfig.DEBUG) {
                Log.d("MiuixDrop", "wrapped listView parent=" + parent.getClass().getName()
                        + " index=" + index + " lp=" + lp.width + "x" + lp.height
                        + " listScrollable=" + listView.canScrollList(1));
            }
        } catch (Throwable t) {
            Log.d("MiuixDrop", "wrapListWithSpringBack failed", t);
        }
    }

    /**
     * 遮罩 alpha 过渡：必须用 Folme 定时驱动而非 ViewPropertyAnimator——后者受开发者选项
     * "Animator 时长缩放"影响（10x 下淡入 300ms 拖成 3s，展开观感被放慢），
     * 而 HC 的 dim 与菜单动画同为自计时引擎驱动、不受该缩放影响（菜单几何同走 Folme）。
     *
     * @param onSettled 结束/取消回调（均可触发，调用方自行保证幂等）；可为 null
     */
    private static void fadeMask(View mask, float targetAlpha, long durationMs, Runnable onSettled) {
        AnimConfig config = new AnimConfig()
                .setEase(EaseManager.getStyle(1, (int) durationMs));
        if (onSettled != null) {
            config.addListeners(new TransitionListener() {
                @Override
                public void onComplete(Object o) {
                    onSettled.run();
                }

                @Override
                public void onCancel(Object o) {
                    onSettled.run();
                }
            });
        }
        Folme.use(mask).to(ViewProperty.ALPHA, targetAlpha, config);
    }

    /**
     * 解析主题属性（?attr/xxx）对应的具体色值；
     * 每次调用均从当前主题取值，深浅色重建后自然生效
     */
    private static int resolveThemeColor(Context context, int attr) {
        TypedValue value = new TypedValue();
        return context.getTheme().resolveAttribute(attr, value, true) ? value.data : 0;
    }

    /**
     * 构造下拉菜单通用选项行 adapter（纯文字/纯图片/图片+文字三种行布局通用）：
     * 图片行写入条目图片资源（纯图片行以条目文本作无障碍描述），文字行写入条目文本，
     * 且当前选中项文字以 colorPrimary 高亮（与右侧√同色），仅当前选中项右侧显示√
     *
     * @param layoutId  行布局（须含 option_text/option_check，图片行另含 option_image）
     * @param drawables 图片行各条目对应的图片资源（纯文字行为 null）
     */
    @NonNull
    public static ArrayAdapter<String> createAdapter(Activity activity, int selectedIndex,
                                                     String[] labels, int layoutId, int[] drawables) {
        return createAdapter(activity, selectedIndex, labels, layoutId, drawables, null, null);
    }

    /**
     * 多图预览行 adapter（标题 + N 个预览位 + √，供强卡概率计算器的预设组合菜单使用）：
     * 在通用文字/√渲染（标题 colorPrimary 高亮、仅生效项显示√）基础上，
     * 按 previewImageIds 顺序把 previewDrawables[position] 各预览位图片写入行内对应 ImageView
     *
     * @param previewImageIds  行内各预览位 view id（按预览顺序，与行布局声明顺序一致）
     * @param previewDrawables 每个条目各预览位的图片资源（与 previewImageIds 一一对应）
     */
    @NonNull
    public static ArrayAdapter<String> createAdapter(Activity activity, int selectedIndex,
                                                     String[] labels, int layoutId, int[] drawables,
                                                     int[] previewImageIds, int[][] previewDrawables) {
        final int checkedIndex = selectedIndex;
        // 选中项文字与√同用 colorPrimary，未选中恢复行布局声明的 colorOnSurface
        // （非传递 R 类下 Material 的 attr 需完全限定引用，与项目其他处一致）
        final int checkedTextColor = resolveThemeColor(activity,
                com.google.android.material.R.attr.colorPrimary);
        final int normalTextColor = resolveThemeColor(activity,
                com.google.android.material.R.attr.colorOnSurface);
        return new ArrayAdapter<>(activity, layoutId, labels) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView != null ? convertView
                        : activity.getLayoutInflater().inflate(layoutId, parent, false);
                TextView optionText = row.findViewById(R.id.option_text);
                if (optionText != null) {
                    optionText.setText(getItem(position));
                    // 行视图会被复用，两种颜色都要按当前选中态写入
                    optionText.setTextColor(position == checkedIndex
                            ? checkedTextColor : normalTextColor);
                }
                ImageView optionImage = row.findViewById(R.id.option_image);
                if (optionImage != null && drawables != null) {
                    optionImage.setImageResource(drawables[position]);
                    if (optionText == null) {
                        optionImage.setContentDescription(getItem(position));
                    }
                }
                // 多图预览行：按 previewImageIds 顺序写入本条目各预览位图片（行视图会被复用，需每次全量写入）
                if (previewImageIds != null && previewDrawables != null
                        && position < previewDrawables.length) {
                    int[] previews = previewDrawables[position];
                    if (previews != null) {
                        for (int i = 0; i < previewImageIds.length && i < previews.length; i++) {
                            ImageView preview = row.findViewById(previewImageIds[i]);
                            if (preview != null) {
                                preview.setImageResource(previews[i]);
                            }
                        }
                    }
                }
                // 仅当前选中项右侧显示对勾，其余隐藏但保留占位（INVISIBLE 而非 GONE）：
                // 对勾列宽度恒定，预设预览行的右对齐列（预览图）不会随对勾显隐左右错位，
                // 左对齐的纯文字/纯图片行则完全不受影响
                row.findViewById(R.id.option_check)
                        .setVisibility(position == checkedIndex ? View.VISIBLE : View.INVISIBLE);
                return row;
            }
        };
    }

    /**
     * 以 anchor 就地弹出下拉菜单（弹出流程公用实现）：
     * 1. 实测全部选项行——测量时统一强制显示√，菜单宽恒为“最长条目+对勾”，不随选中项变化，
     *    再按屏幕宽钳制；行高用于限制菜单总高；
     * 2. 四分支展开方向（下足向下/上足上翻/都不够取空间大侧收缩至少两行）；
     * 3. 横向固定对齐容器右缘（菜单右缘 = 容器右缘，从容器右下角（向下弹）/右上角
     *    （向上弹）固定展示，不随手指按压位置变化）；
     * 4. 打开时列表定位到当前选中项（居中显示）；
     * 5. 弹出（附带灰遮罩+波纹圆角裁切）并隐藏滚动条，点击行执行 onMenuItemClick 后自动关闭；
     * 6. 弹出菜单时与点击条目时各触发一次设备振动反馈（CONFIRM，与设置页既有交互反馈一致）。
     * 须在 adapter 构造完成之后调用。
     *
     * @param onMenuItemClick         行点击回调（菜单关闭由本方法统一处理）
     */
    public static void showAnchored(Activity activity, View anchor, ArrayAdapter<String> adapter,
                                    int itemCount, int selectedMenuIndex,
                                    Consumer<Integer> onMenuItemClick) {
        // 点击弹出菜单时先触发一次振动反馈（与点击条目时的振动一致）
        anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        // 实测选项宽高：ListView 在 PopupWindow 中无法真正 wrap_content，需自行测量内容宽后按像素设置。
        // getView 的 parent 形参标注 @NonNull：这里传一个仅用于生成 LayoutParams 的空容器（不挂载子视图），避免传 null
        ViewGroup measureParent = new FrameLayout(activity);
        int contentWidth = 0;
        int itemHeight = 0;
        for (int i = 0; i < itemCount; i++) {
            View itemView = adapter.getView(i, null, measureParent);
            // √仅选中行可见时，各行实测宽会随选中项变化（选中行带√、其余不带）：
            // 测量时统一强制显示√（宽度固定），使菜单宽恒为“最长条目+对勾”，
            // 选中短条目或长条目都不再改变菜单宽度；此行为仅用于测量，不反馈给真实列表
            itemView.findViewById(R.id.option_check).setVisibility(View.VISIBLE);
            itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            itemHeight = itemView.getMeasuredHeight();
        }
        // 极端大字体下限制菜单宽度不超出屏幕
        contentWidth = Math.min(contentWidth, anchor.getRootView().getWidth());

        // 展开方向与高度：优先向下全量展开；下方空间不足时向上翻转（负 offset，菜单底缘停在锚点上方）；
        // 上下都放不下时取空间大的一侧并收缩高度（超出部分列表内滚动，至少保留两行）
        int gap = DensityUtil.dpToPx(activity, 4);
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        int fullHeight = itemHeight * itemCount;
        int spaceBelow = anchor.getRootView().getHeight() - location[1] - anchor.getHeight() - gap;
        int spaceAbove = location[1] - gap;
        int verticalOffset = gap;
        int popupHeight;
        if (spaceBelow >= fullHeight) {
            // 下方足够：向下展开
            popupHeight = fullHeight;
        } else if (spaceAbove >= fullHeight) {
            // 下方不够、上方足够：整体上移，菜单底缘停在锚点上方
            popupHeight = fullHeight;
            verticalOffset = -(anchor.getHeight() + fullHeight + gap);
        } else if (spaceAbove > spaceBelow) {
            // 上下都不够、上方空间更大：向上展开并收缩到可用高度
            popupHeight = Math.max(spaceAbove, itemHeight * 2);
            verticalOffset = -(anchor.getHeight() + popupHeight + gap);
        } else {
            // 上下都不够、下方空间更大：向下展开并收缩到可用高度
            popupHeight = Math.max(spaceBelow, itemHeight * 2);
        }

        // 横向固定对齐容器右缘：菜单右缘 = 容器右缘，弹窗从容器右下角（向下弹）/
        // 右上角（向上弹）固定展示，不随手指按压位置变化；
        // 再钳进屏幕左右边界，防窄容器靠边时菜单越出屏幕
        int horizontalOffset = anchor.getWidth() - contentWidth;
        horizontalOffset = Math.max(-location[0],
                Math.min(horizontalOffset, anchor.getRootView().getWidth() - contentWidth - location[0]));

        // 列表高于可视区时打开即定位当前选中项：取选中项居中时的首行下标
        // （可视行数按已算出的弹出高度折算；全部可见时折算结果为 0，不产生滚动）
        int visibleCount = Math.max(1, popupHeight / Math.max(1, itemHeight));
        int firstVisible = Math.max(0, Math.min(selectedMenuIndex - visibleCount / 2,
                itemCount - visibleCount));

        ListPopupWindow popup = new ListPopupWindow(activity);
        popup.setAnchorView(anchor);
        popup.setWidth(contentWidth);
        popup.setHeight(popupHeight);
        popup.setVerticalOffset(verticalOffset);
        popup.setHorizontalOffset(horizontalOffset);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.popup_dropdown_background));
        popup.setAdapter(adapter);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            // 点击条目时触发一次振动反馈，随后执行回调并关闭菜单
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            onMenuItemClick.accept(position);
            MiuixDropdownAnimator.dismissAnimated(popup);
        });

        // 弹出菜单（附带全屏灰色遮罩 + 首末条目波纹裁进浮层圆角），随后隐藏滚动条
        // （选中态由 adapter 渲染右侧对勾，无需 ListView 单选模式）
        show(activity, popup);
        ListView listView = popup.getListView();
        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
            // 长列表打开时滚动到当前选中项（选中靠后条目再次打开时可见）
            listView.setSelection(firstVisible);
        }
    }
}
