package com.careful.HyperFVM.utils.ForDashboard.FertilizationTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FertilizationTask {
    private static String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/newtask.xml";
    private final OkHttpClient client;
    private final DBHelper dbHelper;
    private final Handler mainHandler;

    public FertilizationTask(Context context) {
        this.dbHelper = new DBHelper(context);
        // 禁用OkHttp缓存
        this.client = new OkHttpClient.Builder()
                .cache(null)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void execute() {
        fetchXmlContent();
    }

    private void fetchXmlContent() {
        Random random = new Random();
        int num = 1 + random.nextInt(1000000000);

        XML_URL = XML_URL + "?" + num;

        Request request = new Request.Builder()
                .url(XML_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络请求失败处理
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String xmlContent = response.body().string();
                    parseXmlAndSave(xmlContent);
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void parseXmlAndSave(String xmlContent) {
        Pattern pattern = Pattern.compile(
                "<task id=\"39\".*?startTime=\"(\\d+)\".*?endTime=\"(\\d+)\".*?</task>",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(xmlContent);

        if (matcher.find()) {
            long startTimeSec = Long.parseLong(matcher.group(1));
            long endTimeSec = Long.parseLong(matcher.group(2));
            String startDate = convertTimestamp(startTimeSec);
            String endDate = convertTimestamp(endTimeSec);
            int dayOfTask = calculateDayOfTask(startTimeSec, endTimeSec);

            mainHandler.post(() -> {
                String content;
                if (dayOfTask == -1) {
                    content = "本轮施肥活动已结束，请等待新的活动开始⏳";
                    dbHelper.updateDashboardContent("fertilization_task_notification", "暂无⏳");
                } else if (dayOfTask == 0) {
                    content = "活动尚未开始，请耐心等待⏳";
                    dbHelper.updateDashboardContent("fertilization_task_notification", "暂无⏳");
                } else {
                    content = String.format("开始：%s\n结束：%s\n进度：(%d/21)✊", startDate, endDate, dayOfTask);
                    dbHelper.updateDashboardContent("fertilization_task_notification", "(" + dayOfTask + "/21)✊");
                }
                dbHelper.updateDashboardContent("fertilization_task", content);
            });
        }
    }

    private String convertTimestamp(long timestampSec) {
        long timestampMs = timestampSec * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日", Locale.CHINA);
        return sdf.format(new Date(timestampMs));
    }

    // 按日期（0点为界）计算天数
    private int calculateDayOfTask(long startTimeSec, long endTimeSec) {
        // 将时间戳转换为"当天0点"的时间戳
        long startDateSec = getZeroTimeStamp(startTimeSec);
        long endDateSec = getZeroTimeStamp(endTimeSec);
        long currentDateSec = getZeroTimeStamp(System.currentTimeMillis() / 1000);

        if (currentDateSec < startDateSec) {
            return 0; // 未开始（当前日期早于活动开始日期）
        } else if (currentDateSec > endDateSec) {
            return -1; // 已结束（当前日期晚于活动结束日期）
        }

        // 计算间隔天数（起始日为第1天）
        return (int) ((currentDateSec - startDateSec) / 86400 + 1);
    }

    // 辅助方法：将任意时间戳转换为当天0点的时间戳（秒）
    private long getZeroTimeStamp(long timestampSec) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampSec * 1000);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() / 1000;
    }
}
