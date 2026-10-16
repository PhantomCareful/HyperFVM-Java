package com.careful.HyperFVM.utils.ForDownload;

import android.os.Handler;
import android.os.Looper;

import com.careful.HyperFVM.Activities.DataCenter.DataImageInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 进程级下载状态快照仓库（单例）。
 * DownloadService 在状态变化时发布快照，各界面在 onStart 注册监听（注册即回放当前快照）、
 * onStop 注销——任意时刻回到界面都能同步当前的下载进度，与 Activity 生命周期完全解耦
 */
public class DownloadStateStore {

    public static final int TASK_IMAGES = 0; // 数据图批量下载
    public static final int TASK_APK = 1;    // APK升级包下载

    /**
     * 任务阶段
     */
    public enum Phase {
        IDLE,        // 无任务（或已取消清理完毕）
        DOWNLOADING, // 下载中
        PAUSED,      // 已暂停
        SUCCESS,     // 已完成
        FAILED       // 已结束但存在失败项（图片批任务）/ 下载失败（APK任务）
    }

    /**
     * 任务状态快照（不可变，字段按任务类型择用）
     */
    public static class Snapshot {
        public final int taskType;
        public final Phase phase;
        // 图片批任务：已完成/总数与百分比（APK任务 percent 直接使用）
        public final int completedCount;
        public final int totalCount;
        public final int percent;
        // 图片批任务：本批清单与失败清单（供点击重试/长按全量重下）
        public final List<DataImageInfo> batchList;
        public final List<DataImageInfo> failedList;
        // APK任务：已下载完成的文件路径 / 远程下载地址 / 服务器版本号
        public final String apkFilePath;
        public final String apkDownloadUrl;
        public final long serverVersionCode;

        Snapshot(int taskType, Phase phase, int completedCount, int totalCount, int percent,
                 List<DataImageInfo> batchList, List<DataImageInfo> failedList,
                 String apkFilePath, String apkDownloadUrl, long serverVersionCode) {
            this.taskType = taskType;
            this.phase = phase;
            this.completedCount = completedCount;
            this.totalCount = totalCount;
            this.percent = percent;
            this.batchList = batchList;
            this.failedList = failedList;
            this.apkFilePath = apkFilePath;
            this.apkDownloadUrl = apkDownloadUrl;
            this.serverVersionCode = serverVersionCode;
        }

        /**
         * 是否处于活跃下载（下载中或已暂停）
         */
        public boolean isActive() {
            return phase == Phase.DOWNLOADING || phase == Phase.PAUSED;
        }
    }

    /**
     * 状态监听器（主线程回调）
     */
    public interface Listener {
        void onStateChanged(Snapshot snapshot);
    }

    private static volatile DownloadStateStore instance;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Snapshot[] snapshots = new Snapshot[2];
    @SuppressWarnings("unchecked")
    private final CopyOnWriteArrayList<Listener>[] listeners = new CopyOnWriteArrayList[]{new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>()};

    private DownloadStateStore() {
        snapshots[TASK_IMAGES] = new Snapshot(TASK_IMAGES, Phase.IDLE, 0, 0, 0,
                new ArrayList<>(), new ArrayList<>(), null, null, -1);
        snapshots[TASK_APK] = new Snapshot(TASK_APK, Phase.IDLE, 0, 0, 0,
                new ArrayList<>(), new ArrayList<>(), null, null, -1);
    }

    public static DownloadStateStore getInstance() {
        if (instance == null) {
            synchronized (DownloadStateStore.class) {
                if (instance == null) {
                    instance = new DownloadStateStore();
                }
            }
        }
        return instance;
    }

    /**
     * 注册监听器，并立即在主线程回放当前快照（实现回到界面即同步进度）
     */
    public void addListener(int taskType, Listener listener) {
        listeners[taskType].addIfAbsent(listener);
        Snapshot snapshot = snapshots[taskType];
        mainHandler.post(() -> listener.onStateChanged(snapshot));
    }

    /**
     * 注销监听器（界面在 onStop 调用）
     */
    public void removeListener(Listener listener) {
        listeners[TASK_IMAGES].remove(listener);
        listeners[TASK_APK].remove(listener);
    }

    /**
     * 获取当前快照
     */
    public Snapshot getSnapshot(int taskType) {
        return snapshots[taskType];
    }

    /**
     * 发布新快照（Service 调用），主线程分发给对应任务的所有监听器
     */
    public void publish(Snapshot snapshot) {
        snapshots[snapshot.taskType] = snapshot;
        List<Listener> targets = new ArrayList<>(listeners[snapshot.taskType]);
        if (targets.isEmpty()) return;
        mainHandler.post(() -> {
            for (Listener listener : targets) {
                listener.onStateChanged(snapshot);
            }
        });
    }

    // ===== 便捷构造方法，避免各处拼装超长构造参数 =====

    public static Snapshot imagesSnapshot(Phase phase, int completedCount, int totalCount, int percent,
                                          List<DataImageInfo> batchList, List<DataImageInfo> failedList) {
        return new Snapshot(TASK_IMAGES, phase, completedCount, totalCount, percent,
                batchList, failedList, null, null, -1);
    }

    public static Snapshot apkSnapshot(Phase phase, int percent,
                                       String apkFilePath, String apkDownloadUrl, long serverVersionCode) {
        return new Snapshot(TASK_APK, phase, 0, 0, percent,
                new ArrayList<>(), new ArrayList<>(), apkFilePath, apkDownloadUrl, serverVersionCode);
    }
}
