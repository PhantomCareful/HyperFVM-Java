package com.careful.HyperFVM.utils.ForDesign.Widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

/**
 * 支持外描边的星卡容器：仅持有描边参数（宽度、间距、颜色）并对外提供查询，
 * 实际绘制由父容器 StrokeRowLayout 代绘（描边位于卡片外侧，子视图无法在自身
 * bounds 之外绘制——画布裁剪恒等于视图自身 bounds，溢出像素会被整体裁掉）。
 */
public class StrokeCardView extends CardView {
    // 描边宽度（像素），0 表示不描边
    private float strokeWidthPx;
    // 描边与卡片边缘的间距（像素，描边位于卡片外侧）
    private float strokeGapPx;
    // 描边颜色
    private int strokeColor;

    // 卡片圆角半径（像素）：从 XML 的 cardCornerRadius 读取，并跟随 setRadius 更新
    private float cardRadiusPx;

    public StrokeCardView(@NonNull Context context) {
        this(context, null);
    }

    public StrokeCardView(@NonNull Context context, @NonNull AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeCardView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // XML 中的圆角由 CardView 构造器直接消费、不会回调 setRadius，这里主动读取一次
        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs,
                androidx.cardview.R.styleable.CardView, defStyleAttr, 0);
        cardRadiusPx = a.getDimension(androidx.cardview.R.styleable.CardView_cardCornerRadius, 0f);
        a.recycle();
    }

    /** 设置卡片外侧的描边（宽度、与卡片边缘的间距、颜色；宽度 <= 0 时取消描边） */
    public void setStroke(float widthPx, float gapPx, int color) {
        this.strokeWidthPx = Math.max(0f, widthPx);
        this.strokeGapPx = Math.max(0f, gapPx);
        this.strokeColor = color;
        invalidateWithAncestors();
    }

    /** 是否处于描边选中态（描边宽度 > 0） */
    public boolean isStroked() {
        return strokeWidthPx > 0;
    }

    /** 描边宽度（像素） */
    public float getStrokeWidthPx() {
        return strokeWidthPx;
    }

    /** 描边与卡片边缘的间距（像素，描边位于卡片外侧） */
    public float getStrokeGapPx() {
        return strokeGapPx;
    }

    /** 描边颜色 */
    public int getStrokeColor() {
        return strokeColor;
    }

    /** 卡片圆角半径（像素） */
    public float getCardRadiusPx() {
        return cardRadiusPx;
    }

    @Override
    public void setRadius(float radius) {
        super.setRadius(radius);
        this.cardRadiusPx = radius;
        invalidateWithAncestors();
    }

    // 描边由父容器 StrokeRowLayout 代绘，其绘制坐标与自身 bounds 相交的区域
    // 也依赖脏区覆盖，而 invalidate() 的脏区会被裁剪在 bounds 内，
    // 因此脏区需逐级向上传播，让父容器及其祖先一并重绘
    private void invalidateWithAncestors() {
        invalidate();
        ViewParent parent = getParent();
        while (parent instanceof View) {
            ((View) parent).invalidate();
            parent = parent.getParent();
        }
    }
}
