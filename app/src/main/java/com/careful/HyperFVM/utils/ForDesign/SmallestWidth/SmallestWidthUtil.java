package com.careful.HyperFVM.utils.ForDesign.SmallestWidth;

import android.app.Application;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * 屏幕最小宽度（Smallest Width）工具类（适配 API 31+）
 * 对应开发者选项中的“最小宽度”概念
 */
public final class SmallestWidthUtil {

    private static Application sApplication; // 全局Application上下文

    // 禁止实例化
    private SmallestWidthUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 初始化工具类（必须在Application中调用）
     * @param application 应用全局上下文
     */
    public static void init(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("必须传入有效的Application实例");
        }
        sApplication = application;
    }

    /**
     * 获取屏幕最小宽度（Smallest Width，单位：dp）
     * 计算逻辑：min(屏幕宽度像素, 屏幕高度像素) / 密度比例（density）
     * @return 最小宽度（dp）；若未初始化或计算失败，返回-1
     */
    public static int getSmallestWidthDp() {
        checkInit(); // 检查是否初始化

        try {
            // 1. 通过 DisplayManager 获取默认显示（替代 getDefaultDisplay()）
            DisplayManager displayManager = (DisplayManager) sApplication.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY); // 获取默认显示
            if (defaultDisplay == null) {
                return -1; // 无法获取显示设备
            }

            // 2. 获取屏幕真实像素尺寸（包含状态栏/导航栏）
            DisplayMetrics metrics = new DisplayMetrics();
            defaultDisplay.getRealMetrics(metrics); // 替代 windowManager.getDefaultDisplay().getRealMetrics()
            int screenWidthPx = metrics.widthPixels;  // 屏幕宽度（像素）
            int screenHeightPx = metrics.heightPixels; // 屏幕高度（像素）

            // 3. 取最小边的像素值
            int smallestSidePx = Math.min(screenWidthPx, screenHeightPx);

            // 4. 获取密度比例（density = DPI / 160，用于像素转dp）
            float density = metrics.density;

            // 5. 计算最小宽度（dp）= 最小边像素 / 密度比例（四舍五入取整）
            return Math.round(smallestSidePx / density);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 检查是否初始化，未初始化则抛出异常
     */
    private static void checkInit() {
        if (sApplication == null) {
            throw new IllegalStateException("SmallestWidthUtil 未初始化，请在Application中调用 SmallestWidthUtil.init(this)");
        }
    }
}
