package com.careful.HyperFVM.Activities.ThanksList;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class ThanksGameActivity extends BaseActivity {

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
        setContentView(R.layout.activity_thanks_game);

        // 添加模糊材质
        setupBlurEffect();

        // 跳转浏览器，前往陌路的哔哩哔哩主页
        findViewById(R.id.thanks_list_container_fvm_1).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_fvm_1_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_1_url)));

        // 跳转浏览器，前往夏夜的哔哩哔哩主页
        findViewById(R.id.thanks_list_container_fvm_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_fvm_2_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_2_url)));

        // 跳转浏览器，前往高清图楼帖子
        findViewById(R.id.thanks_list_container_fvm_3).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_fvm_3_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_3_url)));

        // 跳转浏览器，前往查黑系统网站
        findViewById(R.id.thanks_list_container_fvm_4).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                getResources().getString(R.string.title_thanks_list_fvm_4_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_4_url)));
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