package com.careful.HyperFVM.utils.ForDataImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageViewerActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;

/**
 * 数据图查看入口：启动内置查看器（DataImageViewerActivity）直接打开应用私有目录中的数据图。
 * 内置查看器采用区域解码显示，效果与系统图库一致；全程不写入系统媒体库，
 * 相册（含回收站）零痕迹，也不需要任何存储授权。
 */
public class DataImageViewerHelper {

    /**
     * 查看数据图（需已下载图片资源）
     *
     * @param imageName 数据图的文件名（不含扩展名）
     */
    public static void openDataImage(Context context, String imageName) {
        File imageFile = new File(new File(context.getFilesDir(), "data_images"), imageName + ".png");

        // 图片文件不存在：提示可能的原因与解决方式
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

        Intent intent = new Intent(context, DataImageViewerActivity.class);
        intent.putExtra(DataImageViewerActivity.EXTRA_IMAGE_PATH, imageFile.getAbsolutePath());
        // 非 Activity 上下文启动 Activity 时必须使用新任务栈
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
