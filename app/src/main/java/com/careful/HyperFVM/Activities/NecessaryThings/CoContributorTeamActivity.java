package com.careful.HyperFVM.Activities.NecessaryThings;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class CoContributorTeamActivity extends BaseActivity {
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
        setContentView(R.layout.activity_co_contributor_team);

        // 添加模糊材质
        setupBlurEffect();

        TextView CoContributorTeamTop = findViewById(R.id.CoContributorTeam_Top_Content);
        TextView CoContributorTeamContent = findViewById(R.id.CoContributorTeam_Content_Content);

        getContentFromAssets(this, CoContributorTeamTop, "CoContributorTeamTop.txt");
        getContentFromAssets(this, CoContributorTeamContent, "CoContributorTeamContent.txt");

        // 初始化动画效果
        TransitionSet transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

        LinearLayout CoContributorTeamContainer = findViewById(R.id.CoContributorTeam_Container);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(CoContributorTeamContainer, transition);
            findViewById(R.id.CoContributorTeam_Top_Container).setVisibility(View.VISIBLE);
            findViewById(R.id.CoContributorTeam_Content_Container).setVisibility(View.VISIBLE);
            findViewById(R.id.placeholder).setVisibility(View.GONE);
        }, 300);
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
     * 在onResume阶段：设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        boolean isPressFeedbackAnimation;
        try (DBHelper dbHelper = new DBHelper(this)) {
            // 添加按压动画
            if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
                pressFeedbackAnimationDelay = 200;
                isPressFeedbackAnimation = true;
            } else {
                pressFeedbackAnimationDelay = 0;
                isPressFeedbackAnimation = false;
            }
        }
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }
}