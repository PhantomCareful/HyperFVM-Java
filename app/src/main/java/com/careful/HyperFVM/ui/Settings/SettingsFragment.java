package com.careful.HyperFVM.ui.Settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.databinding.FragmentSettingsBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.DashboardNotificationManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private DBHelper dbHelper;
    private DashboardNotificationManager dashboardNotificationManager;

    // 提前注册权限请求 Launcher
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    // 开关对应的数据库字段名（假设与开关名称对应）
    private static final String CONTENT_MEISHI_WECHAT = "通知-温馨礼包";
    private static final String CONTENT_ACTIVITY = "通知-双爆信息";
    private static final String CONTENT_FERTILIZATION_TASK = "通知-施肥活动";
    private static final String CONTENT_NEW_YEAR = "通知-美食悬赏";

    // 特殊，只会在第一次开启的时候才会起作用：记录当前需要发送的通知类型（区分哪个开关触发了权限请求）
    private String currentNotificationType;

    private static final String CONTENT_IS_DYNAMIC_COLOR = "主题-是否动态取色";
    private static final String CONTENT_APP_THEME = "主题-自定义主题色";
    private String currentTheme;
    private View themeSelectorContainer;
    private TextView themeCurrentSelection;

    private static final String CONTENT_DARK_MODE = "主题-深色主题";
    private String currentDarkMode;
    private View darkModeSelectorContainer;
    private TextView darkModeCurrentSelection;

    private static final String CONTENT_AUTO_TASK = "自动任务";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setTopAppBarTitle(getResources().getString(R.string.label_settings));

        // 初始化数据库帮助类
        dbHelper = new DBHelper(requireContext());

        // 初始化权限授予状态
        checkPermissionStates();

        // 初始化仪表盘通知管理类
        dashboardNotificationManager = new DashboardNotificationManager(requireContext());

        // 初始化主题选择器
        initThemeSelector();

        // 初始化所有开关状态（从数据库读取）
        initSwitches();
        // 设置开关监听（更新数据库）
        setupSwitchListeners();

        // 注册权限请求器
        // 提前在 Fragment 创建时注册权限请求器（必须在 onCreate 中）
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        switch (currentNotificationType) {
                            case CONTENT_MEISHI_WECHAT:
                                dashboardNotificationManager.sendMeishiWechatImportantNotification();
                                break;
                            case CONTENT_ACTIVITY:
                                dashboardNotificationManager.sendActivityNotification();
                                break;
                            case CONTENT_FERTILIZATION_TASK:
                                dashboardNotificationManager.sendFertilizationTaskNotification();
                                break;
                            case CONTENT_NEW_YEAR:
                                dashboardNotificationManager.sendNewYearNotification();
                                break;
                        }
                    } else {
                        // 权限被拒，先重置按钮状态，再提示
                        // 临时移除监听器，避免重置时触发循环回调
                        removeSwitchListeners();
                        // 根据类型重置对应开关
                        switch (currentNotificationType) {
                            case CONTENT_MEISHI_WECHAT:
                                binding.SwitchMeishiWechat.setChecked(false);
                                dbHelper.updateSettingValue(CONTENT_MEISHI_WECHAT, "false");
                                break;
                            case CONTENT_ACTIVITY:
                                binding.SwitchActivity.setChecked(false);
                                dbHelper.updateSettingValue(CONTENT_ACTIVITY, "false");
                                break;
                            case CONTENT_FERTILIZATION_TASK:
                                binding.SwitchFertilizationTask.setChecked(false);
                                dbHelper.updateSettingValue(CONTENT_FERTILIZATION_TASK, "false");
                                break;
                            case CONTENT_NEW_YEAR:
                                binding.SwitchNewYear.setChecked(false);
                                dbHelper.updateSettingValue(CONTENT_NEW_YEAR, "false");
                                break;
                        }
                        // 重新设置监听器
                        setupSwitchListeners();
                        // 弹窗提示
                        Toast.makeText(getContext(), "开启失败，请先授予通知权限", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            checkPermissionStates();
        }
    }

    private void setTopAppBarTitle(String title) {
        //设置标题
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
    }

    private void checkPermissionStates() {
        // 1. 获取权限状态显示的TextView（根据实际布局ID调整）
        TextView notificationStateTv = binding.permissionCurrentStateNotification; // 对应XML中的id
        // 2. 检查通知权限并更新状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                //点击授权
                binding.permissionNotificationContainer.setOnClickListener(v -> {
                    // 调用已注册的权限请求器
                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                    startActivity(intent);
                });
            } else {
                // 有权限时移除点击事件
                binding.permissionNotificationContainer.setOnClickListener(null);
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
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下默认拥有通知权限
            return true;
        }
    }

    // 从数据库读取状态并初始化开关
    private void initSwitches() {
        // 温馨礼包开关
        boolean isNotificationMeishiWechatEnabled = dbHelper.getSettingValue(CONTENT_MEISHI_WECHAT);
        binding.SwitchMeishiWechat.setChecked(isNotificationMeishiWechatEnabled);
        // 双爆信息开关
        boolean isActivityEnabled = dbHelper.getSettingValue(CONTENT_ACTIVITY);
        binding.SwitchActivity.setChecked(isActivityEnabled);
        // 施肥活动开关
        boolean isFertilizationTaskEnabled = dbHelper.getSettingValue(CONTENT_FERTILIZATION_TASK);
        binding.SwitchFertilizationTask.setChecked(isFertilizationTaskEnabled);
        // 美食悬赏开关
        boolean isNewYearEnabled = dbHelper.getSettingValue(CONTENT_NEW_YEAR);
        binding.SwitchNewYear.setChecked(isNewYearEnabled);
        // 动态取色开关
        boolean isDynamicColor = dbHelper.getSettingValue(CONTENT_IS_DYNAMIC_COLOR);
        binding.SwitchIsDynamicColor.setChecked(isDynamicColor);
        // 自动任务开关
        boolean isDoAutoTask = dbHelper.getSettingValue(CONTENT_AUTO_TASK);
        binding.SwitchAutoTask.setChecked(isDoAutoTask);
    }

    // 设置开关状态变化监听，同步更新数据库
    private void setupSwitchListeners() {
        // 温馨礼包开关
        binding.SwitchMeishiWechat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_MEISHI_WECHAT, isChecked ? "true" : "false");
            if (isChecked) {
                currentNotificationType = CONTENT_MEISHI_WECHAT;
                initPersistentNotification();
                dashboardNotificationManager.sendMeishiWechatImportantNotification();
            } else {
                dashboardNotificationManager.cancelMeishiWechatNotification();
            }
        });
        // 双爆信息开关
        binding.SwitchActivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_ACTIVITY, isChecked ? "true" : "false");
            if (isChecked) {
                currentNotificationType = CONTENT_ACTIVITY;
                initPersistentNotification();
                dashboardNotificationManager.sendActivityNotification();
            } else {
                dashboardNotificationManager.cancelActivityNotification();
            }
        });
        // 施肥活动开关
        binding.SwitchFertilizationTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_FERTILIZATION_TASK, isChecked ? "true" : "false");
            if (isChecked) {
                currentNotificationType = CONTENT_FERTILIZATION_TASK;
                initPersistentNotification();
                dashboardNotificationManager.sendFertilizationTaskNotification();
            } else {
                dashboardNotificationManager.cancelFertilizationTaskNotification();
            }
        });
        // 美食悬赏开关
        binding.SwitchNewYear.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_NEW_YEAR, isChecked ? "true" : "false");
            if (isChecked) {
                currentNotificationType = CONTENT_NEW_YEAR;
                initPersistentNotification();
                dashboardNotificationManager.sendNewYearNotification();
            } else {
                dashboardNotificationManager.cancelNewYearNotification();
            }
        });
        // 动态取色开关
        binding.SwitchIsDynamicColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_COLOR, isChecked ? "true" : "false");
            updateThemeClickable(isChecked);
            Toast.makeText(getContext(), "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();
            // 重启App
            restartApp();
        });
        // 自动任务开关
        binding.SwitchAutoTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_AUTO_TASK, isChecked ? "true" : "false");
            if (!isChecked) {
                // 取消所有已调度的自动任务
                WorkManager.getInstance(requireContext()).cancelAllWorkByTag("AUTO_TASK_TAG");
                dbHelper.updateSettingValue("自动任务-初始时间", "0");
                Log.d("WorkManager", "All scheduled auto tasks have been canceled");
                // 停止前台服务
                Intent serviceIntent = new Intent(requireContext(), PersistentService.class);
                requireContext().stopService(serviceIntent);
                Log.d("WorkManager", "PersistentService stopped, notification removed");
            } else {
                Toast.makeText(getContext(), "请重启App，看到保护通知则启用成功~", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 临时移除所有开关的监听器（避免重置时触发回调）
     */
    private void removeSwitchListeners() {
        binding.SwitchMeishiWechat.setOnCheckedChangeListener(null);
        binding.SwitchActivity.setOnCheckedChangeListener(null);
        binding.SwitchFertilizationTask.setOnCheckedChangeListener(null);
        binding.SwitchNewYear.setOnCheckedChangeListener(null);
    }

    // 初始化常驻通知
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue("通知-美食悬赏") || dbHelper.getSettingValue("通知-施肥活动") || dbHelper.getSettingValue("通知-双爆信息") || dbHelper.getSettingValue("通知-温馨礼包")) {
            // 1. 确保通知渠道存在
            dashboardNotificationManager.createNotificationChannel();
            // 2. 检查并请求通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 启动权限请求
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initThemeSelector() {
        // 获取视图引用
        themeCurrentSelection = binding.themeCurrentSelection;
        themeSelectorContainer = binding.themeSelectorContainer;
        darkModeCurrentSelection = binding.darkModeCurrentSelection;
        darkModeSelectorContainer = binding.darkModeSelectorContainer;

        // 从数据库读取当前主题设置
        getCurrentThemeValue();
        themeCurrentSelection.setText(currentTheme);
        darkModeCurrentSelection.setText(currentDarkMode);

        // 设置点击事件
        boolean isDynamicColor = dbHelper.getSettingValue(CONTENT_IS_DYNAMIC_COLOR);
        updateThemeClickable(isDynamicColor);
        // 设置深色模式点击事件
        darkModeSelectorContainer.setOnClickListener(v -> showDarkModeSelectionDialog());
    }

    private void updateThemeClickable(boolean isDynamicColor) {
        if (!isDynamicColor) {
            // 动态取色关闭：允许点击
            themeSelectorContainer.setOnClickListener(v -> showThemeSelectionDialog());
        } else {
            // 动态取色开启：禁用点击
            themeSelectorContainer.setOnClickListener(null);
        }
    }

    private void getCurrentThemeValue() {
        // 从数据库获取当前主题值
        currentTheme = dbHelper.getSettingValueString(CONTENT_APP_THEME);
        currentTheme = (currentTheme != null && !currentTheme.isEmpty()) ? currentTheme : "宫墙";
        // 从数据库获取深色模式
        currentDarkMode = dbHelper.getSettingValueString(CONTENT_DARK_MODE);
        currentDarkMode = (currentDarkMode != null && !currentDarkMode.isEmpty()) ? currentDarkMode : "跟随系统\uD83C\uDF17";
    }

    private void showThemeSelectionDialog() {
        String[] themeEntries = getResources().getStringArray(R.array.theme_entries);

        int selectedIndex = 0;
        for (int i = 0; i < themeEntries.length; i++) {
            if (themeEntries[i].equals(currentTheme)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择主题")
                .setSingleChoiceItems(themeEntries, selectedIndex, (dialog, which) -> {
                    String selectedEntries = themeEntries[which];

                    dbHelper.updateSettingValue(CONTENT_APP_THEME, selectedEntries);

                    themeCurrentSelection.setText(selectedEntries);
                    dialog.dismiss();
                    Toast.makeText(getContext(), "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();
                    // 重启App
                    restartApp();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDarkModeSelectionDialog() {
        String[] darkModeEntries = getResources().getStringArray(R.array.dark_mode_entries);

        int selectedIndex = 0;
        for (int i = 0; i < darkModeEntries.length; i++) {
            if (darkModeEntries[i].equals(currentDarkMode)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("深色模式\uD83C\uDF1D\uD83C\uDF1A")
                .setSingleChoiceItems(darkModeEntries, selectedIndex, (dialog, which) -> {
                    String selectedEntries = darkModeEntries[which];

                    dbHelper.updateSettingValue(CONTENT_DARK_MODE, selectedEntries);

                    darkModeCurrentSelection.setText(selectedEntries);
                    dialog.dismiss();
                    Toast.makeText(getContext(), "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();
                    // 重启App
                    restartApp();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 重启App的方法
    private void restartApp() {
        // 获取App的主Activity（通常是AndroidManifest中声明的LAUNCHER Activity）
        Intent intent = requireActivity().getPackageManager()
                .getLaunchIntentForPackage(requireActivity().getPackageName());
        if (intent != null) {
            // 清除之前的任务栈，避免重启后返回旧页面
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 启动主Activity
            requireActivity().startActivity(intent);
            // 关闭当前所有Activity
            requireActivity().finishAffinity();
            // System.exit(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // 关闭数据库连接（如果DBHelper需要）
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}