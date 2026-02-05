package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.ImageViewerActivity.ImageViewerActivity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.DataImagesUpdaterUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class TiramisuImageActivity extends BaseActivity {

    private DBHelper dbHelper;
    private DataImagesUpdaterUtil imageUtil;
    private LinearLayout tools_tiramisu_image_container;
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
        // 加载布局文件
        setContentView(R.layout.activity_tiramisu_image);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        dbHelper = new DBHelper(this);
        imageUtil = DataImagesUpdaterUtil.getInstance();
        update_image_action = findViewById(R.id.update_image_resources_action);
        tools_tiramisu_image_container = findViewById(R.id.tools_tiramisu_image_container);

        initViews();
    }

    private void initViews() {
        // 添加模糊材质
        setupBlurEffect();

        setupContainer(R.id.tiramisu_image_1_container, "tiramisu_image_1");
        setupContainer(R.id.tiramisu_image_2_container, "tiramisu_image_2");
        setupContainer(R.id.tiramisu_image_3_1_container, "tiramisu_image_3_1");
        setupContainer(R.id.tiramisu_image_3_2_container, "tiramisu_image_3_2");
        setupContainer(R.id.tiramisu_image_3_3_container, "tiramisu_image_3_3");
        setupContainer(R.id.tiramisu_image_4_container, "tiramisu_image_4");
        setupContainer(R.id.tiramisu_image_5_container, "tiramisu_image_5");
        setupContainer(R.id.tiramisu_image_6_container, "tiramisu_image_6");
        setupContainer(R.id.tiramisu_image_7_1_container, "tiramisu_image_7_1");
        setupContainer(R.id.tiramisu_image_7_2_container, "tiramisu_image_7_2");
        setupContainer(R.id.tiramisu_image_7_3_container, "tiramisu_image_7_3");
        setupContainer(R.id.tiramisu_image_7_4_container, "tiramisu_image_7_4");

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(400); // 动画时长400ms
    }

    private void setupContainer(int viewId, String imageName) {
        LinearLayout container = findViewById(viewId);
        container.setTag(imageName);
        container.setOnClickListener(v -> v.postDelayed(() -> {
            if (!isResourcesReady) {
                Toast.makeText(this, "还没有图片资源哦，请先更新", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("imgPath", imageName);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));
    }

    private void checkVersion() {
        // 获取本地版本号
        String localVersion = dbHelper.getDataStationValue("DataImagesVersionCode");
        if (localVersion == null) {
            localVersionCode = 0;
        } else {
            localVersionCode = Long.parseLong(localVersion);
        }

        // 检查本地资源是否就绪
        isResourcesReady = imageUtil.isResourcesReady(this);
    }

    private void getImageServerVersionAndCheckImageUpdate() {
        update_image_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        imageUtil.checkServerVersion(new DataImagesUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                runOnUiThread(() -> {
                    try {
                        TransitionManager.beginDelayedTransition(tools_tiramisu_image_container, transition);
                        if (serverVersion > localVersionCode) {
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_new));
                            update_image_action.setOnClickListener(v -> v.postDelayed(() -> {
                                Intent intent = new Intent(TiramisuImageActivity.this, CheckUpdateActivity.class);
                                startActivity(intent);
                            }, pressFeedbackAnimationDelay));
                            showViewWithAnimation(update_image_action);
                        } else {
                            // 已是最新版本
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_current));
                            hideViewWithAnimation(update_image_action);
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