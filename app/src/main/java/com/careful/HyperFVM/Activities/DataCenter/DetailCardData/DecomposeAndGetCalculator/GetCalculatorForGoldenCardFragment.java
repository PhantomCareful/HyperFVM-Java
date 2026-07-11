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
import com.careful.HyperFVM.databinding.FragmentGetCalculatorForGoldenCardBinding;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;

public class GetCalculatorForGoldenCardFragment extends Fragment {
    private static final String KEY_CALCULATION_STATES = "calculation_states";
    private BlurUtil blurUtil;

    private View root;

    private String getItemName;
    private String[] imageIdsArray;
    private int[] getDataArray;

    private TextView get_calculator_for_golden_card_result;

    private CardView get_calculator_for_golden_card_card_container;
    private LinearLayout get_calculator_for_golden_card_card_1_container;
    private CheckBox get_calculator_for_golden_card_card_1_checkbox;
    private LinearLayout get_calculator_for_golden_card_card_2_container;
    private CheckBox get_calculator_for_golden_card_card_2_checkbox;
    private LinearLayout get_calculator_for_golden_card_card_3_container;
    private CheckBox get_calculator_for_golden_card_card_3_checkbox;
    private LinearLayout get_calculator_for_golden_card_card_4_container;
    private CheckBox get_calculator_for_golden_card_card_4_checkbox;

    private CardView get_calculator_for_golden_card_skill_container;
    private LinearLayout get_calculator_for_golden_card_skill_1_container;
    private CheckBox get_calculator_for_golden_card_skill_1_checkbox;
    private LinearLayout get_calculator_for_golden_card_skill_2_container;
    private CheckBox get_calculator_for_golden_card_skill_2_checkbox;
    private LinearLayout get_calculator_for_golden_card_skill_3_container;
    private CheckBox get_calculator_for_golden_card_skill_3_checkbox;
    private LinearLayout get_calculator_for_golden_card_skill_4_container;
    private CheckBox get_calculator_for_golden_card_skill_4_checkbox;

    private CardView get_calculator_for_golden_card_transfer_container;
    private LinearLayout get_calculator_for_golden_card_transfer_1_a_container;
    private CheckBox get_calculator_for_golden_card_transfer_1_a_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_1_b_container;
    private CheckBox get_calculator_for_golden_card_transfer_1_b_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_1_c_container;
    private CheckBox get_calculator_for_golden_card_transfer_1_c_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_2_a_container;
    private CheckBox get_calculator_for_golden_card_transfer_2_a_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_2_b_container;
    private CheckBox get_calculator_for_golden_card_transfer_2_b_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_2_c_container;
    private CheckBox get_calculator_for_golden_card_transfer_2_c_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_3_a_container;
    private CheckBox get_calculator_for_golden_card_transfer_3_a_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_3_b_container;
    private CheckBox get_calculator_for_golden_card_transfer_3_b_checkbox;
    private LinearLayout get_calculator_for_golden_card_transfer_3_c_container;
    private CheckBox get_calculator_for_golden_card_transfer_3_c_checkbox;

    private CardView get_calculator_for_golden_card_compose_container;
    private LinearLayout get_calculator_for_golden_card_compose_container_;
    private CheckBox get_calculator_for_golden_card_compose_checkbox;

    public GetCalculatorForGoldenCardFragment newInstance(String decomposeItemName, String[] imageIdsArray, int[] getDataArray) {
        GetCalculatorForGoldenCardFragment fragment = new GetCalculatorForGoldenCardFragment();
        Bundle args = new Bundle();
        args.putString("get_item_name", decomposeItemName);
        args.putStringArray("image_ids", imageIdsArray);
        args.putIntArray("get_data", getDataArray);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 初始化binding
        FragmentGetCalculatorForGoldenCardBinding binding = FragmentGetCalculatorForGoldenCardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        if (getArguments() != null) {
            getItemName = getArguments().getString("get_item_name");
            imageIdsArray = getArguments().getStringArray("image_ids");
            getDataArray = getArguments().getIntArray("get_data");
        }

        // 初始化所有组件
        initViews();

        // 初始化各种装饰效果
        initDecoration();

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            get_calculator_for_golden_card_result.setText("未显示的物品代表不可兑换\n兑换勾选的物品需消耗 0 个" + getItemName);
        } else {
            get_calculator_for_golden_card_result.setText("未显示的物品代表不可兑换。兑换勾选的物品需消耗 0 个" + getItemName);
        }

        // 为每个组件分配对应的图片
        // 如果对应的分解/兑换数据为0，则隐藏对应的Container
        ImageView imageView = root.findViewById(R.id.get_calculator_for_golden_card_card_1_image);
        String imageIdStr = imageIdsArray[0];
        int imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_card_2_image);
        imageIdStr = imageIdsArray[1];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_card_3_image);
        imageIdStr = imageIdsArray[2];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_card_4_image);
        imageIdStr = imageIdsArray[3];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_skill_1_image);
        imageIdStr = imageIdsArray[4];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_skill_2_image);
        imageIdStr = imageIdsArray[5];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_skill_3_image);
        imageIdStr = imageIdsArray[6];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_skill_4_image);
        imageIdStr = imageIdsArray[7];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_a_image);
        imageIdStr = imageIdsArray[8];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_b_image);
        imageIdStr = imageIdsArray[9];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_c_image);
        imageIdStr = imageIdsArray[10];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_a_image);
        imageIdStr = imageIdsArray[11];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_b_image);
        imageIdStr = imageIdsArray[12];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_c_image);
        imageIdStr = imageIdsArray[13];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_a_image);
        imageIdStr = imageIdsArray[14];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_b_image);
        imageIdStr = imageIdsArray[15];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_c_image);
        imageIdStr = imageIdsArray[16];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        imageView = root.findViewById(R.id.get_calculator_for_golden_card_compose_image);
        imageIdStr = imageIdsArray[17];
        imageResId = getResources().getIdentifier(
                imageIdStr,
                "drawable",
                requireActivity().getPackageName()
        );
        imageView.setImageResource(imageResId);

        // 如果某个物品不能被分解/兑换，则隐藏对应的组件
        if (getDataArray[0] + getDataArray[1] + getDataArray[2] + getDataArray[3] == 0) {
            get_calculator_for_golden_card_card_container.setVisibility(View.GONE);
        } else {
            if (getDataArray[0] == 0) {
                get_calculator_for_golden_card_card_1_container.setVisibility(View.GONE);
            }
            if (getDataArray[1] == 0) {
                get_calculator_for_golden_card_card_2_container.setVisibility(View.GONE);
            }
            if (getDataArray[2] == 0) {
                get_calculator_for_golden_card_card_3_container.setVisibility(View.GONE);
            }
            if (getDataArray[3] == 0) {
                get_calculator_for_golden_card_card_4_container.setVisibility(View.GONE);
            }
        }
        if (getDataArray[4] + getDataArray[5] + getDataArray[6] + getDataArray[7] == 0) {
            get_calculator_for_golden_card_skill_container.setVisibility(View.GONE);
        }
        if (getDataArray[8] + getDataArray[9] + getDataArray[10] + getDataArray[11] + getDataArray[12] + getDataArray[13] + getDataArray[14] + getDataArray[15] + getDataArray[16] == 0) {
            get_calculator_for_golden_card_transfer_container.setVisibility(View.GONE);
        } else {
            if (getDataArray[8] == 0) {
                get_calculator_for_golden_card_transfer_1_a_container.setVisibility(View.GONE);
            }
            if (getDataArray[9] == 0) {
                get_calculator_for_golden_card_transfer_1_b_container.setVisibility(View.GONE);
            }
            if (getDataArray[10] == 0) {
                get_calculator_for_golden_card_transfer_1_c_container.setVisibility(View.GONE);
            }
            if (getDataArray[11] == 0) {
                get_calculator_for_golden_card_transfer_2_a_container.setVisibility(View.GONE);
            }
            if (getDataArray[12] == 0) {
                get_calculator_for_golden_card_transfer_2_b_container.setVisibility(View.GONE);
            }
            if (getDataArray[13] == 0) {
                get_calculator_for_golden_card_transfer_2_c_container.setVisibility(View.GONE);
            }
            if (getDataArray[14] == 0) {
                get_calculator_for_golden_card_transfer_3_a_container.setVisibility(View.GONE);
            }
            if (getDataArray[15] == 0) {
                get_calculator_for_golden_card_transfer_3_b_container.setVisibility(View.GONE);
            }
            if (getDataArray[16] == 0) {
                get_calculator_for_golden_card_transfer_3_c_container.setVisibility(View.GONE);
            }
        }
        if (getDataArray[17] == 0) {
            get_calculator_for_golden_card_compose_container.setVisibility(View.GONE);
        }

        // 设置点击事件
        get_calculator_for_golden_card_card_1_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_card_1_checkbox.setChecked(!get_calculator_for_golden_card_card_1_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_card_2_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_card_2_checkbox.setChecked(!get_calculator_for_golden_card_card_2_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_card_3_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_card_3_checkbox.setChecked(!get_calculator_for_golden_card_card_3_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_card_4_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_card_4_checkbox.setChecked(!get_calculator_for_golden_card_card_4_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_skill_1_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_skill_1_checkbox.setChecked(!get_calculator_for_golden_card_skill_1_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_skill_2_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_skill_2_checkbox.setChecked(!get_calculator_for_golden_card_skill_2_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_skill_3_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_skill_3_checkbox.setChecked(!get_calculator_for_golden_card_skill_3_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_skill_4_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_skill_4_checkbox.setChecked(!get_calculator_for_golden_card_skill_4_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_1_a_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_1_a_checkbox.setChecked(!get_calculator_for_golden_card_transfer_1_a_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_1_b_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_1_b_checkbox.setChecked(!get_calculator_for_golden_card_transfer_1_b_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_1_c_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_1_c_checkbox.setChecked(!get_calculator_for_golden_card_transfer_1_c_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_2_a_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_2_a_checkbox.setChecked(!get_calculator_for_golden_card_transfer_2_a_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_2_b_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_2_b_checkbox.setChecked(!get_calculator_for_golden_card_transfer_2_b_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_2_c_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_2_c_checkbox.setChecked(!get_calculator_for_golden_card_transfer_2_c_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_3_a_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_3_a_checkbox.setChecked(!get_calculator_for_golden_card_transfer_3_a_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_3_b_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_3_b_checkbox.setChecked(!get_calculator_for_golden_card_transfer_3_b_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_transfer_3_c_container.setOnClickListener(v -> {
            get_calculator_for_golden_card_transfer_3_c_checkbox.setChecked(!get_calculator_for_golden_card_transfer_3_c_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        get_calculator_for_golden_card_compose_container_.setOnClickListener(v -> {
            get_calculator_for_golden_card_compose_checkbox.setChecked(!get_calculator_for_golden_card_compose_checkbox.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            calculateGetResult();
        });

        // recreate后恢复
        if (savedInstanceState != null) {
            boolean[] states = savedInstanceState.getBooleanArray(KEY_CALCULATION_STATES);
            if (states != null && states.length == 18) {
                get_calculator_for_golden_card_card_1_checkbox.setChecked(states[0]);
                get_calculator_for_golden_card_card_2_checkbox.setChecked(states[1]);
                get_calculator_for_golden_card_card_3_checkbox.setChecked(states[2]);
                get_calculator_for_golden_card_card_4_checkbox.setChecked(states[3]);
                get_calculator_for_golden_card_skill_1_checkbox.setChecked(states[4]);
                get_calculator_for_golden_card_skill_2_checkbox.setChecked(states[5]);
                get_calculator_for_golden_card_skill_3_checkbox.setChecked(states[6]);
                get_calculator_for_golden_card_skill_4_checkbox.setChecked(states[7]);
                get_calculator_for_golden_card_transfer_1_a_checkbox.setChecked(states[8]);
                get_calculator_for_golden_card_transfer_1_b_checkbox.setChecked(states[9]);
                get_calculator_for_golden_card_transfer_1_c_checkbox.setChecked(states[10]);
                get_calculator_for_golden_card_transfer_2_a_checkbox.setChecked(states[11]);
                get_calculator_for_golden_card_transfer_2_b_checkbox.setChecked(states[12]);
                get_calculator_for_golden_card_transfer_2_c_checkbox.setChecked(states[13]);
                get_calculator_for_golden_card_transfer_3_a_checkbox.setChecked(states[14]);
                get_calculator_for_golden_card_transfer_3_b_checkbox.setChecked(states[15]);
                get_calculator_for_golden_card_transfer_3_c_checkbox.setChecked(states[16]);
                get_calculator_for_golden_card_compose_checkbox.setChecked(states[17]);
                // 重新计算结果并更新文本
                calculateGetResult();
            }
        }

        return root;
    }

    private void initViews() {
        get_calculator_for_golden_card_result = root.findViewById(R.id.get_calculator_for_golden_card_result);

        get_calculator_for_golden_card_card_container = root.findViewById(R.id.get_calculator_for_golden_card_card_container);
        get_calculator_for_golden_card_card_1_container = root.findViewById(R.id.get_calculator_for_golden_card_card_1_container);
        get_calculator_for_golden_card_card_1_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_card_1_checkbox);
        get_calculator_for_golden_card_card_2_container = root.findViewById(R.id.get_calculator_for_golden_card_card_2_container);
        get_calculator_for_golden_card_card_2_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_card_2_checkbox);
        get_calculator_for_golden_card_card_3_container = root.findViewById(R.id.get_calculator_for_golden_card_card_3_container);
        get_calculator_for_golden_card_card_3_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_card_3_checkbox);
        get_calculator_for_golden_card_card_4_container = root.findViewById(R.id.get_calculator_for_golden_card_card_4_container);
        get_calculator_for_golden_card_card_4_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_card_4_checkbox);

        get_calculator_for_golden_card_skill_container = root.findViewById(R.id.get_calculator_for_golden_card_skill_container);
        get_calculator_for_golden_card_skill_1_container = root.findViewById(R.id.get_calculator_for_golden_card_skill_1_container);
        get_calculator_for_golden_card_skill_1_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_skill_1_checkbox);
        get_calculator_for_golden_card_skill_2_container = root.findViewById(R.id.get_calculator_for_golden_card_skill_2_container);
        get_calculator_for_golden_card_skill_2_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_skill_2_checkbox);
        get_calculator_for_golden_card_skill_3_container = root.findViewById(R.id.get_calculator_for_golden_card_skill_3_container);
        get_calculator_for_golden_card_skill_3_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_skill_3_checkbox);
        get_calculator_for_golden_card_skill_4_container = root.findViewById(R.id.get_calculator_for_golden_card_skill_4_container);
        get_calculator_for_golden_card_skill_4_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_skill_4_checkbox);

        get_calculator_for_golden_card_transfer_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_container);
        get_calculator_for_golden_card_transfer_1_a_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_a_container);
        get_calculator_for_golden_card_transfer_1_a_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_a_checkbox);
        get_calculator_for_golden_card_transfer_1_b_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_b_container);
        get_calculator_for_golden_card_transfer_1_b_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_b_checkbox);
        get_calculator_for_golden_card_transfer_1_c_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_c_container);
        get_calculator_for_golden_card_transfer_1_c_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_1_c_checkbox);
        get_calculator_for_golden_card_transfer_2_a_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_a_container);
        get_calculator_for_golden_card_transfer_2_a_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_a_checkbox);
        get_calculator_for_golden_card_transfer_2_b_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_b_container);
        get_calculator_for_golden_card_transfer_2_b_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_b_checkbox);
        get_calculator_for_golden_card_transfer_2_c_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_c_container);
        get_calculator_for_golden_card_transfer_2_c_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_2_c_checkbox);
        get_calculator_for_golden_card_transfer_3_a_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_a_container);
        get_calculator_for_golden_card_transfer_3_a_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_a_checkbox);
        get_calculator_for_golden_card_transfer_3_b_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_b_container);
        get_calculator_for_golden_card_transfer_3_b_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_b_checkbox);
        get_calculator_for_golden_card_transfer_3_c_container = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_c_container);
        get_calculator_for_golden_card_transfer_3_c_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_transfer_3_c_checkbox);

        get_calculator_for_golden_card_compose_container = root.findViewById(R.id.get_calculator_for_golden_card_compose_container);
        get_calculator_for_golden_card_compose_container_ = root.findViewById(R.id.get_calculator_for_golden_card_compose_container_);
        get_calculator_for_golden_card_compose_checkbox = root.findViewById(R.id.get_calculator_for_golden_card_compose_checkbox);
    }

    @SuppressLint("SetTextI18n")
    private void calculateGetResult() {
        int getResult = 0;
        if (get_calculator_for_golden_card_card_1_checkbox.isChecked()) {
            getResult += getDataArray[0];
        }
        if (get_calculator_for_golden_card_card_2_checkbox.isChecked()) {
            getResult += getDataArray[1];
        }
        if (get_calculator_for_golden_card_card_3_checkbox.isChecked()) {
            getResult += getDataArray[2];
        }
        if (get_calculator_for_golden_card_card_4_checkbox.isChecked()) {
            getResult += getDataArray[3];
        }
        if (get_calculator_for_golden_card_skill_1_checkbox.isChecked()) {
            getResult += getDataArray[4];
        }
        if (get_calculator_for_golden_card_skill_2_checkbox.isChecked()) {
            getResult += getDataArray[5];
        }
        if (get_calculator_for_golden_card_skill_3_checkbox.isChecked()) {
            getResult += getDataArray[6];
        }
        if (get_calculator_for_golden_card_skill_4_checkbox.isChecked()) {
            getResult += getDataArray[7];
        }
        if (get_calculator_for_golden_card_transfer_1_a_checkbox.isChecked()) {
            getResult += getDataArray[8];
        }
        if (get_calculator_for_golden_card_transfer_1_b_checkbox.isChecked()) {
            getResult += getDataArray[9];
        }
        if (get_calculator_for_golden_card_transfer_1_c_checkbox.isChecked()) {
            getResult += getDataArray[10];
        }
        if (get_calculator_for_golden_card_transfer_2_a_checkbox.isChecked()) {
            getResult += getDataArray[11];
        }
        if (get_calculator_for_golden_card_transfer_2_b_checkbox.isChecked()) {
            getResult += getDataArray[12];
        }
        if (get_calculator_for_golden_card_transfer_2_c_checkbox.isChecked()) {
            getResult += getDataArray[13];
        }
        if (get_calculator_for_golden_card_transfer_3_a_checkbox.isChecked()) {
            getResult += getDataArray[14];
        }
        if (get_calculator_for_golden_card_transfer_3_b_checkbox.isChecked()) {
            getResult += getDataArray[15];
        }
        if (get_calculator_for_golden_card_transfer_3_c_checkbox.isChecked()) {
            getResult += getDataArray[16];
        }
        if (get_calculator_for_golden_card_compose_checkbox.isChecked()) {
            getResult += getDataArray[17];
        }

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            get_calculator_for_golden_card_result.setText("未显示的物品代表不可兑换\n兑换勾选的物品需消耗 " + getResult + " 个" + getItemName);
        } else {
            get_calculator_for_golden_card_result.setText("未显示的物品代表不可兑换。兑换勾选的物品需消耗 " + getResult + " 个" + getItemName);
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
        blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewResult), root.findViewById(R.id.targetView));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean[] states = new boolean[18];
        states[0] = get_calculator_for_golden_card_card_1_checkbox.isChecked();
        states[1] = get_calculator_for_golden_card_card_2_checkbox.isChecked();
        states[2] = get_calculator_for_golden_card_card_3_checkbox.isChecked();
        states[3] = get_calculator_for_golden_card_card_4_checkbox.isChecked();
        states[4] = get_calculator_for_golden_card_skill_1_checkbox.isChecked();
        states[5] = get_calculator_for_golden_card_skill_2_checkbox.isChecked();
        states[6] = get_calculator_for_golden_card_skill_3_checkbox.isChecked();
        states[7] = get_calculator_for_golden_card_skill_4_checkbox.isChecked();
        states[8] = get_calculator_for_golden_card_transfer_1_a_checkbox.isChecked();
        states[9] = get_calculator_for_golden_card_transfer_1_b_checkbox.isChecked();
        states[10] = get_calculator_for_golden_card_transfer_1_c_checkbox.isChecked();
        states[11] = get_calculator_for_golden_card_transfer_2_a_checkbox.isChecked();
        states[12] = get_calculator_for_golden_card_transfer_2_b_checkbox.isChecked();
        states[13] = get_calculator_for_golden_card_transfer_2_c_checkbox.isChecked();
        states[14] = get_calculator_for_golden_card_transfer_3_a_checkbox.isChecked();
        states[15] = get_calculator_for_golden_card_transfer_3_b_checkbox.isChecked();
        states[16] = get_calculator_for_golden_card_transfer_3_c_checkbox.isChecked();
        states[17] = get_calculator_for_golden_card_compose_checkbox.isChecked();
        outState.putBooleanArray(KEY_CALCULATION_STATES, states);
    }

    @Override
    public void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        super.onDestroy();
    }
}