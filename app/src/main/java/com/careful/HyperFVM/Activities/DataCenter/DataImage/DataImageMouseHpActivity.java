package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDataImage.DataImageViewerHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

public class DataImageMouseHpActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_data_image_mouse_hp_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）
    private static final int TOP_BAR_FADE_RANGE_DP_PAD = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;
    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（手机三组件全联动；PAD 仅模糊层与右栏 scrollView2 联动）

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
        setContentView(R.layout.activity_data_image_mouse_hp);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化点击事件
        initViews();
    }

    private void initViews() {
        // 老输血量数据图
        setupContainer(R.id.data_images_index_mouse_hp_1_container, "data_image_mouse_hp_1");
        setupContainer(R.id.data_images_index_mouse_hp_2_container, "data_image_mouse_hp_2");
        setupContainer(R.id.data_images_index_mouse_hp_3_container, "data_image_mouse_hp_3");
        setupContainer(R.id.data_images_index_mouse_hp_4_container, "data_image_mouse_hp_4");
        setupContainer(R.id.data_images_index_mouse_hp_5_container, "data_image_mouse_hp_5");
        setupContainer(R.id.data_images_index_mouse_hp_6_container, "data_image_mouse_hp_6");
        setupContainer(R.id.data_images_index_mouse_hp_7_container, "data_image_mouse_hp_7");
        setupContainer(R.id.data_images_index_mouse_hp_8_container, "data_image_mouse_hp_8");
        setupContainer(R.id.data_images_index_mouse_hp_9_container, "data_image_mouse_hp_9");
        setupContainer(R.id.data_images_index_mouse_hp_10_container, "data_image_mouse_hp_10");
    }

    private void setupContainer(int viewId, String imageName) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> DataImageViewerHelper.openSystemPhotoViewerToSeeDataImages(this, imageName));
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：手机单栏三组件全联动；PAD 仅模糊层与右栏（scrollView2）联动
        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            nestedScrollUtil = NestedScrollUtil.attach(
                    findViewById(R.id.scrollView),
                    findViewById(R.id.topBarBottom),
                    topBar,
                    blurViewTopBar,
                    TOP_BAR_FADE_RANGE_DP);
        } else {
            nestedScrollUtil = NestedScrollUtil.attach(rootView,
                    R.id.scrollView2, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP_PAD);
        }

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_data_image_mouse_hp).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}