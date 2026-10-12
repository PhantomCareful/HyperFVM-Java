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
import androidx.annotation.NonNull;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.BottomSheet.MiuixBottomSheetModal;

import java.util.HashMap;
import java.util.Map;

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
 * <p>多宿主并存：每个宿主 Activity 各自至多一层弹层（静态状态按宿主分槽），跨页互
 * 不干扰——图鉴页开层后点卡片进详情页（旧层保留），详情页开自己的新层，返回图鉴页
 * 时旧层依然存在。
 *
 * <p>深浅色切换重建恢复：fan 弹层直接挂在 decorView，Activity recreate 即消失，
 * 无 Dialog/Fragment 自动恢复；模拟 M3 DialogFragment 行为——重建后重弹并恢复滚动位置。
 */
public final class CookeryBottomSheetModal {

    /** 卡片内容绑定回调（原各 CookeryBottomSheetFragment_X_Y#onViewCreated 的绑定逻辑） */
    public interface ContentBinder {
        void bind(View content);
    }

    /* ===== 多宿主并存 + 深浅色切换重建恢复（静态状态按宿主分槽） ===== */
    /** 当前各宿主持有的活跃弹层（同宿主至多一层；不同宿主互不拦截、互不关闭） */
    @SuppressLint("StaticFieldLeak")
    private static final Map<Activity, MiuixBottomSheetModal> sActiveByHost = new HashMap<>();
    /** 按宿主类记录的重建恢复状态（重建后 Activity 实例已换，只能按 class 匹配） */
    private static final Map<Class<?>, RestoreState> sRestoreByHostClass = new HashMap<>();
    private static boolean sLifecycleRegistered;

    /** 单个宿主的弹层恢复状态（关闭/重建的状态分流 + 重弹所需信息） */
    private static final class RestoreState {
        boolean pendingRestore;
        int restoreScrollY;
        @LayoutRes
        int restoreLayoutRes;
        ContentBinder restoreBinder;
    }

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
        // 防重入：同宿主已有弹层时忽略（模态遮挡下连点几乎不可能，仅防关闭动画
        // 窗口内的二次触发）；不同宿主各自一槽——不拦截也不关闭对方的弹层
        MiuixBottomSheetModal existing = sActiveByHost.get(activity);
        if (existing != null && existing.isShowing()) {
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
        sActiveByHost.put(activity, modal);
        RestoreState restore = restoreStateFor(activity.getClass());
        restore.restoreLayoutRes = layoutRes;
        restore.restoreBinder = binder;
        restore.pendingRestore = false;
        modal.setDismissAction(() -> {
            if (sActiveByHost.get(activity) != modal) {
                return; // 该槽已被清理（宿主销毁兜底已处理），避免重复分流状态
            }
            sActiveByHost.remove(activity);
            if (activity.isChangingConfigurations()) {
                // Activity 正在重建（深浅色切换等）：保存滚动位置等重建后重弹
                captureScrollY(modal, restore);
                restore.pendingRestore = true;
            } else {
                // 用户主动关闭：下次打开从头开始
                restore.pendingRestore = false;
                restore.restoreScrollY = 0;
            }
        });
        registerLifecycle(activity.getApplication());

        // 重建重弹后恢复切换前的滚动位置
        if (restore.restoreScrollY > 0) {
            final int scrollY = restore.restoreScrollY;
            restore.restoreScrollY = 0;
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
        if (titleSlot == null || !(content instanceof ViewGroup root)) {
            return;
        }
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

    /** 取（无则创建）某宿主类的重建恢复状态 */
    private static RestoreState restoreStateFor(Class<?> hostClass) {
        return sRestoreByHostClass.computeIfAbsent(hostClass, k -> new RestoreState());
    }

    private static void captureScrollY(MiuixBottomSheetModal modal, RestoreState restore) {
        View scroll = modal.getRootView().findViewById(R.id.miuix_cookery_scroll);
        if (scroll != null) {
            restore.restoreScrollY = scroll.getScrollY();
        }
    }

    private static void registerLifecycle(Application app) {
        if (sLifecycleRegistered) {
            return;
        }
        sLifecycleRegistered = true;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreDestroyed(@NonNull Activity activity) {
                // fan 弹层若未在重建时回调 dismiss，此处兜底（用户已关时槽已清，不会误触）
                MiuixBottomSheetModal modal = sActiveByHost.get(activity);
                if (modal != null && activity.isChangingConfigurations()) {
                    RestoreState restore = restoreStateFor(activity.getClass());
                    captureScrollY(modal, restore);
                    restore.pendingRestore = true;
                    sActiveByHost.remove(activity);
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                RestoreState restore = sRestoreByHostClass.get(activity.getClass());
                if (restore != null && restore.pendingRestore
                        && restore.restoreLayoutRes != 0 && restore.restoreBinder != null) {
                    restore.pendingRestore = false;
                    final int layoutRes = restore.restoreLayoutRes;
                    final ContentBinder binder = restore.restoreBinder;
                    // 首帧后再弹，避免重建初始化与弹出动画叠加
                    activity.getWindow().getDecorView().post(
                            () -> show(activity, layoutRes, binder));
                }
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                MiuixBottomSheetModal modal = sActiveByHost.remove(activity);
                if (activity.isChangingConfigurations()) {
                    return; // 重建：恢复状态已由 PreDestroyed/dismiss 分流，留给 resumed 重弹
                }
                // 正常销毁（finish）：关掉仍挂着的弹层并清恢复状态，避免 WindowLeaked
                // 与静态残留（否则同一入口下次进入会被旧弹层拦截）
                if (modal != null && modal.isShowing()) {
                    modal.dismiss();
                }
                sRestoreByHostClass.remove(activity.getClass());
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }
        });
    }
}
