package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.view.View;

public class ScrollEffectForBackgroundItem {
    public static void applyScrollEffect(View view, int scrollY, int maxScroll) {
        // 计算渐变因子：0（未滚动）→ 1（完全消失）
        float fraction = Math.min(1f, Math.max(0f, scrollY / (float) maxScroll));

        // 渐隐 Logo、名称、版本号，同时可选地做轻微缩小
        float alpha = 1f - fraction;
        float scale = 1f - 0.1f * fraction;   // 缩小到 90%

        setViewAlphaScale(view, alpha, scale);
    }

    public static void setViewAlphaScale(View view, float alpha, float scale) {
        if (view == null) return;
        view.setAlpha(alpha);
        view.setScaleX(scale);
        view.setScaleY(scale);
    }
}
