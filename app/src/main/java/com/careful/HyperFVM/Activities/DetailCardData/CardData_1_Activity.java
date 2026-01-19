package com.careful.HyperFVM.Activities.DetailCardData;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class CardData_1_Activity extends AppCompatActivity {
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 初始化布局和基础设置
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_card_data_1);

        // 获取传入的参数
        String cardName = getIntent().getStringExtra("name");
        String tableName = getIntent().getStringExtra("table");

        // 校验参数
        if (cardName == null || tableName == null) {
            finish(); // 参数错误直接关闭页面
            return;
        }

        setTopAppBarTitle(cardName + " ");

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

        // 查询卡片数据并显示
        queryAndShowCardData(tableName, cardName);
    }

    // 查询并展示卡片数据
    private void queryAndShowCardData(String tableName, String cardName) {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            // 从指定表中查询卡片数据
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                ((TextView) findViewById(R.id.base_info)).setText("未找到卡片数据");
                return;
            }

            // 逐个绑定控件（确保控件ID与表列名完全一致）
            // 基础信息区域
            ImageView imageView = findViewById(R.id.Image_View);
            @SuppressLint("Range") String imageIdStr = cursor.getString(cursor.getColumnIndex("image_id"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            @SuppressLint("DiscouragedApi") int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            //全新的Markdown样式
            String contentBaseInfo = "## 👉" + getStringFromCursor(cursor, "name") + "\n" +
                    "- 所属分类：" + getStringFromCursor(cursor, "category") + "\n" +
                    "- 耗能：" + getStringFromCursor(cursor, "price_0") + "\n" +
                    getStringFromCursor(cursor, "base_info") + "\n" +
                    "## 👉人话解释" + "\n" + getStringFromCursor(cursor, "transfer_change") + "\n\n\n" +
                    "### 作为副卡：" + getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info), contentBaseInfo);

            // 数据信息区域（星级）
            setTextToView(R.id.star, "\uD83C\uDF1F强化提升：" + getStringFromCursor(cursor, "star"));
            setTextToView(R.id.star_detail, getStringFromCursor(cursor, "star_detail"));
            setTextToView(R.id.star_0, getStringFromCursor(cursor, "star_0"));
            setTextToView(R.id.star_1, getStringFromCursor(cursor, "star_1"));
            setTextToView(R.id.star_2, getStringFromCursor(cursor, "star_2"));
            setTextToView(R.id.star_3, getStringFromCursor(cursor, "star_3"));
            setTextToView(R.id.star_4, getStringFromCursor(cursor, "star_4"));
            setTextToView(R.id.star_5, getStringFromCursor(cursor, "star_5"));
            setTextToView(R.id.star_6, getStringFromCursor(cursor, "star_6"));
            setTextToView(R.id.star_7, getStringFromCursor(cursor, "star_7"));
            setTextToView(R.id.star_8, getStringFromCursor(cursor, "star_8"));
            setTextToView(R.id.star_9, getStringFromCursor(cursor, "star_9"));
            setTextToView(R.id.star_10, getStringFromCursor(cursor, "star_10"));
            setTextToView(R.id.star_11, getStringFromCursor(cursor, "star_11"));
            setTextToView(R.id.star_12, getStringFromCursor(cursor, "star_12"));
            setTextToView(R.id.star_13, getStringFromCursor(cursor, "star_13"));
            setTextToView(R.id.star_14, getStringFromCursor(cursor, "star_14"));
            setTextToView(R.id.star_15, getStringFromCursor(cursor, "star_15"));
            setTextToView(R.id.star_16, getStringFromCursor(cursor, "star_16"));
            setTextToView(R.id.star_M, getStringFromCursor(cursor, "star_M"));
            setTextToView(R.id.star_U, getStringFromCursor(cursor, "star_U"));

            // 技能信息
            if (getStringFromCursor(cursor, "skill").equals("该防御卡不支持技能")) {
                findViewById(R.id.Card_Skill).setVisibility(View.GONE);
            }
            setTextToView(R.id.skill, "\uD83C\uDF1F技能提升：" + getStringFromCursor(cursor, "skill"));
            setTextToView(R.id.skill_detail, getStringFromCursor(cursor, "skill_detail"));
            setTextToView(R.id.skill_0, getStringFromCursor(cursor, "skill_0"));
            setTextToView(R.id.skill_1, getStringFromCursor(cursor, "skill_1"));
            setTextToView(R.id.skill_2, getStringFromCursor(cursor, "skill_2"));
            setTextToView(R.id.skill_3, getStringFromCursor(cursor, "skill_3"));
            setTextToView(R.id.skill_4, getStringFromCursor(cursor, "skill_4"));
            setTextToView(R.id.skill_5, getStringFromCursor(cursor, "skill_5"));
            setTextToView(R.id.skill_6, getStringFromCursor(cursor, "skill_6"));
            setTextToView(R.id.skill_7, getStringFromCursor(cursor, "skill_7"));
            setTextToView(R.id.skill_8, getStringFromCursor(cursor, "skill_8"));

            // 其他信息
            if (getStringFromCursor(cursor, "additional_info").equals("无")) {
                findViewById(R.id.card_data_other_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Other).setVisibility(View.GONE);
            }
            //全新的Markdown样式
            getContent(this, findViewById(R.id.additional_info), getStringFromCursor(cursor, "additional_info"));

        } catch (Exception e) {
            ((TextView) findViewById(R.id.base_info)).setText("数据加载失败");
        }
        // 关闭游标，避免内存泄漏
    }

    // 辅助方法：设置文本到控件，避免重复代码
    private void setTextToView(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    // 辅助方法：从游标获取字符串（处理空值）
    private String getStringFromCursor(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) {
            return "未知"; // 列名不存在时提示
        }
        String value = cursor.getString(columnIndex);
        return (value == null || value.isEmpty()) ? "无" : value;
    }

    // 顶栏设置
    private void setTopAppBarTitle(String title) {
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }
}
