package com.careful.HyperFVM.Activities.UpdateLogHistory;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UpdateLogHistoryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

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

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.label_update_log_history));

        tabLayout = findViewById(R.id.Tab_Layout);
        viewPager2 = findViewById(R.id.View_Page2);

        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        // 添加模糊材质
        setupBlurEffect();
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题、启用返回按钮
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> this.finish());
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
        blurUtil.setBlur(findViewById(R.id.blurViewTopTabLayout));
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new Version3UpdateLogFragment(), getResources().getString(R.string.label_version_3));
        adapter.addFragment(new Version2UpdateLogFragment(), getResources().getString(R.string.label_version_2));
        adapter.addFragment(new Version1UpdateLogFragment(), getResources().getString(R.string.label_version_1));
        adapter.addFragment(new Version0UpdateLogFragment(), getResources().getString(R.string.label_version_0));
    }
}