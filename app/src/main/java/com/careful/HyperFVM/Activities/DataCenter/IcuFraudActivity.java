package com.careful.HyperFVM.Activities.DataCenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class IcuFraudActivity extends BaseActivity {
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

                    victim_info_qq.setText("QQ号：" + victims.get(i).victim);
                    victim_info_platform.setText("所在平台：" + victims.get(i).platform);
                    victim_info_server.setText("所在区服：" + victims.get(i).server);
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
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout icu_fraud_container = findViewById(R.id.icu_fraud_container);
        InsetsUtil.setMarginHorizontal(this, icu_fraud_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) icu_fraud_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            icu_fraud_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);
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
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }
}