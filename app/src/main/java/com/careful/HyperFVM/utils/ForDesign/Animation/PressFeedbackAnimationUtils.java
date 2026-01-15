package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;

public class PressFeedbackAnimationUtils {
    // 动画参数
    private static final long SINK_DURATION = 200;
    private static final long TILT_DURATION = 200;
    private static final float PRESSED_SCALE = 0.94f;
    private static final float NORMAL_SCALE = 1.0f;
    private static final float TILT_ANGLE = 2.0f;

    // 反馈类型枚举（与Miuix的PressFeedbackType对应）
    public enum PressFeedbackType {
        SINK, TILT, NONE
    }

    // 保存每个View的按压状态（避免多View冲突）
    private static class TiltState {
        float pivotX;          // 按压时的支点X
        float pivotY;          // 按压时的支点Y
        float rotateX;         // 目标旋转X角度
        float rotateY;         // 目标旋转Y角度
        boolean isPressed;     // 是否处于按压状态
        boolean isAnimating;   // 是否正在执行动画（状态锁）
        Animator currentAnim;  // 当前执行的动画引用
    }
    private static final java.util.WeakHashMap<View, TiltState> tiltStateMap = new java.util.WeakHashMap<>();

    /**
     * 执行按压反馈动画（统一入口）
     * @param v 目标View
     * @param event 触摸事件（倾斜效果需要）
     * @param isPressed 是否按压状态
     * @param type 反馈类型
     */
    public static void playPressFeedbackAnimation(
            View v, MotionEvent event, boolean isPressed, PressFeedbackType type) {

        if (type == PressFeedbackType.NONE) return;

        // 清除未完成动画（强制终止，避免动画叠加）
        cancelCurrentAnimation(v);

        if (type == PressFeedbackType.SINK) {
            playSinkAnimation(v, isPressed);
        } else if (type == PressFeedbackType.TILT) {
            playTiltAnimation(v, event, isPressed);
        }
    }

    // 下沉动画（原缩放效果优化版）
    private static void playSinkAnimation(View v, boolean isPressed) {
        float targetScale = isPressed ? PRESSED_SCALE : NORMAL_SCALE;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", v.getScaleX(), targetScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", v.getScaleY(), targetScale);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(SINK_DURATION);
        set.setInterpolator(isPressed
                ? new AccelerateInterpolator(0.8f)
                : new OvershootInterpolator(0.8f)); // 恢复时带轻微回弹
        set.start();
    }

    // 倾斜动画（终极版：解决快速点按抽搐）
    private static void playTiltAnimation(View v, MotionEvent event, boolean isPressed) {
        // 初始化当前View的状态记录
        TiltState state = tiltStateMap.get(v);
        if (state == null) {
            state = new TiltState();
            // 初始状态：中心支点、无旋转、无动画
            state.pivotX = v.getWidth() / 2f;
            state.pivotY = v.getHeight() / 2f;
            state.rotateX = 0f;
            state.rotateY = 0f;
            state.isPressed = false;
            state.isAnimating = false;
            state.currentAnim = null;
            tiltStateMap.put(v, state);
        }

        // 标记动画开始
        state.isAnimating = true;

        // 增强3D透视效果（匹配Miuix的cameraDistance）
        v.setCameraDistance(v.getResources().getDisplayMetrics().density * 12 * 72);

        if (isPressed) {
            // ========== 按压逻辑：强制同步状态 + 执行倾斜动画 ==========
            int width = v.getWidth();
            int height = v.getHeight();
            float x = event.getX();
            float y = event.getY();

            // 1. 强制同步当前View的真实状态（核心：解决快速点按状态不同步）
            state.pivotX = x < width / 2f ? width : 0f;
            state.pivotY = y < height / 2f ? height : 0f;
            state.rotateX = y < height / 2f ? TILT_ANGLE : -TILT_ANGLE;
            state.rotateY = x < width / 2f ? -TILT_ANGLE : TILT_ANGLE;
            state.isPressed = true;

            // 2. 立即应用支点（避免延迟导致的跳变）
            v.setPivotX(state.pivotX);
            v.setPivotY(state.pivotY);

            // 3. 执行按压动画（从当前真实角度→目标角度，无缝衔接）
            ObjectAnimator rotateXAnim = ObjectAnimator.ofFloat(v, "rotationX", v.getRotationX(), state.rotateX);
            ObjectAnimator rotateYAnim = ObjectAnimator.ofFloat(v, "rotationY", v.getRotationY(), state.rotateY);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(rotateXAnim, rotateYAnim);
            set.setDuration(TILT_DURATION);
            set.setInterpolator(new AccelerateInterpolator(0.6f));

            // 保存当前动画引用
            state.currentAnim = set;

            TiltState finalState1 = state;
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    finalState1.isAnimating = false;
                    finalState1.currentAnim = null;
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });

            set.start();

        } else {
            // ========== 恢复逻辑：强制基于当前状态平滑过渡 ==========
            if (!state.isPressed && !state.isAnimating) return; // 无按压/动画状态，直接返回

            // 1. 强制保留当前支点（即使快速点按，也不突变）
            v.setPivotX(state.pivotX);
            v.setPivotY(state.pivotY);

            // 2. 执行反向动画（从当前真实角度→0，无缝衔接）
            ObjectAnimator rotateXAnim = ObjectAnimator.ofFloat(v, "rotationX", v.getRotationX(), 0f);
            ObjectAnimator rotateYAnim = ObjectAnimator.ofFloat(v, "rotationY", v.getRotationY(), 0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(rotateXAnim, rotateYAnim);
            set.setDuration(TILT_DURATION);
            set.setInterpolator(new OvershootInterpolator(0.6f));

            // 保存当前动画引用
            state.currentAnim = set;
            TiltState finalState = state;

            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    // 动画结束后再重置支点和状态（核心：避免快速点按干扰）
                    v.setPivotX(v.getWidth() / 2f);
                    v.setPivotY(v.getHeight() / 2f);
                    finalState.isPressed = false;
                    finalState.isAnimating = false;
                    finalState.rotateX = 0f;
                    finalState.rotateY = 0f;
                    finalState.currentAnim = null;
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                    onAnimationEnd(animation); // 取消时也重置
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });

            set.start();
        }
    }

    /**
     * 强制取消当前View的所有动画（解决快速点按动画叠加）
     */
    private static void cancelCurrentAnimation(View v) {
        TiltState state = tiltStateMap.get(v);
        if (state != null && state.currentAnim != null) {
            state.currentAnim.cancel();
            state.currentAnim = null;
        }
        // 强制终止View的所有属性动画
        v.animate().cancel();
    }
}