package com.careful.HyperFVM.Activities.Oobe;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.Necessary.UsingInstruction.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

/**
 * OOBE 软件介绍页（浅色 Provision 风格骨架）。
 * 本轮只建框架：页头（返回+标题）/ 占位内容区 / 底部下一步；
 * 内部组件与文案等内容由用户设计后二期填充。
 */
public class OobeIntroductionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oobe_introduction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化各种装饰效果
        initDecoration(view);

        // 跳转使用说明
        view.findViewById(R.id.oobe_introduction_using_instruction_container).setOnClickListener(v -> startActivity(new Intent(requireActivity(), UsingInstructionActivity.class)));

        // 下一步 → 权限申请页（栈式启动，本页留栈供返回）
        view.findViewById(R.id.oobe_btn_next).setOnClickListener(v ->
                ((OobePageActivity) requireActivity()).goNextStep());
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration(View view) {
        // 适配状态栏高度
        ImageButton floatButtonBack = view.findViewById(R.id.oobe_back);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(requireContext(), 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态获取导航栏高度（小白条/三键导航）
        CardView oobe_btn_next_container = view.findViewById(R.id.oobe_btn_next_container);
        InsetsUtil.setNavigationBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oobe_btn_next_container.getLayoutParams();
            params.bottomMargin = height;
            params.leftMargin = height;
            params.rightMargin = height;
            oobe_btn_next_container.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout oobe_introduction_container = view.findViewById(R.id.oobe_introduction_container);
        InsetsUtil.setMarginHorizontal(requireContext(), oobe_introduction_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oobe_introduction_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            oobe_introduction_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置返回按钮的功能
        floatButtonBack.setOnClickListener(v -> requireActivity().finish());

        // 添加按压动画
        view.findViewById(R.id.tips_oobe_introduction_1_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        view.findViewById(R.id.tips_oobe_introduction_2_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InsetsUtil.removeListener(getView());
    }
}
