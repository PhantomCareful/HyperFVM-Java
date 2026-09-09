package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.DecomposeAndGetCalculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
        viewPager2.setOffscreenPageLimit(2);

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
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonDetailContainer = findViewById(R.id.FloatButton_Detail_Container);
        LinearLayout tabLayoutContainer = findViewById(R.id.TabLayout_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabLayoutContainer.getLayoutParams();
            params.bottomMargin = DensityUtil.dpToPx(this, 12) + height;
            tabLayoutContainer.setLayoutParams(params);
        });
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonDetailContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonDetailContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        ConstraintLayout decompose_and_get_calculator_for_animal_card_container = findViewById(R.id.decompose_and_get_calculator_for_animal_card_container);
        InsetsUtil.setMarginHorizontal(this, decompose_and_get_calculator_for_animal_card_container, layout_marginHorizontal -> {
            Log.d("updateLog", String.valueOf(layout_marginHorizontal));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonDetailContainer.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonDetailContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonDetail));
        blurUtil.setBlur(findViewById(R.id.blurViewTabLayout));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Detail_Container).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, cardName));
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