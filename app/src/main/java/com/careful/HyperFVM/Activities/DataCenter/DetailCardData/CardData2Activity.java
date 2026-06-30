package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import android.widget.ScrollView;
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

public class CardData2Activity extends BaseActivity {
    private DBHelper dbHelper;

    private BgEffectController bgEffectController;
    private boolean isDynamicBackground;

    private TransitionSet transition;
    private LinearLayout bigImageContainer;

    private View Image_View_Card_Big_1_Container;
    private View Image_View_Card_Big_2_Container;

    private int savedScrollY = 0;                              // 用于保存/恢复的滚动位置

    private int imageViewCardBig1ContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int imageViewCardBig2ContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    
    private String cardName;
    private final List<ExportInfo> exportInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        // 初始化数据库工具
        dbHelper = new DBHelper(this);

        // 是否启用动态背景
        isDynamicBackground = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_BACKGROUND);

        super.onCreate(savedInstanceState);
        // 初始化布局和基础设置
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isDynamicBackground) {
            setContentView(R.layout.activity_card_data2_effect);
        } else {
            setContentView(R.layout.activity_card_data2);
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
            Toast.makeText(this, "cardName或tableName为null", Toast.LENGTH_SHORT).show();
            finish(); // 参数错误直接关闭页面
            return;
        }

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
                Toast.makeText(this, "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取卡片的名字
            String cardName1 = cursor.getString(cursor.getColumnIndex("name"));
            String cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
            String cardName3 = cursor.getString(cursor.getColumnIndex("name_3"));

            // 逐个绑定控件（确保控件ID与表列名完全一致）
            // 大图片区域
            ImageView ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1_1);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            setTextToView(R.id.card_name_1_1, cardName1);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName1 + "(初级融合, 大)"));

            ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_1_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            setTextToView(R.id.card_name_1_2, cardName2);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName2 + "(深度融合, 大)"));

            ImageViewCardBig = findViewById(R.id.Image_View_Card_Big_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3")) + "_big";
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCardBig.setImageResource(imageResId);
            setTextToView(R.id.card_name_2, CardDataHelper.getStringFromCursor(cursor, "name_3"));
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCardBig, cardName3 + "(灵魂融合, 大)"));

            // 内容部分：全新的Markdown样式
            String contentBaseInfo1 = CardDataHelper.getStringFromCursor(cursor, "base_info");
            getContent(this, findViewById(R.id.base_info_1), contentBaseInfo1);

            String contentBaseInfo2 = "- 所属分类：" + CardDataHelper.getStringFromCursor(cursor, "category") + "\n" +
                    "- 耗能：" + CardDataHelper.getStringFromCursor(cursor, "price") + "\n" +
                    "- 作为副卡：" + CardDataHelper.getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info_2), contentBaseInfo2);

            String contentTransferChange = CardDataHelper.getStringFromCursor(cursor, "transfer_change");
            if (!contentTransferChange.equals("无")) {
                getContent(this, findViewById(R.id.transfer_change), contentTransferChange);
            } else {
                findViewById(R.id.title_card_data_transfer_change).setVisibility(View.GONE);
                findViewById(R.id.Card_TransferChange).setVisibility(View.GONE);
            }

            // 相关卡片
            // 参与融合的卡片
            ImageView ImageViewCard = findViewById(R.id.Image_View_Card_1_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName1 + "(初级融合, 主卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_1_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName1 + "(初级融合, 融合副卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName1 + "(初级融合)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_2_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_1"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName2 + "(深度融合, 主卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_2_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName2 + "(深度融合, 融合副卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName2 + "(深度融合)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_3_1);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName3 + "(灵魂融合, 主卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_3_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_3_2"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName3 + "(灵魂融合, 融合副卡)"));

            ImageViewCard = findViewById(R.id.Image_View_Card_Result_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_result_id_3"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            ImageViewCard.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(ImageViewCard, cardName3 + "(灵魂融合)"));

            // 给相关卡片设置跳转查询的点击事件
            // 缓存点击事件需要用到的字段值
            String name1_1 = CardDataHelper.getStringFromCursor(cursor, "name_1_1");
            String name1_2 = CardDataHelper.getStringFromCursor(cursor, "name_1_2");
            String name2_2 = CardDataHelper.getStringFromCursor(cursor, "name_2_2");
            String name3_2 = CardDataHelper.getStringFromCursor(cursor, "name_3_2");
            findViewById(R.id.Image_View_Card_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name1_1));
            findViewById(R.id.Image_View_Card_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name1_2));
            findViewById(R.id.Image_View_Card_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name2_2));
            findViewById(R.id.Image_View_Card_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, name3_2));

            // 其他（如增幅卡）
            LinearLayout container = findViewById(R.id.Card_Corresponding_Container);
            CardView CardCorresponding = findViewById(R.id.Card_Corresponding);
            CardDataHelper.addCorrespondingCardForFusionCard(this, container, cursor, cardName, CardCorresponding);

            // 星级信息
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

            // 品阶信息
            setTextToView(R.id.star_fusion, "\uD83C\uDF1F品阶提升：" + "\n" + CardDataHelper.getStringFromCursor(cursor, "star_fusion"));
            setTextToView(R.id.star_fusion_detail, CardDataHelper.getStringFromCursor(cursor, "star_fusion_detail"));
            setTextToView(R.id.star_fusion_0, CardDataHelper.getStringFromCursor(cursor, "star_fusion_0"));
            setTextToView(R.id.star_fusion_1, CardDataHelper.getStringFromCursor(cursor, "star_fusion_1"));
            setTextToView(R.id.star_fusion_2, CardDataHelper.getStringFromCursor(cursor, "star_fusion_2"));
            setTextToView(R.id.star_fusion_3, CardDataHelper.getStringFromCursor(cursor, "star_fusion_3"));
            setTextToView(R.id.star_fusion_4, CardDataHelper.getStringFromCursor(cursor, "star_fusion_4"));
            setTextToView(R.id.star_fusion_5, CardDataHelper.getStringFromCursor(cursor, "star_fusion_5"));
            setTextToView(R.id.star_fusion_6, CardDataHelper.getStringFromCursor(cursor, "star_fusion_6"));
            setTextToView(R.id.star_fusion_7, CardDataHelper.getStringFromCursor(cursor, "star_fusion_7"));
            setTextToView(R.id.star_fusion_8, CardDataHelper.getStringFromCursor(cursor, "star_fusion_8"));
            setTextToView(R.id.star_fusion_9, CardDataHelper.getStringFromCursor(cursor, "star_fusion_9"));
            setTextToView(R.id.star_fusion_10, CardDataHelper.getStringFromCursor(cursor, "star_fusion_10"));
            setTextToView(R.id.star_fusion_11, CardDataHelper.getStringFromCursor(cursor, "star_fusion_11"));
            setTextToView(R.id.star_fusion_12, CardDataHelper.getStringFromCursor(cursor, "star_fusion_12"));
            setTextToView(R.id.star_fusion_13, CardDataHelper.getStringFromCursor(cursor, "star_fusion_13"));
            setTextToView(R.id.star_fusion_14, CardDataHelper.getStringFromCursor(cursor, "star_fusion_14"));
            setTextToView(R.id.star_fusion_15, CardDataHelper.getStringFromCursor(cursor, "star_fusion_15"));
            setTextToView(R.id.star_fusion_16, CardDataHelper.getStringFromCursor(cursor, "star_fusion_16"));
            setTextToView(R.id.star_fusion_M, CardDataHelper.getStringFromCursor(cursor, "star_fusion_M"));
            setTextToView(R.id.star_fusion_U, CardDataHelper.getStringFromCursor(cursor, "star_fusion_U"));

            // 技能信息
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

            // 其他信息
            if (CardDataHelper.getStringFromCursor(cursor, "additional_info").equals("无")) {
                findViewById(R.id.card_data_other_title).setVisibility(View.GONE);
                findViewById(R.id.Card_Other).setVisibility(View.GONE);
            } else {
                // 全新的Markdown样式
                getContent(this, findViewById(R.id.additional_info), CardDataHelper.getStringFromCursor(cursor, "additional_info"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
        }

        // 所有任务完成后，显示大图片
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(bigImageContainer, transition);
            findViewById(R.id.Image_View_Card_Big_1_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_1_2).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_Big_2).setVisibility(View.VISIBLE);
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
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonExportContainer = findViewById(R.id.FloatButton_Export_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonExportContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonExportContainer.setLayoutParams(params);
        });

        // 设置顶栏标题
        TextView topBar = findViewById(R.id.topBar);
        topBar.setText(cardName);

        // 初始化大图片的淡入动画
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(300); // 动画时长300ms
        bigImageContainer = findViewById(R.id.big_image_container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isDynamicBackground) {
            // 初始化流光背景
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                bgEffectController.setDetailFusionCardDataColorType(this);
            }
            if (bgEffectController != null) {
                bgEffectController.startDetailFusionCardDataBgEffect();
            }
        }

        // 添加模糊材质
        setupBlurEffect();

        // 获取需要渐隐的元素
        Image_View_Card_Big_1_Container = findViewById(R.id.Image_View_Card_Big_1_Container);
        Image_View_Card_Big_2_Container = findViewById(R.id.Image_View_Card_Big_2_Container);

        // 获取滚动视图ScrollView
        ScrollView scrollView = findViewById(R.id.ScrollView);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        imageViewCardBig1ContainerMaxScroll = DensityUtil.dpToPx(this, 200);
        imageViewCardBig2ContainerMaxScroll = DensityUtil.dpToPx(this, 50);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
                // 手动触发一次效果更新，让透明度与恢复的滚动位置同步
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, savedScrollY, imageViewCardBig1ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, savedScrollY, imageViewCardBig2ContainerMaxScroll);
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_1_Container, scrollY, imageViewCardBig1ContainerMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Big_2_Container, scrollY, imageViewCardBig2ContainerMaxScroll);
            });
        }
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonExport));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Export_Container).setOnClickListener(v -> exportAllImages(exportInfoList));
    }

    /**
     * 批量导出图片
     */
    private void exportAllImages(List<ExportInfo> exportInfoList) {
        DialogBuilderManager.showDialogWithCallBack(
                this, "导出所有图片", "📦",
                "图片将保存到：\nPictures/" + getResources().getString(R.string.app_name) + "/" + cardName, true,
                "咱手滑了", "一键导出", () -> ImageExportUtil.exportAllImages(this, cardName, exportInfoList)
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }
}
