package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.DisplayBackgroundCardImageHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class CardDataIndexActivity extends BaseActivity {
    private static final int TOP_BAR_FADE_RANGE_DP = 150; // 顶栏模糊层渐显区间（滚动该距离后完全显现）
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private DBHelper dbHelper;
    private BlurUtil blurUtil;
    private ValueAnimator scrollAnimator;

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

        adapter = new CardDataIndexAdapter(this, CardDataCatalogData.buildSectionTitles(this));
        recyclerView.setAdapter(adapter);
        // 注意：滚动位置恢复不在这里执行——必须在滚动监听器注册完成后才能恢复
        // （详见 initDecoration 末尾），保证恢复动作能经由 onScrolled 的 dy 累计出真实偏移。
    }

    /**
     * 弹出标题导航弹窗
     * 这个弹窗和当前Activity联系非常紧密，为了方便起见，不归到DialogBuilderManager中去
     */
    private void showTitleNavigationDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.item_dialog_card_category_index, null);

        Dialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        // 循环绑定：弹窗按钮顺序与分节顺序一一对应，点击后滚动到对应标题
        for (int i = 0; i < CardDataCatalogData.SECTION_BUTTON_RES_IDS.length; i++) {
            final int sectionIndex = i;
            dialogView.findViewById(CardDataCatalogData.SECTION_BUTTON_RES_IDS[i]).setOnClickListener(v -> {
                runFastScroll(sectionIndex);
                dialog.dismiss();
            });
        }

        dialogView.findViewById(R.id.button_close).setOnClickListener(v -> dialog.dismiss());

        // 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
        dialog.show();
    }

    /**
     * 快速滚动：让对应分节标题停在与屏幕顶部相距约 400px 的位置（与原页面视觉一致）。
     */
    private void runFastScroll(int sectionIndex) {
        if (recyclerView == null || adapter == null || sectionIndex < 0
                || sectionIndex >= CardDataCatalogData.SECTION_PREFIXES.length) {
            return;
        }
        int headerPosition = adapter.getHeaderPosition(sectionIndex);
        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(headerPosition);

        // 等列表布局完成后再测量标题当前位置，计算并平滑滚动到目标偏移
        recyclerView.post(() -> {
            View targetView = recyclerView.getLayoutManager().findViewByPosition(headerPosition);
            if (targetView == null) {
                return; // 极端情况（如目标尚未布局），保持当前位置即可
            }
            int delta = targetView.getTop() - 400; // 还需要滚动的像素量（可正可负）
            if (delta == 0) {
                return;
            }
            // 当前滚动位置（真实像素偏移，与渐隐效果同源）
            int currentScrollY = savedScrollY;
            // 初始化值动画：实现从当前位置 → 目标位置的渐变滚动
            scrollAnimator = ValueAnimator.ofInt(currentScrollY, currentScrollY + delta);
            // 滚动时长（核心：控制顺滑度，300-500ms是安卓舒适区间，值越大越慢越丝滑）
            scrollAnimator.setDuration(500);
            // 核心插值器（决定滚动的速度变化规律，这是平滑的关键！）
            // DecelerateInterpolator：减速插值器 → 滚动由快到慢，符合人眼视觉习惯，最推荐
            scrollAnimator.setInterpolator(new DecelerateInterpolator(1.0f));
            // 逐帧按相邻帧差值增量滚动，保证滚动量与动画值严格一致
            int[] previousAnimatedValue = {currentScrollY};
            scrollAnimator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                recyclerView.scrollBy(0, animatedValue - previousAnimatedValue[0]);
                previousAnimatedValue[0] = animatedValue;
            });
            // 启动动画（加入防重复点击：先取消之前的滚动动画，再启动新的）
            scrollAnimator.cancel();
            scrollAnimator.start();
        });
    }

    /**
     * 计算背景渐隐效果使用的滚动偏移。
     * <p>
     * 不用累计值也不用估算 API：列表首行（1_1 分节标题）的内容坐标恒为 0，
     * 只要它还存在于 RecyclerView 的布局内，真实偏移 = paddingTop - 首行.getTop()，
     * 该几何测量在任何滚动方式下（手动滑动 / 动画 / scrollToPosition 布局式跳转）都精确：
     * 跳转产生的残差会在首行重新可见的瞬间被自动纠正，不会提前或推迟渐显。
     * 首行不可见说明列表已滚出渐隐窗口很远，背景保持完全消失即可。
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
        floatButtonIndex.setOnClickListener(v -> showTitleNavigationDialog());
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
        if (savedScrollY > 0) {
            final int targetScrollY = savedScrollY;
            savedScrollY = 0;
            recyclerView.post(() -> recyclerView.scrollBy(0, targetScrollY));
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

        if (scrollAnimator != null) {
            scrollAnimator.cancel();
            scrollAnimator.removeAllUpdateListeners();
            scrollAnimator = null;
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
