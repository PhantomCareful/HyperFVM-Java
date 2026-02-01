package com.careful.HyperFVM.utils.ForDashboard.AutoTask;

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
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class AutoTaskWorker extends Worker {
    private final DBHelper dbHelper = new DBHelper(getApplicationContext());
    private final AutoTaskNotificationManager autoManager;
    private final PersistentServiceNotificationManager persistentManager;

    public AutoTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        autoManager = new AutoTaskNotificationManager(context);
        persistentManager = new PersistentServiceNotificationManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WorkManager", "开始执行自动任务");

        Context context = getApplicationContext();

        try {
            // 检查本次执行是否需要发送通知
            String today = TimeUtil.getCurrentDate();
            String lastDate = dbHelper.getDashboardContent("last_date");
            boolean needExecute = !today.equals(lastDate)
                    || "失败".equals(dbHelper.getDashboardContent("meishi_wechat_result"));

            // 执行任务
            ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(context);
            executeDailyTasks.executeDailyTasks();
            // 延迟10秒，确保任务执行完毕
            try {
                Thread.sleep(10000); // 10秒延迟
            } catch (InterruptedException e) {
                Log.e("WorkManager", "延迟被中断", e);
                Thread.currentThread().interrupt();
                return Result.failure();
            }

            if (needExecute) {
                // 自动任务执行结果通知每天只发一次
                autoManager.createNotificationChannel();
                autoManager.sendGeneralNotification();
            }
            // 保护通知每小时发一次
            persistentManager.sendForegroundNotification();

            // 调度下一次任务
            scheduleNextTask(context);

            return Result.success();
        } catch (Exception e) {
            Log.e("WorkManager", "任务执行失败", e);
            return Result.failure();
        } finally {
            dbHelper.close();
        }
    }

    // 计算下一次任务的延迟并调度
    private void scheduleNextTask(Context context) {
        // 计算下一个整点01分
        LocalDateTime nextTime = calculateNextOclock01();

        long delay = Duration.between(LocalDateTime.now(), nextTime).toMillis();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(AutoTaskWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("AUTO_TASK_TAG")
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "AUTO_TASK_UNIQUE",
                ExistingWorkPolicy.REPLACE,
                nextRequest
        );

        Log.d("WorkManager", "下次任务时间: " + nextTime);
    }

    /**
     * 计算下一个整点01分的时间（与Application中相同的逻辑）
     */
    private LocalDateTime calculateNextOclock01() {
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        if (currentMinute < 1) {
            // 当前分钟数小于1，说明当前小时的01分还没到
            return LocalDateTime.of(now.toLocalDate(), LocalTime.of(currentHour, 1, 0));
        } else {
            // 当前分钟数大于等于1，说明当前小时的01分已过，安排到下个小时
            int nextHour = currentHour + 1;
            LocalDate date = now.toLocalDate();

            if (nextHour > 23) {
                // 跨天处理
                date = date.plusDays(1);
                nextHour = 0;
            }

            return LocalDateTime.of(date, LocalTime.of(nextHour, 1, 0));
        }
    }
}
