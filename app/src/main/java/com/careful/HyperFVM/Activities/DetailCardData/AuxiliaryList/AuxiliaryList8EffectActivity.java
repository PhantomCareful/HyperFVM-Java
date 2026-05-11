package com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList8EffectBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class AuxiliaryList8EffectActivity extends BaseActivity {
    private ActivityAuxiliaryList8EffectBinding binding;
    private DBHelper dbHelper;
    private BgEffectController bgEffectController;

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
        dbHelper = new DBHelper(this);

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
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.getStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
        });

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
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
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
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex821.cardDataIndex821.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "旋转咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex822.cardDataIndex822.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狮子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex823.cardDataIndex823.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "波塞冬神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex824.cardDataIndex824.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex825.cardDataIndex825.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex826.cardDataIndex826.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex827.cardDataIndex827.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巫蛊蛇"));
        // 魔杖蛇和海洋女神追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex811.cardDataIndex811.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex812.cardDataIndex812.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex813.cardDataIndex813.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex814.cardDataIndex814.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫斯提亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex1011.cardDataIndex1011.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肥牛火锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex1012.cardDataIndex1012.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麻辣香锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex945.cardDataIndex945.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金刚马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList8).cardCardDataIndex1123.cardDataIndex1123.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹珠汽水"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }
    }
}