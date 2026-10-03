package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.careful.HyperFVM.Activities.DataCenter.DataImage.DataImageTiramisuActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImagesIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.IcuFraudActivity;
import com.careful.HyperFVM.Activities.Necessary.UsingInstruction.UsingInstructionActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDataImage.DataImageViewerHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.ForCardData.CardSearchSuggestion;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

/**
 * 弹窗管理类
 * 将散落在各个地方的MaterialAlertDialogBuilder集中到这里，方便管理
 */
public class DialogBuilderManager {
    @SuppressLint("StaticFieldLeak")
    private static final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

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
        TextView buttonAction = dialogView.findViewById(R.id.button_action);
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
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

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

        TextView buttonAction1 = dialogView.findViewById(R.id.button_action1);
        TextView buttonAction2 = dialogView.findViewById(R.id.button_action2);
        TextView buttonAction3 = dialogView.findViewById(R.id.button_action3);

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
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

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
     * @param imageName             要查看的图片文件名，若为空，则只跳转到米鼠的图
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImage(Context context, String title, String emoji, String contentStatus, String contentDetail, String imageName) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            if (imageName.isEmpty()) {
                context.startActivity(new Intent(context, DataImageTiramisuActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, imageName);
        });

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗，并可以跳转米鼠的图
     * 仅适用于营地任务
     * @param title                 弹窗标题
     * @param emoji                 弹窗中的大表情
     * @param contentStatus         状态内容
     * @param contentDetail         详细内容
     * @param urlTask               营地任务顺序表链接
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImageCampTask(Context context, String title, String emoji, String contentStatus, String contentDetail, String urlTask) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu_camp_task, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction1 = dialogView.findViewById(R.id.button_action_1);
        TextView buttonAction2 = dialogView.findViewById(R.id.button_action_2);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction1.setOnClickListener(v -> {
            // 还需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_2_5");
        });

        buttonAction2.setOnClickListener(v -> {
            // 创建打开浏览器的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(urlTask));

            // 启动浏览器（添加try-catch处理没有浏览器的异常）
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
     * 仪表盘：展示详细信息的弹窗，并可以直接跳转对应的卡片详情页
     * 仅适用于欢乐假期和三岛福利
     * @param title         弹窗标题
     * @param emoji         弹窗中的大表情
     * @param contentStatus 状态内容
     * @param contentDetail 详细内容
     * @param cardList      返场卡片名单
     * @param imageName     对应的米鼠的图的文件名
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImageHappyHolidayAndThreeIslands(Context context, String title, String emoji, String contentStatus, String contentDetail, List<String> cardList, String imageName) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_happy_holiday_three_islands, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction = dialogView.findViewById(R.id.button_action);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        // 开始逐个匹配卡片名称，直接复用目录页单卡布局展示卡片信息，点击可跳转数据详情页
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list_dashboard);
        for (String cardName : cardList) {
            CardDataHelper.addCardRowToDialog(context, layoutInflater, suggestion_card_list, cardName);
        }

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        buttonAction.setOnClickListener(v -> {
            // 还需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, imageName);
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
     * @param cardList              返场卡片名单
     */
    public static void showDashboardDetailDialogAndSeeTiramisuImageFoodContest(Context context, String title, String emoji, String contentStatus, String contentDetail, List<String> cardList) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_tiramisu_food_contest, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonWeek = dialogView.findViewById(R.id.button_week);
        TextView buttonReward = dialogView.findViewById(R.id.button_reward);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        // 开始逐个匹配卡片名称，直接复用目录页单卡布局展示卡片信息，点击可跳转数据详情页
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list_dashboard);
        for (String cardName : cardList) {
            CardDataHelper.addCardRowToDialog(context, layoutInflater, suggestion_card_list, cardName);
        }

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonWeek.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_2_3_1");
        });

        buttonReward.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_2_3");
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
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonConsumption1 = dialogView.findViewById(R.id.button_consumption_1);
        TextView buttonConsumption2 = dialogView.findViewById(R.id.button_consumption_2);
        TextView buttonConsumption3 = dialogView.findViewById(R.id.button_consumption_3);

        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonConsumption1.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_1_3_1");
        });

        buttonConsumption2.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_1_3_2");
        });

        buttonConsumption3.setOnClickListener(v -> {
            // 需要检查版本号，如果当前还没有下载图片或者图片已删除，则跳转目录界面
            long localVersionCode = LocalVersionUtil.getImageResourcesVersionCode();
            if (localVersionCode == 0 || localVersionCode == 1) {
                context.startActivity(new Intent(context, DataImagesIndexActivity.class));
                return;
            }

            DataImageViewerHelper.openDataImage(context, "tiramisu_image_1_3_3");
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
    public static void showDashboardTransferDiscountDialog(Context context, String title, String emoji, String contentStatus, String contentDetail, List<String> discountList) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_with_card_list, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);

        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本
        contentDetailTextView.setText(contentDetail); // 设置内容文本

        // 开始逐个匹配卡片名称，直接复用目录页单卡布局展示卡片信息，点击可跳转数据详情页
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list_dashboard);
        for (String cardName : discountList) {
            CardDataHelper.addCardRowToDialog(context, layoutInflater, suggestion_card_list, cardName);
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
     * 仪表盘：展示福利打卡详细信息的弹窗，并可以直接跳转新卡片详情页（要求数据库不低于指定版本）
     * @param title         弹窗标题
     * @param emoji         弹窗中的大表情
     * @param contentStatus 状态内容
     * @param contentDetail 详细内容
     * @param newCardName   打折名单
     * @param requiredDatabaseVersion 超过这个版本后，数据库中才能有这张新卡
     */
    @SuppressLint("SetTextI18n")
    public static void showDashboardBirthdayActivityDialog(Context context, String title, String emoji, String contentStatus, String contentDetail, String newCardName, int requiredDatabaseVersion) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard_with_card_list, null);

        TextView titleTextView = dialogView.findViewById(R.id.title);
        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentStatusTextView = dialogView.findViewById(R.id.content_status);
        TextView contentDetailTextView = dialogView.findViewById(R.id.content_detail);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);

        titleTextView.setText(title); // 设置标题
        emojiTextView.setText(emoji); // 设置表情符号
        contentStatusTextView.setText(contentStatus); // 设置状态文本

        // 开始逐个匹配卡片名称，直接复用目录页单卡布局展示卡片信息，点击可跳转数据详情页
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list_dashboard);
        if (Integer.parseInt(context.getString(R.string.version_card_data_search).split("：")[1]) >= requiredDatabaseVersion) {
            if (!CardDataHelper.addCardRowToDialog(context, layoutInflater, suggestion_card_list, newCardName)) {
                return;
            }
        } else {
            contentDetail = contentDetail + "\n\n" + newCardName + "\n\n当前数据库尚未包含此卡片\n请更新数据库版本到" + requiredDatabaseVersion;
        }

        contentDetailTextView.setText(contentDetail); // 设置内容文本

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
        TextView title = dialogView.findViewById(R.id.title);
        TextInputEditText cardName = dialogView.findViewById(R.id.textInputEditText);
        TextView content_tips = dialogView.findViewById(R.id.content_tips);
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);

        title.setText(context.getString(R.string.title_card_data_search));

        // 实时模糊查询（修改核心：适配新的数据模型）
        cardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // 从数据库获取匹配的形态（name + image_id）
                    List<CardSearchSuggestion> suggestions = dbHelper.searchCards(keyword);

                    suggestion_card_list.removeAllViews();
                    boolean hasResult = false;
                    for (int i = 0; i < suggestions.size(); i++) {
                        CardSearchSuggestion suggestion = suggestions.get(i);
                        // 每个匹配形态直接用自己的 image_id 复用已有单卡布局展示
                        hasResult |= CardDataHelper.addCardRowByImageId(context, layoutInflater, suggestion_card_list,
                                suggestion.getImageId(), v -> CardDataHelper.selectCardDataByName(context, suggestion.getName()));
                    }

                    if (hasResult) {
                        suggestion_card_list.setVisibility(View.VISIBLE);
                        content_tips.setVisibility(View.VISIBLE);
                    } else {
                        suggestion_card_list.setVisibility(View.GONE);
                        content_tips.setVisibility(View.GONE);
                    }
                } else {
                    suggestion_card_list.setVisibility(View.GONE);
                    content_tips.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

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
     * 显示分解兑换计算器的查询弹窗
     */
    @SuppressLint("InflateParams")
    public static void showDecomposeAndGetQueryDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_input_card_data, null);
        TextView title = dialogView.findViewById(R.id.title);
        TextInputEditText cardName = dialogView.findViewById(R.id.textInputEditText);
        TextView content_tips = dialogView.findViewById(R.id.content_tips);
        LinearLayout suggestion_card_list = dialogView.findViewById(R.id.suggestion_card_list);
        TextView buttonClose = dialogView.findViewById(R.id.button_close);

        title.setText(context.getString(R.string.title_decompose_and_get_calculator));

        // 实时模糊查询
        cardName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    // 从数据库获取匹配的形态（name + image_id）
                    List<CardSearchSuggestion> suggestions = dbHelper.searchAnimalAndGoldenCards(keyword);

                    suggestion_card_list.removeAllViews();
                    boolean hasResult = false;
                    for (int i = 0; i < suggestions.size(); i++) {
                        CardSearchSuggestion suggestion = suggestions.get(i);
                        // 每个匹配形态直接用自己的 image_id 复用已有单卡布局展示，点击时再查询分解兑换数据
                        hasResult |= CardDataHelper.addCardRowByImageId(context, layoutInflater, suggestion_card_list,
                                suggestion.getImageId(), v -> openDecomposeDetail(context, suggestion.getName()));
                    }

                    if (hasResult) {
                        suggestion_card_list.setVisibility(View.VISIBLE);
                        content_tips.setVisibility(View.VISIBLE);
                    } else {
                        suggestion_card_list.setVisibility(View.GONE);
                        content_tips.setVisibility(View.GONE);
                    }
                } else {
                    suggestion_card_list.setVisibility(View.GONE);
                    content_tips.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 显示弹窗（保持原有逻辑）
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /** 点击搜索结果后查询该卡的分解兑换数据并打开计算页（惰性查询，避免每次输入都重建数据数组） */
    @SuppressLint("Range")
    private static void openDecomposeDetail(Context context, String cardName) {
        String baseName = dbHelper.getCardBaseName(cardName);
        String tableName = dbHelper.getCardTableName(cardName);
        if (baseName == null || tableName == null) {
            return;
        }
        try (Cursor cursor = dbHelper.getCardData(tableName, baseName)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            String decomposeItemName = cursor.getString(cursor.getColumnIndex("decompose_item"));

            String[] imageIdsArray = tableName.equals("card_data_4") ? new String[] {
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")),
            } : new String[] {
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_1")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_2")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_3")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_card_4")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c")),
                    cursor.getString(cursor.getColumnIndex("decompose_image_id_compose")),
            };

            int[] decomposeDataArray = tableName.equals("card_data_4") ? new int[] {
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_1")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_2")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_3")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c")),
            } : new int[] {
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_1")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_2")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_3")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_card_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_card_4")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_1")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_2")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_3")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_skill_4")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_a")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_b")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_1_c")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_a")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_b")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_2_c")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_a")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_b")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_transfer_3_c")),
                    CardDataHelper.getStringFromCursor(cursor, "decompose_compose").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "decompose_compose")),
            };

            int[] getDataArray = tableName.equals("card_data_4") ? new int[] {
                    CardDataHelper.getStringFromCursor(cursor, "get_card_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_1")),
                    CardDataHelper.getStringFromCursor(cursor, "get_card_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_2")),
                    CardDataHelper.getStringFromCursor(cursor, "get_card_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_3")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_1")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_2")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_3")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_4")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c")),
            } : new int[] {
                    CardDataHelper.getStringFromCursor(cursor, "get_card_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_1")),
                    CardDataHelper.getStringFromCursor(cursor, "get_card_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_2")),
                    CardDataHelper.getStringFromCursor(cursor, "get_card_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_3")),
                    CardDataHelper.getStringFromCursor(cursor, "get_card_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_card_4")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_1").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_1")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_2").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_2")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_3").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_3")),
                    CardDataHelper.getStringFromCursor(cursor, "get_skill_4").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_skill_4")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_a")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_b")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_1_c")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_a")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_b")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_2_c")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_a")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_b")),
                    CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_transfer_3_c")),
                    CardDataHelper.getStringFromCursor(cursor, "get_compose").equals("🚫") ? 0 : Integer.parseInt(CardDataHelper.getStringFromCursor(cursor, "get_compose")),
            };

            CardDataHelper.selectDecomposeAndGetData(context, baseName, decomposeItemName, imageIdsArray, decomposeDataArray, getDataArray);
        }
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
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

        if (image != null) {
            visit_image.setImageDrawable(image);
            visit_image.setClipToOutline(true);
            visit_image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = DensityUtil.dpToPx(context, imageRadius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
        } else {
            visit_image.setVisibility(View.GONE);
        }

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
            // 创建打开浏览器的Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            // 启动浏览器（添加try-catch处理没有浏览器的异常）
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
        TextView buttonClose = dialogView.findViewById(R.id.button_close);
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

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

                        @Override
                        public void onCertificateError() {
                            showDialog(
                                    context,
                                    "查询失败",
                                    "🔒",
                                    "服务器安全证书校验未通过（可能已过期），请稍后再试。",
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
        TextView buttonAction = dialogView.findViewById(R.id.button_action);

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
