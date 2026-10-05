package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForDesign.Widget.LetterIndexBarView;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class CardDataIndexActivity extends BaseActivity {
    private static final int TOP_BAR_FADE_RANGE_DP = 25; // 顶栏模糊层渐显区间（滚动该距离后完全显现）
    private static final int HEADER_TARGET_TOP_OFFSET_PX = 400; // 目录跳转后分节标题停在距列表顶的像素距离（与原页面视觉一致）
    private static final float JUMP_SCROLL_MS_PER_PX = 0.01f; // 目录跳转的滚动速度（每像素毫秒数，越小越快）
    private static final int JUMP_TAIL_SCROLL_MIN_MS = 120; // 收尾减速动画最短时长（速度调快后避免收尾退化成瞬间突变）
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private DBHelper dbHelper;
    private BlurUtil blurUtil;
    private long jumpCounter; // 目录跳转令牌计数：新一次跳转使旧跳转的收尾回调让位

    private RecyclerView recyclerView;
    private CardDataIndexAdapter adapter;

    private int savedScrollY = 0;            // 用于保存/恢复的滚动位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        //小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        setContentView(R.layout.activity_card_data_index);

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 装配虚拟化目录列表（字母分节标题 + 366 张单卡，按需创建与解码）
        setupRecyclerView();

        // 初始化各种装饰效果
        initDecoration();

        // 进入提示弹窗（与旧逻辑一致：延迟片刻后弹出）
        mainHandler.postDelayed(() -> {
            if (dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX)) {
                Toast.makeText(this, "点击卡片可查看其数据\n此弹窗可在设置内关闭", Toast.LENGTH_SHORT).show();
            }
        }, 50);
    }

    /**
     * 装配 RecyclerView：字母分节标题行 + 单卡行 + 页脚，并恢复上次的滚动位置。
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        // 关闭 RecyclerView 的系统自动滚动恢复（LayoutManager 锚点会在重建时自动恢复，与下方
        // post scrollBy 的全量恢复叠加会超调；本页滚动位置完全自管 savedScrollY，故禁用之）
        recyclerView.setSaveEnabled(false);

        List<CardDataLetterCatalogData.Section> sections = CardDataLetterCatalogData.getSections(this);
        adapter = new CardDataIndexAdapter(this, sections);
        recyclerView.setAdapter(adapter);

        // 右侧 A-Z 快速索引条：标签 = 实际存在的分节（顺序与 Adapter 分节下标一一对应），
        // 命中回调直连目录快速跳转（runFastScroll 内自带越界守卫与令牌抢占）
        LetterIndexBarView indexBar = findViewById(R.id.LetterIndexBar);
        List<String> labels = new ArrayList<>(sections.size());
        for (CardDataLetterCatalogData.Section section : sections) {
            labels.add(section.label);
        }
        indexBar.setLabels(labels);
        indexBar.setOnSectionPickListener(this::runFastScroll);
        // 注意：滚动位置恢复不在这里执行——必须在滚动监听器注册完成后才能恢复
        // （详见 initDecoration 末尾），保证恢复动作能经由 onScrolled 的 dy 累计出真实偏移。
    }

    /**
     * 快速滚动：让对应分节标题停在与屏幕顶部相距约 400px 的位置（与原页面视觉一致）。
     * 全程走 LinearSmoothScroller 逐帧 scrollBy（每帧增量都经 onScrolled 回调），不使用
     * scrollToPosition 布局式跳转：后者不回调滚动监听，会让滚动位置记录（savedScrollY 与
     * 顶部栏联动偏移）与列表真实位置脱钩，导致往回滚动时渐变提前/延后、直至滑回顶部才自愈。
     * 最终位置由 onTargetFound 收尾减速动画一次滚到，避免默认"先贴齐列表顶、结束后再瞬移补差"
     * 造成的快到位时的位置突变。
     */
    private void runFastScroll(int sectionIndex) {
        if (recyclerView == null || adapter == null || sectionIndex < 0
                || sectionIndex >= adapter.getSectionCount()) {
            return;
        }
        final int headerPosition = adapter.getHeaderPosition(sectionIndex);
        final long jumpToken = ++jumpCounter;

        // 兜底校正：正常路径下 onTargetFound 收尾动画已带偏移一次到位（此回调里 delta == 0 不动作）；
        // 仅当收尾被内容边界 clamp 或被打断、未停在目标位置时，才在此做最后一次瞬移校正
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                rv.removeOnScrollListener(this);
                if (jumpToken != jumpCounter) {
                    return; // 已有更新的跳转接管，本次回调让位
                }
                RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
                View targetView = layoutManager == null ? null
                        : layoutManager.findViewByPosition(headerPosition);
                if (targetView == null) {
                    return; // 目标已滚出可见区（滚动被打断后滑走）：保持现状即可
                }
                int delta = targetView.getTop() - HEADER_TARGET_TOP_OFFSET_PX; // 还需滚动的像素量（可正可负）
                if (delta != 0) {
                    rv.scrollBy(0, delta);
                }
            }
        });

        // 匀速平滑滚动到目标分节标题（速度见 JUMP_SCROLL_MS_PER_PX，已尽量加快）：每一帧都真实驱动
        // 列表滚动，透明度联动与位置记录不会像布局式跳转那样失去对账
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                // 默认收尾只把标题顶对齐列表内容起点（顶部 padding），要停到距顶 400px 还需再滚一段；
                // 那段若留到 SCROLL_STATE_IDLE 里 scrollBy 会形成可见的瞬移突变，故在此把完整收尾
                // 距离并入同一次减速动画，从触发点平滑滚到最终位置（内容不足时会被 clamp，与旧逻辑一致）
                final int delta = targetView.getTop() - HEADER_TARGET_TOP_OFFSET_PX; // 还需滚动的像素量（可正可负）
                if (delta != 0) {
                    // 收尾时间设下限：滚动速度调快后纯计算值会短到形同瞬间突变，保底时长维持“滑入”观感
                    final int time = Math.max(calculateTimeForDeceleration(Math.abs(delta)), JUMP_TAIL_SCROLL_MIN_MS);
                    action.update(0, delta, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(@NonNull DisplayMetrics displayMetrics) {
                return JUMP_SCROLL_MS_PER_PX;
            }
        };
        smoothScroller.setTargetPosition(headerPosition);
        Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
    }

    /**
     * 维护并返回列表真实滚动偏移（savedScrollY），供界面重建后恢复滚动位置使用。
     * <p>
     * 不用累计值也不用估算 API：列表首行（首个字母分节标题）的内容坐标恒为 0，
     * 只要它还存在于 RecyclerView 的布局内，真实偏移 = paddingTop - 首行.getTop()，
     * 该几何测量精确且自纠，无需依赖增量累计；
     * 首行不可见说明列表已滚出很远，此时按增量累计维护（页面所有滚动——手动滑动 /
     * 目录平滑跳转——都经 onScrolled 回调，累计与真实位置严格同步）。
     */
    private void computeScrollOffsetForBackgroundEffect(@NonNull RecyclerView rv, int dy) {
        View firstRow = Objects.requireNonNull(rv.getLayoutManager()).findViewByPosition(0);
        if (firstRow != null) {
            // 几何测量：首行随列表平移，其屏幕 top = paddingTop - 真实偏移
            savedScrollY = Math.max(0, rv.getPaddingTop() - firstRow.getTop());
            return;
        }
        // 首行已被回收（滚出很远）：仅累计维护 savedScrollY 供保存/恢复滚动位置使用；
        // 顶部锚定兜底（真正到达顶部时首行必然可见，会走上面的几何分支）。
        savedScrollY = rv.canScrollVertically(-1) ? Math.max(0, savedScrollY + dy) : 0;
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        ImageButton floatButtonSearch = findViewById(R.id.FloatButton_Search);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonSearch.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonSearch.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonSearch.setOnClickListener(v -> DialogBuilderManager.showCardQueryDialog(this));

        // 滚动监听：以列表首行几何测量维护 savedScrollY，供界面重建后恢复滚动位置使用
        // （原背景随机图片与滑动渐隐效果已迁移至 DataCenterFragment，渐隐效果随之移除）
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                // 以列表首行（内容坐标恒为 0）为锚点的几何测量，见 computeScrollOffsetForBackgroundEffect
                computeScrollOffsetForBackgroundEffect(rv, dy);
            }
        });

        // 添加模糊材质
        setupBlurEffect();

        // 接入顶部栏滚动联动：本页只联动模糊背景层（topBarBottom/topBar 传0跳过）。
        // 滚动位置保存与恢复沿用页面自有机制——下方 post + scrollBy 恢复会经由 onScrolled
        // 驱动本工具类同步透明度，故不再调用工具类的 saveScrollY/restoreScrollY，避免双重恢复
        // 顶部栏滚动联动（仅模糊层参与；滚动位置由页面自有机制保存/恢复）
        NestedScrollUtil.attach(rootView,
                R.id.RecyclerView, 0, 0, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP);

        // 恢复上次的滚动位置（必须在滚动监听器全部注册完成后执行）：
        // 先清零 savedScrollY，让 scrollBy 触发的 onScrolled 用 dy 重新累计出真实偏移，
        // 从而保证 savedScrollY 与列表当前位置严格同步。
        // 不能用 post 延迟：此处视图尚未 attach，post 会积压到 attach 时刻执行，彼时 RecyclerView
        // 还未完成首次布局（无子视图），scrollBy 会被忽略；GlobalLayout 回调发生在布局完成后、
        // 同帧绘制前，一次性滚动后首帧即呈现最终状态（savedScrollY 同步经 scrollBy 触发的 onScrolled 完成）
        if (savedScrollY > 0) {
            final int targetScrollY = savedScrollY;
            savedScrollY = 0;
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    recyclerView.scrollBy(0, targetScrollY);
                }
            });
        }
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("最小宽度", String.valueOf(SmallestWidthUtil.getSmallestWidthDp()));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

    @Override
    protected void onDestroy() {
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}
