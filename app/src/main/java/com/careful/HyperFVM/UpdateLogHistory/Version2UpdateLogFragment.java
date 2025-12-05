package com.careful.HyperFVM.UpdateLogHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentVersion2UpdateLogBinding;
import com.careful.HyperFVM.utils.OtherUtils.UpdateLogReader;

public class Version2UpdateLogFragment extends Fragment {

    private FragmentVersion2UpdateLogBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        binding = FragmentVersion2UpdateLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //显示历史版本更新日志
        getHistoryUpdateLog2(root);

        return root;
    }

    private void getHistoryUpdateLog2(View root) {
        TextView currentUpdateLog = root.findViewById(R.id.about_app_history_update_log_2);
        // 调用工具类异步读取更新日志
        UpdateLogReader.readAssetsTxtAsync(
                requireContext(),
                "HistoryUpdateLog2.txt", // assets下的文件名
                new UpdateLogReader.ReadCallback() {
                    @Override
                    public void onReadSuccess(String content) {
                        // 读取成功，展示到TextView
                        currentUpdateLog.setText(content);
                    }

                    @Override
                    public void onReadFailed(String errorMsg) {
                        // 读取失败，提示用户
                        currentUpdateLog.setText(errorMsg);
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}