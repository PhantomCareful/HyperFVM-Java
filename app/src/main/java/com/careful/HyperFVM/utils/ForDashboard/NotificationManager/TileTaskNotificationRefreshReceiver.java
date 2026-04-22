package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TileTaskNotificationRefreshReceiver extends BroadcastReceiver {
    // 定义一个常量作为通知ID的Key
    public static final String ACTION_REFRESH = "ACTION_REFRESH_AUTO_TASK_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            TileTaskNotificationManager tileTaskNotificationManager = new TileTaskNotificationManager(context);
            TileTaskHelper.executeTileTasks(context, tileTaskNotificationManager);
        }
    }
}
