package com.careful.HyperFVM.ui.DataStation.DecomposeAndExchangeData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationThirdBinding;
import com.careful.HyperFVM.utils.OtherUtils.ImageViewerOutUtil;

public class PigCardFragment extends Fragment {

    private FragmentDataStationThirdBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationThirdBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView imageView = root.findViewById(R.id.Image_View);
        imageView.setImageResource(R.drawable.data_image_decompose_and_get_5);

        // 调用工具类绑定点击事件，参数：当前Fragment、ImageView、临时文件名、authority
        // 传入FileProvider的authority（与AndroidManifest.xml中一致）
        String authority = requireContext().getPackageName() + ".fileprovider";
        ImageViewerOutUtil.setupImageViewer(this, imageView, "temp.png", authority);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}