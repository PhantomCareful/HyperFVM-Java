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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList3EffectBinding;
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
public class AuxiliaryList3EffectActivity extends BaseActivity {
    private ActivityAuxiliaryList3EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list3_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList3EffectBinding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "莓果点心"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex944.cardDataIndex944.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex946.cardDataIndex946.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "归元马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList3).cardCardDataIndex936.cardDataIndex936.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));

    }
    
    @Override
    protected void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }
    }
}