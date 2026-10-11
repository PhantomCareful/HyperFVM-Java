package com.careful.HyperFVM.utils.ForDesign.Scroll;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import com.careful.HyperFVM.BuildConfig;

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
 * <p>作用轴：默认竖直；{@link #setScrollOrientation(int)} 可把 touch 路径切到水平
 * （供 HorizontalScrollView 包裹使用，如卡牌详情星级表）；嵌套路径仅服务竖直容器。
 *
 * <p>与原版的差异（有意为之）：向上层同步实现 NestedScrollingChild3 协议（把本容器
 * 消费后剩余的嵌套量转发给上层，如 Miuix BottomSheet 弹层跟手）、无 OnSpringListener/
 * 滚动状态分派（无消费者）、屏幕尺寸用 DisplayMetrics。
 */
public class SpringBackLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3 {

    private static final String TAG = "SpringBackLayout";
    /** 作用轴/方向（与 SpringScroller 的 orientation 语义一致）：2=竖直，1=水平 */
    public static final int DIRECTION_VERTICAL = 2;
    public static final int DIRECTION_HORIZONTAL = 1;
    private static final int MAX_FLING_CONSUME_COUNTER = 4;
    private static final float VELOCITY_THRESHOLD = 2000f;
    /** fling 撞界回弹的初始越界量（dp）：先造一段越界位移再 springBack，撞墙呈橡皮筋而非硬停 */
    private static final float EDGE_BOUNCE_KICK_DP = 20f;
    /** touch 路径松手甩出速度上限（px/s）：限制 springBack 初速的过冲幅度，避免短内容被甩出过远 */
    private static final float MAX_RELEASE_FLING = 3000f;

    private View mTarget;
    private final SpringScroller mSpringScroller;
    private boolean mSpringBackEnable = true;
    /** 回弹作用轴：默认竖直；touch 路径可切水平（嵌套路径恒竖直） */
    private int mOrientation = DIRECTION_VERTICAL;

    private boolean mIsBeingDragged;
    private int mActivePointerId = -1;
    // 手势基准坐标（随作用轴存 Y/X）
    private float mInitialDownPos;
    private float mInitialMotionPos;
    // 手势是否已进入"目标在边缘"状态：行进中刚到达边缘时以当前触点重设方向基准，
    // 否则方向判定要先回拖过整段行进位移才够 touchSlop，同手势中途到边无法越界拖拽
    private boolean mEdgeEligible;
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
    /** 向上层（祖父容器）转发嵌套量的 helper：本容器消费后把剩余量继续上抛 */
    private final NestedScrollingChildHelper mChildHelper;
    /** dispatchNestedPreScroll 的上抛消费数组（复用避免每帧分配） */
    private final int[] mNestedUpConsumed = new int[2];
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
    // 触摸路径诊断日志限流（仅 debug 构建生效，定位拦截链用）
    private long mLastTouchLogAt;
    // 手势链起点时目标的边缘状态（start=顶/左，end=底/右）：区分"行进中撞边缘（回弹要振）"与
    // "已停稳在边缘后再拖出越界（回弹不振）"；fling 是同一条手势链的延续不覆盖
    private boolean mTargetStartedAtStart;
    private boolean mTargetStartedAtEnd;
    // ---------------------------------------------------------------------
    // fling 撞界回弹（touch 路径补位）：target 的惯性滚动不经本容器 touch 事件，
    // 撞边界即硬停（HSV overscroll=NEVER）——经 target 滚动监听补一次越界+回弹
    // ---------------------------------------------------------------------
    private boolean mEdgeFlingBounce;
    /** 本实例是否收到过手势 DOWN：排除 setSelection 等程序化滚动误触发回弹 */
    private boolean mSawDown;
    /** 已挂撞界回弹监听的 target（防重复挂载/覆盖） */
    private View mEdgeBounceWatched;
    private final View.OnScrollChangeListener mEdgeBounceListener = this::onTargetScrolled;
    // 撞界方向由边缘状态差分判定（非边缘→边缘的那次回调 = 撞界帧）：AbsListView 的
    // invokeOnItemScrollListener 回调 onScrollChanged(0,0,0,0)（AOSP dummy values），
    // 坐标差分对菜单 DropDownListView 恒为 0——旧 delta 判定在菜单上从未触发过
    private boolean mEdgePrevAtStart;
    private boolean mEdgePrevAtEnd;
    private boolean mEdgePrevValid;
    // ---------------------------------------------------------------------
    // 接管期事件并行转发（对齐嵌套路径观感）：target 是非嵌套子时手势被本容器独占，
    // 容器归零交还后 target 收不到事件，无法继续滚动/按速度 fling——接管期间把完整
    // 事件序列（缺 DOWN 则合成补发）并行转发给 target，由它原生处理恢复方向滚动
    // 与快甩惯性；自身仍按越界端状态机驱动容器位移，两边互不干扰
    // ---------------------------------------------------------------------
    /** 本手势越界端：-1=起始侧(顶/左)、1=终止侧(底/右)、0=容器已归 0（滚动交由 target） */
    private int mOverdragSide;
    /** target 是否已收到本手势 DOWN（真实或合成）：决定转发序列是否完整 */
    private boolean mTargetHasDown;
    /** 合成 DOWN 触点（原始坐标系）：UP 位移未超 slop 则改发 CANCEL，防重放误触发点击；兼作越界段钉点 */
    private float mTargetDownX;
    private float mTargetDownY;
    /** side 变化时给 target 重放 CANCEL+DOWN 换基准：断层藏进 DOWN，避免坐标跳变产生瞬移滚动 */
    private boolean mTargetReplayDown;
    /** target 视角是否收到过超 slop 的真实坐标 MOVE（已进入滚动状态）：钉住段 target 持续收
     *  静止 MOVE，mTouchMode 停在 TAP——UP 若发出会被 AbsListView 以 pointToPosition(UP)
     *  判成点击（命中手指位置的条目）；未真实滑动过则收口只能发 CANCEL */
    private boolean mTargetScrolled;
    /** touch 路径松手速度采集（仅本容器接管的手势维护，UP/CANCEL 后释放） */
    private VelocityTracker mVelocityTracker;

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
        mChildHelper = new NestedScrollingChildHelper(this);
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

    /** 设置回弹作用轴（仅 touch 路径生效；应在首次触摸前调用）。 */
    public void setScrollOrientation(int orientation) {
        mOrientation = (orientation == DIRECTION_HORIZONTAL)
                ? DIRECTION_HORIZONTAL : DIRECTION_VERTICAL;
    }

    /**
     * 开启/关闭 fling 惯性撞边界回弹：target（HSV/ListView 等非嵌套子）的惯性滚动
     * 不经过本容器 touch 路径，撞边界直接硬停；开启后经 target 滚动监听在撞界瞬间
     * 造一小段越界位移再走标准 springBack（含振动）。手指拖拽越界仍由 touch 路径处理，
     * 程序化滚动（setSelection 等）因实例未收到过手势 DOWN 不触发。
     */
    public void setEdgeFlingBounceEnable(boolean enable) {
        mEdgeFlingBounce = enable;
        if (enable) {
            ensureEdgeBounceWatch();
        } else if (mEdgeBounceWatched != null) {
            mEdgeBounceWatched.setOnScrollChangeListener(null);
            mEdgeBounceWatched = null;
        }
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
            // 原版 ensureTarget：启用嵌套上报，越界 delta 交给本容器接管
            NestedScrollingChild child = (NestedScrollingChild) mTarget;
            if (!child.isNestedScrollingEnabled()) {
                child.setNestedScrollingEnabled(true);
            }
        }
        if (mSpringBackEnable && mTarget.getOverScrollMode() != View.OVER_SCROLL_NEVER) {
            // 关闭系统 overscroll：边缘光晕/辉光与回弹冲突（touch 路径的
            // 普通 ScrollView / ListView / HorizontalScrollView 同样要关）
            mTarget.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        ensureEdgeBounceWatch();
        return mTarget;
    }

    /** 给 target 挂 fling 撞界回弹监听（幂等：target 变更时先摘旧监听再挂新的）。 */
    private void ensureEdgeBounceWatch() {
        if (!mEdgeFlingBounce || mTarget == null || mEdgeBounceWatched == mTarget) {
            return;
        }
        if (mEdgeBounceWatched != null) {
            mEdgeBounceWatched.setOnScrollChangeListener(null);
        }
        mTarget.setOnScrollChangeListener(mEdgeBounceListener);
        mEdgeBounceWatched = mTarget;
        mEdgePrevValid = false; // 换 target：边缘状态基线重取
    }

    /**
     * target 滚动回调：fling 惯性把内容推到边界时（无手指、非拖拽、容器无越界位移），
     * 补一次撞界回弹。方向由边缘状态转变判定（非边缘→边缘 = 撞界那一帧），不用回调
     * 坐标差分——AbsListView（菜单 DropDownListView）滚动经 invokeOnItemScrollListener
     * 回调 onScrollChanged(0,0,0,0)（AOSP dummy values），坐标差分恒 0 永不触发；状态
     * 转变判定对 HSV 的真实坐标回调同样成立。状态记录不受前置条件排除影响（手指在屏
     * 的拖拽帧也要记账，否则后续首次滚动会把陈旧基线误判为撞界），仅判定受门控。
     */
    private void onTargetScrolled(View v, int sx, int sy, int osx, int osy) {
        boolean atStart = isTargetAtStart();
        boolean atEnd = isTargetAtEnd();
        boolean crossedStart = mEdgePrevValid && !mEdgePrevAtStart && atStart;
        boolean crossedEnd = mEdgePrevValid && !mEdgePrevAtEnd && atEnd;
        mEdgePrevAtStart = atStart;
        mEdgePrevAtEnd = atEnd;
        mEdgePrevValid = true;
        if (!mEdgeFlingBounce || !mSpringBackEnable || !isEnabled() || !mSawDown) {
            return;
        }
        if (mIsBeingDragged || mActivePointerId >= 0) {
            return; // 手指在屏：越界拖拽由 touch 路径负责
        }
        if (axisScroll() != 0 || !mSpringScroller.isFinished() || mNestedScrollInProgress) {
            return; // 越界/回弹进行中，或嵌套路径自会收口
        }
        if (crossedEnd || crossedStart) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "edgeCross end=" + crossedEnd + " start=" + crossedStart
                        + " scroll=" + axisScroll());
            }
            bounceFromEdge(crossedEnd);
        }
    }

    /**
     * fling 撞界回弹：终点侧造正向越界、起始侧造负向越界（与 handleDrag 的接受区间一致），
     * 随即零速 springBack——撞墙从硬停变成短暂橡皮筋 + 振动。
     */
    private void bounceFromEdge(boolean atEnd) {
        int kick = (int) (EDGE_BOUNCE_KICK_DP * getResources().getDisplayMetrics().density);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "edgeBounce atEnd=" + atEnd + " kick=" + kick);
        }
        scrollAxisTo(atEnd ? kick : -kick);
        springBack(mOrientation);
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
    // 边界判定与作用轴辅助
    // ---------------------------------------------------------------------

    /** 目标是否已到"起始"边缘（竖直=顶、水平=左）。 */
    private boolean isTargetAtStart() {
        View target = resolveTarget();
        if (target == null) return true;
        if (mOrientation == DIRECTION_VERTICAL) {
            if (target instanceof ListView) {
                return !((ListView) target).canScrollList(-1);
            }
            return !target.canScrollVertically(-1);
        }
        return !target.canScrollHorizontally(-1);
    }

    /** 目标是否已到"终止"边缘（竖直=底、水平=右）。 */
    private boolean isTargetAtEnd() {
        View target = resolveTarget();
        if (target == null) return true;
        if (mOrientation == DIRECTION_VERTICAL) {
            if (target instanceof ListView) {
                return !((ListView) target).canScrollList(1);
            }
            return !target.canScrollVertically(1);
        }
        return !target.canScrollHorizontally(1);
    }

    /** 取手势坐标在作用轴上的分量（竖直=Y、水平=X）。 */
    private float axisOf(MotionEvent ev, int pointerIndex) {
        return mOrientation == DIRECTION_VERTICAL
                ? ev.getY(pointerIndex) : ev.getX(pointerIndex);
    }

    /** 容器在作用轴上的当前偏移（另一轴恒 0）。 */
    private int axisScroll() {
        return mOrientation == DIRECTION_VERTICAL ? getScrollY() : getScrollX();
    }

    /** 把作用轴偏移落到指定值。 */
    private void scrollAxisTo(int value) {
        if (mOrientation == DIRECTION_VERTICAL) {
            scrollTo(0, value);
        } else {
            scrollTo(value, 0);
        }
    }

    // ---------------------------------------------------------------------
    // 拦截
    // ---------------------------------------------------------------------

    /**
     * 原版语义仅适用“容器即根”场景：吞掉子请求保证边缘拖拽随时可接管。本容器作为
     * 中间层时（如 HorizontalScrollView 包裹层）必须把请求继续向父链传递——HSV 横滑
     * 超 slop 时调 mParent.requestDisallowInterceptTouchEvent(true) 禁掉外层
     * NestedScrollView 的竖直拦截，若在此被吞，横滑会频繁被竖直滚动打断；
     * 自身不置 DISALLOW 标志（不调 super），边缘越界拖拽仍保持随时可接管。
     */
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (!isEnabled() || !mSpringBackEnable) {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
            return;
        }
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
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
        // 松手甩动速度采集：intercept 每拍必经且先于分发，序列完整（仅 touch 路径走到这里）
        if (action == MotionEvent.ACTION_DOWN) {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
            }
            mVelocityTracker = VelocityTracker.obtain();
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }
        if (BuildConfig.DEBUG && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL)) {
            View probe = resolveTarget();
            Log.d(TAG, "icpt " + action + " canSB=" + canSpringBack() + " target="
                    + (probe == null ? "null" : probe.getClass().getName()) + " scroll=" + axisScroll()
                    + " enable=" + mSpringBackEnable + "/" + isEnabled());
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActivePointerId = ev.getPointerId(0);
                mSawDown = true;
                // 新手势开始：target 必未收到本手势 DOWN（真实分发尚未发生），接管时需合成补发
                mTargetHasDown = false;
                // 手势链起点的边缘状态（回弹振动判定用；嵌套页随后由 accepted 覆盖同值）
                mTargetStartedAtStart = isTargetAtStart();
                mTargetStartedAtEnd = isTargetAtEnd();
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) return false;
                mInitialDownPos = axisOf(ev, idx);
                // 起点即在边缘（或越界动画进行中）才纳入边缘判定
                mEdgeEligible = mTargetStartedAtStart || mTargetStartedAtEnd;
                if (axisScroll() != 0) {
                    // 越界动画进行中按下：接管拖拽（精确重映射在 onTouchEvent 的
                    // checkScrollStart 中用反阻尼完成）
                    mIsBeingDragged = true;
                    mInitialMotionPos = mInitialDownPos;
                } else {
                    mIsBeingDragged = false;
                }
                return mIsBeingDragged;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId < 0) {
                    return false;
                }
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                if (mIsBeingDragged) {
                    return true;
                }
                if (BuildConfig.DEBUG && SystemClock.uptimeMillis() - mLastTouchLogAt > 300) {
                    mLastTouchLogAt = SystemClock.uptimeMillis();
                    Log.d(TAG, "icpt MOVE atS=" + isTargetAtStart() + " atE=" + isTargetAtEnd()
                            + " edge=" + mEdgeEligible + " pos=" + axisOf(ev, idx)
                            + " down=" + mInitialDownPos + " activeId=" + mActivePointerId);
                }
                float pos = axisOf(ev, idx);
                boolean atStart = isTargetAtStart();
                boolean atEnd = isTargetAtEnd();
                // 两端都不在边缘时不拦截（内容正常滚动）
                if (!atStart && !atEnd) {
                    mEdgeEligible = false;
                    return false;
                }
                if (!mEdgeEligible) {
                    // 行进中刚到达边缘：以当前触点重设方向判定基准
                    mEdgeEligible = true;
                    mInitialDownPos = pos;
                }
                float delta = pos - mInitialDownPos;
                boolean bothEdges = atStart && atEnd;
                boolean pullStart = (!bothEdges && atStart) || (bothEdges && delta > 0);
                if (pullStart ? delta > mTouchSlop : -delta > mTouchSlop) {
                    mIsBeingDragged = true;
                    // 基准设为当前触点：拖拽位移从 0 开始，消除 touchSlop 段的跳变
                    mInitialMotionPos = pos;
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "icpt INTERCEPT delta=" + delta + " atS=" + atStart + " atE=" + atEnd);
                    }
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                mActivePointerId = -1;
                mEdgeEligible = false;
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
                if (!mTargetHasDown) {
                    // 接管按下（回弹动画进行中/容器仍越界）：target 缺本手势 DOWN，
                    // 合成补发使后续转发的 MOVE/UP 构成完整序列（可滚、可 fling、不误点）
                    forwardToTarget(ev, true);
                }
                checkScrollStart();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "touchevt DOWN dragged=" + mIsBeingDragged);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int idx = ev.findPointerIndex(mActivePointerId);
                if (idx < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                boolean freshDown = false;
                if (!mTargetHasDown) {
                    // 边缘拦截型接管：框架刚给 target 发过 CANCEL，用当前触点重放 DOWN 后再转发
                    forwardToTarget(ev, true);
                    freshDown = true;
                }
                if (mIsBeingDragged) {
                    handleDrag(axisOf(ev, idx));
                }
                if (freshDown) {
                    // 本拍刚放的 DOWN 即钉点，同拍的 side 变化无需再重放
                    mTargetReplayDown = false;
                }
                if (mTargetHasDown) {
                    forwardToTarget(ev, false);
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
                float delta = axisOf(ev, activeIdx) - mInitialDownPos;
                float newInitial = axisOf(ev, actionIndex) - delta;
                mInitialDownPos = newInitial;
                mInitialMotionPos = newInitial;
                mActivePointerId = ev.getPointerId(actionIndex);
                if (mTargetHasDown) {
                    forwardToTarget(ev, false);
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(ev);
                if (mTargetHasDown) {
                    forwardToTarget(ev, false);
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (ev.findPointerIndex(mActivePointerId) < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "touchevt UP dragged=" + mIsBeingDragged);
                }
                // 先把序列交还 target：超 slop 发 UP（触发它按真实速度 fling = 快甩惯性），
                // 未超 slop 发 CANCEL 防合成重放误触发点击
                finishForwardToTarget(ev, false);
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBackOnRelease();
                }
                mOverdragSide = 0;
                mActivePointerId = -1;
                recycleVelocityTracker();
                return false;
            }
            case MotionEvent.ACTION_CANCEL: {
                finishForwardToTarget(ev, true);
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    springBack(mOrientation);
                }
                mOverdragSide = 0;
                mActivePointerId = -1;
                recycleVelocityTracker();
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
     * 把手势事件并行转发给 target（asDown=true 时合成补发 DOWN），坐标换到 target 左上角：
     * <ul>
     *   <li>side!=0（容器越界中）：坐标钉死在基准点——target 视角手指静止，不产生滚动，
     *       避免与容器位移重复计账（视觉超速）；</li>
     *   <li>side 变化瞬间：重放 CANCEL+DOWN（坐标=当拍真实触点）重建滚动基准，
     *       把坐标断层藏进 DOWN，target 从新基准 1:1 跟手无瞬移；</li>
     *   <li>side==0（容器已归 0）：坐标原样转发，恢复方向由 target 原生滚动与 fling。</li>
     * </ul>
     */
    private void forwardToTarget(MotionEvent ev, boolean asDown) {
        View target = resolveTarget();
        if (target == null) {
            return;
        }
        MotionEvent copy;
        if (asDown) {
            copy = MotionEvent.obtain(ev);
            mTargetDownX = ev.getX();
            mTargetDownY = ev.getY();
            mTargetReplayDown = false;
            mTargetScrolled = false; // 新基准：target 视角从静止重新起算
        } else if (mTargetReplayDown) {
            // 越界端切换：先 CANCEL 结束上一段，再以当拍触点重放 DOWN 换基准
            MotionEvent cancel = MotionEvent.obtain(ev);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            cancel.offsetLocation(-target.getLeft(), -target.getTop());
            target.dispatchTouchEvent(cancel);
            cancel.recycle();
            copy = MotionEvent.obtain(ev);
            copy.setAction(MotionEvent.ACTION_DOWN);
            mTargetDownX = ev.getX();
            mTargetDownY = ev.getY();
            mTargetReplayDown = false;
            mTargetScrolled = false;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "handoff replay DOWN side=" + mOverdragSide);
            }
        } else {
            copy = MotionEvent.obtain(ev);
            if (mOverdragSide != 0) {
                copy.setLocation(mTargetDownX, mTargetDownY);
            } else if (!mTargetScrolled && Math.hypot(ev.getX() - mTargetDownX,
                    ev.getY() - mTargetDownY) > mTouchSlop) {
                mTargetScrolled = true; // 真实坐标滑出 slop：target 已进滚动态，UP 走 fling 不会点击
            }
        }
        copy.offsetLocation(-target.getLeft(), -target.getTop());
        target.dispatchTouchEvent(copy);
        copy.recycle();
        mTargetHasDown = true;
        if (BuildConfig.DEBUG && asDown) {
            Log.d(TAG, "handoff DOWN(synth) at=" + ev.getX() + "," + ev.getY());
        }
    }

    /**
     * 手势收口：target 真实滑动过（mTargetScrolled，已进滚动状态）发 UP 让它按真实速度
     * fling；否则发 CANCEL——钉住段 target 视角手指静止（mTouchMode 停在 TAP），发 UP 会被
     * AbsListView 以 pointToPosition(UP) 判成点击，把越界回弹拖拽误升级成条目选中。
     */
    private void finishForwardToTarget(MotionEvent ev, boolean forceCancel) {
        if (!mTargetHasDown) {
            return;
        }
        View target = resolveTarget();
        mTargetHasDown = false;
        if (target == null) {
            return;
        }
        boolean cancel = forceCancel || !mTargetScrolled;
        MotionEvent copy = MotionEvent.obtain(ev);
        copy.setAction(cancel ? MotionEvent.ACTION_CANCEL : MotionEvent.ACTION_UP);
        copy.offsetLocation(-target.getLeft(), -target.getTop());
        target.dispatchTouchEvent(copy);
        copy.recycle();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "handoff " + (cancel ? "CANCEL" : "UP(target fling)")
                    + " scrolled=" + mTargetScrolled);
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 动画中按下（或换手指）时的基准重映射：
     * 用反阻尼把当前容器偏移换算回等效手指位移，使下一拍 drag 的 delta
     * 恰好重现当前偏移——按住无跳变（对齐原版 checkVerticalScrollStart）。
     */
    private void checkScrollStart() {
        int scroll = axisScroll();
        if (scroll != 0) {
            mIsBeingDragged = true;
            mOverdragSide = scroll > 0 ? 1 : -1;
            mEdgeEligible = true;
            float touch = obtainTouchDistance(
                    Math.abs(scroll),
                    Math.abs(obtainMaxSpringBackDistance(mOrientation)),
                    mOrientation);
            if (scroll < 0) {
                mInitialDownPos -= touch;
            } else {
                mInitialDownPos += touch;
            }
            mInitialMotionPos = mInitialDownPos;
        } else {
            mIsBeingDragged = false;
            mOverdragSide = 0;
        }
    }
    
    /**
     * 阻尼拖拽（越界端状态机）：side!=0 时按本端接受区间映射容器位移；手指反穿
     * 本端边界把容器归 0、side 清 0——此后滚动由转发给 target 的事件原生驱动
     * （恢复方向直接跟手，如 ScrollView 竖向）；target 滚到对侧边界且方向可拉出时
     * 以当前触点重建基准再入越界（无跳变）。
     */
    private void handleDrag(float pos) {
        float delta = pos - mInitialMotionPos;
        float damped = obtainSpringBackDistance(delta, mOrientation);
        float desired = -Math.signum(delta) * damped;
        if (BuildConfig.DEBUG && SystemClock.uptimeMillis() - mLastTouchLogAt > 300) {
            mLastTouchLogAt = SystemClock.uptimeMillis();
            Log.d(TAG, "drag side=" + mOverdragSide + " desired=" + desired
                    + " scroll=" + axisScroll());
        }
        requestDisallowParentInterceptTouchEvent(true);
        if (mOverdragSide == 0) {
            if (desired < 0 && isTargetAtStart()) {
                mOverdragSide = -1;
            } else if (desired > 0 && isTargetAtEnd()) {
                mOverdragSide = 1;
            } else {
                return; // target 滚动中（或其不可滚方向）：容器保持 0，不干预
            }
            mInitialMotionPos = pos; // 当拍位移并入基准，越界从 0 起拉出无跳变
            if (mTargetHasDown) {
                mTargetReplayDown = true; // target 从滚动切入钉住：换基准
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "drag sideIn=" + mOverdragSide);
            }
            return;
        }
        if ((mOverdragSide < 0 && desired >= 0) || (mOverdragSide > 0 && desired <= 0)) {
            // 反穿本端边界：归 0 并交还 target（本拍起 target 经转发事件继续滚）
            if (axisScroll() != 0) {
                scrollAxisTo(0);
            }
            mOverdragSide = 0;
            if (mTargetHasDown) {
                mTargetReplayDown = true; // target 从钉住恢复跟手：换基准
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "drag sideOut");
            }
            return;
        }
        scrollAxisTo((int) desired);
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
        if (!vertical || mOrientation != DIRECTION_VERTICAL) {
            // 嵌套路径仅服务竖直容器（原版 scrollOrientation 恒 VERTICAL）；
            // 水平容器的 target 为非嵌套 View，只走 touch 路径
            return false;
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
        // 向祖父（如 BottomSheet 的 CoordinatorLayout）登记本次嵌套滚动
        mChildHelper.startNestedScroll(axes, type);
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
                mTargetStartedAtStart = isTargetAtStart();
                mTargetStartedAtEnd = isTargetAtEnd();
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
        int preX = consumed[0];
        int preY = consumed[1];
        onNestedScrollInternal(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                type, consumed);
        // 本容器本轮消费 = consumed 增量；剩余未消费量上抛祖父（Miuix BottomSheet
        // 弹层跟手/Behavior 协作入口），祖父消费回填 consumed 交回孩子
        int mineX = consumed[0] - preX;
        int mineY = consumed[1] - preY;
        int remainX = dxUnconsumed - mineX;
        int remainY = dyUnconsumed - mineY;
        if (remainX != 0 || remainY != 0) {
            mNestedUpConsumed[0] = 0;
            mNestedUpConsumed[1] = 0;
            mChildHelper.dispatchNestedScroll(dxConsumed + mineX, dyConsumed + mineY,
                    remainX, remainY, null, type, mNestedUpConsumed);
            consumed[0] += mNestedUpConsumed[0];
            consumed[1] += mNestedUpConsumed[1];
        }
    }

    /** 内部嵌套消费主体（原 onNestedScroll 实现）；向上转发由外层包装负责。 */
    private void onNestedScrollInternal(@NonNull View target, int dxConsumed, int dyConsumed,
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
        if (vertical && unconsumed < 0 && isTargetAtStart()) {
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
        } else if (vertical && unconsumed > 0 && isTargetAtEnd()) {
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
        mNestedConsumedCompat[0] = 0;
        mNestedConsumedCompat[1] = 0;
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                type, mNestedConsumedCompat);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        mNestedConsumedCompat[0] = 0;
        mNestedConsumedCompat[1] = 0;
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                ViewCompat.TYPE_TOUCH, mNestedConsumedCompat);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (mSpringBackEnable && mNestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            nestedPreScrollVertical(dy, consumed, type);
        }
        // 本容器消费后剩余的量经 dispatchNestedPreScroll 上抛祖父（Miuix BottomSheet
        // 弹层跟手入口）；mSpringBackEnable=false 时全量转发；普通页面父容器非嵌套型
        // → dispatch 返回 false，行为与接入前一致
        int remainX = dx - consumed[0];
        int remainY = dy - consumed[1];
        if (remainX != 0 || remainY != 0) {
            mNestedUpConsumed[0] = 0;
            mNestedUpConsumed[1] = 0;
            if (mChildHelper.dispatchNestedPreScroll(remainX, remainY, mNestedUpConsumed, null, type)) {
                consumed[0] += mNestedUpConsumed[0];
                consumed[1] += mNestedUpConsumed[1];
            }
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
        // 同步注销祖父的嵌套滚动登记
        mChildHelper.stopNestedScroll(type);
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
    // 嵌套滚动子协议（NestedScrollingChild3，全量委托 helper）：把本容器作为
    // Parent 消费后仍剩余的嵌套量继续上抛祖父容器（如 Miuix BottomSheet 的
    // CoordinatorLayout/Behavior）。四个转发点（start/pre/scroll/stop）在上方
    // Parent3 方法内完成，这里补齐接口方法；普通页面父容器非嵌套型时 dispatch
    // 全 false，行为与接入前一致
    // ---------------------------------------------------------------------

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
                                           @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
                                           @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                     int dyUnconsumed, @Nullable int[] offsetInWindow, int type,
                                     @NonNull int[] consumed) {
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    // ---------------------------------------------------------------------
    // 回弹
    // ---------------------------------------------------------------------

    /**
     * touch 路径松手收口：越界拖拽带甩动初速回弹（“快甩惯性”——像手指把越界量
     * 甩出去再弹回；零速 springBack 只是慢回位）。容器已归 0 时 target 的 fling
     * 已由转发的 UP 提供，这里零速早退即可。
     */
    private void springBackOnRelease() {
        float v = 0f;
        if (mOverdragSide != 0 && axisScroll() != 0 && mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(1000);
            float finger = mOrientation == DIRECTION_VERTICAL
                    ? mVelocityTracker.getYVelocity(mActivePointerId)
                    : mVelocityTracker.getXVelocity(mActivePointerId);
            // 容器位移与手指位移反号（与 desired = -signum(delta)·damped 同构）
            v = -finger;
            if (Math.abs(v) < VELOCITY_THRESHOLD) {
                v = 0f;
            } else if (Math.abs(v) > MAX_RELEASE_FLING) {
                v = Math.signum(v) * MAX_RELEASE_FLING;
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "release side=" + mOverdragSide + " v=" + v
                    + " scroll=" + axisScroll());
        }
        springBack(v, mOrientation, true);
    }

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
        int pos = axisScroll();
        boolean haptic;
        if (pos < 0) {
            haptic = !mTargetStartedAtStart;
        } else if (pos > 0) {
            haptic = !mTargetStartedAtEnd;
        } else {
            haptic = !mTargetStartedAtStart && !mTargetStartedAtEnd;
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
