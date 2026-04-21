package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDismissReceiver extends BroadcastReceiver {
    // 定义一个常量作为通知ID的Key
    public static final String ACTION_DISMISS = "ACTION_DISMISS_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 从Intent中取出通知ID
        int notificationId = intent.getIntExtra(ACTION_DISMISS, -1);

        if (notificationId != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(notificationId);
        }
    }
}
