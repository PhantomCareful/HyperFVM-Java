package com.careful.HyperFVM.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PersistentServiceNotificationManager;

public class PersistentService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 9001;

    @Override
    public void onCreate() {
        super.onCreate();
        // 启动时立即显示前台通知（必须）
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 服务被杀死后，系统会尝试重启（START_STICKY）
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 创建前台通知（必须显示，否则服务会被系统终止）
    public Notification createForegroundNotification() {
        PersistentServiceNotificationManager manager = new PersistentServiceNotificationManager(this);
        manager.createNotificationChannel();
        // 构建通知
        return manager.getForegroundNotification();
    }

    // 服务销毁时移除前台通知
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止前台服务并移除通知（STOP_FOREGROUND_REMOVE表示彻底移除）
        stopForeground(STOP_FOREGROUND_REMOVE);
        // 可选：如果需要彻底清除通知，可额外调用通知管理器取消
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.cancel(FOREGROUND_NOTIFICATION_ID);
    }
}
