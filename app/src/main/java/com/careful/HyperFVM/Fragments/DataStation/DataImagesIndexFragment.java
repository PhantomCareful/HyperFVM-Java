package com.careful.HyperFVM.Fragments.DataStation;

import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.ImageViewerActivity.ImageViewerActivity;
import com.careful.HyperFVM.Activities.ImageViewerActivity.ImageViewerDynamicActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataImagesIndexBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForUpdate.DataImagesUpdaterUtil;

public class DataImagesIndexFragment extends Fragment {

    private FragmentDataImagesIndexBinding binding;

    private DBHelper dbHelper;

    private DataImagesUpdaterUtil imageUtil;
    private LinearLayout data_images_index_container;
    private Button update_image_action;
    private boolean isResourcesReady = false;
    private long localVersionCode;

    private TransitionSet transition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataImagesIndexBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DBHelper(requireContext());
        imageUtil = DataImagesUpdaterUtil.getInstance();
        update_image_action = root.findViewById(R.id.update_image_action);
        data_images_index_container = root.findViewById(R.id.data_images_index_container);

        initViews(root);

        return root;
    }

    private void checkVersion() {
        // 获取本地版本号
        String localVersion = dbHelper.getDataStationValue("DataImagesVersionCode");
        if (localVersion == null) {
            localVersionCode = 0;
        } else {
            localVersionCode = Long.parseLong(localVersion);
        }

        // 检查本地资源是否就绪
        isResourcesReady = imageUtil.isResourcesReady(requireContext());
    }

    private void getImageServerVersionAndCheckImageUpdate() {
        update_image_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        imageUtil.checkServerVersion(new DataImagesUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (serverVersion > localVersionCode) {
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_new));
                            update_image_action.setOnClickListener(v -> {
                                Intent intent = new Intent(requireActivity(), CheckUpdateActivity.class);
                                startActivity(intent);
                            });
                            showViewWithAnimation(update_image_action);
                        } else {
                            // 已是最新版本
                            hideViewWithAnimation(update_image_action);
                            update_image_action.setText(getResources().getString(R.string.label_check_update_status_current));
                        }
                    } catch (Exception e) {
                        update_image_action.setText("检查版本时发生错误");
                    }
                });
            }

            @Override
            public void onVersionCheckFailure(String errorMsg) {
                requireActivity().runOnUiThread(() -> update_image_action.setText("检查版本失败，请稍后再试"));
            }

            @Override
            public void onVersionParseError() {
                requireActivity().runOnUiThread(() -> update_image_action.setText("版本信息错误"));
            }
        });
    }

    // ========== UI逻辑 ==========
    private void initViews(View root) {
        // 防御卡数据图
        setupContainer(root, R.id.data_images_index_card_0_1_container, "data_image_card_0_1", false);
        setupContainer(root, R.id.data_images_index_card_0_2_container, "data_image_card_0_2", false);
        setupContainer(root, R.id.data_images_index_card_0_3_container, "data_image_card_0_3", false);
        setupContainer(root, R.id.data_images_index_card_1_container, "data_image_card_1", false);
        setupContainer(root, R.id.data_images_index_card_2_container, "data_image_card_2", false);
        setupContainer(root, R.id.data_images_index_card_3_container, "data_image_card_3", false);
        setupContainer(root, R.id.data_images_index_card_4_container, "data_image_card_4", false);
        setupContainer(root, R.id.data_images_index_card_5_container, "data_image_card_5", false);
        setupContainer(root, R.id.data_images_index_card_6_container, "data_image_card_6", false);
        setupContainer(root, R.id.data_images_index_card_7_container, "data_image_card_7", false);
        setupContainer(root, R.id.data_images_index_card_8_container, "data_image_card_8", false);
        setupContainer(root, R.id.data_images_index_card_9_container, "data_image_card_9", false);
        setupContainer(root, R.id.data_images_index_card_10_container, "data_image_card_10", false);
        setupContainer(root, R.id.data_images_index_card_11_container, "data_image_card_11", false);
        setupContainer(root, R.id.data_images_index_card_12_container, "data_image_card_12", false);
        setupContainer(root, R.id.data_images_index_card_13_container, "data_image_card_13", false);
        setupContainer(root, R.id.data_images_index_card_14_container, "data_image_card_14", false);
        setupContainer(root, R.id.data_images_index_card_15_container, "data_image_card_15", false);
        setupContainer(root, R.id.data_images_index_card_16_container, "data_image_card_16", false);
        setupContainer(root, R.id.data_images_index_card_17_container, "data_image_card_17", false);
        setupContainer(root, R.id.data_images_index_card_18_container, "data_image_card_18", false);
        setupContainer(root, R.id.data_images_index_card_19_container, "data_image_card_19", false);

        // 武器宝石数据图
        setupContainer(root, R.id.data_images_index_weapon_and_gem_0_1_container, "data_image_weapon_and_gem_0_1", false);
        setupContainer(root, R.id.data_images_index_weapon_and_gem_1_container, "data_image_weapon_and_gem_1", false);
        setupContainer(root, R.id.data_images_index_weapon_and_gem_2_container, "data_image_weapon_and_gem_2", false);
        setupContainer(root, R.id.data_images_index_weapon_and_gem_3_container, "data_image_weapon_and_gem_3", false);
        setupContainer(root, R.id.data_images_index_weapon_and_gem_4_container, "data_image_weapon_and_gem_4", false);
        setupContainer(root, R.id.data_images_index_weapon_and_gem_5_container, "data_image_weapon_and_gem_5", false);

        // 道具分解&兑换数据图
        setupContainer(root, R.id.data_images_index_decompose_and_get_1_container, "data_image_decompose_and_get_1", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_2_container, "data_image_decompose_and_get_2", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_3_container, "data_image_decompose_and_get_3", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_4_container, "data_image_decompose_and_get_4", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_5_container, "data_image_decompose_and_get_5", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_6_container, "data_image_decompose_and_get_6", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_7_container, "data_image_decompose_and_get_7", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_8_container, "data_image_decompose_and_get_8", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_9_container, "data_image_decompose_and_get_9", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_10_container, "data_image_decompose_and_get_10", false);
        setupContainer(root, R.id.data_images_index_decompose_and_get_11_container, "data_image_decompose_and_get_11", false);

        // 老输血量数据图
        setupContainer(root, R.id.data_images_index_mouse_hp_1_container, "data_image_mouse_hp_1", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_2_container, "data_image_mouse_hp_2", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_3_container, "data_image_mouse_hp_3", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_4_container, "data_image_mouse_hp_4", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_5_container, "data_image_mouse_hp_5", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_6_container, "data_image_mouse_hp_6", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_7_container, "data_image_mouse_hp_7", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_8_container, "data_image_mouse_hp_8", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_9_container, "data_image_mouse_hp_9", false);
        setupContainer(root, R.id.data_images_index_mouse_hp_10_container, "data_image_mouse_hp_10", false);

        // 其他数据图
        setupContainer(root, R.id.data_images_index_others_1_container, "data_image_others_1", true);
        setupContainer(root, R.id.data_images_index_others_2_container, "data_image_others_2", true);
        setupContainer(root, R.id.data_images_index_others_3_container, "data_image_others_3", true);
        setupContainer(root, R.id.data_images_index_others_4_container, "data_image_others_4", false);
        setupContainer(root, R.id.data_images_index_others_5_container, "data_image_others_5", true);
        setupContainer(root, R.id.data_images_index_others_6_container, "data_image_others_6", false);
        setupContainer(root, R.id.data_images_index_others_7_container, "data_image_others_7", true);

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(400); // 动画时长400ms
    }

    private void setupContainer(View root, int viewId, String imageName, boolean isDynamic) {
        LinearLayout container = root.findViewById(viewId);
        container.setTag(imageName);
        container.setOnClickListener(v -> {
            if (!isResourcesReady) {
                Toast.makeText(requireContext(), "还没有图片资源哦，请先更新", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent;
            if (isDynamic) {
                intent = new Intent(requireActivity(), ImageViewerDynamicActivity.class);
            } else {
                intent = new Intent(requireActivity(), ImageViewerActivity.class);
            }
            intent.putExtra("imgPath", imageName);
            startActivity(intent);
        });
    }

    /**
     * 给检查更新的按钮和文字添加动画
     */
    private void showViewWithAnimation(View view) {
        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0.95f);
        view.setScaleY(0.95f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start();
    }

    private void hideViewWithAnimation(View view) {
        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
        if (view.getVisibility() == View.GONE) return;
        view.animate()
                .alpha(0f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(400)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 获取本地版本号
        checkVersion();
        // 检查图片资源是否有更新
        getImageServerVersionAndCheckImageUpdate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
