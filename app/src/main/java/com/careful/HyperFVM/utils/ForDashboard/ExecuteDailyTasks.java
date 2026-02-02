package com.careful.HyperFVM.utils.ForDashboard;

import android.content.Context;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.Activity.ActivityCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.FertilizationTask.FertilizationTaskCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.MeishiWechat.GiftFetcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear.NewYearCatcher;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

public class ExecuteDailyTasks {

    private final DBHelper dbHelper;
    private final GiftFetcher giftFetcher;
    private final ActivityCatcher activityCatcher;
    private final FertilizationTaskCatcher fertilizationTaskCatcher;
    private final NewYearCatcher newYearCatcher;

    public ExecuteDailyTasks(Context context) {
        dbHelper = new DBHelper(context);
        giftFetcher = new GiftFetcher(context);
        activityCatcher = new ActivityCatcher(context);
        fertilizationTaskCatcher = new FertilizationTaskCatcher(context);
        newYearCatcher = new NewYearCatcher(context);
    }

    public void executeDailyTasks() {
        String today = TimeUtil.getCurrentDate();
        String lastDate = dbHelper.getDashboardContent("last_date");
        boolean needExecute = !today.equals(lastDate)
                || "失败".equals(dbHelper.getDashboardContent("meishi_wechat_result"));
        if (needExecute) {
            giftFetcher.fetchAndSaveGift();
        }
        activityCatcher.catchTodayActivityInfo();
        fertilizationTaskCatcher.catchFertilizationTaskInfo();
        newYearCatcher.catchBountyInfo();
        newYearCatcher.catchMillionConsumptionInfo();
        newYearCatcher.catchLuckyConsumptionInfo();
    }

    public void executeDailyTasksForRefreshDashboard() {
        giftFetcher.fetchAndSaveGift();
        activityCatcher.catchTodayActivityInfo();
        fertilizationTaskCatcher.catchFertilizationTaskInfo();
        newYearCatcher.catchBountyInfo();
        newYearCatcher.catchMillionConsumptionInfo();
        newYearCatcher.catchLuckyConsumptionInfo();
    }
}
