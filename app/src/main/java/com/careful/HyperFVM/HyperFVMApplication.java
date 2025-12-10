package com.careful.HyperFVM;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.AutoTask.AutoTaskWorker;
import com.careful.HyperFVM.utils.ForDesign.DeviceType.DeviceTypeUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.google.android.material.color.DynamicColors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class HyperFVMApplication extends Application {

    private DBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        DeviceTypeUtil.init(this);
        SmallestWidthUtil.init(this);

        dbHelper = new DBHelper(getApplicationContext());

        // 设置每天固定时间执行任务
        if (dbHelper.getSettingValue("自动任务")) {
            scheduleDailyTask();
        }
    }

    /**
     * 每天固定时间执行任务
     */
    public void scheduleDailyTask() {
        // Read stored hour from database (default 0)
        String hourStr = dbHelper.getSettingValueString("自动任务-初始时间");
        int targetHour;
        try {
            targetHour = Integer.parseInt(hourStr);
            if (targetHour < 0 || targetHour > 23) {
                targetHour = 0;
            }
        } catch (NumberFormatException e) {
            targetHour = 0;
            Log.e("WorkManager", "Failed to read auto task time, using default value 0");
        }
        // Build scheduled time: current date + target hour + fixed 1 minute 0 second
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.of(
                now.toLocalDate(),
                LocalTime.of(targetHour, 1, 0)
        );
        Log.d("WorkManager", String.valueOf(now));
        Log.d("WorkManager", String.valueOf(targetTime));
        Log.d("WorkManager", String.valueOf(now.isAfter(targetTime)));
        // Check if current time has passed scheduled time
        if (now.isAfter(targetTime)) {
            // Update database hour to current hour + 1 (keep within 0-23)
            int newHour = now.getMinute() == 0 ? now.getHour() : now.getHour() + 1;
            newHour = newHour > 23 ? 0 : newHour;
            dbHelper.updateSettingValue("自动任务-初始时间", String.valueOf(newHour));
            Log.d("WorkManager", "Current time has passed scheduled time, updating task hour to: " + newHour);
            // Recalculate scheduled time with updated hour
            targetTime = LocalDateTime.of(
                    now.toLocalDate(),
                    LocalTime.of(newHour, 1, 0)
            );
            // Calculate delay milliseconds
            long initialDelayMillis = Duration.between(now, targetTime).toMillis();
            Log.d("WorkManager", "In Application, Scheduled execution time: " + targetTime + ", delay: " + initialDelayMillis + "ms");
            // Create constraints
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            // Create and enqueue task
            OneTimeWorkRequest dailyTaskRequest = new OneTimeWorkRequest.Builder(AutoTaskWorker.class)
                    .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .addTag("AUTO_TASK_TAG")
                    .build();
            WorkManager.getInstance(this).enqueueUniqueWork(
                    "AUTO_TASK_UNIQUE",// 唯一标识：确保同一时间仅存在一个该任务
                    ExistingWorkPolicy.REPLACE,// 策略：若已存在相同任务，替换为新任务
                    dailyTaskRequest
            );
            Log.d("WorkManager", "Task submitted, ID: " + dailyTaskRequest.getId());
            // Observe task state
            WorkManager.getInstance(this)
                    .getWorkInfoByIdLiveData(dailyTaskRequest.getId())
                    .observeForever(workInfo -> {
                        if (workInfo != null) {
                            Log.d("WorkManager", "Task state: " + workInfo.getState());
                            if (workInfo.getState() == WorkInfo.State.BLOCKED) {
                                Log.d("WorkManager", "Task blocked reason: Constraints not met " + workInfo.getConstraints());
                            }
                        }
                    });
        } else {
            Log.d("WorkManager", "early, target time: " + targetTime);
        }
    }
}
