package com.careful.HyperFVM;

import static com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil.getSmallestWidthDp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.ui.DataStation.DataStationFragment;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.DashboardNotificationManager;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PermissionCallback;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
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
    private DashboardNotificationManager dashboardNotificationManager;
    private DBHelper dbHelper;
    private List<Integer> menuOrder; // 导航菜单顺序（与bottom_nav_menu.xml一致）
    private BootReceiver bootReceiver;
    private BottomNavigationView navView;
    private NavigationRailView leftNavView;
    private ObjectAnimator floatButtonAnimator;// 跟踪当前运行的浮动按钮动画
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化通知管理和数据库
        dashboardNotificationManager = new DashboardNotificationManager(this);
        dbHelper = new DBHelper(this);

        // 应用主题（必须在super.onCreate前）
        DarkModeManager.applyDarkMode(this);
        ThemeManager.applyTheme(this);

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
        preferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
        preferences.edit().putInt("current_fragment_index", 0).apply();
        preferences.edit().putBoolean("exit_animation_permission", true).apply();

        // 挖孔屏/刘海屏适配
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(params);

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

        ExtendedFloatingActionButton floatButton = findViewById(R.id.FloatButton);
        if (floatButton != null) {
            floatButton.setOnClickListener(v -> {
                // 1. 获取导航宿主Fragment
                NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main);
                if (navHost == null) return;

                // 2. 获取当前导航显示的主Fragment（即DataStationFragment）
                // 导航宿主中当前显示的Fragment就是DataStationFragment（当处于该页面时）
                Fragment currentMainFragment = navHost.getChildFragmentManager().getPrimaryNavigationFragment();

                // 3. 判断是否为DataStationFragment，是的话调用切换方法
                if (currentMainFragment instanceof DataStationFragment) {
                    ((DataStationFragment) currentMainFragment).switchToNextFragment();
                }
            });
        }

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
            // 配置顶部ToolBar
            setupToolBar();

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

        // 小插曲：如果targetIndex指向数据站，那么显示翻页按钮，否则隐藏
        if (preferences.getBoolean("exit_animation_permission", true) || targetIndex == 1) {
            // 清理旧动画（取消并移除监听器）
            if (floatButtonAnimator != null && floatButtonAnimator.isRunning()) {
                floatButtonAnimator.removeAllListeners(); // 移除旧监听器，避免干扰
                floatButtonAnimator.cancel();
            }
        }

        if (targetId == R.id.navigation_data_station) {
            findViewById(R.id.FloatButton).setVisibility(View.VISIBLE);

            // 顺便添加一个位移动画
            CardView cardView = findViewById(R.id.Card_FloatButton);
            floatButtonAnimator = ObjectAnimator.ofFloat(
                    cardView,
                    View.TRANSLATION_X,
                    550f, 0f
            );
            floatButtonAnimator.setDuration(800);
            floatButtonAnimator.start();
            preferences.edit().putBoolean("exit_animation_permission", true).apply();
        } else if (preferences.getBoolean("exit_animation_permission", true)) {
            // 顺便添加一个位移动画
            CardView cardView = findViewById(R.id.Card_FloatButton);
            floatButtonAnimator = ObjectAnimator.ofFloat(
                    cardView,
                    View.TRANSLATION_X,
                    0f, 550f
            );
            floatButtonAnimator.setDuration(800);
            floatButtonAnimator.start();

            // 添加动画监听器，监听动画结束事件
            floatButtonAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // 动画开始时的操作（可选）
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // 动画执行完毕后，隐藏按钮
                    findViewById(R.id.FloatButton).setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // 动画被取消时的操作（可选，如需要可在这里也隐藏按钮）
                    findViewById(R.id.FloatButton).setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // 动画重复时的操作（当前动画不重复，留空即可）
                    findViewById(R.id.FloatButton).setVisibility(View.GONE);
                }
            });
            preferences.edit().putBoolean("exit_animation_permission", false).apply();
        }

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

    /**
     * 配置导航栏（适配PAD左侧/手机底部）
     */
    private void setupNavView() {
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

        // 小插曲：如果targetIndex指向数据站，那么显示翻页按钮，否则隐藏
        // 清理旧动画（取消并移除监听器）
        if (floatButtonAnimator != null && floatButtonAnimator.isRunning()) {
            floatButtonAnimator.removeAllListeners(); // 移除旧监听器，避免干扰
            floatButtonAnimator.cancel();
        }

        if (currentNavId == R.id.navigation_data_station) {
            findViewById(R.id.FloatButton).setVisibility(View.VISIBLE);

            // 顺便添加一个位移动画
            CardView cardView = findViewById(R.id.Card_FloatButton);
            floatButtonAnimator = ObjectAnimator.ofFloat(
                    cardView,
                    View.TRANSLATION_X,
                    550f, 0f
            );
            floatButtonAnimator.setDuration(800);
            floatButtonAnimator.start();
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
     * 配置模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
        if (getSmallestWidthDp() < 600) {
            blurUtil.setBlur(findViewById(R.id.blurViewNavView));
        }
        blurUtil.setBlur(findViewById(R.id.blurViewButton));

        // 顺便添加一个位移动画
        CardView cardView = findViewById(R.id.Card_FloatButton);
        floatButtonAnimator = ObjectAnimator.ofFloat(
                cardView,
                View.TRANSLATION_X,
                550f, 0f // 从1000px移动到0px
        );
        floatButtonAnimator.setDuration(800);
        floatButtonAnimator.start();
    }

    /**
     * 初始化常驻通知
     */
    private void initPersistentNotification() {
        if (dbHelper.getSettingValue("通知-美食悬赏") || dbHelper.getSettingValue("通知-施肥活动") ||
                dbHelper.getSettingValue("通知-双爆信息") || dbHelper.getSettingValue("通知-温馨礼包")) {
            dashboardNotificationManager.createNotificationChannel();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                dashboardNotificationManager.requestNotificationPermission(this, new PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        sendPersistentNotification();
                    }

                    @Override
                    public void onPermissionDenied() {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("权限申请")
                                .setMessage("为了向通知中心推送消息，需要您授予通知权限哦~")
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
     * 发送常驻通知
     */
    @SuppressLint("MissingPermission")
    private void sendPersistentNotification() {
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

    /**
     * 销毁时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
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