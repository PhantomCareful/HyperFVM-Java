package com.careful.HyperFVM;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_FIXED_FONT_SCALE;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

public class BaseActivity extends AppCompatActivity {

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
        recreate();
    }

    /**
     * 修改Configuration，固定fontScale=1.0
     */
    protected Context setFontScale(Context context) {
        try (DBHelper dbHelper = new DBHelper(context)) {
            // 获取当前配置
            Configuration configuration = context.getResources().getConfiguration();

            // 先获取系统原始的fontScale（关键：从系统配置中拿未被修改的值）
            Configuration systemConfig = Resources.getSystem().getConfiguration();
            float originalFontScale = systemConfig.fontScale;

            // 仅当fontScale≠1.0时修改，避免不必要的操作
            if (dbHelper.getSettingValue(CONTENT_IS_FIXED_FONT_SCALE)) {
                // 开关开启：强制固定为1.0
                configuration.fontScale = 1.0f;

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
        }
        return context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}