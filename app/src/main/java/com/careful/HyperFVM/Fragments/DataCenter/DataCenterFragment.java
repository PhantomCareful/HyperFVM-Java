package com.careful.HyperFVM.Fragments.DataCenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.DataCenter.CardDataIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImagesIndexActivity;
import com.careful.HyperFVM.Activities.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Activities.TodayLuckyActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataCenterBinding;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.google.android.material.card.MaterialCardView;

public class DataCenterFragment extends Fragment {
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDataCenterBinding binding = FragmentDataCenterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化各种装饰效果
        initDecoration();

        // ------------------------------ 设置点击事件 ------------------------------
        // 防御卡全能数据库
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnClickListener(v -> {
            TextView DataCenter_CardDataIndex_Content = root.findViewById(R.id.DataCenter_CardDataIndex_Content);
            DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index_loading));
            Intent intent = new Intent(requireActivity(), CardDataIndexActivity.class);
            startActivity(intent);
        });

        // 数据图合集
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), DataImagesIndexActivity.class);
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
                0,
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

        // FVM查黑系统
        root.findViewById(R.id.card_icu_container).setOnClickListener(v ->
                DialogBuilderManager.showIcuQQInputDialog(requireContext()));

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
                getResources().getString(R.string.dialog_title_tools_gem_calculator),
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
     * 在onResume阶段还原卡片状态，确保从二级界面返回后文案是恢复了的
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 还原卡片状态
        TextView DataCenter_CardDataIndex_Content = root.findViewById(R.id.DataCenter_CardDataIndex_Content);
        DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index));
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
        MaterialCardView topBarContainer = root.findViewById(R.id.TopBar_Container);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), root, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout dataCenterContainer = root.findViewById(R.id.DataCenter_Container);
        InsetsUtil.setMarginHorizontal(requireContext(), dataCenterContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) dataCenterContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            dataCenterContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewTopBar), root.findViewById(R.id.targetView));
    }
}
