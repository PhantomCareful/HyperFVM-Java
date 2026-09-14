package com.careful.HyperFVM.Activities.DataCenter.DetailCardData.Cookery;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * 食神谱系列 BottomSheet 的公共基类：半开起步、可上滑展开的模态弹层。
 *
 * <p>打开行为按内容自然高度自动选择：
 * 1. 内容高于窗口高度的 {@link #HALF_EXPANDED_RATIO}（半屏）时——以半开状态打开，
 *    上滑可展开为全屏，内容由 NestedScrollView 接管滚动（sheet 为全高，
 *    内容根高度由本类在运行时设为窗口高度）；
 * 2. 内容不超过半屏时——sheet 高度自适应裹住内容，直接完整显示。
 * 下拉关闭保持 BottomSheetDialog 原生行为。
 *
 * <p>圆角：顶部圆角在运行时读取设备屏幕的物理圆角（Android 12+ 的 RoundedCorner API），
 * 同步应用到外层 design_bottom_sheet 的 MaterialShapeDrawable 与内容根布局的 bottom_sheet_rounded；
 * 取不到圆角信息（平板/模拟器等无圆角设备）时保持 XML 默认值（40dp）。
 */
public class BaseCookeryBottomSheetFragment extends BottomSheetDialogFragment {

    /** 半开高度占窗口高度的比例（内容高于该比例时以半开状态打开） */
    private static final float HALF_EXPANDED_RATIO = 0.5f;

    /** 设备屏幕圆角是否已处理（取不到圆角信息时也会置位，避免反复尝试） */
    private boolean screenCornerHandled;

    @Override
    public void onStart() {
        super.onStart();

        View content = getView();
        if (content == null || !(getDialog() instanceof BottomSheetDialog)) {
            return;
        }
        FrameLayout bottomSheet = ((BottomSheetDialog) getDialog())
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) {
            return;
        }

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

        // 预测量内容自然高度：矮内容自适应完整显示；高内容半开起步、可上滑展开全屏
        int windowHeight = getWindowHeight(bottomSheet);
        int naturalHeight = measureNaturalHeight(content);
        final boolean halfMode = naturalHeight > (int) (windowHeight * HALF_EXPANDED_RATIO);
        if (halfMode) {
            // 半开模式：sheet 全高（内容根设为窗口高度，保证 NestedScrollView 有滚动空间），
            // 打开停在半屏处，上滑展开为全屏
            behavior.setFitToContents(false);
            behavior.setHalfExpandedRatio(HALF_EXPANDED_RATIO);
            setContentHeight(content, windowHeight);
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            // 自适应模式：sheet 高度裹住内容，完整显示
            behavior.setFitToContents(true);
            setContentHeight(content, ViewGroup.LayoutParams.WRAP_CONTENT);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // 尽量提前应用设备屏幕圆角（insets 未就绪时会在全局布局回调中重试）
        screenCornerHandled = false;
        applyScreenCornerRadius(bottomSheet, content);

        // 首帧后：按实际窗口高度校正半开模式的内容高度；补做设备圆角同步
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (halfMode) {
                            View parent = (View) bottomSheet.getParent();
                            if (parent != null && parent.getHeight() > 0) {
                                setContentHeight(content, parent.getHeight());
                            }
                        }
                        applyScreenCornerRadius(bottomSheet, content);
                        if (screenCornerHandled) {
                            bottomSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
    }

    /** 窗口（父容器）高度；未就绪时退回屏幕高度估算 */
    private static int getWindowHeight(FrameLayout bottomSheet) {
        View parent = (View) bottomSheet.getParent();
        if (parent != null && parent.getHeight() > 0) {
            return parent.getHeight();
        }
        return bottomSheet.getResources().getDisplayMetrics().heightPixels;
    }

    /** 以屏幕宽度预测量内容自然高度（NestedScrollView 为 wrap_content 时得到完整内容高度） */
    private static int measureNaturalHeight(View content) {
        int width = content.getWidth() > 0
                ? content.getWidth()
                : content.getResources().getDisplayMetrics().widthPixels;
        content.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return content.getMeasuredHeight();
    }

    /** 设置内容根布局的高度（半开模式为窗口高度，自适应模式为 wrap_content） */
    private static void setContentHeight(View content, int height) {
        ViewGroup.LayoutParams lp = content.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        } else {
            if (lp.height == height) {
                return;
            }
            lp.height = height;
        }
        content.setLayoutParams(lp);
    }

    /**
     * 读取设备屏幕顶部的物理圆角半径（px）并同步到 sheet 的内外两层背景：
     * 外层 design_bottom_sheet 的 MaterialShapeDrawable（shapeAppearance 顶部两角）
     * 与内容根布局的 bottom_sheet_rounded（cornerRadii 顶部两角，底部保持直角）。
     * 取不到圆角信息时保持 XML 默认圆角。
     */
    private void applyScreenCornerRadius(FrameLayout bottomSheet, View content) {
        if (screenCornerHandled) {
            return;
        }
        WindowInsets insets = content.getRootWindowInsets();
        if (insets == null) {
            return; // insets 尚未分发，等下一次全局布局回调再试
        }
        // insets 已就绪：无论是否有圆角信息都只处理一次
        screenCornerHandled = true;

        int radius = 0;
        RoundedCorner topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
        if (topLeft != null) {
            radius = Math.max(radius, topLeft.getRadius());
        }
        RoundedCorner topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT);
        if (topRight != null) {
            radius = Math.max(radius, topRight.getRadius());
        }
        if (radius == 0) {
            return; // 设备没有圆角信息（如平板/模拟器），保持默认圆角
        }

        // 外层：design_bottom_sheet 的 MaterialShapeDrawable 背景
        Drawable outerBackground = bottomSheet.getBackground();
        if (outerBackground instanceof MaterialShapeDrawable) {
            MaterialShapeDrawable outer = (MaterialShapeDrawable) outerBackground.mutate();
            ShapeAppearanceModel model = outer.getShapeAppearanceModel().toBuilder()
                    .setTopLeftCornerSize((float) radius)
                    .setTopRightCornerSize((float) radius)
                    .build();
            outer.setShapeAppearanceModel(model);
        }

        // 内层：内容根布局的 bottom_sheet_rounded 背景（顶部两角，底部保持直角）
        Drawable innerBackground = content.getBackground();
        if (innerBackground instanceof GradientDrawable) {
            GradientDrawable inner = (GradientDrawable) innerBackground.mutate();
            inner.setCornerRadii(new float[]{radius, radius, radius, radius, 0f, 0f, 0f, 0f});
        }
    }
}
