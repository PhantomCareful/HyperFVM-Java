package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageViewerOutUtil {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 传入应用包名（用于FileProvider授权）
    public static void setupImageViewer(Fragment fragment, ImageView imageView, String tempFileName, String authority) {
        imageView.setOnLongClickListener(v -> handleLongClick(fragment, v, tempFileName, authority));
    }

    private static boolean handleLongClick(Fragment fragment, View view, String tempFileName, String authority) {
        Context context = fragment.requireContext();
        ImageView imageView = (ImageView) view;
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if (drawable == null) {
            showToast(context, "图片资源不存在");
            return true; // 消费事件
        }
        Bitmap bitmap = drawable.getBitmap();
        executor.execute(() -> {
            File imageFile = saveBitmapToCache(context, bitmap, tempFileName);
            fragment.requireActivity().runOnUiThread(() -> {
                if (imageFile == null) {
                    showToast(context, "无法打开图片");
                    return;
                }
                // 通过FileProvider生成Uri
                Uri imageUri = FileProvider.getUriForFile(context, authority, imageFile);
                openImageWithChooser(context, imageUri);
            });
        });
        return true; // 关键：返回true表示事件已处理，避免传递给其他监听器
    }

    // 保存图片为File（而非直接返回Uri）
    @Nullable
    private static File saveBitmapToCache(Context context, Bitmap bitmap, String fileName) {
        try {
            File cacheDir = context.getExternalCacheDir() != null
                    ? context.getExternalCacheDir()
                    : context.getCacheDir();
            File imageFile = new File(cacheDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 打开图片选择器（确保权限正确）
    private static void openImageWithChooser(Context context, Uri imageUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        // 授予所有应用临时访问权限（关键）
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 强制显示选择器
        Intent chooser = Intent.createChooser(intent, "选择图片查看器");
        try {
            context.startActivity(chooser);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常日志，方便排查具体原因
            showToast(context, "没有可用的图片查看应用");
        }
    }

    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
