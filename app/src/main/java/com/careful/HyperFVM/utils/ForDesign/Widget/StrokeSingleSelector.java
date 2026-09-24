package com.careful.HyperFVM.utils.ForDesign.Widget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

/**
 * 横向卡片组通用单选器：传入一组组件 id 数组，自动向上找到各组件所在的
 * StrokeCardView 并挂载点击监听，初始默认全部未选中，点击后仅选中项绘制
 * colorPrimary 外描边（5dp 宽 / 5dp 间距，与今日运势得分描边一致）。
 * 再次点击已选中组件可取消选中，本组可处于无任何选中状态（下标 -1）。
 * <p>
 * 每个卡片组独立 new 一个实例，选中下标统一通过 {@link #getSelected()} 查询，
 * 选中项的组件视图通过 {@link #getSelectedView()} 获取（供调用方同步选中图片等），
 * 选中状态除通过 saveState/restoreState 随界面重建（深浅色切换等）保存恢复外，
 * 还会同步写入 SharedPreferences 持久化存储（非数据库），应用重启后构造时自动恢复，
 * 调用方无需再为某一组单独写查询方法。
 * <p>
 * 用法示例：
 * <pre>
 * StrokeSingleSelector selector = new StrokeSingleSelector(
 *         this, "state_selected_main_card",
 *         new int[]{R.id.star_main_0, R.id.star_main_1, ...});
 * </pre>
 * <p>
 * 每次选中/取消选中后会回调 {@link #setOnSelectionChangedListener(OnSelectionChangedListener)}
 * 注册的监听，调用方借此在任意一组变更后统一重新计算结果。
 */
public class StrokeSingleSelector {
    // 描边参数（与今日运势的得分描边一致）
    private static final int STROKE_WIDTH_DP = 5;
    private static final int STROKE_GAP_DP = 5;
    // 初始选中下标：默认全部未选中（-1）
    private static final int DEFAULT_SELECTED = -1;

    // 本组选中状态的保存键（每组一个，需互不相同）：
    // 既用于 outState（界面重建恢复），也用于 SharedPreferences（跨应用重启持久化）
    private final String stateKey;
    // 持久化存储：按 stateKey 记录选中下标，应用重启后构造时恢复（按 Activity 隔离存文件）
    private final SharedPreferences prefs;
    // 本组各卡片（按传入 id 顺序，下标即选中下标；id 解析不到的槽位为 null）
    private final StrokeCardView[] cards;
    // 本组各组件本身（按传入 id 顺序，下标即选中下标；供调用方读取选中项的内容视图）
    private final View[] views;
    // 描边参数（像素）与颜色
    private final float strokeWidthPx;
    private final float strokeGapPx;
    private final int strokeColor;

    // 当前选中的下标，-1 表示本组当前无选中；初始状态即为无选中
    private int selected;

    /** 选中变更监听：本组每次选中/取消选中（{@link #select(int)}）后回调一次 */
    public interface OnSelectionChangedListener {
        void onSelectionChanged(StrokeSingleSelector selector);
    }

    // 选中变更监听；restoreState 恢复不回调，由调用方恢复完毕后统一处理
    private OnSelectionChangedListener onSelectionChanged;

    /**
     * @param activity 用于 findViewById、主题解析与持久化存储（SharedPreferences）
     * @param stateKey 选中状态的保存键（同一界面内各组必须互不相同）
     * @param viewIds  本组各卡片内组件的 id 数组，数组顺序即选中下标
     */
    public StrokeSingleSelector(Activity activity, String stateKey, int[] viewIds) {
        this.stateKey = stateKey;
        this.prefs = activity.getPreferences(Context.MODE_PRIVATE);

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        this.strokeColor = typedValue.data;
        this.strokeWidthPx = DensityUtil.dpToPx(activity, STROKE_WIDTH_DP);
        this.strokeGapPx = DensityUtil.dpToPx(activity, STROKE_GAP_DP);

        this.cards = new StrokeCardView[viewIds.length];
        this.views = new View[viewIds.length];
        for (int i = 0; i < viewIds.length; i++) {
            View view = activity.findViewById(viewIds[i]);
            views[i] = view;
            StrokeCardView card = resolveCard(view);
            if (card == null) continue;
            cards[i] = card;
            final int index = i;
            card.setOnClickListener(v -> select(index));
        }

        // 从持久化存储恢复上次选中的下标（首次运行无记录、或旧记录下标越界时保持默认全不选）
        int restored = prefs.getInt(stateKey, DEFAULT_SELECTED);
        selected = restored >= 0 && restored < viewIds.length ? restored : DEFAULT_SELECTED;
        refreshStrokes();
    }

    /** 从组件本身或其任意祖先中找到承载它的 StrokeCardView（id 可直接标在卡片或卡片内子组件上） */
    private static StrokeCardView resolveCard(View view) {
        for (View current = view; current != null; ) {
            if (current instanceof StrokeCardView card) return card;
            current = current.getParent() instanceof View parent ? parent : null;
        }
        return null;
    }

    /**
     * 切换选中：点击非选中项时选中该项；再次点击已选中项时取消选中
     * （本组变为无任何选中，下标 -1），同步刷新本组描边并在变更后回调监听
     */
    public void select(int index) {
        selected = index == selected ? -1 : index;
        persistState();
        refreshStrokes();
        if (onSelectionChanged != null) {
            onSelectionChanged.onSelectionChanged(this);
        }
    }

    /** 当前选中的下标（-1 表示当前无选中，初始即为此状态）；本类即各组选中状态的统一查询入口 */
    public int getSelected() {
        return selected;
    }

    /** 当前选中项的组件视图（未选中、下标越界或 id 解析不到时返回 null），供调用方读取选中项内容 */
    public View getSelectedView() {
        if (selected < 0 || selected >= views.length) return null;
        return views[selected];
    }

    /** 注册选中变更监听：本组每次选中/取消选中（{@link #select(int)}）后回调一次 */
    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChanged = listener;
    }

    // 刷新本组外描边：解析 colorPrimary 作为描边颜色，仅当前选中项绘制
    private void refreshStrokes() {
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) continue;
            cards[i].setStroke(i == selected ? strokeWidthPx : 0f, strokeGapPx, strokeColor);
        }
    }

    /** 保存本组选中下标，供界面重建后恢复 */
    public void saveState(Bundle outState) {
        outState.putInt(stateKey, selected);
    }

    /** 恢复重建前的选中下标并刷新描边；Bundle 无记录时保留构造时从持久化存储恢复的值 */
    public void restoreState(Bundle savedInstanceState) {
        int restored = savedInstanceState.getInt(stateKey, selected);
        // 防御旧版本持久化记录下标越界（如组件数量变化后），越界则回退为全不选
        selected = restored >= 0 && restored < views.length ? restored : DEFAULT_SELECTED;
        persistState();
        refreshStrokes();
    }

    // 选中状态写入持久化存储（apply 异步落盘，内存值立即生效），供下次启动构造时恢复
    private void persistState() {
        prefs.edit().putInt(stateKey, selected).apply();
    }
}
