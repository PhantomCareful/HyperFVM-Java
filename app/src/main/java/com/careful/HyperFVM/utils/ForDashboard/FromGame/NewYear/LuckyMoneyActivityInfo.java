package com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear;

import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 抢红包场次数据类
 * 对应new_year.xml中<LuckyMoney>包裹的<li>条目：
 * beginTime/endTime：场次开始/结束时间戳（秒）
 * total：红包总金额（单位D）
 * userNum：红包数量
 * minGet/maxGet：单个红包的最低/最高金额（单位D）
 */
public class LuckyMoneyActivityInfo {

    // 时间显示固定使用北京时间，不随设备时区变化（时间戳本身即按此约定生成）
    private static final String TIME_ZONE_ID = "GMT+8";

    private final long beginTime;
    private final long endTime;
    private final int total;
    private final int userNum;
    private final int minGet;
    private final int maxGet;

    public LuckyMoneyActivityInfo(long beginTime, long endTime, int total, int userNum, int minGet, int maxGet) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.total = total;
        this.userNum = userNum;
        this.minGet = minGet;
        this.maxGet = maxGet;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    /**
     * 场次时间显示文本，格式：yyyy-MM-dd HH:mm - HH:mm
     * beginTime与endTime的日期一定相同，因此结束时间只显示时分
     *
     * @return 格式化后的场次时间区间文本
     */
    public String formatTimeRange() {
        SimpleDateFormat beginFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        beginFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SimpleDateFormat endFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        endFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return beginFormat.format(new Date(beginTime * 1000L))
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

        return beginFormat.format(new Date(beginTime * 1000L));
    }

    /**
     * 场次时段显示文本（仅时分），格式：HH:mm - HH:mm
     * 用于当天有活动时展示具体抢红包时段
     *
     * @return 格式化后的场次时段文本
     */
    public String formatTimeOfDayRange() {
        SimpleDateFormat beginFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        beginFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));
        SimpleDateFormat endFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        endFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return beginFormat.format(new Date(beginTime * 1000L))
                + " - " + endFormat.format(new Date(endTime * 1000L));
    }

    /**
     * 北京时间今天的日期，格式：yyyy-MM-dd
     * 用于按自然日判断当天是否有抢红包场次
     *
     * @return 今天的日期文本
     */
    public static String formatToday() {
        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        todayFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ID));

        return todayFormat.format(new Date());
    }

    /**
     * 场次描述文本：总金额、红包数量、单个红包金额区间
     * 手机布局双行显示，PAD布局（最小宽度>600dp）单行显示
     *
     * @return 格式化后的场次描述文本
     */
    public String formatDescription() {
        String head = "共" + total + "D，" + userNum + "个红包";
        String tail = "每个红包最少" + minGet + "D，最多" + maxGet + "D";

        if (SmallestWidthUtil.getSmallestWidthDp() > 600) {
            // PAD布局：单行显示
            return head + "，" + tail;
        }
        // 手机布局：双行显示
        return head + "\n" + tail;
    }

    /**
     * 序列化场次列表为字符串（聚合器整条数据链只支持Map<String,String>传输）
     * 格式：beginTime,endTime,total,userNum,minGet,maxGet，多条以"|"拼接
     *
     * @param activityInfoList 场次列表
     * @return 序列化后的字符串，列表为空返回空串
     */
    public static String serialize(List<LuckyMoneyActivityInfo> activityInfoList) {
        if (activityInfoList == null || activityInfoList.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (LuckyMoneyActivityInfo activityInfo : activityInfoList) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(activityInfo.beginTime)
                    .append(",").append(activityInfo.endTime)
                    .append(",").append(activityInfo.total)
                    .append(",").append(activityInfo.userNum)
                    .append(",").append(activityInfo.minGet)
                    .append(",").append(activityInfo.maxGet);
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
    public static List<LuckyMoneyActivityInfo> deserialize(String serialized) {
        List<LuckyMoneyActivityInfo> activityInfoList = new ArrayList<>();
        if (serialized == null || serialized.trim().isEmpty()) {
            return activityInfoList;
        }

        for (String item : serialized.split("\\|")) {
            String[] fields = item.split(",", -1);
            if (fields.length != 6) {
                continue;
            }
            try {
                activityInfoList.add(new LuckyMoneyActivityInfo(
                        Long.parseLong(fields[0].trim()),
                        Long.parseLong(fields[1].trim()),
                        Integer.parseInt(fields[2].trim()),
                        Integer.parseInt(fields[3].trim()),
                        Integer.parseInt(fields[4].trim()),
                        Integer.parseInt(fields[5].trim())
                ));
            } catch (NumberFormatException e) {
                // 单条格式异常只跳过该条，不影响其余场次展示
            }
        }
        return activityInfoList;
    }
}
