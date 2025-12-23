package com.careful.HyperFVM.utils.ForDashboard.EveryMonthAndEveryWeek;

import java.util.Calendar;

public class EveryMonthAndEveryWeek {
    private Calendar calendar;

    // （1）判断当前是否为周三
    public boolean isWednesday() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY;
    }

    // （2）判断当前是否为周四
    public boolean isThursday() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY;
    }

    // （3）每日通知
    public String dailyNotifications() {
        int currentDayOfMonth = getCurrentDayOfMonth();
        if (currentDayOfMonth >= 1 && currentDayOfMonth <= 24) {
            return "记得每天都要签到\n本月进度\n(" + currentDayOfMonth + "/25)✊✊✊";
        } else {
            return "记得每天都要签到\n本月签到礼包可以领取了哦🍾🍾🍾";
        }
    }

    // （4）判断是否为当月最后一天
    public boolean isLastDayOfMonth() {
        // 获取当月最大天数（即最后一天的日期）
        calendar = Calendar.getInstance();
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 获取当前日期的天数
        int currentDay = getCurrentDayOfMonth();
        // 对比：若当前天数等于当月最大天数，则为月末最后一天
        return currentDay == maxDay;
    }

    // 辅助方法：获取当前日期是几号（用于签到提示）
    public int getCurrentDayOfMonth() {
        calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}

