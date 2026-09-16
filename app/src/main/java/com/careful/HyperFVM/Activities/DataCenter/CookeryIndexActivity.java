package com.careful.HyperFVM.Activities.DataCenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

public class CookeryIndexActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_cookery_index_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）
    // 章节目录跳转的目标分节标题（与 R.array.cookery_index_entries 选项一一对应）
    private static final int[] SECTION_TITLE_IDS = {R.id.cookery_index_1, R.id.cookery_index_2, R.id.cookery_index_3};

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）

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
        setContentView(R.layout.activity_cookery_index);

        // 初始化各种装饰效果
        initDecoration();

        // 为食神谱图鉴所有卡片绑定点击事件（点击弹出对应食神谱的 BottomSheet）
        setupCookeryClickListeners();
    }

    /**
     * 为食神谱图鉴所有卡片绑定点击事件（点击弹出对应食神谱的 BottomSheet）
     */
    private void setupCookeryClickListeners() {
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_1), findViewById(R.id.cookery_index_1_1), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_2), findViewById(R.id.cookery_index_1_2), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_3), findViewById(R.id.cookery_index_1_3), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_4), findViewById(R.id.cookery_index_1_4), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_5), findViewById(R.id.cookery_index_1_5), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_6), findViewById(R.id.cookery_index_1_6), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_1_7), findViewById(R.id.cookery_index_1_7), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_1), findViewById(R.id.cookery_index_2_1), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_2), findViewById(R.id.cookery_index_2_2), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_3), findViewById(R.id.cookery_index_2_3), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_4), findViewById(R.id.cookery_index_2_4), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_5), findViewById(R.id.cookery_index_2_5), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_6), findViewById(R.id.cookery_index_2_6), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_7), findViewById(R.id.cookery_index_2_7), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_2_8), findViewById(R.id.cookery_index_2_8), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_1), findViewById(R.id.cookery_index_3_1), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_2), findViewById(R.id.cookery_index_3_2), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_3), findViewById(R.id.cookery_index_3_3), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_4), findViewById(R.id.cookery_index_3_4), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_5), findViewById(R.id.cookery_index_3_5), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_6), findViewById(R.id.cookery_index_3_6), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_7), findViewById(R.id.cookery_index_3_7), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_8), findViewById(R.id.cookery_index_3_8), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_9), findViewById(R.id.cookery_index_3_9), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_10), findViewById(R.id.cookery_index_3_10), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_11), findViewById(R.id.cookery_index_3_11), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_12), findViewById(R.id.cookery_index_3_12), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_13), findViewById(R.id.cookery_index_3_13), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_14), findViewById(R.id.cookery_index_3_14), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_15), findViewById(R.id.cookery_index_3_15), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_16), findViewById(R.id.cookery_index_3_16), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_17), findViewById(R.id.cookery_index_3_17), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_18), findViewById(R.id.cookery_index_3_18), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_19), findViewById(R.id.cookery_index_3_19), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_20), findViewById(R.id.cookery_index_3_20), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_21), findViewById(R.id.cookery_index_3_21), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_22), findViewById(R.id.cookery_index_3_22), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_23), findViewById(R.id.cookery_index_3_23), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_24), findViewById(R.id.cookery_index_3_24), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_25), findViewById(R.id.cookery_index_3_25), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_26), findViewById(R.id.cookery_index_3_26), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_27), findViewById(R.id.cookery_index_3_27), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_28), findViewById(R.id.cookery_index_3_28), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_29), findViewById(R.id.cookery_index_3_29), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_30), findViewById(R.id.cookery_index_3_30), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_31), findViewById(R.id.cookery_index_3_31), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_32), findViewById(R.id.cookery_index_3_32), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_33), findViewById(R.id.cookery_index_3_33), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_34), findViewById(R.id.cookery_index_3_34), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_35), findViewById(R.id.cookery_index_3_35), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_36), findViewById(R.id.cookery_index_3_36), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_37), findViewById(R.id.cookery_index_3_37), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_38), findViewById(R.id.cookery_index_3_38), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_39), findViewById(R.id.cookery_index_3_39), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_40), findViewById(R.id.cookery_index_3_40), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_41), findViewById(R.id.cookery_index_3_41), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_42), findViewById(R.id.cookery_index_3_42), getSupportFragmentManager());
        CardDataHelper.selectCookeryByName(getString(R.string.name_card_data_cookery_3_43), findViewById(R.id.cookery_index_3_43), getSupportFragmentManager());
    }

    /**
     * 以目录按钮为锚点弹出章节跳转下拉菜单（样式参照设置页下拉菜单，仅去掉了选中对勾）
     */
    private void showIndexDropdown(View anchor) {
        String[] entries = getResources().getStringArray(R.array.cookery_index_entries);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown_selection, entries) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView != null ? convertView
                        : getLayoutInflater().inflate(R.layout.item_dropdown_selection, parent, false);
                ((TextView) row.findViewById(R.id.option_text)).setText(getItem(position));
                // 章节目录不需要选中对勾
                row.findViewById(R.id.option_check).setVisibility(View.GONE);
                return row;
            }
        };

        // 实测选项宽高：菜单宽度取最宽选项（wrap_content 效果）。ListView 在 PopupWindow 中无法
        // 真正 wrap_content，需自行测量内容宽后按像素设置；getView 的 parent 形参标注 @NonNull，
        // 这里传一个仅用于生成 LayoutParams 的空容器（不挂载子视图），避免传 null
        ViewGroup measureParent = new FrameLayout(this);
        int contentWidth = 0;
        int itemHeight = 0;
        for (int i = 0; i < entries.length; i++) {
            View itemView = adapter.getView(i, null, measureParent);
            itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            itemHeight = itemView.getMeasuredHeight();
        }
        // 极端大字体下限制菜单宽度不超出屏幕
        contentWidth = Math.min(contentWidth, anchor.getRootView().getWidth());

        ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAnchorView(anchor);
        popup.setWidth(contentWidth);
        popup.setHeight(itemHeight * entries.length);
        popup.setVerticalOffset((int) (4 * getResources().getDisplayMetrics().density));
        // 菜单右缘与按钮右缘对齐（水平偏移取负值左移），点击行外区域自动关闭
        popup.setHorizontalOffset(anchor.getWidth() - contentWidth);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_dropdown_background));
        popup.setAdapter(adapter);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            scrollToSectionTitle(findViewById(SECTION_TITLE_IDS[position]));
        });
        popup.show();
    }

    /**
     * 平滑滚动到指定分节标题：标题最终停在顶部模糊栏下方（留一点间距），滚动中正常联动顶部栏渐显
     */
    private void scrollToSectionTitle(View titleView) {
        ScrollView scrollView = findViewById(R.id.scrollView);
        View blurViewTopBar = findViewById(R.id.blurViewTopBar);
        int[] scrollLocation = new int[2];
        int[] titleLocation = new int[2];
        scrollView.getLocationInWindow(scrollLocation);
        titleView.getLocationInWindow(titleLocation);
        // 目标位置 = 标题内容坐标 - 顶部模糊栏高度 - 呼吸间距（ScrollView 内部会自行 clamp 边界）
        int prefixSpace = blurViewTopBar.getHeight() + DensityUtil.dpToPx(this, 10);
        int targetY = scrollView.getScrollY() + (titleLocation[1] - scrollLocation[1]) - prefixSpace;
        scrollView.smoothScrollTo(0, targetY);
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
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        ImageButton floatButtonIndex = findViewById(R.id.FloatButton_Index);
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

            params = (ViewGroup.MarginLayoutParams) floatButtonIndex.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonIndex.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout cookeryIndexContainer = findViewById(R.id.CookeryIndex_Container);
        InsetsUtil.setMarginHorizontal(this, cookeryIndexContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cookeryIndexContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            cookeryIndexContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonIndex.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonIndex.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonIndex.setOnClickListener(this::showIndexDropdown);

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        nestedScrollUtil = NestedScrollUtil.attach(
                findViewById(R.id.scrollView),
                findViewById(R.id.topBarBottom),
                topBar,
                blurViewTopBar,
                TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
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
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
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