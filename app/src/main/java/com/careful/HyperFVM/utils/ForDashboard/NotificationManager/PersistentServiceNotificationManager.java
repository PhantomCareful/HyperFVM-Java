package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.R;

public class PersistentServiceNotificationManager {
    private static final String CHANNEL_ID_PERSISTENT = "persistent_channel";
    private static final int FOREGROUND_NOTIFICATION_ID = 9001;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public PersistentServiceNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_PERSISTENT,
                "前台服务", // 渠道名称（用户在设置中可见）
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("尽量确保自动任务在任何时候都能顺利执行"); // 渠道描述
        channel.setSound(null, null); // 禁用通知声音
        channel.setShowBadge(false); // 不显示角标

        // 获取系统通知管理器并注册渠道
        notificationManager.createNotificationChannel(channel);
    }

    public Notification getForegroundNotification() {
        return new NotificationCompat.Builder(context, "persistent_channel")
                .setContentTitle("HyperFVM正在全力保护美味镇🛡")
                .setContentText("点击进入App")
                .setSmallIcon(R.mipmap.ic_launcher) // 替换为你的图标
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true) // 通知无法被手动关闭
                .setContentIntent(PendingIntentHelper.getPendingIntent(context))
                .build();
    }

    @SuppressLint("MissingPermission")
    public void sendForegroundNotification() {
        Notification notification = getForegroundNotification();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }
}
