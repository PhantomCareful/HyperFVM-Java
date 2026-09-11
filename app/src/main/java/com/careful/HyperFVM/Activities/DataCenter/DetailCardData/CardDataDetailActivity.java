package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import eightbitlab.com.blurview.BlurView;

public class CardDataDetailActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_card_dara_detail_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;

    private BgEffectController bgEffectController;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（本页仅联动模糊层）

    private String cardName;
    private int tableId;
    private String starDataDetail;
    private String[] starDataArray;
    private String fusionData;
    private String fusionDataDetail;
    private String[] fusionDataArray;
    private String skillData;
    private String skillDataDetail;
    private String[] skillDataArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        // 是否启用动态背景

        super.onCreate(savedInstanceState);

        // 初始化布局和基础设置
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            setContentView(R.layout.activity_card_dara_detail_effect);
        } else {
            setContentView(R.layout.activity_card_dara_detail);
        }

        cardName = getIntent().getStringExtra("cardName");
        tableId = getIntent().getIntExtra("tableId", 1);
        starDataDetail = getIntent().getStringExtra("starDataDetail");
        starDataArray = getIntent().getStringArrayExtra("starDataArray");
        fusionData = getIntent().getStringExtra("fusionData");
        fusionDataDetail = getIntent().getStringExtra("fusionDataDetail");
        fusionDataArray = getIntent().getStringArrayExtra("fusionDataArray");
        skillData = getIntent().getStringExtra("skillData");
        skillDataDetail = getIntent().getStringExtra("skillDataDetail");
        skillDataArray = getIntent().getStringArrayExtra("skillDataArray");

        // 初始化各种装饰效果
        initDecoration();

        // 查询卡片数据并显示
        displayDetailCardData();
    }

    // 展示卡片数据
    @SuppressLint({"Range", "DiscouragedApi"})
    private void displayDetailCardData() {
        // 星级信息
        setTextToView(R.id.star_detail, starDataDetail);
        setTextToView(R.id.star_0, starDataArray[0]);
        setTextToView(R.id.star_1, starDataArray[1]);
        setTextToView(R.id.star_2, starDataArray[2]);
        setTextToView(R.id.star_3, starDataArray[3]);
        setTextToView(R.id.star_4, starDataArray[4]);
        setTextToView(R.id.star_5, starDataArray[5]);
        setTextToView(R.id.star_6, starDataArray[6]);
        setTextToView(R.id.star_7, starDataArray[7]);
        setTextToView(R.id.star_8, starDataArray[8]);
        setTextToView(R.id.star_9, starDataArray[9]);
        setTextToView(R.id.star_10, starDataArray[10]);
        setTextToView(R.id.star_11, starDataArray[11]);
        setTextToView(R.id.star_12, starDataArray[12]);
        setTextToView(R.id.star_13, starDataArray[13]);
        setTextToView(R.id.star_14, starDataArray[14]);
        setTextToView(R.id.star_15, starDataArray[15]);
        setTextToView(R.id.star_16, starDataArray[16]);
        setTextToView(R.id.star_M, starDataArray[17]);
        setTextToView(R.id.star_U, starDataArray[18]);

        // 品阶信息
        if (fusionData.equals("无")) {
            findViewById(R.id.Card_Fusion).setVisibility(View.GONE);
        } else {
            setTextToView(R.id.star_fusion_detail, fusionDataDetail);
            setTextToView(R.id.star_fusion_1, fusionDataArray[1]);
            setTextToView(R.id.star_fusion_2, fusionDataArray[2]);
            setTextToView(R.id.star_fusion_3, fusionDataArray[3]);
            setTextToView(R.id.star_fusion_4, fusionDataArray[4]);
            setTextToView(R.id.star_fusion_5, fusionDataArray[5]);
            setTextToView(R.id.star_fusion_6, fusionDataArray[6]);
            setTextToView(R.id.star_fusion_7, fusionDataArray[7]);
            setTextToView(R.id.star_fusion_8, fusionDataArray[8]);
            setTextToView(R.id.star_fusion_9, fusionDataArray[9]);
            setTextToView(R.id.star_fusion_10, fusionDataArray[10]);
            setTextToView(R.id.star_fusion_11, fusionDataArray[11]);
            setTextToView(R.id.star_fusion_12, fusionDataArray[12]);
            setTextToView(R.id.star_fusion_13, fusionDataArray[13]);
            setTextToView(R.id.star_fusion_14, fusionDataArray[14]);
            setTextToView(R.id.star_fusion_15, fusionDataArray[15]);
            setTextToView(R.id.star_fusion_16, fusionDataArray[16]);
            setTextToView(R.id.star_fusion_M, fusionDataArray[17]);
            setTextToView(R.id.star_fusion_U, fusionDataArray[18]);
        }

        // 技能信息
        if (skillData.equals("该防御卡不支持技能")) {
            findViewById(R.id.Card_Skill).setVisibility(View.GONE);
        } else {
            setTextToView(R.id.skill_detail, skillDataDetail);
            setTextToView(R.id.skill_0, skillDataArray[0]);
            setTextToView(R.id.skill_1, skillDataArray[1]);
            setTextToView(R.id.skill_2, skillDataArray[2]);
            setTextToView(R.id.skill_3, skillDataArray[3]);
            setTextToView(R.id.skill_4, skillDataArray[4]);
            setTextToView(R.id.skill_5, skillDataArray[5]);
            setTextToView(R.id.skill_6, skillDataArray[6]);
            setTextToView(R.id.skill_7, skillDataArray[7]);
            setTextToView(R.id.skill_8, skillDataArray[8]);
        }
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
    @SuppressLint("SetTextI18n")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout card_data_detail_container = findViewById(R.id.card_data_detail_container);
        InsetsUtil.setMarginHorizontal(this, card_data_detail_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card_data_detail_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            card_data_detail_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 设置顶栏标题
        topBar.setText(cardName + " - 数据横屏展示");

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：本页只联动模糊背景层（topBarBottom/topBar 传0跳过）
        nestedScrollUtil = NestedScrollUtil.attach(rootView,
                R.id.scrollView, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                switch (tableId) {
                    case 1, 4:
                        bgEffectController.setDetailAnimalCardDataColorType(this);
                        break;
                    case 2:
                        bgEffectController.setDetailFusionCardDataColorType(this);
                        break;
                    case 3:
                        bgEffectController.setDetailGoldenCardDataColorType(this);
                        break;
                }
            }
            if (bgEffectController != null) {
                switch (tableId) {
                    case 1, 4:
                        bgEffectController.startDetailAnimalCardDataBgEffect();
                        break;
                    case 2:
                        bgEffectController.startDetailFusionCardDataBgEffect();
                        break;
                    case 3:
                        bgEffectController.startDetailGoldenCardDataBgEffect();
                        break;
                }
            }
        }

        // 添加模糊材质
        setupBlurEffect();

    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), HyperFVMApplication.isContentDynamicBackgroundEnabled() ? 0f : 0.5f);
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}