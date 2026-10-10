package com.careful.HyperFVM.utils.ForMeishi;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 美食微信链接处理工具类
 * 抽出链接校验与区服/角色解析逻辑，供手动添加流程和链接跳转接入流程共用，避免两份拷贝
 */
public class MeishiLinkUtil {

    // 提取openid的正则表达式
    private static final Pattern OPENID_PATTERN = Pattern.compile("openid=([^&]+)");

    /**
     * 解析结果回调（均在主线程回调）
     */
    public interface FetchCallback {
        /**
         * 解析成功
         *
         * @param serverName 区服名
         * @param playerName 角色名
         */
        void onSuccess(String serverName, String playerName);

        /**
         * 解析失败，失败提示（Toast或弹窗）已由工具类展示
         *
         * @param failureDialog 非null表示展示的是错误弹窗（调用方可挂dismiss监听做收尾）；
         *                      Toast提示时为null
         */
        void onFailure(@Nullable Dialog failureDialog);
    }

    /**
     * 检查链接是否有问题
     *
     * @param inputLink 待检查的链接
     * @return 提取到的openid；空字符串表示链接格式不正确或无法提取openid
     */
    public static String checkLink(String inputLink) {
        // 检查1：是否是一个链接
        if (!Patterns.WEB_URL.matcher(inputLink).matches()) {
            return "";
        }

        // 检查2：是否能正常从链接中提取到openid
        Matcher matcher = OPENID_PATTERN.matcher(inputLink);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 网络请求：获取网页内容解析区服和角色ID
     * 失败提示（Toast或错误弹窗）由本方法直接展示，调用方无需重复提示
     *
     * @param context  用于展示Toast和错误弹窗的上下文
     * @param openId   链接中提取到的openid
     * @param callback 解析结果回调（主线程）
     */
    public static void fetchPlayerInfo(Context context, String openId, FetchCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String url = "http://meishi.wechat.123u.com/meishi/index?openid=" + openId;
            OkHttpClient client = new OkHttpClient();

            try {
                // 请求构建可能因url非法抛出IllegalArgumentException（RuntimeException），
                // 必须纳入try避免线程未捕获异常导致App闪退
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body().string();

                    try {
                        Pattern pattern = Pattern.compile("<h1 class=\"title\">(.*?)</h1>");
                        Matcher matcher = pattern.matcher(html);

                        if (matcher.find()) {
                            String title = Objects.requireNonNull(matcher.group(1)).trim();
                            String[] parts = title.split(" - ");

                            // 解析成功
                            if (parts.length == 2) {
                                String serverName = parts[0].trim();
                                String playerName = parts[1].trim();
                                mainHandler.post(() -> callback.onSuccess(serverName, playerName));
                                return;
                            }
                        }

                        // 解析失败提示（UI操作，用Handler）
                        mainHandler.post(() -> {
                            Toast.makeText(context, "解析失败\n未找到区服和角色信息", Toast.LENGTH_SHORT).show();
                            callback.onFailure(null);
                        });

                    } catch (Exception e) {
                        // 异常提示（UI操作，用Handler）
                        mainHandler.post(() -> {
                            Dialog failureDialog = DialogBuilderManager.showDialog(
                                    context,
                                    "出现问题",
                                    "❌",
                                    "以下为问题日志，请将此界面截图并向开发者反馈。\n" + e.getMessage(),
                                    true,
                                    "好的"
                            );
                            callback.onFailure(failureDialog);
                        });
                    }
                } else {
                    // 用Handler显示Toast（主线程）
                    mainHandler.post(() -> {
                        Toast.makeText(context, "获取信息失败\n服务器无响应", Toast.LENGTH_SHORT).show();
                        callback.onFailure(null);
                    });
                }
            } catch (Exception e) {
                // 兜底捕获RuntimeException（如url非法）；用Handler显示Toast（主线程）
                mainHandler.post(() -> {
                    Toast.makeText(context, "网络错误\n无法连接服务器", Toast.LENGTH_SHORT).show();
                    callback.onFailure(null);
                });
            }
        }).start();
    }
}
