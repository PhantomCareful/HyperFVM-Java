package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Smooth.SmoothCornerTraverser;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;

import fan.smooth.SmoothCornerHelper;

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
 * ColorDrawable 会 SKIPPED 导致“完全矩形”，两者须一并取色），替换为同色
 * GradientDrawable 圆角背景，并经 fan.miuix.smooth 的 SmoothCornerHelper 反射
 * View/Drawable.setSmoothCornerEnabled，由 HyperOS 系统把圆角渲染成原生 squircle。
 * 非 HyperOS/MIUI（persist.sys.support_view_smoothcorner=false）反射静默跳过，
 * 退化为普通圆角，半径逻辑不变。
 * 弹窗内按钮保持 Material 默认圆角，与原 G2 版一致不在改造范围内。
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

    /** 诊断日志开关（圆角/出现动画已验证通过，关闭以清噪声；再调试时改 true） */
    private static final boolean DEBUG = false;
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
        // HC 弹窗两端动画复刻（纯窗口级，WMS 播放），按当前窗口最小宽度分支：
        // · 手机（<600dp）：屏幕下方飞入/飞出；PAD（≥600dp）：屏幕中心淡入+微缩放/淡出。
        // 判据取 Configuration.smallestScreenWidthDp——与 layout-sw600dp 布局选择同源，
        // PAD 小窗（freeform）下页面用手机布局时动画同步跟随；不用 DeviceTypeUtil（物理屏，
        // 小窗不变恒判 PAD），也不用 SmallestWidthUtil（Application context 的 window metrics
        // 在小窗下是 display 最大值，不随任务窗口）。
        // 必须在 show 前设置；退场动画（windowExitAnimation）由系统在移除窗口时加载——
        // 动画资源内插值器必须是框架内置项，否则 WMS 进程解析失败导致退场直接消失
        // （实测：自定义插值器报 Unknown interpolator name → 动画加载失败 → removeImmediately）。
        final boolean tablet = dialog.getContext().getResources().getConfiguration()
                .smallestScreenWidthDp >= 600;
        final Window window = Objects.requireNonNull(dialog.getWindow());
        // HC 弹窗位置（fan AlertController.getGravity：isTablet ? 17(CENTER) : 81(BOTTOM|CENTER_HORIZONTAL)）：
        // 手机端贴屏幕底部居中、PAD 端屏幕居中；底部留 12dp 间距（fan miuix_appcompat_dialog_ime_margin
        // 同值，fan 以 verticalMargin=间距/窗口高的比例实现，此处 show 前窗口尚未布局，用屏幕高度折算）。
        // 与退场动画自洽：贴底窗口下移 100%p = 完全滑出屏幕底缘，进场则从底缘外滑入到位。
        if (tablet) {
            window.setGravity(Gravity.CENTER);
        } else {
            window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            final int bottomMargin = dialog.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.dialog_window_bottom_margin);
            final float rootHeight = dialog.getContext().getResources()
                    .getDisplayMetrics().heightPixels;
            final WindowManager.LayoutParams attrs = window.getAttributes();
            attrs.verticalMargin = rootHeight > 0 ? (float) bottomMargin / rootHeight : 0f;
            window.setAttributes(attrs);
        }
        window.setWindowAnimations(
                tablet ? R.style.DialogWindowAnim_Pad : R.style.DialogWindowAnim_Phone);
        // 全组件系统平滑圆角：弹窗为独立窗口，在 decor 上挂持续遍历，
        // 覆盖弹窗内全部视图（按钮、列表行等），并幂等补盖 applyLayers 换上的新背景
        SmoothCornerTraverser.install(decor);
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
     * 替换为同色 GradientDrawable 圆角并开系统 squircle，弹窗可见轮廓仅此一层。
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
        // ③ 按钮不处理：保持 Material 默认圆角，见类注释
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
     * 内容层根背景圆角化：统一替换为同色 GradientDrawable（四角 radius）并经
     * SmoothCornerHelper 开启系统 squircle（HyperOS 把该 drawable/View 的圆角
     * 渲染成原生 squircle）；已是本工具设置的 GradientDrawable 时只更新半径
     * （重复应用，保留既有填色）。非 HyperOS/MIUI 反射静默跳过，退化普通圆角。
     */
    private static void applyContentBackground(View contentRoot, int radius) {
        Drawable background = contentRoot.getBackground();
        if (background instanceof GradientDrawable) {
            // 重复应用：只更新半径，保留既有填色（含本工具外的 tint 状态）
            GradientDrawable existing = (GradientDrawable) background.mutate();
            existing.setCornerRadius(radius);
            SmoothCornerHelper.setDrawableSmoothCornerEnable(existing, true);
            SmoothCornerHelper.setViewSmoothCornerEnable(contentRoot, true);
            if (DEBUG) android.util.Log.d(TAG, "content layer: rounded refreshed " + radius + "px + system smooth");
            return;
        }
        if (background instanceof MaterialShapeDrawable) {
            // 主题原生 MSD 兜底：保留模型 edges，只把四角改回标准圆角并开 smooth
            // （系统 squircle 不一定认 MSD 自绘 path，最差退化为普通圆角而非矩形）
            MaterialShapeDrawable existing = (MaterialShapeDrawable) background.mutate();
            existing.setShapeAppearanceModel(existing.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(new RoundedCornerTreatment())
                    .setAllCornerSizes((float) radius)
                    .build());
            SmoothCornerHelper.setDrawableSmoothCornerEnable(existing, true);
            SmoothCornerHelper.setViewSmoothCornerEnable(contentRoot, true);
            if (DEBUG) android.util.Log.d(TAG, "content layer: MSD rounded " + radius + "px + system smooth");
            return;
        }
        ColorStateList bgColors = null;
        if (background instanceof ColorDrawable) {
            bgColors = ColorStateList.valueOf(((ColorDrawable) background).getColor());
        } else if (background instanceof ColorStateListDrawable) {
            // Android 10+ framework 把 ?attr 纯色包装成的 ColorStateListDrawable
            // （Android 13 实测走此类型）：只判 ColorDrawable 会 SKIPPED，
            // 叠加 window 层已透明 = 完全矩形，故须一并取色
            bgColors = ((ColorStateListDrawable) background).getColorStateList();
        }
        if (bgColors == null) {
            // 真渐变/图片背景不处理：framework 私有字段已在 Android 17 移除、
            // 纯色无法可靠读取（真渐变也不能覆盖）——保持原背景
            if (DEBUG) android.util.Log.d(TAG, "content layer: SKIPPED (bg type not handled): "
                    + background.getClass().getName());
            return;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(bgColors);
        shape.setCornerRadius(radius);
        contentRoot.setBackground(shape);
        SmoothCornerHelper.setDrawableSmoothCornerEnable(shape, true);
        SmoothCornerHelper.setViewSmoothCornerEnable(contentRoot, true);
        if (DEBUG) android.util.Log.d(TAG, "content layer: -> GradientDrawable rounded " + radius + "px + system smooth");
    }
}
