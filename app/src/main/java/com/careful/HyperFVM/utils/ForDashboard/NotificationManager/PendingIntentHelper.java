package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.careful.HyperFVM.MainActivity;

public class PendingIntentHelper {

    // 创建通用的PendingIntent（跳转MainActivity）
    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return android.app.PendingIntent.getActivity(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
