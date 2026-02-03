package com.careful.HyperFVM.utils.OtherUtils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 查黑系统工具类，负责查询QQ号是否被标记为骗子
 * 优化版：直接使用从主页面获取的QQ号拼接昵称API地址
 */
public class IcuHelper {
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String BASE_URL = "https://www.msdzls.icu";
    private static final String NICKNAME_API_PATH = "/fraud/getNickname?qq=";

    public IcuHelper(Context context) {
        this.context = context;
    }

    // 查询QQ号是否为骗子
    public void queryFraudInfo(String qqNumber, QueryCallback callback) {
        if (qqNumber == null || qqNumber.isEmpty()) {
            callback.onError("QQ号不能为空");
            return;
        }

        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("查询中")
                .setMessage("正在验证QQ号信息，请稍候...")
                .setCancelable(false);
        Dialog dialog = dialogBuilder.create();

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();

        executor.execute(() -> {
            FraudResult result;
            String mainUrl = BASE_URL + "/fraud/viewFraud?qq=" + qqNumber;
            try {
                // --- 步骤 1: 请求主页面，获取基础信息 ---
                Document doc = Jsoup.connect(mainUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .get();

                // 从主页面解析出QQ号（用于拼接昵称API）和其他信息
                Elements qqInfoRow = doc.select(".info-section.info-row:has(.info-label:contains(查询QQ：))");
                if (qqInfoRow.isEmpty()) {
                    throw new IOException("未在主页面找到QQ号信息");
                }
                String qqFromPage = qqInfoRow.select(".info-col:first-child .info-value").text();
                String remark = "";
                String recordTime = "";
                Elements fraudRemarkRow = doc.select(".info-section.info-row:has(.info-label:contains(备注：))");
                if (!fraudRemarkRow.isEmpty()) {
                    remark = fraudRemarkRow.select(".info-col:first-child .info-value").text();
                    recordTime = fraudRemarkRow.select(".info-col:last-child .info-value").text();
                }

                // --- 步骤 2: 使用获取到的QQ号拼接URL，请求昵称API ---
                String nicknameApiUrl = BASE_URL + NICKNAME_API_PATH + qqFromPage;
                String nicknameJson = Jsoup.connect(nicknameApiUrl)
                        .ignoreContentType(true) // 关键：忽略内容类型，允许接收JSON
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .get()
                        .body()
                        .text();

                JSONObject jsonObject = new JSONObject(nicknameJson);
                String realNickname = jsonObject.getString("nickname");

                // --- 步骤 3: 组装最终结果 ---
                result = new FraudResult(!remark.isEmpty(), qqFromPage, realNickname, remark, recordTime);

            } catch (IOException e) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    callback.onError("网络异常，请检查网络连接或稍后重试。");
                });
                return;
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    callback.onError("解析昵称数据失败。");
                });
                return;
            }

            FraudResult finalResult = result;
            mainHandler.post(() -> {
                dialog.dismiss();
                callback.onSuccess(finalResult);
            });
        });
    }

    public interface QueryCallback {
        void onSuccess(FraudResult result);
        void onError(String message);
    }

    public static class FraudResult {
        public final boolean isFraud;
        public final String qq;
        public final String nickname;
        public final String remark;
        public final String recordTime;

        public FraudResult(boolean isFraud, String qq, String nickname, String remark, String recordTime) {
            this.isFraud = isFraud;
            this.qq = qq;
            this.nickname = nickname;
            this.remark = remark;
            this.recordTime = recordTime;
        }
    }
}