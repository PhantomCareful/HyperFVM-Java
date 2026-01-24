package com.careful.HyperFVM.Activities.NecessaryThings;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class CoContributorTeamActivity extends BaseActivity {

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
        setContentView(R.layout.activity_co_contributor_team);

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.label_about_app_co_construction_team) + " ");

        TextView CoContributorTeamTop = findViewById(R.id.CoContributorTeam_Top_Content);
        TextView CoContributorTeamContent = findViewById(R.id.CoContributorTeam_Content_Content);

        getContentFromAssets(this, CoContributorTeamTop, "CoContributorTeamTop.txt");
        getContentFromAssets(this, CoContributorTeamContent, "CoContributorTeamContent.txt");

        // 初始化动画效果
        TransitionSet transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

        LinearLayout CoContributorTeamContainer = findViewById(R.id.CoContributorTeam_Container);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(CoContributorTeamContainer, transition);
            findViewById(R.id.CoContributorTeam_Top_Container).setVisibility(View.VISIBLE);
            findViewById(R.id.CoContributorTeam_Content_Container).setVisibility(View.VISIBLE);
            findViewById(R.id.placeholder).setVisibility(View.GONE);
        }, 300);
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
}