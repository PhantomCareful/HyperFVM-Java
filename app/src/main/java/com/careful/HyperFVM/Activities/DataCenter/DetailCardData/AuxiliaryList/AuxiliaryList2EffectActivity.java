package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;

import android.os.Build;
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
import androidx.annotation.RequiresApi;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList2EffectBinding;
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

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class AuxiliaryList2EffectActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list2_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list2_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list2_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private ActivityAuxiliaryList2EffectBinding binding;
    private DBHelper dbHelper;
    private BlurUtil blurUtil;
    private BgEffectController bgEffectController;
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
        setContentView(R.layout.activity_auxiliary_list2_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList2EffectBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

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

        // 初始化流光背景
        View bgView = findViewById(R.id.bgEffectView);
        if (bgView != null) {
            bgEffectController = new BgEffectController(bgView);
            bgEffectController.setDetailAnimalCardDataColorType(this);
        }
        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0f);
    }

    /**
     * 加载所有卡片的点击事件
     */
    private void initCardImages() {
        // 增幅卡
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "能量喵"));
        findViewById(R.id.card_data_index_background_images_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪加强器"));
        findViewById(R.id.card_data_index_background_images_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蓝莓信号塔塔"));
        findViewById(R.id.card_data_index_background_images_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美味水果塔"));
        findViewById(R.id.card_data_index_background_images_5_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "欧若拉神使"));
        findViewById(R.id.card_data_index_background_images_5_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "欧若拉神使"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex211.cardDataIndex211.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "勺勺兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex212.cardDataIndex212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "窃蛋龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex213.cardDataIndex213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "尤弥尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex214.cardDataIndex214.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "幻影蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex215.cardDataIndex215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "全能糖球投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex216.cardDataIndex216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金乌马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex221.cardDataIndex221.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "煮蛋器投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex222.cardDataIndex222.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex223.cardDataIndex223.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双鱼座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex224.cardDataIndex224.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹弹鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex225.cardDataIndex225.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "索尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex226.cardDataIndex226.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机械汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex227.cardDataIndex227.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投弹猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex228.cardDataIndex228.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪糕投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex229.cardDataIndex229.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞鱼喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2210.cardDataIndex2210.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "壮壮牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2211.cardDataIndex2211.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烤蜥蜴投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2212.cardDataIndex2212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投篮虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2213.cardDataIndex2213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钵钵鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2214.cardDataIndex2214.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2215.cardDataIndex2215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2216.cardDataIndex2216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex2217.cardDataIndex2217.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "8周年蛋糕"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex231.cardDataIndex231.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生煎锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex232.cardDataIndex232.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex233.cardDataIndex233.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "祝融神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex234.cardDataIndex234.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖炒栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex235.cardDataIndex235.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1041.cardDataIndex1041.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1042.cardDataIndex1042.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖人马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1212.cardDataIndex1212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1213.cardDataIndex1213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1612.cardDataIndex1612.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪芭煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1614.cardDataIndex1614.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酱香锅烤栗子"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
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