package com.careful.HyperFVM.utils.ForDownload;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 单文件下载引擎（数据图与APK升级包共用）。
 * 支持断点续传：以临时文件当前长度为起点发送 Range 请求，服务器不支持时自动从头下载；
 * 支持暂停（保留临时文件，恢复后续传）与取消（断开连接，由调用方清理临时文件）
 */
public class FileDownloader {
    private static final String TAG = "FileDownloader";

    /**
     * 下载结果
     */
    public enum Result {
        SUCCESS,  // 下载完成
        PAUSED,   // 已暂停：临时文件保留，恢复后可从断点续传
        CANCELLED,// 已取消
        FAILED    // 失败
    }

    /**
     * 字节进度回调（调用方负责跨任务聚合与节流）
     */
    public interface ProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);

        /**
         * 服务器不支持断点续传：已发送Range请求但服务器返回200全量，
         * 已下载数据作废、从头重新下载（调用方可据此提示用户进度将归零）
         */
        default void onRangeUnsupported() {
        }

        /**
         * 断点续传成功：本次Range请求拿到206，调用方可撤下此前的"不支持续传"说明态
         */
        default void onRangeResumed() {
        }
    }

    /**
     * 下载单个文件到 tmpFile（追加模式由 Range 响应码决定）
     *
     * @param urlStr      下载地址
     * @param tmpFile     临时文件（断点续传的落盘位置）
     * @param control     暂停/取消控制柄
     * @param listener    进度回调，可为 null
     * @return 下载结果
     */
    public static Result download(String urlStr, File tmpFile, DownloadControl control, ProgressListener listener) {
        HttpURLConnection connection = null;
        try {
            // 断点续传起点：临时文件已有的字节数
            long offset = tmpFile.exists() ? tmpFile.length() : 0;

            URL url = new URL(urlStr);
            int responseCode;
            boolean rangeRetried = false;
            while (true) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestMethod("GET");
                if (offset > 0) {
                    connection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                // 先挂接再检查状态：避免与 pause()/cancel() 的断开动作交错导致漏断
                control.attach(connection);
                if (control.isCancelled()) return Result.CANCELLED;
                if (control.isPaused()) return Result.PAUSED;

                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK && offset > 0 && !rangeRetried) {
                    // CDN边缘节点可能对某URL的首个Range请求回缓存的200全量
                    // （实测Gitee raw即如此：首个Range请求200，之后的Range请求均206），
                    // 立即重连重试一次再下结论，避免误判为不支持续传而从头重下
                    rangeRetried = true;
                    control.detach(connection);
                    connection.disconnect();
                    connection = null;
                    continue;
                }
                break;
            }
            boolean append;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (offset > 0 && listener != null) {
                    // 重试后携带Range仍返回200：服务器确实不支持断点续传，即将从头覆盖下载
                    listener.onRangeUnsupported();
                }
                // 服务器忽略 Range（或首次下载）：从头写入
                append = false;
                offset = 0;
            } else if (responseCode == 206) {
                // 服务器支持断点续传：从临时文件末尾追加
                append = true;
                if (offset > 0 && listener != null) {
                    // 续传实际成功：通知调用方撤下此前可能误置的"不支持续传"说明
                    listener.onRangeResumed();
                }
            } else {
                Log.w(TAG, "服务器响应异常: HTTP " + responseCode + " - " + urlStr);
                return Result.FAILED;
            }

            // 206 时 contentLength 是剩余字节数，需要加上 offset 还原总长；
            // 服务器未返回 contentLength 时为 -1（进度未知，只上报已下载字节）
            long contentLength = connection.getContentLengthLong();
            long totalBytes = contentLength > 0 ? contentLength + offset : -1;

            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(tmpFile, append)) {
                byte[] buffer = new byte[8192];
                int len;
                long downloaded = offset;
                // 进度节流：每500毫秒或每变化1‰上报一次，避免高频回调刷爆通知
                long lastReportTime = 0;
                int lastPermille = -1;
                while ((len = in.read(buffer)) != -1) {
                    if (control.isCancelled()) return Result.CANCELLED;
                    if (control.isPaused()) return Result.PAUSED;

                    out.write(buffer, 0, len);
                    downloaded += len;

                    if (listener != null && totalBytes > 0) {
                        int permille = (int) (downloaded * 1000 / totalBytes);
                        long now = System.currentTimeMillis();
                        if (permille != lastPermille && now - lastReportTime >= 500) {
                            lastPermille = permille;
                            lastReportTime = now;
                            listener.onProgress(downloaded, totalBytes);
                        }
                    }
                }
                out.flush();
            }

            // 边界情况：暂停/取消恰好发生在最后一次读（返回-1）之后
            if (control.isCancelled()) return Result.CANCELLED;
            if (control.isPaused()) return Result.PAUSED;
            return Result.SUCCESS;
        } catch (Exception e) {
            // 主动 disconnect 会让阻塞中的 read 抛 IOException，此时按暂停/取消处理而非失败；
            // 兜底捕获RuntimeException避免下载线程崩溃导致App闪退
            if (control.isCancelled()) return Result.CANCELLED;
            if (control.isPaused()) return Result.PAUSED;
            Log.e(TAG, "下载失败: " + urlStr, e);
            return Result.FAILED;
        } finally {
            control.detach(connection);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
