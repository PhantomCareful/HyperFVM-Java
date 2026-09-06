package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.view.MotionEvent;
import android.view.View;

public class PressFeedbackAnimationHelper {
    /**
     * 给按钮和卡片添加按压反馈动画
     * 注意：目标 View 必须可点击（clickable=true），否则自身无法消费 ACTION_DOWN、
     * 收不到 ACTION_UP/ACTION_CANCEL，恢复动画不会触发（卡片会一直保持按压姿态）
     * @return 是否拦截触摸事件
     */
    public static boolean setPressFeedbackAnimation(View v, MotionEvent event, PressFeedbackAnimationUtils.PressFeedbackType type) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 传入事件对象用于计算倾斜角度
                PressFeedbackAnimationUtils.playPressFeedbackAnimation(
                        v, event, true, type);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                PressFeedbackAnimationUtils.playPressFeedbackAnimation(
                        v, event, false, type);
                break;
        }
        return false; // 保持原有事件传递逻辑
    }
}
