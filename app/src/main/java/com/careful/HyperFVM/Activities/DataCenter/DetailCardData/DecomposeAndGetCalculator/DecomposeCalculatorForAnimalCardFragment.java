package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.DecomposeAndGetCalculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDecomposeCalculatorForAnimalCardBinding;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;

public class DecomposeCalculatorForAnimalCardFragment extends Fragment {
    private static final String KEY_CALCULATION_STATES = "calculation_states";

    private View root;

    private String decomposeItemName;
    private String[] imageIdsArray;
    private int[] decomposeDataArray;

    private TextView decompose_calculator_for_animal_card_result;

    private CardView decompose_calculator_for_animal_card_card_container;
    private LinearLayout decompose_calculator_for_animal_card_card_1_container;
    private CheckBox decompose_calculator_for_animal_card_card_1_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_card_2_container;
    private CheckBox decompose_calculator_for_animal_card_card_2_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_card_3_container;
    private CheckBox decompose_calculator_for_animal_card_card_3_checkbox;

    private CardView decompose_calculator_for_animal_card_skill_container;
    private LinearLayout decompose_calculator_for_animal_card_skill_1_container;
    private CheckBox decompose_calculator_for_animal_card_skill_1_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_skill_2_container;
    private CheckBox decompose_calculator_for_animal_card_skill_2_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_skill_3_container;
    private CheckBox decompose_calculator_for_animal_card_skill_3_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_skill_4_container;
    private CheckBox decompose_calculator_for_animal_card_skill_4_checkbox;

    private CardView decompose_calculator_for_animal_card_transfer_container;
    private LinearLayout decompose_calculator_for_animal_card_transfer_1_a_container;
    private CheckBox decompose_calculator_for_animal_card_transfer_1_a_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_transfer_1_b_container;
    private CheckBox decompose_calculator_for_animal_card_transfer_1_b_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_transfer_2_a_container;
    private CheckBox decompose_calculator_for_animal_card_transfer_2_a_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_transfer_2_b_container;
    private CheckBox decompose_calculator_for_animal_card_transfer_2_b_checkbox;
    private LinearLayout decompose_calculator_for_animal_card_transfer_2_c_container;
    private CheckBox decompose_calculator_for_animal_card_transfer_2_c_checkbox;

    public DecomposeCalculatorForAnimalCardFragment newInstance(String decomposeItemName, String[] imageIdsArray, int[] decomposeDataArray) {
        DecomposeCalculatorForAnimalCardFragment fragment = new DecomposeCalculatorForAnimalCardFragment();
        Bundle args = new Bundle();
        args.putString("decompose_item_name", decomposeItemName);
        args.putStringArray("image_ids", imageIdsArray);
        args.putIntArray("decompose_data", decomposeDataArray);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 初始化binding
        FragmentDecomposeCalculatorForAnimalCardBinding binding = FragmentDecomposeCalculatorForAnimalCardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        if (getArguments() != null) {
            decomposeItemName = getArguments().getString("decompose_item_name");
            imageIdsArray = getArguments().getStringArray("image_ids");
            decomposeDataArray = getArguments().getIntArray("decompose_data");
        }

        // 初始化所有组件
        initViews();

        // 初始化各种装饰效果
        initDecoration();

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            decompose_calculator_for_animal_card_result.setText("未显示的物品代表不可分解\n分解勾选的物品可获得 0 个" + decomposeItemName);
        } else {
            decompose_calculator_for_animal_card_result.setText("未显示的物品代表不可分解。分解勾选的物品可获得 0 个" + decomposeItemName);
        }

        // 为每个组件分配对应的图片
        // 如果对应的分解/兑换数据为0，则隐藏对应的Container
        ImageView imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_card_1_image);
        String imageIdStr = imageIdsArray[0];
        int imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_card_2_image);
        imageIdStr = imageIdsArray[1];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_card_3_image);
        imageIdStr = imageIdsArray[2];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_1_image);
        imageIdStr = imageIdsArray[3];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_2_image);
        imageIdStr = imageIdsArray[4];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_3_image);
        imageIdStr = imageIdsArray[5];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_4_image);
        imageIdStr = imageIdsArray[6];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_a_image);
        imageIdStr = imageIdsArray[7];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_b_image);
        imageIdStr = imageIdsArray[8];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_a_image);
        imageIdStr = imageIdsArray[9];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_b_image);
        imageIdStr = imageIdsArray[10];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_c_image);
        imageIdStr = imageIdsArray[11];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        // 如果某个物品不能被分解/兑换，则隐藏对应的组件
        if (decomposeDataArray[0] + decomposeDataArray[1] + decomposeDataArray[2] == 0) {
            decompose_calculator_for_animal_card_card_container.setVisibility(View.GONE);
        } else {
            if (decomposeDataArray[0] == 0) {
                decompose_calculator_for_animal_card_card_1_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[1] == 0) {
                decompose_calculator_for_animal_card_card_2_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[2] == 0) {
                decompose_calculator_for_animal_card_card_3_container.setVisibility(View.GONE);
            }
        }
        if (decomposeDataArray[3] + decomposeDataArray[4] + decomposeDataArray[5] + decomposeDataArray[6] == 0) {
            decompose_calculator_for_animal_card_skill_container.setVisibility(View.GONE);
        }
        if (decomposeDataArray[7] + decomposeDataArray[8] + decomposeDataArray[9] + decomposeDataArray[10] + decomposeDataArray[11] == 0) {
            decompose_calculator_for_animal_card_transfer_container.setVisibility(View.GONE);
        } else {
            if (decomposeDataArray[7] == 0) {
                decompose_calculator_for_animal_card_transfer_1_a_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[8] == 0) {
                decompose_calculator_for_animal_card_transfer_1_b_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[9] == 0) {
                decompose_calculator_for_animal_card_transfer_2_a_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[10] == 0) {
                decompose_calculator_for_animal_card_transfer_2_b_container.setVisibility(View.GONE);
            }
            if (decomposeDataArray[11] == 0) {
                decompose_calculator_for_animal_card_transfer_2_c_container.setVisibility(View.GONE);
            }
        }

        // 设置点击事件
        decompose_calculator_for_animal_card_card_1_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_card_1_checkbox.setChecked(!decompose_calculator_for_animal_card_card_1_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_card_2_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_card_2_checkbox.setChecked(!decompose_calculator_for_animal_card_card_2_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_card_3_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_card_3_checkbox.setChecked(!decompose_calculator_for_animal_card_card_3_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_skill_1_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_skill_1_checkbox.setChecked(!decompose_calculator_for_animal_card_skill_1_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_skill_2_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_skill_2_checkbox.setChecked(!decompose_calculator_for_animal_card_skill_2_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_skill_3_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_skill_3_checkbox.setChecked(!decompose_calculator_for_animal_card_skill_3_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_skill_4_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_skill_4_checkbox.setChecked(!decompose_calculator_for_animal_card_skill_4_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_transfer_1_a_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_transfer_1_a_checkbox.setChecked(!decompose_calculator_for_animal_card_transfer_1_a_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_transfer_1_b_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_transfer_1_b_checkbox.setChecked(!decompose_calculator_for_animal_card_transfer_1_b_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_transfer_2_a_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_transfer_2_a_checkbox.setChecked(!decompose_calculator_for_animal_card_transfer_2_a_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_transfer_2_b_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_transfer_2_b_checkbox.setChecked(!decompose_calculator_for_animal_card_transfer_2_b_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        decompose_calculator_for_animal_card_transfer_2_c_container.setOnClickListener(v -> {
            decompose_calculator_for_animal_card_transfer_2_c_checkbox.setChecked(!decompose_calculator_for_animal_card_transfer_2_c_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateDecomposeResult();
        });

        // recreate后恢复
        if (savedInstanceState != null) {
            boolean[] states = savedInstanceState.getBooleanArray(KEY_CALCULATION_STATES);
            if (states != null && states.length == 12) {
                decompose_calculator_for_animal_card_card_1_checkbox.setChecked(states[0]);
                decompose_calculator_for_animal_card_card_2_checkbox.setChecked(states[1]);
                decompose_calculator_for_animal_card_card_3_checkbox.setChecked(states[2]);
                decompose_calculator_for_animal_card_skill_1_checkbox.setChecked(states[3]);
                decompose_calculator_for_animal_card_skill_2_checkbox.setChecked(states[4]);
                decompose_calculator_for_animal_card_skill_3_checkbox.setChecked(states[5]);
                decompose_calculator_for_animal_card_skill_4_checkbox.setChecked(states[6]);
                decompose_calculator_for_animal_card_transfer_1_a_checkbox.setChecked(states[7]);
                decompose_calculator_for_animal_card_transfer_1_b_checkbox.setChecked(states[8]);
                decompose_calculator_for_animal_card_transfer_2_a_checkbox.setChecked(states[9]);
                decompose_calculator_for_animal_card_transfer_2_b_checkbox.setChecked(states[10]);
                decompose_calculator_for_animal_card_transfer_2_c_checkbox.setChecked(states[11]);
                // 重新计算结果并更新文本
                calculateDecomposeResult();
            }
        }

        return root;
    }

    private void initViews() {
        decompose_calculator_for_animal_card_result = root.findViewById(R.id.decompose_calculator_for_animal_card_result);

        decompose_calculator_for_animal_card_card_container = root.findViewById(R.id.decompose_calculator_for_animal_card_card_container);
        decompose_calculator_for_animal_card_card_1_container = root.findViewById(R.id.decompose_calculator_for_animal_card_card_1_container);
        decompose_calculator_for_animal_card_card_1_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_card_1_checkbox);
        decompose_calculator_for_animal_card_card_2_container = root.findViewById(R.id.decompose_calculator_for_animal_card_card_2_container);
        decompose_calculator_for_animal_card_card_2_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_card_2_checkbox);
        decompose_calculator_for_animal_card_card_3_container = root.findViewById(R.id.decompose_calculator_for_animal_card_card_3_container);
        decompose_calculator_for_animal_card_card_3_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_card_3_checkbox);

        decompose_calculator_for_animal_card_skill_container = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_container);
        decompose_calculator_for_animal_card_skill_1_container = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_1_container);
        decompose_calculator_for_animal_card_skill_1_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_1_checkbox);
        decompose_calculator_for_animal_card_skill_2_container = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_2_container);
        decompose_calculator_for_animal_card_skill_2_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_2_checkbox);
        decompose_calculator_for_animal_card_skill_3_container = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_3_container);
        decompose_calculator_for_animal_card_skill_3_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_3_checkbox);
        decompose_calculator_for_animal_card_skill_4_container = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_4_container);
        decompose_calculator_for_animal_card_skill_4_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_skill_4_checkbox);

        decompose_calculator_for_animal_card_transfer_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_container);
        decompose_calculator_for_animal_card_transfer_1_a_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_a_container);
        decompose_calculator_for_animal_card_transfer_1_a_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_a_checkbox);
        decompose_calculator_for_animal_card_transfer_1_b_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_b_container);
        decompose_calculator_for_animal_card_transfer_1_b_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_1_b_checkbox);
        decompose_calculator_for_animal_card_transfer_2_a_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_a_container);
        decompose_calculator_for_animal_card_transfer_2_a_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_a_checkbox);
        decompose_calculator_for_animal_card_transfer_2_b_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_b_container);
        decompose_calculator_for_animal_card_transfer_2_b_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_b_checkbox);
        decompose_calculator_for_animal_card_transfer_2_c_container = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_c_container);
        decompose_calculator_for_animal_card_transfer_2_c_checkbox = root.findViewById(R.id.decompose_calculator_for_animal_card_transfer_2_c_checkbox);
    }

    @SuppressLint("SetTextI18n")
    private void calculateDecomposeResult() {
        int decomposeResult = 0;
        if (decompose_calculator_for_animal_card_card_1_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[0];
        }
        if (decompose_calculator_for_animal_card_card_2_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[1];
        }
        if (decompose_calculator_for_animal_card_card_3_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[2];
        }
        if (decompose_calculator_for_animal_card_skill_1_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[3];
        }
        if (decompose_calculator_for_animal_card_skill_2_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[4];
        }
        if (decompose_calculator_for_animal_card_skill_3_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[5];
        }
        if (decompose_calculator_for_animal_card_skill_4_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[6];
        }
        if (decompose_calculator_for_animal_card_transfer_1_a_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[7];
        }
        if (decompose_calculator_for_animal_card_transfer_1_b_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[8];
        }
        if (decompose_calculator_for_animal_card_transfer_2_a_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[9];
        }
        if (decompose_calculator_for_animal_card_transfer_2_b_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[10];
        }
        if (decompose_calculator_for_animal_card_transfer_2_c_checkbox.isChecked()) {
            decomposeResult += decomposeDataArray[11];
        }

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            decompose_calculator_for_animal_card_result.setText("未显示的物品代表不可分解\n分解勾选的物品可获得 " + decomposeResult + " 个" + decomposeItemName);
        } else {
            decompose_calculator_for_animal_card_result.setText("未显示的物品代表不可分解。分解勾选的物品可获得 " + decomposeResult + " 个" + decomposeItemName);
        }
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
        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewResult), root.findViewById(R.id.targetView));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean[] states = new boolean[12];
        states[0] = decompose_calculator_for_animal_card_card_1_checkbox.isChecked();
        states[1] = decompose_calculator_for_animal_card_card_2_checkbox.isChecked();
        states[2] = decompose_calculator_for_animal_card_card_3_checkbox.isChecked();
        states[3] = decompose_calculator_for_animal_card_skill_1_checkbox.isChecked();
        states[4] = decompose_calculator_for_animal_card_skill_2_checkbox.isChecked();
        states[5] = decompose_calculator_for_animal_card_skill_3_checkbox.isChecked();
        states[6] = decompose_calculator_for_animal_card_skill_4_checkbox.isChecked();
        states[7] = decompose_calculator_for_animal_card_transfer_1_a_checkbox.isChecked();
        states[8] = decompose_calculator_for_animal_card_transfer_1_b_checkbox.isChecked();
        states[9] = decompose_calculator_for_animal_card_transfer_2_a_checkbox.isChecked();
        states[10] = decompose_calculator_for_animal_card_transfer_2_b_checkbox.isChecked();
        states[11] = decompose_calculator_for_animal_card_transfer_2_c_checkbox.isChecked();
        outState.putBooleanArray(KEY_CALCULATION_STATES, states);
    }
}