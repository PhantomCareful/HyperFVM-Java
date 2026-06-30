package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

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

    @SuppressLint("SetTextI18n")
    private void initViews() {
        // 设置子标题
        TextView textView;

        textView = findViewById(R.id.data_images_index_card_0_1_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_0_1_1));
        textView = findViewById(R.id.data_images_index_card_0_2_1_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_0_2_1_1));
        textView = findViewById(R.id.data_images_index_card_0_2_2_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_0_2_2_1));
        textView = findViewById(R.id.data_images_index_card_0_3_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_0_3_1));

        textView = findViewById(R.id.data_images_index_card_1_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_1_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_1_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_1_3)
        );

        textView = findViewById(R.id.data_images_index_card_2_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_2_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_2_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_2_3)
        );

        textView = findViewById(R.id.data_images_index_card_3_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_3_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_3_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_3_3)
        );

        textView = findViewById(R.id.data_images_index_card_4_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_4_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_4_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_4_3)
        );

        textView = findViewById(R.id.data_images_index_card_5_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_5_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_5_2)
        );

        textView = findViewById(R.id.data_images_index_card_6_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_6_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_6_2)
        );

        textView = findViewById(R.id.data_images_index_card_7_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_7_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_7_2)
        );

        textView = findViewById(R.id.data_images_index_card_8_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_8_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_8_2)
        );

        textView = findViewById(R.id.data_images_index_card_9_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_9_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_9_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_9_3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_9_4)
        );

        textView = findViewById(R.id.data_images_index_card_10_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_10_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_10_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_10_3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_10_4)
        );

        textView = findViewById(R.id.data_images_index_card_11_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_11_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_11_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_11_3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_11_4)
        );

        textView = findViewById(R.id.data_images_index_card_12_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_12_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_12_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_12_3)
        );

        textView = findViewById(R.id.data_images_index_card_12__description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_12__1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_12__2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_12__3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_12__4)
        );

        textView = findViewById(R.id.data_images_index_card_13_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_13_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_13_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_13_3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_13_4)
        );

        textView = findViewById(R.id.data_images_index_card_14_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_14_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_14_2)
        );

        textView = findViewById(R.id.data_images_index_card_15_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_15_1));
        textView = findViewById(R.id.data_images_index_card_16_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_16_1));
        textView = findViewById(R.id.data_images_index_card_17_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_17_1));
        textView = findViewById(R.id.data_images_index_card_18_description);
        textView.setText(getResources().getString(R.string.text_data_images_index_card_18_1));

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
            File dir = new File(getFilesDir(), "data_images");
            File imageFile = new File(dir, imageName + ".png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(
                        this,
                        getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title),
                        "❌",
                        getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content),
                        true,
                        "好的"
                );
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
                DialogBuilderManager.showDialog(
                        this,
                        getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title),
                        "❌",
                        getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content),
                        true,
                        "好的"
                );
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

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }
}