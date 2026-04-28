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
        activityCatcher = new ActivityCatcher(context);
        fertilizationTaskCatcher = new FertilizationTaskCatcher(context);
        newYearCatcher = new NewYearCatcher(context);
        dashboardGitCatcher = new DashboardGitCatcher(context);
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
                result.put("resultMeishiWechatInfoDetail", catchMeishiWechatInfoResult.get(0).get("resultDetail"));

                // 双爆
                result.put("resultTodayActivityInfoSimple", catchTodayActivityInfoResult.get(0).get("resultSimple"));
                result.put("resultTodayActivityInfoNotification", catchTodayActivityInfoResult.get(0).get("resultNotification"));
                result.put("resultTodayActivityInfoEmoji", catchTodayActivityInfoResult.get(0).get("resultEmoji"));
                result.put("resultTodayActivityInfoDetail", catchTodayActivityInfoResult.get(0).get("resultDetail"));

                // 施肥任务
                result.put("resultFertilizationTaskInfoSimple", catchFertilizationTaskInfoResult.get(0).get("resultSimple"));
                result.put("resultFertilizationTaskInfoNotification", catchFertilizationTaskInfoResult.get(0).get("resultNotification"));
                result.put("resultFertilizationTaskInfoEmoji", catchFertilizationTaskInfoResult.get(0).get("resultEmoji"));
                result.put("resultFertilizationTaskInfoDetail", catchFertilizationTaskInfoResult.get(0).get("resultDetail"));

                // 美食悬赏
                result.put("resultBountyInfoSimple", catchBountyInfoResult.get(0).get("resultSimple"));
                result.put("resultBountyInfoNotification", catchBountyInfoResult.get(0).get("resultNotification"));
                result.put("resultBountyInfoEmoji", catchBountyInfoResult.get(0).get("resultEmoji"));
                result.put("resultBountyInfoDetail", catchBountyInfoResult.get(0).get("resultDetail"));

                // 百万消费
                result.put("resultMillionConsumptionInfoSimple", catchMillionConsumptionInfoResult.get(0).get("resultSimple"));
                result.put("resultMillionConsumptionInfoEmoji", catchMillionConsumptionInfoResult.get(0).get("resultEmoji"));
                result.put("resultMillionConsumptionInfoDetail", catchMillionConsumptionInfoResult.get(0).get("resultDetail"));

                // 抢红包
                result.put("resultLuckyConsumptionInfoSimple", catchLuckyConsumptionInfoResult.get(0).get("resultSimple"));
                result.put("resultLuckyConsumptionInfoEmoji", catchLuckyConsumptionInfoResult.get(0).get("resultEmoji"));
                result.put("resultLuckyConsumptionInfoDetail", catchLuckyConsumptionInfoResult.get(0).get("resultDetail"));

                // 日氪
                result.put("resultDailyRechargeSimple", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeSimple"));
                result.put("resultDailyRechargeEmoji", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeEmoji"));
                result.put("resultDailyRechargeDetail", catchGitDashboardInfoResult.get(0).get("resultDailyRechargeDetail"));

                // 欢乐假期
                result.put("resultHappyHolidaySimple", catchGitDashboardInfoResult.get(0).get("resultHappyHolidaySimple"));
                result.put("resultHappyHolidayEmoji", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayEmoji"));
                result.put("resultHappyHolidayDetail", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayDetail"));

                // 助人为乐
                result.put("resultServerTeamUpSimple", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpSimple"));
                result.put("resultServerTeamUpEmoji", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpEmoji"));
                result.put("resultServerTeamUpDetail", catchGitDashboardInfoResult.get(0).get("resultServerTeamUpDetail"));

                // 三岛
                result.put("resultThreeIslandsSimple", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsSimple"));
                result.put("resultThreeIslandsEmoji", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsEmoji"));
                result.put("resultThreeIslandsDetail", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsDetail"));

                // 美食大赛
                result.put("resultFoodContestSimple", catchGitDashboardInfoResult.get(0).get("resultFoodContestSimple"));
                result.put("resultFoodContestEmoji", catchGitDashboardInfoResult.get(0).get("resultFoodContestEmoji"));
                result.put("resultFoodContestDetail", catchGitDashboardInfoResult.get(0).get("resultFoodContestDetail"));

                // 二转打折
                result.put("resultTransferDiscountSimple", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountSimple"));
                result.put("resultTransferDiscountEmoji", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountEmoji"));
                result.put("resultTransferDiscountDetail", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountDetail"));

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
