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
public class CookeryBottomSheetFragment_3_26 {
    @SuppressLint("SetTextI18n")
    public static void bind(View view) {

        // 在这里可以找到内部的子控件并设置监听，例如：
        TextView card_data_cookery_base_info_category_and_reward = view.findViewById(R.id.card_data_cookery_base_info_category_and_reward);
        card_data_cookery_base_info_category_and_reward.setText(
                view.getContext().getResources().getString(R.string.category_card_data_cookery_3) + "·" + view.getContext().getResources().getString(R.string.reward_card_data_cookery_3_26)
        );

        view.findViewById(R.id.card_data_cookery_compose_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "瓜皮护罩"));
        view.findViewById(R.id.card_data_cookery_compose_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "扑克牌护罩"));
        view.findViewById(R.id.card_data_cookery_compose_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "老虎蟹面包"));

        view.findViewById(R.id.card_data_index_x1124002a).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "赫拉神使"));
        view.findViewById(R.id.card_data_index_x11240030).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "祥龙环"));
        view.findViewById(R.id.card_data_index_x11240040).setOnClickListener(v -> CardDataHelper.selectCardDataByName(view.getContext(), "彩虹蛇"));
    }
}