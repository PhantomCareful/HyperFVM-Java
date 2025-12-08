package com.careful.HyperFVM.ui.DataStation;

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

    private static final int index_both_way_and_three_shot = 5;
    private static final int index_x_pult = 6;
    private static final int index_star_and_pvp = 7;
    private static final int index_auxiliary = 8;
    private static final int index_energy_flower = 9;
    private static final int index_jelly_pudding_and_tray = 10;
    private static final int index_air_force_i = 11;
    private static final int index_eight_directions = 12;
    private static final int index_follow = 13;
    private static final int index_sundry = 14;
    private static final int index_straight_shot = 15;
    private static final int index_ash_bomb = 16;
    private static final int index_ashless_bomb = 17;
    private static final int index_cool_down_and_upgrade_and_drive_fog_and_clear_obstacle_and_special_effect = 18;
    private static final int index_bread_common_and_bread_middle_and_guard_protector = 19;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationCardDataIndexBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DBHelper(requireContext()); // 初始化数据库工具

        //防御卡数据
        root.findViewById(R.id.Img_CardDataButton).setOnClickListener(v -> showCardQueryDialog());

        initAllTextView(root);

        return root;
    }

    // 显示卡片查询弹窗
    private void showCardQueryDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.item_dialog_input_card_data, null);

        // 获取控件（替换为RecyclerView）
        TextInputEditText etCardName = dialogView.findViewById(R.id.et_card_name);
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

    // 定义回调接口
    public interface OnImageClickCallback {
        void onImageClick(int targetPosition); // targetPosition为目标Fragment的索引
    }
    // 持有接口实例（由父Fragment实现）
    private OnImageClickCallback callback;
    // 绑定接口实现（通常在父Fragment中调用）
    public void setOnImageClickCallback(OnImageClickCallback callback) {
        this.callback = callback;
    }

    private void initAllTextView(View root) {
        root.findViewById(R.id.card_data_1).setOnLongClickListener(v -> {
            setIndex(index_both_way_and_three_shot);
            return true;
        });
        root.findViewById(R.id.card_data_2).setOnLongClickListener(v -> {
            setIndex(index_x_pult);
            return true;
        });
        root.findViewById(R.id.card_data_3).setOnLongClickListener(v -> {
            setIndex(index_star_and_pvp);
            return true;
        });
        root.findViewById(R.id.card_data_4).setOnLongClickListener(v -> {
            setIndex(index_auxiliary);
            return true;
        });
        root.findViewById(R.id.card_data_5).setOnLongClickListener(v -> {
            setIndex(index_energy_flower);
            return true;
        });
        root.findViewById(R.id.card_data_6).setOnLongClickListener(v -> {
            setIndex(index_jelly_pudding_and_tray);
            return true;
        });
        root.findViewById(R.id.card_data_7).setOnLongClickListener(v -> {
            setIndex(index_air_force_i);
            return true;
        });
        root.findViewById(R.id.card_data_8).setOnLongClickListener(v -> {
            setIndex(index_eight_directions);
            return true;
        });
        root.findViewById(R.id.card_data_9).setOnLongClickListener(v -> {
            setIndex(index_follow);
            return true;
        });
        root.findViewById(R.id.card_data_10).setOnLongClickListener(v -> {
            setIndex(index_sundry);
            return true;
        });
        root.findViewById(R.id.card_data_11).setOnLongClickListener(v -> {
            setIndex(index_straight_shot);
            return true;
        });
        root.findViewById(R.id.card_data_12).setOnLongClickListener(v -> {
            setIndex(index_ash_bomb);
            return true;
        });
        root.findViewById(R.id.card_data_13).setOnLongClickListener(v -> {
            setIndex(index_ashless_bomb);
            return true;
        });
        root.findViewById(R.id.card_data_14).setOnLongClickListener(v -> {
            setIndex(index_cool_down_and_upgrade_and_drive_fog_and_clear_obstacle_and_special_effect);
            return true;
        });
        root.findViewById(R.id.card_data_15).setOnLongClickListener(v -> {
            setIndex(index_bread_common_and_bread_middle_and_guard_protector);
            return true;
        });
    }

    private void setIndex(int index) {
        if (callback != null) {
            callback.onImageClick(index);
        }
    }
}