package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

public class ViewAnimationUtils {
    // 动画参数（可根据手感微调）
    private static final long ANIM_DURATION = 300;
    private static final float PRESSED_SCALE = 0.9f;
    private static final float NORMAL_SCALE = 1.0f;

    /**
     * 执行按压缩放动画（从当前状态无缝过渡）
     * @param view 要动画的View（CardView/Button）
     * @param isPressed true=按压（缩小），false=恢复（放大）
     */
    public static void playPressScaleAnimation(View view, boolean isPressed) {
        // 1. 清除未完成的旧动画，避免叠加导致跳变
        view.animate().cancel();

        // 2. 获取View当前的缩放值（核心：从当前状态开始动画）
        float currentScaleX = view.getScaleX();
        float currentScaleY = view.getScaleY();

        // 3. 目标缩放值
        float targetScale = isPressed ? PRESSED_SCALE : NORMAL_SCALE;

        // 4. 创建缩放动画（从当前值→目标值）
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", currentScaleX, targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", currentScaleY, targetScale);

        // 5. 配置动画参数，提升丝滑感
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setDuration(ANIM_DURATION);
        // 按压时加速（更贴近物理按压感），恢复时减速
        animSet.setInterpolator(isPressed ? new AccelerateInterpolator(0.3f) : new DecelerateInterpolator(0.3f));

        // 6. 启动动画
        animSet.start();
    }
}