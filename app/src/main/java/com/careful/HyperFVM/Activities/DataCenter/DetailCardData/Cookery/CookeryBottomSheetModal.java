package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MiuixBottomSheetModal;

/**
 * 食神谱系列卡片的 Miuix BottomSheet 通用弹层（58 张卡共用，替换原
 * DialogFragment 打开路径，全项目推广）。
 *
 * <p>结构：固定标题 + vendored SpringBackLayout（嵌套回弹；经 NestedScrollingChild3
 * 把消费剩余量转发给弹层 BottomSheetBehavior 实现跟手）+ NestedScrollView 内容
 * （运行时 inflate 各卡布局 fragment_bottom_sheet_cookery_X_Y）。
 *
 * <p>标题处理：各卡布局模板统一（根首子元素 = 内嵌标题 TextView），show 时把它搬到
 * 脚手架的固定标题槽，保持 M3 版标题不随内容滚动的观感；判定不中时标题留原位（降级不崩）。
 *
 * <p>深浅色切换重建恢复：fan 弹层直接挂在 decorView，Activity recreate 即消失，
 * 无 Dialog/Fragment 自动恢复；模拟 M3 DialogFragment 行为——重建后重弹并恢复滚动位置。
 */
public final class CookeryBottomSheetModal {

    /** 卡片内容绑定回调（原各 CookeryBottomSheetFragment_X_Y#onViewCreated 的绑定逻辑） */
    public interface ContentBinder {
        void bind(View content);
    }

    /* ===== 深浅色切换重建恢复（重建后凭以下静态状态重弹并恢复滚动位置） ===== */
    private static Class<?> sHostClass;
    private static Activity sActiveHost;
    private static MiuixBottomSheetModal sActiveModal;
    private static View sActiveContent;
    private static boolean sPendingRestore;
    private static int sRestoreScrollY;
    private static int sRestoreLayoutRes;
    private static ContentBinder sRestoreBinder;
    private static boolean sLifecycleRegistered;

    private CookeryBottomSheetModal() {
    }

    /**
     * 打开一张食神谱卡的 Miuix 弹层。
     *
     * @param context   上下文（现有两个宿主的 view context，内部解包取 Activity）
     * @param layoutRes 各卡布局（fragment_bottom_sheet_cookery_X_Y）
     * @param binder    内容绑定（原 fragment 的 onViewCreated 逻辑）
     */
    public static void show(Context context, @LayoutRes int layoutRes, ContentBinder binder) {
        Activity activity = unwrapActivity(context);
        if (activity == null) {
            return; // 取不到 Activity（异常宿主），静默忽略
        }
        // 防重入：弹层模态遮挡下连点几乎不可能，仅防关闭动画窗口内的二次触发
        if (sActiveModal != null && sActiveModal.isShowing()) {
            return;
        }

        MiuixBottomSheetModal modal = new MiuixBottomSheetModal(activity);
        modal.setContentView(R.layout.bottom_sheet_cookery_miuix);
        View scaffold = modal.getRootView();

        // 运行时注入各卡布局，并把内嵌标题搬到固定标题槽（模板判定，见类注释）
        FrameLayout container = scaffold.findViewById(R.id.miuix_cookery_content);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View content = inflater.inflate(layoutRes, container, false);
        container.addView(content);
        moveInlineTitleToSlot(content, scaffold.findViewById(R.id.miuix_cookery_sheet_title_slot));
        binder.bind(content);
        modal.show();

        // 记录活跃弹层 + 关闭动作（重建/关闭的状态分流）
        sHostClass = activity.getClass();
        sActiveHost = activity;
        sActiveModal = modal;
        sActiveContent = scaffold;
        sRestoreLayoutRes = layoutRes;
        sRestoreBinder = binder;
        modal.setDismissAction(() -> {
            if (sActiveHost != null && sActiveHost.isChangingConfigurations()) {
                // Activity 正在重建（深浅色切换等）：保存滚动位置等重建后重弹
                captureScrollY();
                sPendingRestore = true;
            } else {
                // 用户主动关闭：下次打开从头开始
                sPendingRestore = false;
                sRestoreScrollY = 0;
            }
            clearActive();
        });
        registerLifecycle(activity.getApplication());

        // 重建重弹后恢复切换前的滚动位置
        if (sRestoreScrollY > 0) {
            final int scrollY = sRestoreScrollY;
            sRestoreScrollY = 0;
            scaffold.post(() -> {
                View scroll = scaffold.findViewById(R.id.miuix_cookery_scroll);
                if (scroll != null) {
                    scroll.scrollTo(0, scrollY);
                }
            });
        }
    }

    /**
     * 把各卡布局根的内嵌标题（根首子元素，模板统一为 60dp 标题 TextView）搬到脚手架
     * 固定标题槽；判定不中（非 TextView 首子元素）时留原位，仅影响观感不崩。
     */
    private static void moveInlineTitleToSlot(View content, ViewGroup titleSlot) {
        if (titleSlot == null || !(content instanceof ViewGroup)) {
            return;
        }
        ViewGroup root = (ViewGroup) content;
        if (root.getChildCount() == 0 || !(root.getChildAt(0) instanceof TextView)) {
            return;
        }
        View title = root.getChildAt(0);
        root.removeViewAt(0);
        titleSlot.addView(title);
    }

    /** 从任意 context 解包出 Activity（view context 可能是 ContextThemeWrapper 包装） */
    private static Activity unwrapActivity(Context context) {
        Context current = context;
        while (current != null) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = (current instanceof ContextWrapper)
                    ? ((ContextWrapper) current).getBaseContext() : null;
        }
        return null;
    }

    private static void captureScrollY() {
        if (sActiveContent != null) {
            View scroll = sActiveContent.findViewById(R.id.miuix_cookery_scroll);
            if (scroll != null) {
                sRestoreScrollY = scroll.getScrollY();
            }
        }
    }

    private static void clearActive() {
        sActiveHost = null;
        sActiveModal = null;
        sActiveContent = null;
    }

    private static void registerLifecycle(Application app) {
        if (sLifecycleRegistered) {
            return;
        }
        sLifecycleRegistered = true;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreDestroyed(Activity activity) {
                // fan 弹层若未在重建时回调 dismiss，此处兜底（用户已关时 active 已清，不会误触）
                if (sActiveModal != null && activity == sActiveHost && activity.isChangingConfigurations()) {
                    captureScrollY();
                    sPendingRestore = true;
                    clearActive();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (sPendingRestore && sHostClass != null && activity.getClass() == sHostClass
                        && sRestoreLayoutRes != 0 && sRestoreBinder != null) {
                    sPendingRestore = false;
                    // 首帧后再弹，避免重建初始化与弹出动画叠加
                    activity.getWindow().getDecorView().post(
                            () -> show(activity, sRestoreLayoutRes, sRestoreBinder));
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
        });
    }
}
