package com.careful.HyperFVM.utils.ForDashboard.FromGame.EveryMonthAndEveryWeek;

import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.util.Calendar;

public class EveryMonthAndEveryWeek {

    // （1）判断当前是否为周三
    public boolean isWednesday() {
        return TimeUtil.getWeek() == Calendar.WEDNESDAY;
    }

    // （2）判断当前是否为周四
    public boolean isThursday() {
        return TimeUtil.getWeek() == Calendar.THURSDAY;
    }

    // （3）每日签到
    public String generateDailyCheckingContentStatus() {
        int currentDayOfMonth = TimeUtil.getCurrentDay();
        if (currentDayOfMonth >= 1 && currentDayOfMonth <= 24) {
            return "月签道具：" + currentDayOfMonth + "/25";
        } else {
            return "月签礼包可领取";
        }
    }

    // （4）判断是否为当月最后一天
    public boolean isLastDayOfMonth() {
        // 对比：若当前天数等于当月最大天数，则为月末最后一天
        return TimeUtil.getCurrentDay() == TimeUtil.getMaxDayOfMonth();
    }

}

