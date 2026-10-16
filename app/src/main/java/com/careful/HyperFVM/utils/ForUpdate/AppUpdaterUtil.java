package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

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

    // 注：APK的实际下载已迁移至 DownloadService（前台服务，
    // 支持通知栏进度/暂停/取消/断点续传，与界面生命周期解耦），
    // 本类仅保留版本检查与下载链接获取
}