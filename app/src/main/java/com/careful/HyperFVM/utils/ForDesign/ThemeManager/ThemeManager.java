package com.careful.HyperFVM.utils.ForDesign.ThemeManager;

import android.app.Activity;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class ThemeManager {

    // 常量：数据库中主题设置的键（仅以下两个新键，旧主题键已废弃、不再兼容）
    // 主题模式（“Miuix/Monet（跟随系统/浅色/深色）”六选一），深浅色部分由 DarkModeManager 按后缀解析
    public static final String KEY_THEME_MODE = "主题-主题模式";
    // 强调色（仅 Monet 模式下可更改的种子色下拉）
    public static final String KEY_ACCENT_COLOR = "主题-强调色";
    // 两个键的默认值（与预制数据库一致）
    public static final String THEME_MODE_DEFAULT = "Miuix（跟随系统）";
    // 强调色的“默认”值：对应动态取色（壁纸取色）
    private static final String ACCENT_DEFAULT = "默认";

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

        // 2. 读取主题模式（键缺失时落为默认值）
        String themeMode = dbHelper.getSettingStringValue(KEY_THEME_MODE);
        if (themeMode.isEmpty()) {
            themeMode = THEME_MODE_DEFAULT;
        }

        if (themeMode.startsWith("Monet")) {
            // 3. Monet 模式：强调色为“默认”（或空）时使用动态取色，否则使用选定的种子色
            String accent = dbHelper.getSettingStringValue(KEY_ACCENT_COLOR);
            if (accent.isEmpty() || ACCENT_DEFAULT.equals(accent)) {
                // 注意：此处必须 setTheme 一个与 manifest 主题不同的 resId（空壳 NoShadow 即可）。
                // 若与 manifest 相同，setTheme 为 no-op，Application 阶段 material DynamicColors
                // 在 onActivityPreCreated apply 的 overlay 会保留；该 overlay 经 values-v31/v34
                // 最终指向 @android:color/system_error_container_light（系统壁纸动态色，可能偏深，
                // 实测 #FFA746F），导致 colorErrorContainer 卡片背景显示为深红而非预期浅红。
                // setTheme 不同 resId 会重建 mTheme、丢弃该 overlay，回落静态浅红 #F9DEDC（2dff6dd8 行为）。
                activity.setTheme(R.style.Base_Theme_HyperFVMJava_NoShadow);
            } else {
                applyCustomTheme(activity, accent);
            }
        } else {
            // 4. Miuix 模式：固定使用 Miuix 主题（深浅色由 DarkModeManager 按主题模式后缀控制）
            activity.setTheme(R.style.Miuix);
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
