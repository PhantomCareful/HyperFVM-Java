package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;

public class ScrollEffectForBackgroundItem {
    public static void applyScrollAlphaEffect(View view, int scrollY, int maxScroll) {
        // 计算渐变因子：0（未滚动）→ 1（完全消失）
        float fraction = Math.min(1f, Math.max(0f, scrollY / (float) maxScroll));

        // 渐隐 Logo、名称、版本号，同时可选地做轻微缩小
        float alpha = 1f - fraction;
        float scale = 1f;   // 不缩小

        setViewAlphaScale(view, alpha, scale);
    }

    public static void applyScrollAlphaAndScaleEffect(View view, int scrollY, int maxScroll) {
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

    /**
     * 为了避免背景图片隐藏后继续消费点击事件，需要做一个处理
     * @param context 上下文
     * @param imageView 要处理的图片
     */
    public static void updateBackgroundLogoClickable(Context context, View imageView) {
        if (imageView.getAlpha() == 0) {
            imageView.setVisibility(View.GONE);
            imageView.setOnClickListener(null);
        } else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(v -> Toast.makeText(context, "Make FVM Great Again🎉🎉🎉", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 为了避免背景图片隐藏后继续消费点击事件，需要做一个处理
     * @param context 上下文
     * @param backgroundImages 承载这些图片的父布局
     * @param imageView 要处理的图片
     * @param cardName 要查询的防御卡名称
     */
    public static void updateCardDataIndexBackgroundImageClickable(Context context, View backgroundImages, View imageView, String cardName) {
        if (backgroundImages.getAlpha() == 0) {
            imageView.setVisibility(View.GONE);
            imageView.setOnClickListener(null);
        } else {
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, cardName));
        }
    }
}
