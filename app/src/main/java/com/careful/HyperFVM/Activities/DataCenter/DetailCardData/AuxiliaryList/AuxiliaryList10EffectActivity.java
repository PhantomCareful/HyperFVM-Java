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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList10EffectBinding;
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
public class AuxiliaryList10EffectActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list10_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list10_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list10_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private ActivityAuxiliaryList10EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list10_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList10EffectBinding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炎焱兔"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex511.cardDataIndex511.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex512.cardDataIndex512.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex513.cardDataIndex513.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酒杯灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex514.cardDataIndex514.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex515.cardDataIndex515.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex516.cardDataIndex516.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex517.cardDataIndex517.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿波罗神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex518.cardDataIndex518.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "7周年蜡烛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex519.cardDataIndex519.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex1525.cardDataIndex1525.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex1526.cardDataIndex1526.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生日帽"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex1527.cardDataIndex1527.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList10).cardCardDataIndex1528.cardDataIndex1528.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "扑克牌护罩"));

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