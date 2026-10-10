package com.careful.HyperFVM.utils.ForDesign.Blur;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogCornerRadiusUtil;

import fan.animation.Folme;
import fan.animation.base.AnimConfig;
import fan.animation.listener.TransitionListener;
import fan.animation.property.ViewProperty;
import fan.animation.utils.EaseManager;

public class DialogBackgroundBlurUtil {

    /** 遮罩淡入时长（对齐下拉菜单 DIM_MASK_FADE_IN_MS=300，与弹窗出现同步） */
    private static final long SCRIM_FADE_IN_MS = 300;
    /** 遮罩淡出时长（对齐下拉菜单 DIM_MASK_FADE_OUT_MS=150，关闭时平滑退场） */
    private static final long SCRIM_FADE_OUT_MS = 150;

    /**
     * 项目全部弹窗的统一入口：应用固定 46dp G2 squircle 圆角 + Miuix 规范窗口遮罩。
     * 遮罩为纯色压暗（背景模糊已按 Miuix 规范移除）：浅色 #4D000000 / 深色 #99000000，
     * 经 R.color.color_window_scrim 的 night 资源自适应，与下拉菜单遮罩同源同观感。
     * <p>
     * 遮罩为 view 层（挂宿主 Activity 的 content）而非窗口 dim：dismiss 时窗口
     * 立即销毁，dim 无从做退场动画、会瞬间消失；view 层则可在 OnDismissListener
     * 中平滑淡出 150ms 再移除，与下拉菜单的关闭观感一致。
     *
     * @param dialog 目标 Dialog（需先通过 Builder 创建但未 show()）
     */
    public static void setDialogBackgroundScrim(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        // 项目全部弹窗的统一入口：顺带应用固定 46dp 的 G2 squircle 圆角
        // （需在 show() 前调用：window 层同步生效，内容层待首次全局布局回调补齐）
        DialogCornerRadiusUtil.apply(dialog);

        final Activity activity = unwrapActivity(dialog.getContext());
        if (activity == null) {
            // 理论不可达（全部调用方 context 均为 Activity）：退回窗口 dim 兜底
            applyWindowDim(window, dialog);
            return;
        }

        // 遮罩挂在 content 上：随 Activity 销毁自动回收，且不遮挡系统栏图标
        final ViewGroup content = activity.findViewById(android.R.id.content);
        final View mask = new View(activity);
        // Miuix 窗口遮罩规范：浅色 #4D000000 / 深色 #99000000（night 资源自适应）
        mask.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_window_scrim));
        // 防止遮罩期间误触底下控件（对话框外部点击由其窗口的 OUTSIDE 事件关闭，不受影响）
        mask.setClickable(true);
        mask.setAlpha(0f);
        content.addView(mask, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fadeScrim(mask, 1f, SCRIM_FADE_IN_MS, null);

        // 关闭（按钮/back/外部点击均经 dismiss）→ 遮罩平滑淡出后移除。
        // 注意：会覆盖调用方已设置的 OnDismissListener（DialogBuilderManager 全部调用点均未设置）
        dialog.setOnDismissListener(d -> fadeScrim(mask, 0f, SCRIM_FADE_OUT_MS, () -> {
            if (mask.getParent() != null) {
                content.removeView(mask);
            }
        }));
    }

    /**
     * 遮罩 alpha 过渡：用 Folme 定时驱动而非 ViewPropertyAnimator/ValueAnimator——
     * 后者受开发者选项"Animator 时长缩放"影响，与下拉菜单遮罩（fadeMask）同理。
     *
     * @param onSettled 结束/取消回调（均可触发，保证幂等）；可为 null
     */
    private static void fadeScrim(View mask, float targetAlpha, long durationMs, Runnable onSettled) {
        AnimConfig config = new AnimConfig()
                .setEase(EaseManager.getStyle(1, (int) durationMs));
        if (onSettled != null) {
            config.addListeners(new TransitionListener() {
                @Override
                public void onComplete(Object o) {
                    onSettled.run();
                }

                @Override
                public void onCancel(Object o) {
                    onSettled.run();
                }
            });
        }
        Folme.use(mask).to(ViewProperty.ALPHA, targetAlpha, config);
    }

    /** 从 Dialog 的 context 逐层解包出宿主 Activity（ContextThemeWrapper → base）。 */
    private static Activity unwrapActivity(Context context) {
        for (int i = 0; i < 10 && context != null; i++) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                return null;
            }
        }
        return null;
    }

    /** 兜底：取不到宿主 Activity 时退回窗口 dim（关闭时遮罩瞬间消失，无退场动画）。 */
    private static void applyWindowDim(Window window, Dialog dialog) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0f); // 从全透明渐入到目标值

        // Miuix 规范：浅色 #4D000000（30% 黑）/ 深色 #99000000（60% 黑），按当前主题取值
        boolean night = (dialog.getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        float targetDim = night ? 0.60f : 0.30f;

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        @SuppressLint("Recycle") ValueAnimator animator = ValueAnimator.ofFloat(0f, targetDim);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            layoutParams.dimAmount = (float) animation.getAnimatedValue();
            window.setAttributes(layoutParams);
        });
        animator.start();
    }
}
