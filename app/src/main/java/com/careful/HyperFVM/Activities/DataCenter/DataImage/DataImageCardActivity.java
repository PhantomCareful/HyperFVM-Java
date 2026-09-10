package com.careful.HyperFVM.Activities.DataCenter.DataImage;

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
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

public class DataImageCardActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_data_image_card_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_data_image_card_scroll_y_pad_1";// PAD 左栏（scrollView1）滚动位置保存键：左栏不参与联动，但 ScrollView 不自存滚动状态，需重建后恢复
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
        textView.setText(getResources().getString(R.string.description_data_images_index_card_0_1));
        textView = findViewById(R.id.data_images_index_card_0_2_1_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_0_2_1));
        textView = findViewById(R.id.data_images_index_card_0_2_2_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_0_2_2));
        textView = findViewById(R.id.data_images_index_card_0_3_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_0_3));

        textView = findViewById(R.id.data_images_index_card_1_description);
        textView.setText("包含：" +
                getResources().getString(R.string.text_data_images_index_card_1_1) + "、" +
                getResources().getString(R.string.text_data_images_index_card_1_2) + "、" +
                getResources().getString(R.string.text_data_images_index_card_1_3) + "、" +
                getResources().getString(R.string.text_data_images_index_card_1_4)
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
                getResources().getString(R.string.text_data_images_index_card_9_4) + "、" +
                getResources().getString(R.string.text_data_images_index_card_9_5)
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
        textView.setText(getResources().getString(R.string.description_data_images_index_card_15));
        textView = findViewById(R.id.data_images_index_card_16_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_16));
        textView = findViewById(R.id.data_images_index_card_17_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_17));
        textView = findViewById(R.id.data_images_index_card_18_description);
        textView.setText(getResources().getString(R.string.description_data_images_index_card_18));

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
        container.setOnClickListener(v -> DataImageViewerHelper.openSystemPhotoViewerToSeeDataImages(this, imageName));
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
        // PAD 左栏（scrollView1）不参与联动，ScrollView 不自存滚动状态，需单独保存
        View scrollView1 = findViewById(R.id.scrollView1);
        if (scrollView1 != null) {
            outState.putInt(STATE_SCROLL_Y_PAD_1, scrollView1.getScrollY());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
        // PAD 左栏（scrollView1）单独恢复
        if (SmallestWidthUtil.getSmallestWidthDp() >= 600) {
            View scrollView1 = findViewById(R.id.scrollView1);
            if (scrollView1 != null) {
                int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y_PAD_1, 0);
                if (scrollY > 0) {
                    scrollView1.setScrollY(scrollY);
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