package com.careful.HyperFVM.Fragments.Settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.databinding.FragmentSettingsBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private DBHelper dbHelper;
    private AutoTaskNotificationManager autoTaskNotificationManager;

    // 提前注册通知权限请求器
    private ActivityResultLauncher<String> notificationPermissionLauncher;

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
    private static final String CONTENT_AUTO_TASK_ENHANCED = "自动任务-增强";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setTopAppBarTitle(getResources().getString(R.string.label_settings));

        // 初始化数据库类
        dbHelper = new DBHelper(requireContext());

        // 初始化权限授予状态
        checkPermissionStates();

        // 初始化仪表盘通知管理类
        autoTaskNotificationManager = new AutoTaskNotificationManager(requireContext());

        // 在 onCreate() 中注册权限请求器（符合生命周期要求）
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        // 权限拒绝后的处理
                        binding.SwitchAutoTask.setChecked(false);
                        dbHelper.updateSettingValue(CONTENT_AUTO_TASK, "false");
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("权限申请")
                                .setMessage("为了向通知中心推送消息，需要您授予通知权限哦~")
                                .setCancelable(false)
                                .setPositiveButton("去开启", (dialog, which) -> {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                                    startActivity(intent);
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                }
        );

        // 初始化主题选择器
        initThemeSelector();

        // 初始化所有开关状态（从数据库读取）
        initSwitches();
        // 设置开关监听（更新数据库）
        setupSwitchListeners();

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
        // 动态取色开关
        boolean isDynamicColor = dbHelper.getSettingValue(CONTENT_IS_DYNAMIC_COLOR);
        binding.SwitchIsDynamicColor.setChecked(isDynamicColor);
        // 自动任务开关
        boolean isDoAutoTask = dbHelper.getSettingValue(CONTENT_AUTO_TASK);
        binding.SwitchAutoTask.setChecked(isDoAutoTask);
        // 自动任务增强模式开关
        boolean isDoAutoTaskEnhanced = dbHelper.getSettingValue(CONTENT_AUTO_TASK_ENHANCED);
        binding.SwitchAutoTaskEnhanced.setChecked(isDoAutoTaskEnhanced);
    }

    // 设置开关状态变化监听，同步更新数据库
    private void setupSwitchListeners() {
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
                initPersistentNotification();
            }
        });
        // 自动任务增强模式开关
        binding.SwitchAutoTaskEnhanced.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_AUTO_TASK_ENHANCED, isChecked ? "true" : "false");
            ActivityManager systemService = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
            if (!appTasks.isEmpty()) {
                appTasks.get(0).setExcludeFromRecents(isChecked);//设置activity是否隐藏
            }
        });
    }

    // 初始化常驻通知，使用提前注册的 launcher
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue(CONTENT_AUTO_TASK)) {
            autoTaskNotificationManager.createNotificationChannel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 直接使用提前注册的 launcher 发起请求，而非让 AutoTaskNotificationManager 注册
                if (!hasNotificationPermission()) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    Toast.makeText(getContext(), "请重启App\n看到保护通知则启用成功~", Toast.LENGTH_SHORT).show();
                }
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