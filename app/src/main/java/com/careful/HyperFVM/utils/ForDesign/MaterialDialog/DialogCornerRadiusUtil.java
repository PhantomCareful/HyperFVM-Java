package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;

import com.careful.HyperFVM.R;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Objects;

/**
 * 弹窗圆角与设备屏幕物理圆角同步（与 BottomSheet 的 applyScreenCornerRadius 同构）。
 * <p>
 * 项目全部 17 处弹窗都经过 {@link com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil}
 * 的统一入口，故在其中调用本类，避免逐点散落。
 * <p>
 * 弹窗可见轮廓由两层背景叠加而成，必须同值同步（均为 G2 连续曲率的 squircle 圆角，
 * 角形状由 {@link SquircleCornerTreatment} 提供，同心半径仍按下方原则计算）：
 * ① window 层 MaterialShapeDrawable（MaterialAlertDialogBuilder 设置的背景）；
 * ② 内容层根容器背景（布局根的 ?attr/colorSurface，inflate 后为 ColorDrawable，
 *    替换为同色 MaterialShapeDrawable 后才能表达 G2 角曲线并与 window 层轮廓吻合）。
 * <p>
 * 需在 dialog.show() 之前调用：通过 OnGlobalLayout 等待 insets 分发完成（show 后
 * decor attach 才会触发），处理一次后即移除监听。设备无圆角信息（平板/模拟器）时
 * 两层统一回退到 dimens 的 dialog_corner_radius，保证两层始终一致。
 * <p>
 * 同心原则：弹窗未紧贴屏幕边缘，直接用屏幕物理圆角会显得过大。故弹窗圆角取
 * “屏幕圆角 - 弹窗到屏幕最近边缘的距离”，使弹窗圆弧与屏幕圆角同心、看起来和谐；
 * 贴边容器（距离为0）自动等于物理圆角，与 BottomSheet 行为一致。
 */
public final class DialogCornerRadiusUtil {

    /** 诊断日志开关（同心换算调试中，验证通过后关闭） */
    private static final boolean DEBUG = true;
    private static final String TAG = "DialogCorner";

    private DialogCornerRadiusUtil() {
    }

    /**
     * 注册圆角同步（show 前调用，实际生效于首次全局布局回调）。
     *
     * @param dialog 目标 Dialog（已 create、未 show）
     */
    public static void apply(final Dialog dialog) {
        if (dialog.getWindow() == null) {
            return;
        }
        final View decor = dialog.getWindow().getDecorView();
        if (DEBUG) {
            StringBuilder from = new StringBuilder();
            for (StackTraceElement e : new Throwable().getStackTrace()) {
                if (e.getClassName().contains("DialogBuilderManager")
                        || e.getClassName().contains("Activity")) {
                    from.append(e.getMethodName()).append(" <- ");
                }
            }
            android.util.Log.d(TAG, "apply: " + dialog.getClass().getSimpleName()
                    + " from=" + from);
        }
        decor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                WindowInsets insets = decor.getRootWindowInsets();
                if (insets == null) {
                    if (DEBUG) android.util.Log.d(TAG, "onGlobalLayout: insets=null, retry");
                    return; // insets 尚未分发，等下一次全局布局回调再试
                }
                decor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int radius = resolveRadius(dialog, insets);
                if (DEBUG) {
                    RoundedCorner tl = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
                    android.util.Log.d(TAG, "radius resolve: dialogInsetsTopLeft="
                            + (tl == null ? "null" : tl.getRadius() + "px")
                            + " -> appliedRadius=" + radius + "px");
                }
                applyLayers(dialog, radius);
            }
        });
    }

    /**
     * 读取设备屏幕物理圆角半径（px），换算为与屏幕圆角同心的弹窗圆角；
     * 无圆角信息时回退 dimens 默认值。
     * <p>
     * 弹窗窗口居于屏幕中央、不与屏幕角落相交时，dialog window 的 insets 不携带
     * RoundedCorner 信息，此时改从宿主 Activity 的全屏窗口 insets 读取（全屏窗口
     * 必然贴屏幕角落，与 BottomSheet 能拿到圆角信息同理）。
     */
    private static int resolveRadius(Dialog dialog, WindowInsets insets) {
        int screenRadius = readCornerRadius(insets);
        if (screenRadius == 0) {
            // 居中弹窗的 window insets 无圆角信息：改从宿主 Activity 的全屏窗口取
            Activity activity = findActivity(dialog.getContext());
            if (activity != null) {
                WindowInsets activityInsets = activity.getWindow().getDecorView().getRootWindowInsets();
                if (activityInsets != null) {
                    screenRadius = readCornerRadius(activityInsets);
                }
            }
            if (DEBUG) android.util.Log.d(TAG, "resolve: dialogInsets=0 -> activityInsets=" + screenRadius + "px");
        }
        if (screenRadius == 0) {
            // 设备没有圆角信息（如平板/模拟器）：两层统一回退默认值
            return dialog.getContext().getResources().getDimensionPixelSize(R.dimen.dialog_corner_radius);
        }
        // 与屏幕圆角同心：弹窗圆角 = 屏幕圆角 - 弹窗到屏幕最近边缘的距离。
        // 居中弹窗水平/垂直边距不等，取更近的边做同心基准（该方向上圆弧圆心与
        // 屏幕圆角圆心严格重合）；贴边时距离为 0，自动等于物理圆角
        int[] location = new int[2];
        Objects.requireNonNull(dialog.getWindow()).getDecorView().getLocationOnScreen(location);
        int inset = Math.min(location[0], location[1]);
        int radius = screenRadius - inset;
        if (DEBUG) android.util.Log.d(TAG, "resolve: screenRadius=" + screenRadius
                + "px dialogAt=(" + location[0] + "," + location[1] + ") inset=" + inset
                + "px -> concentricRadius=" + radius + "px");
        if (radius <= 0) {
            // 弹窗离屏幕边缘过远，同心值非正：回退默认值
            radius = dialog.getContext().getResources().getDimensionPixelSize(R.dimen.dialog_corner_radius);
        }
        return radius;
    }

    /** 从 insets 中读取顶部两角的物理圆角半径最大值（px），无信息返回 0 */
    private static int readCornerRadius(WindowInsets insets) {
        int radius = 0;
        RoundedCorner topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
        if (topLeft != null) {
            radius = Math.max(radius, topLeft.getRadius());
        }
        RoundedCorner topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT);
        if (topRight != null) {
            radius = Math.max(radius, topRight.getRadius());
        }
        return radius;
    }

    /** 从 Dialog 的 context 中解包出宿主 Activity（兼容 ContextWrapper 包装） */
    private static Activity findActivity(Context context) {
        Context ctx = context;
        while (ctx != null) {
            if (ctx instanceof Activity) {
                return (Activity) ctx;
            }
            if (ctx instanceof ContextWrapper) {
                ctx = ((ContextWrapper) ctx).getBaseContext();
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * 同步两层背景圆角：① window 层 MaterialShapeDrawable ② 内容层根容器背景。
     * 两层均为 G2 squircle 角曲线，半径同值保证轮廓吻合。
     */
    private static void applyLayers(Dialog dialog, int radius) {
        // ① window 层：MaterialAlertDialogBuilder.create() 将 MaterialShapeDrawable
        //    包成 InsetDrawable 后设为窗口背景，decorView 背景即窗口背景（公开 API 取法），需解包内层
        Drawable windowBackground = Objects.requireNonNull(dialog.getWindow()).getDecorView().getBackground();
        if (DEBUG) android.util.Log.d(TAG, "window decorBg=" + (windowBackground == null ? "null" : windowBackground.getClass().getName()));
        if (windowBackground instanceof InsetDrawable) {
            windowBackground = ((InsetDrawable) windowBackground).getDrawable();
            if (DEBUG) android.util.Log.d(TAG, "window unwrapped=" + (windowBackground == null ? "null" : windowBackground.getClass().getName()));
        }
        if (windowBackground instanceof MaterialShapeDrawable) {
            MaterialShapeDrawable shape = (MaterialShapeDrawable) windowBackground.mutate();
            // 四角换成 G2 squircle 角处理，半径维持同心换算值
            ShapeAppearanceModel model = shape.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(new SquircleCornerTreatment())
                    .setAllCornerSizes((float) radius)
                    .build();
            shape.setShapeAppearanceModel(model);
            if (DEBUG) android.util.Log.d(TAG, "window layer: squircle(G2) rounded to " + radius + "px");
        } else if (DEBUG) {
            android.util.Log.d(TAG, "window layer: SKIPPED (not MaterialShapeDrawable)");
        }

        // ② 内容层：setView 放入 customPanel/custom 的布局根容器
        View contentRoot = findContentRoot(dialog);
        if (DEBUG) android.util.Log.d(TAG, "content root=" + (contentRoot == null ? "null"
                : contentRoot.getClass().getSimpleName() + " bg="
                + (contentRoot.getBackground() == null ? "null" : contentRoot.getBackground().getClass().getSimpleName())));
        if (contentRoot != null) {
            applyContentBackground(contentRoot, radius);
        }
    }

    /**
     * 定位 setView 的布局根（customPanel > custom > dialogView，兼容仅有 customPanel 的情况）。
     */
    private static View findContentRoot(Dialog dialog) {
        View container = dialog.findViewById(androidx.appcompat.R.id.custom);
        if (!(container instanceof ViewGroup) || ((ViewGroup) container).getChildCount() == 0) {
            container = dialog.findViewById(androidx.appcompat.R.id.customPanel);
        }
        if (container instanceof ViewGroup && ((ViewGroup) container).getChildCount() > 0) {
            return ((ViewGroup) container).getChildAt(0);
        }
        return null;
    }

    /**
     * 内容层根背景圆角化：G2 角曲线无法用 GradientDrawable 表达，统一替换为
     * 同色 MaterialShapeDrawable（squircle 角处理）；已是本工具设置的
     * MaterialShapeDrawable 时只更新模型（重复应用，保留既有填色）。
     */
    private static void applyContentBackground(View contentRoot, int radius) {
        Drawable background = contentRoot.getBackground();
        if (background instanceof MaterialShapeDrawable) {
            // 重复应用：原模型可能携带本工具外的 edges，toBuilder 保留后只换四角
            MaterialShapeDrawable existing = (MaterialShapeDrawable) background.mutate();
            existing.setShapeAppearanceModel(existing.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(new SquircleCornerTreatment())
                    .setAllCornerSizes((float) radius)
                    .build());
            if (DEBUG) android.util.Log.d(TAG, "content layer: squircle(G2) model refreshed " + radius + "px");
            return;
        }
        int color;
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
        } else {
            // GradientDrawable 等不处理：G2 曲线无法用它表达，而 framework 私有字段
            // 已在 Android 17 移除、纯色无法可靠读取（真渐变也不能覆盖）——保持原背景
            if (DEBUG) android.util.Log.d(TAG, "content layer: SKIPPED (bg type not handled)");
            return;
        }
        MaterialShapeDrawable shape = new MaterialShapeDrawable(new ShapeAppearanceModel().toBuilder()
                .setAllCorners(new SquircleCornerTreatment())
                .setAllCornerSizes((float) radius)
                .build());
        shape.setFillColor(ColorStateList.valueOf(color));
        contentRoot.setBackground(shape);
        if (DEBUG) android.util.Log.d(TAG, "content layer: -> MaterialShapeDrawable squircle(G2) " + radius + "px");
    }
}
