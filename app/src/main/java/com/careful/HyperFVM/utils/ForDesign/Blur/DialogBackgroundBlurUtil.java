package com.careful.HyperFVM.utils.ForDesign.Blur;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;

public class DialogBackgroundBlurUtil {

    /**
     * 给 MaterialAlertDialog 添加窗口级背景模糊
     *
     * @param dialog     目标 Dialog（需先通过 Builder 创建但未 show()）
     * @param blurRadius 模糊半径
     */
    public static void setDialogBackgroundBlur(Dialog dialog, int blurRadius) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 添加压暗，增强模糊对比
        window.setDimAmount(0.0f); // 压暗程度（0=完全透明，1=完全黑）
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND); // 启用模糊

        // 设置模糊程度
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.setBlurBehindRadius(0); // 初始为0
        window.setAttributes(layoutParams);

        // 设置渐变模糊动画
        @SuppressLint("Recycle") ValueAnimator animator = ValueAnimator.ofInt(0, blurRadius);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            int currentRadius = (int) (blurRadius * fraction);
            float currentDim = 0.5f * fraction;

            layoutParams.setBlurBehindRadius(currentRadius);
            layoutParams.dimAmount = currentDim;
            window.setAttributes(layoutParams);
        });
        animator.start();
    }
}
