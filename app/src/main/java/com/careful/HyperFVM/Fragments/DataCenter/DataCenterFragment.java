package com.careful.HyperFVM.Fragments.DataCenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.DataCenter.CardDataIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.CookeryIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImagesIndexActivity;
import com.careful.HyperFVM.Activities.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Activities.TodayLuckyActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataCenterBinding;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

import eightbitlab.com.blurview.BlurView;

public class DataCenterFragment extends Fragment {
    // 保存/恢复滚动位置的key，用于深浅色切换等界面重建后恢复顶部栏透明度状态
    private static final String STATE_SCROLL_Y = "state_data_center_scroll_y";
    // 顶部栏渐变过渡区间（dp）：暂与Dashboard一致为50dp，待实测调整
    private static final int TOP_BAR_FADE_RANGE_DP = 50;

    private View root;

    // 顶部栏滚动联动
    private NestedScrollUtil nestedScrollUtil;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDataCenterBinding binding = FragmentDataCenterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化各种装饰效果
        initDecoration();

        // ------------------------------ 设置点击事件 ------------------------------
        // 防御卡全能数据库
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CardDataIndexActivity.class);
            startActivity(intent);
        });

        // 数据图合集
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), DataImagesIndexActivity.class);
            startActivity(intent);
        });

        // 食神谱合集
        root.findViewById(R.id.DataCenter_CookeryIndex_Container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), CookeryIndexActivity.class);
            startActivity(intent);
        });

        // 提拉米鼠官网
        root.findViewById(R.id.card_tiramisu_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_tiramisu),
                100,
                getResources().getString(R.string.dialog_title_tiramisu),
                "",
                getResources().getString(R.string.dialog_url_tiramisu)));

        // 陌路の综合数据表
        root.findViewById(R.id.card_molu_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_molu),
                100,
                getResources().getString(R.string.dialog_title_molu),
                "",
                getResources().getString(R.string.dialog_url_molu)));

        // FAA米苏物流
        root.findViewById(R.id.card_faa_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_faa),
                100,
                getResources().getString(R.string.dialog_title_faa),
                "",
                getResources().getString(R.string.dialog_url_faa)));

        // 轨道强卡统计
        root.findViewById(R.id.card_guidao_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_guidao),
                0,
                getResources().getString(R.string.dialog_title_guidao),
                "",
                getResources().getString(R.string.dialog_url_guidao)));

        // 卡片鼠军对策表
        root.findViewById(R.id.card_strategy_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_strategy),
                0,
                getResources().getString(R.string.dialog_title_strategy),
                "",
                getResources().getString(R.string.dialog_url_strategy)));

        // 巅峰对决部分机制解析
        root.findViewById(R.id.card_strategy_world_boss_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.dashboard_world_boss),
                0,
                getResources().getString(R.string.dialog_title_strategy_world_boss),
                "",
                getResources().getString(R.string.dialog_url_strategy_world_boss)));

        // 强卡最优路径计算器
        root.findViewById(R.id.card_card_calculator_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_card_calculator),
                25,
                getResources().getString(R.string.dialog_title_card_calculator),
                "",
                getResources().getString(R.string.dialog_url_card_calculator)));

        // 宝石最优路径计算器
        root.findViewById(R.id.card_gem_calculator_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.data_center_image_gem_calculator),
                25,
                getResources().getString(R.string.dialog_title_gem_calculator),
                "",
                getResources().getString(R.string.dialog_url_gem_calculator)));

        // 今日运势
        root.findViewById(R.id.card_today_lucky_container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), TodayLuckyActivity.class);
            startActivity(intent);
        });

        // 威望计算器
        root.findViewById(R.id.card_prestige_calculator_container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), PrestigeCalculatorActivity.class);
            startActivity(intent);
        });

        return root;
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = root.findViewById(R.id.blurViewTopBar);
        TextView topBar = root.findViewById(R.id.topBar);
        ImageButton floatButtonIcu = root.findViewById(R.id.FloatButton_Icu);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), root, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(requireContext(), 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonIcu.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(requireContext(), 5);
            floatButtonIcu.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout dataCenterContainer = root.findViewById(R.id.DataCenter_Container);
        InsetsUtil.setMarginHorizontal(requireContext(), dataCenterContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) dataCenterContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            dataCenterContainer.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        // FVM查黑系统
        floatButtonIcu.setOnClickListener(v -> DialogBuilderManager.showIcuQQInputDialog(requireContext()));

        // 添加模糊材质
        setupBlurEffect();

        // 添加顶部栏滚动联动：上滑时大标题淡出、悬浮小标题与模糊层淡入
        nestedScrollUtil = NestedScrollUtil.attach(root, R.id.scrollView, R.id.topBarBottom,
                R.id.topBar, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewTopBar), root.findViewById(R.id.targetView));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存滚动位置，供界面重建（旋转/深浅色切换等）后恢复顶部栏透明度状态
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // 恢复滚动位置并同步透明度（内部会在首帧绘制前按最终滚动位置同步，避免突变回初始状态）
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }
}
