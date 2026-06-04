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

                    // 默认把二转打折的内容放在最后一个位置，方便处理
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
        String resultTransferDiscountSimple;
        String resultTransferDiscountEmoji;
        String resultTransferDiscountContentStatus;
        String resultTransferDiscountContentDetail;

        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardListResult = itemObj.getString("cardList");
        String[] cardListArr = cardListResult.split("\\|");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "二转打折：活动尚未开始");
            resultTransferDiscountSimple = "尚未开始";
            resultTransferDiscountEmoji = "⏳";
            resultTransferDiscountContentStatus = "等等等等";
            resultTransferDiscountContentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n以下卡片保险金仅需1500D\n\n" + cardListResult;
        } else if (today.after(end)) {
            Log.d(TAG, "二转打折：活动已结束");
            resultTransferDiscountSimple = "暂无";
            resultTransferDiscountEmoji = "⏳";
            resultTransferDiscountContentStatus = "空空如也";
            resultTransferDiscountContentDetail = "众所周知打折活动不会间断，因此大概率是作者还没更新，建议去速速催促\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21";
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10) {
                Log.d(TAG, "二转打折：活动已结束");
                resultTransferDiscountSimple = "暂无";
                resultTransferDiscountEmoji = "⏳";
                resultTransferDiscountContentStatus = "空空如也";
                resultTransferDiscountContentDetail = "众所周知打折活动不会间断，因此大概率是作者还没更新，建议去速速催促\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21";
            } else {
                Log.d(TAG, "二转打折：活动正在进行中");
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;

                resultTransferDiscountSimple = cardListArr.length + "张";
                resultTransferDiscountEmoji = "😍";
                resultTransferDiscountContentStatus = "第" + duringCount + "天/持续" + length + "天";
                resultTransferDiscountContentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n以下卡片保险金仅需1500D\n\n" + cardListResult;
            }
        }

        Log.d("resultTransferDiscountContentStatus", "resultTransferDiscountContentStatus = " + resultTransferDiscountContentStatus);
        result.put("resultTransferDiscountSimple", resultTransferDiscountSimple);
        result.put("resultTransferDiscountEmoji", resultTransferDiscountEmoji);
        result.put("resultTransferDiscountContentStatus", resultTransferDiscountContentStatus);
        result.put("resultTransferDiscountContentDetail", resultTransferDiscountContentDetail);
    }

    /**
     * 获取其他活动内容
     * 示例内容
     *   {
     *     "name": "日氪",
     *     "startDate": "2026-01-30",
     *     "endDate": "2026-02-05"
     *   },
     *   {
     *     "name": "欢乐假期",
     *     "startDate": "2025-12-25",
     *     "endDate": "2026-01-22"
     *   },
     *   {
     *     "name": "助人为乐",
     *     "startDate": "2026-01-08",
     *     "endDate": "2026-02-26"
     *   },
     *   {
     *     "name": "三岛",
     *     "startDate": "2025-12-25",
     *     "endDate": "2026-01-22"
     *   },
     *   {
     *     "name": "美食大赛",
     *     "startDate": "2025-12-25",
     *     "endDate": "2026-01-29"
     *   }
     */
    private void catchOtherActivityInfo(JSONObject itemObj) throws JSONException {
        String resultSimple;
        String resultEmoji;
        String resultContentStatus;
        String resultContentDetail;

        String name = itemObj.getString("name");
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
                case "欢乐假期":
                    result.put("resultHappyHolidaySimple", resultSimple);
                    result.put("resultHappyHolidayEmoji", resultEmoji);
                    result.put("resultHappyHolidayContentStatus", resultContentStatus);
                    result.put("resultHappyHolidayContentDetail", resultContentDetail);
                    break;
                case "助人为乐":
                    result.put("resultServerTeamUpSimple", resultSimple);
                    result.put("resultServerTeamUpEmoji", resultEmoji);
                    result.put("resultServerTeamUpContentStatus", resultContentStatus);
                    result.put("resultServerTeamUpContentDetail", resultContentDetail);
                    break;
                case "三岛":
                    result.put("resultThreeIslandsSimple", resultSimple);
                    result.put("resultThreeIslandsEmoji", resultEmoji);
                    result.put("resultThreeIslandsContentStatus", resultContentStatus);
                    result.put("resultThreeIslandsContentDetail", resultContentDetail);
                    break;
                case "美食大赛":
                    result.put("resultFoodContestSimple", resultSimple);
                    result.put("resultFoodContestEmoji", resultEmoji);
                    result.put("resultFoodContestContentStatus", resultContentStatus);
                    result.put("resultFoodContestContentDetail", resultContentDetail);
                    break;
                case "App通知":
                    String title = itemObj.getString("title");
                    String content = itemObj.getString("content");
                    Log.d(TAG, "title = " + title + "\ncontent = " + content);

                    result.put("resultGlobalNotificationIsShow", "false");
                    result.put("resultGlobalNotificationTitle", title);
                    result.put("resultGlobalNotificationContent", content);
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
                case "欢乐假期":
                    result.put("resultHappyHolidaySimple", resultSimple);
                    result.put("resultHappyHolidayEmoji", resultEmoji);
                    result.put("resultHappyHolidayContentStatus", resultContentStatus);
                    result.put("resultHappyHolidayContentDetail", resultContentDetail);
                    break;
                case "助人为乐":
                    result.put("resultServerTeamUpSimple", resultSimple);
                    result.put("resultServerTeamUpEmoji", resultEmoji);
                    result.put("resultServerTeamUpContentStatus", resultContentStatus);
                    result.put("resultServerTeamUpContentDetail", resultContentDetail);
                    break;
                case "三岛":
                    result.put("resultThreeIslandsSimple", resultSimple);
                    result.put("resultThreeIslandsEmoji", resultEmoji);
                    result.put("resultThreeIslandsContentStatus", resultContentStatus);
                    result.put("resultThreeIslandsContentDetail", resultContentDetail);
                    break;
                case "美食大赛":
                    result.put("resultFoodContestSimple", resultSimple);
                    result.put("resultFoodContestEmoji", resultEmoji);
                    result.put("resultFoodContestContentStatus", resultContentStatus);
                    result.put("resultFoodContestContentDetail", resultContentDetail);
                    break;
                case "App通知":
                    String title = itemObj.getString("title");
                    String content = itemObj.getString("content");
                    Log.d(TAG, "title = " + title + "\ncontent = " + content);

                    result.put("resultGlobalNotificationIsShow", "false");
                    result.put("resultGlobalNotificationTitle", title);
                    result.put("resultGlobalNotificationContent", content);
            }
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            // 日氪和假期除外
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10 && !name.equals("日氪") && !name.equals("欢乐假期") && !name.equals("App通知")) {
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
                    case "三岛":
                        result.put("resultThreeIslandsSimple", resultSimple);
                        result.put("resultThreeIslandsEmoji", resultEmoji);
                        result.put("resultThreeIslandsContentStatus", resultContentStatus);
                        result.put("resultThreeIslandsContentDetail", resultContentDetail);
                        break;
                    case "美食大赛":
                        result.put("resultFoodContestSimple", resultSimple);
                        result.put("resultFoodContestEmoji", resultEmoji);
                        result.put("resultFoodContestContentStatus", resultContentStatus);
                        result.put("resultFoodContestContentDetail", resultContentDetail);
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
                    case "欢乐假期":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultHappyHolidaySimple", resultSimple);
                        result.put("resultHappyHolidayEmoji", resultEmoji);
                        result.put("resultHappyHolidayContentStatus", resultContentStatus);
                        result.put("resultHappyHolidayContentDetail", resultContentDetail);
                        break;
                    case "助人为乐":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultServerTeamUpSimple", resultSimple);
                        result.put("resultServerTeamUpEmoji", resultEmoji);
                        result.put("resultServerTeamUpContentStatus", resultContentStatus);
                        result.put("resultServerTeamUpContentDetail", resultContentDetail);
                        break;
                    case "三岛":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";

                        result.put("resultThreeIslandsSimple", resultSimple);
                        result.put("resultThreeIslandsEmoji", resultEmoji);
                        result.put("resultThreeIslandsContentStatus", resultContentStatus);
                        result.put("resultThreeIslandsContentDetail", resultContentDetail);
                        break;
                    case "美食大赛":
                        resultSimple = duringCount + "/" + length;
                        resultContentStatus = "第" + duringCount + "天/持续" + length + "天";
                        if (length < 29) {
                            resultContentDetail = resultContentDetail + "\n\n⚠️请注意⚠️\n大赛结束时间要早于第4周周四";
                        }

                        result.put("resultFoodContestSimple", resultSimple);
                        result.put("resultFoodContestEmoji", resultEmoji);
                        result.put("resultFoodContestContentStatus", resultContentStatus);
                        result.put("resultFoodContestContentDetail", resultContentDetail);
                        break;
                    case "App通知":
                        String title = itemObj.getString("title");
                        String content = itemObj.getString("content");
                        Log.d(TAG, "title = " + title + "\ncontent = " + content);

                        result.put("resultGlobalNotificationIsShow", "true");
                        result.put("resultGlobalNotificationTitle", title);
                        result.put("resultGlobalNotificationContent", content);
                }
            }
        }
    }
}
