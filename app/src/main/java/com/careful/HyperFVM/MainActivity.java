package com.careful.HyperFVM;

import static com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil.getSmallestWidthDp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PermissionCallback;
import com.careful.HyperFVM.utils.OtherUtils.SignatureChecker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigationrail.NavigationRailView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.careful.HyperFVM.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 广播接收器：监听设备重启
    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DBHelper dbHelper = new DBHelper(context);
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && dbHelper.getSettingValue("自动任务")) {
                Intent restartIntent = new Intent(context, PersistentService.class);
                context.startForegroundService(restartIntent);
                HyperFVMApplication app = (HyperFVMApplication) context.getApplicationContext();
                app.scheduleDailyTask();
                Log.d("BootReceiver", "设备重启，重新调度每日任务");
            }
            dbHelper.close(); // 关闭数据库，避免泄漏
        }
    }

    // 核心：全局跟踪当前导航ID，解决状态同步问题
    private int currentNavId = R.id.navigation_data_station; // 与menu默认选中项一致
    private ActivityMainBinding binding;
    private NavController navController;
    private AutoTaskNotificationManager autoTaskNotificationManager;
    private DBHelper dbHelper;
    private List<Integer> menuOrder; // 导航菜单顺序（与bottom_nav_menu.xml一致）
    private BootReceiver bootReceiver;
    private BottomNavigationView navView;
    private NavigationRailView leftNavView;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainHandler = new Handler(Looper.getMainLooper()); // 初始化主线程 Handler

        // 启动时进行签名校验
        new Thread(() -> {
            if (!SignatureChecker.verifyAppSignature(this)) {
                // 在主线程中显示对话框提示
                mainHandler.post(() -> new MaterialAlertDialogBuilder(this)
                        .setTitle("签名校验失败")
                        .setMessage("同学，您使用的HyperFVM非官方版本，应用将关闭。\n请从官方通道下载安装，非常感谢~")
                        .setCancelable(false)
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 退出应用
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        })
                        .show());
            }
        }).start();

        // 初始化通知管理和数据库
        autoTaskNotificationManager = new AutoTaskNotificationManager(this);
        dbHelper = new DBHelper(this);

        // 应用主题（必须在super.onCreate前）
        DarkModeManager.applyDarkMode(this);
        ThemeManager.applyTheme(this);

        boolean isDoAutoTaskEnhanced = dbHelper.getSettingValue("自动任务-增强");
        ActivityManager systemService = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
        if (!appTasks.isEmpty()) {
            appTasks.get(0).setExcludeFromRecents(isDoAutoTaskEnhanced);//设置activity是否隐藏
        }

        super.onCreate(savedInstanceState);

        // 恢复导航状态（布局切换/重建时）
        if (savedInstanceState != null) {
            currentNavId = savedInstanceState.getInt("currentNavId", R.id.navigation_data_station);
        }

        // 启动自动任务服务 & 注册重启广播
        if (dbHelper.getSettingValue("自动任务")) {
            Intent serviceIntent = new Intent(this, PersistentService.class);
            startForegroundService(serviceIntent);

            bootReceiver = new BootReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            registerReceiver(bootReceiver, filter);
        }

        // 小白条沉浸（MIUI/澎湃OS适配）
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        // 布局初始化
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化菜单顺序（必须与bottom_nav_menu.xml的item顺序一致）
        menuOrder = new ArrayList<>();
        menuOrder.add(R.id.navigation_overview);
        menuOrder.add(R.id.navigation_data_station);
        menuOrder.add(R.id.navigation_tools);
        menuOrder.add(R.id.navigation_settings);
        menuOrder.add(R.id.navigation_about_app);

        // 确保视图加载完成后初始化导航（避免空指针）
        binding.getRoot().post(this::setupNavigation);

        // 配置模糊效果
        setupBlurEffect();

        // 注册返回键回调，主界面返回直接退出App
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // 初始化常驻通知
        initPersistentNotification();
    }

    /**
     * 保存导航状态，防止布局切换时丢失
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentNavId", currentNavId);
    }

    /**
     * 初始化导航逻辑
     */
    private void setupNavigation() {
        try {
            // 获取导航控制器
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            navView = findViewById(R.id.nav_view);
            leftNavView = findViewById(R.id.left_nav_view);

            // 配置导航栏（PAD/手机双端）
            setupNavView();

        } catch (Exception e) {
            Log.e("NavigationError", "导航初始化失败", e);
        }
    }

    /**
     * 统一处理导航项选中逻辑
     */
    private boolean handleNavItemSelection(int targetId) {
        // 点击当前已选中项：直接返回，避免重复处理
        if (targetId == currentNavId) {
            return true;
        }

        // 计算动画方向（根据菜单顺序）
        int currentIndex = menuOrder.indexOf(currentNavId);
        int targetIndex = menuOrder.indexOf(targetId);

        // 设置Tab栏是否可见
        setTabLayoutVisibility(targetId);

        NavOptions.Builder navOptions = new NavOptions.Builder();

        if (targetIndex > currentIndex) {
            if (getSmallestWidthDp() < 600) {
                // 目标在右侧：右滑入 + 左滑出
                navOptions.setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right);
            } else {
                // 目标在下方：下滑入 + 上滑出
                navOptions.setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_top)
                        .setPopEnterAnim(R.anim.slide_in_top)
                        .setPopExitAnim(R.anim.slide_out_bottom);
            }
        } else if (targetIndex < currentIndex) {
            if (getSmallestWidthDp() < 600) {
                // 目标在左侧：左滑入 + 右滑出
                navOptions.setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .setPopEnterAnim(R.anim.slide_in_right)
                        .setPopExitAnim(R.anim.slide_out_left);
            } else {
                // 目标在上方：上滑入 + 下滑出
                navOptions.setEnterAnim(R.anim.slide_in_top)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .setPopEnterAnim(R.anim.slide_in_bottom)
                        .setPopExitAnim(R.anim.slide_out_top);
            }
        }

        navController.navigate(targetId, null, navOptions.build());

        // 更新全局导航状态
        currentNavId = targetId;
        return true;
    }

    // 公开方法，方便DataStation调用
    public void updateNavigationSelection(int targetNavId) {
        // 直接用现有的导航选中逻辑，确保currentNavId并同步UI
        handleNavItemSelection(targetNavId);
        if (navView != null) {
            navView.setSelectedItemId(currentNavId);
        } else if (leftNavView != null) {
            leftNavView.setSelectedItemId(currentNavId);
        }
    }

    private void setTabLayoutVisibility(int navId) {
        // 获取TabLayout并添加淡入淡出动画
        View tabLayout = findViewById(R.id.Tab_Layout);
        View blurViewTopTabLayout = findViewById(R.id.blurViewTopTabLayout);
        if (tabLayout != null) {
            if (navId == R.id.navigation_data_station) {
                // 淡入动画
                tabLayout.setVisibility(View.VISIBLE);
                tabLayout.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
                blurViewTopTabLayout.setVisibility(View.VISIBLE);
                blurViewTopTabLayout.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
            } else {
                // 淡出动画，结束后隐藏
                tabLayout.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> tabLayout.setVisibility(View.GONE))
                        .start();
                blurViewTopTabLayout.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> blurViewTopTabLayout.setVisibility(View.GONE))
                        .start();
            }
        }
    }

    /**
     * 配置导航栏（适配PAD左侧/手机底部）
     */
    private void setupNavView() {
        // 设置Tab栏是否可见
        setTabLayoutVisibility(currentNavId);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_overview,
                R.id.navigation_data_station,
                R.id.navigation_tools,
                R.id.navigation_settings,
                R.id.navigation_about_app
        ).build();

        // PAD端：左侧导航栏
        if (leftNavView != null) {
            // 自定义选中监听器
            leftNavView.setOnItemSelectedListener(item -> handleNavItemSelection(item.getItemId()));
            // 强制同步UI选中状态
            leftNavView.setSelectedItemId(currentNavId);

            // ToolBar联动
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }
            return;
        }

        // 手机端：底部导航
        if (navView != null) {
            // 自定义选中监听器
            navView.setOnItemSelectedListener(item -> handleNavItemSelection(item.getItemId()));
            // 强制同步UI选中状态
            navView.setSelectedItemId(currentNavId);

            // ToolBar联动
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }
        }
    }

    /**
     * 配置模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
        blurUtil.setBlur(findViewById(R.id.blurViewNavView));
        blurUtil.setBlur(findViewById(R.id.blurViewTopTabLayout));
    }

    /**
     * 初始化常驻通知
     */
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue("自动任务")) {
            autoTaskNotificationManager.createNotificationChannel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                autoTaskNotificationManager.requestNotificationPermission(this, new PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                    }

                    @Override
                    public void onPermissionDenied() {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("权限申请")
                                .setMessage("为了向通知中心推送消息，需要您授予通知权限哦~")
                                .setCancelable(false)
                                .setPositiveButton("去开启", (dialog, which) -> {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                                    startActivity(intent);
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                });
            }
        }
    }

    /**
     * 导航返回键支持
     */
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController,
                new AppBarConfiguration.Builder(
                        R.id.navigation_overview,
                        R.id.navigation_data_station,
                        R.id.navigation_tools,
                        R.id.navigation_about_app
                ).build())
                || super.onSupportNavigateUp();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 销毁时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 清除 Handler 的 callbacks
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        // 取消绑定
        binding = null;

        dbHelper.close();

        // 注销广播接收器，避免内存泄漏
        if (bootReceiver != null) {
            try {
                unregisterReceiver(bootReceiver);
            } catch (IllegalArgumentException e) {
                Log.w("MainActivity", "广播接收器未注册或已注销", e);
            }
        }
    }
}