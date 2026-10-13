package com.careful.HyperFVM.utils.OtherUtils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

/**
 * 剪贴板读写权限检查（AppOps 判定，供设置页与温馨礼包备份还原共用的单一来源）
 */
public class ClipboardPermissionUtil {

    // 剪贴板读/写操作对应的 AppOps 名称（即 AppOpsManager.OP_READ_CLIPBOARD / OP_WRITE_CLIPBOARD）
    private static final String OP_READ_CLIPBOARD = "android:read_clipboard";
    private static final String OP_WRITE_CLIPBOARD = "android:write_clipboard";

    private ClipboardPermissionUtil() {
    }

    /**
     * 检查是否拥有剪贴板读写权限：系统无对应运行时权限，经由 AppOps 读/写剪贴板操作码判定；
     * 厂商（MIUI/HyperOS 等）在“权限管理”中撤销剪贴板权限时会将对应 AppOps 置为忽略/拒绝
     */
    public static boolean hasPermission(Context context) {
        AppOpsManager appOps = context.getSystemService(AppOpsManager.class);
        return isOpGranted(appOps, OP_READ_CLIPBOARD, context.getPackageName())
                && isOpGranted(appOps, OP_WRITE_CLIPBOARD, context.getPackageName());
    }

    /**
     * 剪贴板 AppOps 模式是否为已授予（MODE_DEFAULT 表示系统未做限制，视为已授予）
     */
    private static boolean isOpGranted(AppOpsManager appOps, String op, String packageName) {
        int mode = appOps.checkOpNoThrow(op, Process.myUid(), packageName);
        return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_DEFAULT;
    }
}
