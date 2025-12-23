package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Call> downloadCalls; // 新增：存储多个下载请求

    // 回调接口：请求结果
    public interface OnGetCallback {
        void onSuccess(String content); // 检查成功
        void onFailure(String errorMsg); // 请求失败
    }

    // 下载回调接口
    public interface OnDownloadCallback {
        void onProgress(int progress); // 下载进度（0-100）
        void onSuccess(String filePath); // 下载成功，返回文件路径
        void onFailure(String errorMsg); // 下载失败
    }

    // 单例模式
    private HttpUtil() {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper()); // 确保回调在主线程执行
        downloadCalls = new HashMap<>(); // 初始化下载请求映射
    }

    public static synchronized HttpUtil getInstance() {
        if (instance == null) {
            instance = new HttpUtil();
        }
        return instance;
    }

    public void getContentFromLink(String url, OnGetCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 检查是否是取消导致的失败
                if (!call.isCanceled()) {
                    mainHandler.post(() -> callback.onFailure("请求失败，请检查网络或稍后再试"));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (call.isCanceled()) {
                    return; // 已取消则不处理响应
                }

                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().string().trim();
                    mainHandler.post(() -> callback.onSuccess(content));
                } else {
                    mainHandler.post(() -> callback.onFailure("检查更新失败，服务器响应异常"));
                }
            }
        });
    }

    public void downloadFileFromLink(String url, String savePath, OnDownloadCallback callback) {
        // 生成唯一的下载标识符（使用URL和保存路径的组合）
        String downloadKey = url + "|" + savePath;

        // 先取消相同标识符的下载请求（如果有）
        cancelDownloadByKey(downloadKey);

        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        // 保存当前请求
        downloadCalls.put(downloadKey, call);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 从映射中移除
                downloadCalls.remove(downloadKey);

                // 过滤取消导致的失败回调
                if (!call.isCanceled()) {
                    mainHandler.post(() -> callback.onFailure("下载失败：" + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // 已取消的请求不处理响应
                if (call.isCanceled()) {
                    downloadCalls.remove(downloadKey);
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onFailure("服务器响应异常"));
                    downloadCalls.remove(downloadKey);
                    return;
                }

                // 处理字节流并写入文件（需在子线程执行）
                new Thread(() -> {
                    try (InputStream in = response.body().byteStream();
                         FileOutputStream out = new FileOutputStream(savePath)) {

                        long total = response.body().contentLength();
                        byte[] buf = new byte[1024];
                        int len;
                        long downloaded = 0;

                        while ((len = in.read(buf)) != -1) {
                            // 检查是否已取消
                            if (call.isCanceled()) {
                                mainHandler.post(() -> callback.onFailure("下载已取消"));
                                downloadCalls.remove(downloadKey);
                                return;
                            }

                            out.write(buf, 0, len);
                            downloaded += len;
                            int progress = (int) (downloaded * 100 / total);
                            mainHandler.post(() -> callback.onProgress(progress)); // 通知进度
                        }
                        mainHandler.post(() -> callback.onSuccess(savePath)); // 下载完成
                        downloadCalls.remove(downloadKey);
                    } catch (IOException e) {
                        // 过滤取消导致的异常
                        if (!call.isCanceled()) {
                            mainHandler.post(() -> callback.onFailure("文件写入失败：" + e.getMessage()));
                        }
                        downloadCalls.remove(downloadKey);
                    }
                }).start();
            }
        });
    }

    /**
     * 根据键取消下载
     */
    private void cancelDownloadByKey(String key) {
        Call call = downloadCalls.get(key);
        if (call != null && !call.isCanceled()) {
            call.cancel();
            downloadCalls.remove(key);
        }
    }

    /**
     * 取消所有下载请求
     */
    public void cancelAllDownloads() {
        for (Call call : downloadCalls.values()) {
            if (!call.isCanceled()) {
                call.cancel();
            }
        }
        downloadCalls.clear();
    }

    /**
     * 根据URL和保存路径取消特定下载
     */
    public void cancelDownload(String url, String savePath) {
        String key = url + "|" + savePath;
        cancelDownloadByKey(key);
    }
}