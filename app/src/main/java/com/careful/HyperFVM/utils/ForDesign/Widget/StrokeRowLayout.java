package com.careful.HyperFVM.utils.ForDesign.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

/**
 * 星卡横向行容器：代替子卡片绘制选中描边。
 * <p>
 * 子视图（StrokeCardView）在自身 bounds 之外绘制时，画布裁剪恒等于该视图自身 bounds，
 * 溢出的描边像素会被整体裁掉，因此描边必须由"坐标空间足够大"的父容器代绘：
 * 容器 padding 预留出描边驻留区（描边外缘 = 间距 + 线宽，需小于 padding），
 * 描边坐标即恒处于容器自身 bounds 内。
 */
public class StrokeRowLayout extends LinearLayout {

    private final RectF strokeRect = new RectF();
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public StrokeRowLayout(@NonNull Context context) {
        this(context, null);
    }

    public StrokeRowLayout(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeRowLayout(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        drawChildStrokes(canvas);
    }

    //在子卡片外侧绘制选中描边（坐标基于容器自身，处于容器 bounds 内的 padding 驻留区）
    private void drawChildStrokes(Canvas canvas) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            if (!(child instanceof StrokeCardView card) || !card.isStroked()) continue;

            float strokeWidth = card.getStrokeWidthPx();
            float gap = card.getStrokeGapPx();

            strokeRect.set(card.getLeft(), card.getTop(), card.getRight(), card.getBottom());
            // 描边路径居于“间距 + 半个线宽”的外扩偏移处：描边整体位于卡片外侧
            float inset = -(gap + strokeWidth / 2f);
            strokeRect.inset(inset, inset);

            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(strokeWidth);
            strokePaint.setColor(card.getStrokeColor());

            float radius = card.getCardRadiusPx();
            if (radius > 0) {
                // 半径同步外扩，保证描边与卡片圆角边缘同心
                float strokeRadius = Math.min(radius, Math.min(card.getWidth(), card.getHeight()) / 2f) - inset;
                canvas.drawRoundRect(strokeRect, strokeRadius, strokeRadius, strokePaint);
            } else {
                canvas.drawRect(strokeRect, strokePaint);
            }
        }
    }
}
