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

    private final BgEffectData mRMBLightData;
    private final BgEffectData mRMBDarkData;

    private final BgEffectData mSupremeGoldenLightData;
    private final BgEffectData mSupremeGoldenDarkData;

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

        // 自建主题：20/50/100元的人民币配色
        // 浅色主题
        mRMBLightData = new BgEffectData();
        mRMBLightData.uTranslateY = 0.0f;
        mRMBLightData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mRMBLightData.uAlphaMulti = 1.0f;
        mRMBLightData.uNoiseScale = 1.5f;
        mRMBLightData.uPointOffset = 0.2f;
        mRMBLightData.uPointRadiusMulti = 1.0f;
        mRMBLightData.uSaturateOffset = 0.2f;
        mRMBLightData.uLightOffset = 0.1f;
        mRMBLightData.uAlphaOffset = 0.5f;
        mRMBLightData.uShadowColorMulti = 0.3f;
        mRMBLightData.uShadowColorOffset = 0.3f;
        mRMBLightData.uShadowNoiseScale = 5.0f;
        mRMBLightData.uShadowOffset = 0.01f;
        mRMBLightData.colorInterpPeriod = 5.0f;
        mRMBLightData.gradientSpeedChange = 1.6f;
        mRMBLightData.gradientSpeedRest = 1.05f;
        mRMBLightData.gradientColors1 = new float[]{
                0.8f, 0.286f, 0.408f, 1.0f,
                0.925f, 0.678f, 0.714f, 1.0f,
                0.957f, 0.475f, 0.416f, 1.0f,
                0.824f, 0.784f, 0.675f, 1.0f
        };
        mRMBLightData.gradientColors2 = new float[]{
                0.522f, 0.392f, 0.255f, 1.0f,
                0.984f, 0.765f, 0.533f, 1.0f,
                0.604f, 0.792f, 0.682f, 1.0f,
                0.929f, 0.804f, 0.714f, 1.0f
        };
        mRMBLightData.gradientColors3 = new float[]{
                0.318f, 0.518f, 0.388f, 1.0f,
                0.655f, 0.827f, 0.698f, 1.0f,
                0.796f, 0.949f, 0.878f, 1.0f,
                0.824f, 0.784f, 0.675f, 1.0f
        };

        // 深色主题
        mRMBDarkData = new BgEffectData();
        mRMBDarkData.uTranslateY = 0.0f;
        mRMBDarkData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mRMBDarkData.uAlphaMulti = 1.0f;
        mRMBDarkData.uNoiseScale = 1.5f;
        mRMBDarkData.uPointOffset = 0.4f;
        mRMBDarkData.uPointRadiusMulti = 1.0f;
        mRMBDarkData.uSaturateOffset = 0.17f;
        mRMBDarkData.uLightOffset = 0.0f;
        mRMBDarkData.uAlphaOffset = 0.5f;
        mRMBDarkData.uShadowColorMulti = 0.3f;
        mRMBDarkData.uShadowColorOffset = 0.3f;
        mRMBDarkData.uShadowNoiseScale = 5.0f;
        mRMBDarkData.uShadowOffset = 0.01f;
        mRMBDarkData.colorInterpPeriod = 8.0f;
        mRMBDarkData.gradientSpeedChange = 1.0f;
        mRMBDarkData.gradientSpeedRest = 1.0f;
        mRMBDarkData.gradientColors1 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };
        mRMBDarkData.gradientColors2 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };
        mRMBDarkData.gradientColors3 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };

        // 自建主题：金卡终转
        // 浅色主题
        mSupremeGoldenLightData = new BgEffectData();
        mSupremeGoldenLightData.uTranslateY = 0.0f;
        mSupremeGoldenLightData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mSupremeGoldenLightData.uAlphaMulti = 1.0f;
        mSupremeGoldenLightData.uNoiseScale = 1.5f;
        mSupremeGoldenLightData.uPointOffset = 0.2f;
        mSupremeGoldenLightData.uPointRadiusMulti = 1.0f;
        mSupremeGoldenLightData.uSaturateOffset = 0.2f;
        mSupremeGoldenLightData.uLightOffset = 0.1f;
        mSupremeGoldenLightData.uAlphaOffset = 0.5f;
        mSupremeGoldenLightData.uShadowColorMulti = 0.3f;
        mSupremeGoldenLightData.uShadowColorOffset = 0.3f;
        mSupremeGoldenLightData.uShadowNoiseScale = 5.0f;
        mSupremeGoldenLightData.uShadowOffset = 0.01f;
        mSupremeGoldenLightData.colorInterpPeriod = 5.0f;
        mSupremeGoldenLightData.gradientSpeedChange = 1.6f;
        mSupremeGoldenLightData.gradientSpeedRest = 1.05f;
        mSupremeGoldenLightData.gradientColors1 = new float[]{
                0.769f, 0.278f, 0.765f, 1.0f,  // #C447C3
                1.0f, 0.941f, 0.482f, 1.0f,    // #FEF07B
                0.867f, 0.267f, 0.745f, 1.0f,  // #DD44BE
                0.506f, 0.973f, 0.996f, 1.0f,  // #81F8FE
        };
        mSupremeGoldenLightData.gradientColors2 = new float[]{
                0.769f, 0.278f, 0.765f, 1.0f,  // #C447C3
                1.0f, 0.941f, 0.482f, 1.0f,    // #FEF07B
                0.867f, 0.267f, 0.745f, 1.0f,  // #DD44BE
                0.506f, 0.973f, 0.996f, 1.0f,  // #81F8FE
        };
        mSupremeGoldenLightData.gradientColors3 = new float[]{
                0.769f, 0.278f, 0.765f, 1.0f,  // #C447C3
                1.0f, 0.941f, 0.482f, 1.0f,    // #FEF07B
                0.867f, 0.267f, 0.745f, 1.0f,  // #DD44BE
                0.506f, 0.973f, 0.996f, 1.0f,  // #81F8FE
        };

        // 深色主题
        mSupremeGoldenDarkData = new BgEffectData();
        mSupremeGoldenDarkData.uTranslateY = 0.0f;
        mSupremeGoldenDarkData.uPoints = new float[]{0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f};
        mSupremeGoldenDarkData.uAlphaMulti = 1.0f;
        mSupremeGoldenDarkData.uNoiseScale = 1.5f;
        mSupremeGoldenDarkData.uPointOffset = 0.4f;
        mSupremeGoldenDarkData.uPointRadiusMulti = 1.0f;
        mSupremeGoldenDarkData.uSaturateOffset = 0.17f;
        mSupremeGoldenDarkData.uLightOffset = 0.0f;
        mSupremeGoldenDarkData.uAlphaOffset = 0.5f;
        mSupremeGoldenDarkData.uShadowColorMulti = 0.3f;
        mSupremeGoldenDarkData.uShadowColorOffset = 0.3f;
        mSupremeGoldenDarkData.uShadowNoiseScale = 5.0f;
        mSupremeGoldenDarkData.uShadowOffset = 0.01f;
        mSupremeGoldenDarkData.colorInterpPeriod = 8.0f;
        mSupremeGoldenDarkData.gradientSpeedChange = 1.0f;
        mSupremeGoldenDarkData.gradientSpeedRest = 1.0f;
        mSupremeGoldenDarkData.gradientColors1 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };
        mSupremeGoldenDarkData.gradientColors2 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };
        mSupremeGoldenDarkData.gradientColors3 = new float[]{
                0.0f, 0.31f, 0.58f, 1.0f, 0.53f, 0.29f, 0.15f, 1.0f, 0.46f, 0.06f, 0.27f, 1.0f, 0.16f, 0.12f, 0.45f, 1.0f
        };
    }

    public BgEffectData getAboutAppColorData(BgEffectController.DeviceType deviceType, BgEffectController.ThemeMode themeMode) {
        if (themeMode == LIGHT) {
            return deviceType == PHONE ? mPhoneLightData : mPadLightData;
        } else {
            return deviceType == PHONE ? mPhoneDarkData : mPadDarkData;
        }
    }

    public BgEffectData getDetailAnimalCardDataColorData(BgEffectController.ThemeMode themeMode) {
        return themeMode == LIGHT ? mRMBLightData : mRMBDarkData;
    }

    public BgEffectData getDetailGoldenCardDataColorData(BgEffectController.ThemeMode themeMode) {
        return themeMode == LIGHT ? mSupremeGoldenLightData : mSupremeGoldenDarkData;
    }
}