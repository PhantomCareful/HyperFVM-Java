package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

public class CardDataDataFragment extends Fragment {
    private final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    private View root;

    private String cardName;
    private String tableName;
    private int tableId;

    private int savedScrollY = 0;// 用于保存/恢复的滚动位置

    public CardDataDataFragment newInstance(String cardName, String tableName) {
        CardDataDataFragment fragment = new CardDataDataFragment();
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
            root = inflater.inflate(R.layout.fragment_card_data_data_effect, container, false);
        } else {
            root = inflater.inflate(R.layout.fragment_card_data_data, container, false);
        }

        if (getArguments() != null) {
            cardName = getArguments().getString("cardName");
            tableName = getArguments().getString("tableName");
        }

        // 来自第几张数据表
        if (tableName != null) {
            tableId = Integer.parseInt(tableName.split("card_data_")[1]);
        }

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

            // 星级信息
            setTextToView(R.id.star, "\uD83C\uDF1F强化提升：" + CardDataHelper.getStringFromCursor(cursor, "star"));
            setTextToView(R.id.star_detail, CardDataHelper.getStringFromCursor(cursor, "star_detail"));
            setTextToView(R.id.star_0, CardDataHelper.getStringFromCursor(cursor, "star_0"));
            setTextToView(R.id.star_1, CardDataHelper.getStringFromCursor(cursor, "star_1"));
            setTextToView(R.id.star_2, CardDataHelper.getStringFromCursor(cursor, "star_2"));
            setTextToView(R.id.star_3, CardDataHelper.getStringFromCursor(cursor, "star_3"));
            setTextToView(R.id.star_4, CardDataHelper.getStringFromCursor(cursor, "star_4"));
            setTextToView(R.id.star_5, CardDataHelper.getStringFromCursor(cursor, "star_5"));
            setTextToView(R.id.star_6, CardDataHelper.getStringFromCursor(cursor, "star_6"));
            setTextToView(R.id.star_7, CardDataHelper.getStringFromCursor(cursor, "star_7"));
            setTextToView(R.id.star_8, CardDataHelper.getStringFromCursor(cursor, "star_8"));
            setTextToView(R.id.star_9, CardDataHelper.getStringFromCursor(cursor, "star_9"));
            setTextToView(R.id.star_10, CardDataHelper.getStringFromCursor(cursor, "star_10"));
            setTextToView(R.id.star_11, CardDataHelper.getStringFromCursor(cursor, "star_11"));
            setTextToView(R.id.star_12, CardDataHelper.getStringFromCursor(cursor, "star_12"));
            setTextToView(R.id.star_13, CardDataHelper.getStringFromCursor(cursor, "star_13"));
            setTextToView(R.id.star_14, CardDataHelper.getStringFromCursor(cursor, "star_14"));
            setTextToView(R.id.star_15, CardDataHelper.getStringFromCursor(cursor, "star_15"));
            setTextToView(R.id.star_16, CardDataHelper.getStringFromCursor(cursor, "star_16"));
            setTextToView(R.id.star_M, CardDataHelper.getStringFromCursor(cursor, "star_M"));
            setTextToView(R.id.star_U, CardDataHelper.getStringFromCursor(cursor, "star_U"));

            // 金卡专属
            if (tableId == 3) {
                // 金卡援护
                if (!CardDataHelper.getStringFromCursor(cursor, "support_1").equals("无")) {
                    getContent(requireContext(), root.findViewById(R.id.support_info_1), CardDataHelper.getStringFromCursor(cursor, "support_1"));
                    getContent(requireContext(), root.findViewById(R.id.support_info_2), CardDataHelper.getStringFromCursor(cursor, "support_2"));

                    root.findViewById(R.id.card_data_support_title).setVisibility(View.VISIBLE);
                    root.findViewById(R.id.Card_Support).setVisibility(View.VISIBLE);
                }
            }

            // 融合卡专属
            if (tableId == 2) {
                // 品阶信息
                setTextToView(R.id.star_fusion, "\uD83C\uDF1F品阶提升：" + "\n" + CardDataHelper.getStringFromCursor(cursor, "star_fusion"));
                setTextToView(R.id.star_fusion_detail, CardDataHelper.getStringFromCursor(cursor, "star_fusion_detail"));
                setTextToView(R.id.star_fusion_1, CardDataHelper.getStringFromCursor(cursor, "star_fusion_1"));
                setTextToView(R.id.star_fusion_2, CardDataHelper.getStringFromCursor(cursor, "star_fusion_2"));
                setTextToView(R.id.star_fusion_3, CardDataHelper.getStringFromCursor(cursor, "star_fusion_3"));
                setTextToView(R.id.star_fusion_4, CardDataHelper.getStringFromCursor(cursor, "star_fusion_4"));
                setTextToView(R.id.star_fusion_5, CardDataHelper.getStringFromCursor(cursor, "star_fusion_5"));
                setTextToView(R.id.star_fusion_6, CardDataHelper.getStringFromCursor(cursor, "star_fusion_6"));
                setTextToView(R.id.star_fusion_7, CardDataHelper.getStringFromCursor(cursor, "star_fusion_7"));
                setTextToView(R.id.star_fusion_8, CardDataHelper.getStringFromCursor(cursor, "star_fusion_8"));
                setTextToView(R.id.star_fusion_9, CardDataHelper.getStringFromCursor(cursor, "star_fusion_9"));
                setTextToView(R.id.star_fusion_10, CardDataHelper.getStringFromCursor(cursor, "star_fusion_10"));
                setTextToView(R.id.star_fusion_11, CardDataHelper.getStringFromCursor(cursor, "star_fusion_11"));
                setTextToView(R.id.star_fusion_12, CardDataHelper.getStringFromCursor(cursor, "star_fusion_12"));
                setTextToView(R.id.star_fusion_13, CardDataHelper.getStringFromCursor(cursor, "star_fusion_13"));
                setTextToView(R.id.star_fusion_14, CardDataHelper.getStringFromCursor(cursor, "star_fusion_14"));
                setTextToView(R.id.star_fusion_15, CardDataHelper.getStringFromCursor(cursor, "star_fusion_15"));
                setTextToView(R.id.star_fusion_16, CardDataHelper.getStringFromCursor(cursor, "star_fusion_16"));
                setTextToView(R.id.star_fusion_M, CardDataHelper.getStringFromCursor(cursor, "star_fusion_M"));
                setTextToView(R.id.star_fusion_U, CardDataHelper.getStringFromCursor(cursor, "star_fusion_U"));

                // 显示对应的卡片和标题
                root.findViewById(R.id.star_fusion).setVisibility(View.VISIBLE);
                root.findViewById(R.id.Card_StarFusion).setVisibility(View.VISIBLE);
            }

            // 技能信息
            setTextToView(R.id.skill, "\uD83C\uDF1F技能提升：" + CardDataHelper.getStringFromCursor(cursor, "skill"));
            if (!CardDataHelper.getStringFromCursor(cursor, "skill").equals("该防御卡不支持技能")) {
                setTextToView(R.id.skill_detail, CardDataHelper.getStringFromCursor(cursor, "skill_detail"));
                setTextToView(R.id.skill_0, CardDataHelper.getStringFromCursor(cursor, "skill_0"));
                setTextToView(R.id.skill_1, CardDataHelper.getStringFromCursor(cursor, "skill_1"));
                setTextToView(R.id.skill_2, CardDataHelper.getStringFromCursor(cursor, "skill_2"));
                setTextToView(R.id.skill_3, CardDataHelper.getStringFromCursor(cursor, "skill_3"));
                setTextToView(R.id.skill_4, CardDataHelper.getStringFromCursor(cursor, "skill_4"));
                setTextToView(R.id.skill_5, CardDataHelper.getStringFromCursor(cursor, "skill_5"));
                setTextToView(R.id.skill_6, CardDataHelper.getStringFromCursor(cursor, "skill_6"));
                setTextToView(R.id.skill_7, CardDataHelper.getStringFromCursor(cursor, "skill_7"));
                setTextToView(R.id.skill_8, CardDataHelper.getStringFromCursor(cursor, "skill_8"));

                // 显示对应的卡片
                root.findViewById(R.id.Card_Skill).setVisibility(View.VISIBLE);
            }

            // 横屏查看，仅在手机上使用
            if (SmallestWidthUtil.getSmallestWidthDp() >= 600) {
                root.findViewById(R.id.Button_SeeCardDataDetail).setVisibility(View.GONE);
            } else {
                // 把数据传递给横着看活动
                String starDataDetail = CardDataHelper.getStringFromCursor(cursor, "star_detail");
                String[] starDataArray = {
                        CardDataHelper.getStringFromCursor(cursor, "star_0"),
                        CardDataHelper.getStringFromCursor(cursor, "star_1"),
                        CardDataHelper.getStringFromCursor(cursor, "star_2"),
                        CardDataHelper.getStringFromCursor(cursor, "star_3"),
                        CardDataHelper.getStringFromCursor(cursor, "star_4"),
                        CardDataHelper.getStringFromCursor(cursor, "star_5"),
                        CardDataHelper.getStringFromCursor(cursor, "star_6"),
                        CardDataHelper.getStringFromCursor(cursor, "star_7"),
                        CardDataHelper.getStringFromCursor(cursor, "star_8"),
                        CardDataHelper.getStringFromCursor(cursor, "star_9"),
                        CardDataHelper.getStringFromCursor(cursor, "star_10"),
                        CardDataHelper.getStringFromCursor(cursor, "star_11"),
                        CardDataHelper.getStringFromCursor(cursor, "star_12"),
                        CardDataHelper.getStringFromCursor(cursor, "star_13"),
                        CardDataHelper.getStringFromCursor(cursor, "star_14"),
                        CardDataHelper.getStringFromCursor(cursor, "star_15"),
                        CardDataHelper.getStringFromCursor(cursor, "star_16"),
                        CardDataHelper.getStringFromCursor(cursor, "star_M"),
                        CardDataHelper.getStringFromCursor(cursor, "star_U"),
                };
                String fusionData = tableId == 2 ? CardDataHelper.getStringFromCursor(cursor, "star_fusion") : "无";
                String fusionDataDetail = tableId == 2 ? CardDataHelper.getStringFromCursor(cursor, "star_fusion_detail") : "无";
                String[] fusionDataArray = tableId == 2 ? new String[] {
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_0"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_1"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_2"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_3"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_4"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_5"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_6"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_7"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_8"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_9"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_10"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_11"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_12"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_13"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_14"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_15"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_16"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_M"),
                        CardDataHelper.getStringFromCursor(cursor, "star_fusion_U"),
                } : null;
                String skillData = CardDataHelper.getStringFromCursor(cursor, "skill");
                String skillDataDetail = CardDataHelper.getStringFromCursor(cursor, "skill_detail");
                String[] skillDataArray = {
                        CardDataHelper.getStringFromCursor(cursor, "skill_0"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_1"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_2"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_3"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_4"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_5"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_6"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_7"),
                        CardDataHelper.getStringFromCursor(cursor, "skill_8"),
                };
                root.findViewById(R.id.Button_SeeCardDataDetail).setOnClickListener(v -> {
                    Intent intent = new Intent(requireActivity(), CardDaraDetailActivity.class);
                    intent.putExtra("cardName", cardName);
                    intent.putExtra("tableId", tableId);
                    intent.putExtra("starDataDetail", starDataDetail);
                    intent.putExtra("starDataArray", starDataArray);
                    intent.putExtra("fusionData", fusionData);
                    intent.putExtra("fusionDataDetail", fusionDataDetail);
                    intent.putExtra("fusionDataArray", fusionDataArray);
                    intent.putExtra("skillData", skillData);
                    intent.putExtra("skillDataDetail", skillDataDetail);
                    intent.putExtra("skillDataArray", skillDataArray);
                    startActivity(intent);
                });
            }

            // 分解兑换信息
            if (tableId == 3 || tableId == 4) {
                setTextToView(R.id.decompose_and_get, "\uD83C\uDF1F分解&兑换：" + CardDataHelper.getStringFromCursor(cursor, "decompose_item"));

                ImageView imageView = root.findViewById(R.id.decompose_image_id_card_1);
                String imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1"));
                int imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_card_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_card_3);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                if (tableId == 3) {
                    imageView = root.findViewById(R.id.decompose_image_id_card_4);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_4"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);
                } else {
                    root.findViewById(R.id.decompose_image_id_card_4).setVisibility(View.GONE);
                }

                imageView = root.findViewById(R.id.decompose_image_id_skill_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_skill_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_skill_3);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_skill_4);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_transfer_1_a);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_transfer_1_b);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                if (tableId == 3) {
                    imageView = root.findViewById(R.id.decompose_image_id_transfer_1_c);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);
                } else {
                    root.findViewById(R.id.decompose_image_id_transfer_1_c).setVisibility(View.GONE);
                }

                imageView = root.findViewById(R.id.decompose_image_id_transfer_2_a);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_transfer_2_b);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                imageView = root.findViewById(R.id.decompose_image_id_transfer_2_c);
                imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c"));
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        requireContext().getPackageName()
                );
                imageView.setImageResource(imageResId);

                if (tableId == 3) {
                    imageView = root.findViewById(R.id.decompose_image_id_transfer_3_a);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);

                    imageView = root.findViewById(R.id.decompose_image_id_transfer_3_b);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);

                    imageView = root.findViewById(R.id.decompose_image_id_transfer_3_c);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);

                    imageView = root.findViewById(R.id.decompose_image_id_compose);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_compose"));
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    imageView.setImageResource(imageResId);
                } else {
                    root.findViewById(R.id.decompose_image_id_transfer_3_a).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_image_id_transfer_3_b).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_image_id_transfer_3_c).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_image_id_compose).setVisibility(View.GONE);
                }

                setTextToView(R.id.decompose_card_1, CardDataHelper.getStringFromCursor(cursor, "decompose_card_1").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_card_1"));
                setTextToView(R.id.decompose_card_2, CardDataHelper.getStringFromCursor(cursor, "decompose_card_2").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_card_2"));
                setTextToView(R.id.decompose_card_3, CardDataHelper.getStringFromCursor(cursor, "decompose_card_3").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_card_3"));
                setTextToView(R.id.decompose_skill_1, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1"));
                setTextToView(R.id.decompose_skill_2, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2"));
                setTextToView(R.id.decompose_skill_3, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3"));
                setTextToView(R.id.decompose_skill_4, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4"));
                setTextToView(R.id.decompose_transfer_1_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a"));
                setTextToView(R.id.decompose_transfer_1_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b"));
                setTextToView(R.id.decompose_transfer_2_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a"));
                setTextToView(R.id.decompose_transfer_2_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b"));
                setTextToView(R.id.decompose_transfer_2_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c"));

                setTextToView(R.id.get_card_1, CardDataHelper.getStringFromCursor(cursor, "get_card_1").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_card_1"));
                setTextToView(R.id.get_card_2, CardDataHelper.getStringFromCursor(cursor, "get_card_2").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_card_2"));
                setTextToView(R.id.get_card_3, CardDataHelper.getStringFromCursor(cursor, "get_card_3").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_card_3"));
                setTextToView(R.id.get_skill_1, CardDataHelper.getStringFromCursor(cursor, "get_skill_1").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_skill_1"));
                setTextToView(R.id.get_skill_2, CardDataHelper.getStringFromCursor(cursor, "get_skill_2").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_skill_2"));
                setTextToView(R.id.get_skill_3, CardDataHelper.getStringFromCursor(cursor, "get_skill_3").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_skill_3"));
                setTextToView(R.id.get_skill_4, CardDataHelper.getStringFromCursor(cursor, "get_skill_4").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_skill_4"));
                setTextToView(R.id.get_transfer_1_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a"));
                setTextToView(R.id.get_transfer_1_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b"));
                setTextToView(R.id.get_transfer_2_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a"));
                setTextToView(R.id.get_transfer_2_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b"));
                setTextToView(R.id.get_transfer_2_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c"));

                if (tableId == 3) {
                    setTextToView(R.id.decompose_card_4, CardDataHelper.getStringFromCursor(cursor, "decompose_card_4").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_card_4"));
                    setTextToView(R.id.decompose_transfer_1_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c"));
                    setTextToView(R.id.decompose_transfer_3_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a"));
                    setTextToView(R.id.decompose_transfer_3_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b"));
                    setTextToView(R.id.decompose_transfer_3_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c"));
                    setTextToView(R.id.decompose_compose, CardDataHelper.getStringFromCursor(cursor, "decompose_compose").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "decompose_compose"));

                    setTextToView(R.id.get_card_4, CardDataHelper.getStringFromCursor(cursor, "get_card_4").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_card_4"));
                    setTextToView(R.id.get_transfer_1_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c"));
                    setTextToView(R.id.get_transfer_3_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a"));
                    setTextToView(R.id.get_transfer_3_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b"));
                    setTextToView(R.id.get_transfer_3_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c"));
                    setTextToView(R.id.get_compose, CardDataHelper.getStringFromCursor(cursor, "get_compose").equals("0") ? "🚫" : CardDataHelper.getStringFromCursor(cursor, "get_compose"));
                } else {
                    root.findViewById(R.id.decompose_card_4).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_transfer_1_c).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_transfer_3_a).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_transfer_3_b).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_transfer_3_c).setVisibility(View.GONE);
                    root.findViewById(R.id.decompose_compose).setVisibility(View.GONE);

                    root.findViewById(R.id.get_card_4).setVisibility(View.GONE);
                    root.findViewById(R.id.get_transfer_1_c).setVisibility(View.GONE);
                    root.findViewById(R.id.get_transfer_3_a).setVisibility(View.GONE);
                    root.findViewById(R.id.get_transfer_3_b).setVisibility(View.GONE);
                    root.findViewById(R.id.get_transfer_3_c).setVisibility(View.GONE);
                    root.findViewById(R.id.get_compose).setVisibility(View.GONE);
                }

                // 分解兑换计算器
                // 先整理出三个数组：图片id、分解数据、兑换数据
                String[] imageIdsArray = tableId == 3 ? new String[] {
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_4")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_compose")),
                } : new String[] {
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")),
                        cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")),
                };

                int[] decomposeDataArray = tableId == 3 ? new int[] {
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_compose")),
                } : new int[] {
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c")),
                };

                int[] getDataArray = tableId == 3 ? new int[] {
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_compose")),
                } : new int[] {
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_1")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_2")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_3")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_4")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b")),
                        Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c")),
                };

                imageView = root.findViewById(R.id.Card_Decompose_and_Get_Calculator_Image);
                String decomposeItemName = cursor.getString(cursor.getColumnIndex("decompose_item"));
                switch (decomposeItemName) {
                    case "神谕之石" -> {
                        imageResId = getResources().getIdentifier(
                                "god_stone",
                                "drawable",
                                requireContext().getPackageName()
                        );
                        imageView.setImageResource(imageResId);
                    }
                    case "生肖宝珠" -> {
                        imageResId = getResources().getIdentifier(
                                "animal_pearl",
                                "drawable",
                                requireContext().getPackageName()
                        );
                        imageView.setImageResource(imageResId);
                    }
                    case "星座碎片" -> {
                        imageResId = getResources().getIdentifier(
                                "yellow_crystal",
                                "drawable",
                                requireContext().getPackageName()
                        );
                        imageView.setImageResource(imageResId);
                    }
                }
                root.findViewById(R.id.Card_Decompose_and_Get_Calculator).setOnClickListener(v -> CardDataHelper.selectDecomposeAndGetData(requireContext(), cardName, decomposeItemName, imageIdsArray, decomposeDataArray, getDataArray));
            } else {
                root.findViewById(R.id.decompose_and_get).setVisibility(View.GONE);
                root.findViewById(R.id.Card_Decompose_and_Get).setVisibility(View.GONE);
                root.findViewById(R.id.Card_Decompose_and_Get_Calculator_Container).setVisibility(View.GONE);
            }
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
        // 动态调整侧边距（手机/PAD）
        LinearLayout card_data_container = root.findViewById(R.id.card_data_container);
        InsetsUtil.setMarginHorizontal(requireActivity(), card_data_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card_data_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            card_data_container.setLayoutParams(params);
        });

        // 获取滚动视图ScrollView
        ScrollView scrollView = root.findViewById(R.id.ScrollView);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

}