package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.ExportInfo;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ImageExportUtil {

    @SuppressLint("DiscouragedApi")
    private static void exportAllImagesToPictures(Context context, String folderName, List<ExportInfo> exportInfoList) {
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

            // 2. 准备文件名和相对路径
            String fileName = exportInfoList.get(i).getFileName() + ".webp";
            // 相对路径：Pictures/应用名/folderName/
            String relativePath = NoMediaFileHelper.getAlbumPath(context) + File.separator
                    + folderName + File.separator;

            // 3. 使用 MediaStore 插入文件记录
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                DialogBuilderManager.showDialog(
                        context,
                        "出现问题",
                        "❌",
                        "无法创建文件，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                        true,
                        "好的");
                return;
            }

            // 4. 写入 Bitmap 到输出流
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    DialogBuilderManager.showDialog(
                            context,
                            "出现问题",
                            "❌",
                            "无法打开输出流，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                            true,
                            "好的");
                    return;
                }

                // 使用无损压缩的WEBP_LOSSLESS格式
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream);
                outputStream.flush();
            } catch (IOException e) {
                Log.e("export", "导出异常", e);

                DialogBuilderManager.showDialog(
                        context,
                        "导出失败",
                        "❌",
                        "异常信息：" + e.getMessage() + "\n\n请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                        false,
                        "好的");

                return;
            }
        }

        // 全部图片写入完成，弹出成功提示
        DialogBuilderManager.showDialog(
                context,
                "导出成功",
                "🎉",
                "所有图片已保存到：\nPictures/HyperFVM/" + folderName,
                true,
                "好耶"
        );
    }

    /**
     * 批量导出图片（导出前自动确保相簿目录及其子目录中存在 .nomedia 文件，导出的图片不会被系统相册展示；
     * 首次使用需要在弹窗中一次性授权相簿目录）
     * @param exportInfoList 封装好的数据类
     */
    public static void exportAllImages(Context context, String folderName, List<ExportInfo> exportInfoList) {
        // 先确保 .nomedia 就位（首次会弹窗引导授权），就位后才开始写入图片：
        // 相簿根目录与本次即将写入的批次子目录都会放置 .nomedia
        // （系统媒体库不会把父目录的 .nomedia 递归应用到子目录，子目录没有标记时图片仍会出现在相册中），
        // 这样即使是首次使用的场景，图片从写入那一刻起就不会被系统相册收录，
        // 不存在“先被相册收录、授权完成后才隐藏”的可见窗口期
        if (context instanceof Activity) {
            NoMediaFileHelper.ensureNoMediaFile((Activity) context, folderName,
                    () -> exportAllImagesToPictures(context, folderName, exportInfoList));
        } else {
            exportAllImagesToPictures(context, folderName, exportInfoList);
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
