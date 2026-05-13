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

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class BgEffectPainter {
    private final RuntimeShader mBgRuntimeShader;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final BgEffectDataManager mBgEffectDataManager;
    private BgEffectDataManager.BgEffectData mBgEffectData;

    private float uAnimTime = 0.0f;
    private final float[] uColors = {0.57f, 0.76f, 0.98f, 1.0f, 0.98f, 0.85f, 0.68f, 1.0f, 0.98f, 0.75f, 0.93f, 1.0f, 0.73f, 0.7f, 0.98f, 1.0f};
    private float gradientSpeed = 1.0f;

    public BgEffectPainter(Context context) {
        @Language("AGSL") String shaderSource = loadShader(context.getResources(), R.raw.bg_frag);
        mBgRuntimeShader = new RuntimeShader(shaderSource);
        mBgEffectDataManager = new BgEffectDataManager();
        mBgEffectData = mBgEffectDataManager.getAboutAppColorData(BgEffectController.DeviceType.PHONE, BgEffectController.ThemeMode.LIGHT);
        applyData(mBgEffectData);
    }

    public RenderEffect getRenderEffect() {
        return RenderEffect.createShaderEffect(mBgRuntimeShader);
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
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
        uAnimTime = 0.0f;
        gradientSpeed = mBgEffectData.gradientSpeedRest;
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailAnimalCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailAnimalCardDataColorData(themeMode);
        uAnimTime = 0.0f;
        gradientSpeed = mBgEffectData.gradientSpeedRest;
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailGoldenCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailGoldenCardDataColorData(themeMode);
        uAnimTime = 0.0f;
        gradientSpeed = mBgEffectData.gradientSpeedRest;
        applyData(mBgEffectData);
        mBgRuntimeShader.setFloatUniform("uBound", uBound);
    }

    public void setDetailFusionCardDataColorType(BgEffectController.ThemeMode themeMode, float[] uBound) {
        mBgEffectData = mBgEffectDataManager.getDetailFusionCardDataColorData(themeMode);
        uAnimTime = 0.0f;
        gradientSpeed = mBgEffectData.gradientSpeedRest;
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

    private void computeGradientColor() {
        float period = mBgEffectData.colorInterpPeriod;
        float t = (uAnimTime % (period * 4)) / period; // [0,4)
        int index = (int) t;
        float frac = t - index;
        float[] start, end;
        if (index == 0) {
            start = mBgEffectData.gradientColors2;
            end = mBgEffectData.gradientColors1;
        } else if (index == 1) {
            start = mBgEffectData.gradientColors1;
            end = mBgEffectData.gradientColors2;
        } else if (index == 2) {
            start = mBgEffectData.gradientColors2;
            end = mBgEffectData.gradientColors3;
        } else {
            start = mBgEffectData.gradientColors3;
            end = mBgEffectData.gradientColors2;
        }
        linearInterpolate(uColors, start, end, frac);
    }

    private static void linearInterpolate(float[] out, float[] a, float[] b, float t) {
        for (int i = 0; i < out.length; i++) {
            out[i] = a[i] + (b[i] - a[i]) * t;
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