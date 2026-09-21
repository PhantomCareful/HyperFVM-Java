package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category.CategoryItem;
import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.IndexItem.IndexItem;
import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category.CategoryArray;
import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.OddsDisclosure.OddsItem;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

public class OddsDisclosureHelper {

    /**
     * 在概率公示详情页加载所有概率条目
     * @param container 用于承载内容的容器
     * @param array 概率条目数组
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void loadOddsDisclosureInfo(Context context, LinearLayout container, OddsItem[] array) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // 根据数组内精度最高的概率，统一决定整个数组显示的小数位数
        int precision = OddsItem.calculateOddsTextPrecision(array);

        // 逐个添加概率条目
        for (OddsItem oddsItem : array) {
            CardView cardView = (CardView) layoutInflater.inflate(oddsItem.isExpected() ? R.layout.item_odds_item_expected : R.layout.item_odds_item, container, false);
            ImageView image = cardView.findViewById(R.id.odds_image);
            TextView nameAndCount = cardView.findViewById(R.id.odds_name_and_count);
            TextView odds = cardView.findViewById(R.id.odds_odds);

            image.setImageResource(oddsItem.getItemImageId());
            nameAndCount.setText(oddsItem.getItemNameAndCount());
            odds.setText(oddsItem.getOddsText(precision));

            // 添加按压动画
            cardView.setOnTouchListener((v, event) ->
                    setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));

            container.addView(cardView);
        }
    }

    /**
     * 在宝箱概率公示合集目录页加载所有类别及其对应的内容
     * @param odds_disclosure_index_item_container 用于承载内容的容器
     */
    public static void loadIndexItem(Context context, LinearLayout odds_disclosure_index_item_container) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        // 逐个添加每个类别，先添加标题，再添加每个标题对应的内容
        for (CategoryItem categoryItem : CategoryArray.array) {
            // 添加标题
            TextView title = (TextView) layoutInflater.inflate(R.layout.item_odds_category_title, odds_disclosure_index_item_container, false);
            title.setText(categoryItem.getItemTitle());

            odds_disclosure_index_item_container.addView(title);

            // 添加对应的内容
            IndexItem[] indexItemArray = categoryItem.getIndexArray();
            for(IndexItem indexItem : indexItemArray) {
                CardView cardView = (CardView) layoutInflater.inflate(R.layout.item_odds_index_item, odds_disclosure_index_item_container, false);
                LinearLayout container = cardView.findViewById(R.id.odds_index_container);
                ImageView itemImage = cardView.findViewById(R.id.odds_index_image);
                TextView itemName = cardView.findViewById(R.id.odds_index_name);

                itemImage.setImageResource(indexItem.getItemImageId());
                itemName.setText(indexItem.getItemName());

                // 设置点击事件
                container.setOnClickListener(v -> {
                    Intent intent = new Intent(context, OddsDisclosureInfoActivity.class);
                    intent.putExtra("topBarTitle", indexItem.getTopBarName());
                    intent.putExtra("oddsDisclosureInfo", indexItem.getOddsArray());
                    context.startActivity(intent);
                });

                odds_disclosure_index_item_container.addView(cardView);
            }

            // 添加一个高度为15dp的<Space>作为占位
            Space space = new Space(context);
            odds_disclosure_index_item_container.addView(space, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dpToPx(context, 15)));

        }
    }
}
