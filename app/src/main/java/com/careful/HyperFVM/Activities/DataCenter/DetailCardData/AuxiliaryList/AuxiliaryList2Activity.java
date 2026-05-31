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
import com.careful.HyperFVM.databinding.ActivityAuxiliaryList2Binding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

public class AuxiliaryList2Activity extends BaseActivity {
    private ActivityAuxiliaryList2Binding binding;
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
        setContentView(R.layout.activity_auxiliary_list2);

        // 初始化ViewBinding
        binding = ActivityAuxiliaryList2Binding.inflate(getLayoutInflater());
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
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex231.cardDataIndex231.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex232.cardDataIndex232.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex233.cardDataIndex233.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex234.cardDataIndex234.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "8周年蛋糕"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1013.cardDataIndex1013.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生煎锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1014.cardDataIndex1014.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1015.cardDataIndex1015.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "祝融神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1016.cardDataIndex1016.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖炒栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1017.cardDataIndex1017.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1041.cardDataIndex1041.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1042.cardDataIndex1042.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖人马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1215.cardDataIndex1215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1216.cardDataIndex1216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1612.cardDataIndex1612.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪芭煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryList2).cardCardDataIndex1614.cardDataIndex1614.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酱香锅烤栗子"));

    }
}