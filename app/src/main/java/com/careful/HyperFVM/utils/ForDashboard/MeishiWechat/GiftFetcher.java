package com.careful.HyperFVM.utils.ForDashboard.MeishiWechat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class GiftFetcher {
    private final DBHelper dbHelper;
    private final GiftFetchHelper giftHelper;
    private final Handler mainHandler;
    private final Context context;

    // 回调接口：通知领取结果
    public interface GiftFetchListener {
        void onResult(String resultText);
    }

    public GiftFetcher(Context context) {
        this.dbHelper = new DBHelper(context);
        this.giftHelper = new GiftFetchHelper();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.context = context; // 初始化
    }

    // 执行自动领取并保存结果
    public void fetchAndSaveGift(GiftFetchListener listener) {
        List<DBHelper.PlayerInfo> playerInfos = dbHelper.getAllMeishiWechat();

        if (playerInfos.isEmpty()) {
            String resultText = "✅暂无账号可领取\n👉点击管理链接";
            dbHelper.updateDashboardContent("meishi_wechat_result_text_notification", "暂无✅");
            saveResult(resultText, "成功");
            listener.onResult(resultText);
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
                giftHelper.fetchAllGifts(context, openids, new GiftFetchHelper.GiftFetchCallback() {
                    @Override
                    public void onResult(int successCount) {
                        String text = "✅" + successCount + "个账号已完成领取😎😎\n👉点击管理链接";
                        dbHelper.updateDashboardContent("meishi_wechat_result_text_notification", successCount + "个✅");
                        saveResult(text, "成功");
                        mainHandler.post(() -> listener.onResult(text));
                    }

                    @Override
                    public void onError() {
                        String text = "❌领取失败，锑食服务器又炸了\n将在适当的时间再次尝试领取\n👉点击管理链接";
                        dbHelper.updateDashboardContent("meishi_wechat_result_text_notification", "服务器❌");
                        saveResult(text, "失败");
                        mainHandler.post(() -> listener.onResult(text));
                    }
                });
            } catch (Exception e) {
                String text = "❌领取异常\n将在适当的时间再次尝试领取\n👉点击管理链接";
                dbHelper.updateDashboardContent("meishi_wechat_result_text_notification", "失败❌");
                saveResult(text, "失败");
                mainHandler.post(() -> listener.onResult(text));
            }
        }).start();
    }

    // 保存结果到数据库
    private void saveResult(String resultText, String resultState) {
        dbHelper.updateDashboardContent("meishi_wechat_result_text", resultText);
        dbHelper.updateDashboardContent("meishi_wechat_result", resultState);
    }

    // 关闭数据库连接
    public void close() {
        dbHelper.close();
    }
}
