package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.ImageResourcesUpdaterUtil;
import com.careful.HyperFVM.utils.ForUpdate.AppUpdaterUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.io.File;
import java.util.Objects;

public class CheckUpdateActivity extends BaseActivity {

    private DBHelper dbHelper;

    private AppUpdaterUtil appUpdaterUtil;
    private ImageResourcesUpdaterUtil imageUtil;

    private long localAppVersionCode;
    private long serverAppVersionCode = -1;
    private String downloadAppUrl;
    private boolean isAppDownloading = false;

    private Button update_app_action;
    private CardView container_app_log_new;
    private TextView app_log_new;
    private TextView app_log_current;

    private long localImageResourcesVersionCode;
    private long serverImageResourcesVersionCode = -1;
    private String serverImageResourcesUpdateLog;
    private String downloadImageResourcesUrl;
    private boolean isImageResourcesDownloading = false;

    private Button update_image_resources_action;
    private CardView container_image_resources_log_new;
    private TextView image_resources_log_new;
    private TextView image_resources_log_current;

    private LinearLayout check_update_container;
    private TransitionSet transition;

    private int pressFeedbackAnimationDelay;

    private boolean hasImageResult = false; // 图片检查是否完成
    private boolean hasAppResult = false; // App检查是否完成

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
        hasImageResult = false;
        hasAppResult = false;

        // 初始化工具类
        dbHelper = new DBHelper(this);
        appUpdaterUtil = AppUpdaterUtil.getInstance();
        imageUtil = ImageResourcesUpdaterUtil.getInstance();

        // 初始化动画效果
        check_update_container = findViewById(R.id.check_update_container);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.addTransition(new Fade());
        transition.setDuration(400); // 动画时长400ms
        initViewsForImage();
        initViewsForApp();

        // 添加模糊材质
        setupBlurEffect();

        getImageLocalVersion();
        getAppLocalVersion();
        getImageServerVersionAndCheckImageUpdate();
        getAppServerVersionAndCheckAppUpdate();
    }

    // =========================== 以下是图片资源部分 ===========================

    /**
     * 初始化图片资源更新要用到的视图
     */
    private void initViewsForImage() {
        getContent(this, findViewById(R.id.title_image_resources_log), "# 图片资源");
        update_image_resources_action = findViewById(R.id.update_image_resources_action);
        container_image_resources_log_new = findViewById(R.id.container_image_resources_log_new);
        image_resources_log_new = findViewById(R.id.image_resources_log_new);
        image_resources_log_current = findViewById(R.id.image_resources_log_current);
        getContent(this, image_resources_log_current, dbHelper.getDataStationValue("CurrentUpdateLogImageResources"));

        // 设置图片资源更新按钮点击监听
        update_image_resources_action.setOnClickListener(v -> v.postDelayed(() -> {
            if (update_image_resources_action.getText().toString().equals(getResources().getString(R.string.label_check_update_status_new))) {
                // 有新版本，开始下载
                if (!isImageResourcesDownloading) {
                    startImageDownload();
                }
            } else {
                // 检查更新
                getImageServerVersionAndCheckImageUpdate();
            }
        }, pressFeedbackAnimationDelay));
    }

    /**
     * 获取图片资源本地版本号
     */
    private void getImageLocalVersion() {
        // 从数据库获取
        localImageResourcesVersionCode = LocalVersionUtil.getImageResourcesVersionCode(this);
    }

    /**
     * 检查云端仓库的图片资源版本号
     */
    private void getImageServerVersionAndCheckImageUpdate() {
        update_image_resources_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        imageUtil.checkServerVersion(new ImageResourcesUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                runOnUiThread(() -> {
                    serverImageResourcesVersionCode = serverVersion;
                    try {
                        long versionDiff = serverVersion - localImageResourcesVersionCode;

                        if (versionDiff > 0) {
                            // 有新版本，显示更新日志
                            serverImageResourcesUpdateLog = "## 当前：" + updateLog;
                            getContent(CheckUpdateActivity.this, image_resources_log_new, "## 最新：" + updateLog);
                            update_image_resources_action.setText(getResources().getString(R.string.label_check_update_status_new));
                        } else {
                            // 已是最新版本
                            update_image_resources_action.setText(getResources().getString(R.string.label_check_update_status_current));
                        }
                        // 标记完成并尝试执行动画
                        hasImageResult = true;
                        checkAndRunAnimation();
                    } catch (Exception e) {
                        hasImageResult = true; // 即使出错也标记完成
                        checkAndRunAnimation();
                        update_image_resources_action.setText("检查版本时发生错误");
                    }
                });
            }

            @Override
            public void onVersionCheckFailure(String errorMsg) {
                runOnUiThread(() -> {
                    hasImageResult = true; // 标记完成
                    checkAndRunAnimation();
                    update_image_resources_action.setText("检查版本失败，请稍后再试");
                });
            }

            @Override
            public void onVersionParseError() {
                runOnUiThread(() -> {
                    hasImageResult = true; // 标记完成
                    checkAndRunAnimation();
                    update_image_resources_action.setText("版本信息错误");
                });
            }
        });
    }

    /**
     * 下载图片资源的方法
     */
    private void startImageDownload() {
        update_image_resources_action.setText("⏳获取下载链接中⏳");

        isImageResourcesDownloading = true;
        update_image_resources_action.setEnabled(false);

        // 计算版本差，判断是全量更新还是增量更新
        long versionDiff = serverImageResourcesVersionCode - localImageResourcesVersionCode;
        boolean isFullDownload = (versionDiff > 1); // 差值大于1：全量更新，差值等于1：增量更新

        // 根据更新类型获取下载链接
        ImageResourcesUpdaterUtil.OnDownloadUrlCallback urlCallback = new ImageResourcesUpdaterUtil.OnDownloadUrlCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                downloadImageResourcesUrl = downloadUrl;
                Log.d("downloadImageUrl", downloadImageResourcesUrl);
                // 调用工具类下载解压
                imageUtil.downloadAndUnzip(CheckUpdateActivity.this, downloadImageResourcesUrl, isFullDownload, new ImageResourcesUpdaterUtil.DownloadCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDownloadProgress(int progress) {
                        runOnUiThread(() ->
                                update_image_resources_action.setText("⏳下载中: " + progress + "%⏳")
                        );
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onUnzipProgress(int progress) {
                        runOnUiThread(() ->
                                update_image_resources_action.setText("⏳解压中: " + progress + "%⏳")
                        );
                    }

                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            // 更新本地版本号
                            String newVersion = String.valueOf(serverImageResourcesVersionCode);
                            dbHelper.updateDataStationValue("DataImageResourcesVersionCode", newVersion);
                            // 更新本地更新日志
                            dbHelper.updateDataStationValue("CurrentUpdateLogImageResources", serverImageResourcesUpdateLog);
                            localImageResourcesVersionCode = serverImageResourcesVersionCode;

                            isImageResourcesDownloading = false;

                            update_image_resources_action.setText("✅更新成功✅");
                            update_image_resources_action.setEnabled(true);
                        });
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        runOnUiThread(() -> {
                            isImageResourcesDownloading = false;
                            update_image_resources_action.setText("下载失败，点击重试");
                            update_image_resources_action.setEnabled(true);
                        });
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> {
                    isImageResourcesDownloading = false;
                    update_image_resources_action.setText("获取链接失败，点击重试");
                    update_image_resources_action.setEnabled(true);
                });
            }
        };

        // 根据更新类型调用相应的方法获取下载链接
        if (isFullDownload) {
            imageUtil.getFullDownloadUrl(urlCallback);
        } else {
            imageUtil.getPartialDownloadUrl(urlCallback);
        }
    }

    // =========================== 以下是App部分 ===========================

    /**
     * 初始化App更新要用到的视图
     */
    private void initViewsForApp() {
        getContent(this, findViewById(R.id.title_app_log), "# App");
        update_app_action = findViewById(R.id.update_app_action);
        container_app_log_new = findViewById(R.id.container_app_log_new);
        app_log_new = findViewById(R.id.app_log_new);
        app_log_current = findViewById(R.id.app_log_current);
        getContentFromAssets(this, app_log_current, "CurrentUpdateLog.txt");

        // 设置App更新按钮点击监听
        update_app_action.setOnClickListener(v -> v.postDelayed(() -> {
            String buttonText = update_app_action.getText().toString();
            if (buttonText.equals(getResources().getString(R.string.label_check_update_status_new))) {
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
        }, pressFeedbackAnimationDelay));
    }

    /**
     * 获取App本地版本号，从build.gradle.kts获取
     */
    private void getAppLocalVersion() {
        // 获取version信息
        localAppVersionCode = LocalVersionUtil.getAppLocalVersionCode(this);
        String localAppVersionName = LocalVersionUtil.getAppLocalVersionName(this);

        // 判断是否为Beta版
        String betaOrRelease = Objects.equals(localAppVersionName.split("\\.")[2], "0") ? " | Release" : " | Beta";

        // 拼接最终版本信息
        TextView version_info = findViewById(R.id.version_info);
        String versionInfo = localAppVersionName + "(" + localAppVersionCode + ")" + betaOrRelease;
        version_info.setText(versionInfo);
    }

    /**
     * 检查App版本更新
     */
    private void getAppServerVersionAndCheckAppUpdate() {
        update_app_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        // 调用UpdaterUtil检查更新
        appUpdaterUtil.checkServerVersion(
                new AppUpdaterUtil.OnVersionCheckCallback() {
                    @Override
                    public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                        runOnUiThread(() -> {
                            serverAppVersionCode = serverVersion; // 保存服务器版本
                            try {
                                if (serverVersion > localAppVersionCode) {
                                    // 有新版本，显示更新日志
                                    getContent(CheckUpdateActivity.this, app_log_new, updateLog);

                                    // 检查是否存在已下载的APK
                                    if (checkExistingApkFile()) {
                                        update_app_action.setText(getResources().getString(R.string.label_check_update_status_downloaded_past));
                                    } else {
                                        update_app_action.setText(getResources().getString(R.string.label_check_update_status_new));
                                    }
                                } else {
                                    // 已是最新版本
                                    update_app_action.setText(getResources().getString(R.string.label_check_update_status_current));
                                    downloadAppUrl = null;
                                }
                                // 标记完成并尝试执行动画
                                hasAppResult = true;
                                checkAndRunAnimation();
                            } catch (Exception e) {
                                hasAppResult = true; // 即使出错也标记完成
                                checkAndRunAnimation();
                                update_app_action.setText("检查版本时发生错误");
                                downloadAppUrl = null;
                            }
                        });
                    }

                    @Override
                    public void onVersionCheckFailure(String errorMsg) {
                        runOnUiThread(() -> {
                            hasAppResult = true; // 标记完成
                            checkAndRunAnimation();
                            update_app_action.setText(errorMsg);
                            downloadAppUrl = null;
                        });
                    }

                    @Override
                    public void onVersionParseError() {
                        runOnUiThread(() -> {
                            hasAppResult = true; // 标记完成
                            checkAndRunAnimation();
                            update_app_action.setText("版本信息错误");
                            downloadAppUrl = null;
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
                if (apkFile.exists() && apkFile.isFile() && apkFile.length() > 0 && savedApkFileVersionCode > localAppVersionCode) {
                    downloadAppUrl = apkFile.getAbsolutePath();
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
        update_app_action.setText("⏳获取下载链接中⏳");

        appUpdaterUtil.getDownloadUrl(new AppUpdaterUtil.OnDownloadUrlCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                downloadAppUrl = downloadUrl;
                // 清理旧的APK文件
                cleanupOldApkFiles();
                // 开始下载
                startAppDownload();
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> {
                    update_app_action.setText("获取链接失败，点击重试");
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
        if (downloadAppUrl == null || downloadAppUrl.isEmpty()) {
            Toast.makeText(this, "下载链接无效", Toast.LENGTH_SHORT).show();
            update_app_action.setText("下载链接无效，点击重试");
            return;
        }

        isAppDownloading = true;
        update_app_action.setEnabled(false);

        appUpdaterUtil.downloadApk(this, downloadAppUrl, new AppUpdaterUtil.DownloadCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadProgress(int progress) {
                runOnUiThread(() ->
                        update_app_action.setText("⏳下载中: " + progress + "%⏳")
                );
            }

            @Override
            public void onSuccess(String apkFilePath) {
                runOnUiThread(() -> {
                    isAppDownloading = false;
                    update_app_action.setText(getResources().getString(R.string.label_check_update_status_downloaded_now));
                    update_app_action.setEnabled(true);

                    // 保存APK文件路径
                    downloadAppUrl = apkFilePath;

                    // 保存APK文件名和APK的版本号到数据库
                    File apkFile = new File(apkFilePath);
                    if (apkFile.exists()) {
                        String apkFileName = apkFile.getName();
                        dbHelper.updateDataStationValue("DownloadedApkFileName", apkFileName);
                        dbHelper.updateDataStationValue("DownloadedApkFileVersionCode", String.valueOf(serverAppVersionCode));
                        Log.d("ApkDownload", "Saved APK filename to DB: " + apkFileName);
                        Log.d("ApkDownload", "Saved APK version code to DB: " + serverAppVersionCode);
                    }
                });
            }

            @Override
            public void onFailure(String errorMsg) {
                runOnUiThread(() -> {
                    isAppDownloading = false;
                    update_app_action.setText("下载失败，点击重试");
                    update_app_action.setEnabled(true);
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

            File apkFile = new File(downloadAppUrl);
            if (!apkFile.exists()) {
                Toast.makeText(this, "安装文件不存在", Toast.LENGTH_SHORT).show();
                update_app_action.setText("安装文件不存在，请重新下载");
                return;
            }

            // 使用FileProvider获取Uri - 修改authority
            Uri apkUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider", // 修改为 .provider
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

    // =========================== 以下是动画和界面部分 ===========================

    // 添加同步检查方法
    private void checkAndRunAnimation() {
        // 只有当两个检查都完成且所有待完成检查都计数完成时才执行动画
        if (hasImageResult && hasAppResult) {
            // 使用post确保在UI线程的下一个循环执行动画
            check_update_container.post(() -> {
                TransitionManager.beginDelayedTransition(check_update_container, transition);
                if (localImageResourcesVersionCode < serverImageResourcesVersionCode) {
                    showViewWithAnimation(container_image_resources_log_new);
                } else {
                    hideViewWithAnimation(container_image_resources_log_new);
                }
                if (localAppVersionCode < serverAppVersionCode) {
                    showViewWithAnimation(container_app_log_new);
                } else {
                    hideViewWithAnimation(container_app_log_new);
                }
            });
        }
    }

    private void showViewWithAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start();
    }

    private void hideViewWithAnimation(View view) {
        if (view.getVisibility() == View.GONE) return;
        view.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(400)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
        // 添加按压动画
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        findViewById(R.id.update_image_resources_action).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.update_app_action).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消所有下载
        if (isAppDownloading) {
            appUpdaterUtil.cancelDownload();
        }
        if (isImageResourcesDownloading) {
            imageUtil.cancelDownload();
        }
    }
}