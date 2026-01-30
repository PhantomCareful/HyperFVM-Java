package com.careful.HyperFVM.utils.ForDashboard;

import android.content.Context;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.Activity.ActivityCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FertilizationTask.FertilizationTask;
import com.careful.HyperFVM.utils.ForDashboard.MeishiWechat.GiftFetcher;
import com.careful.HyperFVM.utils.ForDashboard.NewYear.NewYear;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExecuteDailyTasks {

    private final DBHelper dbHelper;
    private final GiftFetcher giftFetcher;
    private final ActivityCatcher activityCatcher;
    private final FertilizationTask fertilizationTask;
    private final NewYear newYear;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public ExecuteDailyTasks(Context context) {
        dbHelper = new DBHelper(context);
        giftFetcher = new GiftFetcher(context);
        activityCatcher = new ActivityCatcher(context);
        fertilizationTask = new FertilizationTask(context);
        newYear = new NewYear(context);
    }

    public void executeDailyTasks() {
        String today = getCurrentDate();
        String lastDate = dbHelper.getDashboardContent("last_date");
        boolean needExecute = !today.equals(lastDate)
                || "失败".equals(dbHelper.getDashboardContent("meishi_wechat_result"));
        if (needExecute) {
            giftFetcher.fetchAndSaveGift(resultText -> dbHelper.updateDashboardContent("last_date", today));
        }
        activityCatcher.parseTodayActivityContent();
        fertilizationTask.execute();
        newYear.execute();
    }

    public void executeDailyTasksForRefreshDashboard() {
        String today = getCurrentDate();
        giftFetcher.fetchAndSaveGift(resultText -> dbHelper.updateDashboardContent("last_date", today));
        activityCatcher.parseTodayActivityContent();
        fertilizationTask.execute();
        newYear.execute();
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(new Date());
    }
}
