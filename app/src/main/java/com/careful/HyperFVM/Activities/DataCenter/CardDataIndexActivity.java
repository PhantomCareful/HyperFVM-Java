package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_INTERFACE_STYLE;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.Animation.ViewAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.CardItemDecoration;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardDataIndexActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private SpringBackScrollView CardDataIndexContainer;

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

        String currentInterfaceStyle = dbHelper.getSettingValueString(CONTENT_INTERFACE_STYLE);
        switch (currentInterfaceStyle) {
            case "鲜艳-立体":

                break;
            case "素雅-扁平":

                break;
        }

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.top_bar_data_center_card_data_index) + " ");

        // 添加模糊材质
        setupBlurEffect();

        // 防御卡目录按钮
        CardDataIndexContainer = findViewById(R.id.CardDataIndex_Container);
        findViewById(R.id.FloatButton_CardDataIndex_Container).setOnClickListener(v -> showTitleNavigationDialog());

        // 防御卡数据查询按钮
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnClickListener(v -> showCardQueryDialog());

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }}, 50);
    }

    /**
     * 给按钮和卡片添加按压反馈动画
     * @return 是否拦截触摸事件
     */
    private boolean setPressAnimation(View v, MotionEvent event) {
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            //setPress
            switch (event.getAction()) {
                // 按下：执行缩小动画（从当前大小开始）
                case MotionEvent.ACTION_DOWN:
                    ViewAnimationUtils.playPressScaleAnimation(v, true);
                    break;

                // 松开：执行恢复动画（从当前缩小的大小开始）
                case MotionEvent.ACTION_UP:
                    ViewAnimationUtils.playPressScaleAnimation(v, false);
                    break;

                // 取消（比如滑动离开View）：强制恢复动画
                case MotionEvent.ACTION_CANCEL:
                    ViewAnimationUtils.playPressScaleAnimation(v, false);
                    break;
            }
        }
        return false;
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

        // 获取控件（替换为RecyclerView）
        TextInputEditText etCardName = dialogView.findViewById(R.id.textInputEditText);
        RecyclerView suggestionList = dialogView.findViewById(R.id.suggestion_list);

        // 初始化适配器（使用自定义Material风格适配器）
        SuggestionAdapter adapter = new SuggestionAdapter(new ArrayList<>(), selected -> {
            // 点击项自动填充输入框并隐藏列表
            etCardName.setText(selected);
            suggestionList.setVisibility(View.GONE);
        });

        // 配置RecyclerView
        suggestionList.setLayoutManager(new LinearLayoutManager(this));
        suggestionList.setAdapter(adapter);

        // 配置建议列表的布局：第一张卡片顶部距离增加20dp，最后一张卡片底部距离增加20dp
        CardItemDecoration itemDecoration = new CardItemDecoration(suggestionList, 20, 20);
        suggestionList.addItemDecoration(itemDecoration);

        // 实时模糊查询
        etCardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // 从数据库获取匹配结果
                    List<String> suggestions = dbHelper.searchCardNames(keyword);
                    // 更新适配器数据
                    adapter.updateData(suggestions);
                    suggestionList.setVisibility(View.VISIBLE);
                } else {
                    // 清空数据并隐藏列表
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
        findViewById(R.id.card_data_index_1_1_1_0).setOnClickListener(v -> selectCardDataByName("双向水管"));
        findViewById(R.id.card_data_index_1_1_1_1).setOnClickListener(v -> selectCardDataByName("控温双向水管"));
        findViewById(R.id.card_data_index_1_1_1_2).setOnClickListener(v -> selectCardDataByName("合金水管"));
        findViewById(R.id.card_data_index_1_1_2_0).setOnClickListener(v -> selectCardDataByName("天秤座精灵"));
        findViewById(R.id.card_data_index_1_1_2_1).setOnClickListener(v -> selectCardDataByName("天秤座战将"));
        findViewById(R.id.card_data_index_1_1_2_2).setOnClickListener(v -> selectCardDataByName("天秤座星宿"));
        findViewById(R.id.card_data_index_1_1_3_0).setOnClickListener(v -> selectCardDataByName("呆呆鸡"));
        findViewById(R.id.card_data_index_1_1_3_1).setOnClickListener(v -> selectCardDataByName("水遁呆呆鸡"));
        findViewById(R.id.card_data_index_1_1_3_2).setOnClickListener(v -> selectCardDataByName("贤圣呆呆鸡"));
        findViewById(R.id.card_data_index_1_1_4_0).setOnClickListener(v -> selectCardDataByName("阿瑞斯神使"));
        findViewById(R.id.card_data_index_1_1_4_1).setOnClickListener(v -> selectCardDataByName("阿瑞斯圣神"));
        findViewById(R.id.card_data_index_1_1_4_2).setOnClickListener(v -> selectCardDataByName("战神·阿瑞斯"));
        findViewById(R.id.card_data_index_1_1_4_3).setOnClickListener(v -> selectCardDataByName("至尊战神"));
        findViewById(R.id.card_data_index_1_1_5_0).setOnClickListener(v -> selectCardDataByName("二哈汪"));
        findViewById(R.id.card_data_index_1_1_5_1).setOnClickListener(v -> selectCardDataByName("秘银二哈汪"));
        findViewById(R.id.card_data_index_1_1_5_2).setOnClickListener(v -> selectCardDataByName("金刚二哈汪"));
        findViewById(R.id.card_data_index_1_1_6_0).setOnClickListener(v -> selectCardDataByName("双枪喵"));
        findViewById(R.id.card_data_index_1_1_6_1).setOnClickListener(v -> selectCardDataByName("左轮双枪喵"));
        findViewById(R.id.card_data_index_1_1_6_2).setOnClickListener(v -> selectCardDataByName("等离子双枪喵"));
        findViewById(R.id.card_data_index_1_1_7_0).setOnClickListener(v -> selectCardDataByName("散弹牛"));
        findViewById(R.id.card_data_index_1_1_7_1).setOnClickListener(v -> selectCardDataByName("威武散弹牛"));
        findViewById(R.id.card_data_index_1_1_7_2).setOnClickListener(v -> selectCardDataByName("霸气散弹牛"));
        findViewById(R.id.card_data_index_1_1_8_0).setOnClickListener(v -> selectCardDataByName("威风虎"));
        findViewById(R.id.card_data_index_1_1_8_1).setOnClickListener(v -> selectCardDataByName("爆气威风虎"));
        findViewById(R.id.card_data_index_1_1_8_2).setOnClickListener(v -> selectCardDataByName("连弩威风虎"));
        findViewById(R.id.card_data_index_1_2_1_0).setOnClickListener(v -> selectCardDataByName("三线酒架"));
        findViewById(R.id.card_data_index_1_2_1_1).setOnClickListener(v -> selectCardDataByName("强力三线酒架"));
        findViewById(R.id.card_data_index_1_2_1_2).setOnClickListener(v -> selectCardDataByName("终结者酒架"));
        findViewById(R.id.card_data_index_1_2_2_0).setOnClickListener(v -> selectCardDataByName("射手座精灵"));
        findViewById(R.id.card_data_index_1_2_2_1).setOnClickListener(v -> selectCardDataByName("射手座战将"));
        findViewById(R.id.card_data_index_1_2_2_2).setOnClickListener(v -> selectCardDataByName("射手座星宿"));
        findViewById(R.id.card_data_index_1_2_3_0).setOnClickListener(v -> selectCardDataByName("砰砰鸡"));
        findViewById(R.id.card_data_index_1_2_3_1).setOnClickListener(v -> selectCardDataByName("固岩砰砰鸡"));
        findViewById(R.id.card_data_index_1_2_3_2).setOnClickListener(v -> selectCardDataByName("暗殇砰砰鸡"));
        findViewById(R.id.card_data_index_1_2_4_0).setOnClickListener(v -> selectCardDataByName("丘比特神使"));
        findViewById(R.id.card_data_index_1_2_4_1).setOnClickListener(v -> selectCardDataByName("丘比特圣神"));
        findViewById(R.id.card_data_index_1_2_4_2).setOnClickListener(v -> selectCardDataByName("爱神·丘比特"));
        findViewById(R.id.card_data_index_1_2_4_3).setOnClickListener(v -> selectCardDataByName("至尊爱神"));
        findViewById(R.id.card_data_index_1_2_5_0).setOnClickListener(v -> selectCardDataByName("狩猎汪"));
        findViewById(R.id.card_data_index_1_2_5_1).setOnClickListener(v -> selectCardDataByName("丛林狩猎汪"));
        findViewById(R.id.card_data_index_1_2_5_2).setOnClickListener(v -> selectCardDataByName("部落狩猎汪"));
        findViewById(R.id.card_data_index_1_2_6_0).setOnClickListener(v -> selectCardDataByName("猪猪猎手"));
        findViewById(R.id.card_data_index_1_2_6_1).setOnClickListener(v -> selectCardDataByName("青铜猪猪猎手"));
        findViewById(R.id.card_data_index_1_2_6_2).setOnClickListener(v -> selectCardDataByName("王者猪猪猎手"));
        findViewById(R.id.card_data_index_1_2_7_0).setOnClickListener(v -> selectCardDataByName("炙烤灯笼鱼"));
        findViewById(R.id.card_data_index_1_2_7_1).setOnClickListener(v -> selectCardDataByName("炙烤辣灯笼鱼"));
        findViewById(R.id.card_data_index_1_2_7_2).setOnClickListener(v -> selectCardDataByName("炙烤五香灯笼鱼"));
        findViewById(R.id.card_data_index_1_3_1_0).setOnClickListener(v -> selectCardDataByName("仙人掌刺身"));
        findViewById(R.id.card_data_index_1_3_1_1).setOnClickListener(v -> selectCardDataByName("仙人球刺身"));
        findViewById(R.id.card_data_index_1_3_1_2).setOnClickListener(v -> selectCardDataByName("仙人掌花刺身"));
        findViewById(R.id.card_data_index_1_3_2_0).setOnClickListener(v -> selectCardDataByName("枪塔喵"));
        findViewById(R.id.card_data_index_1_3_2_1).setOnClickListener(v -> selectCardDataByName("榴弹枪塔喵"));
        findViewById(R.id.card_data_index_1_3_2_2).setOnClickListener(v -> selectCardDataByName("射线枪塔喵"));
        findViewById(R.id.card_data_index_1_3_3_0).setOnClickListener(v -> selectCardDataByName("弩箭牛"));
        findViewById(R.id.card_data_index_1_3_3_1).setOnClickListener(v -> selectCardDataByName("精英弩箭牛"));
        findViewById(R.id.card_data_index_1_3_3_2).setOnClickListener(v -> selectCardDataByName("暴力弩箭牛"));
        findViewById(R.id.card_data_index_2_1_1_0).setOnClickListener(v -> selectCardDataByName("勺勺兔"));
        findViewById(R.id.card_data_index_2_1_1_1).setOnClickListener(v -> selectCardDataByName("增强勺勺兔"));
        findViewById(R.id.card_data_index_2_1_1_2).setOnClickListener(v -> selectCardDataByName("盖世勺勺兔"));
        findViewById(R.id.card_data_index_2_1_2_0).setOnClickListener(v -> selectCardDataByName("窃蛋龙"));
        findViewById(R.id.card_data_index_2_1_2_1).setOnClickListener(v -> selectCardDataByName("蓝角窃蛋龙"));
        findViewById(R.id.card_data_index_2_1_2_2).setOnClickListener(v -> selectCardDataByName("钢爪窃蛋龙"));
        findViewById(R.id.card_data_index_2_1_3_0).setOnClickListener(v -> selectCardDataByName("尤弥尔神使"));
        findViewById(R.id.card_data_index_2_1_3_1).setOnClickListener(v -> selectCardDataByName("尤弥尔圣神"));
        findViewById(R.id.card_data_index_2_1_3_2).setOnClickListener(v -> selectCardDataByName("巨神·尤弥尔"));
        findViewById(R.id.card_data_index_2_1_3_3).setOnClickListener(v -> selectCardDataByName("至尊巨神"));
        findViewById(R.id.card_data_index_2_1_4_0).setOnClickListener(v -> selectCardDataByName("幻影蛇"));
        findViewById(R.id.card_data_index_2_1_4_1).setOnClickListener(v -> selectCardDataByName("羽翼幻影蛇"));
        findViewById(R.id.card_data_index_2_1_4_2).setOnClickListener(v -> selectCardDataByName("金盔幻影蛇"));
        findViewById(R.id.card_data_index_2_1_5_0).setOnClickListener(v -> selectCardDataByName("全能糖球投手"));
        findViewById(R.id.card_data_index_2_1_5_1).setOnClickListener(v -> selectCardDataByName("水果糖全能投手"));
        findViewById(R.id.card_data_index_2_1_5_2).setOnClickListener(v -> selectCardDataByName("可可糖全能投手"));
        findViewById(R.id.card_data_index_2_2_1_0).setOnClickListener(v -> selectCardDataByName("煮蛋器投手"));
        findViewById(R.id.card_data_index_2_2_1_1).setOnClickListener(v -> selectCardDataByName("威力煮蛋器"));
        findViewById(R.id.card_data_index_2_2_1_2).setOnClickListener(v -> selectCardDataByName("强袭煮蛋器"));
        findViewById(R.id.card_data_index_2_2_2_0).setOnClickListener(v -> selectCardDataByName("冰煮蛋器"));
        findViewById(R.id.card_data_index_2_2_2_1).setOnClickListener(v -> selectCardDataByName("节能冰煮蛋器"));
        findViewById(R.id.card_data_index_2_2_2_2).setOnClickListener(v -> selectCardDataByName("冰河煮蛋器"));
        findViewById(R.id.card_data_index_2_2_3_0).setOnClickListener(v -> selectCardDataByName("双鱼座精灵"));
        findViewById(R.id.card_data_index_2_2_3_1).setOnClickListener(v -> selectCardDataByName("双鱼座战将"));
        findViewById(R.id.card_data_index_2_2_3_2).setOnClickListener(v -> selectCardDataByName("双鱼座星宿"));
        findViewById(R.id.card_data_index_2_2_4_0).setOnClickListener(v -> selectCardDataByName("弹弹鸡"));
        findViewById(R.id.card_data_index_2_2_4_1).setOnClickListener(v -> selectCardDataByName("寒冰弹弹鸡"));
        findViewById(R.id.card_data_index_2_2_4_2).setOnClickListener(v -> selectCardDataByName("月光弹弹鸡"));
        findViewById(R.id.card_data_index_2_2_5_0).setOnClickListener(v -> selectCardDataByName("索尔神使"));
        findViewById(R.id.card_data_index_2_2_5_1).setOnClickListener(v -> selectCardDataByName("索尔圣神"));
        findViewById(R.id.card_data_index_2_2_5_2).setOnClickListener(v -> selectCardDataByName("雷神·索尔"));
        findViewById(R.id.card_data_index_2_2_5_3).setOnClickListener(v -> selectCardDataByName("至尊雷神"));
        findViewById(R.id.card_data_index_2_2_6_0).setOnClickListener(v -> selectCardDataByName("机械汪"));
        findViewById(R.id.card_data_index_2_2_6_1).setOnClickListener(v -> selectCardDataByName("改装机械汪"));
        findViewById(R.id.card_data_index_2_2_6_2).setOnClickListener(v -> selectCardDataByName("自律机械汪"));
        findViewById(R.id.card_data_index_2_2_7_0).setOnClickListener(v -> selectCardDataByName("投弹猪"));
        findViewById(R.id.card_data_index_2_2_7_1).setOnClickListener(v -> selectCardDataByName("獠牙投弹猪"));
        findViewById(R.id.card_data_index_2_2_7_2).setOnClickListener(v -> selectCardDataByName("振金投弹猪"));
        findViewById(R.id.card_data_index_2_2_8_0).setOnClickListener(v -> selectCardDataByName("雪糕投手"));
        findViewById(R.id.card_data_index_2_2_8_1).setOnClickListener(v -> selectCardDataByName("麦旋风投手"));
        findViewById(R.id.card_data_index_2_2_8_2).setOnClickListener(v -> selectCardDataByName("水果雪芭投手"));
        findViewById(R.id.card_data_index_2_2_9_0).setOnClickListener(v -> selectCardDataByName("飞鱼喵"));
        findViewById(R.id.card_data_index_2_2_9_1).setOnClickListener(v -> selectCardDataByName("河滨飞鱼喵"));
        findViewById(R.id.card_data_index_2_2_9_2).setOnClickListener(v -> selectCardDataByName("深海飞鱼喵"));
        findViewById(R.id.card_data_index_2_2_10_0).setOnClickListener(v -> selectCardDataByName("壮壮牛"));
        findViewById(R.id.card_data_index_2_2_10_1).setOnClickListener(v -> selectCardDataByName("蛮力壮壮牛"));
        findViewById(R.id.card_data_index_2_2_10_2).setOnClickListener(v -> selectCardDataByName("乾坤壮壮牛"));
        findViewById(R.id.card_data_index_2_2_11_0).setOnClickListener(v -> selectCardDataByName("烤蜥蜴投手"));
        findViewById(R.id.card_data_index_2_2_11_1).setOnClickListener(v -> selectCardDataByName("坚果蜥蜴投手"));
        findViewById(R.id.card_data_index_2_2_11_2).setOnClickListener(v -> selectCardDataByName("花椒蜥蜴投手"));
        findViewById(R.id.card_data_index_2_2_12_0).setOnClickListener(v -> selectCardDataByName("投篮虎"));
        findViewById(R.id.card_data_index_2_2_12_1).setOnClickListener(v -> selectCardDataByName("职业投篮虎"));
        findViewById(R.id.card_data_index_2_2_12_2).setOnClickListener(v -> selectCardDataByName("球星投篮虎"));
        findViewById(R.id.card_data_index_2_2_13_0).setOnClickListener(v -> selectCardDataByName("钵钵鸡"));
        findViewById(R.id.card_data_index_2_2_13_1).setOnClickListener(v -> selectCardDataByName("飘香钵钵鸡"));
        findViewById(R.id.card_data_index_2_2_13_2).setOnClickListener(v -> selectCardDataByName("川香钵钵鸡"));
        findViewById(R.id.card_data_index_2_3_1_0).setOnClickListener(v -> selectCardDataByName("色拉投手"));
        findViewById(R.id.card_data_index_2_3_1_1).setOnClickListener(v -> selectCardDataByName("果蔬色拉投手"));
        findViewById(R.id.card_data_index_2_3_1_2).setOnClickListener(v -> selectCardDataByName("凯撒色拉投手"));
        findViewById(R.id.card_data_index_2_3_2_0).setOnClickListener(v -> selectCardDataByName("巧克力投手"));
        findViewById(R.id.card_data_index_2_3_2_1).setOnClickListener(v -> selectCardDataByName("浓情巧克力投手"));
        findViewById(R.id.card_data_index_2_3_2_2).setOnClickListener(v -> selectCardDataByName("脆心巧克力投手"));
        findViewById(R.id.card_data_index_2_3_3_0).setOnClickListener(v -> selectCardDataByName("臭豆腐投手"));
        findViewById(R.id.card_data_index_2_3_3_1).setOnClickListener(v -> selectCardDataByName("什锦臭豆腐投手"));
        findViewById(R.id.card_data_index_2_3_3_2).setOnClickListener(v -> selectCardDataByName("铁板臭豆腐投手"));
        findViewById(R.id.card_data_index_2_3_4_0).setOnClickListener(v -> selectCardDataByName("8周年蛋糕"));
        findViewById(R.id.card_data_index_2_3_4_1).setOnClickListener(v -> selectCardDataByName("8周年慕斯"));
        findViewById(R.id.card_data_index_2_3_4_2).setOnClickListener(v -> selectCardDataByName("8周年红丝绒"));
        findViewById(R.id.card_data_index_3_1_1_0).setOnClickListener(v -> selectCardDataByName("炭烧海星"));
        findViewById(R.id.card_data_index_3_1_1_1).setOnClickListener(v -> selectCardDataByName("芝士焗海星"));
        findViewById(R.id.card_data_index_3_1_1_2).setOnClickListener(v -> selectCardDataByName("芥末海星刺身"));
        findViewById(R.id.card_data_index_3_1_2_0).setOnClickListener(v -> selectCardDataByName("猪猪料理机"));
        findViewById(R.id.card_data_index_3_1_2_1).setOnClickListener(v -> selectCardDataByName("猪猪搅拌机"));
        findViewById(R.id.card_data_index_3_1_2_2).setOnClickListener(v -> selectCardDataByName("猪猪破壁机"));
        findViewById(R.id.card_data_index_3_1_3_0).setOnClickListener(v -> selectCardDataByName("陀螺喵"));
        findViewById(R.id.card_data_index_3_1_3_1).setOnClickListener(v -> selectCardDataByName("极光陀螺喵"));
        findViewById(R.id.card_data_index_3_1_3_2).setOnClickListener(v -> selectCardDataByName("金翼陀螺喵"));
        findViewById(R.id.card_data_index_3_1_4_0).setOnClickListener(v -> selectCardDataByName("哈迪斯神使"));
        findViewById(R.id.card_data_index_3_1_4_1).setOnClickListener(v -> selectCardDataByName("哈迪斯圣神"));
        findViewById(R.id.card_data_index_3_1_4_2).setOnClickListener(v -> selectCardDataByName("冥神·哈迪斯"));
        findViewById(R.id.card_data_index_3_1_4_3).setOnClickListener(v -> selectCardDataByName("至尊冥神"));
        findViewById(R.id.card_data_index_3_1_5_0).setOnClickListener(v -> selectCardDataByName("查克拉兔"));
        findViewById(R.id.card_data_index_3_1_5_1).setOnClickListener(v -> selectCardDataByName("上忍查克拉兔"));
        findViewById(R.id.card_data_index_3_1_5_2).setOnClickListener(v -> selectCardDataByName("影级查克拉兔"));
        findViewById(R.id.card_data_index_3_2_1_0).setOnClickListener(v -> selectCardDataByName("厨师虎"));
        findViewById(R.id.card_data_index_3_2_1_1).setOnClickListener(v -> selectCardDataByName("银牌厨师虎"));
        findViewById(R.id.card_data_index_3_2_1_2).setOnClickListener(v -> selectCardDataByName("金牌厨师虎"));
        findViewById(R.id.card_data_index_3_2_2_0).setOnClickListener(v -> selectCardDataByName("星星兔"));
        findViewById(R.id.card_data_index_3_2_2_1).setOnClickListener(v -> selectCardDataByName("科技星星兔"));
        findViewById(R.id.card_data_index_3_2_2_2).setOnClickListener(v -> selectCardDataByName("宇宙星星兔"));
        findViewById(R.id.card_data_index_3_2_3_0).setOnClickListener(v -> selectCardDataByName("坚果爆炒机"));
        findViewById(R.id.card_data_index_3_2_3_1).setOnClickListener(v -> selectCardDataByName("橡子搅拌机"));
        findViewById(R.id.card_data_index_3_2_3_2).setOnClickListener(v -> selectCardDataByName("松塔爆破机"));
        findViewById(R.id.card_data_index_3_2_4_0).setOnClickListener(v -> selectCardDataByName("里格神使"));
        findViewById(R.id.card_data_index_3_2_4_1).setOnClickListener(v -> selectCardDataByName("里格圣神"));
        findViewById(R.id.card_data_index_3_2_4_2).setOnClickListener(v -> selectCardDataByName("守护神·里格"));
        findViewById(R.id.card_data_index_3_2_4_3).setOnClickListener(v -> selectCardDataByName("至尊守护神"));
        findViewById(R.id.card_data_index_3_2_5_0).setOnClickListener(v -> selectCardDataByName("怪味鱿鱼"));
        findViewById(R.id.card_data_index_3_2_5_1).setOnClickListener(v -> selectCardDataByName("爆汁怪味鱿鱼"));
        findViewById(R.id.card_data_index_3_2_5_2).setOnClickListener(v -> selectCardDataByName("天椒怪味鱿鱼"));
        findViewById(R.id.card_data_index_3_2_6_0).setOnClickListener(v -> selectCardDataByName("烟花虎"));
        findViewById(R.id.card_data_index_3_2_6_1).setOnClickListener(v -> selectCardDataByName("冷光烟花虎"));
        findViewById(R.id.card_data_index_3_2_6_2).setOnClickListener(v -> selectCardDataByName("礼炮烟花虎"));
        findViewById(R.id.card_data_index_3_2_7_0).setOnClickListener(v -> selectCardDataByName("风车龙"));
        findViewById(R.id.card_data_index_3_2_7_1).setOnClickListener(v -> selectCardDataByName("暴击风车龙"));
        findViewById(R.id.card_data_index_3_2_7_2).setOnClickListener(v -> selectCardDataByName("迅猛风车龙"));
        findViewById(R.id.card_data_index_3_3_1_0).setOnClickListener(v -> selectCardDataByName("鲈鱼"));
        findViewById(R.id.card_data_index_3_3_1_1).setOnClickListener(v -> selectCardDataByName("美味鲈鱼"));
        findViewById(R.id.card_data_index_3_3_1_2).setOnClickListener(v -> selectCardDataByName("龙门鲈鱼"));
        findViewById(R.id.card_data_index_3_3_2_0).setOnClickListener(v -> selectCardDataByName("便便汪"));
        findViewById(R.id.card_data_index_3_3_2_1).setOnClickListener(v -> selectCardDataByName("白银便便汪"));
        findViewById(R.id.card_data_index_3_3_2_2).setOnClickListener(v -> selectCardDataByName("黄金便便汪"));
        findViewById(R.id.card_data_index_3_3_3_0).setOnClickListener(v -> selectCardDataByName("烧鸡"));
        findViewById(R.id.card_data_index_3_3_3_1).setOnClickListener(v -> selectCardDataByName("美味烤鸡"));
        findViewById(R.id.card_data_index_3_3_3_2).setOnClickListener(v -> selectCardDataByName("奥尔良烤鸡"));
        findViewById(R.id.card_data_index_3_3_4_0).setOnClickListener(v -> selectCardDataByName("饼干汪"));
        findViewById(R.id.card_data_index_3_3_4_1).setOnClickListener(v -> selectCardDataByName("冲锋饼干汪"));
        findViewById(R.id.card_data_index_3_3_4_2).setOnClickListener(v -> selectCardDataByName("加农饼干汪"));
        findViewById(R.id.card_data_index_3_3_6_0).setOnClickListener(v -> selectCardDataByName("牛角面包"));
        findViewById(R.id.card_data_index_3_3_7_0).setOnClickListener(v -> selectCardDataByName("盾盾汪"));
        findViewById(R.id.card_data_index_3_3_7_1).setOnClickListener(v -> selectCardDataByName("钢化盾盾汪"));
        findViewById(R.id.card_data_index_4_1_1_0).setOnClickListener(v -> selectCardDataByName("火盆"));
        findViewById(R.id.card_data_index_4_1_1_1).setOnClickListener(v -> selectCardDataByName("电子烤盘"));
        findViewById(R.id.card_data_index_4_1_1_2).setOnClickListener(v -> selectCardDataByName("岩烧烤盘"));
        findViewById(R.id.card_data_index_4_1_2_0).setOnClickListener(v -> selectCardDataByName("金牛座精灵"));
        findViewById(R.id.card_data_index_4_1_2_1).setOnClickListener(v -> selectCardDataByName("金牛座战将"));
        findViewById(R.id.card_data_index_4_1_2_2).setOnClickListener(v -> selectCardDataByName("金牛座星宿"));
        findViewById(R.id.card_data_index_4_1_3_0).setOnClickListener(v -> selectCardDataByName("洛基神使"));
        findViewById(R.id.card_data_index_4_1_3_1).setOnClickListener(v -> selectCardDataByName("洛基圣神"));
        findViewById(R.id.card_data_index_4_1_3_2).setOnClickListener(v -> selectCardDataByName("火神·洛基"));
        findViewById(R.id.card_data_index_4_1_3_3).setOnClickListener(v -> selectCardDataByName("至尊火神"));
        findViewById(R.id.card_data_index_4_1_4_0).setOnClickListener(v -> selectCardDataByName("暖炉汪"));
        findViewById(R.id.card_data_index_4_1_4_1).setOnClickListener(v -> selectCardDataByName("高温暖炉汪"));
        findViewById(R.id.card_data_index_4_1_4_2).setOnClickListener(v -> selectCardDataByName("火热暖炉汪"));
        findViewById(R.id.card_data_index_4_1_5_0).setOnClickListener(v -> selectCardDataByName("能量喵"));
        findViewById(R.id.card_data_index_4_1_5_1).setOnClickListener(v -> selectCardDataByName("蓝焰能量喵"));
        findViewById(R.id.card_data_index_4_1_5_2).setOnClickListener(v -> selectCardDataByName("樱红能量喵"));
        findViewById(R.id.card_data_index_4_1_6_0).setOnClickListener(v -> selectCardDataByName("坩埚蛇"));
        findViewById(R.id.card_data_index_4_1_6_1).setOnClickListener(v -> selectCardDataByName("灵力坩埚蛇"));
        findViewById(R.id.card_data_index_4_1_6_2).setOnClickListener(v -> selectCardDataByName("幻术坩埚蛇"));
        findViewById(R.id.card_data_index_4_1_7_0).setOnClickListener(v -> selectCardDataByName("猪猪加强器"));
        findViewById(R.id.card_data_index_4_1_7_1).setOnClickListener(v -> selectCardDataByName("猪猪信号塔"));
        findViewById(R.id.card_data_index_4_1_7_2).setOnClickListener(v -> selectCardDataByName("猪猪发射站"));
        findViewById(R.id.card_data_index_4_1_8_0).setOnClickListener(v -> selectCardDataByName("蓝莓信号塔塔"));
        findViewById(R.id.card_data_index_4_1_9_0).setOnClickListener(v -> selectCardDataByName("美味水果塔"));
        findViewById(R.id.card_data_index_4_1_9_1).setOnClickListener(v -> selectCardDataByName("风车水果塔"));
        findViewById(R.id.card_data_index_4_1_9_2).setOnClickListener(v -> selectCardDataByName("巧克力风车塔"));
        findViewById(R.id.card_data_index_4_1_10_0).setOnClickListener(v -> selectCardDataByName("欧若拉神使"));
        findViewById(R.id.card_data_index_4_1_10_1).setOnClickListener(v -> selectCardDataByName("欧若拉圣神"));
        findViewById(R.id.card_data_index_4_1_10_2).setOnClickListener(v -> selectCardDataByName("曙光女神·欧若拉"));
        findViewById(R.id.card_data_index_4_1_10_3).setOnClickListener(v -> selectCardDataByName("至尊曙光女神"));
        findViewById(R.id.card_data_index_4_2_1_0).setOnClickListener(v -> selectCardDataByName("莓果点心"));
        findViewById(R.id.card_data_index_4_2_1_1).setOnClickListener(v -> selectCardDataByName("薄荷莓果点心"));
        findViewById(R.id.card_data_index_4_2_1_2).setOnClickListener(v -> selectCardDataByName("流心莓果点心"));
        findViewById(R.id.card_data_index_4_2_2_0).setOnClickListener(v -> selectCardDataByName("香料虎"));
        findViewById(R.id.card_data_index_4_2_2_1).setOnClickListener(v -> selectCardDataByName("海洋香料虎"));
        findViewById(R.id.card_data_index_4_2_2_2).setOnClickListener(v -> selectCardDataByName("魔力香料虎"));
        findViewById(R.id.card_data_index_4_2_3_0).setOnClickListener(v -> selectCardDataByName("塔利亚神使"));
        findViewById(R.id.card_data_index_4_2_3_1).setOnClickListener(v -> selectCardDataByName("塔利亚圣神"));
        findViewById(R.id.card_data_index_4_2_3_2).setOnClickListener(v -> selectCardDataByName("宴飨女神·塔利亚"));
        findViewById(R.id.card_data_index_4_2_4_0).setOnClickListener(v -> selectCardDataByName("精灵龙"));
        findViewById(R.id.card_data_index_4_2_4_1).setOnClickListener(v -> selectCardDataByName("蛋筒精灵龙"));
        findViewById(R.id.card_data_index_4_2_4_2).setOnClickListener(v -> selectCardDataByName("樱桃精灵龙"));
        findViewById(R.id.card_data_index_4_2_5_0).setOnClickListener(v -> selectCardDataByName("龙须面"));
        findViewById(R.id.card_data_index_4_2_6_0).setOnClickListener(v -> selectCardDataByName("五谷丰登"));
        findViewById(R.id.card_data_index_4_2_6_1).setOnClickListener(v -> selectCardDataByName("五谷营养餐"));
        findViewById(R.id.card_data_index_4_2_6_2).setOnClickListener(v -> selectCardDataByName("杂粮大丰收"));
        findViewById(R.id.card_data_index_4_2_7_0).setOnClickListener(v -> selectCardDataByName("五行蛇"));
        findViewById(R.id.card_data_index_4_2_7_1).setOnClickListener(v -> selectCardDataByName("通灵五行蛇"));
        findViewById(R.id.card_data_index_4_2_7_2).setOnClickListener(v -> selectCardDataByName("泰斗五行蛇"));
        findViewById(R.id.card_data_index_4_2_8_0).setOnClickListener(v -> selectCardDataByName("弗雷神使"));
        findViewById(R.id.card_data_index_4_2_8_1).setOnClickListener(v -> selectCardDataByName("弗雷圣神"));
        findViewById(R.id.card_data_index_4_2_8_2).setOnClickListener(v -> selectCardDataByName("丰饶神·弗雷"));
        findViewById(R.id.card_data_index_4_2_8_3).setOnClickListener(v -> selectCardDataByName("至尊丰饶神"));
        findViewById(R.id.card_data_index_4_2_9_0).setOnClickListener(v -> selectCardDataByName("加速榨汁机"));
        findViewById(R.id.card_data_index_4_2_9_1).setOnClickListener(v -> selectCardDataByName("苹果榨汁机"));
        findViewById(R.id.card_data_index_4_2_9_2).setOnClickListener(v -> selectCardDataByName("大菠萝榨汁机"));
        findViewById(R.id.card_data_index_4_2_10_0).setOnClickListener(v -> selectCardDataByName("魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_10_1).setOnClickListener(v -> selectCardDataByName("青木魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_10_2).setOnClickListener(v -> selectCardDataByName("凤羽魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_11_0).setOnClickListener(v -> selectCardDataByName("炎焱兔"));
        findViewById(R.id.card_data_index_4_2_11_1).setOnClickListener(v -> selectCardDataByName("火火炎焱兔"));
        findViewById(R.id.card_data_index_4_2_11_2).setOnClickListener(v -> selectCardDataByName("燚燚炎焱兔"));
        findViewById(R.id.card_data_index_4_3_1_0).setOnClickListener(v -> selectCardDataByName("11周年美食盒子"));
        findViewById(R.id.card_data_index_5_1_1_0).setOnClickListener(v -> selectCardDataByName("小火炉"));
        findViewById(R.id.card_data_index_5_1_1_1).setOnClickListener(v -> selectCardDataByName("日光炉"));
        findViewById(R.id.card_data_index_5_1_1_2).setOnClickListener(v -> selectCardDataByName("太阳能高效炉"));
        findViewById(R.id.card_data_index_5_1_2_0).setOnClickListener(v -> selectCardDataByName("大火炉"));
        findViewById(R.id.card_data_index_5_1_2_1).setOnClickListener(v -> selectCardDataByName("高能火炉"));
        findViewById(R.id.card_data_index_5_1_2_2).setOnClickListener(v -> selectCardDataByName("超能燃气炉"));
        findViewById(R.id.card_data_index_5_1_3_0).setOnClickListener(v -> selectCardDataByName("酒杯灯"));
        findViewById(R.id.card_data_index_5_1_3_1).setOnClickListener(v -> selectCardDataByName("节能灯"));
        findViewById(R.id.card_data_index_5_1_3_2).setOnClickListener(v -> selectCardDataByName("高效节能灯"));
        findViewById(R.id.card_data_index_5_1_4_0).setOnClickListener(v -> selectCardDataByName("双子座精灵"));
        findViewById(R.id.card_data_index_5_1_4_1).setOnClickListener(v -> selectCardDataByName("双子座战将"));
        findViewById(R.id.card_data_index_5_1_4_2).setOnClickListener(v -> selectCardDataByName("双子座星宿"));
        findViewById(R.id.card_data_index_5_1_5_0).setOnClickListener(v -> selectCardDataByName("咕咕鸡"));
        findViewById(R.id.card_data_index_5_1_5_1).setOnClickListener(v -> selectCardDataByName("萤火咕咕鸡"));
        findViewById(R.id.card_data_index_5_1_5_2).setOnClickListener(v -> selectCardDataByName("梦幻咕咕鸡"));
        findViewById(R.id.card_data_index_5_1_6_0).setOnClickListener(v -> selectCardDataByName("暖暖鸡"));
        findViewById(R.id.card_data_index_5_1_6_1).setOnClickListener(v -> selectCardDataByName("焰羽暖暖鸡"));
        findViewById(R.id.card_data_index_5_1_6_2).setOnClickListener(v -> selectCardDataByName("日耀暖暖鸡"));
        findViewById(R.id.card_data_index_5_1_7_0).setOnClickListener(v -> selectCardDataByName("阿波罗神使"));
        findViewById(R.id.card_data_index_5_1_7_1).setOnClickListener(v -> selectCardDataByName("阿波罗圣神"));
        findViewById(R.id.card_data_index_5_1_7_2).setOnClickListener(v -> selectCardDataByName("太阳神·阿波罗"));
        findViewById(R.id.card_data_index_5_1_7_3).setOnClickListener(v -> selectCardDataByName("至尊太阳神"));
        findViewById(R.id.card_data_index_5_1_8_0).setOnClickListener(v -> selectCardDataByName("7周年蜡烛"));
        findViewById(R.id.card_data_index_5_1_9_0).setOnClickListener(v -> selectCardDataByName("火焰牛"));
        findViewById(R.id.card_data_index_5_1_9_1).setOnClickListener(v -> selectCardDataByName("幽蓝火焰牛"));
        findViewById(R.id.card_data_index_5_1_9_2).setOnClickListener(v -> selectCardDataByName("幻紫火焰牛"));
        findViewById(R.id.card_data_index_5_1_10_0).setOnClickListener(v -> selectCardDataByName("花火龙"));
        findViewById(R.id.card_data_index_5_1_10_1).setOnClickListener(v -> selectCardDataByName("灼灼花火龙"));
        findViewById(R.id.card_data_index_5_1_10_2).setOnClickListener(v -> selectCardDataByName("炽焰花火龙"));
        findViewById(R.id.card_data_index_5_1_11_0).setOnClickListener(v -> selectCardDataByName("蛇羹煲"));
        findViewById(R.id.card_data_index_5_1_11_1).setOnClickListener(v -> selectCardDataByName("厨圣蛇羹煲"));
        findViewById(R.id.card_data_index_5_1_11_2).setOnClickListener(v -> selectCardDataByName("帝王蛇羹煲"));
        findViewById(R.id.card_data_index_5_2_1_0).setOnClickListener(v -> selectCardDataByName("钱罐猪"));
        findViewById(R.id.card_data_index_5_2_1_1).setOnClickListener(v -> selectCardDataByName("彩晶钱罐猪"));
        findViewById(R.id.card_data_index_5_2_1_2).setOnClickListener(v -> selectCardDataByName("招财钱罐猪"));
        findViewById(R.id.card_data_index_5_2_2_0).setOnClickListener(v -> selectCardDataByName("罐罐牛"));
        findViewById(R.id.card_data_index_5_2_2_1).setOnClickListener(v -> selectCardDataByName("五香罐罐牛"));
        findViewById(R.id.card_data_index_5_2_2_2).setOnClickListener(v -> selectCardDataByName("香辣罐罐牛"));
        findViewById(R.id.card_data_index_5_2_3_0).setOnClickListener(v -> selectCardDataByName("烈火虎"));
        findViewById(R.id.card_data_index_5_2_3_1).setOnClickListener(v -> selectCardDataByName("煤气烈火虎"));
        findViewById(R.id.card_data_index_5_2_3_2).setOnClickListener(v -> selectCardDataByName("燃油烈火虎"));
        findViewById(R.id.card_data_index_6_1_1_0).setOnClickListener(v -> selectCardDataByName("樱桃反弹布丁"));
        findViewById(R.id.card_data_index_6_1_1_1).setOnClickListener(v -> selectCardDataByName("节能反弹布丁"));
        findViewById(R.id.card_data_index_6_1_1_2).setOnClickListener(v -> selectCardDataByName("热量反弹布丁"));
        findViewById(R.id.card_data_index_6_1_2_0).setOnClickListener(v -> selectCardDataByName("艾草粑粑"));
        findViewById(R.id.card_data_index_6_1_3_0).setOnClickListener(v -> selectCardDataByName("布丁汪"));
        findViewById(R.id.card_data_index_6_1_3_1).setOnClickListener(v -> selectCardDataByName("双拼布丁汪"));
        findViewById(R.id.card_data_index_6_1_3_2).setOnClickListener(v -> selectCardDataByName("什锦布丁汪"));
        findViewById(R.id.card_data_index_6_1_4_0).setOnClickListener(v -> selectCardDataByName("凉粉牛"));
        findViewById(R.id.card_data_index_6_1_4_1).setOnClickListener(v -> selectCardDataByName("焦糖凉粉牛"));
        findViewById(R.id.card_data_index_6_1_4_2).setOnClickListener(v -> selectCardDataByName("山楂凉粉牛"));
        findViewById(R.id.card_data_index_6_1_5_0).setOnClickListener(v -> selectCardDataByName("忒提丝神使"));
        findViewById(R.id.card_data_index_6_1_5_1).setOnClickListener(v -> selectCardDataByName("忒提丝圣神"));
        findViewById(R.id.card_data_index_6_1_5_2).setOnClickListener(v -> selectCardDataByName("水神·忒提丝"));
        findViewById(R.id.card_data_index_6_1_5_3).setOnClickListener(v -> selectCardDataByName("至尊水神"));
        findViewById(R.id.card_data_index_6_2_1_0).setOnClickListener(v -> selectCardDataByName("木盘子"));
        findViewById(R.id.card_data_index_6_2_1_1).setOnClickListener(v -> selectCardDataByName("友情木盘子"));
        findViewById(R.id.card_data_index_6_2_1_2).setOnClickListener(v -> selectCardDataByName("坚韧木盘子"));
        findViewById(R.id.card_data_index_6_2_2_0).setOnClickListener(v -> selectCardDataByName("盘盘鸡"));
        findViewById(R.id.card_data_index_6_2_2_1).setOnClickListener(v -> selectCardDataByName("王者盘盘鸡"));
        findViewById(R.id.card_data_index_6_2_2_2).setOnClickListener(v -> selectCardDataByName("超神盘盘鸡"));
        findViewById(R.id.card_data_index_6_2_3_0).setOnClickListener(v -> selectCardDataByName("猫猫盘"));
        findViewById(R.id.card_data_index_6_2_3_1).setOnClickListener(v -> selectCardDataByName("光能猫猫盘"));
        findViewById(R.id.card_data_index_6_2_3_2).setOnClickListener(v -> selectCardDataByName("无敌猫猫盘"));
        findViewById(R.id.card_data_index_6_2_4_0).setOnClickListener(v -> selectCardDataByName("魔法软糖"));
        findViewById(R.id.card_data_index_6_2_4_1).setOnClickListener(v -> selectCardDataByName("魔法师软糖"));
        findViewById(R.id.card_data_index_6_2_4_2).setOnClickListener(v -> selectCardDataByName("魔导士软糖"));
        findViewById(R.id.card_data_index_6_2_5_0).setOnClickListener(v -> selectCardDataByName("棉花糖"));
        findViewById(R.id.card_data_index_6_2_5_1).setOnClickListener(v -> selectCardDataByName("草莓棉花糖"));
        findViewById(R.id.card_data_index_6_2_5_2).setOnClickListener(v -> selectCardDataByName("彩虹棉花糖"));
        findViewById(R.id.card_data_index_6_2_6_0).setOnClickListener(v -> selectCardDataByName("苏打气泡"));
        findViewById(R.id.card_data_index_6_2_6_1).setOnClickListener(v -> selectCardDataByName("五彩香皂泡泡"));
        findViewById(R.id.card_data_index_6_2_7_0).setOnClickListener(v -> selectCardDataByName("麦芽糖"));
        findViewById(R.id.card_data_index_6_2_7_1).setOnClickListener(v -> selectCardDataByName("莓莓麦芽糖"));
        findViewById(R.id.card_data_index_7_1_1_0).setOnClickListener(v -> selectCardDataByName("糖葫芦炮弹"));
        findViewById(R.id.card_data_index_7_1_1_1).setOnClickListener(v -> selectCardDataByName("水果糖葫芦弹"));
        findViewById(R.id.card_data_index_7_1_1_2).setOnClickListener(v -> selectCardDataByName("七彩糖葫芦弹"));
        findViewById(R.id.card_data_index_7_1_2_0).setOnClickListener(v -> selectCardDataByName("跳跳鸡"));
        findViewById(R.id.card_data_index_7_1_2_1).setOnClickListener(v -> selectCardDataByName("动感跳跳鸡"));
        findViewById(R.id.card_data_index_7_1_2_2).setOnClickListener(v -> selectCardDataByName("魔音跳跳鸡"));
        findViewById(R.id.card_data_index_7_1_3_0).setOnClickListener(v -> selectCardDataByName("防空喵"));
        findViewById(R.id.card_data_index_7_1_3_1).setOnClickListener(v -> selectCardDataByName("钢盔防空喵"));
        findViewById(R.id.card_data_index_7_1_3_2).setOnClickListener(v -> selectCardDataByName("狂暴防空喵"));
        findViewById(R.id.card_data_index_7_1_4_0).setOnClickListener(v -> selectCardDataByName("赫丘利神使"));
        findViewById(R.id.card_data_index_7_1_4_1).setOnClickListener(v -> selectCardDataByName("赫丘利圣神"));
        findViewById(R.id.card_data_index_7_1_4_2).setOnClickListener(v -> selectCardDataByName("大力神·赫丘利"));
        findViewById(R.id.card_data_index_7_1_4_3).setOnClickListener(v -> selectCardDataByName("至尊大力神"));
        findViewById(R.id.card_data_index_7_2_1_0).setOnClickListener(v -> selectCardDataByName("香肠"));
        findViewById(R.id.card_data_index_7_2_2_0).setOnClickListener(v -> selectCardDataByName("热狗大炮"));
        findViewById(R.id.card_data_index_7_2_2_1).setOnClickListener(v -> selectCardDataByName("热狗高射炮"));
        findViewById(R.id.card_data_index_7_2_2_2).setOnClickListener(v -> selectCardDataByName("热狗榴弹炮"));
        findViewById(R.id.card_data_index_7_2_3_0).setOnClickListener(v -> selectCardDataByName("弹簧虎"));
        findViewById(R.id.card_data_index_7_2_3_1).setOnClickListener(v -> selectCardDataByName("飞行弹簧虎"));
        findViewById(R.id.card_data_index_7_2_3_2).setOnClickListener(v -> selectCardDataByName("机器弹簧虎"));
        findViewById(R.id.card_data_index_7_2_4_0).setOnClickListener(v -> selectCardDataByName("泡泡龙"));
        findViewById(R.id.card_data_index_7_2_4_1).setOnClickListener(v -> selectCardDataByName("蓝鳍泡泡龙"));
        findViewById(R.id.card_data_index_7_2_4_2).setOnClickListener(v -> selectCardDataByName("彩色泡泡龙"));
        findViewById(R.id.card_data_index_7_2_5_0).setOnClickListener(v -> selectCardDataByName("爱心便当"));
        findViewById(R.id.card_data_index_7_2_6_0).setOnClickListener(v -> selectCardDataByName("梦幻多拿滋"));
        findViewById(R.id.card_data_index_7_2_6_1).setOnClickListener(v -> selectCardDataByName("仙女多拿滋"));
        findViewById(R.id.card_data_index_7_2_6_2).setOnClickListener(v -> selectCardDataByName("女王多拿滋"));
        findViewById(R.id.card_data_index_7_2_7_0).setOnClickListener(v -> selectCardDataByName("埃罗斯神使"));
        findViewById(R.id.card_data_index_7_2_7_1).setOnClickListener(v -> selectCardDataByName("埃罗斯圣神"));
        findViewById(R.id.card_data_index_7_2_7_2).setOnClickListener(v -> selectCardDataByName("恶作剧神·埃罗斯"));
        findViewById(R.id.card_data_index_7_2_7_3).setOnClickListener(v -> selectCardDataByName("至尊恶作剧神"));
        findViewById(R.id.card_data_index_7_2_8_0).setOnClickListener(v -> selectCardDataByName("耗油双菇"));
        findViewById(R.id.card_data_index_7_2_8_1).setOnClickListener(v -> selectCardDataByName("伯爵耗油双菇"));
        findViewById(R.id.card_data_index_7_2_8_2).setOnClickListener(v -> selectCardDataByName("皇家耗油双菇"));
        findViewById(R.id.card_data_index_7_2_9_0).setOnClickListener(v -> selectCardDataByName("奶茶猪"));
        findViewById(R.id.card_data_index_7_2_9_1).setOnClickListener(v -> selectCardDataByName("紫薯奶茶猪"));
        findViewById(R.id.card_data_index_7_2_9_2).setOnClickListener(v -> selectCardDataByName("爆弹奶茶猪"));
        findViewById(R.id.card_data_index_7_2_10_0).setOnClickListener(v -> selectCardDataByName("科技喵"));
        findViewById(R.id.card_data_index_7_2_10_1).setOnClickListener(v -> selectCardDataByName("坦克科技喵"));
        findViewById(R.id.card_data_index_7_2_10_2).setOnClickListener(v -> selectCardDataByName("重坦科技喵"));
        findViewById(R.id.card_data_index_8_1_1_0).setOnClickListener(v -> selectCardDataByName("咖啡喷壶"));
        findViewById(R.id.card_data_index_8_1_1_1).setOnClickListener(v -> selectCardDataByName("香醇咖啡喷壶"));
        findViewById(R.id.card_data_index_8_1_1_2).setOnClickListener(v -> selectCardDataByName("红温咖啡喷壶"));
        findViewById(R.id.card_data_index_8_1_2_0).setOnClickListener(v -> selectCardDataByName("关东煮喷锅"));
        findViewById(R.id.card_data_index_8_1_2_1).setOnClickListener(v -> selectCardDataByName("福袋关东煮喷锅"));
        findViewById(R.id.card_data_index_8_1_2_2).setOnClickListener(v -> selectCardDataByName("海鲜关东煮喷锅"));
        findViewById(R.id.card_data_index_8_1_3_0).setOnClickListener(v -> selectCardDataByName("烈焰龙"));
        findViewById(R.id.card_data_index_8_1_3_1).setOnClickListener(v -> selectCardDataByName("火山烈焰龙"));
        findViewById(R.id.card_data_index_8_1_3_2).setOnClickListener(v -> selectCardDataByName("岩浆烈焰龙"));
        findViewById(R.id.card_data_index_8_1_4_0).setOnClickListener(v -> selectCardDataByName("赫斯提亚神使"));
        findViewById(R.id.card_data_index_8_1_4_1).setOnClickListener(v -> selectCardDataByName("赫斯提亚圣神"));
        findViewById(R.id.card_data_index_8_1_4_2).setOnClickListener(v -> selectCardDataByName("圣火女神·赫斯提亚"));
        findViewById(R.id.card_data_index_8_1_4_3).setOnClickListener(v -> selectCardDataByName("至尊圣火女神"));
        findViewById(R.id.card_data_index_8_2_1_0).setOnClickListener(v -> selectCardDataByName("旋转咖啡喷壶"));
        findViewById(R.id.card_data_index_8_2_1_1).setOnClickListener(v -> selectCardDataByName("节能旋转咖啡壶"));
        findViewById(R.id.card_data_index_8_2_1_2).setOnClickListener(v -> selectCardDataByName("原子咖啡壶"));
        findViewById(R.id.card_data_index_8_2_2_0).setOnClickListener(v -> selectCardDataByName("狮子座精灵"));
        findViewById(R.id.card_data_index_8_2_2_1).setOnClickListener(v -> selectCardDataByName("狮子座战将"));
        findViewById(R.id.card_data_index_8_2_2_2).setOnClickListener(v -> selectCardDataByName("狮子座星宿"));
        findViewById(R.id.card_data_index_8_2_3_0).setOnClickListener(v -> selectCardDataByName("波塞冬神使"));
        findViewById(R.id.card_data_index_8_2_3_1).setOnClickListener(v -> selectCardDataByName("波塞冬圣神"));
        findViewById(R.id.card_data_index_8_2_3_2).setOnClickListener(v -> selectCardDataByName("海神·波塞冬"));
        findViewById(R.id.card_data_index_8_2_3_3).setOnClickListener(v -> selectCardDataByName("至尊海神"));
        findViewById(R.id.card_data_index_8_2_4_0).setOnClickListener(v -> selectCardDataByName("转转鸡"));
        findViewById(R.id.card_data_index_8_2_4_1).setOnClickListener(v -> selectCardDataByName("五彩转转鸡"));
        findViewById(R.id.card_data_index_8_2_4_2).setOnClickListener(v -> selectCardDataByName("王室转转鸡"));
        findViewById(R.id.card_data_index_8_2_5_0).setOnClickListener(v -> selectCardDataByName("可乐汪"));
        findViewById(R.id.card_data_index_8_2_5_1).setOnClickListener(v -> selectCardDataByName("冰摇可乐汪"));
        findViewById(R.id.card_data_index_8_2_5_2).setOnClickListener(v -> selectCardDataByName("星杯可乐汪"));
        findViewById(R.id.card_data_index_8_2_6_0).setOnClickListener(v -> selectCardDataByName("元气牛"));
        findViewById(R.id.card_data_index_8_2_6_1).setOnClickListener(v -> selectCardDataByName("泡泡元气牛"));
        findViewById(R.id.card_data_index_8_2_6_2).setOnClickListener(v -> selectCardDataByName("酷酷元气牛"));
        findViewById(R.id.card_data_index_8_2_7_0).setOnClickListener(v -> selectCardDataByName("巫蛊蛇"));
        findViewById(R.id.card_data_index_8_2_7_1).setOnClickListener(v -> selectCardDataByName("暗黑巫蛊蛇"));
        findViewById(R.id.card_data_index_8_2_7_2).setOnClickListener(v -> selectCardDataByName("秘术巫蛊蛇"));
        findViewById(R.id.card_data_index_9_1_1_0).setOnClickListener(v -> selectCardDataByName("章鱼烧"));
        findViewById(R.id.card_data_index_9_1_1_1).setOnClickListener(v -> selectCardDataByName("两栖章鱼烧"));
        findViewById(R.id.card_data_index_9_1_1_2).setOnClickListener(v -> selectCardDataByName("火影章鱼烧"));
        findViewById(R.id.card_data_index_9_1_2_0).setOnClickListener(v -> selectCardDataByName("巨蟹座精灵"));
        findViewById(R.id.card_data_index_9_1_2_1).setOnClickListener(v -> selectCardDataByName("巨蟹座战将"));
        findViewById(R.id.card_data_index_9_1_2_2).setOnClickListener(v -> selectCardDataByName("巨蟹座星宿"));
        findViewById(R.id.card_data_index_9_1_3_0).setOnClickListener(v -> selectCardDataByName("忍忍鸡"));
        findViewById(R.id.card_data_index_9_1_3_1).setOnClickListener(v -> selectCardDataByName("疾风忍忍鸡"));
        findViewById(R.id.card_data_index_9_1_3_2).setOnClickListener(v -> selectCardDataByName("幻影忍忍鸡"));
        findViewById(R.id.card_data_index_9_1_4_0).setOnClickListener(v -> selectCardDataByName("狄安娜神使"));
        findViewById(R.id.card_data_index_9_1_4_1).setOnClickListener(v -> selectCardDataByName("狄安娜圣神"));
        findViewById(R.id.card_data_index_9_1_4_2).setOnClickListener(v -> selectCardDataByName("月神·狄安娜"));
        findViewById(R.id.card_data_index_9_1_4_3).setOnClickListener(v -> selectCardDataByName("至尊月神"));
        findViewById(R.id.card_data_index_9_1_5_0).setOnClickListener(v -> selectCardDataByName("飞盘汪"));
        findViewById(R.id.card_data_index_9_1_5_1).setOnClickListener(v -> selectCardDataByName("大厨飞盘汪"));
        findViewById(R.id.card_data_index_9_1_5_2).setOnClickListener(v -> selectCardDataByName("名厨飞盘汪"));
        findViewById(R.id.card_data_index_9_1_6_0).setOnClickListener(v -> selectCardDataByName("铁甲飞镖猪"));
        findViewById(R.id.card_data_index_9_1_6_1).setOnClickListener(v -> selectCardDataByName("银甲飞镖猪"));
        findViewById(R.id.card_data_index_9_1_6_2).setOnClickListener(v -> selectCardDataByName("金甲飞镖猪"));
        findViewById(R.id.card_data_index_9_1_7_0).setOnClickListener(v -> selectCardDataByName("海盗兔"));
        findViewById(R.id.card_data_index_9_1_7_1).setOnClickListener(v -> selectCardDataByName("首领海盗兔"));
        findViewById(R.id.card_data_index_9_1_7_2).setOnClickListener(v -> selectCardDataByName("洛克斯海贼兔"));
        findViewById(R.id.card_data_index_9_2_1_0).setOnClickListener(v -> selectCardDataByName("咖喱龙虾炮"));
        findViewById(R.id.card_data_index_9_2_1_1).setOnClickListener(v -> selectCardDataByName("麻辣龙虾炮"));
        findViewById(R.id.card_data_index_9_2_1_2).setOnClickListener(v -> selectCardDataByName("加农龙虾炮"));
        findViewById(R.id.card_data_index_9_2_2_0).setOnClickListener(v -> selectCardDataByName("雅典娜守护"));
        findViewById(R.id.card_data_index_9_2_2_1).setOnClickListener(v -> selectCardDataByName("雅典娜圣衣"));
        findViewById(R.id.card_data_index_9_2_2_2).setOnClickListener(v -> selectCardDataByName("雅典娜光辉"));
        findViewById(R.id.card_data_index_9_2_3_0).setOnClickListener(v -> selectCardDataByName("火箭猪"));
        findViewById(R.id.card_data_index_9_2_3_1).setOnClickListener(v -> selectCardDataByName("运载火箭猪"));
        findViewById(R.id.card_data_index_9_2_3_2).setOnClickListener(v -> selectCardDataByName("反重力火箭猪"));
        findViewById(R.id.card_data_index_9_2_4_0).setOnClickListener(v -> selectCardDataByName("宙斯神使"));
        findViewById(R.id.card_data_index_9_2_4_1).setOnClickListener(v -> selectCardDataByName("宙斯圣神"));
        findViewById(R.id.card_data_index_9_2_4_2).setOnClickListener(v -> selectCardDataByName("天神·宙斯"));
        findViewById(R.id.card_data_index_9_2_4_3).setOnClickListener(v -> selectCardDataByName("至尊天神"));
        findViewById(R.id.card_data_index_9_3_1_0).setOnClickListener(v -> selectCardDataByName("魔法猪"));
        findViewById(R.id.card_data_index_9_3_1_1).setOnClickListener(v -> selectCardDataByName("冰霜魔法猪"));
        findViewById(R.id.card_data_index_9_3_1_2).setOnClickListener(v -> selectCardDataByName("暴雪元素猪"));
        findViewById(R.id.card_data_index_9_3_2_0).setOnClickListener(v -> selectCardDataByName("招财喵"));
        findViewById(R.id.card_data_index_9_3_2_1).setOnClickListener(v -> selectCardDataByName("贵族招财喵"));
        findViewById(R.id.card_data_index_9_3_2_2).setOnClickListener(v -> selectCardDataByName("御守招财喵"));
        findViewById(R.id.card_data_index_9_3_3_0).setOnClickListener(v -> selectCardDataByName("雪球兔"));
        findViewById(R.id.card_data_index_9_3_3_1).setOnClickListener(v -> selectCardDataByName("见习雪球兔"));
        findViewById(R.id.card_data_index_9_3_3_2).setOnClickListener(v -> selectCardDataByName("导师雪球兔"));
        findViewById(R.id.card_data_index_9_3_4_0).setOnClickListener(v -> selectCardDataByName("典伊神使"));
        findViewById(R.id.card_data_index_9_3_4_1).setOnClickListener(v -> selectCardDataByName("典伊圣神"));
        findViewById(R.id.card_data_index_9_3_4_2).setOnClickListener(v -> selectCardDataByName("冰神·典伊"));
        findViewById(R.id.card_data_index_9_3_4_3).setOnClickListener(v -> selectCardDataByName("至尊冰神"));
        findViewById(R.id.card_data_index_9_3_5_0).setOnClickListener(v -> selectCardDataByName("冰晶龙"));
        findViewById(R.id.card_data_index_9_3_5_1).setOnClickListener(v -> selectCardDataByName("四棱冰晶龙"));
        findViewById(R.id.card_data_index_9_3_5_2).setOnClickListener(v -> selectCardDataByName("独角冰晶龙"));
        findViewById(R.id.card_data_index_9_3_6_0).setOnClickListener(v -> selectCardDataByName("冰块冷萃机"));
        findViewById(R.id.card_data_index_9_3_6_1).setOnClickListener(v -> selectCardDataByName("低温冷萃机"));
        findViewById(R.id.card_data_index_9_3_6_2).setOnClickListener(v -> selectCardDataByName("迅捷冷萃机"));
        findViewById(R.id.card_data_index_9_4_1_0).setOnClickListener(v -> selectCardDataByName("鼠鼠蛋糕空投器"));
        findViewById(R.id.card_data_index_9_4_1_1).setOnClickListener(v -> selectCardDataByName("喵博士蛋糕空投器"));
        findViewById(R.id.card_data_index_9_4_2_0).setOnClickListener(v -> selectCardDataByName("风力空投猪"));
        findViewById(R.id.card_data_index_9_4_2_1).setOnClickListener(v -> selectCardDataByName("导弹空投猪"));
        findViewById(R.id.card_data_index_9_4_2_2).setOnClickListener(v -> selectCardDataByName("涡轮空投猪"));
        findViewById(R.id.card_data_index_9_4_3_0).setOnClickListener(v -> selectCardDataByName("电流虎"));
        findViewById(R.id.card_data_index_9_4_3_1).setOnClickListener(v -> selectCardDataByName("磁铁电流虎"));
        findViewById(R.id.card_data_index_9_4_3_2).setOnClickListener(v -> selectCardDataByName("氢能电流虎"));
        findViewById(R.id.card_data_index_10_1_1_0).setOnClickListener(v -> selectCardDataByName("肥牛火锅"));
        findViewById(R.id.card_data_index_10_1_1_1).setOnClickListener(v -> selectCardDataByName("酸汤肥牛锅"));
        findViewById(R.id.card_data_index_10_1_1_2).setOnClickListener(v -> selectCardDataByName("海鲜肥牛锅"));
        findViewById(R.id.card_data_index_10_1_2_0).setOnClickListener(v -> selectCardDataByName("麻辣香锅"));
        findViewById(R.id.card_data_index_10_1_2_1).setOnClickListener(v -> selectCardDataByName("孜然羊肉锅"));
        findViewById(R.id.card_data_index_10_1_2_2).setOnClickListener(v -> selectCardDataByName("酱香鱿鱼锅"));
        findViewById(R.id.card_data_index_10_1_3_0).setOnClickListener(v -> selectCardDataByName("生煎锅"));
        findViewById(R.id.card_data_index_10_1_3_1).setOnClickListener(v -> selectCardDataByName("水煎包锅"));
        findViewById(R.id.card_data_index_10_1_3_2).setOnClickListener(v -> selectCardDataByName("驴肉火烧锅"));
        findViewById(R.id.card_data_index_10_1_4_0).setOnClickListener(v -> selectCardDataByName("铛铛虎"));
        findViewById(R.id.card_data_index_10_1_4_1).setOnClickListener(v -> selectCardDataByName("速热铛铛虎"));
        findViewById(R.id.card_data_index_10_1_4_2).setOnClickListener(v -> selectCardDataByName("微波铛铛虎"));
        findViewById(R.id.card_data_index_10_1_5_0).setOnClickListener(v -> selectCardDataByName("祝融神使"));
        findViewById(R.id.card_data_index_10_1_5_1).setOnClickListener(v -> selectCardDataByName("祝融圣神"));
        findViewById(R.id.card_data_index_10_1_5_2).setOnClickListener(v -> selectCardDataByName("赤帝·祝融"));
        findViewById(R.id.card_data_index_10_1_5_3).setOnClickListener(v -> selectCardDataByName("至尊赤帝"));
        findViewById(R.id.card_data_index_10_1_6_0).setOnClickListener(v -> selectCardDataByName("糖炒栗子"));
        findViewById(R.id.card_data_index_10_1_6_1).setOnClickListener(v -> selectCardDataByName("开口笑栗子"));
        findViewById(R.id.card_data_index_10_1_6_2).setOnClickListener(v -> selectCardDataByName("焦香烤栗子"));
        findViewById(R.id.card_data_index_10_1_7_0).setOnClickListener(v -> selectCardDataByName("霜霜蛇"));
        findViewById(R.id.card_data_index_10_1_7_1).setOnClickListener(v -> selectCardDataByName("雪花霜霜蛇"));
        findViewById(R.id.card_data_index_10_1_7_2).setOnClickListener(v -> selectCardDataByName("玄冰霜霜蛇"));
        findViewById(R.id.card_data_index_10_2_1_0).setOnClickListener(v -> selectCardDataByName("汉堡包"));
        findViewById(R.id.card_data_index_10_2_1_1).setOnClickListener(v -> selectCardDataByName("天椒双层堡"));
        findViewById(R.id.card_data_index_10_2_1_2).setOnClickListener(v -> selectCardDataByName("牛肉双黑汉堡"));
        findViewById(R.id.card_data_index_10_2_2_0).setOnClickListener(v -> selectCardDataByName("贪食蛙"));
        findViewById(R.id.card_data_index_10_2_3_0).setOnClickListener(v -> selectCardDataByName("吞噬龙"));
        findViewById(R.id.card_data_index_10_2_3_1).setOnClickListener(v -> selectCardDataByName("幼年吞噬龙"));
        findViewById(R.id.card_data_index_10_2_3_2).setOnClickListener(v -> selectCardDataByName("成年吞噬龙"));
        findViewById(R.id.card_data_index_10_2_4_0).setOnClickListener(v -> selectCardDataByName("香辣年糕蟹"));
        findViewById(R.id.card_data_index_10_2_4_1).setOnClickListener(v -> selectCardDataByName("中辣年糕蟹"));
        findViewById(R.id.card_data_index_10_2_4_2).setOnClickListener(v -> selectCardDataByName("厨神年糕蟹"));
        findViewById(R.id.card_data_index_10_2_5_0).setOnClickListener(v -> selectCardDataByName("混沌神使"));
        findViewById(R.id.card_data_index_10_2_5_1).setOnClickListener(v -> selectCardDataByName("混沌圣神"));
        findViewById(R.id.card_data_index_10_2_5_2).setOnClickListener(v -> selectCardDataByName("上古神·混沌"));
        findViewById(R.id.card_data_index_10_3_1_0).setOnClickListener(v -> selectCardDataByName("新疆炒面"));
        findViewById(R.id.card_data_index_10_3_1_1).setOnClickListener(v -> selectCardDataByName("刀削拉面"));
        findViewById(R.id.card_data_index_10_3_1_2).setOnClickListener(v -> selectCardDataByName("真刀削拉面"));
        findViewById(R.id.card_data_index_10_3_2_0).setOnClickListener(v -> selectCardDataByName("丸子厨师"));
        findViewById(R.id.card_data_index_10_3_3_0).setOnClickListener(v -> selectCardDataByName("功夫汪"));
        findViewById(R.id.card_data_index_10_3_3_1).setOnClickListener(v -> selectCardDataByName("铁皮功夫汪"));
        findViewById(R.id.card_data_index_10_3_3_2).setOnClickListener(v -> selectCardDataByName("金甲功夫汪"));
        findViewById(R.id.card_data_index_10_3_4_0).setOnClickListener(v -> selectCardDataByName("鱼刺"));
        findViewById(R.id.card_data_index_10_3_5_0).setOnClickListener(v -> selectCardDataByName("钢鱼刺"));
        findViewById(R.id.card_data_index_10_3_5_1).setOnClickListener(v -> selectCardDataByName("无座钢鱼刺"));
        findViewById(R.id.card_data_index_10_3_5_2).setOnClickListener(v -> selectCardDataByName("海贼王鱼刺"));
        findViewById(R.id.card_data_index_10_3_6_0).setOnClickListener(v -> selectCardDataByName("糖渍刺梨"));
        findViewById(R.id.card_data_index_10_3_6_1).setOnClickListener(v -> selectCardDataByName("烟熏刺梨"));
        findViewById(R.id.card_data_index_10_3_6_2).setOnClickListener(v -> selectCardDataByName("生腌酸刺梨"));
        findViewById(R.id.card_data_index_10_4_1_0).setOnClickListener(v -> selectCardDataByName("蜂蜜史莱姆"));
        findViewById(R.id.card_data_index_10_4_1_1).setOnClickListener(v -> selectCardDataByName("蜂糖史莱姆"));
        findViewById(R.id.card_data_index_10_4_1_2).setOnClickListener(v -> selectCardDataByName("蜂王浆史莱姆"));
        findViewById(R.id.card_data_index_11_1_1_0).setOnClickListener(v -> selectCardDataByName("小笼包"));
        findViewById(R.id.card_data_index_11_1_2_0).setOnClickListener(v -> selectCardDataByName("双层小笼包"));
        findViewById(R.id.card_data_index_11_1_3_0).setOnClickListener(v -> selectCardDataByName("三向小笼包"));
        findViewById(R.id.card_data_index_11_1_4_0).setOnClickListener(v -> selectCardDataByName("机枪小笼包"));
        findViewById(R.id.card_data_index_11_1_4_1).setOnClickListener(v -> selectCardDataByName("竹筒机枪小笼包"));
        findViewById(R.id.card_data_index_11_1_4_2).setOnClickListener(v -> selectCardDataByName("格林机枪笼包"));
        findViewById(R.id.card_data_index_11_1_5_0).setOnClickListener(v -> selectCardDataByName("冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_6_0).setOnClickListener(v -> selectCardDataByName("双层冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_7_0).setOnClickListener(v -> selectCardDataByName("三向冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_8_0).setOnClickListener(v -> selectCardDataByName("机枪冰冻小笼包"));
        findViewById(R.id.card_data_index_11_1_9_0).setOnClickListener(v -> selectCardDataByName("国王小笼包"));
        findViewById(R.id.card_data_index_11_1_9_1).setOnClickListener(v -> selectCardDataByName("红心国王小笼包"));
        findViewById(R.id.card_data_index_11_1_9_2).setOnClickListener(v -> selectCardDataByName("黑心国王小笼包"));
        findViewById(R.id.card_data_index_11_1_10_0).setOnClickListener(v -> selectCardDataByName("三向国王小笼包"));
        findViewById(R.id.card_data_index_11_1_10_1).setOnClickListener(v -> selectCardDataByName("红心三向国王小笼包"));
        findViewById(R.id.card_data_index_11_1_10_2).setOnClickListener(v -> selectCardDataByName("黑心三向国王小笼包"));
        findViewById(R.id.card_data_index_11_1_11_0).setOnClickListener(v -> selectCardDataByName("贵族小笼包"));
        findViewById(R.id.card_data_index_11_1_12_0).setOnClickListener(v -> selectCardDataByName("玉蜀黍"));
        findViewById(R.id.card_data_index_11_1_12_1).setOnClickListener(v -> selectCardDataByName("奶油玉米机枪"));
        findViewById(R.id.card_data_index_11_1_12_2).setOnClickListener(v -> selectCardDataByName("加农玉米机枪"));
        findViewById(R.id.card_data_index_11_1_13_0).setOnClickListener(v -> selectCardDataByName("包包龙"));
        findViewById(R.id.card_data_index_11_1_13_1).setOnClickListener(v -> selectCardDataByName("懵懂包包龙"));
        findViewById(R.id.card_data_index_11_1_13_2).setOnClickListener(v -> selectCardDataByName("觉醒包包龙"));
        findViewById(R.id.card_data_index_11_1_14_0).setOnClickListener(v -> selectCardDataByName("咖啡杯"));
        findViewById(R.id.card_data_index_11_1_14_1).setOnClickListener(v -> selectCardDataByName("花纹咖啡杯"));
        findViewById(R.id.card_data_index_11_1_14_2).setOnClickListener(v -> selectCardDataByName("骨瓷咖啡杯"));
        findViewById(R.id.card_data_index_11_1_15_0).setOnClickListener(v -> selectCardDataByName("水上茶杯"));
        findViewById(R.id.card_data_index_11_1_16_0).setOnClickListener(v -> selectCardDataByName("激光汪"));
        findViewById(R.id.card_data_index_11_1_16_1).setOnClickListener(v -> selectCardDataByName("高频激光汪"));
        findViewById(R.id.card_data_index_11_1_16_2).setOnClickListener(v -> selectCardDataByName("超频激光汪"));
        findViewById(R.id.card_data_index_11_2_1_0).setOnClickListener(v -> selectCardDataByName("天蝎座精灵"));
        findViewById(R.id.card_data_index_11_2_1_1).setOnClickListener(v -> selectCardDataByName("天蝎座战将"));
        findViewById(R.id.card_data_index_11_2_2_0).setOnClickListener(v -> selectCardDataByName("工程猪"));
        findViewById(R.id.card_data_index_11_2_2_1).setOnClickListener(v -> selectCardDataByName("高级工程猪"));
        findViewById(R.id.card_data_index_11_2_2_2).setOnClickListener(v -> selectCardDataByName("科幻工程猪"));
        findViewById(R.id.card_data_index_11_2_3_0).setOnClickListener(v -> selectCardDataByName("双刃蛇"));
        findViewById(R.id.card_data_index_11_2_3_1).setOnClickListener(v -> selectCardDataByName("武士双刃蛇"));
        findViewById(R.id.card_data_index_11_2_3_2).setOnClickListener(v -> selectCardDataByName("游侠双刃蛇"));
        findViewById(R.id.card_data_index_11_2_4_0).setOnClickListener(v -> selectCardDataByName("元素蛇"));
        findViewById(R.id.card_data_index_11_2_4_1).setOnClickListener(v -> selectCardDataByName("烈暴元素蛇"));
        findViewById(R.id.card_data_index_11_2_4_2).setOnClickListener(v -> selectCardDataByName("流星元素蛇"));
        findViewById(R.id.card_data_index_11_2_5_0).setOnClickListener(v -> selectCardDataByName("回旋虎"));
        findViewById(R.id.card_data_index_11_2_5_1).setOnClickListener(v -> selectCardDataByName("高能回旋虎"));
        findViewById(R.id.card_data_index_11_2_5_2).setOnClickListener(v -> selectCardDataByName("宗师回旋虎"));
        findViewById(R.id.card_data_index_11_2_6_0).setOnClickListener(v -> selectCardDataByName("大师兔"));
        findViewById(R.id.card_data_index_11_2_6_1).setOnClickListener(v -> selectCardDataByName("黑带大师兔"));
        findViewById(R.id.card_data_index_11_2_6_2).setOnClickListener(v -> selectCardDataByName("功夫大师兔"));
        findViewById(R.id.card_data_index_11_2_7_0).setOnClickListener(v -> selectCardDataByName("15周年猴赛雷"));
        findViewById(R.id.card_data_index_11_2_7_1).setOnClickListener(v -> selectCardDataByName("捕风手猴赛雷"));
        findViewById(R.id.card_data_index_11_2_7_2).setOnClickListener(v -> selectCardDataByName("艺术家猴赛雷"));
        findViewById(R.id.card_data_index_11_2_8_0).setOnClickListener(v -> selectCardDataByName("赖皮蛇"));
        findViewById(R.id.card_data_index_11_2_8_1).setOnClickListener(v -> selectCardDataByName("宝石赖皮蛇"));
        findViewById(R.id.card_data_index_11_2_8_2).setOnClickListener(v -> selectCardDataByName("大王赖皮蛇"));
        findViewById(R.id.card_data_index_11_2_9_0).setOnClickListener(v -> selectCardDataByName("迷你披萨炉"));
        findViewById(R.id.card_data_index_11_2_9_1).setOnClickListener(v -> selectCardDataByName("香脆披萨炉"));
        findViewById(R.id.card_data_index_11_2_9_2).setOnClickListener(v -> selectCardDataByName("拉丝披萨炉"));
        findViewById(R.id.card_data_index_11_3_1_0).setOnClickListener(v -> selectCardDataByName("焦油喷壶"));
        findViewById(R.id.card_data_index_11_3_1_1).setOnClickListener(v -> selectCardDataByName("强力焦油喷壶"));
        findViewById(R.id.card_data_index_11_3_1_2).setOnClickListener(v -> selectCardDataByName("祝融喷壶"));
        findViewById(R.id.card_data_index_11_3_2_0).setOnClickListener(v -> selectCardDataByName("喷壶汪"));
        findViewById(R.id.card_data_index_11_3_2_1).setOnClickListener(v -> selectCardDataByName("高压喷壶汪"));
        findViewById(R.id.card_data_index_11_3_2_2).setOnClickListener(v -> selectCardDataByName("连射喷壶汪"));
        findViewById(R.id.card_data_index_11_3_3_0).setOnClickListener(v -> selectCardDataByName("派派鸡"));
        findViewById(R.id.card_data_index_11_3_3_1).setOnClickListener(v -> selectCardDataByName("少校派派鸡"));
        findViewById(R.id.card_data_index_11_3_3_2).setOnClickListener(v -> selectCardDataByName("将军派派鸡"));
        findViewById(R.id.card_data_index_11_3_4_0).setOnClickListener(v -> selectCardDataByName("小猪米花机"));
        findViewById(R.id.card_data_index_11_3_4_1).setOnClickListener(v -> selectCardDataByName("巧克力米花机"));
        findViewById(R.id.card_data_index_11_3_4_2).setOnClickListener(v -> selectCardDataByName("金箔米花机"));
        findViewById(R.id.card_data_index_11_3_5_0).setOnClickListener(v -> selectCardDataByName("喷气牛"));
        findViewById(R.id.card_data_index_11_3_5_1).setOnClickListener(v -> selectCardDataByName("恒温喷气牛"));
        findViewById(R.id.card_data_index_11_3_5_2).setOnClickListener(v -> selectCardDataByName("无限喷气牛"));
        findViewById(R.id.card_data_index_11_3_6_0).setOnClickListener(v -> selectCardDataByName("卖萌喵"));
        findViewById(R.id.card_data_index_11_3_6_1).setOnClickListener(v -> selectCardDataByName("核能卖萌喵"));
        findViewById(R.id.card_data_index_11_3_6_2).setOnClickListener(v -> selectCardDataByName("原子卖萌喵"));
        findViewById(R.id.card_data_index_11_3_7_0).setOnClickListener(v -> selectCardDataByName("奥丁神使"));
        findViewById(R.id.card_data_index_11_3_7_1).setOnClickListener(v -> selectCardDataByName("奥丁圣神"));
        findViewById(R.id.card_data_index_11_3_7_2).setOnClickListener(v -> selectCardDataByName("主神·奥丁"));
        findViewById(R.id.card_data_index_11_3_7_3).setOnClickListener(v -> selectCardDataByName("至尊主神"));
        findViewById(R.id.card_data_index_11_3_8_0).setOnClickListener(v -> selectCardDataByName("阴阳蛇"));
        findViewById(R.id.card_data_index_11_3_8_1).setOnClickListener(v -> selectCardDataByName("伯仲阴阳蛇"));
        findViewById(R.id.card_data_index_11_3_8_2).setOnClickListener(v -> selectCardDataByName("龙虎阴阳蛇"));
        findViewById(R.id.card_data_index_11_3_9_0).setOnClickListener(v -> selectCardDataByName("法师蛇"));
        findViewById(R.id.card_data_index_11_3_9_1).setOnClickListener(v -> selectCardDataByName("幽冥法师蛇"));
        findViewById(R.id.card_data_index_11_3_9_2).setOnClickListener(v -> selectCardDataByName("奥术法师蛇"));
        findViewById(R.id.card_data_index_11_3_10_0).setOnClickListener(v -> selectCardDataByName("街头烤肉大师"));
        findViewById(R.id.card_data_index_11_3_10_1).setOnClickListener(v -> selectCardDataByName("户外烤肉大师"));
        findViewById(R.id.card_data_index_11_3_10_2).setOnClickListener(v -> selectCardDataByName("BBQ烤肉大师"));
        findViewById(R.id.card_data_index_11_3_11_0).setOnClickListener(v -> selectCardDataByName("后羿神使"));
        findViewById(R.id.card_data_index_11_3_11_1).setOnClickListener(v -> selectCardDataByName("后羿圣神"));
        findViewById(R.id.card_data_index_11_3_11_2).setOnClickListener(v -> selectCardDataByName("宗布神·后羿"));
        findViewById(R.id.card_data_index_11_3_11_3).setOnClickListener(v -> selectCardDataByName("至尊宗布神"));
        findViewById(R.id.card_data_index_12_1_1_0).setOnClickListener(v -> selectCardDataByName("雷电长棍面包"));
        findViewById(R.id.card_data_index_12_1_1_1).setOnClickListener(v -> selectCardDataByName("节能面包"));
        findViewById(R.id.card_data_index_12_1_1_2).setOnClickListener(v -> selectCardDataByName("负离子面包"));
        findViewById(R.id.card_data_index_12_1_2_0).setOnClickListener(v -> selectCardDataByName("三指兔"));
        findViewById(R.id.card_data_index_12_1_2_1).setOnClickListener(v -> selectCardDataByName("开罗三指兔"));
        findViewById(R.id.card_data_index_12_1_2_2).setOnClickListener(v -> selectCardDataByName("胡夫三指兔"));
        findViewById(R.id.card_data_index_12_1_3_0).setOnClickListener(v -> selectCardDataByName("巧克力大炮"));
        findViewById(R.id.card_data_index_12_1_3_1).setOnClickListener(v -> selectCardDataByName("节能巧克力大炮"));
        findViewById(R.id.card_data_index_12_1_3_2).setOnClickListener(v -> selectCardDataByName("死神大炮"));
        findViewById(R.id.card_data_index_12_1_4_0).setOnClickListener(v -> selectCardDataByName("导弹蛇"));
        findViewById(R.id.card_data_index_12_1_4_1).setOnClickListener(v -> selectCardDataByName("舰地导弹蛇"));
        findViewById(R.id.card_data_index_12_1_4_2).setOnClickListener(v -> selectCardDataByName("洲际导弹蛇"));
        findViewById(R.id.card_data_index_12_1_5_0).setOnClickListener(v -> selectCardDataByName("盖亚神使"));
        findViewById(R.id.card_data_index_12_1_5_1).setOnClickListener(v -> selectCardDataByName("盖亚圣神"));
        findViewById(R.id.card_data_index_12_1_5_2).setOnClickListener(v -> selectCardDataByName("大地女神·盖亚"));
        findViewById(R.id.card_data_index_12_1_5_3).setOnClickListener(v -> selectCardDataByName("至尊大地女神"));
        findViewById(R.id.card_data_index_12_2_1_0).setOnClickListener(v -> selectCardDataByName("可乐炸弹"));
        findViewById(R.id.card_data_index_12_2_1_1).setOnClickListener(v -> selectCardDataByName("云爆可乐弹"));
        findViewById(R.id.card_data_index_12_2_1_2).setOnClickListener(v -> selectCardDataByName("燃烧可乐弹"));
        findViewById(R.id.card_data_index_12_2_2_0).setOnClickListener(v -> selectCardDataByName("酒瓶炸弹"));
        findViewById(R.id.card_data_index_12_2_2_1).setOnClickListener(v -> selectCardDataByName("高爆酒瓶弹"));
        findViewById(R.id.card_data_index_12_2_2_2).setOnClickListener(v -> selectCardDataByName("子母酒瓶弹"));
        findViewById(R.id.card_data_index_12_2_3_0).setOnClickListener(v -> selectCardDataByName("开水壶炸弹"));
        findViewById(R.id.card_data_index_12_2_3_1).setOnClickListener(v -> selectCardDataByName("汽油壶炸弹"));
        findViewById(R.id.card_data_index_12_2_3_2).setOnClickListener(v -> selectCardDataByName("汽油干冰弹"));
        findViewById(R.id.card_data_index_12_2_4_0).setOnClickListener(v -> selectCardDataByName("威士忌炸弹"));
        findViewById(R.id.card_data_index_12_2_4_1).setOnClickListener(v -> selectCardDataByName("水晶威士忌炸弹"));
        findViewById(R.id.card_data_index_12_2_4_2).setOnClickListener(v -> selectCardDataByName("钻石威士忌炸弹"));
        findViewById(R.id.card_data_index_12_2_5_0).setOnClickListener(v -> selectCardDataByName("潘多拉"));
        findViewById(R.id.card_data_index_12_2_5_1).setOnClickListener(v -> selectCardDataByName("潘多拉魔盒"));
        findViewById(R.id.card_data_index_12_2_5_2).setOnClickListener(v -> selectCardDataByName("疫神·潘多拉"));
        findViewById(R.id.card_data_index_12_2_6_0).setOnClickListener(v -> selectCardDataByName("深水炸弹"));
        findViewById(R.id.card_data_index_12_2_7_0).setOnClickListener(v -> selectCardDataByName("爆辣河豚"));
        findViewById(R.id.card_data_index_12_2_7_1).setOnClickListener(v -> selectCardDataByName("朝天椒河豚"));
        findViewById(R.id.card_data_index_12_2_7_2).setOnClickListener(v -> selectCardDataByName("青芥末河豚"));
        findViewById(R.id.card_data_index_12_2_8_0).setOnClickListener(v -> selectCardDataByName("爆竹"));
        findViewById(R.id.card_data_index_12_2_9_0).setOnClickListener(v -> selectCardDataByName("美食烟花普通版"));
        findViewById(R.id.card_data_index_12_2_10_0).setOnClickListener(v -> selectCardDataByName("美食烟花华丽版"));
        findViewById(R.id.card_data_index_12_2_11_0).setOnClickListener(v -> selectCardDataByName("水瓶座精灵"));
        findViewById(R.id.card_data_index_12_2_11_1).setOnClickListener(v -> selectCardDataByName("水瓶座战将"));
        findViewById(R.id.card_data_index_12_2_11_2).setOnClickListener(v -> selectCardDataByName("水瓶座星宿"));
        findViewById(R.id.card_data_index_12_2_12_0).setOnClickListener(v -> selectCardDataByName("雷暴猪"));
        findViewById(R.id.card_data_index_12_2_12_1).setOnClickListener(v -> selectCardDataByName("离子雷暴猪"));
        findViewById(R.id.card_data_index_12_2_12_2).setOnClickListener(v -> selectCardDataByName("电磁雷暴猪"));
        findViewById(R.id.card_data_index_12_2_13_0).setOnClickListener(v -> selectCardDataByName("微波炉爆弹"));
        findViewById(R.id.card_data_index_12_2_13_1).setOnClickListener(v -> selectCardDataByName("星际微波爆弹"));
        findViewById(R.id.card_data_index_12_2_13_2).setOnClickListener(v -> selectCardDataByName("宇宙微波核弹"));
        findViewById(R.id.card_data_index_12_2_14_0).setOnClickListener(v -> selectCardDataByName("玉兔灯笼"));
        findViewById(R.id.card_data_index_12_2_14_1).setOnClickListener(v -> selectCardDataByName("广寒玉兔灯笼"));
        findViewById(R.id.card_data_index_12_2_14_2).setOnClickListener(v -> selectCardDataByName("莲花玉兔灯笼"));
        findViewById(R.id.card_data_index_12_2_15_0).setOnClickListener(v -> selectCardDataByName("爆裂蛇"));
        findViewById(R.id.card_data_index_12_2_15_1).setOnClickListener(v -> selectCardDataByName("凤梨爆裂蛇"));
        findViewById(R.id.card_data_index_12_2_15_2).setOnClickListener(v -> selectCardDataByName("西瓜爆裂蛇"));
        findViewById(R.id.card_data_index_12_2_16_0).setOnClickListener(v -> selectCardDataByName("糖果罐子"));
        findViewById(R.id.card_data_index_12_2_17_0).setOnClickListener(v -> selectCardDataByName("烛阴龙"));
        findViewById(R.id.card_data_index_12_2_17_1).setOnClickListener(v -> selectCardDataByName("青莲烛阴龙"));
        findViewById(R.id.card_data_index_12_2_17_2).setOnClickListener(v -> selectCardDataByName("圣火烛阴龙"));
        findViewById(R.id.card_data_index_12_2_18_0).setOnClickListener(v -> selectCardDataByName("老鼠夹子"));
        findViewById(R.id.card_data_index_12_2_18_1).setOnClickListener(v -> selectCardDataByName("多用老鼠夹子"));
        findViewById(R.id.card_data_index_12_2_18_2).setOnClickListener(v -> selectCardDataByName("黑猫鼠夹"));
        findViewById(R.id.card_data_index_12_2_19_0).setOnClickListener(v -> selectCardDataByName("麻辣串炸弹"));
        findViewById(R.id.card_data_index_12_2_20_0).setOnClickListener(v -> selectCardDataByName("竹筒粽子"));
        findViewById(R.id.card_data_index_12_2_21_0).setOnClickListener(v -> selectCardDataByName("娇娇虎"));
        findViewById(R.id.card_data_index_12_2_21_1).setOnClickListener(v -> selectCardDataByName("贵族娇娇虎"));
        findViewById(R.id.card_data_index_12_2_21_2).setOnClickListener(v -> selectCardDataByName("皇室娇娇虎"));
        findViewById(R.id.card_data_index_12_3_1_0).setOnClickListener(v -> selectCardDataByName("辣椒粉"));
        findViewById(R.id.card_data_index_12_3_1_1).setOnClickListener(v -> selectCardDataByName("火爆辣椒粉"));
        findViewById(R.id.card_data_index_12_3_1_2).setOnClickListener(v -> selectCardDataByName("魔鬼辣椒粉"));
        findViewById(R.id.card_data_index_12_3_2_0).setOnClickListener(v -> selectCardDataByName("月蟾兔"));
        findViewById(R.id.card_data_index_12_3_3_0).setOnClickListener(v -> selectCardDataByName("爆炸汪"));
        findViewById(R.id.card_data_index_12_3_3_1).setOnClickListener(v -> selectCardDataByName("时尚爆炸汪"));
        findViewById(R.id.card_data_index_12_3_3_2).setOnClickListener(v -> selectCardDataByName("魔幻爆炸汪"));
        findViewById(R.id.card_data_index_12_3_4_0).setOnClickListener(v -> selectCardDataByName("肉松清明粿"));
        findViewById(R.id.card_data_index_12_3_5_0).setOnClickListener(v -> selectCardDataByName("10周年烟花"));
        findViewById(R.id.card_data_index_12_3_5_1).setOnClickListener(v -> selectCardDataByName("10周年爆竹"));
        findViewById(R.id.card_data_index_12_3_5_2).setOnClickListener(v -> selectCardDataByName("10周年礼花"));
        findViewById(R.id.card_data_index_12_3_6_0).setOnClickListener(v -> selectCardDataByName("芥末牛"));
        findViewById(R.id.card_data_index_12_3_6_1).setOnClickListener(v -> selectCardDataByName("微辣芥末牛"));
        findViewById(R.id.card_data_index_12_3_6_2).setOnClickListener(v -> selectCardDataByName("辛辣芥末牛"));
        findViewById(R.id.card_data_index_13_1_1_0).setOnClickListener(v -> selectCardDataByName("钢丝球"));
        findViewById(R.id.card_data_index_13_1_2_0).setOnClickListener(v -> selectCardDataByName("炸地鼠爆竹"));
        findViewById(R.id.card_data_index_13_1_3_0).setOnClickListener(v -> selectCardDataByName("面粉袋"));
        findViewById(R.id.card_data_index_13_1_3_1).setOnClickListener(v -> selectCardDataByName("影分身袋"));
        findViewById(R.id.card_data_index_13_1_3_2).setOnClickListener(v -> selectCardDataByName("乾坤分身袋"));
        findViewById(R.id.card_data_index_13_1_4_0).setOnClickListener(v -> selectCardDataByName("椰子果"));
        findViewById(R.id.card_data_index_13_1_5_0).setOnClickListener(v -> selectCardDataByName("青涩柿柿"));
        findViewById(R.id.card_data_index_13_1_5_1).setOnClickListener(v -> selectCardDataByName("成熟柿柿"));
        findViewById(R.id.card_data_index_13_1_5_2).setOnClickListener(v -> selectCardDataByName("柿柿如意"));
        findViewById(R.id.card_data_index_13_1_6_0).setOnClickListener(v -> selectCardDataByName("萌虎高压锅"));
        findViewById(R.id.card_data_index_13_1_7_0).setOnClickListener(v -> selectCardDataByName("白羊座精灵"));
        findViewById(R.id.card_data_index_13_1_8_0).setOnClickListener(v -> selectCardDataByName("酋长汪"));
        findViewById(R.id.card_data_index_13_1_8_1).setOnClickListener(v -> selectCardDataByName("天使酋长汪"));
        findViewById(R.id.card_data_index_13_1_8_2).setOnClickListener(v -> selectCardDataByName("金翼酋长汪"));
        findViewById(R.id.card_data_index_13_1_9_0).setOnClickListener(v -> selectCardDataByName("逗猫棒"));
        findViewById(R.id.card_data_index_13_1_9_1).setOnClickListener(v -> selectCardDataByName("金鱼逗猫棒"));
        findViewById(R.id.card_data_index_13_1_9_2).setOnClickListener(v -> selectCardDataByName("鼠鼠逗猫棒"));
        findViewById(R.id.card_data_index_13_1_10_0).setOnClickListener(v -> selectCardDataByName("金牛烟花"));
        findViewById(R.id.card_data_index_13_1_10_1).setOnClickListener(v -> selectCardDataByName("迷幻金牛烟花"));
        findViewById(R.id.card_data_index_13_1_10_2).setOnClickListener(v -> selectCardDataByName("璀璨金牛烟花"));
        findViewById(R.id.card_data_index_13_1_11_0).setOnClickListener(v -> selectCardDataByName("贪吃兔"));
        findViewById(R.id.card_data_index_13_1_11_1).setOnClickListener(v -> selectCardDataByName("肥肥贪吃兔"));
        findViewById(R.id.card_data_index_13_1_11_2).setOnClickListener(v -> selectCardDataByName("暴走贪吃兔"));
        findViewById(R.id.card_data_index_13_1_12_0).setOnClickListener(v -> selectCardDataByName("灵鱼摩蹉神使"));
        findViewById(R.id.card_data_index_13_1_12_1).setOnClickListener(v -> selectCardDataByName("灵鱼摩蹉圣神"));
        findViewById(R.id.card_data_index_13_1_12_2).setOnClickListener(v -> selectCardDataByName("救世神·灵鱼摩蹉"));
        findViewById(R.id.card_data_index_13_1_12_3).setOnClickListener(v -> selectCardDataByName("至尊救世神"));
        findViewById(R.id.card_data_index_13_2_1_0).setOnClickListener(v -> selectCardDataByName("榴莲"));
        findViewById(R.id.card_data_index_13_2_1_1).setOnClickListener(v -> selectCardDataByName("雪山榴莲"));
        findViewById(R.id.card_data_index_13_2_1_2).setOnClickListener(v -> selectCardDataByName("冰河世纪榴莲"));
        findViewById(R.id.card_data_index_13_2_2_0).setOnClickListener(v -> selectCardDataByName("美味电鳗"));
        findViewById(R.id.card_data_index_13_2_2_1).setOnClickListener(v -> selectCardDataByName("变异美味电鳗"));
        findViewById(R.id.card_data_index_13_2_2_2).setOnClickListener(v -> selectCardDataByName("霸王美味电鳗"));
        findViewById(R.id.card_data_index_13_2_3_0).setOnClickListener(v -> selectCardDataByName("镭射喵"));
        findViewById(R.id.card_data_index_13_2_3_1).setOnClickListener(v -> selectCardDataByName("荧光镭射喵"));
        findViewById(R.id.card_data_index_13_2_3_2).setOnClickListener(v -> selectCardDataByName("电音镭射喵"));
        findViewById(R.id.card_data_index_13_2_4_0).setOnClickListener(v -> selectCardDataByName("黑暗神使"));
        findViewById(R.id.card_data_index_13_2_4_1).setOnClickListener(v -> selectCardDataByName("黑暗圣神"));
        findViewById(R.id.card_data_index_13_2_4_2).setOnClickListener(v -> selectCardDataByName("黑暗神·霍德尔"));
        findViewById(R.id.card_data_index_13_2_4_3).setOnClickListener(v -> selectCardDataByName("至尊黑暗神"));
        findViewById(R.id.card_data_index_13_2_5_0).setOnClickListener(v -> selectCardDataByName("火龙果"));
        findViewById(R.id.card_data_index_13_2_5_1).setOnClickListener(v -> selectCardDataByName("炎阳火龙果"));
        findViewById(R.id.card_data_index_13_2_5_2).setOnClickListener(v -> selectCardDataByName("九天皓日火龙果"));
        findViewById(R.id.card_data_index_13_2_6_0).setOnClickListener(v -> selectCardDataByName("摩羯座精灵"));
        findViewById(R.id.card_data_index_13_2_7_0).setOnClickListener(v -> selectCardDataByName("龙珠果"));
        findViewById(R.id.card_data_index_13_2_7_1).setOnClickListener(v -> selectCardDataByName("烈火龙珠果"));
        findViewById(R.id.card_data_index_13_2_7_2).setOnClickListener(v -> selectCardDataByName("燃爆龙珠果"));
        findViewById(R.id.card_data_index_13_2_8_0).setOnClickListener(v -> selectCardDataByName("巴德尔神使"));
        findViewById(R.id.card_data_index_13_2_8_1).setOnClickListener(v -> selectCardDataByName("巴德尔圣神"));
        findViewById(R.id.card_data_index_13_2_8_2).setOnClickListener(v -> selectCardDataByName("光明神·巴德尔"));
        findViewById(R.id.card_data_index_13_3_1_0).setOnClickListener(v -> selectCardDataByName("冰桶炸弹"));
        findViewById(R.id.card_data_index_13_3_1_1).setOnClickListener(v -> selectCardDataByName("酸橙冰桶炸弹"));
        findViewById(R.id.card_data_index_13_3_1_2).setOnClickListener(v -> selectCardDataByName("杂果冰桶炸弹"));
        findViewById(R.id.card_data_index_13_3_2_0).setOnClickListener(v -> selectCardDataByName("冰弹喵"));
        findViewById(R.id.card_data_index_13_3_2_1).setOnClickListener(v -> selectCardDataByName("爆裂冰弹喵"));
        findViewById(R.id.card_data_index_13_3_2_2).setOnClickListener(v -> selectCardDataByName("旋风冰弹喵"));
        findViewById(R.id.card_data_index_13_3_3_0).setOnClickListener(v -> selectCardDataByName("冰兔菓子"));
        findViewById(R.id.card_data_index_13_3_4_0).setOnClickListener(v -> selectCardDataByName("泡泡糖"));
        findViewById(R.id.card_data_index_13_3_4_1).setOnClickListener(v -> selectCardDataByName("大大泡泡糖"));
        findViewById(R.id.card_data_index_13_3_4_2).setOnClickListener(v -> selectCardDataByName("萄萄泡泡糖"));
        findViewById(R.id.card_data_index_13_3_5_0).setOnClickListener(v -> selectCardDataByName("逆转牛"));
        findViewById(R.id.card_data_index_13_3_5_1).setOnClickListener(v -> selectCardDataByName("匀速逆转牛"));
        findViewById(R.id.card_data_index_13_3_5_2).setOnClickListener(v -> selectCardDataByName("光速逆转牛"));
        findViewById(R.id.card_data_index_13_4_1_0).setOnClickListener(v -> selectCardDataByName("蛋蛋兔"));
        findViewById(R.id.card_data_index_13_4_1_1).setOnClickListener(v -> selectCardDataByName("智能蛋蛋兔"));
        findViewById(R.id.card_data_index_13_4_1_2).setOnClickListener(v -> selectCardDataByName("外星蛋蛋兔"));
        findViewById(R.id.card_data_index_14_1_1_0).setOnClickListener(v -> selectCardDataByName("冰激凌"));
        findViewById(R.id.card_data_index_14_1_1_1).setOnClickListener(v -> selectCardDataByName("果蔬冰淇淋"));
        findViewById(R.id.card_data_index_14_1_1_2).setOnClickListener(v -> selectCardDataByName("极寒冰沙"));
        findViewById(R.id.card_data_index_14_1_2_0).setOnClickListener(v -> selectCardDataByName("13周年时光机"));
        findViewById(R.id.card_data_index_14_1_2_1).setOnClickListener(v -> selectCardDataByName("超载时光机"));
        findViewById(R.id.card_data_index_14_1_2_2).setOnClickListener(v -> selectCardDataByName("未来时光机"));
        findViewById(R.id.card_data_index_14_1_3_0).setOnClickListener(v -> selectCardDataByName("转龙壶"));
        findViewById(R.id.card_data_index_14_1_3_1).setOnClickListener(v -> selectCardDataByName("充能转龙壶"));
        findViewById(R.id.card_data_index_14_1_3_2).setOnClickListener(v -> selectCardDataByName("巨星转龙壶"));
        findViewById(R.id.card_data_index_14_1_4_0).setOnClickListener(v -> selectCardDataByName("顽皮龙"));
        findViewById(R.id.card_data_index_14_1_4_1).setOnClickListener(v -> selectCardDataByName("捣蛋顽皮龙"));
        findViewById(R.id.card_data_index_14_1_4_2).setOnClickListener(v -> selectCardDataByName("神奇顽皮龙"));
        findViewById(R.id.card_data_index_14_1_5_0).setOnClickListener(v -> selectCardDataByName("美味计时器"));
        findViewById(R.id.card_data_index_14_1_5_1).setOnClickListener(v -> selectCardDataByName("佳肴计时器"));
        findViewById(R.id.card_data_index_14_1_5_2).setOnClickListener(v -> selectCardDataByName("珍馐计时器"));
        findViewById(R.id.card_data_index_14_1_6_0).setOnClickListener(v -> selectCardDataByName("柯罗诺斯神使"));
        findViewById(R.id.card_data_index_14_1_6_1).setOnClickListener(v -> selectCardDataByName("柯罗诺斯圣神"));
        findViewById(R.id.card_data_index_14_1_6_2).setOnClickListener(v -> selectCardDataByName("时间神·柯罗诺斯"));
        findViewById(R.id.card_data_index_14_1_6_3).setOnClickListener(v -> selectCardDataByName("至尊时间神"));
        findViewById(R.id.card_data_index_14_1_7_0).setOnClickListener(v -> selectCardDataByName("克洛托神使"));
        findViewById(R.id.card_data_index_14_1_7_1).setOnClickListener(v -> selectCardDataByName("克洛托圣神"));
        findViewById(R.id.card_data_index_14_1_7_2).setOnClickListener(v -> selectCardDataByName("命运女神·克洛托"));
        findViewById(R.id.card_data_index_14_1_7_3).setOnClickListener(v -> selectCardDataByName("至尊命运女神"));
        findViewById(R.id.card_data_index_14_1_8_0).setOnClickListener(v -> selectCardDataByName("蛇蛇酒"));
        findViewById(R.id.card_data_index_14_1_8_1).setOnClickListener(v -> selectCardDataByName("仙露蛇蛇酒"));
        findViewById(R.id.card_data_index_14_1_8_2).setOnClickListener(v -> selectCardDataByName("琼浆蛇蛇酒"));
        findViewById(R.id.card_data_index_14_1_9_0).setOnClickListener(v -> selectCardDataByName("幻幻鸡"));
        findViewById(R.id.card_data_index_14_1_9_1).setOnClickListener(v -> selectCardDataByName("学者幻幻鸡"));
        findViewById(R.id.card_data_index_14_1_9_2).setOnClickListener(v -> selectCardDataByName("导师幻幻鸡"));
        findViewById(R.id.card_data_index_14_1_10_0).setOnClickListener(v -> selectCardDataByName("圣诞包裹"));
        findViewById(R.id.card_data_index_14_1_10_1).setOnClickListener(v -> selectCardDataByName("奢华圣诞包裹"));
        findViewById(R.id.card_data_index_14_1_10_2).setOnClickListener(v -> selectCardDataByName("至尊圣诞包裹"));
        findViewById(R.id.card_data_index_14_1_11_0).setOnClickListener(v -> selectCardDataByName("天使猪"));
        findViewById(R.id.card_data_index_14_1_11_1).setOnClickListener(v -> selectCardDataByName("魔法天使猪"));
        findViewById(R.id.card_data_index_14_1_11_2).setOnClickListener(v -> selectCardDataByName("神力天使猪"));
        findViewById(R.id.card_data_index_14_1_12_0).setOnClickListener(v -> selectCardDataByName("黯然销魂饭"));
        findViewById(R.id.card_data_index_14_1_12_1).setOnClickListener(v -> selectCardDataByName("培根香肠饭"));
        findViewById(R.id.card_data_index_14_1_12_2).setOnClickListener(v -> selectCardDataByName("天妇罗盖饭"));
        findViewById(R.id.card_data_index_14_1_13_0).setOnClickListener(v -> selectCardDataByName("梵天神使"));
        findViewById(R.id.card_data_index_14_1_13_1).setOnClickListener(v -> selectCardDataByName("梵天圣神"));
        findViewById(R.id.card_data_index_14_1_13_2).setOnClickListener(v -> selectCardDataByName("创造神·梵天"));
        findViewById(R.id.card_data_index_14_1_14_0).setOnClickListener(v -> selectCardDataByName("百变蛇"));
        findViewById(R.id.card_data_index_14_1_14_1).setOnClickListener(v -> selectCardDataByName("孪生百变蛇"));
        findViewById(R.id.card_data_index_14_1_14_2).setOnClickListener(v -> selectCardDataByName("双子百变蛇"));
        findViewById(R.id.card_data_index_14_2_1_0).setOnClickListener(v -> selectCardDataByName("油灯"));
        findViewById(R.id.card_data_index_14_2_1_1).setOnClickListener(v -> selectCardDataByName("高亮油灯"));
        findViewById(R.id.card_data_index_14_2_2_0).setOnClickListener(v -> selectCardDataByName("南瓜灯"));
        findViewById(R.id.card_data_index_14_2_3_0).setOnClickListener(v -> selectCardDataByName("肉松清明粿"));
        findViewById(R.id.card_data_index_14_2_4_0).setOnClickListener(v -> selectCardDataByName("防萤草灯笼"));
        findViewById(R.id.card_data_index_14_2_5_0).setOnClickListener(v -> selectCardDataByName("萤火蛇"));
        findViewById(R.id.card_data_index_14_2_5_1).setOnClickListener(v -> selectCardDataByName("星辉萤火蛇"));
        findViewById(R.id.card_data_index_14_2_5_2).setOnClickListener(v -> selectCardDataByName("月芒萤火蛇"));
        findViewById(R.id.card_data_index_14_2_6_0).setOnClickListener(v -> selectCardDataByName("换气扇"));
        findViewById(R.id.card_data_index_14_2_7_0).setOnClickListener(v -> selectCardDataByName("9周年幸运草扇"));
        findViewById(R.id.card_data_index_14_2_7_1).setOnClickListener(v -> selectCardDataByName("9周年超能扇"));
        findViewById(R.id.card_data_index_14_2_7_2).setOnClickListener(v -> selectCardDataByName("9周年SSR草扇"));
        findViewById(R.id.card_data_index_14_2_8_0).setOnClickListener(v -> selectCardDataByName("棕榈吹风机"));
        findViewById(R.id.card_data_index_14_2_8_1).setOnClickListener(v -> selectCardDataByName("金棕榈吹风机"));
        findViewById(R.id.card_data_index_14_2_9_0).setOnClickListener(v -> selectCardDataByName("爆爆鸡"));
        findViewById(R.id.card_data_index_14_2_9_1).setOnClickListener(v -> selectCardDataByName("疯狂爆爆鸡"));
        findViewById(R.id.card_data_index_14_2_9_2).setOnClickListener(v -> selectCardDataByName("酷炫爆爆鸡"));
        findViewById(R.id.card_data_index_14_2_10_0).setOnClickListener(v -> selectCardDataByName("清障猪"));
        findViewById(R.id.card_data_index_14_2_10_1).setOnClickListener(v -> selectCardDataByName("拉环清障猪"));
        findViewById(R.id.card_data_index_14_2_10_2).setOnClickListener(v -> selectCardDataByName("核能清障猪"));
        findViewById(R.id.card_data_index_14_2_11_0).setOnClickListener(v -> selectCardDataByName("旋风牛"));
        findViewById(R.id.card_data_index_14_2_11_1).setOnClickListener(v -> selectCardDataByName("强压旋风牛"));
        findViewById(R.id.card_data_index_14_2_11_2).setOnClickListener(v -> selectCardDataByName("极速旋风牛"));
        findViewById(R.id.card_data_index_14_2_12_0).setOnClickListener(v -> selectCardDataByName("酸柠檬爆弹"));
        findViewById(R.id.card_data_index_14_2_12_1).setOnClickListener(v -> selectCardDataByName("电子柠檬爆弹"));
        findViewById(R.id.card_data_index_14_2_12_2).setOnClickListener(v -> selectCardDataByName("质子柠檬爆弹"));
        findViewById(R.id.card_data_index_14_2_13_0).setOnClickListener(v -> selectCardDataByName("炸炸菇"));
        findViewById(R.id.card_data_index_14_2_13_1).setOnClickListener(v -> selectCardDataByName("魔王炸炸菇"));
        findViewById(R.id.card_data_index_14_2_14_0).setOnClickListener(v -> selectCardDataByName("海盐粉"));
        findViewById(R.id.card_data_index_14_2_14_1).setOnClickListener(v -> selectCardDataByName("芥末海盐粉"));
        findViewById(R.id.card_data_index_14_2_14_2).setOnClickListener(v -> selectCardDataByName("十三香海盐粉"));
        findViewById(R.id.card_data_index_14_2_15_0).setOnClickListener(v -> selectCardDataByName("碎冰喵"));
        findViewById(R.id.card_data_index_14_2_15_1).setOnClickListener(v -> selectCardDataByName("闹钟碎冰喵"));
        findViewById(R.id.card_data_index_14_2_15_2).setOnClickListener(v -> selectCardDataByName("发条碎冰喵"));
        findViewById(R.id.card_data_index_14_3_1_0).setOnClickListener(v -> selectCardDataByName("木塞子"));
        findViewById(R.id.card_data_index_14_3_2_0).setOnClickListener(v -> selectCardDataByName("防风草沙拉"));
        findViewById(R.id.card_data_index_14_3_2_1).setOnClickListener(v -> selectCardDataByName("蛋黄酱防风草"));
        findViewById(R.id.card_data_index_14_3_3_0).setOnClickListener(v -> selectCardDataByName("金箔甜筒"));
        findViewById(R.id.card_data_index_14_3_3_1).setOnClickListener(v -> selectCardDataByName("金箔可可甜筒"));
        findViewById(R.id.card_data_index_14_3_4_0).setOnClickListener(v -> selectCardDataByName("治愈喵"));
        findViewById(R.id.card_data_index_14_3_4_1).setOnClickListener(v -> selectCardDataByName("武装治愈喵"));
        findViewById(R.id.card_data_index_14_3_4_2).setOnClickListener(v -> selectCardDataByName("全能治愈喵"));
        findViewById(R.id.card_data_index_14_3_5_0).setOnClickListener(v -> selectCardDataByName("12周年能量饮料"));
        findViewById(R.id.card_data_index_14_3_6_0).setOnClickListener(v -> selectCardDataByName("咖啡粉"));
        findViewById(R.id.card_data_index_14_3_6_1).setOnClickListener(v -> selectCardDataByName("名贵咖啡粉"));
        findViewById(R.id.card_data_index_14_3_6_2).setOnClickListener(v -> selectCardDataByName("皇族咖啡粉"));
        findViewById(R.id.card_data_index_14_4_1_0).setOnClickListener(v -> selectCardDataByName("猫猫盒"));
        findViewById(R.id.card_data_index_14_4_2_0).setOnClickListener(v -> selectCardDataByName("猫猫箱"));
        findViewById(R.id.card_data_index_14_4_3_0).setOnClickListener(v -> selectCardDataByName("小丑盒子"));
        findViewById(R.id.card_data_index_14_4_4_0).setOnClickListener(v -> selectCardDataByName("鼠乐宝味觉糖"));
        findViewById(R.id.card_data_index_14_4_4_1).setOnClickListener(v -> selectCardDataByName("黑加仑味觉糖"));
        findViewById(R.id.card_data_index_14_4_4_2).setOnClickListener(v -> selectCardDataByName("车厘子味觉糖"));
        findViewById(R.id.card_data_index_14_4_5_0).setOnClickListener(v -> selectCardDataByName("大福虎"));
        findViewById(R.id.card_data_index_14_4_5_1).setOnClickListener(v -> selectCardDataByName("草莓大福虎"));
        findViewById(R.id.card_data_index_14_4_5_2).setOnClickListener(v -> selectCardDataByName("可可大福虎"));
        findViewById(R.id.card_data_index_15_1_1_0).setOnClickListener(v -> selectCardDataByName("土司面包"));
        findViewById(R.id.card_data_index_15_1_2_0).setOnClickListener(v -> selectCardDataByName("月饼"));
        findViewById(R.id.card_data_index_15_1_3_0).setOnClickListener(v -> selectCardDataByName("冰皮月饼"));
        findViewById(R.id.card_data_index_15_1_4_0).setOnClickListener(v -> selectCardDataByName("巧克力面包"));
        findViewById(R.id.card_data_index_15_1_4_1).setOnClickListener(v -> selectCardDataByName("德芙面包"));
        findViewById(R.id.card_data_index_15_1_5_0).setOnClickListener(v -> selectCardDataByName("菠萝爆炸面包"));
        findViewById(R.id.card_data_index_15_1_5_1).setOnClickListener(v -> selectCardDataByName("独角菠萝面包"));
        findViewById(R.id.card_data_index_15_1_5_2).setOnClickListener(v -> selectCardDataByName("皇冠菠萝面包"));
        findViewById(R.id.card_data_index_15_1_6_0).setOnClickListener(v -> selectCardDataByName("老虎蟹面包"));
        findViewById(R.id.card_data_index_15_1_6_1).setOnClickListener(v -> selectCardDataByName("帝王蟹面包"));
        findViewById(R.id.card_data_index_15_1_7_0).setOnClickListener(v -> selectCardDataByName("桂花酒"));
        findViewById(R.id.card_data_index_15_1_8_0).setOnClickListener(v -> selectCardDataByName("榴莲千层饼"));
        findViewById(R.id.card_data_index_15_2_1_0).setOnClickListener(v -> selectCardDataByName("瓜皮护罩"));
        findViewById(R.id.card_data_index_15_2_1_1).setOnClickListener(v -> selectCardDataByName("尖刺瓜皮护罩"));
        findViewById(R.id.card_data_index_15_2_1_2).setOnClickListener(v -> selectCardDataByName("锋芒瓜皮护罩"));
        findViewById(R.id.card_data_index_15_2_2_0).setOnClickListener(v -> selectCardDataByName("处女座精灵"));
        findViewById(R.id.card_data_index_15_2_2_1).setOnClickListener(v -> selectCardDataByName("处女座战将"));
        findViewById(R.id.card_data_index_15_2_2_2).setOnClickListener(v -> selectCardDataByName("处女座星宿"));
        findViewById(R.id.card_data_index_15_2_3_0).setOnClickListener(v -> selectCardDataByName("赫拉神使"));
        findViewById(R.id.card_data_index_15_2_3_1).setOnClickListener(v -> selectCardDataByName("赫拉圣神"));
        findViewById(R.id.card_data_index_15_2_3_2).setOnClickListener(v -> selectCardDataByName("天后·赫拉"));
        findViewById(R.id.card_data_index_15_2_3_3).setOnClickListener(v -> selectCardDataByName("至尊天后"));
        findViewById(R.id.card_data_index_15_2_4_0).setOnClickListener(v -> selectCardDataByName("祥龙环"));
        findViewById(R.id.card_data_index_15_2_4_1).setOnClickListener(v -> selectCardDataByName("点睛祥龙环"));
        findViewById(R.id.card_data_index_15_2_4_2).setOnClickListener(v -> selectCardDataByName("紫鳞祥龙环"));
        findViewById(R.id.card_data_index_15_2_5_0).setOnClickListener(v -> selectCardDataByName("守能汪"));
        findViewById(R.id.card_data_index_15_2_5_1).setOnClickListener(v -> selectCardDataByName("蓝焰守能汪"));
        findViewById(R.id.card_data_index_15_2_5_2).setOnClickListener(v -> selectCardDataByName("耀金守能汪"));
        findViewById(R.id.card_data_index_15_2_6_0).setOnClickListener(v -> selectCardDataByName("生日帽"));
        findViewById(R.id.card_data_index_15_2_7_0).setOnClickListener(v -> selectCardDataByName("喵喵炉"));
        findViewById(R.id.card_data_index_15_2_7_1).setOnClickListener(v -> selectCardDataByName("靓粉喵喵炉"));
        findViewById(R.id.card_data_index_15_2_7_2).setOnClickListener(v -> selectCardDataByName("炫紫喵喵炉"));
        findViewById(R.id.card_data_index_15_2_8_0).setOnClickListener(v -> selectCardDataByName("扑克牌护罩"));
        findViewById(R.id.card_data_index_15_2_8_1).setOnClickListener(v -> selectCardDataByName("精致黑桃护罩"));
        findViewById(R.id.card_data_index_15_2_8_2).setOnClickListener(v -> selectCardDataByName("豪华梅花护罩"));
        findViewById(R.id.card_data_index_15_2_9_0).setOnClickListener(v -> selectCardDataByName("彩虹蛇"));
        findViewById(R.id.card_data_index_15_2_9_1).setOnClickListener(v -> selectCardDataByName("溏心彩虹蛇"));
        findViewById(R.id.card_data_index_15_2_9_2).setOnClickListener(v -> selectCardDataByName("缤纷彩虹蛇"));
        findViewById(R.id.card_data_index_16_1_1_1).setOnClickListener(v -> selectCardDataByName("火炉菠萝面包"));
        findViewById(R.id.card_data_index_16_1_1_2).setOnClickListener(v -> selectCardDataByName("火龙果菠萝包"));
        findViewById(R.id.card_data_index_16_1_1_3).setOnClickListener(v -> selectCardDataByName("机枪菠萝面包"));
        findViewById(R.id.card_data_index_16_1_2_1).setOnClickListener(v -> selectCardDataByName("雪芭煮蛋器"));
        findViewById(R.id.card_data_index_16_1_2_2).setOnClickListener(v -> selectCardDataByName("臭豆腐煮蛋器"));
        findViewById(R.id.card_data_index_16_1_2_3).setOnClickListener(v -> selectCardDataByName("终结者煮蛋器"));
        findViewById(R.id.card_data_index_16_1_3_1).setOnClickListener(v -> selectCardDataByName("火影怪味鱿鱼"));
        findViewById(R.id.card_data_index_16_1_3_2).setOnClickListener(v -> selectCardDataByName("合金怪味鱿鱼"));
        findViewById(R.id.card_data_index_16_1_3_3).setOnClickListener(v -> selectCardDataByName("松香怪味鱿鱼"));
        findViewById(R.id.card_data_index_16_1_4_1).setOnClickListener(v -> selectCardDataByName("酱香锅烤栗子"));
        findViewById(R.id.card_data_index_16_1_4_2).setOnClickListener(v -> selectCardDataByName("可乐香烤栗子"));
        findViewById(R.id.card_data_index_16_1_4_3).setOnClickListener(v -> selectCardDataByName("如意香烤栗子"));
        findViewById(R.id.card_data_index_16_1_5_1).setOnClickListener(v -> selectCardDataByName("热狗耗油双菇"));
        findViewById(R.id.card_data_index_16_1_5_2).setOnClickListener(v -> selectCardDataByName("喷壶耗油双菇"));
        findViewById(R.id.card_data_index_16_1_5_3).setOnClickListener(v -> selectCardDataByName("糖葫芦耗油双菇"));
    }

    private void selectCardDataByName(String cardName) {
        if (cardName.isEmpty()) {
            Toast.makeText(this, "请输入卡片名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String tableName = dbHelper.getCardTable(cardName);
        if (tableName == null) {
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
            intent.putExtra("name", cardName);
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

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.FloatButton_CardDataIndex_Container).setOnTouchListener(this::setPressAnimation);
        findViewById(R.id.FloatButton_CardDataSearch_Container).setOnTouchListener(this::setPressAnimation);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }
}