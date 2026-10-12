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
import com.careful.HyperFVM.databinding.FragmentVersion3UpdateLogBinding;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;

import java.util.Objects;

public class Version3UpdateLogFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        FragmentVersion3UpdateLogBinding binding = FragmentVersion3UpdateLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //判断当前版本是Release版还是Beta版（与关于软件页写法一致）
        String localVersionName = LocalVersionUtil.getAppLocalVersionName(requireContext());
        boolean isRelease = Objects.equals(localVersionName.split("\\.")[2], "0");

        //显示历史版本更新日志（Release版隐藏第三位非0条目，Beta版隐藏第三位为0条目）
        TextView currentUpdateLog = root.findViewById(R.id.about_app_history_update_log_3);
        getContentFromAssets(requireContext(), currentUpdateLog, "HistoryUpdateLog3.txt", isRelease);

        return root;
    }
}