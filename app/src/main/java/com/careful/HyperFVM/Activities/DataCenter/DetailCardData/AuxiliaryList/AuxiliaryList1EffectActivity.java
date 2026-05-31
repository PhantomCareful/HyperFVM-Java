package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.AuxiliaryList;

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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList1EffectBinding;
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
public class AuxiliaryList1EffectActivity extends BaseActivity {
    private ActivityAuxiliaryList1EffectBinding binding;
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
        setContentView(R.layout.activity_auxiliary_list1_effect);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList1EffectBinding.inflate(getLayoutInflater());
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
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex131.cardDataIndex131.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "枪塔喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex132.cardDataIndex132.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弩箭牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex133.cardDataIndex133.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "仙人掌刺身"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex312.cardDataIndex312.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex322.cardDataIndex322.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex728.cardDataIndex728.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "耗油双菇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex729.cardDataIndex729.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奶茶猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex7210.cardDataIndex7210.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "科技喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1041.cardDataIndex1041.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1042.cardDataIndex1042.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖人马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1111.cardDataIndex1111.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1112.cardDataIndex1112.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1113.cardDataIndex1113.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1114.cardDataIndex1114.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1115.cardDataIndex1115.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1116.cardDataIndex1116.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1117.cardDataIndex1117.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1118.cardDataIndex1118.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪冰冻小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex1119.cardDataIndex1119.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "国王小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex11110.cardDataIndex11110.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向国王小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex11111.cardDataIndex11111.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贵族小笼包"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex11112.cardDataIndex11112.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "玉蜀黍"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList1).cardCardDataIndex11113.cardDataIndex11113.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "包包龙"));
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
}