package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.ImageViewerActivity.ImageViewerActivity;
import com.careful.HyperFVM.Activities.ImageViewerActivity.ImageViewerDynamicActivity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.ImageResourcesUpdaterUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class DataImagesIndexActivity extends BaseActivity {
    private DBHelper dbHelper;

    private ImageResourcesUpdaterUtil imageUtil;
    private LinearLayout data_images_index_container;
    private Button update_image_action;
    private boolean isResourcesReady = false;
    private long localVersionCode;

    private TransitionSet transition;

    private int pressFeedbackAnimationDelay;

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
        setContentView(R.layout.activity_data_images_index);

        dbHelper = new DBHelper(this);
        imageUtil = ImageResourcesUpdaterUtil.getInstance();
        update_image_action = findViewById(R.id.update_image_resources_action);
        data_images_index_container = findViewById(R.id.data_images_index_container);

        // 初始化组件点击事件
        initViews();
    }

    private void initViews() {
        // 添加模糊效果
        setupBlurEffect();

        // 防御卡数据图
        setupContainer(R.id.data_images_index_card_0_1_container, "data_image_card_0_1", false);
        setupContainer(R.id.data_images_index_card_0_2_1_container, "data_image_card_0_2_1", false);
        setupContainer(R.id.data_images_index_card_0_2_2_container, "data_image_card_0_2_2", false);
        setupContainer(R.id.data_images_index_card_0_3_container, "data_image_card_0_3", false);
        setupContainer(R.id.data_images_index_card_1_container, "data_image_card_1", false);
        setupContainer(R.id.data_images_index_card_2_container, "data_image_card_2", false);
        setupContainer(R.id.data_images_index_card_3_container, "data_image_card_3", false);
        setupContainer(R.id.data_images_index_card_4_container, "data_image_card_4", false);
        setupContainer(R.id.data_images_index_card_5_container, "data_image_card_5", false);
        setupContainer(R.id.data_images_index_card_6_container, "data_image_card_6", false);
        setupContainer(R.id.data_images_index_card_7_container, "data_image_card_7", false);
        setupContainer(R.id.data_images_index_card_8_container, "data_image_card_8", false);
        setupContainer(R.id.data_images_index_card_9_container, "data_image_card_9", false);
        setupContainer(R.id.data_images_index_card_10_container, "data_image_card_10", false);
        setupContainer(R.id.data_images_index_card_11_container, "data_image_card_11", false);
        setupContainer(R.id.data_images_index_card_12_container, "data_image_card_12", false);
        setupContainer(R.id.data_images_index_card_13_container, "data_image_card_13", false);
        setupContainer(R.id.data_images_index_card_14_container, "data_image_card_14", false);
        setupContainer(R.id.data_images_index_card_15_container, "data_image_card_15", false);
        setupContainer(R.id.data_images_index_card_16_container, "data_image_card_16", false);
        setupContainer(R.id.data_images_index_card_17_container, "data_image_card_17", false);
        setupContainer(R.id.data_images_index_card_18_container, "data_image_card_18", false);
        setupContainer(R.id.data_images_index_card_19_container, "data_image_card_19", false);

        // 武器宝石数据图
        setupContainer(R.id.data_images_index_weapon_and_gem_0_1_container, "data_image_weapon_and_gem_0_1", false);
        setupContainer(R.id.data_images_index_weapon_and_gem_1_container, "data_image_weapon_and_gem_1", false);
        setupContainer(R.id.data_images_index_weapon_and_gem_2_container, "data_image_weapon_and_gem_2", false);
        setupContainer(R.id.data_images_index_weapon_and_gem_3_container, "data_image_weapon_and_gem_3", false);
        setupContainer(R.id.data_images_index_weapon_and_gem_4_container, "data_image_weapon_and_gem_4", false);
        setupContainer(R.id.data_images_index_weapon_and_gem_5_container, "data_image_weapon_and_gem_5", false);

        // 道具分解&兑换数据图
        setupContainer(R.id.data_images_index_decompose_and_get_1_container, "data_image_decompose_and_get_1", false);
        setupContainer(R.id.data_images_index_decompose_and_get_2_container, "data_image_decompose_and_get_2", false);
        setupContainer(R.id.data_images_index_decompose_and_get_3_container, "data_image_decompose_and_get_3", false);
        setupContainer(R.id.data_images_index_decompose_and_get_4_container, "data_image_decompose_and_get_4", false);
        setupContainer(R.id.data_images_index_decompose_and_get_5_container, "data_image_decompose_and_get_5", false);
        setupContainer(R.id.data_images_index_decompose_and_get_6_container, "data_image_decompose_and_get_6", false);
        setupContainer(R.id.data_images_index_decompose_and_get_7_container, "data_image_decompose_and_get_7", false);
        setupContainer(R.id.data_images_index_decompose_and_get_8_container, "data_image_decompose_and_get_8", false);
        setupContainer(R.id.data_images_index_decompose_and_get_9_container, "data_image_decompose_and_get_9", false);
        setupContainer(R.id.data_images_index_decompose_and_get_10_container, "data_image_decompose_and_get_10", false);
        setupContainer(R.id.data_images_index_decompose_and_get_11_container, "data_image_decompose_and_get_11", false);

        // 老输血量数据图
        setupContainer(R.id.data_images_index_mouse_hp_1_container, "data_image_mouse_hp_1", false);
        setupContainer(R.id.data_images_index_mouse_hp_2_container, "data_image_mouse_hp_2", false);
        setupContainer(R.id.data_images_index_mouse_hp_3_container, "data_image_mouse_hp_3", false);
        setupContainer(R.id.data_images_index_mouse_hp_4_container, "data_image_mouse_hp_4", false);
        setupContainer(R.id.data_images_index_mouse_hp_5_container, "data_image_mouse_hp_5", false);
        setupContainer(R.id.data_images_index_mouse_hp_6_container, "data_image_mouse_hp_6", false);
        setupContainer(R.id.data_images_index_mouse_hp_7_container, "data_image_mouse_hp_7", false);
        setupContainer(R.id.data_images_index_mouse_hp_8_container, "data_image_mouse_hp_8", false);
        setupContainer(R.id.data_images_index_mouse_hp_9_container, "data_image_mouse_hp_9", false);
        setupContainer(R.id.data_images_index_mouse_hp_10_container, "data_image_mouse_hp_10", false);

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

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(400); // 动画时长400ms
    }

    private void setupContainer(int viewId, String imageName, boolean isDynamic) {
        LinearLayout container = findViewById(viewId);
        container.setTag(imageName);
        container.setOnClickListener(v -> v.postDelayed(() -> {
            if (!isResourcesReady) {
                Toast.makeText(this, "还没有图片资源哦，请先更新", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;
            if (isDynamic) {
                intent = new Intent(this, ImageViewerDynamicActivity.class);
            } else {
                intent = new Intent(this, ImageViewerActivity.class);
            }
            intent.putExtra("imgPath", imageName);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));
    }

    private void checkVersion() {
        localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(this);

        // 检查本地资源是否就绪
        isResourcesReady = imageUtil.isResourcesReady(this);
    }

    private void getImageServerVersionAndCheckImageUpdate() {
        update_image_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        imageUtil.checkServerVersion(new ImageResourcesUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                runOnUiThread(() -> {
                    try {
                        if (serverVersion > localVersionCode) {
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_new));
                            update_image_action.setOnClickListener(v -> v.postDelayed(() -> {
                                Intent intent = new Intent(DataImagesIndexActivity.this, CheckUpdateActivity.class);
                                startActivity(intent);
                            }, pressFeedbackAnimationDelay));
                            showViewWithAnimation(update_image_action);
                        } else {
                            // 已是最新版本
                            hideViewWithAnimation(update_image_action);
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_current));
                        }
                    } catch (Exception e) {
                        update_image_action.setText("检查版本时发生错误");
                    }
                });
            }

            @Override
            public void onVersionCheckFailure(String errorMsg) {
                runOnUiThread(() -> update_image_action.setText("检查版本失败，请稍后再试"));
            }

            @Override
            public void onVersionParseError() {
                runOnUiThread(() -> update_image_action.setText("版本信息错误"));
            }
        });
    }

    /**
     * 给检查更新的按钮和文字添加动画
     */
    private void showViewWithAnimation(View view) {
        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start();
    }

    private void hideViewWithAnimation(View view) {
        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
        if (view.getVisibility() == View.GONE) return;
        view.animate()
                .alpha(0f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(400)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 在onResume阶段：
     * 1. 检查图片资源更新
     * 2. 设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 获取本地版本号
        checkVersion();
        // 检查图片资源是否有更新
        getImageServerVersionAndCheckImageUpdate();
        // 添加按压动画
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        findViewById(R.id.update_image_resources_action).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }
}