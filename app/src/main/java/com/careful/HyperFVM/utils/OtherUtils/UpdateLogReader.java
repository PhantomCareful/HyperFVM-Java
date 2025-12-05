package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

/**
 * 读取assets目录下更新日志（.txt）的工具类
 * 封装读取逻辑，解耦Activity/Fragment
 */
public class UpdateLogReader {
    // 回调接口：返回读取结果（异步读取用）
    public interface ReadCallback {
        // 读取成功，返回文本内容
        void onReadSuccess(String content);
        // 读取失败，返回错误信息
        void onReadFailed(String errorMsg);
    }

    /**
     * 【同步读取】assets下的txt文件（不推荐主线程调用大文件）
     * @param context 上下文（建议传ApplicationContext）
     * @param fileName 文件名（如 "update_log.txt"）
     * @return 文本内容（空字符串=读取失败）
     */
    public static String readAssetsTxtSync(Context context, String fileName) {
        // 弱引用上下文，避免内存泄漏
        WeakReference<Context> contextRef = new WeakReference<>(context.getApplicationContext());
        Context appContext = contextRef.get();
        if (appContext == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (
                // 自动关闭流（try-with-resources语法，无需手动close）
                InputStream is = appContext.getAssets().open(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n"); // 保留换行符
            }

            // 读取完成后，判断是否有内容，有则删除最后一个换行符
            if (content.length() > 0) {
                content.deleteCharAt(content.length() - 1); // 删除最后一个\n
            }
        } catch (IOException e) {
            return "";
        }
        return content.toString();
    }

    /**
     * 【异步读取】assets下的txt文件（推荐，避免主线程阻塞）
     * @param context 上下文
     * @param fileName 文件名
     * @param callback 读取结果回调（主线程执行）
     */
    public static void readAssetsTxtAsync(Context context, String fileName, ReadCallback callback) {
        // 子线程执行读取
        new Thread(() -> {
            String content = readAssetsTxtSync(context, fileName);
            // 切回主线程回调结果
            new Handler(Looper.getMainLooper()).post(() -> {
                if (content.isEmpty()) {
                    callback.onReadFailed("读取失败：文件不存在或编码错误");
                } else {
                    callback.onReadSuccess(content);
                }
            });
        }).start();
    }
}