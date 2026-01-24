package com.careful.HyperFVM.Activities.NecessaryThings;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

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
import androidx.appcompat.app.ActionBar;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class UsingInstructionActivity extends BaseActivity {

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
        setContentView(R.layout.activity_using_instruction);

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.label_using_instruction) + " ");

        TextView overview_top = findViewById(R.id.using_instruction_top);
        TextView overview1 = findViewById(R.id.using_instruction1);
        TextView overview2 = findViewById(R.id.using_instruction2);
        TextView overview3 = findViewById(R.id.using_instruction3);
        TextView overview4 = findViewById(R.id.using_instruction4);
        TextView overview5 = findViewById(R.id.using_instruction5);
        TextView overview6 = findViewById(R.id.using_instruction6);
        TextView overview7 = findViewById(R.id.using_instruction7);

        getContentFromAssets(this, overview_top, "QATop.txt");
        getContentFromAssets(this, overview1, "QA1.txt");
        getContentFromAssets(this, overview2, "QA2.txt");
        getContentFromAssets(this, overview3, "QA3.txt");
        getContentFromAssets(this, overview4, "QA4.txt");
        getContentFromAssets(this, overview5, "QA5.txt");
        getContentFromAssets(this, overview6, "QA6.txt");
        getContentFromAssets(this, overview7, "QA7.txt");

        // 初始化动画效果
        TransitionSet transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

        LinearLayout using_instruction_container = findViewById(R.id.using_instruction_container);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(using_instruction_container, transition);
            findViewById(R.id.using_instruction_top_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction1_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction2_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction3_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction4_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction5_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction6_container).setVisibility(View.VISIBLE);
            findViewById(R.id.using_instruction7_container).setVisibility(View.VISIBLE);
            findViewById(R.id.placeholder).setVisibility(View.GONE);
        }, 300);
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题、启用返回按钮
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> this.finish());
    }
}