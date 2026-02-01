package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;

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
    // 统一的日期格式
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
     * @throws ParseException 解析异常
     */
    public static int calculateDaysBetween(String startDate, String endDate) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
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
    }
}
