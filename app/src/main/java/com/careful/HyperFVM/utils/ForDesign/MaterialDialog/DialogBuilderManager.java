package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.Activities.DataCenter.TiramisuImageActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.OtherUtils.CardSuggestion;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.utils.OtherUtils.ImageExportUtil;
import com.careful.HyperFVM.utils.OtherUtils.SuggestionAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
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
     * @param detailContent 详细内容
     */
    public static void showDashboardDetailDialog(Context context, String title, String emoji, String detailContent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard, null);

        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentTextView = dialogView.findViewById(R.id.content);
        emojiTextView.setText(emoji); // 设置表情符号
        contentTextView.setText(detailContent); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("好的", null)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 仪表盘：展示详细信息的弹窗，并可以跳转米鼠的图
     * @param title         弹窗标题
     * @param emoji         弹窗中的大表情
     * @param detailContent 详细内容
     */
    public static void showDashboardDetailDialogAndJumpToTiramisuImage(Context context, String title, String emoji, String detailContent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_dashboard, null);

        TextView emojiTextView = dialogView.findViewById(R.id.emoji);
        TextView contentTextView = dialogView.findViewById(R.id.content);
        emojiTextView.setText(emoji); // 设置表情符号
        contentTextView.setText(detailContent); // 设置内容文本

        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("去查看米鼠的图", (dialogInterface, which) -> {
                    Intent intent = new Intent(context, TiramisuImageActivity.class);
                    context.startActivity(intent);
                })
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 显示卡片查询弹窗
     */
    @SuppressLint("InflateParams")
    public static void showCardQueryDialog(Context context) {
        View dialogView;
        TextInputEditText etCardName;
        try (DBHelper dbHelper = new DBHelper(context)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            dialogView = inflater.inflate(R.layout.item_dialog_input_card_data, null);
            etCardName = dialogView.findViewById(R.id.textInputEditText);
            RecyclerView suggestionList = dialogView.findViewById(R.id.suggestion_list);

            // 初始化适配器（传入上下文、空数据、点击监听）
            SuggestionAdapter adapter = new SuggestionAdapter(context, new ArrayList<>(), suggestion -> {
                // 点击项：填充名称到输入框，隐藏列表
                etCardName.setText(suggestion.getName());
                suggestionList.setVisibility(View.GONE);
            });

            // 配置RecyclerView（保持原有逻辑）
            suggestionList.setLayoutManager(new LinearLayoutManager(context));
            suggestionList.setAdapter(adapter);
            CardItemDecoration itemDecoration = new CardItemDecoration(suggestionList, 20, 20);
            suggestionList.addItemDecoration(itemDecoration);

            // 实时模糊查询（修改核心：适配新的数据模型）
            etCardName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        // 从数据库获取：包含name和image_id的搜索结果
                        List<CardSuggestion> suggestions = dbHelper.searchCards(keyword);
                        adapter.updateData(suggestions);
                        suggestionList.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateData(new ArrayList<>());
                        suggestionList.setVisibility(View.GONE);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }

        // 显示弹窗（保持原有逻辑）
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle(context.getResources().getString(R.string.card_data_search_title))
                .setView(dialogView)
                .setPositiveButton("查询", (dialogInterface, which) -> {
                    String cardName = Objects.requireNonNull(etCardName.getText()).toString().trim();
                    CardDataHelper.selectCardDataByName(context, cardName);
                })
                .setNegativeButton("取消", null)
                .create();

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
        View dialogView = inflater.inflate(R.layout.item_dialog_input_layout_icu, null);
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
    private static void showResultDialog(Context context, IcuHelper.FraudResult result) {
        StringBuilder content = new StringBuilder();
        content.append("QQ号：").append(result.qq).append("\n\n");
        content.append("昵称：").append(result.nickname).append("\n\n");
        if (result.isFraud) {
            content.append("备注：").append(result.remark).append("\n\n");
            content.append("录入时间：").append(result.recordTime);
        } else {
            content.append("该QQ号暂未被标记为骗子。");
        }

        showDialog(context,
                result.isFraud ? "查询结果(骗子\uD83D\uDEAB)" : "查询结果(正常✅)",
                content.toString(),
                true,
                "好的");
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
    public static void showSelectionDialog(Context context, int arrayId, String currentContent, String dialogTitle, String dbHelperUpdateContent, TextView currentSelection) {
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
                dialog.dismiss();
                Toast.makeText(context, "切换主题ing⏳⏳⏳", Toast.LENGTH_SHORT).show();

                // 重启App
                // 获取App的主Activity（通常是AndroidManifest中声明的LAUNCHER Activity）
                Intent intent = context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName());
                if (intent != null) {
                    // 清除之前的任务栈，避免重启后返回旧页面
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // 启动主Activity
                    context.startActivity(intent);
                    // 关闭当前所有Activity
                    if (context instanceof Activity) {
                        ((Activity) context).finishAffinity();
                    }
                }
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
     * 图片资源导出弹窗
     */
    public static void showImageExportDialog(Context context, ImageView imageView, String folderName, String cardName, String categoryName) {
        imageView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Dialog dialog = new MaterialAlertDialogBuilder(context, materialAlertDialogThemeStyleId)
                .setTitle("导出图片")
                .setMessage("图片将保存到：Pictures/" + context.getResources().getString(R.string.app_name) +
                        "/" + folderName +
                        "/" + cardName + "(" + categoryName + ")" + ".webp")
                .setPositiveButton("确定", (dialogInterface, which) -> ImageExportUtil.exportCardImage(context, imageView, folderName, cardName, categoryName))
                .setNegativeButton("咱手滑了\uD83E\uDEE3", (dialogInterface, which) -> dialogInterface.dismiss())
                .setCancelable(true)
                .create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }
}
