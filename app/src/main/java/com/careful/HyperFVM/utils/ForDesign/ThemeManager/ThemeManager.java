package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.app.Activity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class ThemeManager {

    // 常量：数据库中主题设置的键（与原代码保持一致）
    public static final String KEY_IS_DYNAMIC_COLOR = "主题-是否动态取色";
    public static final String KEY_CUSTOM_THEME = "主题-自定义主题色";

    /**
     * 为Activity应用主题（需在super.onCreate前调用）
     * @param activity 目标Activity
     */
    public static void applyTheme(Activity activity) {
        // 1. 获取数据库实例（使用Activity的Context）

        try (DBHelper dbHelper = new DBHelper(activity)) {
            // 2. 读取主题设置
            boolean isDynamicColor = dbHelper.getSettingValue(KEY_IS_DYNAMIC_COLOR);
            String themeValue = dbHelper.getSettingValueString(KEY_CUSTOM_THEME);

            // 3. 非动态取色时，应用自定义主题
            if (!isDynamicColor) {
                applyCustomTheme(activity, themeValue);
            }
            // 动态取色时不额外设置（使用默认主题）
        }
    }

    /**
     * 根据主题名称应用对应的自定义主题
     */
    private static void applyCustomTheme(Activity activity, String themeValue) {
        // 处理空值（默认主题）
        if (themeValue == null || themeValue.isEmpty()) {
            activity.setTheme(R.style.AppTheme_Red_Gongqiang); // 默认"宫墙"
            return;
        }

        // 根据主题名称匹配对应的样式资源
        int themeResId;
        switch (themeValue) {
            case "宫墙":
                themeResId = R.style.AppTheme_Red_Gongqiang;
                break;
            case "琥珀":
                themeResId = R.style.AppTheme_Orange_Hupo;
                break;
            case "麦芽糖":
                themeResId = R.style.AppTheme_Yellow_Maiyatang;
                break;
            case "金橘":
                themeResId = R.style.AppTheme_Yellow_Jinju;
                break;
            case "秋葵":
                themeResId = R.style.AppTheme_Green_Qiukui;
                break;
            case "青绿":
                themeResId = R.style.AppTheme_Green_Qinglv;
                break;
            case "绿宝石":
                themeResId = R.style.AppTheme_Green_Lvbaoshi;
                break;
            case "天水碧":
                themeResId = R.style.AppTheme_Cyan_Tianshuibi;
                break;
            case "品蓝":
                themeResId = R.style.AppTheme_Blue_Pinlan;
                break;
            case "孔雀蓝":
                themeResId = R.style.AppTheme_Blue_Kongquelan;
                break;
            case "凝夜紫":
                themeResId = R.style.AppTheme_Purple_Ningyezi;
                break;
            case "杏花":
                themeResId = R.style.AppTheme_Pink_Xinghua;
                break;
            case "蜜桃":
                themeResId = R.style.AppTheme_Pink_Mitao;
                break;
            default:
                themeResId = R.style.AppTheme_Red_Gongqiang; // 默认主题
                break;
        }

        // 应用主题（必须在super.onCreate前调用才有效）
        activity.setTheme(themeResId);
    }
}
