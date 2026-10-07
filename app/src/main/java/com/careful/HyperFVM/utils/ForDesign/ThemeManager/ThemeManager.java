package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.app.Activity;

import com.careful.HyperFVM.HyperFVMApplication;
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
        // 1. 从 Application 获取全局 DBHelper
        DBHelper dbHelper = HyperFVMApplication.getDBHelper();
        if (dbHelper == null) {
            // 若为空，说明 Application 未正确初始化（防御）
            return;
        }

        // 2. 读取主题设置
        boolean isDynamicColor = dbHelper.getSettingBooleanValue(KEY_IS_DYNAMIC_COLOR);
        String currentTheme = dbHelper.getSettingStringValue(KEY_CUSTOM_THEME);

        if (!isDynamicColor) {
            // 3. 非动态取色时，应用自定义主题
            applyCustomTheme(activity, currentTheme);
        } else {
            // 4. 动态取色时，使用默认主题
            // 注意：此处必须 setTheme 一个与 manifest 主题不同的 resId（空壳 NoShadow 即可）。
            // 若与 manifest 相同，setTheme 为 no-op，Application 阶段 material DynamicColors
            // 在 onActivityPreCreated apply 的 overlay 会保留；该 overlay 经 values-v31/v34
            // 最终指向 @android:color/system_error_container_light（系统壁纸动态色，可能偏深，
            // 实测 #FFA746F），导致 colorErrorContainer 卡片背景显示为深红而非预期浅红。
            // setTheme 不同 resId 会重建 mTheme、丢弃该 overlay，回落静态浅红 #F9DEDC（2dff6dd8 行为）。
            activity.setTheme(R.style.Base_Theme_HyperFVMJava_NoShadow);
        }
    }

    /**
     * 根据主题名称应用对应的自定义主题
     */
    private static void applyCustomTheme(Activity activity, String themeValue) {
        // 处理空值（默认主题）
        if (themeValue == null || themeValue.isEmpty()) {
            activity.setTheme(R.style.Miuix); // 默认Miuix
            return;
        }

        // 根据主题名称匹配对应的样式资源
        int themeResId = switch (themeValue) {
            case "Miuix" -> R.style.Miuix;
            case "宫墙" -> R.style.Monet_Red_Gongqiang;
            case "琥珀" -> R.style.Monet_Orange_Hupo;
            case "麦芽糖" -> R.style.Monet_Yellow_Maiyatang;
            case "金橘" -> R.style.Monet_Yellow_Jinju;
            case "秋葵" -> R.style.Monet_Green_Qiukui;
            case "青绿" -> R.style.Monet_Green_Qinglv;
            case "绿宝石" -> R.style.Monet_Green_Lvbaoshi;
            case "天水碧" -> R.style.Monet_Cyan_Tianshuibi;
            case "品蓝" -> R.style.Monet_Blue_Pinlan;
            case "孔雀蓝" -> R.style.Monet_Blue_Kongquelan;
            case "凝夜紫" -> R.style.Monet_Purple_Ningyezi;
            case "杏花" -> R.style.Monet_Pink_Xinghua;
            case "蜜桃" -> R.style.Monet_Pink_Mitao;
            default -> R.style.Miuix; // 默认主题
        };

        // 应用主题（必须在super.onCreate前调用才有效）
        activity.setTheme(themeResId);
    }
}
