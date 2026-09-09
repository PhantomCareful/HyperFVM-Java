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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList1EffectBinding;
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
public class AuxiliaryList1EffectActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_auxiliary_list1_scroll_y";
    private static final String STATE_SCROLL_Y_PAD_1 = "state_auxiliary_list1_scroll_y_pad_1";// PAD 左栏（scrollView1 大图栏）滚动位置保存键：PAD 不接入联动，但 ScrollView 不自存滚动状态，需重建后恢复
    private static final String STATE_SCROLL_Y_PAD_2 = "state_auxiliary_list1_scroll_y_pad_2";// PAD 右栏（scrollView2 名单栏）滚动位置保存键：同上
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private ActivityAuxiliaryList1EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list1_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList1EffectBinding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火盆"));
        findViewById(R.id.card_data_index_background_images_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "刺梨烧烤盘"));
        findViewById(R.id.card_data_index_background_images_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金牛座精灵"));
        findViewById(R.id.card_data_index_background_images_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "洛基神使"));
        findViewById(R.id.card_data_index_background_images_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "洛基神使"));
        findViewById(R.id.card_data_index_background_images_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖炉汪"));
        findViewById(R.id.card_data_index_background_images_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "能量喵"));
        findViewById(R.id.card_data_index_background_images_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坩埚蛇"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex111.cardDataIndex111.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双向水管"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex112.cardDataIndex112.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "天秤座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex113.cardDataIndex113.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "呆呆鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex114.cardDataIndex114.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿瑞斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex115.cardDataIndex115.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "二哈汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex116.cardDataIndex116.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双枪喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex117.cardDataIndex117.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex118.cardDataIndex118.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威风虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex121.cardDataIndex121.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三线酒架"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex122.cardDataIndex122.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "射手座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex123.cardDataIndex123.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "砰砰鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex124.cardDataIndex124.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "丘比特神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex125.cardDataIndex125.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狩猎汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex126.cardDataIndex126.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪猎手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex127.cardDataIndex127.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炙烤灯笼鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex131.cardDataIndex131.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex132.cardDataIndex132.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex133.cardDataIndex133.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex134.cardDataIndex134.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex135.cardDataIndex135.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex136.cardDataIndex136.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex137.cardDataIndex137.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex138.cardDataIndex138.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex139.cardDataIndex139.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "国王小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1310.cardDataIndex1310.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向国王小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1311.cardDataIndex1311.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贵族小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1312.cardDataIndex1312.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "玉蜀黍"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1313.cardDataIndex1313.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "包包龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex141.cardDataIndex141.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "枪塔喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex142.cardDataIndex142.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弩箭牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex143.cardDataIndex143.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "仙人掌刺身"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex312.cardDataIndex312.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex322.cardDataIndex322.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex728.cardDataIndex728.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "耗油双菇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex729.cardDataIndex729.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奶茶猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex7210.cardDataIndex7210.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "科技喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1041.cardDataIndex1041.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1042.cardDataIndex1042.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖人马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1121.cardDataIndex1121.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阴阳蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1122.cardDataIndex1122.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焚寂马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1123.cardDataIndex1123.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹珠汽水"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1141.cardDataIndex1141.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焦油喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1142.cardDataIndex1142.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷壶汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1143.cardDataIndex1143.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "派派鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1144.cardDataIndex1144.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小猪米花机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1145.cardDataIndex1145.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1146.cardDataIndex1146.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "卖萌喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1147.cardDataIndex1147.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奥丁神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1148.cardDataIndex1148.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "法师蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1149.cardDataIndex1149.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "街头烤肉大师"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex11410.cardDataIndex11410.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "后羿神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1613.cardDataIndex1613.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1615.cardDataIndex1615.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "热狗耗油双菇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1616.cardDataIndex1616.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "子母三线酒架"));
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