package com.careful.HyperFVM.utils.ForCardData;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

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
}
