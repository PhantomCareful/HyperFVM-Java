package com.careful.HyperFVM.utils.ForDataImage;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;
import java.util.List;

public class DataImageViewerHelper {

    public static void openSystemPhotoViewerToSeeDataImages(Context context, String imageName) {
        File dir = new File(context.getFilesDir(), "data_images");
        File imageFile = new File(dir, imageName + ".png");

        if (!imageFile.exists()) {
            DialogBuilderManager.showDialog(
                    context,
                    context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title),
                    "❌",
                    context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content),
                    true,
                    "好的"
            );
            return;
        }

        try {
            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

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
            DialogBuilderManager.showDialog(
                    context,
                    context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title),
                    "❌",
                    context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content),
                    true,
                    "好的"
            );
        }
    }
}
