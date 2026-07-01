package com.careful.HyperFVM.Activities.NecessaryThings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends BaseActivity {

    private DBHelper dbHelper;

    private static final String CONTENT_IS_DYNAMIC_COLOR = "主题-是否动态取色";
    private static final String CONTENT_APP_THEME = "主题-自定义主题色";
    private String currentTheme;
    private View themeSelectorContainer;
    private TextView themeCurrentSelection;

    public static final String CONTENT_DARK_MODE = "主题-深色主题";
    private String currentDarkMode;
    private TextView darkModeCurrentSelection;

    public static final String CONTENT_INTERFACE_STYLE = "界面风格";
    private String currentInterfaceStyle;
    private TextView interfaceStyleCurrentSelection;

    public static final String CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE = "跟随系统字体大小";
    public static final String CONTENT_DIY_FONT_SCALE = "自定义字体大小";
    private Slider fontScaleSlider;
    private float fontScale;

    public static final String CONTENT_IS_DYNAMIC_BACKGROUND = "动态背景";

    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX = "提示语显示-防御卡全能数据库";
    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST = "提示语显示-增幅卡名单";
    public static final String CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD = "提示语显示-仪表盘刷新完成";

    public static final String CONTENT_IS_BIOMETRIC_AUTH = "安全-生物认证";

    // 使用标志位来防止循环调用
    private boolean isPermitSwitchChanging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_settings);

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化主题选择器
        initThemeSelector();

        // 初始化所有开关状态（从数据库读取）
        initSwitchesAndSliders();

        // 设置开关监听（更新数据库）
        setupSwitchAndSliderListeners();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void checkPermissionStates() {
        // 1 获取权限状态显示的TextView（根据实际布局ID调整）
        TextView notificationStateTv = findViewById(R.id.permission_current_state_notification);
        TextView installStateTv = findViewById(R.id.permission_current_state_install);

        // 2.1 跳转授予通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //点击授权
            findViewById(R.id.permission_notification_container).setOnClickListener(v -> {
                // 调用已注册的权限请求器
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            });
        } else {
            findViewById(R.id.permission_notification_container).setOnClickListener(null);
        }

        // 2.2 跳转授予安装权限
        //点击授权
        findViewById(R.id.permission_install_container).setOnClickListener(v -> {
            // 跳转到安装未知应用权限设置页面
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

            // 需要指定包名
            intent.setData(android.net.Uri.parse("package:" + this.getPackageName()));

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                this.startActivity(intent);
            } else {
                // 如果无法跳转到精确设置页面，跳转到应用详情页
                Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appDetailsIntent.setData(android.net.Uri.parse("package:" + this.getPackageName()));
                this.startActivity(appDetailsIntent);
            }
        });

        // 更新UI
        notificationStateTv.setText(hasNotificationPermission() ? "已授予✅" : "点我去授权👉");
        installStateTv.setText(hasInstallPermission() ? "已授予✅" : "点我去授权👉");
    }

    /**
     * 检查是否拥有通知权限
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下默认拥有通知权限
            return true;
        }
    }

    /**
     * 检查是否拥有安装权限
     */
    private boolean hasInstallPermission() {
        return getPackageManager().canRequestPackageInstalls();
    }

    private void initThemeSelector() {
        themeCurrentSelection = findViewById(R.id.theme_current_selection);
        themeSelectorContainer = findViewById(R.id.theme_selector_container);
        darkModeCurrentSelection = findViewById(R.id.dark_mode_current_selection);
        View darkModeSelectorContainer = findViewById(R.id.dark_mode_selector_container);
        interfaceStyleCurrentSelection = findViewById(R.id.interface_style_current_selection);
        View interfaceStyleSelectorContainer = findViewById(R.id.interface_style_selector_container);

        // 从数据库获取当前主题值
        currentTheme = dbHelper.getSettingStringValue(CONTENT_APP_THEME);
        themeCurrentSelection.setText(currentTheme);
        // 从数据库获取深色模式
        currentDarkMode = dbHelper.getSettingStringValue(CONTENT_DARK_MODE);
        darkModeCurrentSelection.setText(currentDarkMode);
        // 从数据库获取界面风格
        currentInterfaceStyle = dbHelper.getSettingStringValue(CONTENT_INTERFACE_STYLE);
        interfaceStyleCurrentSelection.setText(currentInterfaceStyle);

        // 设置点击事件
        if (!dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_COLOR)) {
            // 动态取色关闭：允许点击
            themeSelectorContainer.setOnClickListener(v -> showThemeSelectionDialog());
        } else {
            // 动态取色开启：禁用点击
            themeSelectorContainer.setOnClickListener(null);
        }
        // 设置深色模式点击事件
        darkModeSelectorContainer.setOnClickListener(v -> showDarkModeSelectionDialog());
        // 设置界面风格点击事件
        interfaceStyleSelectorContainer.setOnClickListener(v -> showInterfaceStyleSelectionDialog());
    }

    private void showThemeSelectionDialog() {
        DialogBuilderManager.showSelectionDialog(this, R.array.theme_entries, currentTheme, "🎨设置主题", CONTENT_APP_THEME, themeCurrentSelection,
                selectedEntries -> currentTheme = selectedEntries);
    }

    private void showDarkModeSelectionDialog() {
        DialogBuilderManager.showSelectionDialog(this, R.array.dark_mode_entries, currentDarkMode, "\uD83C\uDF1D\uD83C\uDF1A设置深色模式", CONTENT_DARK_MODE, darkModeCurrentSelection,
                selectedEntries -> currentDarkMode = selectedEntries);
    }

    private void showInterfaceStyleSelectionDialog() {
        DialogBuilderManager.showSelectionDialog(this, R.array.interface_style_entries, currentInterfaceStyle, "🥕设置界面风格", CONTENT_INTERFACE_STYLE, interfaceStyleCurrentSelection,
                selectedEntries -> currentInterfaceStyle = selectedEntries);
    }

    /**
     * 重启App的方法
     */
    private void restartApp() {
        // 获取App的主Activity（通常是AndroidManifest中声明的LAUNCHER Activity）
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            // 清除之前的任务栈，避免重启后返回旧页面
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 启动主Activity
            startActivity(intent);
            // 关闭当前所有Activity
            finishAffinity();
        }
    }

    /**
     * 从数据库读取状态并初始化开关和滑条
     */
    @SuppressLint("SetTextI18n")
    private void initSwitchesAndSliders() {
        MaterialSwitch materialSwitch;
        // 动态取色开关
        boolean isDynamicColor = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_COLOR);
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setChecked(isDynamicColor);
        // 跟随系统字体大小开关
        boolean isFollowSystemFontScale = dbHelper.getSettingBooleanValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE);
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setChecked(isFollowSystemFontScale);
        // 自定义字体大小滑条
        fontScaleSlider = findViewById(R.id.Slider_FontScale);
        fontScaleSlider.setEnabled(!isFollowSystemFontScale);
        fontScale = dbHelper.getSettingFloatValue(CONTENT_DIY_FONT_SCALE);
        fontScaleSlider.setValue(fontScale);
        // 动态背景开关
        boolean isDynamicBackground = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_BACKGROUND);
        materialSwitch = findViewById(R.id.Switch_isDynamicBackground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            materialSwitch.setEnabled(true);
            materialSwitch.setChecked(isDynamicBackground);
        } else {
            materialSwitch.setEnabled(false);
            materialSwitch.setChecked(false);
        }
        // Toast显示设置开关
        boolean toastIsVisibleCardDataIndex = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX);
        boolean toastIsVisibleCardDataAuxiliaryList = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST);
        boolean toastIsVisibleRefreshDashboard = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setChecked(toastIsVisibleCardDataIndex);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setChecked(toastIsVisibleCardDataAuxiliaryList);
        materialSwitch = findViewById(R.id.Switch_isVisible_RefreshDashboard);
        materialSwitch.setChecked(toastIsVisibleRefreshDashboard);
        // 生物认证开关
        materialSwitch = findViewById(R.id.Switch_BiometricAuth);
        if (BiometricAuthHelper.isBiometricAvailable(this)) {
            // 设备支持生物认证
            boolean isBiometricAuth = dbHelper.getSettingBooleanValue(CONTENT_IS_BIOMETRIC_AUTH);
            materialSwitch.setChecked(isBiometricAuth);
        } else {
            // 设备不支持生物认证
            materialSwitch.setChecked(false);
            materialSwitch.setEnabled(false);
            TextView BiometricAuthDescription = findViewById(R.id.TextView_BiometricAuth_Description);
            BiometricAuthDescription.setText(getResources().getString(R.string.label_settings_biometric_auth_description_not_support) + "\n" +
                    getResources().getString(R.string.label_settings_biometric_auth_description));
        }
    }

    /**
     * 设置开关和滑条状态变化监听，同步更新数据库
     */
    private void setupSwitchAndSliderListeners() {
        MaterialSwitch materialSwitch;
        // 动态取色开关
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_COLOR, isChecked ? "true" : "false");
            if (!isChecked) {
                // 动态取色关闭：允许点击
                themeSelectorContainer.setOnClickListener(v -> showThemeSelectionDialog());
            } else {
                // 动态取色开启：禁用点击
                themeSelectorContainer.setOnClickListener(null);
            }
            Toast.makeText(this, "重启App后生效哦\uD83E\uDEF0", Toast.LENGTH_SHORT).show();
        });
        // 跟随系统字体大小开关
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE, isChecked ? "true" : "false");
            fontScaleSlider.setEnabled(!isChecked);
            Toast.makeText(this, "重启App后生效哦\uD83E\uDEF0", Toast.LENGTH_SHORT).show();
        });
        // 自定义字体大小滑条
        fontScaleSlider.addOnChangeListener((slider, v, b) -> {
            fontScale = v;
            dbHelper.updateSettingValue(CONTENT_DIY_FONT_SCALE, String.valueOf(v));
        });
        fontScaleSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // 啥也不做
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                Toast.makeText(SettingsActivity.this, "重启App后生效哦\uD83E\uDEF0", Toast.LENGTH_SHORT).show();
            }
        });
        // 动态背景开关
        materialSwitch = findViewById(R.id.Switch_isDynamicBackground);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_BACKGROUND, isChecked ? "true" : "false");
            Toast.makeText(this, "重启App后生效哦\uD83E\uDEF0", Toast.LENGTH_SHORT).show();
        });
        // Toast显示设置开关
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX, isChecked ? "true" : "false"));
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST, isChecked ? "true" : "false"));
        materialSwitch = findViewById(R.id.Switch_isVisible_RefreshDashboard);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD, isChecked ? "true" : "false"));
        // 生物认证开关
        materialSwitch = findViewById(R.id.Switch_BiometricAuth);
        MaterialSwitch finalMaterialSwitch = materialSwitch;
        materialSwitch.setOnClickListener(v -> {
            boolean isChecked = finalMaterialSwitch.isChecked();
            BiometricAuthHelper.simpleBiometricAuth(this, getResources().getString(R.string.biometric_auth_title),
                    getResources().getString(R.string.biometric_auth_sub_title), () -> {
                        // 验证成功
                        dbHelper.updateSettingValue(CONTENT_IS_BIOMETRIC_AUTH, !isChecked ? "true" : "false");
                        isPermitSwitchChanging = true;
                        finalMaterialSwitch.setChecked(!isChecked);
            });
        });
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isPermitSwitchChanging) {
                isPermitSwitchChanging = false;
            } else {
                finalMaterialSwitch.setChecked(!isChecked);
            }
        });
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonRestartContainer = findViewById(R.id.FloatButton_Restart_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonRestartContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonRestartContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout settings_container = findViewById(R.id.settings_container);
        InsetsUtil.setMarginHorizontal(this, settings_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) settings_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            settings_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonRestart));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Restart_Container).setOnClickListener(v -> {
            Toast.makeText(this, "重启App⏳⏳⏳", Toast.LENGTH_SHORT).show();
            // 重启App
            restartApp();
        });
    }

    /**
     * 在onResume阶段：检查通知权限并实时更新
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 检查权限授予状态
        checkPermissionStates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭数据库连接
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

}