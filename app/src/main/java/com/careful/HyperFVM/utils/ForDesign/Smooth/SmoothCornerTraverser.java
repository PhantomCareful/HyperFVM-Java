package com.careful.HyperFVM.utils.ForDesign.Smooth;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.careful.HyperFVM.R;

import fan.smooth.SmoothCornerHelper;

/**
 * 视图树递归遍历开启系统平滑圆角（HyperOS squircle）的通用工具。
 * <p>
 * 背景：全局开关实验已证伪——本机 framework 的 android.view.View 不存在
 * sAppSmoothCornerEnabled 静态字段，系统仅提供 per-view 粒度的
 * setSmoothCornerEnabled（View/Drawable 各一个），故只能逐视图开启。
 * <p>
 * 四个统一入口接入：
 * <ul>
 *   <li>{@code BaseActivity.onCreate}——全部 Activity 及挂入其 decor 的 fan BottomSheet；</li>
 *   <li>{@code DialogCornerRadiusUtil.apply}——全部弹窗（独立窗口）；</li>
 *   <li>{@code MiuixBottomSheetModal.show}——BottomSheet 子树兜底（与上一条有重叠，幂等无害）；</li>
 *   <li>{@code DropdownPopupHelper.show}——全部下拉菜单（ListPopupWindow 独立窗口）。</li>
 * </ul>
 * fan 自家组件（HyperCardView、DialogParentPanel 等）已内置 smooth，重复开启幂等无害。
 * <p>
 * 实现要点：
 * <ul>
 *   <li><b>绘制前扫描</b>（OnPreDrawListener）：用户"能看到"的任何视图/背景变化必然伴随重绘，
 *       绘制前补齐即可保证首帧生效；空闲时不触发任何开销，滚动/动画期间每次绘制仅做 O(n)
 *       的 tag 读取，只有新增部分走反射；</li>
 *   <li><b>view 级 + 背景 drawable 级双开</b>（对齐 fan HyperCardView / DialogCornerRadiusUtil
 *       既有真机验证做法）；</li>
 *   <li><b>幂等</b>：每视图首次处理后打 keyed tag 跳过反射；tag 同时记录上一次的背景实例，
 *       背景被替换（如主题切换、DialogCornerRadiusUtil 换背景）时对新实例补开；</li>
 *   <li><b>零开销降级</b>：非支持设备（persist.sys.support_view_smoothcorner=false）
 *       install 直接返回，行为与接入前完全一致（普通圆角）。</li>
 * </ul>
 */
public final class SmoothCornerTraverser {

    /** 状态 tag 哨兵值：该 view 当前无背景（区别于 getTag 返回 null 的"尚未处理"） */
    private static final Object NO_BACKGROUND = new Object();

    private SmoothCornerTraverser() {
    }

    /**
     * 在根视图上安装持续遍历：立即扫一次，并挂绘制前监听覆盖此后所有动态变化
     * （新增子视图、RecyclerView 回收复用行、被替换的背景）。
     * 对同一根重复调用幂等；非支持设备/空参静默跳过。
     *
     * @param root 窗口根视图（Activity decor、Dialog decor、Popup 根或 BottomSheet 容器）
     */
    public static void install(View root) {
        if (root == null || !SmoothCornerHelper.IS_SUPPORT_SMOOTH_CORNER) {
            return;
        }
        if (root.getTag(R.id.smooth_corner_installed) != null) {
            return;
        }
        root.setTag(R.id.smooth_corner_installed, Boolean.TRUE);
        // 未 attach 时拿到的是浮动 observer，attach 时自动并入窗口 observer，监听不丢失
        root.getViewTreeObserver().addOnPreDrawListener(() -> {
            sweep(root);
            return true;
        });
        sweep(root);
    }

    /** 递归处理整棵子树（apply 级幂等，重复扫描只读 tag 不走反射） */
    private static void sweep(View view) {
        apply(view);
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                sweep(group.getChildAt(i));
            }
        }
    }

    /**
     * 单视图双开：view 级（HyperOS 按 view 渲染圆角轮廓）+ 背景 drawable 级
     * （drawable 自绘圆角同样转 squircle）；已处理的视图仅在背景实例变化时补开。
     */
    private static void apply(View view) {
        Object state = view.getTag(R.id.smooth_corner_state);
        Drawable background = view.getBackground();
        if (state == null) {
            // 首次处理
            SmoothCornerHelper.setViewSmoothCornerEnable(view, true);
            if (background != null) {
                SmoothCornerHelper.setDrawableSmoothCornerEnable(background, true);
                view.setTag(R.id.smooth_corner_state, background);
            } else {
                view.setTag(R.id.smooth_corner_state, NO_BACKGROUND);
            }
            return;
        }
        if (background != null && state != background) {
            // 背景在上一轮之后被替换（换主题/换背景逻辑）：对新实例补开
            SmoothCornerHelper.setDrawableSmoothCornerEnable(background, true);
            view.setTag(R.id.smooth_corner_state, background);
        } else if (background == null && state != NO_BACKGROUND) {
            view.setTag(R.id.smooth_corner_state, NO_BACKGROUND);
        }
    }
}
