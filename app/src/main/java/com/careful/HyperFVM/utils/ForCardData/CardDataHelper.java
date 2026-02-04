package com.careful.HyperFVM.utils.ForCardData;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
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
        Intent intent = switch (tableName) {
            case "card_data_1" ->
                    new Intent(context, CardData_1_Activity.class);
            case "card_data_2" ->
                    new Intent(context, CardData_2_Activity.class);
            case "card_data_3" ->
                    new Intent(context, CardData_3_Activity.class);
            case "card_data_4" ->
                    new Intent(context, CardData_4_Activity.class);
            default -> null;
        };

        if (intent != null) {
            intent.putExtra("name", baseName);
            intent.putExtra("table", tableName);
            context.startActivity(intent);
        }
    }
}
