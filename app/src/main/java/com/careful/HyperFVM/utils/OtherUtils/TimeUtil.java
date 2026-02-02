package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * 时间相关的工具类
 */
public class TimeUtil {
    private static final String TAG = "TimeUtil";

    // 统一的日期格式
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    // 日期类
    private static final Calendar calendar = Calendar.getInstance();

    /**
     * 获取当前日期（格式：yyyy-MM-dd）
     * @return 格式化后的当前日期字符串
     */
    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.CHINA);
        return sdf.format(calendar.getTime());
    }

    /**
     * 将时间戳转换为yyyy-MM-dd格式的日期字符串
     * @param timeStamp 时间戳（秒级，需确认XML中时间戳单位）
     * @return 格式化后的日期字符串
     * @throws ParseException 解析异常
     */
    public static String convertTimeStampToDate(long timeStamp) throws ParseException {
        // 若时间戳为秒级，需转换为毫秒级
        Date date = new Date(timeStamp * 1000);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault()); // 适配本地时区
        return sdf.format(date);
    }

    /**
     * 计算两个日期之间的天数（包含开始和结束日期）
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 天数差
     */
    public static int calculateDaysBetween(String startDate, String endDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // 使用Calendar计算天数差（避免时区问题）
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(Objects.requireNonNull(start));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(Objects.requireNonNull(end));

            long diffInMillis = calEnd.getTimeInMillis() - calStart.getTimeInMillis();
            long days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            // 包含首尾日期，天数+1
            return (int) days + 1;
        } catch (ParseException e) {
            Log.e(TAG, "transformStringToDate: 解析错误：" + "startDate = " + startDate + "，endDate = " + endDate);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取今天是周几
     * SUNDAY = 1
     * MONDAY = 2
     * TUESDAY = 3
     * WEDNESDAY = 4
     * THURSDAY = 5
     * FRIDAY = 6
     * SATURDAY = 7
     */
    public static int getWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取今天几号
     */
    public static int getCurrentDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当月一共多少天
     */
    public static int getMaxDayOfMonth() {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取今天几月
     */
    public static int getCurrentMonth() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取今天是哪一年
     */
    public static int getCurrentYear() {
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 提供年月日，返回yyyy-MM-dd格式的日期
     * @param year 年
     * @param month 月
     * @param day 日
     */
    public static String generateFormattedDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    /**
     * 将String类型的日期转换成Date类型，方便计算前后关系
     * @param strDate 以字符串形式表示的日期，也是yyyy-MM-dd格式
     */
    public static Date transformStringToDate(String strDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault()); // 保持和其他日期处理一致的时区
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            Log.e(TAG, "transformStringToDate: 解析错误：" + "strDate = " + strDate);
            throw new RuntimeException(e);
        }
    }
}
