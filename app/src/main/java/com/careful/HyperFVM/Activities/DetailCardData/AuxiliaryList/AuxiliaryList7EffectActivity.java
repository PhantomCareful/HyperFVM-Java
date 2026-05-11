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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList7EffectBinding;
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
public class AuxiliaryList7EffectActivity extends BaseActivity {
    private ActivityAuxiliaryList7EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list7_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList7EffectBinding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "龙须面"));
        findViewById(R.id.card_data_index_background_images_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五谷丰登"));
        findViewById(R.id.card_data_index_background_images_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五行蛇"));
        findViewById(R.id.card_data_index_background_images_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弗雷神使"));
        findViewById(R.id.card_data_index_background_images_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弗雷神使"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex311.cardDataIndex311.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炭烧海星"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex312.cardDataIndex312.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex313.cardDataIndex313.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "陀螺喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex314.cardDataIndex314.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "哈迪斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex315.cardDataIndex315.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "查克拉兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex322.cardDataIndex322.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex323.cardDataIndex323.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坚果爆炒机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex324.cardDataIndex324.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "里格神使"));
        // 龙须面二转后追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex3251.cardDataIndex325.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex16131.cardDataIndex1613.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        // 丰饶神三转后追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex321.cardDataIndex321.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "厨师虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex1137.cardDataIndex1137.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大师兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex3252.cardDataIndex325.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex16132.cardDataIndex1613.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex326.cardDataIndex326.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烟花虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex327.cardDataIndex327.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "风车龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex117.cardDataIndex117.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList7).cardCardDataIndex118.cardDataIndex118.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威风虎"));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startDetailAnimalCardDataBgEffect();
        }
    }
}