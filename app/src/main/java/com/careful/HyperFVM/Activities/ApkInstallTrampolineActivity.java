package com.careful.HyperFVM.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDownload.DownloadNotificationHelper;
import com.careful.HyperFVM.utils.ForDownload.DownloadStateStore;

import java.io.File;

/**
 * APK完成通知的点击中转页（无界面透明蹦床Activity，独立任务taskAffinity=""）：
 * 1. 点击即撤下完成通知——promoted ongoing 通知不可滑除，必须由本App组件在点击时撤下；
 * 2. 直接拉起系统安装器——不经过检查更新页，且独立任务保证通知点击不会把本App
 *    主任务（栈顶可能是检查更新页）带到前台；
 * 3. 等安装器窗口上台（onPause）后才finish——透明窗口先浮在点通知时的前台画面上，
 *    若立即finish会闪现任务栈下层页面（Theme.NoDisplay方案的教训）。
 * Android 12+ 的 notification trampoline 政策只禁止service/broadcast中转startActivity，
 * Activity中转是官方文档认可的迁移方案
 */
public class ApkInstallTrampolineActivity extends Activity {

    // 安装器已成功拉起：onPause（安装器窗口上台）后finish；失败路径自行finish
    private boolean installerLaunched = false;
    // 超时兑底：极端情况下onPause未按预期到来时撤出，避免透明壳滞留
    private final Handler finishFallback = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 点击即视为已读：撤下APK完成通知（promoted ongoing不可滑除，本页是唯一点击撤下入口）
        NotificationManagerCompat.from(this).cancel(DownloadNotificationHelper.NOTIF_ID_APK);

        installerLaunched = launchInstaller();
        if (!installerLaunched) {
            // 没拉起安装器：立即结束，不留透明壳
            finish();
            return;
        }
        // 兑底：若安装器迟迟未上台（onPause未到来），5秒后强制撤出
        finishFallback.postDelayed(this::finish, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (installerLaunched) {
            // 安装器窗口已上台（或已离开本页）：此刻finish，前台始终被安装器盖着，
            // 用户看不到任务栈下层的任何页面
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        finishFallback.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 拉起系统安装器：直接复用检查更新页的通用安装入口
     *
     * @return 是否成功发起安装器启动
     */
    private boolean launchInstaller() {
        try {
            String apkPath = resolveApkPath();
            if (apkPath == null) {
                return false;
            }

            // 首次安装未授权：权限引导弹窗需要Activity窗口承载（挂在本透明页上会随finish消失，
            // 且去授权/返回流程会把透明壳吊死），回落到检查更新页完成授权；授权一次后
            // 再点通知即走下方直达安装器路径
            if (!getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(this, "首次安装需要授予‘安装未知应用’权限", Toast.LENGTH_SHORT).show();
                Intent page = new Intent(this, CheckUpdateActivity.class);
                page.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(page);
                return false;
            }

            return CheckUpdateActivity.installApk(this, apkPath);
        } catch (Exception e) {
            Log.e("ApkInstallTrampoline", "拉起安装器失败", e);
            return false;
        }
    }

    /**
     * 优先取下载服务发布的完成快照；下载进程被杀、快照丢失时回落数据库记录的
     * 已下载APK文件名（与检查更新页的已下载判定口径一致）
     */
    private String resolveApkPath() {
        DownloadStateStore.Snapshot snapshot = DownloadStateStore.getInstance()
                .getSnapshot(DownloadStateStore.TASK_APK);
        if (snapshot.phase == DownloadStateStore.Phase.SUCCESS && snapshot.apkFilePath != null) {
            return snapshot.apkFilePath;
        }
        try {
            DBHelper dbHelper = HyperFVMApplication.getDBHelper();
            String savedApkFileName = dbHelper.getDataStationValue("DownloadedApkFileName");
            if (savedApkFileName != null && !savedApkFileName.isEmpty()) {
                File apkDir = getExternalFilesDir("apk");
                if (apkDir != null) {
                    return new File(apkDir, savedApkFileName).getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Log.e("ApkInstallTrampoline", "回落读取已下载APK记录失败", e);
        }
        return null;
    }
}
