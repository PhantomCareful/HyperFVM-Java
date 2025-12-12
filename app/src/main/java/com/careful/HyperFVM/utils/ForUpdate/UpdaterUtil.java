package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

public class UpdaterUtil {

    private final HttpUtil httpUtil;
    private final Handler mainHandler; // 确保回调在主线程执行
    // 统一管理所有远程URL
    private static final String DOWNLOAD_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestUpdateUrl.m3u";
    private static final String VERSION_CODE_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestVersionCode.m3u";
    private static final String UPDATE_LOG_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestUpdateLog.m3u";

    // 更新检查结果回调，增加下载链接参数
    public interface OnUpdateCheckListener {
        void onNoUpdate(); // 无新版本
        void onHasUpdate(String updateLog, String downloadUrl); // 有新版本，返回更新日志和下载链接
        void onError(String errorMsg); // 检查失败
    }

    // 单例模式
    private static UpdaterUtil instance;

    private UpdaterUtil() {
        httpUtil = HttpUtil.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized UpdaterUtil getInstance() {
        if (instance == null) {
            instance = new UpdaterUtil();
        }
        return instance;
    }

    /**
     * 检查更新的入口方法（不再需要传入URL参数）
     * @param localVersionCode 本地版本号
     * @param listener 回调监听
     */
    public void checkUpdate(long localVersionCode, OnUpdateCheckListener listener) {
        // 使用类内部的URL常量
        httpUtil.getContentFromLink(VERSION_CODE_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String remoteVersionCodeStr) {
                try {
                    long remoteVersionCode = Long.parseLong(remoteVersionCodeStr);
                    // 比对版本号
                    if (remoteVersionCode <= localVersionCode) {
                        // 无更新
                        mainHandler.post(listener::onNoUpdate);
                    } else {
                        // 有更新，先获取更新日志，再获取下载链接
                        getUpdateLogAndLink(listener);
                    }
                } catch (NumberFormatException e) {
                    // 版本号解析失败
                    mainHandler.post(() -> listener.onError("版本信息解析错误"));
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                // 版本号请求失败
                mainHandler.post(() -> listener.onError(errorMsg));
            }
        });
    }

    /**
     * 先获取更新日志，再获取下载链接（不再需要传入URL参数）
     */
    private void getUpdateLogAndLink(OnUpdateCheckListener listener) {
        httpUtil.getContentFromLink(UPDATE_LOG_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String updateLog) {
                // 日志获取成功后，继续获取下载链接
                getDownloadUrl(updateLog, listener);
            }

            @Override
            public void onFailure(String errorMsg) {
                // 日志获取失败
                mainHandler.post(() -> listener.onError("获取更新日志失败：" + errorMsg));
            }
        });
    }

    /**
     * 获取下载链接
     */
    private void getDownloadUrl(String updateLog, OnUpdateCheckListener listener) {
        httpUtil.getContentFromLink(DOWNLOAD_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // 下载链接获取成功，返回更新日志和下载链接
                mainHandler.post(() -> listener.onHasUpdate(updateLog, downloadUrl.trim()));
            }

            @Override
            public void onFailure(String errorMsg) {
                // 下载链接获取失败
                mainHandler.post(() -> listener.onError("获取下载链接失败：" + errorMsg));
            }
        });
    }
}