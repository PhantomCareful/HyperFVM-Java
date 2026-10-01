package com.careful.HyperFVM.utils.ForDesign.Widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 右侧 A-Z 快速索引条（自研，Material You / Monet 自适应）。
 * <p>
 * 竖排胶囊内逐格绘制分节标签（0、A-Z、#，由调用方传入实际存在的分节，
 * 空桶不显示）；手指按住并上下滑动时按触点所在格命中分节，
 * 逐格回调 {@link OnSectionPickListener#onSectionPicked(int)}（带轻量震动反馈），
 * 调用方据 {@code sectionIndex}（即标签列表下标，与列表分节下标一一对应）执行快速跳转。
 * <p>
 * 配色取自当前主题属性，随 Monet 取色（动态色 / 用户选色）自动变化：
 * <ul>
 *   <li>胶囊底色 = ?attr/GeneralCardViewBackground，字母 = ?attr/TextViewColorOnGeneralCardView；</li>
 *   <li>滑动时条内不绘制选中遮罩，命中反馈完全由左侧悬浮气泡承担。</li>
 * </ul>
 * <p>
 * 触摸语义：ACTION_DOWN 即选中并回调（单击也可跳）；滑动中仅在命中格
 * <b>发生变化</b>时才回调，天然节流；ACTION_UP/CANCEL 清除选中态。
 * <p>
 * 滑动时在条左侧绘制 Slider 风格的悬浮气泡（底 ?attr/colorPrimary、字
 * ?attr/colorOnPrimary），实时显示当前命中的章节标签，松手停留片刻后淡出；
 * 气泡绘制超出本 View 左侧 bounds，需父容器 clipChildren=false（布局已配置）。
 */
public class LetterIndexBarView extends View {

    /** 分节命中回调（sectionIndex 即 labels 下标，与 Adapter 分节下标一致） */
    public interface OnSectionPickListener {
        void onSectionPicked(int sectionIndex);
    }

    // 胶囊内容区上下内边距系数（相对胶囊宽度，保证首末字母落在圆头弧度之外）
    private static final float INNER_V_PAD_RATIO = 0.5f;
    // 字号：随格高缩放的比例与钳制范围（dp），格多时自动缩小、格少时不超大
    private static final float TEXT_SIZE_CELL_RATIO = 0.6f;
    private static final float TEXT_SIZE_MIN_DP = 6f;
    private static final float TEXT_SIZE_MAX_DP = 11.5f;
    // wrap_content 的期望尺寸
    private static final float DESIRED_WIDTH_DP = 18f;
    private static final float DESIRED_HEIGHT_PER_LABEL_DP = 14f;
    // —— 滑动悬浮气泡（Slider tooltip 风格）——
    private static final float BUBBLE_HEIGHT_DP = 40f;     // 气泡直径（单字符呈圆形，多字符撑成胶囊）
    private static final float BUBBLE_MIN_WIDTH_DP = 40f;
    private static final float BUBBLE_H_PAD_DP = 10f;      // 文字左右内边距
    private static final float BUBBLE_GAP_DP = 6f;         // 气泡右缘与条左缘的间距
    private static final float BUBBLE_TEXT_SIZE_SP = 16f;
    private static final long BUBBLE_ANIM_MS = 150L;       // 淡入/淡出时长
    private static final long BUBBLE_HIDE_DELAY_MS = 400L; // 松手后停留时长（同 Slider）

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF pillRect = new RectF();

    private List<String> labels = Collections.emptyList();
    private OnSectionPickListener listener;
    private int selectedIndex = -1; // 未触摸为 -1

    // 滑动悬浮气泡状态（跟手显示当前章节，松手延迟淡出）
    private final RectF bubbleRect = new RectF();
    private float bubbleAlpha;        // 0（隐藏）~1（完全显示），驱动淡入淡出
    private float bubbleCenterY;      // 气泡中心 y（px，跟随触点并钳制在条内）
    private String bubbleLabel = "";  // 当前气泡文字（松手后保留至淡出结束）
    private ValueAnimator bubbleAnimator;
    private final Runnable hideBubbleRunnable = () -> animateBubbleTo(0f);

    // 主题配色（构造与 attach 时从当前主题解析，深色模式切换会重建视图）
    private int colorContainer;
    private int colorOnContainer;
    private int colorBubble;          // 气泡底色 ?attr/colorPrimary
    private int colorOnBubble;        // 气泡文字色 ?attr/colorOnPrimary

    public LetterIndexBarView(Context context) {
        this(context, null);
    }

    public LetterIndexBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterIndexBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.CENTER);
        resolveThemeColors();
    }

    /** 设置要显示的分节标签（如 ["0","A",...,"Z","#"]，只传实际存在的分节） */
    public void setLabels(@Nullable List<String> newLabels) {
        labels = newLabels == null || newLabels.isEmpty()
                ? Collections.emptyList() : new ArrayList<>(newLabels);
        if (selectedIndex >= labels.size()) {
            selectedIndex = -1;
        }
        StringBuilder description = new StringBuilder("快速索引");
        for (String label : labels) {
            description.append(' ').append(label);
        }
        setContentDescription(description.toString());
        requestLayout();
        invalidate();
    }

    public void setOnSectionPickListener(@Nullable OnSectionPickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // 防御性重解析：视图可能被 attach 到主题不同的窗口（如主题覆盖后的弹窗上下文）
        resolveThemeColors();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(hideBubbleRunnable);
        if (bubbleAnimator != null) {
            bubbleAnimator.cancel();
            bubbleAnimator = null;
        }
        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public String getAccessibilityClassName() {
        return LetterIndexBarView.class.getSimpleName();
    }

    // ==================== 测量 ====================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 固定尺寸（match_parent / 具体 dp / 约束链）由测量规格直接决定；
        // wrap_content 时给出合理期望值：宽 = 单字容宽，高 = 每格约 14dp
        int desiredWidth = (int) (dpToPx(DESIRED_WIDTH_DP) + getPaddingLeft() + getPaddingRight());
        int desiredHeight = (int) (labels.size() * dpToPx(DESIRED_HEIGHT_PER_LABEL_DP)
                + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    // ==================== 绘制 ====================

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (labels.isEmpty()) {
            return;
        }

        // 1) 胶囊底：铺满 padding 盒，圆头半径 = 短边一半（视觉上为竖直胶囊）
        pillRect.set(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        float pillRadius = Math.min(pillRect.width(), pillRect.height()) / 2f;
        backgroundPaint.setColor(colorContainer);
        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, backgroundPaint);

        // 2) 内容区：再收一圈上下内边距，避开胶囊两端弧度
        float innerTop = pillRect.top + pillRect.width() * INNER_V_PAD_RATIO;
        float innerBottom = pillRect.bottom - pillRect.width() * INNER_V_PAD_RATIO;
        float cellHeight = (innerBottom - innerTop) / labels.size();
        textPaint.setTextSize(dpToPx(clampCellTextSize(cellHeight)));

        // 3) 逐格绘制字符居中（滑动时条内不画选中遮罩，命中反馈由左侧悬浮气泡承担）
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        textPaint.setColor(colorOnContainer);
        for (int i = 0; i < labels.size(); i++) {
            float cellTop = innerTop + i * cellHeight;
            float cellCenterY = cellTop + cellHeight / 2f;
            float baseline = cellCenterY - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(labels.get(i), pillRect.centerX(), baseline, textPaint);
        }

        // 4) 滑动悬浮气泡（Slider 风格，位于条左侧、跟随触点，绘制可超出本 View bounds）
        drawBubble(canvas);
    }

    /** 由格高推字号（dp）：随格距缩放，钳制在可读区间内 */
    private float clampCellTextSize(float cellHeightPx) {
        float sizePx = cellHeightPx * TEXT_SIZE_CELL_RATIO;
        return pxToDp(clamp(sizePx, dpToPx(TEXT_SIZE_MIN_DP), dpToPx(TEXT_SIZE_MAX_DP)));
    }

    // ==================== 触摸 ====================

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (labels.isEmpty()) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 拦截父容器：沿索引条拖动时不要触发列表滚动
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                selectAt(event.getY());
                showBubble(event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                selectAt(event.getY());
                updateBubble(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                clearSelection();
                scheduleHideBubble();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /** 按触点 y 命中分节；命中格变化才回调（DOWN 即首跳，MOVE 逐格节流） */
    private void selectAt(float y) {
        int index = indexAt(y);
        if (index == selectedIndex) {
            return;
        }
        selectedIndex = index;
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        invalidate();
        if (listener != null) {
            listener.onSectionPicked(index);
        }
    }

    private void clearSelection() {
        if (selectedIndex != -1) {
            selectedIndex = -1;
            invalidate();
        }
    }

    // ==================== 滑动悬浮气泡（Slider tooltip 风格）====================

    /** 手指落下：立即显示气泡并跟随触点（Slider 按下即出的语义） */
    private void showBubble(float y) {
        removeCallbacks(hideBubbleRunnable);
        updateBubble(y);
        animateBubbleTo(1f);
    }

    /** 滑动中：气泡垂直跟随触点，文字始终为当前命中章节 */
    private void updateBubble(float y) {
        if (selectedIndex < 0 || selectedIndex >= labels.size()) {
            return;
        }
        String label = labels.get(selectedIndex);
        float half = dpToPx(BUBBLE_HEIGHT_DP) / 2f;
        float cy = clamp(y, half, getHeight() - half);
        if (!label.equals(bubbleLabel) || cy != bubbleCenterY) {
            bubbleLabel = label;
            bubbleCenterY = cy;
            invalidate();
        }
    }

    /** 松手/取消：停留片刻后淡出（同 Slider 松手后 tooltip 的短暂停留） */
    private void scheduleHideBubble() {
        removeCallbacks(hideBubbleRunnable);
        postDelayed(hideBubbleRunnable, BUBBLE_HIDE_DELAY_MS);
    }

    /** 气泡透明度动画（淡入/淡出）；目标态与当前一致时不重复启动 */
    private void animateBubbleTo(float target) {
        if (bubbleAnimator != null) {
            bubbleAnimator.cancel();
        }
        if (bubbleAlpha == target) {
            return;
        }
        bubbleAnimator = ValueAnimator.ofFloat(bubbleAlpha, target);
        bubbleAnimator.setDuration(BUBBLE_ANIM_MS);
        bubbleAnimator.addUpdateListener(animation -> {
            bubbleAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        bubbleAnimator.start();
    }

    /** 绘制悬浮气泡：位于条左侧（超出本 View 左 bounds），垂直跟随触点 */
    private void drawBubble(Canvas canvas) {
        if (bubbleAlpha <= 0f || bubbleLabel.isEmpty()) {
            return;
        }
        float height = dpToPx(BUBBLE_HEIGHT_DP);
        textPaint.setTextSize(dpToPx(BUBBLE_TEXT_SIZE_SP));
        float width = Math.max(dpToPx(BUBBLE_MIN_WIDTH_DP),
                textPaint.measureText(bubbleLabel) + dpToPx(BUBBLE_H_PAD_DP) * 2f);
        float right = pillRect.left - dpToPx(BUBBLE_GAP_DP);
        float top = clamp(bubbleCenterY, height / 2f, getHeight() - height / 2f) - height / 2f;
        bubbleRect.set(right - width, top, right, top + height);
        float radius = height / 2f;

        // 底 + 投影：shadowLayer 对非文本绘制在硬件 Canvas 需 API 28+（minSdk 31 满足）
        backgroundPaint.setColor(colorBubble);
        backgroundPaint.setAlpha(Math.round(bubbleAlpha * 255));
        backgroundPaint.setShadowLayer(dpToPx(4f), 0f, dpToPx(1.5f), 0x59000000);
        canvas.drawRoundRect(bubbleRect, radius, radius, backgroundPaint);
        backgroundPaint.clearShadowLayer();
        backgroundPaint.setAlpha(255);

        // 标签居中（随淡入淡出同步透明度）
        textPaint.setColor(withAlpha(colorOnBubble, bubbleAlpha));
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float baseline = bubbleRect.centerY() - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(bubbleLabel, bubbleRect.centerX(), baseline, textPaint);
    }

    /** 触点 y → 分节下标：内容区按格均分，越界钳制到首末格（沿边缘滑动不落空） */
    private int indexAt(float y) {
        float innerTop = pillRect.top + pillRect.width() * INNER_V_PAD_RATIO;
        float innerBottom = pillRect.bottom - pillRect.width() * INNER_V_PAD_RATIO;
        if (pillRect.isEmpty() || innerBottom <= innerTop) {
            return 0; // 尚未完成首次绘制（理论不可达：触摸必在绘制后）
        }
        int index = (int) ((y - innerTop) / ((innerBottom - innerTop) / labels.size()));
        return clamp(index, labels.size() - 1);
    }

    // ==================== 主题色解析 ====================

    /** 解析当前主题配色（Monet 动态色 / 主题覆盖色均自动跟随） */
    private void resolveThemeColors() {
        // 条底色/字色：与通用卡片同源（?attr/GeneralCardViewBackground 系）
        colorContainer = resolveColorAttr(
                com.careful.HyperFVM.R.attr.GeneralCardViewBackground, 0xFFF3EDF7);
        colorOnContainer = resolveColorAttr(
                com.careful.HyperFVM.R.attr.TextViewColorOnGeneralCardView, 0xFF1C1B1F);
        // 悬浮气泡：主色对（?attr/colorPrimary / ?attr/colorOnPrimary）
        colorBubble = resolveColorAttr(
                com.google.android.material.R.attr.colorPrimary, 0xFF6750A4);
        colorOnBubble = resolveColorAttr(
                com.google.android.material.R.attr.colorOnPrimary, 0xFFFFFFFF);
    }

    /** 从当前主题解析颜色属性；解析不到或非颜色类型时回退到 M3 基准紫色 */
    private int resolveColorAttr(int attr, int fallback) {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(attr, value, true)) {
            return fallback;
        }
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return value.data;
        }
        if (value.resourceId != 0) {
            try {
                return androidx.core.content.ContextCompat.getColor(getContext(), value.resourceId);
            } catch (Exception ignored) {
                // 资源无法当颜色解析：走下方兜底
            }
        }
        return fallback;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    /** 返回指定透明度（0~1）下的颜色副本，用于气泡淡入淡出 */
    private static int withAlpha(int color, float alpha) {
        int a = Math.round(Color.alpha(color) * clamp(alpha, 0f, 1f));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private float pxToDp(float px) {
        return px / getResources().getDisplayMetrics().density;
    }
}
