package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoTaskNotificationRefreshReceiver extends BroadcastReceiver {
    // 定义一个常量作为通知ID的Key
    public static final String ACTION_REFRESH = "ACTION_REFRESH_AUTO_TASK_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            // 执行与 AutoTaskTileService.executeAutoTask() 相同的逻辑
            AutoTaskNotificationManager manager = new AutoTaskNotificationManager(context);
            manager.createNotificationChannel();
            manager.sendAutoTaskNotification(0, "执行中⏳");
            manager.sendAutoTaskNotification(50, "执行中⏳");
            manager.sendAutoTaskNotification(100, "执行完成🎉");
        }
    }
}
