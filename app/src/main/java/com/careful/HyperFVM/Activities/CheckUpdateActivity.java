package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.AppUpdaterUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.Objects;

public class CheckUpdateActivity extends BaseActivity {

    private DBHelper dbHelper;

    private AppUpdaterUtil appUpdaterUtil;

    private String versionInfo;
    private long localVersionCode;
    private long serverVersionCode = -1;
    private String serverVersionName;
    private String downloadUrl;
    private boolean isDownloading = false;

    private TextView version_info;
    private TextView app_log;
    private TextView float_button_update;

    private String newUpdateLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        // 加载布局文件
        setContentView(R.layout.activity_check_update);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        // 重置计数器

        // 初始化工具类
        dbHelper = new DBHelper(this);
        appUpdaterUtil = AppUpdaterUtil.getInstance();

        initViews();

        // 初始化各种装饰效果
        initDecoration();

        // 获取本地版本信息
        localVersionCode = LocalVersionUtil.getAppLocalVersionCode(this);
        String localAppVersionName = LocalVersionUtil.getAppLocalVersionName(this);
        versionInfo = localAppVersionName + "(" + localVersionCode + ")";

        generateAndSetVersionInfo(versionInfo);
        getAppServerVersionAndCheckAppUpdate();
    }

    /**
     * 初始化App更新要用到的视图
     */
    private void initViews() {
        version_info = findViewById(R.id.version_info);
        float_button_update = findViewById(R.id.FloatButton_Update);
        TextView float_button_join = findViewById(R.id.FloatButton_Join);
        app_log = findViewById(R.id.app_log);
        TextView app_update_note = findViewById(R.id.app_update_note);

        getContentFromAssets(this, app_log, "CurrentUpdateLog.txt");
        getContent(this, app_update_note, """
                ### 📣注意事项
                - App更新时不会删除任何用户数据，但建议您在更新前做好数据备份(如温馨礼包的领取链接)
                - 软件更新包会定期删除，不会持续占用您的存储空间
                - 在使用过程中遇到任何问题，可以加入聊天群组寻求帮助、进行问题反馈""");

        // 设置App更新按钮点击监听
        float_button_update.setOnClickListener(v -> {
            String buttonText = float_button_update.getText().toString();
            if (buttonText.equals(getResources().getString(R.string.label_check_update_status_need_update))) {
                // 发现新版本，展示新版本号和更新日志
                float_button_update.setText(getResources().getString(R.string.label_check_update_status_download_update));

                versionInfo = serverVersionName + "(" + serverVersionCode + ")";
                generateAndSetVersionInfo(versionInfo);

                newUpdateLog = newUpdateLog.split("\\)\n")[1];
                getContent(CheckUpdateActivity.this, app_log, newUpdateLog);
            } else if (buttonText.equals(getResources().getString(R.string.label_check_update_status_download_update))) {
                // 有新版本，检查是否存在已下载的APK
                checkExistingApkAndInstall();
            } else if (buttonText.equals(getResources().getString(R.string.label_check_update_status_downloaded_now))) {
                // 重新安装APK
                installApk();
            } else if (buttonText.equals(getResources().getString(R.string.label_check_update_status_need_install_permission))) {
                // 重新安装APK
                installApk();
            } else if (buttonText.equals(getResources().getString(R.string.label_check_update_status_downloaded_past))) {
                // 安装已下载的APK
                installApk();
            } else {
                // 检查更新
                getAppServerVersionAndCheckAppUpdate();
            }
        });

        // 加入群组按钮
        float_button_join.setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                ContextCompat.getDrawable(this, R.drawable.ic_qq),
                0,
                getResources().getString(R.string.dialog_title_tencent_group),
                getResources().getString(R.string.dialog_sub_title_tencent_group),
                getResources().getString(R.string.dialog_url_tencent_group)));
    }

    @SuppressLint("SetTextI18n")
    private void generateAndSetVersionInfo(String versionInfo) {
        // 判断是否为Beta版
        String betaOrRelease = Objects.equals(versionInfo.split("\\.")[2].split("\\(")[0], "0") ? " | Release" : " | Beta";

        // 拼接最终版本信息
        version_info.setText(versionInfo + betaOrRelease);
    }

    /**
     * 检查App版本更新
     */
    private void getAppServerVersionAndCheckAppUpdate() {
        float_button_update.setText(getResources().getString(R.string.label_check_update_status_checking));

        // 调用UpdaterUtil检查更新
        appUpdaterUtil.checkServerVersion(
                new AppUpdaterUtil.OnVersionCheckCallback() {
                    @Override
                    public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                        runOnUiThread(() -> {
                            serverVersionCode = serverVersion; // 保存服务器版本
                            try {
                                if (serverVersion > localVersionCode) {
                                    // 发现新版本，接收更新日志
                                    newUpdateLog = updateLog;
                                    // 解析新版本versionName
                                    serverVersionName = updateLog.split("### ")[0].split("## ")[1].split("\\(")[0];

                                    // 检查是否存在已下载的APK
                                    if (checkExistingApkFile()) {
                                        float_button_update.setText(getResources().getString(R.string.label_check_update_status_downloaded_past));
                                    } else {
                                        float_button_update.setText(getResources().getString(R.string.label_check_update_status_need_update));
                                    }
                                } else {
                                    // 已是最新版本
                                    float_button_update.setText(getResources().getString(R.string.label_check_update_status_current));
                                    downloadUrl = null;
                                }
                                // 标记完成并尝试执行动画
                            } catch (Exception e) {
                                float_button_update.setText("检查版本时发生错误");
                                downloadUrl = null;
                            }
                        });
                    }

                    @Override
                    public void onVersionCheckFailure(String errorMsg) {
                        runOnUiThread(() -> {
                            float_button_update.setText(errorMsg);
                            downloadUrl = null;
                        });
                    }

                    @Override
                    public void onVersionParseError() {
                        runOnUiThread(() -> {
                            float_button_update.setText("版本信息错误");
                            downloadUrl = null;
                        });
                    }
                }
        );
    }

    /**
     * 检查是否存在已下载的APK文件
     */
    private boolean checkExistingApkFile() {
        // 从数据库获取已保存的APK文件名和版本号
        String savedApkFileName = dbHelper.getDataStationValue("DownloadedApkFileName");
        long savedApkFileVersionCode = Long.parseLong(dbHelper.getDataStationValue("DownloadedApkFileVersionCode"));

        if (savedApkFileName != null && !savedApkFileName.isEmpty()) {
            // 构建完整的APK文件路径
            File apkDir = getExternalFilesDir("apk");
            if (apkDir != null && apkDir.exists()) {
                File apkFile = new File(apkDir, savedApkFileName);

                // 检查文件是否存在且是一个文件（不是目录），并且这个文件的版本号比当前App的版本号更高（必须是大于）
                if (apkFile.exists() && apkFile.isFile() && apkFile.length() > 0 && savedApkFileVersionCode > localVersionCode) {
                    downloadUrl = apkFile.getAbsolutePath();
                    return true;
                } else {
                    // 文件不存在或无效，清除数据库记录
                    dbHelper.updateDataStationValue("DownloadedApkFileName", "");
                }
            }
        }
        return false;
    }

    /**
     * 检查已下载的APK并安装
     */
    private void checkExistingApkAndInstall() {
        if (checkExistingApkFile()) {
            // 直接安装已存在的APK
            installApk();
        } else {
            // 没有已下载的APK，开始下载
            getAppDownloadUrl();
        }
    }

    /**
     * 获取App下载链接
     */
    private void getAppDownloadUrl() {
        float_button_update.setText("⏳获取下载链接中⏳");

        appUpdaterUtil.getDownloadUrl(new AppUpdaterUtil.OnDownloadUrlCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                CheckUpdateActivity.this.downloadUrl = downloadUrl;
                // 清理旧的APK文件
                cleanupOldApkFiles();
                // 开始下载
                startAppDownload();
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> {
                    float_button_update.setText("获取链接失败，点击重试");
                    Toast.makeText(CheckUpdateActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 清理旧的APK文件
     */
    private void cleanupOldApkFiles() {
        try {
            File apkDir = getExternalFilesDir("apk");
            if (apkDir != null && apkDir.exists() && apkDir.isDirectory()) {
                File[] apkFiles = apkDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".apk"));
                if (apkFiles != null) {
                    for (File apkFile : apkFiles) {
                        if (apkFile.exists()) {
                            boolean deleted = apkFile.delete();
                            if (!deleted) {
                                Log.w("ApkCleanup", "Failed to delete old APK: " + apkFile.getName());
                            }
                        }
                    }
                    Log.d("ApkCleanup", "Cleaned up " + apkFiles.length + " old APK files");
                }
            }
        } catch (Exception e) {
            Log.e("ApkCleanup", "Error cleaning up old APK files", e);
        }
    }

    /**
     * 开始下载App更新
     */
    private void startAppDownload() {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            Toast.makeText(this, "下载链接无效", Toast.LENGTH_SHORT).show();
            float_button_update.setText("下载链接无效，点击重试");
            return;
        }

        isDownloading = true;
        float_button_update.setEnabled(false);

        appUpdaterUtil.downloadApk(this, downloadUrl, new AppUpdaterUtil.DownloadCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadProgress(int progress) {
                runOnUiThread(() ->
                        float_button_update.setText("⏳下载中: " + progress + "%⏳")
                );
            }

            @Override
            public void onSuccess(String apkFilePath) {
                runOnUiThread(() -> {
                    isDownloading = false;
                    float_button_update.setText(getResources().getString(R.string.label_check_update_status_downloaded_now));
                    float_button_update.setEnabled(true);

                    // 保存APK文件路径
                    downloadUrl = apkFilePath;

                    // 保存APK文件名和APK的版本号到数据库
                    File apkFile = new File(apkFilePath);
                    if (apkFile.exists()) {
                        String apkFileName = apkFile.getName();
                        dbHelper.updateDataStationValue("DownloadedApkFileName", apkFileName);
                        dbHelper.updateDataStationValue("DownloadedApkFileVersionCode", String.valueOf(serverVersionCode));
                        Log.d("ApkDownload", "Saved APK filename to DB: " + apkFileName);
                        Log.d("ApkDownload", "Saved APK version code to DB: " + serverVersionCode);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> {
                    isDownloading = false;
                    float_button_update.setText("下载失败，点击重试");
                    float_button_update.setEnabled(true);
                });
            }
        });
    }

    /**
     * 安装APK文件
     */
    @SuppressLint("QueryPermissionsNeeded")
    private void installApk() {
        try {
            // 检查是否有安装未知应用的权限
            if (!getPackageManager().canRequestPackageInstalls()) {
                // 引导用户开启安装权限
                DialogBuilderManager.showPackageInstallPermissionDialog(this);
                return;
            }

            File apkFile = new File(downloadUrl);
            if (!apkFile.exists()) {
                Toast.makeText(this, "安装文件不存在", Toast.LENGTH_SHORT).show();
                float_button_update.setText("安装文件不存在，请重新下载");
                return;
            }

            // 使用FileProvider获取Uri - 修改authority
            Uri apkUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有可以处理此Intent的应用
            if (installIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(installIntent);
            } else {
                Toast.makeText(this, "没有找到可以安装应用的程序", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("install", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(this, "安装失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);
        });
        // 动态获取导航栏高度（小白条/三键导航）
        MaterialCardView floatButtonJoinContainer = findViewById(R.id.FloatButton_Join_Container);
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonJoinContainer.getLayoutParams();
            params.bottomMargin = DensityUtil.dpToPx(this, 24) + height;
            floatButtonJoinContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        MaterialCardView floatButtonUpdateContainer = findViewById(R.id.FloatButton_Update_Container);
        LinearLayout check_update_container = findViewById(R.id.check_update_container);
        InsetsUtil.setMarginHorizontal(this, check_update_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) check_update_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            check_update_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);
            params = (ViewGroup.MarginLayoutParams) floatButtonUpdateContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonUpdateContainer.setLayoutParams(params);
            params = (ViewGroup.MarginLayoutParams) floatButtonJoinContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonJoinContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonUpdate));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonJoin));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消下载
        if (isDownloading) {
            appUpdaterUtil.cancelDownload();
        }
    }
}