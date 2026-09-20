package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList5Binding;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList5EffectBinding;
import com.careful.HyperFVM.databinding.CardCardDataAuxiliaryList5Binding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class AuxiliaryList5Activity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list5_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list5_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list5_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private CardCardDataAuxiliaryList5Binding cardListBinding;// 增幅名单卡片列表（普通/流光两种布局的 include 同 id 同类型，合并后共用同一引用）
    private BlurUtil blurUtil;
    private DBHelper dbHelper;
    private BgEffectController bgEffectController;// 流光背景控制器（仅“动态背景”启用时初始化）
    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（仅手机单栏布局接入，PAD 双栏布局不接入）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        // 单活动双布局：按“动态背景”总开关选用 带流光背景/不带流光背景 的布局（与卡片数据详情页同构）
        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            ActivityAuxiliaryList5EffectBinding effectBinding = ActivityAuxiliaryList5EffectBinding.inflate(getLayoutInflater());
            cardListBinding = Objects.requireNonNull(effectBinding.cardCardDataAuxiliaryList5);
            setContentView(effectBinding.getRoot());
        } else {
            ActivityAuxiliaryList5Binding normalBinding = ActivityAuxiliaryList5Binding.inflate(getLayoutInflater());
            cardListBinding = Objects.requireNonNull(normalBinding.cardCardDataAuxiliaryList5);
            setContentView(normalBinding.getRoot());
        }

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 初始化各种装饰效果
        initDecoration();

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }}, 50);
    }

    private void initDecoration() {
        // 适配状态栏高度（顶栏模糊层/悬浮标题/返回按钮，与卡片数据详情页同构）
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
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

        // 设置返回按钮功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            nestedScrollUtil = NestedScrollUtil.attach(
                    findViewById(R.id.scrollView),
                    findViewById(R.id.topBarBottom),
                    topBar,
                    blurViewTopBar,
                    TOP_BAR_FADE_RANGE_DP);
        }

        // 初始化流光背景（仅动态背景启用时布局内存在 bgEffectView）
        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                bgEffectController.setDetailGoldenCardDataColorType(this);
                bgEffectController.startDetailGoldenCardDataBgEffect();
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
     * 加载所有卡片的点击事件
     */
    private void initCardImages() {
        // 增幅卡
        findViewById(R.id.card_data_index_background_images_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔利亚神使"));
        findViewById(R.id.card_data_index_background_images_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔利亚神使"));

        // 增幅名单
        cardListBinding.cardCardDataIndex236.cardDataIndex236.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "马卡龙烤箱"));
        cardListBinding.cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        cardListBinding.cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        cardListBinding.cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        cardListBinding.cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        cardListBinding.cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        cardListBinding.cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        cardListBinding.cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        cardListBinding.cardCardDataIndex954.cardDataIndex954.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        cardListBinding.cardCardDataIndex955.cardDataIndex955.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "归元马"));
        cardListBinding.cardCardDataIndex958.cardDataIndex958.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "萌海马"));
        cardListBinding.cardCardDataIndex1213.cardDataIndex1213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        cardListBinding.cardCardDataIndex923.cardDataIndex923.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火箭猪"));
        cardListBinding.cardCardDataIndex1212.cardDataIndex1212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        cardListBinding.cardCardDataIndex1135.cardDataIndex1135.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "御风马"));
        // 四转追加
        cardListBinding.cardCardDataIndex922.cardDataIndex922.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雅典娜守护"));
        cardListBinding.cardCardDataIndex924.cardDataIndex924.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "宙斯神使"));
        cardListBinding.cardCardDataIndex931.cardDataIndex931.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔法猪"));
        cardListBinding.cardCardDataIndex932.cardDataIndex932.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "招财喵"));
        cardListBinding.cardCardDataIndex933.cardDataIndex933.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪球兔"));
        cardListBinding.cardCardDataIndex934.cardDataIndex934.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "典伊神使"));
        cardListBinding.cardCardDataIndex935.cardDataIndex935.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰晶龙"));
        cardListBinding.cardCardDataIndex936.cardDataIndex936.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));
        cardListBinding.cardCardDataIndex714Auxiliary.cardDataIndex714Auxiliary.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "至尊大力神"));
        cardListBinding.cardCardDataIndex727.cardDataIndex727.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "埃罗斯神使"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            if (bgEffectController != null) {
                bgEffectController.startDetailGoldenCardDataBgEffect();
            }
        }
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        } else {
            // PAD 双栏：不接入联动，仅保存两栏滚动位置（滚动容器按 id 存在性处理，各页 PAD 布局栏数不同）
            View scrollView1 = findViewById(R.id.scrollView1);
            if (scrollView1 != null) {
                outState.putInt(STATE_SCROLL_Y_PAD_1, scrollView1.getScrollY());
            }
            View scrollView2 = findViewById(R.id.scrollView2);
            if (scrollView2 != null) {
                outState.putInt(STATE_SCROLL_Y_PAD_2, scrollView2.getScrollY());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
            return;
        }

        if (SmallestWidthUtil.getSmallestWidthDp() >= 600) {
            View scrollView1 = findViewById(R.id.scrollView1);
            if (scrollView1 != null) {
                int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y_PAD_1, 0);
                if (scrollY > 0) {
                    scrollView1.setScrollY(scrollY);
                }
            }
            View scrollView2 = findViewById(R.id.scrollView2);
            if (scrollView2 != null) {
                int scrollY = savedInstanceState.getInt(STATE_SCROLL_Y_PAD_2, 0);
                if (scrollY > 0) {
                    scrollView2.setScrollY(scrollY);
                }
            }
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