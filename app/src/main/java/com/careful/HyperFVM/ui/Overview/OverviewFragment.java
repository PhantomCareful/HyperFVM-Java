package com.careful.HyperFVM.ui.Overview;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentOverviewBinding;
import com.google.android.material.appbar.MaterialToolbar;

public class OverviewFragment extends Fragment {

    private FragmentOverviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setTopAppBarTitle(getResources().getString(R.string.label_overview));

        TextView overview_top = root.findViewById(R.id.overview_top);
        TextView overview1 = root.findViewById(R.id.overview1);
        TextView overview2 = root.findViewById(R.id.overview2);
        TextView overview3 = root.findViewById(R.id.overview3);
        TextView overview4 = root.findViewById(R.id.overview4);
        TextView overview5 = root.findViewById(R.id.overview5);

        getContentFromAssets(requireContext(), overview_top, "QATop.txt");
        getContentFromAssets(requireContext(), overview1, "QA1.txt");
        getContentFromAssets(requireContext(), overview2, "QA2.txt");
        getContentFromAssets(requireContext(), overview3, "QA3.txt");
        getContentFromAssets(requireContext(), overview4, "QA4.txt");
        getContentFromAssets(requireContext(), overview5, "QA5.txt");

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