package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class DarkModeManager {
    @SuppressLint("StaticFieldLeak")
    private static final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    // 常量：数据库中主题设置的键（与原代码保持一致）
    public static final String KEY_DARK_MODE = "主题-深色主题";

    /**
     * 为Activity应用主题（需在super.onCreate前调用）
     */
    public static void applyDarkMode() {
        String darkMode = dbHelper.getSettingStringValue(KEY_DARK_MODE);

        switch (darkMode) {
            case "总是开启\uD83C\uDF1A":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "总是关闭\uD83C\uDF1D":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "跟随系统\uD83C\uDF17":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * 获取系统的深色模式
     * @param context 上下文
     * @return 深色模式返回true，浅色模式返回false
     */
    public static boolean isDarkMode(Context context) {
        String darkMode = dbHelper.getSettingStringValue(KEY_DARK_MODE);

        return switch (darkMode) {
            case "总是开启\uD83C\uDF1A" -> true;
            case "总是关闭\uD83C\uDF1D" -> false;
            default -> {
                int nightMode = context.getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                yield nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            }
        };
    }
}
