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
        if (context == null || window == null) return;  // 加防护
        View decorView = window.getDecorView();
        BlurTarget target = ((Activity) context).findViewById(R.id.target);
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        // 统一叠加色：主题 colorSurface 的 50% alpha 版本
        applySurfaceOverlayColor(blurViewId);
    }

    public void setBlur(BlurView blurViewId, BlurTarget blurTarget) {
        if (context == null || window == null) return;  // 加防护
        View decorView = window.getDecorView();
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(blurTarget)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        // 统一叠加色：主题 colorSurface 的 50% alpha 版本
        applySurfaceOverlayColor(blurViewId);
    }

    /**
     * 从当前主题解析 colorSurface 并叠加 50% alpha 后设为模糊叠加色
     */
    private void applySurfaceOverlayColor(BlurView blurView) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
                && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            int color = typedValue.data;
            int alpha = Math.round(Color.alpha(color) * OVERLAY_ALPHA);
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