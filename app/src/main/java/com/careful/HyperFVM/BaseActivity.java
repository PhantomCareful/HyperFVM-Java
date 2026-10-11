package com.careful.HyperFVM;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_DIY_FONT_SCALE;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

public class BaseActivity extends AppCompatActivity {

    // 数据库升级欢迎弹窗标记（onUpgrade 写入，首个 Activity 弹出后清除），与 settings 表 content 列一一对应
    private static final String CONTENT_MIUIX_WELCOME_DIALOG = "提示-Miuix主题欢迎弹窗";

    // 本实例创建时的 UI 配置版本快照（字段初始化在构造期执行，早于 onResume）
    private int seenUiConfigVersion = HyperFVMApplication.getUiConfigVersion();

    @Override
    protected void attachBaseContext(Context newBase) {
        // 复用 Application 中的 fontScale 修正逻辑，确保 Activity 上下文也生效
        super.attachBaseContext(setFontScale(newBase));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 系统配置变化时，重新修正 Activity 的 fontScale
        setFontScale(getBaseContext());
        // 刷新布局（可选，确保配置变化后界面立即更新）
        if (shouldRecreateOnConfigurationChanged()) {
            recreate();
        }
    }

    /**
     * 配置变化时是否重建界面（默认重建，确保配置变化后界面立即更新）。
     * 子类可覆写为 false 以避免重建（需自行保证配置变化后界面正确适配，
     * 如查看器需要旋转时保持大图不重新加载）
     */
    protected boolean shouldRecreateOnConfigurationChanged() {
        return true;
    }

    /**
     * 修改Configuration，固定fontScale=1.0
     */
    protected Context setFontScale(Context context) {
        DBHelper dbHelper = HyperFVMApplication.getDBHelper();

        // 获取当前配置
        Configuration configuration = context.getResources().getConfiguration();

        // 先获取系统原始的fontScale（关键：从系统配置中拿未被修改的值）
        Configuration systemConfig = Resources.getSystem().getConfiguration();
        float originalFontScale = systemConfig.fontScale;

        if (!dbHelper.getSettingBooleanValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE)) {
            // 开关关闭：读取自定义字体大小
            configuration.fontScale = dbHelper.getSettingFloatValue(CONTENT_DIY_FONT_SCALE);

            // Android 8.0以后，Configuration不可变，需要新建对象
            configuration = new Configuration(configuration);

            // 更新上下文配置
            context = context.createConfigurationContext(configuration);

            // 同步更新DisplayMetrics（防止部分组件读取旧的值）
            context.getResources().getDisplayMetrics().scaledDensity =
                    context.getResources().getDisplayMetrics().density * configuration.fontScale;
        } else {
            // 开关关闭：恢复系统原始fontScale

            if (configuration.fontScale != originalFontScale) {
                // Android 8.0以后，Configuration不可变，需要新建对象
                configuration = new Configuration(configuration);

                // 跟随系统设置的大小
                configuration.fontScale = originalFontScale;

                // 更新上下文配置
                context = context.createConfigurationContext(configuration);

                // 同步更新DisplayMetrics（防止部分组件读取旧的值）
                context.getResources().getDisplayMetrics().scaledDensity =
                        context.getResources().getDisplayMetrics().density * configuration.fontScale;
            }
        }

        return context;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 主题/字体/背景等设置变更后返回前台时，自动重建为最新配置（免手动重启）
        if (seenUiConfigVersion != HyperFVMApplication.getUiConfigVersion()) {
            seenUiConfigVersion = HyperFVMApplication.getUiConfigVersion();
            if (!isFinishing() && !isDestroyed()) {
                recreate();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPendingUpgradeDialog();
    }

    /**
     * 数据库升级后的欢迎弹窗：onUpgrade 阶段没有 Activity，Application 主题缺少弹窗布局引用的 attr，
     * 直接 inflate 会崩溃，故升级时只写标记，待首个 Activity 创建时用 Activity 主题弹出。
     * 先清标记再弹窗，保证 recreate 或后续 Activity 不会重复弹出。
     */
    private void showPendingUpgradeDialog() {
        DBHelper dbHelper = HyperFVMApplication.getDBHelper();
        if (!dbHelper.getSettingBooleanValue(CONTENT_MIUIX_WELCOME_DIALOG)) {
            return;
        }
        dbHelper.updateSettingValue(CONTENT_MIUIX_WELCOME_DIALOG, "false");
        DialogBuilderManager.showDialog(this, "全新Miuix默认主题", "🎨",
                "体验激动人心的HyperFVM吧！", true, "开始使用");
    }
}