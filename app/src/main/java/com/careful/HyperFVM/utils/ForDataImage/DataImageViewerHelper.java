package com.careful.HyperFVM.utils.ForDataImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageViewerActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;

import java.io.File;

/**
 * 数据图查看入口：启动内置查看器（DataImageViewerActivity）直接打开应用私有目录中的数据图。
 * 内置查看器采用区域解码显示，效果与系统图库一致；全程不写入系统媒体库，
 * 相册（含回收站）零痕迹，也不需要任何存储授权。
 * 部分数据图有深浅色两个版本（_dark/_light 后缀，尺寸一致仅配色不同）：
 * 这里不把版本定死，只把基础文件名交给查看器，由查看器每次创建时按当前深浅色实时解析——
 * 查看中切换深浅色，图片可在查看器内直接换版且查看位置保持不变
 */
public class DataImageViewerHelper {

    /**
     * 查看数据图（需已下载图片资源）
     *
     * @param imageName 数据图的基础文件名（不含扩展名、不含 _dark/_light 后缀）
     */
    public static void openDataImage(Context context, String imageName) {
        File imageFile = resolveImageFile(context, imageName);

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
        // 只传基础文件名：版本选择由查看器在每次创建（含深浅色切换触发的重建）时实时解析
        intent.putExtra(DataImageViewerActivity.EXTRA_IMAGE_NAME, imageName);
        // 非 Activity 上下文启动 Activity 时必须使用新任务栈
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 解析数据图实际要显示的文件：有深浅色双版本的图按当前深浅色选 _dark/_light 后缀文件，
     * 对应版本不存在（普通单版本图或该版本未下载）则回退无后缀原图。
     * 查看器每次 onCreate 都会重新调用，深浅色切换重建后即可换版显示
     *
     * @param imageName 数据图的基础文件名（不含扩展名、不含 _dark/_light 后缀）
     * @return 当前主题下应显示的图片文件（可能不存在，调用方需自行判断）
     */
    public static File resolveImageFile(Context context, String imageName) {
        File dir = new File(context.getFilesDir(), "data_images");
        String suffix = DarkModeManager.isDarkMode(context) ? "_dark" : "_light";
        File themedFile = new File(dir, imageName + suffix + ".png");
        return themedFile.exists() ? themedFile : new File(dir, imageName + ".png");
    }
}
