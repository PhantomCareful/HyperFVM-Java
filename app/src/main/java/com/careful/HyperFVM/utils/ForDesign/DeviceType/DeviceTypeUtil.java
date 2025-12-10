package com.careful.HyperFVM.utils.ForDesign.DeviceType;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.Display;

/**
 * 设备类型判断工具类
 */
public final class DeviceTypeUtil {

    private static Application sApplication;

    public static void init(Application application) {
        sApplication = application;
    }

    /**
     * 判断是否是平板（基于物理屏幕尺寸，不受窗口模式影响）
     * @return true 如果是平板，false 如果是手机
     */
    public static boolean isTabletByPhysicalScreen() {
        checkInit();

        try {
            WindowManager windowManager = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();

            // 获取物理屏幕尺寸
            Point realSize = new Point();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(realSize);
            } else {
                display.getSize(realSize);
            }

            // 获取屏幕密度
            Resources resources = sApplication.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float density = metrics.density;

            // 计算物理屏幕的最小宽度（dp）
            int screenWidthPx = realSize.x;
            int screenHeightPx = realSize.y;
            int smallestSidePx = Math.min(screenWidthPx, screenHeightPx);
            int smallestWidthDp = Math.round(smallestSidePx / density);

            // 通常认为最小宽度 >= 600dp 的是平板
            return smallestWidthDp >= 600;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取物理屏幕的最小宽度（不受窗口模式影响）
     */
    public static int getPhysicalScreenSmallestWidthDp() {
        checkInit();

        try {
            WindowManager windowManager = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();

            // 获取物理屏幕尺寸
            Point realSize = new Point();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(realSize);
            } else {
                display.getSize(realSize);
            }

            // 获取屏幕密度
            Resources resources = sApplication.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float density = metrics.density;

            // 计算物理屏幕的最小宽度（dp）
            int screenWidthPx = realSize.x;
            int screenHeightPx = realSize.y;
            int smallestSidePx = Math.min(screenWidthPx, screenHeightPx);
            return Math.round(smallestSidePx / density);

        } catch (Exception e) {
            return -1;
        }
    }

    private static void checkInit() {
        if (sApplication == null) {
            throw new IllegalStateException("DeviceTypeUtil 未初始化");
        }
    }
}