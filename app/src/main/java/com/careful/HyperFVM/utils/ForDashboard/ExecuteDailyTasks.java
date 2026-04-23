package com.careful.HyperFVM.utils.ForDashboard;

import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.Activity.ActivityCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.FertilizationTask.FertilizationTaskCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.MeishiWechat.GiftFetcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear.NewYearCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGit.DashboardGitCatcher;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

public class ExecuteDailyTasks {

    private final DBHelper dbHelper;
    private final GiftFetcher giftFetcher;
    private final ActivityCatcher activityCatcher;
    private final FertilizationTaskCatcher fertilizationTaskCatcher;
    private final NewYearCatcher newYearCatcher;
    private final DashboardGitCatcher dashboardGitCatcher;

    public ExecuteDailyTasks(Context context) {
        dbHelper = new DBHelper(context);
        giftFetcher = new GiftFetcher(context);
        activityCatcher = new ActivityCatcher(context);
        fertilizationTaskCatcher = new FertilizationTaskCatcher(context);
        newYearCatcher = new NewYearCatcher(context);
        dashboardGitCatcher = new DashboardGitCatcher(context);
    }

    public void executeGiftTask(GiftTaskResultCallBack callBack) {
        giftFetcher.fetchAndSaveGift(result ->
                callBack.onResult("🎁温馨礼包：" + result.get("resultNotification"))
        );
    }

    public void executeDashboardTask(DashboardTaskResultCallBack callBack) {

        // 存放结果
        final String[] catchTodayActivityInfoResult = {""};
        final boolean[] catchTodayActivityInfoDone = {false};

        // 检查是否都完成，完成则返回最终结果
        Runnable checkAndSend = () -> {
            if (catchTodayActivityInfoDone[0]) {
                String result = catchTodayActivityInfoResult[0];
                callBack.onResult(result);
            }
        };

        activityCatcher.catchTodayActivityInfo(result -> {
            catchTodayActivityInfoResult[0] = "⬆️双爆：" + result.get("resultNotification");
            catchTodayActivityInfoDone[0] = true;
            checkAndSend.run();
        });
        fertilizationTaskCatcher.catchFertilizationTaskInfo();
        newYearCatcher.catchBountyInfo();
        newYearCatcher.catchMillionConsumptionInfo();
        newYearCatcher.catchLuckyConsumptionInfo();
        dashboardGitCatcher.catchGitDashboardInfo();

        /*return "⬆️双爆：" + dbHelper.getDashboardContent("double_explosion_rate_notification") + "\n" +
                "🌳施肥：" + dbHelper.getDashboardContent("fertilization_task_notification") + " " +
                "📜悬赏：" + dbHelper.getDashboardContent("bounty_notification") + "\n" +
                dbHelper.getDashboardContent("git_dashboard_notification");*/
    }

    public void executeDailyTasks() {
        /*String today = TimeUtil.getCurrentDate();
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
        dashboardGitCatcher.catchGitDashboardInfo();*/
    }

    public void executeDailyTasksForRefreshDashboard() {
        /*giftFetcher.fetchAndSaveGift();
        activityCatcher.catchTodayActivityInfo();
        fertilizationTaskCatcher.catchFertilizationTaskInfo();
        newYearCatcher.catchBountyInfo();
        newYearCatcher.catchMillionConsumptionInfo();
        newYearCatcher.catchLuckyConsumptionInfo();
        dashboardGitCatcher.catchGitDashboardInfo();*/
    }
}
