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
 */
public class IcuHelper {
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String API_URL = "https://www.msdzls.icu/fraud/getFraudDetail?qq=";
    private final OkHttpClient client = new OkHttpClient.Builder().build();

    public IcuHelper(Context context) {
        this.context = context;
    }

    /**
     * 将 API 返回的 ISO 8601 时间（UTC）转换为设备当前时区的日期字符串
     * @param createTime 格式如 "2026-05-29T02:56:30.000+00:00"
     * @return 设备本地时区日期，格式 "yyyy-MM-dd"，解析失败返回原字符串或空
     */
    private String formatCreateTimeToLocalDate(String createTime) {
        if (createTime == null || createTime.isEmpty()) {
            return "";
        }
        try {
            // 解析 ISO 8601 格式（支持毫秒和时区偏移）
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
            Date date = isoFormat.parse(createTime);
            // 转换为设备本地时区的日期格式
            SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            localDateFormat.setTimeZone(TimeZone.getDefault());
            return localDateFormat.format(Objects.requireNonNull(date));
        } catch (ParseException e) {
            // 解析失败时返回原始字符串（或空字符串）
            return createTime;
        }
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

        // 显示加载对话框
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle("查询中")
                .setMessage("正在验证QQ号信息，请稍候...")
                .setCancelable(false);
        Dialog dialog = dialogBuilder.create();

        // 添加背景模糊
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
                        // 无行骗记录
                        result = new FraudResult(false, "", "", "", null);
                    } else {
                        // 有行骗记录
                        String qq = data.optString("qq", "");
                        String remark = data.optString("remark", "");
                        String createTimeRaw = data.optString("createTime", "");
                        String createTimeLocalDate = formatCreateTimeToLocalDate(createTimeRaw);
                        List<VictimInfo> victims = new ArrayList<>();
                        JSONArray records = data.optJSONArray("records");
                        if (records != null && records.length() > 0) {
                            for (int i = 0; i < records.length(); i++) {
                                JSONObject record = records.getJSONObject(i);
                                String victim = record.optString("victim", "");
                                if (victim.isEmpty()) {
                                    victim = "未知";
                                }
                                String fraudTime = record.optString("fraudTime", "");
                                String recordRemark = record.optString("remark", "");

                                victims.add(new VictimInfo(victim, fraudTime, recordRemark));
                            }
                        }
                        result = new FraudResult(true, qq, remark, createTimeLocalDate, victims);
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
        public final String victim;      // 受害人QQ号
        public final String fraudTime;   // 被骗日期，格式 yyyy-MM-dd
        public final String remark;      // 被骗过程描述

        public VictimInfo(String victim, String fraudTime, String remark) {
            this.victim = victim;
            this.fraudTime = fraudTime;
            this.remark = remark;
        }
    }

    /**
     * 查询结果封装
     */
    public static class FraudResult {
        public final boolean isFraud;          // 是否为骗子
        public final String qq;                // 骗子QQ号
        public final String remark;            // 骗子备注（行骗手段）
        public final String recordTime;        // 录入日期（转换后的本地日期，yyyy-MM-dd）
        public final List<VictimInfo> victims; // 受害人列表，无记录时为 null 或空列表

        public FraudResult(boolean isFraud, String qq, String remark, String recordTime, List<VictimInfo> victims) {
            this.isFraud = isFraud;
            this.qq = qq;
            this.remark = remark;
            this.recordTime = recordTime;
            this.victims = victims;
        }
    }
}