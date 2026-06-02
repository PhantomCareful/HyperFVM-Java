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

public class UsingInstructionActivity extends BaseActivity {
    private LinearLayout using_instruction_container;

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
        setContentView(R.layout.activity_using_instruction);

        // 初始化各种装饰效果
        initDecoration();

        TextView overview_top = findViewById(R.id.using_instruction_top);
        TextView overview1 = findViewById(R.id.using_instruction1);
        TextView overview2 = findViewById(R.id.using_instruction2);
        TextView overview3 = findViewById(R.id.using_instruction3);
        TextView overview4 = findViewById(R.id.using_instruction4);
        TextView overview5 = findViewById(R.id.using_instruction5);
        TextView overview6 = findViewById(R.id.using_instruction6);
        TextView overview7 = findViewById(R.id.using_instruction7);

        getContentFromAssets(this, overview_top, "QATop.txt");
        getContentFromAssets(this, overview1, "QA1.txt");
        getContentFromAssets(this, overview2, "QA2.txt");
        getContentFromAssets(this, overview3, "QA3.txt");
        getContentFromAssets(this, overview4, "QA4.txt");
        getContentFromAssets(this, overview5, "QA5.txt");
        getContentFromAssets(this, overview6, "QA6.txt");
        getContentFromAssets(this, overview7, "QA7.txt");

        // 初始化动画效果
        TransitionSet transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(using_instruction_container, transition);
            findViewById(R.id.using_instruction_top_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction1_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction2_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction3_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction4_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction5_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction6_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction7_container).setVisibility(View.VISIBLE);
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
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        using_instruction_container = findViewById(R.id.using_instruction_container);
        InsetsUtil.setMarginHorizontal(this, using_instruction_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) using_instruction_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            using_instruction_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
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