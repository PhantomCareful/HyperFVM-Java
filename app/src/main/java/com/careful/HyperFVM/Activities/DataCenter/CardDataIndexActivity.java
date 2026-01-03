package com.careful.HyperFVM.Activities.DataCenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardDataIndexActivity extends AppCompatActivity {
    private DBHelper dbHelper;

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

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.top_bar_data_center_data_images_index));

        // 添加模糊材质
        setupBlurEffect();

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 防御卡数据查询按钮
        findViewById(R.id.Img_CardDataButton).setOnClickListener(v -> showCardQueryDialog());

        // 给所有防御卡图片设置点击事件，以实现点击卡片查询其数据
        initCardImages();
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
        new MaterialAlertDialogBuilder(this)
                .setTitle("防御卡数据查询")
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
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
    }

}