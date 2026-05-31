package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.ImageViewerActivity.ImageViewerActivity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

public class DataImageCardActivity extends BaseActivity {

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
        setContentView(R.layout.activity_data_image_card);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化点击事件
        initViews();
    }

    private void initViews() {
        // 防御卡数据图
        setupContainer(R.id.data_images_index_card_0_1_container, "data_image_card_0_1");
        setupContainer(R.id.data_images_index_card_0_2_1_container, "data_image_card_0_2_1");
        setupContainer(R.id.data_images_index_card_0_2_2_container, "data_image_card_0_2_2");
        setupContainer(R.id.data_images_index_card_0_3_container, "data_image_card_0_3");
        setupContainer(R.id.data_images_index_card_1_container, "data_image_card_1");
        setupContainer(R.id.data_images_index_card_2_container, "data_image_card_2");
        setupContainer(R.id.data_images_index_card_3_container, "data_image_card_3");
        setupContainer(R.id.data_images_index_card_4_container, "data_image_card_4");
        setupContainer(R.id.data_images_index_card_5_container, "data_image_card_5");
        setupContainer(R.id.data_images_index_card_6_container, "data_image_card_6");
        setupContainer(R.id.data_images_index_card_7_container, "data_image_card_7");
        setupContainer(R.id.data_images_index_card_8_container, "data_image_card_8");
        setupContainer(R.id.data_images_index_card_9_container, "data_image_card_9");
        setupContainer(R.id.data_images_index_card_10_container, "data_image_card_10");
        setupContainer(R.id.data_images_index_card_11_container, "data_image_card_11");
        setupContainer(R.id.data_images_index_card_12_container, "data_image_card_12");
        setupContainer(R.id.data_images_index_card_12__container, "data_image_card_12_");
        setupContainer(R.id.data_images_index_card_13_container, "data_image_card_13");
        setupContainer(R.id.data_images_index_card_14_container, "data_image_card_14");
        setupContainer(R.id.data_images_index_card_15_container, "data_image_card_15");
        setupContainer(R.id.data_images_index_card_16_container, "data_image_card_16");
        setupContainer(R.id.data_images_index_card_17_container, "data_image_card_17");
        setupContainer(R.id.data_images_index_card_18_container, "data_image_card_18");
    }

    private void setupContainer(int viewId, String imageName) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("imgPath", imageName);
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
    @SuppressLint("DiscouragedApi")
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

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }
}