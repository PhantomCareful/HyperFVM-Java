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
public class CookeryBottomSheetFragment_2_1 {
    @SuppressLint("SetTextI18n")
    public static void bind(View view) {

        // 在这里可以找到内部的子控件并设置监听，例如：
        TextView card_data_cookery_base_info_category_and_reward = view.findViewById(R.id.card_data_cookery_base_info_category_and_reward);
        card_data_cookery_base_info_category_and_reward.setText(
                view.getContext().getResources().getString(R.string.category_card_data_cookery_2) + "·" + view.getContext().getResources().getString(R.string.reward_card_data_cookery_2_1)
        );

        view.findViewById(R.id.card_data_cookery_compose_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "双层小笼包"));
        view.findViewById(R.id.card_data_cookery_compose_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "双层冰冻小笼包"));

        view.findViewById(R.id.card_data_index_x11130014).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "小笼包"));
        view.findViewById(R.id.card_data_index_x11130020).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "双层小笼包"));
        view.findViewById(R.id.card_data_index_x11130100).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "三向小笼包"));
        view.findViewById(R.id.card_data_index_x11930010).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "机枪小笼包"));
        view.findViewById(R.id.card_data_index_x11130050).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "冰冻小笼包"));
        view.findViewById(R.id.card_data_index_x11130160).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "双层冰冻小笼包"));
        view.findViewById(R.id.card_data_index_x11130170).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "三向冰冻小笼包"));
        view.findViewById(R.id.card_data_index_x11930070).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "机枪冰冻小笼包"));
        view.findViewById(R.id.card_data_index_x11122320).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "包包龙"));
    }
}