package com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear;

import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;

public class NewYearCatcher {
    private static final String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/new_year.xml";
    private static final String TAG = "NewYearCatcher";
    private final DBHelper dbHelper;

    // 缓存XML内容，避免重复网络请求
    private String cachedXmlContent;

    public NewYearCatcher(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 异步解析美食悬赏活动内容
     */
    public void catchBountyInfo() {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            String contentDetail; // 最终生成的结果文本

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);
                    sendBountyResultToDB("获取失败", "失败❌", "❌", errorMsg);
                    return;
                }

                // 第2步：获取原始内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "\\s*(活动时间:\\s*\\d{1,2}月\\d{1,2}日-\\d{1,2}月\\d{1,2}日10:00)\\s*");
                if (matcher == null) {
                    Log.e(TAG, "获取XML内容失败");
                    sendBountyResultToDB(
                            "获取失败", "失败❌", "❌", "获取内容失败，请联系开发者并提交此界面截图。"
                    );
                    return;
                }

                String bountyInfo = matcher.group(0);

                if (bountyInfo == null || bountyInfo.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);
                    sendBountyResultToDB("获取失败", "失败❌", "❌", errorMsg);
                    return;
                }
                Log.d(TAG, "catchTodayActivityInfo: 原始内容：" + bountyInfo);

                // 第3步：从原始内容提取两个日期
                /*
                    原始内容形如：[活动时间: 1月22日-2月5日10:00]
                    需要进行一步步分割
                 */
                String startDate = bountyInfo.split(":")[1].trim()
                        .split("-")[0].trim();
                String endDate = bountyInfo.split(":")[1].trim()
                        .split("-")[1].trim()
                        .split("日")[0].trim() + "日";

                /*
                    第4步：由于这里给的日期不是形如2026-01-01的形式，我们需要进行手动转换
                    涉及到跨年的两种特殊情况（startDate比endDate大）需要注意
                    （1）currentMonth == startMonth：startDate和todayDate属于同一年，endDate属于第二年
                    （2）currentMonth == endMonth：endDate和todayDate属于同一年，startDate属于前一年
                 */
                int startYear = 0;
                int startMonth = Integer.parseInt(startDate.split("月")[0]);
                int startDay = Integer.parseInt(startDate.split("月")[1].split("日")[0]);
                int endYear = 0;
                int endMonth = Integer.parseInt(endDate.split("月")[0]);
                int endDay = Integer.parseInt(endDate.split("月")[1].split("日")[0]);
                int currentYear = TimeUtil.getCurrentYear();
                int currentMonth = TimeUtil.getCurrentMonth();
                // 开始处理特殊情况
                if (startMonth > endMonth) {
                    if (currentMonth == startMonth) {
                        startYear = currentYear;
                        endYear = currentYear + 1;
                    } else if (currentMonth == endMonth) {
                        startYear = currentYear - 1;
                        endYear = currentYear;
                    }
                } else {
                    startYear = currentYear;
                    endYear = currentYear;
                }
                startDate = TimeUtil.generateFormattedDate(startYear, startMonth, startDay);
                endDate = TimeUtil.generateFormattedDate(endYear, endMonth, endDay);

                // 第5步：开始判断todayDate和startDate、endDate之间的关系，并向数据库写入结果
                // 先转换成Date类型，方便比较
                String todayDate = TimeUtil.getCurrentDate();
                Date today = TimeUtil.transformStringToDate(todayDate);
                Date start = TimeUtil.transformStringToDate(startDate);
                Date end = TimeUtil.transformStringToDate(endDate);

                if (today.before(start)) {
                    Log.d(TAG, "活动尚未开始");
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n活动还没开始呢";
                    sendBountyResultToDB(
                            "尚未开始",
                            "暂无⏳",
                            "⏳",
                            contentDetail
                    );
                } else if (today.after(end)) {
                    Log.d(TAG, "活动已结束");
                    contentDetail = "还没有新的活动呢";
                    sendBountyResultToDB(
                            "暂无",
                            "暂无⏳",
                            "⏳",
                            contentDetail
                    );
                } else {
                    Log.d(TAG, "活动正在进行中");
                    int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                    /*
                        还需要确定是2周的悬赏还是3周的悬赏
                     */
                    int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n进度：" + duringCount + "/" + length;
                    sendBountyResultToDB(
                            duringCount + "/" + length,
                            duringCount + "/" + length + "✊",
                            "✊",
                            contentDetail
                    );
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 异步解析百万消费活动内容
     */
    public void catchMillionConsumptionInfo() {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            String contentDetail; // 最终生成的结果文本

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);
                    sendMillionConsumptionResultToDB("获取失败", "❌", errorMsg);
                    return;
                }

                // 第2步：获取原始内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "\\s*(\\s*\\d{1,2}月\\d{1,2}日-\\d{1,2}月\\d{1,2}日10:00\" reddes=\"以下方式消费点券可领取豪礼：诸神宝殿、幸运转转、法老宝藏、塔罗寻宝、大富翁、商城、保险金、月卡、婚礼、宠物开槽、公会捐献！\")\\s*");
                if (matcher == null) {
                    Log.e(TAG, "获取XML内容失败");
                    sendMillionConsumptionResultToDB("获取失败", "❌", "获取内容失败，请联系开发者并提交此界面截图。");
                    return;
                }

                String millionConsumptionInfo = matcher.group(0);

                if (millionConsumptionInfo == null || millionConsumptionInfo.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);
                    sendMillionConsumptionResultToDB("获取失败", "❌", errorMsg);
                    return;
                }
                Log.d(TAG, "catchTodayActivityInfo: 原始内容：" + millionConsumptionInfo);

                // 第3步：从原始内容提取两个日期
                /*
                    原始内容形如：[01月29日-02月05日10:00" reddes="以下方式消费点券可领取豪礼：诸神宝殿、幸运转转、法老宝藏、塔罗寻宝、大富翁、商城、保险金、月卡、婚礼、宠物开槽、公会捐献！"]
                    需要进行一步步分割
                 */
                millionConsumptionInfo = millionConsumptionInfo.split("\"")[0];
                String startDate = millionConsumptionInfo.split("-")[0].trim();
                String endDate = millionConsumptionInfo.split("-")[1].trim()
                        .split("日")[0].trim() + "日";

                /*
                    第4步：由于这里给的日期不是形如2026-01-01的形式，我们需要进行手动转换
                    涉及到跨年的两种特殊情况（startDate比endDate大）需要注意
                    （1）currentMonth == startMonth：startDate和todayDate属于同一年，endDate属于第二年
                    （2）currentMonth == endMonth：endDate和todayDate属于同一年，startDate属于前一年
                 */
                int startYear = 0;
                int startMonth = Integer.parseInt(startDate.split("月")[0]);
                int startDay = Integer.parseInt(startDate.split("月")[1].split("日")[0]);
                int endYear = 0;
                int endMonth = Integer.parseInt(endDate.split("月")[0]);
                int endDay = Integer.parseInt(endDate.split("月")[1].split("日")[0]);
                int currentYear = TimeUtil.getCurrentYear();
                int currentMonth = TimeUtil.getCurrentMonth();
                // 开始处理特殊情况
                if (startMonth > endMonth) {
                    if (currentMonth == startMonth) {
                        startYear = currentYear;
                        endYear = currentYear + 1;
                    } else if (currentMonth == endMonth) {
                        startYear = currentYear - 1;
                        endYear = currentYear;
                    }
                } else {
                    startYear = currentYear;
                    endYear = currentYear;
                }
                startDate = TimeUtil.generateFormattedDate(startYear, startMonth, startDay);
                endDate = TimeUtil.generateFormattedDate(endYear, endMonth, endDay);

                // 第5步：开始判断todayDate和startDate、endDate之间的关系，并向数据库写入结果
                // 先转换成Date类型，方便比较
                String todayDate = TimeUtil.getCurrentDate();
                Date today = TimeUtil.transformStringToDate(todayDate);
                Date start = TimeUtil.transformStringToDate(startDate);
                Date end = TimeUtil.transformStringToDate(endDate);

                if (today.before(start)) {
                    Log.d(TAG, "活动尚未开始");
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n活动还没开始呢";
                    sendMillionConsumptionResultToDB(
                            "尚未开始",
                            "⏳",
                            contentDetail
                    );
                } else if (today.after(end)) {
                    Log.d(TAG, "活动已结束");
                    contentDetail = "还没有新的活动呢";
                    sendMillionConsumptionResultToDB(
                            "暂无",
                            "⏳",
                            contentDetail
                    );
                } else {
                    Log.d(TAG, "活动正在进行中");
                    int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                    /*
                        还需要确定消费的持续时间
                     */
                    int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n本次消费一共持续" + length + "天\n今天是第" + duringCount + "天";
                    sendMillionConsumptionResultToDB(
                            duringCount + "/" + length,
                            "\uD83D\uDCB8",
                            contentDetail
                    );
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 异步解析抢红包活动内容
     */
    public void catchLuckyConsumptionInfo() {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            StringBuilder contentDetail; // 最终生成的结果文本

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchLuckyConsumptionInfo: " + errorMsg);
                    sendLuckyMoneyResultToDB("获取失败", "❌", errorMsg);
                    return;
                }

                // 第2步：获取原始内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "stime\\s*=\\s*\"[^\"]*\"\\s+reddes\\s*=\\s*\"下午13:00-15:00之间，各个服务器随机间隔一定时间开启抢红包活动\"");
                if (matcher == null) {
                    Log.e(TAG, "catchLuckyConsumptionInfo：获取XML内容失败");
                    sendLuckyMoneyResultToDB("获取失败", "❌", "获取内容失败，请联系开发者并提交此界面截图。");
                    return;
                }

                String luckyMoneyInfo = matcher.group(0);

                if (luckyMoneyInfo == null || luckyMoneyInfo.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchLuckyConsumptionInfo: " + errorMsg);
                    sendLuckyMoneyResultToDB("获取失败", "❌", errorMsg);
                    return;
                }
                Log.d(TAG, "catchLuckyConsumptionInfo: 原始内容：" + luckyMoneyInfo);

                /*
                    第3步：从原始内容提取所有日期
                    原始内容形如：[stime="12月25日|1月1日|1月26日|2月5日" reddes="下午13:00-15:00之间，各个服务器随机间隔一定时间开启抢红包活动"]
                    需要进行一步步分割
                 */
                luckyMoneyInfo = luckyMoneyInfo.split("\"")[1];
                String[] dateArray = luckyMoneyInfo.split("\\|");

                /*
                    第4步：
                    因为这里不是yyyy-MM-dd的形式，所以我们只能逐一对比月和日
                    如果当前日期能和其中一个匹配上，则说明今天有抢红包活动，直接退出
                    否则继续寻找
                    如果全部都没匹配上，则说明今天没有抢红包活动
                 */
                int currentMonth = TimeUtil.getCurrentMonth();
                int currentDay = TimeUtil.getCurrentDay();
                for (String s : dateArray) {
                    int month = Integer.parseInt(s.split("月")[0]);
                    int day = Integer.parseInt(s.split("月")[1].split("日")[0]);
                    Log.d(TAG, "catchLuckyConsumptionInfo：正在匹配日期，今天：" + currentMonth + "月" + currentDay + "日，匹配到：" + month + "月" + day + "日");
                    if (month == currentMonth && day == currentDay) {
                        Log.d(TAG, "catchLuckyConsumptionInfo：匹配到了日期");
                        contentDetail = new StringBuilder("今天13点到15点抢红包\n恭喜发财，红包拿来");
                        sendLuckyMoneyResultToDB(
                                "恭喜发财",
                                "\uD83E\uDDE7",
                                contentDetail.toString()
                        );
                        return;
                    }
                }

                // 来到这里的话说明今天没有抢红包活动
                Log.d(TAG, "catchLuckyConsumptionInfo：一个日期都没匹配上");
                contentDetail = new StringBuilder("今天没有抢红包活动\n\n以下日期有抢红包活动\n");
                for (String s : dateArray) {
                    contentDetail.append(s).append("\n");
                }
                contentDetail.append("\n再等等吧");
                sendLuckyMoneyResultToDB(
                        "暂无",
                        "⏳",
                        contentDetail.toString()
                );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 美食悬赏：向数据库写入结果
     * @param contentSimple 显示在主界面的简要信息
     * @param contentNotification 显示在常驻通知的简要信息
     * @param emoji 显示在主界面和弹窗上的表情
     * @param contentDetail 显示在弹窗上的详细信息
     */
    private void sendBountyResultToDB(String contentSimple, String contentNotification, String emoji, String contentDetail) {
        dbHelper.updateDashboardContent("bounty", contentSimple);
        dbHelper.updateDashboardContent("bounty_notification", contentNotification);
        dbHelper.updateDashboardContent("bounty_emoji", emoji);
        dbHelper.updateDashboardContent("bounty_detail", contentDetail);
    }

    /**
     * 百万消费：向数据库写入结果
     * @param contentSimple 显示在主界面的简要信息
     * @param emoji 显示在主界面和弹窗上的表情
     * @param contentDetail 显示在弹窗上的详细信息
     */
    private void sendMillionConsumptionResultToDB(String contentSimple, String emoji, String contentDetail) {
        dbHelper.updateDashboardContent("million_consumption", contentSimple);
        dbHelper.updateDashboardContent("million_consumption_emoji", emoji);
        dbHelper.updateDashboardContent("million_consumption_detail", contentDetail);
    }

    /**
     * 抢红包：向数据库写入结果
     * @param contentSimple 显示在主界面的简要信息
     * @param emoji 显示在主界面和弹窗上的表情
     * @param contentDetail 显示在弹窗上的详细信息
     */
    private void sendLuckyMoneyResultToDB(String contentSimple, String emoji, String contentDetail) {
        dbHelper.updateDashboardContent("lucky_money", contentSimple);
        dbHelper.updateDashboardContent("lucky_money_emoji", emoji);
        dbHelper.updateDashboardContent("lucky_money_detail", contentDetail);
    }
}
