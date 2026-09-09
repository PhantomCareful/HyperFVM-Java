package com.careful.HyperFVM;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.careful.HyperFVM.Fragments.AboutApp.AboutAppEffectFragment;
import com.careful.HyperFVM.Fragments.AboutApp.AboutAppFragment;
import com.careful.HyperFVM.Fragments.Dashboard.DashboardFragment;
import com.careful.HyperFVM.Fragments.DataCenter.DataCenterFragment;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.BadgeDotUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.ForSafety.SignatureChecker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.databinding.ActivityMainBinding;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;

public class MainActivity extends BaseActivity {
    // 底栏 Tab 的图标资源（必须与添加 Fragment 的顺序一一对应）
    private static final int[] BOTTOM_TAB_ICON_RES = {
            R.drawable.ic_dashboard,
            R.drawable.ic_horizontal_split,
            R.drawable.ic_notes
    };
    // 底栏 Tab 的无障碍描述资源（纯图片 Tab 需要它来朗读含义）
    private static final int[] BOTTOM_TAB_DESCRIPTION_RES = {
            R.string.label_dashboard_navigation,
            R.string.label_data_center_navigation,
            R.string.label_about_app_navigation
    };

    private ActivityMainBinding binding;
    private TabLayout tabLayout;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainHandler = new Handler(Looper.getMainLooper()); // 初始化主线程 Handler

        // 启动时进行签名校验
        new Thread(() -> {
            if (!SignatureChecker.verifyAppSignature(this)) {
                // 在主线程中显示对话框提示
                mainHandler.post(() -> DialogBuilderManager.showSignatureCheckerDialog(this));
            }
        }).start();

        // 应用主题（必须在super.onCreate前）
        DarkModeManager.applyDarkMode();
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

        // 确保视图加载完成后初始化ViewPager（避免空指针）
        setupViewPager();

        // 初始化各种装饰效果
        initDecoration();

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_Search).setOnClickListener(v -> DialogBuilderManager.showCardQueryDialog(this));
        // 分解兑换计算器查询按钮
        findViewById(R.id.FloatButton_CardDataDecomposeAndGetSearch).setOnClickListener(v -> DialogBuilderManager.showDecomposeAndGetQueryDialog(this));
    }

    /**
     * 初始化ViewPager2，并通过TabLayoutMediator与底栏纯图片TabLayout双向绑定
     * （点击Tab切换页面、页面切换同步选中Tab均由Mediator完成）
     */
    private void setupViewPager() {
        try {
            ViewPager2 viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.tabLayout);

            // 初始化适配器
            TabLayoutFragmentStateAdapter viewPagerAdapter = new TabLayoutFragmentStateAdapter(this);

            // 添加Fragment
            viewPagerAdapter.addFragment(new DashboardFragment(), getResources().getString(R.string.top_bar_dashboard));
            viewPagerAdapter.addFragment(new DataCenterFragment(), getResources().getString(R.string.top_bar_data_center));
            if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
                viewPagerAdapter.addFragment(new AboutAppEffectFragment(), getResources().getString(R.string.top_bar_about_app));
            } else {
                viewPagerAdapter.addFragment(new AboutAppFragment(), getResources().getString(R.string.top_bar_about_app));
            }
            viewPager.setAdapter(viewPagerAdapter);

            // 禁用预加载相邻页面（可选，减少内存使用）
            viewPager.setOffscreenPageLimit(2);

            // 禁用ViewPager2的滚动动画（如果不想让用户滑动）
            viewPager.setUserInputEnabled(false);

            // 图标高亮依赖自定义视图内 ImageView 的 selected 状态（驱动tab_icon_tint着色），
            // TabLayout 不会自动同步它，因此监听器必须先于 attach() 注册
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    setTabIconSelected(tab, true);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    setTabIconSelected(tab, false);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    // 无需处理
                }
            });

            // 为每个Tab组装纯图片自定义视图
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                View customView = getLayoutInflater().inflate(R.layout.view_main_bottom_tab, tabLayout, false);
                ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                tabIcon.setImageResource(BOTTOM_TAB_ICON_RES[position]);
                tab.setCustomView(customView);
                tab.setContentDescription(getResources().getString(BOTTOM_TAB_DESCRIPTION_RES[position]));
            }).attach();

            // 首次进入时若尚无选中的Tab，手动选中第0个以触发图标高亮
            if (tabLayout.getTabCount() > 0 && tabLayout.getSelectedTabPosition() == -1) {
                tabLayout.selectTab(tabLayout.getTabAt(0));
            }

        } catch (Exception e) {
            Log.e("ViewPagerSetup", "ViewPager初始化失败", e);
        }
    }

    /**
     * 同步Tab自定义视图内图标的选中状态，驱动tab_icon_tint的着色切换
     */
    private void setTabIconSelected(TabLayout.Tab tab, boolean selected) {
        View customView = tab.getCustomView();
        if (customView == null) return;
        ImageView tabIcon = customView.findViewById(R.id.tab_icon);
        if (tabIcon != null) {
            tabIcon.setSelected(selected);
        }
    }

    /**
     * 检查App更新和图片资源更新，如果其中任何一个有更新，则在底栏的图标上添加小红点
     */
    private void checkUpdate() {
        TabLayout bottomTabLayout = findViewById(R.id.tabLayout);

        BadgeDotUtil.checkUpdateAndShowRedDot(this, isShowRedDot -> {
            if (isShowRedDot) {
                BadgeDotUtil.showRedDot(bottomTabLayout, 2);
            } else {
                BadgeDotUtil.hideRedDot(bottomTabLayout, 2);
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
        // 适配导航栏高度
        LinearLayout navigationBarContainer = findViewById(R.id.navigation_bar_container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            Log.d("height", "height in MainActivity = " + height);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) navigationBarContainer.getLayoutParams();
            params.bottomMargin = height;
            navigationBarContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 配置模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewNavView), 0f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSearch), 0f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonDecomposeAndGetSearch), 0f);
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
    }
}