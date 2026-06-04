package com.careful.HyperFVM.utils.ForDashboard.FromGame.FertilizationTask;

import android.util.Log;

import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * 施肥任务解析器
 * 负责获取并解析施肥任务XML，提取时间戳并处理日期计算逻辑
 */
public class FertilizationTaskCatcher {
    private static final String TAG = "FertilizationTaskCatcher";
    // XML数据源地址
    private static final String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/newtask.xml";
    // 获取内容的正则表达式
    private static final String regularExpression = "<task id=\"39\".*?startTime=\"(\\d+)\".*?endTime=\"(\\d+)\".*?</task>";

    /**
     * 核心方法：执行施肥任务解析流程
     */
    public void catchFertilizationTaskInfo(FertilizationTaskCatchResultCallBack callBack) {
        new Thread(() -> {
            try {
                // 步骤1：获取XML字符串
                String xmlContent = XMLHelper.getContentFromUrl(XML_URL);

                // 步骤2：通过正则表达式解析XML字符串
                Matcher matcher = XMLHelper.getContentByRegularExpression(xmlContent, regularExpression);
                if (matcher == null) {
                    Log.e(TAG, "catchFertilizationTaskInfo: 获取XML内容失败");

                    callBack.onResult(
                            generateMap("获取失败", "❌失败", "❌", "出错了呢", "获取内容失败，请联系开发者并提交此界面截图。")
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
                Date today = TimeUtil.transformStringToDate(todayDate);
                Date start = TimeUtil.transformStringToDate(startDate);
                Date end = TimeUtil.transformStringToDate(endDate);

                if (today.before(start)) {
                    Log.d(TAG, "活动尚未开始");
                    String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n趁现在多攒点肥料吧";

                    callBack.onResult(
                            generateMap("尚未开始", "暂无", "⏳", "等等等等", contentDetail)
                    );
                } else if (today.after(end)) {
                    Log.d(TAG, "活动已结束");
                    String contentDetail = "趁现在多攒点肥料吧";

                    callBack.onResult(
                            generateMap("暂无", "暂无", "⏳", "空空如也", contentDetail)
                    );
                } else {
                    Log.d(TAG, "活动正在进行中");
                    String contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate;

                    callBack.onResult(
                            generateMap(duringCount + "/21", duringCount + "/21", "✊", "施肥道具：" + duringCount + "/21", contentDetail)
                    );
                }

            } catch (IOException e) {
                Log.e(TAG, "catchFertilizationTaskInfo: 网络IO异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateMap("网络异常", "❌网络", "❌", "出错了呢", "网络异常\n请检查网络是否可用")
                );
            } catch (NumberFormatException e) {
                Log.e(TAG, "catchFertilizationTaskInfo: 时间戳转换异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateMap("获取失败", "❌失败", "❌", "出错了呢", "时间戳转换异常，请联系开发者并提交此界面截图")
                );
            } catch (ParseException e) {
                Log.e(TAG, "catchFertilizationTaskInfo: 日期解析异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateMap("获取失败", "❌失败", "❌", "出错了呢", "日期解析异常，请联系开发者并提交此界面截图")
                );
            }
        }).start();
    }

    /**
     * 保存结果到Map，用于及时输出数据
     * @param resultSimple 显示在主界面的简要信息
     * @param resultNotification 显示在通知的简要信息
     * @param resultEmoji 显示在主界面和弹窗上的表情
     * @param resultContentStatus 显示在弹窗上的状态信息
     * @param resultContentDetail 显示在弹窗上的详细信息
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateMap(String resultSimple, String resultNotification, String resultEmoji, String resultContentStatus, String resultContentDetail) {
        Map<String, String> result = new HashMap<>();

        result.put("resultSimple", resultSimple);
        result.put("resultNotification", resultNotification);
        result.put("resultEmoji", resultEmoji);
        result.put("resultContentStatus", resultContentStatus);
        result.put("resultContentDetail", resultContentDetail);

        return result;
    }

}