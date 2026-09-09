package com.careful.HyperFVM.utils.ForDesign.Blur;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

import com.careful.HyperFVM.R;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class BlurUtil {
    private Context context;
    private Window window;
    private final float radius = 20f;
    /** 模糊叠加色的透明度（0.5 = 50%） */
    private static final float OVERLAY_ALPHA = 0.5f;

    public BlurUtil(Context context) {
        this.context = context;
        this.window = ((Activity) context).getWindow();
    }

    public void setBlur(BlurView blurViewId) {
        setBlur(blurViewId, OVERLAY_ALPHA);
    }

    /**
     * 设置模糊效果（target 为 Activity 内 id=target 的组件），叠加色透明度可自定义
     *
     * @param overlayAlpha 叠加色透明度（0~1，如0.5 = 50%）
     */
    public void setBlur(BlurView blurViewId, float overlayAlpha) {
        if (context == null || window == null) return;  // 加防护
        View decorView = window.getDecorView();
        BlurTarget target = ((Activity) context).findViewById(R.id.target);
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        // 统一叠加色：主题 colorSurface 叠加指定 alpha
        applySurfaceOverlayColor(blurViewId, overlayAlpha);
    }

    public void setBlur(BlurView blurViewId, BlurTarget blurTarget) {
        setBlur(blurViewId, blurTarget, OVERLAY_ALPHA);
    }

    /**
     * 设置模糊效果（target 自定义），叠加色透明度可自定义
     *
     * @param overlayAlpha 叠加色透明度（0~1，如0.5 = 50%）
     */
    public void setBlur(BlurView blurViewId, BlurTarget blurTarget, float overlayAlpha) {
        if (context == null || window == null) return;  // 加防护
        View decorView = window.getDecorView();
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(blurTarget)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        // 统一叠加色：主题 colorSurface 叠加指定 alpha
        applySurfaceOverlayColor(blurViewId, overlayAlpha);
    }

    /**
     * 从当前主题解析 colorSurface 并叠加指定 alpha 后设为模糊叠加色
     */
    private void applySurfaceOverlayColor(BlurView blurView, float overlayAlpha) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
                && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            int color = typedValue.data;
            int alpha = Math.round(Color.alpha(color) * overlayAlpha);
            blurView.setOverlayColor((color & 0x00FFFFFF) | (alpha << 24));
        }
    }

    /**
     * 释放对 Context 和 Window 的引用，防止 Activity 泄漏
     */
    public void release() {
        context = null;
        window = null;
    }
}