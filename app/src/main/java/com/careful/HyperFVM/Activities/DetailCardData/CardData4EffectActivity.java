package com.careful.HyperFVM.Activities.DetailCardData;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class CardData4EffectActivity extends BaseActivity {
    private DBHelper dbHelper;
    
    private BgEffectController bgEffectController;
    
    private TransitionSet transition;
    private LinearLayout bigImageContainer;

    private LinearLayout cardDataContainer;

    private View Image_View_Card_Big_1_Container;
    private View Image_View_Card_Big_2_Container;
    private View Image_View_Card_Big_3_Container;

    private int savedScrollY = 0;                              // 用于保存/恢复的滚动位置

    private int imageViewCardBig1ContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int imageViewCardBig2ContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int imageViewCardBig3ContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）

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
        setContentView(R.layout.activity_card_data4_effect);

        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 获取传入的参数
        String cardName = getIntent().getStringExtra("name");
        String tableName = getIntent().getStringExtra("table");

        // 校验参数
        if (cardName == null || tableName == null) {
            finish(); // 参数错误直接关闭页面
            return;
        }

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

        // 初始化各种装饰效果
        initDecoration();

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
            String cardName0 = cursor.getString(cursor.getColumnIndex("name"));
            String cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
            String cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));

            // 大图片区域
            ImageView ImageViewCardBig;
            String imageIdStr;
            int imageResId;
            // 先判断这张卡是否只有不转，如果是的话，启用Image_View_Card_Big_1_1_Container
            if (cursor.getString(cursor.getColumnIndex("image_id_1")).equals("无")) {
                // 这张卡只有不转，启用Image_View_Card_Big_1_Container
                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_1, cardName0);
                exportImage(ImageViewCardBig, cardName, cardName0, "不转形态, 大");

                // 隐藏剩下的组件
                findViewById(R.id.Image_View_Card_Big_2_1_Container).setVisibility(View.GONE);
                findViewById(R.id.Image_View_Card_Big_2_2_Container).setVisibility(View.GONE);
                findViewById(R.id.Image_View_Card_Big_3_Container).setVisibility(View.GONE);

                // 调整容器顶部距离
                cardDataContainer.setPadding(
                        cardDataContainer.getPaddingLeft(),
                        DensityUtil.dpToPx(this, 320),
                        cardDataContainer.getPaddingRight(),
                        cardDataContainer.getPaddingBottom());
            } else {
                // 这张卡有一转，启用Image_View_Card_Big_2_Container
                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_2_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_2_1, cardName0);
                exportImage(ImageViewCardBig, cardName, cardName0, "不转形态, 大");

                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_2_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_2_2, cardName1);
                exportImage(ImageViewCardBig, cardName, cardName1, "一转形态, 大");

                // 隐藏Image_View_Card_Big_1_Container
                findViewById(R.id.Image_View_Card_Big_1_Container).setVisibility(View.GONE);

                // 再判断是否有二转
                if (!cursor.getString(cursor.getColumnIndex("image_id_2")).equals("无")) {
                    // 这张卡有二转，启用Image_View_Card_Big_3_Container
                    ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_3);
                    imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big";
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            getPackageName()
                    );
                    ImageViewCardBig.setImageResource(imageResId);
                    setTextToView(R.id.card_name_3, cardName2);
                    exportImage(ImageViewCardBig, cardName, cardName2, "二转形态, 大");

                    // 调整容器顶部距离
                    cardDataContainer.setPadding(
                            cardDataContainer.getPaddingLeft(),
                            DensityUtil.dpToPx(this, 460),
                            cardDataContainer.getPaddingRight(),
                            cardDataContainer.getPaddingBottom());
                } else {
                    // 这张卡没有二转，隐藏Image_View_Card_Big_3_Container
                    findViewById(R.id.Image_View_Card_Big_3_Container).setVisibility(View.GONE);

                    // 调整容器顶部距离
                    cardDataContainer.setPadding(
                            cardDataContainer.getPaddingLeft(),
                            DensityUtil.dpToPx(this, 320),
                            cardDataContainer.getPaddingRight(),
                            cardDataContainer.getPaddingBottom());
                }
            }

            //全新的Markdown样式
            String contentBaseInfo = getStringFromCursor(cursor, "base_info") + "\n" +
                    "### 所属分类：" + getStringFromCursor(cursor, "category") + "\n" +
                    "### 耗能：" + getStringFromCursor(cursor, "price") + "\n";
            String contentTransferChange = getStringFromCursor(cursor, "transfer_change");
            if (!contentTransferChange.equals("无")) {
                contentBaseInfo = contentBaseInfo +
                        "## 👉人话解释" + "\n" + contentTransferChange + "\n";
            }
            contentBaseInfo = contentBaseInfo +
                    "### 作为副卡：" + getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info), contentBaseInfo);

            // 相关卡片 - 金卡
            LinearLayout container = findViewById(R.id.Card_Corresponding_Container);
            String correspondingGoldenCardName = getStringFromCursor(cursor, "corresponding_golden_card_name");
            if (!correspondingGoldenCardName.equals("无")) {
                LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);
                // 绑定控件并设置内容
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                ImageView correspondingCardImageId = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);
                correspondingCardName.setText(correspondingGoldenCardName);
                correspondingCardContent.setText("本卡片是合成此金卡的必要素材");
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
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);

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
                            correspondingCardContent.setText("本卡片是初级融合此卡片的必要素材");
                            break;
                        case 2:
                            correspondingCardContent.setText("本卡片是深度融合此卡片的必要素材");
                            break;
                        case 3:
                            correspondingCardContent.setText("本卡片是灵魂融合此卡片的必要素材");
                            break;
                    }

                    // 8. 设置点击事件（点击跳转到对应卡片详情）
                    correspondingCardContainer.setOnClickListener(v -> selectCardDataByName(singleCardName));

                    // 9. 将当前卡片布局添加到父容器
                    container.addView(correspondingCardContainer);
                }
            }

            // 相关卡片 - 增幅卡
            String correspondingAuxiliaryCardName = getStringFromCursor(cursor, "corresponding_auxiliary_card_name");
            if (!correspondingAuxiliaryCardName.equals("无")) {
                // 1. 读取增幅卡片图片ID列（同样增加null校验）
                String correspondingAuxiliaryCardImageId = cursor.getString(cursor.getColumnIndex("corresponding_auxiliary_card_image_id"));

                // 2. 按换行符拆分名称和图片ID数组（兼容Windows(\r\n)和Linux(\n)换行符）
                String[] nameArray = correspondingAuxiliaryCardName.split("\\r?\\n");
                String[] imageIdArray = correspondingAuxiliaryCardImageId.split("\\r?\\n");

                // 3. 遍历拆分后的名称数组，为每条数据生成布局
                for (int i = 0; i < nameArray.length; i++) {
                    String singleCardName = nameArray[i].trim(); // 去除首尾空格（避免空行/空格干扰）
                    // 跳过空名称（比如拆分后出现空字符串）
                    if (singleCardName.isEmpty() || singleCardName.equals("无")) {
                        continue;
                    }

                    // 4. Inflate单个增幅卡片的布局（每次循环新建一个布局，避免复用导致的问题）
                    LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                            .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);

                    // 5. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                    TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                    TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                    @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                    // 6. 匹配对应索引的图片ID（处理图片ID数组长度不足的情况）
                    if (i < imageIdArray.length) {
                        imageIdStr = imageIdArray[i];
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
                    correspondingCardContent.setText("此类卡片增幅本卡片");

                    // 8. 设置点击事件（点击跳转到对应卡片详情）
                    correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardByName(this, singleCardName));

                    // 9. 将当前卡片布局添加到父容器
                    container.addView(correspondingCardContainer);
                }
            }

            // 相关卡片 - 自己就是增幅卡
            if (
                    Objects.equals(cardName, "金牛座精灵") || Objects.equals(cardName, "暖炉汪") || Objects.equals(cardName, "能量喵") ||
                            Objects.equals(cardName, "坩埚蛇") || Objects.equals(cardName, "猪猪加强器") || Objects.equals(cardName, "香料虎") ||
                            Objects.equals(cardName, "精灵龙") || Objects.equals(cardName, "五行蛇") || Objects.equals(cardName, "魔杖蛇") ||
                            Objects.equals(cardName, "炎焱兔")
            ) {
                // 1. Inflate单个增幅卡片的布局
                LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);

                // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 3. 设置标题，并隐藏描述和图片
                if (Objects.equals(cardName, "能量喵")) {
                    correspondingCardName.setText("查看此卡片的平射增幅名单");
                } else {
                    correspondingCardName.setText("查看此卡片的增幅名单");
                }
                correspondingCardContent.setText("点击跳转");
                imageResId = getResources().getIdentifier(
                        "ic_chevron_right",
                        "drawable",
                        getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
                int tintColor = typedValue.data;
                correspondingCardImage.setColorFilter(tintColor);

                // 4. 设置点击事件（点击跳转到对应卡片详情）
                if (Objects.equals(cardName, "能量喵")) {
                    correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(this, cardName + "平射"));
                } else {
                    correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(this, cardName));
                }

                // 5. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
            } else {
                // 没有任何相关卡片的话，隐藏标题和CardView
                if (correspondingGoldenCardName.equals("无") && correspondingFusionCardName.equals("无") && correspondingAuxiliaryCardName.equals("无")) {
                    findViewById(R.id.title_card_data_corresponding_info).setVisibility(View.GONE);
                    findViewById(R.id.Card_Corresponding).setVisibility(View.GONE);
                }
            }

            if (Objects.equals(cardName, "能量喵")) {
                // 1. Inflate单个增幅卡片的布局
                LinearLayout correspondingCardContainer = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.card_card_data_corresponding_card_effect, container, false);

                // 2. 绑定当前布局的子控件（必须从当前container查找，避免复用错误）
                TextView correspondingCardName = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_name);
                TextView correspondingCardContent = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_content);
                @SuppressLint("CutPasteId") ImageView correspondingCardImage = correspondingCardContainer.findViewById(R.id.card_data_index_corresponding_card_image);

                // 3. 设置标题，并隐藏描述和图片
                correspondingCardName.setText("查看此卡片的投手增幅名单");
                correspondingCardContent.setText("点击跳转");
                imageResId = getResources().getIdentifier(
                        "ic_chevron_right",
                        "drawable",
                        getPackageName()
                );
                correspondingCardImage.setImageResource(imageResId);
                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
                int tintColor = typedValue.data;
                correspondingCardImage.setColorFilter(tintColor);

                // 4. 设置点击事件（点击跳转到对应卡片详情）
                correspondingCardContainer.setOnClickListener(v -> CardDataHelper.selectAuxiliaryCardBySelfName(this, cardName + "投手"));

                // 5. 将当前卡片布局添加到父容器
                container.addView(correspondingCardContainer);
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

            // 分解&兑换信息
            setTextToView(R.id.decompose_and_get, "\uD83C\uDF1F分解&兑换：" + getStringFromCursor(cursor, "decompose_item"));

            ImageView imageView = findViewById(R.id.decompose_image_id_card_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            exportImage(imageView, cardName, cardName0, "不转形态");

            imageView = findViewById(R.id.decompose_image_id_card_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!cardName1.equals("无")) {
                exportImage(imageView, cardName, cardName1, "一转形态");
            }

            imageView = findViewById(R.id.decompose_image_id_card_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!cardName2.equals("无")) {
                exportImage(imageView, cardName, cardName2, "二转形态");
            }

            imageView = findViewById(R.id.decompose_image_id_skill_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "初级技能书");
            }

            imageView = findViewById(R.id.decompose_image_id_skill_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "高级技能书");
            }

            imageView = findViewById(R.id.decompose_image_id_skill_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "终级技能书");
            }

            imageView = findViewById(R.id.decompose_image_id_skill_4);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "究级技能书");
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_1_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "一转凭证A");
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_1_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "一转凭证B");
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_2_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "二转凭证A");
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_2_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "二转凭证B");
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_2_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportImage(imageView, cardName, cardName0, "二转凭证C");
            }

            setTextToView(R.id.decompose_card_1, getStringFromCursor(cursor, "decompose_card_1"));
            setTextToView(R.id.decompose_card_2, getStringFromCursor(cursor, "decompose_card_2"));
            setTextToView(R.id.decompose_card_3, getStringFromCursor(cursor, "decompose_card_3"));
            setTextToView(R.id.decompose_skill_1, getStringFromCursor(cursor, "decompose_skill_1"));
            setTextToView(R.id.decompose_skill_2, getStringFromCursor(cursor, "decompose_skill_2"));
            setTextToView(R.id.decompose_skill_3, getStringFromCursor(cursor, "decompose_skill_3"));
            setTextToView(R.id.decompose_skill_4, getStringFromCursor(cursor, "decompose_skill_4"));
            setTextToView(R.id.decompose_transfer_1_a, getStringFromCursor(cursor, "decompose_transfer_1_a"));
            setTextToView(R.id.decompose_transfer_1_b, getStringFromCursor(cursor, "decompose_transfer_1_b"));
            setTextToView(R.id.decompose_transfer_2_a, getStringFromCursor(cursor, "decompose_transfer_2_a"));
            setTextToView(R.id.decompose_transfer_2_b, getStringFromCursor(cursor, "decompose_transfer_2_b"));
            setTextToView(R.id.decompose_transfer_2_c, getStringFromCursor(cursor, "decompose_transfer_2_c"));
            setTextToView(R.id.get_card_1, getStringFromCursor(cursor, "get_card_1"));
            setTextToView(R.id.get_card_2, getStringFromCursor(cursor, "get_card_2"));
            setTextToView(R.id.get_card_3, getStringFromCursor(cursor, "get_card_3"));
            setTextToView(R.id.get_skill_1, getStringFromCursor(cursor, "get_skill_1"));
            setTextToView(R.id.get_skill_2, getStringFromCursor(cursor, "get_skill_2"));
            setTextToView(R.id.get_skill_3, getStringFromCursor(cursor, "get_skill_3"));
            setTextToView(R.id.get_skill_4, getStringFromCursor(cursor, "get_skill_4"));
            setTextToView(R.id.get_transfer_1_a, getStringFromCursor(cursor, "get_transfer_1_a"));
            setTextToView(R.id.get_transfer_1_b, getStringFromCursor(cursor, "get_transfer_1_b"));
            setTextToView(R.id.get_transfer_2_a, getStringFromCursor(cursor, "get_transfer_2_a"));
            setTextToView(R.id.get_transfer_2_b, getStringFromCursor(cursor, "get_transfer_2_b"));
            setTextToView(R.id.get_transfer_2_c, getStringFromCursor(cursor, "get_transfer_2_c"));

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

        // 所有任务完成后，显示大图片
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(bigImageContainer, transition);
            findViewById(R.id.Image_View_Card_Big_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_2_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_2_2).setVisibility(View.VISIBLE);
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
                    new Intent(this, CardData4EffectActivity.class);
            default -> null;
        };
        if (intent != null) {
            intent.putExtra("name", baseName);
            intent.putExtra("table", tableName);
            startActivity(intent);
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 初始化大图片的淡入动画
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(300); // 动画时长300ms
        bigImageContainer = findViewById(R.id.big_image_container);

        cardDataContainer = findViewById(R.id.card_data_container);

        // 添加模糊材质
        setupBlurEffect();

        // 初始化流光背景
        View bgView = findViewById(R.id.bgEffectView);
        if (bgView != null) {
            bgEffectController = new BgEffectController(bgView);
            bgEffectController.setDetailAnimalCardDataColorType(this);
        }
        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }

        // 获取需要渐隐的元素
        Image_View_Card_Big_1_Container = findViewById(R.id.Image_View_Card_Big_1_Container);
        Image_View_Card_Big_2_Container = findViewById(R.id.Image_View_Card_Big_2_Container);
        Image_View_Card_Big_3_Container = findViewById(R.id.Image_View_Card_Big_3_Container);

        // 获取滚动视图SpringBackScrollView
        SpringBackScrollView scrollView = findViewById(R.id.ScrollView);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        imageViewCardBig1ContainerMaxScroll = DensityUtil.dpToPx(this, 50);
        imageViewCardBig2ContainerMaxScroll = DensityUtil.dpToPx(this, 200);
        imageViewCardBig3ContainerMaxScroll = DensityUtil.dpToPx(this, 50);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
                // 手动触发一次效果更新，让透明度与恢复的滚动位置同步
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, savedScrollY, imageViewCardBig1ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, savedScrollY, imageViewCardBig2ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_3_Container, savedScrollY, imageViewCardBig3ContainerMaxScroll);
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, scrollY, imageViewCardBig1ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, scrollY, imageViewCardBig2ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_3_Container, scrollY, imageViewCardBig3ContainerMaxScroll);
            });
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }
    }
}
