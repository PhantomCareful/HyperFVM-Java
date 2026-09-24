package com.careful.HyperFVM.Activities.Tools;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

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
    // 每一帧对应的分数（下标 = 帧号 0 ~ 27，与素材帧数保持一致）
    private static final int[] FRAME_SCORES = {
            5, 7, 14, 8, 6, // 帧 0 ~ 4
            9, 7, 12, 10, 6, // 帧 5 ~ 9
            9, 6, 8, 5, 11, // 帧 10 ~ 14
            16, 13, 5, 8, 7, // 帧 15 ~ 19
            15, 10, 5, 7, 6, // 帧 20 ~ 24
            11, 8, 12, // 帧 25 ~ 27
    };

    // 得分达标时的描边宽度与描边距动图边缘的间距（描边位于动图外侧），单位：dp
    private static final int STROKE_WIDTH_DP = 5;
    private static final int STROKE_GAP_DP = 5;

    // 界面重建（深浅色切换等）时保存/恢复的状态键
    private static final String STATE_IS_PLAYING = "state_is_playing";
    private static final String STATE_MATERIAL_TIMES = "state_material_times";

    //圆角半径，单位：像素
    private static final int CORNER_RADIUS_DP = 25;
    private int cornerRadiusPx;//转换后的像素值

    private BlurUtil blurUtil;
    private FrameSequence frameSequence;
    private FrameSequenceView[] frameAnimViews;
    private Button Button_ControlGif;
    private TextView TextView_TotalScore;
    private TextView TextView_Emoji1;
    private TextView TextView_Emoji2;
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

        TextView_TotalScore = findViewById(R.id.today_lucky_total_score);
        TextView_Emoji1 = findViewById(R.id.today_lucky_emoji_1);
        TextView_Emoji2 = findViewById(R.id.today_lucky_emoji_2);

        // 初始化各种装饰效果
        initDecoration();

        // 将dp值转换为像素
        cornerRadiusPx = DensityUtil.dpToPx(this, CORNER_RADIUS_DP);

        isPlaying = true;

        //加载动图帧序列（4个视图共享同一序列，以不同速率播放）
        loadFrameSequence();

        //恢复重建前的播放状态与定格信息（深浅色切换等场景）
        restoreState(savedInstanceState);

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
                // 恢复播放时清空上一次的得分描边
                view.setStroke(0f, 0f, 0);
                TextView_TotalScore.setText("⏳");
                TextView_Emoji1.setText("⏳");
                TextView_Emoji2.setText("⏳");
            } else {
                view.stop();
            }
        }
    }

    //暂停时统计 4 个动图定格帧的分数，并展示总分与判断结果
    private void updateScoreDisplay() {
        if (frameAnimViews == null) return;

        // 收集 4 个定格帧的分数
        int[] scores = new int[frameAnimViews.length];
        int totalScore = 0;
        for (int i = 0; i < frameAnimViews.length; i++) {
            scores[i] = scoreOfFrame(frameAnimViews[i].getCurrentFrame());
            totalScore += scores[i];
        }

        // 先按单分数规则更新描边，再展示总分与判断结果
        updateScoreStrokes(scores);

        String resultText = judgeResultText(scores, totalScore);
        TextView_TotalScore.setText(String.valueOf(totalScore));
        TextView_Emoji1.setText(resultText);
        TextView_Emoji2.setText(resultText);
    }

    //按单分数规则更新 4 个动图的描边（在结果判断之前）：得分 >= 13 的组件描边；4 个得分完全相同时全部描边
    private void updateScoreStrokes(int[] scores) {
        if (frameAnimViews == null) return;

        // 4 个组件得分是否完全相同
        boolean allSame = allScoresSame(scores);

        // 解析主题色 colorPrimary 作为描边颜色
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);

        float strokeWidthPx = DensityUtil.dpToPx(this, STROKE_WIDTH_DP);
        float strokeGapPx = DensityUtil.dpToPx(this, STROKE_GAP_DP);
        for (int i = 0; i < frameAnimViews.length; i++) {
            boolean highlighted = allSame || scores[i] >= 13;
            frameAnimViews[i].setStroke(highlighted ? strokeWidthPx : 0f, strokeGapPx, typedValue.data);
        }
    }

    //安全读取某帧的分数（帧号越界时按 0 分处理）
    private int scoreOfFrame(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= FRAME_SCORES.length) return 0;
        return FRAME_SCORES[frameIndex];
    }

    //4 个动图的定格分数是否完全相同
    private boolean allScoresSame(int[] scores) {
        for (int score : scores) {
            if (score != scores[0]) return false;
        }
        return true;
    }

    //根据总分与 4 个单分数判断结果文字
    private String judgeResultText(int[] scores, int totalScore) {
        // 第一步：总分特判
        if (totalScore == 20) return "🤡";
        if (totalScore == 64) return "🤑";

        // 第二步：四个动图得分完全相同时统一显示
        if (allScoresSame(scores)) return "🃏";

        // 第三步：按单个分数判断
        for (int score : scores) {
            if (score == 16) return "🤩";
        }
        for (int score : scores) {
            if (score == 14 || score == 15) return "😄";
        }
        for (int score : scores) {
            if (score == 13) return "👍";
        }
        for (int score : scores) {
            if (score == 12) return "🫣";
        }
        for (int score : scores) {
            if (score >= 9 && score < 12) return "😭";
        }

        // 第四步：均未命中时兜底显示
        return "😭";
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

            // 停止播放时：按 4 个动图定格帧计算分数并展示结果
            if (!isPlaying) {
                updateScoreDisplay();
            }
        });
    }

    //更新按钮文本
    private void updateButtonText() {
        Button_ControlGif.setText(isPlaying ?
                getString(R.string.text_today_lucky_pause_gif) :
                getString(R.string.text_today_lucky_play_gif));
    }

    //恢复重建前的播放状态与 4 个动图的播放位置（素材时间），并还原暂停时的定格分数显示
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null || frameAnimViews == null) return;

        long[] materialTimes = savedInstanceState.getLongArray(STATE_MATERIAL_TIMES);
        if (materialTimes != null && materialTimes.length == frameAnimViews.length) {
            for (int i = 0; i < frameAnimViews.length; i++) {
                frameAnimViews[i].setMaterialTimeMs(materialTimes[i]);
            }
        }

        isPlaying = savedInstanceState.getBoolean(STATE_IS_PLAYING, true);
        if (!isPlaying) {
            // 暂停态：停止播放并还原定格分数、描边与表情
            updatePlayState();
            updateScoreDisplay();
        }
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

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 添加按压动画
        findViewById(R.id.Button_ControlGif).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // 保存播放状态与各动图的素材播放位置，供重建（深浅色切换等）后恢复
        outState.putBoolean(STATE_IS_PLAYING, isPlaying);
        if (frameAnimViews != null) {
            long[] materialTimes = new long[frameAnimViews.length];
            for (int i = 0; i < frameAnimViews.length; i++) {
                materialTimes[i] = frameAnimViews[i].getMaterialTimeMs();
            }
            outState.putLongArray(STATE_MATERIAL_TIMES, materialTimes);
        }
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