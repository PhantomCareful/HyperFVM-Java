package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category.CategoryArray;
import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.OddsDisclosureHelper;
import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;

public class OddsDisclosureIndexActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_cookery_index_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）

    // 动态添加的分节标题（与 CategoryArray.titleArray 一一对应），目录下拉菜单的跳转锚点；
    // 标题无固定 id，由 OddsDisclosureHelper.loadIndexItem 返回收集
    private List<TextView> sectionTitleViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_odds_index);

        // 初始化各种装饰效果
        initDecoration();

        // 初始化卡片点击事件
        findViewById(R.id.tips_odds_index_1_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                this,
                null,
                0,
                "4399美食大战老鼠",
                "概率公示说明",
                "https://my.4399.com/forums/thread-63198880"));

        sectionTitleViews = OddsDisclosureHelper.loadIndexItem(this, findViewById(R.id.odds_disclosure_index_item_container));
    }

    /**
     * 以目录按钮为锚点弹出章节跳转下拉菜单（样式与食神谱图鉴目录菜单一致，仅无选中对勾）。
     * 菜单条目直接取 CategoryArray.titleArray——与页面动态添加的分节标题同源，天然一一对应
     */
    private void showIndexDropdown(View anchor) {
        String[] entries = CategoryArray.titleArray;
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
        // 极端大字体下限制菜单宽度不超出屏幕
        contentWidth = Math.min(contentWidth, anchor.getRootView().getWidth());

        ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAnchorView(anchor);
        popup.setWidth(contentWidth);
        popup.setHeight(itemHeight * entries.length);
        popup.setVerticalOffset((int) (4 * getResources().getDisplayMetrics().density));
        // 菜单右缘与按钮右缘对齐（水平偏移取负值左移），点击行外区域自动关闭
        popup.setHorizontalOffset(anchor.getWidth() - contentWidth);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_dropdown_background));
        popup.setAdapter(adapter);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            // 标题是动态添加的：用收集到的标题列表按位置取锚点（替代固定 id 数组）
            if (position >= 0 && position < sectionTitleViews.size()) {
                scrollToSectionTitle(sectionTitleViews.get(position));
            }
        });
        popup.show();
    }

    /**
     * 平滑滚动到指定分节标题：标题最终停在顶部模糊栏下方（留一点间距），滚动中正常联动顶部栏渐显
     */
    private void scrollToSectionTitle(View titleView) {
        ScrollView scrollView = findViewById(R.id.scrollView);
        View blurViewTopBar = findViewById(R.id.blurViewTopBar);
        int[] scrollLocation = new int[2];
        int[] titleLocation = new int[2];
        scrollView.getLocationInWindow(scrollLocation);
        titleView.getLocationInWindow(titleLocation);
        // 目标位置 = 标题内容坐标 - 顶部模糊栏高度 - 呼吸间距（ScrollView 内部会自行 clamp 边界）
        int prefixSpace = blurViewTopBar.getHeight() + DensityUtil.dpToPx(this, 10);
        int targetY = scrollView.getScrollY() + (titleLocation[1] - scrollLocation[1]) - prefixSpace;
        scrollView.smoothScrollTo(0, targetY);
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        ImageButton floatButtonIndex = findViewById(R.id.FloatButton_Index);
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
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout oddsIndexContainer = findViewById(R.id.OddsIndex_Container);
        InsetsUtil.setMarginHorizontal(this, oddsIndexContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oddsIndexContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            oddsIndexContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonIndex.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonIndex.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonIndex.setOnClickListener(this::showIndexDropdown);

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        nestedScrollUtil = NestedScrollUtil.attach(
                findViewById(R.id.scrollView),
                findViewById(R.id.topBarBottom),
                topBar,
                blurViewTopBar,
                TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_odds_index_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onDestroy() {
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