package com.careful.HyperFVM.utils.ForCardData;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList1Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList1EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList2Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList2EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList3Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList3EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList4Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList4EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList5Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList5EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList6Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList6EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList7Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList7EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList8Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList8EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList9Activity;
import com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList.AuxiliaryList9EffectActivity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData1Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData2Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData3Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData4Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData4EffectActivity;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class CardDataHelper {
    /**
     * 给定卡片名称，查询对应的卡片
     * @param context 上下文
     * @param cardName 卡片名称
     */
    public static void selectCardDataByName(Context context, String cardName) {
        String tableName;
        String baseName;

        try (DBHelper dbHelper = new DBHelper(context)) {
            if (cardName.isEmpty()) {
                Toast.makeText(context, "请输入卡片名称", Toast.LENGTH_SHORT).show();
                return;
            }
            tableName = dbHelper.getCardTable(cardName);
            baseName = dbHelper.getCardBaseName(cardName);
        }

        if (tableName == null) {
            Toast.makeText(context, "未找到该卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转详情页
        Intent intent = null;
        switch (tableName) {
            case "card_data_1":
                intent = new Intent(context, CardData1Activity.class);
                break;
            case "card_data_2":
                intent = new Intent(context, CardData2Activity.class);
                break;
            case "card_data_3":
                intent = new Intent(context, CardData3Activity.class);
                break;
            case "card_data_4":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, CardData4EffectActivity.class);
                } else {
                    intent = new Intent(context, CardData4Activity.class);
                }
                break;
        }

        if (intent != null) {
            intent.putExtra("name", baseName);
            intent.putExtra("table", tableName);
            context.startActivity(intent);
        }
    }

    public static void selectAuxiliaryCardByName(Context context, String cardName) {
        Intent intent;
        switch (cardName) {
            case "平射增幅卡":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList1EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList1Activity.class);
                }
                context.startActivity(intent);
                break;
            case "投手增幅卡":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList2EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList2Activity.class);
                }
                context.startActivity(intent);
                break;
            case "莓果点心":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList3EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList3Activity.class);
                }
                context.startActivity(intent);
                break;
            case "香料虎":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList4EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList4Activity.class);
                }
                context.startActivity(intent);
                break;
            case "塔利亚神使", "宴飨女神·塔利亚":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList5EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList5Activity.class);
                }
                context.startActivity(intent);
                break;
            case "精灵龙":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList6EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList6Activity.class);
                }
                context.startActivity(intent);
                break;
            case "五向增幅卡":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList7EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList7Activity.class);
                }
                context.startActivity(intent);
                break;
            case "喷壶增幅卡":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList8EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList8Activity.class);
                }
                context.startActivity(intent);
                break;
            case "炎焱兔":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent = new Intent(context, AuxiliaryList9EffectActivity.class);
                } else {
                    intent = new Intent(context, AuxiliaryList9Activity.class);
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
            default:
                selectAuxiliaryCardByName(context, cardName);
                break;
        }
    }

}
