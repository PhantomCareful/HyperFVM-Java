package com.careful.HyperFVM.utils.ForDashboard.FromGit;

import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

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

    private final DBHelper dbHelper;

    public DashboardGitCatcher(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void catchGitDashboardInfo() {
        new Thread(() -> {
            try {
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
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
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
        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");
        String cardList = itemObj.getString("cardList");
        String[] cardListArr = cardList.split("\\|");
        StringBuilder cardListResult = new StringBuilder();
        for (int j = 0; j < cardListArr.length; j++) {
            cardListResult.append(cardListArr[j]);
            if (j != cardListArr.length - 1) {
                cardListResult.append("\n");
            }
        }

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);
        if (today.before(start)) {
            Log.d(TAG, "二转打折：活动尚未开始");
            String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n活动还没开始呢\n\n以下卡片\n二转保险金仅需1500D\n\n" + cardListResult;
            dbHelper.updateDashboardContent("transfer_discount", "尚未开始");
            dbHelper.updateDashboardContent("transfer_discount_emoji", "⏳");
            dbHelper.updateDashboardContent("transfer_discount_detail", contentDetail);
        } else if (today.after(end)) {
            Log.d(TAG, "二转打折：活动已结束");
            String contentDetail = "还没有新的打折活动呢";
            dbHelper.updateDashboardContent("transfer_discount", "暂无");
            dbHelper.updateDashboardContent("transfer_discount_emoji", "⏳");
            dbHelper.updateDashboardContent("transfer_discount_detail", contentDetail);
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10) {
                Log.d(TAG, "二转打折：活动已结束");
                String contentDetail = "还没有新的打折活动呢";
                dbHelper.updateDashboardContent("transfer_discount", "暂无");
                dbHelper.updateDashboardContent("transfer_discount_emoji", "⏳");
                dbHelper.updateDashboardContent("transfer_discount_detail", contentDetail);
            } else {
                Log.d(TAG, "二转打折：活动正在进行中");
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate +
                        "\n\n本次打折一共持续" + length + "天\n今天是第" + duringCount + "天" +
                        "\n\n以下卡片\n二转保险金仅需1500D\n\n" + cardListResult;
                dbHelper.updateDashboardContent("transfer_discount", cardListArr.length + "张");
                dbHelper.updateDashboardContent("transfer_discount_emoji", "\uD83E\uDD29");
                dbHelper.updateDashboardContent("transfer_discount_detail", contentDetail);
            }
        }
    }

    /**
     * 获取其他活动内容
     * 示例内容
     * {
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
        String name = itemObj.getString("name");
        String startDate = itemObj.getString("startDate");
        String endDate = itemObj.getString("endDate");

        // 判断today和start、end之间的前后关系
        String todayDate = TimeUtil.getCurrentDate();
        Date today = TimeUtil.transformStringToDate(todayDate);
        Date start = TimeUtil.transformStringToDate(startDate);
        Date end = TimeUtil.transformStringToDate(endDate);

        String contentDetail;

        if (today.before(start)) {
            Log.d(TAG, name + ": 活动尚未开始");
            contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n活动还没开始呢";
            switch (name) {
                case "日氪":
                    dbHelper.updateDashboardContent("daily_recharge", "尚未开始");
                    dbHelper.updateDashboardContent("daily_recharge_emoji", "⏳");
                    dbHelper.updateDashboardContent("daily_recharge_detail", contentDetail);
                    break;
                case "欢乐假期":
                    dbHelper.updateDashboardContent("happy_holiday", "尚未开始");
                    dbHelper.updateDashboardContent("happy_holiday_emoji", "⏳");
                    dbHelper.updateDashboardContent("happy_holiday_detail", contentDetail);
                    break;
                case "助人为乐":
                    dbHelper.updateDashboardContent("cross_server_team_up", "尚未开始");
                    dbHelper.updateDashboardContent("cross_server_team_up_emoji", "⏳");
                    dbHelper.updateDashboardContent("cross_server_team_up_detail", contentDetail);
                    break;
                case "三岛":
                    dbHelper.updateDashboardContent("three_islands", "尚未开始");
                    dbHelper.updateDashboardContent("three_islands_emoji", "⏳");
                    dbHelper.updateDashboardContent("three_islands_detail", contentDetail);
                    break;
                case "美食大赛":
                    dbHelper.updateDashboardContent("food_contest", "尚未开始");
                    dbHelper.updateDashboardContent("food_contest_emoji", "⏳");
                    dbHelper.updateDashboardContent("food_contest_detail", contentDetail);
                    break;
            }
        } else if (today.after(end)) {
            Log.d(TAG, name + ": 活动已结束");
            contentDetail = "还没有新的活动呢";
            switch (name) {
                case "日氪":
                    dbHelper.updateDashboardContent("daily_recharge", "暂无");
                    dbHelper.updateDashboardContent("daily_recharge_emoji", "⏳");
                    dbHelper.updateDashboardContent("daily_recharge_detail", contentDetail);
                    break;
                case "欢乐假期":
                    dbHelper.updateDashboardContent("happy_holiday", "暂无");
                    dbHelper.updateDashboardContent("happy_holiday_emoji", "⏳");
                    dbHelper.updateDashboardContent("happy_holiday_detail", contentDetail);
                    break;
                case "助人为乐":
                    dbHelper.updateDashboardContent("cross_server_team_up", "暂无");
                    dbHelper.updateDashboardContent("cross_server_team_up_emoji", "⏳");
                    dbHelper.updateDashboardContent("cross_server_team_up_detail", contentDetail);
                    break;
                case "三岛":
                    dbHelper.updateDashboardContent("three_islands", "暂无");
                    dbHelper.updateDashboardContent("three_islands_emoji", "⏳");
                    dbHelper.updateDashboardContent("three_islands_detail", contentDetail);
                    break;
                case "美食大赛":
                    dbHelper.updateDashboardContent("food_contest", "暂无");
                    dbHelper.updateDashboardContent("food_contest_emoji", "⏳");
                    dbHelper.updateDashboardContent("food_contest_detail", contentDetail);
                    break;
            }
        } else {
            // 如果在结束当天过了上午10点，则也视为活动结束
            // 日氪和假期除外
            if (todayDate.equals(endDate) && TimeUtil.getCurrentHour() >= 10 && !name.equals("日氪") && !name.equals("欢乐假期")) {
                Log.d(TAG, name + ": 活动已结束");
                contentDetail = "还没有新的活动呢";
                switch (name) {
                    case "助人为乐":
                        dbHelper.updateDashboardContent("cross_server_team_up", "暂无");
                        dbHelper.updateDashboardContent("cross_server_team_up_emoji", "⏳");
                        dbHelper.updateDashboardContent("cross_server_team_up_detail", contentDetail);
                        break;
                    case "三岛":
                        dbHelper.updateDashboardContent("three_islands", "暂无");
                        dbHelper.updateDashboardContent("three_islands_emoji", "⏳");
                        dbHelper.updateDashboardContent("three_islands_detail", contentDetail);
                        break;
                    case "美食大赛":
                        dbHelper.updateDashboardContent("food_contest", "暂无");
                        dbHelper.updateDashboardContent("food_contest_emoji", "⏳");
                        dbHelper.updateDashboardContent("food_contest_detail", contentDetail);
                        break;
                }
            } else {
                Log.d(TAG, name + ": 活动正在进行中");
                // 需要计算活动持续多少天
                int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                int length;
                if (name.equals("日氪") || name.equals("假期")) {
                    // 当天晚上12点后才结束的，不减1
                    length = TimeUtil.calculateDaysBetween(startDate, endDate);
                } else {
                    // 当天10点后就结束的，减1
                    length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                }

                contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n";

                switch (name) {
                    case "日氪":
                        contentDetail = contentDetail + "本次日氪持续" + length + "天\n今天是第" + duringCount + "天\n\n" +
                                "今天你应该有" + duringCount * 8 + "个道具了";
                        dbHelper.updateDashboardContent("daily_recharge", duringCount * 8 + "/" + length * 8);
                        dbHelper.updateDashboardContent("daily_recharge_emoji", "✊");
                        dbHelper.updateDashboardContent("daily_recharge_detail", contentDetail);
                        break;
                    case "欢乐假期":
                        contentDetail = contentDetail + "本次假期持续" + length + "天\n今天是第" + duringCount + "天";
                        dbHelper.updateDashboardContent("happy_holiday", duringCount + "/" + length);
                        dbHelper.updateDashboardContent("happy_holiday_emoji", "✊");
                        dbHelper.updateDashboardContent("happy_holiday_detail", contentDetail);
                        break;
                    case "助人为乐":
                        contentDetail = contentDetail + "本次助人为乐持续" + length + "天\n今天是第" + duringCount + "天";
                        dbHelper.updateDashboardContent("cross_server_team_up", duringCount + "/" + length);
                        dbHelper.updateDashboardContent("cross_server_team_up_emoji", "✊");
                        dbHelper.updateDashboardContent("cross_server_team_up_detail", contentDetail);
                        break;
                    case "三岛":
                        contentDetail = contentDetail + "本次三岛活动持续" + length + "天\n今天是第" + duringCount + "天";
                        dbHelper.updateDashboardContent("three_islands", duringCount + "/" + length);
                        dbHelper.updateDashboardContent("three_islands_emoji", "✊");
                        dbHelper.updateDashboardContent("three_islands_detail", contentDetail);
                        break;
                    case "美食大赛":
                        contentDetail = contentDetail + "本次美食大赛持续" + length + "天\n今天是第" + duringCount + "天";
                        dbHelper.updateDashboardContent("food_contest", duringCount + "/" + length);
                        dbHelper.updateDashboardContent("food_contest_emoji", "✊");
                        dbHelper.updateDashboardContent("food_contest_detail", contentDetail);
                        break;
                }
            }
        }
    }
}
