package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.careful.HyperFVM.R;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Objects;

/**
 * 弹窗圆角：与底部按钮同心的固定值（与 BottomSheet 的 applyScreenCornerRadius 相互独立）。
 * <p>
 * 项目全部 17 处弹窗都经过 {@link com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil}
 * 的统一入口，故在其中调用本类，避免逐点散落。
 * <p>
 * 弹窗可见轮廓为单层：window 层背景（MaterialAlertDialogBuilder 设置的 InsetDrawable/MSD）
 * 直接设为透明、不再绘制；仅保留内容层根容器的 ?attr/colorSurface 背景（inflate 后为
 * ColorDrawable 或 framework ColorStateListDrawable——Android 13 实测为后者，仅判
 * ColorDrawable 会 SKIPPED 导致“完全矩形”，两者须一并取色），替换为
 * MaterialShapeDrawable 后表达 G2 连续曲率的 squircle 圆角，
 * 角形状由 {@link SquircleCornerTreatment} 提供），避免两层同形轮廓的双重 AA 叠加。
 * 弹窗内按钮保持 Material 默认圆角，不加 G2：按钮短边（约 40dp 级）小于翼曲线
 * 所需延伸 2(1+s)r（r=20、s=0.4 时 56dp），相邻角翼在直边上互相重叠会触发
 * ShapePath 的 overlap/UNION 规范化，在直边转入圆角处留下衔接折痕，实测无法规避。
 * <p>
 * 圆角取固定值 R.dimen.dialog_corner_radius（46dp = 按钮 margin 26dp + 按钮圆角
 * 20dp）：弹窗未贴屏幕边缘且居中显示，与屏幕物理圆角同心反而不协调；固定 46dp
 * 使弹窗角圆心与底部按钮角圆心严格重合，观感统一，也不再依赖设备圆角信息。
 * <p>
 * 必须在 dialog.show() 之前调用并**同步**完成：此时 decor 尚未 attach 到窗口，
 * 替换背景不会触发窗口布局，弹窗首帧即为终态。若像早期版本那样等首次全局布局
 * 后再改背景，窗口会先按旧背景（InsetDrawable 带 inset）完成第一帧布局，替换后
 * 背景 padding 变化引发 requestLayout 二次布局/重定位，表现为“先出现再瞬间移动”
 * 的跳变动画。
 */
public final class DialogCornerRadiusUtil {

    /** 诊断日志开关（圆角/出现动画调试中，验证通过后关闭） */
    private static final boolean DEBUG = true;
    private static final String TAG = "DialogCorner";

    private DialogCornerRadiusUtil() {
    }

    /**
     * 同步应用圆角与背景（show 前调用，decor 未 attach，无布局副作用）。
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
        if (applyLayers(dialog, resolveRadius(dialog))) {
            return;
        }
        // 兜底：内容视图树尚未就绪时（理论不可达），等首次全局布局补齐一次并移除监听
        decor.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                decor.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyLayers(dialog, resolveRadius(dialog));
            }
        });
    }

    /**
     * 弹窗圆角固定值（px）：R.dimen.dialog_corner_radius（46dp = 按钮 margin 26dp
     * + 按钮圆角 20dp），与底部按钮角圆心严格同心；不依赖设备屏幕圆角信息，
     * 平板/模拟器上行为一致。
     */
    private static int resolveRadius(Dialog dialog) {
        int radius = dialog.getContext().getResources().getDimensionPixelSize(R.dimen.dialog_corner_radius);
        if (DEBUG) android.util.Log.d(TAG, "resolve: fixed dialog_corner_radius=" + radius + "px");
        return radius;
    }

    /**
     * 同步弹窗背景：① window 层背景设为透明（不再绘制）② 内容层根容器背景
     * 替换为 G2 squircle 圆角的 MaterialShapeDrawable，弹窗可见轮廓仅此一层。
     *
     * @return 内容层已就绪（或无需重试）返回 true；内容视图树未就绪返回 false
     */
    private static boolean applyLayers(Dialog dialog, int radius) {
        // ① window 层：MaterialAlertDialogBuilder 会把背景包成 InsetDrawable(MSD) 设为
        //    窗口背景；整体替换为透明，弹窗轮廓只由内容层 colorSurface 提供，避免两层
        //    同形轮廓双重 AA 叠加。必须在 show 前（decor 未 attach）执行，否则背景
        //    padding 变化会引发 requestLayout 二次布局，造成出现位置跳变
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        if (DEBUG) android.util.Log.d(TAG, "window layer: background -> transparent");

        // ② 内容层：setView 放入 customPanel/custom 的布局根容器
        View contentRoot = findContentRoot(dialog);
        if (DEBUG) android.util.Log.d(TAG, "content root=" + (contentRoot == null ? "null"
                : contentRoot.getClass().getSimpleName() + " bg="
                + (contentRoot.getBackground() == null ? "null" : contentRoot.getBackground().getClass().getSimpleName())));
        if (contentRoot != null) {
            applyContentBackground(contentRoot, radius);
        }
        // ③ 按钮不加 G2：见类注释，短边不足会触发 ShapePath UNION 折痕
        return contentRoot != null;
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
        ColorStateList bgColors = null;
        if (background instanceof ColorDrawable) {
            bgColors = ColorStateList.valueOf(((ColorDrawable) background).getColor());
        } else if (background instanceof ColorStateListDrawable) {
            // Android 10+ framework 把 ?attr 纯色包装成的 ColorStateListDrawable
            // （Android 13 实测走此类型）：只判 ColorDrawable 会 SKIPPED，
            // 叠加 window 层已透明 = 完全矩形，故须一并取色
            bgColors = ((ColorStateListDrawable) background).getColorStateList();
        }
        if (bgColors != null) {
            color = bgColors.getDefaultColor();
        } else {
            // 真渐变/图片背景不处理：G2 曲线无法用它表达，而 framework 私有字段
            // 已在 Android 17 移除、纯色无法可靠读取（真渐变也不能覆盖）——保持原背景
            if (DEBUG) android.util.Log.d(TAG, "content layer: SKIPPED (bg type not handled): "
                    + background.getClass().getName());
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
