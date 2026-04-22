package com.careful.HyperFVM.Activities.NecessaryThings;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.TileTaskNotificationManager;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class SettingsActivity extends BaseActivity {

    private DBHelper dbHelper;
    private int pressFeedbackAnimationDelay;
    private TileTaskNotificationManager tileTaskNotificationManager;

    // 提前注册通知权限请求器
    private ActivityResultLauncher<String> notificationPermissionLauncher;

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

    public static final String CONTENT_IS_FIXED_FONT_SCALE = "界面布局优化";

    private static final String CONTENT_AUTO_TASK = "自动任务";
    private static final String CONTENT_AUTO_TASK_ENHANCED = "自动任务-增强";

    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX = "提示语显示-防御卡全能数据库";
    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST = "提示语显示-增幅卡名单";
    public static final String CONTENT_TOAST_IS_VISIBLE_DATA_IMAGE_VIEWER = "提示语显示-数据图查看器";

    public static final String CONTENT_IS_PRESS_FEEDBACK_ANIMATION = "按压反馈动画";

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

        // 添加模糊材质
        setupBlurEffect();

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 初始自动任务通知管理类
        tileTaskNotificationManager = new TileTaskNotificationManager(this);

        // 在 onCreate() 中注册权限请求器（符合生命周期要求）
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        // 权限拒绝后的处理
                        MaterialSwitch switchAutoTask = findViewById(R.id.Switch_AutoTask);
                        switchAutoTask.setChecked(false);
                        dbHelper.updateSettingValue(CONTENT_AUTO_TASK, "false");
                        DialogBuilderManager.showNotificationPermissionRequestDialog(this);
                    }
                }
        );

        // 初始化主题选择器
        initThemeSelector();

        // 初始化所有开关状态（从数据库读取）
        initSwitches();

        // 设置开关监听（更新数据库）
        setupSwitchListeners();
    }

    private void checkPermissionStates() {
        // 1. 获取权限状态显示的TextView（根据实际布局ID调整）
        TextView notificationStateTv = findViewById(R.id.permission_current_state_notification);
        // 2. 检查通知权限并更新状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                //点击授权
                findViewById(R.id.permission_notification_container).setOnClickListener(v -> {
                    // 调用已注册的权限请求器
                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent);
                });
            } else {
                // 有权限时移除点击事件
                findViewById(R.id.permission_notification_container).setOnClickListener(null);
            }
        }
        // 更新UI
        notificationStateTv.setText(hasNotificationPermission() ? "已授予✅" : "未授予，点我去授权👉");
    }

    /**
     * 检查是否拥有通知权限（抽取为单独方法，方便复用）
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

    private void initThemeSelector() {
        themeCurrentSelection = findViewById(R.id.theme_current_selection);
        themeSelectorContainer = findViewById(R.id.theme_selector_container);
        darkModeCurrentSelection = findViewById(R.id.dark_mode_current_selection);
        View darkModeSelectorContainer = findViewById(R.id.dark_mode_selector_container);
        interfaceStyleCurrentSelection = findViewById(R.id.interface_style_current_selection);
        View interfaceStyleSelectorContainer = findViewById(R.id.interface_style_selector_container);

        // 从数据库获取当前主题值
        currentTheme = dbHelper.getSettingValueString(CONTENT_APP_THEME);
        themeCurrentSelection.setText(currentTheme);
        // 从数据库获取深色模式
        currentDarkMode = dbHelper.getSettingValueString(CONTENT_DARK_MODE);
        darkModeCurrentSelection.setText(currentDarkMode);
        // 从数据库获取界面风格
        currentInterfaceStyle = dbHelper.getSettingValueString(CONTENT_INTERFACE_STYLE);
        interfaceStyleCurrentSelection.setText(currentInterfaceStyle);

        // 设置点击事件
        if (!dbHelper.getSettingValue(CONTENT_IS_DYNAMIC_COLOR)) {
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
        DialogBuilderManager.showSelectionDialog(this, R.array.theme_entries, currentTheme, "🎨设置主题", CONTENT_APP_THEME, themeCurrentSelection);
    }

    private void showDarkModeSelectionDialog() {
        DialogBuilderManager.showSelectionDialog(this, R.array.dark_mode_entries, currentDarkMode, "\uD83C\uDF1D\uD83C\uDF1A设置深色模式", CONTENT_DARK_MODE, darkModeCurrentSelection);
    }

    private void showInterfaceStyleSelectionDialog() {
        DialogBuilderManager.showSelectionDialog(this, R.array.interface_style_entries, currentInterfaceStyle, "🥕设置界面风格", CONTENT_INTERFACE_STYLE, interfaceStyleCurrentSelection);
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
     * 从数据库读取状态并初始化开关
     */
    @SuppressLint("SetTextI18n")
    private void initSwitches() {
        MaterialSwitch materialSwitch;
        // 动态取色开关
        boolean isDynamicColor = dbHelper.getSettingValue(CONTENT_IS_DYNAMIC_COLOR);
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setChecked(isDynamicColor);
        // 界面布局优化开关
        boolean isFixedFontScale = dbHelper.getSettingValue(CONTENT_IS_FIXED_FONT_SCALE);
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setChecked(isFixedFontScale);
        // 自动任务开关
        boolean isDoAutoTask = dbHelper.getSettingValue(CONTENT_AUTO_TASK);
        materialSwitch = findViewById(R.id.Switch_AutoTask);
        materialSwitch.setChecked(isDoAutoTask);
        // 自动任务增强模式开关
        boolean isDoAutoTaskEnhanced = dbHelper.getSettingValue(CONTENT_AUTO_TASK_ENHANCED);
        materialSwitch = findViewById(R.id.Switch_AutoTask_Enhanced);
        materialSwitch.setChecked(isDoAutoTaskEnhanced);
        // Toast显示设置开关
        boolean toastIsVisibleCardDataIndex = dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX);
        boolean toastIsVisibleCardDataAuxiliaryList = dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST);
        boolean toastIsVisibleDataImageViewer = dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_DATA_IMAGE_VIEWER);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setChecked(toastIsVisibleCardDataIndex);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setChecked(toastIsVisibleCardDataAuxiliaryList);
        materialSwitch = findViewById(R.id.Switch_isVisible_DataImageViewer);
        materialSwitch.setChecked(toastIsVisibleDataImageViewer);
        // 按压反馈动画开关
        boolean isPressFeedbackAnimation = dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION);
        materialSwitch = findViewById(R.id.Switch_isPressFeedbackAnimation);
        materialSwitch.setChecked(isPressFeedbackAnimation);
        // 生物认证开关
        materialSwitch = findViewById(R.id.Switch_BiometricAuth);
        if (BiometricAuthHelper.isBiometricAvailable(this)) {
            // 设备支持生物认证
            boolean isBiometricAuth = dbHelper.getSettingValue(CONTENT_IS_BIOMETRIC_AUTH);
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
     * 设置开关状态变化监听，同步更新数据库
     */
    private void setupSwitchListeners() {
        MaterialSwitch materialSwitch;
        // 动态取色开关
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_COLOR, isChecked ? "true" : "false");
            if (isChecked) {
                // 动态取色关闭：允许点击
                themeSelectorContainer.setOnClickListener(v -> showThemeSelectionDialog());
            } else {
                // 动态取色开启：禁用点击
                themeSelectorContainer.setOnClickListener(null);
            }
            Toast.makeText(this, "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();
            // 重启App
            restartApp();
        });
        // 界面布局优化开关
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_FIXED_FONT_SCALE, isChecked ? "true" : "false");
            Toast.makeText(this, "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();
            // 重启App
            restartApp();
        });
        // 自动任务开关
        materialSwitch = findViewById(R.id.Switch_AutoTask);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_AUTO_TASK, isChecked ? "true" : "false");
            if (!isChecked) {
                // 取消所有已调度的自动任务
                WorkManager.getInstance(this).cancelAllWorkByTag("AUTO_TASK_TAG");
                dbHelper.updateSettingValue("自动任务-初始时间", "0");
                Log.d("WorkManager", "All scheduled auto tasks have been canceled");
                // 停止前台服务
                Intent serviceIntent = new Intent(this, PersistentService.class);
                stopService(serviceIntent);
                Log.d("WorkManager", "PersistentService stopped, notification removed");
            } else {
                initPersistentNotification();
            }
        });
        // 自动任务增强模式开关
        materialSwitch = findViewById(R.id.Switch_AutoTask_Enhanced);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_AUTO_TASK_ENHANCED, isChecked ? "true" : "false");
            ActivityManager systemService = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
            if (!appTasks.isEmpty()) {
                appTasks.get(0).setExcludeFromRecents(isChecked);//设置activity是否隐藏
            }
        });
        // Toast显示设置开关
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX, isChecked ? "true" : "false"));
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST, isChecked ? "true" : "false"));
        materialSwitch = findViewById(R.id.Switch_isVisible_DataImageViewer);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_DATA_IMAGE_VIEWER, isChecked ? "true" : "false"));
        // 按压反馈动画开关
        materialSwitch = findViewById(R.id.Switch_isPressFeedbackAnimation);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION, isChecked ? "true" : "false"));
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
     * 初始化常驻通知，使用提前注册的 launcher
     */
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue(CONTENT_AUTO_TASK)) {
            tileTaskNotificationManager.createNotificationChannel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 直接使用提前注册的 launcher 发起请求，而非让 TileTaskNotificationManager 注册
                if (!hasNotificationPermission()) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    Toast.makeText(this, "请重启App\n看到保护通知则启用成功~", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    /**
     * 在onResume阶段：
     * 1. 设置按压反馈动画
     * 2. 检查通知权限并实时更新
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        boolean isPressFeedbackAnimation;
        try (DBHelper dbHelper = new DBHelper(this)) {
            // 添加按压动画
            if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
                pressFeedbackAnimationDelay = 200;
                isPressFeedbackAnimation = true;
            } else {
                pressFeedbackAnimationDelay = 0;
                isPressFeedbackAnimation = false;
            }
        }
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));

        // 检查通知权限授予状态
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