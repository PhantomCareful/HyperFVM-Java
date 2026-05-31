package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.ImageViewerActivity.ImageViewerActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.ImageViewerActivity.ImageViewerDynamicActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

public class DataImageOthersActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_data_image_others);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化点击事件
        initViews();
    }

    private void initViews() {
        // 其他数据图
        setupContainer(R.id.data_images_index_others_1_container, "data_image_others_1", true);
        setupContainer(R.id.data_images_index_others_2_container, "data_image_others_2", true);
        setupContainer(R.id.data_images_index_others_3_container, "data_image_others_3", true);
        setupContainer(R.id.data_images_index_others_4_container, "data_image_others_4", false);
        setupContainer(R.id.data_images_index_others_5_container, "data_image_others_5", true);
        setupContainer(R.id.data_images_index_others_6_container, "data_image_others_6", false);
        setupContainer(R.id.data_images_index_others_7_container, "data_image_others_7", true);
        setupContainer(R.id.data_images_index_others_8_container, "data_image_others_8", true);
        setupContainer(R.id.data_images_index_others_9_container, "data_image_others_9", true);
    }

    private void setupContainer(int viewId, String imageName, boolean isDynamic) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> {
            Intent intent;
            if (isDynamic) {
                intent = new Intent(this, ImageViewerDynamicActivity.class);
            } else {
                intent = new Intent(this, ImageViewerActivity.class);
            }
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