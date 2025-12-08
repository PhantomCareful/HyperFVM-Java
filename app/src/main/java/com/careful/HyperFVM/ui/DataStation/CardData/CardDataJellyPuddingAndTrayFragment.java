package com.careful.HyperFVM.ui.DataStation.CardData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationThirdCardBinding;
import com.careful.HyperFVM.utils.OtherUtils.ImageViewerOutUtil;

public class CardDataJellyPuddingAndTrayFragment extends Fragment {

    private FragmentDataStationThirdCardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationThirdCardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView imageView = root.findViewById(R.id.Image_View);
        imageView.setImageResource(R.drawable.data_image_card_6);

        // 调用工具类绑定点击事件，参数：当前Fragment、ImageView、临时文件名、authority
        // 传入FileProvider的authority（与AndroidManifest.xml中一致）
        String authority = requireContext().getPackageName() + ".fileprovider";
        ImageViewerOutUtil.setupImageViewer(this, imageView, "temp.png", authority);

        // 设置返回按钮点击事件
        root.findViewById(R.id.Button_Back).setOnClickListener(v -> {
            if (callback != null) {
                callback.backToIndex();
            }
        });

        return root;
    }

    // 定义回调接口
    public interface OnBackToIndexCallback {
        void backToIndex(); // 跳转回Index的方法
    }
    // 持有接口实例（由父Fragment实现）
    private CardDataAirForceIFragment.OnBackToIndexCallback callback;
    // 绑定接口实现（通常在父Fragment中调用）
    public void setOnBackToIndexCallback(CardDataAirForceIFragment.OnBackToIndexCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}