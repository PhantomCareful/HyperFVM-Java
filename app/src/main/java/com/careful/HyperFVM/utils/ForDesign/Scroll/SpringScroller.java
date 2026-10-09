package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.view.animation.AnimationUtils;

/**
 * 弹簧滚动物理步进器（复刻 miuix springback 的 fan.springback.view.SpringScroller）。
 * <p>
 * 每帧流程：计算 dt（上限 0.016s）→ SpringOperator 更新速度 → 位置步进
 * → isAtEquilibrium 判定平衡（到位/越过终点）→ 吸附到终点，下一帧 finished。
 */
public class SpringScroller {

    /** 单帧最大时间步长（s），与原版 MAX_DELTA_TIME 一致 */
    private static final float MAX_DELTA_TIME = 0.016f;
    /** 到位判定阈值（px），与原版 VALUE_THRESHOLD 一致 */
    private static final double VALUE_THRESHOLD = 1.0;

    private static final int ORIENTATION_HORIZONTAL = 1;
    private static final int ORIENTATION_VERTICAL = 2;

    private SpringOperator mSpringOperator;
    private boolean mFinished = true;
    private boolean mLastStep = false;
    private int mFirstStep = 0;

    private double mStartX;
    private double mEndX;
    private double mCurrX;
    private double mStartY;
    private double mEndY;
    private double mCurrY;
    private double mOriginStartX;
    private double mOriginStartY;
    private double mVelocity;
    private double mOriginVelocity;
    private int mOrientation;
    private long mStartTime;

    /**
     * 启动一次弹簧滚动：从 (startX, startY) 弹到 (endX, endY)。
     *
     * @param velocity    初速度（touch 松手路径传 0，fling 路径带甩动速度）
     * @param orientation 2 = 垂直，1 = 水平
     * @param flag        true 时强制使用更硬的弹簧（period 0.4）
     */
    public void scrollByFling(float startX, float endX, float startY, float endY,
                              float velocity, int orientation, boolean flag) {
        mFinished = false;
        mLastStep = false;
        mStartX = startX;
        mOriginStartX = startX;
        mEndX = endX;
        mStartY = startY;
        mOriginStartY = startY;
        mCurrY = (int) startY;
        mEndY = endY;
        mVelocity = velocity;
        mOriginVelocity = velocity;
        // 与原版一致：快速甩动（|v|>5000 且非 flag 调用）用更软的 period=0.55，
        // 常规回位用更硬的 period=0.4
        mSpringOperator = (Math.abs(velocity) > 5000 && !flag)
                ? new SpringOperator(1.0f, 0.55f)
                : new SpringOperator(1.0f, 0.4f);
        mOrientation = orientation;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    /**
     * 推进一步。返回 true 表示本帧产生了新位置（用 getCurrX/getCurrY 读取）。
     */
    public boolean computeScrollOffset() {
        if (mSpringOperator == null || mFinished) {
            return false;
        }
        if (mFirstStep != 0) {
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                mCurrX = mStartX = mFirstStep;
            } else {
                mCurrY = mStartY = mFirstStep;
            }
            mFirstStep = 0;
            return true;
        }
        if (mLastStep) {
            mFinished = true;
            return true;
        }

        long now = AnimationUtils.currentAnimationTimeMillis();
        float dt = (now - mStartTime) / 1000f;
        dt = Math.min(dt, MAX_DELTA_TIME);
        if (dt == 0f) {
            dt = MAX_DELTA_TIME;
        }
        mStartTime = now;

        if (mOrientation == ORIENTATION_VERTICAL) {
            mVelocity = mSpringOperator.updateVelocity(mVelocity, dt, mEndY, mStartY);
            mCurrY = mStartY + dt * mVelocity;
            if (isAtEquilibrium(mCurrY, mOriginStartY, mEndY)) {
                mLastStep = true;
                mCurrY = mEndY;
            } else {
                mStartY = mCurrY;
            }
        } else {
            mVelocity = mSpringOperator.updateVelocity(mVelocity, dt, mEndX, mStartX);
            mCurrX = mStartX + dt * mVelocity;
            if (isAtEquilibrium(mCurrX, mOriginStartX, mEndX)) {
                mLastStep = true;
                mCurrX = mEndX;
            } else {
                mStartX = mCurrX;
            }
        }
        return true;
    }

    /**
     * 平衡判定（与原版 isAtEquilibrium 逐分支一致）：
     * 1. 位置越过终点（含离散步长造成的过冲）→ 到位；
     * 2. 起终点相同（纯速度回弹）且运动方向反转 → 到位；
     * 3. 距终点不足 1px → 到位。
     */
    private boolean isAtEquilibrium(double curr, double originStart, double end) {
        if (originStart < end && curr > end) {
            return true;
        }
        if (originStart > end && curr < end) {
            return true;
        }
        if (originStart == end && Math.signum(mOriginVelocity) != Math.signum(curr)) {
            return true;
        }
        return Math.abs(curr - end) < VALUE_THRESHOLD;
    }

    /** 立即停在当前位置（手指拦停回弹动画时调用），与原版 forceStop 一致 */
    public void forceStop() {
        mFinished = true;
        mFirstStep = 0;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public int getCurrX() {
        return (int) mCurrX;
    }

    public int getCurrY() {
        return (int) mCurrY;
    }

    public void setFirstStep(int firstStep) {
        mFirstStep = firstStep;
    }
}
