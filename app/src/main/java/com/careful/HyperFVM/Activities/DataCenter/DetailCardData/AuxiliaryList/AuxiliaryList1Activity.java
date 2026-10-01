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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList1Binding;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList1EffectBinding;
import com.careful.HyperFVM.databinding.CardCardDataAuxiliaryList1Binding;
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

public class AuxiliaryList1Activity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list1_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list1_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list1_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private CardCardDataAuxiliaryList1Binding cardListBinding;// 增幅名单卡片列表（普通/流光两种布局的 include 同 id 同类型，合并后共用同一引用）
    private DBHelper dbHelper;
    private BlurUtil blurUtil;
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
            ActivityAuxiliaryList1EffectBinding effectBinding = ActivityAuxiliaryList1EffectBinding.inflate(getLayoutInflater());
            cardListBinding = Objects.requireNonNull(effectBinding.cardCardDataAuxiliaryList1);
            setContentView(effectBinding.getRoot());
        } else {
            ActivityAuxiliaryList1Binding normalBinding = ActivityAuxiliaryList1Binding.inflate(getLayoutInflater());
            cardListBinding = Objects.requireNonNull(normalBinding.cardCardDataAuxiliaryList1);
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
                bgEffectController.setDetailAnimalCardDataColorType(this);
                bgEffectController.startDetailAnimalCardDataBgEffect();
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火盆"));
        findViewById(R.id.card_data_index_background_images_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "刺梨烧烤盘"));
        findViewById(R.id.card_data_index_background_images_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金牛座精灵"));
        findViewById(R.id.card_data_index_background_images_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "洛基神使"));
        findViewById(R.id.card_data_index_background_images_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "洛基神使"));
        findViewById(R.id.card_data_index_background_images_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖炉汪"));
        findViewById(R.id.card_data_index_background_images_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "能量喵"));
        findViewById(R.id.card_data_index_background_images_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坩埚蛇"));

        // 增幅名单
        cardListBinding.cardCardDataIndexX11130060.cardDataIndexX11130060.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双向水管"));
        cardListBinding.cardCardDataIndexX11130074.cardDataIndexX11130074.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "天秤座精灵"));
        cardListBinding.cardCardDataIndexX11130304.cardDataIndexX11130304.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "呆呆鸡"));
        cardListBinding.cardCardDataIndexX1113000a.cardDataIndexX1113000a.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿瑞斯神使"));
        cardListBinding.cardCardDataIndexX11130104.cardDataIndexX11130104.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "二哈汪"));
        cardListBinding.cardCardDataIndexX11120260.cardDataIndexX11120260.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双枪喵"));
        cardListBinding.cardCardDataIndexX11120490.cardDataIndexX11120490.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "散弹牛"));
        cardListBinding.cardCardDataIndexX11120720.cardDataIndexX11120720.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威风虎"));
        cardListBinding.cardCardDataIndexX111300b0.cardDataIndexX111300b0.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三线酒架"));
        cardListBinding.cardCardDataIndexX111300e4.cardDataIndexX111300e4.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "射手座精灵"));
        cardListBinding.cardCardDataIndexX111303e4.cardDataIndexX111303e4.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "砰砰鸡"));
        cardListBinding.cardCardDataIndexX111300ea.cardDataIndexX111300ea.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "丘比特神使"));
        cardListBinding.cardCardDataIndexX11130114.cardDataIndexX11130114.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狩猎汪"));
        cardListBinding.cardCardDataIndexX11120130.cardDataIndexX11120130.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪猎手"));
        cardListBinding.cardCardDataIndexX11120460.cardDataIndexX11120460.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炙烤灯笼鱼"));
        cardListBinding.cardCardDataIndexX11130014.cardDataIndexX11130014.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小笼包"));
        cardListBinding.cardCardDataIndexX11130020.cardDataIndexX11130020.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层小笼包"));
        cardListBinding.cardCardDataIndexX11130100.cardDataIndexX11130100.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向小笼包"));
        cardListBinding.cardCardDataIndexX11930010.cardDataIndexX11930010.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪小笼包"));
        cardListBinding.cardCardDataIndexX11130050.cardDataIndexX11130050.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰冻小笼包"));
        cardListBinding.cardCardDataIndexX11130160.cardDataIndexX11130160.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层冰冻小笼包"));
        cardListBinding.cardCardDataIndexX11130170.cardDataIndexX11130170.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向冰冻小笼包"));
        cardListBinding.cardCardDataIndexX11930070.cardDataIndexX11930070.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪冰冻小笼包"));
        cardListBinding.cardCardDataIndexX11630010.cardDataIndexX11630010.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "国王小笼包"));
        cardListBinding.cardCardDataIndexX11630100.cardDataIndexX11630100.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向国王小笼包"));
        cardListBinding.cardCardDataIndexX11130030.cardDataIndexX11130030.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贵族小笼包"));
        cardListBinding.cardCardDataIndexX11930060.cardDataIndexX11930060.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "玉蜀黍"));
        cardListBinding.cardCardDataIndexX11122320.cardDataIndexX11122320.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "包包龙"));
        cardListBinding.cardCardDataIndexX11120190.cardDataIndexX11120190.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "枪塔喵"));
        cardListBinding.cardCardDataIndexX11120300.cardDataIndexX11120300.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弩箭牛"));
        cardListBinding.cardCardDataIndexX111304e0.cardDataIndexX111304e0.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "仙人掌刺身"));
        cardListBinding.cardCardDataIndexX11131200.cardDataIndexX11131200.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        cardListBinding.cardCardDataIndexX11120820.cardDataIndexX11120820.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        cardListBinding.cardCardDataIndexX11120690.cardDataIndexX11120690.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "耗油双菇"));
        cardListBinding.cardCardDataIndexX11131140.cardDataIndexX11131140.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奶茶猪"));
        cardListBinding.cardCardDataIndexX11132140.cardDataIndexX11132140.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "科技喵"));
        cardListBinding.cardCardDataIndexX11120880.cardDataIndexX11120880.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        cardListBinding.cardCardDataIndexX11122720.cardDataIndexX11122720.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖人马"));
        cardListBinding.cardCardDataIndexX11122600.cardDataIndexX11122600.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阴阳蛇"));
        cardListBinding.cardCardDataIndexX11122670.cardDataIndexX11122670.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焚寂马"));
        cardListBinding.cardCardDataIndexX11122740.cardDataIndexX11122740.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹珠汽水"));
        cardListBinding.cardCardDataIndexX11130090.cardDataIndexX11130090.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焦油喷壶"));
        cardListBinding.cardCardDataIndexX11130190.cardDataIndexX11130190.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷壶汪"));
        cardListBinding.cardCardDataIndexX111303a4.cardDataIndexX111303a4.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "派派鸡"));
        cardListBinding.cardCardDataIndexX11120100.cardDataIndexX11120100.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小猪米花机"));
        cardListBinding.cardCardDataIndexX11120400.cardDataIndexX11120400.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷气牛"));
        cardListBinding.cardCardDataIndexX111311a0.cardDataIndexX111311a0.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "卖萌喵"));
        cardListBinding.cardCardDataIndexX1112064a.cardDataIndexX1112064a.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奥丁神使"));
        cardListBinding.cardCardDataIndexX11122390.cardDataIndexX11122390.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "法师蛇"));
        cardListBinding.cardCardDataIndexX11122430.cardDataIndexX11122430.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "街头烤肉大师"));
        cardListBinding.cardCardDataIndexX1112244a.cardDataIndexX1112244a.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "后羿神使"));
        cardListBinding.cardCardDataIndexX11700010.cardDataIndexX11700010.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        cardListBinding.cardCardDataIndexX11700060.cardDataIndexX11700060.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "热狗耗油双菇"));
        cardListBinding.cardCardDataIndexX11700070.cardDataIndexX11700070.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "子母三线酒架"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
            if (bgEffectController != null) {
                bgEffectController.startDetailAnimalCardDataBgEffect();
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