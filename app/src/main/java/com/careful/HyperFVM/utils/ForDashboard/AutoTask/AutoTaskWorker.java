package com.careful.HyperFVM.utils.ForDashboard.AutoTask;

import static com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks.getCurrentDate;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PersistentServiceNotificationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class AutoTaskWorker extends Worker {
    private final DBHelper dbHelper = new DBHelper(getApplicationContext());

    public AutoTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WorkManager", "Starting to execute auto task");
        Context context = getApplicationContext();
        ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(context);
        AutoTaskNotificationManager autoTaskNotificationManager = new AutoTaskNotificationManager(context);
        autoTaskNotificationManager.createNotificationChannel();
        // Task execution logic
        String today = getCurrentDate();
        String lastDate = dbHelper.getDashboardContent("last_date");
        boolean needExecute = !today.equals(lastDate)
                || "失败".equals(dbHelper.getDashboardContent("meishi_wechat_result"));
        executeDailyTasks.executeDailyTasks();
        new Thread(() -> {
            try {
                // 等待10秒，确保任务执行完毕
                Thread.sleep(10000);
                if (needExecute) {
                    autoTaskNotificationManager.sendGeneralNotification();
                }
                PersistentServiceNotificationManager persistentServiceNotificationManager = new PersistentServiceNotificationManager(context);
                persistentServiceNotificationManager.sendForegroundNotification();
            } catch (InterruptedException e) {
                // 异常处理：恢复线程中断状态（避免影响其他逻辑）
                Thread.currentThread().interrupt();
            }
        }).start();
        // Schedule next task
        scheduleNextDayTask(context);
        return Result.success();
    }

    // 计算下一次任务的延迟并调度
    private void scheduleNextDayTask(Context context) {
        // Read current hour from database
        String currentHourStr = dbHelper.getSettingValueString("自动任务-初始时间");
        int currentHour;
        try {
            currentHour = Integer.parseInt(currentHourStr);
        } catch (NumberFormatException e) {
            currentHour = 0;
        }
        // Update hour to current + 1 (keep within 0-23)
        int nextHour = currentHour + 1;
        nextHour = nextHour > 23 ? 0 : nextHour;
        dbHelper.updateSettingValue("自动任务-初始时间", String.valueOf(nextHour));
        Log.d("WorkManager", "Updating next task hour to: " + nextHour);
        // Calculate next scheduled time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTargetTime = LocalDateTime.of(
                now.toLocalDate(),
                LocalTime.of(nextHour, 1, 0)
        );
        // If next time is passed, delay to next day
        if (now.isAfter(nextTargetTime)) {
            nextTargetTime = nextTargetTime.plusDays(1);
        }
        // Calculate delay and schedule
        long nextDelayMillis = Duration.between(now, nextTargetTime).toMillis();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(AutoTaskWorker.class)
                .setInitialDelay(nextDelayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("AUTO_TASK_TAG")
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "AUTO_TASK_UNIQUE",// 唯一标识：确保同一时间仅存在一个该任务
                ExistingWorkPolicy.REPLACE,// 策略：若已存在相同任务，替换为新任务
                nextRequest
        );
        Log.d("WorkManager", "Next task time: " + nextTargetTime + ", delay: " + nextDelayMillis + "ms");
    }

}
