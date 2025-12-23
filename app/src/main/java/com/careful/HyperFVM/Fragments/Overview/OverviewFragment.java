package com.careful.HyperFVM.Fragments.Overview;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.app.Activity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentOverviewBinding;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

public class OverviewFragment extends Fragment {

    private FragmentOverviewBinding binding;

    private TransitionSet transition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setTopAppBarTitle(getResources().getString(R.string.label_overview));

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

        TextView overview_top = root.findViewById(R.id.overview_top);
        TextView overview1 = root.findViewById(R.id.overview1);
        TextView overview2 = root.findViewById(R.id.overview2);
        TextView overview3 = root.findViewById(R.id.overview3);
        TextView overview4 = root.findViewById(R.id.overview4);

        getContentFromAssets(requireContext(), overview_top, "QATop.txt");
        getContentFromAssets(requireContext(), overview1, "QA1.txt");
        getContentFromAssets(requireContext(), overview2, "QA2.txt");
        getContentFromAssets(requireContext(), overview3, "QA3.txt");
        getContentFromAssets(requireContext(), overview4, "QA4.txt");

        // 初始化延迟任务，添加binding非空检查
        // 执行前检查binding是否已销毁
        Runnable transitionRunnable = () -> {
            // 执行前检查binding是否已销毁
            if (binding == null) {
                return;
            }
            TransitionManager.beginDelayedTransition(binding.overviewContainer, transition);
            Objects.requireNonNull(binding.overviewPlaceholder).setVisibility(View.GONE);
            binding.overviewTopContainer.setVisibility(View.VISIBLE);
            binding.overview1Container.setVisibility(View.VISIBLE);
            binding.overview2Container.setVisibility(View.VISIBLE);
            binding.overview3Container.setVisibility(View.VISIBLE);
            binding.overview4Container.setVisibility(View.VISIBLE);
            binding.overviewCopyright.setVisibility(View.VISIBLE);
        };

        // 执行延迟任务
        root.postDelayed(transitionRunnable, 300);

        return root;
    }

    private void setTopAppBarTitle(String title) {
        //设置标题
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}