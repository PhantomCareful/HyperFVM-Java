package com.careful.HyperFVM.Activities.Oobe;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.OtherUtils.ClipboardPermissionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

/**
 * OOBE 权限申请页（浅色 Provision 风格骨架）。
 * 本轮只建框架：页头（返回+标题）/ 占位内容区 / 底部下一步；
 * 权限条目与申请功能（POST_NOTIFICATIONS 运行时申请、REQUEST_INSTALL_PACKAGES 跳系统"安装未知应用"页
 */
public class OobePermissionFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oobe_permission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        // 初始化各种装饰效果
        initDecoration();

        // 下一步 → 主题设置页（栈式启动，本页留栈供返回）
        view.findViewById(R.id.oobe_btn_next).setOnClickListener(v ->
                ((OobePageActivity) requireActivity()).goNextStep());
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void checkPermissionStates() {
        // 1. 获取权限状态图标（已授予显示对勾，未授予显示右箭头）
        ImageView notificationStatusIv = view.findViewById(R.id.status_settings_permission_notification);
        ImageView installStatusIv = view.findViewById(R.id.status_settings_permission_install);
        ImageView clipboardStatusIv = view.findViewById(R.id.status_settings_permission_clipboard);

        // 2.1 跳转授予通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //点击授权
            view.findViewById(R.id.permission_notification_container).setOnClickListener(v -> {
                // 调用已注册的权限请求器
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(intent);
            });
        } else {
            view.findViewById(R.id.permission_notification_container).setOnClickListener(null);
        }

        // 2.2 跳转授予安装权限
        //点击授权
        view.findViewById(R.id.permission_install_container).setOnClickListener(v -> {
            // 跳转到安装未知应用权限设置页面
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

            // 需要指定包名
            intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                this.startActivity(intent);
            } else {
                // 如果无法跳转到精确设置页面，跳转到应用详情页
                Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appDetailsIntent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                this.startActivity(appDetailsIntent);
            }
        });

        // 2.3 跳转系统“默认打开”页面，手动勾选受支持的链接（域名无法自动验证，勾选后链接直接跳转进App）
        view.findViewById(R.id.permission_links_container).setOnClickListener(v -> {
            // 跳转到本应用的“默认打开”设置页
            Intent intent = new Intent(android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
                    .setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                this.startActivity(intent);
            } else {
                // 如果无法跳转到精确设置页面，跳转到应用详情页
                Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appDetailsIntent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                this.startActivity(appDetailsIntent);
            }
        });

        // 2.4 跳转授权剪贴板读写权限：米系设备直跳安全中心的本应用权限管理页（剪贴板开关在“其他权限”内），其他设备走应用详情页
        view.findViewById(R.id.permission_clipboard_container).setOnClickListener(v -> {
            Intent intent;

            if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
                // MIUI/HyperOS权限编辑器action，可精准定位到本应用的权限管理页（包名extra的key为extra_pkgname，已真机验证）
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR")
                        .putExtra("extra_pkgname", requireContext().getPackageName());
            } else {
                // 其他设备无此action，直接跳转应用详情页，手动前往“权限管理”授予剪贴板权限
                intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
            }

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                this.startActivity(intent);
            }
        });

        // 更新UI
        updatePermissionStatusIcon(notificationStatusIv, hasNotificationPermission());
        updatePermissionStatusIcon(installStatusIv, hasInstallPermission());
        updatePermissionStatusIcon(clipboardStatusIv, hasClipboardPermission());
    }

    /**
     * 根据权限授予状态切换卡片右侧图标：已授予显示对勾，未授予显示右箭头
     */
    private void updatePermissionStatusIcon(ImageView statusIv, boolean granted) {
        statusIv.setImageResource(granted ? R.drawable.ic_check : R.drawable.ic_arrow_right);
    }

    /**
     * 检查是否拥有通知权限
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下默认拥有通知权限
            return true;
        }
    }

    /**
     * 检查是否拥有安装权限
     */
    private boolean hasInstallPermission() {
        return requireContext().getPackageManager().canRequestPackageInstalls();
    }

    /**
     * 检查是否拥有剪贴板读写权限（实现抽取至 ClipboardPermissionUtil，与温馨礼包备份还原共用单一来源）
     */
    private boolean hasClipboardPermission() {
        return ClipboardPermissionUtil.hasPermission(requireContext());
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        ImageButton floatButtonBack = view.findViewById(R.id.oobe_back);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(requireContext(), 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout oobe_permission_container = view.findViewById(R.id.oobe_permission_container);
        InsetsUtil.setMarginHorizontal(requireContext(), oobe_permission_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oobe_permission_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            oobe_permission_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置返回按钮的功能
        floatButtonBack.setOnClickListener(v -> requireActivity().finish());

        // 添加按压动画
        view.findViewById(R.id.tips_oobe_permission_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 在onResume阶段：检查通知权限并实时更新
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 检查权限授予状态
        checkPermissionStates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InsetsUtil.removeListener(getView());
    }
}
