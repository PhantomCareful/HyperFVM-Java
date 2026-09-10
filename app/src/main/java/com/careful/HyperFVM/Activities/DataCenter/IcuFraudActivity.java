package com.careful.HyperFVM.Activities.DataCenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class IcuFraudActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_icu_fraud_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（本页仅联动模糊层）

    private TextView fraud_info_qq;
    private TextView fraud_info_record_time;
    private TextView fraud_info_last_fraud_time;
    private TextView fraud_info_fraud_count;
    private TextView fraud_info_fraud_amount;
    private TextView fraud_info_uncertain_fraud_count;

    private TextView title_victim_info;
    private LinearLayout icu_victim_info_container;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_icu_fraud);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化组件
        initViews();

        IcuHelper.FraudResult result = getIntent().getParcelableExtra("FraudResult");

        if (result != null) {
            fraud_info_qq.setText("QQ号：" + result.qq);
            fraud_info_record_time.setText("录入时间：" + result.recordTime);
            fraud_info_last_fraud_time.setText("上一次行骗时间：" + result.lastFraudTime);
            fraud_info_fraud_count.setText("行骗次数：" + result.fraudCount + "次");
            fraud_info_fraud_amount.setText("行骗总金额：" + result.fraudAmount);
            fraud_info_uncertain_fraud_count.setText("不确定金额的行骗次数：" + result.uncertainAmountCount + "次");

            List<IcuHelper.VictimInfo> victims = result.victims;
            if (victims.isEmpty()) {
                title_victim_info.setVisibility(View.GONE);
            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(this);

                title_victim_info.setText("行骗记录(共" + victims.size() + "条)");
                icu_victim_info_container.removeAllViews();
                for (int i = 0; i < victims.size(); i++) {
                    CardView cardView = (CardView) layoutInflater.inflate(R.layout.item_victim_info_card, icu_victim_info_container, false);
                    // 绑定好需要用到的组件
                    TextView victim_info_qq = cardView.findViewById(R.id.victim_info_qq);
                    TextView victim_info_platform = cardView.findViewById(R.id.victim_info_platform);
                    TextView victim_info_server = cardView.findViewById(R.id.victim_info_server);
                    TextView victim_info_fraud_time = cardView.findViewById(R.id.victim_info_fraud_time);
                    TextView victim_info_amount = cardView.findViewById(R.id.victim_info_amount);
                    TextView victim_info_remark = cardView.findViewById(R.id.victim_info_remark);

                    victim_info_qq.setText("受害者QQ：" + victims.get(i).victim);
                    victim_info_platform.setText("受害者所在平台：" + victims.get(i).platform);
                    victim_info_server.setText("受害者所在区服：" + victims.get(i).server);
                    victim_info_fraud_time.setText("被骗日期：" + victims.get(i).fraudTime);
                    victim_info_amount.setText("被骗金额：" + victims.get(i).amount);
                    victim_info_remark.setText("备注：" + victims.get(i).remark);

                    icu_victim_info_container.addView(cardView);
                }
            }
        }
    }

    private void initViews() {
        fraud_info_qq = findViewById(R.id.fraud_info_qq);
        fraud_info_record_time = findViewById(R.id.fraud_info_record_time);
        fraud_info_last_fraud_time = findViewById(R.id.fraud_info_last_fraud_time);
        fraud_info_fraud_count = findViewById(R.id.fraud_info_fraud_count);
        fraud_info_fraud_amount = findViewById(R.id.fraud_info_fraud_amount);
        fraud_info_uncertain_fraud_count = findViewById(R.id.fraud_info_uncertain_fraud_count);

        title_victim_info = findViewById(R.id.title_victim_info);
        icu_victim_info_container = findViewById(R.id.icu_victim_info_container);
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("DiscouragedApi")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout icu_fraud_container = findViewById(R.id.icu_fraud_container);
        InsetsUtil.setMarginHorizontal(this, icu_fraud_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) icu_fraud_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            icu_fraud_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：本页只联动模糊背景层（topBarBottom/topBar 传0跳过）
        nestedScrollUtil = NestedScrollUtil.attach(rootView,
                R.id.scrollView, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();
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