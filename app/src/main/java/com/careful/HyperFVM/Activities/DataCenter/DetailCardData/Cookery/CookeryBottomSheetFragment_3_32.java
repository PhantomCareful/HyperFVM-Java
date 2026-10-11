package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;


import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;

/**
 * Static content binder for one cookery card (formerly a DialogFragment,
 * converted for the Miuix bottom sheet rollout; see CookeryBottomSheetModal).
 */
public class CookeryBottomSheetFragment_3_32 {
    @SuppressLint("SetTextI18n")
    public static void bind(View view) {

        // 在这里可以找到内部的子控件并设置监听，例如：
        TextView card_data_cookery_base_info_category_and_reward = view.findViewById(R.id.card_data_cookery_base_info_category_and_reward);
        card_data_cookery_base_info_category_and_reward.setText(
                view.getContext().getResources().getString(R.string.category_card_data_cookery_3) + "·" + view.getContext().getResources().getString(R.string.reward_card_data_cookery_3_32)
        );

        view.findViewById(R.id.card_data_cookery_compose_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "耗油双菇"));
        view.findViewById(R.id.card_data_cookery_compose_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "怪味鱿鱼"));
        view.findViewById(R.id.card_data_cookery_compose_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "扑克牌护罩"));
        view.findViewById(R.id.card_data_cookery_compose_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "炸炸菇"));
        view.findViewById(R.id.card_data_cookery_compose_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "樱桃反弹布丁"));

        view.findViewById(R.id.card_data_index_x11120690).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "耗油双菇"));
        view.findViewById(R.id.card_data_index_x11700060).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "热狗耗油双菇"));
        view.findViewById(R.id.card_data_index_x11120760).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "怪味鱿鱼"));
        view.findViewById(R.id.card_data_index_x11700010).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "火影怪味鱿鱼"));
        view.findViewById(R.id.card_data_index_x11250010).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "扑克牌护罩"));
    }
}