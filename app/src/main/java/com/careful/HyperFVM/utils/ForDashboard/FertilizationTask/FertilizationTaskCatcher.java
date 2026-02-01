package com.careful.HyperFVM.utils.ForDashboard.FertilizationTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;

/**
 * 施肥任务解析器
 * 负责获取并解析施肥任务XML，提取时间戳并处理日期计算逻辑
 */
public class FertilizationTaskCatcher {
    private static final String TAG = "FertilizationTaskCatcher";
    // XML数据源地址
    private static final String TASK_XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/newtask.xml";
    // 获取内容的正则表达式
    private static final String regularExpression = "<task id=\"39\".*?startTime=\"(\\d+)\".*?endTime=\"(\\d+)\".*?</task>";

    // 日期格式化器（线程不安全，需局部创建）
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final DBHelper dbHelper;

    public FertilizationTaskCatcher(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 核心方法：执行施肥任务解析流程
     */
    public void catchFertilizationTaskInfo() {
        try {
            // 步骤1：获取XML字符串
            String xmlContent = XMLHelper.getXMLStringFromUrl(TASK_XML_URL);

            // 步骤2：通过正则表达式解析XML字符串
            Matcher matcher = XMLHelper.getContentByRegularExpression(xmlContent, regularExpression);
            if (matcher == null) {
                Log.e(TAG, "catchFertilizationTaskInfo: 获取XML内容失败");
                sendResultToDB(
                        "获取失败",
                        "失败❌",
                        "❌",
                        "获取内容失败，请联系开发者并提交此界面截图。"
                );
                return;
            }

            // 步骤3：获取startTime和endTime
            String startTimeStr = matcher.group(1);
            String endTimeStr = matcher.group(2);

            // 步骤4：转换时间戳为日期字符串
            long startTimeStamp = Long.parseLong(Objects.requireNonNull(startTimeStr));
            long endTimeStamp = Long.parseLong(Objects.requireNonNull(endTimeStr));
            String startDate = TimeUtil.convertTimeStampToDate(startTimeStamp);
            String endDate = TimeUtil.convertTimeStampToDate(endTimeStamp);
            Log.d(TAG, "catchFertilizationTaskInfo: 活动开始日期：" + startDate + "，结束日期：" + endDate);

            // 步骤5：获取当前日期
            String todayDate = TimeUtil.getCurrentDate();
            Log.d(TAG, "catchFertilizationTaskInfo: 当前日期：" + todayDate);

            // 步骤6：计算天数差
            int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
            Log.d(TAG, "catchFertilizationTaskInfo: 从当前日期到活动开始日期的天数（包含首尾）：" + duringCount);

            // 步骤7：判断活动状态
            checkActivityStatus(todayDate, startDate, endDate, duringCount);

        } catch (IOException e) {
            Log.e(TAG, "catchFertilizationTaskInfo: 网络IO异常：" + e.getMessage(), e);
            sendResultToDB(
                    "网络异常",
                    "网络❌",
                    "❌",
                    "网络异常\n请检查网络是否可用"
            );
        } catch (NumberFormatException e) {
            Log.e(TAG, "catchFertilizationTaskInfo: 时间戳转换异常：" + e.getMessage(), e);
            sendResultToDB(
                    "获取失败",
                    "失败❌",
                    "❌",
                    "时间戳转换异常，请联系开发者并提交此界面截图"
            );
        } catch (ParseException e) {
            Log.e(TAG, "catchFertilizationTaskInfo: 日期解析异常：" + e.getMessage(), e);
            sendResultToDB(
                    "获取失败",
                    "失败❌",
                    "❌",
                    "日期解析异常，请联系开发者并提交此界面截图"
            );
        }
    }

    /**
     * 检查活动状态
     * @param todayDate 当前日期
     * @param startDate 活动开始日期
     * @param endDate 活动结束日期
     * @throws ParseException 解析异常
     */
    private void checkActivityStatus(String todayDate, String startDate, String endDate, int duringCount) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault()); // 保持和其他日期处理一致的时区
        Date today = sdf.parse(todayDate);
        Date start = sdf.parse(startDate);
        Date end = sdf.parse(endDate);

        if (Objects.requireNonNull(today).before(start)) {
            Log.d(TAG, "活动尚未开始");
            String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n活动还没开始\n趁现在多攒点肥料吧";
            sendResultToDB(
                    "尚未开始",
                    "暂无⏳",
                    "⏳",
                    contentDetail
            );
        } else if (today.after(end)) {
            Log.d(TAG, "活动已结束");
            String contentDetail = "还没有新的活动\n趁现在多攒点肥料吧";
            sendResultToDB(
                    "暂无",
                    "暂无⏳",
                    "⏳",
                    contentDetail
            );
        } else {
            Log.d(TAG, "活动正在进行中");
            String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n进度：" + "(" + duringCount + "/21)";
            sendResultToDB(
                    "(" + duringCount + "/21)",
                    "(" + duringCount + "/21)✊",
                    "✊",
                    contentDetail
            );
        }
    }

    /**
     * 向数据库写入结果
     * @param contentSimple 显示在主界面的简要信息
     * @param contentNotification 显示在常驻通知的简要信息
     * @param emoji 显示在主界面和弹窗上的表情
     * @param contentDetail 显示在弹窗上的详细信息
     */
    private void sendResultToDB(String contentSimple, String contentNotification, String emoji, String contentDetail) {
        dbHelper.updateDashboardContent("fertilization_task", contentSimple);
        dbHelper.updateDashboardContent("fertilization_task_notification", contentNotification);
        dbHelper.updateDashboardContent("fertilization_task_emoji", emoji);
        dbHelper.updateDashboardContent("fertilization_task_detail", contentDetail);
    }
}