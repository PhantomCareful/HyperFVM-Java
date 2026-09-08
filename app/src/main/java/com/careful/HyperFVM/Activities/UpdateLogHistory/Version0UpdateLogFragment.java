package com.careful.HyperFVM.Activities.UpdateLogHistory;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentVersion0UpdateLogBinding;

public class Version0UpdateLogFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        FragmentVersion0UpdateLogBinding binding = FragmentVersion0UpdateLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //显示历史版本更新日志
        TextView currentUpdateLog = root.findViewById(R.id.about_app_history_update_log_0);
        getContentFromAssets(requireContext(), currentUpdateLog, "HistoryUpdateLog0.txt");

        return root;
    }

}