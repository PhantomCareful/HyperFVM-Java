package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.CardItemDecoration;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.CardSuggestion;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.top_bar_data_center_card_data_index) + " ");

        // 添加模糊材质
        setupBlurEffect();

        // 防御卡目录按钮
        CardDataIndexContainer = findViewById(R.id.CardDataIndex_Container);
        findViewById(R.id.FloatButton_CardDataIndex_Container).setOnClickListener(v ->
                v.postDelayed(this::showTitleNavigationDialog, pressFeedbackAnimationDelay));

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnClickListener(v ->
                v.postDelayed(this::showCardQueryDialog, pressFeedbackAnimationDelay));

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }}, 50);
    }

    /**
     * 弹出标题导航弹窗
     */
    private void showTitleNavigationDialog() {
        // 获取标题数组
        String[] titleEntries = getResources().getStringArray(R.array.card_data_index_titles);

        // 加载自定义布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.item_dialog_selection, null);
        ListView listView = dialogView.findViewById(R.id.dialog_list);

        // 设置列表
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_index_selection, titleEntries);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // 构建目录列表弹窗
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
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
            case 29 -> R.id.title_card_data_index_11_2;
            case 30 -> R.id.title_card_data_index_11_3;
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
     * 显示卡片查询弹窗
     */
    private void showCardQueryDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_card_data, null);
        TextInputEditText etCardName = dialogView.findViewById(R.id.textInputEditText);
        RecyclerView suggestionList = dialogView.findViewById(R.id.suggestion_list);

        // 初始化适配器（传入上下文、空数据、点击监听）
        SuggestionAdapter adapter = new SuggestionAdapter(this, new ArrayList<>(), suggestion -> {
            // 点击项：填充名称到输入框，隐藏列表
            etCardName.setText(suggestion.getName());
            suggestionList.setVisibility(View.GONE);
        });

        // 配置RecyclerView（保持原有逻辑）
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        suggestionList.setAdapter(adapter);
        CardItemDecoration itemDecoration = new CardItemDecoration(suggestionList, 20, 20);
        suggestionList.addItemDecoration(itemDecoration);

        // 实时模糊查询（修改核心：适配新的数据模型）
        etCardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // 从数据库获取：包含name和image_id的搜索结果
                    List<CardSuggestion> suggestions = dbHelper.searchCards(keyword);
                    adapter.updateData(suggestions);
                    suggestionList.setVisibility(View.VISIBLE);
                } else {
                    adapter.updateData(new ArrayList<>());
                    suggestionList.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 显示弹窗（保持原有逻辑）
        new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setTitle(getResources().getString(R.string.card_data_search_title))
                .setView(dialogView)
                .setPositiveButton("查询", (dialog, which) -> {
                    String cardName = Objects.requireNonNull(etCardName.getText()).toString().trim();
                    selectCardDataByName(cardName);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
     */
    private void initCardImages() {
        findViewById(R.id.card_data_index_1_1_1).setOnClickListener(v -> selectCardDataByName("双向水管"));
        findViewById(R.id.card_data_index_1_1_2).setOnClickListener(v -> selectCardDataByName("天秤座精灵"));
        findViewById(R.id.card_data_index_1_1_3).setOnClickListener(v -> selectCardDataByName("呆呆鸡"));
        findViewById(R.id.card_data_index_1_1_4).setOnClickListener(v -> selectCardDataByName("阿瑞斯神使"));
        findViewById(R.id.card_data_index_1_1_5).setOnClickListener(v -> selectCardDataByName("二哈汪"));
        findViewById(R.id.card_data_index_1_1_6).setOnClickListener(v -> selectCardDataByName("双枪喵"));
        findViewById(R.id.card_data_index_1_1_7).setOnClickListener(v -> selectCardDataByName("散弹牛"));
        findViewById(R.id.card_data_index_1_1_8).setOnClickListener(v -> selectCardDataByName("威风虎"));
        findViewById(R.id.card_data_index_1_2_1).setOnClickListener(v -> selectCardDataByName("三线酒架"));
        findViewById(R.id.card_data_index_1_2_2).setOnClickListener(v -> selectCardDataByName("射手座精灵"));
        findViewById(R.id.card_data_index_1_2_3).setOnClickListener(v -> selectCardDataByName("砰砰鸡"));
        findViewById(R.id.card_data_index_1_2_4).setOnClickListener(v -> selectCardDataByName("丘比特神使"));
        findViewById(R.id.card_data_index_1_2_5).setOnClickListener(v -> selectCardDataByName("狩猎汪"));
        findViewById(R.id.card_data_index_1_2_6).setOnClickListener(v -> selectCardDataByName("猪猪猎手"));
        findViewById(R.id.card_data_index_1_2_7).setOnClickListener(v -> selectCardDataByName("炙烤灯笼鱼"));
        findViewById(R.id.card_data_index_1_3_1).setOnClickListener(v -> selectCardDataByName("枪塔喵"));
        findViewById(R.id.card_data_index_1_3_2).setOnClickListener(v -> selectCardDataByName("弩箭牛"));
        findViewById(R.id.card_data_index_1_3_3).setOnClickListener(v -> selectCardDataByName("仙人掌刺身"));
        findViewById(R.id.card_data_index_2_1_1).setOnClickListener(v -> selectCardDataByName("勺勺兔"));
        findViewById(R.id.card_data_index_2_1_2).setOnClickListener(v -> selectCardDataByName("窃蛋龙"));
        findViewById(R.id.card_data_index_2_1_3).setOnClickListener(v -> selectCardDataByName("尤弥尔神使"));
        findViewById(R.id.card_data_index_2_1_4).setOnClickListener(v -> selectCardDataByName("幻影蛇"));
        findViewById(R.id.card_data_index_2_1_5).setOnClickListener(v -> selectCardDataByName("全能糖球投手"));
        findViewById(R.id.card_data_index_2_2_1).setOnClickListener(v -> selectCardDataByName("煮蛋器投手"));
        findViewById(R.id.card_data_index_2_2_2).setOnClickListener(v -> selectCardDataByName("冰煮蛋器"));
        findViewById(R.id.card_data_index_2_2_3).setOnClickListener(v -> selectCardDataByName("双鱼座精灵"));
        findViewById(R.id.card_data_index_2_2_4).setOnClickListener(v -> selectCardDataByName("弹弹鸡"));
        findViewById(R.id.card_data_index_2_2_5).setOnClickListener(v -> selectCardDataByName("索尔神使"));
        findViewById(R.id.card_data_index_2_2_6).setOnClickListener(v -> selectCardDataByName("机械汪"));
        findViewById(R.id.card_data_index_2_2_7).setOnClickListener(v -> selectCardDataByName("投弹猪"));
        findViewById(R.id.card_data_index_2_2_8).setOnClickListener(v -> selectCardDataByName("雪糕投手"));
        findViewById(R.id.card_data_index_2_2_9).setOnClickListener(v -> selectCardDataByName("飞鱼喵"));
        findViewById(R.id.card_data_index_2_2_10).setOnClickListener(v -> selectCardDataByName("壮壮牛"));
        findViewById(R.id.card_data_index_2_2_11).setOnClickListener(v -> selectCardDataByName("烤蜥蜴投手"));
        findViewById(R.id.card_data_index_2_2_12).setOnClickListener(v -> selectCardDataByName("投篮虎"));
        findViewById(R.id.card_data_index_2_2_13).setOnClickListener(v -> selectCardDataByName("钵钵鸡"));
        findViewById(R.id.card_data_index_2_3_1).setOnClickListener(v -> selectCardDataByName("色拉投手"));
        findViewById(R.id.card_data_index_2_3_2).setOnClickListener(v -> selectCardDataByName("巧克力投手"));
        findViewById(R.id.card_data_index_2_3_3).setOnClickListener(v -> selectCardDataByName("臭豆腐投手"));
        findViewById(R.id.card_data_index_2_3_4).setOnClickListener(v -> selectCardDataByName("8周年蛋糕"));
        findViewById(R.id.card_data_index_3_1_1).setOnClickListener(v -> selectCardDataByName("炭烧海星"));
        findViewById(R.id.card_data_index_3_1_2).setOnClickListener(v -> selectCardDataByName("猪猪料理机"));
        findViewById(R.id.card_data_index_3_1_3).setOnClickListener(v -> selectCardDataByName("陀螺喵"));
        findViewById(R.id.card_data_index_3_1_4).setOnClickListener(v -> selectCardDataByName("哈迪斯神使"));
        findViewById(R.id.card_data_index_3_1_5).setOnClickListener(v -> selectCardDataByName("查克拉兔"));
        findViewById(R.id.card_data_index_3_2_1).setOnClickListener(v -> selectCardDataByName("厨师虎"));
        findViewById(R.id.card_data_index_3_2_2).setOnClickListener(v -> selectCardDataByName("星星兔"));
        findViewById(R.id.card_data_index_3_2_3).setOnClickListener(v -> selectCardDataByName("坚果爆炒机"));
        findViewById(R.id.card_data_index_3_2_4).setOnClickListener(v -> selectCardDataByName("里格神使"));
        findViewById(R.id.card_data_index_3_2_5).setOnClickListener(v -> selectCardDataByName("怪味鱿鱼"));
        findViewById(R.id.card_data_index_3_2_6).setOnClickListener(v -> selectCardDataByName("烟花虎"));
        findViewById(R.id.card_data_index_3_2_7).setOnClickListener(v -> selectCardDataByName("风车龙"));
        findViewById(R.id.card_data_index_3_3_1).setOnClickListener(v -> selectCardDataByName("鲈鱼"));
        findViewById(R.id.card_data_index_3_3_2).setOnClickListener(v -> selectCardDataByName("便便汪"));
        findViewById(R.id.card_data_index_3_3_3).setOnClickListener(v -> selectCardDataByName("烧鸡"));
        findViewById(R.id.card_data_index_3_3_4).setOnClickListener(v -> selectCardDataByName("饼干汪"));
        findViewById(R.id.card_data_index_3_3_5).setOnClickListener(v -> selectCardDataByName("牛角面包"));
        findViewById(R.id.card_data_index_3_3_6).setOnClickListener(v -> selectCardDataByName("盾盾汪"));
        findViewById(R.id.card_data_index_4_1_1).setOnClickListener(v -> selectCardDataByName("火盆"));
        findViewById(R.id.card_data_index_4_1_2).setOnClickListener(v -> selectCardDataByName("金牛座精灵"));
        findViewById(R.id.card_data_index_4_1_3).setOnClickListener(v -> selectCardDataByName("洛基神使"));
        findViewById(R.id.card_data_index_4_1_4).setOnClickListener(v -> selectCardDataByName("暖炉汪"));
        findViewById(R.id.card_data_index_4_1_5).setOnClickListener(v -> selectCardDataByName("能量喵"));
        findViewById(R.id.card_data_index_4_1_6).setOnClickListener(v -> selectCardDataByName("坩埚蛇"));
        findViewById(R.id.card_data_index_4_1_7).setOnClickListener(v -> selectCardDataByName("猪猪加强器"));
        findViewById(R.id.card_data_index_4_1_8).setOnClickListener(v -> selectCardDataByName("蓝莓信号塔塔"));
        findViewById(R.id.card_data_index_4_1_9).setOnClickListener(v -> selectCardDataByName("美味水果塔"));
        findViewById(R.id.card_data_index_4_1_10).setOnClickListener(v -> selectCardDataByName("欧若拉神使"));
        findViewById(R.id.card_data_index_4_2_1).setOnClickListener(v -> selectCardDataByName("莓果点心"));
        findViewById(R.id.card_data_index_4_2_2).setOnClickListener(v -> selectCardDataByName("香料虎"));
        findViewById(R.id.card_data_index_4_2_3).setOnClickListener(v -> selectCardDataByName("塔利亚神使"));
        findViewById(R.id.card_data_index_4_2_4).setOnClickListener(v -> selectCardDataByName("精灵龙"));
        findViewById(R.id.card_data_index_4_2_5).setOnClickListener(v -> selectCardDataByName("龙须面"));
        findViewById(R.id.card_data_index_4_2_6).setOnClickListener(v -> selectCardDataByName("五谷丰登"));
        findViewById(R.id.card_data_index_4_2_7).setOnClickListener(v -> selectCardDataByName("五行蛇"));
        findViewById(R.id.card_data_index_4_2_8).setOnClickListener(v -> selectCardDataByName("弗雷神使"));
        findViewById(R.id.card_data_index_4_2_9).setOnClickListener(v -> selectCardDataByName("加速榨汁机"));
        findViewById(R.id.card_data_index_4_2_10).setOnClickListener(v -> selectCardDataByName("魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_11).setOnClickListener(v -> selectCardDataByName("炎焱兔"));
        findViewById(R.id.card_data_index_4_3_1).setOnClickListener(v -> selectCardDataByName("11周年美食盒子"));
        findViewById(R.id.card_data_index_5_1_1).setOnClickListener(v -> selectCardDataByName("小火炉"));
        findViewById(R.id.card_data_index_5_1_2).setOnClickListener(v -> selectCardDataByName("大火炉"));
        findViewById(R.id.card_data_index_5_1_3).setOnClickListener(v -> selectCardDataByName("酒杯灯"));
        findViewById(R.id.card_data_index_5_1_4).setOnClickListener(v -> selectCardDataByName("双子座精灵"));
        findViewById(R.id.card_data_index_5_1_5).setOnClickListener(v -> selectCardDataByName("咕咕鸡"));
        findViewById(R.id.card_data_index_5_1_6).setOnClickListener(v -> selectCardDataByName("暖暖鸡"));
        findViewById(R.id.card_data_index_5_1_7).setOnClickListener(v -> selectCardDataByName("阿波罗神使"));
        findViewById(R.id.card_data_index_5_1_8).setOnClickListener(v -> selectCardDataByName("7周年蜡烛"));
        findViewById(R.id.card_data_index_5_1_9).setOnClickListener(v -> selectCardDataByName("火焰牛"));
        findViewById(R.id.card_data_index_5_1_10).setOnClickListener(v -> selectCardDataByName("花火龙"));
        findViewById(R.id.card_data_index_5_1_11).setOnClickListener(v -> selectCardDataByName("蛇羹煲"));
        findViewById(R.id.card_data_index_5_2_1).setOnClickListener(v -> selectCardDataByName("钱罐猪"));
        findViewById(R.id.card_data_index_5_2_2).setOnClickListener(v -> selectCardDataByName("罐罐牛"));
        findViewById(R.id.card_data_index_5_2_3).setOnClickListener(v -> selectCardDataByName("烈火虎"));
        findViewById(R.id.card_data_index_6_1_1).setOnClickListener(v -> selectCardDataByName("樱桃反弹布丁"));
        findViewById(R.id.card_data_index_6_1_2).setOnClickListener(v -> selectCardDataByName("艾草粑粑"));
        findViewById(R.id.card_data_index_6_1_3).setOnClickListener(v -> selectCardDataByName("布丁汪"));
        findViewById(R.id.card_data_index_6_1_4).setOnClickListener(v -> selectCardDataByName("凉粉牛"));
        findViewById(R.id.card_data_index_6_1_5).setOnClickListener(v -> selectCardDataByName("忒提丝神使"));
        findViewById(R.id.card_data_index_6_2_1).setOnClickListener(v -> selectCardDataByName("木盘子"));
        findViewById(R.id.card_data_index_6_2_2).setOnClickListener(v -> selectCardDataByName("盘盘鸡"));
        findViewById(R.id.card_data_index_6_2_3).setOnClickListener(v -> selectCardDataByName("猫猫盘"));
        findViewById(R.id.card_data_index_6_2_4).setOnClickListener(v -> selectCardDataByName("魔法软糖"));
        findViewById(R.id.card_data_index_6_2_5).setOnClickListener(v -> selectCardDataByName("棉花糖"));
        findViewById(R.id.card_data_index_6_2_6).setOnClickListener(v -> selectCardDataByName("苏打气泡"));
        findViewById(R.id.card_data_index_6_2_7).setOnClickListener(v -> selectCardDataByName("麦芽糖"));
        findViewById(R.id.card_data_index_7_1_1).setOnClickListener(v -> selectCardDataByName("糖葫芦炮弹"));
        findViewById(R.id.card_data_index_7_1_2).setOnClickListener(v -> selectCardDataByName("跳跳鸡"));
        findViewById(R.id.card_data_index_7_1_3).setOnClickListener(v -> selectCardDataByName("防空喵"));
        findViewById(R.id.card_data_index_7_1_4).setOnClickListener(v -> selectCardDataByName("赫丘利神使"));
        findViewById(R.id.card_data_index_7_2_1).setOnClickListener(v -> selectCardDataByName("香肠"));
        findViewById(R.id.card_data_index_7_2_2).setOnClickListener(v -> selectCardDataByName("热狗大炮"));
        findViewById(R.id.card_data_index_7_2_3).setOnClickListener(v -> selectCardDataByName("弹簧虎"));
        findViewById(R.id.card_data_index_7_2_4).setOnClickListener(v -> selectCardDataByName("泡泡龙"));
        findViewById(R.id.card_data_index_7_2_5).setOnClickListener(v -> selectCardDataByName("爱心便当"));
        findViewById(R.id.card_data_index_7_2_6).setOnClickListener(v -> selectCardDataByName("梦幻多拿滋"));
        findViewById(R.id.card_data_index_7_2_7).setOnClickListener(v -> selectCardDataByName("埃罗斯神使"));
        findViewById(R.id.card_data_index_7_2_8).setOnClickListener(v -> selectCardDataByName("耗油双菇"));
        findViewById(R.id.card_data_index_7_2_9).setOnClickListener(v -> selectCardDataByName("奶茶猪"));
        findViewById(R.id.card_data_index_7_2_10).setOnClickListener(v -> selectCardDataByName("科技喵"));
        findViewById(R.id.card_data_index_8_1_1).setOnClickListener(v -> selectCardDataByName("咖啡喷壶"));
        findViewById(R.id.card_data_index_8_1_2).setOnClickListener(v -> selectCardDataByName("关东煮喷锅"));
        findViewById(R.id.card_data_index_8_1_3).setOnClickListener(v -> selectCardDataByName("烈焰龙"));
        findViewById(R.id.card_data_index_8_1_4).setOnClickListener(v -> selectCardDataByName("赫斯提亚神使"));
        findViewById(R.id.card_data_index_8_2_1).setOnClickListener(v -> selectCardDataByName("旋转咖啡喷壶"));
        findViewById(R.id.card_data_index_8_2_2).setOnClickListener(v -> selectCardDataByName("狮子座精灵"));
        findViewById(R.id.card_data_index_8_2_3).setOnClickListener(v -> selectCardDataByName("波塞冬神使"));
        findViewById(R.id.card_data_index_8_2_4).setOnClickListener(v -> selectCardDataByName("转转鸡"));
        findViewById(R.id.card_data_index_8_2_5).setOnClickListener(v -> selectCardDataByName("可乐汪"));
        findViewById(R.id.card_data_index_8_2_6).setOnClickListener(v -> selectCardDataByName("元气牛"));
        findViewById(R.id.card_data_index_8_2_7).setOnClickListener(v -> selectCardDataByName("巫蛊蛇"));
        findViewById(R.id.card_data_index_9_1_1).setOnClickListener(v -> selectCardDataByName("章鱼烧"));
        findViewById(R.id.card_data_index_9_1_2).setOnClickListener(v -> selectCardDataByName("巨蟹座精灵"));
        findViewById(R.id.card_data_index_9_1_3).setOnClickListener(v -> selectCardDataByName("忍忍鸡"));
        findViewById(R.id.card_data_index_9_1_4).setOnClickListener(v -> selectCardDataByName("狄安娜神使"));
        findViewById(R.id.card_data_index_9_1_5).setOnClickListener(v -> selectCardDataByName("飞盘汪"));
        findViewById(R.id.card_data_index_9_1_6).setOnClickListener(v -> selectCardDataByName("铁甲飞镖猪"));
        findViewById(R.id.card_data_index_9_1_7).setOnClickListener(v -> selectCardDataByName("海盗兔"));
        findViewById(R.id.card_data_index_9_2_1).setOnClickListener(v -> selectCardDataByName("咖喱龙虾炮"));
        findViewById(R.id.card_data_index_9_2_2).setOnClickListener(v -> selectCardDataByName("雅典娜守护"));
        findViewById(R.id.card_data_index_9_2_3).setOnClickListener(v -> selectCardDataByName("火箭猪"));
        findViewById(R.id.card_data_index_9_2_4).setOnClickListener(v -> selectCardDataByName("宙斯神使"));
        findViewById(R.id.card_data_index_9_3_1).setOnClickListener(v -> selectCardDataByName("魔法猪"));
        findViewById(R.id.card_data_index_9_3_2).setOnClickListener(v -> selectCardDataByName("招财喵"));
        findViewById(R.id.card_data_index_9_3_3).setOnClickListener(v -> selectCardDataByName("雪球兔"));
        findViewById(R.id.card_data_index_9_3_4).setOnClickListener(v -> selectCardDataByName("典伊神使"));
        findViewById(R.id.card_data_index_9_3_5).setOnClickListener(v -> selectCardDataByName("冰晶龙"));
        findViewById(R.id.card_data_index_9_3_6).setOnClickListener(v -> selectCardDataByName("冰块冷萃机"));
        findViewById(R.id.card_data_index_9_4_1).setOnClickListener(v -> selectCardDataByName("鼠鼠蛋糕空投器"));
        findViewById(R.id.card_data_index_9_4_2).setOnClickListener(v -> selectCardDataByName("风力空投猪"));
        findViewById(R.id.card_data_index_9_4_3).setOnClickListener(v -> selectCardDataByName("电流虎"));
        findViewById(R.id.card_data_index_10_1_1).setOnClickListener(v -> selectCardDataByName("肥牛火锅"));
        findViewById(R.id.card_data_index_10_1_2).setOnClickListener(v -> selectCardDataByName("麻辣香锅"));
        findViewById(R.id.card_data_index_10_1_3).setOnClickListener(v -> selectCardDataByName("生煎锅"));
        findViewById(R.id.card_data_index_10_1_4).setOnClickListener(v -> selectCardDataByName("铛铛虎"));
        findViewById(R.id.card_data_index_10_1_5).setOnClickListener(v -> selectCardDataByName("祝融神使"));
        findViewById(R.id.card_data_index_10_1_6).setOnClickListener(v -> selectCardDataByName("糖炒栗子"));
        findViewById(R.id.card_data_index_10_1_7).setOnClickListener(v -> selectCardDataByName("霜霜蛇"));
        findViewById(R.id.card_data_index_10_2_1).setOnClickListener(v -> selectCardDataByName("汉堡包"));
        findViewById(R.id.card_data_index_10_2_2).setOnClickListener(v -> selectCardDataByName("贪食蛙"));
        findViewById(R.id.card_data_index_10_2_3).setOnClickListener(v -> selectCardDataByName("吞噬龙"));
        findViewById(R.id.card_data_index_10_2_4).setOnClickListener(v -> selectCardDataByName("香辣年糕蟹"));
        findViewById(R.id.card_data_index_10_2_5).setOnClickListener(v -> selectCardDataByName("混沌神使"));
        findViewById(R.id.card_data_index_10_3_1).setOnClickListener(v -> selectCardDataByName("新疆炒面"));
        findViewById(R.id.card_data_index_10_3_2).setOnClickListener(v -> selectCardDataByName("丸子厨师"));
        findViewById(R.id.card_data_index_10_3_3).setOnClickListener(v -> selectCardDataByName("功夫汪"));
        findViewById(R.id.card_data_index_10_3_4).setOnClickListener(v -> selectCardDataByName("鱼刺"));
        findViewById(R.id.card_data_index_10_3_5).setOnClickListener(v -> selectCardDataByName("钢鱼刺"));
        findViewById(R.id.card_data_index_10_3_6).setOnClickListener(v -> selectCardDataByName("糖渍刺梨"));
        findViewById(R.id.card_data_index_10_4_1).setOnClickListener(v -> selectCardDataByName("蜂蜜史莱姆"));
        findViewById(R.id.card_data_index_11_1_1).setOnClickListener(v -> selectCardDataByName("小笼包"));
        findViewById(R.id.card_data_index_11_1_2).setOnClickListener(v -> selectCardDataByName("双层小笼包"));
        findViewById(R.id.card_data_index_11_1_3).setOnClickListener(v -> selectCardDataByName("三向小笼包"));
        findViewById(R.id.card_data_index_11_1_4).setOnClickListener(v -> selectCardDataByName("机枪小笼包"));
        findViewById(R.id.card_data_index_11_1_5).setOnClickListener(v -> selectCardDataByName("冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_6).setOnClickListener(v -> selectCardDataByName("双层冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_7).setOnClickListener(v -> selectCardDataByName("三向冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_8).setOnClickListener(v -> selectCardDataByName("机枪冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_9).setOnClickListener(v -> selectCardDataByName("国王小笼包"));
        findViewById(R.id.card_data_index_11_1_10).setOnClickListener(v -> selectCardDataByName("三向国王小笼包"));
        findViewById(R.id.card_data_index_11_1_11).setOnClickListener(v -> selectCardDataByName("贵族小笼包"));
        findViewById(R.id.card_data_index_11_1_12).setOnClickListener(v -> selectCardDataByName("玉蜀黍"));
        findViewById(R.id.card_data_index_11_1_13).setOnClickListener(v -> selectCardDataByName("包包龙"));
        findViewById(R.id.card_data_index_11_1_14).setOnClickListener(v -> selectCardDataByName("咖啡杯"));
        findViewById(R.id.card_data_index_11_1_15).setOnClickListener(v -> selectCardDataByName("水上茶杯"));
        findViewById(R.id.card_data_index_11_1_16).setOnClickListener(v -> selectCardDataByName("激光汪"));
        findViewById(R.id.card_data_index_11_2_1).setOnClickListener(v -> selectCardDataByName("天蝎座精灵"));
        findViewById(R.id.card_data_index_11_2_2).setOnClickListener(v -> selectCardDataByName("工程猪"));
        findViewById(R.id.card_data_index_11_2_3).setOnClickListener(v -> selectCardDataByName("双刃蛇"));
        findViewById(R.id.card_data_index_11_2_4).setOnClickListener(v -> selectCardDataByName("元素蛇"));
        findViewById(R.id.card_data_index_11_2_5).setOnClickListener(v -> selectCardDataByName("回旋虎"));
        findViewById(R.id.card_data_index_11_2_6).setOnClickListener(v -> selectCardDataByName("大师兔"));
        findViewById(R.id.card_data_index_11_2_7).setOnClickListener(v -> selectCardDataByName("15周年猴赛雷"));
        findViewById(R.id.card_data_index_11_2_8).setOnClickListener(v -> selectCardDataByName("赖皮蛇"));
        findViewById(R.id.card_data_index_11_2_9).setOnClickListener(v -> selectCardDataByName("迷你披萨炉"));
        findViewById(R.id.card_data_index_11_3_1).setOnClickListener(v -> selectCardDataByName("焦油喷壶"));
        findViewById(R.id.card_data_index_11_3_2).setOnClickListener(v -> selectCardDataByName("喷壶汪"));
        findViewById(R.id.card_data_index_11_3_3).setOnClickListener(v -> selectCardDataByName("派派鸡"));
        findViewById(R.id.card_data_index_11_3_4).setOnClickListener(v -> selectCardDataByName("小猪米花机"));
        findViewById(R.id.card_data_index_11_3_5).setOnClickListener(v -> selectCardDataByName("喷气牛"));
        findViewById(R.id.card_data_index_11_3_6).setOnClickListener(v -> selectCardDataByName("卖萌喵"));
        findViewById(R.id.card_data_index_11_3_7).setOnClickListener(v -> selectCardDataByName("奥丁神使"));
        findViewById(R.id.card_data_index_11_3_8).setOnClickListener(v -> selectCardDataByName("阴阳蛇"));
        findViewById(R.id.card_data_index_11_3_9).setOnClickListener(v -> selectCardDataByName("法师蛇"));
        findViewById(R.id.card_data_index_11_3_10).setOnClickListener(v -> selectCardDataByName("街头烤肉大师"));
        findViewById(R.id.card_data_index_11_3_11).setOnClickListener(v -> selectCardDataByName("后羿神使"));
        findViewById(R.id.card_data_index_12_1_1).setOnClickListener(v -> selectCardDataByName("雷电长棍面包"));
        findViewById(R.id.card_data_index_12_1_2).setOnClickListener(v -> selectCardDataByName("三指兔"));
        findViewById(R.id.card_data_index_12_1_3).setOnClickListener(v -> selectCardDataByName("巧克力大炮"));
        findViewById(R.id.card_data_index_12_1_4).setOnClickListener(v -> selectCardDataByName("导弹蛇"));
        findViewById(R.id.card_data_index_12_1_5).setOnClickListener(v -> selectCardDataByName("盖亚神使"));
        findViewById(R.id.card_data_index_12_2_1).setOnClickListener(v -> selectCardDataByName("可乐炸弹"));
        findViewById(R.id.card_data_index_12_2_2).setOnClickListener(v -> selectCardDataByName("酒瓶炸弹"));
        findViewById(R.id.card_data_index_12_2_3).setOnClickListener(v -> selectCardDataByName("开水壶炸弹"));
        findViewById(R.id.card_data_index_12_2_4).setOnClickListener(v -> selectCardDataByName("威士忌炸弹"));
        findViewById(R.id.card_data_index_12_2_5).setOnClickListener(v -> selectCardDataByName("潘多拉"));
        findViewById(R.id.card_data_index_12_2_6).setOnClickListener(v -> selectCardDataByName("深水炸弹"));
        findViewById(R.id.card_data_index_12_2_7).setOnClickListener(v -> selectCardDataByName("爆辣河豚"));
        findViewById(R.id.card_data_index_12_2_8).setOnClickListener(v -> selectCardDataByName("爆竹"));
        findViewById(R.id.card_data_index_12_2_9).setOnClickListener(v -> selectCardDataByName("美食烟花普通版"));
        findViewById(R.id.card_data_index_12_2_10).setOnClickListener(v -> selectCardDataByName("美食烟花华丽版"));
        findViewById(R.id.card_data_index_12_2_11).setOnClickListener(v -> selectCardDataByName("水瓶座精灵"));
        findViewById(R.id.card_data_index_12_2_12).setOnClickListener(v -> selectCardDataByName("雷暴猪"));
        findViewById(R.id.card_data_index_12_2_13).setOnClickListener(v -> selectCardDataByName("微波炉爆弹"));
        findViewById(R.id.card_data_index_12_2_14).setOnClickListener(v -> selectCardDataByName("玉兔灯笼"));
        findViewById(R.id.card_data_index_12_2_15).setOnClickListener(v -> selectCardDataByName("爆裂蛇"));
        findViewById(R.id.card_data_index_12_2_16).setOnClickListener(v -> selectCardDataByName("糖果罐子"));
        findViewById(R.id.card_data_index_12_2_17).setOnClickListener(v -> selectCardDataByName("烛阴龙"));
        findViewById(R.id.card_data_index_12_2_18).setOnClickListener(v -> selectCardDataByName("老鼠夹子"));
        findViewById(R.id.card_data_index_12_2_19).setOnClickListener(v -> selectCardDataByName("麻辣串炸弹"));
        findViewById(R.id.card_data_index_12_2_20).setOnClickListener(v -> selectCardDataByName("竹筒粽子"));
        findViewById(R.id.card_data_index_12_2_21).setOnClickListener(v -> selectCardDataByName("娇娇虎"));
        findViewById(R.id.card_data_index_12_3_1).setOnClickListener(v -> selectCardDataByName("辣椒粉"));
        findViewById(R.id.card_data_index_12_3_2).setOnClickListener(v -> selectCardDataByName("月蟾兔"));
        findViewById(R.id.card_data_index_12_3_3).setOnClickListener(v -> selectCardDataByName("爆炸汪"));
        findViewById(R.id.card_data_index_12_3_4).setOnClickListener(v -> selectCardDataByName("肉松清明粿"));
        findViewById(R.id.card_data_index_12_3_5).setOnClickListener(v -> selectCardDataByName("10周年烟花"));
        findViewById(R.id.card_data_index_12_3_6).setOnClickListener(v -> selectCardDataByName("芥末牛"));
        findViewById(R.id.card_data_index_13_1_1).setOnClickListener(v -> selectCardDataByName("钢丝球"));
        findViewById(R.id.card_data_index_13_1_2).setOnClickListener(v -> selectCardDataByName("炸地鼠爆竹"));
        findViewById(R.id.card_data_index_13_1_3).setOnClickListener(v -> selectCardDataByName("面粉袋"));
        findViewById(R.id.card_data_index_13_1_4).setOnClickListener(v -> selectCardDataByName("椰子果"));
        findViewById(R.id.card_data_index_13_1_5).setOnClickListener(v -> selectCardDataByName("青涩柿柿"));
        findViewById(R.id.card_data_index_13_1_6).setOnClickListener(v -> selectCardDataByName("萌虎高压锅"));
        findViewById(R.id.card_data_index_13_1_7).setOnClickListener(v -> selectCardDataByName("白羊座精灵"));
        findViewById(R.id.card_data_index_13_1_8).setOnClickListener(v -> selectCardDataByName("酋长汪"));
        findViewById(R.id.card_data_index_13_1_9).setOnClickListener(v -> selectCardDataByName("逗猫棒"));
        findViewById(R.id.card_data_index_13_1_10).setOnClickListener(v -> selectCardDataByName("金牛烟花"));
        findViewById(R.id.card_data_index_13_1_11).setOnClickListener(v -> selectCardDataByName("贪吃兔"));
        findViewById(R.id.card_data_index_13_1_12).setOnClickListener(v -> selectCardDataByName("灵鱼摩蹉神使"));
        findViewById(R.id.card_data_index_13_2_1).setOnClickListener(v -> selectCardDataByName("榴莲"));
        findViewById(R.id.card_data_index_13_2_2).setOnClickListener(v -> selectCardDataByName("美味电鳗"));
        findViewById(R.id.card_data_index_13_2_3).setOnClickListener(v -> selectCardDataByName("镭射喵"));
        findViewById(R.id.card_data_index_13_2_4).setOnClickListener(v -> selectCardDataByName("黑暗神使"));
        findViewById(R.id.card_data_index_13_2_5).setOnClickListener(v -> selectCardDataByName("火龙果"));
        findViewById(R.id.card_data_index_13_2_6).setOnClickListener(v -> selectCardDataByName("摩羯座精灵"));
        findViewById(R.id.card_data_index_13_2_7).setOnClickListener(v -> selectCardDataByName("龙珠果"));
        findViewById(R.id.card_data_index_13_2_8).setOnClickListener(v -> selectCardDataByName("巴德尔神使"));
        findViewById(R.id.card_data_index_13_3_1).setOnClickListener(v -> selectCardDataByName("冰桶炸弹"));
        findViewById(R.id.card_data_index_13_3_2).setOnClickListener(v -> selectCardDataByName("冰弹喵"));
        findViewById(R.id.card_data_index_13_3_3).setOnClickListener(v -> selectCardDataByName("冰兔菓子"));
        findViewById(R.id.card_data_index_13_3_4).setOnClickListener(v -> selectCardDataByName("泡泡糖"));
        findViewById(R.id.card_data_index_13_3_5).setOnClickListener(v -> selectCardDataByName("逆转牛"));
        findViewById(R.id.card_data_index_13_4_1).setOnClickListener(v -> selectCardDataByName("蛋蛋兔"));
        findViewById(R.id.card_data_index_14_1_1).setOnClickListener(v -> selectCardDataByName("冰激凌"));
        findViewById(R.id.card_data_index_14_1_2).setOnClickListener(v -> selectCardDataByName("13周年时光机"));
        findViewById(R.id.card_data_index_14_1_3).setOnClickListener(v -> selectCardDataByName("转龙壶"));
        findViewById(R.id.card_data_index_14_1_4).setOnClickListener(v -> selectCardDataByName("顽皮龙"));
        findViewById(R.id.card_data_index_14_1_5).setOnClickListener(v -> selectCardDataByName("美味计时器"));
        findViewById(R.id.card_data_index_14_1_6).setOnClickListener(v -> selectCardDataByName("柯罗诺斯神使"));
        findViewById(R.id.card_data_index_14_1_7).setOnClickListener(v -> selectCardDataByName("克洛托神使"));
        findViewById(R.id.card_data_index_14_1_8).setOnClickListener(v -> selectCardDataByName("蛇蛇酒"));
        findViewById(R.id.card_data_index_14_1_9).setOnClickListener(v -> selectCardDataByName("幻幻鸡"));
        findViewById(R.id.card_data_index_14_1_10).setOnClickListener(v -> selectCardDataByName("圣诞包裹"));
        findViewById(R.id.card_data_index_14_1_11).setOnClickListener(v -> selectCardDataByName("天使猪"));
        findViewById(R.id.card_data_index_14_1_12).setOnClickListener(v -> selectCardDataByName("黯然销魂饭"));
        findViewById(R.id.card_data_index_14_1_13).setOnClickListener(v -> selectCardDataByName("梵天神使"));
        findViewById(R.id.card_data_index_14_1_14).setOnClickListener(v -> selectCardDataByName("百变蛇"));
        findViewById(R.id.card_data_index_14_2_1).setOnClickListener(v -> selectCardDataByName("油灯"));
        findViewById(R.id.card_data_index_14_2_2).setOnClickListener(v -> selectCardDataByName("南瓜灯"));
        findViewById(R.id.card_data_index_14_2_3).setOnClickListener(v -> selectCardDataByName("肉松清明粿"));
        findViewById(R.id.card_data_index_14_2_4).setOnClickListener(v -> selectCardDataByName("防萤草灯笼"));
        findViewById(R.id.card_data_index_14_2_5).setOnClickListener(v -> selectCardDataByName("萤火蛇"));
        findViewById(R.id.card_data_index_14_2_6).setOnClickListener(v -> selectCardDataByName("换气扇"));
        findViewById(R.id.card_data_index_14_2_7).setOnClickListener(v -> selectCardDataByName("9周年幸运草扇"));
        findViewById(R.id.card_data_index_14_2_8).setOnClickListener(v -> selectCardDataByName("棕榈吹风机"));
        findViewById(R.id.card_data_index_14_2_9).setOnClickListener(v -> selectCardDataByName("爆爆鸡"));
        findViewById(R.id.card_data_index_14_2_10).setOnClickListener(v -> selectCardDataByName("清障猪"));
        findViewById(R.id.card_data_index_14_2_11).setOnClickListener(v -> selectCardDataByName("旋风牛"));
        findViewById(R.id.card_data_index_14_2_12).setOnClickListener(v -> selectCardDataByName("酸柠檬爆弹"));
        findViewById(R.id.card_data_index_14_2_13).setOnClickListener(v -> selectCardDataByName("炸炸菇"));
        findViewById(R.id.card_data_index_14_2_14).setOnClickListener(v -> selectCardDataByName("海盐粉"));
        findViewById(R.id.card_data_index_14_2_15).setOnClickListener(v -> selectCardDataByName("碎冰喵"));
        findViewById(R.id.card_data_index_14_3_1).setOnClickListener(v -> selectCardDataByName("木塞子"));
        findViewById(R.id.card_data_index_14_3_2).setOnClickListener(v -> selectCardDataByName("防风草沙拉"));
        findViewById(R.id.card_data_index_14_3_3).setOnClickListener(v -> selectCardDataByName("金箔甜筒"));
        findViewById(R.id.card_data_index_14_3_4).setOnClickListener(v -> selectCardDataByName("治愈喵"));
        findViewById(R.id.card_data_index_14_3_5).setOnClickListener(v -> selectCardDataByName("12周年能量饮料"));
        findViewById(R.id.card_data_index_14_3_6).setOnClickListener(v -> selectCardDataByName("咖啡粉"));
        findViewById(R.id.card_data_index_14_4_1).setOnClickListener(v -> selectCardDataByName("猫猫盒"));
        findViewById(R.id.card_data_index_14_4_2).setOnClickListener(v -> selectCardDataByName("猫猫箱"));
        findViewById(R.id.card_data_index_14_4_3).setOnClickListener(v -> selectCardDataByName("小丑盒子"));
        findViewById(R.id.card_data_index_14_4_4).setOnClickListener(v -> selectCardDataByName("鼠乐宝味觉糖"));
        findViewById(R.id.card_data_index_14_4_5).setOnClickListener(v -> selectCardDataByName("大福虎"));
        findViewById(R.id.card_data_index_15_1_1).setOnClickListener(v -> selectCardDataByName("土司面包"));
        findViewById(R.id.card_data_index_15_1_2).setOnClickListener(v -> selectCardDataByName("月饼"));
        findViewById(R.id.card_data_index_15_1_3).setOnClickListener(v -> selectCardDataByName("冰皮月饼"));
        findViewById(R.id.card_data_index_15_1_4).setOnClickListener(v -> selectCardDataByName("巧克力面包"));
        findViewById(R.id.card_data_index_15_1_5).setOnClickListener(v -> selectCardDataByName("菠萝爆炸面包"));
        findViewById(R.id.card_data_index_15_1_6).setOnClickListener(v -> selectCardDataByName("老虎蟹面包"));
        findViewById(R.id.card_data_index_15_1_7).setOnClickListener(v -> selectCardDataByName("桂花酒"));
        findViewById(R.id.card_data_index_15_1_8).setOnClickListener(v -> selectCardDataByName("榴莲千层饼"));
        findViewById(R.id.card_data_index_15_2_1).setOnClickListener(v -> selectCardDataByName("瓜皮护罩"));
        findViewById(R.id.card_data_index_15_2_2).setOnClickListener(v -> selectCardDataByName("处女座精灵"));
        findViewById(R.id.card_data_index_15_2_3).setOnClickListener(v -> selectCardDataByName("赫拉神使"));
        findViewById(R.id.card_data_index_15_2_4).setOnClickListener(v -> selectCardDataByName("祥龙环"));
        findViewById(R.id.card_data_index_15_2_5).setOnClickListener(v -> selectCardDataByName("守能汪"));
        findViewById(R.id.card_data_index_15_2_6).setOnClickListener(v -> selectCardDataByName("生日帽"));
        findViewById(R.id.card_data_index_15_2_7).setOnClickListener(v -> selectCardDataByName("喵喵炉"));
        findViewById(R.id.card_data_index_15_2_8).setOnClickListener(v -> selectCardDataByName("扑克牌护罩"));
        findViewById(R.id.card_data_index_15_2_9).setOnClickListener(v -> selectCardDataByName("彩虹蛇"));
        findViewById(R.id.card_data_index_16_1_1).setOnClickListener(v -> selectCardDataByName("火炉菠萝面包"));
        findViewById(R.id.card_data_index_16_1_2).setOnClickListener(v -> selectCardDataByName("雪芭煮蛋器"));
        findViewById(R.id.card_data_index_16_1_3).setOnClickListener(v -> selectCardDataByName("火影怪味鱿鱼"));
        findViewById(R.id.card_data_index_16_1_4).setOnClickListener(v -> selectCardDataByName("酱香锅烤栗子"));
        findViewById(R.id.card_data_index_16_1_5).setOnClickListener(v -> selectCardDataByName("热狗耗油双菇"));
    }

    private void selectCardDataByName(String cardName) {
        if (cardName.isEmpty()) {
            Toast.makeText(this, "请输入卡片名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String tableName = dbHelper.getCardTable(cardName);
        String baseName = dbHelper.getCardBaseName(cardName);
        if (tableName == null || baseName == null) {
            Toast.makeText(this, "未找到该卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 跳转详情页
        Intent intent = switch (tableName) {
            case "card_data_1" ->
                    new Intent(this, CardData_1_Activity.class);
            case "card_data_2" ->
                    new Intent(this, CardData_2_Activity.class);
            case "card_data_3" ->
                    new Intent(this, CardData_3_Activity.class);
            case "card_data_4" ->
                    new Intent(this, CardData_4_Activity.class);
            default -> null;
        };
        if (intent != null) {
            intent.putExtra("name", baseName);
            intent.putExtra("table", tableName);
            startActivity(intent);
        }
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题、启用返回按钮
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> this.finish());
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonIndex));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSearch));

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
    }
}