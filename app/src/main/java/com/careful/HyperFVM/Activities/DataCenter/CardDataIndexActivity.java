package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.DisplayBackgroundCardImageHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class CardDataIndexActivity extends BaseActivity {
    private static final int TOP_BAR_FADE_RANGE_DP = 150; // 顶栏模糊层渐显区间（滚动该距离后完全显现）
    private static final int HEADER_TARGET_TOP_OFFSET_PX = 400; // 目录跳转后分节标题停在距列表顶的像素距离（与原页面视觉一致）
    private static final float MENU_MAX_WIDTH_SCREEN_RATIO = 0.7f; // 目录菜单宽度上限（占屏幕宽比例），过长标题的条目以省略号结尾
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private DBHelper dbHelper;
    private BlurUtil blurUtil;
    private long jumpCounter; // 目录跳转令牌计数：新一次跳转使旧跳转的收尾回调让位

    private RecyclerView recyclerView;
    private CardDataIndexAdapter adapter;
    private View backgroundImage1;
    private View backgroundImage2;

    private int savedScrollY = 0;            // 用于保存/恢复的滚动位置
    private int backgroundImageMaxScroll1;   // 判定完全消失的滚动距离（dp 转 px）
    private int backgroundImageMaxScroll2;   // 判定完全消失的滚动距离（dp 转 px）

    /** 背景随机图清单（每行 {drawable 名, 卡名}）：重建时沿用原图，仅全新进入才重新随机 */
    private String[][] backgroundCardImageFileInfo;

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
            // 重建（深色模式切换、小窗等）时沿用重建前的背景随机图；完全销毁后重新进入才重新随机
            backgroundCardImageFileInfo =
                    (String[][]) savedInstanceState.getSerializable("backgroundCardImageFileInfo");
        }

        // 装配虚拟化目录列表（分节标题 + 367 张卡片，按需创建与解码）
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
     * 装配 RecyclerView：分节标题 + 卡片行 + 页脚，并恢复上次的滚动位置。
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        // 关闭 RecyclerView 的系统自动滚动恢复（LayoutManager 锚点会在重建时自动恢复，与下方
        // post scrollBy 的全量恢复叠加会超调；本页滚动位置完全自管 savedScrollY，故禁用之）
        recyclerView.setSaveEnabled(false);

        adapter = new CardDataIndexAdapter(this, CardDataCatalogData.buildSectionTitles(this));
        recyclerView.setAdapter(adapter);
        // 注意：滚动位置恢复不在这里执行——必须在滚动监听器注册完成后才能恢复
        // （详见 initDecoration 末尾），保证恢复动作能经由 onScrolled 的 dy 累计出真实偏移。
    }

    /**
     * 以目录按钮为锚点弹出分节跳转下拉菜单（样式与食神谱图鉴目录菜单一致，仅无选中对勾）。
     * 47 个分节标题远超一屏，菜单高度按锚点下方剩余空间收缩，超出部分滚动查看。
     */
    private void showIndexDropdown(View anchor) {
        String[] entries = CardDataCatalogData.buildSectionTitles(this).toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown_selection, entries) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView != null ? convertView
                        : getLayoutInflater().inflate(R.layout.item_dropdown_selection, parent, false);
                ((TextView) row.findViewById(R.id.option_text)).setText(getItem(position));
                // 目录菜单不需要选中对勾
                row.findViewById(R.id.option_check).setVisibility(View.GONE);
                return row;
            }
        };

        // 实测选项宽高：菜单宽度取最宽选项（wrap_content 效果）。ListView 在 PopupWindow 中无法
        // 真正 wrap_content，需自行测量内容宽后按像素设置；getView 的 parent 形参标注 @NonNull，
        // 这里传一个仅用于生成 LayoutParams 的空容器（不挂载子视图），避免传 null
        ViewGroup measureParent = new FrameLayout(this);
        int contentWidth = 0;
        int itemHeight = 0;
        for (int i = 0; i < entries.length; i++) {
            View itemView = adapter.getView(i, null, measureParent);
            itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            itemHeight = itemView.getMeasuredHeight();
        }
        // 菜单宽度上限：个别分节标题很长（最长的近 24 个全角字符），若完全按最宽条目展示会接近满屏，
        // 观感差；这里限制为屏幕宽的一定比例，过长条目以省略号结尾（item_dropdown_selection 已配置 ellipsize）
        int screenWidth = anchor.getRootView().getWidth();
        contentWidth = Math.min(contentWidth, (int) (screenWidth * MENU_MAX_WIDTH_SCREEN_RATIO));

        // 锚点下方剩余空间不足时收缩菜单高度（47 个分节远超一屏，超出部分滚动）
        int verticalOffset = DensityUtil.dpToPx(this, 4);
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        int spaceBelow = anchor.getRootView().getHeight() - location[1] - anchor.getHeight() - verticalOffset;
        int popupHeight = Math.min(itemHeight * entries.length, Math.max(spaceBelow, itemHeight * 2));

        // 菜单右缘与按钮右缘对齐（水平偏移取负值左移）；极端窄屏/锚点贴边时钳制在屏幕内
        int horizontalOffset = anchor.getWidth() - contentWidth;
        horizontalOffset = Math.max(-location[0], Math.min(horizontalOffset, screenWidth - contentWidth - location[0]));

        ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAnchorView(anchor);
        popup.setWidth(contentWidth);
        popup.setHeight(popupHeight);
        popup.setVerticalOffset(verticalOffset);
        popup.setHorizontalOffset(horizontalOffset);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_dropdown_background));
        popup.setAdapter(adapter);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            runFastScroll(position);
        });
        popup.show();
        // 弹出后隐藏滚动条（47 项超一屏可滚动，滚动条影响观感）
        ListView listView = popup.getListView();
        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
        }
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
                || sectionIndex >= CardDataCatalogData.SECTION_PREFIXES.length) {
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

        // 匀速平滑滚动到目标分节标题：滚动距离远时耗时随之变长，但每一帧都真实驱动
        // 列表滚动，透明度联动与位置记录不会像布局式跳转那样失去对账
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                // 默认收尾只把标题顶对齐列表内容起点（顶部 padding），要停到距顶 400px 还需再滚一段；
                // 那段若留到 SCROLL_STATE_IDLE 里 scrollBy 会形成可见的瞬移突变，故在此把完整收尾
                // 距离并入同一次减速动画，从触发点平滑滚到最终位置（内容不足时会被 clamp，与旧逻辑一致）
                final int delta = targetView.getTop() - HEADER_TARGET_TOP_OFFSET_PX; // 还需滚动的像素量（可正可负）
                final int time = calculateTimeForDeceleration(Math.abs(delta));
                if (time > 0) {
                    action.update(0, delta, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(@NonNull DisplayMetrics displayMetrics) {
                return 0.06f; // ms/px：值越小滚动越快，可按手感调整
            }
        };
        smoothScroller.setTargetPosition(headerPosition);
        Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
    }

    /**
     * 计算背景渐隐效果使用的滚动偏移。
     * <p>
     * 不用累计值也不用估算 API：列表首行（1_1 分节标题）的内容坐标恒为 0，
     * 只要它还存在于 RecyclerView 的布局内，真实偏移 = paddingTop - 首行.getTop()，
     * 该几何测量精确且自纠，无需依赖增量累计；
     * 首行不可见说明列表已滚出渐隐窗口很远，此时按增量累计维护（页面所有滚动——手动滑动 /
     * 目录平滑跳转——都经 onScrolled 回调，累计与真实位置严格同步）。
     */
    private int computeScrollOffsetForBackgroundEffect(@NonNull RecyclerView rv, int dy) {
        View firstRow = Objects.requireNonNull(rv.getLayoutManager()).findViewByPosition(0);
        if (firstRow != null) {
            // 几何测量：首行随列表平移，其屏幕 top = paddingTop - 真实偏移
            savedScrollY = Math.max(0, rv.getPaddingTop() - firstRow.getTop());
            return savedScrollY;
        }
        // 首行已被回收（滚出很远）：仅累计维护 savedScrollY 供保存/恢复滚动位置使用；
        // 顶部锚定兜底（真正到达顶部时首行必然可见，会走上面的几何分支）。
        savedScrollY = rv.canScrollVertically(-1) ? Math.max(0, savedScrollY + dy) : 0;
        // 效果值：已远离渐隐窗口，视为完全消失
        return Integer.MAX_VALUE / 4;
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("DiscouragedApi")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        ImageButton floatButtonIndex = findViewById(R.id.FloatButton_Index);
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

            params = (ViewGroup.MarginLayoutParams) floatButtonIndex.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonIndex.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonSearch.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonSearch.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonIndex.setOnClickListener(this::showIndexDropdown);
        floatButtonSearch.setOnClickListener(v -> DialogBuilderManager.showCardQueryDialog(this));

        if (SmallestWidthUtil.getSmallestWidthDp() < 600) {
            // ==================== 手机版：两组背景容器，每组三张图 ====================
            ImageView[] cardDataIndexBackgroundImages = {
                    findViewById(R.id.card_data_index_background_image_1),
                    findViewById(R.id.card_data_index_background_image_2),
                    findViewById(R.id.card_data_index_background_image_3),
                    findViewById(R.id.card_data_index_background_image_4),
                    findViewById(R.id.card_data_index_background_image_5),
                    findViewById(R.id.card_data_index_background_image_6),
            };

            // 全新进入才重新随机（重建时 onCreate 已从 savedInstanceState 恢复出原清单）
            if (backgroundCardImageFileInfo == null || backgroundCardImageFileInfo.length != 6) {
                backgroundCardImageFileInfo = DisplayBackgroundCardImageHelper.giveRandomCardImageFileInfoArray(6);
            }
            // final 别名：下方匿名监听器/lambda 只能捕获 effectively final 的局部变量
            final String[][] cardImageFileInfoArray = backgroundCardImageFileInfo;

            // 展示随机图片
            for (int i = 0; i < 6; i++) {
                int resId = getResources().getIdentifier(cardImageFileInfoArray[i][0], "drawable", getPackageName());
                if (resId != 0) {
                    Glide.with(this)
                            .load(resId)
                            .override(200, 175)
                            .centerCrop()
                            .into(cardDataIndexBackgroundImages[i]);
                }
            }

            // 获取需要渐隐的元素
            backgroundImage1 = findViewById(R.id.card_data_index_background_images_1);
            backgroundImage2 = findViewById(R.id.card_data_index_background_images_2);

            // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
            backgroundImageMaxScroll1 = DensityUtil.dpToPx(this, 150);
            backgroundImageMaxScroll2 = DensityUtil.dpToPx(this, 100);

            // 等列表完成布局后：同步一次初始效果（透明度与恢复的滚动位置同步）
            recyclerView.post(() -> {
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage1, savedScrollY, backgroundImageMaxScroll1);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage2, savedScrollY, backgroundImageMaxScroll2);

                // 给图片设置点击事件
                // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                for (int i = 0; i <= 2; i++) {
                    ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                            this, backgroundImage1, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                }
                for (int i = 3; i <= 5; i++) {
                    ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                            this, backgroundImage2, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                }
            });

            // 滚动监听：以列表滚动偏移（等价于原 ScrollView 的 scrollY）驱动背景渐隐渐显
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    // 以列表首行（内容坐标恒为 0）为锚点的几何测量，见 computeScrollOffsetForBackgroundEffect
                    int scrollY = computeScrollOffsetForBackgroundEffect(rv, dy);
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage1, scrollY, backgroundImageMaxScroll1);
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage2, scrollY, backgroundImageMaxScroll2);

                    // 给图片设置点击事件
                    // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                    for (int i = 0; i <= 2; i++) {
                        ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                                CardDataIndexActivity.this, backgroundImage1, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                    }
                    for (int i = 3; i <= 5; i++) {
                        ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                                CardDataIndexActivity.this, backgroundImage2, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                    }
                }
            });
        } else {
            // ==================== 平板版：单行七张背景图 ====================
            ImageView[] cardDataIndexBackgroundImages = {
                    findViewById(R.id.card_data_index_background_image_1),
                    findViewById(R.id.card_data_index_background_image_2),
                    findViewById(R.id.card_data_index_background_image_3),
                    findViewById(R.id.card_data_index_background_image_4),
                    findViewById(R.id.card_data_index_background_image_5),
                    findViewById(R.id.card_data_index_background_image_6),
                    findViewById(R.id.card_data_index_background_image_7),
            };

            // 全新进入才重新随机（重建时 onCreate 已从 savedInstanceState 恢复出原清单）
            if (backgroundCardImageFileInfo == null || backgroundCardImageFileInfo.length != 7) {
                backgroundCardImageFileInfo = DisplayBackgroundCardImageHelper.giveRandomCardImageFileInfoArray(7);
            }
            // final 别名：下方匿名监听器/lambda 只能捕获 effectively final 的局部变量
            final String[][] cardImageFileInfoArray = backgroundCardImageFileInfo;

            for (int i = 0; i < 7; i++) {
                int resId = getResources().getIdentifier(cardImageFileInfoArray[i][0], "drawable", getPackageName());
                if (resId != 0) {
                    Glide.with(this)
                            .load(resId)
                            .override(200, 175)
                            .centerCrop()
                            .into(cardDataIndexBackgroundImages[i]);
                }
            }

            // 获取需要渐隐的元素
            backgroundImage1 = findViewById(R.id.card_data_index_background_images_1);

            // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
            backgroundImageMaxScroll1 = DensityUtil.dpToPx(this, 100);

            // 等列表完成布局后：同步一次初始效果
            recyclerView.post(() -> {
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage1, savedScrollY, backgroundImageMaxScroll1);

                // 给图片设置点击事件
                // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                for (int i = 0; i < 7; i++) {
                    ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                            this, backgroundImage1, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                }
            });

            // 滚动监听：以列表滚动偏移（等价于原 ScrollView 的 scrollY）驱动背景渐隐渐显
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    // 以列表首行（内容坐标恒为 0）为锚点的几何测量，见 computeScrollOffsetForBackgroundEffect
                    int scrollY = computeScrollOffsetForBackgroundEffect(rv, dy);
                    ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(backgroundImage1, scrollY, backgroundImageMaxScroll1);

                    // 给图片设置点击事件
                    // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                    for (int i = 0; i < 7; i++) {
                        ScrollEffectForBackgroundItem.updateCardDataIndexBackgroundImageClickable(
                                CardDataIndexActivity.this, backgroundImage1, cardDataIndexBackgroundImages[i], cardImageFileInfoArray[i][1]);
                    }
                }
            });
        }

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
        // 从而保证背景渐隐效果与列表当前位置严格同步。
        // 不能用 post 延迟：此处视图尚未 attach，post 会积压到 attach 时刻执行，彼时 RecyclerView
        // 还未完成首次布局（无子视图），scrollBy 会被忽略；GlobalLayout 回调发生在布局完成后、
        // 同帧绘制前，一次性滚动后首帧即呈现最终状态（透明度同步经 scrollBy 触发的 onScrolled 完成）
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
        if (backgroundCardImageFileInfo != null) {
            outState.putSerializable("backgroundCardImageFileInfo", backgroundCardImageFileInfo);
        }
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

        Glide.get(this).clearMemory();

        super.onDestroy();
    }
}
