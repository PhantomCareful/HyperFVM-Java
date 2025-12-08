package com.careful.HyperFVM.ui.DataStation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.ImageViewerActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataImagesIndexBinding;

public class DataImagesIndexFragment extends Fragment {

    private FragmentDataImagesIndexBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataImagesIndexBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /*防御卡数据图*/
        TextView tv = root.findViewById(R.id.text_data_images_index_card_0_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_0_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_0_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_0_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_0_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_0_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_4);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_4);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_5);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_5);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_6);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_6);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_7);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_7);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_8);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_8);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_9);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_9);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_10);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_10);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_11);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_11);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_12);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_12);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_13);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_13);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_14);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_14);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_15);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_15);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_16);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_16);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_17);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_17);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_18);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_18);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_card_19);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_card_19);
            startActivity(intent);
        });

        /*武器宝石数据图*/
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_0_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_0_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_4);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_4);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_weapon_and_gem_5);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_weapon_and_gem_5);
            startActivity(intent);
        });

        /*道具分解&兑换数据图*/
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_4);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_4);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_5);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_5);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_6);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_6);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_7);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_7);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_8);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_8);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_9);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_9);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_10);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_10);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_decompose_and_get_11);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_decompose_and_get_11);
            startActivity(intent);
        });

        /*老输血量数据图*/
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_4);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_4);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_5);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_5);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_6);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_6);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_7);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_7);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_8);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_8);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_mouse_hp_10);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_mouse_hp_10);
            startActivity(intent);
        });

        /*其他数据图*/
        tv = root.findViewById(R.id.text_data_images_index_others_1);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_1);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_2);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_2);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_3);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_3);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_4);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_4);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_5);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_5);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_6);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_6);
            startActivity(intent);
        });
        tv = root.findViewById(R.id.text_data_images_index_others_7);
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgName", R.drawable.data_image_others_7);
            startActivity(intent);
        });

        return root;
    }
}
