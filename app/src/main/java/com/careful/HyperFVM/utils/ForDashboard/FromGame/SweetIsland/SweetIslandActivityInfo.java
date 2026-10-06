package com.careful.HyperFVM.utils.ForDashboard.FromGame.SweetIsland;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 甜蜜岛/走马观花开放场次数据类
 * 对应sweetisland.xml中systemOpen（甜蜜岛）与tjbz_Open第一个map（走马观花）包裹的<open>条目：
 * startTime：场次开始时间戳（秒）
 * endTime：场次结束时间戳（秒）
 */
public class SweetIslandActivityInfo {

    // 时间显示固定使用北京时间，不随设备时区变化（时间戳本身即按此约定生成）
    private static final String TIME_ZONE_ID = "GMT+8";

    private final long startTime;
    private final long endTime;

    public SweetIslandActivityInfo(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    /**
     * 场次时间显示文本，格式：yyyy-MM-dd HH:mm - HH:mm
     * startTime与endTime的日期一定相同（场次持续时间很短），因此结束时间只显示时分
     *
     * @return 格式化后的场次时间区间文本
     */
    public String formatTimeRange() {
        SimpleDateFormat beginFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        beginFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SimpleDateFormat endFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        endFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return beginFormat.format(new Date(startTime * 1000L))
                + " - " + endFormat.format(new Date(endTime * 1000L));
    }

    /**
     * 场次开始日期显示文本，格式：yyyy-MM-dd
     *
     * @return 格式化后的场次开始日期文本
     */
    public String formatDate() {
        SimpleDateFormat beginFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        beginFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return beginFormat.format(new Date(startTime * 1000L));
    }

    /**
     * 场次时段显示文本（仅时分），格式：HH:mm - HH:mm
     * 用于当天有活动时展示具体开放时段
     *
     * @return 格式化后的场次时段文本
     */
    public String formatTimeOfDayRange() {
        SimpleDateFormat beginFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        beginFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SimpleDateFormat endFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        endFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return beginFormat.format(new Date(startTime * 1000L))
                + " - " + endFormat.format(new Date(endTime * 1000L));
    }

    /**
     * 北京时间今天的日期，格式：yyyy-MM-dd
     * 用于按自然日判断当天是否有开放场次
     *
     * @return 今天的日期文本
     */
    public static String formatToday() {
        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        todayFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return todayFormat.format(new Date());
    }

    /**
     * 序列化场次列表为字符串（聚合器整条数据链只支持Map<String,String>传输）
     * 格式：startTime,endTime，多条以"|"拼接
     *
     * @param activityInfoList 场次列表
     * @return 序列化后的字符串，列表为空返回空串
     */
    public static String serialize(List<SweetIslandActivityInfo> activityInfoList) {
        if (activityInfoList == null || activityInfoList.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (SweetIslandActivityInfo activityInfo : activityInfoList) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(activityInfo.startTime)
                    .append(",").append(activityInfo.endTime);
        }
        return builder.toString();
    }

    /**
     * 反序列化场次列表字符串
     * 超时兜底等场景下入参可能为null或空串，此时返回空列表
     *
     * @param serialized 序列化的场次字符串
     * @return 场次列表（解析失败的条目会被跳过）
     */
    public static List<SweetIslandActivityInfo> deserialize(String serialized) {
        List<SweetIslandActivityInfo> activityInfoList = new ArrayList<>();
        if (serialized == null || serialized.trim().isEmpty()) {
            return activityInfoList;
        }

        for (String item : serialized.split("\\|")) {
            String[] fields = item.split(",", -1);
            if (fields.length != 2) {
                continue;
            }
            try {
                activityInfoList.add(new SweetIslandActivityInfo(
                        Long.parseLong(fields[0].trim()),
                        Long.parseLong(fields[1].trim())
                ));
            } catch (NumberFormatException e) {
                // 单条格式异常只跳过该条，不影响其余场次展示
            }
        }
        return activityInfoList;
    }
}
