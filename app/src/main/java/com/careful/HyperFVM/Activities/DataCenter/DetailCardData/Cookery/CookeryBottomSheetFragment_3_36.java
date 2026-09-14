package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;

public class CookeryBottomSheetFragment_3_36 extends BaseCookeryBottomSheetFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 加载布局
        return inflater.inflate(R.layout.fragment_bottom_sheet_cookery_3_36, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 在这里可以找到内部的子控件并设置监听，例如：
        TextView card_data_cookery_base_info_category_and_reward = view.findViewById(R.id.card_data_cookery_base_info_category_and_reward);
        card_data_cookery_base_info_category_and_reward.setText(
                requireContext().getResources().getString(R.string.category_card_data_cookery_3) + "·" + requireContext().getResources().getString(R.string.reward_card_data_cookery_3_36)
        );

        view.findViewById(R.id.card_data_cookery_compose_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "旋转咖啡喷壶"));
        view.findViewById(R.id.card_data_cookery_compose_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "冰块冷萃机"));
        view.findViewById(R.id.card_data_cookery_compose_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "麦芽糖"));

        view.findViewById(R.id.card_data_index_8_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "波塞冬神使"));
        view.findViewById(R.id.card_data_index_8_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "巫蛊蛇"));
        view.findViewById(R.id.card_data_index_8_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "元气牛"));
        view.findViewById(R.id.card_data_index_8_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "可乐汪"));
        view.findViewById(R.id.card_data_index_8_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "转转鸡"));
    }
}