package com.careful.HyperFVM.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForMeishi.MeishiLinkUtil;

/**
 * 链接接入中转页（透明壳）
 * 系统解析“打开受支持的链接”后启动的是本页面而非{@link MeishiWechatActivity}：
 * 本页面透明无内容，收到链接后直接在来源应用（如微信）画面上弹出Toast与确认弹窗，
 * 确认入库或取消后立即finish回到来源应用，全程不跳转任何业务界面。
 * 注意：本页面不能调用ThemeManager.applyTheme，透明主题由Manifest指定。
 */
public class MeishiLinkReceiverActivity extends BaseActivity {

    // 防止重复处理同一条链接（singleTop下onNewIntent可能连续到达）
    private boolean isResolving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 系统因内存不足重建了本页面（弹窗已丢失），直接关闭避免残留透明壳
        if (savedInstanceState != null) {
            finish();
            return;
        }

        Uri data = getIntent() != null ? getIntent().getData() : null;
        if (data == null) {
            finish();
            return;
        }

        handleLink(data);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri data = intent.getData();
        if (data != null && !isResolving) {
            handleLink(data);
        }
    }

    /**
     * 处理从系统链接跳转进来的链接：
     * Toast 提示 → 后台解析区服和角色 → 确认弹窗展示解析结果 → 用户点击“添加”后入库 → finish
     */
    private void handleLink(Uri data) {
        if (isResolving) {
            return;
        }

        String openId = MeishiLinkUtil.checkLink(data.toString());
        if (openId.isEmpty()) {
            Toast.makeText(this, "链接格式不正确", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isResolving = true;
        Toast.makeText(this, "收到链接，正在解析...", Toast.LENGTH_SHORT).show();

        MeishiLinkUtil.fetchPlayerInfo(this, openId, new MeishiLinkUtil.FetchCallback() {
            @Override
            public void onSuccess(String serverName, String playerName) {
                showAddConfirmDialog(openId, serverName, playerName);
            }

            @Override
            public void onFailure(@Nullable Dialog failureDialog) {
                if (failureDialog != null) {
                    // 错误弹窗：等用户看完并关闭后再finish，避免弹窗被瞬间收走或WindowLeaked
                    failureDialog.setOnDismissListener(dialog -> finish());
                } else {
                    // Toast失败提示不会随Activity销毁，直接finish回到来源应用
                    finish();
                }
            }
        });
    }

    /**
     * 确认弹窗：展示解析到的区服和角色，点击“添加”后才入库
     */
    private void showAddConfirmDialog(String openId, String serverName, String playerName) {
        Dialog dialog = DialogBuilderManager.showDialogWithCallBack(
                this, "添加链接", "🔗",
                serverName + "\n" + playerName,
                true, "取消", "添加",
                () -> {
                    HyperFVMApplication.getDBHelper().insertMeishiWechat(openId, serverName, playerName);
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                }
        );
        // 添加/取消/点背景关闭，任何收尾路径都关闭中转页，避免透明壳残留挡住来源应用
        dialog.setOnDismissListener(d -> finish());
    }

    @Override
    protected boolean shouldRecreateOnConfigurationChanged() {
        // 配置变化（旋转/深浅色切换）不重建，防止进行中的确认弹窗被销毁
        return false;
    }
}
