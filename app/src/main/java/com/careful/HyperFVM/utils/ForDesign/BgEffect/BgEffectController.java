package com.careful.HyperFVM.utils.ForDesign.BgEffect;  // 新包名

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class BgEffectController implements Runnable {
    public enum DeviceType { PHONE, TABLET }
    public enum ThemeMode { LIGHT, DARK }

    private final View mTarget;
    private BgEffectPainter mBgEffectPainter;
    private long mLastGlobalTime;
    private float mTime;
    private float mTimeDirection = 1.0f;
    private float mDeltaTime;

    private DeviceType mDeviceType = DeviceType.PHONE;
    private ThemeMode mThemeMode = ThemeMode.LIGHT;
    private final float[] mBound = {0.0f, 0.0f, 1.0f, 1.0f}; // 全屏

    public BgEffectController(View target) {
        mTarget = target;
    }

    public void setType(Context context, DeviceType deviceType, ThemeMode themeMode) {
        mDeviceType = deviceType;
        mThemeMode = themeMode;
        // 不再需要计算 bound，直接全屏
        if (mBgEffectPainter != null) {
            mBgEffectPainter.setType(deviceType, themeMode, mBound);
        }
    }

    public void start() {
        if (mBgEffectPainter == null) {
            mBgEffectPainter = new BgEffectPainter(mTarget.getContext());
            mBgEffectPainter.setType(mDeviceType, mThemeMode, mBound);
            mLastGlobalTime = System.nanoTime();
            resetTime();
            mTarget.postOnAnimation(this);
        }
    }

    @Override
    public void run() {
        if (mBgEffectPainter != null) {
            tickPingPong();
            if (mTarget.getWidth() > 0 && mTarget.getHeight() > 0) {
                mBgEffectPainter.setResolution(mTarget.getWidth(), mTarget.getHeight());
                mBgEffectPainter.updateMaterials(mDeltaTime * mTimeDirection);
                mTarget.setRenderEffect(mBgEffectPainter.getRenderEffect());
                mTarget.invalidate();
            }
            mTarget.postOnAnimation(this);
        }
    }

    private void tickPingPong() {
        long nanoTime = System.nanoTime();
        mDeltaTime = (float) ((nanoTime - mLastGlobalTime) * 1.0E-9d);
        mTime += mDeltaTime * mTimeDirection;
        if (mTimeDirection > 0.0f && mTime >= 120.0f) {
            mTimeDirection = -1.0f;
        } else if (mTimeDirection < 0.0f && mTime <= 0.0f) {
            mTimeDirection = 1.0f;
        }
        mLastGlobalTime = nanoTime;
    }

    private void resetTime() {
        mLastGlobalTime = System.nanoTime();
        mTime = 0.0f;
        mTimeDirection = 1.0f;
    }

    public void stop() {
        if (mBgEffectPainter != null) {
            mTarget.removeCallbacks(this);
            mBgEffectPainter.stop();
            mBgEffectPainter = null;
            mTarget.setRenderEffect(null);
            mTarget.invalidate();
        }
    }
}