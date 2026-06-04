package com.careful.HyperFVM.utils.ForDashboard;

import android.content.Context;

import com.careful.HyperFVM.utils.ForDashboard.FromGame.Activity.ActivityCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.FertilizationTask.FertilizationTaskCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.MeishiWechat.GiftFetcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear.NewYearCatcher;
import com.careful.HyperFVM.utils.ForDashboard.FromGit.DashboardGitCatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecuteDailyTask {

    private final GiftFetcher giftFetcher;
    private final ActivityCatcher activityCatcher;
    private final FertilizationTaskCatcher fertilizationTaskCatcher;
    private final NewYearCatcher newYearCatcher;
    private final DashboardGitCatcher dashboardGitCatcher;

    public ExecuteDailyTask(Context context) {
        giftFetcher = new GiftFetcher(context);
        activityCatcher = new ActivityCatcher();
        fertilizationTaskCatcher = new FertilizationTaskCatcher();
        newYearCatcher = new NewYearCatcher();
        dashboardGitCatcher = new DashboardGitCatcher();
    }

    public void executeGiftTileTask(GiftTileTaskResultCallBack callBack) {
        giftFetcher.fetchAndSaveGift(result ->
                callBack.onResult("🎁温馨礼包：" + result.get("resultNotification"))
        );
    }

    public void executeDashboardTileTask(DashboardTileTaskResultCallBack callBack) {
        // 存放结果
        final String[] catchTodayActivityInfoResult = {""};
        final String[] catchFertilizationTaskInfoResult = {""};
        final String[] catchBountyInfoResult = {""};
        final String[] catchGitDashboardInfoResult = {""};
        final boolean[] catchTodayActivityInfoDone = {false};
        final boolean[] catchFertilizationTaskInfoDone = {false};
        final boolean[] catchBountyInfoDone = {false};
        final boolean[] catchMillionConsumptionInfoDone = {false};
        final boolean[] catchLuckyConsumptionInfoDone = {false};
        final boolean[] catchGitDashboardInfoDone = {false};

        // 检查是否都完成，完成则返回最终结果
        Runnable checkAndSend = () -> {
            if (catchTodayActivityInfoDone[0] && catchFertilizationTaskInfoDone[0] && catchBountyInfoDone[0] && catchMillionConsumptionInfoDone[0] && catchLuckyConsumptionInfoDone[0] && catchGitDashboardInfoDone[0]) {
                String result = catchTodayActivityInfoResult[0] + "\n" +
                        catchFertilizationTaskInfoResult[0] + " " + catchBountyInfoResult[0] + "\n" +
                        catchGitDashboardInfoResult[0];
                callBack.onResult(result);
            }
        };

        activityCatcher.catchTodayActivityInfo(result -> {
            catchTodayActivityInfoResult[0] = "💥双爆：" + result.get("resultNotification");
            catchTodayActivityInfoDone[0] = true;
            checkAndSend.run();
        });
        fertilizationTaskCatcher.catchFertilizationTaskInfo(result -> {
            catchFertilizationTaskInfoResult[0] = "🌳施肥：" + result.get("resultNotification");
            catchFertilizationTaskInfoDone[0] = true;
            checkAndSend.run();
        });
        newYearCatcher.catchBountyInfo(result -> {
            catchBountyInfoResult[0] = "📜悬赏：" + result.get("resultNotification");
            catchBountyInfoDone[0] = true;
            checkAndSend.run();
        });
        newYearCatcher.catchMillionConsumptionInfo(result -> {
            catchMillionConsumptionInfoDone[0] = true;
            checkAndSend.run();
        });
        newYearCatcher.catchLuckyConsumptionInfo(result -> {
            catchLuckyConsumptionInfoDone[0] = true;
            checkAndSend.run();
        });
        dashboardGitCatcher.catchGitDashboardInfo(result -> {
            catchGitDashboardInfoResult[0] = "🏝️三岛：" + result.get("resultThreeIslandsSimple") + " 🥟大赛：" + result.get("resultFoodContestSimple");
            catchGitDashboardInfoDone[0] = true;
            checkAndSend.run();
        });
    }

    public void executeDashboardTask(DashboardTaskResultCallBack callBack) {
        // 存放结果
        final List<Map<String, String>> catchMeishiWechatInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchTodayActivityInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchFertilizationTaskInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchBountyInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchMillionConsumptionInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchLuckyConsumptionInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final List<Map<String, String>> catchGitDashboardInfoResult = new ArrayList<>(Collections.nCopies(1, null));
        final boolean[] catchMeishiWechatInfoDone = {false};
        final boolean[] catchTodayActivityInfoDone = {false};
        final boolean[] catchFertilizationTaskInfoDone = {false};
        final boolean[] catchBountyInfoDone = {false};
        final boolean[] catchMillionConsumptionInfoDone = {false};
        final boolean[] catchLuckyConsumptionInfoDone = {false};
        final boolean[] catchGitDashboardInfoDone = {false};

        // 检查是否都完成，完成则返回最终结果
        Runnable checkAndReturn = () -> {
            if (catchMeishiWechatInfoDone[0] && catchTodayActivityInfoDone[0] && catchFertilizationTaskInfoDone[0] && catchBountyInfoDone[0] && catchMillionConsumptionInfoDone[0] && catchLuckyConsumptionInfoDone[0] && catchGitDashboardInfoDone[0]) {
                Map<String, String> result = new HashMap<>();

                // 将所有子任务获取到的Map数据汇总成一个Map
                // 温馨礼包
                result.put("resultMeishiWechatInfoSimple", catchMeishiWechatInfoResult.get(0).get("resultSimple"));
                result.put("resultMeishiWechatInfoNotification", catchMeishiWechatInfoResult.get(0).get("resultNotification"));
                result.put("resultMeishiWechatInfoEmoji", catchMeishiWechatInfoResult.get(0).get("resultEmoji"));

                // 双爆
                result.put("resultTodayActivityInfoSimple", catchTodayActivityInfoResult.get(0).get("resultSimple"));
                result.put("resultTodayActivityInfoNotification", catchTodayActivityInfoResult.get(0).get("resultNotification"));
                result.put("resultTodayActivityInfoEmoji", catchTodayActivityInfoResult.get(0).get("resultEmoji"));
                result.put("resultTodayActivityInfoContentStatus", catchTodayActivityInfoResult.get(0).get("resultContentStatus"));
                result.put("resultTodayActivityInfoContentDetail", catchTodayActivityInfoResult.get(0).get("resultContentDetail"));

                // 施肥任务
                result.put("resultFertilizationTaskInfoSimple", catchFertilizationTaskInfoResult.get(0).get("resultSimple"));
                result.put("resultFertilizationTaskInfoNotification", catchFertilizationTaskInfoResult.get(0).get("resultNotification"));
                result.put("resultFertilizationTaskInfoEmoji", catchFertilizationTaskInfoResult.get(0).get("resultEmoji"));
                result.put("resultFertilizationTaskInfoContentStatus", catchFertilizationTaskInfoResult.get(0).get("resultContentStatus"));
                result.put("resultFertilizationTaskInfoContentDetail", catchFertilizationTaskInfoResult.get(0).get("resultContentDetail"));

                // 美食悬赏
                result.put("resultBountyInfoSimple", catchBountyInfoResult.get(0).get("resultSimple"));
                result.put("resultBountyInfoNotification", catchBountyInfoResult.get(0).get("resultNotification"));
                result.put("resultBountyInfoEmoji", catchBountyInfoResult.get(0).get("resultEmoji"));
                result.put("resultBountyInfoContentStatus", catchBountyInfoResult.get(0).get("resultContentStatus"));
                result.put("resultBountyInfoContentDetail", catchBountyInfoResult.get(0).get("resultContentDetail"));

                // 百万消费
                result.put("resultMillionConsumptionInfoSimple", catchMillionConsumptionInfoResult.get(0).get("resultSimple"));
                result.put("resultMillionConsumptionInfoEmoji", catchMillionConsumptionInfoResult.get(0).get("resultEmoji"));
                result.put("resultMillionConsumptionInfoContentStatus", catchMillionConsumptionInfoResult.get(0).get("resultContentStatus"));
                result.put("resultMillionConsumptionInfoContentDetail", catchMillionConsumptionInfoResult.get(0).get("resultContentDetail"));

                // 抢红包
                result.put("resultLuckyConsumptionInfoSimple", catchLuckyConsumptionInfoResult.get(0).get("resultSimple"));
                result.put("resultLuckyConsumptionInfoEmoji", catchLuckyConsumptionInfoResult.get(0).get("resultEmoji"));
                result.put("resultLuckyConsumptionInfoContentStatus", catchLuckyConsumptionInfoResult.get(0).get("resultContentStatus"));
                result.put("resultLuckyConsumptionInfoContentDetail", catchLuckyConsumptionInfoResult.get(0).get("resultContentDetail"));

                // 日氪
                result.put("resultDailyRechargeSimple", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeSimple"));
                result.put("resultDailyRechargeEmoji", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeEmoji"));
                result.put("resultDailyRechargeContentStatus", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeContentStatus"));
                result.put("resultDailyRechargeContentDetail", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeContentDetail"));

                // 欢乐假期
                result.put("resultHappyHolidaySimple", catchGitDashboardInfoResult.get(0).get("resultHappyHolidaySimple"));
                result.put("resultHappyHolidayEmoji", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayEmoji"));
                result.put("resultHappyHolidayContentStatus", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayContentStatus"));
                result.put("resultHappyHolidayContentDetail", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayContentDetail"));

                // 助人为乐
                result.put("resultServerTeamUpSimple", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpSimple"));
                result.put("resultServerTeamUpEmoji", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpEmoji"));
                result.put("resultServerTeamUpContentStatus", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpContentStatus"));
                result.put("resultServerTeamUpContentDetail", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpContentDetail"));

                // 三岛
                result.put("resultThreeIslandsSimple", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsSimple"));
                result.put("resultThreeIslandsEmoji", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsEmoji"));
                result.put("resultThreeIslandsContentStatus", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsContentStatus"));
                result.put("resultThreeIslandsContentDetail", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsContentDetail"));

                // 美食大赛
                result.put("resultFoodContestSimple", catchGitDashboardInfoResult.get(0).get("resultFoodContestSimple"));
                result.put("resultFoodContestEmoji", catchGitDashboardInfoResult.get(0).get("resultFoodContestEmoji"));
                result.put("resultFoodContestContentStatus", catchGitDashboardInfoResult.get(0).get("resultFoodContestContentStatus"));
                result.put("resultFoodContestContentDetail", catchGitDashboardInfoResult.get(0).get("resultFoodContestContentDetail"));

                // 二转打折
                result.put("resultTransferDiscountSimple", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountSimple"));
                result.put("resultTransferDiscountEmoji", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountEmoji"));
                result.put("resultTransferDiscountContentStatus", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountContentStatus"));
                result.put("resultTransferDiscountContentDetail", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountContentDetail"));

                // App通知
                result.put("resultGlobalNotificationIsShow", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationIsShow"));
                result.put("resultGlobalNotificationTitle", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationTitle"));
                result.put("resultGlobalNotificationContent", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationContent"));

                callBack.onResult(result);
            }
        };

        giftFetcher.fetchAndSaveGift(result -> {
            catchMeishiWechatInfoResult.set(0, result);
            catchMeishiWechatInfoDone[0] = true;
            checkAndReturn.run();
        });
        activityCatcher.catchTodayActivityInfo(result -> {
            catchTodayActivityInfoResult.set(0, result);
            catchTodayActivityInfoDone[0] = true;
            checkAndReturn.run();
        });
        fertilizationTaskCatcher.catchFertilizationTaskInfo(result -> {
            catchFertilizationTaskInfoResult.set(0, result);
            catchFertilizationTaskInfoDone[0] = true;
            checkAndReturn.run();
        });
        newYearCatcher.catchBountyInfo(result -> {
            catchBountyInfoResult.set(0, result);
            catchBountyInfoDone[0] = true;
            checkAndReturn.run();
        });
        newYearCatcher.catchMillionConsumptionInfo(result -> {
            catchMillionConsumptionInfoResult.set(0, result);
            catchMillionConsumptionInfoDone[0] = true;
            checkAndReturn.run();
        });
        newYearCatcher.catchLuckyConsumptionInfo(result -> {
            catchLuckyConsumptionInfoResult.set(0, result);
            catchLuckyConsumptionInfoDone[0] = true;
            checkAndReturn.run();
        });
        dashboardGitCatcher.catchGitDashboardInfo(result -> {
            catchGitDashboardInfoResult.set(0, result);
            catchGitDashboardInfoDone[0] = true;
            checkAndReturn.run();
        });
    }
}
