package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

import android.annotation.SuppressLint;
import androidx.activity.ComponentActivity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class DashboardNotificationManager {
    // 通知相关常量
    private static final String CHANNEL_ID_DASHBOARD = "data_station_persistent_channel"; // 通知通道：仪表盘信息常驻显示
    private static final int NOTIFICATION_ID_MEISHI_WECHAT = 1001; // 唯一通知ID
    private static final int NOTIFICATION_ID_ACTIVITY = 1002; // 唯一通知ID
    private static final int NOTIFICATION_ID_FERTILIZATION_TASK = 1003; // 唯一通知ID
    private static final int NOTIFICATION_ID_NEW_YEAR = 1004; // 唯一通知ID

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    private final DBHelper dbHelper;

    public DashboardNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.dbHelper = new DBHelper(context);
    }

    // 独立创建通知渠道
    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_DASHBOARD,
                "HyperFVM小助手", // 渠道名称（用户在设置中可见）
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("仪表盘信息常驻显示"); // 渠道描述
        channel.setSound(null, null); // 禁用通知声音
        channel.setShowBadge(false); // 不显示角标

        // 获取系统通知管理器并注册渠道
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    // 发送常驻通知
    @SuppressLint("MissingPermission")
    private void sendImportantNotification(String title, String content, int NOTIFICATION_ID) {
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_DASHBOARD)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content.isEmpty() ? "" : content)
                .setContentIntent(PendingIntentHelper.getPendingIntent(context))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(false)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // 发送“温馨礼包”通知
    public void sendMeishiWechatImportantNotification() {
        // 读取礼包领取结果
        String giftResult = dbHelper.getDashboardContent("meishi_wechat_result_text");
        giftResult = (giftResult.isEmpty() ? "" : giftResult);

        sendImportantNotification("温馨礼包", giftResult, NOTIFICATION_ID_MEISHI_WECHAT);
    }

    // 发送“双爆信息”通知
    public void sendActivityNotification() {
        // 读取双倍双爆结果
        String activityResult = dbHelper.getDashboardContent("double_explosion_rate_notification");
        activityResult = (activityResult.isEmpty() ? "null" : activityResult);

        sendImportantNotification("双爆信息", activityResult, NOTIFICATION_ID_ACTIVITY);
    }

    // 发送“施肥任务”通知
    public void sendFertilizationTaskNotification() {
        // 读取施肥活动结果
        String fertilizationTaskResult = dbHelper.getDashboardContent("fertilization_task_notification");
        fertilizationTaskResult = (fertilizationTaskResult.isEmpty() ? "null" : fertilizationTaskResult);

        sendImportantNotification("施肥任务", fertilizationTaskResult, NOTIFICATION_ID_FERTILIZATION_TASK);
    }

    // 发送“美食悬赏”通知
    public void sendNewYearNotification() {
        // 读取美食悬赏活动结果
        String newYearResult = dbHelper.getDashboardContent("new_year_notification");
        newYearResult = (newYearResult.isEmpty() ? "null" : newYearResult);

        sendImportantNotification("美食悬赏", newYearResult, NOTIFICATION_ID_NEW_YEAR);
    }

    // 清除指定ID的通知
    private void cancelNotification(int NOTIFICATION_ID) {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    // 清除“温馨礼包”通知
    public void cancelMeishiWechatNotification() {
        cancelNotification(NOTIFICATION_ID_MEISHI_WECHAT);
    }

    // 清除“双爆信息”通知
    public void cancelActivityNotification() {
        cancelNotification(NOTIFICATION_ID_ACTIVITY);
    }

    // 清除“施肥活动”通知
    public void cancelFertilizationTaskNotification() {
        cancelNotification(NOTIFICATION_ID_FERTILIZATION_TASK);
    }

    // 清除“美食悬赏”通知
    public void cancelNewYearNotification() {
        cancelNotification(NOTIFICATION_ID_NEW_YEAR);
    }

    // 清除所有通知
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * 封装权限请求逻辑（仅Android 13+）
     * @param fragment 调用权限请求的Fragment（需依赖其生命周期）
     * @param callback 权限请求结果回调
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void requestNotificationPermission(Fragment fragment, PermissionCallback callback) {
        // 在Fragment中注册权限请求 launcher
        ActivityResultLauncher<String> permissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        callback.onPermissionGranted(); // 权限通过，回调给Fragment
                    } else {
                        callback.onPermissionDenied();  // 权限拒绝，回调给Fragment
                    }
                }
        );
        // 发起权限请求
        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
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
