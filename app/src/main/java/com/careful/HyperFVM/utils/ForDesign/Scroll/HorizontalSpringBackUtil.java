package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * 把布局内全部 HorizontalScrollView 程序化包进水平 SpringBackLayout（越界回弹）。
 * HorizontalScrollView 非 NestedScrollingChild，只能走 SpringBackLayout 的 touch 路径；
 * 包裹保持原 LayoutParams 宽高与位置（测量/视觉零变化），且幂等（已包裹的跳过），
 * 两套布局（普通/_effect）通用，无需改动布局 XML。
 */
public final class HorizontalSpringBackUtil {

    private HorizontalSpringBackUtil() {
    }

    /**
     * 收集 root 下全部 HorizontalScrollView 并逐个包裹（横向滚动区内不再嵌套横向回弹）。
     */
    public static void wrapHorizontalScrollViews(View root) {
        List<HorizontalScrollView> found = new ArrayList<>();
        collectHorizontalScrollViews(root, found);
        for (HorizontalScrollView hs : found) {
            wrapHorizontalScrollView(hs);
        }
    }

    private static void collectHorizontalScrollViews(View view, List<HorizontalScrollView> out) {
        if (view instanceof HorizontalScrollView hsv) {
            out.add(hsv);
            return; // 横向滚动区内不再嵌套横向回弹
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                collectHorizontalScrollViews(group.getChildAt(i), out);
            }
        }
    }

    private static void wrapHorizontalScrollView(HorizontalScrollView hs) {
        if (hs.getParent() instanceof SpringBackLayout) {
            return; // 已包裹（幂等）
        }
        if (!(hs.getParent() instanceof ViewGroup parent)) {
            return;
        }
        int index = parent.indexOfChild(hs);
        ViewGroup.LayoutParams lp = hs.getLayoutParams();
        if (index < 0 || lp == null) {
            return;
        }
        parent.removeView(hs);
        SpringBackLayout spring = new SpringBackLayout(parent.getContext());
        spring.setScrollOrientation(SpringBackLayout.DIRECTION_HORIZONTAL);
        // fling 惯性撞边界不经 touch 路径（HSV 硬停无回弹），靠滚动监听补一次撞界回弹
        spring.setEdgeFlingBounceEnable(true);
        parent.addView(spring, index, lp);
        spring.addView(hs, new ViewGroup.LayoutParams(lp.width, lp.height));
    }
}
