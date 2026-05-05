package com.careful.HyperFVM.Activities.DetailCardData;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class CardData2Activity extends BaseActivity {
    private DBHelper dbHelper;

    private TransitionSet transition;
    private LinearLayout container;

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
        setContentView(R.layout.activity_card_data_2);

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(300); // 动画时长300ms
        container = findViewById(R.id.card_data_container);

        // 获取传入的参数
        String cardName = getIntent().getStringExtra("name");
        String tableName = getIntent().getStringExtra("table");

        // 校验参数
        if (cardName == null || tableName == null) {
            finish(); // 参数错误直接关闭页面
            return;
        }

        setupBlurEffect();

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

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
                ((TextView) findViewById(R.id.base_info)).setText("未找到卡片数据");
                return;
            }

            // 逐个绑定控件（确保控件ID与表列名完全一致）
            // 大图片区域
            ImageView ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            String cardName1 = getStringFromCursor(cursor, "name");
            setTextToView(R.id.card_name_1, cardName1);
            exportImage(ImageViewCardBig, cardName, cardName1, "初级融合, 大");

            ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            String cardName2 = getStringFromCursor(cursor, "name_2");
            setTextToView(R.id.card_name_2, cardName2);
            exportImage(ImageViewCardBig, cardName, cardName2, "深度融合, 大");

            ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            String cardName3 = getStringFromCursor(cursor, "name_3");
            setTextToView(R.id.card_name_3, getStringFromCursor(cursor, "name_3"));
            exportImage(ImageViewCardBig, cardName, cardName3, "灵魂融合, 大");

            // 基础信息区域
            // 全新的Markdown样式
            String contentBaseInfo = "### 所属分类：" + getStringFromCursor(cursor, "category") + "\n" +
                    "### 耗能：" + getStringFromCursor(cursor, "price") + "\n" +
                    "## 👉主卡信息" + "\n" + getStringFromCursor(cursor, "base_info") + "\n" +
                    "## 👉融合信息" + "\n" + getStringFromCursor(cursor, "fusion_info") + "\n" +
                    "### 相关卡片" + "\n" + "- 点击材料卡的图片可跳转该卡片数据";
            getContent(this, findViewById(R.id.base_info_1), contentBaseInfo);

            ImageView ImageViewCard = findViewById(R.id.Image_View_Card_1_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName1, "初级融合, 主卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_1_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName1, "初级融合, 融合副卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName1, "初级融合");

            ImageViewCard = findViewById(R.id.Image_View_Card_2_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName2, "深度融合, 主卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_2_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName2, "深度融合, 融合副卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName2, "深度融合");

            ImageViewCard = findViewById(R.id.Image_View_Card_3_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName3, "灵魂融合, 主卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_3_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_3_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName3, "灵魂融合, 融合副卡");

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportImage(ImageViewCard, cardName, cardName3, "灵魂融合");

            // 给相关卡片设置跳转查询的点击事件
            // 缓存点击事件需要用到的字段值
            String name1_1 = getStringFromCursor(cursor, "name_1_1");
            String name1_2 = getStringFromCursor(cursor, "name_1_2");
            String name2_2 = getStringFromCursor(cursor, "name_2_2");
            String name3_2 = getStringFromCursor(cursor, "name_3_2");
            findViewById(R.id.Image_View_Card_1_1).setOnClickListener(v -> selectCardDataByName(name1_1));
            findViewById(R.id.Image_View_Card_1_2).setOnClickListener(v -> selectCardDataByName(name1_2));
            findViewById(R.id.Image_View_Card_2_2).setOnClickListener(v -> selectCardDataByName(name2_2));
            findViewById(R.id.Image_View_Card_3_2).setOnClickListener(v -> selectCardDataByName(name3_2));

            contentBaseInfo = "## 👉人话解释" + "\n" + getStringFromCursor(cursor, "transfer_change") + "\n" +
                    "### 作为副卡：" + getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info_2), contentBaseInfo);

            // 星级信息
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

            // 品阶信息
            setTextToView(R.id.star_fusion, "\uD83C\uDF1F品阶提升：" + "\n" + getStringFromCursor(cursor, "star_fusion"));
            setTextToView(R.id.star_fusion_detail, getStringFromCursor(cursor, "star_fusion_detail"));
            setTextToView(R.id.star_fusion_0, getStringFromCursor(cursor, "star_fusion_0"));
            setTextToView(R.id.star_fusion_1, getStringFromCursor(cursor, "star_fusion_1"));
            setTextToView(R.id.star_fusion_2, getStringFromCursor(cursor, "star_fusion_2"));
            setTextToView(R.id.star_fusion_3, getStringFromCursor(cursor, "star_fusion_3"));
            setTextToView(R.id.star_fusion_4, getStringFromCursor(cursor, "star_fusion_4"));
            setTextToView(R.id.star_fusion_5, getStringFromCursor(cursor, "star_fusion_5"));
            setTextToView(R.id.star_fusion_6, getStringFromCursor(cursor, "star_fusion_6"));
            setTextToView(R.id.star_fusion_7, getStringFromCursor(cursor, "star_fusion_7"));
            setTextToView(R.id.star_fusion_8, getStringFromCursor(cursor, "star_fusion_8"));
            setTextToView(R.id.star_fusion_9, getStringFromCursor(cursor, "star_fusion_9"));
            setTextToView(R.id.star_fusion_10, getStringFromCursor(cursor, "star_fusion_10"));
            setTextToView(R.id.star_fusion_11, getStringFromCursor(cursor, "star_fusion_11"));
            setTextToView(R.id.star_fusion_12, getStringFromCursor(cursor, "star_fusion_12"));
            setTextToView(R.id.star_fusion_13, getStringFromCursor(cursor, "star_fusion_13"));
            setTextToView(R.id.star_fusion_14, getStringFromCursor(cursor, "star_fusion_14"));
            setTextToView(R.id.star_fusion_15, getStringFromCursor(cursor, "star_fusion_15"));
            setTextToView(R.id.star_fusion_16, getStringFromCursor(cursor, "star_fusion_16"));
            setTextToView(R.id.star_fusion_M, getStringFromCursor(cursor, "star_fusion_M"));
            setTextToView(R.id.star_fusion_U, getStringFromCursor(cursor, "star_fusion_U"));

            // 技能信息
            setTextToView(R.id.skill, "\uD83C\uDF1F技能提升：" + getStringFromCursor(cursor, "skill"));
            if (getStringFromCursor(cursor, "skill").equals("该防御卡不支持技能")) {
                findViewById(R.id.Card_Skill).setVisibility(View.GONE);
            } else {
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
            }

            // 其他信息
            if (getStringFromCursor(cursor, "additional_info").equals("无")) {
                findViewById(R.id.card_data_other_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Other).setVisibility(View.GONE);
            } else {
                // 全新的Markdown样式
                getContent(this, findViewById(R.id.additional_info), getStringFromCursor(cursor, "additional_info"));
            }

        } catch (Exception e) {
            ((TextView) findViewById(R.id.base_info)).setText("数据加载失败");
        }

        // 所有任务完成后，显示大图片
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(container, transition);
            findViewById(R.id.Image_View_Card_Big_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_2).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_3).setVisibility(View.VISIBLE);
        }, 500);
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

    // 辅助方法：给图片控件设置长按导出图片
    private void exportImage(ImageView imageView, String folderName, String cardName, String categoryName) {
        imageView.setOnLongClickListener(v -> {
            DialogBuilderManager.showImageExportDialog(this, imageView, folderName, cardName, categoryName);
            return false;
        });
    }

    /**
     * 直接查询相关卡片数据
     * @param cardName 卡片名称
     */
    private void selectCardDataByName(String cardName) {
        if (cardName.isEmpty()) {
            Toast.makeText(this, "请输入卡片名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String tableName = dbHelper.getCardTable(cardName);
        String baseName = dbHelper.getCardBaseName(cardName);
        if (tableName == null || baseName == null) {
            Toast.makeText(this, "未找到该卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转详情页
        Intent intent = switch (tableName) {
            case "card_data_1" ->
                    new Intent(this, CardData1Activity.class);
            case "card_data_2" ->
                    new Intent(this, CardData2Activity.class);
            case "card_data_3" ->
                    new Intent(this, CardData3Activity.class);
            case "card_data_4" ->
                    new Intent(this, CardData4Activity.class);
            default -> null;
        };
        if (intent != null) {
            intent.putExtra("name", baseName);
            intent.putExtra("table", tableName);
            startActivity(intent);
        }
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }
}
