package com.careful.HyperFVM.utils.ForUpdate;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                mainHandler.post(() -> callback.onFailure("请求失败，请检查网络或稍后再试"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onFailure("下载失败：" + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onFailure("服务器响应异常"));
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
                            out.write(buf, 0, len);
                            downloaded += len;
                            int progress = (int) (downloaded * 100 / total);
                            mainHandler.post(() -> callback.onProgress(progress)); // 通知进度
                        }
                        mainHandler.post(() -> callback.onSuccess(savePath)); // 下载完成
                    } catch (IOException e) {
                        mainHandler.post(() -> callback.onFailure("文件写入失败：" + e.getMessage()));
                    }
                }).start();
            }
        });
    }
}