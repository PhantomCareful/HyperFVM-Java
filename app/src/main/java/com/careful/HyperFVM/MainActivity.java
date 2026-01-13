package com.careful.HyperFVM;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.Fragments.AboutApp.AboutAppFragment;
import com.careful.HyperFVM.Fragments.DataCenter.DataCenterFragment;
import com.careful.HyperFVM.Service.PersistentService;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.AutoTaskNotificationManager;
import com.careful.HyperFVM.utils.ForDesign.Animation.ViewAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.CardItemDecoration;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForDashboard.NotificationManager.PermissionCallback;
import com.careful.HyperFVM.utils.OtherUtils.SignatureChecker;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.databinding.ActivityMainBinding;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private ActivityMainBinding binding;
    private AutoTaskNotificationManager autoTaskNotificationManager;
    private DBHelper dbHelper;
    private List<Integer> menuOrder; // 导航菜单顺序
    private BootReceiver bootReceiver;
    private BottomNavigationView navView;

    // 新增：ViewPager2相关
    private ViewPager2 viewPager;
    private TabLayoutFragmentStateAdapter viewPagerAdapter;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainHandler = new Handler(Looper.getMainLooper()); // 初始化主线程 Handler

        // 初始化通知管理和数据库
        autoTaskNotificationManager = new AutoTaskNotificationManager(this);
        dbHelper = new DBHelper(this);

        // 启动时进行签名校验
        new Thread(() -> {
            if (!SignatureChecker.verifyAppSignature(this)) {
                // 在主线程中显示对话框提示
                mainHandler.post(() -> new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
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
        menuOrder.add(R.id.navigation_data_station);
        menuOrder.add(R.id.navigation_about_app);

        // 确保视图加载完成后初始化ViewPager（避免空指针）
        setupViewPager();
        setTopAppBarTitle(getResources().getString(R.string.top_bar_data_center) + " ");

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

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnTouchListener(this::setPressAnimation);
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnClickListener(v -> showCardQueryDialog());
    }

    /**
     * 初始化ViewPager2
     */
    private void setupViewPager() {
        try {
            viewPager = findViewById(R.id.viewPager);
            navView = findViewById(R.id.nav_view);

            // 初始化适配器
            viewPagerAdapter = new TabLayoutFragmentStateAdapter(this);

            // 添加Fragment
            viewPagerAdapter.addFragment(new DataCenterFragment(), getResources().getString(R.string.top_bar_data_center));
            viewPagerAdapter.addFragment(new AboutAppFragment(), getResources().getString(R.string.top_bar_about_app));

            viewPager.setAdapter(viewPagerAdapter);

            // 禁用预加载相邻页面（可选，减少内存使用）
            viewPager.setOffscreenPageLimit(1);

            // 禁用ViewPager2的滚动动画（如果需要）
            viewPager.setUserInputEnabled(false); // 如果不想让用户滑动

            // 设置ViewPager2页面变化监听
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    // 同步底部导航栏状态
                    int selectedId = menuOrder.get(position);
                    navView.setSelectedItemId(selectedId);

                    // 更新Toolbar标题
                    updateToolbarTitle(position);
                }
            });

            // 设置底部导航栏点击监听
            navView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                int targetPosition = menuOrder.indexOf(itemId);

                if (targetPosition != -1 && targetPosition != viewPager.getCurrentItem()) {
                    // 添加平滑滚动动画
                    viewPager.setCurrentItem(targetPosition, true);
                    return true;
                }
                return false;
            });

            // 设置默认选中项
            navView.setSelectedItemId(R.id.navigation_data_station);

        } catch (Exception e) {
            Log.e("ViewPagerSetup", "ViewPager初始化失败", e);
        }
    }

    /**
     * 更新Toolbar标题
     */
    private void updateToolbarTitle(int position) {
        if (position >= 0 && position < viewPagerAdapter.getItemCount()) {
            CharSequence title = viewPagerAdapter.getPageTitle(position);
            if (title != null) {
                binding.TopAppBar.setTitle(title + " ");
            }
        }
    }

    /**
     * 设置Toolbar标题
     */
    private void setTopAppBarTitle(String title) {
        //设置顶栏标题
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        toolbar.setTitle(title + " ");
    }

    /**
     * 配置模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewNavView));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSearch));
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
                        new MaterialAlertDialogBuilder(MainActivity.this, materialAlertDialogThemeStyleId)
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
     * 给按钮和卡片添加按压反馈动画
     * @return 是否拦截触摸事件
     */
    private boolean setPressAnimation(View v, MotionEvent event) {
        //setPress
        switch (event.getAction()) {
            // 按下：执行缩小动画（从当前大小开始）
            case MotionEvent.ACTION_DOWN:
                ViewAnimationUtils.playPressScaleAnimation(v, true);
                break;

            // 松开：执行恢复动画（从当前缩小的大小开始）
            case MotionEvent.ACTION_UP:
                ViewAnimationUtils.playPressScaleAnimation(v, false);
                break;

            // 取消（比如滑动离开View）：强制恢复动画
            case MotionEvent.ACTION_CANCEL:
                ViewAnimationUtils.playPressScaleAnimation(v, false);
                break;
        }

        return false;
    }

    /**
     * 显示卡片查询弹窗
     */
    private void showCardQueryDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_card_data, null);

        // 获取控件（替换为RecyclerView）
        TextInputEditText etCardName = dialogView.findViewById(R.id.textInputEditText);
        RecyclerView suggestionList = dialogView.findViewById(R.id.suggestion_list);

        // 初始化适配器（使用自定义Material风格适配器）
        SuggestionAdapter adapter = new SuggestionAdapter(new ArrayList<>(), selected -> {
            // 点击项自动填充输入框并隐藏列表
            etCardName.setText(selected);
            suggestionList.setVisibility(View.GONE);
        });

        // 配置RecyclerView
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        suggestionList.setAdapter(adapter);

        // 配置建议列表的布局：第一张卡片顶部距离增加10dp，最后一张卡片底部距离增加10dp
        CardItemDecoration itemDecoration = new CardItemDecoration(suggestionList, 20, 20);
        suggestionList.addItemDecoration(itemDecoration);

        // 实时模糊查询
        etCardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // 从数据库获取匹配结果
                    List<String> suggestions = dbHelper.searchCardNames(keyword);
                    // 更新适配器数据
                    adapter.updateData(suggestions);
                    suggestionList.setVisibility(View.VISIBLE);
                } else {
                    // 清空数据并隐藏列表
                    adapter.updateData(new ArrayList<>());
                    suggestionList.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 显示弹窗（保持原有逻辑）
        new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setTitle(getResources().getString(R.string.card_data_search_title))
                .setView(dialogView)
                .setPositiveButton("查询", (dialog, which) -> {
                    String cardName = Objects.requireNonNull(etCardName.getText()).toString().trim();
                    selectCardDataByName(cardName);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void selectCardDataByName(String cardName) {
        if (cardName.isEmpty()) {
            Toast.makeText(this, "请输入卡片名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String tableName = dbHelper.getCardTable(cardName);
        if (tableName == null) {
            Toast.makeText(this, "未找到该卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转详情页
        Intent intent = switch (tableName) {
            case "card_data_1" ->
                    new Intent(this, CardData_1_Activity.class);
            case "card_data_2" ->
                    new Intent(this, CardData_2_Activity.class);
            case "card_data_3" ->
                    new Intent(this, CardData_3_Activity.class);
            case "card_data_4" ->
                    new Intent(this, CardData_4_Activity.class);
            default -> null;
        };
        if (intent != null) {
            intent.putExtra("name", cardName);
            intent.putExtra("table", tableName);
            startActivity(intent);
        }
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