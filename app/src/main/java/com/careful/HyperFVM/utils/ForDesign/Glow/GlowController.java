package com.careful.HyperFVM.utils.ForDesign.Glow;

import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * OOBE 欢迎页 glow 背景的帧驱动（移植自 HyperCeiler provision renderengine.GlowController）。
 * {@link View#postOnAnimation} 对齐 vsync 一帧一推进：uTime（ping-pong 0↔120s，防 float 精度
 * 漂移）、uResolution，把 {@link GlowPainter} 的 RenderEffect 挂到目标 View 上。
 * （HC 原版用 postDelayed(16) 固定间隔，与 vsync 存在相位漂移，帧节奏会抖）
 * 光圈圆心经 {@link #setCircleYOffsetWithView(View, View)} 对齐 logo 实际位置。
 */
@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class GlowController implements Runnable {

    GlowPainter mGlowPainter;

    private float mTime;
    private float mDeltaTime;
    private float mTimeDirection = 1.0f;

    private long mLastGlobalTime;

    // 帧循环运行标记：防止 warmUp 后 start 重复 post、running 中再次 start 重复刷新基准时刻
    private boolean mRunning;

    private final View mTarget;

    public GlowController(View target) {
        mTarget = target;
    }

    /**
     * 预热：提前创建 painter 并把 RenderEffect 挂上目标 View，把 AGSL 解析校验
     * （构造期）与 GPU shader 管线编译（首帧 draw）挪到首帧之前。
     * 否则编译顿挫会落在开场光圈扩散期（首个 run() 才挂 effect 的话），表现为
     * "扩散过程中卡一下"。time=0 时 mask=0（uMaskDelay 0.3 未到），输出全黑，
     * 与黑底无视觉差；后续 {@link #start(boolean)} 仅补发帧循环。
     */
    public void warmUp() {
        if (mGlowPainter == null) {
            mGlowPainter = new GlowPainter(mTarget.getContext());
            resetTime();
            mTarget.setRenderEffect(mGlowPainter.getRenderEffect());
        }
    }

    public void start(boolean z) {
        if (mGlowPainter == null) {
            mGlowPainter = new GlowPainter(mTarget.getContext());
            resetTime();
        }
        mGlowPainter.needAdmission(z);
        if (!mRunning) {
            // 首帧前刷新基准时刻：warmUp 创建的 painter 距 start 可能已隔若干帧，
            // 不刷新会让首帧 delta 跳变（uTime 直接冲过开场光圈与 mask）
            mLastGlobalTime = System.nanoTime();
            mRunning = true;
            mTarget.post(this);
        }
    }

    @Override
    public void run() {
        if (mGlowPainter != null) {
            tickPingPong();
            // 临时诊断：帧间隔 >33ms 时记录（定位首启动画期卡顿根因，定位后删除）
            if (mDeltaTime > 0.033f) {
                Log.w("OOBEDiag", "glow frame gap " + (int) (mDeltaTime * 1000)
                        + "ms at uTime=" + mTime);
            }
            mGlowPainter.setAnimTime(mTime);
            mGlowPainter.setResolution(mTarget.getWidth(), mTarget.getHeight());
            mTarget.setRenderEffect(mGlowPainter.getRenderEffect());
            mTarget.postOnAnimation(this);
        }
    }

    private void tickPingPong() {
        long jNanoTime = System.nanoTime();
        mDeltaTime = (float) ((jNanoTime - mLastGlobalTime) * 1.0E-9d);
        mTime = mTime + (mDeltaTime * mTimeDirection);
        if (mTimeDirection > 0.0f) {
            if (mTime >= 120.0f) {
                mTimeDirection = -1.0f;
            }
        } else if (mTime <= 2.0f) {
            mTimeDirection = 1.0f;
        }
        mLastGlobalTime = jNanoTime;
    }

    public void resetTime() {
        mLastGlobalTime = System.nanoTime();
        mTime = 0.0f;
        mTimeDirection = 1.0f;
    }

    public void setCircleYOffset(float f) {
        if (mGlowPainter != null) {
            mGlowPainter.setCircleYOffset(f);
        }
    }

    /**
     * 把光圈圆心纵向对齐到 anchor（logo）在 render 容器中的实际位置：
     * offset = (容器高/2 - anchor 中心 y) / 容器高（shader 中 center = 0.5 - offset）。
     */
    public void setCircleYOffsetWithView(View view, View view2) {
        if (mGlowPainter == null || view == null || view2 == null) {
            return;
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int height = iArr[1] + (view.getHeight() / 2);
        float height2 = view2.getHeight();
        setCircleYOffset(((height2 / 2.0f) - height) / height2);
    }

    public void stop() {
        if (mGlowPainter != null) {
            mRunning = false;
            mTarget.removeCallbacks(this);
            mGlowPainter = null;
            mTarget.setRenderEffect(null);
        }
    }
}
