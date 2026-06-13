package com.careful.HyperFVM.utils.OtherUtils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 查黑系统工具类，负责查询QQ号是否被标记为骗子
 * 适配最新 API：<a href="https://www.msdzls.icu/fraud/getFraudAccountDetail?qq=">...</a>
 */
public class IcuHelper {
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String API_URL = "https://www.msdzls.icu/fraud/getFraudAccountDetail?qq=";
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    public IcuHelper(Context context) {
        this.context = context;
    }

    /**
     * 将 API 返回的 ISO 8601 时间（UTC）转换为设备当前时区的日期字符串
     * @param time 格式如 "2026-05-29T02:56:30.000+00:00"
     * @return 设备本地时区日期，格式 "yyyy-MM-dd"，解析失败返回原字符串或空
     */
    private String formatToLocalDate(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            Date date = isoFormat.parse(time);
            SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            localDateFormat.setTimeZone(TimeZone.getDefault());
            return localDateFormat.format(Objects.requireNonNull(date));
        } catch (ParseException e) {
            return time;
        }
    }

    /**
     * 将金额从分转换为元（除以10）
     * @param amountInCents 分为单位的金额
     * @return 元为单位的金额，若为0则返回0.0
     */
    private double convertAmountToYuan(int amountInCents) {
        return amountInCents / 10.0;
    }

    /**
     * 查询QQ号是否为骗子
     * @param qqNumber 待查询的QQ号
     * @param callback 回调接口
     */
    public void queryFraudInfo(String qqNumber, QueryCallback callback) {
        if (qqNumber == null || qqNumber.isEmpty()) {
            callback.onError("QQ号不能为空");
            return;
        }

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("查询中")
                .setMessage("正在验证QQ号信息，请稍候...")
                .setCancelable(false);
        Dialog dialog = dialogBuilder.create();
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();

        executor.execute(() -> {
            try {
                String url = API_URL + qqNumber;
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    String jsonStr = Objects.requireNonNull(response.body()).string();
                    JSONObject root = new JSONObject(jsonStr);

                    JSONObject data = root.optJSONObject("data");
                    FraudResult result;
                    if (data == null) {
                        result = new FraudResult(false, "", "", "", null, 0, 0.0, 0, null);
                    } else {
                        int status = data.optInt("status", -1);
                        JSONObject fraudAccount = data.optJSONObject("fraudAccount");
                        if (status != 1 || fraudAccount == null) {
                            // 无行骗记录
                            result = new FraudResult(false, "", "", "", null, 0, 0.0, 0, null);
                        } else {
                            // 有行骗记录 - 解析骗子信息
                            String qq = fraudAccount.optString("qq", "");
                            String lastFraudTime = formatToLocalDate(fraudAccount.optString("lastFraudTime", ""));
                            int fraudCount = fraudAccount.optInt("fraudCount", 0);
                            int fraudAmountCents = fraudAccount.optInt("fraudAmount", 0);
                            double fraudAmount = convertAmountToYuan(fraudAmountCents);
                            int uncertainAmountCount = fraudAccount.optInt("uncertainAmountCount", 0);
                            String createTimeLocal = formatToLocalDate(fraudAccount.optString("createTime", ""));

                            // 解析受害人列表
                            List<VictimInfo> victims = new ArrayList<>();
                            JSONArray fraudRecordList = data.optJSONArray("fraudRecordList");
                            if (fraudRecordList != null && fraudRecordList.length() > 0) {
                                for (int i = 0; i < fraudRecordList.length(); i++) {
                                    JSONObject record = fraudRecordList.getJSONObject(i);
                                    String victimQq = record.optString("victimQq", "");
                                    if (victimQq.isEmpty()) {
                                        victimQq = "未知";
                                    }
                                    String platform = record.optString("platform", "");
                                    if (platform.isEmpty()) {
                                        platform = null;
                                    }
                                    String victimServer = record.optString("victimServer", "");
                                    if (victimServer.isEmpty()) {
                                        victimServer = null;
                                    }
                                    String time = formatToLocalDate(record.optString("time", ""));
                                    String remark = record.optString("remark", "");
                                    int amountStatus = record.optInt("amountStatus", 0);
                                    Double amount = null;
                                    if (amountStatus == 1 && record.has("fraudAmount")) {
                                        int amountCents = record.getInt("fraudAmount");
                                        amount = convertAmountToYuan(amountCents);
                                    }
                                    victims.add(new VictimInfo(victimQq, platform, victimServer, time, remark, amountStatus, amount));
                                }
                            }

                            // 注意：原 remark 字段（骗子行骗手段）接口已不再提供，置为空字符串
                            result = new FraudResult(true, qq, "", createTimeLocal, victims, fraudCount, fraudAmount, uncertainAmountCount, lastFraudTime);
                        }
                    }

                    mainHandler.post(() -> {
                        dialog.dismiss();
                        callback.onSuccess(result);
                    });
                }
            } catch (IOException e) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    callback.onError("网络异常，请检查网络连接或稍后重试。");
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    dialog.dismiss();
                    callback.onError("解析数据失败，请稍后重试。");
                });
            }
        });
    }

    public interface QueryCallback {
        void onSuccess(FraudResult result);
        void onError(String message);
    }

    /**
     * 受害人信息
     */
    public static class VictimInfo {
        public final String victim;          // 受害人QQ号
        public final String platform;        // 受害者所在平台，可能为 null
        public final String server;          // 受害者服务器，可能为 null
        public final String fraudTime;       // 被骗日期，格式 yyyy-MM-dd
        public final String remark;          // 被骗过程描述
        public final int amountStatus;       // 是否能确定受骗金额，1=能确定，0=不能确定
        public final Double amount;          // 受骗金额（元），仅当 amountStatus==1 时有值，否则为 null

        public VictimInfo(String victim, String platform, String server, String fraudTime,
                          String remark, int amountStatus, Double amount) {
            this.victim = victim;
            this.platform = platform;
            this.server = server;
            this.fraudTime = fraudTime;
            this.remark = remark;
            this.amountStatus = amountStatus;
            this.amount = amount;
        }
    }

    /**
     * 查询结果封装
     */
    public static class FraudResult {
        public final boolean isFraud;               // 是否为骗子
        public final String qq;                     // 骗子QQ号
        public final String remark;                 // 骗子备注（行骗手段），接口已不提供，固定为空
        public final String recordTime;             // 录入日期（转换后的本地日期，yyyy-MM-dd）
        public final List<VictimInfo> victims;      // 受害人列表，无记录时为 null 或空列表
        public final int fraudCount;                // 行骗次数
        public final double fraudAmount;            // 行骗总金额（元）
        public final int uncertainAmountCount;      // 不确定金额的被骗次数
        public final String lastFraudTime;          // 上一次行骗日期（yyyy-MM-dd）

        public FraudResult(boolean isFraud, String qq, String remark, String recordTime,
                           List<VictimInfo> victims, int fraudCount, double fraudAmount,
                           int uncertainAmountCount, String lastFraudTime) {
            this.isFraud = isFraud;
            this.qq = qq;
            this.remark = remark;
            this.recordTime = recordTime;
            this.victims = victims;
            this.fraudCount = fraudCount;
            this.fraudAmount = fraudAmount;
            this.uncertainAmountCount = uncertainAmountCount;
            this.lastFraudTime = lastFraudTime;
        }
    }
}