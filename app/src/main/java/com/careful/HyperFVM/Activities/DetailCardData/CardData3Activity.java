package com.careful.HyperFVM.Activities.DetailCardData;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

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
import com.careful.HyperFVM.utils.OtherUtils.ImageExportUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CardData3Activity extends BaseActivity {
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

    private String cardName;
    private final List<ExportInfo> exportInfoList = new ArrayList<>();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setContentView(R.layout.activity_card_data3_effect);
        } else {
            setContentView(R.layout.activity_card_data3);
        }

        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 获取传入的参数
        cardName = getIntent().getStringExtra("name");
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
        queryAndShowCardData(tableName);
    }

    // 查询并展示卡片数据
    @SuppressLint({"Range", "DiscouragedApi"})
    private void queryAndShowCardData(String tableName) {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            // 从指定表中查询卡片数据
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                ((TextView) findViewById(R.id.base_info)).setText("未找到卡片数据");
                return;
            }

            // 逐个绑定控件（确保控件ID与表列名完全一致）
            ImageView ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1_1);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            String cardName0 = CardDataHelper.getStringFromCursor(cursor, "name");
            setTextToView(R.id.card_name_1_1, cardName0);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName0 + "(不转形态, 大)"));

            ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            String cardName1 = CardDataHelper.getStringFromCursor(cursor, "name_1");
            setTextToView(R.id.card_name_1_2, cardName1);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName1 + "(三转形态, 大)"));

            // 对于第3、4张大图，先判断该金卡是否有终转，有和没有的情况下，需要使用的组件不一样
            String cardName2 = CardDataHelper.getStringFromCursor(cursor, "name_2");
            String cardName3 = CardDataHelper.getStringFromCursor(cursor, "name_3");

            String imageId3 = cursor.getString(cursor.getColumnIndex("image_id_3"));
            if (imageId3.equals("无")) { // 无终转
                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_2, cardName2);
                exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName2 + "(四转形态, 大)"));

                // 隐藏不用的组件
                findViewById(R.id.Image_View_Card_Big_3_Container).setVisibility(View.GONE);

                // 调整容器顶部距离
                cardDataContainer.setPadding(
                        cardDataContainer.getPaddingLeft(),
                        DensityUtil.dpToPx(this, 480),
                        cardDataContainer.getPaddingRight(),
                        cardDataContainer.getPaddingBottom());
            } else { // 有终转
                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_3_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_3_1, cardName2);
                exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName2 + "(四转形态, 大)"));

                ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_3_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_3")) + "_big";
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCardBig.setImageResource(imageResId);
                setTextToView(R.id.card_name_3_2, cardName3);
                exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName3 + "(终转形态, 大)"));

                // 隐藏不用的组件
                findViewById(R.id.Image_View_Card_Big_2_Container).setVisibility(View.GONE);

                // 调整容器顶部距离
                cardDataContainer.setPadding(
                        cardDataContainer.getPaddingLeft(),
                        DensityUtil.dpToPx(this, 520),
                        cardDataContainer.getPaddingRight(),
                        cardDataContainer.getPaddingBottom());
            }

            // 全新的Markdown样式
            String contentBaseInfo1 = CardDataHelper.getStringFromCursor(cursor, "base_info");
            getContent(this, findViewById(R.id.base_info_1), contentBaseInfo1);

            String contentBaseInfo2 = "- 所属分类：" + CardDataHelper.getStringFromCursor(cursor, "category") + "\n" +
                    "- 耗能：" + CardDataHelper.getStringFromCursor(cursor, "price") + "\n" +
                    "- 作为副卡：" + CardDataHelper.getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info_2), contentBaseInfo2);

            String contentTransferChange = CardDataHelper.getStringFromCursor(cursor, "transfer_change");
            if (!contentTransferChange.equals("无")) {
                getContent(this, findViewById(R.id.transfer_change), contentTransferChange);
            }

            // 相关卡片
            // 金卡合成的卡片
            if (!CardDataHelper.getStringFromCursor(cursor, "name_1_1").equals("无")) {
                // 缓存点击事件需要用到的字段值
                String name1_1 = CardDataHelper.getStringFromCursor(cursor, "name_1_1");
                String name1_2 = CardDataHelper.getStringFromCursor(cursor, "name_1_2");

                TextView correspondingSubCardName = findViewById(R.id.card_data_index_corresponding_sub_card_name_1);
                TextView correspondingSubCardContent = findViewById(R.id.card_data_index_corresponding_sub_card_content_1);
                correspondingSubCardName.setText(name1_1);
                correspondingSubCardContent.setText("此卡片是合成本金卡的必要素材");

                ImageView ImageViewCard = findViewById(R.id.card_data_index_corresponding_sub_card_image_1);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_1"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);
                exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, name1_1 + "(进化用卡1)"));

                correspondingSubCardName = findViewById(R.id.card_data_index_corresponding_sub_card_name_2);
                correspondingSubCardContent = findViewById(R.id.card_data_index_corresponding_sub_card_content_2);
                correspondingSubCardName.setText(name1_2);
                correspondingSubCardContent.setText("此卡片是合成本金卡的必要素材");

                ImageViewCard = findViewById(R.id.card_data_index_corresponding_sub_card_image_2);
                imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_2"));
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                ImageViewCard.setImageResource(imageResId);
                exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, name1_2 + "(进化用卡2)"));

                // 给相关卡片设置跳转查询的点击事件
                findViewById(R.id.card_data_index_corresponding_sub_card_1_container).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name1_1));
                findViewById(R.id.card_data_index_corresponding_sub_card_2_container).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name1_2));
            } else {
                findViewById(R.id.Card_Corresponding_SubCard).setVisibility(View.GONE);
            }

            // 其他（如增幅卡）
            LinearLayout container = findViewById(R.id.Card_Corresponding_Container);
            TextView titleCardDataCorrespondingInfo = findViewById(R.id.title_card_data_corresponding_info);
            CardView CardCorresponding = findViewById(R.id.Card_Corresponding);
            CardDataHelper.addCorrespondingCardForGoldenCard(this, container, cursor, cardName, titleCardDataCorrespondingInfo, CardCorresponding);

            // 数据信息区域
            // 星级数据
            setTextToView(R.id.star, "\uD83C\uDF1F强化提升：" + CardDataHelper.getStringFromCursor(cursor, "star"));
            setTextToView(R.id.star_detail, CardDataHelper.getStringFromCursor(cursor, "star_detail"));
            setTextToView(R.id.star_0, CardDataHelper.getStringFromCursor(cursor, "star_0"));
            setTextToView(R.id.star_1, CardDataHelper.getStringFromCursor(cursor, "star_1"));
            setTextToView(R.id.star_2, CardDataHelper.getStringFromCursor(cursor, "star_2"));
            setTextToView(R.id.star_3, CardDataHelper.getStringFromCursor(cursor, "star_3"));
            setTextToView(R.id.star_4, CardDataHelper.getStringFromCursor(cursor, "star_4"));
            setTextToView(R.id.star_5, CardDataHelper.getStringFromCursor(cursor, "star_5"));
            setTextToView(R.id.star_6, CardDataHelper.getStringFromCursor(cursor, "star_6"));
            setTextToView(R.id.star_7, CardDataHelper.getStringFromCursor(cursor, "star_7"));
            setTextToView(R.id.star_8, CardDataHelper.getStringFromCursor(cursor, "star_8"));
            setTextToView(R.id.star_9, CardDataHelper.getStringFromCursor(cursor, "star_9"));
            setTextToView(R.id.star_10, CardDataHelper.getStringFromCursor(cursor, "star_10"));
            setTextToView(R.id.star_11, CardDataHelper.getStringFromCursor(cursor, "star_11"));
            setTextToView(R.id.star_12, CardDataHelper.getStringFromCursor(cursor, "star_12"));
            setTextToView(R.id.star_13, CardDataHelper.getStringFromCursor(cursor, "star_13"));
            setTextToView(R.id.star_14, CardDataHelper.getStringFromCursor(cursor, "star_14"));
            setTextToView(R.id.star_15, CardDataHelper.getStringFromCursor(cursor, "star_15"));
            setTextToView(R.id.star_16, CardDataHelper.getStringFromCursor(cursor, "star_16"));
            setTextToView(R.id.star_M, CardDataHelper.getStringFromCursor(cursor, "star_M"));
            setTextToView(R.id.star_U, CardDataHelper.getStringFromCursor(cursor, "star_U"));

            // 金卡援护（如果有的话）
            if (CardDataHelper.getStringFromCursor(cursor, "support_1").equals("无")) {
                findViewById(R.id.card_data_support_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Support).setVisibility(View.GONE);
            } else {
                //全新的Markdown样式
                /*
                  TODO:Markdown渲染的表格在PAD端上有点问题，最右边可能会多出来一截或少掉一截，目前尚不清楚是什么原因。
                 */
                getContent(this, findViewById(R.id.support_info_1), CardDataHelper.getStringFromCursor(cursor, "support_1"));
                getContent(this, findViewById(R.id.support_info_2), CardDataHelper.getStringFromCursor(cursor, "support_2"));
            }

            // 技能数据
            setTextToView(R.id.skill, "\uD83C\uDF1F技能提升：" + CardDataHelper.getStringFromCursor(cursor, "skill"));
            if (CardDataHelper.getStringFromCursor(cursor, "skill").equals("该防御卡不支持技能")) {
                findViewById(R.id.Card_Skill).setVisibility(View.GONE);
            } else {
                setTextToView(R.id.skill_detail, CardDataHelper.getStringFromCursor(cursor, "skill_detail"));
                setTextToView(R.id.skill_0, CardDataHelper.getStringFromCursor(cursor, "skill_0"));
                setTextToView(R.id.skill_1, CardDataHelper.getStringFromCursor(cursor, "skill_1"));
                setTextToView(R.id.skill_2, CardDataHelper.getStringFromCursor(cursor, "skill_2"));
                setTextToView(R.id.skill_3, CardDataHelper.getStringFromCursor(cursor, "skill_3"));
                setTextToView(R.id.skill_4, CardDataHelper.getStringFromCursor(cursor, "skill_4"));
                setTextToView(R.id.skill_5, CardDataHelper.getStringFromCursor(cursor, "skill_5"));
                setTextToView(R.id.skill_6, CardDataHelper.getStringFromCursor(cursor, "skill_6"));
                setTextToView(R.id.skill_7, CardDataHelper.getStringFromCursor(cursor, "skill_7"));
                setTextToView(R.id.skill_8, CardDataHelper.getStringFromCursor(cursor, "skill_8"));
            }

            // 分解&兑换信息
            setTextToView(R.id.decompose_and_get, "\uD83C\uDF1F分解&兑换：" + CardDataHelper.getStringFromCursor(cursor, "decompose_item"));

            ImageView imageView = findViewById(R.id.decompose_image_id_card_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(不转形态)"));

            imageView = findViewById(R.id.decompose_image_id_card_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName1 + "(三转形态)"));

            imageView = findViewById(R.id.decompose_image_id_card_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName2 + "(四转形态)"));

            imageView = findViewById(R.id.decompose_image_id_card_4);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_card_4"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!cardName3.equals("无")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName3 + "(终转形态)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(初级技能书)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(高级技能书)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(终极技能书)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(究极技能书)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(三转凭证A)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(三转凭证B)"));
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_1_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(三转凭证C)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(四转凭证A)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(四转凭证B)"));
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
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(四转凭证C)"));
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_3_a);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(终转凭证A)"));
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_3_b);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(终转凭证B)"));
            }

            imageView = findViewById(R.id.decompose_image_id_transfer_3_c);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(终转凭证C)"));
            }

            imageView = findViewById(R.id.decompose_image_id_compose);
            imageIdStr = cursor.getString(cursor.getColumnIndex("decompose_image_id_compose"));
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            if (!imageIdStr.equals("card_data_x")) {
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName0 + "(进化凭证)"));
            }

            setTextToView(R.id.decompose_card_1, CardDataHelper.getStringFromCursor(cursor, "decompose_card_1"));
            setTextToView(R.id.decompose_card_2, CardDataHelper.getStringFromCursor(cursor, "decompose_card_2"));
            setTextToView(R.id.decompose_card_3, CardDataHelper.getStringFromCursor(cursor, "decompose_card_3"));
            setTextToView(R.id.decompose_card_4, CardDataHelper.getStringFromCursor(cursor, "decompose_card_4"));
            setTextToView(R.id.decompose_skill_1, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1"));
            setTextToView(R.id.decompose_skill_2, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2"));
            setTextToView(R.id.decompose_skill_3, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3"));
            setTextToView(R.id.decompose_skill_4, CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4"));
            setTextToView(R.id.decompose_transfer_1_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a"));
            setTextToView(R.id.decompose_transfer_1_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b"));
            setTextToView(R.id.decompose_transfer_1_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c"));
            setTextToView(R.id.decompose_transfer_2_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a"));
            setTextToView(R.id.decompose_transfer_2_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b"));
            setTextToView(R.id.decompose_transfer_2_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c"));
            setTextToView(R.id.decompose_transfer_3_a, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a"));
            setTextToView(R.id.decompose_transfer_3_b, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b"));
            setTextToView(R.id.decompose_transfer_3_c, CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c"));
            setTextToView(R.id.decompose_compose, CardDataHelper.getStringFromCursor(cursor, "decompose_compose"));
            setTextToView(R.id.get_card_1, CardDataHelper.getStringFromCursor(cursor, "get_card_1"));
            setTextToView(R.id.get_card_2, CardDataHelper.getStringFromCursor(cursor, "get_card_2"));
            setTextToView(R.id.get_card_3, CardDataHelper.getStringFromCursor(cursor, "get_card_3"));
            setTextToView(R.id.get_card_4, CardDataHelper.getStringFromCursor(cursor, "get_card_4"));
            setTextToView(R.id.get_skill_1, CardDataHelper.getStringFromCursor(cursor, "get_skill_1"));
            setTextToView(R.id.get_skill_2, CardDataHelper.getStringFromCursor(cursor, "get_skill_2"));
            setTextToView(R.id.get_skill_3, CardDataHelper.getStringFromCursor(cursor, "get_skill_3"));
            setTextToView(R.id.get_skill_4, CardDataHelper.getStringFromCursor(cursor, "get_skill_4"));
            setTextToView(R.id.get_transfer_1_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a"));
            setTextToView(R.id.get_transfer_1_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b"));
            setTextToView(R.id.get_transfer_1_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c"));
            setTextToView(R.id.get_transfer_2_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a"));
            setTextToView(R.id.get_transfer_2_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b"));
            setTextToView(R.id.get_transfer_2_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c"));
            setTextToView(R.id.get_transfer_3_a, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a"));
            setTextToView(R.id.get_transfer_3_b, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b"));
            setTextToView(R.id.get_transfer_3_c, CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c"));
            setTextToView(R.id.get_compose, CardDataHelper.getStringFromCursor(cursor, "get_compose"));

            // 其他信息
            if (CardDataHelper.getStringFromCursor(cursor, "additional_info").equals("无")) {
                findViewById(R.id.card_data_other_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Other).setVisibility(View.GONE);
            } else {
                //全新的Markdown样式
                getContent(this, findViewById(R.id.additional_info), CardDataHelper.getStringFromCursor(cursor, "additional_info"));
            }

        } catch (Exception e) {
            ((TextView) findViewById(R.id.base_info_1)).setText("数据加载失败");
        }

        // 所有任务完成后，显示大图片
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(bigImageContainer, transition);
            findViewById(R.id.Image_View_Card_Big_1_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_1_2).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_2).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_3_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_3_2).setVisibility(View.VISIBLE);
        }, 500);
    }

    // 辅助方法：设置文本到控件，避免重复代码
    private void setTextToView(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
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
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView floatButtonExportContainer = findViewById(R.id.FloatButton_Export_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.getStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonExportContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonExportContainer.setLayoutParams(params);
        });

        // 初始化大图片的淡入动画
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(300); // 动画时长300ms
        bigImageContainer = findViewById(R.id.big_image_container);

        cardDataContainer = findViewById(R.id.card_data_container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 初始化流光背景
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                bgEffectController.setDetailGoldenCardDataColorType(this);
            }
            if (bgEffectController != null) {
                bgEffectController.startDetailGoldenCardDataBgEffect();
            }
        }

        // 添加模糊材质
        setupBlurEffect();

        // 获取需要渐隐的元素
        Image_View_Card_Big_1_Container = findViewById(R.id.Image_View_Card_Big_1_Container);
        Image_View_Card_Big_2_Container = findViewById(R.id.Image_View_Card_Big_2_Container);
        Image_View_Card_Big_3_Container = findViewById(R.id.Image_View_Card_Big_3_Container);

        // 获取滚动视图SpringBackScrollView
        SpringBackScrollView scrollView = findViewById(R.id.ScrollView);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        imageViewCardBig1ContainerMaxScroll = DensityUtil.dpToPx(this, 200);
        imageViewCardBig2ContainerMaxScroll = DensityUtil.dpToPx(this, 50);
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
        blurUtil.setBlur(findViewById(R.id.blurViewButtonExport));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Export_Container).setOnClickListener(v -> exportAllImages(exportInfoList));
    }

    /**
     * 批量导出图片
     */
    private void exportAllImages(List<ExportInfo> exportInfoList) {
        DialogBuilderManager.showExportAllImagesDialog(this, cardName, exportInfoList);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (bgEffectController != null) {
                bgEffectController.startDetailGoldenCardDataBgEffect();
            }
        }
    }
}
