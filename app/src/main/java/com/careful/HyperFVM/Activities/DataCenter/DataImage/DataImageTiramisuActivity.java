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

public class DataImageTiramisuActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_data_image_tiramisu_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_data_image_tiramisu_scroll_y_pad_1";// PAD 左栏（scrollView1）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_data_image_tiramisu_scroll_y_pad_2";// PAD 右栏（scrollView2）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;
    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（仅手机布局接入，PAD 双栏布局不接入）

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
        setupContainer(R.id.tiramisu_image_2_3_container, "tiramisu_image_2_3");
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
        setupContainer(R.id.tiramisu_image_2_5_container, "tiramisu_image_2_5");
    }

    private void setupContainer(int viewId, String imageName) {
        LinearLayout container = findViewById(viewId);
        container.setOnClickListener(v -> DataImageViewerHelper.openDataImage(this, imageName));
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

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            nestedScrollUtil = NestedScrollUtil.attach(
                    findViewById(R.id.scrollView),
                    findViewById(R.id.topBarBottom),
                    topBar,
                    blurViewTopBar,
                    TOP_BAR_FADE_RANGE_DP);
        }

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_data_image_tiramisu).setOnTouchListener((v, event) ->
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
        } else {
            // PAD 双栏：不接入联动，仅保存两栏滚动位置（滚动容器按 id 存在性处理，各页 PAD 布局栏数不同）
            View scrollView1 = findViewById(R.id.scrollView1);
            if (scrollView1 != null) {
                outState.putInt(STATE_SCROLL_Y_PAD_1, scrollView1.getScrollY());
            }
            View scrollView2 = findViewById(R.id.scrollView2);
            if (scrollView2 != null) {
                outState.putInt(STATE_SCROLL_Y_PAD_2, scrollView2.getScrollY());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
            return;
        }

        if (SmallestWidthUtil.getSmallestWidthDp() >= 600) {
            View scrollView1 = findViewById(R.id.scrollView1);
            if (scrollView1 != null) {
                int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y_PAD_1, 0);
                if (scrollY > 0) {
                    scrollView1.setScrollY(scrollY);
                }
            }
            View scrollView2 = findViewById(R.id.scrollView2);
            if (scrollView2 != null) {
                int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y_PAD_2, 0);
                if (scrollY > 0) {
                    scrollView2.setScrollY(scrollY);
                }
            }
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