package com.careful.HyperFVM.utils.ForCardData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList1Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList1EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList2Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList2EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList3Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList3EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList4Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList4EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList5Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList5EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList6Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList6EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList7Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList7EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList8Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList8EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList10Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList10EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList9Activity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList.AuxiliaryList9EffectActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.CardDataActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_1;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_2;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_3;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_4;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_5;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_6;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_1_7;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_1;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_2;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_3;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_4;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_5;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_6;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_7;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_2_8;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_1;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_10;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_11;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_12;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_13;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_14;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_15;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_16;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_17;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_18;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_19;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_2;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_20;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_21;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_22;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_23;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_24;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_25;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_26;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_27;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_28;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_29;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_3;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_30;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_31;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_32;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_33;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_34;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_35;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_36;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_37;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_38;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_39;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_4;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_40;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_41;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_42;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_43;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_5;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_6;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_7;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_8;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery.CookeryBottomSheetFragment_3_9;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.DecomposeAndGetCalculator.DecomposeAndGetCalculatorForAnimalCardActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.DecomposeAndGetCalculator.DecomposeAndGetCalculatorForGoldenCardActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import java.util.Objects;

public class CardDataHelper {
    @SuppressLint("StaticFieldLeak")
    private static final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    /**
     * 给定卡片名称，查询对应的卡片
     * @param context 上下文
     * @param cardName 卡片名称
     */
    public static void selectCardDataByName(Context context, String cardName) {
        String tableName;
        String baseName;

        if (cardName.isEmpty()) {
            Toast.makeText(context, "请输入卡片名称", Toast.LENGTH_SHORT).show();
            return;
        }
        tableName = dbHelper.getCardTableName(cardName);
        baseName = dbHelper.getCardBaseName(cardName);

        if (tableName == null) {
            Toast.makeText(context, "未找到该卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转详情页
        Intent intent = new Intent(context, CardDataActivity.class);
        intent.putExtra("cardName", baseName);
        intent.putExtra("tableName", tableName);
        context.startActivity(intent);
    }

    public static void selectAuxiliaryCardByName(Context context, String cardName) {
        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();

        Intent intent;
        switch (cardName) {
            case "平射增幅卡":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList1EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList1Activity.class);
                }
                context.startActivity(intent);
                break;
            case "投手增幅卡":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList2EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList2Activity.class);
                }
                context.startActivity(intent);
                break;
            case "莓果点心":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList3EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList3Activity.class);
                }
                context.startActivity(intent);
                break;
            case "香料虎":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList4EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList4Activity.class);
                }
                context.startActivity(intent);
                break;
            case "塔利亚神使", "宴飨女神·塔利亚":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList5EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList5Activity.class);
                }
                context.startActivity(intent);
                break;
            case "精灵龙":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList6EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList6Activity.class);
                }
                context.startActivity(intent);
                break;
            case "五向增幅卡":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList7EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList7Activity.class);
                }
                context.startActivity(intent);
                break;
            case "喷壶增幅卡":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList8EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList8Activity.class);
                }
                context.startActivity(intent);
                break;
            case "固定轨迹增幅卡":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList9EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList9Activity.class);
                }
                context.startActivity(intent);
                break;
            case "炎焱兔":
                if (isDynamicBackground) {
                    intent = new Intent(context, AuxiliaryList10EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList10Activity.class);
                }
                context.startActivity(intent);
                break;
        }
    }

    public static void selectAuxiliaryCardBySelfName(Context context, String cardName) {
        switch (cardName) {
            case "火盆", "金牛座精灵", "洛基神使", "暖炉汪", "能量喵平射", "坩埚蛇", "刺梨烧烤盘":
                selectAuxiliaryCardByName(context, "平射增幅卡");
                break;
            case "能量喵投手", "猪猪加强器", "蓝莓信号塔塔", "美味水果塔", "欧若拉神使":
                selectAuxiliaryCardByName(context, "投手增幅卡");
                break;
            case "龙须面", "五谷丰登", "五行蛇", "弗雷神使":
                selectAuxiliaryCardByName(context, "五向增幅卡");
                break;
            case "加速榨汁机", "魔杖蛇", "塔拉萨神使":
                selectAuxiliaryCardByName(context, "喷壶增幅卡");
                break;
            case "音盒马", "浮生茶":
                selectAuxiliaryCardByName(context, "固定轨迹增幅卡");
                break;
            default:
                selectAuxiliaryCardByName(context, cardName);
                break;
        }
    }

    /**
     * 解析并添加相关卡片信息，包括：金卡、融合卡、增幅卡、自己就是增幅卡
     * 注意：此方法仅用于普通卡、星座卡和生肖卡使用
     * @param context 上下文
     * @param container 相关卡片CardView内部的组件，每个组件装一个卡片信息
     * @param cursor 从数据库取出的当前卡片的信息，需要用这个来读取相关卡片信息
     * @param cardName 当前卡片的名字，主要是【自己就是增幅卡】这里需要用到
     * @param CardCorresponding 如果没有相关卡片内容时，需要对内容CardView进行隐藏
     */
    @SuppressLint({"Range", "DiscouragedApi", "CutPasteId"})
    public static void addCorrespondingCardForGeneralAndAnimalCard(Context context, LinearLayout container, Cursor cursor, String cardName, CardView CardCorresponding) {
        String imageIdStr = "";
        int imageResId;

        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();

        // 相关卡片 - 金卡
        String correspondingGoldenCardName = getStringFromCursor(cursor, "corresponding_golden_card_name");
        Log.d("correspondingCard", "cardName: " + cardName + ", correspondingGoldenCardName: " + correspondingGoldenCardName);
        if (!correspondingGoldenCardName.equals("无")) {
            LinearLayout correspondingCardContainer;
            if (isDynamicBackground) {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
            } else {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card, container, false);
            }

            // 绑定控件并设置内容
            TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
            TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
            ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);
            correspondingCardName.setText(correspondingGoldenCardName);
            correspondingCardContent.setText("本卡片是合成此金卡的必要素材");
            imageIdStr = cursor.getString(cursor.getColumnIndex("corresponding_golden_card_image_id"));
            if (!imageIdStr.equals("无")) {
                // 根据image_id获取资源ID（如"card_splash_logo" -> R.drawable.card_splash_logo）
                imageResId = context.getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        context.getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);
            }

            correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, correspondingGoldenCardName));

            container.addView(correspondingCardContainer);
        }

        // 相关卡片 - 融合卡
        String correspondingFusionCardName = getStringFromCursor(cursor, "corresponding_fusion_card_name");
        Log.d("correspondingCard", "cardName: " + cardName + ", correspondingFusionCardName: " + correspondingFusionCardName);
        if (!correspondingFusionCardName.equals("无")) {
            // 1. 读取融合卡片图片ID列（同样增加null校验）
            String correspondingFusionCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_fusion_card_image_id"));

            // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
            String[] nameArray = correspondingFusionCardName.split("\\r?\\n");
            String[] imageIdArray = correspondingFusionCardImageId.split("\\r?\\n");

            // 3. 遍历拆分后的名称数组，为每条数据生成布局
            for (int i = 0; i < nameArray.length; i++) {
                String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                // 跳过空名称（比如拆分后出现空字符串）
                if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                    continue;
                }

                // 4. Inflate单个融合卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                LinearLayout correspondingCardContainer;
                if (isDynamicBackground) {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
                } else {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card, container, false);
                }

                // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                int lastNum = 1;
                if (i < imageIdArray.length) {
                    imageIdStr = imageIdArray[i];
                    lastNum = Character.getNumericValue(imageIdStr.charAt(imageIdStr.length() - 1));
                }

                // 根据image_id获取资源ID
                imageResId = context.getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        context.getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);

                // 7. 设置卡片名称和描述
                correspondingCardName.setText(singleCardName);
                switch (lastNum) {
                    case 1:
                        correspondingCardContent.setText("本卡片是初级融合此卡片的必要素材");
                        break;
                    case 2:
                        correspondingCardContent.setText("本卡片是深度融合此卡片的必要素材");
                        break;
                    case 3:
                        correspondingCardContent.setText("本卡片是灵魂融合此卡片的必要素材");
                        break;
                }

                // 8. 设置点击事件（点击跳转到对应卡片详情）
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, singleCardName));

                // 9. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
            }
        }

        // 相关卡片 - 增幅卡
        String correspondingAuxiliaryCardName = getStringFromCursor(cursor, "corresponding_auxiliary_card_name");
        Log.d("correspondingCard", "cardName: " + cardName + ", correspondingAuxiliaryCardName: " + correspondingAuxiliaryCardName);
        if (!correspondingAuxiliaryCardName.equals("无")) {
            // 1. 读取增幅卡片图片ID列（同样增加null校验）
            String correspondingAuxiliaryCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_auxiliary_card_image_id"));

            // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
            String[] nameArray = correspondingAuxiliaryCardName.split("\\r?\\n");
            String[] imageIdArray = correspondingAuxiliaryCardImageId.split("\\r?\\n");

            // 3. 遍历拆分后的名称数组，为每条数据生成布局
            for (int i = 0; i < nameArray.length; i++) {
                String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                // 跳过空名称（比如拆分后出现空字符串）
                if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                    continue;
                }

                // 4. Inflate单个增幅卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                LinearLayout correspondingCardContainer;
                if (isDynamicBackground) {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
                } else {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card, container, false);
                }

                // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                if (i < imageIdArray.length) {
                    imageIdStr = imageIdArray[i];
                }

                // 根据image_id获取资源ID
                imageResId = context.getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        context.getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);

                // 7. 设置卡片名称和描述
                correspondingCardName.setText(singleCardName);
                correspondingCardContent.setText("此类卡片增幅本卡片");

                // 8. 设置点击事件（点击跳转到对应卡片详情）
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardByName(context, singleCardName));

                // 9. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
            }
        }

        // 相关卡片 - 自己就是增幅卡
        if (
                Objects.equals(cardName, "火盆") || Objects.equals(cardName, "蓝莓信号塔塔") || Objects.equals(cardName, "美味水果塔") ||
                        Objects.equals(cardName, "莓果点心") || Objects.equals(cardName, "龙须面") || Objects.equals(cardName, "五谷丰登") ||
                        Objects.equals(cardName, "加速榨汁机") ||
                Objects.equals(cardName, "金牛座精灵") || Objects.equals(cardName, "暖炉汪") || Objects.equals(cardName, "能量喵") ||
                        Objects.equals(cardName, "坩埚蛇") || Objects.equals(cardName, "猪猪加强器") || Objects.equals(cardName, "香料虎") ||
                        Objects.equals(cardName, "精灵龙") || Objects.equals(cardName, "五行蛇") || Objects.equals(cardName, "魔杖蛇") ||
                        Objects.equals(cardName, "音盒马") || Objects.equals(cardName, "浮生茶") || Objects.equals(cardName, "炎焱兔")
        ) {
            // 1. Inflate单个增幅卡片的布局
            LinearLayout correspondingCardContainer;
            if (isDynamicBackground) {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
            } else {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card, container, false);
            }

            // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
            TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
            TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
            ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

            // 3. 设置标题，并隐藏描述和图片
            if (Objects.equals(cardName, "能量喵")) {
                correspondingCardName.setText("查看此卡片的平射增幅名单");
            } else {
                correspondingCardName.setText("查看此卡片的增幅名单");
            }
            correspondingCardContent.setText("点击跳转");
            imageResId = context.getResources().getIdentifier(
                    "ic_chevron_right",
                    "drawable",
                    context.getPackageName()
            );
            correspondingCardImage.setImageResource(imageResId);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            int tintColor = typedValue.data;
            correspondingCardImage.setColorFilter(tintColor);

            // 4. 设置点击事件（点击跳转到对应卡片详情）
            if (Objects.equals(cardName, "能量喵")) {
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(context, cardName + "平射"));
            } else {
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(context, cardName));
            }

            // 5. 将当前卡片布局添加到父容器
            container.addView(correspondingCardContainer);
        } else {
            // 没有任何相关卡片的话，隐藏标题和CardView
            if (correspondingGoldenCardName.equals("无") && correspondingFusionCardName.equals("无") && correspondingAuxiliaryCardName.equals("无")) {
                CardCorresponding.setVisibility(View.GONE);
            }
        }

        // 能量喵有两个增幅名单
        if (Objects.equals(cardName, "能量喵")) {
            // 1. Inflate单个增幅卡片的布局
            LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);

            // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
            TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
            TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
            @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

            // 3. 设置标题，并隐藏描述和图片
            correspondingCardName.setText("查看此卡片的投手增幅名单");
            correspondingCardContent.setText("点击跳转");
            imageResId = context.getResources().getIdentifier(
                    "ic_chevron_right",
                    "drawable",
                    context.getPackageName()
            );
            correspondingCardImage.setImageResource(imageResId);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            int tintColor = typedValue.data;
            correspondingCardImage.setColorFilter(tintColor);

            // 4. 设置点击事件（点击跳转到对应卡片详情）
            correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(context, cardName + "投手"));

            // 5. 将当前卡片布局添加到父容器
            container.addView(correspondingCardContainer);
        }
    }

    /**
     * 解析并添加相关卡片信息，包括：金卡、融合卡、增幅卡、自己就是增幅卡
     * 注意：此方法仅用于金卡使用
     * @param context 上下文
     * @param container 相关卡片CardView内部的组件，每个组件装一个卡片信息
     * @param cursor 从数据库取出的当前卡片的信息，需要用这个来读取相关卡片信息
     * @param cardName 当前卡片的名字，主要是【自己就是增幅卡】这里需要用到
     * @param CardCorresponding 如果没有相关卡片内容时，需要对内容CardView进行隐藏
     */
    @SuppressLint({"Range", "DiscouragedApi", "CutPasteId"})
    public static void addCorrespondingCardForGoldenCard(Context context, LinearLayout container, Cursor cursor, String cardName, CardView CardCorresponding) {
        String imageIdStr = "";
        int imageResId;

        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();

        // 相关卡片 - 增幅卡
        String correspondingAuxiliaryCardName = getStringFromCursor(cursor, "corresponding_auxiliary_card_name");
        if (!correspondingAuxiliaryCardName.equals("无")) {
            // 1. 读取增幅卡片图片ID列（同样增加null校验）
            String correspondingAuxiliaryCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_auxiliary_card_image_id"));

            // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
            String[] nameArray = correspondingAuxiliaryCardName.split("\\r?\\n");
            String[] imageIdArray = correspondingAuxiliaryCardImageId.split("\\r?\\n");

            // 3. 遍历拆分后的名称数组，为每条数据生成布局
            for (int i = 0; i < nameArray.length; i++) {
                String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                // 跳过空名称（比如拆分后出现空字符串）
                if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                    continue;
                }

                // 4. Inflate单个增幅卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                LinearLayout correspondingCardContainer;
                if (isDynamicBackground) {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
                } else {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card, container, false);
                }

                // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                if (i < imageIdArray.length) {
                    imageIdStr = imageIdArray[i];
                }

                // 根据image_id获取资源ID
                imageResId = context.getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        context.getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);

                // 7. 设置卡片名称和描述
                correspondingCardName.setText(singleCardName);
                correspondingCardContent.setText("此类卡片增幅本卡片");

                // 8. 设置点击事件（点击跳转到对应卡片详情）
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardByName(context, singleCardName));

                // 9. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
            }
        }

        // 相关卡片 - 自己就是增幅卡
        if (
                Objects.equals(cardName, "洛基神使") || Objects.equals(cardName, "欧若拉神使") || Objects.equals(cardName, "塔利亚神使") ||
                        Objects.equals(cardName, "弗雷神使") || Objects.equals(cardName, "塔拉萨神使")
        ) {
            // 1. Inflate单个增幅卡片的布局
            LinearLayout correspondingCardContainer;
            if (isDynamicBackground) {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
            } else {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card, container, false);
            }

            // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
            TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
            TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
            ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

            // 3. 设置标题，并隐藏描述和图片
            correspondingCardName.setText("查看此卡片的增幅名单");
            correspondingCardContent.setText("点击跳转");
            imageResId = context.getResources().getIdentifier(
                    "ic_chevron_right",
                    "drawable",
                    context.getPackageName()
            );
            correspondingCardImage.setImageResource(imageResId);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            int tintColor = typedValue.data;
            correspondingCardImage.setColorFilter(tintColor);

            // 4. 设置点击事件（点击跳转到对应卡片详情）
            correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(context, cardName));

            // 5. 将当前卡片布局添加到父容器
            container.addView(correspondingCardContainer);
        } else {
            // 没有任何相关卡片的话，隐藏标题和CardView
            if (correspondingAuxiliaryCardName.equals("无")) {
                CardCorresponding.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 解析并添加相关卡片信息，包括：金卡、融合卡、增幅卡、自己就是增幅卡
     * 注意：此方法仅用于融合卡使用
     * @param context 上下文
     * @param container 相关卡片CardView内部的组件，每个组件装一个卡片信息
     * @param cursor 从数据库取出的当前卡片的信息，需要用这个来读取相关卡片信息
     * @param cardName 当前卡片的名字，主要是【自己就是增幅卡】这里需要用到
     * @param CardCorresponding 如果没有相关卡片内容时，需要对内容CardView进行隐藏
     */
    @SuppressLint({"Range", "DiscouragedApi", "CutPasteId"})
    public static void addCorrespondingCardForFusionCard(Context context, LinearLayout container, Cursor cursor, String cardName, CardView CardCorresponding) {
        String imageIdStr = "";
        int imageResId;

        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();

        // 相关卡片 - 增幅卡
        String correspondingAuxiliaryCardName = getStringFromCursor(cursor, "corresponding_auxiliary_card_name");
        if (!correspondingAuxiliaryCardName.equals("无")) {
            // 1. 读取增幅卡片图片ID列（同样增加null校验）
            String correspondingAuxiliaryCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_auxiliary_card_image_id"));

            // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
            String[] nameArray = correspondingAuxiliaryCardName.split("\\r?\\n");
            String[] imageIdArray = correspondingAuxiliaryCardImageId.split("\\r?\\n");

            // 3. 遍历拆分后的名称数组，为每条数据生成布局
            for (int i = 0; i < nameArray.length; i++) {
                String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                // 跳过空名称（比如拆分后出现空字符串）
                if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                    continue;
                }

                // 4. Inflate单个增幅卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                LinearLayout correspondingCardContainer;
                if (isDynamicBackground) {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
                } else {
                    correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                            .inflate(R.layout.card_card_data_corresponding_card, container, false);
                }

                // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                if (i < imageIdArray.length) {
                    imageIdStr = imageIdArray[i];
                }

                // 根据image_id获取资源ID
                imageResId = context.getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        context.getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);

                // 7. 设置卡片名称和描述
                correspondingCardName.setText(singleCardName);
                correspondingCardContent.setText("此类卡片增幅本卡片");

                // 8. 设置点击事件（点击跳转到对应卡片详情）
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardByName(context, singleCardName));

                // 9. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
            }
        }

        // 相关卡片 - 自己就是增幅卡
        if (
                Objects.equals(cardName, "刺梨烧烤盘")
        ) {
            // 1. Inflate单个增幅卡片的布局
            LinearLayout correspondingCardContainer;
            if (isDynamicBackground) {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
            } else {
                correspondingCardContainer = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.card_card_data_corresponding_card, container, false);
            }

            // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
            TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
            TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
            ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

            // 3. 设置标题，并隐藏描述和图片
            correspondingCardName.setText("查看此卡片的增幅名单");
            correspondingCardContent.setText("点击跳转");
            imageResId = context.getResources().getIdentifier(
                    "ic_chevron_right",
                    "drawable",
                    context.getPackageName()
            );
            correspondingCardImage.setImageResource(imageResId);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            int tintColor = typedValue.data;
            correspondingCardImage.setColorFilter(tintColor);

            // 4. 设置点击事件（点击跳转到对应卡片详情）
            correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(context, cardName));

            // 5. 将当前卡片布局添加到父容器
            container.addView(correspondingCardContainer);
        } else {
            // 没有任何相关卡片的话，隐藏CardView
            if (correspondingAuxiliaryCardName.equals("无")) {
                CardCorresponding.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("Range")
    public static void selectDecomposeAndGetData(Context context, String cardName, String decomposeItemName, String[] imageIdsArray, int[] decomposeDataArray, int[] getDataArray) {
        Intent intent;
        if (decomposeItemName.equals("神谕之石")) {
            intent = new Intent(context, DecomposeAndGetCalculatorForGoldenCardActivity.class);
        } else {
            intent = new Intent(context, DecomposeAndGetCalculatorForAnimalCardActivity.class);
        }
        intent.putExtra("card_name", cardName);
        intent.putExtra("decompose_item_name", decomposeItemName);
        intent.putExtra("image_ids", imageIdsArray);
        intent.putExtra("decompose_data", decomposeDataArray);
        intent.putExtra("get_data", getDataArray);
        context.startActivity(intent);
    }

    public static void selectCookeryByName(String cookeryName, LinearLayout container, FragmentManager fragmentManager) {
        switch (cookeryName) {
            case "南翔小笼包":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_1 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_1();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "咖啡奶茶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_2 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_2();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "美食豪华套餐":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_3 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_3();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "冰镇西瓜":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_4 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_4();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "麻辣烫":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_5 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_5();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "葡式蛋挞":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_6 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_6();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "菠萝布丁":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_1_7 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_1_7();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "王府井包子":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_1 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_1();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "猫屎咖啡":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_2 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_2();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "酒蒸鸡":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_3 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_3();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "劲爆鸡米花":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_4 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_4();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "葡萄美酒":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_5 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_5();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "巧克力雪球":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_6 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_6();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "冷烤火鸡":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_7 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_7();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "麻婆豆腐":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_2_8 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_2_8();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "狗不理包子":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_1 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_1();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "鸡丝银鱼汤":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_2 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_2();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "毛血旺":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_3 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_3();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "番茄炒蛋":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_4 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_4();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "面包家族":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_5 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_5();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "关东煮":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_6 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_6();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "茶叶蛋":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_7 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_7();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "拉菲限量版":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_8 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_8();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "圣诞姜饼":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_9 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_9();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "水煮鱼":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_10 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_10();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "钻石风味香槟":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_11 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_11();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "川香冒菜":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_12 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_12();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "咖喱鱼蛋":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_13 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_13();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "海鲜烧烤":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_14 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_14();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "海星拉面":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_15 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_15();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "老北京涮锅":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_16 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_16();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "菲丽水":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_17 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_17();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "汉帝茅台":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_18 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_18();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "怀石料理":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_19 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_19();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "复活节彩蛋":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_20 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_20();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "铁板鱿鱼":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_21 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_21();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "法式焗海螺":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_22 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_22();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "普罗旺斯鱼汤":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_23 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_23();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "锤子肉片":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_24 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_24();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "幻露流光":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_25 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_25();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "椰子西米露":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_26 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_26();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "明炉炭烧":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_27 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_27();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "冰糖葫芦":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_28 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_28();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "海鲜大咖":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_29 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_29();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "沙坑羊排":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_30 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_30();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "雷城全家桶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_31 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_31();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "奇境下午茶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_32 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_32();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "M双层堡":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_33 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_33();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "太阳饼":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_34 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_34();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "杏仁芝麻茶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_35 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_35();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "冰摇桃桃":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_36 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_36();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "烈焰熔岩蛋糕":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_37 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_37();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "火焰冰淇淋":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_38 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_38();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "营地炖肉":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_39 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_39();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "星云奶茶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_40 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_40();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "多元料理":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_41 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_41();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "印度飞饼":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_42 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_42();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
            case "暴打柠檬茶":
                container.setOnClickListener(v -> {
                    CookeryBottomSheetFragment_3_43 cookeryBottomSheetFragment = new CookeryBottomSheetFragment_3_43();
                    cookeryBottomSheetFragment.show(fragmentManager, "食神谱");
                });
                break;
        }
    }

    // 辅助方法：从游标获取字符串（处理空值）
    public static String getStringFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            return "未知"; // 列名不存在时提示
        }
        String value = cursor.getString(columnIndex);
        return (value == null || value.isEmpty()) ? "无" : value;
    }

}
