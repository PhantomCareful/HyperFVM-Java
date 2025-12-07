package com.careful.HyperFVM.ui.Overview;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentOverviewBinding;
import com.careful.HyperFVM.utils.OtherUtils.UpdateLogReader;
import com.google.android.material.appbar.MaterialToolbar;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;

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

        getQAContent(overview_top, "QATop.txt");
        getQAContent(overview1, "QA1.txt");
        getQAContent(overview2, "QA2.txt");
        getQAContent(overview3, "QA3.txt");
        getQAContent(overview4, "QA4.txt");
        getQAContent(overview5, "QA5.txt");

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

    private void getQAContent(TextView textView, String filename) {
        // 调用工具类异步读取QA内容
        UpdateLogReader.readAssetsTxtAsync(
                requireContext(),
                filename, // assets下的文件名
                new UpdateLogReader.ReadCallback() {
                    @Override
                    public void onReadSuccess(String content) {
                        // 读取成功，展示到TextView
                        Markwon markwon = Markwon.builder(requireContext())
                                .usePlugin(StrikethroughPlugin.create())// 启用删除线支持
                                .build();
                        markwon.setMarkdown(textView, content);
                    }

                    @Override
                    public void onReadFailed(String errorMsg) {
                        // 读取失败，提示用户
                        textView.setText(errorMsg);
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}