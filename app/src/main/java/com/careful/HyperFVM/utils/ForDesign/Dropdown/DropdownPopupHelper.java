package com.careful.HyperFVM.utils.ForDesign.Dropdown;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ListView;

import androidx.appcompat.widget.ListPopupWindow;

/**
 * 下拉菜单（ListPopupWindow + popup_dropdown_background 浮层）通用增强：
 * 1. 菜单弹出时给菜单以外的屏幕区域加深灰色遮罩，突出菜单主体（菜单为独立窗口，
 *    天然层叠在遮罩之上）；菜单关闭时遮罩自动移除。
 * 2. 把列表绘制（含首/末条目的按压波纹）裁进浮层圆角，防止波纹溢出 16dp 圆角。
 * 供 SettingsActivity / CookeryIndexActivity / OddsDisclosureIndexActivity 复用。
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
}
