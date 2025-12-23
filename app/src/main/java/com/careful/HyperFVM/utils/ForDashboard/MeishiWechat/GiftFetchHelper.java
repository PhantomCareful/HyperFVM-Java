package com.careful.HyperFVM.utils.ForDashboard.MeishiWechat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GiftFetchHelper {
    private static final String TAG = "GiftFetchHelper";
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 领取结果回调接口
    public interface GiftFetchCallback {
        void onResult(int successCount); // 成功领取的账号数量
        void onError(); // 整体失败（如服务器问题）
    }

    // 批量领取礼包
    public void fetchAllGifts(Context context, List<String> openids, GiftFetchCallback callback) {
        executor.execute(() -> {
            int successCount = 0;
            boolean isServerError = false;

            for (String openid : openids) {
                String url = "http://meishi.wechat.123u.com/meishi/gift?openid=" + openid;
                try {
                    Response response = client.newCall(new Request.Builder().url(url).build()).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        int ret = jsonObject.getInt("ret");

                        // 两种成功情况：ret=0（未领取）或ret=1（已领取）
                        if (ret == 0 || ret == 1) {
                            successCount++;
                            Log.d(TAG, "openid=" + openid + " 领取成功");
                        } else {
                            isServerError = true;
                            break;
                        }
                    } else {
                        isServerError = true;
                        break;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    isServerError = true;
                    break;
                }
            }

            // 回调结果到主线程
            boolean finalIsServerError = isServerError;
            int finalSuccessCount = successCount;
            mainHandler.post(() -> {
                if (finalIsServerError) {
                    callback.onError();
                } else {
                    callback.onResult(finalSuccessCount);
                }
            });
        });
    }
}
