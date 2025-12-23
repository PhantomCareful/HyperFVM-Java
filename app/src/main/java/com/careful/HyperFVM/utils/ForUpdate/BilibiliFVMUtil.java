package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

public class BilibiliFVMUtil {
    private final HttpUtil httpUtil;
    private final Handler mainHandler; // 确保回调在主线程执行

    private static final String GET_BILIBILI_FVM_ANNOUNCEMENT_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestBilibiliFVMAnnouncement.m3u";

    // 获取结果回调
    public interface OnGetCallback {
        void onSuccess(String latestBilibiliFVMUrl); // 请求成功
        void onFailure(String errorMsg); // 请求失败
    }

    // 单例模式
    private static BilibiliFVMUtil instance;

    private BilibiliFVMUtil() {
        httpUtil = HttpUtil.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized BilibiliFVMUtil getInstance() {
        if (instance == null) {
            instance = new BilibiliFVMUtil();
        }
        return instance;
    }

    public void getLatestBilibiliFVMAnnouncement(OnGetCallback callback) {
        // 使用类内部的URL常量
        httpUtil.getContentFromLink(GET_BILIBILI_FVM_ANNOUNCEMENT_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String latestBilibiliFVMUrl) {
                mainHandler.post(() -> callback.onSuccess(latestBilibiliFVMUrl));
            }

            @Override
            public void onFailure(String errorMsg) {
                mainHandler.post(() -> callback.onFailure(errorMsg));
            }
        });
    }
}
