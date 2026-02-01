package com.careful.HyperFVM.utils.ForDashboard.MeishiWechat;

import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class GiftFetcher {
    private final DBHelper dbHelper;
    private final GiftFetchHelper giftHelper;

    public GiftFetcher(Context context) {
        this.dbHelper = new DBHelper(context);
        this.giftHelper = new GiftFetchHelper();
    }

    // 执行自动领取并保存结果
    public void fetchAndSaveGift() {
        List<DBHelper.PlayerInfo> playerInfos = dbHelper.getAllMeishiWechat();

        if (playerInfos.isEmpty()) {
            String resultSimple = "暂无";
            String resultNotification = "暂无✅";
            saveResult("✅", resultSimple, resultNotification, "成功");
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
                        String resultNotification = successCount + "个✅";
                        saveResult("✅", resultSimple, resultNotification, "成功");
                    }

                    @Override
                    public void onError() {
                        String resultSimple = "失败";
                        String resultNotification = "服务器❌";
                        saveResult("❌", resultSimple, resultNotification, "失败");
                    }
                });
            } catch (Exception e) {
                String resultSimple = "领取异常";
                String resultNotification = "失败❌";
                saveResult("❌", resultSimple, resultNotification, "失败");
            }
        }).start();
    }

    // 保存结果到数据库
    private void saveResult(String resultEmoji, String resultSimple, String resultNotification, String resultState) {
        Log.d("meishi_wechat_result", "in util: resultEmoji: " + resultEmoji + ", resultSimple: " + resultSimple + ", resultNotification: " + resultNotification + ", resultState: " + resultState);
        dbHelper.updateDashboardContent("meishi_wechat_result_emoji", resultEmoji);
        dbHelper.updateDashboardContent("meishi_wechat_result_text", resultSimple);
        dbHelper.updateDashboardContent("meishi_wechat_result_text_notification", resultNotification);
        dbHelper.updateDashboardContent("meishi_wechat_result", resultState);
        if (resultEmoji.equals("✅")) {
            dbHelper.updateDashboardContent("last_date", TimeUtil.getCurrentDate());
        }
    }

}
