package com.careful.HyperFVM.utils.ForDashboard;

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
import java.util.Timer;
import java.util.TimerTask;

public class ExecuteDailyTask {

    // 整体超时时间：网络异常时兜底返回结果，避免调用方永远处于等待状态
    // 取45秒：XMLHelper中OkHttp的最坏超时为 连接10s + 读取15s，再留出重试与调度余量
    private static final long TASK_TIMEOUT_MS = 45_000L;

    private final GiftFetcher giftFetcher;
    private final ActivityCatcher activityCatcher;
    private final FertilizationTaskCatcher fertilizationTaskCatcher;
    private final NewYearCatcher newYearCatcher;
    private final DashboardGitCatcher dashboardGitCatcher;

    public ExecuteDailyTask() {
        giftFetcher = new GiftFetcher();
        activityCatcher = new ActivityCatcher();
        fertilizationTaskCatcher = new FertilizationTaskCatcher();
        newYearCatcher = new NewYearCatcher();
        dashboardGitCatcher = new DashboardGitCatcher();
    }

    public void executeGiftTileTask(GiftTileTaskResultCallBack callBack) {
        // 一次性回调保护：避免超时兜底与子任务回调同时触发导致重复回调
        final Object stateLock = new Object();
        final boolean[] hasReturned = {false};
        final Timer timer = new Timer();

        // 超时兜底：网络异常时也能返回结果，避免调用方永远等待
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean shouldReturn = false;
                synchronized (stateLock) {
                    if (!hasReturned[0]) {
                        hasReturned[0] = true;
                        shouldReturn = true;
                    }
                }
                if (shouldReturn) {
                    timer.cancel();
                    callBack.onResult("🎁温馨礼包：❌超时");
                }
            }
        }, TASK_TIMEOUT_MS);

        giftFetcher.fetchAndSaveGift(result -> {
            synchronized (stateLock) {
                if (hasReturned[0]) {
                    return;
                }
                hasReturned[0] = true;
            }
            timer.cancel();
            callBack.onResult("🎁温馨礼包：" + result.get("resultNotification"));
        });
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

        // 一次性回调保护：避免超时兜底与子任务回调同时触发导致重复回调
        final Object stateLock = new Object();
        final boolean[] hasReturned = {false};
        final Timer timer = new Timer();

        // 检查是否都完成，完成则返回最终结果（仅回调一次）
        Runnable checkAndSend = () -> {
            String result = null;
            synchronized (stateLock) {
                if (!hasReturned[0] && catchTodayActivityInfoDone[0] && catchFertilizationTaskInfoDone[0] && catchBountyInfoDone[0] && catchMillionConsumptionInfoDone[0] && catchLuckyConsumptionInfoDone[0] && catchGitDashboardInfoDone[0]) {
                    hasReturned[0] = true;
                    result = catchTodayActivityInfoResult[0] + "\n" +
                            catchFertilizationTaskInfoResult[0] + " " + catchBountyInfoResult[0] + "\n" +
                            catchGitDashboardInfoResult[0];
                }
            }
            if (result != null) {
                timer.cancel();
                callBack.onResult(result);
            }
        };

        // 超时兜底：将未完成的子任务填充为超时提示，再走统一的完成检查
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (stateLock) {
                    if (hasReturned[0]) {
                        return;
                    }

                    // 未完成的子任务填充超时提示并置位完成标志
                    if (!catchTodayActivityInfoDone[0]) {
                        catchTodayActivityInfoResult[0] = "💥双爆：❌超时";
                        catchTodayActivityInfoDone[0] = true;
                    }
                    if (!catchFertilizationTaskInfoDone[0]) {
                        catchFertilizationTaskInfoResult[0] = "🌳施肥：❌超时";
                        catchFertilizationTaskInfoDone[0] = true;
                    }
                    if (!catchBountyInfoDone[0]) {
                        catchBountyInfoResult[0] = "📜悬赏：❌超时";
                        catchBountyInfoDone[0] = true;
                    }
                    if (!catchGitDashboardInfoDone[0]) {
                        catchGitDashboardInfoResult[0] = "🏝️三岛：❌超时 🥟大赛：❌超时";
                        catchGitDashboardInfoDone[0] = true;
                    }
                    // 百万消费、抢红包任务不参与最终文本拼接，仅需置位完成标志
                    catchMillionConsumptionInfoDone[0] = true;
                    catchLuckyConsumptionInfoDone[0] = true;
                }

                // 所有子任务已判定完成，统一走完成检查
                checkAndSend.run();
            }
        }, TASK_TIMEOUT_MS);

        activityCatcher.catchTodayActivityInfo(result -> {
            synchronized (stateLock) {
                catchTodayActivityInfoResult[0] = "💥双爆：" + result.get("resultNotification");
                catchTodayActivityInfoDone[0] = true;
            }
            checkAndSend.run();
        });
        fertilizationTaskCatcher.catchFertilizationTaskInfo(result -> {
            synchronized (stateLock) {
                catchFertilizationTaskInfoResult[0] = "🌳施肥：" + result.get("resultNotification");
                catchFertilizationTaskInfoDone[0] = true;
            }
            checkAndSend.run();
        });
        newYearCatcher.catchBountyInfo(result -> {
            synchronized (stateLock) {
                catchBountyInfoResult[0] = "📜悬赏：" + result.get("resultNotification");
                catchBountyInfoDone[0] = true;
            }
            checkAndSend.run();
        });
        newYearCatcher.catchMillionConsumptionInfo(result -> {
            synchronized (stateLock) {
                catchMillionConsumptionInfoDone[0] = true;
            }
            checkAndSend.run();
        });
        newYearCatcher.catchLuckyConsumptionInfo(result -> {
            synchronized (stateLock) {
                catchLuckyConsumptionInfoDone[0] = true;
            }
            checkAndSend.run();
        });
        dashboardGitCatcher.catchGitDashboardInfo(result -> {
            synchronized (stateLock) {
                catchGitDashboardInfoResult[0] = "🏝️三岛：" + result.get("resultThreeIslandsSimple") + " 🥟大赛：" + result.get("resultFoodContestSimple");
                catchGitDashboardInfoDone[0] = true;
            }
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

        // 一次性回调保护：避免超时兜底与子任务回调同时触发导致重复回调
        final Object stateLock = new Object();
        final boolean[] hasReturned = {false};
        final Timer timer = new Timer();

        // 检查是否都完成，完成则返回最终结果（仅回调一次）
        Runnable checkAndReturn = () -> {
            boolean needReturn = false;
            synchronized (stateLock) {
                if (!hasReturned[0] && catchMeishiWechatInfoDone[0] && catchTodayActivityInfoDone[0] && catchFertilizationTaskInfoDone[0] && catchBountyInfoDone[0] && catchMillionConsumptionInfoDone[0] && catchLuckyConsumptionInfoDone[0] && catchGitDashboardInfoDone[0]) {
                    hasReturned[0] = true;
                    needReturn = true;
                }
            }
            if (needReturn) {
                Map<String, String> result = new HashMap<>();

                // 将所有子任务获取到的Map数据汇总成一个Map
                // 温馨礼包
                result.put("resultMeishiWechatInfoSimple", catchMeishiWechatInfoResult.get(0).get("resultSimple"));
                result.put("resultMeishiWechatInfoNotification", catchMeishiWechatInfoResult.get(0).get("resultNotification"));

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
                result.put("resultBountyInfoIsDouble", catchBountyInfoResult.get(0).get("resultIsDouble"));
                result.put("resultBountyInfoDayMax", catchBountyInfoResult.get(0).get("resultDayMax"));

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
                result.put("resultHappyHolidayCardList", catchGitDashboardInfoResult.get(0).get("resultHappyHolidayCardList"));

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
                result.put("resultThreeIslandsCardList", catchGitDashboardInfoResult.get(0).get("resultThreeIslandsCardList"));

                // 美食大赛
                result.put("resultFoodContestSimple", catchGitDashboardInfoResult.get(0).get("resultFoodContestSimple"));
                result.put("resultFoodContestEmoji", catchGitDashboardInfoResult.get(0).get("resultFoodContestEmoji"));
                result.put("resultFoodContestContentStatus", catchGitDashboardInfoResult.get(0).get("resultFoodContestContentStatus"));
                result.put("resultFoodContestContentDetail", catchGitDashboardInfoResult.get(0).get("resultFoodContestContentDetail"));
                result.put("resultFoodContestCardList", catchGitDashboardInfoResult.get(0).get("resultFoodContestCardList"));

                // 二转打折
                result.put("resultTransferDiscountSimple", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountSimple"));
                result.put("resultTransferDiscountCardNum", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountCardNum"));
                result.put("resultTransferDiscountEmoji", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountEmoji"));
                result.put("resultTransferDiscountContentStatus", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountContentStatus"));
                result.put("resultTransferDiscountContentDetail", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountContentDetail"));
                result.put("resultTransferDiscountCardList", catchGitDashboardInfoResult.get(0).get("resultTransferDiscountCardList"));

                // App通知
                result.put("resultGlobalNotificationIsShow", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationIsShow"));
                result.put("resultGlobalNotificationTitle", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationTitle"));
                result.put("resultGlobalNotificationContent", catchGitDashboardInfoResult.get(0).get("resultGlobalNotificationContent"));

                // 世界BOSS
                result.put("resultWorldBossIsShow", catchGitDashboardInfoResult.get(0).get("resultWorldBossIsShow"));
                result.put("resultWorldBossTitle", catchGitDashboardInfoResult.get(0).get("resultWorldBossTitle"));
                result.put("resultWorldBossContentDetail", catchGitDashboardInfoResult.get(0).get("resultWorldBossContentDetail"));
                result.put("resultWorldBossContentStatus", catchGitDashboardInfoResult.get(0).get("resultWorldBossContentStatus"));
                result.put("resultWorldBossStartDate", catchGitDashboardInfoResult.get(0).get("resultWorldBossStartDate"));
                result.put("resultWorldBossChallengeDate", catchGitDashboardInfoResult.get(0).get("resultWorldBossChallengeDate"));
                result.put("resultWorldBossSettlementDate", catchGitDashboardInfoResult.get(0).get("resultWorldBossSettlementDate"));
                result.put("resultWorldBossEndDate", catchGitDashboardInfoResult.get(0).get("resultWorldBossEndDate"));
                result.put("resultWorldBossUrlRule", catchGitDashboardInfoResult.get(0).get("resultWorldBossUrlRule"));
                result.put("resultWorldBossUrlReward", catchGitDashboardInfoResult.get(0).get("resultWorldBossUrlReward"));

                // 营地任务
                result.put("resultCampTaskSimple", catchGitDashboardInfoResult.get(0).get("resultCampTaskSimple"));
                result.put("resultCampTaskEmoji", catchGitDashboardInfoResult.get(0).get("resultCampTaskEmoji"));
                result.put("resultCampTaskContentStatus", catchGitDashboardInfoResult.get(0).get("resultCampTaskContentStatus"));
                result.put("resultCampTaskContentDetail", catchGitDashboardInfoResult.get(0).get("resultCampTaskContentDetail"));
                result.put("resultCampTaskUrl", catchGitDashboardInfoResult.get(0).get("resultCampTaskUrl"));

                // 豪华婚礼
                result.put("resultWeddingDiscountSimple", catchGitDashboardInfoResult.get(0).get("resultWeddingDiscountSimple"));
                result.put("resultWeddingDiscountEmoji", catchGitDashboardInfoResult.get(0).get("resultWeddingDiscountEmoji"));
                result.put("resultWeddingDiscountContentStatus", catchGitDashboardInfoResult.get(0).get("resultWeddingDiscountContentStatus"));
                result.put("resultWeddingDiscountContentDetail", catchGitDashboardInfoResult.get(0).get("resultWeddingDiscountContentDetail"));

                // 结晶打折
                result.put("resultCryStoneDiscountSimple", catchGitDashboardInfoResult.get(0).get("resultCryStoneDiscountSimple"));
                result.put("resultCryStoneDiscountEmoji", catchGitDashboardInfoResult.get(0).get("resultCryStoneDiscountEmoji"));
                result.put("resultCryStoneDiscountContentStatus", catchGitDashboardInfoResult.get(0).get("resultCryStoneDiscountContentStatus"));
                result.put("resultCryStoneDiscountContentDetail", catchGitDashboardInfoResult.get(0).get("resultCryStoneDiscountContentDetail"));

                // 福利打卡
                result.put("resultBirthdayActivitySimple", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivitySimple"));
                result.put("resultBirthdayActivityEmoji", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivityEmoji"));
                result.put("resultBirthdayActivityContentStatus", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivityContentStatus"));
                result.put("resultBirthdayActivityContentDetail", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivityContentDetail"));
                result.put("resultBirthdayActivityNewCardName", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivityNewCardName"));
                result.put("resultBirthdayActivityRequiredDatabaseVersion", catchGitDashboardInfoResult.get(0).get("resultBirthdayActivityRequiredDatabaseVersion"));

                timer.cancel();
                callBack.onResult(result);
            }
        };

        // 超时兜底：将未完成的子任务填充为超时兜底结果，再走统一的完成检查
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (stateLock) {
                    if (hasReturned[0]) {
                        return;
                    }

                    // 未完成的子任务填充超时兜底结果并置位完成标志
                    if (!catchMeishiWechatInfoDone[0]) {
                        catchMeishiWechatInfoResult.set(0, generateGiftTimeoutResult());
                        catchMeishiWechatInfoDone[0] = true;
                    }
                    if (!catchTodayActivityInfoDone[0]) {
                        catchTodayActivityInfoResult.set(0, generateCommonTimeoutResult());
                        catchTodayActivityInfoDone[0] = true;
                    }
                    if (!catchFertilizationTaskInfoDone[0]) {
                        catchFertilizationTaskInfoResult.set(0, generateCommonTimeoutResult());
                        catchFertilizationTaskInfoDone[0] = true;
                    }
                    if (!catchBountyInfoDone[0]) {
                        catchBountyInfoResult.set(0, generateCommonTimeoutResult());
                        catchBountyInfoDone[0] = true;
                    }
                    if (!catchMillionConsumptionInfoDone[0]) {
                        catchMillionConsumptionInfoResult.set(0, generateCommonTimeoutResult());
                        catchMillionConsumptionInfoDone[0] = true;
                    }
                    if (!catchLuckyConsumptionInfoDone[0]) {
                        catchLuckyConsumptionInfoResult.set(0, generateCommonTimeoutResult());
                        catchLuckyConsumptionInfoDone[0] = true;
                    }
                    if (!catchGitDashboardInfoDone[0]) {
                        catchGitDashboardInfoResult.set(0, DashboardGitCatcher.buildFailResult());
                        catchGitDashboardInfoDone[0] = true;
                    }
                }

                // 所有子任务已判定完成，统一走完成检查
                checkAndReturn.run();
            }
        }, TASK_TIMEOUT_MS);

        giftFetcher.fetchAndSaveGift(result -> {
            synchronized (stateLock) {
                catchMeishiWechatInfoResult.set(0, result);
                catchMeishiWechatInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        activityCatcher.catchTodayActivityInfo(result -> {
            synchronized (stateLock) {
                catchTodayActivityInfoResult.set(0, result);
                catchTodayActivityInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        fertilizationTaskCatcher.catchFertilizationTaskInfo(result -> {
            synchronized (stateLock) {
                catchFertilizationTaskInfoResult.set(0, result);
                catchFertilizationTaskInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        newYearCatcher.catchBountyInfo(result -> {
            synchronized (stateLock) {
                catchBountyInfoResult.set(0, result);
                catchBountyInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        newYearCatcher.catchMillionConsumptionInfo(result -> {
            synchronized (stateLock) {
                catchMillionConsumptionInfoResult.set(0, result);
                catchMillionConsumptionInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        newYearCatcher.catchLuckyConsumptionInfo(result -> {
            synchronized (stateLock) {
                catchLuckyConsumptionInfoResult.set(0, result);
                catchLuckyConsumptionInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
        dashboardGitCatcher.catchGitDashboardInfo(result -> {
            synchronized (stateLock) {
                catchGitDashboardInfoResult.set(0, result);
                catchGitDashboardInfoDone[0] = true;
            }
            checkAndReturn.run();
        });
    }

    /**
     * 构建温馨礼包超时兜底结果（仅含礼包任务输出的key）
     */
    private Map<String, String> generateGiftTimeoutResult() {
        Map<String, String> result = new HashMap<>();
        result.put("resultSimple", "领取异常");
        result.put("resultNotification", "❌网络");
        return result;
    }

    /**
     * 构建常规子任务超时兜底结果（双爆/施肥/悬赏/百万消费/抢红包通用，含5个key）
     */
    private Map<String, String> generateCommonTimeoutResult() {
        Map<String, String> result = new HashMap<>();
        result.put("resultSimple", "网络异常");
        result.put("resultNotification", "❌网络");
        result.put("resultEmoji", "❌");
        result.put("resultContentStatus", "出错了呢");
        result.put("resultContentDetail", "请求超时\n请检查网络后重试");
        return result;
    }
}
