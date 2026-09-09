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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList8EffectBinding;
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
public class AuxiliaryList8EffectActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list8_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list8_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list8_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private ActivityAuxiliaryList8EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list8_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList8EffectBinding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "加速榨汁机"));
        findViewById(R.id.card_data_index_background_images_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔杖蛇"));
        findViewById(R.id.card_data_index_background_images_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔拉萨神使"));
        findViewById(R.id.card_data_index_background_images_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔拉萨神使"));

        // 增幅名单
        // N×N范围增幅
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex821.cardDataIndex821.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "旋转咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex822.cardDataIndex822.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狮子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex823.cardDataIndex823.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "波塞冬神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex824.cardDataIndex824.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex825.cardDataIndex825.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex826.cardDataIndex826.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex827.cardDataIndex827.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巫蛊蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex942.cardDataIndex942.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "红柳烤串机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex941.cardDataIndex941.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金刚马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex943.cardDataIndex943.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "俱吠罗神使"));

        // 附加类
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex1123.cardDataIndex1123.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹珠汽水"));

        // 本行增幅
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex811.cardDataIndex811.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex812.cardDataIndex812.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex813.cardDataIndex813.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex814.cardDataIndex814.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫斯提亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex815.cardDataIndex815.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肥牛火锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex816.cardDataIndex816.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麻辣香锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex1136.cardDataIndex1136.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "云霞马"));

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