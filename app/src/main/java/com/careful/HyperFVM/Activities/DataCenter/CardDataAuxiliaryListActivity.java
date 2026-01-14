package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityCardDataAuxiliaryListBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.Animation.ViewAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class CardDataAuxiliaryListActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ActivityCardDataAuxiliaryListBinding binding;
    private SpringBackScrollView CardDataAuxiliaryListContainer;

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

        // 设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.top_bar_data_center_card_data_auxiliary_list) + " ");

        // 添加模糊材质
        setupBlurEffect();

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 目录按钮
        CardDataAuxiliaryListContainer = findViewById(R.id.CardDataAuxiliaryList_Container);
        findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container).setOnClickListener(v -> showTitleNavigationDialog());

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            initCardImages();
            if (dbHelper.getSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST)) {
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
        AlertDialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
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
        findViewById(R.id.card_data_index_4_1_5_0).setOnClickListener(v -> selectCardDataByName("能量喵"));
        findViewById(R.id.card_data_index_4_1_5_1).setOnClickListener(v -> selectCardDataByName("蓝焰能量喵"));
        findViewById(R.id.card_data_index_4_1_5_2).setOnClickListener(v -> selectCardDataByName("樱红能量喵"));
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
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex211.cardDataIndex2110.setOnClickListener(v -> selectCardDataByName("勺勺兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex211.cardDataIndex2111.setOnClickListener(v -> selectCardDataByName("增强勺勺兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex211.cardDataIndex2112.setOnClickListener(v -> selectCardDataByName("盖世勺勺兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex212.cardDataIndex2120.setOnClickListener(v -> selectCardDataByName("窃蛋龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex212.cardDataIndex2121.setOnClickListener(v -> selectCardDataByName("蓝角窃蛋龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex212.cardDataIndex2122.setOnClickListener(v -> selectCardDataByName("钢爪窃蛋龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex213.cardDataIndex2130.setOnClickListener(v -> selectCardDataByName("尤弥尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex213.cardDataIndex2131.setOnClickListener(v -> selectCardDataByName("尤弥尔圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex213.cardDataIndex2132.setOnClickListener(v -> selectCardDataByName("巨神·尤弥尔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex213.cardDataIndex2133.setOnClickListener(v -> selectCardDataByName("至尊巨神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex214.cardDataIndex2140.setOnClickListener(v -> selectCardDataByName("幻影蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex214.cardDataIndex2141.setOnClickListener(v -> selectCardDataByName("羽翼幻影蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex214.cardDataIndex2142.setOnClickListener(v -> selectCardDataByName("金盔幻影蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex215.cardDataIndex2150.setOnClickListener(v -> selectCardDataByName("全能糖球投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex215.cardDataIndex2151.setOnClickListener(v -> selectCardDataByName("水果糖全能投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex215.cardDataIndex2152.setOnClickListener(v -> selectCardDataByName("可可糖全能投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex221.cardDataIndex2210.setOnClickListener(v -> selectCardDataByName("煮蛋器投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex221.cardDataIndex2211.setOnClickListener(v -> selectCardDataByName("威力煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex221.cardDataIndex2212.setOnClickListener(v -> selectCardDataByName("强袭煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex222.cardDataIndex2220.setOnClickListener(v -> selectCardDataByName("冰煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex222.cardDataIndex2221.setOnClickListener(v -> selectCardDataByName("节能冰煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex222.cardDataIndex2222.setOnClickListener(v -> selectCardDataByName("冰河煮蛋器"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex223.cardDataIndex2230.setOnClickListener(v -> selectCardDataByName("双鱼座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex223.cardDataIndex2231.setOnClickListener(v -> selectCardDataByName("双鱼座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex223.cardDataIndex2232.setOnClickListener(v -> selectCardDataByName("双鱼座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex224.cardDataIndex2240.setOnClickListener(v -> selectCardDataByName("弹弹鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex224.cardDataIndex2241.setOnClickListener(v -> selectCardDataByName("寒冰弹弹鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex224.cardDataIndex2242.setOnClickListener(v -> selectCardDataByName("月光弹弹鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex225.cardDataIndex2250.setOnClickListener(v -> selectCardDataByName("索尔神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex225.cardDataIndex2251.setOnClickListener(v -> selectCardDataByName("索尔圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex225.cardDataIndex2252.setOnClickListener(v -> selectCardDataByName("雷神·索尔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex225.cardDataIndex2253.setOnClickListener(v -> selectCardDataByName("至尊雷神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex226.cardDataIndex2260.setOnClickListener(v -> selectCardDataByName("机械汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex226.cardDataIndex2261.setOnClickListener(v -> selectCardDataByName("改装机械汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex226.cardDataIndex2262.setOnClickListener(v -> selectCardDataByName("自律机械汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex227.cardDataIndex2270.setOnClickListener(v -> selectCardDataByName("投弹猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex227.cardDataIndex2271.setOnClickListener(v -> selectCardDataByName("獠牙投弹猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex227.cardDataIndex2272.setOnClickListener(v -> selectCardDataByName("振金投弹猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex228.cardDataIndex2280.setOnClickListener(v -> selectCardDataByName("雪糕投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex228.cardDataIndex2281.setOnClickListener(v -> selectCardDataByName("麦旋风投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex228.cardDataIndex2282.setOnClickListener(v -> selectCardDataByName("水果雪芭投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex229.cardDataIndex2290.setOnClickListener(v -> selectCardDataByName("飞鱼喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex229.cardDataIndex2291.setOnClickListener(v -> selectCardDataByName("河滨飞鱼喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex229.cardDataIndex2292.setOnClickListener(v -> selectCardDataByName("深海飞鱼喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2210.cardDataIndex22100.setOnClickListener(v -> selectCardDataByName("壮壮牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2210.cardDataIndex22101.setOnClickListener(v -> selectCardDataByName("蛮力壮壮牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2210.cardDataIndex22102.setOnClickListener(v -> selectCardDataByName("乾坤壮壮牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2211.cardDataIndex22110.setOnClickListener(v -> selectCardDataByName("烤蜥蜴投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2211.cardDataIndex22111.setOnClickListener(v -> selectCardDataByName("坚果蜥蜴投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2211.cardDataIndex22112.setOnClickListener(v -> selectCardDataByName("花椒蜥蜴投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2212.cardDataIndex22120.setOnClickListener(v -> selectCardDataByName("投篮虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2212.cardDataIndex22121.setOnClickListener(v -> selectCardDataByName("职业投篮虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2212.cardDataIndex22122.setOnClickListener(v -> selectCardDataByName("球星投篮虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2213.cardDataIndex22130.setOnClickListener(v -> selectCardDataByName("钵钵鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2213.cardDataIndex22131.setOnClickListener(v -> selectCardDataByName("飘香钵钵鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex2213.cardDataIndex22132.setOnClickListener(v -> selectCardDataByName("川香钵钵鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex231.cardDataIndex2310.setOnClickListener(v -> selectCardDataByName("色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex231.cardDataIndex2311.setOnClickListener(v -> selectCardDataByName("果蔬色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex231.cardDataIndex2312.setOnClickListener(v -> selectCardDataByName("凯撒色拉投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex232.cardDataIndex2320.setOnClickListener(v -> selectCardDataByName("巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex232.cardDataIndex2321.setOnClickListener(v -> selectCardDataByName("浓情巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex232.cardDataIndex2322.setOnClickListener(v -> selectCardDataByName("脆心巧克力投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex233.cardDataIndex2330.setOnClickListener(v -> selectCardDataByName("臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex233.cardDataIndex2331.setOnClickListener(v -> selectCardDataByName("什锦臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex233.cardDataIndex2332.setOnClickListener(v -> selectCardDataByName("铁板臭豆腐投手"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex234.cardDataIndex2340.setOnClickListener(v -> selectCardDataByName("8周年蛋糕"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex234.cardDataIndex2341.setOnClickListener(v -> selectCardDataByName("8周年慕斯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex234.cardDataIndex2342.setOnClickListener(v -> selectCardDataByName("8周年红丝绒"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1013.cardDataIndex10130.setOnClickListener(v -> selectCardDataByName("生煎锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1013.cardDataIndex10131.setOnClickListener(v -> selectCardDataByName("水煎包锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1013.cardDataIndex10132.setOnClickListener(v -> selectCardDataByName("驴肉火烧锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1014.cardDataIndex10140.setOnClickListener(v -> selectCardDataByName("铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1014.cardDataIndex10141.setOnClickListener(v -> selectCardDataByName("速热铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1014.cardDataIndex10142.setOnClickListener(v -> selectCardDataByName("微波铛铛虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1015.cardDataIndex10150.setOnClickListener(v -> selectCardDataByName("祝融神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1015.cardDataIndex10151.setOnClickListener(v -> selectCardDataByName("祝融圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1015.cardDataIndex10152.setOnClickListener(v -> selectCardDataByName("赤帝·祝融"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1015.cardDataIndex10153.setOnClickListener(v -> selectCardDataByName("至尊赤帝"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1016.cardDataIndex10160.setOnClickListener(v -> selectCardDataByName("糖炒栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1016.cardDataIndex10161.setOnClickListener(v -> selectCardDataByName("开口笑栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1016.cardDataIndex10162.setOnClickListener(v -> selectCardDataByName("焦香烤栗子"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1017.cardDataIndex10170.setOnClickListener(v -> selectCardDataByName("霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1017.cardDataIndex10171.setOnClickListener(v -> selectCardDataByName("雪花霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1017.cardDataIndex10172.setOnClickListener(v -> selectCardDataByName("玄冰霜霜蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1041.cardDataIndex10410.setOnClickListener(v -> selectCardDataByName("蜂蜜史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1041.cardDataIndex10411.setOnClickListener(v -> selectCardDataByName("蜂糖史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1041.cardDataIndex10412.setOnClickListener(v -> selectCardDataByName("蜂王浆史莱姆"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1214.cardDataIndex12140.setOnClickListener(v -> selectCardDataByName("导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1214.cardDataIndex12141.setOnClickListener(v -> selectCardDataByName("舰地导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1214.cardDataIndex12142.setOnClickListener(v -> selectCardDataByName("洲际导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1215.cardDataIndex12150.setOnClickListener(v -> selectCardDataByName("盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1215.cardDataIndex12151.setOnClickListener(v -> selectCardDataByName("盖亚圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1215.cardDataIndex12152.setOnClickListener(v -> selectCardDataByName("大地女神·盖亚"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent1).cardCardDataIndex1215.cardDataIndex12153.setOnClickListener(v -> selectCardDataByName("至尊大地女神"));

        // 2. 莓果点心
        findViewById(R.id.card_data_index_4_2_1_0).setOnClickListener(v -> selectCardDataByName("莓果点心"));
        findViewById(R.id.card_data_index_4_2_1_1).setOnClickListener(v -> selectCardDataByName("薄荷莓果点心"));
        findViewById(R.id.card_data_index_4_2_1_2).setOnClickListener(v -> selectCardDataByName("流心莓果点心"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex911.cardDataIndex9110.setOnClickListener(v -> selectCardDataByName("章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex911.cardDataIndex9111.setOnClickListener(v -> selectCardDataByName("两栖章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex911.cardDataIndex9112.setOnClickListener(v -> selectCardDataByName("火影章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex912.cardDataIndex9120.setOnClickListener(v -> selectCardDataByName("巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex912.cardDataIndex9121.setOnClickListener(v -> selectCardDataByName("巨蟹座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex912.cardDataIndex9122.setOnClickListener(v -> selectCardDataByName("巨蟹座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex913.cardDataIndex9130.setOnClickListener(v -> selectCardDataByName("忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex913.cardDataIndex9131.setOnClickListener(v -> selectCardDataByName("疾风忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex913.cardDataIndex9132.setOnClickListener(v -> selectCardDataByName("幻影忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex914.cardDataIndex9140.setOnClickListener(v -> selectCardDataByName("狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex914.cardDataIndex9141.setOnClickListener(v -> selectCardDataByName("狄安娜圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex914.cardDataIndex9142.setOnClickListener(v -> selectCardDataByName("月神·狄安娜"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex914.cardDataIndex9143.setOnClickListener(v -> selectCardDataByName("至尊月神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex915.cardDataIndex9150.setOnClickListener(v -> selectCardDataByName("飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex915.cardDataIndex9151.setOnClickListener(v -> selectCardDataByName("大厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex915.cardDataIndex9152.setOnClickListener(v -> selectCardDataByName("名厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex916.cardDataIndex9160.setOnClickListener(v -> selectCardDataByName("铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex916.cardDataIndex9161.setOnClickListener(v -> selectCardDataByName("银甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex916.cardDataIndex9162.setOnClickListener(v -> selectCardDataByName("金甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex917.cardDataIndex9170.setOnClickListener(v -> selectCardDataByName("海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex917.cardDataIndex9171.setOnClickListener(v -> selectCardDataByName("首领海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex917.cardDataIndex9172.setOnClickListener(v -> selectCardDataByName("洛克斯海贼兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex936.cardDataIndex9360.setOnClickListener(v -> selectCardDataByName("冰块冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex936.cardDataIndex9361.setOnClickListener(v -> selectCardDataByName("低温冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent2).cardCardDataIndex936.cardDataIndex9362.setOnClickListener(v -> selectCardDataByName("迅捷冷萃机"));

        // 3. 香料虎
        findViewById(R.id.card_data_index_4_2_2_0).setOnClickListener(v -> selectCardDataByName("香料虎"));
        findViewById(R.id.card_data_index_4_2_2_1).setOnClickListener(v -> selectCardDataByName("海洋香料虎"));
        findViewById(R.id.card_data_index_4_2_2_2).setOnClickListener(v -> selectCardDataByName("魔力香料虎"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex911.cardDataIndex9110.setOnClickListener(v -> selectCardDataByName("章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex911.cardDataIndex9111.setOnClickListener(v -> selectCardDataByName("两栖章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex911.cardDataIndex9112.setOnClickListener(v -> selectCardDataByName("火影章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex912.cardDataIndex9120.setOnClickListener(v -> selectCardDataByName("巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex912.cardDataIndex9121.setOnClickListener(v -> selectCardDataByName("巨蟹座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex912.cardDataIndex9122.setOnClickListener(v -> selectCardDataByName("巨蟹座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex913.cardDataIndex9130.setOnClickListener(v -> selectCardDataByName("忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex913.cardDataIndex9131.setOnClickListener(v -> selectCardDataByName("疾风忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex913.cardDataIndex9132.setOnClickListener(v -> selectCardDataByName("幻影忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex914.cardDataIndex9140.setOnClickListener(v -> selectCardDataByName("狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex914.cardDataIndex9141.setOnClickListener(v -> selectCardDataByName("狄安娜圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex914.cardDataIndex9142.setOnClickListener(v -> selectCardDataByName("月神·狄安娜"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex914.cardDataIndex9143.setOnClickListener(v -> selectCardDataByName("至尊月神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex915.cardDataIndex9150.setOnClickListener(v -> selectCardDataByName("飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex915.cardDataIndex9151.setOnClickListener(v -> selectCardDataByName("大厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex915.cardDataIndex9152.setOnClickListener(v -> selectCardDataByName("名厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex916.cardDataIndex9160.setOnClickListener(v -> selectCardDataByName("铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex916.cardDataIndex9161.setOnClickListener(v -> selectCardDataByName("银甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex916.cardDataIndex9162.setOnClickListener(v -> selectCardDataByName("金甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex917.cardDataIndex9170.setOnClickListener(v -> selectCardDataByName("海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex917.cardDataIndex9171.setOnClickListener(v -> selectCardDataByName("首领海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex917.cardDataIndex9172.setOnClickListener(v -> selectCardDataByName("洛克斯海贼兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex923.cardDataIndex9230.setOnClickListener(v -> selectCardDataByName("火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex923.cardDataIndex9231.setOnClickListener(v -> selectCardDataByName("运载火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex923.cardDataIndex9232.setOnClickListener(v -> selectCardDataByName("反重力火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex1215.cardDataIndex12150.setOnClickListener(v -> selectCardDataByName("盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex1215.cardDataIndex12151.setOnClickListener(v -> selectCardDataByName("盖亚圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex1215.cardDataIndex12152.setOnClickListener(v -> selectCardDataByName("大地女神·盖亚"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent3).cardCardDataIndex1215.cardDataIndex12153.setOnClickListener(v -> selectCardDataByName("至尊大地女神"));
        
        // 4. 塔利亚神使
        findViewById(R.id.card_data_index_4_2_3_0).setOnClickListener(v -> selectCardDataByName("塔利亚神使"));
        findViewById(R.id.card_data_index_4_2_3_1).setOnClickListener(v -> selectCardDataByName("塔利亚圣神"));
        findViewById(R.id.card_data_index_4_2_3_2).setOnClickListener(v -> selectCardDataByName("宴飨女神·塔利亚"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex911.cardDataIndex9110.setOnClickListener(v -> selectCardDataByName("章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex911.cardDataIndex9111.setOnClickListener(v -> selectCardDataByName("两栖章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex911.cardDataIndex9112.setOnClickListener(v -> selectCardDataByName("火影章鱼烧"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex912.cardDataIndex9120.setOnClickListener(v -> selectCardDataByName("巨蟹座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex912.cardDataIndex9121.setOnClickListener(v -> selectCardDataByName("巨蟹座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex912.cardDataIndex9122.setOnClickListener(v -> selectCardDataByName("巨蟹座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex913.cardDataIndex9130.setOnClickListener(v -> selectCardDataByName("忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex913.cardDataIndex9131.setOnClickListener(v -> selectCardDataByName("疾风忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex913.cardDataIndex9132.setOnClickListener(v -> selectCardDataByName("幻影忍忍鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex914.cardDataIndex9140.setOnClickListener(v -> selectCardDataByName("狄安娜神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex914.cardDataIndex9141.setOnClickListener(v -> selectCardDataByName("狄安娜圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex914.cardDataIndex9142.setOnClickListener(v -> selectCardDataByName("月神·狄安娜"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex914.cardDataIndex9143.setOnClickListener(v -> selectCardDataByName("至尊月神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex915.cardDataIndex9150.setOnClickListener(v -> selectCardDataByName("飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex915.cardDataIndex9151.setOnClickListener(v -> selectCardDataByName("大厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex915.cardDataIndex9152.setOnClickListener(v -> selectCardDataByName("名厨飞盘汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex916.cardDataIndex9160.setOnClickListener(v -> selectCardDataByName("铁甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex916.cardDataIndex9161.setOnClickListener(v -> selectCardDataByName("银甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex916.cardDataIndex9162.setOnClickListener(v -> selectCardDataByName("金甲飞镖猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex917.cardDataIndex9170.setOnClickListener(v -> selectCardDataByName("海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex917.cardDataIndex9171.setOnClickListener(v -> selectCardDataByName("首领海盗兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex917.cardDataIndex9172.setOnClickListener(v -> selectCardDataByName("洛克斯海贼兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex923.cardDataIndex9230.setOnClickListener(v -> selectCardDataByName("火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex923.cardDataIndex9231.setOnClickListener(v -> selectCardDataByName("运载火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex923.cardDataIndex9232.setOnClickListener(v -> selectCardDataByName("反重力火箭猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1214.cardDataIndex12140.setOnClickListener(v -> selectCardDataByName("导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1214.cardDataIndex12141.setOnClickListener(v -> selectCardDataByName("舰地导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1214.cardDataIndex12142.setOnClickListener(v -> selectCardDataByName("洲际导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1215.cardDataIndex12150.setOnClickListener(v -> selectCardDataByName("盖亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1215.cardDataIndex12151.setOnClickListener(v -> selectCardDataByName("盖亚圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1215.cardDataIndex12152.setOnClickListener(v -> selectCardDataByName("大地女神·盖亚"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex1215.cardDataIndex12153.setOnClickListener(v -> selectCardDataByName("至尊大地女神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex922.cardDataIndex9220.setOnClickListener(v -> selectCardDataByName("雅典娜守护"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex922.cardDataIndex9221.setOnClickListener(v -> selectCardDataByName("雅典娜圣衣"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex922.cardDataIndex9222.setOnClickListener(v -> selectCardDataByName("雅典娜光辉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex924.cardDataIndex9240.setOnClickListener(v -> selectCardDataByName("宙斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex924.cardDataIndex9241.setOnClickListener(v -> selectCardDataByName("宙斯圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex924.cardDataIndex9242.setOnClickListener(v -> selectCardDataByName("天神·宙斯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex924.cardDataIndex9243.setOnClickListener(v -> selectCardDataByName("至尊天神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex931.cardDataIndex9310.setOnClickListener(v -> selectCardDataByName("魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex931.cardDataIndex9311.setOnClickListener(v -> selectCardDataByName("冰霜魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex931.cardDataIndex9312.setOnClickListener(v -> selectCardDataByName("暴雪元素猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex932.cardDataIndex9320.setOnClickListener(v -> selectCardDataByName("招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex932.cardDataIndex9321.setOnClickListener(v -> selectCardDataByName("贵族招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex932.cardDataIndex9322.setOnClickListener(v -> selectCardDataByName("御守招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex933.cardDataIndex9330.setOnClickListener(v -> selectCardDataByName("雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex933.cardDataIndex9331.setOnClickListener(v -> selectCardDataByName("见习雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex933.cardDataIndex9332.setOnClickListener(v -> selectCardDataByName("导师雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex934.cardDataIndex9340.setOnClickListener(v -> selectCardDataByName("典伊神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex934.cardDataIndex9341.setOnClickListener(v -> selectCardDataByName("典伊圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex934.cardDataIndex9342.setOnClickListener(v -> selectCardDataByName("冰神·典伊"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex934.cardDataIndex9343.setOnClickListener(v -> selectCardDataByName("至尊冰神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex935.cardDataIndex9350.setOnClickListener(v -> selectCardDataByName("冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex935.cardDataIndex9351.setOnClickListener(v -> selectCardDataByName("四棱冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex935.cardDataIndex9352.setOnClickListener(v -> selectCardDataByName("独角冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex936.cardDataIndex9360.setOnClickListener(v -> selectCardDataByName("冰块冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex936.cardDataIndex9361.setOnClickListener(v -> selectCardDataByName("低温冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex936.cardDataIndex9362.setOnClickListener(v -> selectCardDataByName("迅捷冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex714Auxiliary.cardDataIndex7143.setOnClickListener(v -> selectCardDataByName("至尊大力神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex727.cardDataIndex7270.setOnClickListener(v -> selectCardDataByName("埃罗斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex727.cardDataIndex7271.setOnClickListener(v -> selectCardDataByName("埃罗斯圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex727.cardDataIndex7272.setOnClickListener(v -> selectCardDataByName("恶作剧神·埃罗斯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent4).cardCardDataIndex727.cardDataIndex7273.setOnClickListener(v -> selectCardDataByName("至尊恶作剧神"));

        // 5. 精灵龙
        findViewById(R.id.card_data_index_4_2_4_0).setOnClickListener(v -> selectCardDataByName("精灵龙"));
        findViewById(R.id.card_data_index_4_2_4_1).setOnClickListener(v -> selectCardDataByName("蛋筒精灵龙"));
        findViewById(R.id.card_data_index_4_2_4_2).setOnClickListener(v -> selectCardDataByName("樱桃精灵龙"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex922.cardDataIndex9220.setOnClickListener(v -> selectCardDataByName("雅典娜守护"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex922.cardDataIndex9221.setOnClickListener(v -> selectCardDataByName("雅典娜圣衣"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex922.cardDataIndex9222.setOnClickListener(v -> selectCardDataByName("雅典娜光辉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex924.cardDataIndex9240.setOnClickListener(v -> selectCardDataByName("宙斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex924.cardDataIndex9241.setOnClickListener(v -> selectCardDataByName("宙斯圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex924.cardDataIndex9242.setOnClickListener(v -> selectCardDataByName("天神·宙斯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex924.cardDataIndex9243.setOnClickListener(v -> selectCardDataByName("至尊天神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex931.cardDataIndex9310.setOnClickListener(v -> selectCardDataByName("魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex931.cardDataIndex9311.setOnClickListener(v -> selectCardDataByName("冰霜魔法猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex931.cardDataIndex9312.setOnClickListener(v -> selectCardDataByName("暴雪元素猪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex932.cardDataIndex9320.setOnClickListener(v -> selectCardDataByName("招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex932.cardDataIndex9321.setOnClickListener(v -> selectCardDataByName("贵族招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex932.cardDataIndex9322.setOnClickListener(v -> selectCardDataByName("御守招财喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex933.cardDataIndex9330.setOnClickListener(v -> selectCardDataByName("雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex933.cardDataIndex9331.setOnClickListener(v -> selectCardDataByName("见习雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex933.cardDataIndex9332.setOnClickListener(v -> selectCardDataByName("导师雪球兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex934.cardDataIndex9340.setOnClickListener(v -> selectCardDataByName("典伊神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex934.cardDataIndex9341.setOnClickListener(v -> selectCardDataByName("典伊圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex934.cardDataIndex9342.setOnClickListener(v -> selectCardDataByName("冰神·典伊"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex934.cardDataIndex9343.setOnClickListener(v -> selectCardDataByName("至尊冰神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex935.cardDataIndex9350.setOnClickListener(v -> selectCardDataByName("冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex935.cardDataIndex9351.setOnClickListener(v -> selectCardDataByName("四棱冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex935.cardDataIndex9352.setOnClickListener(v -> selectCardDataByName("独角冰晶龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex936.cardDataIndex9360.setOnClickListener(v -> selectCardDataByName("冰块冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex936.cardDataIndex9361.setOnClickListener(v -> selectCardDataByName("低温冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex936.cardDataIndex9362.setOnClickListener(v -> selectCardDataByName("迅捷冷萃机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex921.cardDataIndex9210.setOnClickListener(v -> selectCardDataByName("咖喱龙虾炮"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex921.cardDataIndex9211.setOnClickListener(v -> selectCardDataByName("麻辣龙虾炮"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex921.cardDataIndex9212.setOnClickListener(v -> selectCardDataByName("加农龙虾炮"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex1214.cardDataIndex12140.setOnClickListener(v -> selectCardDataByName("导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex1214.cardDataIndex12141.setOnClickListener(v -> selectCardDataByName("舰地导弹蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent5).cardCardDataIndex1214.cardDataIndex12142.setOnClickListener(v -> selectCardDataByName("洲际导弹蛇"));

        // 6. 五向增幅卡
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
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex311.cardDataIndex3110.setOnClickListener(v -> selectCardDataByName("炭烧海星"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex311.cardDataIndex3111.setOnClickListener(v -> selectCardDataByName("芝士焗海星"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex311.cardDataIndex3112.setOnClickListener(v -> selectCardDataByName("芥末海星刺身"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex312.cardDataIndex3120.setOnClickListener(v -> selectCardDataByName("猪猪料理机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex312.cardDataIndex3121.setOnClickListener(v -> selectCardDataByName("猪猪搅拌机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex312.cardDataIndex3122.setOnClickListener(v -> selectCardDataByName("猪猪破壁机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex313.cardDataIndex3130.setOnClickListener(v -> selectCardDataByName("陀螺喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex313.cardDataIndex3131.setOnClickListener(v -> selectCardDataByName("极光陀螺喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex313.cardDataIndex3132.setOnClickListener(v -> selectCardDataByName("金翼陀螺喵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex314.cardDataIndex3140.setOnClickListener(v -> selectCardDataByName("哈迪斯神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex314.cardDataIndex3141.setOnClickListener(v -> selectCardDataByName("哈迪斯圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex314.cardDataIndex3142.setOnClickListener(v -> selectCardDataByName("冥神·哈迪斯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex314.cardDataIndex3143.setOnClickListener(v -> selectCardDataByName("至尊冥神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex315.cardDataIndex3150.setOnClickListener(v -> selectCardDataByName("查克拉兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex315.cardDataIndex3151.setOnClickListener(v -> selectCardDataByName("上忍查克拉兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex315.cardDataIndex3152.setOnClickListener(v -> selectCardDataByName("影级查克拉兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex322.cardDataIndex3220.setOnClickListener(v -> selectCardDataByName("星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex322.cardDataIndex3221.setOnClickListener(v -> selectCardDataByName("科技星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex322.cardDataIndex3222.setOnClickListener(v -> selectCardDataByName("宇宙星星兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex323.cardDataIndex3230.setOnClickListener(v -> selectCardDataByName("坚果爆炒机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex323.cardDataIndex3231.setOnClickListener(v -> selectCardDataByName("橡子搅拌机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex323.cardDataIndex3232.setOnClickListener(v -> selectCardDataByName("松塔爆破机"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex324.cardDataIndex3240.setOnClickListener(v -> selectCardDataByName("里格神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex324.cardDataIndex3241.setOnClickListener(v -> selectCardDataByName("里格圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex324.cardDataIndex3242.setOnClickListener(v -> selectCardDataByName("守护神·里格"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex324.cardDataIndex3243.setOnClickListener(v -> selectCardDataByName("至尊守护神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex321.cardDataIndex3210.setOnClickListener(v -> selectCardDataByName("厨师虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex321.cardDataIndex3211.setOnClickListener(v -> selectCardDataByName("银牌厨师虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex321.cardDataIndex3212.setOnClickListener(v -> selectCardDataByName("金牌厨师虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1126.cardDataIndex11260.setOnClickListener(v -> selectCardDataByName("大师兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1126.cardDataIndex11261.setOnClickListener(v -> selectCardDataByName("黑带大师兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1126.cardDataIndex11262.setOnClickListener(v -> selectCardDataByName("功夫大师兔"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex325.cardDataIndex3250.setOnClickListener(v -> selectCardDataByName("怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex325.cardDataIndex3251.setOnClickListener(v -> selectCardDataByName("爆汁怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex325.cardDataIndex3252.setOnClickListener(v -> selectCardDataByName("天椒怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1613.cardDataIndex16131.setOnClickListener(v -> selectCardDataByName("火影怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1613.cardDataIndex16132.setOnClickListener(v -> selectCardDataByName("合金怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex1613.cardDataIndex16133.setOnClickListener(v -> selectCardDataByName("松香怪味鱿鱼"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex326.cardDataIndex3260.setOnClickListener(v -> selectCardDataByName("烟花虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex326.cardDataIndex3261.setOnClickListener(v -> selectCardDataByName("冷光烟花虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex326.cardDataIndex3262.setOnClickListener(v -> selectCardDataByName("礼炮烟花虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex327.cardDataIndex3270.setOnClickListener(v -> selectCardDataByName("风车龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex327.cardDataIndex3271.setOnClickListener(v -> selectCardDataByName("暴击风车龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex327.cardDataIndex3272.setOnClickListener(v -> selectCardDataByName("迅猛风车龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex117.cardDataIndex1170.setOnClickListener(v -> selectCardDataByName("散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex117.cardDataIndex1171.setOnClickListener(v -> selectCardDataByName("威武散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex117.cardDataIndex1172.setOnClickListener(v -> selectCardDataByName("霸气散弹牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex118.cardDataIndex1180.setOnClickListener(v -> selectCardDataByName("威风虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex118.cardDataIndex1181.setOnClickListener(v -> selectCardDataByName("爆气威风虎"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent6).cardCardDataIndex118.cardDataIndex1182.setOnClickListener(v -> selectCardDataByName("连弩威风虎"));

        // 7. 喷壶增幅卡
        findViewById(R.id.card_data_index_4_2_9_0).setOnClickListener(v -> selectCardDataByName("加速榨汁机"));
        findViewById(R.id.card_data_index_4_2_9_1).setOnClickListener(v -> selectCardDataByName("苹果榨汁机"));
        findViewById(R.id.card_data_index_4_2_9_2).setOnClickListener(v -> selectCardDataByName("大菠萝榨汁机"));
        findViewById(R.id.card_data_index_4_2_10_0).setOnClickListener(v -> selectCardDataByName("魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_10_1).setOnClickListener(v -> selectCardDataByName("青木魔杖蛇"));
        findViewById(R.id.card_data_index_4_2_10_2).setOnClickListener(v -> selectCardDataByName("凤羽魔杖蛇"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex821.cardDataIndex8210.setOnClickListener(v -> selectCardDataByName("旋转咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex821.cardDataIndex8211.setOnClickListener(v -> selectCardDataByName("节能旋转咖啡壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex821.cardDataIndex8212.setOnClickListener(v -> selectCardDataByName("原子咖啡壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex822.cardDataIndex8220.setOnClickListener(v -> selectCardDataByName("狮子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex822.cardDataIndex8221.setOnClickListener(v -> selectCardDataByName("狮子座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex822.cardDataIndex8222.setOnClickListener(v -> selectCardDataByName("狮子座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex823.cardDataIndex8230.setOnClickListener(v -> selectCardDataByName("波塞冬神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex823.cardDataIndex8231.setOnClickListener(v -> selectCardDataByName("波塞冬圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex823.cardDataIndex8232.setOnClickListener(v -> selectCardDataByName("海神·波塞冬"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex823.cardDataIndex8233.setOnClickListener(v -> selectCardDataByName("至尊海神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex824.cardDataIndex8240.setOnClickListener(v -> selectCardDataByName("转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex824.cardDataIndex8241.setOnClickListener(v -> selectCardDataByName("五彩转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex824.cardDataIndex8242.setOnClickListener(v -> selectCardDataByName("王室转转鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex825.cardDataIndex8250.setOnClickListener(v -> selectCardDataByName("可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex825.cardDataIndex8251.setOnClickListener(v -> selectCardDataByName("冰摇可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex825.cardDataIndex8252.setOnClickListener(v -> selectCardDataByName("星杯可乐汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex826.cardDataIndex8260.setOnClickListener(v -> selectCardDataByName("元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex826.cardDataIndex8261.setOnClickListener(v -> selectCardDataByName("泡泡元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex826.cardDataIndex8262.setOnClickListener(v -> selectCardDataByName("酷酷元气牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex827.cardDataIndex8270.setOnClickListener(v -> selectCardDataByName("巫蛊蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex827.cardDataIndex8271.setOnClickListener(v -> selectCardDataByName("暗黑巫蛊蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex827.cardDataIndex8272.setOnClickListener(v -> selectCardDataByName("秘术巫蛊蛇"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex811.cardDataIndex8110.setOnClickListener(v -> selectCardDataByName("咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex811.cardDataIndex8111.setOnClickListener(v -> selectCardDataByName("香醇咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex811.cardDataIndex8112.setOnClickListener(v -> selectCardDataByName("红温咖啡喷壶"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex812.cardDataIndex8120.setOnClickListener(v -> selectCardDataByName("关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex812.cardDataIndex8121.setOnClickListener(v -> selectCardDataByName("福袋关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex812.cardDataIndex8122.setOnClickListener(v -> selectCardDataByName("海鲜关东煮喷锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex813.cardDataIndex8130.setOnClickListener(v -> selectCardDataByName("烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex813.cardDataIndex8131.setOnClickListener(v -> selectCardDataByName("火山烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex813.cardDataIndex8132.setOnClickListener(v -> selectCardDataByName("岩浆烈焰龙"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex814.cardDataIndex8140.setOnClickListener(v -> selectCardDataByName("赫斯提亚神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex814.cardDataIndex8141.setOnClickListener(v -> selectCardDataByName("赫斯提亚圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex814.cardDataIndex8142.setOnClickListener(v -> selectCardDataByName("圣火女神·赫斯提亚"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex814.cardDataIndex8143.setOnClickListener(v -> selectCardDataByName("至尊圣火女神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1011.cardDataIndex10110.setOnClickListener(v -> selectCardDataByName("肥牛火锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1011.cardDataIndex10111.setOnClickListener(v -> selectCardDataByName("酸汤肥牛锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1011.cardDataIndex10112.setOnClickListener(v -> selectCardDataByName("海鲜肥牛锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1012.cardDataIndex10120.setOnClickListener(v -> selectCardDataByName("麻辣香锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1012.cardDataIndex10121.setOnClickListener(v -> selectCardDataByName("孜然羊肉锅"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent7).cardCardDataIndex1012.cardDataIndex10122.setOnClickListener(v -> selectCardDataByName("酱香鱿鱼锅"));

        // 8.炎焱兔
        findViewById(R.id.card_data_index_4_2_11_0).setOnClickListener(v -> selectCardDataByName("炎焱兔"));
        findViewById(R.id.card_data_index_4_2_11_1).setOnClickListener(v -> selectCardDataByName("火火炎焱兔"));
        findViewById(R.id.card_data_index_4_2_11_2).setOnClickListener(v -> selectCardDataByName("燚燚炎焱兔"));
        // 增幅名单
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex511.cardDataIndex5110.setOnClickListener(v -> selectCardDataByName("小火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex511.cardDataIndex5111.setOnClickListener(v -> selectCardDataByName("日光炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex511.cardDataIndex5112.setOnClickListener(v -> selectCardDataByName("太阳能高效炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex512.cardDataIndex5120.setOnClickListener(v -> selectCardDataByName("大火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex512.cardDataIndex5121.setOnClickListener(v -> selectCardDataByName("高能火炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex512.cardDataIndex5122.setOnClickListener(v -> selectCardDataByName("超能燃气炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex513.cardDataIndex5130.setOnClickListener(v -> selectCardDataByName("酒杯灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex513.cardDataIndex5131.setOnClickListener(v -> selectCardDataByName("节能灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex513.cardDataIndex5132.setOnClickListener(v -> selectCardDataByName("高效节能灯"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex514.cardDataIndex5140.setOnClickListener(v -> selectCardDataByName("双子座精灵"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex514.cardDataIndex5141.setOnClickListener(v -> selectCardDataByName("双子座战将"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex514.cardDataIndex5142.setOnClickListener(v -> selectCardDataByName("双子座星宿"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex515.cardDataIndex5150.setOnClickListener(v -> selectCardDataByName("咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex515.cardDataIndex5151.setOnClickListener(v -> selectCardDataByName("萤火咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex515.cardDataIndex5152.setOnClickListener(v -> selectCardDataByName("梦幻咕咕鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex516.cardDataIndex5160.setOnClickListener(v -> selectCardDataByName("暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex516.cardDataIndex5161.setOnClickListener(v -> selectCardDataByName("焰羽暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex516.cardDataIndex5162.setOnClickListener(v -> selectCardDataByName("日耀暖暖鸡"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex517.cardDataIndex5170.setOnClickListener(v -> selectCardDataByName("阿波罗神使"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex517.cardDataIndex5171.setOnClickListener(v -> selectCardDataByName("阿波罗圣神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex517.cardDataIndex5172.setOnClickListener(v -> selectCardDataByName("太阳神·阿波罗"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex517.cardDataIndex5173.setOnClickListener(v -> selectCardDataByName("至尊太阳神"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex518.cardDataIndex5180.setOnClickListener(v -> selectCardDataByName("7周年蜡烛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex519.cardDataIndex5190.setOnClickListener(v -> selectCardDataByName("火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex519.cardDataIndex5191.setOnClickListener(v -> selectCardDataByName("幽蓝火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex519.cardDataIndex5192.setOnClickListener(v -> selectCardDataByName("幻紫火焰牛"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1525.cardDataIndex15250.setOnClickListener(v -> selectCardDataByName("守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1525.cardDataIndex15251.setOnClickListener(v -> selectCardDataByName("蓝焰守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1525.cardDataIndex15252.setOnClickListener(v -> selectCardDataByName("耀金守能汪"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1526.cardDataIndex15260.setOnClickListener(v -> selectCardDataByName("生日帽"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1527.cardDataIndex15270.setOnClickListener(v -> selectCardDataByName("喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1527.cardDataIndex15271.setOnClickListener(v -> selectCardDataByName("靓粉喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1527.cardDataIndex15272.setOnClickListener(v -> selectCardDataByName("炫紫喵喵炉"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1528.cardDataIndex15280.setOnClickListener(v -> selectCardDataByName("扑克牌护罩"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1528.cardDataIndex15281.setOnClickListener(v -> selectCardDataByName("精致黑桃护罩"));
        Objects.requireNonNull(binding.cardCardDataAuxiliaryListContent8).cardCardDataIndex1528.cardDataIndex15282.setOnClickListener(v -> selectCardDataByName("豪华梅花护罩"));

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

        // 顺便添加一个位移动画
        MaterialCardView cardView = findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
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
        findViewById(R.id.FloatButton_CardDataAuxiliaryListIndex_Container).setOnTouchListener(this::setPressAnimation);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }
}