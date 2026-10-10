package com.careful.HyperFVM.utils.ForDesign.BgEffect;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RenderEffect;
import android.graphics.RuntimeShader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.careful.HyperFVM.R;

import org.intellij.lang.annotations.Language;

import java.io.InputStream;
import java.util.Scanner;

import fan.animation.Folme;
import fan.animation.FolmeEase;
import fan.animation.IStateStyle;
import fan.animation.base.AnimConfig;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class BgEffectPainter {
    private final RuntimeShader mBgRuntimeShader;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final BgEffectDataManager mBgEffectDataManager;
    private final AnimConfig mAnimConfig1;
    private final AnimConfig mAnimConfig2;
    private BgEffectDataManager.BgEffectData mBgEffectData;
    private IStateStyle mStateStyle;

    private float uAnimTime = 0.0f;
    private final float[] uColors = {0.57f, 0.76f, 0.98f, 1.0f, 0.98f, 0.85f, 0.68f, 1.0f, 0.98f, 0.75f, 0.93f, 1.0f, 0.73f, 0.7f, 0.98f, 1.0f};
    // 以下两个字段由 Folme.useValue 按字段名反射读写，混淆时不可改名（见 proguard-rules.pro）
    private float colorInterpT = 0.0f;
    private float gradientSpeed = 1.0f;
    private float cycleCount = 0.0f;
    private float prevT = 0.0f;
    private float[] startColorValue;
    private float[] endColorValue;

    public BgEffectPainter(Context context) {
        @Language("AGSL") String shaderSource = loadShader(context.getResources(), R.raw.bg_frag);
        mBgRuntimeShader = new RuntimeShader(shaderSource);
        mBgEffectDataManager = new BgEffectDataManager();
        mBgEffectData = mBgEffectDataManager.getAboutAppColorData(BgEffectController.DeviceType.PHONE, BgEffectController.ThemeMode.LIGHT);
        mAnimConfig1 = new AnimConfig().setEase(FolmeEase.spring(0.9f, 1.3f));
        mAnimConfig2 = new AnimConfig().setEase(FolmeEase.spring(0.9f, 0.6f));
        mStateStyle = Folme.useValue(this);
        startColorValue = mBgEffectData.gradientColors2;
        endColorValue = mBgEffectData.gradientColors2;
        applyData(mBgEffectData);
    }

    public RenderEffect getRenderEffect() {
        return RenderEffect.createShaderEffect(mBgRuntimeShader);
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
        if (mStateStyle != null) {
            mStateStyle.cancel();
            mStateStyle.clean();
            mStateStyle = null;
        }
    }

    public void updateMaterials(float deltaTime) {
        uAnimTime += deltaTime * gradientSpeed;
        computeGradientColor();
        mBgRuntimeShader.setFloatUniform("uAnimTime", uAnimTime);
        mBgRuntimeShader.setFloatUniform("uColors", uColors);
    }

    public void setResolution(float width, float height) {
        mBgRuntimeShader.setFloatUniform("uResolution", width, height);
    }

    public void setAboutAppColorType(BgEffectController.DeviceType deviceType, BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getAboutAppColorData(deviceType, themeMode);
        resetColorAnim();
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailAnimalCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailAnimalCardDataColorData(themeMode);
        resetColorAnim();
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailGoldenCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailGoldenCardDataColorData(themeMode);
        resetColorAnim();
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailFusionCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailFusionCardDataColorData(themeMode);
        resetColorAnim();
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    private void applyData(BgEffectDataManager.BgEffectData data) {
        mBgRuntimeShader.setFloatUniform("uTranslateY", data.uTranslateY);
        mBgRuntimeShader.setFloatUniform("uPoints", data.uPoints);
        mBgRuntimeShader.setFloatUniform("uColors", uColors);
        mBgRuntimeShader.setFloatUniform("uNoiseScale", data.uNoiseScale);
        mBgRuntimeShader.setFloatUniform("uPointOffset", data.uPointOffset);
        mBgRuntimeShader.setFloatUniform("uPointRadiusMulti", data.uPointRadiusMulti);
        mBgRuntimeShader.setFloatUniform("uSaturateOffset", data.uSaturateOffset);
        mBgRuntimeShader.setFloatUniform("uShadowColorMulti", data.uShadowColorMulti);
        mBgRuntimeShader.setFloatUniform("uShadowColorOffset", data.uShadowColorOffset);
        mBgRuntimeShader.setFloatUniform("uShadowOffset", data.uShadowOffset);
        mBgRuntimeShader.setFloatUniform("uAlphaMulti", data.uAlphaMulti);
        mBgRuntimeShader.setFloatUniform("uLightOffset", data.uLightOffset);
        mBgRuntimeShader.setFloatUniform("uAlphaOffset", data.uAlphaOffset);
        mBgRuntimeShader.setFloatUniform("uShadowNoiseScale", data.uShadowNoiseScale);
    }

    // 切换配色时重置换色动画状态（对齐 HyperCeiler 的 setType 重置逻辑）
    private void resetColorAnim() {
        uAnimTime = 0.0f;
        cycleCount = 0.0f;
        prevT = 0.0f;
        colorInterpT = 0.0f;
        gradientSpeed = mBgEffectData.gradientSpeedRest;
        startColorValue = mBgEffectData.gradientColors2;
        endColorValue = mBgEffectData.gradientColors2;
        linearInterpolate(uColors, startColorValue, endColorValue, colorInterpT);
        if (mStateStyle != null) {
            mStateStyle.cancel();
            mStateStyle.setTo("colorInterpT", colorInterpT);
            mStateStyle.setTo("gradientSpeed", gradientSpeed);
        }
    }

    private void computeGradientColor() {
        // 半程检测：uAnimTime 跨过半个周期时触发换色事件，由弹簧驱动 colorInterpT 过渡
        double d = uAnimTime / mBgEffectData.colorInterpPeriod;
        float floor = (float) Math.floor((d - Math.floor(d)) * 2.0d);
        if (Math.abs(prevT - floor) > 0.5d) {
            if (cycleCount % 4.0f == 0.0f) {
                startColorValue = mBgEffectData.gradientColors2;
                endColorValue = mBgEffectData.gradientColors1;
                executeAnim();
            } else if (cycleCount % 4.0f == 1.0f) {
                startColorValue = mBgEffectData.gradientColors1;
                endColorValue = mBgEffectData.gradientColors2;
                executeAnim();
            } else if (cycleCount % 4.0f == 2.0f) {
                startColorValue = mBgEffectData.gradientColors2;
                endColorValue = mBgEffectData.gradientColors3;
                executeAnim();
            } else if (cycleCount % 4.0f == 3.0f) {
                startColorValue = mBgEffectData.gradientColors3;
                endColorValue = mBgEffectData.gradientColors2;
                executeAnim();
            }
            cycleCount += 1.0f;
        }
        prevT = floor;
        linearInterpolate(uColors, startColorValue, endColorValue, colorInterpT);
    }

    // 弹簧换色：colorInterpT 走 spring(0.9,1.3)（带过冲），gradientSpeed 先加速再回落呼吸
    private void executeAnim() {
        if (mStateStyle != null) {
            mHandler.removeCallbacksAndMessages(null);
            mStateStyle.setTo("colorInterpT", 0.0f);
            mStateStyle.to("colorInterpT", 1.0f, mAnimConfig1);
            mStateStyle.to("gradientSpeed", mBgEffectData.gradientSpeedChange, mAnimConfig1);
            mHandler.postDelayed(() -> {
                if (mStateStyle != null) {
                    mStateStyle.to("gradientSpeed", mBgEffectData.gradientSpeedRest, mAnimConfig2);
                }
            }, 300L);
        }
    }

    private static void linearInterpolate(float[] out, float[] startColor, float[] endColor, float t) {
        for (int i = 0; i < startColor.length; i++) {
            out[i] = startColor[i] + ((endColor[i] - startColor[i]) * t);
        }
    }

    private static String loadShader(Resources resources, int id) {
        try (InputStream stream = resources.openRawResource(id);
             Scanner scanner = new Scanner(stream)) {
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader", e);
        }
    }
}