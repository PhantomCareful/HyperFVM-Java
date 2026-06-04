package com.careful.HyperFVM.utils.ForDashboard.FromGame.MeishiWechat;

import android.content.Context;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftFetcher {
    private final DBHelper dbHelper;
    private final GiftFetchHelper giftHelper;

    public GiftFetcher(Context context) {
        this.dbHelper = new DBHelper(context);
        this.giftHelper = new GiftFetchHelper();
    }

    // 执行自动领取并保存结果
    public void fetchAndSaveGift(GiftFetchResultCallback callback) {
        List<DBHelper.PlayerInfo> playerInfos = dbHelper.getAllMeishiWechat();

        if (playerInfos.isEmpty()) {
            String resultSimple = "暂无账号";
            String resultNotification = "暂无账号";

            callback.onResult(
                    generateMap("✅", resultSimple, resultNotification)
            );

            return;
        }

        // 提取openid列表
        List<String> openids = new ArrayList<>();
        for (DBHelper.PlayerInfo info : playerInfos) {
            openids.add(info.openid);
        }

        // 子线程执行网络请求
        new Thread(() -> {
            try {
                giftHelper.fetchAllGifts(openids, new GiftFetchHelper.GiftFetchCallback() {
                    @Override
                    public void onResult(int successCount) {
                        String resultSimple = successCount + "个";
                        String resultNotification = successCount + "个已领取";

                        callback.onResult(
                                generateMap("✅", resultSimple, resultNotification)
                        );
                    }

                    @Override
                    public void onError() {
                        String resultSimple = "失败";
                        String resultNotification = "❌服务器";

                        callback.onResult(
                                generateMap("❌", resultSimple, resultNotification)
                        );
                    }
                });
            } catch (Exception e) {
                String resultSimple = "领取异常";
                String resultNotification = "❌请重新尝试";

                callback.onResult(
                        generateMap("❌", resultSimple, resultNotification)
                );
            }
        }).start();
    }

    // 保存结果到Map，用于及时输出数据
    private Map<String, String> generateMap(String resultEmoji, String resultSimple, String resultNotification) {
        Map<String, String> result = new HashMap<>();

        result.put("resultEmoji", resultEmoji);
        result.put("resultSimple", resultSimple);
        result.put("resultNotification", resultNotification);

        return result;
    }

}
