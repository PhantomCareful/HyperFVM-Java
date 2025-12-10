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
    private final Context context;
    private final Window window; // 用于获取DecorView
    private final float radius = 20f;

    public BlurUtil(Context context) {
        this.context = context;
        this.window = ((Activity) context).getWindow();
    }

    public void setBlur(BlurView blurViewId) {
        View decorView = window.getDecorView();
        BlurTarget target = ((Activity) context).findViewById(R.id.target);
        Drawable windowBackground = decorView.getBackground();

        blurViewId.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
    }
}
