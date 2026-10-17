package com.careful.HyperFVM.utils.ForDownload;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import com.careful.HyperFVM.Activities.ApkInstallTrampolineActivity;
import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImageIndexActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Service.DownloadService;

import java.util.Collections;

/**
 * 下载通知构建器。
 * Android 16+（API 36）使用 Notification.ProgressStyle 构建 Live Update 实时通知
 * （promoted ongoing：置顶于通知栏、锁屏与状态栏胶囊展示进度）；
 * API 31-35 降级为经典 setProgress 进度条通知。
 * 进行中的通知带操作按钮：图片任务为「暂停/继续」「取消」，升级包任务为「停止升级」；
 * 完成后的通知为 Live Update 实时通知（promoted ongoing，不带进度条），可由对应页面打开时撤下
 */
public class DownloadNotificationHelper {
    public static final String CHANNEL_ID = "download_progress_channel";
    // 结果通知单独渠道：进度渠道是无声的（高频更新不打扰），完成/失败需要提示音+悬浮弹出让用户知晓
    public static final String RESULT_CHANNEL_ID = "download_result_channel";

    // 图片任务与APK任务各占一个通知ID，互不覆盖
    public static final int NOTIF_ID_IMAGES = 2101;
    public static final int NOTIF_ID_APK = 2102;

    // promoted ongoing（Live Update）请求的 extras 键。
    // Notification.EXTRA_REQUEST_PROMOTED_ONGOING 是 API 36.1 才加入的字段，直接引用会触发
    // NewApi lint（min 31）；其值为公开契约字符串，编译期内联、运行时无任何 API 依赖，
    // Android 16.0（36.0）起系统即按此键值识别 promoted 请求
    private static final String EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing";

    private DownloadNotificationHelper() {
    }

    /**
     * 创建（幂等）下载通知渠道：高重要性、无声音、不显示角标
     */
    public static void ensureChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "下载进度通道",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("数据图资源与升级包的下载进度");
        channel.setSound(null, null);
        channel.setShowBadge(false);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 创建（幂等）下载结果通知渠道：高重要性 + 默认提示音，完成/失败通知以悬浮通知弹出
     */
    public static void ensureResultChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                RESULT_CHANNEL_ID,
                "下载完成提醒",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("数据图更新完成与升级包下载完成的提醒");
        channel.setShowBadge(false);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 数据图批量下载的进行中通知
     *
     * @param completedCount 已完成张数
     * @param totalCount    本批总张数
     * @param percent       总体进度百分比（0-100）
     * @param paused        是否处于暂停态
     */
    public static Notification buildImagesProgress(Context context, int completedCount, int totalCount, int percent, boolean paused) {
        return buildImagesProgress(context, completedCount, totalCount, percent, paused, false);
    }

    /**
     * @param rangeRestarted 服务器不支持断点续传、恢复后已从头重新下载的说明态（解释进度为何回落）
     */
    public static Notification buildImagesProgress(Context context, int completedCount, int totalCount, int percent,
                                                   boolean paused, boolean rangeRestarted) {
        // 标题（含Live Update状态栏胶囊展示的主文本）带进度百分比
        String title = paused
                ? "⏸ 数据图资源下载已暂停 " + percent + "%"
                : "⏳ 数据图资源下载中 " + percent + "%";
        String text;
        if (rangeRestarted) {
            text = paused
                    ? "服务器不支持续传，点击继续将从头下载"
                    : "已完成 " + completedCount + "/" + totalCount + "，不支持续传已从头下载";
        } else {
            text = paused
                    ? "已完成 " + completedCount + "/" + totalCount + "，点击继续恢复下载"
                    : "已完成 " + completedCount + "/" + totalCount;
        }
        return buildProgressNotification(context, DownloadStateStore.TASK_IMAGES,
                title, text, percent, paused,
                openPageIntent(context, DataImageIndexActivity.class));
    }

    /**
     * APK升级包下载的进行中通知。
     * 升级包下载源不支持断点续传，暂停后继续只会从头重下，因此不提供暂停/继续，
     * 操作按钮只有「停止升级」（终止下载并删除已下载部分，通知随之撤下）
     *
     * @param rangeRestarted 服务器不支持断点续传、已从头重新下载的说明态（解释进度为何回落）
     */
    public static Notification buildApkProgress(Context context, int percent, boolean rangeRestarted) {
        // 标题（含Live Update状态栏胶囊展示的主文本）带进度百分比
        String title = "⏳ App升级包下载中 " + percent + "%";
        String text = rangeRestarted
                ? "服务器不支持断点续传，已从头重新下载"
                : "已完成 " + percent + "%";
        // 下载中点击通知回到检查更新页
        return buildProgressNotification(context, DownloadStateStore.TASK_APK,
                title, text, percent, false,
                openPageIntent(context, CheckUpdateActivity.class));
    }

    /**
     * 数据图批量下载的结果通知（全部成功/部分失败）。
     * API 36+ 以 Live Update 完成态展示（进度满格 + promoted 置顶），点击打开数据图主页
     */
    public static Notification buildImagesResult(Context context, int successCount, int failCount) {
        boolean allSuccess = failCount == 0;
        Notification.Builder builder = buildResultNotificationBuilder(context,
                allSuccess ? "数据图资源更新完成" : "数据图资源更新完成（部分失败）",
                successCount + "张图片更新成功，" + failCount + "张图片更新失败",
                openPageIntent(context, DataImageIndexActivity.class),
                true);
        if (!allSuccess) {
            // 部分失败：通知栏直达重试入口（页面收到extra后弹确认框重新下载失败清单）
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.app_icon_tile),
                    "部分失败",
                    retryFailedImagesPendingIntent(context)).build());
        }
        return builder.build();
    }

    /**
     * APK下载完成的通知（Live Update 实时通知）：点击经无界面中转页直达系统安装器
     * （不跳转检查更新页）；promoted ongoing 不可滑除，由中转页在点击时撤下
     */
    public static Notification buildApkResult(Context context) {
        return buildResultNotificationBuilder(context,
                "App升级包下载完成", "点击安装",
                installTrampolineIntent(context), true).build();
    }

    /**
     * APK下载失败的通知：点击回到检查更新页重试
     */
    public static Notification buildApkFailed(Context context) {
        return buildResultNotificationBuilder(context,
                "App升级包下载失败", "点击回到检查更新页重试",
                openPageIntent(context, CheckUpdateActivity.class), false).build();
    }

    /**
     * 构建带进度条与操作按钮的进行中通知
     */
    private static Notification buildProgressNotification(Context context, int taskType, String title, String text,
                                                          int percent, boolean paused, PendingIntent contentIntent) {
        ensureChannel(context);

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_tile)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // Android 16+ Live Update：ProgressStyle 进度模板 + promoted ongoing（置顶/锁屏展示）。
            // progressMax 取所有 Segment 长度之和，这里用单段100表示百分比刻度。
            // 注意：Builder#setRequestPromotedOngoing 是 36.1（BAKLAVA_1）才加入的 API，
            // Android 16.0（SDK_INT_FULL=36.0）设备调用会 NoSuchMethodError；
            // 改用 extras 直接声明，36.0 即生效（磁贴任务通知已实证可 promoted）
            Notification.ProgressStyle progressStyle = new Notification.ProgressStyle()
                    .setStyledByProgress(true)
                    .setProgressSegments(Collections.singletonList(new Notification.ProgressStyle.Segment(100)))
                    .setProgress(Math.max(0, Math.min(100, percent)));
            builder.setStyle(progressStyle)
                    .addExtras(requestPromotedExtras())
                    .setShortCriticalText(paused ? "已暂停" : percent + "%");
        } else {
            // API 31-35 降级：经典进度条通知
            builder.setProgress(100, Math.max(0, Math.min(100, percent)), false);
        }

        // 操作按钮（PendingIntent 指向下载服务）：图片任务提供「暂停/继续」+「取消」；
        // 升级包下载源不支持断点续传，暂停后继续只会从头重下，故只提供「停止升级」
        if (taskType == DownloadStateStore.TASK_APK) {
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.app_icon_tile),
                    "停止升级",
                    servicePendingIntent(context, DownloadService.ACTION_CANCEL, taskType, taskType * 10 + 2)).build());
        } else {
            String pauseResumeLabel = paused ? "继续" : "暂停";
            String pauseResumeAction = paused ? DownloadService.ACTION_RESUME : DownloadService.ACTION_PAUSE;
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.app_icon_tile),
                    pauseResumeLabel,
                    servicePendingIntent(context, pauseResumeAction, taskType, taskType * 10 + 1)).build());
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.app_icon_tile),
                    "取消",
                    servicePendingIntent(context, DownloadService.ACTION_CANCEL, taskType, taskType * 10 + 2)).build());
        }

        return builder.build();
    }

    /**
     * 构建结果通知 Builder（结果渠道：HIGH + 提示音，完成/失败以悬浮通知弹出）。
     * liveUpdate=true 且 API 36+ 时以 Live Update 实时通知展示：promoted ongoing 置顶于
     * 通知栏、锁屏与状态栏胶囊（不带进度条，完成态无进度可言）；
     * promoted 要求 ongoing 不可滑除，由对应页面打开时撤下；其余情况为普通可滑除通知（展开详情）
     */
    private static Notification.Builder buildResultNotificationBuilder(Context context, String title, String text,
                                                                       PendingIntent contentIntent, boolean liveUpdate) {
        ensureResultChannel(context);
        Notification.Builder builder = new Notification.Builder(context, RESULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_tile)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        if (liveUpdate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // Live Update 实时通知形态：promoted ongoing 置顶/锁屏/状态栏胶囊展示（不带进度条）。
            // promoted 请求用 extras 方式（见 buildProgressNotification 内注释）；
            // 官方要求 promoted 通知必须是标准样式，补 BigTextStyle
            builder.setStyle(new Notification.BigTextStyle().bigText(text))
                    .addExtras(requestPromotedExtras())
                    .setShortCriticalText("已完成")
                    .setOngoing(true);
        } else {
            // 普通通知：展开详情 + 可滑除、点击消失
            builder.setStyle(new Notification.BigTextStyle().bigText(text))
                    .setOngoing(false)
                    .setAutoCancel(true);
        }
        return builder;
    }

    /**
     * promoted ongoing（Live Update 实时通知）请求 extras。
     * 不直接引用 Notification.EXTRA_REQUEST_PROMOTED_ONGOING（36.1 字段会触发 NewApi lint），
     * 改用本地常量保存其契约字符串：编译期内联、系统只读 extras 键值，Android 16.0 即生效
     */
    private static Bundle requestPromotedExtras() {
        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        return extras;
    }

    /**
     * 指向下载服务的命令 PendingIntent（不同 action/任务类型互不影响）
     */
    private static PendingIntent servicePendingIntent(Context context, String action, int taskType, int requestCode) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(action);
        intent.putExtra(DownloadService.EXTRA_TASK_TYPE, taskType);
        return PendingIntent.getService(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * 打开指定页面的通知点击 PendingIntent：直接指向 Activity——Android 12+ 的
     * notification trampoline 政策禁止经 broadcast/service 中转 startActivity（会被系统静默阻止，
     * 表现为点击通知后App不跳转不前置）；CLEAR_TOP|SINGLE_TOP 复用栈内已有实例避免重复Activity
     */
    private static PendingIntent openPageIntent(Context context, Class<?> pageClass) {
        Intent intent = new Intent(context, pageClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, (int) System.nanoTime(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * 「重新下载失败的图片」action：打开数据图主页并携带重试标记
     */
    private static PendingIntent retryFailedImagesPendingIntent(Context context) {
        Intent intent = new Intent(context, DataImageIndexActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(DataImageIndexActivity.EXTRA_RETRY_FAILED_DOWNLOAD, true);
        return PendingIntent.getActivity(context, (int) (System.nanoTime() >> 2), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * APK完成通知的点击 PendingIntent：指向无界面中转页（蹦床Activity），
     * 由它撤下通知并直接拉起系统安装器后finish——既满足promoted通知需由本App组件
     * 点击撤下的要求，又实现点击直达安装器、不跳转检查更新页。
     * 不能直接指向系统安装器（否则通知永远撤不下）；Android 12+ trampoline政策
     * 只禁止service/broadcast中转，Activity中转是官方认可的迁移方案
     */
    private static PendingIntent installTrampolineIntent(Context context) {
        Intent intent = new Intent(context, ApkInstallTrampolineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, (int) (System.nanoTime() >> 1), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
