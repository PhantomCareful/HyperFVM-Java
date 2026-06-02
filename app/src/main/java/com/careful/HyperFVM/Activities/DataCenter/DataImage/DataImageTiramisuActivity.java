package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class DataImageTiramisuActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_data_image_tiramisu);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化点击事件
        initViews();
    }

    private void initViews() {
        // 米鼠的图
        setupContainer(R.id.tiramisu_image_1_1_container, "tiramisu_image_1_1");
        setupContainer(R.id.tiramisu_image_1_2_container, "tiramisu_image_1_2");
        setupContainer(R.id.tiramisu_image_1_3_1_container, "tiramisu_image_1_3_1");
        setupContainer(R.id.tiramisu_image_1_3_2_container, "tiramisu_image_1_3_2");
        setupContainer(R.id.tiramisu_image_1_3_3_container, "tiramisu_image_1_3_3");
        setupContainer(R.id.tiramisu_image_1_4_container, "tiramisu_image_1_4");
        setupContainer(R.id.tiramisu_image_1_5_container, "tiramisu_image_1_5");
        setupContainer(R.id.tiramisu_image_1_6_container, "tiramisu_image_1_6");
        setupContainer(R.id.tiramisu_image_1_7_container, "tiramisu_image_1_7");
        setupContainer(R.id.tiramisu_image_2_1_container, "tiramisu_image_2_1");
        setupContainer(R.id.tiramisu_image_2_2_container, "tiramisu_image_2_2");
        setupContainer(R.id.tiramisu_image_2_3_1_container, "tiramisu_image_2_3_1");
        setupContainer(R.id.tiramisu_image_2_3_2_container, "tiramisu_image_2_3_2");
        setupContainer(R.id.tiramisu_image_2_3_3_container, "tiramisu_image_2_3_3");
        setupContainer(R.id.tiramisu_image_2_3_4_container, "tiramisu_image_2_3_4");
        setupContainer(R.id.tiramisu_image_2_4_1_container, "tiramisu_image_2_4_1");
        setupContainer(R.id.tiramisu_image_2_4_2_container, "tiramisu_image_2_4_2");
        setupContainer(R.id.tiramisu_image_2_4_3_container, "tiramisu_image_2_4_3");
        setupContainer(R.id.tiramisu_image_2_4_4_container, "tiramisu_image_2_4_4");
        setupContainer(R.id.tiramisu_image_2_4_5_container, "tiramisu_image_2_4_5");
        setupContainer(R.id.tiramisu_image_2_4_6_container, "tiramisu_image_2_4_6");
        setupContainer(R.id.tiramisu_image_2_4_7_container, "tiramisu_image_2_4_7");
        setupContainer(R.id.tiramisu_image_2_4_8_container, "tiramisu_image_2_4_8");
        setupContainer(R.id.tiramisu_image_2_4_9_container, "tiramisu_image_2_4_9");
        setupContainer(R.id.tiramisu_image_2_4_10_container, "tiramisu_image_2_4_10");
        setupContainer(R.id.tiramisu_image_2_4_11_container, "tiramisu_image_2_4_11");
        setupContainer(R.id.tiramisu_image_2_4_12_container, "tiramisu_image_2_4_12");
        setupContainer(R.id.tiramisu_image_2_4_13_container, "tiramisu_image_2_4_13");
        setupContainer(R.id.tiramisu_image_2_4_14_container, "tiramisu_image_2_4_14");
        setupContainer(R.id.tiramisu_image_2_4_15_container, "tiramisu_image_2_4_15");
        setupContainer(R.id.tiramisu_image_2_4_16_container, "tiramisu_image_2_4_16");
    }

    private void setupContainer(int viewId, String imageName) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> {
            File dir = new File(getFilesDir(), "data_images");
            File imageFile = new File(dir, imageName + ".png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(this, getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(this, getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
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