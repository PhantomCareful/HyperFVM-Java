package com.careful.HyperFVM.Activities.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

public class CardData_1_Activity extends AppCompatActivity {
    private DBHelper dbHelper;

    private int pressFeedbackAnimationDelay;

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

        setupBlurEffect();

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

        // 查询卡片数据并显示
        queryAndShowCardData(tableName, cardName);
    }

    // 查询并展示卡片数据
    @SuppressLint({"DiscouragedApi", "Range"})
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

            // 第1张图片
            ImageView imageView = findViewById(R.id.Image_View_0);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);

            //第2张图片
            imageView = findViewById(R.id.Image_View_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1"));
            if (!imageIdStr.equals("无")) {
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                imageView.setImageResource(imageResId);
            } else {
                imageView.setVisibility(View.GONE);
            }

            //第3张图片
            imageView = findViewById(R.id.Image_View_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2"));
            if (!imageIdStr.equals("无")) {
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                imageView.setImageResource(imageResId);
            } else {
                imageView.setVisibility(View.GONE);
            }

            //全新的Markdown样式
            String contentBaseInfo = getStringFromCursor(cursor, "base_info") + "\n" +
                    "### 所属分类：" + getStringFromCursor(cursor, "category") + "\n" +
                    "### 耗能：" + getStringFromCursor(cursor, "price_0") + "\n" +
                    "## 👉人话解释" + "\n" + getStringFromCursor(cursor, "transfer_change") + "\n" +
                    "### 作为副卡：" + getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info), contentBaseInfo);

            // 相关卡片 - 金卡
            LinearLayout container = findViewById(R.id.Card_Corresponding_Container);
            String correspondingGoldenCardName = getStringFromCursor(cursor, "corresponding_golden_card_name");
            if (!correspondingGoldenCardName.equals("无")) {
                LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.card_card_data_corresponding_card, container, false);
                // 绑定控件并设置内容
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                ImageView correspondingCardImageId = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);
                correspondingCardName.setText(correspondingGoldenCardName);
                correspondingCardContent.setText("本卡片二转后可参与合成此金卡");
                imageIdStr = cursor.getString(cursor.getColumnIndex("corresponding_golden_card_image_id"));
                if (!imageIdStr.equals("无")) {
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            getPackageName()
                    );
                    correspondingCardImageId.setImageResource(imageResId);
                }

                correspondingCardContainer.setOnClickListener(v -> selectCardDataByName(correspondingGoldenCardName));
                container.addView(correspondingCardContainer);
            }

            // 相关卡片 - 融合卡
            String correspondingFusionCardName = getStringFromCursor(cursor, "corresponding_fusion_card_name");
            if (!correspondingFusionCardName.equals("无")) {
                // 1. 读取融合卡片图片ID列（同样增加null校验）
                String correspondingFusionCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_fusion_card_image_id"));

                // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
                String[] nameArray = correspondingFusionCardName.split("\\r?\\n");
                String[] imageIdArray = correspondingFusionCardImageId.split("\\r?\\n");

                // 3. 遍历拆分后的名称数组，为每条数据生成布局
                for (int i = 0; i < nameArray.length; i++) {
                    String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                    // 跳过空名称（比如拆分后出现空字符串）
                    if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                        continue;
                    }

                    // 4. Inflate单个融合卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                    LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                            .inflate(R.layout.card_card_data_corresponding_card, container, false);

                    // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                    TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                    TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                    @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                    // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                    int lastNum = 1;
                    if (i < imageIdArray.length) {
                        imageIdStr = imageIdArray[i];
                        lastNum = Character.getNumericValue(imageIdStr.charAt(imageIdStr.length() - 1));
                    }

                    // 根据image_id获取资源ID
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            getPackageName()
                    );
                    correspondingCardImage.setImageResource(imageResId);

                    // 7. 设置卡片名称和描述
                    correspondingCardName.setText(singleCardName);
                    switch (lastNum) {
                        case 1:
                            correspondingCardContent.setText("本卡片二转后可参与初级融合为此卡片");
                            break;
                        case 2:
                            correspondingCardContent.setText("本卡片二转后可参与深度融合为此卡片");
                            break;
                        case 3:
                            correspondingCardContent.setText("本卡片二转后可参与灵魂融合为此卡片");
                            break;
                    }

                    // 8. 设置点击事件（点击跳转到对应卡片详情）
                    correspondingCardContainer.setOnClickListener(v -> selectCardDataByName(singleCardName));

                    // 9. 将当前卡片布局添加到父容器
                    container.addView(correspondingCardContainer);
                }
            }

            // 没有任何相关卡片的话，隐藏标题和CardView
            if (correspondingGoldenCardName.equals("无") && correspondingFusionCardName.equals("无")) {
                findViewById(R.id.title_card_data_corresponding_info).setVisibility(View.GONE);
                findViewById(R.id.Card_Corresponding).setVisibility(View.GONE);
            }

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
                    new Intent(this, CardData_1_Activity.class);
            case "card_data_2" ->
                    new Intent(this, CardData_2_Activity.class);
            case "card_data_3" ->
                    new Intent(this, CardData_3_Activity.class);
            case "card_data_4" ->
                    new Intent(this, CardData_4_Activity.class);
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
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
        // 添加按压动画
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }
}
