package com.careful.HyperFVM.Activities.ThanksList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

public class ThanksAppActivity extends BaseActivity {

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
        setContentView(R.layout.activity_thanks_app);

        // 初始化各种装饰效果
        initDecoration();

        //跳转浏览器，前往miuix仓库
        findViewById(R.id.thanks_list_container_app_1).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_app_1_dialog),
                getResources().getString(R.string.label_thanks_list_app_1_url)));

        //跳转浏览器，前往HyperCeiler仓库
        findViewById(R.id.thanks_list_container_app_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_app_2_dialog),
                getResources().getString(R.string.label_thanks_list_app_2_url)));

        //跳转浏览器，前往BlurView仓库
        findViewById(R.id.thanks_list_container_app_3).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_app_3_dialog),
                getResources().getString(R.string.label_thanks_list_app_3_url)));

        //跳转浏览器，前往ZoomImageView仓库
        findViewById(R.id.thanks_list_container_app_4).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_app_4_dialog),
                getResources().getString(R.string.label_thanks_list_app_4_url)));

        //跳转浏览器，前往SpringBackScrollView文章
        findViewById(R.id.thanks_list_container_app_5).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_app_5_dialog),
                getResources().getString(R.string.label_thanks_list_app_5_url)));
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