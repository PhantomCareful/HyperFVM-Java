package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

/**
 * 越界回弹容器（复刻 fan.springback.view.SpringBackLayout 1.0.13.0）。
 *
 * <p>双路径与原版一致（互斥）：
 * <ul>
 *   <li><b>嵌套路径</b>（target 为 NestedScrollView）：实现 NestedScrollingParent3，
 *       touch/fling 越界 delta 由 onNestedScroll 线性累积、阻尼映射后移动容器自身，
 *       反向撤销经 onNestedPreScroll，停止时 springBack 回位（含 fling 到边缘的自动回弹）；</li>
 *   <li><b>touch 路径</b>（普通 ScrollView 等非嵌套 target）：拦截边缘手势，
 *       阻尼拖拽 + 松手零速回弹，动画中按住用 obtainTouchDistance 反阻尼重映射不跳变。</li>
 * </ul>
 *
 * <p>与原版的差异（有意为之）：不实现向上层的 NestedScrollingChild 协议（本容器为顶层）、
 * 无 OnSpringListener/滚动状态分派（无消费者）、屏幕尺寸用 DisplayMetrics。
 */
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3 {

    private static final String TAG = "SpringBackLayout";
    private static final int DIRECTION_VERTICAL = 2;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    private static final float VELOCITY_THRESHOLD = 2000f;

    private View mTarget;
    private final SpringScroller mSpringScroller;
    private boolean mSpringBackEnable = true;

    private boolean mIsBeingDragged;
    private int mActivePointerId = -1;
    private float mInitialDownY;
    private float mInitialMotionY;
    private final int mTouchSlop;

    private int mScreenWidth;
    private int mScreenHeight;

    // ---------------------------------------------------------------------
    // 嵌套滚动父协议状态（原版字段）：NestedScrollView 的越界 delta 接管
    // ---------------------------------------------------------------------
    private int mNestedScrollAxes;
    private boolean mNestedScrollInProgress;
    private boolean mNestedFlingInProgress;
    private boolean mScrollByFling;
    private float mTotalScrollTopUnconsumed;
    private float mTotalScrollBottomUnconsumed;
    private float mTotalFlingUnconsumed;
    private int mConsumeFlingCounter;
    // 原版速度来自 Miuix 自家 NestedScrollView 的 onNestedCurrentFling；
    // androidx NestedScrollView 无此回调 → 恒 0，fling 走原版的限幅累积分支
    private float mVelocityX;
    private float mVelocityY;
    private final int[] mNestedConsumedCompat = new int[2];
    // 回弹振动去重：同一次手势的嵌套/fling 多条回弹链路可能先后收口到 springBack，短窗口内只振一次
    private long mLastSpringBackHapticAt;
    private static final long SPRING_BACK_HAPTIC_DEBOUNCE_MS = 250;
    // touch 手势链起点时目标的边缘状态：区分"行进中撞边缘（回弹要振）"与
    // "已停稳在边缘后再拖出越界（回弹不振）"；fling 是同一条手势链的延续不覆盖
    private boolean mTargetStartedAtTop;
    private boolean mTargetStartedAtBottom;

    public SpringBackLayout(Context context) {
        this(context, null);
    }

    public SpringBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringBackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mSpringScroller = new SpringScroller();
        updateScreenSize();
    }

    /** 屏幕尺寸是阻尼 range 的来源，转屏/折叠形态变化时刷新（对齐原版 onConfigurationChanged）。 */
    private void updateScreenSize() {
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreenSize();
    }

    public void setTarget(View target) {
        mTarget = target;
    }

    public void setSpringBackEnable(boolean enable) {
        mSpringBackEnable = enable;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mTarget == null) {
            // 未显式 setTarget 时取第一个子 View（布局中只有一个可滚动内容 View）
            mTarget = getChildCount() > 0 ? getChildAt(0) : null;
        }
    }

    private View resolveTarget() {
        if (mTarget == null && getChildCount() > 0) {
            mTarget = getChildAt(0);
        }
        if (mTarget instanceof NestedScrollingChild) {
            // 原版 ensureTarget：启用嵌套上报，越界 delta 交给本容器接管；
            // 同时关闭系统 overscroll（边缘光晕/辉光与回弹冲突）
            NestedScrollingChild child = (NestedScrollingChild) mTarget;
            if (!child.isNestedScrollingEnabled()) {
                child.setNestedScrollingEnabled(true);
            }
            if (mTarget.getOverScrollMode() != View.OVER_SCROLL_NEVER && mSpringBackEnable) {
                mTarget.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
        return mTarget;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View target = resolveTarget();
        if (target != null) {
            measureChild(target, widthMeasureSpec, heightMeasureSpec);
        }
        int width, height;
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        if (wMode == MeasureSpec.UNSPECIFIED) {
            width = (target != null ? target.getMeasuredWidth() : 0) + getPaddingLeft() + getPaddingRight();
        } else if (wMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = Math.min(MeasureSpec.getSize(widthMeasureSpec),
                    (target != null ? target.getMeasuredWidth() : 0) + getPaddingLeft() + getPaddingRight());
        }
        if (hMode == MeasureSpec.UNSPECIFIED) {
            height = (target != null ? target.getMeasuredHeight() : 0) + getPaddingTop() + getPaddingBottom();
        } else if (hMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = Math.min(MeasureSpec.getSize(heightMeasureSpec),
                    (target != null ? target.getMeasuredHeight() : 0) + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View target = resolveTarget();
        if (target == null || target.getVisibility() == GONE) {
            return;
        }
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        target.layout(pl, pt, pl + target.getMeasuredWidth(), pt + target.getMeasuredHeight());
    }

    // ---------------------------------------------------------------------
    // 屏幕 range 与阻尼公式（与原版字节码一致）
    // ---------------------------------------------------------------------

    /** dir==2（竖直）用屏高，否则用屏宽（对齐原版 getSpringBackRange）。 */
    protected int getSpringBackRange(int dir) {
        return dir == DIRECTION_VERTICAL ? mScreenHeight : mScreenWidth;
    }

    /** 手指位移 → 容器位移（无符号幅值，方向由调用方还原）。 */
    protected float obtainSpringBackDistance(float delta, int dir) {
        int range = getSpringBackRange(dir);
        float f = Math.min(Math.abs(delta) / range, 1.0f);
        return obtainDampingDistance(f, range);
    }

    /** 最大阻尼位移（f=1 时 1/3·range）。 */
    protected float obtainMaxSpringBackDistance(int dir) {
        return obtainDampingDistance(1.0f, getSpringBackRange(dir));
    }

    /** 阻尼曲线：(f³/3 − f² + f) · range，与原版一致（返回值不带符号）。 */
    protected float obtainDampingDistance(float f, int range) {
        float min = Math.min(f, 1.0f);
        double damped = Math.pow(min, 3) / 3 - Math.pow(min, 2) + min;
        return (float) damped * range;
    }

    /**
     * 阻尼的精确反函数：给定容器位移（绝对值），反推等效手指位移。
     * 原版实现：range − range^(2/3)·(range − 3·v)^(1/3)（代数上等于 range·damp⁻¹(v/range)）。
     * 用于动画中按下/换指针时的基准重映射，避免位移跳变。
     */
    protected float obtainTouchDistance(float v, float max, int dir) {
        int range = getSpringBackRange(dir);
        if (Math.abs(v) >= Math.abs(max)) {
            v = max;
        }
        return (float) ((double) range - Math.pow(range, 2.0 / 3.0) * Math.pow(range - v * 3.0f, 1.0 / 3.0));
    }

    // ---------------------------------------------------------------------
    // 边界判定
    // ---------------------------------------------------------------------

    private boolean isTargetScrollToTop() {
        View target = resolveTarget();
        if (target == null) return true;
        if (target instanceof ListView) {
            return !((ListView) target).canScrollList(-1);
        }
        return !target.canScrollVertically(-1);
    }

    private boolean isTargetScrollToBottom() {
        View target = resolveTarget();
        if (target == null) return true;
        if (target instanceof ListView) {
            return !((ListView) target).canScrollList(1);
        }
        return !target.canScrollVertically(1);
    }

    // ---------------------------------------------------------------------
    // 拦截
    // ---------------------------------------------------------------------

    /**
     * 原版启用时会吞掉子 View 的 disallow 请求（requestDisallowInterceptTouchEvent
     * 只在 disabled 时才 super），保证边缘拖拽随时可接管——这里保持一致。
     */
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (!isEnabled() || !mSpringBackEnable) {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean canSpringBack() {
        // 目标是 NestedScrolling 子 View 时原版走嵌套协议路径，本实现不支持
        return !mSpringBackEnable || !isEnabled() || resolveTarget() == null
                || mTarget.isNestedScrollingEnabled();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (canSpringBack()) {
            return false;
        }
        final int action = ev.getActionMasked();
        if (!mSpringScroller.isFinished() && action == MotionEvent.ACTION_DOWN) {
            mSpringScroller.forceStop();
        }
        // 中段既不在顶也不在底时不拦截（内容正常滚动）
        if (!isTargetScrollToTop() && !isTargetScrollToBottom()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(0);
                // 非嵌套（touch 路径）手势起点的边缘状态；嵌套页随后由 accepted 覆盖（同值）
                mTargetStartedAtTop = isTargetScrollToTop();
                mTargetStartedAtBottom = isTargetScrollToBottom();
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) return false;
                mInitialDownY = ev.getY(idx);
                if (getScrollY() != 0) {
                    // 越界动画进行中按下：接管拖拽（精确重映射在 onTouchEvent 的
                    // checkScrollStart 中用反阻尼完成）
                    mIsBeingDragged = true;
                    mInitialMotionY = mInitialDownY;
                } else {
                    mIsBeingDragged = false;
                }
                return mIsBeingDragged;
            }
            case MotionEvent.ACTION_MOVE: {
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                float y = ev.getY(idx);
                if (mIsBeingDragged) {
                    return true;
                }
                float dy = y - mInitialDownY;
                boolean bothEdges = isTargetScrollToTop() && isTargetScrollToBottom();
                boolean pullDown = (!bothEdges && isTargetScrollToTop()) || (bothEdges && dy > 0);
                if (pullDown ? dy > mTouchSlop : -dy > mTouchSlop) {
                    mIsBeingDragged = true;
                    // 基准设为当前触点：拖拽位移从 0 开始，消除 touchSlop 段的跳变
                    mInitialMotionY = y;
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                mActivePointerId = -1;
                return false;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(ev);
                return false;
            }
            default:
                return false;
        }
    }

    // ---------------------------------------------------------------------
    // 触摸处理
    // ---------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (canSpringBack()) {
            return false;
        }
        final int action = ev.getActionMasked();
        if (!mSpringScroller.isFinished() && action == MotionEvent.ACTION_DOWN) {
            mSpringScroller.forceStop();
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(0);
                checkScrollStart();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    handleDrag(ev.getY(idx));
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                // 切换到新指针并平移基准，使已积累的位移保持连续（对齐原版）
                int activeIdx = ev.findPointerIndex(mActivePointerId);
                if (activeIdx < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid active pointer id.");
                    return false;
                }
                int actionIndex = ev.getActionIndex();
                if (actionIndex < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                float delta = ev.getY(activeIdx) - mInitialDownY;
                float newInitial = ev.getY(actionIndex) - delta;
                mInitialDownY = newInitial;
                mInitialMotionY = newInitial;
                mActivePointerId = ev.getPointerId(actionIndex);
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(ev);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (ev.findPointerIndex(mActivePointerId) < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(DIRECTION_VERTICAL);
                }
                mActivePointerId = -1;
                return false;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(DIRECTION_VERTICAL);
                }
                mActivePointerId = -1;
                return false;
            }
            default:
                return false;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int actionIndex = ev.getActionIndex();
        if (ev.getPointerId(actionIndex) == mActivePointerId) {
            mActivePointerId = ev.getPointerId(actionIndex == 0 ? 1 : 0);
        }
    }

    /**
     * 动画中按下（或换手指）时的基准重映射：
     * 用反阻尼把当前容器偏移换算回等效手指位移，使下一拍 drag 的 delta
     * 恰好重现当前偏移——按住无跳变（对齐原版 checkVerticalScrollStart）。
     */
    private void checkScrollStart() {
        int scrollY = getScrollY();
        if (scrollY != 0) {
            mIsBeingDragged = true;
            float touch = obtainTouchDistance(
                    Math.abs(scrollY),
                    Math.abs(obtainMaxSpringBackDistance(DIRECTION_VERTICAL)),
                    DIRECTION_VERTICAL);
            if (scrollY < 0) {
                mInitialDownY -= touch;
            } else {
                mInitialDownY += touch;
            }
            mInitialMotionY = mInitialDownY;
        } else {
            mIsBeingDragged = false;
        }
    }

    /**
     * 阻尼拖拽（统一处理顶部下拉/底部上拉/中段兜底，与原版三分支等价）：
     * desired = 容器 scrollY 目标；越过当前边界时归零并交还控制权。
     */
    private void handleDrag(float y) {
        float delta = y - mInitialMotionY;
        float damped = obtainSpringBackDistance(delta, DIRECTION_VERTICAL);
        float desired = -Math.signum(delta) * damped;
        boolean atTop = isTargetScrollToTop();
        boolean atBottom = isTargetScrollToBottom();
        // 顶部只接受负偏移（下拉）、底部只接受正偏移（上推）；越过边界仅归零本拍位移，
        // 拖拽状态与活动指针保持不动（对齐原版 moveTarget(0) + return false）——
        // 手指反向回穿边界时下一拍立即恢复跟手，快速上下交替不丢手势
        if (atTop && !atBottom && desired >= 0) {
            if (getScrollY() != 0) {
                scrollTo(0, 0);
            }
            return;
        }
        if (atBottom && !atTop && desired <= 0) {
            if (getScrollY() != 0) {
                scrollTo(0, 0);
            }
            return;
        }
        requestDisallowParentInterceptTouchEvent(true);
        scrollTo(0, (int) desired);
    }

    /** 把沿手势链的 disallow 请求向上传给真正的父容器（原版 requestDisallowParentInterceptTouchEvent）。 */
    private void requestDisallowParentInterceptTouchEvent(boolean disallow) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallow);
        }
    }

    // ---------------------------------------------------------------------
    // 嵌套滚动父协议（NestedScrollingParent3，移植原版核心）：
    // NestedScrollView 的 touch/fling 越界 delta 经此接管 → 容器阻尼位移 → 停止时回弹
    // ---------------------------------------------------------------------

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollAxes = axes;
        boolean vertical = axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        if (!vertical) {
            return false; // 仅竖直（原版 scrollOrientation 恒 VERTICAL）
        }
        if (mSpringBackEnable) {
            if (!isEnabled()) {
                return false;
            }
            // fling 启动时容器仍有越界位移则拒绝（对齐原版，避免打断进行中的回弹）
            if (type != ViewCompat.TYPE_TOUCH && getScrollY() != 0 && target instanceof NestedScrollView) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes) {
        return onStartNestedScroll(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (mSpringBackEnable) {
            if (type == ViewCompat.TYPE_TOUCH) {
                // 记录 touch 手势链起点的边缘状态（fling NON_TOUCH 不覆盖：同链延续）
                mTargetStartedAtTop = isTargetScrollToTop();
                mTargetStartedAtBottom = isTargetScrollToBottom();
            }
            boolean vertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
            int dir = vertical ? DIRECTION_VERTICAL : 1;
            float scroll = vertical ? getScrollY() : getScrollX();
            if (type != ViewCompat.TYPE_TOUCH) {
                // fling 开始：反阻尼初始化未消费累积量
                mTotalFlingUnconsumed = scroll == 0 ? 0
                        : obtainTouchDistance(Math.abs(scroll),
                        Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                mNestedFlingInProgress = true;
                mConsumeFlingCounter = 0;
            } else {
                if (scroll == 0) {
                    mTotalScrollTopUnconsumed = 0;
                    mTotalScrollBottomUnconsumed = 0;
                } else if (scroll < 0) {
                    mTotalScrollTopUnconsumed = obtainTouchDistance(Math.abs(scroll),
                            Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                    mTotalScrollBottomUnconsumed = 0;
                } else {
                    mTotalScrollTopUnconsumed = 0;
                    mTotalScrollBottomUnconsumed = obtainTouchDistance(Math.abs(scroll),
                            Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                }
                mNestedScrollInProgress = true;
            }
            mScrollByFling = false;
            mSpringScroller.forceStop();
        }
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (!mSpringBackEnable) {
            return;
        }
        boolean vertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        int dir = vertical ? DIRECTION_VERTICAL : 1;
        int unconsumed = vertical ? dyUnconsumed : dxUnconsumed;
        if (unconsumed == 0) {
            return;
        }
        if (vertical && unconsumed < 0 && isTargetScrollToTop()) {
            if (type != ViewCompat.TYPE_TOUCH) {
                // fling 撞顶：限幅累积越界（mVelocity 恒 0 → 原版限幅分支）
                float max = obtainMaxSpringBackDistance(dir);
                if (mVelocityY == 0 && mVelocityX == 0) {
                    if (mTotalScrollTopUnconsumed == 0) {
                        float remain = max - mTotalFlingUnconsumed;
                        if (mConsumeFlingCounter < MAX_FLING_CONSUME_COUNTER) {
                            if (remain <= Math.abs(unconsumed)) {
                                mTotalFlingUnconsumed += remain;
                                consumed[vertical ? 1 : 0] += (int) remain;
                            } else {
                                mTotalFlingUnconsumed += Math.abs(unconsumed);
                                consumed[vertical ? 1 : 0] += unconsumed;
                            }
                            moveTarget(obtainSpringBackDistance(mTotalFlingUnconsumed, dir), dir);
                            ++mConsumeFlingCounter;
                        }
                    }
                } else {
                    mScrollByFling = true;
                    int consumedMain = vertical ? dyConsumed : dxConsumed;
                    if (consumedMain != 0 && -unconsumed <= max) {
                        mSpringScroller.setFirstStep(unconsumed);
                    }
                }
            } else if (mSpringScroller.isFinished()) {
                // touch 下拉：手指位移线性累积，阻尼映射移动容器
                mTotalScrollTopUnconsumed += Math.abs(unconsumed);
                moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, dir), dir);
                consumed[vertical ? 1 : 0] += unconsumed;
            }
        } else if (vertical && unconsumed > 0 && isTargetScrollToBottom()) {
            if (type != ViewCompat.TYPE_TOUCH) {
                float max = obtainMaxSpringBackDistance(dir);
                if (mVelocityY == 0 && mVelocityX == 0) {
                    if (mTotalScrollBottomUnconsumed == 0) {
                        float remain = max - mTotalFlingUnconsumed;
                        if (mConsumeFlingCounter < MAX_FLING_CONSUME_COUNTER) {
                            if (remain <= Math.abs(unconsumed)) {
                                mTotalFlingUnconsumed += remain;
                                consumed[vertical ? 1 : 0] += (int) remain;
                            } else {
                                mTotalFlingUnconsumed += Math.abs(unconsumed);
                                consumed[vertical ? 1 : 0] += unconsumed;
                            }
                            moveTarget(-obtainSpringBackDistance(mTotalFlingUnconsumed, dir), dir);
                            ++mConsumeFlingCounter;
                        }
                    }
                } else {
                    mScrollByFling = true;
                    int consumedMain = vertical ? dyConsumed : dxConsumed;
                    if (consumedMain != 0 && unconsumed <= max) {
                        mSpringScroller.setFirstStep(unconsumed);
                    }
                }
            } else if (mSpringScroller.isFinished()) {
                mTotalScrollBottomUnconsumed += Math.abs(unconsumed);
                moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, dir), dir);
                consumed[vertical ? 1 : 0] += unconsumed;
            }
        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                type, mNestedConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                ViewCompat.TYPE_TOUCH, mNestedConsumedCompat);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (!mSpringBackEnable) {
            return;
        }
        if (mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            nestedPreScrollVertical(dy, consumed, type);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    /** 原版私有 onNestedPreScroll(int, int[], int)（竖直）：撤销越界位移 / fling 同步回弹。 */
    private void nestedPreScrollVertical(int dy, int[] consumed, int type) {
        boolean vertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        int dir = vertical ? DIRECTION_VERTICAL : 1;
        int absScroll = Math.abs(vertical ? getScrollY() : getScrollX());
        float leftover = 0f;
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dy > 0) {
                // 手指上推：先撤销顶部越界（线性、逐帧 → 跟手关键）
                float top = mTotalScrollTopUnconsumed;
                if (top > 0) {
                    if (dy > top) {
                        consumeDelta((int) top, consumed, dir);
                        mTotalScrollTopUnconsumed = 0;
                    } else {
                        mTotalScrollTopUnconsumed = top - dy;
                        consumeDelta(dy, consumed, dir);
                    }
                    moveTarget(obtainSpringBackDistance(mTotalScrollTopUnconsumed, dir), dir);
                    return;
                }
            }
            if (dy < 0) {
                // 手指下拉：撤销底部越界
                float bottom = mTotalScrollBottomUnconsumed;
                if (bottom > 0) {
                    if (-dy > bottom) {
                        consumeDelta((int) bottom, consumed, dir);
                        mTotalScrollBottomUnconsumed = 0;
                    } else {
                        mTotalScrollBottomUnconsumed = bottom + dy;
                        consumeDelta(dy, consumed, dir);
                    }
                    moveTarget(-obtainSpringBackDistance(mTotalScrollBottomUnconsumed, dir), dir);
                }
            }
        } else {
            float velocity = dir == 2 ? mVelocityY : mVelocityX;
            if (dy > 0) {
                float top = mTotalScrollTopUnconsumed;
                if (top > 0) {
                    if (velocity > VELOCITY_THRESHOLD) {
                        float damped = obtainSpringBackDistance(top, dir);
                        if (dy > damped) {
                            consumeDelta((int) damped, consumed, dir);
                            mTotalScrollTopUnconsumed = 0;
                        } else {
                            consumeDelta(dy, consumed, dir);
                            leftover = damped - dy;
                            mTotalScrollTopUnconsumed = obtainTouchDistance(leftover,
                                    Math.signum(leftover) * Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                        }
                        moveTarget(leftover, dir);
                        return;
                    }
                    if (!mScrollByFling) {
                        mScrollByFling = true;
                        springBack(velocity, dir, false);
                    }
                    if (mSpringScroller.computeScrollOffset()) {
                        scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                        mTotalScrollTopUnconsumed = obtainTouchDistance(absScroll,
                                Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                    } else {
                        mTotalScrollTopUnconsumed = 0;
                    }
                    consumeDelta(dy, consumed, dir);
                    return;
                }
            }
            if (dy < 0) {
                float bottom = mTotalScrollBottomUnconsumed;
                if (bottom > 0) {
                    if (velocity < -VELOCITY_THRESHOLD) {
                        float damped = obtainSpringBackDistance(bottom, dir);
                        if (-dy > damped) {
                            consumeDelta((int) damped, consumed, dir);
                            mTotalScrollBottomUnconsumed = 0;
                        } else {
                            consumeDelta(dy, consumed, dir);
                            leftover = damped + dy;
                            mTotalScrollBottomUnconsumed = obtainTouchDistance(leftover,
                                    Math.signum(leftover) * Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                        }
                        moveTarget(-leftover, dir);
                        return;
                    }
                    if (!mScrollByFling) {
                        mScrollByFling = true;
                        springBack(velocity, dir, false);
                    }
                    if (mSpringScroller.computeScrollOffset()) {
                        scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                        mTotalScrollBottomUnconsumed = obtainTouchDistance(absScroll,
                                Math.abs(obtainMaxSpringBackDistance(dir)), dir);
                    } else {
                        mTotalScrollBottomUnconsumed = 0;
                    }
                    consumeDelta(dy, consumed, dir);
                    return;
                }
            }
            if (dy != 0 && (mTotalScrollBottomUnconsumed == 0 || mTotalScrollTopUnconsumed == 0)
                    && mScrollByFling && getScrollY() == 0) {
                consumeDelta(dy, consumed, dir);
            }
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull View child, int type) {
        if (mSpringBackEnable) {
            boolean vertical = mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
            int dir = vertical ? DIRECTION_VERTICAL : 1;
            float scroll = vertical ? getScrollY() : getScrollX();
            if (mNestedScrollInProgress) {
                // touch 滚动结束：容器仍越界则回弹（松手回弹主入口）
                mNestedScrollInProgress = false;
                if (!mNestedFlingInProgress && scroll != 0) {
                    springBack(dir);
                } else if (scroll != 0) {
                    stopNestedFlingScroll(dir);
                }
            } else if (mNestedFlingInProgress) {
                // fling 结束：容器仍越界则回弹（用力滑到边缘的自动回弹）
                stopNestedFlingScroll(dir);
            }
        }
    }

    @Override
    public void onStopNestedScroll(@NonNull View child) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH);
    }

    /** 原版 stopNestedFlingScroll：mVelocity 恒 0 → mScrollByFling 不会置位 → 零速 springBack。 */
    private void stopNestedFlingScroll(int dir) {
        mNestedFlingInProgress = false;
        if (mScrollByFling) {
            if (mSpringScroller.isFinished()) {
                springBack(dir == 2 ? mVelocityY : mVelocityX, dir, false);
            }
            postInvalidateOnAnimation();
        } else {
            springBack(dir);
        }
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return false;
    }

    /** 原版 consumeDelta：竖直写 consumed[1]。 */
    private void consumeDelta(int delta, int[] consumed, int dir) {
        consumed[dir == 2 ? 1 : 0] = delta;
    }

    /** 原版 moveTarget：容器自身 scrollTo 产生越界位移（dir==2 → 竖直）。 */
    private void moveTarget(float distance, int dir) {
        if (dir == DIRECTION_VERTICAL) {
            scrollTo(0, (int) -distance);
        } else {
            scrollTo((int) -distance, 0);
        }
    }

    // ---------------------------------------------------------------------
    // 回弹
    // ---------------------------------------------------------------------

    /** 零初速回弹到 0（touch 路径的松手/取消、嵌套路径的停止回位）。 */
    private void springBack(int direction) {
        springBack(0f, direction, true);
    }

    /** 原版 springBack(float, int, boolean)：forceStop 当前动画后带初速启动回弹。 */
    private void springBack(float velocity, int direction, boolean invalidate) {
        int x = getScrollX();
        int y = getScrollY();
        if (x == 0 && y == 0 && velocity == 0f) {
            return; // 原版会启动一个立即完成的空动画并分派 state 0，这里等价早退
        }
        // 触发回弹效果时的振动反馈（对齐 HyperCeiler 观感）：仅"行进中撞到边缘"的回弹
        // 才振（手势链起点不在该边缘）；已停稳在边缘后再拖出的越界回弹不振（起点即在该边缘）
        boolean haptic;
        if (y < 0) {
            haptic = !mTargetStartedAtTop;
        } else if (y > 0) {
            haptic = !mTargetStartedAtBottom;
        } else {
            haptic = !mTargetStartedAtTop && !mTargetStartedAtBottom;
        }
        long now = SystemClock.uptimeMillis();
        if (haptic && now - mLastSpringBackHapticAt > SPRING_BACK_HAPTIC_DEBOUNCE_MS) {
            mLastSpringBackHapticAt = now;
            performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
        mSpringScroller.forceStop();
        mSpringScroller.scrollByFling(x, 0, y, 0, velocity, direction, false);
        if (invalidate) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public void computeScroll() {
        if (!mSpringScroller.isFinished()) {
            if (mSpringScroller.computeScrollOffset()) {
                scrollTo(mSpringScroller.getCurrX(), mSpringScroller.getCurrY());
                if (!mSpringScroller.isFinished()) {
                    postInvalidateOnAnimation();
                }
            }
        }
    }
}
