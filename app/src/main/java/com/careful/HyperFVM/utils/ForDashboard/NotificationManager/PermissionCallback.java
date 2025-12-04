package com.careful.HyperFVM.utils.ForDashboard.NotificationManager;

// 定义权限请求结果回调接口
public interface PermissionCallback {
    void onPermissionGranted(); // 权限授予
    void onPermissionDenied();  // 权限拒绝
}
