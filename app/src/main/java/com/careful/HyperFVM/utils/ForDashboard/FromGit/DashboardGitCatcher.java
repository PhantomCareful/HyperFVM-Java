package com.careful.HyperFVM.utils.ForDashboard.FromGit;

import android.util.Log;

import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 从我们自己的仓库获取部分无法直接在游戏内获取或者获取起来比较麻烦的内容
 * 目前已支持：
 * 1. 日氪
 * 2. 欢乐假期
 * 3. 助人为乐
 * 4. 三岛
 * 5. 美食大赛
 * 6. 二转打折
 * 7. App置顶公告
 * 8. 世界BOSS
 */
public class DashboardGitCatcher {
    private static final String TAG = "DashboardGitCatcher";
    private static final String GIT_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/dashboard.m3u";

    // 这个Map用于及时将获取到的内容显示出来，而不从数据库获取
    private Map<String, String> result;

    public void catchGitDashboardInfo(DashboardGitCatchResultCallBack callBack) {
        new Thread(() -> {
            try {
                // 第0步：初始化Map
                result = new HashMap<>();

                // 第1步：先从给定链接获取JSON字符串
                String JSONArrayStr = XMLHelper.getContentFromUrl(GIT_URL);

                // 第2步：将JSON字符串转换成JSON数组
                JSONArray jsonArray = new JSONArray(JSONArrayStr);

                JSONObject itemObj;

                // 第3步：遍历数组中的每个JSON对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    // 提取单个JSON对象
                    itemObj = jsonArray.getJSONObject(i);

                    // 把二转打折的内容放在最后一个位置，方便处理
                    if (i == jsonArray.length() - 1) {
                        catchTransferDiscountInfo(itemObj);
                    } else {
                        catchOtherActivityInfo(itemObj);
                    }
                }

                // 第4步，将Map回调出去
                callBack.onResult(result);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "捕获异常：" + e.getMessage());
            }
        }).start();
    }

    /**
     * 获取二转打折的内容
     * 示例内容
     * "name": "二转打折",
     * "startDate": "2025-01-08",
     * "endDate": "2026-02-05",
     * "cardList": "肥牛火锅|烤蜥蜴投手|炭烧海星|章鱼烧|金牛烟花|玉兔灯笼|蜂蜜史莱姆|糖葫芦炮弹|糖炒栗子|冰激凌|可乐炸弹|青涩柿柿|火龙果"
     */
    private void catchTransferDiscountInfo(JSONObject itemObj) throws JSONException {
        String simple;
        String emoji;
        String contentStatus;
        String contentDetail;

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardList = itemObj.getString("cardList");
        String[] cardListArr = cardList.split("\\|");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "二转打折：活动尚未开始");
            simple = "尚未开始";
            emoji = "⏳";
            contentStatus = "等等等等";
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n以下卡片保险金仅需1500D\n\n";
        } else if (today.after(end)) {
            Log.d(TAG, "二转打折：活动已结束");
            simple = "暂无";
            emoji = "⏳";
            contentStatus = "空空如也";
            contentDetail = "众所周知打折活动不会间断，因此大概率是作者还没更新，建议去速速催促\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21";
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10) {
                Log.d(TAG, "二转打折：活动已结束");
                simple = "暂无";
                emoji = "⏳";
                contentStatus = "空空如也";
                contentDetail = "众所周知打折活动不会间断，因此大概率是作者还没更新，建议去速速催促\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21";
            } else {
                Log.d(TAG, "二转打折：活动正在进行中");
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;

                simple = cardListArr.length + "张";
                emoji = "😍";
                contentStatus = "第" + duringCount + "天/持续" + length + "天";
                contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n以下卡片保险金仅需1500D";
            }
        }

        Log.d("resultTransferDiscountContentStatus", "resultTransferDiscountContentStatus = " + contentStatus);
        result.put("resultTransferDiscountSimple", simple);
        result.put("resultTransferDiscountEmoji", emoji);
        result.put("resultTransferDiscountContentStatus", contentStatus);
        result.put("resultTransferDiscountContentDetail", contentDetail);
        result.put("resultTransferDiscountCardList", cardList);
    }

    /**
     * 获取三岛的内容
     * 示例内容
     * "name": "三岛",
     * "startDate": "2026-06-11",
     * "endDate": "2026-07-02",
     * "cardList": "猪猪猎手|弩箭牛|双枪喵|壮壮牛"
     */
    private void catchThreeIslandsInfo(JSONObject itemObj) throws JSONException {
        String simple;
        String emoji;
        String contentStatus;
        String contentDetail;

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardList = itemObj.getString("cardList");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "三岛福利：活动尚未开始");
            simple = "尚未开始";
            emoji = "⏳";
            contentStatus = "等等等等";
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次返场这些卡片";
        } else if (today.after(end)) {
            Log.d(TAG, "三岛福利：活动已结束");
            simple = "暂无";
            emoji = "⏳";
            contentStatus = "三岛福利";
            contentDetail = "趁现在多攒点徽章吧";
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10) {
                Log.d(TAG, "三岛福利：活动已结束");
                simple = "暂无";
                emoji = "⏳";
                contentStatus = "空空如也";
                contentDetail = "趁现在多攒点徽章吧";
            } else {
                Log.d(TAG, "美食大赛：活动正在进行中");
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;

                simple = duringCount + "/" + length;
                emoji = "✊";
                contentStatus = "第" + duringCount + "天/持续" + length + "天";
                contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次返场这些卡片";
            }
        }

        result.put("resultThreeIslandsSimple", simple);
        result.put("resultThreeIslandsEmoji", emoji);
        result.put("resultThreeIslandsContentStatus", contentStatus);
        result.put("resultThreeIslandsContentDetail", contentDetail);
        result.put("resultThreeIslandsCardList", cardList);
    }

    /**
     * 获取美食大赛的内容
     * 示例内容
     * "name": "美食大赛",
     * "startDate": "2026-06-04",
     * "endDate": "2026-06-30",
     * "cardList": "泡泡鸡尾酒|蜜糖陷阱|尖叫马咖|美味水果塔|13周年时光机|美味计时器|五谷丰登|黯然销魂饭"
     */
    private void catchFoodContestInfo(JSONObject itemObj) throws JSONException {
        String simple;
        String emoji;
        String contentStatus;
        String contentDetail;

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardList = itemObj.getString("cardList");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "美食大赛：活动尚未开始");
            simple = "尚未开始";
            emoji = "⏳";
            contentStatus = "等等等等";
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次返场这些卡片";
        } else if (today.after(end)) {
            Log.d(TAG, "美食大赛：活动已结束");
            simple = "暂无";
            emoji = "⏳";
            contentStatus = "空空如也";
            contentDetail = "休息，休息一下~";
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10) {
                Log.d(TAG, "美食大赛：活动已结束");
                simple = "暂无";
                emoji = "⏳";
                contentStatus = "空空如也";
                contentDetail = "休息，休息一下~";
            } else {
                Log.d(TAG, "美食大赛：活动正在进行中");
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;

                simple = duringCount + "/" + length;
                emoji = "✊";
                contentStatus = "第" + duringCount + "天/持续" + length + "天";
                contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate;
                if (length < 29) {
                    contentDetail = contentDetail + "\n\n⚠️请注意⚠️\n大赛结束时间要早于第4周周四";
                }
                contentDetail = contentDetail + "\n\n本次返场这些卡片";
            }
        }

        result.put("resultFoodContestSimple", simple);
        result.put("resultFoodContestEmoji", emoji);
        result.put("resultFoodContestContentStatus", contentStatus);
        result.put("resultFoodContestContentDetail", contentDetail);
        result.put("resultFoodContestCardList", cardList);
    }

    /**
     * 获取欢乐假期的内容
     * 示例内容
     * "name": "欢乐假期",
     * "startDate": "2026-06-18",
     * "endDate": "2026-07-09",
     * "cardList": "泡泡鸡尾酒|蜜糖陷阱|尖叫马咖|街头烤肉大师|13周年时光机|美味计时器|烛阴龙"
     */
    private void catchHappyHolidayInfo(JSONObject itemObj) throws JSONException {
        String simple;
        String emoji;
        String contentStatus;
        String contentDetail;

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardList = itemObj.getString("cardList");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "欢乐假期：活动尚未开始");
            simple = "尚未开始";
            emoji = "⏳";
            contentStatus = "等等等等";
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次返场这些卡片";
        } else if (today.after(end)) {
            Log.d(TAG, "欢乐假期：活动已结束");
            simple = "暂无";
            emoji = "⏳";
            contentStatus = "空空如也";
            contentDetail = "趁现在多攒点假期票吧";
        } else {
            Log.d(TAG, "欢乐假期：活动正在进行中");
            int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
            int length = TimeUtil.calculateDaysBetween(startDate, endDate);

            simple = duringCount + "/" + length;
            emoji = "✊";
            contentStatus = "第" + duringCount + "天/持续" + length + "天";
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次返场这些卡片";
        }

        result.put("resultHappyHolidaySimple", simple);
        result.put("resultHappyHolidayEmoji", emoji);
        result.put("resultHappyHolidayContentStatus", contentStatus);
        result.put("resultHappyHolidayContentDetail", contentDetail);
        result.put("resultHappyHolidayCardList", cardList);
    }

    /**
     * 获取其他活动内容
     * 示例内容
     * {
     * "name": "日氪",
     * "startDate": "2026-01-30",
     * "endDate": "2026-02-05"
     * },
     * {
     * "name": "助人为乐",
     * "startDate": "2026-01-08",
     * "endDate": "2026-02-26"
     * },
     * {
     * "name": "App通知",
     * "startDate": "2026-06-13",
     * "endDate": "2026-06-30",
     * "title": "重要通知",
     * "content": ""
     * },
     * {
     * "name": "世界BOSS",
     * "startDate": "2026-06-18",
     * "challengeDate": "2026-06-19",
     * "settlementDate": "2026-07-03",
     * "endDate": "2026-07-09",
     * "title": "巅峰对决S11",
     * "content": "本赛季出场BOSS：阿斯莫德、玛门、路西法"
     * },
     * {
     * "name": "营地任务",
     * "startDate": "2026-04-16",
     * "endDate": "2026-07-30",
     * "urlTask": "<a href="https://www.kdocs.cn/l/cjEHQ7XO5T26">...</a>"
     * },
     * {
     * "name": "豪华婚礼",
     * "startDate": "2026-06-18",
     * "endDate": "2026-07-09"
     * },
     * {
     * "name": "结晶打折",
     * "startDate": "2026-06-25",
     * "endDate": "2026-08-27"
     * },
     */
    private void catchOtherActivityInfo(JSONObject itemObj) throws JSONException {
        String resultSimple;
        String resultEmoji;
        String resultContentStatus;
        String resultContentDetail;

        String name = itemObj.getString("name");

        // 有些活动写了单独的获取方法的（主要是涉及cardList，单独写一下更方便），这里先判断一下
        switch (name) {
            case "欢乐假期":
                catchHappyHolidayInfo(itemObj);
                return;
            case "三岛":
                catchThreeIslandsInfo(itemObj);
                return;
            case "美食大赛":
                catchFoodContestInfo(itemObj);
                return;
        }

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);

        if (today.before(start)) {
            Log.d(TAG, name + ": 活动尚未开始");
            resultSimple = "尚未开始";
            resultEmoji = "⏳";
            resultContentStatus = "等等等等";
            resultContentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n活动还没开始呢";
            switch (name) {
                case "日氪":
                    result.put("resultDailyRechargeSimple", resultSimple);
                    result.put("resultDailyRechargeEmoji", resultEmoji);
                    result.put("resultDailyRechargeContentStatus", resultContentStatus);
                    result.put("resultDailyRechargeContentDetail", resultContentDetail);
                    break;
                case "助人为乐":
                    result.put("resultServerTeamUpSimple", resultSimple);
                    result.put("resultServerTeamUpEmoji", resultEmoji);
                    result.put("resultServerTeamUpContentStatus", resultContentStatus);
                    result.put("resultServerTeamUpContentDetail", resultContentDetail);
                    break;
                case "App通知":
                    result.put("resultGlobalNotificationIsShow", "false");
                    result.put("resultGlobalNotificationTitle", itemObj.getString("title"));
                    result.put("resultGlobalNotificationContent", itemObj.getString("content"));
                    break;
                case "世界BOSS":
                    result.put("resultWorldBossIsShow", "true");
                    result.put("resultWorldBossTitle", itemObj.getString("title"));
                    result.put("resultWorldBossContentDetail", itemObj.getString("content"));
                    result.put("resultWorldBossContentStatus", "新赛季即将开始，请做好准备✊");
                    result.put("resultWorldBossStartDate", startDate);
                    result.put("resultWorldBossChallengeDate", itemObj.getString("challengeDate"));
                    result.put("resultWorldBossSettlementDate", itemObj.getString("settlementDate"));
                    result.put("resultWorldBossEndDate", endDate);
                    result.put("resultWorldBossUrlRule", itemObj.getString("urlRule"));
                    result.put("resultWorldBossUrlReward", itemObj.getString("urlReward"));
                    break;
                case "营地任务":
                    result.put("resultCampTaskSimple", resultSimple);
                    result.put("resultCampTaskEmoji", resultEmoji);
                    result.put("resultCampTaskContentStatus", resultContentStatus);
                    result.put("resultCampTaskContentDetail", resultContentDetail);
                    result.put("resultCampTaskUrl", itemObj.getString("urlTask"));
                    break;
                case "豪华婚礼":
                    result.put("resultWeddingDiscountSimple", resultSimple);
                    result.put("resultWeddingDiscountEmoji", resultEmoji);
                    result.put("resultWeddingDiscountContentStatus", resultContentStatus);
                    result.put("resultWeddingDiscountContentDetail", resultContentDetail);
                    break;
                case "结晶打折":
                    result.put("resultCryStoneDiscountSimple", resultSimple);
                    result.put("resultCryStoneDiscountEmoji", resultEmoji);
                    result.put("resultCryStoneDiscountContentStatus", resultContentStatus);
                    result.put("resultCryStoneDiscountContentDetail", resultContentDetail + "\n\n在此期间\n10级以上结晶强化保险金5折");
                    break;
            }
        } else if (today.after(end)) {
            Log.d(TAG, name + ": 活动已结束");
            resultSimple = "暂无";
            resultEmoji = "⏳";
            resultContentStatus = "空空如也";
            resultContentDetail = "还没有新的活动呢";
            switch (name) {
                case "日氪":
                    result.put("resultDailyRechargeSimple", resultSimple);
                    result.put("resultDailyRechargeEmoji", resultEmoji);
                    result.put("resultDailyRechargeContentStatus", resultContentStatus);
                    result.put("resultDailyRechargeContentDetail", resultContentDetail);
                    break;
                case "助人为乐":
                    result.put("resultServerTeamUpSimple", resultSimple);
                    result.put("resultServerTeamUpEmoji", resultEmoji);
                    result.put("resultServerTeamUpContentStatus", resultContentStatus);
                    result.put("resultServerTeamUpContentDetail", resultContentDetail);
                    break;
                case "App通知":
                    result.put("resultGlobalNotificationIsShow", "false");
                    result.put("resultGlobalNotificationTitle", itemObj.getString("title"));
                    result.put("resultGlobalNotificationContent", itemObj.getString("content"));
                    break;
                case "世界BOSS":
                    result.put("resultWorldBossIsShow", "false");
                    result.put("resultWorldBossTitle", itemObj.getString("title"));
                    result.put("resultWorldBossContentDetail", itemObj.getString("content"));
                    result.put("resultWorldBossContentStatus", "赛季已结束，快去领取奖励吧🎉");
                    result.put("resultWorldBossStartDate", startDate);
                    result.put("resultWorldBossChallengeDate", itemObj.getString("challengeDate"));
                    result.put("resultWorldBossSettlementDate", itemObj.getString("settlementDate"));
                    result.put("resultWorldBossEndDate", endDate);
                    result.put("resultWorldBossUrlRule", itemObj.getString("urlRule"));
                    result.put("resultWorldBossUrlReward", itemObj.getString("urlReward"));
                    break;
                case "营地任务":
                    result.put("resultCampTaskSimple", resultSimple);
                    result.put("resultCampTaskEmoji", resultEmoji);
                    result.put("resultCampTaskContentStatus", resultContentStatus);
                    result.put("resultCampTaskContentDetail", resultContentDetail);
                    result.put("resultCampTaskUrl", itemObj.getString("urlTask"));
                    break;
                case "豪华婚礼":
                    result.put("resultWeddingDiscountSimple", resultSimple);
                    result.put("resultWeddingDiscountEmoji", resultEmoji);
                    result.put("resultWeddingDiscountContentStatus", resultContentStatus);
                    result.put("resultWeddingDiscountContentDetail", resultContentDetail);
                    break;
                case "结晶打折":
                    result.put("resultCryStoneDiscountSimple", resultSimple);
                    result.put("resultCryStoneDiscountEmoji", resultEmoji);
                    result.put("resultCryStoneDiscountContentStatus", resultContentStatus);
                    result.put("resultCryStoneDiscountContentDetail", resultContentDetail);
                    break;
            }
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10 && !name.equals("日氪") && !name.equals("App通知") && !name.equals("世界BOSS")) {
                Log.d(TAG, name + ": 活动已结束");
                resultSimple = "暂无";
                resultEmoji = "⏳";
                resultContentStatus = "空空如也";
                resultContentDetail = "还没有新的活动呢";
                switch (name) {
                    case "助人为乐":
                        result.put("resultServerTeamUpSimple", resultSimple);
                        result.put("resultServerTeamUpEmoji", resultEmoji);
                        result.put("resultServerTeamUpContentStatus", resultContentStatus);
                        result.put("resultServerTeamUpContentDetail", resultContentDetail);
                        break;
                    case "营地任务":
                        result.put("resultCampTaskSimple", resultSimple);
                        result.put("resultCampTaskEmoji", resultEmoji);
                        result.put("resultCampTaskContentStatus", resultContentStatus);
                        result.put("resultCampTaskContentDetail", resultContentDetail);
                        result.put("resultCampTaskUrl", itemObj.getString("urlTask"));
                        break;
                    case "豪华婚礼":
                        result.put("resultWeddingDiscountSimple", resultSimple);
                        result.put("resultWeddingDiscountEmoji", resultEmoji);
                        result.put("resultWeddingDiscountContentStatus", resultContentStatus);
                        result.put("resultWeddingDiscountContentDetail", resultContentDetail);
                        break;
                    case "结晶打折":
                        result.put("resultCryStoneDiscountSimple", resultSimple);
                        result.put("resultCryStoneDiscountEmoji", resultEmoji);
                        result.put("resultCryStoneDiscountContentStatus", resultContentStatus);
                        result.put("resultCryStoneDiscountContentDetail", resultContentDetail);
                        break;
                }
            } else {
                Log.d(TAG, name + ": 活动正在进行中");
                // 需要计算活动持续多少天
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate);

                resultEmoji = "✊";
                resultContentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate;

                switch (name) {
                    case "日氪":
                        resultSimple = duringCount * 8 + "/" + length * 8;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天\n\n日氪道具：" + duringCount * 8 + "/" + length * 8 + "\n金钥匙：" + duringCount * 4 + "/" + length * 4;

                        result.put("resultDailyRechargeSimple", resultSimple);
                        result.put("resultDailyRechargeEmoji", resultEmoji);
                        result.put("resultDailyRechargeContentStatus", resultContentStatus);
                        result.put("resultDailyRechargeContentDetail", resultContentDetail);
                        break;
                    case "助人为乐":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultServerTeamUpSimple", resultSimple);
                        result.put("resultServerTeamUpEmoji", resultEmoji);
                        result.put("resultServerTeamUpContentStatus", resultContentStatus);
                        result.put("resultServerTeamUpContentDetail", resultContentDetail);
                        break;
                    case "App通知":
                        result.put("resultGlobalNotificationIsShow", "true");
                        result.put("resultGlobalNotificationTitle", itemObj.getString("title"));
                        result.put("resultGlobalNotificationContent", itemObj.getString("content"));
                        break;
                    case "世界BOSS":
                        result.put("resultWorldBossIsShow", "true");
                        result.put("resultWorldBossTitle", itemObj.getString("title"));
                        result.put("resultWorldBossContentDetail", itemObj.getString("content"));
                        result.put("resultWorldBossContentStatus", "");
                        result.put("resultWorldBossStartDate", startDate);
                        result.put("resultWorldBossChallengeDate", itemObj.getString("challengeDate"));
                        result.put("resultWorldBossSettlementDate", itemObj.getString("settlementDate"));
                        result.put("resultWorldBossEndDate", endDate);
                        result.put("resultWorldBossUrlRule", itemObj.getString("urlRule"));
                        result.put("resultWorldBossUrlReward", itemObj.getString("urlReward"));
                        break;
                    case "营地任务":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultCampTaskSimple", resultSimple);
                        result.put("resultCampTaskEmoji", resultEmoji);
                        result.put("resultCampTaskContentStatus", resultContentStatus);
                        result.put("resultCampTaskContentDetail", resultContentDetail);
                        result.put("resultCampTaskUrl", itemObj.getString("urlTask"));
                        break;
                    case "豪华婚礼":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultWeddingDiscountSimple", resultSimple);
                        result.put("resultWeddingDiscountEmoji", "💍💍");
                        result.put("resultWeddingDiscountContentStatus", resultContentStatus);
                        result.put("resultWeddingDiscountContentDetail", resultContentDetail);
                        break;
                    case "结晶打折":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultCryStoneDiscountSimple", resultSimple);
                        result.put("resultCryStoneDiscountEmoji", "😍");
                        result.put("resultCryStoneDiscountContentStatus", resultContentStatus);
                        result.put("resultCryStoneDiscountContentDetail", resultContentDetail + "\n\n在此期间\n10级以上结晶强化保险金打5折");

                        break;
                }
            }
        }
    }
}
