package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 更新检查工具类（URL由外部传入）
 */
public class HttpUtil {
    private static HttpUtil instance;
    private final OkHttpClient client;
    private final Handler mainHandler;

    // 回调接口：请求结果
    public interface OnGetCallback {
        void onSuccess(String content); // 检查成功
        void onFailure(String errorMsg); // 请求失败
    }

    // 单例模式
    private HttpUtil() {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper()); // 确保回调在主线程执行
    }

    public static synchronized HttpUtil getInstance() {
        if (instance == null) {
            instance = new HttpUtil();
        }
        return instance;
    }

    public void getContentFromLink(String url, OnGetCallback callback) {
        // 请求构建可能因url非法抛出IllegalArgumentException（RuntimeException），
        // 若不捕获会直接在调用方线程（多为主线程）崩溃
        final Call call;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            call = client.newCall(request);
        } catch (Exception e) {
            mainHandler.post(() -> callback.onFailure("请求失败：链接无效"));
            return;
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 检查是否是取消导致的失败
                if (!call.isCanceled()) {
                    mainHandler.post(() -> callback.onFailure("请求失败，请检查网络或稍后再试"));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (call.isCanceled()) {
                    return; // 已取消则不处理响应
                }

                // 读取响应体可能抛异常，需自行捕获转为失败回调，
                // 否则回调丢失导致界面永久卡在加载态
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String content = response.body().string().trim();
                        mainHandler.post(() -> callback.onSuccess(content));
                    } else {
                        mainHandler.post(() -> callback.onFailure("检查更新失败，服务器响应异常"));
                    }
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onFailure("读取服务器响应失败，请稍后再试"));
                }
            }
        });
    }

    // 注：文件下载已迁移至 DownloadService（前台服务，
    // 支持通知栏进度/暂停/取消/断点续传，与界面生命周期解耦）
}
