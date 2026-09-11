package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.FrameSequence;
import com.careful.HyperFVM.utils.ForDesign.Animation.FrameSequenceView;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

public class TodayLuckyActivity extends BaseActivity {
    // 4 个动图视图的播放速率（1f 为素材原始速度）
    private static final float[] PLAY_SPEEDS = {0.5f, 1f, 2f, 4f};
    // 帧序列所在的 assets 目录
    private static final String FRAME_ASSETS_DIR = "today_lucky_frames";

    //圆角半径，单位：像素
    private static final int CORNER_RADIUS_DP = 25;
    private int cornerRadiusPx;//转换后的像素值

    private BlurUtil blurUtil;
    private FrameSequence frameSequence;
    private FrameSequenceView[] frameAnimViews;
    private Button Button_ControlGif;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        //小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_today_lucky);

        // 初始化各种装饰效果
        initDecoration();

        // 将dp值转换为像素
        cornerRadiusPx = DensityUtil.dpToPx(this, CORNER_RADIUS_DP);

        isPlaying = true;

        //加载动图帧序列（4个视图共享同一序列，以不同速率播放）
        loadFrameSequence();

        //应用动图圆角
        applyRoundedCorners();

        //加载控制Gif的按钮
        setupButtonControlGif();

    }

    //加载动图帧序列
    private void loadFrameSequence() {
        frameAnimViews = new FrameSequenceView[]{
                findViewById(R.id.Frame_Anim_View_1),
                findViewById(R.id.Frame_Anim_View_2),
                findViewById(R.id.Frame_Anim_View_3),
                findViewById(R.id.Frame_Anim_View_4)
        };

        // 4 个视图共享同一帧序列，以不同速率播放
        frameSequence = FrameSequence.load(this, FRAME_ASSETS_DIR);
        if (frameSequence != null) {
            for (int i = 0; i < frameAnimViews.length; i++) {
                frameAnimViews[i].setFrameSequence(frameSequence);
                frameAnimViews[i].setSpeed(PLAY_SPEEDS[i]);
            }
        }

        // 自动开始播放
        updatePlayState();
    }

    //统一同步4个动图的播放/暂停状态
    private void updatePlayState() {
        if (frameAnimViews == null) return;
        for (FrameSequenceView view : frameAnimViews) {
            if (isPlaying) {
                view.start();
            } else {
                view.stop();
            }
        }
    }

    // 应用圆角效果（由视图对图片实际绘制区域裁剪，视图留白时同样生效）
    private void applyRoundedCorners() {
        if (frameAnimViews == null) return;
        for (FrameSequenceView view : frameAnimViews) {
            view.setCornerRadius(cornerRadiusPx);
        }
    }

    //加载控制Gif的按钮
    private void setupButtonControlGif() {
        //初始状态：播放中
        updateButtonText();

        Button_ControlGif.setOnClickListener(v -> {
            if (frameSequence == null) return;

            // 切换播放状态
            isPlaying = !isPlaying;
            updatePlayState();

            // 更新按钮文本
            updateButtonText();
        });
    }

    //更新按钮文本
    private void updateButtonText() {
        Button_ControlGif.setText(isPlaying ?
                getString(R.string.text_today_lucky_pause_gif) :
                getString(R.string.text_today_lucky_play_gif));
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        Button_ControlGif = findViewById(R.id.Button_ControlGif);
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
        // 动态调整侧边距（手机/PAD）
        LinearLayout today_lucky_container = findViewById(R.id.today_lucky_container);
        InsetsUtil.setMarginHorizontal(this, today_lucky_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) today_lucky_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            today_lucky_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) Button_ControlGif.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            Button_ControlGif.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 添加按压动画
        findViewById(R.id.Button_ControlGif).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 当Activity重新可见时，恢复播放状态
        if (isPlaying) {
            updatePlayState();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 当Activity不可见时，暂停动图以节省资源
        if (frameAnimViews != null) {
            for (FrameSequenceView view : frameAnimViews) {
                view.stop();
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