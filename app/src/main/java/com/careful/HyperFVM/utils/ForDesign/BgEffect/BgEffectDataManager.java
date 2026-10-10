package com.careful.HyperFVM.utils.ForDesign.BgEffect;

import static com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController.DeviceType.PHONE;
import static com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController.ThemeMode.LIGHT;

import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class BgEffectDataManager {

    private final BgEffectData mPhoneLightData;
    private final BgEffectData mPhoneDarkData;
    private final BgEffectData mPadLightData;
    private final BgEffectData mPadDarkData;
    // 详情页专用副本：详情页会覆写 gradientColors*，若直接改主页实例，返回后主页配色会被污染
    private final BgEffectData mDetailLightData;
    private final BgEffectData mDetailDarkData;

    private final float[] mHyperOS2ThemeLight = {
            0.57f, 0.76f, 0.98f, 1.0f,
            0.98f, 0.85f, 0.68f, 1.0f,
            0.98f, 0.75f, 0.93f, 1.0f,
            0.73f, 0.70f, 0.98f, 1.0f,
    };

    private final float[] mHyperOS2ThemeDark = {
            0.00f, 0.31f, 0.58f, 1.0f,
            0.53f, 0.29f, 0.15f, 1.0f,
            0.46f, 0.06f, 0.27f, 1.0f,
            0.16f, 0.12f, 0.45f, 1.0f
    };

    private final float[] mGoldenCardThemeLight = {
            0.769f, 0.278f, 0.765f, 1.0f,  // #C447C3
            1.000f, 0.941f, 0.482f, 1.0f,  // #FEF07B
            0.867f, 0.267f, 0.745f, 1.0f,  // #DD44BE
            0.506f, 0.973f, 0.996f, 1.0f,  // #81F8FE
    };

    private final float[] mFusionCardThemeLight = {
            0.769f, 0.941f, 0.627f, 1.0f,   // #C4F0A0
            0.984f, 0.945f, 0.843f, 1.0f,   // #FBF1D7
            0.980f, 0.871f, 0.239f, 1.0f,   // #FADE3D
            0.553f, 0.839f, 0.549f, 1.0f,   // #8DD68C
    };

    public static class BgEffectData {
        public float uTranslateY;
        public float[] uPoints;
        public float uAlphaMulti;
        public float uNoiseScale;
        public float uPointOffset;
        public float uPointRadiusMulti;
        public float uSaturateOffset;
        public float uLightOffset;
        public float uAlphaOffset;
        public float uShadowColorMulti;
        public float uShadowColorOffset;
        public float uShadowNoiseScale;
        public float uShadowOffset;
        public float colorInterpPeriod;
        public float gradientSpeedChange;
        public float gradientSpeedRest;
        public float[] gradientColors1;
        public float[] gradientColors2;
        public float[] gradientColors3;
    }

    public BgEffectDataManager() {
        // 手机浅色
        mPhoneLightData = new BgEffectData();
        mPhoneLightData.uTranslateY = 0.0f;
        mPhoneLightData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mPhoneLightData.uAlphaMulti = 1.0f;
        mPhoneLightData.uNoiseScale = 1.5f;
        mPhoneLightData.uPointOffset = 0.2f;
        mPhoneLightData.uPointRadiusMulti = 1.0f;
        mPhoneLightData.uSaturateOffset = 0.2f;
        mPhoneLightData.uLightOffset = 0.1f;
        mPhoneLightData.uAlphaOffset = 0.5f;
        mPhoneLightData.uShadowColorMulti = 0.3f;
        mPhoneLightData.uShadowColorOffset = 0.3f;
        mPhoneLightData.uShadowNoiseScale = 5.0f;
        mPhoneLightData.uShadowOffset = 0.01f;
        mPhoneLightData.colorInterpPeriod = 5.0f;
        mPhoneLightData.gradientSpeedChange = 1.6f;
        mPhoneLightData.gradientSpeedRest = 1.05f;
        mPhoneLightData.gradientColors1 = new float[]{1.0f, 0.9f, 0.94f, 1.0f, 1.0f, 0.84f, 0.89f, 1.0f, 0.97f, 0.73f, 0.82f, 1.0f, 0.64f, 0.65f, 0.98f, 1.0f};
        mPhoneLightData.gradientColors2 = new float[]{0.58f, 0.74f, 1.0f, 1.0f, 1.0f, 0.9f, 0.93f, 1.0f, 0.74f, 0.76f, 1.0f, 1.0f, 0.97f, 0.77f, 0.84f, 1.0f};
        mPhoneLightData.gradientColors3 = new float[]{0.98f, 0.86f, 0.9f, 1.0f, 0.6f, 0.73f, 0.98f, 1.0f, 0.92f, 0.93f, 1.0f, 1.0f, 0.56f, 0.69f, 1.0f, 1.0f};

        // 平板浅色（可省略，仅保留手机参数）
        mPadLightData = new BgEffectData();
        mPadLightData.uTranslateY = 0.0f;
        mPadLightData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mPadLightData.uAlphaMulti = 1.0f;
        mPadLightData.uNoiseScale = 1.5f;
        mPadLightData.uPointOffset = 0.2f;
        mPadLightData.uPointRadiusMulti = 1.0f;
        mPadLightData.uSaturateOffset = 0.2f;
        mPadLightData.uLightOffset = 0.1f;
        mPadLightData.uAlphaOffset = 0.5f;
        mPadLightData.uShadowColorMulti = 0.3f;
        mPadLightData.uShadowColorOffset = 0.3f;
        mPadLightData.uShadowNoiseScale = 5.0f;
        mPadLightData.uShadowOffset = 0.01f;
        mPadLightData.colorInterpPeriod = 7.0f;
        mPadLightData.gradientSpeedChange = 1.8f;
        mPadLightData.gradientSpeedRest = 1.0f;
        mPadLightData.gradientColors1 = new float[]{0.99f, 0.77f, 0.86f, 1.0f, 0.74f, 0.76f, 1.0f, 1.0f, 0.72f, 0.74f, 1.0f, 1.0f, 0.98f, 0.76f, 0.8f, 1.0f};
        mPadLightData.gradientColors2 = new float[]{0.66f, 0.75f, 1.0f, 1.0f, 1.0f, 0.86f, 0.91f, 1.0f, 0.74f, 0.76f, 1.0f, 1.0f, 0.97f, 0.77f, 0.84f, 1.0f};
        mPadLightData.gradientColors3 = new float[]{0.97f, 0.79f, 0.85f, 1.0f, 0.65f, 0.68f, 0.98f, 1.0f, 0.66f, 0.77f, 1.0f, 1.0f, 0.72f, 0.73f, 0.98f, 1.0f};

        // 手机深色
        mPhoneDarkData = new BgEffectData();
        mPhoneDarkData.uTranslateY = 0.0f;
        mPhoneDarkData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mPhoneDarkData.uAlphaMulti = 1.0f;
        mPhoneDarkData.uNoiseScale = 1.5f;
        mPhoneDarkData.uPointOffset = 0.4f;
        mPhoneDarkData.uPointRadiusMulti = 1.0f;
        mPhoneDarkData.uSaturateOffset = 0.17f;
        mPhoneDarkData.uLightOffset = 0.0f;
        mPhoneDarkData.uAlphaOffset = 0.5f;
        mPhoneDarkData.uShadowColorMulti = 0.3f;
        mPhoneDarkData.uShadowColorOffset = 0.3f;
        mPhoneDarkData.uShadowNoiseScale = 5.0f;
        mPhoneDarkData.uShadowOffset = 0.01f;
        mPhoneDarkData.colorInterpPeriod = 8.0f;
        mPhoneDarkData.gradientSpeedChange = 1.0f;
        mPhoneDarkData.gradientSpeedRest = 1.0f;
        mPhoneDarkData.gradientColors1 = new float[]{0.2f, 0.06f, 0.88f, 0.4f, 0.3f, 0.14f, 0.55f, 0.5f, 0.0f, 0.64f, 0.96f, 0.5f, 0.11f, 0.16f, 0.83f, 0.4f};
        mPhoneDarkData.gradientColors2 = new float[]{0.07f, 0.15f, 0.79f, 0.5f, 0.62f, 0.21f, 0.67f, 0.5f, 0.06f, 0.25f, 0.84f, 0.5f, 0.0f, 0.2f, 0.78f, 0.5f};
        mPhoneDarkData.gradientColors3 = new float[]{0.58f, 0.3f, 0.74f, 0.4f, 0.27f, 0.18f, 0.6f, 0.5f, 0.66f, 0.26f, 0.62f, 0.5f, 0.12f, 0.16f, 0.7f, 0.6f};

        // 平板深色
        mPadDarkData = new BgEffectData();
        mPadDarkData.uTranslateY = 0.0f;
        mPadDarkData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mPadDarkData.uAlphaMulti = 1.0f;
        mPadDarkData.uNoiseScale = 1.5f;
        mPadDarkData.uPointOffset = 0.2f;
        mPadDarkData.uPointRadiusMulti = 1.0f;
        mPadDarkData.uSaturateOffset = 0.0f;
        mPadDarkData.uLightOffset = 0.0f;
        mPadDarkData.uAlphaOffset = 0.5f;
        mPadDarkData.uShadowColorMulti = 0.3f;
        mPadDarkData.uShadowColorOffset = 0.3f;
        mPadDarkData.uShadowNoiseScale = 5.0f;
        mPadDarkData.uShadowOffset = 0.01f;
        mPadDarkData.colorInterpPeriod = 7.0f;
        mPadDarkData.gradientSpeedChange = 1.6f;
        mPadDarkData.gradientSpeedRest = 1.2f;
        mPadDarkData.gradientColors1 = new float[]{0.66f, 0.26f, 0.62f, 0.4f, 0.06f, 0.25f, 0.84f, 0.5f, 0.0f, 0.64f, 0.96f, 0.5f, 0.14f, 0.18f, 0.55f, 0.5f};
        mPadDarkData.gradientColors2 = new float[]{0.07f, 0.15f, 0.79f, 0.5f, 0.11f, 0.16f, 0.83f, 0.5f, 0.06f, 0.25f, 0.84f, 0.5f, 0.66f, 0.26f, 0.62f, 0.5f};
        mPadDarkData.gradientColors3 = new float[]{0.58f, 0.3f, 0.74f, 0.5f, 0.11f, 0.16f, 0.83f, 0.5f, 0.66f, 0.26f, 0.62f, 0.5f, 0.27f, 0.18f, 0.6f, 0.6f};

        mDetailLightData = copyOf(mPhoneLightData);
        mDetailDarkData = copyOf(mPhoneDarkData);
    }

    public BgEffectData getAboutAppColorData(BgEffectController.DeviceType deviceType, BgEffectController.ThemeMode themeMode) {
        if (themeMode == LIGHT) {
            return deviceType == PHONE ? mPhoneLightData : mPadLightData;
        } else {
            return deviceType == PHONE ? mPhoneDarkData : mPadDarkData;
        }
    }

    public BgEffectData getDetailAnimalCardDataColorData(BgEffectController.ThemeMode themeMode) {
        if (themeMode == LIGHT) {
            mDetailLightData.gradientColors1 = mHyperOS2ThemeLight;
            mDetailLightData.gradientColors2 = mHyperOS2ThemeLight;
            mDetailLightData.gradientColors3 = mHyperOS2ThemeLight;
            return mDetailLightData;
        } else {
            mDetailDarkData.gradientColors1 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors2 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors3 = mHyperOS2ThemeDark;
            return mDetailDarkData;
        }
    }

    public BgEffectData getDetailGoldenCardDataColorData(BgEffectController.ThemeMode themeMode) {
        if (themeMode == LIGHT) {
            mDetailLightData.gradientColors1 = mGoldenCardThemeLight;
            mDetailLightData.gradientColors2 = mGoldenCardThemeLight;
            mDetailLightData.gradientColors3 = mGoldenCardThemeLight;
            return mDetailLightData;
        } else {
            mDetailDarkData.gradientColors1 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors2 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors3 = mHyperOS2ThemeDark;
            return mDetailDarkData;
        }
    }

    public BgEffectData getDetailFusionCardDataColorData(BgEffectController.ThemeMode themeMode) {
        if (themeMode == LIGHT) {
            mDetailLightData.gradientColors1 = mFusionCardThemeLight;
            mDetailLightData.gradientColors2 = mFusionCardThemeLight;
            mDetailLightData.gradientColors3 = mFusionCardThemeLight;
            return mDetailLightData;
        } else {
            mDetailDarkData.gradientColors1 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors2 = mHyperOS2ThemeDark;
            mDetailDarkData.gradientColors3 = mHyperOS2ThemeDark;
            return mDetailDarkData;
        }
    }

    // 浅拷贝基础参数（gradientColors* 由各 getDetailXxx 方法覆写，故此处直接共享引用）
    private static BgEffectData copyOf(BgEffectData src) {
        BgEffectData d = new BgEffectData();
        d.uTranslateY = src.uTranslateY;
        d.uPoints = src.uPoints;
        d.uAlphaMulti = src.uAlphaMulti;
        d.uNoiseScale = src.uNoiseScale;
        d.uPointOffset = src.uPointOffset;
        d.uPointRadiusMulti = src.uPointRadiusMulti;
        d.uSaturateOffset = src.uSaturateOffset;
        d.uLightOffset = src.uLightOffset;
        d.uAlphaOffset = src.uAlphaOffset;
        d.uShadowColorMulti = src.uShadowColorMulti;
        d.uShadowColorOffset = src.uShadowColorOffset;
        d.uShadowNoiseScale = src.uShadowNoiseScale;
        d.uShadowOffset = src.uShadowOffset;
        d.colorInterpPeriod = src.colorInterpPeriod;
        d.gradientSpeedChange = src.gradientSpeedChange;
        d.gradientSpeedRest = src.gradientSpeedRest;
        d.gradientColors1 = src.gradientColors1;
        d.gradientColors2 = src.gradientColors2;
        d.gradientColors3 = src.gradientColors3;
        return d;
    }
}