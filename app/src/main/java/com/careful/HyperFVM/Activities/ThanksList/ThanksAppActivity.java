package com.careful.HyperFVM.Activities.ThanksList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;

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
                ContextCompat.getDrawable(this, R.drawable.ic_github),
                0,
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_sub_title_thanks_list_app_1),
                getResources().getString(R.string.dialog_url_thanks_list_app_1)));

        //跳转浏览器，前往HyperCeiler仓库
        findViewById(R.id.thanks_list_container_app_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                ContextCompat.getDrawable(this, R.drawable.ic_github),
                0,
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_sub_title_thanks_list_app_2),
                getResources().getString(R.string.dialog_url_thanks_list_app_2)));

        //跳转浏览器，前往BlurView仓库
        findViewById(R.id.thanks_list_container_app_3).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                ContextCompat.getDrawable(this, R.drawable.ic_github),
                0,
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_sub_title_thanks_list_app_3),
                getResources().getString(R.string.dialog_url_thanks_list_app_3)));

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
        LinearLayout CoContributorTeam_Container = findViewById(R.id.CoContributorTeam_Container);
        InsetsUtil.setMarginHorizontal(this, CoContributorTeam_Container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) CoContributorTeam_Container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            CoContributorTeam_Container.setLayoutParams(params);

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