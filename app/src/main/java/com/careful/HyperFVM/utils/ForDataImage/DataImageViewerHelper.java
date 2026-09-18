package com.careful.HyperFVM.utils.ForDataImage;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DataImageViewerHelper {

    // 导出到系统相册的相簿目录（Pictures 下的子目录，相册中会显示为一个独立相簿）
    private static final String GALLERY_ALBUM_PATH = "Pictures/HyperFVM";

    /**
     * 查看数据图：先把图片导出到系统相册（Pictures/HyperFVM），再调用系统图片查看器打开。
     * 导出后的图片会被系统媒体库收录，图库会把它当作“相册自家图片”，用直接从原文件按需解码的高清管线显示，
     * 避免直接传 FileProvider URI 时，图库对高分辨率大图强制降采样显示导致的模糊。
     * 导出的副本会在 App 每次回到前台时自动删除（见 cleanUpExportedImages）。
     */
    public static void openSystemPhotoViewerToSeeDataImages(Context context, String imageName) {
        File dir = new File(context.getFilesDir(), "data_images");
        File imageFile = new File(dir, imageName + ".png");

        if (!imageFile.exists()) {
            DialogBuilderManager.showDialog(
                    context,
                    context.getResources().getString(R.string.title_dialog_data_images_index_open_failed_file_not_found),
                    "❌",
                    context.getResources().getString(R.string.content_dialog_data_images_index_open_failed_file_not_found),
                    true,
                    "好的"
            );
            return;
        }

        // 优先导出到系统相册，让图库按高清管线显示；导出失败则回退到 FileProvider 方式（显示效果可能被降采样）
        Uri exportedUri = exportImageToGallery(context, imageFile);
        Uri contentUri = exportedUri != null
                ? exportedUri
                : FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 优先使用系统自带的相册/图库App进行查看，如果没找到，再弹出选择窗口
            // 先查询所有能处理此Intent的Activity列表
            PackageManager packageManager = context.getPackageManager();
            @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            Log.d("ACTION_VIEW", resolveInfos.toString());

            if (resolveInfos.isEmpty()) {
                // 列表为空，说明没有任何应用，抛出异常
                throw new ActivityNotFoundException();
            }

            String targetPackageName = null;
            for (ResolveInfo info : resolveInfos) {
                ActivityInfo activityInfo = info.activityInfo;

                if (activityInfo == null) {
                    continue;
                }

                String pkg = activityInfo.packageName;

                Log.d("ACTION_VIEW", "当前遍历的App：" + pkg);

                // 使用各家系统图库包名的关键词进行判断，找到第一个匹配的就跳出循环
                if (pkg.contains("gallery") || pkg.contains("photo") || pkg.contains("media")) {
                    targetPackageName = pkg;

                    Log.d("ACTION_VIEW", "匹配到图片查看器：" + targetPackageName);

                    break;
                }
            }

            // 结束循环后，检查是否匹配到包名
            if (targetPackageName != null) {
                Log.d("ACTION_VIEW", "最终使用的图片查看器：" + targetPackageName);
                intent.setPackage(targetPackageName);
            }

            context.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            // 打开失败：删除刚导出的相册副本，避免残留
            if (exportedUri != null) {
                deleteExportedImage(context, exportedUri);
            }
            DialogBuilderManager.showDialog(
                    context,
                    context.getResources().getString(R.string.title_dialog_data_images_index_open_failed_app_not_found),
                    "❌",
                    context.getResources().getString(R.string.content_dialog_data_images_index_open_failed_app_not_found),
                    true,
                    "好的"
            );
        }
    }

    /**
     * 把数据图导出到系统相册（写入 Pictures/HyperFVM 相簿）。
     * 采用 IS_PENDING 两段式写入：先以隐藏状态插入条目，写完内容后再置为可见，保证相册不会读到半成品。
     * 无需任何存储权限：Android 10+ 对本 App 插入的媒体条目天然拥有完整读写权限。
     *
     * @return 相册中该图片的条目 URI；导出失败返回 null（调用方回退到 FileProvider 方式）
     */
    @Nullable
    private static Uri exportImageToGallery(Context context, File imageFile) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, GALLERY_ALBUM_PATH);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return null;
            }
            try (InputStream in = new FileInputStream(imageFile);
                 OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("打开相册输出流失败");
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            // 写入完成，清除待定状态，图片正式对相册可见
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return uri;
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "导出数据图到系统相册失败，回退到FileProvider方式", e);
            // 清理写入失败的半成品条目
            if (uri != null) {
                try {
                    resolver.delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            return null;
        }
    }

    /**
     * 删除相册中指定的导出图片（打开失败时清理用）
     */
    private static void deleteExportedImage(Context context, Uri imageUri) {
        try {
            context.getContentResolver().delete(imageUri, null, null);
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "删除导出的数据图失败", e);
        }
    }

    /**
     * 清理系统相册中导出的全部数据图副本（Pictures/HyperFVM 相簿下的所有条目）。
     * 由 HyperFVMApplication 在每次 Activity 回到前台时调用：
     * 1.用户从系统图库返回时删除刚看完的图（看完直接删除）；
     * 2.同时兜底清理各种原因遗留的旧副本（含冷启动），保证相册中不会残留多张导出图。
     * 无需存储权限：无权限时 MediaStore 的删除只会作用在本 App 插入的条目上。
     */
    public static void cleanUpExportedImages(Context context) {
        try {
            int deleted = context.getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ?",
                    new String[]{GALLERY_ALBUM_PATH + "/%"});
            if (deleted > 0) {
                Log.d("ACTION_VIEW", "已清理相册中导出的数据图：" + deleted + " 张");
            }
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "清理相册中导出的数据图失败", e);
        }
    }
}
