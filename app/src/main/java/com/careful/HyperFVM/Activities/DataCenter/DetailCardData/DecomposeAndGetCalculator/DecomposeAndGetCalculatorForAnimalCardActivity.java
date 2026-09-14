package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.DecomposeAndGetCalculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import eightbitlab.com.blurview.BlurView;

public class DecomposeAndGetCalculatorForAnimalCardActivity extends BaseActivity {
    private BlurUtil blurUtil;

    private String cardName;
    private String decomposeItemName;
    private String[] imageIdsArray;
    private int[] decomposeDataArray;
    private int[] getDataArray;

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
        setContentView(R.layout.activity_decompose_and_get_calculator_for_animal_card);

        // 从 Intent 取出数据
        cardName = getIntent().getStringExtra("card_name");
        decomposeItemName = getIntent().getStringExtra("decompose_item_name");
        imageIdsArray = getIntent().getStringArrayExtra("image_ids");
        decomposeDataArray = getIntent().getIntArrayExtra("decompose_data");
        getDataArray = getIntent().getIntArrayExtra("get_data");

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.View_Page2);

        // 创建 Fragment 并传入参数
        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setOffscreenPageLimit(1);

        // 设置Tab文字颜色：选中=colorOnSurface，未选中=其50%半透明（文字颜色不受TextAppearance控制，见MyTabTextAppearance注释）
        TabLayoutUtil.setOnSurfaceTextColors(this, tabLayout);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        // 初始化各种装饰效果
        initDecoration();
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        // 添加Fragment对应的标题，按标签顺序
        adapter.addFragment(new DecomposeCalculatorForAnimalCardFragment().newInstance(decomposeItemName, imageIdsArray, decomposeDataArray), getResources().getString(R.string.title_decompose_calculator));
        adapter.addFragment(new GetCalculatorForAnimalCardFragment().newInstance(decomposeItemName, imageIdsArray, getDataArray), getResources().getString(R.string.title_get_calculator));
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
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        ImageButton floatButtonDetail = findViewById(R.id.FloatButton_Detail);
        MaterialCardView tabLayoutContainer = findViewById(R.id.tabLayoutContainer);
        View rootView = findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabLayoutContainer.getLayoutParams();
            params.bottomMargin = height;
            tabLayoutContainer.setLayoutParams(params);
        });
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonDetail.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonDetail.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        ConstraintLayout decompose_and_get_calculator_for_animal_card_container = findViewById(R.id.decompose_and_get_calculator_for_animal_card_container);
        InsetsUtil.setMarginHorizontal(this, decompose_and_get_calculator_for_animal_card_container, layout_marginHorizontal -> {
            Log.d("updateLog", String.valueOf(layout_marginHorizontal));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonDetail.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonDetail.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonDetail.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, cardName));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0.5f);
        blurUtil.setBlur(findViewById(R.id.blurViewTabLayout), 0f);
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}