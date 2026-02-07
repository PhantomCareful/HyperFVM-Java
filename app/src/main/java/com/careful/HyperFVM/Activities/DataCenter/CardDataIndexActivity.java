package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;
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
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CardDataIndexActivity extends BaseActivity {
    private DBHelper dbHelper;
    private SpringBackScrollView CardDataIndexContainer;

    private int pressFeedbackAnimationDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        //小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_card_data_index);

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 添加模糊材质
        setupBlurEffect();

        // 防御卡目录按钮
        CardDataIndexContainer = findViewById(R.id.CardDataIndex_Container);
        findViewById(R.id.FloatButton_CardDataIndex_Container).setOnClickListener(v ->
                v.postDelayed(this::showTitleNavigationDialog, pressFeedbackAnimationDelay));

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnClickListener(v ->v.postDelayed(() ->
                DialogBuilderManager.showCardQueryDialog(this), pressFeedbackAnimationDelay));

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }}, 50);
    }

    /**
     * 弹出标题导航弹窗
     * 这个弹窗和当前Activity联系非常紧密，为了方便起见，不归到DialogBuilderManager中去
     */
    private void showTitleNavigationDialog() {
        // 获取标题数组
        String[] titleEntries = getResources().getStringArray(R.array.card_data_index_titles);

        // 加载自定义布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_dialog_selection, null);
        ListView listView = dialogView.findViewById(R.id.dialog_list);

        // 设置列表
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_index_selection, titleEntries);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // 构建目录列表弹窗
        Dialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setTitle("🛰卡片类别导航") // 弹窗标题
                .setView(dialogView) // 弹窗主题
                .setNegativeButton("关闭", null) // 取消按钮
                .create();

        // 列表点击事件
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 点击列表项时：滚动到对应标题位置
            if (position >= 0 && CardDataIndexContainer != null) {
                // 根据索引获取对应标题View的ID
                int targetViewId = getTitleViewIdByIndex(position);
                View targetView = findViewById(targetViewId);
                if (targetView != null) {
                    // 计算滚动位置（减去顶部100dp的padding，让标题显示更友好）
                    int scrollTop = targetView.getTop() - 400;
                    // 目标滚动位置（保留你原有的顶部间距、边界保护逻辑）
                    int targetScrollY = Math.max(scrollTop, 0);
                    // 当前滚动位置
                    int currentScrollY = CardDataIndexContainer.getScrollY();
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
                        CardDataIndexContainer.scrollTo(0, animatedValue);
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
            case 0 -> R.id.title_card_data_index_1_1;
            case 1 -> R.id.title_card_data_index_1_2;
            case 2 -> R.id.title_card_data_index_1_3;
            case 3 -> R.id.title_card_data_index_2_1;
            case 4 -> R.id.title_card_data_index_2_2;
            case 5 -> R.id.title_card_data_index_2_3;
            case 6 -> R.id.title_card_data_index_3_1;
            case 7 -> R.id.title_card_data_index_3_2;
            case 8 -> R.id.title_card_data_index_3_3;
            case 9 -> R.id.title_card_data_index_4_1;
            case 10 -> R.id.title_card_data_index_4_2;
            case 11 -> R.id.title_card_data_index_4_3;
            case 12 -> R.id.title_card_data_index_5_1;
            case 13 -> R.id.title_card_data_index_5_2;
            case 14 -> R.id.title_card_data_index_6_1;
            case 15 -> R.id.title_card_data_index_6_2;
            case 16 -> R.id.title_card_data_index_7_1;
            case 17 -> R.id.title_card_data_index_7_2;
            case 18 -> R.id.title_card_data_index_8_1;
            case 19 -> R.id.title_card_data_index_8_2;
            case 20 -> R.id.title_card_data_index_9_1;
            case 21 -> R.id.title_card_data_index_9_2;
            case 22 -> R.id.title_card_data_index_9_3;
            case 23 -> R.id.title_card_data_index_9_4;
            case 24 -> R.id.title_card_data_index_10_1;
            case 25 -> R.id.title_card_data_index_10_2;
            case 26 -> R.id.title_card_data_index_10_3;
            case 27 -> R.id.title_card_data_index_10_4;
            case 28 -> R.id.title_card_data_index_11_1;
            case 29 -> R.id.title_card_data_index_11_3;
            case 30 -> R.id.title_card_data_index_11_4;
            case 31 -> R.id.title_card_data_index_12_1;
            case 32 -> R.id.title_card_data_index_12_2;
            case 33 -> R.id.title_card_data_index_12_3;
            case 34 -> R.id.title_card_data_index_13_1;
            case 35 -> R.id.title_card_data_index_13_2;
            case 36 -> R.id.title_card_data_index_13_3;
            case 37 -> R.id.title_card_data_index_13_4;
            case 38 -> R.id.title_card_data_index_14_1;
            case 39 -> R.id.title_card_data_index_14_2;
            case 40 -> R.id.title_card_data_index_14_3;
            case 41 -> R.id.title_card_data_index_14_4;
            case 42 -> R.id.title_card_data_index_15_1;
            case 43 -> R.id.title_card_data_index_15_2;
            case 44 -> R.id.title_card_data_index_16_1;
            default -> -1;
        };
    }

    /**
     * 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
     */
    private void initCardImages() {
        findViewById(R.id.card_data_index_1_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双向水管"));
        findViewById(R.id.card_data_index_1_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "天秤座精灵"));
        findViewById(R.id.card_data_index_1_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "呆呆鸡"));
        findViewById(R.id.card_data_index_1_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿瑞斯神使"));
        findViewById(R.id.card_data_index_1_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "二哈汪"));
        findViewById(R.id.card_data_index_1_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双枪喵"));
        findViewById(R.id.card_data_index_1_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "散弹牛"));
        findViewById(R.id.card_data_index_1_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威风虎"));
        findViewById(R.id.card_data_index_1_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三线酒架"));
        findViewById(R.id.card_data_index_1_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "射手座精灵"));
        findViewById(R.id.card_data_index_1_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "砰砰鸡"));
        findViewById(R.id.card_data_index_1_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "丘比特神使"));
        findViewById(R.id.card_data_index_1_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狩猎汪"));
        findViewById(R.id.card_data_index_1_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪猎手"));
        findViewById(R.id.card_data_index_1_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炙烤灯笼鱼"));
        findViewById(R.id.card_data_index_1_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "枪塔喵"));
        findViewById(R.id.card_data_index_1_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弩箭牛"));
        findViewById(R.id.card_data_index_1_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "仙人掌刺身"));
        findViewById(R.id.card_data_index_2_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "勺勺兔"));
        findViewById(R.id.card_data_index_2_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "窃蛋龙"));
        findViewById(R.id.card_data_index_2_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "尤弥尔神使"));
        findViewById(R.id.card_data_index_2_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "幻影蛇"));
        findViewById(R.id.card_data_index_2_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "全能糖球投手"));
        findViewById(R.id.card_data_index_2_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金乌马"));
        findViewById(R.id.card_data_index_2_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "煮蛋器投手"));
        findViewById(R.id.card_data_index_2_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰煮蛋器"));
        findViewById(R.id.card_data_index_2_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双鱼座精灵"));
        findViewById(R.id.card_data_index_2_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹弹鸡"));
        findViewById(R.id.card_data_index_2_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "索尔神使"));
        findViewById(R.id.card_data_index_2_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机械汪"));
        findViewById(R.id.card_data_index_2_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投弹猪"));
        findViewById(R.id.card_data_index_2_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪糕投手"));
        findViewById(R.id.card_data_index_2_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞鱼喵"));
        findViewById(R.id.card_data_index_2_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "壮壮牛"));
        findViewById(R.id.card_data_index_2_2_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烤蜥蜴投手"));
        findViewById(R.id.card_data_index_2_2_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "投篮虎"));
        findViewById(R.id.card_data_index_2_2_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钵钵鸡"));
        findViewById(R.id.card_data_index_2_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "色拉投手"));
        findViewById(R.id.card_data_index_2_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力投手"));
        findViewById(R.id.card_data_index_2_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "臭豆腐投手"));
        findViewById(R.id.card_data_index_2_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "8周年蛋糕"));
        findViewById(R.id.card_data_index_3_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炭烧海星"));
        findViewById(R.id.card_data_index_3_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪料理机"));
        findViewById(R.id.card_data_index_3_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "陀螺喵"));
        findViewById(R.id.card_data_index_3_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "哈迪斯神使"));
        findViewById(R.id.card_data_index_3_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "查克拉兔"));
        findViewById(R.id.card_data_index_3_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "厨师虎"));
        findViewById(R.id.card_data_index_3_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星星兔"));
        findViewById(R.id.card_data_index_3_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坚果爆炒机"));
        findViewById(R.id.card_data_index_3_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "里格神使"));
        findViewById(R.id.card_data_index_3_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "怪味鱿鱼"));
        findViewById(R.id.card_data_index_3_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烟花虎"));
        findViewById(R.id.card_data_index_3_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "风车龙"));
        findViewById(R.id.card_data_index_3_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "鲈鱼"));
        findViewById(R.id.card_data_index_3_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "便便汪"));
        findViewById(R.id.card_data_index_3_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烧鸡"));
        findViewById(R.id.card_data_index_3_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "饼干汪"));
        findViewById(R.id.card_data_index_3_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "牛角面包"));
        findViewById(R.id.card_data_index_3_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盾盾汪"));
        findViewById(R.id.card_data_index_4_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火盆"));
        findViewById(R.id.card_data_index_4_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金牛座精灵"));
        findViewById(R.id.card_data_index_4_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "洛基神使"));
        findViewById(R.id.card_data_index_4_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖炉汪"));
        findViewById(R.id.card_data_index_4_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "能量喵"));
        findViewById(R.id.card_data_index_4_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "坩埚蛇"));
        findViewById(R.id.card_data_index_4_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猪猪加强器"));
        findViewById(R.id.card_data_index_4_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蓝莓信号塔塔"));
        findViewById(R.id.card_data_index_4_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美味水果塔"));
        findViewById(R.id.card_data_index_4_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "欧若拉神使"));
        findViewById(R.id.card_data_index_4_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "莓果点心"));
        findViewById(R.id.card_data_index_4_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "香料虎"));
        findViewById(R.id.card_data_index_4_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "塔利亚神使"));
        findViewById(R.id.card_data_index_4_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "精灵龙"));
        findViewById(R.id.card_data_index_4_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "龙须面"));
        findViewById(R.id.card_data_index_4_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五谷丰登"));
        findViewById(R.id.card_data_index_4_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "五行蛇"));
        findViewById(R.id.card_data_index_4_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弗雷神使"));
        findViewById(R.id.card_data_index_4_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "加速榨汁机"));
        findViewById(R.id.card_data_index_4_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炎焱兔"));
        findViewById(R.id.card_data_index_4_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "11周年美食盒子"));
        findViewById(R.id.card_data_index_4_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "战旗马"));
        findViewById(R.id.card_data_index_5_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小火炉"));
        findViewById(R.id.card_data_index_5_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大火炉"));
        findViewById(R.id.card_data_index_5_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酒杯灯"));
        findViewById(R.id.card_data_index_5_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双子座精灵"));
        findViewById(R.id.card_data_index_5_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咕咕鸡"));
        findViewById(R.id.card_data_index_5_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "暖暖鸡"));
        findViewById(R.id.card_data_index_5_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阿波罗神使"));
        findViewById(R.id.card_data_index_5_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "7周年蜡烛"));
        findViewById(R.id.card_data_index_5_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火焰牛"));
        findViewById(R.id.card_data_index_5_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "花火龙"));
        findViewById(R.id.card_data_index_5_1_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蛇羹煲"));
        findViewById(R.id.card_data_index_5_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钱罐猪"));
        findViewById(R.id.card_data_index_5_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "罐罐牛"));
        findViewById(R.id.card_data_index_5_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烈火虎"));
        findViewById(R.id.card_data_index_6_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "樱桃反弹布丁"));
        findViewById(R.id.card_data_index_6_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "艾草粑粑"));
        findViewById(R.id.card_data_index_6_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "布丁汪"));
        findViewById(R.id.card_data_index_6_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "凉粉牛"));
        findViewById(R.id.card_data_index_6_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忒提丝神使"));
        findViewById(R.id.card_data_index_6_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "木盘子"));
        findViewById(R.id.card_data_index_6_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盘盘鸡"));
        findViewById(R.id.card_data_index_6_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猫猫盘"));
        findViewById(R.id.card_data_index_6_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔法软糖"));
        findViewById(R.id.card_data_index_6_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "棉花糖"));
        findViewById(R.id.card_data_index_6_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "苏打气泡"));
        findViewById(R.id.card_data_index_6_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麦芽糖"));
        findViewById(R.id.card_data_index_7_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖葫芦炮弹"));
        findViewById(R.id.card_data_index_7_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "跳跳鸡"));
        findViewById(R.id.card_data_index_7_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "防空喵"));
        findViewById(R.id.card_data_index_7_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫丘利神使"));
        findViewById(R.id.card_data_index_7_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "香肠"));
        findViewById(R.id.card_data_index_7_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "热狗大炮"));
        findViewById(R.id.card_data_index_7_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "弹簧虎"));
        findViewById(R.id.card_data_index_7_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "泡泡龙"));
        findViewById(R.id.card_data_index_7_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爱心便当"));
        findViewById(R.id.card_data_index_7_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "梦幻多拿滋"));
        findViewById(R.id.card_data_index_7_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "埃罗斯神使"));
        findViewById(R.id.card_data_index_7_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "耗油双菇"));
        findViewById(R.id.card_data_index_7_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奶茶猪"));
        findViewById(R.id.card_data_index_7_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "科技喵"));
        findViewById(R.id.card_data_index_8_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡喷壶"));
        findViewById(R.id.card_data_index_8_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "关东煮喷锅"));
        findViewById(R.id.card_data_index_8_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烈焰龙"));
        findViewById(R.id.card_data_index_8_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫斯提亚神使"));
        findViewById(R.id.card_data_index_8_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "旋转咖啡喷壶"));
        findViewById(R.id.card_data_index_8_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狮子座精灵"));
        findViewById(R.id.card_data_index_8_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "波塞冬神使"));
        findViewById(R.id.card_data_index_8_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "转转鸡"));
        findViewById(R.id.card_data_index_8_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "可乐汪"));
        findViewById(R.id.card_data_index_8_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "元气牛"));
        findViewById(R.id.card_data_index_8_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巫蛊蛇"));
        findViewById(R.id.card_data_index_9_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "章鱼烧"));
        findViewById(R.id.card_data_index_9_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巨蟹座精灵"));
        findViewById(R.id.card_data_index_9_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "忍忍鸡"));
        findViewById(R.id.card_data_index_9_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "狄安娜神使"));
        findViewById(R.id.card_data_index_9_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "飞盘汪"));
        findViewById(R.id.card_data_index_9_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铁甲飞镖猪"));
        findViewById(R.id.card_data_index_9_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盗兔"));
        findViewById(R.id.card_data_index_9_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖喱龙虾炮"));
        findViewById(R.id.card_data_index_9_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雅典娜守护"));
        findViewById(R.id.card_data_index_9_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火箭猪"));
        findViewById(R.id.card_data_index_9_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "宙斯神使"));
        findViewById(R.id.card_data_index_9_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "魔法猪"));
        findViewById(R.id.card_data_index_9_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "招财喵"));
        findViewById(R.id.card_data_index_9_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪球兔"));
        findViewById(R.id.card_data_index_9_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "典伊神使"));
        findViewById(R.id.card_data_index_9_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰晶龙"));
        findViewById(R.id.card_data_index_9_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰块冷萃机"));
        findViewById(R.id.card_data_index_9_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "鼠鼠蛋糕空投器"));
        findViewById(R.id.card_data_index_9_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "风力空投猪"));
        findViewById(R.id.card_data_index_9_4_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "电流虎"));
        findViewById(R.id.card_data_index_9_4_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霹雳马"));
        findViewById(R.id.card_data_index_9_4_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金刚马"));
        findViewById(R.id.card_data_index_10_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肥牛火锅"));
        findViewById(R.id.card_data_index_10_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麻辣香锅"));
        findViewById(R.id.card_data_index_10_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生煎锅"));
        findViewById(R.id.card_data_index_10_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "铛铛虎"));
        findViewById(R.id.card_data_index_10_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "祝融神使"));
        findViewById(R.id.card_data_index_10_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖炒栗子"));
        findViewById(R.id.card_data_index_10_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "霜霜蛇"));
        findViewById(R.id.card_data_index_10_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "马卡龙烤箱"));
        findViewById(R.id.card_data_index_10_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "汉堡包"));
        findViewById(R.id.card_data_index_10_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贪食蛙"));
        findViewById(R.id.card_data_index_10_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "吞噬龙"));
        findViewById(R.id.card_data_index_10_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "香辣年糕蟹"));
        findViewById(R.id.card_data_index_10_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "混沌神使"));
        findViewById(R.id.card_data_index_10_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "新疆炒面"));
        findViewById(R.id.card_data_index_10_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "丸子厨师"));
        findViewById(R.id.card_data_index_10_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "功夫汪"));
        findViewById(R.id.card_data_index_10_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "鱼刺"));
        findViewById(R.id.card_data_index_10_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钢鱼刺"));
        findViewById(R.id.card_data_index_10_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖渍刺梨"));
        findViewById(R.id.card_data_index_10_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蜂蜜史莱姆"));
        findViewById(R.id.card_data_index_11_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小笼包"));
        findViewById(R.id.card_data_index_11_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层小笼包"));
        findViewById(R.id.card_data_index_11_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向小笼包"));
        findViewById(R.id.card_data_index_11_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪小笼包"));
        findViewById(R.id.card_data_index_11_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双层冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "机枪冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "国王小笼包"));
        findViewById(R.id.card_data_index_11_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三向国王小笼包"));
        findViewById(R.id.card_data_index_11_1_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贵族小笼包"));
        findViewById(R.id.card_data_index_11_1_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "玉蜀黍"));
        findViewById(R.id.card_data_index_11_1_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "包包龙"));
        findViewById(R.id.card_data_index_11_1_14).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡杯"));
        findViewById(R.id.card_data_index_11_1_15).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "水上茶杯"));
        findViewById(R.id.card_data_index_11_1_16).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "激光汪"));
        findViewById(R.id.card_data_index_11_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "阴阳蛇"));
        findViewById(R.id.card_data_index_11_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焚寂马"));
        findViewById(R.id.card_data_index_11_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "天蝎座精灵"));
        findViewById(R.id.card_data_index_11_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "工程猪"));
        findViewById(R.id.card_data_index_11_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "双刃蛇"));
        findViewById(R.id.card_data_index_11_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "元素蛇"));
        findViewById(R.id.card_data_index_11_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "御风马"));
        findViewById(R.id.card_data_index_11_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "回旋虎"));
        findViewById(R.id.card_data_index_11_3_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大师兔"));
        findViewById(R.id.card_data_index_11_3_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "15周年猴赛雷"));
        findViewById(R.id.card_data_index_11_3_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赖皮蛇"));
        findViewById(R.id.card_data_index_11_3_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "迷你披萨炉"));
        findViewById(R.id.card_data_index_11_3_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "鲁班神使"));
        findViewById(R.id.card_data_index_11_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "焦油喷壶"));
        findViewById(R.id.card_data_index_11_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷壶汪"));
        findViewById(R.id.card_data_index_11_4_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "派派鸡"));
        findViewById(R.id.card_data_index_11_4_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小猪米花机"));
        findViewById(R.id.card_data_index_11_4_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喷气牛"));
        findViewById(R.id.card_data_index_11_4_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "卖萌喵"));
        findViewById(R.id.card_data_index_11_4_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "奥丁神使"));
        findViewById(R.id.card_data_index_11_4_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "法师蛇"));
        findViewById(R.id.card_data_index_11_4_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "街头烤肉大师"));
        findViewById(R.id.card_data_index_11_4_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "后羿神使"));
        findViewById(R.id.card_data_index_12_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雷电长棍面包"));
        findViewById(R.id.card_data_index_12_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "三指兔"));
        findViewById(R.id.card_data_index_12_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "结界马"));
        findViewById(R.id.card_data_index_12_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力大炮"));
        findViewById(R.id.card_data_index_12_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "导弹蛇"));
        findViewById(R.id.card_data_index_12_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "盖亚神使"));
        findViewById(R.id.card_data_index_12_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "可乐炸弹"));
        findViewById(R.id.card_data_index_12_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酒瓶炸弹"));
        findViewById(R.id.card_data_index_12_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "开水壶炸弹"));
        findViewById(R.id.card_data_index_12_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "威士忌炸弹"));
        findViewById(R.id.card_data_index_12_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "潘多拉"));
        findViewById(R.id.card_data_index_12_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "深水炸弹"));
        findViewById(R.id.card_data_index_12_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爆辣河豚"));
        findViewById(R.id.card_data_index_12_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爆竹"));
        findViewById(R.id.card_data_index_12_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美食烟花普通版"));
        findViewById(R.id.card_data_index_12_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美食烟花华丽版"));
        findViewById(R.id.card_data_index_12_2_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "水瓶座精灵"));
        findViewById(R.id.card_data_index_12_2_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雷暴猪"));
        findViewById(R.id.card_data_index_12_2_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "微波炉爆弹"));
        findViewById(R.id.card_data_index_12_2_14).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "玉兔灯笼"));
        findViewById(R.id.card_data_index_12_2_15).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爆裂蛇"));
        findViewById(R.id.card_data_index_12_2_16).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "糖果罐子"));
        findViewById(R.id.card_data_index_12_2_17).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "烛阴龙"));
        findViewById(R.id.card_data_index_12_2_18).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "老鼠夹子"));
        findViewById(R.id.card_data_index_12_2_19).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "麻辣串炸弹"));
        findViewById(R.id.card_data_index_12_2_20).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "竹筒粽子"));
        findViewById(R.id.card_data_index_12_2_21).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "娇娇虎"));
        findViewById(R.id.card_data_index_12_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "辣椒粉"));
        findViewById(R.id.card_data_index_12_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "月蟾兔"));
        findViewById(R.id.card_data_index_12_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爆炸汪"));
        findViewById(R.id.card_data_index_12_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肉松清明粿"));
        findViewById(R.id.card_data_index_12_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "10周年烟花"));
        findViewById(R.id.card_data_index_12_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "芥末牛"));
        findViewById(R.id.card_data_index_13_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "钢丝球"));
        findViewById(R.id.card_data_index_13_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炸地鼠爆竹"));
        findViewById(R.id.card_data_index_13_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "面粉袋"));
        findViewById(R.id.card_data_index_13_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "椰子果"));
        findViewById(R.id.card_data_index_13_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "青涩柿柿"));
        findViewById(R.id.card_data_index_13_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "萌虎高压锅"));
        findViewById(R.id.card_data_index_13_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "白羊座精灵"));
        findViewById(R.id.card_data_index_13_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酋长汪"));
        findViewById(R.id.card_data_index_13_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "逗猫棒"));
        findViewById(R.id.card_data_index_13_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金牛烟花"));
        findViewById(R.id.card_data_index_13_1_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "贪吃兔"));
        findViewById(R.id.card_data_index_13_1_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "灵鱼摩蹉神使"));
        findViewById(R.id.card_data_index_13_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "榴莲"));
        findViewById(R.id.card_data_index_13_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美味电鳗"));
        findViewById(R.id.card_data_index_13_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "镭射喵"));
        findViewById(R.id.card_data_index_13_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "黑暗神使"));
        findViewById(R.id.card_data_index_13_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火龙果"));
        findViewById(R.id.card_data_index_13_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "摩羯座精灵"));
        findViewById(R.id.card_data_index_13_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "龙珠果"));
        findViewById(R.id.card_data_index_13_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巴德尔神使"));
        findViewById(R.id.card_data_index_13_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰桶炸弹"));
        findViewById(R.id.card_data_index_13_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰弹喵"));
        findViewById(R.id.card_data_index_13_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰兔菓子"));
        findViewById(R.id.card_data_index_13_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "泡泡糖"));
        findViewById(R.id.card_data_index_13_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "逆转牛"));
        findViewById(R.id.card_data_index_13_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蛋蛋兔"));
        findViewById(R.id.card_data_index_14_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰激凌"));
        findViewById(R.id.card_data_index_14_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "转龙壶"));
        findViewById(R.id.card_data_index_14_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "美味计时器"));
        findViewById(R.id.card_data_index_14_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "柯罗诺斯神使"));
        findViewById(R.id.card_data_index_14_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "幻幻鸡"));
        findViewById(R.id.card_data_index_14_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "百变蛇"));
        findViewById(R.id.card_data_index_14_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "梵天神使"));
        findViewById(R.id.card_data_index_14_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "顽皮龙"));
        findViewById(R.id.card_data_index_14_1_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "圣诞包裹"));
        findViewById(R.id.card_data_index_14_1_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "天使猪"));
        findViewById(R.id.card_data_index_14_1_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "黯然销魂饭"));
        findViewById(R.id.card_data_index_14_1_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "星穹马"));
        findViewById(R.id.card_data_index_14_1_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "13周年时光机"));
        findViewById(R.id.card_data_index_14_1_14).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "蛇蛇酒"));
        findViewById(R.id.card_data_index_14_1_15).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "克洛托神使"));
        findViewById(R.id.card_data_index_14_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "油灯"));
        findViewById(R.id.card_data_index_14_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "南瓜灯"));
        findViewById(R.id.card_data_index_14_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "肉松清明粿"));
        findViewById(R.id.card_data_index_14_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "防萤草灯笼"));
        findViewById(R.id.card_data_index_14_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "萤火蛇"));
        findViewById(R.id.card_data_index_14_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "换气扇"));
        findViewById(R.id.card_data_index_14_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "9周年幸运草扇"));
        findViewById(R.id.card_data_index_14_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "棕榈吹风机"));
        findViewById(R.id.card_data_index_14_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "爆爆鸡"));
        findViewById(R.id.card_data_index_14_2_10).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "清障猪"));
        findViewById(R.id.card_data_index_14_2_11).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "旋风牛"));
        findViewById(R.id.card_data_index_14_2_12).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酸柠檬爆弹"));
        findViewById(R.id.card_data_index_14_2_13).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "炸炸菇"));
        findViewById(R.id.card_data_index_14_2_14).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "海盐粉"));
        findViewById(R.id.card_data_index_14_2_15).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "碎冰喵"));
        findViewById(R.id.card_data_index_14_3_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "防风草沙拉"));
        findViewById(R.id.card_data_index_14_3_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "金箔甜筒"));
        findViewById(R.id.card_data_index_14_3_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "治愈喵"));
        findViewById(R.id.card_data_index_14_3_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "12周年能量饮料"));
        findViewById(R.id.card_data_index_14_3_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "木塞子"));
        findViewById(R.id.card_data_index_14_3_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "咖啡粉"));
        findViewById(R.id.card_data_index_14_3_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "尖叫马咖"));
        findViewById(R.id.card_data_index_14_3_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "傀儡马"));
        findViewById(R.id.card_data_index_14_4_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猫猫盒"));
        findViewById(R.id.card_data_index_14_4_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "猫猫箱"));
        findViewById(R.id.card_data_index_14_4_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "小丑盒子"));
        findViewById(R.id.card_data_index_14_4_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "鼠乐宝味觉糖"));
        findViewById(R.id.card_data_index_14_4_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "大福虎"));
        findViewById(R.id.card_data_index_15_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "土司面包"));
        findViewById(R.id.card_data_index_15_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "月饼"));
        findViewById(R.id.card_data_index_15_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "冰皮月饼"));
        findViewById(R.id.card_data_index_15_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "巧克力面包"));
        findViewById(R.id.card_data_index_15_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "菠萝爆炸面包"));
        findViewById(R.id.card_data_index_15_1_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "老虎蟹面包"));
        findViewById(R.id.card_data_index_15_1_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "桂花酒"));
        findViewById(R.id.card_data_index_15_1_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "榴莲千层饼"));
        findViewById(R.id.card_data_index_15_2_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "瓜皮护罩"));
        findViewById(R.id.card_data_index_15_2_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "处女座精灵"));
        findViewById(R.id.card_data_index_15_2_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "赫拉神使"));
        findViewById(R.id.card_data_index_15_2_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "祥龙环"));
        findViewById(R.id.card_data_index_15_2_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "守能汪"));
        findViewById(R.id.card_data_index_15_2_6).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "生日帽"));
        findViewById(R.id.card_data_index_15_2_7).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "喵喵炉"));
        findViewById(R.id.card_data_index_15_2_8).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "扑克牌护罩"));
        findViewById(R.id.card_data_index_15_2_9).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "彩虹蛇"));
        findViewById(R.id.card_data_index_16_1_1).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火炉菠萝面包"));
        findViewById(R.id.card_data_index_16_1_2).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "雪芭煮蛋器"));
        findViewById(R.id.card_data_index_16_1_3).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "火影怪味鱿鱼"));
        findViewById(R.id.card_data_index_16_1_4).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "酱香锅烤栗子"));
        findViewById(R.id.card_data_index_16_1_5).setOnClickListener(v -> CardDataHelper.selectCardDataByName(this, "热狗耗油双菇"));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonIndex));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSearch));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便添加一个位移动画
        MaterialCardView cardView = findViewById(R.id.FloatButton_CardDataIndex_Container);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                cardView,
                View.TRANSLATION_X,
                550f, 0f // 从1000px移动到0px
        );
        animator.setDuration(1200);
        animator.start();

        // 顺便添加一个位移动画
        cardView = findViewById(R.id.FloatButton_CardDataSearch_Container);
        animator = ObjectAnimator.ofFloat(
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
        findViewById(R.id.FloatButton_CardDataIndex_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }
}