package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityCardDataAuxiliaryListBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class CardDataAuxiliaryListActivity extends BaseActivity {

    private DBHelper dbHelper;
    private ActivityCardDataAuxiliaryListBinding binding;
    private SpringBackScrollView CardDataAuxiliaryListContainer;

    private int pressFeedbackAnimationDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        // 初始化ViewBinding
        binding = ActivityCardDataAuxiliaryListBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        // 添加模糊材质
        setupBlurEffect();

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 目录按钮
        CardDataAuxiliaryListContainer = findViewById(R.id.CardDataAuxiliaryList_Container);
        findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container).setOnClickListener(v ->
                v.postDelayed(this::showTitleNavigationDialog, pressFeedbackAnimationDelay));

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }}, 50);
    }

    /**
     * 弹出标题导航弹窗
     * 这个弹窗和当前Activity联系非常紧密，为了方便起见，不归到DialogBuilderManager中去
     */
    private void showTitleNavigationDialog() {
        // 获取标题数组
        String[] titleEntries = getResources().getStringArray(R.array.card_data_auxiliary_list_titles);

        // 加载自定义布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_dialog_selection, null);
        ListView listView = dialogView.findViewById(R.id.dialog_list);
        dialogView.findViewById(R.id.dialog_list_top_gradient).setVisibility(View.GONE);
        dialogView.findViewById(R.id.dialog_list_bottom_gradient).setVisibility(View.GONE);

        // 设置列表
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_index_selection, titleEntries);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // 构建目录列表弹窗
        Dialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setTitle("🛰增幅卡导航") // 弹窗标题
                .setView(dialogView) // 弹窗主题
                .setNegativeButton("关闭", null) // 取消按钮
                .create();

        // 列表点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 点击列表项时：滚动到对应标题位置
            if (position >= 0 && CardDataAuxiliaryListContainer != null) {
                // 根据索引获取对应标题View的ID
                int targetViewId = getTitleViewIdByIndex(position);
                View targetView = findViewById(targetViewId);
                if (targetView != null) {
                    // 计算滚动位置（减去顶部100dp的padding，让标题显示更友好）
                    int scrollTop = targetView.getTop() - 400;
                    // 目标滚动位置（保留你原有的顶部间距、边界保护逻辑）
                    int targetScrollY = Math.max(scrollTop, 0);
                    // 当前滚动位置
                    int currentScrollY = CardDataAuxiliaryListContainer.getScrollY();
                    // 初始化值动画：实现从当前位置 → 目标位置的渐变滚动
                    ValueAnimator scrollAnimator = ValueAnimator.ofInt(currentScrollY, targetScrollY);
                    // 滚动时长（核心：控制顺滑度，300-500ms是安卓舒适区间，值越大越慢越丝滑）
                    scrollAnimator.setDuration(500);
                    // 核心插值器（决定滚动的速度变化规律，这是平滑的关键！）
                    // DecelerateInterpolator：减速插值器 → 滚动由快到慢，符合人眼视觉习惯，最推荐
                    scrollAnimator.setInterpolator(new DecelerateInterpolator(1.0f));
                    // 逐帧更新滚动位置
                    scrollAnimator.addUpdateListener(animation -> {
                        int animatedValue = (int) animation.getAnimatedValue();
                        CardDataAuxiliaryListContainer.scrollTo(0, animatedValue);
                    });
                    // 启动动画（加入防重复点击：先取消之前的滚动动画，再启动新的）
                    scrollAnimator.cancel();
                    scrollAnimator.start();
                }
            }
            dialog.dismiss(); // 选择后关闭弹窗
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 映射列表索引到标题View的ID（需和字符串数组顺序完全一致）
     */
    private int getTitleViewIdByIndex(int index) {
        return switch (index) {
            case 0 -> R.id.title_card_data_auxiliary_list_1;
            case 1 -> R.id.title_card_data_auxiliary_list_2;
            case 2 -> R.id.title_card_data_auxiliary_list_3;
            case 3 -> R.id.title_card_data_auxiliary_list_4;
            case 4 -> R.id.title_card_data_auxiliary_list_5;
            case 5 -> R.id.title_card_data_auxiliary_list_6;
            case 6 -> R.id.title_card_data_auxiliary_list_7;
            case 7 -> R.id.title_card_data_auxiliary_list_8;
            default -> -1;
        };
    }

    private void initCardImages() {
        // 1. 投手增幅卡
        findViewById(R.id.card_data_index_4_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "能量喵"));
        findViewById(R.id.card_data_index_4_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪加强器"));
        findViewById(R.id.card_data_index_4_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蓝莓信号塔塔"));
        findViewById(R.id.card_data_index_4_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美味水果塔"));
        findViewById(R.id.card_data_index_4_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "欧若拉神使"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex211.cardDataIndex211.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "勺勺兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex212.cardDataIndex212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "窃蛋龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex213.cardDataIndex213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "尤弥尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex214.cardDataIndex214.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "幻影蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex215.cardDataIndex215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "全能糖球投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex216.cardDataIndex216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金乌马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex221.cardDataIndex221.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "煮蛋器投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex222.cardDataIndex222.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex223.cardDataIndex223.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双鱼座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex224.cardDataIndex224.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹弹鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex225.cardDataIndex225.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "索尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex226.cardDataIndex226.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机械汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex227.cardDataIndex227.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投弹猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex228.cardDataIndex228.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪糕投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex229.cardDataIndex229.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞鱼喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2210.cardDataIndex2210.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "壮壮牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2211.cardDataIndex2211.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烤蜥蜴投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2212.cardDataIndex2212.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投篮虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2213.cardDataIndex2213.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钵钵鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex231.cardDataIndex231.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex232.cardDataIndex232.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex233.cardDataIndex233.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex234.cardDataIndex234.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "8周年蛋糕"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1013.cardDataIndex1013.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生煎锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1014.cardDataIndex1014.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1015.cardDataIndex1015.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "祝融神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1016.cardDataIndex1016.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖炒栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1614.cardDataIndex1614.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酱香锅烤栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1017.cardDataIndex1017.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1041.cardDataIndex1041.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1215.cardDataIndex1215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1216.cardDataIndex1216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));

        // 2. 莓果点心
        findViewById(R.id.card_data_index_4_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "莓果点心"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex944.cardDataIndex944.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex936.cardDataIndex936.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));

        // 3. 香料虎
        findViewById(R.id.card_data_index_4_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "香料虎"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex944.cardDataIndex944.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex1216.cardDataIndex1216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex923.cardDataIndex923.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火箭猪"));
        
        // 4. 塔利亚神使
        findViewById(R.id.card_data_index_4_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔利亚神使"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex911.cardDataIndex911.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex912.cardDataIndex912.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex913.cardDataIndex913.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex914.cardDataIndex914.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex915.cardDataIndex915.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex916.cardDataIndex916.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex917.cardDataIndex917.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex944.cardDataIndex944.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1216.cardDataIndex1216.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex923.cardDataIndex923.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1215.cardDataIndex1215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1135.cardDataIndex1135.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "御风马"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1018.cardDataIndex1018.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "马卡龙烤箱"));
        // 四转追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex922.cardDataIndex922.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雅典娜守护"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex924.cardDataIndex924.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "宙斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex931.cardDataIndex931.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex932.cardDataIndex932.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex933.cardDataIndex933.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex934.cardDataIndex934.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "典伊神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex935.cardDataIndex935.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex936.cardDataIndex936.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex714Auxiliary.cardDataIndex714Auxiliary.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "至尊大力神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex727.cardDataIndex727.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "埃罗斯神使"));

        // 5. 精灵龙
        findViewById(R.id.card_data_index_4_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "精灵龙"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex922.cardDataIndex922.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雅典娜守护"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex924.cardDataIndex924.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "宙斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex931.cardDataIndex931.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex932.cardDataIndex932.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex933.cardDataIndex933.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex934.cardDataIndex934.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "典伊神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex935.cardDataIndex935.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex936.cardDataIndex936.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex921.cardDataIndex921.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖喱龙虾炮"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex1214.cardDataIndex1215.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex1018.cardDataIndex1018.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "马卡龙烤箱"));

        // 6. 五向增幅卡
        findViewById(R.id.card_data_index_4_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "龙须面"));
        findViewById(R.id.card_data_index_4_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五谷丰登"));
        findViewById(R.id.card_data_index_4_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五行蛇"));
        findViewById(R.id.card_data_index_4_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弗雷神使"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex311.cardDataIndex311.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炭烧海星"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex312.cardDataIndex312.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex313.cardDataIndex313.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "陀螺喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex314.cardDataIndex314.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "哈迪斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex315.cardDataIndex315.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "查克拉兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex322.cardDataIndex322.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex323.cardDataIndex323.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坚果爆炒机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex324.cardDataIndex324.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "里格神使"));
        // 龙须面二转后追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex3251.cardDataIndex325.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex16131.cardDataIndex1613.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        // 丰饶神三转后追加
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex321.cardDataIndex321.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "厨师虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1137.cardDataIndex1137.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大师兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex3252.cardDataIndex325.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex16132.cardDataIndex1613.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex326.cardDataIndex326.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烟花虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex327.cardDataIndex327.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "风车龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex117.cardDataIndex117.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex118.cardDataIndex118.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威风虎"));

        // 7. 喷壶增幅卡
        findViewById(R.id.card_data_index_4_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "加速榨汁机"));
        findViewById(R.id.card_data_index_4_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔杖蛇"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex821.cardDataIndex821.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "旋转咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex822.cardDataIndex822.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狮子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex823.cardDataIndex823.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "波塞冬神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex824.cardDataIndex824.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex825.cardDataIndex825.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex826.cardDataIndex826.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex827.cardDataIndex827.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巫蛊蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex811.cardDataIndex811.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex812.cardDataIndex812.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex813.cardDataIndex813.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex814.cardDataIndex814.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫斯提亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1011.cardDataIndex1011.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肥牛火锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1012.cardDataIndex1012.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麻辣香锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex945.cardDataIndex945.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金刚马"));

        // 8.炎焱兔
        findViewById(R.id.card_data_index_4_2_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炎焱兔"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex511.cardDataIndex511.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex512.cardDataIndex512.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex513.cardDataIndex513.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酒杯灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex514.cardDataIndex514.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex515.cardDataIndex515.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex516.cardDataIndex516.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex517.cardDataIndex517.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿波罗神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex518.cardDataIndex518.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "7周年蜡烛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex519.cardDataIndex519.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1525.cardDataIndex1525.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1526.cardDataIndex1526.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生日帽"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1527.cardDataIndex1527.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1528.cardDataIndex1528.setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "扑克牌护罩"));

    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonIndex));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便添加一个位移动画
        MaterialCardView cardView = findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                cardView,
                View.TRANSLATION_X,
                550f, 0f // 从1000px移动到0px
        );
        animator.setDuration(1200);
        animator.start();

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
        // 添加按压动画
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }
}