package com.careful.HyperFVM.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.Activities.DataCenter.DataImageInfo;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.utils.ForDownload.DownloadControl;
import com.careful.HyperFVM.utils.ForDownload.DownloadNotificationHelper;
import com.careful.HyperFVM.utils.ForDownload.DownloadStateStore;
import com.careful.HyperFVM.utils.ForDownload.DownloadStateStore.Phase;
import com.careful.HyperFVM.utils.ForDownload.FileDownloader;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载前台服务：数据图批量下载与APK升级包下载的统一宿主。
 * 下载不再绑定任何 Activity——界面 finish 后下载继续在服务内进行；
 * 进度通过通知栏展示（Android 16+ 为 Live Update 实时通知），支持暂停/继续/取消；
 * 状态经 DownloadStateStore 发布，各界面可随时回显当前进度
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    // 命令动作
    public static final String ACTION_START_IMAGES = "com.careful.HyperFVM.download.action.START_IMAGES";
    public static final String ACTION_START_APK = "com.careful.HyperFVM.download.action.START_APK";
    public static final String ACTION_PAUSE = "com.careful.HyperFVM.download.action.PAUSE";
    public static final String ACTION_RESUME = "com.careful.HyperFVM.download.action.RESUME";
    public static final String ACTION_CANCEL = "com.careful.HyperFVM.download.action.CANCEL";

    // 命令参数
    public static final String EXTRA_TASK_TYPE = "extra_task_type";
    public static final String EXTRA_IMAGE_LIST = "extra_image_list";
    public static final String EXTRA_NEWEST_VERSION = "extra_newest_version";
    public static final String EXTRA_APK_URL = "extra_apk_url";
    public static final String EXTRA_SERVER_VERSION_CODE = "extra_server_version_code";

    // 图片批任务的并发下载线程数
    private static final int IMAGE_THREAD_COUNT = 4;
    // 进度通知/状态快照的发布节流间隔
    private static final long PROGRESS_NOTIFY_INTERVAL_MS = 300;

    /**
     * 启动数据图批量下载
     *
     * @param list          本批要下载的图片清单
     * @param newestVersion 云端全量清单中的最大版本号（全部成功后落库）
     */
    public static void startImagesDownload(Context context, ArrayList<DataImageInfo> list, long newestVersion) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START_IMAGES);
        intent.putExtra(EXTRA_TASK_TYPE, DownloadStateStore.TASK_IMAGES);
        intent.putExtra(EXTRA_IMAGE_LIST, list);
        intent.putExtra(EXTRA_NEWEST_VERSION, newestVersion);
        ContextCompat.startForegroundService(context, intent);
    }

    /**
     * 启动APK升级包下载
     *
     * @param serverVersionCode 服务器版本号（下载成功后与文件名一起落库）
     */
    public static void startApkDownload(Context context, String url, long serverVersionCode) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_START_APK);
        intent.putExtra(EXTRA_TASK_TYPE, DownloadStateStore.TASK_APK);
        intent.putExtra(EXTRA_APK_URL, url);
        intent.putExtra(EXTRA_SERVER_VERSION_CODE, serverVersionCode);
        ContextCompat.startForegroundService(context, intent);
    }

    /**
     * 发送暂停/继续/取消命令（服务已处于前台运行，直接 startService 即可）
     */
    public static void sendCommand(Context context, String action, int taskType) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_TASK_TYPE, taskType);
        context.startService(intent);
    }

    private final DownloadStateStore stateStore = DownloadStateStore.getInstance();
    // 进度/结果通知的发送方：POST_NOTIFICATIONS 权限已在发起下载的页面入口申请把关，
    // 无权限时 notify 被系统静默丢弃不会崩溃，故各发送点以 @SuppressLint("MissingPermission") 豁免 lint
    private NotificationManagerCompat notificationManager;

    // ===== 图片批任务状态 =====
    private volatile boolean imagesActive;
    private volatile boolean imagesPaused;
    // 取消后立即阻断进度发布与通知重建（收尾前线程状态尚未切换完成）
    private volatile boolean imagesCancelled;
    private volatile int imagesCompletedCount;
    private volatile int imagesTotalCount;
    private volatile int imagesPercent;
    // 服务器不支持断点续传（带Range请求仍返200）：恢复后已从头重下，通知文案需说明进度回落原因
    private volatile boolean imagesRangeRestarted;
    private DownloadControl imagesControl;
    private List<DataImageInfo> imagesBatchList;
    private long imagesLastProgressPublishTime;

    // ===== APK任务状态 =====
    private volatile boolean apkActive;
    private volatile boolean apkCancelled;
    private volatile int apkPercent;
    // 服务器不支持断点续传（带Range请求仍返200）：恢复后已从头重下，通知文案需说明进度回落原因
    private volatile boolean apkRangeRestarted;
    private DownloadControl apkControl;
    private File currentApkFile;
    private String currentApkUrl;
    private long currentApkServerVersionCode;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        DownloadNotificationHelper.ensureChannel(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            // 进程被系统回收后重启（START_NOT_STICKY 下不应出现，兑底处理）
            if (!imagesActive && !apkActive) {
                stopSelf();
            }
            return START_NOT_STICKY;
        }

        int taskType = intent.getIntExtra(EXTRA_TASK_TYPE, -1);
        switch (intent.getAction()) {
            case ACTION_START_IMAGES:
                handleStartImages(intent);
                break;
            case ACTION_START_APK:
                handleStartApk(intent);
                break;
            case ACTION_PAUSE:
                handlePause(taskType);
                break;
            case ACTION_RESUME:
                handleResume(taskType);
                break;
            case ACTION_CANCEL:
                handleCancel(taskType);
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    // ==================== 图片批任务 ====================

    @SuppressWarnings("unchecked")
    private void handleStartImages(Intent intent) {
        ArrayList<DataImageInfo> list = (ArrayList<DataImageInfo>) intent.getSerializableExtra(EXTRA_IMAGE_LIST);
        long newestVersion = intent.getLongExtra(EXTRA_NEWEST_VERSION, 0);
        // 已有图片任务在跑时忽略重复启动请求（防止连点重复下载）
        if (imagesActive) {
            return;
        }
        if (list == null || list.isEmpty()) {
            // startForegroundService 的启动请求必须以 startForeground 兜底，否则系统会报错
            startAsForeground(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                    DownloadNotificationHelper.buildImagesProgress(this, 0, 0, 0, false));
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            stopSelf();
            return;
        }

        imagesActive = true;
        imagesPaused = false;
        imagesCancelled = false;
        imagesRangeRestarted = false;
        imagesControl = new DownloadControl();
        imagesBatchList = new ArrayList<>(list);
        imagesCompletedCount = 0;
        imagesTotalCount = list.size();
        imagesPercent = 0;

        stateStore.publish(DownloadStateStore.imagesSnapshot(
                Phase.DOWNLOADING, 0, imagesTotalCount, 0, imagesBatchList, new ArrayList<>()));
        refreshForeground();

        final ArrayList<DataImageInfo> batch = new ArrayList<>(list);
        new Thread(() -> runImagesBatch(batch, newestVersion), "images-download").start();
    }

    /**
     * 图片批任务主体：协调线程按序收集各文件结果，工作线程池并发下载
     */
    @SuppressLint("MissingPermission")
    private void runImagesBatch(List<DataImageInfo> list, long newestVersion) {
        DownloadControl control = imagesControl;
        int total = list.size();

        File dir = new File(getFilesDir(), "data_images");
        if (!dir.exists()) dir.mkdirs();

        List<DataImageInfo> failed = Collections.synchronizedList(new ArrayList<>());
        // 槽位=文件下标：记录每个在飞文件的千分比进度，聚合出平滑的总体进度
        Map<Integer, Integer> inflightPercents = new ConcurrentHashMap<>();
        AtomicInteger completed = new AtomicInteger(0);

        try (ExecutorService executor = Executors.newFixedThreadPool(IMAGE_THREAD_COUNT)) {
            try {
                List<Future<FileDownloader.Result>> futures = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    final DataImageInfo info = list.get(i);
                    final int fileIndex = i;
                    futures.add(executor.submit(() -> downloadSingleImage(info, fileIndex, control, inflightPercents, dir)));
                }

                for (int i = 0; i < futures.size(); i++) {
                    FileDownloader.Result result;
                    try {
                        result = futures.get(i).get();
                    } catch (Exception e) {
                        // 兜底：单个任务异常按失败计，不能让整个批次卡死
                        result = FileDownloader.Result.FAILED;
                    }

                    if (result == FileDownloader.Result.CANCELLED) {
                        // 已取消：不再统计，等待其余在飞任务自然退出
                        continue;
                    }
                    if (result != FileDownloader.Result.SUCCESS) {
                        failed.add(list.get(i));
                    }
                    int current = completed.incrementAndGet();
                    publishImagesProgress(current, total, inflightPercents, false);
                }
            } finally {
                executor.shutdown();
            }
        }

        // 取消收尾：清理所有临时文件，状态回到空闲，通知已在取消指令时撤下
        if (control.isCancelled()) {
            deleteTmpFiles(dir);
            stateStore.publish(DownloadStateStore.imagesSnapshot(
                    Phase.IDLE, 0, total, 0, imagesBatchList, new ArrayList<>()));
            finishImagesTask();
            return;
        }

        // 完成收尾：全部成功才更新本地版本号
        // （失败重试时本批清单只是全量的子集，按子集计算会低估版本号，导致下次进页面误报更新）
        if (failed.isEmpty()) {
            LocalVersionUtil.setImageResourcesVersionCode(newestVersion);
        }

        int successCount = total - failed.size();
        Phase phase = failed.isEmpty() ? Phase.SUCCESS : Phase.FAILED;
        stateStore.publish(DownloadStateStore.imagesSnapshot(
                phase, total, total, 100, imagesBatchList, new ArrayList<>(failed)));

        imagesActive = false;
        imagesPaused = false;
        notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                DownloadNotificationHelper.buildImagesResult(this, successCount, failed.size()));
        refreshForeground();
    }

    /**
     * 单张图片下载（工作线程）：暂停后等待恢复并从断点续传，直至成功/失败/取消
     */
    private FileDownloader.Result downloadSingleImage(DataImageInfo info, int fileIndex,
                                                      DownloadControl control, Map<Integer, Integer> inflightPercents, File dir) {
        File tempFile = new File(dir, info.getFileName() + ".tmp");
        File targetFile = new File(dir, info.getFileName() + ".png");
        try {
            while (true) {
                if (control.awaitResumed()) return FileDownloader.Result.CANCELLED;

                FileDownloader.Result result = FileDownloader.download(info.getUrl(), tempFile, control,
                        new FileDownloader.ProgressListener() {
                            @Override
                            public void onProgress(long downloadedBytes, long totalBytes) {
                                if (totalBytes > 0) {
                                    inflightPercents.put(fileIndex, (int) (downloadedBytes * 1000 / totalBytes));
                                    // 单个大文件下载期间也要平滑刷新总体进度（内部按间隔节流）
                                    publishImagesProgress(imagesCompletedCount, imagesTotalCount, inflightPercents, false);
                                }
                            }

                            @Override
                            public void onRangeUnsupported() {
                                // 该文件已从头重下：在飞进度归零并立即刷新通知，避免百分比骤降无解释
                                imagesRangeRestarted = true;
                                inflightPercents.put(fileIndex, 0);
                                publishImagesProgress(imagesCompletedCount, imagesTotalCount, inflightPercents, true);
                            }

                            @Override
                            public void onRangeResumed() {
                                // 续传实际成功（206）：撤下"不支持续传"说明态，
                                // 否则某次CDN误回200置位后，后续每次暂停都会误提示将从头下载
                                imagesRangeRestarted = false;
                                publishImagesProgress(imagesCompletedCount, imagesTotalCount, inflightPercents, true);
                            }
                        });
                if (result == FileDownloader.Result.PAUSED) {
                    continue; // 回到循环顶部等待恢复，恢复后从临时文件断点续传
                }
                if (result == FileDownloader.Result.SUCCESS) {
                    // 下载成功，替换原文件
                    if (targetFile.exists()) targetFile.delete();
                    if (!tempFile.renameTo(targetFile)) {
                        result = FileDownloader.Result.FAILED;
                    }
                }
                return result;
            }
        } finally {
            inflightPercents.remove(fileIndex);
        }
    }

    /**
     * 聚合发布图片批任务进度：completed*100% + 在飞文件的部分进度，按发布节流间隔去频
     */
    @SuppressLint("MissingPermission")
    private void publishImagesProgress(int completed, int total, Map<Integer, Integer> inflightPercents, boolean force) {
        // 暂停/取消后不再发布进度：否则在飞线程退出前的最后一次回调会把PAUSED快照覆盖回DOWNLOADING，
        // 导致页面/通知的暂停状态被回写为下载中
        if (imagesCancelled || imagesPaused) return;
        int inflightSum = 0;
        for (int value : inflightPercents.values()) {
            inflightSum += value;
        }
        int percent = total > 0 ? Math.min(100, (completed * 1000 + inflightSum) / total / 10) : 0;

        long now = System.currentTimeMillis();
        if (!force && now - imagesLastProgressPublishTime < PROGRESS_NOTIFY_INTERVAL_MS) {
            return;
        }
        imagesLastProgressPublishTime = now;
        imagesCompletedCount = completed;
        imagesTotalCount = total;
        imagesPercent = percent;

        stateStore.publish(DownloadStateStore.imagesSnapshot(
                Phase.DOWNLOADING, completed, total, percent, imagesBatchList, new ArrayList<>()));
        if (imagesActive) {
            notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                    DownloadNotificationHelper.buildImagesProgress(this, completed, total, percent, imagesPaused, imagesRangeRestarted));
        }
    }

    private void finishImagesTask() {
        imagesActive = false;
        imagesPaused = false;
        refreshForeground();
    }

    // ==================== APK任务 ====================

    private void handleStartApk(Intent intent) {
        String url = intent.getStringExtra(EXTRA_APK_URL);
        long serverVersionCode = intent.getLongExtra(EXTRA_SERVER_VERSION_CODE, -1);
        // 已有APK任务在跑时忽略重复启动请求
        if (apkActive) {
            return;
        }
        if (url == null || url.isEmpty()) {
            // startForegroundService 的启动请求必须以 startForeground 兜底，否则系统会报错
            startAsForeground(DownloadNotificationHelper.NOTIF_ID_APK,
                    DownloadNotificationHelper.buildApkFailed(this));
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            stopSelf();
            return;
        }

        // 存储不可用（如被系统禁用）时getExternalFilesDir返回null，降级为失败状态
        File externalDir = getExternalFilesDir("apk");
        if (externalDir == null) {
            stateStore.publish(DownloadStateStore.apkSnapshot(
                    Phase.FAILED, 0, null, url, serverVersionCode));
            startAsForeground(DownloadNotificationHelper.NOTIF_ID_APK,
                    DownloadNotificationHelper.buildApkFailed(this));
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
            stopSelf();
            return;
        }
        if (!externalDir.exists()) externalDir.mkdirs();

        currentApkFile = new File(externalDir, "hyper_fvm_update_" + System.currentTimeMillis() + ".apk");
        currentApkUrl = url;
        currentApkServerVersionCode = serverVersionCode;

        apkActive = true;
        apkCancelled = false;
        apkRangeRestarted = false;
        apkControl = new DownloadControl();
        apkPercent = 0;

        stateStore.publish(DownloadStateStore.apkSnapshot(
                Phase.DOWNLOADING, 0, null, url, serverVersionCode));
        refreshForeground();

        final File apkFile = currentApkFile;
        new Thread(() -> runApkDownload(apkFile, url, serverVersionCode), "apk-download").start();
    }

    /**
     * APK下载主体：下载循环（支持取消），完成后落库并发布可直接安装的完成通知
     */
    @SuppressLint("MissingPermission")
    private void runApkDownload(File apkFile, String url, long serverVersionCode) {
        DownloadControl control = apkControl;
        while (true) {
            if (control.awaitResumed()) {
                handleApkCancelled(apkFile);
                return;
            }

            FileDownloader.Result result = FileDownloader.download(url, apkFile, control,
                    new FileDownloader.ProgressListener() {
                        @Override
                        public void onProgress(long downloadedBytes, long totalBytes) {
                            if (totalBytes > 0) {
                                publishApkProgress((int) (downloadedBytes * 100 / totalBytes), false);
                            }
                        }

                        @Override
                        public void onRangeUnsupported() {
                            // 已下载字节作废、从头重下：立即把进度归零并刷新通知，
                            // 否则旧百分比会停留到首个新进度回调才跳变，用户会误以为进度凭空清零
                            apkRangeRestarted = true;
                            publishApkProgress(0, true);
                        }

                        @Override
                        public void onRangeResumed() {
                            // 续传实际成功（206）：撤下"不支持续传"说明态并刷新通知
                            apkRangeRestarted = false;
                            publishApkProgress(apkPercent, true);
                        }
                    });

            if (result == FileDownloader.Result.PAUSED) {
                continue; // 回到循环顶部等待恢复，恢复后断点续传
            }
            switch (result) {
                case SUCCESS: {
                    // 保存APK文件名和版本号到数据库（不依赖界面存活，页面关闭也能正常记录）
                    HyperFVMApplication.getDBHelper()
                            .updateDataStationValue("DownloadedApkFileName", apkFile.getName());
                    HyperFVMApplication.getDBHelper()
                            .updateDataStationValue("DownloadedApkFileVersionCode", String.valueOf(serverVersionCode));

                    stateStore.publish(DownloadStateStore.apkSnapshot(
                            Phase.SUCCESS, 100, apkFile.getAbsolutePath(), url, serverVersionCode));
                    apkActive = false;
                    notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_APK,
                            DownloadNotificationHelper.buildApkResult(this));
                    refreshForeground();
                    return;
                }
                case CANCELLED:
                    handleApkCancelled(apkFile);
                    return;
                default: {
                    stateStore.publish(DownloadStateStore.apkSnapshot(
                            Phase.FAILED, apkPercent, null, url, serverVersionCode));
                    apkActive = false;
                    notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_APK,
                            DownloadNotificationHelper.buildApkFailed(this));
                    refreshForeground();
                    return;
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void publishApkProgress(int percent, boolean force) {
        // 取消后不再发布进度：撤下通知后，在飞回调不得重建通知/覆盖IDLE快照
        if (apkCancelled) return;
        percent = Math.max(0, Math.min(100, percent));
        if (!force && percent == apkPercent) {
            return;
        }
        apkPercent = percent;
        stateStore.publish(DownloadStateStore.apkSnapshot(
                Phase.DOWNLOADING, percent, null, currentApkUrl, currentApkServerVersionCode));
        if (apkActive) {
            notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_APK,
                    DownloadNotificationHelper.buildApkProgress(this, percent, apkRangeRestarted));
        }
    }

    private void handleApkCancelled(File apkFile) {
        // 取消：删除已下载的临时APK文件
        if (apkFile.exists()) {
            apkFile.delete();
        }
        stateStore.publish(DownloadStateStore.apkSnapshot(
                Phase.IDLE, 0, null, currentApkUrl, currentApkServerVersionCode));
        apkActive = false;
        refreshForeground();
    }

    // ==================== 暂停/继续（仅图片任务）/取消 ====================

    private void handlePause(int taskType) {
        if (taskType == DownloadStateStore.TASK_IMAGES && imagesActive && !imagesCancelled && !imagesPaused) {
            imagesControl.pause();
            imagesPaused = true;
            stateStore.publish(DownloadStateStore.imagesSnapshot(
                    Phase.PAUSED, imagesCompletedCount, imagesTotalCount, imagesPercent, imagesBatchList, new ArrayList<>()));
            notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                    DownloadNotificationHelper.buildImagesProgress(this, imagesCompletedCount, imagesTotalCount, imagesPercent, true, imagesRangeRestarted));
        }
        // APK任务不支持暂停：下载源不接受断点续传，暂停后继续只会从头重下（通知只提供「停止升级」）
    }

    private void handleResume(int taskType) {
        if (taskType == DownloadStateStore.TASK_IMAGES && imagesActive && !imagesCancelled && imagesPaused) {
            imagesControl.resume();
            imagesPaused = false;
            stateStore.publish(DownloadStateStore.imagesSnapshot(
                    Phase.DOWNLOADING, imagesCompletedCount, imagesTotalCount, imagesPercent, imagesBatchList, new ArrayList<>()));
            notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                    DownloadNotificationHelper.buildImagesProgress(this, imagesCompletedCount, imagesTotalCount, imagesPercent, false, imagesRangeRestarted));
        }
        // APK任务无暂停态，恢复命令对它无意义
    }

    private void handleCancel(int taskType) {
        if (taskType == DownloadStateStore.TASK_IMAGES && imagesActive) {
            imagesCancelled = true; // 先置位：阻断在飞线程的进度回调重新发布通知
            imagesPaused = false;
            imagesControl.cancel();
            // 通知立即撤下；临时文件清理与状态收尾由批任务线程完成
            removeTaskNotification(taskType);
        } else if (taskType == DownloadStateStore.TASK_APK && apkActive) {
            apkCancelled = true;
            apkControl.cancel();
            // 通知立即撤下；文件删除与状态收尾由下载线程完成
            removeTaskNotification(taskType);
        }
    }

    /**
     * 撤下任务的进度通知。
     * 前台服务关联期间 NotificationManager.cancel 对前台通知无效（系统会保持显示），
     * 需先 stopForeground(REMOVE) 摘除前台关联并移除通知；
     * 若另一任务仍在运行，立即恢复其前台声明（图片任务优先占前台）
     */
    private void removeTaskNotification(int taskType) {
        // 图片任务在跑时它始终占据前台通知，否则前台属于APK任务
        boolean imagesOwnsForeground = imagesActive;
        boolean isForegroundNotification = (taskType == DownloadStateStore.TASK_IMAGES) == imagesOwnsForeground;
        if (isForegroundNotification) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            if (taskType == DownloadStateStore.TASK_IMAGES && apkActive) {
                // 图片任务让出前台后，仍在跑的APK任务升级为前台通知
                startAsForeground(DownloadNotificationHelper.NOTIF_ID_APK,
                        DownloadNotificationHelper.buildApkProgress(this, apkPercent, apkRangeRestarted));
            }
        } else {
            notificationManager.cancel(taskType == DownloadStateStore.TASK_IMAGES
                    ? DownloadNotificationHelper.NOTIF_ID_IMAGES : DownloadNotificationHelper.NOTIF_ID_APK);
        }
    }

    // ==================== 前台状态管理 ====================

    /**
     * 维护前台通知：图片任务优先作为前台通知；
     * 两类任务同时下载时，APK任务以普通通知展示；两类都结束后摘除前台状态并停止服务
     * （DETACH 保留已发布的结果通知，不会被一并移除）
     */
    @SuppressLint("MissingPermission")
    private synchronized void refreshForeground() {
        boolean imagesRunning = imagesActive;
        boolean apkRunning = apkActive;
        if (!imagesRunning && !apkRunning) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
            stopSelf();
            return;
        }

        // 先摘除旧的前台关联（通知本身保留），再按当前优先级重新声明
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH);
        if (imagesRunning) {
            startAsForeground(DownloadNotificationHelper.NOTIF_ID_IMAGES,
                    DownloadNotificationHelper.buildImagesProgress(this, imagesCompletedCount, imagesTotalCount, imagesPercent, imagesPaused, imagesRangeRestarted));
            if (apkRunning) {
                notificationManager.notify(DownloadNotificationHelper.NOTIF_ID_APK,
                        DownloadNotificationHelper.buildApkProgress(this, apkPercent, apkRangeRestarted));
            }
        } else {
            startAsForeground(DownloadNotificationHelper.NOTIF_ID_APK,
                    DownloadNotificationHelper.buildApkProgress(this, apkPercent, apkRangeRestarted));
        }
    }

    private void startAsForeground(int notificationId, android.app.Notification notification) {
        ServiceCompat.startForeground(this, notificationId, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    }

    /**
     * 删除数据图目录下所有临时文件（取消清理）
     */
    private void deleteTmpFiles(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".tmp"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
