package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

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

        Toast.makeText(context, "查询中⏳⏳⏳", Toast.LENGTH_SHORT).show();

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
                        result = new FraudResult(false, "", "", null, 0, "", 0, null);
                    } else {
                        int status = data.optInt("status", -1);
                        JSONObject fraudAccount = data.optJSONObject("fraudAccount");
                        if (status != 1 || fraudAccount == null) {
                            // 无行骗记录
                            result = new FraudResult(false, "", "", null, 0, "", 0, null);
                        } else {
                            // 有行骗记录 - 解析骗子信息
                            String qq = fraudAccount.optString("qq", "");
                            String lastFraudTime = formatToLocalDate(fraudAccount.optString("lastFraudTime", ""));
                            int fraudCount = fraudAccount.optInt("fraudCount", 0);
                            double fraudAmountCents = convertAmountToYuan(fraudAccount.optInt("fraudAmount", 0));
                            String fraudAmount = fraudAmountCents == 0 ? "未知" : fraudAmountCents + "元";
                            int uncertainAmountCount = fraudAccount.optInt("uncertainAmountCount", 0);
                            String createTimeLocal = formatToLocalDate(fraudAccount.optString("createTime", ""));

                            // 解析受害人列表
                            List<VictimInfo> victims = new ArrayList<>();
                            JSONArray fraudRecordList = data.optJSONArray("fraudRecordList");
                            if (fraudRecordList != null && fraudRecordList.length() > 0) {
                                for (int i = 0; i < fraudRecordList.length(); i++) {
                                    JSONObject record = fraudRecordList.getJSONObject(i);
                                    String victimQQ = record.optString("victimQq", "");
                                    if (victimQQ.isEmpty() || victimQQ.equals("null")) {
                                        victimQQ = "未知";
                                    }
                                    String platform = record.optString("platform", "");
                                    if (platform.isEmpty() || platform.equals("null")) {
                                        platform = "未知";
                                    }
                                    String victimServer = record.optString("victimServer", "");
                                    if (victimServer.isEmpty() || victimServer.equals("null")) {
                                        victimServer = "未知";
                                    }
                                    String time = formatToLocalDate(record.optString("time", ""));
                                    String remark = record.optString("remark", "");
                                    double amountCents = convertAmountToYuan(record.getInt("fraudAmount"));
                                    String amount = amountCents == 0 ? "未知" : amountCents + "元";
                                    victims.add(new VictimInfo(victimQQ, platform, victimServer, time, remark, amount));
                                }
                            }

                            result = new FraudResult(true, qq, createTimeLocal, victims, fraudCount, fraudAmount, uncertainAmountCount, lastFraudTime);
                        }
                    }

                    mainHandler.post(() -> callback.onSuccess(result));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("网络异常，请检查网络连接或稍后重试。"));
            } catch (JSONException e) {
                mainHandler.post(() -> callback.onError("解析数据失败，请稍后重试。"));
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
    public static class VictimInfo implements Parcelable {
        public final String victim;
        public final String platform;
        public final String server;
        public final String fraudTime;
        public final String remark;
        public final String amount;

        public VictimInfo(String victim, String platform, String server, String fraudTime,
                          String remark, String amount) {
            this.victim = victim;
            this.platform = platform;
            this.server = server;
            this.fraudTime = fraudTime;
            this.remark = remark;
            this.amount = amount;
        }

        protected VictimInfo(Parcel in) {
            victim = in.readString();
            platform = in.readString();
            server = in.readString();
            fraudTime = in.readString();
            remark = in.readString();
            amount = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(victim);
            dest.writeString(platform);
            dest.writeString(server);
            dest.writeString(fraudTime);
            dest.writeString(remark);
            dest.writeString(amount);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<VictimInfo> CREATOR = new Creator<>() {
            @Override
            public VictimInfo createFromParcel(Parcel in) {
                return new VictimInfo(in);
            }

            @Override
            public VictimInfo[] newArray(int size) {
                return new VictimInfo[size];
            }
        };
    }

    /**
     * 骗子信息
     */
    public static class FraudResult implements Parcelable {
        public final boolean isFraud;
        public final String qq;
        public final String recordTime;
        public final List<VictimInfo> victims;
        public final int fraudCount;
        public final String fraudAmount;
        public final int uncertainAmountCount;
        public final String lastFraudTime;

        public FraudResult(boolean isFraud, String qq, String recordTime,
                           List<VictimInfo> victims, int fraudCount, String fraudAmount,
                           int uncertainAmountCount, String lastFraudTime) {
            this.isFraud = isFraud;
            this.qq = qq;
            this.recordTime = recordTime;
            this.victims = victims;
            this.fraudCount = fraudCount;
            this.fraudAmount = fraudAmount;
            this.uncertainAmountCount = uncertainAmountCount;
            this.lastFraudTime = lastFraudTime;
        }

        protected FraudResult(Parcel in) {
            isFraud = in.readByte() != 0;
            qq = in.readString();
            recordTime = in.readString();
            victims = in.readArrayList(VictimInfo.class.getClassLoader());
            fraudCount = in.readInt();
            fraudAmount = in.readString();
            uncertainAmountCount = in.readInt();
            lastFraudTime = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isFraud ? 1 : 0));
            dest.writeString(qq);
            dest.writeString(recordTime);
            dest.writeList(victims);
            dest.writeInt(fraudCount);
            dest.writeString(fraudAmount);
            dest.writeInt(uncertainAmountCount);
            dest.writeString(lastFraudTime);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<FraudResult> CREATOR = new Creator<>() {
            @Override
            public FraudResult createFromParcel(Parcel in) {
                return new FraudResult(in);
            }

            @Override
            public FraudResult[] newArray(int size) {
                return new FraudResult[size];
            }
        };
    }
}