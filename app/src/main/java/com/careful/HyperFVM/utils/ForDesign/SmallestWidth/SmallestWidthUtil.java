package com.careful.HyperFVM.utils.ForDesign.SmallestWidth;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.graphics.Rect;
import android.view.WindowInsets;

/**
 * 屏幕最小宽度（Smallest Width）工具类（适配 API 31+）
 * 对应开发者选项中的“最小宽度”概念
 */
public final class SmallestWidthUtil {

    private static Application sApplication;

    private SmallestWidthUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    public static void init(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("必须传入有效的Application实例");
        }
        sApplication = application;
    }

    public static int getSmallestWidthDp() {
        checkInit();

        try {
            int screenWidthPx;
            int screenHeightPx;
            float density;

            WindowManager windowManager = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);

            // API 30+ 使用 WindowMetrics
            android.view.WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();

            // 获取可用屏幕区域（排除系统装饰）
            WindowInsets windowInsets = windowMetrics.getWindowInsets();
            android.graphics.Insets insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
            );

            screenWidthPx = bounds.width() - insets.left - insets.right;
            screenHeightPx = bounds.height() - insets.top - insets.bottom;

            // 使用 Resources 获取屏幕密度
            Resources resources = sApplication.getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            density = displayMetrics.density;

            int smallestSidePx = Math.min(screenWidthPx, screenHeightPx);
            return Math.round(smallestSidePx / density);

        } catch (Exception e) {
            return -1;
        }
    }

    private static void checkInit() {
        if (sApplication == null) {
            throw new IllegalStateException("SmallestWidthUtil 未初始化，请在Application中调用 SmallestWidthUtil.init(this)");
        }
    }
}