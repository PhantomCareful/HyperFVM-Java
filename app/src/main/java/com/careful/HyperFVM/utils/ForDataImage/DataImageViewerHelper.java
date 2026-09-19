package com.careful.HyperFVM.utils.ForDataImage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.OtherUtils.NoMediaFileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataImageViewerHelper {

    // 导出查看副本的相簿目录（Pictures/应用名的根级，与“一键导出”写入的子目录互不干扰）。
    // 查看期间该目录的 .nomedia 会被临时移除（隐藏目录中的条目查看器打不开、无法高清显示），
    // 副本在 App 回到前台时自动删除（看完即删），删除后立即恢复 .nomedia 隐藏标记
    private static final String GALLERY_ALBUM_PATH = "Pictures/HyperFVM";

    // 1x1 透明 PNG（68 字节）。删除查看副本前先用它覆写文件内容，把文件体积缩到最小：
    // 查看期间根级 .nomedia 处于临时移除状态（见 openSystemPhotoViewerToSeeDataImages），
    // 删除可能走厂商相册的“移入回收站”，缩容后即便进了回收站也只占几十字节，回收站自动清理后空间完全释放
    private static final byte[] PLACEHOLDER_PNG_BYTES = Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR4nGNgAAIAAAUAAXpeqz8AAAAASUVORK5CYII=",
            Base64.DEFAULT);

    /**
     * 查看数据图：把原图导出到系统相册（Pictures/HyperFVM），再调用系统图片查看器打开。
     * 导出后的图片会被系统媒体库收录，图库会把它当作“相册自家图片”，用直接从原文件按需解码的高清管线显示，
     * 避免直接传 FileProvider URI 时，图库对高分辨率大图强制降采样显示导致的模糊。
     * 导出的副本会在 App 每次回到前台时自动删除（见 cleanUpExportedImages）。
     * <p>
     * 隐藏标记联动（重要）：实测发现，目录被 .nomedia 隐藏后，其中的媒体条目对本 App 也完全不可访问
     * （转发给图库的条目 URI 被系统拒绝、查看器打不开），因此导出前需先临时移除相簿根目录的 .nomedia
     * （见 NoMediaFileHelper.removeNoMediaFileForViewing）；看完返回 App 后，
     * cleanUpExportedImages 会先删除副本、再恢复隐藏标记，其余时间图片保持隐藏。
     * 移除失败（未授权等）时回退到 FileProvider 方式（可打开，但显示效果可能被系统降采样）。
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

        // 相簿根目录有 .nomedia 隐藏标记时先临时移除：隐藏目录中的副本条目查看器无法访问（实测），
        // 只有“未隐藏”状态下插入的副本才能走图库高清管线打开；移除失败时回退 FileProvider
        if (isAlbumRootHidden(context) && !NoMediaFileHelper.removeNoMediaFileForViewing(context)) {
            openViaFileProvider(context, imageFile);
            return;
        }

        openViaGalleryExport(context, imageFile);
    }

    /**
     * 导出到系统相册（MediaStore 条目）后打开：图库对相册条目走高清解码管线，显示效果最好。
     * 导出或打开失败、以及“假成功”（查看器启动后立即静默退出、焦点仍在本 App）时，
     * 清理刚导出的副本并恢复被临时移除的 .nomedia，回退到 FileProvider 方式重新打开。
     */
    private static void openViaGalleryExport(Context context, File imageFile) {
        Uri exportedUri = exportImageToGallery(context, imageFile);
        if (exportedUri == null) {
            // 导出失败：恢复隐藏标记后直接回退
            NoMediaFileHelper.restoreNoMediaFile(context);
            openViaFileProvider(context, imageFile);
            return;
        }

        if (openSystemPhotoViewer(context, exportedUri)) {
            // 发起打开失败：清理刚导出的副本、恢复隐藏标记，回退 FileProvider
            removeExportedImage(context, exportedUri);
            NoMediaFileHelper.restoreNoMediaFile(context);
            openViaFileProvider(context, imageFile);
            return;
        }

        // 防“假成功”：个别场景下查看器可能启动后立即静默退出（本 App 保持在焦点），
        // startActivity 不抛异常、无法直接感知，因此稍后检查窗口焦点是否已交给查看器；
        // 若仍在本 App，视为打开失败：清理副本、恢复隐藏标记，并回退 FileProvider 重新打开
        if (context instanceof Activity activity) {
            activity.getWindow().getDecorView().postDelayed(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed() && activity.hasWindowFocus()) {
                    Log.d("ACTION_VIEW", "查看器疑似未打开（焦点仍在本 App），回退 FileProvider 方式");
                    removeExportedImage(context, exportedUri);
                    NoMediaFileHelper.restoreNoMediaFile(context);
                    openViaFileProvider(context, imageFile);
                }
            }, 1500);
        }
    }

    /**
     * FileProvider 方式打开：把私有原图包装成 content URI 交给系统图片查看器
     *（授权由 URI 授予机制处理、与媒体库无关；显示效果可能被系统降采样）
     */
    private static void openViaFileProvider(Context context, File imageFile) {
        Uri contentUri = FileProvider.getUriForFile(
                context, context.getPackageName() + ".fileprovider", imageFile);
        if (openSystemPhotoViewer(context, contentUri)) {
            // 所有方式都失败（含没有任何图片查看器、启动被系统拒绝等场景）
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
     * 相簿根目录（Pictures/HyperFVM）是否存在 .nomedia 隐藏标记文件。
     * 存在时媒体库中该目录的条目对其它 App 不可见，图库/媒体查看器打不开导出副本（实测）
     */
    private static boolean isAlbumRootHidden(Context context) {
        try {
            File albumDir = new File(Environment.getExternalStorageDirectory(), NoMediaFileHelper.getAlbumPath(context));
            return new File(albumDir, ".nomedia").exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 调用系统图片查看器打开指定的图片
     *
     * @return 是否成功发起了打开请求；未找到任何图片查看器或启动失败时返回 false（由调用方决定回退或提示）
     */
    private static boolean openSystemPhotoViewer(Context context, Uri contentUri) {
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
            return false;

        } catch (Exception e) {
            Log.d("ACTION_VIEW", "打开图片查看器失败：" + e.getMessage());
            return true;
        }
    }

    /**
     * 把数据图导出到系统相册（写入 Pictures/HyperFVM 根级）。
     * 采用 IS_PENDING 两段式写入：先以隐藏状态插入条目，写完内容后再置为可见，保证相册不会读到半成品。
     * 无需任何存储权限：Android 10+ 对本 App 插入的媒体条目天然拥有完整读写权限。
     * 查看流程会先临时移除目录的 .nomedia（未隐藏状态下插入的条目才能被查看器访问），
     * 副本以正常照片形态短暂存在，看图返回后即被删除并恢复隐藏标记。
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
            // 写入完成，清除待定状态，图片正式就绪（图库打开时不依赖该状态）
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
     * 彻底删除相册中指定的查看副本（打开失败清理、回前台兜底清理共用）。
     * 查看期间根级 .nomedia 处于临时移除状态，删除可能被厂商相册“移入回收站”，为此做两层防御：
     * 1.先尝试用文件路径直接删除底层文件，可完全绕过媒体库的回收站逻辑（受系统版本/厂商限制，可能不可用）；
     * 2.若直删不可用，则先把文件内容覆写为 1x1 占位图（68 字节）再删除，让回收站最多只占几十字节。
     */
    private static void removeExportedImage(Context context, Uri imageUri) {
        ContentResolver resolver = context.getContentResolver();

        // 第 1 层：尝试直接删除底层文件（绕过媒体库的回收站逻辑）
        try (Cursor cursor = resolver.query(imageUri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(0);
                if (path != null && new File(path).delete()) {
                    Log.d("ACTION_VIEW", "已通过文件路径直接删除查看副本：" + path);
                    // 文件已不在，再清掉媒体库中的残留条目
                    try {
                        resolver.delete(imageUri, null, null);
                    } catch (Exception ignored) {
                    }
                    return;
                }
            }
        } catch (Exception e) {
            Log.d("ACTION_VIEW", "文件路径直删不可用，改用缩容后删除：" + e.getMessage());
        }

        // 第 2 层：先把内容覆写为 1x1 占位图，把文件体积缩到最小，再执行删除
        try (OutputStream out = resolver.openOutputStream(imageUri)) {
            if (out != null) {
                out.write(PLACEHOLDER_PNG_BYTES);
            }
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "缩容查看副本失败（不影响后续删除）", e);
        }
        try {
            resolver.delete(imageUri, null, null);
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "删除查看副本失败", e);
        }
    }

    /**
     * 清理相册中遗留的数据图查看副本（Pictures/HyperFVM 根级），并恢复 .nomedia 隐藏标记。
     * 由 HyperFVMApplication 在每次 Activity 回到前台时调用：
     * 1.用户从系统图库返回 App 时，删除刚看完的图（看完直接删除）；
     * 2.同时兜底清理各种原因遗留的旧副本（含冷启动），保证相册中不会残留多张查看副本；
     * 3.若根级 .nomedia 缺失（查看数据图时被临时移除、或异常中断），重新创建并扫描恢复隐藏。
     * 注意：这里用精确匹配而不是 LIKE，只清理根级、不触碰子目录，避免误删“一键导出”保存在子目录（Pictures/HyperFVM/卡片名/）中的图片
     */
    public static void cleanUpExportedImages(Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();

            List<Uri> exportedUris = new ArrayList<>();

            // 查询 Pictures/HyperFVM 根目录下的查看副本条目（开启 .nomedia 时查询不到，天然跳过）
            try (Cursor cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.MediaColumns._ID},
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                    new String[]{NoMediaFileHelper.getAlbumPath(context) + "/"}, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        exportedUris.add(ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0)));
                    }
                }
            }

            // 逐条彻底删除（见 removeExportedImage 的说明）
            for (Uri uri : exportedUris) {
                removeExportedImage(context, uri);
            }
            if (!exportedUris.isEmpty()) {
                Log.d("ACTION_VIEW", "已清理相册中导出的数据图副本：" + exportedUris.size() + " 张");
            }

            // 兜底：根级 .nomedia 生效后，MediaStore 查询查不到隐藏的副本条目，
            // 上面的查询会遗漏这些副本，需再扫描一次文件系统直接删除
            removeLeftoverExportedFiles(context);

            // 最后恢复根级 .nomedia：“查看数据图”会临时移除它（否则查看器打不开副本），
            // 副本清理完成后必须重新隐藏，保证后续内容继续对系统相册隐身
            NoMediaFileHelper.restoreNoMediaFile(context);
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "清理相册中导出的数据图副本失败", e);
        }
    }

    /**
     * 文件系统兜底清理：直接删除相簿根目录下遗留的数据图查看副本文件。
     * .nomedia 生效时副本的 MediaStore 条目对查询不可见，仅靠媒体库查询会漏删、副本越积越多，
     * 因此这里按文件名前缀识别（数据图副本的文件名均以 data_image_ 开头）直接删除底层文件，
     * 不会误删目录中的 .nomedia 或其它内容（避免误删“一键导出”保存在子目录中的图片）
     */
    private static void removeLeftoverExportedFiles(Context context) {
        try {
            File albumDir = new File(Environment.getExternalStorageDirectory(), NoMediaFileHelper.getAlbumPath(context));
            File[] files = albumDir.listFiles();
            if (files == null) {
                return;
            }
            int deleted = 0;
            for (File file : files) {
                if (file.isFile() && file.getName().startsWith("data_image_") && file.delete()) {
                    deleted++;
                }
            }
            if (deleted > 0) {
                Log.d("ACTION_VIEW", "已从文件系统直接删除遗留的数据图副本：" + deleted + " 个");
            }
        } catch (Exception e) {
            Log.e("ACTION_VIEW", "从文件系统清理遗留副本失败", e);
        }
    }
}
