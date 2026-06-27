package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_DARK_MODE;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class DataImageOthersActivity extends BaseActivity {

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
        setupContainer(R.id.data_images_index_others_3_container, "data_image_others_3", false);
        setupContainer(R.id.data_images_index_others_4_container, "data_image_others_4", true);
        setupContainer(R.id.data_images_index_others_5_container, "data_image_others_5", false);
        setupContainer(R.id.data_images_index_others_6_container, "data_image_others_6", true);
        setupContainer(R.id.data_images_index_others_7_container, "data_image_others_7", true);
        setupContainer(R.id.data_images_index_others_8_container, "data_image_others_8", true);
    }

    private void setupContainer(int viewId, String imageName, boolean isDynamic) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> {
            File dir = new File(getFilesDir(), "data_images");
            File imageFile;
            if (isDynamic) {
                try(DBHelper dbHelper = new DBHelper(this)) {
                    // 根据深色模式动态加载对应的图片
                    int currentNightMode;
                    String darkMode = dbHelper.getSettingStringValue(CONTENT_DARK_MODE);
                    currentNightMode = switch (darkMode) {
                        case "总是开启\uD83C\uDF1A" -> Configuration.UI_MODE_NIGHT_YES;
                        case "总是关闭\uD83C\uDF1D" -> Configuration.UI_MODE_NIGHT_NO;
                        default -> getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    };
                    imageFile = (currentNightMode == Configuration.UI_MODE_NIGHT_YES) ?
                            new File(dir, imageName + "_dark.png") :
                            new File(dir, imageName + "_light.png");
                }
            } else {
                imageFile = new File(dir, imageName + ".png");
            }

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
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout data_images_index_container = findViewById(R.id.data_images_index_container);
        InsetsUtil.setMarginHorizontal(this, data_images_index_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) data_images_index_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            data_images_index_container.setLayoutParams(params);

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

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }
}