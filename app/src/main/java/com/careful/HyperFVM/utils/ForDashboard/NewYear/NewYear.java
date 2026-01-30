package com.careful.HyperFVM.utils.ForDashboard.NewYear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewYear {
    private static String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/new_year.xml";
    private static final String TAG = "NewYear";
    private final OkHttpClient client;
    private final DBHelper dbHelper;
    private final Handler mainHandler;

    public NewYear(Context context) {
        this.dbHelper = new DBHelper(context);
        // 禁用OkHttp缓存
        this.client = new OkHttpClient.Builder()
                .cache(null)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // 启动任务：获取XML并处理
    public void execute() {
        fetchXmlContent();
    }

    // 从网络获取XML内容
    private void fetchXmlContent() {
        Random random = new Random();
        int num = 1 + random.nextInt(1000000000);

        XML_URL = XML_URL + "?" + num;

        Request request = new Request.Builder()
                .url(XML_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "网络请求失败：" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String xmlContent = response.body().string();
                    parseXmlAndSave(xmlContent);
                } else {
                    Log.e(TAG, "服务器响应失败，响应码：" + response.code());
                }
            }
        });
    }

    // 解析XML并保存数据到数据库
    @SuppressLint("DefaultLocale")
    private void parseXmlAndSave(String xmlContent) {
        // 提取第二个stime属性值
        String stimeValue = extractSecondStime(xmlContent);
        if (stimeValue == null) {
            Log.e(TAG, "未找到第二个stime属性");
            mainHandler.post(() -> {
                dbHelper.updateDashboardContent("new_year", "数据解析失败❌");
                dbHelper.updateDashboardContent("new_year_notification", "失败❌");
            });
            return;
        }

        // 从stime中提取开始和结束日期（仅保留月日）
        String[] dates = extractStartAndEndDates(stimeValue);
        if (dates == null || dates.length != 2) {
            Log.e(TAG, "日期格式解析失败，stime值：" + stimeValue);
            mainHandler.post(() -> {
                dbHelper.updateDashboardContent("new_year", "日期格式错误❌");
                dbHelper.updateDashboardContent("new_year_notification", "失败❌");
            });
            return;
        }

        // 处理日期为"MM月dd日"格式（去除时间）
        String startDateDisplay = dates[0]; // 如"6月26日"
        String endDateDisplay = dates[1];   // 如"7月17日"

        // 转换为带年份的完整日期（用于计算）
        String[] fullDates = completeDateWithYear(startDateDisplay, endDateDisplay);
        String startDateFull = fullDates[0];
        String endDateFull = fullDates[1];

        // 计算当前在活动中的天数
        int dayOfEvent = calculateDayOfEvent(startDateFull, endDateFull);

        // 计算活动一共几天
        int lengthOfEvent = calculateLengthOfEvent(startDateFull, endDateFull);

        // 生成显示文本（仅展示月日）
        String content;
        if (dayOfEvent == 0) {
            content = String.format("开始：%s\n结束：%s\n活动尚未开始⏳", startDateDisplay, endDateDisplay);
            dbHelper.updateDashboardContent("new_year_notification", "未开始⏳");
        } else if (dayOfEvent == -1) {
            content = String.format("开始：%s\n结束：%s\n本期活动已结束⏳", startDateDisplay, endDateDisplay);
            dbHelper.updateDashboardContent("new_year_notification", "结束⏳");
        } else if (dayOfEvent < -1) {
            content = String.format("开始：%s\n结束：%s\n日期计算异常❌", startDateDisplay, endDateDisplay);
            dbHelper.updateDashboardContent("new_year_notification", "日期错误❌");
        } else {
            //content = String.format("开始：%s\n结束：%s\n进度：(%d/" + "%d)✊",startDateDisplay, endDateDisplay, dayOfEvent, lengthOfEvent);
            content = String.format("%d/%d", dayOfEvent, lengthOfEvent);
            dbHelper.updateDashboardContent("new_year_notification", "(" + dayOfEvent + "/" + lengthOfEvent + ")✊");
            dbHelper.updateDashboardContent("new_year_emoji", "✊");
        }

        mainHandler.post(() -> dbHelper.updateDashboardContent("new_year", content));
    }

    // 提取XML中第二个stime属性的值
    private String extractSecondStime(String xmlContent) {
        Pattern pattern = Pattern.compile("stime=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(xmlContent);
        int count = 0;

        while (matcher.find()) {
            count++;
            if (count == 2) {
                return matcher.group(1);
            }
        }
        return null;
    }

    // 从stime文本中提取开始和结束日期（仅保留月日，去除时间）
    private String[] extractStartAndEndDates(String stimeText) {
        // 匹配"X月X日"格式（忽略后续时间）
        Pattern datePattern = Pattern.compile("(\\d+月\\d+日)");
        Matcher matcher = datePattern.matcher(stimeText);
        String[] dates = new String[2];
        int index = 0;

        while (matcher.find() && index < 2) {
            dates[index] = Objects.requireNonNull(matcher.group(1)).trim();
            index++;
        }

        return index == 2 ? dates : null;
    }

    // 为日期补全年份（用于计算，不影响显示）
    // 为日期补全年份（处理跨年情况）
    private String[] completeDateWithYear(String startMonthDay, String endMonthDay) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("MM月dd日", Locale.CHINA);
            SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);

            Date startDate = sdfInput.parse(startMonthDay);
            Date endDate = sdfInput.parse(endMonthDay);

            if (startDate == null || endDate == null) {
                return new String[]{startMonthDay, endMonthDay};
            }

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);

            Calendar currentCal = Calendar.getInstance();
            int currentYear = currentCal.get(Calendar.YEAR);

            // 判断是否是跨年活动（开始月份大于结束月份）
            boolean isCrossYear = startCal.get(Calendar.MONTH) > endCal.get(Calendar.MONTH);

            if (isCrossYear) {
                // 跨年活动
                // 当前月份小于等于结束月份，说明还在新年（比如1月1日 - 1月8日期间）
                // 那么开始日期应该是去年，结束日期是今年
                int currentMonth = currentCal.get(Calendar.MONTH);
                int endMonth = endCal.get(Calendar.MONTH);

                if (currentMonth <= endMonth) {
                    // 当前在活动后半段（新年部分）
                    startCal.set(Calendar.YEAR, currentYear - 1);
                    endCal.set(Calendar.YEAR, currentYear);
                } else {
                    // 当前在活动前半段（年底部分）
                    startCal.set(Calendar.YEAR, currentYear);
                    endCal.set(Calendar.YEAR, currentYear + 1);
                }
            } else {
                // 非跨年活动，简单处理为同一年
                startCal.set(Calendar.YEAR, currentYear);
                endCal.set(Calendar.YEAR, currentYear);
            }

            return new String[]{
                    sdfOutput.format(startCal.getTime()),
                    sdfOutput.format(endCal.getTime())
            };
        } catch (ParseException e) {
            Log.e(TAG, "日期补全失败：" + e.getMessage());
            return new String[]{startMonthDay, endMonthDay};
        }
    }

    // 计算当前日期在活动时间段中的天数
    private int calculateDayOfEvent(String startDateFull, String endDateFull) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            Date startDate = sdf.parse(startDateFull);
            Date endDate = sdf.parse(endDateFull);

            if (startDate == null || endDate == null) {
                return -2; // 日期解析失败
            }

            // 仅保留年月日进行比较（忽略时间）
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            clearTime(startCal);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            clearTime(endCal);

            Calendar currentCal = Calendar.getInstance();
            clearTime(currentCal);

            long startTime = startCal.getTimeInMillis();
            long endTime = endCal.getTimeInMillis();
            long currentTime = currentCal.getTimeInMillis();

            if (currentTime < startTime) {
                return 0; // 活动未开始
            } else if (currentTime > endTime) {
                return -1; // 活动已结束
            }

            // 计算间隔天数（起始日为第1天）
            long intervalMs = currentTime - startTime;
            return (int) (intervalMs / (24 * 60 * 60 * 1000) + 1);
        } catch (ParseException e) {
            Log.e(TAG, "计算活动天数失败：" + e.getMessage());
            return -2; // 计算异常
        }
    }

    // 计算两个日期之间的天数
    private int calculateLengthOfEvent(String startDateFull, String endDateFull) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            Date startDate = sdf.parse(startDateFull);
            Date endDate = sdf.parse(endDateFull);

            if (startDate == null || endDate == null) {
                return -1; // 日期解析失败
            }

            // 仅保留年月日进行比较（忽略时间）
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            clearTime(startCal);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            clearTime(endCal);

            Calendar currentCal = Calendar.getInstance();
            clearTime(currentCal);

            long startTime = startCal.getTimeInMillis();
            long endTime = endCal.getTimeInMillis();

            // 计算间隔天数（起始日为第1天）
            long intervalMs = endTime - startTime;
            return (int) (intervalMs / (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            Log.e(TAG, "计算活动天数失败：" + e.getMessage());
            return -1; // 计算异常
        }
    }

    // 清除Calendar中的时间部分（仅保留年月日）
    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
