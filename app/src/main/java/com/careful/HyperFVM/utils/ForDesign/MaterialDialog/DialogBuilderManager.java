package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageTiramisuActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImagesIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.DetailCardData.ExportInfo;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.ForCardSearch.CardSearchSuggestion;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.utils.OtherUtils.ImageExportUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * 弹窗管理类
 * 将散落在各个地方的MaterialAlertDialogBuilder集中到这里，方便管理
 */
public class DialogBuilderManager {
    /**
     * 一般弹窗展示方法，仅展示内容和一个按钮，不做任何额外的操作。
     * @param context 上下文
     * @param title 弹窗标题
     * @param content 弹窗内容
     * @param cancelable 弹窗是否可以通过点击背景关闭
     * @param positiveButtonTitle 弹窗按钮标题，比如【确定】
     */
    public static void showDialog(Context context, String title, String content, boolean cancelable, String positiveButtonTitle) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle(title)
                .setMessage(content)
                .setCancelable(cancelable)
                .setPositiveButton(positiveButtonTitle, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 一般弹窗展示方法，仅展示内容和一个按钮，调用的时候可通过回调执行点击事件
     * @param context 上下文
     * @param title 弹窗标题
     * @param content 弹窗内容
     * @param cancelable 弹窗是否可以通过点击背景关闭
     * @param positiveButtonTitle 弹窗按钮标题，比如【确定】
     * @param callBack 回调事件，点击按钮后执行
     */
    public static void showDialogWithCallBack(Context context, String title, String content, boolean cancelable, String positiveButtonTitle, PositiveButtonClickCallBack callBack) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle(title)
                .setMessage(content)
                .setCancelable(cancelable)
                .setPositiveButton(positiveButtonTitle, (dialogInterface, which) -> callBack.onResult())
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 签名校验弹窗
     */
    public static void showSignatureCheckerDialog(Context context) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("签名校验失败")
                .setMessage("同学，您使用的HyperFVM非官方版本，应用将关闭。\n请从以下官方渠道下载安装，非常感谢~\n\n" +
                        "Github【HyperFVM-Java】：" + context.getResources().getString(R.string.label_about_app_github_url) + "\n" +
                        "腾讯频道【HyperFVM交流社区】：" + context.getResources().getString(R.string.label_about_app_tencent_channel_url))
                .setCancelable(false)
                .setPositiveButton("确定", (dialogInterface, which) -> {
                    // 退出应用
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                })
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 第一次使用App时的弹窗
     */
    public static void showWelcomeDialog(Context context) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("欢迎使用 HyperFVM")
                .setMessage("如果您是第一次使用，建议您先阅读使用说明，以快速了解本App。")
                .setPositiveButton("去阅读👉", (dialogInterface, which) -> {
                    Intent intent = new Intent(context, UsingInstructionActivity.class);
                    context.startActivity(intent);
                })
                .setNegativeButton("我是老手\uD83D\uDE0E", null)
                .setCancelable(false)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗
     * @param title         弹窗标题
     * @param emoji         弹窗中的大表情
     * @param contentStatus 状态内容
     * @param contentDetail 详细内容
     */
    public static void showDashboardDetailDialog(Context context, String title, String emoji, String contentStatus, String contentDetail) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonAction = dialogView.findViewById(R.id.button_action);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本
        buttonAction.setText("关闭窗口");

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonAction.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗，并可以跳转米鼠的图
     * 通用版
     * @param title                 弹窗标题
     * @param emoji                 弹窗中的大表情
     * @param contentStatus         状态内容
     * @param contentDetail         详细内容
     * @param positiveButtonTitle   按钮的内容
     * @param imageName             要查看的图片文件名，若为空，则只跳转到米鼠的图
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImage(Context context, String title, String emoji, String contentStatus, String contentDetail, String positiveButtonTitle, String imageName) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonAction = dialogView.findViewById(R.id.button_action);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本
        buttonAction.setText(positiveButtonTitle);

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
            // 对于某些有多张图片的活动（如大赛、消费），只能跳转到米鼠的图，自行选择要查看哪一张图片
            // 还需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            if (imageName.isEmpty()) {
                context.startActivity(new Intent(context, DataImageTiramisuActivity.class));
                return;
            }

            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, imageName + ".png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗，并可以跳转米鼠的图
     * 仅适用于美食大赛
     * @param title                 弹窗标题
     * @param emoji                 弹窗中的大表情
     * @param contentStatus         状态内容
     * @param contentDetail         详细内容
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImageFoodContest(Context context, String title, String emoji, String contentStatus, String contentDetail) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu_food_contest, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonWeek1 = dialogView.findViewById(R.id.button_week1);
        Button buttonWeek2 = dialogView.findViewById(R.id.button_week2);
        Button buttonWeek3 = dialogView.findViewById(R.id.button_week3);
        Button buttonWeek4 = dialogView.findViewById(R.id.button_week4);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonWeek1.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_2_3_1.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonWeek2.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_2_3_2.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonWeek3.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_2_3_3.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonWeek4.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_2_3_4.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗，并可以跳转米鼠的图
     * 仅适用于百万消费
     * @param title                 弹窗标题
     * @param emoji                 弹窗中的大表情
     * @param contentStatus         状态内容
     * @param contentDetail         详细内容
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImageMillionConsumption(Context context, String title, String emoji, String contentStatus, String contentDetail) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu_million_consumption, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonConsumption1 = dialogView.findViewById(R.id.button_consumption1);
        Button buttonConsumption2 = dialogView.findViewById(R.id.button_consumption2);
        Button buttonConsumption3 = dialogView.findViewById(R.id.button_consumption3);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonConsumption1.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_1_3_1.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonConsumption2.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_1_3_2.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonConsumption3.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode(context);
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            // 到这里就说明本地确实有图片了
            File dir = new File(context.getFilesDir(), "data_images");
            File imageFile = new File(dir, "tiramisu_image_1_3_3.png");

            if (!imageFile.exists()) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_file_not_found_dialog_content), true, "好的");
                return;
            }

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                DialogBuilderManager.showDialog(context, context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_title), context.getResources().getString(R.string.text_data_images_index_open_failed_app_not_found_dialog_content), true, "好的");
            }
        });

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示二转打折详细信息的弹窗，并可以直接跳转对应的卡片详情页
     * @param title         弹窗标题
     * @param emoji         弹窗中的大表情
     * @param contentStatus 状态内容
     * @param contentDetail 详细内容
     * @param discountList  打折名单
     */
    @SuppressLint({"Range", "DiscouragedApi", "SetTextI18n"})
    public static void showDashboardTransferDiscountDialog(Context context, String title, String emoji, String contentStatus, String contentDetail, List<String> discountList) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_transfer_discount, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        // 开始逐个匹配卡片名称，查询防御卡数据库展示卡片信息，点击可跳转数据详情页
        try (DBHelper dbHelper = new DBHelper(context)) {
            LinearLayout suggestion_list_transfer_discount = dialogView.findViewById(R.id.suggestion_list_transfer_discount);
            for (int i = 0; i < discountList.size(); i++) {
                CardView cardView = (CardView) layoutInflater.inflate(R.layout.item_suggestion_transfer_discount, suggestion_list_transfer_discount, false);
                // 绑定好需要用到的组件
                LinearLayout suggestion_card_transfer_discount_container = cardView.findViewById(R.id.suggestion_card_transfer_discount_container);
                TextView suggestion_name_1_transfer_discount = cardView.findViewById(R.id.suggestion_name_1_transfer_discount);
                TextView suggestion_name_2_transfer_discount = cardView.findViewById(R.id.suggestion_name_2_transfer_discount);
                ImageView suggestion_image_0_transfer_discount = cardView.findViewById(R.id.suggestion_image_0_transfer_discount);
                ImageView suggestion_image_1_transfer_discount = cardView.findViewById(R.id.suggestion_image_1_transfer_discount);
                ImageView suggestion_image_2_transfer_discount = cardView.findViewById(R.id.suggestion_image_2_transfer_discount);
                ImageView suggestion_image_3_transfer_discount = cardView.findViewById(R.id.suggestion_image_3_transfer_discount);

                // 先通过名字得到tableName和不转名称
                String tableName = dbHelper.getCardTable(discountList.get(i));
                String baseName = dbHelper.getCardBaseName(discountList.get(i));

                if (tableName == null) {
                    continue;
                }

                // 通过数据库得到卡片名称、图片id
                try (Cursor cursor = dbHelper.getCardData(tableName, baseName)) {
                    if (cursor == null || !cursor.moveToFirst()) {
                        continue;
                    }

                    String imageIdStr0 = cursor.getString(cursor.getColumnIndex("image_id_0"));
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    int imageResId = context.getResources().getIdentifier(
                            imageIdStr0,
                            "drawable",
                            context.getPackageName()
                    );
                    suggestion_image_0_transfer_discount.setImageResource(imageResId);
                    String imageIdStr1 = cursor.getString(cursor.getColumnIndex("image_id_1"));
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = context.getResources().getIdentifier(
                            imageIdStr1.equals("无") ? "card_data_x" : imageIdStr1,
                            "drawable",
                            context.getPackageName()
                    );
                    suggestion_image_1_transfer_discount.setImageResource(imageResId);
                    String imageIdStr2 = cursor.getString(cursor.getColumnIndex("image_id_2"));
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    imageResId = context.getResources().getIdentifier(
                            imageIdStr2.equals("无") ? "card_data_x" : imageIdStr2,
                            "drawable",
                            context.getPackageName()
                    );
                    suggestion_image_2_transfer_discount.setImageResource(imageResId);
                    if (tableName.equals("card_data_3")) {
                        String imageIdStr3 = cursor.getString(cursor.getColumnIndex("image_id_3"));
                        // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                        imageResId = context.getResources().getIdentifier(
                                imageIdStr3.equals("无") ? "card_data_x" : imageIdStr3,
                                "drawable",
                                context.getPackageName()
                        );
                        suggestion_image_3_transfer_discount.setImageResource(imageResId);
                    } else {
                        suggestion_image_3_transfer_discount.setVisibility(View.GONE);
                    }

                    suggestion_name_1_transfer_discount.setText(cursor.getString(cursor.getColumnIndex("name")));
                    if (tableName.equals("card_data_3")) {
                        suggestion_name_2_transfer_discount.setText(cursor.getString(cursor.getColumnIndex("name_1")) + "-" + cursor.getString(cursor.getColumnIndex("name_2")) + "-" + cursor.getString(cursor.getColumnIndex("name_3")));
                    } else {
                        suggestion_name_2_transfer_discount.setText(cursor.getString(cursor.getColumnIndex("name_1")) + "-" + cursor.getString(cursor.getColumnIndex("name_2")));
                    }

                    // 设置点击事件，跳转数据详情页
                    suggestion_card_transfer_discount_container.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, baseName));

                    suggestion_list_transfer_discount.addView(cardView);
                }
            }
        }

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 显示卡片查询弹窗
     */
    @SuppressLint("InflateParams")
    public static void showCardQueryDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_input_card_data, null);
        TextInputEditText cardName = dialogView.findViewById(R.id.textInputEditText);
        TextView content_tips2 = dialogView.findViewById(R.id.content_tips2);
        LinearLayout suggestion_list = dialogView.findViewById(R.id.suggestion_list);
        Button buttonClose = dialogView.findViewById(R.id.button_close);

        try (DBHelper dbHelper = new DBHelper(context)) {
            // 实时模糊查询（修改核心：适配新的数据模型）
            cardName.addTextChangedListener(new TextWatcher() {
                @Override
                @SuppressLint("DiscouragedApi")
                public void afterTextChanged(Editable s) {
                    String keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        // 从数据库获取：包含name和image_id的搜索结果
                        List<CardSearchSuggestion> suggestions = dbHelper.searchCards(keyword);

                        suggestion_list.removeAllViews();
                        for (int i = 0; i < suggestions.size(); i++) {
                            CardView cardView = (CardView) layoutInflater.inflate(R.layout.item_suggestion_search, suggestion_list, false);
                            // 绑定好需要用到的组件
                            LinearLayout suggestion_card_search_container = cardView.findViewById(R.id.suggestion_card_search_container);
                            TextView suggestion_name = cardView.findViewById(R.id.suggestion_name);
                            TextView suggestion_transfer_category = cardView.findViewById(R.id.suggestion_transfer_category);
                            ImageView suggestion_image = cardView.findViewById(R.id.suggestion_image);

                            suggestion_name.setText(suggestions.get(i).getName());
                            suggestion_transfer_category.setText(suggestions.get(i).getTransferCategory());

                            String imageIdStr = suggestions.get(i).getImageId();
                            // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                            int imageResId = context.getResources().getIdentifier(
                                    imageIdStr,
                                    "drawable",
                                    context.getPackageName()
                            );
                            suggestion_image.setImageResource(imageResId);

                            String baseName = dbHelper.getCardBaseName(suggestions.get(i).getName());
                            suggestion_card_search_container.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, baseName));

                            suggestion_list.addView(cardView);
                        }

                        if (!suggestions.isEmpty()) {
                            suggestion_list.setVisibility(View.VISIBLE);
                            content_tips2.setVisibility(View.VISIBLE);
                        } else {
                            suggestion_list.setVisibility(View.GONE);
                            content_tips2.setVisibility(View.GONE);
                        }
                    } else {
                        suggestion_list.setVisibility(View.GONE);
                        content_tips2.setVisibility(View.GONE);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }

        // 显示弹窗（保持原有逻辑）
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 美食数据站：展示二次确认跳转弹窗
     * @param title 要前往的网站名字
     * @param url   网址链接
     */
    public static void showDialogAndVisitUrl(Context context, String title, String url) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("二次确认防误触")
                .setMessage("即将前往：\n" + title) // 显示要前往哪个网站
                .setPositiveButton("立即跳转\uD83E\uDD13", (dialogInterface, which) -> {
                    // 确认后执行跳转
                    //创建打开浏览器的Intent
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    //启动浏览器（添加try-catch处理没有浏览器的异常）
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("咱手滑了\uD83E\uDEE3", null)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 查黑系统：显示查询弹窗
     */
    public static void showQQInputDialog(Context context) {
        // 加载自定义布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_icu, null);
        // 获取布局中的输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText etQQ = (TextInputEditText) inputLayout.getEditText();

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("查黑系统")
                .setView(dialogView)
                .setPositiveButton("确定", (dialogInterface, which) -> {
                    if (etQQ != null) {
                        String qqNumber = Objects.requireNonNull(etQQ.getText()).toString().trim();
                        if (qqNumber.isEmpty()) {
                            Toast.makeText(context, "请输入QQ号", Toast.LENGTH_SHORT).show();
                        } else if (!qqNumber.matches("\\d+")) {
                            Toast.makeText(context, "QQ号只能包含数字", Toast.LENGTH_SHORT).show();
                        } else {
                            // 使用Icu类查询
                            IcuHelper icuHelper = new IcuHelper(context);
                            icuHelper.queryFraudInfo(qqNumber, new IcuHelper.QueryCallback() {
                                @Override
                                public void onSuccess(IcuHelper.FraudResult result) {
                                    showResultDialog(context, result);
                                }

                                @Override
                                public void onError(String message) {
                                    showDialog(context, "查询失败❌", message, true, "好的");
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 查黑系统：显示查询结果弹窗
     *
     * @param result 把查询到的结果显示到弹窗上
     */
    @SuppressLint({"InflateParams", "SetTextI18n"})
    private static void showResultDialog(Context context, IcuHelper.FraudResult result) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_icu, null);

        TextView emoji = dialogView.findViewById(R.id.emoji);
        TextView content_status = dialogView.findViewById(R.id.content_status);
        TextView content_fraud_info = dialogView.findViewById(R.id.content_fraud_info);
        TextView content_victim_info = dialogView.findViewById(R.id.content_victim_info);

        if (result.isFraud) {
            emoji.setText("🚨");
            content_status.setText("是骗子，快跑");
            content_fraud_info.setText("骗子信息👇\n" +
                    "QQ号：" + result.qq + "\n" +
                    "录入时间：" + result.recordTime + "\n" +
                    "上一次行骗时间：" + result.lastFraudTime + "\n" +
                    "行骗次数：" + result.fraudCount + "次\n" +
                    "行骗总金额：" + result.fraudAmount + "元\n" +
                    "不确定金额的行骗次数：" + result.uncertainAmountCount + "次"
            );

            List<IcuHelper.VictimInfo> victims = result.victims;
            if (victims.isEmpty()) {
                content_victim_info.setVisibility(View.GONE);
            } else {
                StringBuilder victimInfo = new StringBuilder("受害者信息");
                for (int i = 0; i < victims.size(); i++) {
                    victimInfo.append("\n")
                            .append("受害人").append(i + 1).append("👇").append("\n")
                            .append("QQ号：").append(victims.get(i).victim).append("\n")
                            .append("所在平台：").append(victims.get(i).platform).append("\n")
                            .append("所在区服：").append(victims.get(i).server).append("\n")
                            .append("被骗日期：").append(victims.get(i).fraudTime).append("\n");
                    if (victims.get(i).amountStatus == 1) {
                        victimInfo.append("被骗金额：").append(victims.get(i).amount).append("元\n");
                    }
                    victimInfo.append("备注：").append(victims.get(i).remark).append("\n");
                }
                content_victim_info.setText(victimInfo.toString());
            }
        } else {
            emoji.setText("✅");
            content_status.setText("暂未被标记为骗子");
            content_fraud_info.setVisibility(View.GONE);
            content_victim_info.setVisibility(View.GONE);
        }

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("查询结果")
                .setView(dialogView)
                .setPositiveButton("好的", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 设置：通知权限申请
     */
    public static void showNotificationPermissionRequestDialog(Context context) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("权限申请")
                .setMessage("为了向通知中心推送消息，需要您授予通知权限哦~")
                .setCancelable(false)
                .setPositiveButton("去开启", (dialogInterface, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                    context.startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 通用的列表弹窗的构建方法
     */
    public static void showSelectionDialog(Context context, int arrayId, String currentContent, String dialogTitle, String dbHelperUpdateContent, TextView currentSelection,
                                           SettingsSelectionDialogCallBack callBack) {
        ListView listView;
        Dialog dialog;
        try (DBHelper dbHelper = new DBHelper(context)) {
            String[] entries = context.getResources().getStringArray(arrayId);
            int selectedIndex = 0;
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(currentContent)) {
                    selectedIndex = i;
                    break;
                }
            }

            // 加载自定义布局
            View dialogView = LayoutInflater.from(context).inflate(R.layout.item_dialog_selection, null);
            listView = dialogView.findViewById(R.id.dialog_list);
            if (entries.length <= 10) {
                dialogView.findViewById(R.id.dialog_list_top_gradient).setVisibility(View.GONE);
                dialogView.findViewById(R.id.dialog_list_bottom_gradient).setVisibility(View.GONE);
            }

            // 设置列表
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_index_selection_single_choice, entries);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(selectedIndex, true);

            // 构建Dialog
            dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                    .setTitle(dialogTitle)
                    .setView(dialogView)
                    .setNegativeButton("关闭", null)
                    .create();

            // 添加背景模糊
            DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);

            // 列表点击事件
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedEntries = entries[position];
                dbHelper.updateSettingValue(dbHelperUpdateContent, selectedEntries);
                currentSelection.setText(selectedEntries);
                // 使用回调，将selectedEntries传回SettingsActivity
                callBack.onResult(selectedEntries);
                dialog.dismiss();
                Toast.makeText(context, "重启App后生效哦\uD83E\uDEF0", Toast.LENGTH_SHORT).show();
            });
        }

        listView.setTag(dialog); // 传递Dialog引用
        dialog.show();
    }

    /**
     * 安装权限申请
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static void showPackageInstallPermissionDialog(Context context) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("需要安装权限")
                .setMessage("应用需要\"安装未知应用\"权限才能安装更新。\n\n请点击\"去设置\"按钮，然后在设置中找到\"安装未知应用\"或\"特殊应用权限\"，为HyperFVM开启安装权限。")
                .setPositiveButton("去设置", (dialogInterface, which) -> {
                    // 跳转到安装未知应用权限设置页面
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

                    // 需要指定包名
                    intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));

                    // 检查是否有可以处理此Intent的应用
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        // 如果无法跳转到精确设置页面，跳转到应用详情页
                        Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        appDetailsIntent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(appDetailsIntent);
                    }
                })
                .setNegativeButton("取消", (dialogInterface, which) -> dialogInterface.dismiss())
                .setCancelable(false)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 批量导出图片的弹窗
     */
    public static void showExportAllImagesDialog(Context context, String folderName, List<ExportInfo> exportInfoList) {
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("导出所有图片")
                .setMessage("图片将保存到：Pictures/" + context.getResources().getString(R.string.app_name) +
                        "/" + folderName)
                .setPositiveButton("确定", (dialogInterface, which) -> ImageExportUtil.exportAllImages(context, folderName, exportInfoList))
                .setNegativeButton("咱手滑了\uD83E\uDEE3", (dialogInterface, which) -> dialogInterface.dismiss())
                .setCancelable(true)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }
}
