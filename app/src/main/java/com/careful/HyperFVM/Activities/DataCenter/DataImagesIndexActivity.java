package com.careful.HyperFVM.Activities.DataCenter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageCardActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageDecomposeAndGetActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageMouseHpActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageOthersActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageTiramisuActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageWeaponAndGemActivity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;

import android.widget.ScrollView;

import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DataImagesIndexActivity extends BaseActivity {
    private final String TAG = "DataImagesIndexActivity";

    private final String DATA_IMAGES_URL = "https://raw.giteeusercontent.com/phantom-careful/hyper-fvm-updater/raw/main/DataImages/DataImagesUrl.m3u";
    // 存储从DATA_IMAGES_URL获取到的图片信息
    private final List<DataImagesInfo> dataImagesInfoList = new ArrayList<>();
    // 存储需要更新的图片信息
    private final List<DataImagesInfo> needUpdateDataImagesInfoList = new ArrayList<>();
    // 存储下载失败的图片信息
    private final List<DataImagesInfo> downloadFailedDataImagesInfoList = new ArrayList<>();

    private long localVersionCode;

    private ScrollView scrollView;
    private TransitionSet transition;

    private LinearLayout data_images_index_update_info_container;
    private TextView data_images_index_update_info_title;
    private TextView data_images_index_update_info_description;

    private LinearLayout data_images_index_delete_container;
    private TextView data_images_index_delete_title;
    TextView data_images_index_delete_description;

    private LinearLayout data_images_index_card_container;
    private LinearLayout data_images_index_weapon_and_gem_container;
    private LinearLayout data_images_index_decompose_and_get_container;
    private LinearLayout data_images_index_mouse_hp_container;
    private LinearLayout data_images_index_others_container;
    private LinearLayout data_images_index_tiramisu_container;

    private ExecutorService downloadExecutor;
    private volatile boolean isActivityDestroyed = false;
    private Handler mainHandler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_data_images_index);

        scrollView = findViewById(R.id.scrollView);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长

        data_images_index_update_info_container = findViewById(R.id.data_images_index_update_info_container);
        data_images_index_update_info_title = findViewById(R.id.data_images_index_update_info_title);
        data_images_index_update_info_description = findViewById(R.id.data_images_index_update_info_description);
        data_images_index_update_info_title.setText("正在检查图片版本⏳");
        data_images_index_update_info_description.setText("请稍候，很快就好哦");

        downloadExecutor = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());

        data_images_index_delete_container = findViewById(R.id.data_images_index_delete_container);
        data_images_index_delete_title = findViewById(R.id.data_images_index_delete_title);
        data_images_index_delete_description = findViewById(R.id.data_images_index_delete_description);

        if (hasWebpImages()) {
            data_images_index_delete_title.setText("检测到旧版App下载的图片");
            data_images_index_delete_description.setText("这些图片的清晰度较低，当前版本已不支持查看。您可以长按本卡片将它们删除，然后使用上面的卡片重新下载更高清的图片。");
        } else {
            data_images_index_delete_title.setText("删除所有图片");
            data_images_index_delete_description.setText("长按本卡片可执行删除操作\n如果您遇到图片无法查看的问题，可尝试先删除所有图片再重新下载");
        }
        data_images_index_delete_container.setOnLongClickListener(v -> {
            DialogBuilderManager.showDialogWithCallBack(
                    this, "二次确认", "🗑️", "将删除所有本地图片。删除后，您需要重新下载才能查看。", true,
                    "咱手滑了", "开始删除", () -> {
                        data_images_index_delete_title.setText("操作执行中⏳");
                        setAllCardViewEnabled(false);

                        File dir = new File(getFilesDir(), "data_images");
                        if (!dir.exists()) return; // 目录不存在，无需清理

                        File[] files = dir.listFiles();
                        if (files == null) return; // 目录为空，也无需清理

                        for (File file : files) {
                            if (file.isFile()) {
                                file.delete();
                            }
                        }

                        data_images_index_delete_title.setText("删除完成🎉🎉🎉");
                        data_images_index_delete_description.setText("长按本卡片可执行删除操作\n如果您遇到图片无法查看的问题，可尝试先删除所有图片再重新下载");

                        LocalVersionUtil.setImageResourcesVersionCode(DataImagesIndexActivity.this, 1);
                        mainHandler.post(() -> {
                            setAllCardViewEnabled(false);
                            data_images_index_delete_container.setEnabled(true);
                            TransitionManager.beginDelayedTransition(scrollView, transition);
                            data_images_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
                            data_images_index_update_info_description.setText("删除图片后需要重新下载才能查看哦");
                            data_images_index_update_info_container.setOnClickListener(view -> downloadImages(dataImagesInfoList));
                        });
                    });

            return true;
        });

        data_images_index_card_container = findViewById(R.id.data_images_index_card_container);
        data_images_index_weapon_and_gem_container = findViewById(R.id.data_images_index_weapon_and_gem_container);
        data_images_index_decompose_and_get_container = findViewById(R.id.data_images_index_decompose_and_get_container);
        data_images_index_mouse_hp_container = findViewById(R.id.data_images_index_mouse_hp_container);
        data_images_index_others_container = findViewById(R.id.data_images_index_others_container);
        data_images_index_tiramisu_container = findViewById(R.id.data_images_index_tiramisu_container);

        // 初始化各种装饰效果
        initDecoration();

        // 从云端仓库获取数据图信息（版本号、访问链接等）
        getDataImagesInfoFromGit(new DataImagesCatchResultCallBack() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                // 获取成功了，再初始化各入口卡片的点击事件
                mainHandler.post(() -> initViews());

                // 获取成功以后，需要将所有图片的version与数据库存储的versionCode进行对比
                // 如果有图片更新，则单独下载这些图片
                // 如果是第一次使用，则数据库存储的versionCode为0，自然所有图片都需要更新

                // 获取本地版本号
                localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(DataImagesIndexActivity.this);

                // 如果localVersionCode == 0，则需要将入口卡片的enable值设为false，因为此时还没有存储任何图片，打开会报错。
                if (localVersionCode == 0) {
                    mainHandler.post(() -> {
                        setAllCardViewEnabled(false);
                        TransitionManager.beginDelayedTransition(scrollView, transition);
                        data_images_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
                        data_images_index_update_info_description.setText("第一次使用需要先下载图片才能查看哦");
                        data_images_index_update_info_container.setOnClickListener(v -> downloadImages(dataImagesInfoList));
                    });
                } else if (localVersionCode == 1) {
                    mainHandler.post(() -> {
                        setAllCardViewEnabled(false);
                        TransitionManager.beginDelayedTransition(scrollView, transition);
                        data_images_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
                        data_images_index_update_info_description.setText("删除图片后需要重新下载才能查看哦");
                        data_images_index_update_info_container.setOnClickListener(v -> downloadImages(dataImagesInfoList));
                    });
                } else {
                    boolean needUpdate = false;
                    for (int i = 0; i < dataImagesInfoList.size(); i++) {
                        // 如有云端图片版本号更新，将其信息加入需要更新的List中
                        Log.d(TAG, dataImagesInfoList.get(i).getFileName() + ": 云端" + dataImagesInfoList.get(i).getVersion() + ", 本地" + localVersionCode);
                        if (localVersionCode < dataImagesInfoList.get(i).getVersion()) {
                            needUpdateDataImagesInfoList.add(dataImagesInfoList.get(i));
                            needUpdate = true;
                        }
                    }

                    boolean finalNeedUpdate = needUpdate;
                    mainHandler.post(() -> {
                        TransitionManager.beginDelayedTransition(scrollView, transition);
                        if (finalNeedUpdate) {
                            data_images_index_update_info_title.setText(needUpdateDataImagesInfoList.size() + "张图片需要更新📣📣📣");
                            data_images_index_update_info_description.setText("单击本卡片仅下载有更新的图片\n长按本卡片下载全部图片(耗时更长，仅在需要时使用)");
                            data_images_index_update_info_container.setOnClickListener(v -> downloadImages(needUpdateDataImagesInfoList));
                        } else {
                            data_images_index_update_info_title.setText("已经是最新版本😋😋😋");
                            data_images_index_update_info_description.setText("如有需要，可长按本卡片重新下载图片");
                            data_images_index_update_info_container.setOnClickListener(null);
                        }
                        data_images_index_update_info_container.setOnLongClickListener(v -> {
                            downloadImages(dataImagesInfoList);
                            return true;
                        });
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                // 失败的话，用弹窗显示异常日志
                DialogBuilderManager.showDialog(
                        DataImagesIndexActivity.this,
                        "抛出异常",
                        "❌",
                        "请将本页面截图反馈给开发者：\n" + e,
                        true,
                        "好的"
                );
            }
        });
    }

    private void getDataImagesInfoFromGit(DataImagesCatchResultCallBack callBack) {
        new Thread(() -> {
            try {
                // 第1步：从给定的链接获取JSON字符串
                String JSONArrayStr = XMLHelper.getContentFromUrl(DATA_IMAGES_URL);

                // 第2步：将JSON字符串转换成JSON数组
                JSONArray jsonArray = new JSONArray(JSONArrayStr);

                JSONObject itemObj;

                // 第3步：遍历数组中的每个JSON对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    // 提取单个JSON对象
                    itemObj = jsonArray.getJSONObject(i);

                    // 讲JSON中的内容存储到DataImagesInfo类中
                    DataImagesInfo dataImagesInfo = new DataImagesInfo();
                    dataImagesInfo.setFileName(itemObj.getString("fileName"));
                    dataImagesInfo.setVersion(Long.parseLong(itemObj.getString("version")));
                    dataImagesInfo.setUrl(itemObj.getString("url"));

                    // 加入List
                    dataImagesInfoList.add(dataImagesInfo);
                }

                // 第4步：触发回调
                callBack.onSuccess();

            } catch (IOException | JSONException e) {
                // 出发回调
                callBack.onFailed(e);
            }
        }).start();
    }

    private void initViews() {
        data_images_index_card_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageCardActivity.class)));
        data_images_index_weapon_and_gem_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageWeaponAndGemActivity.class)));
        data_images_index_decompose_and_get_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageDecomposeAndGetActivity.class)));
        data_images_index_mouse_hp_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageMouseHpActivity.class)));
        data_images_index_others_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageOthersActivity.class)));
        data_images_index_tiramisu_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageTiramisuActivity.class)));
    }

    private void setAllCardViewEnabled(boolean enabled) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> setAllCardViewEnabled(enabled));
            return;
        }
        data_images_index_delete_container.setEnabled(enabled);
        data_images_index_tiramisu_container.setEnabled(enabled);
        data_images_index_card_container.setEnabled(enabled);
        data_images_index_weapon_and_gem_container.setEnabled(enabled);
        data_images_index_decompose_and_get_container.setEnabled(enabled);
        data_images_index_mouse_hp_container.setEnabled(enabled);
        data_images_index_others_container.setEnabled(enabled);
    }

    /**
     * 检查私有目录内是否有webp格式的图片
     * 这些图片是旧版本下载的图片，清晰度较低，应该删除
     *
     * @return 是否存在
     */
    private boolean hasWebpImages() {
        File dir = new File(getFilesDir(), "data_images");
        if (!dir.exists()) return false;
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".webp")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 下载图片
     *
     * @param dataImagesInfoList 需要下载的图片的信息
     *                           全量下载：请传入dataImagesInfoList
     *                           增量下载：请传入needUpdateDataImagesInfoList
     */
    @SuppressLint("SetTextI18n")
    private void downloadImages(List<DataImagesInfo> dataImagesInfoList) {
        DialogBuilderManager.showDialogWithCallBack(
                this, "二次确认", "\uD83D\uDCE5", "将开始下载图片资源，请注意网络流量消耗。", true,
                "咱手滑了", "开始下载", () -> {
                    final AtomicInteger completedCount = new AtomicInteger(0);

                    // 清空之前的失败记录
                    downloadFailedDataImagesInfoList.clear();

                    TransitionManager.beginDelayedTransition(scrollView, transition);
                    data_images_index_update_info_title.setText("正在下载，已完成(" + completedCount + "/" + dataImagesInfoList.size() + ")⏳");
                    data_images_index_update_info_description.setText("请保持App处于前台状态，并不要退出本界面");
                    // 将卡片点击事件设置为null，防止重复下载
                    data_images_index_update_info_container.setOnClickListener(null);
                    data_images_index_update_info_container.setOnLongClickListener(null);
                    // 下载过程中不能查看图片
                    setAllCardViewEnabled(false);

                    for (DataImagesInfo dataImagesInfo : dataImagesInfoList) {
                        downloadExecutor.execute(() -> {
                            boolean success = downloadSingleImage(dataImagesInfo);
                            int current = completedCount.incrementAndGet();
                            if (!success) {
                                synchronized (downloadFailedDataImagesInfoList) {
                                    downloadFailedDataImagesInfoList.add(dataImagesInfo);
                                }
                            }

                            // 更新下载进度
                            mainHandler.post(() -> data_images_index_update_info_title.setText("正在下载，已完成(" + current + "/" + dataImagesInfoList.size() + ")⏳"));

                            // 全部完成
                            if (current == dataImagesInfoList.size() && !isActivityDestroyed) {
                                mainHandler.post(() -> {
                                    data_images_index_update_info_title.setText("更新完成🎉🎉🎉");
                                    if (downloadFailedDataImagesInfoList.isEmpty()) {
                                        data_images_index_update_info_description.setText(
                                                dataImagesInfoList.size() - downloadFailedDataImagesInfoList.size() + "张图片更新成功，" + downloadFailedDataImagesInfoList.size() + "张图片更新失败");

                                        // 更新本地版本号，取图片信息中版本号最大的值
                                        long newestVersion = 0;
                                        for (DataImagesInfo info : dataImagesInfoList) {
                                            if (info.getVersion() > newestVersion) {
                                                newestVersion = info.getVersion();
                                            }
                                        }
                                        LocalVersionUtil.setImageResourcesVersionCode(this, newestVersion);

                                    } else {
                                        data_images_index_update_info_description.setText(
                                                dataImagesInfoList.size() - downloadFailedDataImagesInfoList.size() + "张图片更新成功，" + downloadFailedDataImagesInfoList.size() + "张图片更新失败" + "\n" +
                                                        "点击本卡片可重新下载更新失败的图片"
                                        );
                                        data_images_index_update_info_container.setOnClickListener(v -> downloadImages(downloadFailedDataImagesInfoList));
                                        data_images_index_update_info_container.setOnLongClickListener(v -> {
                                            downloadImages(dataImagesInfoList);
                                            return true;
                                        });
                                    }
                                    setAllCardViewEnabled(true);
                                });
                            }
                        });
                    }
                });
    }

    /**
     * 下载单张图片的方法
     *
     * @param dataImagesInfo 图片信息
     * @return 是否成功
     */
    private boolean downloadSingleImage(DataImagesInfo dataImagesInfo) {
        HttpURLConnection connection = null;
        try {
            File dir = new File(getFilesDir(), "data_images");
            if (!dir.exists()) dir.mkdirs();

            File tempFile = new File(dir, dataImagesInfo.getFileName() + ".tmp");
            File targetFile = new File(dir, dataImagesInfo.getFileName() + ".png");

            URL url = new URL(dataImagesInfo.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }
                // 下载成功，替换原文件
                if (targetFile.exists()) targetFile.delete();
                return tempFile.renameTo(targetFile);
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "下载失败: " + dataImagesInfo.getFileName(), e);
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("DiscouragedApi")
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout data_images_index_container = findViewById(R.id.data_images_index_container);
        InsetsUtil.setMarginHorizontal(this, data_images_index_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) data_images_index_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            data_images_index_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }

    /**
     * 销毁活动时需要关闭下载
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        if (downloadExecutor != null) {
            downloadExecutor.shutdownNow();
        }
    }
}