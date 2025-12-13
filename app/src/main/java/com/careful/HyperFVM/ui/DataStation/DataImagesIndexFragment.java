package com.careful.HyperFVM.ui.DataStation;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.ImageViewerActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataImagesIndexBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForUpdate.DownloadDataImagesUtil;

import java.io.File;

public class DataImagesIndexFragment extends Fragment {

    private FragmentDataImagesIndexBinding binding;

    private DBHelper dbHelper;

    private DownloadDataImagesUtil downloadUtil;
    private LinearLayout data_images_index_container;
    private Button data_images_check_update;
    private TextView data_images_check_update_toast;
    private boolean isDownloading = false;
    private boolean isResourcesReady = false;
    private int localVersionCode;
    private int serverVersionCode;

    private TransitionSet transition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataImagesIndexBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new DBHelper(requireContext());
        downloadUtil = DownloadDataImagesUtil.getInstance();
        data_images_check_update = root.findViewById(R.id.data_images_check_update);
        data_images_check_update_toast = root.findViewById(R.id.data_images_check_update_toast);
        data_images_index_container = root.findViewById(R.id.data_images_index_container);

        initViews(root);
        checkVersionAndInit();

        return root;
    }

    private void checkVersionAndInit() {
        // 1. 获取本地版本号
        String localVersion = dbHelper.getDataStationValue("DataImagesVersionCode");
        if (localVersion == null) {
            localVersionCode = 0;
        } else {
            localVersionCode = Integer.parseInt(localVersion);
        }

        // 2. 检查本地资源是否就绪
        isResourcesReady = downloadUtil.isResourcesReady(requireContext());

        // 检查图片资源是否有更新
        checkServerVersion();
    }

    private void checkServerVersion() {
        downloadUtil.checkServerVersion(new DownloadDataImagesUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(String serverVersion) {
                serverVersionCode = Integer.parseInt(serverVersion);
                requireActivity().runOnUiThread(() -> {
                    try {
                        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
                        if (serverVersionCode > localVersionCode) {
                            showViewWithAnimation(data_images_check_update);
                            if (localVersionCode == 0) {
                                data_images_check_update.setText("点击下载数据图资源");
                                setTextViewsClickable(false);
                            } else {
                                data_images_check_update.setText("发现新版本，点击更新");
                                setTextViewsClickable(true);
                            }
                            setupDownloadButtonListener(true);
                        } else {
                            data_images_check_update.setText("当前已是最新版本");
                            setTextViewsClickable(true);
                            setupDownloadButtonListener(false);
                        }
                    } catch (NumberFormatException e) {
                        onVersionParseError(); // 复用解析错误逻辑
                    }
                });
            }

            @Override
            public void onVersionCheckFailure(String errorMsg) {
                requireActivity().runOnUiThread(() -> {
                    showViewWithAnimation(data_images_check_update);
                    data_images_check_update.setText("检查版本失败，请稍后再试");
                    setupDownloadButtonListener(false);
                    if (localVersionCode != 0) {
                        setTextViewsClickable(true); // 失败仍允许查看本地资源
                    }
                });
            }

            @Override
            public void onVersionParseError() {
                requireActivity().runOnUiThread(() -> {
                    showViewWithAnimation(data_images_check_update);
                    data_images_check_update.setText("版本信息错误");
                    setupDownloadButtonListener(false);
                });
            }
        });
    }

    private void setupDownloadButtonListener(boolean clickable) {
        data_images_check_update.setOnClickListener(v -> {
            if (clickable && !isDownloading) startDownload();
        });
        data_images_check_update.setClickable(clickable);
    }

    private void startDownload() {
        isDownloading = true;
        data_images_check_update.setClickable(false);
        setTextViewsClickable(false);

        TransitionManager.beginDelayedTransition(data_images_index_container, transition);
        showViewWithAnimation(data_images_check_update_toast);

        // 判断是否为全量下载（localVersionCode=0时全量下载）
        boolean isFullDownload = (localVersionCode == 0);

        // 动态拼接下载链接
        String downloadUrl = downloadUtil.getDownloadUrl(String.valueOf(serverVersionCode), isFullDownload);

        // 调用工具类下载解压（传入全量/增量标记）
        downloadUtil.downloadAndUnzip(requireContext(), downloadUrl, isFullDownload, new DownloadDataImagesUtil.DownloadCallback() {
            @Override
            public void onDownloadProgress(int progress) {
                requireActivity().runOnUiThread(() ->
                        data_images_check_update.setText("⏳下载中: " + progress + "%⏳")
                );
            }

            @Override
            public void onUnzipProgress(int progress) {
                requireActivity().runOnUiThread(() ->
                        data_images_check_update.setText("⏳解压中: " + progress + "%⏳")
                );
            }

            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    // 更新本地版本号
                    String newVersion = String.valueOf(serverVersionCode);
                    dbHelper.updateDataStationValue("DataImagesVersionCode", newVersion);

                    isDownloading = false;
                    isResourcesReady = true;
                    setTextViewsClickable(true);

                    TransitionManager.beginDelayedTransition(data_images_index_container, transition);
                    hideViewWithAnimation(data_images_check_update);
                    hideViewWithAnimation(data_images_check_update_toast);
                    Toast.makeText(requireContext(), "资源更新完成🎉🎉🎉", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                requireActivity().runOnUiThread(() -> {
                    isDownloading = false;
                    data_images_check_update.setText("下载失败，点击重试");
                    data_images_check_update.setClickable(true);

                    // 非首次下载失败，恢复TextView点击
                    if (localVersionCode != 0 && isResourcesReady) {
                        setTextViewsClickable(true);
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ========== UI逻辑 ==========
    private void initViews(View root) {
        // 防御卡数据图
        setupTextView(root, R.id.text_data_images_index_card_0_1, "data_image_card_0_1");
        setupTextView(root, R.id.text_data_images_index_card_0_2, "data_image_card_0_2");
        setupTextView(root, R.id.text_data_images_index_card_0_3, "data_image_card_0_3");
        setupTextView(root, R.id.text_data_images_index_card_1, "data_image_card_1");
        setupTextView(root, R.id.text_data_images_index_card_2, "data_image_card_2");
        setupTextView(root, R.id.text_data_images_index_card_3, "data_image_card_3");
        setupTextView(root, R.id.text_data_images_index_card_4, "data_image_card_4");
        setupTextView(root, R.id.text_data_images_index_card_5, "data_image_card_5");
        setupTextView(root, R.id.text_data_images_index_card_6, "data_image_card_6");
        setupTextView(root, R.id.text_data_images_index_card_7, "data_image_card_7");
        setupTextView(root, R.id.text_data_images_index_card_8, "data_image_card_8");
        setupTextView(root, R.id.text_data_images_index_card_9, "data_image_card_9");
        setupTextView(root, R.id.text_data_images_index_card_10, "data_image_card_10");
        setupTextView(root, R.id.text_data_images_index_card_11, "data_image_card_11");
        setupTextView(root, R.id.text_data_images_index_card_12, "data_image_card_12");
        setupTextView(root, R.id.text_data_images_index_card_13, "data_image_card_13");
        setupTextView(root, R.id.text_data_images_index_card_14, "data_image_card_14");
        setupTextView(root, R.id.text_data_images_index_card_15, "data_image_card_15");
        setupTextView(root, R.id.text_data_images_index_card_16, "data_image_card_16");
        setupTextView(root, R.id.text_data_images_index_card_17, "data_image_card_17");
        setupTextView(root, R.id.text_data_images_index_card_18, "data_image_card_18");
        setupTextView(root, R.id.text_data_images_index_card_19, "data_image_card_19");

        // 武器宝石数据图
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_0_1, "data_image_weapon_and_gem_0_1");
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_1, "data_image_weapon_and_gem_1");
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_2, "data_image_weapon_and_gem_2");
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_3, "data_image_weapon_and_gem_3");
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_4, "data_image_weapon_and_gem_4");
        setupTextView(root, R.id.text_data_images_index_weapon_and_gem_5, "data_image_weapon_and_gem_5");

        // 道具分解&兑换数据图
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_1, "data_image_decompose_and_get_1");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_2, "data_image_decompose_and_get_2");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_3, "data_image_decompose_and_get_3");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_4, "data_image_decompose_and_get_4");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_5, "data_image_decompose_and_get_5");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_6, "data_image_decompose_and_get_6");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_7, "data_image_decompose_and_get_7");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_8, "data_image_decompose_and_get_8");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_9, "data_image_decompose_and_get_9");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_10, "data_image_decompose_and_get_10");
        setupTextView(root, R.id.text_data_images_index_decompose_and_get_11, "data_image_decompose_and_get_11");

        // 老输血量数据图
        setupTextView(root, R.id.text_data_images_index_mouse_hp_1, "data_image_mouse_hp_1");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_2, "data_image_mouse_hp_2");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_3, "data_image_mouse_hp_3");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_4, "data_image_mouse_hp_4");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_5, "data_image_mouse_hp_5");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_6, "data_image_mouse_hp_6");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_7, "data_image_mouse_hp_7");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_8, "data_image_mouse_hp_8");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_9, "data_image_mouse_hp_9");
        setupTextView(root, R.id.text_data_images_index_mouse_hp_10, "data_image_mouse_hp_10");

        // 其他数据图
        setupTextView(root, R.id.text_data_images_index_others_1, "data_image_others_1");
        setupTextView(root, R.id.text_data_images_index_others_2, "data_image_others_2");
        setupTextView(root, R.id.text_data_images_index_others_3, "data_image_others_3");
        setupTextView(root, R.id.text_data_images_index_others_4, "data_image_others_4");
        setupTextView(root, R.id.text_data_images_index_others_5, "data_image_others_5");
        setupTextView(root, R.id.text_data_images_index_others_6, "data_image_others_6");
        setupTextView(root, R.id.text_data_images_index_others_7, "data_image_others_7");

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(400); // 动画时长400ms
    }

    private void setupTextView(View root, int viewId, String imageName) {
        TextView tv = root.findViewById(viewId);
        tv.setTag(imageName);
        tv.setOnClickListener(v -> {
            if (!isResourcesReady) {
                Toast.makeText(requireContext(), "图片资源未准备好", Toast.LENGTH_SHORT).show();
                return;
            }

            String imagePath = getImagePath((String) v.getTag());
            Intent intent = new Intent(requireActivity(), ImageViewerActivity.class);
            intent.putExtra("imgPath", imagePath);
            startActivity(intent);
        });
    }

    private void setTextViewsClickable(boolean clickable) {
        ViewGroup root = (ViewGroup) getView();
        if (root != null) setAllChildrenClickable(root, clickable);
    }

    private void setAllChildrenClickable(ViewGroup parent, boolean clickable) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                if (child.getId() != data_images_check_update_toast.getId()) {
                    child.setClickable(clickable);
                    child.setAlpha(clickable ? 1.0f : 0.5f);
                }
            } else if (child instanceof ViewGroup) {
                setAllChildrenClickable((ViewGroup) child, clickable);
            }
        }
    }

    /**
     * 拼接本地图片路径（Fragment内管理路径规则）
     * @param imageName 图片名称（不含扩展名）
     * @return 完整的本地图片路径
     */
    private String getImagePath(String imageName) {
        // 从工具类获取解压根路径，拼接图片名称+扩展名（此处假设为png，可根据实际调整）
        String unzipRootPath = downloadUtil.getUnzipPath(requireContext());
        return unzipRootPath + File.separator + imageName + ".webp";
    }

    /**
     * 给检查更新的按钮和文字添加动画
     */
    private void showViewWithAnimation(View view) {
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
    public void onPause() {
        super.onPause();
        // 如果正在下载时切换页面，终止下载并提示
        if (isDownloading) {
            downloadUtil.cancelDownload(); // 需要在DownloadDataImagesUtil中实现取消方法
            isDownloading = false;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "已取消资源更新", Toast.LENGTH_SHORT).show();
                data_images_check_update.setText("下载已取消，点击重试");
                data_images_check_update.setClickable(true);
                if (localVersionCode != 0 && isResourcesReady) {
                    setTextViewsClickable(true);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
