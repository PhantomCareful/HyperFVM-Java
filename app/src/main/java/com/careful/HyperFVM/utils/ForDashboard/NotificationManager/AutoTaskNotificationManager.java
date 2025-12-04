package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class AutoTaskNotificationManager {
    private static final String CHANNEL_ID_GENERAL = "general_channel"; // 通知通道：普通通知
    private static final int NOTIFICATION_ID_AUTO_MEISHI_WECHAT = 2001; // 唯一通知ID

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    private final DBHelper dbHelper;


    public AutoTaskNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.dbHelper = new DBHelper(context);
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
    private void sendGeneralNotification(String title, String content, int NOTIFICATION_ID) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content.isEmpty() ? "" : content)
                .setContentIntent(PendingIntentHelper.getPendingIntent(context))
                .setOngoing(false)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis()) // 设置时间为当前发送时间（毫秒级）
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void sendGeneralNotification() {
        String result = "温馨礼包：" + dbHelper.getDashboardContent("meishi_wechat_result_text_notification") + " " +
                        "双爆信息：" + dbHelper.getDashboardContent("double_explosion_rate_notification") + "\n" +
                        "施肥活动：" + dbHelper.getDashboardContent("fertilization_task_notification") + " " +
                        "美食悬赏：" + dbHelper.getDashboardContent("new_year_notification");
        sendGeneralNotification("自动任务执行完成✅", result, NOTIFICATION_ID_AUTO_MEISHI_WECHAT);
    }
}
