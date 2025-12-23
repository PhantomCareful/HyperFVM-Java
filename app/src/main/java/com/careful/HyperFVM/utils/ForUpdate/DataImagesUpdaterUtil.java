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

public class DataImagesUpdaterUtil {
    private static DataImagesUpdaterUtil instance;
    private final HttpUtil httpUtil;
    private final Handler mainHandler;

    // ========== 统一管理URL/路径常量 ==========
    // 版本检查固定链接
    private static final String VERSION_CHECK_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestDataImagesVersionCode.m3u";
    // 更新日志链接
    private static final String UPDATE_LOG_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestDataImagesUpdateLog.m3u";
    // 全量更新下载链接获取地址
    private static final String FULL_DOWNLOAD_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestDataImagesUpdateUrlAll.m3u";
    // 增量更新下载链接获取地址
    private static final String PARTIAL_DOWNLOAD_URL = "https://gitee.com/phantom-careful/hyper-fvm-updater/raw/main/LatestDataImagesUpdateUrlPart.m3u";

    // 压缩包/解压目录常量（暴露解压目录名称，方便Fragment拼接路径）
    public static final String UNZIP_DIR = "data_images"; // 改为public

    // ========== 新增：下载取消相关变量 ==========
    private String currentZipFilePath; // 保存当前下载的压缩包路径
    private boolean isDownloadCancelled; // 下载取消标记
    private String currentDownloadUrl; // 保存当前下载的URL

    // ========== 回调接口 ==========
    public interface DownloadCallback {
        void onDownloadProgress(int progress);
        void onUnzipProgress(int progress);
        void onSuccess();
        void onFailure(String errorMsg);
    }

    public interface OnVersionCheckCallback {
        void onVersionCheckSuccess(long serverVersion, String updateLog); // 修改：增加updateLog参数
        void onVersionCheckFailure(String errorMsg);
        void onVersionParseError();
    }

    // 新增：获取下载链接回调接口
    public interface OnDownloadUrlCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMsg);
    }

    private DataImagesUpdaterUtil() {
        httpUtil = HttpUtil.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DataImagesUpdaterUtil getInstance() {
        if (instance == null) {
            instance = new DataImagesUpdaterUtil();
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
                    long versionCode = Long.parseLong(serverVersion);
                    // 版本号解析成功，获取更新日志
                    getUpdateLog(versionCode, callback);
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
     * 获取全量更新下载链接
     */
    public void getFullDownloadUrl(OnDownloadUrlCallback callback) {
        httpUtil.getContentFromLink(FULL_DOWNLOAD_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                mainHandler.post(() -> callback.onSuccess(downloadUrl.trim()));
            }

            @Override
            public void onFailure(String errorMsg) {
                mainHandler.post(() -> callback.onFailure("获取全量更新链接失败：" + errorMsg));
            }
        });
    }

    /**
     * 获取增量更新下载链接
     */
    public void getPartialDownloadUrl(OnDownloadUrlCallback callback) {
        httpUtil.getContentFromLink(PARTIAL_DOWNLOAD_URL, new HttpUtil.OnGetCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                mainHandler.post(() -> callback.onSuccess(downloadUrl.trim()));
            }

            @Override
            public void onFailure(String errorMsg) {
                mainHandler.post(() -> callback.onFailure("获取增量更新链接失败：" + errorMsg));
            }
        });
    }

    public boolean isResourcesReady(Context context) {
        File unzipDir = new File(getUnzipPath(context));
        return unzipDir.exists() && unzipDir.isDirectory() && unzipDir.listFiles() != null && Objects.requireNonNull(unzipDir.listFiles()).length > 0;
    }

    public void downloadAndUnzip(Context context, String dynamicDownloadUrl, boolean isFull, DownloadCallback callback) {
        isDownloadCancelled = false;
        currentDownloadUrl = dynamicDownloadUrl;

        // 根据全量/增量设置压缩包文件名
        String zipFileName = isFull ? "data_images_all.zip" : "data_images_part.zip";
        currentZipFilePath = context.getFilesDir() + File.separator + zipFileName;

        // 删除旧的对应压缩包
        deleteFile(new File(currentZipFilePath));

        httpUtil.downloadFileFromLink(dynamicDownloadUrl, currentZipFilePath, new HttpUtil.OnDownloadCallback() {
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
        });
    }

    // ========== 新增：取消下载的核心方法 ==========
    public void cancelDownload() {
        isDownloadCancelled = true;

        // 删除已下载的临时压缩包
        if (currentZipFilePath != null) {
            deleteFile(new File(currentZipFilePath));
        }

        // 取消HTTP下载请求
        if (currentDownloadUrl != null && currentZipFilePath != null) {
            httpUtil.cancelDownload(currentDownloadUrl, currentZipFilePath);
        }

        // 重置状态
        currentDownloadUrl = null;
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