package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.graphics.ImageDecoder;
import android.graphics.Outline;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

public class TodayLuckyActivity extends BaseActivity {
    //圆角半径，单位：像素
    private static final int CORNER_RADIUS_DP = 50;
    private int cornerRadiusPx;//转换后的像素值

    private ImageView gifImageView;
    private AnimatedImageDrawable animatedDrawable;
    private Button button;
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

        this.gifImageView = findViewById(R.id.Gif_Image_View);
        this.button = findViewById(R.id.Button_ControlGif);
        isPlaying = true;

        //加载Gif动图
        loadAnimatedImage();

        //应用Gif圆角
        applyRoundedCorners();

        //加载控制Gif的按钮
        setupButtonControlGif();

    }

    //加载Gif动图
    private void loadAnimatedImage() {
        try {
            // 使用ImageDecoder加载GIF
            ImageDecoder.Source source = ImageDecoder.createSource(getResources(), R.drawable.today_lucky);

            // 创建AnimatedImageDrawable
            Drawable decodedDrawable = ImageDecoder.decodeDrawable(source);

            if (decodedDrawable instanceof AnimatedImageDrawable) {
                animatedDrawable = (AnimatedImageDrawable) decodedDrawable;
                gifImageView.setImageDrawable(animatedDrawable);

                // 自动开始播放
                animatedDrawable.start();
            }
        } catch (
        IOException e) {
            // 错误处理：显示静态图片
            gifImageView.setImageResource(R.drawable.today_lucky);
        }
    }

    // 应用圆角效果
    private void applyRoundedCorners() {
        gifImageView.setClipToOutline(true);
        gifImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
    }

    //加载控制Gif的按钮
    private void setupButtonControlGif() {
        //初始状态：播放中
        updateButtonText();

        button.setOnClickListener(v -> {
            if (animatedDrawable == null) return;

            // 切换播放状态
            isPlaying = !isPlaying;

            if (isPlaying) {
                // 播放GIF
                animatedDrawable.start();
            } else {
                // 暂停GIF
                animatedDrawable.stop();
            }

            // 更新按钮文本
            updateButtonText();
        });
    }

    //更新按钮文本
    private void updateButtonText() {
        button.setText(isPlaying ?
                getString(R.string.label_tools_today_lucky_pause_gif) :
                getString(R.string.label_tools_today_lucky_play_gif));
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
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

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 当Activity重新可见时，恢复播放状态
        if (isPlaying && animatedDrawable != null) {
            animatedDrawable.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 当Activity不可见时，暂停GIF以节省资源
        if (animatedDrawable != null) {
            animatedDrawable.stop();
        }
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
        // 添加按压动画
        findViewById(R.id.Button_ControlGif).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }
}