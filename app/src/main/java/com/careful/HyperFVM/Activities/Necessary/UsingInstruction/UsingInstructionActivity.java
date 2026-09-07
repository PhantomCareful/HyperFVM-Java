package com.careful.HyperFVM.Activities.Necessary.UsingInstruction;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

public class UsingInstructionActivity extends BaseActivity {
    private BlurUtil blurUtil;

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

        // 设置点击事件：均跳转UsingInstructionDetailActivity，但是要传递不同的QA文件名
        findViewById(R.id.using_instruction_1).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA1.txt");
            startActivity(intent);
        });
        findViewById(R.id.using_instruction_2).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA2.txt");
            startActivity(intent);
        });
        findViewById(R.id.using_instruction_3).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA3.txt");
            startActivity(intent);
        });
        findViewById(R.id.using_instruction_4).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA4.txt");
            startActivity(intent);
        });
        findViewById(R.id.using_instruction_5).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA5.txt");
            startActivity(intent);
        });
        findViewById(R.id.using_instruction_6).setOnClickListener(v -> {
            Intent intent = new Intent(this, UsingInstructionDetailActivity.class);
            intent.putExtra("QAFileName", "QA6.txt");
            startActivity(intent);
        });
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
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
        using_instruction_container = findViewById(R.id.using_instruction_container);
        InsetsUtil.setMarginHorizontal(this, using_instruction_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) using_instruction_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            using_instruction_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_using_instruction).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
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