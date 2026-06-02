package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList9Binding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

public class AuxiliaryList9Activity extends BaseActivity {
    private ActivityAuxiliaryList9Binding binding;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_auxiliary_list9);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList9Binding.inflate(getLayoutInflater());
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
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
        });

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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炎焱兔"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex511.cardDataIndex511.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex512.cardDataIndex512.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex513.cardDataIndex513.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酒杯灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex514.cardDataIndex514.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex515.cardDataIndex515.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex516.cardDataIndex516.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex517.cardDataIndex517.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿波罗神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex518.cardDataIndex518.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "7周年蜡烛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex519.cardDataIndex519.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex1525.cardDataIndex1525.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex1526.cardDataIndex1526.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生日帽"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex1527.cardDataIndex1527.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList9).cardCardDataIndex1528.cardDataIndex1528.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "扑克牌护罩"));

    }
}