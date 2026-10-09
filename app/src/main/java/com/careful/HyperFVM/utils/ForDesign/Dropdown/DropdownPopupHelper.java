package com.careful.HyperFVM.utils.ForDesign.Dropdown;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Outline;
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

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

import java.util.function.Consumer;

/**
 * 下拉菜单（ListPopupWindow + popup_dropdown_background 浮层）通用增强：
 * 1. 菜单弹出时给菜单以外的屏幕区域加深灰色遮罩，突出菜单主体（菜单为独立窗口，
 *    天然层叠在遮罩之上）；菜单关闭时遮罩自动移除。
 * 2. 把列表绘制（含首/末条目的按压波纹）裁进浮层圆角，防止波纹溢出 16dp 圆角。
 * 供 SettingsActivity / CookeryIndexActivity / OddsDisclosureIndexActivity 复用。
 * 另提供 createAdapter/showAnchored 公用构建：选项行渲染（文字高亮/图片/√）与就地弹出
 * 全流程（实测宽度恒为“最长条目+对勾”、四分支展开、手指跟随、居中定位选中项），
 * 供 SettingsActivity（纯文本菜单）与 CardOddsCalculatorActivity（图片/图片+文字菜单）复用。
 */
public final class DropdownPopupHelper {

    // 与 drawable/popup_dropdown_background.xml 的 corners radius 保持一致
    private static final int CORNER_RADIUS_DP = 16;
    // 遮罩压暗程度（黑 40% 透明度）
    private static final int DIM_MASK_COLOR = 0x66000000;
    // 遮罩淡入时长
    private static final long DIM_MASK_FADE_IN_MS = 180;
    // 遮罩淡出时长（菜单关闭后遮罩再淡出消失，避免瞬间闪没）
    private static final long DIM_MASK_FADE_OUT_MS = 180;

    private DropdownPopupHelper() {
    }

    /**
     * 弹出下拉菜单并附加遮罩与圆角裁切。须在 setAdapter/setOnItemClickListener 等
     * 参数全部设置完成之后调用（本方法内部负责 show，调用方不要再调 popup.show()）。
     * 注意：会覆盖调用方已设置的 OnDismissListener（当前三处调用方均未设置）。
     */
    public static void show(Activity activity, ListPopupWindow popup) {
        // 遮罩挂在 content 上：随 Activity 销毁自动回收，且不遮挡系统栏图标
        ViewGroup content = activity.findViewById(android.R.id.content);
        View mask = new View(activity);
        mask.setBackgroundColor(DIM_MASK_COLOR);
        // modal 模式下触摸由 popup 窗口拦截，此点击兜底仅在非 modal 时防止误触底下控件
        mask.setClickable(true);
        mask.setAlpha(0f);
        content.addView(mask, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mask.animate().alpha(1f).setDuration(DIM_MASK_FADE_IN_MS).start();
        // 菜单关闭时遮罩先淡出再移除（此时 popup 窗口已关闭，遮罩短暂残留于底层之上无视觉冲突）。
        // 用 onAnimationEnd 而非 withEndAction：动画被取消时前者仍会回调，
        // 避免残留一个透明但可点击的遮罩拦截页面触摸
        popup.setOnDismissListener(() -> mask.animate()
                .alpha(0f)
                .setDuration(DIM_MASK_FADE_OUT_MS)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mask.getParent() != null) {
                            content.removeView(mask);
                        }
                    }
                })
                .start());

        popup.show();

        // 列表按 16dp 圆角裁剪绘制（波纹等子项绘制一并裁进圆角）
        ListView listView = popup.getListView();
        if (listView != null) {
            float radiusPx = CORNER_RADIUS_DP
                    * activity.getResources().getDisplayMetrics().density;
            listView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
                }
            });
            listView.setClipToOutline(true);
        }
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
                // 仅当前选中项右侧显示对勾，其余不显示
                row.findViewById(R.id.option_check)
                        .setVisibility(position == checkedIndex ? View.VISIBLE : View.GONE);
                return row;
            }
        };
    }

    /**
     * 以 anchor 就地弹出下拉菜单（弹出流程公用实现）：
     * 1. 实测全部选项行——测量时统一强制显示√，菜单宽恒为“最长条目+对勾”，不随选中项变化，
     *    再按屏幕宽钳制；行高用于限制菜单总高；
     * 2. 四分支展开方向（下足向下/上足上翻/都不够取空间大侧收缩至少两行）；
     * 3. 横向跟随手指按下的位置弹出，右缘与页面内容右缘对齐；
     * 4. 打开时列表定位到当前选中项（居中显示）；
     * 5. 弹出（附带灰遮罩+波纹圆角裁切）并隐藏滚动条，点击行执行 onMenuItemClick 后自动关闭；
     * 6. 弹出菜单时与点击条目时各触发一次设备振动反馈（CONFIRM，与设置页既有交互反馈一致）。
     * 须在 adapter 构造完成之后调用。
     *
     * @param lastTouchDownX          anchor 上一次 ACTION_DOWN 的横向位置（bindRowTouch 记录，-1 表示无）
     * @param pageContentSideMarginPx 页面内容区侧边距，用于菜单右缘对齐
     * @param onMenuItemClick         行点击回调（菜单关闭由本方法统一处理）
     */
    public static void showAnchored(Activity activity, View anchor, ArrayAdapter<String> adapter,
                                    int itemCount, int selectedMenuIndex, float lastTouchDownX,
                                    int pageContentSideMarginPx, Consumer<Integer> onMenuItemClick) {
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

        // 横向弹出位置跟随手指按下的位置；最右不得超过屏幕右缘减页面边距，使菜单右缘与页面内容右缘对齐
        int horizontalOffset = 0;
        if (lastTouchDownX >= 0) {
            int maxOffset = anchor.getRootView().getWidth() - contentWidth
                    - location[0] - pageContentSideMarginPx;
            horizontalOffset = Math.max(0, Math.min((int) lastTouchDownX, maxOffset));
        }

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
            popup.dismiss();
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
