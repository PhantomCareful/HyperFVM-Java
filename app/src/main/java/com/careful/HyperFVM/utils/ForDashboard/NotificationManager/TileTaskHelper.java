package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.content.Context;

import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTask;

public class TileTaskHelper {
    public static void executeTileTasks(
            Context context,
            TileTaskNotificationManager tileTaskNotificationManager
    ) {
        tileTaskNotificationManager.createNotificationChannel();

        ExecuteDailyTask executeDailyTask = new ExecuteDailyTask(context);

        // 存放结果
        final String[] giftResult = {""};
        final String[] dashboardResult = {""};
        final boolean[] giftDone = {false};
        final boolean[] dashboardDone = {false};

        // 检查是否都完成，完成则发送最终通知
        Runnable checkAndSend = () -> {
            if (giftDone[0] && dashboardDone[0]) {
                String combinedContent = giftResult[0] + " " + dashboardResult[0];
                tileTaskNotificationManager.sendAutoTaskNotification("执行完成🎉", combinedContent);
            }
        };

        tileTaskNotificationManager.sendAutoTaskNotification("温馨礼包⏳", "正在领取温馨礼包，请稍候~");
        executeDailyTask.executeGiftTileTask(resultGift -> {
            giftResult[0] = resultGift;
            giftDone[0] = true;
            checkAndSend.run();

            tileTaskNotificationManager.sendAutoTaskNotification("更新数据⏳", "正在更新仪表盘数据，请稍候~");
            executeDailyTask.executeDashboardTileTask(resultDashboard -> {
                dashboardResult[0] = resultDashboard;
                dashboardDone[0] = true;
                checkAndSend.run();
            });
        });
    }
}
