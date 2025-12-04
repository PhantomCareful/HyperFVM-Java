package com.careful.HyperFVM.utils.ForDashboard.Activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ActivityQuery {
    private final DBHelper dbHelper;
    private final Handler mainHandler;
    private final List<ActivityItem> activityList = new ArrayList<>();

    private final Context context; // 新增

    // 活动类型常量
    private static final int TYPE_FULL_DAY = 0;
    private static final int TYPE_PARTIAL_DAY = 1;
    private static final int TYPE_EXP_FULL_DAY = 2;

    // 消息类型（用于双倍双爆查询）
    private static final int PARSE_SUCCESS = 1;
    private static final int PARSE_FAILED = 2;

    // 定义日期格式常量
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // 回调接口：通知查询结果
    public interface ActivityQueryListener {
        void onResult(String resultText);
    }

    // UI更新Handler（用于双倍双爆查询结果回调）
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARSE_SUCCESS:
                    // 解析成功后生成显示文本
                    String doubleExplosionText = generateActivityText();
                    dbHelper.updateDashboardContent("double_explosion_rate", doubleExplosionText);
                    break;
                case PARSE_FAILED:
                    dbHelper.updateDashboardContent("double_explosion_rate", "查询双倍双爆失败❌");
                    dbHelper.updateDashboardContent("double_explosion_rate_notification", "查询失败❌");
                    break;
            }
        }
    };

    // 活动数据模型
    private static class ActivityItem {
        String date;
        int type;

        ActivityItem(String date, int type) {
            this.date = date;
            this.type = type;
        }
    }

    public ActivityQuery(Context context) {
        this.dbHelper = new DBHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.context = context; // 初始化
    }


    // 执行查询并保存结果
    public void queryAndSaveActivity(ActivityQueryListener listener) {
        if (!isNetworkAvailable()) {
            dbHelper.updateDashboardContent("double_explosion_rate", "网络不可用，无法查询双倍双爆❌");
            dbHelper.updateDashboardContent("double_explosion_rate_notification", "网络不可用❌");
            listener.onResult("❌请检查网络");
            return;
        }

        // 子线程执行网络请求
        new Thread(() -> fetchActivityData(listener)).start();
    }

    // 网络请求获取活动数据
    private void fetchActivityData(ActivityQueryListener listener) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/activity.xml";

        Random random = new Random();
        int num = 1 + random.nextInt(1000000000);

        XML_URL = XML_URL + "?" + num;

        try {
            // 目标XML地址
            URL url = new URL(XML_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)"); // 模拟浏览器UA

            // 重试机制
            int retryCount = 0;
            final int MAX_RETRIES = 3;
            while (connection.getResponseCode() != HttpURLConnection.HTTP_OK && retryCount < MAX_RETRIES) {
                retryCount++;
                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 恢复重构前的逐行读取方式，确保内容完整
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)); // 指定编码
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Log.d("ActivityQuery", "XML读取成功，长度：" + response.length());

                // 检查响应是否为空
                if (response.length() == 0) {
                    Log.e("ActivityQuery", "XML内容为空");
                    mainHandler.post(() -> listener.onResult("获取双倍双爆数据为空❌"));
                    return;
                }

                // 解析XML并处理结果
                if (parseActivityXml(response.toString())) {
                    handler.sendEmptyMessage(PARSE_SUCCESS);
                } else {
                    handler.sendEmptyMessage(PARSE_FAILED);
                }
            } else {
                Log.e("ActivityQuery", "请求失败，响应码：" + connection.getResponseCode());
                handler.sendEmptyMessage(PARSE_FAILED);
            }
        } catch (Exception e) {
            Log.e("ActivityQuery", "网络请求异常：" + e.getMessage(), e);
            handler.sendEmptyMessage(PARSE_FAILED);
        } finally {
            // 释放资源
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 解析XML数据
    private boolean parseActivityXml(String xmlData) {
        activityList.clear();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new java.io.StringReader(xmlData));
            int eventType = parser.getEventType();
            String currentDate = null;
            String currentContent = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("activity".equals(tagName)) {
                            // 提取日期和内容（与重构前逻辑一致）
                            currentDate = parser.getAttributeValue(null, "time");
                            currentContent = parser.getAttributeValue(null, "content");
                            if (currentDate != null && currentContent != null) {
                                // 处理HTML标签（与重构前一致）
                                int index = currentContent.indexOf("<br>");
                                if (index != -1) {
                                    currentContent = currentContent.substring(index + 4).trim();
                                }
                                if (currentContent.endsWith("<br>")) {
                                    currentContent = currentContent.substring(0, currentContent.length() - 4).trim();
                                }
                                currentContent = currentContent.replace("<br>", "");
                                Log.d("ActivityQuery", "解析到活动：日期=" + currentDate + "，内容=" + currentContent);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("activity".equals(tagName) && currentDate != null && currentContent != null) {
                            // 直接使用原始日期，不额外格式化（与重构前一致）
                            int activityType = getActivityType(currentContent);
                            activityList.add(new ActivityItem(currentDate, activityType));
                            currentDate = null;
                            currentContent = null;
                        }
                        break;
                }
                eventType = parser.next();
            }

            Log.d("ActivityQuery", "XML解析完成，活动总数：" + activityList.size());
            return !activityList.isEmpty();
        } catch (Exception e) {
            Log.e("ActivityQuery", "XML解析异常：" + e.getMessage(), e);
            return false;
        }
    }

    // 生成活动显示文本
    private String generateActivityText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
        String today = dateFormat.format(new Date());
        ActivityItem todayItem = findActivityByDate(today);

        // 明确日志，便于排查匹配问题
        Log.d("ActivityQuery", "今日日期：" + today + "，匹配到活动：" + (todayItem != null ? todayItem.type : "null"));

        if (todayItem == null) {
            return "未找到今日双倍双爆活动❌";
        }

        // 处理未知活动类型（新增容错）
        if (todayItem.type == -1) {
            return "今日活动格式异常，无法识别❌";
        }

        String message;
        if (todayItem.type == TYPE_FULL_DAY) {
            int consecutiveDays = getConsecutiveDays(today);
            String endDate = getEndDate(today, consecutiveDays);
            message = "✅全天双倍双爆\n并将持续到" + endDate + "\n共" + consecutiveDays + "天😊😊😊";
            dbHelper.updateDashboardContent("double_explosion_rate_notification", "全天✅");
        } else {
            dbHelper.updateDashboardContent("double_explosion_rate_notification", "限时⏳");
            String nextFullDay = findNextFullDay(today);
            String contentText = getContentText(todayItem.type);

            // 修复空行问题：确保contentText不为空
            if (contentText.isEmpty()) {
                contentText = "今日活动类型未知";
            }

            if (nextFullDay != null) {
                long daysAway = getDaysBetween(today, nextFullDay);
                message = contentText + "\n下个全天双爆日期\n" + nextFullDay + "\n还有" + daysAway + "天✊";
            } else {
                message = contentText + "\n⏳今年无更多全天双倍双爆活动";
            }
        }
        return message;
    }

    // 根据日期查找活动
    private ActivityItem findActivityByDate(String date) {
        for (ActivityItem item : activityList) {
            if (item.date.equals(date)) {
                return item;
            }
        }
        return null;
    }

    // 根据content内容确定活动类型
    private int getActivityType(String content) {
        // 去除首尾空格和换行符，增强兼容性（修复核心问题）
        String trimmedContent = content.trim().replace("\n", "").replace("\r", "");

        // 与重构前严格匹配，但增加trim处理
        if (trimmedContent.equals("00:00-23:59开启双倍双爆。")) {
            return TYPE_FULL_DAY;
        } else if (trimmedContent.equals("12:00-14:00开启双倍爆率。17:30-19:30开启双倍双爆。")) {
            return TYPE_PARTIAL_DAY;
        } else if (trimmedContent.contains("00:00-23:59开启经验双倍。")) {
            return TYPE_EXP_FULL_DAY;
        }

        // 打印未知内容，便于调试
        Log.w("ActivityQuery", "未知活动类型，内容：" + trimmedContent);
        return -1;
    }

    // 获取活动内容文本
    private String getContentText(int type) {
        if (type == TYPE_PARTIAL_DAY) {
            return "⏳限时双倍双爆\n今日12:00-14:00\n开启双倍爆率\n今日17:30-19:30\n开启双倍双爆";
        } else if (type == TYPE_EXP_FULL_DAY) {
            return "⏳限时双倍双爆\n今日00:00-23:59\n开启经验双倍\n今日12:00-14:00\n开启双倍爆率\n今日17:30-19:30\n开启双倍双爆";
        }
        return "";
    }

    // 计算连续天数
    private int getConsecutiveDays(String startDate) {
        int index = findActivityIndexByDate(startDate);
        if (index == -1) return 0;

        int days = 1;
        int type = activityList.get(index).type;

        for (int i = index + 1; i < activityList.size(); i++) {
            if (activityList.get(i).type == type) {
                days++;
            } else {
                break;
            }
        }

        return days;
    }

    // 查找活动索引
    private int findActivityIndexByDate(String date) {
        for (int i = 0; i < activityList.size(); i++) {
            if (activityList.get(i).date.equals(date)) {
                return i;
            }
        }
        return -1;
    }

    // 获取连续天数的结束日期
    private String getEndDate(String startDate, int days) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(startDate));
            calendar.add(Calendar.DAY_OF_MONTH, days - 1);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(calendar.getTime());
        } catch (Exception e) {
            return startDate;
        }
    }

    // 查找下一次全天双倍活动
    private String findNextFullDay(String startDate) {
        int index = findActivityIndexByDate(startDate);
        if (index == -1) return null;

        for (int i = index + 1; i < activityList.size(); i++) {
            if (activityList.get(i).type == TYPE_FULL_DAY) {
                return activityList.get(i).date;
            }
        }
        return null;
    }

    // 计算两天之间的天数
    private long getDaysBetween(String startDateStr, String endDateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date start = dateFormat.parse(startDateStr);
            Date end = dateFormat.parse(endDateStr);
            long diff = 0;
            if (end != null && start != null) {
                diff = end.getTime() - start.getTime();
            }
            return diff / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    // 检查网络连接
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(
                        connectivityManager.getActiveNetwork());
                return capabilities != null && (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
