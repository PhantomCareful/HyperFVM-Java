package com.careful.HyperFVM.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PersistentServiceNotificationManager;

public class PersistentService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 9001;
    private static final String TAG = "PersistentService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建");
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());

        // 可选：定时检查服务状态
        startSelfCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "服务启动命令");

        // 如果服务被杀死，尝试重启
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isServiceRunning(this)) {
                Log.w(TAG, "服务被杀死，尝试重启");
                Intent restartIntent = new Intent(this, PersistentService.class);
                startForegroundService(restartIntent);
            }
        }, 5000);

        return START_STICKY;
    }

    private void startSelfCheck() {
        // 定期检查服务状态
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isServiceRunning(this)) {
                Log.w(TAG, "自检发现服务停止，重启");
                Intent restartIntent = new Intent(this, PersistentService.class);
                startForegroundService(restartIntent);
            }
        }, 60000); // 每分钟检查一次
    }

    private boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (PersistentService.class.getName().equals(service.service.getClassName())) {
                    return false;
                }
            }
        }
        return true;
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
