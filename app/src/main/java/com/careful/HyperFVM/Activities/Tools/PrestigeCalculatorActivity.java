package com.careful.HyperFVM.Activities.Tools;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class PrestigeCalculatorActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_prestige_calculator_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（本页仅联动模糊层）

    // 存储所有输入框和对应币值（顺序需与输入框一一对应）
    private final List<TextInputEditText> inputEditTexts = new ArrayList<>();
    private final long[] denominations = {1, 5, 10, 50, 100, 200, 500, 1000, 2000, 3000, 4000, 5000, 8000, 10000, 600, 500};
    private TextInputEditText etCoinChange; // 零钱输入框（单独处理，币值为1）
    private TextView tvTotal; // 结果显示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_prestige_calculator);

        // 初始化视图
        initViews();

        // 初始化各种装饰效果
        initDecoration();

        // 给所有输入框设置文本变化监听器
        setupTextWatchers();
    }

    // 初始化所有输入框和结果视图
    private void initViews() {
        // 初始化零钱输入框
        etCoinChange = findViewById(R.id.et_coin_change);

        // 初始化各币值礼包输入框（顺序与denominations数组对应）
        inputEditTexts.add(findViewById(R.id.et_1));
        inputEditTexts.add(findViewById(R.id.et_5));
        inputEditTexts.add(findViewById(R.id.et_10));
        inputEditTexts.add(findViewById(R.id.et_50));
        inputEditTexts.add(findViewById(R.id.et_100));
        inputEditTexts.add(findViewById(R.id.et_200));
        inputEditTexts.add(findViewById(R.id.et_500));
        inputEditTexts.add(findViewById(R.id.et_1000));
        inputEditTexts.add(findViewById(R.id.et_2000));
        inputEditTexts.add(findViewById(R.id.et_3000));
        inputEditTexts.add(findViewById(R.id.et_4000));
        inputEditTexts.add(findViewById(R.id.et_5000));
        inputEditTexts.add(findViewById(R.id.et_8000));
        inputEditTexts.add(findViewById(R.id.et_10000));
        inputEditTexts.add(findViewById(R.id.et_month_card_gift));
        inputEditTexts.add(findViewById(R.id.et_luxury_welfare_gift));

        // 初始化结果显示
        tvTotal = findViewById(R.id.total);
    }

    // 给所有输入框设置文本变化监听器（实时计算）
    private void setupTextWatchers() {
        // 零钱输入框监听器
        etCoinChange.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 各礼包输入框监听器
        for (TextInputEditText et : inputEditTexts) {
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateTotal();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // 计算威望总数
    @SuppressLint("SetTextI18n")
    private void calculateTotal() {
        long total = 0;

        try {
            // 1. 计算零钱（币值为1）
            String coinChangeStr = Objects.requireNonNull(etCoinChange.getText()).toString().trim();
            long coinChange = coinChangeStr.isEmpty() ? 0 : Long.parseLong(coinChangeStr);
            total = Math.addExact(total, coinChange);

            // 2. 计算各礼包总威望（输入框与币值数组顺序对应），用*Exact系列方法让溢出抛出异常
            for (int i = 0; i < inputEditTexts.size(); i++) {
                String countStr = Objects.requireNonNull(inputEditTexts.get(i).getText()).toString().trim();
                long count = countStr.isEmpty() ? 0 : Long.parseLong(countStr);
                total = Math.addExact(total, Math.multiplyExact(count, denominations[i]));
            }
        } catch (NumberFormatException | ArithmeticException e) {
            // 输入数值或计算结果超出long范围：显示兜底文案
            tvTotal.setText("再玩就玩坏了");
            return;
        }

        // 更新结果显示
        tvTotal.setText("总储备：" + total);
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        TextView total = findViewById(R.id.total);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 90);// 模糊层高度覆盖标题与总储备两行
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout prestige_calculator_container = findViewById(R.id.prestige_calculator_container);
        InsetsUtil.setMarginHorizontal(this, prestige_calculator_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) prestige_calculator_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            prestige_calculator_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) total.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            total.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：本页只联动模糊背景层（topBarBottom/topBar 传0跳过）
        nestedScrollUtil = NestedScrollUtil.attach(rootView,
                R.id.scrollView, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_prestige_calculator).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}