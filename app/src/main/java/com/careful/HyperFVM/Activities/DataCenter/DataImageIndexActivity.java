package com.careful.HyperFVM.Activities.DataCenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageCardActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageDecomposeAndGetActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageMouseHpActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageOthersActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageTiramisuActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageWeaponAndGemActivity;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.DownloadService;
import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;

import androidx.core.widget.NestedScrollView;

import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForDownload.DownloadNotificationHelper;
import com.careful.HyperFVM.utils.ForDownload.DownloadStateStore;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class DataImageIndexActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_data_image_index_scroll_y";
    // 通知栏「重新下载失败的图片」action携带的重试标记
    public static final String EXTRA_RETRY_FAILED_DOWNLOAD = "extra_retry_failed_download";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;
    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）
    private final String TAG = "DataImageIndexActivity";

    private final String DATA_IMAGE_URL = "https://raw.giteeusercontent.com/phantom-careful/hyper-fvm-updater/raw/main/DataImages/DataImagesUrl.m3u";
    // 存储从DATA_IMAGE_URL获取到的图片信息
    private final List<DataImageInfo> dataImageInfoList = new ArrayList<>();
    // 存储需要更新的图片信息
    private final List<DataImageInfo> needUpdateDataImageInfoList = new ArrayList<>();
    // 存储下载失败的图片信息
    private final List<DataImageInfo> downloadFailedDataImageInfoList = new ArrayList<>();

    private long localVersionCode;

    private NestedScrollView scrollView;
    private TransitionSet transition;

    private LinearLayout data_image_index_update_info_container;
    private TextView data_image_index_update_info_title;
    private TextView data_image_index_update_info_description;

    private LinearLayout data_image_index_delete_container;
    private TextView data_image_index_delete_title;
    TextView data_image_index_delete_description;

    private LinearLayout data_image_index_card_container;
    private LinearLayout data_image_index_weapon_and_gem_container;
    private LinearLayout data_image_index_decompose_and_get_container;
    private LinearLayout data_image_index_mouse_hp_container;
    private LinearLayout data_image_index_others_container;
    private LinearLayout data_image_index_tiramisu_container;

    private Handler mainHandler;

    // 通知权限请求（下载前检查，拒绝不阻塞下载，仅提示看不到进度通知）
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private Runnable pendingDownloadRunnable;

    // 下载状态监听：onStart注册（注册即回放快照）、onStop注销，任意时刻回到本页同步进度
    private final DownloadStateStore.Listener imagesDownloadListener = this::applyImagesDownloadSnapshot;
    // 页面当前是否正处于下载相关展示（下载中/暂停/完成），用于识别“下载被取消”后的UI恢复
    private boolean showingDownloadUi;

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
        setContentView(R.layout.activity_data_image_index);

        scrollView = findViewById(R.id.scrollView);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长

        data_image_index_update_info_container = findViewById(R.id.data_image_index_update_info_container);
        data_image_index_update_info_title = findViewById(R.id.data_image_index_update_info_title);
        data_image_index_update_info_description = findViewById(R.id.data_image_index_update_info_description);
        data_image_index_update_info_title.setText("正在检查图片版本⏳");
        data_image_index_update_info_description.setText("请稍候，很快就好哦");

        mainHandler = new Handler(Looper.getMainLooper());

        // 注册通知权限请求：下载前若未授予通知权限则先请求，拒绝也不阻塞下载
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "通知权限未开启，下载进度将不在通知栏展示", Toast.LENGTH_LONG).show();
                    }
                    if (pendingDownloadRunnable != null) {
                        pendingDownloadRunnable.run();
                        pendingDownloadRunnable = null;
                    }
                });

        data_image_index_delete_container = findViewById(R.id.data_image_index_delete_container);
        data_image_index_delete_title = findViewById(R.id.data_image_index_delete_title);
        data_image_index_delete_description = findViewById(R.id.data_image_index_delete_description);

        if (hasWebpImages()) {
            data_image_index_delete_title.setText("检测到旧版App下载的图片");
            data_image_index_delete_description.setText("这些图片的清晰度较低，当前版本已不支持查看。您可以长按本卡片将它们删除，然后使用上面的卡片重新下载更高清的图片。");
        } else {
            data_image_index_delete_title.setText("删除所有图片");
            data_image_index_delete_description.setText("长按本卡片可执行删除操作\n如果您遇到图片无法查看的问题，可尝试先删除所有图片再重新下载");
        }
        data_image_index_delete_container.setOnLongClickListener(v -> {
            DialogBuilderManager.showDialogWithCallBack(
                    this, "二次确认", "🗑️", "将删除所有本地图片。删除后，您需要重新下载才能查看。", true,
                    "咱手滑了", "开始删除", () -> {
                        data_image_index_delete_title.setText("操作执行中⏳");
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

                        data_image_index_delete_title.setText("删除完成🎉🎉🎉");
                        data_image_index_delete_description.setText("长按本卡片可执行删除操作\n如果您遇到图片无法查看的问题，可尝试先删除所有图片再重新下载");

                        LocalVersionUtil.setImageResourcesVersionCode(1);
                        mainHandler.post(() -> {
                            setAllCardViewEnabled(false);
                            data_image_index_delete_container.setEnabled(true);
                            TransitionManager.beginDelayedTransition(scrollView, transition);
                            data_image_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
                            data_image_index_update_info_description.setText("删除图片后需要重新下载才能查看哦");
                            data_image_index_update_info_container.setOnClickListener(view -> downloadImages(dataImageInfoList));
                        });
                    });

            return true;
        });

        data_image_index_card_container = findViewById(R.id.data_image_index_card_container);
        data_image_index_weapon_and_gem_container = findViewById(R.id.data_image_index_weapon_and_gem_container);
        data_image_index_decompose_and_get_container = findViewById(R.id.data_image_index_decompose_and_get_container);
        data_image_index_mouse_hp_container = findViewById(R.id.data_image_index_mouse_hp_container);
        data_image_index_others_container = findViewById(R.id.data_image_index_others_container);
        data_image_index_tiramisu_container = findViewById(R.id.data_image_index_tiramisu_container);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化各入口卡片的点击事件
        initViews();

        // 从云端仓库获取数据图信息（版本号、访问链接等）
        getDataImageInfoFromGit(new DataImageCatchResultCallBack() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess() {
                // 获取成功以后，需要将所有图片的version与数据库存储的versionCode进行对比
                // 如果有图片更新，则单独下载这些图片
                // 如果是第一次使用，则数据库存储的versionCode为0，自然所有图片都需要更新

                // 获取本地版本号
                localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();

                // 有下载任务正在进行时以下载进度为准，云端检查结果不覆盖进度显示
                if (DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_IMAGES).isActive()) {
                    return;
                }
                mainHandler.post(() -> refreshUpdateInfoCard());
            }

            @Override
            public void onFailed(Exception e) {
                // 失败的话，显示异常日志
                data_image_index_update_info_title.setText("出现问题❌❌❌");
                data_image_index_update_info_description.setText(e.getMessage());

                // 虽然报错了，但是如果本地已经下载了图片，还是允许查看
                if (localVersionCode != 0 && localVersionCode != 1) {
                    setAllCardViewEnabled(true);
                }
            }
        });
    }

    /**
     * 根据本地版本状态刷新更新卡片展示（主线程调用）。
     * 云端检查成功与下载被取消后的UI恢复共用：图片清单已在内存，无需重新联网
     */
    @SuppressLint("SetTextI18n")
    private void refreshUpdateInfoCard() {
        localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
        TransitionManager.beginDelayedTransition(scrollView, transition);
        // 如果localVersionCode == 0，则需要将入口卡片的enable值设为false，因为此时还没有存储任何图片，打开会报错。
        if (localVersionCode == 0) {
            setAllCardViewEnabled(false);
            data_image_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
            data_image_index_update_info_description.setText("第一次使用需要先下载图片才能查看哦");
            data_image_index_update_info_container.setOnClickListener(v -> downloadImages(dataImageInfoList));
            data_image_index_update_info_container.setOnLongClickListener(null);
        } else if (localVersionCode == 1) {
            setAllCardViewEnabled(false);
            data_image_index_update_info_title.setText("点击本卡片获取图片资源📣📣📣");
            data_image_index_update_info_description.setText("删除图片后需要重新下载才能查看哦");
            data_image_index_update_info_container.setOnClickListener(v -> downloadImages(dataImageInfoList));
            data_image_index_update_info_container.setOnLongClickListener(null);
        } else {
            needUpdateDataImageInfoList.clear();
            boolean needUpdate = false;
            for (int i = 0; i < dataImageInfoList.size(); i++) {
                // 如有云端图片版本号更新，将其信息加入需要更新的List中
                Log.d(TAG, dataImageInfoList.get(i).getFileName() + ": 云端" + dataImageInfoList.get(i).getVersion() + ", 本地" + localVersionCode);
                if (localVersionCode < dataImageInfoList.get(i).getVersion()) {
                    needUpdateDataImageInfoList.add(dataImageInfoList.get(i));
                    needUpdate = true;
                }
            }

            setAllCardViewEnabled(true);
            if (needUpdate) {
                data_image_index_update_info_title.setText(needUpdateDataImageInfoList.size() + "张图片需要更新📣📣📣");
                data_image_index_update_info_description.setText("单击本卡片仅下载有更新的图片\n长按本卡片下载全部图片(耗时更长，仅在需要时使用)");
                data_image_index_update_info_container.setOnClickListener(v -> downloadImages(needUpdateDataImageInfoList));
            } else {
                data_image_index_update_info_title.setText("已经是最新版本😋😋😋");
                data_image_index_update_info_description.setText("如有需要，可长按本卡片重新下载图片");
                data_image_index_update_info_container.setOnClickListener(null);
            }
            data_image_index_update_info_container.setOnLongClickListener(v -> {
                downloadImages(dataImageInfoList);
                return true;
            });
        }
    }

    private void getDataImageInfoFromGit(DataImageCatchResultCallBack callBack) {
        new Thread(() -> {
            try {
                // 第1步：从给定的链接获取JSON字符串
                String JSONArrayStr = XMLHelper.getContentFromUrl(DATA_IMAGE_URL);
                if (JSONArrayStr == null) {
                    // 网络异常/内容获取失败：必须主动回调失败，避免JSONArray构造抛出NPE导致线程静默死亡
                    callBack.onFailed(new IOException("内容获取失败，请检查网络后重试"));
                    return;
                }

                // 第2步：将JSON字符串转换成JSON数组
                JSONArray jsonArray = new JSONArray(JSONArrayStr);

                JSONObject itemObj;

                // 第3步：遍历数组中的每个JSON对象
                for (int i = 0; i < jsonArray.length(); i++) {
                    // 提取单个JSON对象
                    itemObj = jsonArray.getJSONObject(i);

                    // 讲JSON中的内容存储到DataImageInfo类中
                    DataImageInfo dataImageInfo = new DataImageInfo();
                    dataImageInfo.setFileName(itemObj.getString("fileName"));
                    dataImageInfo.setVersion(Long.parseLong(itemObj.getString("version")));
                    dataImageInfo.setUrl(itemObj.getString("url"));

                    // 加入List
                    dataImageInfoList.add(dataImageInfo);
                }

                // 第4步：触发回调
                callBack.onSuccess();

            } catch (Exception e) {
                // 兜底捕获RuntimeException（如version字段非数字抛NumberFormatException），
                // 否则未捕获异常会杀死本工作线程导致App闪退
                callBack.onFailed(e);
            }
        }).start();
    }

    private void initViews() {
        data_image_index_card_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageCardActivity.class)));
        data_image_index_weapon_and_gem_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageWeaponAndGemActivity.class)));
        data_image_index_decompose_and_get_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageDecomposeAndGetActivity.class)));
        data_image_index_mouse_hp_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageMouseHpActivity.class)));
        data_image_index_others_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageOthersActivity.class)));
        data_image_index_tiramisu_container.setOnClickListener(v -> startActivity(new Intent(this, DataImageTiramisuActivity.class)));
    }

    private void setAllCardViewEnabled(boolean enabled) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> setAllCardViewEnabled(enabled));
            return;
        }
        data_image_index_delete_container.setEnabled(enabled);
        data_image_index_tiramisu_container.setEnabled(enabled);
        data_image_index_card_container.setEnabled(enabled);
        data_image_index_weapon_and_gem_container.setEnabled(enabled);
        data_image_index_decompose_and_get_container.setEnabled(enabled);
        data_image_index_mouse_hp_container.setEnabled(enabled);
        data_image_index_others_container.setEnabled(enabled);
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
     * @param dataImageInfoList 需要下载的图片的信息
     *                           全量下载：请传入dataImageInfoList
     *                           增量下载：请传入needUpdateDataImageInfoList
     *                           失败重试：请传入downloadFailedDataImageInfoList
     */
    @SuppressLint("SetTextI18n")
    private void downloadImages(List<DataImageInfo> dataImageInfoList) {
        DialogBuilderManager.showDialogWithCallBack(
                this, "二次确认", "\uD83D\uDCE5", "将开始下载图片资源\n请注意网络流量消耗", true,
                "咱手滑了", "开始下载", () -> {
                    // 快照本次要下载的图片清单：失败重试时传入的是downloadFailedDataImageInfoList本身，
                    // 若直接引用它，后续的clear()会把清单一并清空，导致进度显示0/0且不提交任何下载任务
                    final List<DataImageInfo> imagesToDownload = new ArrayList<>(dataImageInfoList);

                    // 清空之前的失败记录
                    downloadFailedDataImageInfoList.clear();

                    // 全量清单的最大版本号：全部下载成功后由服务落库
                    // （失败重试时本次清单只是全量的子集，按子集计算会低估版本号，导致下次进页面误报更新）
                    long newestVersion = 0;
                    for (DataImageInfo info : this.dataImageInfoList) {
                        if (info.getVersion() > newestVersion) {
                            newestVersion = info.getVersion();
                        }
                    }

                    // 下载交由前台服务进行，不与当前界面绑定：界面 finish 后下载继续，
                    // 完成后版本号落库与失败统计均由服务负责
                    final long finalNewestVersion = newestVersion;
                    ensureNotificationPermissionThen(() ->
                            DownloadService.startImagesDownload(this, new ArrayList<>(imagesToDownload), finalNewestVersion));

                    TransitionManager.beginDelayedTransition(scrollView, transition);
                    data_image_index_update_info_title.setText("正在下载，已完成(0/" + imagesToDownload.size() + ")⏳");
                    data_image_index_update_info_description.setText("下载过程中请注意不要杀死App");
                    // 将卡片点击事件设置为null，防止重复下载
                    data_image_index_update_info_container.setOnClickListener(null);
                    data_image_index_update_info_container.setOnLongClickListener(null);
                    // 下载过程中不能查看图片
                    setAllCardViewEnabled(false);
                });
    }

    /**
     * 下载前确保通知权限：Android 13+ 未授予通知权限时先请求，
     * 无论授予与否都继续执行下载（拒绝仅看不到通知栏进度）
     */
    private void ensureNotificationPermissionThen(Runnable action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            pendingDownloadRunnable = action;
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            action.run();
        }
    }

    /**
     * 根据下载服务发布的快照同步本页 UI（主线程回调，含注册时的快照回放）
     */
    @SuppressLint("SetTextI18n")
    private void applyImagesDownloadSnapshot(DownloadStateStore.Snapshot snapshot) {
        if (snapshot == null || snapshot.taskType != DownloadStateStore.TASK_IMAGES) {
            return;
        }
        if (snapshot.phase != DownloadStateStore.Phase.IDLE) {
            showingDownloadUi = true;
        }
        switch (snapshot.phase) {
            case IDLE:
                // 空闲态不覆盖云端检查结果展示的卡片状态；
                // 但若页面正处于下载相关展示，说明下载被取消了，需恢复为本地版本状态的卡片（可重新发起下载）
                if (showingDownloadUi) {
                    showingDownloadUi = false;
                    refreshUpdateInfoCard();
                }
                return;
            case DOWNLOADING:
                TransitionManager.beginDelayedTransition(scrollView, transition);
                data_image_index_update_info_title.setText("正在下载，已完成(" + snapshot.completedCount + "/" + snapshot.totalCount + ")⏳");
                data_image_index_update_info_description.setText("下载过程中请注意不要杀死App");
                data_image_index_update_info_container.setOnClickListener(null);
                data_image_index_update_info_container.setOnLongClickListener(null);
                setAllCardViewEnabled(false);
                break;
            case PAUSED:
                TransitionManager.beginDelayedTransition(scrollView, transition);
                data_image_index_update_info_title.setText("下载已暂停(" + snapshot.completedCount + "/" + snapshot.totalCount + ")");
                data_image_index_update_info_description.setText("点击本卡片继续下载");
                data_image_index_update_info_container.setOnClickListener(v ->
                        DownloadService.sendCommand(this, DownloadService.ACTION_RESUME, DownloadStateStore.TASK_IMAGES));
                data_image_index_update_info_container.setOnLongClickListener(null);
                setAllCardViewEnabled(false);
                break;
            case SUCCESS:
            case FAILED:
                renderImagesDownloadResult(snapshot);
                break;
        }
    }

    /**
     * 渲染下载完成态：全部成功可长按重下；部分失败可点击重试失败项、长按重下本批
     */
    @SuppressLint("SetTextI18n")
    private void renderImagesDownloadResult(DownloadStateStore.Snapshot snapshot) {
        TransitionManager.beginDelayedTransition(scrollView, transition);
        data_image_index_update_info_title.setText("更新完成🎉🎉🎉");

        final List<DataImageInfo> batchList = new ArrayList<>(snapshot.batchList);
        int successCount = snapshot.totalCount - snapshot.failedList.size();
        if (snapshot.failedList.isEmpty()) {
            data_image_index_update_info_description.setText(
                    successCount + "张图片更新成功，" + snapshot.failedList.size() + "张图片更新失败");
            data_image_index_update_info_container.setOnClickListener(null);
            data_image_index_update_info_container.setOnLongClickListener(v -> {
                downloadImages(batchList);
                return true;
            });
        } else {
            // 刷新本页的失败重试名单（与服务快照保持同步）
            downloadFailedDataImageInfoList.clear();
            downloadFailedDataImageInfoList.addAll(snapshot.failedList);
            data_image_index_update_info_description.setText(
                    successCount + "张图片更新成功，" + snapshot.failedList.size() + "张图片更新失败" + "\n" +
                            "点击本卡片可重新下载更新失败的图片");
            data_image_index_update_info_container.setOnClickListener(v -> downloadImages(downloadFailedDataImageInfoList));
            data_image_index_update_info_container.setOnLongClickListener(v -> {
                downloadImages(batchList);
                return true;
            });
        }
        setAllCardViewEnabled(true);
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
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout data_image_index_container = findViewById(R.id.data_image_index_container);
        InsetsUtil.setMarginHorizontal(this, data_image_index_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) data_image_index_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            data_image_index_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        nestedScrollUtil = NestedScrollUtil.attach(
                findViewById(R.id.scrollView),
                findViewById(R.id.topBarBottom),
                topBar,
                blurViewTopBar,
                TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 注册下载状态监听（注册即回放快照）：回到本页立即同步当前下载进度
        DownloadStateStore.getInstance().addListener(DownloadStateStore.TASK_IMAGES, imagesDownloadListener);
        handleNotificationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        // 保持getIntent为最新，避免后续onStart重复消费旧通知意图
        setIntent(intent);
        // 本页已在前台时点击通知只会走onNewIntent（不经过onStart），
        // 必须在这里处理撤通知与重试action，否则完成通知撤不掉、重试不生效
        handleNotificationIntent(intent);
    }

    /**
     * 处理通知点击带来的意图（onStart/onNewIntent均会触发，重复消费由extra标记防抖）：
     * 1. 任务已结束时撤下结果通知——完成通知为promoted ongoing不可滑除，页面打开即视为已读；
     *    下载进行中的进度通知保留（由服务继续更新）
     * 2. 「重新下载失败的图片」action：弹出确认框重新下载失败清单
     */
    private void handleNotificationIntent(Intent intent) {
        if (intent == null) return;
        if (!DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_IMAGES).isActive()) {
            NotificationManagerCompat.from(this).cancel(DownloadNotificationHelper.NOTIF_ID_IMAGES);
        }
        if (intent.getBooleanExtra(EXTRA_RETRY_FAILED_DOWNLOAD, false)) {
            intent.putExtra(EXTRA_RETRY_FAILED_DOWNLOAD, false); // 消费标记：避免onStart等入口重复触发
            mainHandler.post(() -> {
                List<DataImageInfo> retryList = downloadFailedDataImageInfoList;
                if (retryList.isEmpty()) {
                    // 页面未曾同步过失败清单（如冷启动直接点通知）时从全局快照取
                    retryList = new ArrayList<>(
                            DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_IMAGES).failedList);
                }
                if (!retryList.isEmpty()) {
                    downloadImages(retryList);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadStateStore.getInstance().removeListener(imagesDownloadListener);
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}