package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

/**
 * 顶部栏滚动联动工具类：
 * 内容区内的大标题（topBarBottom）随内容上滑逐渐淡出，
 * 悬浮在滚动容器外的小标题（topBar）与模糊背景层（blurViewTopBar）逐渐淡入，
 * 滚动前 fadeRangeDp 个 dp 内完成全部渐变。
 * <p>
 * 用法示例（Fragment）：
 * 在 onCreateView 中：
 *     nestedScrollUtil = NestedScrollUtil.attach(root,
 *             R.id.topBarBottom, R.id.topBar, R.id.blurViewTopBar);
 * 配合界面重建后的状态保持（深浅色切换等），在生命周期回调中各加一行：
 *     onSaveInstanceState 中：nestedScrollUtil.saveScrollY(outState, "key");
 *     onViewStateRestored 中：nestedScrollUtil.restoreScrollY(savedInstanceState, "key");
 */
public class NestedScrollUtil {
    /** 默认渐变过渡区间（dp）：上滑多少dp内完成全部渐变 */
    private static final int DEFAULT_FADE_RANGE_DP = 50;

    private final View scrollContainer;   // ScrollView 或 NestedScrollView
    private final View topBarBottom;      // 滚动容器内的大标题
    private final View topBar;            // 悬浮的小标题
    private final View blurViewTopBar;    // 悬浮的模糊背景层
    private final int fadeRangePx;        // 渐变过渡区间（px）

    private NestedScrollUtil(View scrollContainer, View topBarBottom, View topBar,
                             View blurViewTopBar, int fadeRangePx) {
        this.scrollContainer = scrollContainer;
        this.topBarBottom = topBarBottom;
        this.topBar = topBar;
        this.blurViewTopBar = blurViewTopBar;
        this.fadeRangePx = fadeRangePx;
    }

    /**
     * 为Activity启用顶部栏滚动联动（默认过渡区间）
     */
    public static NestedScrollUtil attach(Activity activity, @IdRes int topBarBottomId,
                                          @IdRes int topBarId, @IdRes int blurViewTopBarId) {
        return attach(activity.getWindow().getDecorView(), topBarBottomId, topBarId,
                blurViewTopBarId, DEFAULT_FADE_RANGE_DP);
    }

    /**
     * 为根视图启用顶部栏滚动联动（默认过渡区间）
     *
     * @param root             根视图（Fragment的root或Activity的内容视图）
     * @param topBarBottomId   滚动容器内大标题的id
     * @param topBarId         悬浮小标题的id
     * @param blurViewTopBarId 悬浮模糊背景层的id
     * @return NestedScrollUtil 实例，需持有用于配合状态保存/恢复
     */
    public static NestedScrollUtil attach(View root, @IdRes int topBarBottomId,
                                          @IdRes int topBarId, @IdRes int blurViewTopBarId) {
        return attach(root, topBarBottomId, topBarId, blurViewTopBarId, DEFAULT_FADE_RANGE_DP);
    }

    /**
     * 为根视图启用顶部栏滚动联动（自定义过渡区间）
     *
     * @param fadeRangeDp 渐变过渡区间（dp），滚动该距离后顶栏完全显现
     */
    public static NestedScrollUtil attach(View root, @IdRes int topBarBottomId,
                                          @IdRes int topBarId, @IdRes int blurViewTopBarId,
                                          int fadeRangeDp) {
        if (root == null) {
            throw new IllegalArgumentException("root 不能为 null");
        }
        View topBarBottom = root.findViewById(topBarBottomId);
        View topBar = root.findViewById(topBarId);
        View blurViewTopBar = root.findViewById(blurViewTopBarId);
        if (topBarBottom == null || topBar == null || blurViewTopBar == null) {
            throw new IllegalArgumentException("找不到联动组件，请检查传入的id："
                    + topBarBottomId + ", " + topBarId + ", " + blurViewTopBarId);
        }
        View scrollContainer = findScrollContainer(topBarBottom);
        if (scrollContainer == null) {
            throw new IllegalArgumentException("从 topBarBottom(" + topBarBottomId
                    + ") 的父级向上未找到 ScrollView 或 NestedScrollView");
        }
        NestedScrollUtil util = new NestedScrollUtil(scrollContainer, topBarBottom, topBar,
                blurViewTopBar, DensityUtil.dpToPx(root.getContext(), fadeRangeDp));
        // 同步初始透明度（未滚动：大标题全显、顶栏全透明）
        util.syncAlpha();
        // 滚动联动
        scrollContainer.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> util.syncAlpha());
        return util;
    }

    /**
     * 按当前滚动位置同步三者的透明度：
     * 滑动0~fadeRangeDp的过程中，topBarBottom透明度由1渐变为0，
     * topBar与blurViewTopBar透明度由0渐变为1
     */
    public void syncAlpha() {
        float progress = Math.min(1f, scrollContainer.getScrollY() / (float) fadeRangePx);
        topBarBottom.setAlpha(1f - progress);
        blurViewTopBar.setAlpha(progress);
        topBar.setAlpha(progress);
    }

    /**
     * 保存当前滚动位置，供界面重建（旋转/深浅色切换等）后恢复透明度状态。
     * 请在宿主的 onSaveInstanceState 中调用
     */
    public void saveScrollY(@NonNull Bundle outState, @NonNull String key) {
        outState.putInt(key, scrollContainer.getScrollY());
    }

    /**
     * 恢复滚动位置并同步透明度。请在宿主的 onViewStateRestored 中调用：
     * 系统恢复滚动位置发生在该回调之后、首帧绘制之前，
     * 因此这里会注册一次性预绘制监听，待滚动位置最终确定后再同步透明度，
     * 避免重建后透明度停留在初始状态、直到用户滚动才突变回正确状态
     */
    public void restoreScrollY(@Nullable Bundle savedInstanceState, @NonNull String key) {
        if (savedInstanceState == null) return;
        // 兜底：若系统未恢复滚动位置，则按上次保存值显式恢复
        int savedScrollY = savedInstanceState.getInt(key, -1);
        if (savedScrollY > 0) {
            scrollContainer.setScrollY(savedScrollY);
        }
        scrollContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                scrollContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                syncAlpha();
                return true;
            }
        });
    }

    /**
     * 从 topBarBottom 的父级链向上查找滚动容器（兼容 ScrollView 与 NestedScrollView）
     */
    private static View findScrollContainer(View child) {
        ViewParent parent = child.getParent();
        while (parent instanceof View view) {
            if (view instanceof android.widget.ScrollView || view instanceof NestedScrollView) {
                return view;
            }
            parent = view.getParent();
        }
        return null;
    }
}
