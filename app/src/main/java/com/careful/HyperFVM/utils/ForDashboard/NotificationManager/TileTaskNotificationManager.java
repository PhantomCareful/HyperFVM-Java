package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.R;

public class TileTaskNotificationManager {
    private static final String CHANNEL_ID_GENERAL = "general_channel"; // 通知通道：普通通知
    private static final int NOTIFICATION_ID_AUTO_MEISHI_WECHAT = 2001; // 唯一通知ID

    private final Context context;
    private final NotificationManagerCompat notificationManager;


    public TileTaskNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    // 独立创建通知渠道
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_GENERAL,
                "磁贴任务通道", // 渠道名称（用户在设置中可见）
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("快捷操作，解放双手"); // 渠道描述
        channel.setSound(null, null); // 禁用通知声音
        channel.setShowBadge(false); // 不显示角标

        // 获取系统通知管理器并注册渠道
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 发送实时活动通知（Android 16+）
     * 如果系统不支持实时活动，会自动降级为普通通知
     */
    @SuppressLint("MissingPermission")
    public void sendAutoTaskNotification(String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // Android 16+：发送Live Updates实时通知
            sendPromotedNotification(title, content);
        } else {
            // 降级：发送普通通知
            sendGeneralNotification(title, content);
        }
    }

    /**
     * 构建实时活动通知
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void sendPromotedNotification(String title, String content) {
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        dismissIntent.putExtra(NotificationDismissReceiver.ACTION_DISMISS, NOTIFICATION_ID_AUTO_MEISHI_WECHAT);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_AUTO_MEISHI_WECHAT,
                dismissIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent refreshIntent = new Intent(context, TileTaskNotificationRefreshReceiver.class);
        refreshIntent.setAction(TileTaskNotificationRefreshReceiver.ACTION_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_AUTO_MEISHI_WECHAT + 1, // 使用不同的 requestCode
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Bundle bundle = new Bundle();
        bundle.putBoolean(Notification.EXTRA_REQUEST_PROMOTED_ONGOING, true);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.app_icon_tile)
                .addAction(R.drawable.app_icon_tile, "刷新内容", refreshPendingIntent)
                .addAction(R.drawable.app_icon_tile, "关闭", dismissPendingIntent)
                .setOngoing(true)
                .addExtras(bundle)
                .build();

        notificationManager.notify(NOTIFICATION_ID_AUTO_MEISHI_WECHAT, notification);
    }

    /**
     * 发送一般通知
     */
    @SuppressLint("MissingPermission")
    public void sendGeneralNotification(String title, String content) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(PendingIntentHelper.getPendingIntent(context))
                .setOngoing(false)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis()) // 设置时间为当前发送时间（毫秒级）
                .build();
        notificationManager.notify(TileTaskNotificationManager.NOTIFICATION_ID_AUTO_MEISHI_WECHAT, notification);
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
