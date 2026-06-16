package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
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
import com.careful.HyperFVM.Activities.DataCenter.IcuFraudActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.ForCardSearch.CardSearchSuggestion;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
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
     * @param emoji 状态表情
     * @param content 弹窗内容
     * @param cancelable 弹窗是否可以通过点击背景关闭
     * @param positiveButtonTitle 弹窗按钮标题，比如【确定】
     */
    public static void showDialog(Context context, String title, String emoji, String content, boolean cancelable, String positiveButtonTitle) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_general, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentTextView = dialogView.findViewById(R.id.content);
        Button buttonAction = dialogView.findViewById(R.id.button_action);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentTextView.setText(content); // 设置内容文本
        buttonAction.setText(positiveButtonTitle);

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .setCancelable(cancelable)
                .create();

        buttonAction.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 一般弹窗展示方法，仅展示内容和一个按钮，调用的时候可通过回调执行点击事件
     * @param context 上下文
     * @param title 弹窗标题
     * @param emoji 用表情表示状态
     * @param content 弹窗内容
     * @param cancelable 弹窗是否可以通过点击背景关闭
     * @param positiveButtonTitle 弹窗按钮标题，比如【确定】
     * @param negativeButtonTitle 弹窗按钮标题，比如【关闭窗口】
     * @param callBack 回调事件，点击按钮后执行
     */
    public static void showDialogWithCallBack(
            Context context, String title, String emoji, String content, boolean cancelable,
            String negativeButtonTitle, String positiveButtonTitle, PositiveButtonClickCallBack callBack
    ) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_general_call_back, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentTextView = dialogView.findViewById(R.id.content);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonAction = dialogView.findViewById(R.id.button_action);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentTextView.setText(content); // 设置内容文本
        buttonClose.setText(negativeButtonTitle);
        buttonAction.setText(positiveButtonTitle);

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .setCancelable(cancelable)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
            callBack.onResult();
            dialog.dismiss();
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 签名校验弹窗
     */
    @SuppressLint("InflateParams")
    public static void showSignatureCheckerDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_signature_check, null);

        Button buttonAction1 = dialogView.findViewById(R.id.button_action1);
        Button buttonAction2 = dialogView.findViewById(R.id.button_action2);
        Button buttonAction3 = dialogView.findViewById(R.id.button_action3);

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        buttonAction1.setOnClickListener(v -> {
            //创建打开浏览器的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(context.getResources().getString(R.string.dialog_url_tencent_channel)));

            //启动浏览器（添加try-catch处理没有浏览器的异常）
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show();
            }
        });

        buttonAction2.setOnClickListener(v -> {
            //创建打开浏览器的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(context.getResources().getString(R.string.dialog_url_github)));

            //启动浏览器（添加try-catch处理没有浏览器的异常）
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show();
            }
        });

        buttonAction3.setOnClickListener(v -> {
            // 退出App
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 第一次使用App时的弹窗
     */
    public static void showWelcomeDialog(Context context) {
        showDialogWithCallBack(
                context, "欢迎使用\nHyperFVM", "🎉",
                "这是一款专为《美食大战老鼠》游戏制作的工具箱。如果您是第一次使用，强烈建议您先阅读使用说明，以便快速了解App。\n\nHyperFVM是免费软件，如果您是花钱买来的，请立即联系卖家退款。",
                false, "我是老手", "去阅读", () -> {
                    Intent intent = new Intent(context, UsingInstructionActivity.class);
                    context.startActivity(intent);
                }
        );
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(imageUri, "image/*");

            // 授予临时读取权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
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
     * 跳转给定Url的确认弹窗
     * @param image 要展示的图片资源id，字符串形式
     * @param imageRadius 图片裁剪的圆角
     * @param title 要前往的网站名字
     * @param subTitle 网站具体的内容
     * @param url 访问链接
     */
    @SuppressLint("InflateParams,DiscouragedApi")
    public static void showDialogAndVisitUrl(Context context, Drawable image, int imageRadius, String title, String subTitle, String url) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_visit_url, null);

        ImageView visit_image = dialogView.findViewById(R.id.visit_image);
        TextView visit_title = dialogView.findViewById(R.id.visit_title);
        TextView visit_sub_title = dialogView.findViewById(R.id.visit_sub_title);
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonAction = dialogView.findViewById(R.id.button_action);

        visit_image.setImageDrawable(image);
        visit_image.setClipToOutline(true);
        visit_image.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float radius = DensityUtil.dpToPx(context, imageRadius);
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });

        visit_title.setText(title);
        if (subTitle.isEmpty()) {
            visit_sub_title.setVisibility(View.GONE);
        } else {
            visit_sub_title.setText(subTitle);
        }

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
            //创建打开浏览器的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            //启动浏览器（添加try-catch处理没有浏览器的异常）
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show();
            }
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 查黑系统：显示查询弹窗
     */
    public static void showIcuQQInputDialog(Context context) {
        // 加载自定义布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_icu, null);
        // 获取布局中的输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText etQQ = (TextInputEditText) inputLayout.getEditText();
        Button buttonClose = dialogView.findViewById(R.id.button_close);
        Button buttonAction = dialogView.findViewById(R.id.button_action);

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
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
                            showIcuResultDialog(context, result);
                        }

                        @Override
                        public void onError(String message) {
                            showDialog(
                                    context,
                                    "查询失败",
                                    "❌",
                                    message,
                                    true,
                                    "好的"
                            );
                        }
                    });
                }
            }
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 查黑系统：显示查询结果弹窗
     * @param result 把查询到的结果显示到弹窗上
     */
    @SuppressLint({"InflateParams", "SetTextI18n"})
    private static void showIcuResultDialog(Context context, IcuHelper.FraudResult result) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        Button buttonAction = dialogView.findViewById(R.id.button_action);

        if (result.isFraud) {
            Intent intent = new Intent(context, IcuFraudActivity.class);
            intent.putExtra("FraudResult", result);
            context.startActivity(intent);
        } else {
            titleTextView.setText("好消息"); // 设置标题
            emojiTextView.setText("✅"); // 设置表情符号
            contentStatusTextView.setText("暂未被标记为骗子"); // 设置状态文本
            contentDetailTextView.setVisibility(View.GONE);
            buttonAction.setText("关闭窗口");

            Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                    .setView(dialogView)
                    .create();

            buttonAction.setOnClickListener(v -> dialog.dismiss());

            // 添加背景模糊
            DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
            dialog.show();
        }
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
        showDialogWithCallBack(
                context, "权限申请", "🛠️",
                "系统规定，必须要授予App\"安装未知应用\"权限，才能在App内拉起软件安装程序进行安装。\n\nHyperFVM仅会在应用内升级时使用此权限，且必须经过您手动点击安装按钮才会执行，不会私自发起安装，请您放心。",
                true, "关闭窗口", "去授权", () -> {
                    // 跳转到安装未知应用权限设置页面
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

                    // 需要指定包名
                    intent.setData(Uri.parse("package:" + context.getPackageName()));

                    // 检查是否有可以处理此Intent的应用
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        // 如果无法跳转到精确设置页面，跳转到应用详情页
                        Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        appDetailsIntent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(appDetailsIntent);
                    }
                }
        );
    }

}
