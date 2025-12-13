package com.careful.HyperFVM.utils.ForUpdate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadDataImagesUtil {
    private static DownloadDataImagesUtil instance;
    private final HttpUtil httpUtil;
    private final Handler mainHandler;

    // ========== 统一管理URL/路径常量 ==========
    // 版本检查固定链接
    private static final String VERSION_CHECK_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestDataImagesVersionCode.m3u";
    // 下载链接前缀（版本号动态拼接）
    private static final String DOWNLOAD_URL_PREFIX = "https://gitee.com/phantom-careful/hyper-fvm-updater/releases/download/";

    // 压缩包/解压目录常量（暴露解压目录名称，方便Fragment拼接路径）
    public static final String UNZIP_DIR = "data_images"; // 改为public

    // ========== 新增：下载取消相关变量 ==========
    private HttpUtil.OnDownloadCallback currentDownloadCallback; // 保存当前下载回调
    private String currentZipFilePath; // 保存当前下载的压缩包路径
    private boolean isDownloadCancelled; // 下载取消标记

    // ========== 回调接口 ==========
    public interface DownloadCallback {
        void onDownloadProgress(int progress);
        void onUnzipProgress(int progress);
        void onSuccess();
        void onFailure(String errorMsg);
    }

    public interface OnVersionCheckCallback {
        void onVersionCheckSuccess(String serverVersion);
        void onVersionCheckFailure(String errorMsg);
        void onVersionParseError();
    }

    private DownloadDataImagesUtil() {
        httpUtil = HttpUtil.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DownloadDataImagesUtil getInstance() {
        if (instance == null) {
            instance = new DownloadDataImagesUtil();
        }
        return instance;
    }

    // ========== 暴露解压路径的获取方法（方便Fragment调用） ==========
    public String getUnzipPath(Context context) {
        return context.getFilesDir() + File.separator + UNZIP_DIR;
    }

    // ========== 版本检查/下载解压逻辑 ==========
    public void checkServerVersion(OnVersionCheckCallback callback) {
        httpUtil.getContentFromLink(VERSION_CHECK_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String content) {
                String serverVersion = content.trim();
                try {
                    Integer.parseInt(serverVersion);
                    mainHandler.post(() -> callback.onVersionCheckSuccess(serverVersion));
                } catch (NumberFormatException e) {
                    mainHandler.post(callback::onVersionParseError);
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                mainHandler.post(() -> callback.onVersionCheckFailure(errorMsg));
            }
        });
    }

    /**
     *
     * @param versionCode 图片资源最新的版本号，用于拼接下载链接
     * @param isFull 进行全量/增量更新
     * @return 拼接好的下载链接
     */
    public String getDownloadUrl(String versionCode, boolean isFull) {
        String fileName = isFull ? "data_images_all.zip" : "data_images_part.zip";
        return DOWNLOAD_URL_PREFIX + versionCode + "/" + fileName;
    }

    public boolean isResourcesReady(Context context) {
        File unzipDir = new File(getUnzipPath(context));
        return unzipDir.exists() && unzipDir.isDirectory() && unzipDir.listFiles() != null && Objects.requireNonNull(unzipDir.listFiles()).length > 0;
    }

    public void downloadAndUnzip(Context context, String dynamicDownloadUrl, boolean isFull, DownloadCallback callback) {
        isDownloadCancelled = false;
        // 根据全量/增量设置压缩包文件名
        String zipFileName = isFull ? "data_images_all.zip" : "data_images_part.zip";
        currentZipFilePath = context.getFilesDir() + File.separator + zipFileName;

        // 删除旧的对应压缩包
        deleteFile(new File(currentZipFilePath));

        currentDownloadCallback = new HttpUtil.OnDownloadCallback() {
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

                new Thread(() -> {
                    try {
                        if (isDownloadCancelled) {
                            throw new Exception("下载已取消");
                        }
                        unzip(filePath, getUnzipPath(context), callback);
                        deleteFile(new File(filePath)); // 解压后删除压缩包
                        mainHandler.post(callback::onSuccess);
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onFailure("解压失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误")));
                    }
                }).start();
            }

            @Override
            public void onFailure(String errorMsg) {
                String finalMsg = isDownloadCancelled ? "下载已取消" : errorMsg;
                mainHandler.post(() -> callback.onFailure(finalMsg));
            }
        };

        // 发起下载请求
        httpUtil.downloadFileFromLink(dynamicDownloadUrl, currentZipFilePath, currentDownloadCallback);
    }

    // ========== 新增：取消下载的核心方法 ==========
    public void cancelDownload() {
        // 标记为已取消
        isDownloadCancelled = true;

        // 1. 终止当前下载回调（如果HttpUtil有主动取消下载的方法，需在此补充，例如：
        // httpUtil.cancelCurrentDownload();
        // 需根据HttpUtil的实际实现调整，以下是通用兜底逻辑）
        if (currentDownloadCallback != null) {
            mainHandler.post(() -> currentDownloadCallback.onFailure("下载已取消"));
        }

        // 2. 删除已下载的临时压缩包
        if (currentZipFilePath != null) {
            deleteFile(new File(currentZipFilePath));
        }

        // 3. 重置状态
        currentDownloadCallback = null;
        currentZipFilePath = null;
    }

    // 内部解压/文件删除逻辑
    private void unzip(String zipFilePath, String unzipDirPath, DownloadCallback callback) throws Exception {
        // 检查取消标记，若已取消则抛出异常
        if (isDownloadCancelled) {
            throw new Exception("下载已取消");
        }

        File unzipDir = new File(unzipDirPath);
        if (!unzipDir.exists()) unzipDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            int totalFiles = 0;
            List<ZipEntry> entries = new ArrayList<>();

            // 先统计总文件数（用于计算进度）
            while ((entry = zis.getNextEntry()) != null) {
                // 检查取消标记
                if (isDownloadCancelled) {
                    throw new Exception("下载已取消");
                }
                if (!entry.isDirectory()) {
                    totalFiles++;
                    entries.add(entry);
                }
            }

            // 重新打开流进行解压（因为上面的流已读完）
            try (ZipInputStream zis2 = new ZipInputStream(new FileInputStream(zipFilePath))) {
                int extractedFiles = 0;
                while ((entry = zis2.getNextEntry()) != null) {
                    // 检查取消标记
                    if (isDownloadCancelled) {
                        throw new Exception("下载已取消");
                    }
                    if (!entry.isDirectory()) {
                        String entryPath = unzipDirPath + File.separator + entry.getName();
                        File entryFile = new File(entryPath);

                        // 创建父目录
                        if (!Objects.requireNonNull(entryFile.getParentFile()).exists()) {
                            entryFile.getParentFile().mkdirs();
                        }

                        // 写入文件
                        try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zis2.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }

                        extractedFiles++;
                        int progress = (int) (((float) extractedFiles / totalFiles) * 100);
                        mainHandler.post(() -> callback.onUnzipProgress(progress));
                    }
                    zis2.closeEntry();
                }
            }
        }
    }

    private boolean deleteFile(File file) {
        return file.exists() && file.delete();
    }
}