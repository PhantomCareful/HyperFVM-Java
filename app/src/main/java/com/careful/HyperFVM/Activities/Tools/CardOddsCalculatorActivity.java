package com.careful.HyperFVM.Activities.Tools;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForDesign.Widget.StrokeSingleSelector;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class CardOddsCalculatorActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_co_contributor_team_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    // 三张副卡概率表，按各组 SUB_N_CATEGORY 选中的下标取用（0=差卡、1=中卡、2=好卡）
    private static final double[][][] ODDS_TABLES = {
            CardOddsData.SubCardOdds0, CardOddsData.SubCardOdds1, CardOddsData.SubCardOdds2
    };

    // 各组卡片单选的选中状态保存键（每组一个，互不相同）
    private static final String STATE_SELECTED_MAIN_CARD = "state_selected_main_card";
    private static final String STATE_SELECTED_SUB_1 = "state_selected_sub_1";
    private static final String STATE_SELECTED_SUB_1_CATEGORY = "state_selected_sub_1_category";
    private static final String STATE_SELECTED_SUB_2 = "state_selected_sub_2";
    private static final String STATE_SELECTED_SUB_2_CATEGORY = "state_selected_sub_2_category";
    private static final String STATE_SELECTED_SUB_3 = "state_selected_sub_3";
    private static final String STATE_SELECTED_SUB_3_CATEGORY = "state_selected_sub_3_category";
    private static final String STATE_SELECTED_FOUR_LEAF_CLOVER = "state_selected_four_leaf_clover";
    private static final String STATE_SELECTED_VIP = "state_selected_vip";
    private static final String STATE_SELECTED_CONSORTIA = "state_selected_consortia";

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）

    // 各组卡片单选器：每组一个实例，独立维护选中项与外描边
    private StrokeSingleSelector[] cardSelectors;

    // 成功率结果展示组件
    private TextView totalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_card_odds_calculator);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化各组卡片的单选功能
        setupCardSelectors();

        // 成功率结果展示组件
        totalView = findViewById(R.id.total);

        // 初始执行一次计算（初始全部未选中，输出 成功率：0.00%+0.00%）
        recalculate();
    }

    /**
     * 初始化各组卡片的单选：每组传入组件 id 数组，独立记录点击项并绘制外描边
     */
    private void setupCardSelectors() {
        cardSelectors = new StrokeSingleSelector[]{
                new StrokeSingleSelector(this, STATE_SELECTED_MAIN_CARD, new int[]{
                        R.id.star_main_0, R.id.star_main_1, R.id.star_main_2, R.id.star_main_3,
                        R.id.star_main_4, R.id.star_main_5, R.id.star_main_6, R.id.star_main_7,
                        R.id.star_main_8, R.id.star_main_9, R.id.star_main_10, R.id.star_main_11,
                        R.id.star_main_12, R.id.star_main_13, R.id.star_main_14, R.id.star_main_15
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_1, new int[]{
                        R.id.star_sub_1_0, R.id.star_sub_1_1, R.id.star_sub_1_2, R.id.star_sub_1_3,
                        R.id.star_sub_1_4, R.id.star_sub_1_5, R.id.star_sub_1_6, R.id.star_sub_1_7,
                        R.id.star_sub_1_8, R.id.star_sub_1_9, R.id.star_sub_1_10, R.id.star_sub_1_11,
                        R.id.star_sub_1_12, R.id.star_sub_1_13, R.id.star_sub_1_14, R.id.star_sub_1_15,
                        R.id.star_sub_1_16
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_1_CATEGORY, new int[]{
                        R.id.star_sub_1_category_0, R.id.star_sub_1_category_1, R.id.star_sub_1_category_2
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_2, new int[]{
                        R.id.star_sub_2_0, R.id.star_sub_2_1, R.id.star_sub_2_2, R.id.star_sub_2_3,
                        R.id.star_sub_2_4, R.id.star_sub_2_5, R.id.star_sub_2_6, R.id.star_sub_2_7,
                        R.id.star_sub_2_8, R.id.star_sub_2_9, R.id.star_sub_2_10, R.id.star_sub_2_11,
                        R.id.star_sub_2_12, R.id.star_sub_2_13, R.id.star_sub_2_14, R.id.star_sub_2_15,
                        R.id.star_sub_2_16
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_2_CATEGORY, new int[]{
                        R.id.star_sub_2_category_0, R.id.star_sub_2_category_1, R.id.star_sub_2_category_2
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_3, new int[]{
                        R.id.star_sub_3_0, R.id.star_sub_3_1, R.id.star_sub_3_2, R.id.star_sub_3_3,
                        R.id.star_sub_3_4, R.id.star_sub_3_5, R.id.star_sub_3_6, R.id.star_sub_3_7,
                        R.id.star_sub_3_8, R.id.star_sub_3_9, R.id.star_sub_3_10, R.id.star_sub_3_11,
                        R.id.star_sub_3_12, R.id.star_sub_3_13, R.id.star_sub_3_14, R.id.star_sub_3_15,
                        R.id.star_sub_3_16
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_SUB_3_CATEGORY, new int[]{
                        R.id.star_sub_3_category_0, R.id.star_sub_3_category_1, R.id.star_sub_3_category_2
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_FOUR_LEAF_CLOVER, new int[]{
                        R.id.four_leaf_clover_1, R.id.four_leaf_clover_2, R.id.four_leaf_clover_3, R.id.four_leaf_clover_4, R.id.four_leaf_clover_5, R.id.four_leaf_clover_6,
                        R.id.four_leaf_clover_s, R.id.four_leaf_clover_ss, R.id.four_leaf_clover_sss, R.id.four_leaf_clover_ssr, R.id.four_leaf_clover_snake, R.id.four_leaf_clover_horse,
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_VIP, new int[]{
                        R.id.vip_1, R.id.vip_2, R.id.vip_3, R.id.vip_4, R.id.vip_5, R.id.vip_6, R.id.vip_7, R.id.vip_8,
                        R.id.vip_9, R.id.vip_10, R.id.vip_11, R.id.vip_12, R.id.vip_13, R.id.vip_14, R.id.vip_15, R.id.vip_16
                }),
                new StrokeSingleSelector(this, STATE_SELECTED_CONSORTIA, new int[]{
                        R.id.consortia_1, R.id.consortia_2, R.id.consortia_3, R.id.consortia_4, R.id.consortia_5, R.id.consortia_6
                })
        };

        // 需求 1：任意一组选中项变更（含取消选中）后，重新执行一次本计算机制
        for (StrokeSingleSelector selector : cardSelectors) {
            selector.setOnSelectionChangedListener(s -> recalculate());
        }
    }

    /**
     * 重新执行一次成功率计算并写入 total 组件（每次变更任意一组选中项后调用）：
     * 先存储 10 组当前选中的下标，再按 MAIN→行、SUB→列、SUB_N_CATEGORY→表
     * 取出 SUB_1/2/3 三路数据（任一依赖未选中则该路不取数），按第 5 步规则合并为结果1
     * （超过 100% 时直接取 100%），再乘 FOUR_LEAF_CLOVER 对应倍率（未选为 1.0）
     * 并再次钳制到 100%，最后叠加 VIP 与 Consortia 加成得到结果2，
     * 输出 成功率：结果1%+结果2%
     */
    private void recalculate() {
        if (cardSelectors == null || totalView == null) return;

        // 需求 2：存储所有组当前选中的下标（MAIN 组下标决定取哪一行）
        int mainSelected = cardSelectors[0].getSelected();

        // 需求 3/4：三路取数，每路按 {SUB_N 组下标, SUB_N_CATEGORY 组下标} 定列与表
        int[][] subPairs = {{1, 2}, {3, 4}, {5, 6}};
        int[] subSelected = new int[subPairs.length];
        double[] values = new double[subPairs.length];
        for (int i = 0; i < subPairs.length; i++) {
            subSelected[i] = cardSelectors[subPairs[i][0]].getSelected();
            values[i] = fetchOdds(mainSelected, subSelected[i],
                    cardSelectors[subPairs[i][1]].getSelected());
        }

        // 需求 5：三路数据按规则合并为结果1；超过 100% 时直接取 100%（后续乘法基于钳制后的值计算）
        double result1 = mergeValues(values, subSelected);
        if (result1 > 1) {
            result1 = 1;
        }

        // 四叶草：得到最终结果1 后乘 FOUR_LEAF_CLOVER 选中项对应倍率（未选中则为 1.0），
        // 乘积若再次超过 100% 仍直接取 100%
        int cloverSelected = cardSelectors[7].getSelected();
        if (cloverSelected >= 0 && cloverSelected < CardOddsData.FourLeafClover.length) {
            result1 *= CardOddsData.FourLeafClover[cloverSelected];
        }
        if (result1 > 1) {
            result1 = 1;
        }

        // 需求 6：读取 VIP 与 Consortia 选中下标取对应加成，求和后乘结果1 得结果2；
        // 只选中其中一个时加成直接用选中项的值（未选中项不参与加成），
        // 两个都选中时两者相加，两个都未选中时加成为 0（此时结果2 为 0）
        int vipSelected = cardSelectors[8].getSelected();
        int consortiaSelected = cardSelectors[9].getSelected();
        double bonus = 0;
        if (vipSelected >= 0 && vipSelected < CardOddsData.VIP.length) {
            bonus += CardOddsData.VIP[vipSelected];
        }
        if (consortiaSelected >= 0 && consortiaSelected < CardOddsData.Consortia.length) {
            bonus += CardOddsData.Consortia[consortiaSelected];
        }
        double result2 = result1 * bonus;

        // 需求 7：输出 成功率：结果1%+结果2%
        totalView.setText(String.format(Locale.CHINA, "成功率：%s%%+%s%%",
                formatPercent(result1), formatPercent(result2)));
    }

    /**
     * 第 3/4 步的单路取数：MAIN 下标定行、SUB 下标定该行第几个数据、
     * SUB_N_CATEGORY 定取哪一张表；任一未选中（-1）或下标越界时返回 NaN（不取任何数据）
     */
    private static double fetchOdds(int mainSelected, int subSelected, int categorySelected) {
        if (mainSelected < 0 || subSelected < 0 || categorySelected < 0) return Double.NaN;
        if (categorySelected >= ODDS_TABLES.length) return Double.NaN;
        double[][] table = ODDS_TABLES[categorySelected];
        if (mainSelected >= table.length || subSelected >= table[mainSelected].length) return Double.NaN;
        return table[mainSelected][subSelected];
    }

    /**
     * 第 5 步：合并三路数据（NaN 为该路未取到，不参与）。
     * 仅取到 1 个时它就是结果1；取到 2~3 个时，SUB 下标最高且最先出现的那个数据
     * 保持不动，其余数据 ÷3，全部求和得到结果1
     */
    private static double mergeValues(double[] values, int[] subSelected) {
        int firstMaxIndex = -1;
        int maxSub = Integer.MIN_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) continue;
            // 仅严格更大才更新：并列最高时保留最先出现的那个
            if (firstMaxIndex < 0 || subSelected[i] > maxSub) {
                maxSub = subSelected[i];
                firstMaxIndex = i;
            }
        }
        // 三路都没取到数据时结果1 为 0
        if (firstMaxIndex < 0) return 0;

        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) continue;
            sum += i == firstMaxIndex ? values[i] : values[i] / 3;
        }
        return sum;
    }

    /** 百分比格式化：×100 后四舍五入保留两位小数（0.484 → 48.40） */
    private static String formatPercent(double value) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 90);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout coContributorTeamContainer = findViewById(R.id.CoContributorTeam_Container);
        InsetsUtil.setMarginHorizontal(this, coContributorTeamContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) coContributorTeamContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            coContributorTeamContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置返回按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        nestedScrollUtil = NestedScrollUtil.attach(rootView,
                R.id.scrollView, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0.5f);
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
        // 保存各组卡片当前选中的下标，供界面重建（深浅色切换等）后恢复
        if (cardSelectors != null) {
            for (StrokeSingleSelector selector : cardSelectors) {
                selector.saveState(outState);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }

        // 恢复重建前各组卡片的选中下标
        if (cardSelectors != null) {
            for (StrokeSingleSelector selector : cardSelectors) {
                selector.restoreState(savedInstanceState);
            }
            // 恢复完成后按恢复出的选中项重新计算一次（restoreState 不触发变更监听）
            recalculate();
        }
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}