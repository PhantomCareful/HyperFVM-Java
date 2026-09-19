package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.ExportInfo;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ImageExportUtil {

    /**
     * 批量导出图片：逐个取出图片资源，打包为一个 ZIP 压缩包，保存到系统“下载”目录（Download/应用名）。
     * 压缩包不是媒体文件，系统相册不会收录，因此无需任何存储权限、也无需在相簿目录放置隐藏标记；
     * 用户拿到的是单一压缩包，更方便转存与管理。
     */
    @SuppressLint("DiscouragedApi")
    public static void exportAllImages(Context context, String folderName, List<ExportInfo> exportInfoList) {
        // 1.先在应用缓存目录中打包（缓存包用完即删）
        File zipFile = new File(context.getCacheDir(), "export_cache.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            // 图片本身已是压缩格式，压缩包只做归档、不再二次压缩（打包更快，内容原样保留）
            zipOut.setLevel(Deflater.NO_COMPRESSION);

            for (int i = 0; i < exportInfoList.size(); i++) {
                // 通过名称获取资源 ID
                int resId = context.getResources().getIdentifier(exportInfoList.get(i).getDrawableResName(), "drawable", context.getPackageName());
                if (resId == 0) {
                    DialogBuilderManager.showDialog(
                            context,
                            "资源不存在",
                            "❌",
                            "未找到名为 " + exportInfoList.get(i).getDrawableResName() + " 的图片资源，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                            true,
                            "好的"
                    );
                    return;
                }

                Drawable drawable = ContextCompat.getDrawable(context, resId);
                if (drawable == null) {
                    return;
                }
                Bitmap bitmap;
                if (drawable instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                } else {
                    // 如果 Drawable 不是 BitmapDrawable（例如 VectorDrawable），尝试通过 Canvas 绘制为 Bitmap
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }

                if (bitmap == null) {
                    DialogBuilderManager.showDialog(
                            context,
                            "出现问题",
                            "❌",
                            "无法获取图片，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                            true,
                            "好的");
                    return;
                }

                // 写入压缩包条目：文件名与逐张导出时保持一致，内容使用无损压缩的 WEBP_LOSSLESS 格式
                zipOut.putNextEntry(new ZipEntry(exportInfoList.get(i).getFileName() + ".webp"));
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, zipOut);
                zipOut.closeEntry();
            }
        } catch (IOException e) {
            Log.e("export", "打包导出异常", e);
            DialogBuilderManager.showDialog(
                    context,
                    "导出失败",
                    "❌",
                    "异常信息：" + e.getMessage() + "\n\n请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                    false,
                    "好的");
            return;
        }

        // 2.把压缩包写入系统“下载”目录（Download/应用名），完成后删除缓存包
        if (!copyZipToDownloads(context, zipFile, folderName)) {
            zipFile.delete();
            DialogBuilderManager.showDialog(
                    context,
                    "出现问题",
                    "❌",
                    "无法创建文件，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                    true,
                    "好的");
            return;
        }
        zipFile.delete();

        // 全部图片打包完成，弹出成功提示
        DialogBuilderManager.showDialog(
                context,
                "导出成功",
                "🎉",
                "所有图片已打包保存到：\nDownload/" + context.getResources().getString(R.string.app_name) + "/" + folderName + ".zip",
                true,
                "好耶"
        );
    }

    /**
     * 把缓存目录中打好包的 ZIP 复制到系统“下载”目录（Download/应用名）。
     * 采用 IS_PENDING 两段式写入：先以“待定”状态插入条目，内容写完后再置为就绪，保证用户拿到的一定是完整文件；
     * Android 10+ 非媒体文件的合规写入目录本就包含 Download，无需任何存储权限
     *
     * @return 是否成功保存
     */
    private static boolean copyZipToDownloads(Context context, File zipFile, String folderName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, folderName + ".zip");
        values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
        values.put(MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + File.separator + context.getResources().getString(R.string.app_name));
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = null;
        try {
            uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return false;
            }
            try (InputStream in = new FileInputStream(zipFile);
                 OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("打开下载目录输出流失败");
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            // 写入完成，清除待定状态，压缩包正式就绪
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            context.getContentResolver().update(uri, values, null, null);
            return true;
        } catch (Exception e) {
            Log.e("export", "保存压缩包到下载目录失败", e);
            // 清理写入失败的半成品条目
            if (uri != null) {
                try {
                    context.getContentResolver().delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            return false;
        }
    }

    /**
     * 将指定ImageView显示的图片封装成数据类，方便统一导出
     * @param drawableResName drawable文件名
     * @param fileName 导出图片的文件名
     * @return 封装好的数据类
     */
    public static ExportInfo generateExportInfo(String drawableResName, String fileName) {
        ExportInfo exportInfo = new ExportInfo();
        exportInfo.setDrawableResName(drawableResName);
        exportInfo.setFileName(fileName);

        return exportInfo;
    }

}
