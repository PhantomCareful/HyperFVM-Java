package com.careful.HyperFVM.utils.ForSafety;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricAuthHelper {

    /**
     * 使用生物识别（指纹/面部）进行身份验证
     *
     * @param activity 调用此方法的Activity
     * @param title 对话框标题
     * @param subtitle 对话框副标题
     * @param callback 回调接口
     */
    public static void authenticateWithBiometric(
            FragmentActivity activity,
            String title,
            String subtitle,
            BiometricAuthCallback callback) {

        Executor executor = ContextCompat.getMainExecutor(activity);

        // 创建 BiometricPrompt 实例
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onAuthenticationFailed();
            }
        });

        // 创建 PromptInfo
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setConfirmationRequired(false) // 是否需要确认按钮
                // 支持指纹认证失败后使用密码
                .setAllowedAuthenticators(
                        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
                                | androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

        try {
            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            // 设备不支持生物识别或出现其他错误
            callback.onBiometricUnavailable();
        }
    }

    /**
     * 检查设备是否支持生物识别
     *
     * @param context 上下文
     * @return 是否支持生物识别
     */
    public static boolean isBiometricAvailable(Context context) {
        androidx.biometric.BiometricManager biometricManager =
                androidx.biometric.BiometricManager.from(context);

        int canAuthenticate = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK);

        return canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * 简化版的生物识别验证，带有默认UI提示
     *
     * @param fragment 调用此方法的Fragment
     * @param successAction 验证成功后的操作（Runnable）
     */
    public static void simpleBiometricAuth(Fragment fragment, String title, String subTitle, Runnable successAction) {
        simpleBiometricAuth(fragment.requireActivity(), title, subTitle, successAction);
    }

    /**
     * 简化版的生物识别验证，带有默认UI提示
     *
     * @param activity 调用此方法的Activity
     * @param successAction 验证成功后的操作（Runnable）
     */
    public static void simpleBiometricAuth(FragmentActivity activity, String title, String subTitle, Runnable successAction) {
        authenticateWithBiometric(activity,
                title, subTitle,
                new BiometricAuthCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        successAction.run();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    }

                    @Override
                    public void onAuthenticationFailed() {
                    }

                    @Override
                    public void onBiometricUnavailable() {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, "生物识别不可用", Toast.LENGTH_SHORT).show();
                            // 可以选择是否继续执行操作
                            // successAction.run();
                        });
                    }
                });
    }

    public interface BiometricAuthCallback {
        void onAuthenticationSuccess();
        void onAuthenticationError(int errorCode, @NonNull CharSequence errString);
        void onAuthenticationFailed();
        void onBiometricUnavailable();
    }
}