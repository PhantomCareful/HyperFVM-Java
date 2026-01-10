package com.careful.HyperFVM.Activities.NecessaryThings;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class UsingInstructionActivity extends AppCompatActivity {

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