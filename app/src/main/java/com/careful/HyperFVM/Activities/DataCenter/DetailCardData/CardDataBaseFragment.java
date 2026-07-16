package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

public class CardDataBaseFragment extends Fragment {
    private final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    private View root;

    private String cardName;
    private String tableName;
    private int tableId;

    private int savedScrollY = 0;// 用于保存/恢复的滚动位置
    private int imageViewCardBig1ContainerMaxScroll;// 判定完全消失的滚动距离（dp 转 px）
    private int imageViewCardBig2ContainerMaxScroll;// 判定完全消失的滚动距离（dp 转 px）

    private TransitionSet transition;
    private LinearLayout bigImageContainer;
    private LinearLayout cardDataContainer;
    private View Image_View_Card_Big_1_Container;
    private View Image_View_Card_Big_2_Container;

    public CardDataBaseFragment newInstance(String cardName, String tableName) {
        CardDataBaseFragment fragment = new CardDataBaseFragment();
        Bundle bundle = new Bundle();
        bundle.putString("cardName", cardName);
        bundle.putString("tableName", tableName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 是否启用动态背景
        boolean isDynamicBackground = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_BACKGROUND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;

        if (isDynamicBackground) {
            root = inflater.inflate(R.layout.fragment_card_data_base_effect, container, false);
        } else {
            root = inflater.inflate(R.layout.fragment_card_data_base, container, false);
        }

        if (getArguments() != null) {
            cardName = getArguments().getString("cardName");
            tableName = getArguments().getString("tableName");
        }

        // 来自第几张数据表
        if (tableName != null) {
            tableId = Integer.parseInt(tableName.split("card_data_")[1]);
        }

        bigImageContainer = root.findViewById(R.id.big_image_container);
        cardDataContainer = root.findViewById(R.id.card_data_container);

        // 查询卡片数据并显示
        queryAndShowCardData();

        // 初始化各种装饰效果
        initDecoration();

        return root;
    }

    @SuppressLint({"Range", "DiscouragedApi"})
    private void queryAndShowCardData() {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                Toast.makeText(requireContext(), "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            String cardName0;
            String cardName1;
            String cardName2;
            String cardName3;
            
            // 展示大图
            ImageView ImageViewCardBig;
            String imageIdStr;
            int imageResId;
            switch (tableId) {
                case 1, 4:
                    // 普通卡、星座卡、生肖卡
                    cardName0 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));

                    // 不转一定有，所以总是启用Image_View_Card_Big_1_1_Container
                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_1_1, cardName0);

                    // 判断这张卡是否只有不转
                    if (cursor.getString(cursor.getColumnIndex("image_id_1")).equals("无")) {
                        // 这张卡只有不转，隐藏剩下的组件
                        root.findViewById(R.id.Image_View_Card_Big_1_2_Container).setVisibility(View.GONE);
                        root.findViewById(R.id.Image_View_Card_Big_2_Container).setVisibility(View.GONE);

                        // 调整容器顶部距离
                        cardDataContainer.setPadding(
                                cardDataContainer.getPaddingLeft(),
                                DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 320 : 120),
                                cardDataContainer.getPaddingRight(),
                                cardDataContainer.getPaddingBottom()
                        );
                    } else {
                        // 这张卡有一转，启用Image_View_Card_Big_1_2_Container
                        ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_2);
                        imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big";
                        // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                        imageResId = getResources().getIdentifier(
                                imageIdStr,
                                "drawable",
                                requireContext().getPackageName()
                        );
                        ImageViewCardBig.setImageResource(imageResId);
                        setTextToView(R.id.card_name_1_2, cardName1);

                        // 再判断是否有二转
                        if (!cursor.getString(cursor.getColumnIndex("image_id_2")).equals("无")) {
                            // 这张卡有二转，启用Image_View_Card_Big_2_1_Container
                            ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_2_1);
                            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big";
                            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                            imageResId = getResources().getIdentifier(
                                    imageIdStr,
                                    "drawable",
                                    requireContext().getPackageName()
                            );
                            ImageViewCardBig.setImageResource(imageResId);
                            setTextToView(R.id.card_name_2_1, cardName2);

                            // 隐藏Image_View_Card_Big_2_2_Container
                            root.findViewById(R.id.Image_View_Card_Big_2_2_Container).setVisibility(View.GONE);

                            // 调整容器顶部距离
                            cardDataContainer.setPadding(
                                    cardDataContainer.getPaddingLeft(),
                                    DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 520 : 120),
                                    cardDataContainer.getPaddingRight(),
                                    cardDataContainer.getPaddingBottom()
                            );
                        } else {
                            // 这张卡没有二转，隐藏Image_View_Card_Big_2_Container
                            root.findViewById(R.id.Image_View_Card_Big_2_Container).setVisibility(View.GONE);

                            // 调整容器顶部距离
                            cardDataContainer.setPadding(
                                    cardDataContainer.getPaddingLeft(),
                                    DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 320 : 120),
                                    cardDataContainer.getPaddingRight(),
                                    cardDataContainer.getPaddingBottom()
                            );
                        }
                    }
                    break;
                case 2:
                    // 融合卡
                    cardName1 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    cardName3 = cursor.getString(cursor.getColumnIndex("name_3"));

                    // 初级融合、深度融合、灵魂融合全有
                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_1_1, cardName1);

                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_2);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_1_2, cardName2);

                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_2_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_2_1, cardName3);

                    // 隐藏Image_View_Card_Big_2_2_Container
                    root.findViewById(R.id.Image_View_Card_Big_2_2_Container).setVisibility(View.GONE);

                    // 调整容器顶部距离
                    cardDataContainer.setPadding(
                            cardDataContainer.getPaddingLeft(),
                            DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 520 : 120),
                            cardDataContainer.getPaddingRight(),
                            cardDataContainer.getPaddingBottom()
                    );
                    break;
                case 3:
                    // 金卡
                    cardName0 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    cardName3 = cursor.getString(cursor.getColumnIndex("name_3"));

                    // 不转、三转、四转一定有
                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_1_1, cardName0);

                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_1_2);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_1_2, cardName1);

                    ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_2_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_2_1, cardName2);

                    // 判断是否有终转
                    if (!cursor.getString(cursor.getColumnIndex("image_id_3")).equals("无")) {
                        // 这张卡有终转，启用Image_View_Card_Big_2_2_Container
                        ImageViewCardBig = root.findViewById(R.id.Image_View_Card_Big_2_2);
                        imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_3")) + "_big";
                        // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                        imageResId = getResources().getIdentifier(
                                imageIdStr,
                                "drawable",
                                requireContext().getPackageName()
                        );
                        ImageViewCardBig.setImageResource(imageResId);
                        setTextToView(R.id.card_name_2_2, cardName3);

                        // 调整容器顶部距离
                        cardDataContainer.setPadding(
                                cardDataContainer.getPaddingLeft(),
                                DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 520 : 120),
                                cardDataContainer.getPaddingRight(),
                                cardDataContainer.getPaddingBottom()
                        );
                    } else {
                        // 这张卡没有二转，隐藏Image_View_Card_Big_2_2_Container
                        root.findViewById(R.id.Image_View_Card_Big_2_2_Container).setVisibility(View.GONE);

                        // 调整容器顶部距离
                        cardDataContainer.setPadding(
                                cardDataContainer.getPaddingLeft(),
                                DensityUtil.dpToPx(requireContext(), SmallestWidthUtil.getSmallestWidthDp() < 600 ? 320 : 120),
                                cardDataContainer.getPaddingRight(),
                                cardDataContainer.getPaddingBottom()
                        );
                    }
                    break;
            }

            // 基础信息
            String contentBaseInfo1 = CardDataHelper.getStringFromCursor(cursor, "base_info");
            getContent(requireContext(), root.findViewById(R.id.base_info_1), contentBaseInfo1);

            // 转职凭证信息：仅普通卡
            if (tableId == 1) {
                String contentBaseInfo2 = CardDataHelper.getStringFromCursor(cursor, "transfer_certificate_info");
                if (!contentBaseInfo2.equals("无")) {
                    getContent(requireContext(), root.findViewById(R.id.base_info_2), contentBaseInfo2);
                    root.findViewById(R.id.Card_BaseInfo_2).setVisibility(View.VISIBLE);
                }
            }

            // 三个固定的属性
            String contentBaseInfo3 = "- 所属分类：" + CardDataHelper.getStringFromCursor(cursor, "category") + "\n" +
                    "- 耗能：" + CardDataHelper.getStringFromCursor(cursor, "price") + "\n" +
                    "- 作为副卡：" + CardDataHelper.getStringFromCursor(cursor, "sub_card");
            getContent(requireContext(), root.findViewById(R.id.base_info_3), contentBaseInfo3);

            // 人话解释
            String contentTransferChange = CardDataHelper.getStringFromCursor(cursor, "transfer_change");
            if (!contentTransferChange.equals("无")) {
                getContent(requireContext(), root.findViewById(R.id.transfer_change), contentTransferChange);
            } else {
                root.findViewById(R.id.title_card_data_transfer_change).setVisibility(View.GONE);
                root.findViewById(R.id.Card_TransferChange).setVisibility(View.GONE);
            }

            // 相关卡片
            if (tableId == 2) {
                // 融合卡专属
                ImageView ImageViewCard = root.findViewById(R.id.Image_View_Card_1_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_1"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_1_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_Result_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_2_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_2_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_Result_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_3_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_3_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_3_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                ImageViewCard = root.findViewById(R.id.Image_View_Card_Result_3);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);

                // 给相关卡片设置跳转查询的点击事件
                // 缓存点击事件需要用到的字段值
                String name1_1 = CardDataHelper.getStringFromCursor(cursor, "name_1_1");
                String name1_2 = CardDataHelper.getStringFromCursor(cursor, "name_1_2");
                String name2_2 = CardDataHelper.getStringFromCursor(cursor, "name_2_2");
                String name3_2 = CardDataHelper.getStringFromCursor(cursor, "name_3_2");
                root.findViewById(R.id.Image_View_Card_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name1_1));
                root.findViewById(R.id.Image_View_Card_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name1_2));
                root.findViewById(R.id.Image_View_Card_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name2_2));
                root.findViewById(R.id.Image_View_Card_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name3_2));

                // 部分Android12系统不支持显示🟰符号，需要进行特别处理
                TextView equal1 = root.findViewById(R.id.equal_1);
                TextView equal2 = root.findViewById(R.id.equal_2);
                TextView equal3 = root.findViewById(R.id.equal_3);
                TextView plus1 = root.findViewById(R.id.plus_1);
                TextView plus2 = root.findViewById(R.id.plus_2);
                TextView plus3 = root.findViewById(R.id.plus_3);
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
                    equal1.setText("=");
                    equal2.setText("=");
                    equal3.setText("=");
                    plus1.setText("+");
                    plus2.setText("+");
                    plus3.setText("+");
                } else {
                    equal1.setText("🟰");
                    equal2.setText("🟰");
                    equal3.setText("🟰");
                    plus1.setText("➕");
                    plus2.setText("➕");
                    plus3.setText("➕");
                }

                // 显示对应的卡片
                root.findViewById(R.id.Card_Corresponding_SubCard_Fusion).setVisibility(View.VISIBLE);
            } else if (tableId == 3) {
                // 金卡专属
                boolean hasSubCard;// 这张金卡是否是合成出来的
                hasSubCard = !CardDataHelper.getStringFromCursor(cursor, "name_1_1").equals("无");

                if (hasSubCard) {
                    String name1_1 = CardDataHelper.getStringFromCursor(cursor, "name_1_1");
                    String name1_2 = CardDataHelper.getStringFromCursor(cursor, "name_1_2");

                    TextView correspondingSubCardName = root.findViewById(R.id.card_data_index_corresponding_sub_card_name_1);
                    TextView correspondingSubCardContent = root.findViewById(R.id.card_data_index_corresponding_sub_card_content_1);
                    correspondingSubCardName.setText(name1_1);
                    correspondingSubCardContent.setText("此卡片是合成本金卡的必要素材");

                    ImageView ImageViewCard = root.findViewById(R.id.card_data_index_corresponding_sub_card_image_1);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_1"));
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCard.setImageResource(imageResId);

                    correspondingSubCardName = root.findViewById(R.id.card_data_index_corresponding_sub_card_name_2);
                    correspondingSubCardContent = root.findViewById(R.id.card_data_index_corresponding_sub_card_content_2);
                    correspondingSubCardName.setText(name1_2);
                    correspondingSubCardContent.setText("此卡片是合成本金卡的必要素材");

                    ImageViewCard = root.findViewById(R.id.card_data_index_corresponding_sub_card_image_2);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_2"));
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    ImageViewCard.setImageResource(imageResId);

                    // 给相关卡片设置跳转查询的点击事件
                    root.findViewById(R.id.card_data_index_corresponding_sub_card_1_container).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name1_1));
                    root.findViewById(R.id.card_data_index_corresponding_sub_card_2_container).setOnClickListener(v -> CardDataHelper.selectCardDataByName(requireContext(), name1_2));

                    // 显示对应的卡片
                    root.findViewById(R.id.Card_Corresponding_SubCard_Golden).setVisibility(View.VISIBLE);
                }
            }

            LinearLayout container = root.findViewById(R.id.Card_Corresponding_Container);
            CardView CardCorresponding = root.findViewById(R.id.Card_Corresponding);
            switch (tableId) {
                case 1, 4:
                    CardDataHelper.addCorrespondingCardForGeneralAndAnimalCard(requireContext(), container, cursor, cardName, CardCorresponding);
                    break;
                case 2:
                    CardDataHelper.addCorrespondingCardForFusionCard(requireContext(), container, cursor, cardName, CardCorresponding);
                    break;
                case 3:
                    CardDataHelper.addCorrespondingCardForGoldenCard(requireContext(), container, cursor, cardName, CardCorresponding);
                    break;
            }

            // 有相关卡片的话，要显示标题
            if (root.findViewById(R.id.Card_Corresponding_SubCard_Fusion).getVisibility() == View.VISIBLE || root.findViewById(R.id.Card_Corresponding_SubCard_Golden).getVisibility() == View.VISIBLE || root.findViewById(R.id.Card_Corresponding).getVisibility() == View.VISIBLE) {
                root.findViewById(R.id.title_card_data_corresponding_info).setVisibility(View.VISIBLE);
            }

            // 大图片显示动画，在所有任务完成以后进行
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                TransitionManager.beginDelayedTransition(bigImageContainer, transition);
                root.findViewById(R.id.Image_View_Card_Big_1_1).setVisibility(View.VISIBLE);
                root.findViewById(R.id.Image_View_Card_Big_1_2).setVisibility(View.VISIBLE);
                root.findViewById(R.id.Image_View_Card_Big_2_1).setVisibility(View.VISIBLE);
                root.findViewById(R.id.Image_View_Card_Big_2_2).setVisibility(View.VISIBLE);
            }, 500);
        } catch (Exception e) {
            Log.e("Data", "捕捉到异常：" + e.getMessage());
            Toast.makeText(requireContext(), "数据加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 辅助方法：设置文本到控件，避免重复代码
     * @param viewId 在哪个TextView上展示内容
     * @param text 要展示什么内容
     */
    private void setTextToView(int viewId, String text) {
        TextView textView = root.findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 获取滚动视图ScrollView
        ScrollView scrollView = root.findViewById(R.id.ScrollView);

        // 初始化大图片的淡入动画
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(300); // 动画时长300ms

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            // 获取需要渐隐的元素
            Image_View_Card_Big_1_Container = root.findViewById(R.id.Image_View_Card_Big_1_Container);
            Image_View_Card_Big_2_Container = root.findViewById(R.id.Image_View_Card_Big_2_Container);

            // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
            imageViewCardBig1ContainerMaxScroll = DensityUtil.dpToPx(requireContext(), Image_View_Card_Big_2_Container.getVisibility() == View.VISIBLE ? 200 : 50);
            imageViewCardBig2ContainerMaxScroll = DensityUtil.dpToPx(requireContext(), 50);
        }

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
                if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
                    // 手动触发一次效果更新，让透明度与恢复的滚动位置同步
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, savedScrollY, imageViewCardBig1ContainerMaxScroll);
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, savedScrollY, imageViewCardBig2ContainerMaxScroll);
                }
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
                if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, scrollY, imageViewCardBig1ContainerMaxScroll);
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, scrollY, imageViewCardBig2ContainerMaxScroll);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

}