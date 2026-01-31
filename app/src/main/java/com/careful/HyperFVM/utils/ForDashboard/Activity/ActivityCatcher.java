package com.careful.HyperFVM.utils.ForDashboard.Activity;

import android.content.Context;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * 活动捕获器：获取并解析每日双倍双爆活动信息
 */
public class ActivityCatcher {
    private static final String TAG = "ActivityCatcher";
    // XML文件网络地址
    private static String XML_ACTIVITY_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/activity.xml";
    // 目标XML层级路径
    private static final String TARGET_XML_PATH = "root/activitys/activity";
    // 匹配属性Key（日期）
    private static final String MATCH_ATTR_KEY = "time";
    // 目标属性Key（活动内容）
    private static final String TARGET_ATTR_KEY = "content";
    // 统一日期格式常量
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // 新增：存储结果的核心变量
    private String todayDate;                  // 当前日期
    private String nextFullDoubleDay;          // 下一个全天双倍双爆日期
    private int numDaysToNextFullDoubleDay = -1; // 距离下一个全天双倍双爆的天数
    private int numDaysKeepFullDoubleDay = 0;  // 全天双倍双爆持续天数
    private String endDayKeepFullDoubleDay;    // 全天双倍双爆最后一天

    private final DBHelper dbHelper;

    // 缓存XML内容，避免重复网络请求
    private String cachedXmlContent;

    public ActivityCatcher(Context context) {
        this.dbHelper = new DBHelper(context);

        Random random = new Random();
        int num = 1 + random.nextInt(1000000000);

        XML_ACTIVITY_URL = XML_ACTIVITY_URL + "?" + num;
    }

    /**
     * 获取当前日期（格式：yyyy-MM-dd）
     * @return 格式化后的当前日期字符串
     */
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
        return sdf.format(calendar.getTime());
    }

    /**
     * 异步解析今日活动内容（核心方法）
     */
    public void parseTodayActivityContent() {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            String result; // 最终生成的结果文本
            try {
                // 步骤1：获取今日日期（匹配用）
                todayDate = getCurrentDate();
                Log.d(TAG, "今日日期：" + todayDate);

                // 步骤2：从网络获取XML字符串并缓存
                cachedXmlContent = XMLHelper.getXMLStringFromUrl(XML_ACTIVITY_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, errorMsg);
                    updateDBWithError("获取失败", errorMsg);
                    return;
                }

                // 步骤3：获取今日活动原始Content
                String rawContent = getTodayRawContent();
                if (rawContent == null || rawContent.trim().isEmpty()) {
                    errorMsg = "未找到今日(" + todayDate + ")的活动内容，请联系开发者";
                    Log.e(TAG, errorMsg);
                    updateDBWithError("获取失败", errorMsg);
                    return;
                }
                Log.d(TAG, "原始Content内容：" + rawContent);

                // 步骤4：提取第二部分内容（按<br>分割）
                String targetContent = extractSecondPartContent(rawContent);
                if (targetContent == null) {
                    errorMsg = "活动内容格式解析失败，请联系开发者";
                    Log.e(TAG, errorMsg);
                    updateDBWithError("解析失败", errorMsg);
                    return;
                }

                // 步骤5：解析XML中所有活动日期和对应的content（使用缓存的XML内容，重新创建解析器）
                Map<String, String> dateContentMap = getAllActivityDateContentMap();
                if (dateContentMap.isEmpty()) {
                    errorMsg = "未解析到任何活动日期数据，请联系开发者";
                    Log.e(TAG, errorMsg);
                    updateDBWithError("解析失败", errorMsg);
                    return;
                }

                // 步骤6：判断今日是否是全天双倍双爆，分支处理
                if (targetContent.contains("00:00-23:59开启双倍双爆")) {
                    // 分支1：今日是全天双倍双爆
                    Object[] continuousResult = findContinuousFullDoubleDays(dateContentMap, todayDate);
                    numDaysKeepFullDoubleDay = (int) continuousResult[0];
                    endDayKeepFullDoubleDay = (String) continuousResult[1];

                    if (numDaysKeepFullDoubleDay <= 0 || endDayKeepFullDoubleDay == null) {
                        errorMsg = "未找到连续的全天双倍双爆日期，请联系开发者";
                        Log.e(TAG, errorMsg);
                        updateDBWithError("解析失败", errorMsg);
                        return;
                    }

                    // 生成结果文本
                    result = "今天是" + todayDate + "\n今天已开启全天双倍双爆\n将持续到" + endDayKeepFullDoubleDay + "\n共" + numDaysKeepFullDoubleDay + "天";
                    dbHelper.updateDashboardContent("double_explosion_rate", "全天双爆");
                    dbHelper.updateDashboardContent("double_explosion_rate_emoji", "🎉");
                    dbHelper.updateDashboardContent("double_explosion_rate_detail", result);
                } else {
                    // 分支2：今日是限时双倍双爆
                    // 查找下一个全天双倍双爆日期
                    nextFullDoubleDay = findNextFullDoubleDay(dateContentMap, todayDate);
                    if (nextFullDoubleDay == null) {
                        result = "今天是" + todayDate + "\n" + targetContent.split("。")[0] + "\n" + targetContent.split("。")[1] + "\n今年已经没有全天双倍双爆了。";
                        dbHelper.updateDashboardContent("double_explosion_rate", "限时双爆");
                        dbHelper.updateDashboardContent("double_explosion_rate_emoji", "⏳");
                        dbHelper.updateDashboardContent("double_explosion_rate_detail", result);
                        return;
                    }

                    // 计算距离下一个全天双倍双爆的天数
                    numDaysToNextFullDoubleDay = calculateDaysBetween(todayDate, nextFullDoubleDay);
                    if (numDaysToNextFullDoubleDay < 0) {
                        errorMsg = "日期计算错误，下一个全天双倍双爆日期早于今日，请联系开发者。";
                        Log.e(TAG, errorMsg);
                        updateDBWithError("解析失败", errorMsg);
                        return;
                    }

                    // 计算下一个全天双倍双爆的持续天数和结束日期
                    Object[] continuousResult = findContinuousFullDoubleDays(dateContentMap, nextFullDoubleDay);
                    numDaysKeepFullDoubleDay = (int) continuousResult[0];
                    endDayKeepFullDoubleDay = (String) continuousResult[1];

                    if (numDaysKeepFullDoubleDay <= 0 || endDayKeepFullDoubleDay == null) {
                        errorMsg = "未找到" + nextFullDoubleDay + "之后连续的全天双倍双爆日期，请联系开发者。";
                        Log.e(TAG, errorMsg);
                        updateDBWithError("解析失败", errorMsg);
                        return;
                    }

                    // 生成结果文本
                    result = "今天是" + todayDate + "\n" + targetContent.split("。")[0] + "\n" + targetContent.split("。")[1] + "\n\n下一个全天双倍双爆日期为" + nextFullDoubleDay + "\n还有" + numDaysToNextFullDoubleDay + "天\n该全天双倍双爆将持续到" + endDayKeepFullDoubleDay + "\n共" + numDaysKeepFullDoubleDay + "天";
                    dbHelper.updateDashboardContent("double_explosion_rate", "限时双爆");
                    dbHelper.updateDashboardContent("double_explosion_rate_emoji", "⏳");
                    dbHelper.updateDashboardContent("double_explosion_rate_detail", result);
                }

            } catch (IOException e) {
                errorMsg = "网络/解析异常：" + e.getMessage();
                Log.e(TAG, "解析活动内容异常：" + e.getMessage(), e);
                updateDBWithError("解析失败", errorMsg);
            }
        }).start();
    }

    /**
     * 辅助方法：更新数据库错误信息
     */
    private void updateDBWithError(String status, String detail) {
        dbHelper.updateDashboardContent("double_explosion_rate", status);
        dbHelper.updateDashboardContent("double_explosion_rate_emoji", "❌");
        dbHelper.updateDashboardContent("double_explosion_rate_detail", detail);
    }

    /**
     * 获取今日原始Content（单独抽离，避免解析器复用问题）
     */
    private String getTodayRawContent() throws IOException {
        XmlPullParser parser = XMLHelper.getXmlPullParser(cachedXmlContent);
        if (parser == null) {
            return null;
        }
        return XMLHelper.getAttrValueByPathAndMatchAttr(
                parser,
                TARGET_XML_PATH,
                MATCH_ATTR_KEY,
                todayDate,
                TARGET_ATTR_KEY
        );
    }

    /**
     * 提取content字符串的第二部分内容（按<br>分割）
     * @param rawContent 原始content属性值
     * @return 第二部分内容，格式错误返回null
     */
    private String extractSecondPartContent(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            Log.e(TAG, "extractSecondPartContent: 原始内容为空");
            return null;
        }

        // 按<br>分割（注意转义，避免正则问题）
        String[] contentParts = rawContent.split("<br>");
        // 校验分割后的数组长度（至少要有第二部分）
        if (contentParts.length < 2) {
            Log.e(TAG, "extractSecondPartContent: 内容格式错误，分割后长度不足：" + contentParts.length);
            return null;
        }

        // 提取第二部分并去除首尾空格
        String secondPart = contentParts[1].trim();
        if (secondPart.isEmpty()) {
            Log.e(TAG, "extractSecondPartContent: 第二部分内容为空");
            return null;
        }

        return secondPart;
    }

    /**
     * 解析XML中所有活动节点的日期和对应的content第二部分
     * @return 日期->content第二部分的Map，解析失败返回空Map
     */
    private Map<String, String> getAllActivityDateContentMap() {
        Map<String, String> dateContentMap = new HashMap<>();
        if (cachedXmlContent == null || cachedXmlContent.trim().isEmpty()) {
            Log.e(TAG, "getAllActivityDateContentMap: XML内容为空");
            return dateContentMap;
        }

        try {
            // 步骤1：重新创建解析器，避免复用导致的指针偏移
            XmlPullParser parser = XMLHelper.getXmlPullParser(cachedXmlContent);
            if (parser == null) {
                Log.e(TAG, "getAllActivityDateContentMap: 解析器创建失败");
                return dateContentMap;
            }

            // 步骤2：收集所有activity节点的time属性（日期）
            List<String> allActivityDates = collectAllActivityDates(parser);
            if (allActivityDates.isEmpty()) {
                Log.e(TAG, "getAllActivityDateContentMap: 未解析到任何活动日期");
                return dateContentMap;
            }

            // 步骤3：对每个日期，重新创建解析器获取content属性
            for (String date : allActivityDates) {
                XmlPullParser dateParser = XMLHelper.getXmlPullParser(cachedXmlContent);
                String rawContent = XMLHelper.getAttrValueByPathAndMatchAttr(
                        dateParser,
                        TARGET_XML_PATH,
                        MATCH_ATTR_KEY,
                        date,
                        TARGET_ATTR_KEY
                );
                if (rawContent != null && !rawContent.trim().isEmpty()) {
                    String contentPart = extractSecondPartContent(rawContent);
                    if (contentPart != null) {
                        dateContentMap.put(date, contentPart);
                        // Log.d(TAG, "收集到活动：日期：" + date + "，内容：" + contentPart);
                    }
                }
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "getAllActivityDateContentMap: 解析XML失败：" + e.getMessage(), e);
        }

        Log.d(TAG, "解析到的活动日期数量：" + dateContentMap.size());
        return dateContentMap;
    }

    /**
     * 辅助方法：收集所有activity节点的time属性（日期）
     * @param parser XmlPullParser实例（全新创建的）
     * @return 所有活动日期列表
     * @throws XmlPullParserException XML解析异常
     * @throws IOException IO异常
     */
    private List<String> collectAllActivityDates(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<String> dateList = new ArrayList<>();
        if (parser == null) {
            Log.e(TAG, "collectAllActivityDates: 解析器为空");
            return dateList;
        }

        // 重置解析器到初始状态
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        int eventType = parser.getEventType();

        // 遍历XML直到找到root标签，再找activity标签
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    // 匹配activity标签，读取time属性
                    if ("activity".equals(tagName)) {
                        String timeAttr = parser.getAttributeValue(null, "time");
                        if (timeAttr != null && !timeAttr.trim().isEmpty()) {
                            dateList.add(timeAttr.trim());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    // 无需特殊处理
                    break;
            }
            // 移动到下一个节点（避免死循环）
            eventType = parser.next();
        }
        return dateList;
    }

    /**
     * 计算两个日期之间的天数差（date2 - date1）
     * @param date1 开始日期（格式：yyyy-MM-dd）
     * @param date2 结束日期（格式：yyyy-MM-dd）
     * @return 天数差，日期格式错误返回-1
     */
    private int calculateDaysBetween(String date1, String date2) {
        if (date1 == null || date2 == null || date1.trim().isEmpty() || date2.trim().isEmpty()) {
            Log.e(TAG, "calculateDaysBetween: 日期参数为空");
            return -1;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
        try {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(Objects.requireNonNull(sdf.parse(date1)));
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(Objects.requireNonNull(sdf.parse(date2)));

            long diffTime = cal2.getTimeInMillis() - cal1.getTimeInMillis();
            return (int) (diffTime / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            Log.e(TAG, "calculateDaysBetween: 日期解析失败：" + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 从指定起始日期开始，查找连续的全天双倍双爆天数和最后一天日期
     * @param dateContentMap 日期->content的Map
     * @param startDate 起始日期（格式：yyyy-MM-dd）
     * @return 数组：[0] = 持续天数（int），[1] = 最后一天日期（String），无匹配返回[0, null]
     */
    private Object[] findContinuousFullDoubleDays(Map<String, String> dateContentMap, String startDate) {
        int continuousDays = 0;
        String lastDate = null;

        if (dateContentMap == null || dateContentMap.isEmpty() || startDate == null || startDate.trim().isEmpty()) {
            Log.e(TAG, "findContinuousFullDoubleDays: 输入参数无效");
            return new Object[]{0, null};
        }

        // 先将Map中的日期排序
        List<String> sortedDates = new ArrayList<>(dateContentMap.keySet());
        sortedDates.sort((d1, d2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
            try {
                return Objects.requireNonNull(sdf.parse(d1)).compareTo(sdf.parse(d2));
            } catch (ParseException e) {
                Log.e(TAG, "日期排序失败：" + e.getMessage(), e);
                return 0;
            }
        });

        // 找到起始日期的索引
        int startIndex = -1;
        for (int i = 0; i < sortedDates.size(); i++) {
            Log.d(TAG, "i = " + i + "，sortedDates.get(i) = " + sortedDates.get(i) + "，startDate = " + startDate);
            if (sortedDates.get(i).equals(startDate)) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) {
            Log.e(TAG, "findContinuousFullDoubleDays: 起始日期" + startDate + "不在活动列表中");
            return new Object[]{0, null};
        }

        // 从起始日期开始遍历，统计连续的全天双倍双爆天数
        for (int i = startIndex; i < sortedDates.size(); i++) {
            String currentDate = sortedDates.get(i);
            String content = dateContentMap.get(currentDate);
            Log.d(TAG, "统计连续的全天双倍双爆天数：i = " + i + "，日期：" + currentDate + "，内容" + content);

            // 判断是否是全天双倍双爆
            if (content != null && content.contains("00:00-23:59开启双倍双爆")) {
                continuousDays++;
                lastDate = currentDate;
            } else {
                // 非全天，终止遍历
                break;
            }
        }

        return new Object[]{continuousDays, lastDate};
    }

    /**
     * 查找从指定日期（包含）之后的第一个全天双倍双爆日期
     * @param dateContentMap 日期->content的Map
     * @param startDate 起始日期（格式：yyyy-MM-dd）
     * @return 第一个全天双倍双爆日期，无匹配返回null
     */
    private String findNextFullDoubleDay(Map<String, String> dateContentMap, String startDate) {
        if (dateContentMap == null || dateContentMap.isEmpty() || startDate == null || startDate.trim().isEmpty()) {
            Log.e(TAG, "findNextFullDoubleDay: 输入参数无效");
            return null;
        }

        // 日期排序
        List<String> sortedDates = new ArrayList<>(dateContentMap.keySet());
        sortedDates.sort((d1, d2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
            try {
                return Objects.requireNonNull(sdf.parse(d1)).compareTo(sdf.parse(d2));
            } catch (ParseException e) {
                Log.e(TAG, "日期排序失败：" + e.getMessage(), e);
                return 0;
            }
        });

        // 遍历排序后的日期，找到第一个在startDate之后且是全天双倍双爆的日期
        for (String date : sortedDates) {
            // 跳过早于startDate的日期
            if (calculateDaysBetween(startDate, date) < 0) {
                continue;
            }
            String content = dateContentMap.get(date);
            if (content != null && content.contains("00:00-23:59开启双倍双爆")) {
                return date;
            }
        }

        Log.e(TAG, "findNextFullDoubleDay: 未找到" + startDate + "之后的全天双倍双爆日期");
        return null;
    }
}