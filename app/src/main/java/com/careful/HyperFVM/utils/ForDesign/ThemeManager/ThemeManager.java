package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.app.Activity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class ThemeManager {

    // 常量：数据库中主题设置的键（与原代码保持一致）
    public static final String KEY_IS_DYNAMIC_COLOR = "主题-是否动态取色";
    public static final String KEY_CUSTOM_THEME = "主题-自定义主题色";
    public static final String KEY_INTERFACE_STYLE = "界面风格";

    private static DBHelper dbHelper;

    /**
     * 为Activity应用主题（需在super.onCreate前调用）
     * @param activity 目标Activity
     */
    public static void applyTheme(Activity activity) {
        // 1. 获取数据库实例（使用Activity的Context）
        dbHelper = new DBHelper(activity);

        // 2. 读取主题设置
        boolean isDynamicColor = dbHelper.getSettingValue(KEY_IS_DYNAMIC_COLOR);
        String currentTheme = dbHelper.getSettingValueString(KEY_CUSTOM_THEME);

        if (!isDynamicColor) {
            // 3. 非动态取色时，应用自定义主题
            applyCustomTheme(activity, currentTheme);
        } else {
            // 4. 动态取色时，使用默认主题，选择Shadow/NoShadow
            String currentInterfaceStyle = dbHelper.getSettingValueString(KEY_INTERFACE_STYLE);
            if (currentInterfaceStyle.equals("鲜艳-立体")) {
                activity.setTheme(R.style.Base_Theme_HyperFVMJava_Shadow);
            } else {
                activity.setTheme(R.style.Base_Theme_HyperFVMJava_NoShadow);
            }
        }
    }

    /**
     * 根据主题名称应用对应的自定义主题
     */
    private static void applyCustomTheme(Activity activity, String themeValue) {
        // 处理空值（默认主题）
        if (themeValue == null || themeValue.isEmpty()) {
            activity.setTheme(R.style.Monet_Red_Gongqiang_Shadow); // 默认"宫墙"
            return;
        }

        int themeResId;

        String currentInterfaceStyle = dbHelper.getSettingValueString(KEY_INTERFACE_STYLE);
        if (currentInterfaceStyle.equals("鲜艳-立体")) {
            // 根据主题名称匹配对应的样式资源
            themeResId = switch (themeValue) {
                case "宫墙" -> R.style.Monet_Red_Gongqiang_Shadow;
                case "琥珀" -> R.style.Monet_Orange_Hupo_Shadow;
                case "麦芽糖" -> R.style.Monet_Yellow_Maiyatang_Shadow;
                case "金橘" -> R.style.Monet_Yellow_Jinju_Shadow;
                case "秋葵" -> R.style.Monet_Green_Qiukui_Shadow;
                case "青绿" -> R.style.Monet_Green_Qinglv_Shadow;
                case "绿宝石" -> R.style.Monet_Green_Lvbaoshi_Shadow;
                case "天水碧" -> R.style.Monet_Cyan_Tianshuibi_Shadow;
                case "品蓝" -> R.style.Monet_Blue_Pinlan_Shadow;
                case "孔雀蓝" -> R.style.Monet_Blue_Kongquelan_Shadow;
                case "凝夜紫" -> R.style.Monet_Purple_Ningyezi_Shadow;
                case "杏花" -> R.style.Monet_Pink_Xinghua_Shadow;
                case "蜜桃" -> R.style.Monet_Pink_Mitao_Shadow;
                default -> R.style.Monet_Red_Gongqiang_Shadow; // 默认主题
            };
        } else {
            // 根据主题名称匹配对应的样式资源
            themeResId = switch (themeValue) {
                case "宫墙" -> R.style.Monet_Red_Gongqiang_NoShadow;
                case "琥珀" -> R.style.Monet_Orange_Hupo_NoShadow;
                case "麦芽糖" -> R.style.Monet_Yellow_Maiyatang_NoShadow;
                case "金橘" -> R.style.Monet_Yellow_Jinju_NoShadow;
                case "秋葵" -> R.style.Monet_Green_Qiukui_NoShadow;
                case "青绿" -> R.style.Monet_Green_Qinglv_NoShadow;
                case "绿宝石" -> R.style.Monet_Green_Lvbaoshi_NoShadow;
                case "天水碧" -> R.style.Monet_Cyan_Tianshuibi_NoShadow;
                case "品蓝" -> R.style.Monet_Blue_Pinlan_NoShadow;
                case "孔雀蓝" -> R.style.Monet_Blue_Kongquelan_NoShadow;
                case "凝夜紫" -> R.style.Monet_Purple_Ningyezi_NoShadow;
                case "杏花" -> R.style.Monet_Pink_Xinghua_NoShadow;
                case "蜜桃" -> R.style.Monet_Pink_Mitao_NoShadow;
                default -> R.style.Monet_Red_Gongqiang_NoShadow; // 默认主题
            };
        }

        // 应用主题（必须在super.onCreate前调用才有效）
        activity.setTheme(themeResId);
    }
}
