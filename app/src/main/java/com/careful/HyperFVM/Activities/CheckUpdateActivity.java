package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.DownloadService;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForDownload.DownloadStateStore;
import com.careful.HyperFVM.utils.ForDownload.DownloadNotificationHelper;
import com.careful.HyperFVM.utils.ForUpdate.AppUpdaterUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.io.File;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class CheckUpdateActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_check_update_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private DBHelper dbHelper;
    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（本页仅联动模糊层）

    private AppUpdaterUtil appUpdaterUtil;

    private String versionInfo;
    private long localVersionCode;
    private long serverVersionCode = -1;
    private String serverVersionName;
    private String downloadUrl;
    // 远程下载地址快照（下载失败后点击重试用）
    private String apkRetryUrl;

    // 通知权限请求（下载前检查，拒绝不阻塞下载，仅提示看不到进度通知）
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private Runnable pendingDownloadRunnable;

    // 下载状态监听：onStart注册（注册即回放快照）、onStop注销，任意时刻回到本页同步进度
    private final DownloadStateStore.Listener apkDownloadListener = this::applyApkDownloadSnapshot;
    // 按钮当前是否正处于下载相关展示（下载中/暂停/完成/失败），用于识别“下载被取消”后的UI恢复
    private boolean showingDownloadUi;

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

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 初始化工具类
        appUpdaterUtil = AppUpdaterUtil.getInstance();

        // 注册通知权限请求：下载前若未授予通知权限则先请求，拒绝也不阻塞下载
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "通知权限未开启，下载进度将不在通知栏展示", Toast.LENGTH_LONG).show();
                    }
                    if (pendingDownloadRunnable != null) {
                        pendingDownloadRunnable.run();
                        pendingDownloadRunnable = null;
                    }
                });

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

                // 更新日志来自服务器内容，格式不带“)换行”时split结果不足2段，
                // 直接取[1]会ArrayIndexOutOfBoundsException导致点击崩溃
                String[] updateLogParts = newUpdateLog.split("\\)\n");
                if (updateLogParts.length > 1) {
                    newUpdateLog = updateLogParts[1];
                }
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
            } else if (buttonText.equals("下载失败，点击重试")) {
                // 下载失败：用快照中的远程地址重新发起下载
                if (apkRetryUrl != null) {
                    downloadUrl = apkRetryUrl;
                    startAppDownload();
                } else {
                    getAppServerVersionAndCheckAppUpdate();
                }
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
        // 判断是否为Beta版（versionName来自服务器内容，段数不足时不能直接取[2]）
        String[] versionParts = versionInfo.split("\\.");
        String patchPart = versionParts.length > 2 ? versionParts[2].split("\\(")[0] : "";
        String betaOrRelease = Objects.equals(patchPart, "0") ? " | Release" : " | Beta";

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
                            // 下载进行中时按钮显示以下载进度为准，版本检查结果不覆盖
                            if (DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_APK).isActive()) {
                                return;
                            }
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
                            if (DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_APK).isActive()) {
                                return;
                            }
                            float_button_update.setText(errorMsg);
                            downloadUrl = null;
                        });
                    }

                    @Override
                    public void onVersionParseError() {
                        runOnUiThread(() -> {
                            if (DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_APK).isActive()) {
                                return;
                            }
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
     * 开始下载App更新（下载交由前台服务进行，不与当前界面绑定：
     * 界面 finish 后下载继续，完成落库由服务负责，回到本页时同步进度）
     */
    private void startAppDownload() {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            Toast.makeText(this, "下载链接无效", Toast.LENGTH_SHORT).show();
            float_button_update.setText("下载链接无效，点击重试");
            return;
        }

        float_button_update.setEnabled(false);
        float_button_update.setText("⏳下载中: 0%⏳");

        final String remoteUrl = downloadUrl;
        ensureNotificationPermissionThen(() ->
                DownloadService.startApkDownload(this, remoteUrl, serverVersionCode));
    }

    /**
     * 下载前确保通知权限：Android 13+ 未授予通知权限时先请求，
     * 无论授予与否都继续执行下载（拒绝仅看不到通知栏进度）
     */
    private void ensureNotificationPermissionThen(Runnable action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            pendingDownloadRunnable = action;
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            action.run();
        }
    }

    /**
     * 根据下载服务发布的快照同步按钮状态（主线程回调，含注册时的快照回放）
     */
    @SuppressLint("SetTextI18n")
    private void applyApkDownloadSnapshot(DownloadStateStore.Snapshot snapshot) {
        if (snapshot == null || snapshot.taskType != DownloadStateStore.TASK_APK) {
            return;
        }
        // 记住远程下载地址用于失败重试
        if (snapshot.apkDownloadUrl != null && !snapshot.apkDownloadUrl.isEmpty()) {
            apkRetryUrl = snapshot.apkDownloadUrl;
        }
        if (snapshot.phase != DownloadStateStore.Phase.IDLE) {
            showingDownloadUi = true;
        }
        switch (snapshot.phase) {
            case IDLE:
                // 空闲态不覆盖版本检查流程展示的按钮状态；
                // 但若按钮正处于下载相关展示，说明下载被取消了，恢复为可重新下载的状态
                if (showingDownloadUi) {
                    showingDownloadUi = false;
                    float_button_update.setEnabled(true);
                    float_button_update.setText(getResources().getString(R.string.label_check_update_status_download_update));
                }
                return;
            case DOWNLOADING:
                float_button_update.setEnabled(false);
                float_button_update.setText("⏳下载中: " + snapshot.percent + "%⏳");
                break;
            case SUCCESS:
                // 完成落库由服务负责，这里同步按钮为“点击安装”
                downloadUrl = snapshot.apkFilePath;
                float_button_update.setEnabled(true);
                float_button_update.setText(getResources().getString(R.string.label_check_update_status_downloaded_now));
                break;
            case FAILED:
                float_button_update.setEnabled(true);
                float_button_update.setText("下载失败，点击重试");
                break;
        }
    }

    /**
     * 页面安装入口：补页面按钮文案（通用安装方法访问不到页面控件）后委托通用安装方法
     */
    @SuppressLint("QueryPermissionsNeeded")
    private void installApk() {
        if (downloadUrl != null && !new File(downloadUrl).exists()) {
            float_button_update.setText("安装文件不存在，请重新下载");
        }
        installApk(this, downloadUrl);
    }

    /**
     * 拉起系统安装器安装指定APK（检查更新页与APK完成通知中转页共用的通用安装入口）
     *
     * @param context     用于权限引导弹窗/Toast/FileProvider——权限弹窗依赖Activity窗口，
     *                    必须传Activity上下文
     * @param apkFilePath 待安装APK的本地绝对路径
     * @return 是否成功发起安装器启动
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static boolean installApk(Context context, String apkFilePath) {
        try {
            // 检查是否有安装未知应用的权限
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                // 引导用户开启安装权限
                DialogBuilderManager.showPackageInstallPermissionDialog(context);
                return false;
            }

            File apkFile = new File(apkFilePath);
            if (!apkFile.exists()) {
                Toast.makeText(context, "安装文件不存在", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 使用FileProvider获取Uri - 修改authority
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有可以处理此Intent的应用
            if (installIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(installIntent);
                return true;
            } else {
                Toast.makeText(context, "没有找到可以安装应用的程序", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Log.d("install", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(context, "安装失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
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
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态获取导航栏高度（小白条/三键导航）
        LinearLayout bottomBarContainer = findViewById(R.id.bottomBarContainer);
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomBarContainer.getLayoutParams();
            params.bottomMargin = height;
            // 底部按钮最好三个边距等宽
            params.leftMargin = height;
            params.rightMargin = height;
            bottomBarContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout check_update_container = findViewById(R.id.check_update_container);
        InsetsUtil.setMarginHorizontal(this, check_update_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) check_update_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            check_update_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置返回按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：本页只联动模糊背景层（topBarBottom/topBar 传0跳过）
        nestedScrollUtil = NestedScrollUtil.attach(rootView,
                R.id.scrollView, 0, R.id.topBar, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0.5f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonUpdate), 0f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonJoin), 0f);
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 注册下载状态监听（注册即回放快照）：回到本页立即同步当前下载进度
        DownloadStateStore.getInstance().addListener(DownloadStateStore.TASK_APK, apkDownloadListener);
        // 兜底撤下滞留的APK完成通知（promoted ongoing不可滑除，正常由点击通知的
        // 安装中转页撤下）；下载进行中的进度通知保留（由服务继续更新）
        if (!DownloadStateStore.getInstance().getSnapshot(DownloadStateStore.TASK_APK).isActive()) {
            NotificationManagerCompat.from(this).cancel(DownloadNotificationHelper.NOTIF_ID_APK);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadStateStore.getInstance().removeListener(apkDownloadListener);
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
        // 注意：此处不再取消下载——下载在前台服务中进行，与本界面生命周期解耦，
        // 可通过通知栏的“取消”按钮手动终止
    }
}