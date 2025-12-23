package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.R;

public class AutoTaskNotificationManager {
    private static final String CHANNEL_ID_GENERAL = "general_channel"; // 通知通道：普通通知
    private static final int NOTIFICATION_ID_AUTO_MEISHI_WECHAT = 2001; // 唯一通知ID

    private final Context context;
    private final NotificationManagerCompat notificationManager;


    public AutoTaskNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    // 独立创建通知渠道
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_GENERAL,
                "自动任务通道", // 渠道名称（用户在设置中可见）
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("顾名思义，解放双手"); // 渠道描述
        channel.setSound(null, null); // 禁用通知声音
        channel.setShowBadge(false); // 不显示角标

        // 获取系统通知管理器并注册渠道
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    // 发送一般通知
    @SuppressLint("MissingPermission")
    public void sendGeneralNotification() {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("自动任务执行完成✅")
                .setContentIntent(PendingIntentHelper.getPendingIntent(context))
                .setOngoing(false)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis()) // 设置时间为当前发送时间（毫秒级）
                .build();
        notificationManager.notify(AutoTaskNotificationManager.NOTIFICATION_ID_AUTO_MEISHI_WECHAT, notification);
    }

    /**
     * 封装权限请求逻辑（仅Android 13+）
     * @param activity 调用权限请求的Activity（需依赖其生命周期）
     * @param callback 权限请求结果回调
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void requestNotificationPermission(ComponentActivity activity, PermissionCallback callback) {
        // 在Activity中注册权限请求 launcher
        ActivityResultLauncher<String> permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        callback.onPermissionGranted();
                    } else {
                        callback.onPermissionDenied();
                    }
                }
        );
        // 发起权限请求
        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
    }
}
