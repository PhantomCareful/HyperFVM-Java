package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.content.Context;

import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;

public class TileTaskHelper {
    public static void executeTileTasks(
            Context context,
            TileTaskNotificationManager tileTaskNotificationManager
    ) {
        tileTaskNotificationManager.createNotificationChannel();

        ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(context);

        tileTaskNotificationManager.sendAutoTaskNotification("温馨礼包⏳", "正在领取温馨礼包，请稍候~");
        String contentGiftTask = executeDailyTasks.executeGiftTask();

        tileTaskNotificationManager.sendAutoTaskNotification("更新数据⏳", "正在更新仪表盘数据，请稍候~");
        String contentDashboardTask = executeDailyTasks.executeDashboardTask();

        tileTaskNotificationManager.sendAutoTaskNotification("执行完成🎉", contentGiftTask + " " + contentDashboardTask);
    }
}
