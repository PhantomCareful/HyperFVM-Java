package com.careful.HyperFVM;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.careful.HyperFVM.Fragments.AboutApp.AboutAppEffectFragment;
import com.careful.HyperFVM.Fragments.AboutApp.AboutAppFragment;
import com.careful.HyperFVM.Fragments.DataCenter.DataCenterFragment;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.NoPaddingBottomNavigationView.NoPaddingBottomNavigationView;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.BadgeDotUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForSafety.SignatureChecker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.databinding.ActivityMainBinding;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private DBHelper dbHelper;
    private List<Integer> menuOrder; // 导航菜单顺序
    private BottomNavigationView navView;

    // 新增：ViewPager2相关
    private ViewPager2 viewPager;
    private TabLayoutFragmentStateAdapter viewPagerAdapter;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainHandler = new Handler(Looper.getMainLooper()); // 初始化主线程 Handler
        dbHelper = new DBHelper(this);

        // 启动时进行签名校验
        new Thread(() -> {
            if (!SignatureChecker.verifyAppSignature(this)) {
                // 在主线程中显示对话框提示
                mainHandler.post(() -> DialogBuilderManager.showSignatureCheckerDialog(this));
            }
        }).start();

        // 应用主题（必须在super.onCreate前）
        DarkModeManager.applyDarkMode(this);
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        // 小白条沉浸（MIUI/澎湃OS适配）
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        // 布局初始化
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 适配导航栏高度
        LinearLayout navigationBarContainer = findViewById(R.id.navigation_bar_container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.getNavigationBarHeight(rootView, height -> {
            Log.d("height", "height in MainActivity = " + height);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) navigationBarContainer.getLayoutParams();
            params.bottomMargin = DensityUtil.dpToPx(this, 12) + height;
            navigationBarContainer.setLayoutParams(params);
        });

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

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnClickListener(v -> DialogBuilderManager.showCardQueryDialog(this));
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewPagerAdapter.addFragment(new AboutAppEffectFragment(), "");
            } else {
                viewPagerAdapter.addFragment(new AboutAppFragment(), getResources().getString(R.string.top_bar_about_app));
            }
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
     * 检查App更新和图片资源更新，如果其中任何一个有更新，则在底栏的图标上添加小红点
     *
     */
    private void checkUpdate() {
        NoPaddingBottomNavigationView bottomNav = findViewById(R.id.nav_view);

        BadgeDotUtil.checkUpdateAndShowRedDot(this, isShowRedDot -> {
            if (isShowRedDot) {
                BadgeDotUtil.showRedDot(bottomNav, 1);
            } else {
                BadgeDotUtil.hideRedDot(bottomNav, 1);
            }
        });
    }

    /**
     * 更新Toolbar标题
     */
    private void updateToolbarTitle(int position) {
        if (position >= 0 && position < viewPagerAdapter.getItemCount()) {
            CharSequence title = viewPagerAdapter.getPageTitle(position);
            if (title != null) {
                if (position == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    findViewById(R.id.status_bar_gradient).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.status_bar_gradient).setVisibility(View.VISIBLE);
                }
                setTopAppBarTitle(String.valueOf(title));
            }
        }
    }

    /**
     * 设置Toolbar标题
     */
    private void setTopAppBarTitle(String title) {
        //设置顶栏标题
        TextView topAppBar = findViewById(R.id.Top_AppBar);
        topAppBar.setText(title);
    }

    /**
     * 配置模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewNavView));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSearch));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();

        // 检查更新
        new Thread(this::checkUpdate).start();
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
    }
}