package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.os.Bundle;
import android.view.View;
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
 * 三个联动组件均可缺省（id传0或View传null）：缺少哪个组件就只执行其余组件的联动，
 * 便于不同页面按自身布局组合使用。
 * 渐变区间 fadeRangeDp 无内置默认值，需按各界面实际效果传入（如50dp）。
 * <p>
 * 若页面另有自己的滚动效果（如内容元素渐隐），须通过带 pageScrollListener 的 attach 重载
 * 一并传入，由本类与顶部栏联动共用同一滚动监听（View 的 setOnScrollChangeListener 是
 * 覆盖语义，页面不能各自注册），重建恢复滚动位置后也会一并触发该回调。
 * <p>
 * 用法示例（Fragment）：
 * 在 onCreateView 中：
 *     nestedScrollUtil = NestedScrollUtil.attach(root,
 *             R.id.scrollView,          // 滚动容器id（必传）
 *             R.id.topBarBottom,        // 大标题id（无则传0）
 *             R.id.topBar,              // 小标题id（无则传0）
 *             R.id.blurViewTopBar,      // 模糊背景层id（无则传0）
 *             50);                      // 渐变过渡区间dp，按界面自定
 * 配合界面重建后的状态保持（深浅色切换等），在生命周期回调中各加一行：
 *     onSaveInstanceState 中：nestedScrollUtil.saveScrollY(outState, "key");
 *     onViewStateRestored 中：nestedScrollUtil.restoreScrollY(savedInstanceState, "key");
 */
public class NestedScrollUtil {
    private final View scrollContainer;   // ScrollView 或 NestedScrollView
    private final View topBarBottom;      // 滚动容器内的大标题（可为null）
    private final View topBar;            // 悬浮的小标题（可为null）
    private final View blurViewTopBar;    // 悬浮的模糊背景层（可为null）
    private final int fadeRangePx;        // 渐变过渡区间（px）
    private final View.OnScrollChangeListener pageScrollListener; // 页面附加的滚动效果回调（可为null）

    private NestedScrollUtil(View scrollContainer, View topBarBottom, View topBar,
                             View blurViewTopBar, int fadeRangePx,
                             @Nullable View.OnScrollChangeListener pageScrollListener) {
        this.scrollContainer = scrollContainer;
        this.topBarBottom = topBarBottom;
        this.topBar = topBar;
        this.blurViewTopBar = blurViewTopBar;
        this.fadeRangePx = fadeRangePx;
        this.pageScrollListener = pageScrollListener;
    }

    /**
     * 为根视图启用顶部栏滚动联动（显式指定滚动容器与渐变区间）
     *
     * @param root              根视图（Fragment的root或Activity的内容视图）
     * @param scrollContainerId 滚动容器的id（必传，须为 ScrollView/NestedScrollView）
     * @param topBarBottomId    滚动容器内大标题的id，无该组件则传0
     * @param topBarId          悬浮小标题的id，无该组件则传0
     * @param blurViewTopBarId  悬浮模糊背景层的id，无该组件则传0
     * @param fadeRangeDp       渐变过渡区间（dp），滚动该距离后顶栏完全显现，按界面自定
     * @return NestedScrollUtil 实例，需持有用于配合状态保存/恢复
     */
    public static NestedScrollUtil attach(View root, @IdRes int scrollContainerId,
                                          @IdRes int topBarBottomId, @IdRes int topBarId,
                                          @IdRes int blurViewTopBarId, int fadeRangeDp) {
        return attach(root, scrollContainerId, topBarBottomId, topBarId, blurViewTopBarId,
                fadeRangeDp, null);
    }

    /**
     * 带页面附加滚动回调的版本：当页面还有自己的滚动效果（如内容元素渐隐）时，
     * 传入回调与本工具类共用同一个滚动监听（View 的滚动监听是覆盖语义，不能各自注册），
     * 滚动位置变化及界面重建恢复时都会回调
     *
     * @param pageScrollListener 页面附加滚动回调，可传null
     */
    public static NestedScrollUtil attach(View root, @IdRes int scrollContainerId,
                                          @IdRes int topBarBottomId, @IdRes int topBarId,
                                          @IdRes int blurViewTopBarId, int fadeRangeDp,
                                          @Nullable View.OnScrollChangeListener pageScrollListener) {
        if (root == null) {
            throw new IllegalArgumentException("root 不能为 null");
        }
        if (scrollContainerId == 0) {
            throw new IllegalArgumentException("scrollContainerId 不能为0，请传入滚动容器的id");
        }
        View scrollContainer = root.findViewById(scrollContainerId);
        if (scrollContainer == null) {
            throw new IllegalArgumentException("找不到滚动容器，请检查id：" + scrollContainerId);
        }
        return attach(scrollContainer,
                findViewOrThrow(root, topBarBottomId, "topBarBottom"),
                findViewOrThrow(root, topBarId, "topBar"),
                findViewOrThrow(root, blurViewTopBarId, "blurViewTopBar"),
                fadeRangeDp, pageScrollListener);
    }

    /**
     * 为滚动容器启用顶部栏滚动联动（直接传入组件View）
     *
     * @param scrollContainer 滚动容器，须为 ScrollView/NestedScrollView
     * @param topBarBottom    滚动容器内的大标题，可为null
     * @param topBar          悬浮的小标题，可为null
     * @param blurViewTopBar  悬浮的模糊背景层，可为null
     * @param fadeRangeDp     渐变过渡区间（dp），滚动该距离后顶栏完全显现，按界面自定
     * @return NestedScrollUtil 实例，需持有用于配合状态保存/恢复
     */
    public static NestedScrollUtil attach(View scrollContainer, @Nullable View topBarBottom,
                                          @Nullable View topBar, @Nullable View blurViewTopBar,
                                          int fadeRangeDp) {
        return attach(scrollContainer, topBarBottom, topBar, blurViewTopBar, fadeRangeDp, null);
    }

    /**
     * 带页面附加滚动回调的版本（核心实现）：内置透明度联动与页面附加效果共用同一滚动监听，
     * 避免各自 setOnScrollChangeListener 互相覆盖；重建恢复滚动位置后回调也会一并触发
     *
     * @param pageScrollListener 页面附加滚动回调，可传null
     */
    public static NestedScrollUtil attach(View scrollContainer, @Nullable View topBarBottom,
                                          @Nullable View topBar, @Nullable View blurViewTopBar,
                                          int fadeRangeDp,
                                          @Nullable View.OnScrollChangeListener pageScrollListener) {
        if (scrollContainer == null) {
            throw new IllegalArgumentException("scrollContainer 不能为 null");
        }
        if (!isScrollContainer(scrollContainer)) {
            throw new IllegalArgumentException("scrollContainer 必须是 ScrollView 或 NestedScrollView");
        }
        if (fadeRangeDp <= 0) {
            throw new IllegalArgumentException("fadeRangeDp 必须大于0，当前：" + fadeRangeDp);
        }
        NestedScrollUtil util = new NestedScrollUtil(scrollContainer, topBarBottom, topBar,
                blurViewTopBar, DensityUtil.dpToPx(scrollContainer.getContext(), fadeRangeDp),
                pageScrollListener);
        // 同步初始透明度（未滚动：大标题全显、顶栏全透明）
        util.syncAlpha();
        // 滚动联动：内置透明度渐变与页面附加效果共用同一监听，避免覆盖彼此
        scrollContainer.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            util.syncAlpha();
            if (pageScrollListener != null) {
                pageScrollListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
            }
        });
        return util;
    }

    /**
     * 按当前滚动位置同步透明度：
     * 滑动0~fadeRangeDp的过程中，topBarBottom透明度由1渐变为0，
     * topBar与blurViewTopBar透明度由0渐变为1；缺失的组件自动跳过
     */
    public void syncAlpha() {
        float progress = Math.min(1f, scrollContainer.getScrollY() / (float) fadeRangePx);
        if (topBarBottom != null) {
            topBarBottom.setAlpha(1f - progress);
        }
        if (blurViewTopBar != null) {
            blurViewTopBar.setAlpha(progress);
        }
        if (topBar != null) {
            topBar.setAlpha(progress);
        }
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
                // 滚动位置恢复后，一并同步页面附加的滚动效果（如内容元素渐隐）
                if (pageScrollListener != null) {
                    pageScrollListener.onScrollChange(scrollContainer,
                            scrollContainer.getScrollX(), scrollContainer.getScrollY(),
                            scrollContainer.getScrollX(), scrollContainer.getScrollY());
                }
                return true;
            }
        });
    }

    /**
     * id为0表示"页面无此组件"，返回null跳过联动；非0却找不到则说明id配错，直接抛出
     */
    private static View findViewOrThrow(View root, int id, String componentName) {
        if (id == 0) {
            return null;
        }
        View view = root.findViewById(id);
        if (view == null) {
            throw new IllegalArgumentException("找不到联动组件 " + componentName + "，请检查id：" + id
                    + "（若该页面确实没有此组件，请传0）");
        }
        return view;
    }

    private static boolean isScrollContainer(View view) {
        return view instanceof android.widget.ScrollView || view instanceof NestedScrollView;
    }
}
