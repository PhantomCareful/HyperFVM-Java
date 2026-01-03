package com.careful.HyperFVM.Fragments.DataCenter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.Activities.DetailCardData.CardData_3_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_4_Activity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_1_Activity;
import com.careful.HyperFVM.Activities.DetailCardData.CardData_2_Activity;
import com.careful.HyperFVM.databinding.FragmentDataStationCardDataIndexBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardDataIndexFragment extends Fragment {

    private FragmentDataStationCardDataIndexBinding binding;

    private DBHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationCardDataIndexBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DBHelper(requireContext()); // 初始化数据库工具

        //防御卡数据
        root.findViewById(R.id.Img_CardDataButton).setOnClickListener(v -> showCardQueryDialog());

        return root;
    }

    // 显示卡片查询弹窗
    private void showCardQueryDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
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
        suggestionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        suggestionList.setAdapter(adapter);
        //suggestionList.setItemAnimator(new MaterialItemAnimator()); // 添加强调动画

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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("防御卡数据查询")
                .setView(dialogView)
                .setPositiveButton("查询", (dialog, which) -> {
                    String cardName = Objects.requireNonNull(etCardName.getText()).toString().trim();
                    if (cardName.isEmpty()) {
                        Toast.makeText(requireContext(), "请输入卡片名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String tableName = dbHelper.getCardTable(cardName);
                    if (tableName == null) {
                        Toast.makeText(requireContext(), "未找到该卡片", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 跳转详情页
                    Intent intent = switch (tableName) {
                        case "card_data_1" ->
                                new Intent(requireActivity(), CardData_1_Activity.class);
                        case "card_data_2" ->
                                new Intent(requireActivity(), CardData_2_Activity.class);
                        case "card_data_3" ->
                                new Intent(requireActivity(), CardData_3_Activity.class);
                        case "card_data_4" ->
                                new Intent(requireActivity(), CardData_4_Activity.class);
                        default -> null;
                    };
                    if (intent != null) {
                        intent.putExtra("name", cardName);
                        intent.putExtra("table", tableName);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}