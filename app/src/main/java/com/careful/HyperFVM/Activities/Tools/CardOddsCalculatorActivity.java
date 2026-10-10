package com.careful.HyperFVM.Activities.Tools;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Dropdown.DropdownPopupHelper;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CardOddsCalculatorActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_co_contributor_team_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    // 三张副卡概率表，按各组 SUB_N_CATEGORY 选中的逻辑值取用（0=差卡、1=中卡、2=好卡）
    private static final double[][][] ODDS_TABLES = {
            CardOddsData.SubCardOdds0, CardOddsData.SubCardOdds1, CardOddsData.SubCardOdds2
    };

    // 各组下拉选择的选中状态保存键（每组一个，互不相同；沿用旧单选器键名，
    // 历史 SharedPreferences 记录与 Bundle 记录可直接继续生效）
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

    // 组索引（与保存键、dropdownGroups 数组顺序一一对应）
    private static final int GROUP_MAIN = 0;
    private static final int GROUP_SUB_1 = 1;
    private static final int GROUP_SUB_1_CATEGORY = 2;
    private static final int GROUP_SUB_2 = 3;
    private static final int GROUP_SUB_2_CATEGORY = 4;
    private static final int GROUP_SUB_3 = 5;
    private static final int GROUP_SUB_3_CATEGORY = 6;
    private static final int GROUP_FOUR_LEAF_CLOVER = 7;
    private static final int GROUP_VIP = 8;
    private static final int GROUP_CONSORTIA = 9;
    private static final int GROUP_COUNT = 10;

    // 三路取数的组配对：每路为 {SUB_N 组下标, SUB_N_CATEGORY 组下标}
    private static final int[][] SUB_PAIRS = {
            {GROUP_SUB_1, GROUP_SUB_1_CATEGORY},
            {GROUP_SUB_2, GROUP_SUB_2_CATEGORY},
            {GROUP_SUB_3, GROUP_SUB_3_CATEGORY}
    };

    // 逻辑选中值 → 展示图片（下标即逻辑值，-1“不选”不在此列）：
    // 主卡 0~15、副卡 0~16 共用 0~16 的星级图
    private static final int[] STAR_DRAWABLES = {
            R.drawable.star_image_0, R.drawable.star_image_1, R.drawable.star_image_2,
            R.drawable.star_image_3, R.drawable.star_image_4, R.drawable.star_image_5,
            R.drawable.star_image_6, R.drawable.star_image_7, R.drawable.star_image_8,
            R.drawable.star_image_9, R.drawable.star_image_10, R.drawable.star_image_11,
            R.drawable.star_image_12, R.drawable.star_image_13, R.drawable.star_image_14,
            R.drawable.star_image_15, R.drawable.star_image_16
    };
    // 四叶草 12 项（1~6级、超能、SS、SSS、SSR、蛇年、马年），与菜单条目及概率表顺序一致
    private static final int[] CLOVER_DRAWABLES = {
            R.drawable.item_four_leaf_clover_1, R.drawable.item_four_leaf_clover_2,
            R.drawable.item_four_leaf_clover_3, R.drawable.item_four_leaf_clover_4,
            R.drawable.item_four_leaf_clover_5, R.drawable.item_four_leaf_clover_6,
            R.drawable.item_four_leaf_clover_s, R.drawable.item_four_leaf_clover_ss,
            R.drawable.item_four_leaf_clover_sss, R.drawable.item_four_leaf_clover_ssr,
            R.drawable.item_four_leaf_clover_snake, R.drawable.item_four_leaf_clover_horse
    };
    // VIP 等级 1~16
    private static final int[] VIP_DRAWABLES = {
            R.drawable.vip_1, R.drawable.vip_2, R.drawable.vip_3, R.drawable.vip_4,
            R.drawable.vip_5, R.drawable.vip_6, R.drawable.vip_7, R.drawable.vip_8,
            R.drawable.vip_9, R.drawable.vip_10, R.drawable.vip_11, R.drawable.vip_12,
            R.drawable.vip_13, R.drawable.vip_14, R.drawable.vip_15, R.drawable.vip_16
    };

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）

    // 各组下拉选择：每组一个实例，独立维护选中值、菜单与展示位
    private DropdownGroup[] dropdownGroups;

    // 持久化存储：按各组 stateKey 记录逻辑选中值（与旧单选器同键同义，跨升级兼容）
    private SharedPreferences prefs;

    // 成功率结果展示组件：A部分、B部分、求和三段成功率分别写入这三张卡
    private TextView percentAView;
    private TextView percentBView;
    private TextView percentCView;
    // 消耗与保底展示组件：金币/保险金按主卡等级取数，保底显示次数
    private TextView coinView;
    private TextView insuranceView;
    private TextView minimumGuaranteeView;
    // 各展示组件的原始字号（onCreate 记录，每次缩字前先回到该字号再重新适配）
    private final Map<TextView, Float> originalTextSizes = new HashMap<>();

    // 选中组件的图片展示位：MAIN/SUB_1/SUB_2/SUB_3/FOUR_LEAF_CLOVER/VIP 选中项图片分别同步到这 6 个 ImageView
    private ImageView displayStarMain;
    private ImageView displayStarSub1;
    private ImageView displayStarSub2;
    private ImageView displayStarSub3;
    private ImageView displayFourLeafClover;
    private ImageView displayVip;
    // 公会合成屋等级的文字展示位（未选中显示“无”，选中显示等级数字）
    private TextView displayConsortia;

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

        // 持久化存储（各组选中值在构造各组时按 stateKey 恢复）
        prefs = getPreferences(MODE_PRIVATE);

        // 成功率与消耗/保底展示组件（同时记录各自原始字号，供文案超宽时缩字适配、文案变短时恢复）
        percentAView = bindResultView(R.id.percent_a);
        percentBView = bindResultView(R.id.percent_b);
        percentCView = bindResultView(R.id.percent_c);
        coinView = bindResultView(R.id.coin);
        insuranceView = bindResultView(R.id.insurance);
        minimumGuaranteeView = bindResultView(R.id.minimum_guarantee);

        // 选中组件图片/文字展示位
        displayStarMain = findViewById(R.id.display_star_main);
        displayStarSub1 = findViewById(R.id.display_star_sub_1);
        displayStarSub2 = findViewById(R.id.display_star_sub_2);
        displayStarSub3 = findViewById(R.id.display_star_sub_3);
        displayFourLeafClover = findViewById(R.id.display_four_leaf_clover);
        displayVip = findViewById(R.id.display_vip);
        displayConsortia = findViewById(R.id.display_consortia);

        // 初始化各组下拉选择（构造时已从持久化恢复选中值并绑定点击）
        setupDropdownGroups();

        // 初始执行一次计算（持久化恢复的选中项在此还原并刷新展示，
        // 未恢复过则全不选、输出 0.00%+0.00%=0.00% 且保底显示“—”）
        recalculate();
    }

    /**
     * 初始化 10 组下拉选择（弹出方式与设置页 selector 一致：以点击区为锚点、菜单右缘固定对齐容器）：
     * 图标组（主卡/副卡/四叶草/VIP/公会）点击“标题+图片”整列弹出菜单，其中主卡/副卡/VIP
     * 菜单为纯图片、四叶草为图片+加成倍率文字、公会为纯文字；
     * 副卡类型组点击整行弹出菜单，按“好卡、中卡、差卡”顺序排列且默认好卡
     */
    private void setupDropdownGroups() {
        dropdownGroups = new DropdownGroup[GROUP_COUNT];

        // 主卡：不选 + 0~15（菜单为纯图片）
        dropdownGroups[GROUP_MAIN] = createIconGroup(STATE_SELECTED_MAIN_CARD,
                buildNumberLabels(0, 15), findViewById(R.id.display_star_main),
                STAR_DRAWABLES, false);
        // 副卡 1~3：不选 + 0~16（菜单为纯图片）
        dropdownGroups[GROUP_SUB_1] = createIconGroup(STATE_SELECTED_SUB_1,
                buildNumberLabels(0, 16), findViewById(R.id.display_star_sub_1),
                STAR_DRAWABLES, false);
        dropdownGroups[GROUP_SUB_2] = createIconGroup(STATE_SELECTED_SUB_2,
                buildNumberLabels(0, 16), findViewById(R.id.display_star_sub_2),
                STAR_DRAWABLES, false);
        dropdownGroups[GROUP_SUB_3] = createIconGroup(STATE_SELECTED_SUB_3,
                buildNumberLabels(0, 16), findViewById(R.id.display_star_sub_3),
                STAR_DRAWABLES, false);
        // 四叶草：不选 + 12 个四叶草（菜单为图片 + 加成倍率文字）
        dropdownGroups[GROUP_FOUR_LEAF_CLOVER] = createIconGroup(STATE_SELECTED_FOUR_LEAF_CLOVER,
                buildCloverLabels(), findViewById(R.id.display_four_leaf_clover),
                CLOVER_DRAWABLES, true);
        // VIP 等级：不选 + 1~16（菜单为纯图片）
        dropdownGroups[GROUP_VIP] = createIconGroup(STATE_SELECTED_VIP,
                buildNumberLabels(1, 16), findViewById(R.id.display_vip),
                VIP_DRAWABLES, false);
        // 公会合成屋等级：不选 + 1~6（文字展示位、无对应图片，菜单保持纯文字）
        dropdownGroups[GROUP_CONSORTIA] = createIconGroup(STATE_SELECTED_CONSORTIA,
                buildNumberLabels(1, 6), findViewById(R.id.display_consortia),
                null, false);
        // 副卡类型 ×3：好卡/中卡/差卡三选一，默认好卡
        dropdownGroups[GROUP_SUB_1_CATEGORY] = createCategoryGroup(STATE_SELECTED_SUB_1_CATEGORY,
                R.id.sub_card_category_1_selector_container, R.id.sub_card_category_1_current_selection);
        dropdownGroups[GROUP_SUB_2_CATEGORY] = createCategoryGroup(STATE_SELECTED_SUB_2_CATEGORY,
                R.id.sub_card_category_2_selector_container, R.id.sub_card_category_2_current_selection);
        dropdownGroups[GROUP_SUB_3_CATEGORY] = createCategoryGroup(STATE_SELECTED_SUB_3_CATEGORY,
                R.id.sub_card_category_3_selector_container, R.id.sub_card_category_3_current_selection);
    }

    /**
     * 数字类图标组菜单条目：首项“不选”，其余为 from~to 的等级数字
     */
    private String[] buildNumberLabels(int from, int to) {
        String[] labels = new String[to - from + 2];
        labels[0] = getString(R.string.title_card_odds_calculator_not_select);
        for (int i = from; i <= to; i++) {
            labels[i - from + 1] = String.valueOf(i);
        }
        return labels;
    }

    /**
     * 四叶草图标组菜单条目：首项“不选”，其余为 12 个四叶草的加成倍率（格式 X倍，
     * 数值直接取自 FourLeafClover 概率表，与条目图片顺序一一对应）；
     * 用 BigDecimal 去掉小数末尾的 0：2.0 → “2倍”，1.2 → “1.2倍”
     */
    private String[] buildCloverLabels() {
        String[] labels = new String[CardOddsData.FourLeafClover.length + 1];
        labels[0] = getString(R.string.title_card_odds_calculator_not_select);
        for (int i = 0; i < CardOddsData.FourLeafClover.length; i++) {
            labels[i + 1] = BigDecimal.valueOf(CardOddsData.FourLeafClover[i])
                    .stripTrailingZeros().toPlainString() + "倍";
        }
        return labels;
    }

    /**
     * 图标组：点击区与菜单锚点均为“标题+图片”整列（展示位的父容器）；
     * 逻辑选中值 -1=不选、0~N=菜单中第 N+1 项（与旧单选器下标语义一致）。
     * displayDrawables 为各逻辑值对应的展示图（null 表示该组无图片，如公会合成屋，菜单保持纯文字）；
     * 有图片时菜单行改为图片形式，textInMenu=true 时图片右侧附文字列（四叶草的加成倍率）
     */
    private DropdownGroup createIconGroup(String stateKey, String[] labels, View displayView,
                                          int[] displayDrawables, boolean textInMenu) {
        View clickTarget = displayView.getParent() instanceof View parent ? parent : displayView;
        int[] menuToLogic = new int[labels.length];
        for (int i = 0; i < labels.length; i++) {
            menuToLogic[i] = i - 1; // 首项“不选”→ -1，其余顺延
        }
        // 行布局与图片：首项“不选”用 card_data_x，其余行与展示图按逻辑值一一对齐
        int menuLayoutId = R.layout.item_dropdown_text_selection;
        int[] menuDrawables = null;
        if (displayDrawables != null) {
            menuLayoutId = textInMenu
                    ? R.layout.item_dropdown_image_text_selection
                    : R.layout.item_dropdown_image_selection;
            menuDrawables = new int[labels.length];
            menuDrawables[0] = R.drawable.card_data_x;
            System.arraycopy(displayDrawables, 0, menuDrawables, 1,
                    Math.min(labels.length - 1, displayDrawables.length));
        }
        return new DropdownGroup(prefs, stateKey, -1, -1, labels.length - 2,
                labels, menuToLogic, clickTarget, null, menuLayoutId, menuDrawables);
    }

    /**
     * 副卡类型组：点击区与菜单锚点均为整行容器，当前选择展示在行内 selectionText；
     * 菜单按“好卡、中卡、差卡”顺序排列（menuToLogic 映射回概率表下标 0=差、1=中、2=好），默认好卡
     */
    private DropdownGroup createCategoryGroup(String stateKey, int containerId, int selectionTextId) {
        View container = findViewById(containerId);
        TextView selectionText = findViewById(selectionTextId);
        String[] labels = {
                getString(R.string.title_card_odds_calculator_category_2), // 好卡
                getString(R.string.title_card_odds_calculator_category_1), // 中卡
                getString(R.string.title_card_odds_calculator_category_0)  // 差卡
        };
        int[] menuToLogic = {2, 1, 0};
        // 类别组无“不选”，默认 2=好卡，合法逻辑值 0~2（旧记录为 -1 时回退默认）；菜单保持纯文字
        return new DropdownGroup(prefs, stateKey, 2, 0, 2,
                labels, menuToLogic, container, selectionText,
                R.layout.item_dropdown_text_selection, null);
    }

    /**
     * 单组下拉选择：持有本组菜单条目、点击区（兼菜单锚点）与当前逻辑选中值。
     * 点击 clickTarget 弹出 ListPopupWindow（√标记当前项、点击行外关闭），
     * 选中后写入 SharedPreferences、刷新展示并触发重算；随界面重建 saveState/restoreState
     */
    private class DropdownGroup {
        // 本组选中状态的保存键（既用于 outState 也用于 SharedPreferences）
        final String stateKey;
        // 默认逻辑选中值（图标组 -1=不选；类别组 2=好卡）
        final int defaultValue;
        // 合法逻辑值范围（闭区间），越界记录一律回退默认值
        final int minLogic;
        final int maxLogic;
        // 菜单条目文本（按菜单显示顺序；纯图片行不显示文字，仅用于数量统计与无障碍描述）
        final String[] labels;
        // 菜单下标 → 逻辑选中值
        final int[] menuToLogic;
        // 点击触发区，同时作为菜单弹出锚点（图标组为“标题+图片”整列；类别组为整行）
        final View clickTarget;
        // 当前选择展示位（仅类别组有；图标组靠图片展示，为 null）
        final TextView selectionText;
        // 菜单行布局：纯文字（item_dropdown_text_selection）/纯图片/图片+文字（四叶草的加成倍率）
        final int menuLayoutId;
        // 图片行各条目对应的图片资源（纯文字行为 null；首项“不选”为 card_data_x）
        final int[] menuDrawables;

        // 当前逻辑选中值
        int logic;

        DropdownGroup(SharedPreferences prefs, String stateKey, int defaultValue,
                      int minLogic, int maxLogic, String[] labels, int[] menuToLogic,
                      View clickTarget, TextView selectionText,
                      int menuLayoutId, int[] menuDrawables) {
            this.stateKey = stateKey;
            this.defaultValue = defaultValue;
            this.minLogic = minLogic;
            this.maxLogic = maxLogic;
            this.labels = labels;
            this.menuToLogic = menuToLogic;
            this.clickTarget = clickTarget;
            this.selectionText = selectionText;
            this.menuLayoutId = menuLayoutId;
            this.menuDrawables = menuDrawables;

            // 从持久化存储恢复上次选中的逻辑值（无记录或记录越界时取默认值）
            this.logic = prefs.getInt(stateKey, defaultValue);
            if (logic < minLogic || logic > maxLogic) {
                logic = defaultValue;
            }
            this.clickTarget.setOnClickListener(v -> showDropdown());
        }

        /**
         * 就地弹出本组下拉选项菜单：行渲染（文字 colorPrimary 高亮/图片/√）、
         * 宽度恒为“最长条目+对勾”、四分支展开、右缘固定对齐容器、打开时居中定位选中项、
         * 点击行外自动关闭——全部复用 DropdownPopupHelper 公用实现，
         * 本方法只负责构造本组 adapter 与选中回调
         */
        private void showDropdown() {
            int selectedMenuIndex = menuIndexOfLogic();
            ArrayAdapter<String> adapter = DropdownPopupHelper.createAdapter(
                    CardOddsCalculatorActivity.this, selectedMenuIndex, labels, menuLayoutId, menuDrawables);
            DropdownPopupHelper.showAnchored(CardOddsCalculatorActivity.this, clickTarget, adapter,
                    labels.length, selectedMenuIndex, this::selectByMenuIndex);
        }

        /** 菜单项点击：写入逻辑选中值并持久化，随后统一由 recalculate 刷新展示与结果 */
        private void selectByMenuIndex(int menuIndex) {
            logic = menuToLogic[menuIndex];
            prefs.edit().putInt(stateKey, logic).apply();
            recalculate();
        }

        /** 当前逻辑选中值对应的菜单下标（√标记与类别组文本展示用） */
        int menuIndexOfLogic() {
            for (int i = 0; i < menuToLogic.length; i++) {
                if (menuToLogic[i] == logic) return i;
            }
            return 0;
        }

        /** 保存本组选中值，供界面重建后恢复 */
        void saveState(Bundle outState) {
            outState.putInt(stateKey, logic);
        }

        /** 恢复重建前的选中值（越界回退默认值）并写回持久化；Bundle 无记录时保留构造时恢复的值 */
        void restoreState(Bundle savedInstanceState) {
            int restored = savedInstanceState.getInt(stateKey, logic);
            logic = restored >= minLogic && restored <= maxLogic ? restored : defaultValue;
            prefs.edit().putInt(stateKey, logic).apply();
        }
    }

    /**
     * 重新执行一次成功率计算并写入展示组件（每次变更任意一组选中值后调用）：
     * 先读取 10 组当前选中的逻辑值，再按 MAIN→行、SUB→列、SUB_N_CATEGORY→表
     * 取出 SUB_1/2/3 三路数据（任一依赖未选中则该路不取数），按第 5 步规则合并为结果1
     * （超过 100% 时直接取 100%），再乘 FOUR_LEAF_CLOVER 对应倍率（未选为 1.0）
     * 并再次钳制到 100%，最后叠加 VIP 与 Consortia 加成得到结果2；
     * 三段成功率写入 A/B/C 三张卡，另按主卡等级写入金币/保险金，保底规则写入保底卡
     */
    @SuppressLint("SetTextI18n")
    private void recalculate() {
        if (dropdownGroups == null || percentAView == null) return;

        // 需求 2：读取所有组当前选中的逻辑值（MAIN 组值决定取哪一行）
        int mainSelected = dropdownGroups[GROUP_MAIN].logic;

        // 需求 3/4：三路取数，每路按 {SUB_N 组值, SUB_N_CATEGORY 组值} 定列与表
        int[] subSelected = new int[SUB_PAIRS.length];
        double[] values = new double[SUB_PAIRS.length];
        for (int i = 0; i < SUB_PAIRS.length; i++) {
            subSelected[i] = dropdownGroups[SUB_PAIRS[i][0]].logic;
            values[i] = fetchOdds(mainSelected, subSelected[i],
                    dropdownGroups[SUB_PAIRS[i][1]].logic);
        }

        // 需求 5：三路数据按规则合并为结果1；超过 100% 时直接取 100%（后续乘法基于钳制后的值计算）
        double result1 = mergeValues(values, subSelected);
        if (result1 > 1) {
            result1 = 1;
        }

        // 四叶草：得到最终结果1 后乘 FOUR_LEAF_CLOVER 选中项对应倍率（未选中则为 1.0），
        // 乘积若再次超过 100% 仍直接取 100%
        int cloverSelected = dropdownGroups[GROUP_FOUR_LEAF_CLOVER].logic;
        if (cloverSelected >= 0 && cloverSelected < CardOddsData.FourLeafClover.length) {
            result1 *= CardOddsData.FourLeafClover[cloverSelected];
        }
        if (result1 > 1) {
            result1 = 1;
        }

        // 需求 6：读取 VIP 与 Consortia 选中值取对应加成，求和后乘结果1 得结果2；
        // 只选中其中一个时加成直接用选中项的值（未选中项不参与加成），
        // 两个都选中时两者相加，两个都未选中时加成为 0（此时结果2 为 0）
        int vipSelected = dropdownGroups[GROUP_VIP].logic;
        int consortiaSelected = dropdownGroups[GROUP_CONSORTIA].logic;
        double bonus = 0;
        if (vipSelected >= 0 && vipSelected < CardOddsData.VIP.length) {
            bonus += CardOddsData.VIP[vipSelected];
        }
        if (consortiaSelected >= 0 && consortiaSelected < CardOddsData.Consortia.length) {
            bonus += CardOddsData.Consortia[consortiaSelected];
        }
        double result2 = result1 * bonus;

        // 需求 7：三段成功率分别写入 A/B/C 三张卡（C 为 A、B 两个显示值之和，
        // 按显示值相加保证画面上 A+B 恒等于 C）
        String percentA = formatPercent(result1);
        String percentB = formatPercent(result2);
        String percentC = new BigDecimal(percentA).add(new BigDecimal(percentB)).toPlainString();

        // 需求 8：percentA = 100 保底卡为“本次必成”；percentC = 0 保底不适用显示“—”；
        // 其余情况求最小整数 n 使 percentC × n 严格超过 120，n + 1 即保底次数
        String guaranteeText;
        BigDecimal cValue = new BigDecimal(percentC);
        if (new BigDecimal(percentA).compareTo(new BigDecimal("100")) == 0) {
            guaranteeText = "本次必成";
        } else if (cValue.compareTo(BigDecimal.ZERO) == 0) {
            guaranteeText = "—";
        } else {
            // 120/percentC 向下取整 + 1 = 首个严格超过 120 的倍数（整除时 +1 保证 > 而非 =）
            long multiplier = BigDecimal.valueOf(120)
                    .divide(cValue, 0, RoundingMode.DOWN)
                    .add(BigDecimal.ONE)
                    .longValue();
            guaranteeText = (multiplier + 1) + "次";
        }
        percentAView.setText(percentA + "%");
        percentBView.setText(percentB + "%");
        percentCView.setText(percentC + "%");
        minimumGuaranteeView.setText(guaranteeText);

        // 金币/保险金按 MAIN 选中等级取数；未选中（或下标越界）显示 0
        boolean hasMainCost = mainSelected >= 0
                && mainSelected < CardOddsData.Coin.length
                && mainSelected < CardOddsData.Insurance.length;
        coinView.setText(String.valueOf(hasMainCost ? CardOddsData.Coin[mainSelected] : 0));
        insuranceView.setText(String.valueOf(hasMainCost ? CardOddsData.Insurance[mainSelected] : 0));

        // 文案可能超出卡片宽度：布局完成后按可用宽度自动缩字号，使其刚好一行显示完
        for (TextView view : new TextView[]{percentAView, percentBView, percentCView,
                coinView, insuranceView, minimumGuaranteeView}) {
            view.post(() -> fitTextViewToSingleLine(view));
        }

        // 同步展示区：各组选中项图片/文字 + 副卡类型当前选择文本（覆盖初始/点击/恢复三处触发点）
        refreshDisplay();
    }

    /**
     * 绑定结果展示组件并记录其原始字号（供缩字适配前回到该字号）
     */
    private TextView bindResultView(int viewId) {
        TextView view = findViewById(viewId);
        originalTextSizes.put(view, view.getTextSize());
        return view;
    }

    /**
     * 展示文案自动缩字号（每次重算 setText 后 post 调用，此时宽度已就绪）：
     * 先回到原始字号，若整串文本超出可用宽度（单行、不换行、不加省略号），
     * 按 “原始字号 × 可用宽 / 文本宽” 等比缩小（同一字符串渲染宽度与 textSize 成正比），
     * 再向下微调吸收字形渲染的舍入误差，得到刚好能一行显示完的最大字号
     */
    private void fitTextViewToSingleLine(TextView view) {
        // 显式判空后再拆箱：未登记过原始字号（不应发生）时取 0，
        // 由下方 originalSizePx <= 0 分支跳过本次适配
        Float recordedSize = originalTextSizes.get(view);
        float originalSizePx = recordedSize == null ? 0f : recordedSize;
        int available = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        if (available <= 0 || originalSizePx <= 0) return; // 尚未布局，跳过本次适配

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalSizePx);
        String text = view.getText().toString();
        float textWidth = view.getPaint().measureText(text);
        if (textWidth <= available) return; // 放得下，保持原始字号

        float newSize = originalSizePx * available / textWidth;
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        // 等比值可能因字形舍入仍略超宽，逐步下调直到放得下（下限 1px 防死循环）
        while (newSize > 1f && view.getPaint().measureText(text) > available) {
            newSize -= 0.5f;
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        }
    }

    /**
     * 展示区刷新（每次重算末尾统一调用）：
     * 1. 将 MAIN/SUB_1/SUB_2/SUB_3/FOUR_LEAF_CLOVER/VIP 各组选中项的图片
     *    分别同步到对应 display ImageView，未选中（-1）时统一显示 card_data_x；
     * 2. 公会合成屋文字展示位：未选中显示“无”，选中显示等级数字；
     * 3. 三个副卡类型行的当前选择文本按菜单顺序（好/中/差）同步显示
     */
    private void refreshDisplay() {
        updateImageDisplay(displayStarMain, STAR_DRAWABLES, dropdownGroups[GROUP_MAIN].logic);
        updateImageDisplay(displayStarSub1, STAR_DRAWABLES, dropdownGroups[GROUP_SUB_1].logic);
        updateImageDisplay(displayStarSub2, STAR_DRAWABLES, dropdownGroups[GROUP_SUB_2].logic);
        updateImageDisplay(displayStarSub3, STAR_DRAWABLES, dropdownGroups[GROUP_SUB_3].logic);
        updateImageDisplay(displayFourLeafClover, CLOVER_DRAWABLES,
                dropdownGroups[GROUP_FOUR_LEAF_CLOVER].logic);
        updateImageDisplay(displayVip, VIP_DRAWABLES, dropdownGroups[GROUP_VIP].logic);

        // 公会合成屋等级是文字展示位：未选中显示“无”，选中显示 1~6
        int consortiaSelected = dropdownGroups[GROUP_CONSORTIA].logic;
        displayConsortia.setText(consortiaSelected >= 0
                ? String.valueOf(consortiaSelected + 1)
                : getString(R.string.title_card_odds_calculator_none));

        // 副卡类型行的当前选择文本（菜单顺序：好卡、中卡、差卡）
        for (int categoryGroup : new int[]{GROUP_SUB_1_CATEGORY, GROUP_SUB_2_CATEGORY, GROUP_SUB_3_CATEGORY}) {
            DropdownGroup group = dropdownGroups[categoryGroup];
            group.selectionText.setText(group.labels[group.menuIndexOfLogic()]);
        }
    }

    /**
     * 将本组选中值对应的图片同步到展示位：
     * 未选中（-1）或下标越界时显示 card_data_x
     */
    private void updateImageDisplay(ImageView display, int[] drawables, int logic) {
        if (display == null) return;
        display.setImageResource(logic >= 0 && logic < drawables.length
                ? drawables[logic] : R.drawable.card_data_x);
    }

    /**
     * 第 3/4 步的单路取数：MAIN 值定行、SUB 值定该行第几个数据、
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
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout cardOddsCalculatorContainer = findViewById(R.id.CardOddsCalculator_Container);
        InsetsUtil.setMarginHorizontal(this, cardOddsCalculatorContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardOddsCalculatorContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            cardOddsCalculatorContainer.setLayoutParams(params);

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

        // 添加按压动画（三张成功率卡与两张提示卡；helper 不消费事件，不影响后续点击功能）
        findViewById(R.id.percent_a_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.percent_b_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.percent_c_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.coin_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.insurance_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.minimum_guarantee_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_card_odds_calculator_sub_card_category).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_card_odds_calculator_minimum_guarantee).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
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
        // 保存各组当前选中的逻辑值，供界面重建（深浅色切换等）后恢复
        if (dropdownGroups != null) {
            for (DropdownGroup group : dropdownGroups) {
                group.saveState(outState);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }

        // 恢复重建前各组选中的逻辑值（restoreState 不触发重算，恢复完毕后统一处理）
        if (dropdownGroups != null) {
            for (DropdownGroup group : dropdownGroups) {
                group.restoreState(savedInstanceState);
            }
            // 恢复完成后按恢复出的选中项重新计算一次（同步刷新展示区与结果）
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
