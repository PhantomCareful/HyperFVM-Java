package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.app.Activity;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class DarkModeManager {

    // 常量：数据库中主题设置的键（与原代码保持一致）
    public static final String KEY_DARK_MODE = "主题-深色主题";

    /**
     * 为Activity应用主题（需在super.onCreate前调用）
     * @param activity 目标Activity
     */
    public static void applyDarkMode(Activity activity) {
        // 1. 获取数据库实例（使用Activity的Context）

        try (DBHelper dbHelper = new DBHelper(activity)) {
            // 2. 读取深色模式设置
            String darkMode = dbHelper.getSettingValueString(KEY_DARK_MODE);

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
    }
}
