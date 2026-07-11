package com.careful.HyperFVM.utils.ForDesign.Blur;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;

import com.careful.HyperFVM.R;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class BlurUtil {
    private Context context;
    private Window window;
    private final float radius = 20f;

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
    }

    public void setBlur(BlurView blurViewId, BlurTarget blurTarget) {
        if (context == null || window == null) return;  // 加防护
        View decorView = window.getDecorView();
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(blurTarget)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
    }

    /**
     * 释放对 Context 和 Window 的引用，防止 Activity 泄漏
     */
    public void release() {
        context = null;
        window = null;
    }
}