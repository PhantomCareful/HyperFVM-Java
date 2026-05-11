package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

public class CoContributorTeamActivity extends BaseActivity {

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
        setContentView(R.layout.activity_co_contributor_team);

        // 初始化各种装饰效果
        initDecoration();

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

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.getStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
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

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }
}