package com.careful.HyperFVM;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.DashboardNotificationManager;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PermissionCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.careful.HyperFVM.databinding.ActivityMainBinding;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class MainActivity extends AppCompatActivity {

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DBHelper dbHelper = new DBHelper(context);
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && dbHelper.getSettingValue("自动任务")) {
                // 接收到系统启动广播，执行原有逻辑
                Intent restartIntent = new Intent(context, PersistentService.class);
                context.startForegroundService(restartIntent);
                // 重新调度每日任务
                HyperFVMApplication app = (HyperFVMApplication) context.getApplicationContext();
                app.scheduleDailyTask();
                Log.d("BootReceiver", "设备重启，重新调度每日任务");
            }
        }
    }

    private ActivityMainBinding binding;
    private NavController navController;

    private DashboardNotificationManager dashboardNotificationManager;
    private DBHelper dbHelper;

    private List<Integer> menuOrder;// 存储菜单item的ID，顺序与bottom_nav_menu一致

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化仪表盘通知管理类
        dashboardNotificationManager = new DashboardNotificationManager(this);
        dbHelper = new DBHelper(this);

        //设置主题（必须在super.onCreate前调用才有效）
        DarkModeManager.applyDarkMode(this);
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        if (dbHelper.getSettingValue("自动任务")) {
            // 启动前台服务（在App启动时立即启动）
            Intent serviceIntent = new Intent(this, PersistentService.class);
            startForegroundService(serviceIntent);

            // 注册广播接收器监听系统启动事件
            // 初始化静态内部类实例
            BootReceiver bootReceiver = new BootReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            registerReceiver(bootReceiver, filter); // 注册广播接收器
        }

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 处理挖孔屏/刘海屏
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(params);

        // 确保视图完全加载后再初始化导航
        binding.getRoot().post(this::setupNavigation);

        //发送通知
        initPersistentNotification();
    }

    private void setupNavigation() {
        try {
            // 初始化导航控制器
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            // 配置导航栏（适配PAD/手机双端）
            setupNavView();

            // 配置ToolBar
            setupToolBar();

            // 配置模糊材质
            float radius = 20f;
            View decorView = getWindow().getDecorView();
            BlurTarget target = findViewById(R.id.target);
            Drawable windowBackground = decorView.getBackground();

            BlurView blurView = findViewById(R.id.blurViewTopAppBar);
            blurView.setupWith(target)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius);

            blurView = findViewById(R.id.blurViewNavView);
            blurView.setupWith(target)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius);

            // 配置切换动画
            // 1. 初始化菜单顺序（务必与res/menu/bottom_nav_menu.xml中的item顺序一致）
            menuOrder = new ArrayList<>();
            menuOrder.add(R.id.navigation_overview);
            menuOrder.add(R.id.navigation_data_station);
            menuOrder.add(R.id.navigation_tools);
            menuOrder.add(R.id.navigation_settings);
            menuOrder.add(R.id.navigation_about_app);
            // 2. 获取导航控制器和BottomNavigationView
            BottomNavigationView navigationView = findViewById(R.id.nav_view);
            // 3. 记录当前选中的菜单索引（初始为首页）
            final int[] currentIndex = {menuOrder.indexOf(Objects.requireNonNull(navController.getCurrentDestination()).getId())};
            // 4. 监听BottomNavigationView选中事件
            navigationView.setOnItemSelectedListener(item -> {
                int targetId = item.getItemId();
                int targetIndex = menuOrder.indexOf(targetId);

                // 5. 比较当前索引与目标索引，决定动画方向
                NavOptions.Builder navOptions = new NavOptions.Builder();
                if (targetIndex > currentIndex[0]) {
                    // 目标在右侧：当前Fragment左滑出，目标Fragment右滑入
                    navOptions.setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right);
                } else if (targetIndex < currentIndex[0]) {
                    // 目标在左侧：当前Fragment右滑出，目标Fragment左滑入
                    navOptions.setEnterAnim(R.anim.slide_in_left)
                            .setExitAnim(R.anim.slide_out_right)
                            .setPopEnterAnim(R.anim.slide_in_right)
                            .setPopExitAnim(R.anim.slide_out_left);
                } else {
                    // 索引相同则不处理（重复点击同一菜单）
                    return true;
                }
                // 6. 执行导航并应用动画
                navController.navigate(targetId, null, navOptions.build());
                // 7. 更新当前索引
                currentIndex[0] = targetIndex;
                return true;
            });

        } catch (Exception ignored) {
        }
    }

    /**
     * 适配双端导航栏：PAD左侧NavigationView / 手机底部BottomNavigationView
     */
    private void setupNavView() {
        // 导航项配置（共用）
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_overview,
                R.id.navigation_data_station,
                R.id.navigation_tools,
                R.id.navigation_settings,
                R.id.navigation_about_app
        ).build();

        // 尝试获取左侧导航（PAD端）
        NavigationRailView leftNavView = findViewById(R.id.left_nav_view);
        if (leftNavView != null) {
            NavigationUI.setupWithNavController(leftNavView, navController);

            // 仅在ActionBar存在时联动
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }

            return;
        }

        // 手机端逻辑：底部导航
        BottomNavigationView bottomNavView = findViewById(R.id.nav_view);
        if (bottomNavView != null) {
            NavigationUI.setupWithNavController(bottomNavView, navController);
            bottomNavView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_SELECTED);
            // 仅在ActionBar存在时联动
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            }
        }
    }

    /**
     * 配置顶部ToolBar
     */
    private void setupToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.label_data_station);
            }
        }
    }

    /**
     * 支持导航返回键
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

    // 初始化常驻通知
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue("通知-美食悬赏") || dbHelper.getSettingValue("通知-施肥活动") || dbHelper.getSettingValue("通知-双爆信息") || dbHelper.getSettingValue("通知-温馨礼包")) {
            // 1. 确保通知渠道存在
            dashboardNotificationManager.createNotificationChannel();
            // 2. 检查并请求通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                dashboardNotificationManager.requestNotificationPermission(this, new PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        sendPersistentNotification();
                    }

                    @Override
                    public void onPermissionDenied() {
                        // 权限被拒，可弹窗提示用户（可选）
                        new MaterialAlertDialogBuilder(getApplicationContext())
                                .setTitle("权限申请")
                                .setMessage("为了向通知中心推送消息，需要您授予通知权限哦~")
                                .setPositiveButton("去开启", (dialog, which) -> {
                                    // 引导用户手动开启权限（跳转到应用设置）
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getApplicationContext().getPackageName());
                                    startActivity(intent);
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                });
            }
        }
    }

    // 发送常驻通知
    @SuppressLint("MissingPermission")
    private void sendPersistentNotification() {
        // 构建常驻通知
        if (dbHelper.getSettingValue("通知-美食悬赏")) {
            dashboardNotificationManager.sendNewYearNotification();
        }

        if (dbHelper.getSettingValue("通知-施肥活动")) {
            dashboardNotificationManager.sendFertilizationTaskNotification();
        }

        if (dbHelper.getSettingValue("通知-双爆信息")) {
            dashboardNotificationManager.sendActivityNotification();
        }

        if (dbHelper.getSettingValue("通知-温馨礼包")) {
            dashboardNotificationManager.sendMeishiWechatImportantNotification();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // 释放绑定资源
        dbHelper.close();
    }
}