package com.careful.HyperFVM.Activities.DetailCardData;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class CardData_3_Activity extends AppCompatActivity {
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
        setContentView(R.layout.activity_card_data_3);
        setTopAppBarTitle("防御卡数据查询");

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

        // 获取传入的参数
        String cardName = getIntent().getStringExtra("name");
        String tableName = getIntent().getStringExtra("table");

        // 校验参数
        if (cardName == null || tableName == null) {
            finish(); // 参数错误直接关闭页面
            return;
        }

        // 查询卡片数据并显示
        queryAndShowCardData(tableName, cardName);
    }

    // 查询并展示卡片数据
    @SuppressLint({"Range", "DiscouragedApi"})
    private void queryAndShowCardData(String tableName, String cardName) {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            // 从指定表中查询卡片数据
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                ((TextView) findViewById(R.id.name)).setText("未找到卡片数据");
                return;
            }

            // 逐个绑定控件（确保控件ID与表列名完全一致）
            // 基础信息区域
            ImageView imageView = findViewById(R.id.Image_View);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_id"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            setTextToView(R.id.name, "👀卡片名称：" + getStringFromCursor(cursor, "name"));
            setTextToView(R.id.category, "\uD83D\uDFE2所属分类：" + getStringFromCursor(cursor, "category"));
            setTextToView(R.id.price, "\uD83D\uDD25耗能：" + getStringFromCursor(cursor, "price_0"));
            setTextToView(R.id.base_info, getStringFromCursor(cursor, "base_info"));
            setTextToView(R.id.transfer_change, getStringFromCursor(cursor, "transfer_change"));
            setTextToView(R.id.sub_card, "\uD83D\uDD35作为副卡：" + getStringFromCursor(cursor, "sub_card"));

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

            // 分解&兑换信息
            setTextToView(R.id.decompose_and_get, "分解&兑换：神谕之石");

            imageView = findViewById(R.id.decompose_image_id_card_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_card_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_card_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_card_4);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_4"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_skill_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_skill_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_skill_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_skill_4);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_1_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_1_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_1_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_2_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_2_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_2_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_3_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_3_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_transfer_3_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            imageView = findViewById(R.id.decompose_image_id_compose);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_compose"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            setTextToView(R.id.decompose_card_1, getStringFromCursor(cursor, "decompose_card_1"));
            setTextToView(R.id.decompose_card_2, getStringFromCursor(cursor, "decompose_card_2"));
            setTextToView(R.id.decompose_card_3, getStringFromCursor(cursor, "decompose_card_3"));
            setTextToView(R.id.decompose_card_4, getStringFromCursor(cursor, "decompose_card_4"));
            setTextToView(R.id.decompose_skill_1, getStringFromCursor(cursor, "decompose_skill_1"));
            setTextToView(R.id.decompose_skill_2, getStringFromCursor(cursor, "decompose_skill_2"));
            setTextToView(R.id.decompose_skill_3, getStringFromCursor(cursor, "decompose_skill_3"));
            setTextToView(R.id.decompose_skill_4, getStringFromCursor(cursor, "decompose_skill_4"));
            setTextToView(R.id.decompose_transfer_1_a, getStringFromCursor(cursor, "decompose_transfer_1_a"));
            setTextToView(R.id.decompose_transfer_1_b, getStringFromCursor(cursor, "decompose_transfer_1_b"));
            setTextToView(R.id.decompose_transfer_1_c, getStringFromCursor(cursor, "decompose_transfer_1_c"));
            setTextToView(R.id.decompose_transfer_2_a, getStringFromCursor(cursor, "decompose_transfer_2_a"));
            setTextToView(R.id.decompose_transfer_2_b, getStringFromCursor(cursor, "decompose_transfer_2_b"));
            setTextToView(R.id.decompose_transfer_2_c, getStringFromCursor(cursor, "decompose_transfer_2_c"));
            setTextToView(R.id.decompose_transfer_3_a, getStringFromCursor(cursor, "decompose_transfer_3_a"));
            setTextToView(R.id.decompose_transfer_3_b, getStringFromCursor(cursor, "decompose_transfer_3_b"));
            setTextToView(R.id.decompose_transfer_3_c, getStringFromCursor(cursor, "decompose_transfer_3_c"));
            setTextToView(R.id.decompose_compose, getStringFromCursor(cursor, "decompose_compose"));
            setTextToView(R.id.get_card_1, getStringFromCursor(cursor, "get_card_1"));
            setTextToView(R.id.get_card_2, getStringFromCursor(cursor, "get_card_2"));
            setTextToView(R.id.get_card_3, getStringFromCursor(cursor, "get_card_3"));
            setTextToView(R.id.get_card_4, getStringFromCursor(cursor, "get_card_4"));
            setTextToView(R.id.get_skill_1, getStringFromCursor(cursor, "get_skill_1"));
            setTextToView(R.id.get_skill_2, getStringFromCursor(cursor, "get_skill_2"));
            setTextToView(R.id.get_skill_3, getStringFromCursor(cursor, "get_skill_3"));
            setTextToView(R.id.get_skill_4, getStringFromCursor(cursor, "get_skill_4"));
            setTextToView(R.id.get_transfer_1_a, getStringFromCursor(cursor, "get_transfer_1_a"));
            setTextToView(R.id.get_transfer_1_b, getStringFromCursor(cursor, "get_transfer_1_b"));
            setTextToView(R.id.get_transfer_1_c, getStringFromCursor(cursor, "get_transfer_1_c"));
            setTextToView(R.id.get_transfer_2_a, getStringFromCursor(cursor, "get_transfer_2_a"));
            setTextToView(R.id.get_transfer_2_b, getStringFromCursor(cursor, "get_transfer_2_b"));
            setTextToView(R.id.get_transfer_2_c, getStringFromCursor(cursor, "get_transfer_2_c"));
            setTextToView(R.id.get_transfer_3_a, getStringFromCursor(cursor, "get_transfer_3_a"));
            setTextToView(R.id.get_transfer_3_b, getStringFromCursor(cursor, "get_transfer_3_b"));
            setTextToView(R.id.get_transfer_3_c, getStringFromCursor(cursor, "get_transfer_3_c"));
            setTextToView(R.id.get_compose, getStringFromCursor(cursor, "get_compose"));

            // 其他信息
            if (getStringFromCursor(cursor, "additional_info").equals("无")) {
                findViewById(R.id.card_data_other_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Other).setVisibility(View.GONE);
            }
            setTextToView(R.id.additional_info, getStringFromCursor(cursor, "additional_info"));

        } catch (Exception e) {
            ((TextView) findViewById(R.id.name)).setText("数据加载失败");
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
}
