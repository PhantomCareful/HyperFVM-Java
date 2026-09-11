package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * 按指定速率播放 {@link FrameSequence} 的视图，支持播放与暂停（暂停保留当前帧，恢复后从原位置继续）。
 * 多个实例可共享同一个 FrameSequence，以不同速率同时播放同一动画。
 * 宽高未固定时按帧宽高比自适应测量；圆角裁剪作用于图片实际绘制区域，与视图留白无关。
 */
public class FrameSequenceView extends View {
    // 单次回调最多推进的素材时间：设备卡顿后避免一次跳跃过多帧
    private static final long MAX_TICK_DELTA_MS = 100;

    private FrameSequence frameSequence;
    // 播放速率：1f 为素材原始速度，0.5f 为半速，2f 为两倍速
    private float speed = 1f;
    // 图片绘制区域的圆角半径（像素），0 表示直角
    private float cornerRadiusPx;
    // 图片绘制区域的描边宽度（像素），0 表示不描边
    private float strokeWidthPx;
    // 描边与图片边缘的间距（像素，描边位于图片外侧）
    private float strokeGapPx;
    // 描边颜色
    private int strokeColor;

    private boolean playing;        // 是否处于播放状态
    private boolean frameScheduled; // Choreographer 回调是否已排队
    private long materialTimeMs;    // 已播放的素材时间，暂停时保留、恢复时继续
    private long lastTickMs;        // 上次回调的系统时间，用于计算两次回调的间隔
    private int currentFrame = -1;  // 当前显示的帧下标，-1 表示尚未确定

    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();

    // 圆角绘制：用 BitmapShader 把当前帧绘入圆角矩形（抗锯齿平滑）
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix shaderMatrix = new Matrix();
    private BitmapShader bitmapShader;
    private Bitmap shaderBitmap;

    // 描边绘制
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF strokeRect = new RectF();

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            frameScheduled = false;
            if (!playing || frameSequence == null) return;

            long now = frameTimeNanos / 1_000_000;
            if (lastTickMs == 0) lastTickMs = now;
            long delta = Math.min(now - lastTickMs, MAX_TICK_DELTA_MS);
            lastTickMs = now;

            // 速率让素材时间以 speed 倍前进，再由时间映射到对应的帧
            materialTimeMs += (long) (delta * speed);
            int index = frameSequence.getFrameIndex(materialTimeMs);
            if (index != currentFrame) {
                currentFrame = index;
                invalidate();
            }
            scheduleFrame();
        }
    };

    public FrameSequenceView(Context context) {
        this(context, null);
    }

    public FrameSequenceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameSequenceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /** 设置要播放的帧序列（多个视图可共享同一个实例），并立即显示第一帧 */
    public void setFrameSequence(FrameSequence sequence) {
        this.frameSequence = sequence;
        this.currentFrame = -1;
        this.materialTimeMs = 0;
        // 帧尺寸可能影响自适应测量结果
        requestLayout();
        invalidate();
    }

    /** 设置播放速率（1f 为素材原始速度） */
    public void setSpeed(float speed) {
        this.speed = Math.max(0.05f, speed);
    }

    /** 设置图片绘制区域的圆角半径（像素，0 表示直角） */
    public void setCornerRadius(float radiusPx) {
        this.cornerRadiusPx = Math.max(0f, radiusPx);
        invalidate();
    }

    /** 设置图片外侧的描边（宽度、与图片边缘的间距、颜色；宽度 <= 0 时取消描边） */
    public void setStroke(float widthPx, float gapPx, int color) {
        this.strokeWidthPx = Math.max(0f, widthPx);
        this.strokeGapPx = Math.max(0f, gapPx);
        this.strokeColor = color;
        invalidate();
    }

    /** 开始播放；已暂停时从暂停位置继续 */
    public void start() {
        if (frameSequence == null) return;
        playing = true;
        if (isAttachedToWindow()) scheduleFrame();
    }

    /** 暂停播放（保留当前帧与播放位置） */
    public void stop() {
        playing = false;
        if (frameScheduled) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameScheduled = false;
        }
        lastTickMs = 0;
    }

    /** 获取当前显示的帧下标（暂停后即定格帧；尚未确定时按素材时间计算，无帧序列表时返回 -1） */
    public int getCurrentFrame() {
        if (currentFrame >= 0) return currentFrame;
        if (frameSequence == null) return -1;
        return frameSequence.getFrameIndex(materialTimeMs);
    }

    /** 获取当前已播放的素材时间（毫秒），用于界面重建后的状态恢复 */
    public long getMaterialTimeMs() {
        return materialTimeMs;
    }

    /** 设置素材播放时间（毫秒）：重算当前帧并刷新显示，用于界面重建后的状态恢复 */
    public void setMaterialTimeMs(long timeMs) {
        this.materialTimeMs = Math.max(0, timeMs);
        this.currentFrame = -1;
        invalidate();
    }

    private void scheduleFrame() {
        if (frameScheduled || !playing || frameSequence == null) return;
        frameScheduled = true;
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (playing) scheduleFrame();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (frameSequence == null) {
            // 帧序列尚未设置：仅采用布局给定的固定尺寸，其余为 0
            setMeasuredDimension(
                    widthMode == MeasureSpec.EXACTLY ? widthSize : 0,
                    heightMode == MeasureSpec.EXACTLY ? heightSize : 0);
            return;
        }

        int frameWidth = frameSequence.getFrameWidth();
        int frameHeight = frameSequence.getFrameHeight();

        if (widthMode == MeasureSpec.EXACTLY) {
            // 宽度已由父布局（权重分配等）决定：高度按帧宽高比自适应，
            // 布局显式指定了高度则采用布局值（由 onDraw 居中缩放绘制）
            int height = heightMode == MeasureSpec.EXACTLY
                    ? heightSize
                    : Math.round(widthSize * (float) frameHeight / frameWidth);
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
            setMeasuredDimension(widthSize, height);
        } else if (heightMode == MeasureSpec.EXACTLY) {
            // 高度已定：宽度按帧宽高比自适应
            int width = Math.round(heightSize * (float) frameWidth / frameHeight);
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
            setMeasuredDimension(width, heightSize);
        } else {
            // 宽高都未固定：使用帧的自然尺寸，超出上限时等比收缩
            int width = frameWidth;
            int height = frameHeight;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                height = Math.round(height * (float) widthSize / width);
                width = widthSize;
            }
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                width = Math.round(width * (float) heightSize / height);
                height = heightSize;
            }
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (frameSequence == null || getWidth() == 0 || getHeight() == 0) return;
        if (currentFrame < 0) {
            currentFrame = frameSequence.getFrameIndex(materialTimeMs);
        }
        Bitmap bitmap = frameSequence.getFrame(currentFrame);
        if (bitmap == null) return;

        // 保持帧的宽高比，在视图内居中绘制（等价于 ImageView 的 fitCenter）
        float scale = Math.min(getWidth() / (float) bitmap.getWidth(),
                getHeight() / (float) bitmap.getHeight());
        float drawWidth = bitmap.getWidth() * scale;
        float drawHeight = bitmap.getHeight() * scale;
        float left = (getWidth() - drawWidth) / 2f;
        float top = (getHeight() - drawHeight) / 2f;
        srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        dstRect.set(left, top, left + drawWidth, top + drawHeight);

        if (cornerRadiusPx <= 0) {
            bitmapPaint.setShader(null);
            canvas.drawBitmap(bitmap, srcRect, dstRect, bitmapPaint);
            drawStroke(canvas, 0f);
            return;
        }

        // 圆角裁剪作用于"图片实际绘制区域"，视图尺寸与图片比例不一致（有留白）时同样生效
        if (bitmapShader == null || shaderBitmap != bitmap) {
            bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaderBitmap = bitmap;
        }
        shaderMatrix.reset();
        shaderMatrix.setScale(drawWidth / bitmap.getWidth(), drawHeight / bitmap.getHeight());
        shaderMatrix.postTranslate(left, top);
        bitmapShader.setLocalMatrix(shaderMatrix);
        bitmapPaint.setShader(bitmapShader);

        float radius = Math.min(cornerRadiusPx, Math.min(drawWidth, drawHeight) / 2f);
        canvas.drawRoundRect(dstRect, radius, radius, bitmapPaint);
        drawStroke(canvas, radius);
    }

    // 沿图片绘制区域外侧描边（宽度 <= 0 时不绘制；radius > 0 时与圆角边缘保持同心）
    private void drawStroke(Canvas canvas, float radius) {
        if (strokeWidthPx <= 0) return;
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidthPx);
        strokePaint.setColor(strokeColor);

        // 描边路径居中于“间距 + 半个线宽”的外扩偏移处：描边整体位于图片外侧（需父布局允许子视图溢出绘制）
        float inset = -(strokeGapPx + strokeWidthPx / 2f);
        strokeRect.set(dstRect);
        strokeRect.inset(inset, inset);
        if (radius > 0) {
            // 半径同步外扩，保证描边与圆角边缘同心
            float strokeRadius = radius - inset;
            canvas.drawRoundRect(strokeRect, strokeRadius, strokeRadius, strokePaint);
        } else {
            canvas.drawRect(strokeRect, strokePaint);
        }
    }
}
