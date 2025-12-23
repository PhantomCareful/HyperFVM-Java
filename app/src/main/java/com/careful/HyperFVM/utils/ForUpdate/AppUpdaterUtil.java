package com.careful.HyperFVM.utils.ForUpdate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.Objects;

public class AppUpdaterUtil {

    private final HttpUtil httpUtil;
    private final Handler mainHandler; // 确保回调在主线程执行
    // 统一管理所有远程URL
    private static final String DOWNLOAD_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestUpdateUrl.m3u";
    private static final String VERSION_CODE_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestVersionCode.m3u";
    private static final String UPDATE_LOG_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestUpdateLog.m3u";

    // 版本检查回调
    public interface OnVersionCheckCallback {
        void onVersionCheckSuccess(long serverVersion, String updateLog); // 增加updateLog参数
        void onVersionCheckFailure(String errorMsg);
        void onVersionParseError();
    }

    // 获取下载链接回调
    public interface OnDownloadUrlCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMsg);
    }

    // 下载回调接口
    public interface DownloadCallback {
        void onDownloadProgress(int progress);
        void onSuccess(String apkFilePath);
        void onFailure(String errorMsg);
    }

    // ========== 新增：下载取消相关变量 ==========
    private String currentApkFilePath; // 保存当前下载的APK文件路径
    private boolean isDownloadCancelled; // 下载取消标记
    private String currentDownloadUrl; // 保存当前下载的URL

    // 单例模式
    private static AppUpdaterUtil instance;

    private AppUpdaterUtil() {
        httpUtil = HttpUtil.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AppUpdaterUtil getInstance() {
        if (instance == null) {
            instance = new AppUpdaterUtil();
        }
        return instance;
    }

    /**
     * 检查云端仓库的App版本号（不再对比版本，只返回版本号和更新日志）
     * @param callback 回调监听
     */
    public void checkServerVersion(OnVersionCheckCallback callback) {
        httpUtil.getContentFromLink(VERSION_CODE_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String remoteVersionCodeStr) {
                try {
                    long remoteVersionCode = Long.parseLong(remoteVersionCodeStr);
                    // 版本号解析成功，获取更新日志
                    getUpdateLog(remoteVersionCode, callback);
                } catch (NumberFormatException e) {
                    // 版本号解析失败
                    mainHandler.post(callback::onVersionParseError);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                // 版本号请求失败
                mainHandler.post(() -> callback.onVersionCheckFailure(errorMsg));
            }
        });
    }

    /**
     * 获取更新日志
     */
    private void getUpdateLog(long serverVersion, OnVersionCheckCallback callback) {
        httpUtil.getContentFromLink(UPDATE_LOG_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String updateLog) {
                // 获取更新日志成功，返回版本号和更新日志
                mainHandler.post(() -> callback.onVersionCheckSuccess(serverVersion, updateLog.trim()));
            }

            @Override
            public void onFailure(String errorMsg) {
                // 获取更新日志失败，只返回版本号
                mainHandler.post(() -> callback.onVersionCheckSuccess(serverVersion, ""));
            }
        });
    }

    /**
     * 获取下载链接
     * @param callback 回调监听
     */
    public void getDownloadUrl(OnDownloadUrlCallback callback) {
        httpUtil.getContentFromLink(DOWNLOAD_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // 下载链接获取成功
                mainHandler.post(() -> callback.onSuccess(downloadUrl.trim()));
            }

            @Override
            public void onFailure(String errorMsg) {
                // 下载链接获取失败
                mainHandler.post(() -> callback.onFailure("获取下载链接失败：" + errorMsg));
            }
        });
    }

    /**
     * 下载APK文件的方法
     */
    public void downloadApk(Context context, String downloadUrl, DownloadCallback callback) {
        isDownloadCancelled = false;
        currentDownloadUrl = downloadUrl;

        String apkFileName = "hyper_fvm_update_" + System.currentTimeMillis() + ".apk";
        currentApkFilePath = context.getExternalFilesDir("apk") + File.separator + apkFileName;

        // 确保目录存在
        File apkDir = new File(Objects.requireNonNull(context.getExternalFilesDir("apk")).getPath());
        if (!apkDir.exists()) {
            apkDir.mkdirs();
        }

        // 删除旧的APK文件
        deleteFile(new File(currentApkFilePath));

        httpUtil.downloadFileFromLink(downloadUrl, currentApkFilePath, new HttpUtil.OnDownloadCallback() {
            @Override
            public void onProgress(int progress) {
                if (isDownloadCancelled) return;
                mainHandler.post(() -> callback.onDownloadProgress(progress));
            }

            @Override
            public void onSuccess(String filePath) {
                if (isDownloadCancelled) {
                    deleteFile(new File(filePath));
                    mainHandler.post(() -> callback.onFailure("下载已取消"));
                    return;
                }
                mainHandler.post(() -> callback.onSuccess(filePath));
            }

            @Override
            public void onFailure(String errorMsg) {
                String finalMsg = isDownloadCancelled ? "下载已取消" : errorMsg;
                mainHandler.post(() -> callback.onFailure(finalMsg));
            }
        });
    }

    /**
     * 新增：取消下载的核心方法
     */
    public void cancelDownload() {
        isDownloadCancelled = true;

        // 删除已下载的临时APK文件
        if (currentApkFilePath != null) {
            deleteFile(new File(currentApkFilePath));
        }

        // 取消HTTP下载请求
        if (currentDownloadUrl != null && currentApkFilePath != null) {
            httpUtil.cancelDownload(currentDownloadUrl, currentApkFilePath);
        }

        // 重置状态
        currentDownloadUrl = null;
        currentApkFilePath = null;
    }

    /**
     * 删除文件
     */
    private boolean deleteFile(File file) {
        return file.exists() && file.delete();
    }
}