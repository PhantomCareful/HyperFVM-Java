package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class DarkModeManager {
    @SuppressLint("StaticFieldLeak")
    private static final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    /**
     * 按主题模式的深浅色后缀应用夜间模式（需在super.onCreate前调用）
     */
    public static void applyDarkMode() {
        String themeMode = dbHelper.getSettingStringValue(ThemeManager.KEY_THEME_MODE);

        if (themeMode.endsWith("（深色）")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (themeMode.endsWith("（浅色）")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            // 跟随系统（含键缺失时的默认值）
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    /**
     * 获取当前生效的深色模式状态
     * @param context 上下文
     * @return 深色模式返回true，浅色模式返回false
     */
    public static boolean isDarkMode(Context context) {
        String themeMode = dbHelper.getSettingStringValue(ThemeManager.KEY_THEME_MODE);

        if (themeMode.endsWith("（深色）")) {
            return true;
        }
        if (themeMode.endsWith("（浅色）")) {
            return false;
        }
        // 跟随系统（含键缺失时的默认值）：读当前配置的 uiMode
        int nightMode = context.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
}
