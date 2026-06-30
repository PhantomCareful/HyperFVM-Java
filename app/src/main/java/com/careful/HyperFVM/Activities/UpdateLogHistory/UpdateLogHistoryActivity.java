package com.careful.HyperFVM.Activities.UpdateLogHistory;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UpdateLogHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        //小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_update_log_history);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.View_Page2);

        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setOffscreenPageLimit(4);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        // 初始化各种装饰效果
        initDecoration();

    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new Version4UpdateLogFragment(), getResources().getString(R.string.label_version_4));
        adapter.addFragment(new Version3UpdateLogFragment(), getResources().getString(R.string.label_version_3));
        adapter.addFragment(new Version2UpdateLogFragment(), getResources().getString(R.string.label_version_2));
        adapter.addFragment(new Version1UpdateLogFragment(), getResources().getString(R.string.label_version_1));
        adapter.addFragment(new Version0UpdateLogFragment(), getResources().getString(R.string.label_version_0));
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("DiscouragedApi")
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        LinearLayout tabLayoutContainer = findViewById(R.id.TabLayout_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        ConstraintLayout update_log_history_container = findViewById(R.id.update_log_history_container);
        InsetsUtil.setMarginHorizontal(this, update_log_history_container, layout_marginHorizontal -> {
            Log.d("updateLog", String.valueOf(layout_marginHorizontal));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) tabLayoutContainer.getLayoutParams();
            params.topMargin = layout_marginHorizontal;
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            tabLayoutContainer.setLayoutParams(params);
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
        blurUtil.setBlur(findViewById(R.id.blurViewTabLayout));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }

}