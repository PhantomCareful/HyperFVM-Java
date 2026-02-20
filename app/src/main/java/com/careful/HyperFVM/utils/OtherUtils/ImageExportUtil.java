package com.careful.HyperFVM.utils.OtherUtils;

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
import android.widget.ImageView;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ImageExportUtil {
    private static void exportCardImageToPictures(Context context, ImageView imageView, String folderName, String cardName, String categoryName) {
        // 1. 获取 ImageView 中的 Drawable 并转为 Bitmap
        Drawable drawable = imageView.getDrawable();
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
            DialogBuilderManager.showDialog(context,
                    "出现问题",
                    "无法获取图片，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                    true,
                    "好的");
            return;
        }

        // 2. 准备文件名和相对路径
        String fileName = cardName + "(" + categoryName + ").webp";
        // 相对路径：Pictures/应用名/folderName/
        String relativePath = Environment.DIRECTORY_PICTURES + File.separator
                + context.getResources().getString(R.string.app_name) + File.separator
                + folderName + File.separator;

        // 3. 使用 MediaStore 插入文件记录
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/webp");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            DialogBuilderManager.showDialog(context,
                    "出现问题",
                    "无法创建文件，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                    true,
                    "好的");
            return;
        }

        // 4. 写入 Bitmap 到输出流
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                DialogBuilderManager.showDialog(context,
                        "出现问题",
                        "无法打开输出流，请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                        true,
                        "好的");
                return;
            }

            // 使用无损压缩的WEBP_LOSSLESS格式
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream);
            outputStream.flush();

            DialogBuilderManager.showDialog(context,
                    "导出成功🎉",
                    "图片已保存到：Pictures/HyperFVM/" + folderName + "/" + fileName,
                    true,
                    "好耶");
        } catch (IOException e) {
            Log.e("export", "导出异常", e);

            DialogBuilderManager.showDialog(context,
                    "导出失败❌",
                    "异常信息：" + e.getMessage() + "\n\n请将此界面截图并向开发者反馈。感谢您的支持与配合\uD83E\uDEF0",
                    false,
                    "好的");
        }
    }

    public static void exportCardImage(Context context, ImageView imageView, String folderName, String cardName, String categoryName) {
        exportCardImageToPictures(context, imageView, folderName, cardName, categoryName);
    }
}
