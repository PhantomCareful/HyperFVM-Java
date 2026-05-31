package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
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

public class CardData1Activity extends BaseActivity {
    private DBHelper dbHelper;

    private BgEffectController bgEffectController;
    private boolean isDynamicBackground;

    private TransitionSet transition;
    private LinearLayout Image_View_Card_Container;

    private int savedScrollY = 0;                              // 用于保存/恢复的滚动位置

    private int imageViewCardContainerMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）

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
            setContentView(R.layout.activity_card_data1_effect);
        } else {
            setContentView(R.layout.activity_card_data1);
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
    @SuppressLint({"DiscouragedApi", "Range"})
    private void queryAndShowCardData(String tableName) {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            // 从指定表中查询卡片数据
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                Toast.makeText(this, "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            // 逐个绑定控件（确保控件ID与表列名完全一致）

            // 第1张图片
            ImageView imageView = findViewById(R.id.Image_View_Card_1);
            String imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_0"));
            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
            int imageResId = getResources().getIdentifier(
                    imageIdStr,
                    "drawable",
                    getPackageName()
            );
            imageView.setImageResource(imageResId);
            exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName + "(不转形态)"));

            //第2张图片
            imageView = findViewById(R.id.Image_View_Card_2);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_1"));
            if (!imageIdStr.equals("无")) {
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                imageView.setImageResource(imageResId);
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName + "(一转形态)"));
            } else {
                findViewById(R.id.Image_View_Card_2_Container).setVisibility(View.GONE);
            }

            //第3张图片
            imageView = findViewById(R.id.Image_View_Card_3);
            imageIdStr = cursor.getString(cursor.getColumnIndex("image_id_2"));
            if (!imageIdStr.equals("无")) {
                // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                imageResId = getResources().getIdentifier(
                        imageIdStr,
                        "drawable",
                        getPackageName()
                );
                imageView.setImageResource(imageResId);
                exportInfoList.add(ImageExportUtil.generateExportInfo(imageView, cardName + "(二转形态)"));
            } else {
                findViewById(R.id.Image_View_Card_3_Container).setVisibility(View.GONE);
            }

            // 内容部分：全新的Markdown样式
            String contentBaseInfo1 = CardDataHelper.getStringFromCursor(cursor, "base_info");
            getContent(this, findViewById(R.id.base_info_1), contentBaseInfo1);

            String contentBaseInfo2 = CardDataHelper.getStringFromCursor(cursor, "transfer_certificate_info");
            if (!contentBaseInfo2.equals("无")) {
                getContent(this, findViewById(R.id.base_info_2), contentBaseInfo2);
            } else {
                findViewById(R.id.Card_BaseInfo_2).setVisibility(View.GONE);
            }

            String contentBaseInfo3 = "- 所属分类：" + CardDataHelper.getStringFromCursor(cursor, "category") + "\n" +
                    "- 耗能：" + CardDataHelper.getStringFromCursor(cursor, "price") + "\n" +
                    "- 作为副卡：" + CardDataHelper.getStringFromCursor(cursor, "sub_card");
            getContent(this, findViewById(R.id.base_info_3), contentBaseInfo3);

            String contentTransferChange = CardDataHelper.getStringFromCursor(cursor, "transfer_change");
            if (!contentTransferChange.equals("无")) {
                getContent(this, findViewById(R.id.transfer_change), contentTransferChange);
            } else {
                findViewById(R.id.title_card_data_transfer_change).setVisibility(View.GONE);
                findViewById(R.id.Card_TransferChange).setVisibility(View.GONE);
            }

            // 相关卡片
            LinearLayout container = findViewById(R.id.Card_Corresponding_Container);
            TextView titleCardDataCorrespondingInfo = findViewById(R.id.title_card_data_corresponding_info);
            CardView CardCorresponding = findViewById(R.id.Card_Corresponding);
            CardDataHelper.addCorrespondingCardForGeneralAndAnimalCard(this, container, cursor, cardName, titleCardDataCorrespondingInfo, CardCorresponding);

            // 数据信息区域（星级）
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
            Log.e("error", "数据加载失败：" + e);
        }

        // 所有任务完成后，显示大图片，带渐显动画
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransitionManager.beginDelayedTransition(Image_View_Card_Container, transition);
            findViewById(R.id.Image_View_Card_1).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_2).setVisibility(View.VISIBLE);
            findViewById(R.id.Image_View_Card_3).setVisibility(View.VISIBLE);
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
        Image_View_Card_Container = findViewById(R.id.Image_View_Card_Container);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isDynamicBackground) {
            // 初始化流光背景
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                bgEffectController.setDetailAnimalCardDataColorType(this);
            }
            if (bgEffectController != null) {
                bgEffectController.startDetailAnimalCardDataBgEffect();
            }
        }

        // 添加模糊材质
        setupBlurEffect();

        // 获取需要渐隐的元素
        Image_View_Card_Container = findViewById(R.id.Image_View_Card_Container);

        // 获取滚动视图SpringBackScrollView
        SpringBackScrollView scrollView = findViewById(R.id.ScrollView);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        imageViewCardContainerMaxScroll = DensityUtil.dpToPx(this, 50);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
                // 手动触发一次效果更新，让透明度与恢复的滚动位置同步
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Container, savedScrollY, imageViewCardContainerMaxScroll);
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(Image_View_Card_Container, savedScrollY, imageViewCardContainerMaxScroll);
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
}
