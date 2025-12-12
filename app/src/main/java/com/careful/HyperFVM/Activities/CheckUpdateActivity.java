package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForUpdate.UpdaterUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.appbar.MaterialToolbar;

public class CheckUpdateActivity extends AppCompatActivity {

    private long localVersionCode;
    private UpdaterUtil updaterUtil; // 使用UpdaterUtil

    private String downloadUrl;

    private TextView update_action;
    private TextView title_update_log;
    private TextView check_update_update_log;

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

        // 初始化工具类（替换原有OkHttpClient相关初始化）
        updaterUtil = UpdaterUtil.getInstance();

        // 初始化视图
        title_update_log = findViewById(R.id.title_update_log);
        title_update_log.setText(getResources().getString(R.string.title_update_log_current));
        check_update_update_log = findViewById(R.id.check_update_update_log);
        getContentFromAssets(this, check_update_update_log, "CurrentUpdateLog.txt");

        update_action = findViewById(R.id.update_action);
        update_action.setOnClickListener(v -> {
            if (update_action.getText().toString().equals(getResources().getString(R.string.label_check_update_status_new))) {
                // 创建打开浏览器的Intent，使用获取到的下载链接
                if (downloadUrl != null && !downloadUrl.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(downloadUrl));

                    // 启动浏览器（添加try-catch处理没有浏览器的异常）
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                checkForUpdates();
            }
        });

        // 顶栏模糊
        setupBlurEffect();

        // 设置顶栏标题、启用返回按钮
        setTopAppBarTitle(getResources().getString(R.string.label_check_update));

        // 从build.gradle中获取版本号
        getVersion();

        // 检查更新
        checkForUpdates();
    }

    private void getVersion() {
        // 获取version信息
        localVersionCode = 0;
        String versionName = "0.0.0";

        // 获取versionCode
        try {
            localVersionCode = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0)
                    .getLongVersionCode();
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // 获取versionName
        try {
            versionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // 判断是否为Beta版
        String versionSuffix = "";
        String[] versionParts = null; // 分割版本号
        if (versionName != null) {
            versionParts = versionName.split("\\.");
        }
        // 确保版本号格式正确（至少3段）
        if (versionParts != null && versionParts.length >= 3) {
            try {
                int c = Integer.parseInt(versionParts[2]);
                if (c != 0) {
                    versionSuffix = " | Beta"; // 不为0时添加Beta标识
                } else {
                    versionSuffix = " | Release"; // 为0时添加Release标识
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 拼接最终版本信息
        TextView version_info = findViewById(R.id.version_info);
        String versionInfo = versionName + "(" + localVersionCode + ")" + versionSuffix;
        version_info.setText(versionInfo);
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题、启用返回按钮
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> this.finish());
    }

    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButton));
    }

    private void checkForUpdates() {
        update_action.setText(getResources().getString(R.string.label_check_update_status_checking));

        // 调用UpdaterUtil检查更新，接收下载链接
        updaterUtil.checkUpdate(
                localVersionCode,
                new UpdaterUtil.OnUpdateCheckListener() {
                    @Override
                    public void onNoUpdate() {
                        // 无更新：更新UI
                        update_action.setText(getResources().getString(R.string.label_check_update_status_current));
                        title_update_log.setText(getResources().getString(R.string.title_update_log_current));
                        getContentFromAssets(CheckUpdateActivity.this, check_update_update_log, "CurrentUpdateLog.txt");
                        // 清空下载链接
                        downloadUrl = null;
                    }

                    @Override
                    public void onHasUpdate(String updateLog, String url) {
                        // 有更新：保存下载链接并更新UI
                        downloadUrl = url;

                        CardView checkUpdateContainer = findViewById(R.id.check_update_container);
                        checkUpdateContainer.animate()
                                .alpha(0f)
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration(400)
                                .withEndAction(() -> {
                                    update_action.setText(getResources().getString(R.string.label_check_update_status_new));
                                    title_update_log.setText(getResources().getString(R.string.title_update_log_new));
                                    getContent(CheckUpdateActivity.this, check_update_update_log, updateLog);

                                    checkUpdateContainer.animate()
                                            .alpha(1f)
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(400)
                                            .start();
                                })
                                .start();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        // 错误处理：更新UI并提示
                        update_action.setText(errorMsg);
                        title_update_log.setText(getResources().getString(R.string.title_update_log_current));
                        getContentFromAssets(CheckUpdateActivity.this, check_update_update_log, "CurrentUpdateLog.txt");
                        Toast.makeText(CheckUpdateActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        // 清空下载链接
                        downloadUrl = null;
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}