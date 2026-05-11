package com.careful.HyperFVM.Activities.DetailCardData.AuxiliaryList;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList4Binding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.Objects;

public class AuxiliaryList4Activity extends BaseActivity {
    private ActivityAuxiliaryList4Binding binding;
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
        setContentView(R.layout.activity_auxiliary_list4);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList4Binding.inflate(getLayoutInflater());
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
        findViewById(R.id.card_data_index_background_images_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "香料虎"));

        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex944.cardDataIndex944.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex946.cardDataIndex946.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "归元马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex1216.cardDataIndex1216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList4).cardCardDataIndex923.cardDataIndex923.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火箭猪"));

    }
}