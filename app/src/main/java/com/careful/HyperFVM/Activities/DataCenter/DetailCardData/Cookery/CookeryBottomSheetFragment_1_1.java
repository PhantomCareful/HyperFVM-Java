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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CookeryBottomSheetFragment_1_1 extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 加载布局
        return inflater.inflate(R.layout.fragment_bottom_sheet_cookery_1_1, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 在这里可以找到内部的子控件并设置监听，例如：
        TextView card_data_cookery_base_info_category_and_reward = view.findViewById(R.id.card_data_cookery_base_info_category_and_reward);
        card_data_cookery_base_info_category_and_reward.setText(
                requireContext().getResources().getString(R.string.category_card_data_cookery_1) + "·" + requireContext().getResources().getString(R.string.reward_card_data_cookery_1_1)
        );

        view.findViewById(R.id.card_data_cookery_compose_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "小笼包"));
        view.findViewById(R.id.card_data_cookery_compose_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "冰冻小笼包"));

        view.findViewById(R.id.card_data_index_1_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "小笼包"));
        view.findViewById(R.id.card_data_index_1_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "双层小笼包"));
        view.findViewById(R.id.card_data_index_1_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "三向小笼包"));
        view.findViewById(R.id.card_data_index_1_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "机枪小笼包"));
        view.findViewById(R.id.card_data_index_1_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "冰冻小笼包"));
        view.findViewById(R.id.card_data_index_1_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "双层冰冻小笼包"));
        view.findViewById(R.id.card_data_index_1_3_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "三向冰冻小笼包"));
        view.findViewById(R.id.card_data_index_1_3_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "机枪冰冻小笼包"));
        view.findViewById(R.id.card_data_index_1_3_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), "包包龙"));
    }
}