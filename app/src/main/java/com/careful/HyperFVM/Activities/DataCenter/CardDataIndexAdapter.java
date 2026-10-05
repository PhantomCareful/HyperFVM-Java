package com.careful.HyperFVM.Activities.DataCenter;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 防御卡目录页（CardDataIndexActivity）的列表适配器。
 * <p>
 * 将“单个巨型 ScrollView + 367 个静态卡片组件”的页面改为虚拟化列表，按
 * {@link CardDataLetterCatalogData} 的字母分节（0 / A-Z / #，完整拼音字典序）排布：
 * <ul>
 *   <li>页首行（item_card_catalog_page_header，position 0）：topBarBottom 大标题
 *       + 概览卡片区，参与顶部栏滚动联动；概览卡片按约定 id 绑定 CSV 统计
 *       （bindOverviewInfo，增删卡片时须保留这些 id 与顺序约定），
 *       并挂 TILT 按压动画（bindOverviewPressFeedback），其余内容可自由编辑；</li>
 *   <li>分节标题行（item_card_catalog_header）：显示分节标签，顶部 15dp Space
 *       提供章与章之间的间距；</li>
 *   <li>单卡行（item_card_catalog_card）：每张卡独立一个圆角 CardView
 *       （20dp 圆角 + ?attr/GeneralCardViewBackground，与原分组容器同款背景），
 *       绑定时 inflate 该卡的单卡布局并挂点击跳转；</li>
 *   <li>1 个页脚版权行（item_card_catalog_footer）。</li>
 * </ul>
 * 列表位置账目：[页首][节0标题][节0卡×N] … [节K标题][节K卡×M][页脚]，
 * 分节标题位置表在构造时一次算好，供快速滚动跳转查询。
 * <p>
 * 单卡布局文件（card_card_data_index_&lt;image_id&gt;.xml）被其他页面（如辅助卡列表页）复用，
 * 必须原样保留。增删卡片只需改 assets/card_data_index.csv 与单卡布局文件，
 * 字母分节顺序由数据层自动推导，见数据层类注释。
 */
public class CardDataIndexAdapter extends RecyclerView.Adapter<CardDataIndexAdapter.ViewHolder> {

    private static final String TAG = "CardDataIndexAdapter";

    /** 分节标题行 */
    public static final int TYPE_HEADER = 0;
    /** 单卡行（一张卡一个 CardView） */
    public static final int TYPE_ITEM = 1;
    /** 页脚版权行 */
    public static final int TYPE_FOOTER = 2;
    /** 页首行（topBarBottom 大标题 + 快捷卡片区） */
    public static final int TYPE_PAGE_HEADER = 3;

    /** 页首第 4 张小卡（生肖卡）的种数口径：card_data_4 的 baseName 种数 - 星座卡数量 */
    private static final int CONSTELLATION_CARD_NUM = 13;

    /** 页首绑定回调：页首位于 RecyclerView 内部，Activity 在视图绑定后经此注入 topBarBottom */
    public interface PageHeaderBinder {
        void onPageHeaderBound(View topBarBottom);
    }

    private PageHeaderBinder pageHeaderBinder;

    /** 注入页首绑定回调（须在首次 layout 前注册，即 onCreate 内调用即可） */
    public void setPageHeaderBinder(PageHeaderBinder binder) {
        this.pageHeaderBinder = binder;
    }

    private final Context context;
    /** 字母分节（0 / A-Z / #，空桶已隐藏），顺序即显示顺序 */
    private final List<CardDataLetterCatalogData.Section> sections;
    /** 每个分节标题行在列表中的位置（升序，下标与 sections 一致） */
    private final int[] headerPositions;
    /** 列表总条目数（标题行 + 全部单卡行 + 页脚） */
    private final int itemCount;
    /** 单卡布局缓存：布局名 -> 资源 id */
    private final Map<String, Integer> cellLayoutCache = new HashMap<>();

    private final LayoutInflater layoutInflater;

    public CardDataIndexAdapter(Context context, List<CardDataLetterCatalogData.Section> sections) {
        this.context = context;
        this.sections = sections;
        this.layoutInflater = LayoutInflater.from(context);

        headerPositions = new int[sections.size()];
        int position = 1; // position 0 固定是页首行
        for (int i = 0; i < sections.size(); i++) {
            headerPositions[i] = position;
            position += 1 + sections.get(i).cards.size();
        }
        itemCount = position + 1; // 末位是页脚
    }

    /**
     * 返回第 sectionIndex 个分节标题在列表中的位置（供快速滚动使用），越界返回 -1。
     */
    public int getHeaderPosition(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= headerPositions.length) {
            return -1;
        }
        return headerPositions[sectionIndex];
    }

    /**
     * 分节数量（导航条与跳转的目标范围是 [0, sectionCount)）。
     */
    public int getSectionCount() {
        return sections.size();
    }

    /**
     * 返回页脚位置。
     */
    public int getFooterPosition() {
        return itemCount - 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = switch (viewType) {
            case TYPE_PAGE_HEADER -> R.layout.item_card_catalog_page_header;
            case TYPE_HEADER -> R.layout.item_card_catalog_header;
            case TYPE_FOOTER -> R.layout.item_card_catalog_footer;
            default -> R.layout.item_card_catalog_card;
        };
        View itemView = layoutInflater.inflate(layoutRes, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.viewType == TYPE_PAGE_HEADER) {
            bindPageHeader(holder);
        } else if (holder.viewType == TYPE_HEADER) {
            bindHeader(holder, position);
        } else if (holder.viewType == TYPE_ITEM) {
            bindCard(holder, position);
        }
    }

    /**
     * 绑定页首行：概览信息（大卡总种数 + 4 张小卡）与按压动画
     * + 把 topBarBottom 交给调用方（Activity 用于顶部栏滚动联动）。
     */
    private void bindPageHeader(ViewHolder holder) {
        View rootView = holder.itemView;
        bindOverviewInfo(rootView);
        bindOverviewPressFeedback(rootView);
        if (pageHeaderBinder == null) {
            return;
        }
        View topBarBottom = rootView.findViewById(R.id.topBarBottom);
        if (topBarBottom != null) {
            pageHeaderBinder.onPageHeaderBound(topBarBottom);
        } else {
            Log.w(TAG, "页首布局缺少 topBarBottom（联动失效），请检查 item_card_catalog_page_header.xml");
        }
    }

    /**
     * 绑定页首概览卡片（数据源：card_data_index.csv，统计见 {@link CardDataLetterCatalogData.OverviewStats}）：
     * <ul>
     *   <li>大卡 content_2：全表 baseName 种数 + 单位“张”；</li>
     *   <li>4 张小卡：标题依次为 普通卡/融合卡/金卡/生肖卡，种数依次取
     *       card_data_1/card_data_2/card_data_3 的 baseName 种数，
     *       生肖卡 = card_data_4 种数 - {@link #CONSTELLATION_CARD_NUM}。</li>
     * </ul>
     * 4 张小卡共用 card_card_data_index_overview_small.xml，子 View id 完全相同，
     * 且 include 上的 android:id 对 &lt;merge&gt; 根无效（LayoutInflater 直接忽略），
     * 因此按<b>文档序</b>递归收集——天然对应第 1→4 张；调整小卡顺序或增删卡片时，
     * 下面的标题/数据表数组须与布局顺序保持一致。
     */
    @SuppressLint("SetTextI18n")
    private void bindOverviewInfo(View rootView) {
        CardDataLetterCatalogData.OverviewStats stats =
                CardDataLetterCatalogData.getOverviewStats(context);

        TextView bigContent = rootView.findViewById(R.id.card_data_index_overview_big_content_2);
        if (bigContent != null) {
            bigContent.setText(stats.totalBaseNames + "张");
        } else {
            Log.w(TAG, "页首布局缺少 card_data_index_overview_big_content_2，总种数无法展示");
        }

        List<TextView> smallContents = new ArrayList<>();
        List<TextView> smallTitles = new ArrayList<>();
        collectById(rootView, R.id.card_data_index_overview_small_content, smallContents);
        collectById(rootView, R.id.card_data_index_overview_small_title, smallTitles);

        String[] titles = {"普通卡", "金卡", "融合卡", "生肖卡"};
        String[] tables = {"card_data_1", "card_data_3", "card_data_2", "card_data_4"};
        int[] offsets = {0, 0, 0, -CONSTELLATION_CARD_NUM};
        int count = Math.min(titles.length, Math.min(smallContents.size(), smallTitles.size()));
        if (count < titles.length) {
            Log.w(TAG, "页首小卡不完整：期望 " + titles.length + " 张，实得 " + smallContents.size());
        }
        for (int i = 0; i < count; i++) {
            smallTitles.get(i).setText(titles[i]);
            // Math.max 兜底：CSV 变动导致差值为负时不显示负数
            int baseNames = Math.max(0, stats.baseNamesOf(tables[i]) + offsets[i]);
            smallContents.get(i).setText(String.valueOf(baseNames));
        }
    }

    /**
     * 给概览区 1 张大卡 + 4 张小卡挂按压动画（TILT 倾斜反馈，与 Dashboard 卡片一致）。
     * 卡片容器 id：大卡唯一（findViewById 即可）；小卡 4 个同 id，
     * 同 bindOverviewInfo 按文档序收集。
     */
    @SuppressLint("ClickableViewAccessibility")
    private void bindOverviewPressFeedback(View rootView) {
        View bigCard = rootView.findViewById(R.id.card_data_index_overview_big_container);
        if (bigCard != null) {
            bigCard.setOnTouchListener((v, event) -> setPressFeedbackAnimation(
                    v, event, PressFeedbackAnimationUtils.PressFeedbackType.TILT));
        } else {
            Log.w(TAG, "页首布局缺少 card_data_index_overview_big_container，按压动画未生效");
        }
        List<View> smallCards = new ArrayList<>();
        collectById(rootView, R.id.card_data_index_overview_small_container, smallCards);
        for (View card : smallCards) {
            card.setOnTouchListener((v, event) -> setPressFeedbackAnimation(
                    v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        }
    }

    /**
     * 按文档序递归收集指定 id 的 View（重复 id 场景下 findViewById 只能命中首个）。
     * 类型安全由调用方保证：该 id 在布局中只对应一种 View 类型。
     */
    @SuppressWarnings("unchecked")
    private static <T extends View> void collectById(View view, int id, List<T> out) {
        if (view.getId() == id) {
            out.add((T) view);
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                collectById(group.getChildAt(i), id, out);
            }
        }
    }

    private void bindHeader(ViewHolder holder, int position) {
        int sectionIndex = Arrays.binarySearch(headerPositions, position);
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            Log.w(TAG, "找不到分节标题 position=" + position);
            return;
        }
        holder.headerTextView.setText(sections.get(sectionIndex).label);
        // 顶部 15dp Space = 章与章之间的间距（页首与首个分节之间同样靠它分隔），恒显示
        holder.headerTopSpace.setVisibility(View.VISIBLE);
    }

    /**
     * 绑定一张单卡：布局变化时重新 inflate（点击事件随 inflate 挂上），否则直接复用。
     */
    private void bindCard(ViewHolder holder, int position) {
        CardDataLetterCatalogData.CardEntry entry = entryAt(position);
        if (entry == null) {
            Log.w(TAG, "找不到单卡条目 position=" + position);
            return;
        }
        String cellKey = entry.imageId;
        if (cellKey.equals(holder.lastInflatedCellKey)) {
            return; // 复用视图对应同一张卡，点击事件仍有效
        }
        holder.cardContainer.removeAllViews();
        View cellView = layoutInflater.inflate(
                resolveCellLayoutRes(entry.imageId), holder.cardContainer, false);
        // 每张卡片都有唯一对应的点击事件：跳转到该卡的详细数据页
        cellView.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, entry.name));
        holder.cardContainer.addView(cellView);
        holder.lastInflatedCellKey = cellKey;
    }

    /** 由列表位置反查单卡条目（页首/标题行/页脚返回 null） */
    private CardDataLetterCatalogData.CardEntry entryAt(int position) {
        if (position < 1 || position >= itemCount - 1) {
            return null; // position 0 是页首，binarySearch 未命中会误算成 sectionIndex=-1，必须先挡掉
        }
        int hit = Arrays.binarySearch(headerPositions, position);
        if (hit >= 0) {
            return null; // 标题行
        }
        // 未命中：插入点 - 1 即所属分节（详见 binarySearch 返回值语义）
        int sectionIndex = -hit - 2;
        int cardIndex = position - headerPositions[sectionIndex] - 1;
        List<CardDataLetterCatalogData.CardEntry> cards = sections.get(sectionIndex).cards;
        if (cardIndex < 0 || cardIndex >= cards.size()) {
            return null;
        }
        return cards.get(cardIndex);
    }

    /**
     * 动态解析单卡布局资源：布局名 = "card_card_data_index_" + image_id
     * （如 card_card_data_index_x11130060，与 assets CSV 的 image_id 列一一对应）。
     */
    private int resolveCellLayoutRes(String imageId) {
        String layoutName = "card_card_data_index_" + imageId;
        Integer cached = cellLayoutCache.get(layoutName);
        if (cached != null) {
            return cached;
        }
        @SuppressLint("DiscouragedApi") int resId = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
        if (resId == 0) {
            throw new IllegalStateException("找不到单卡布局资源：" + layoutName);
        }
        cellLayoutCache.put(layoutName, resId);
        return resId;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PAGE_HEADER;
        }
        if (position == itemCount - 1) {
            return TYPE_FOOTER;
        }
        // headerPositions 升序：命中即标题行，落在两个标题之间即单卡行
        return Arrays.binarySearch(headerPositions, position) >= 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final int viewType;
        TextView headerTextView;
        View headerTopSpace;
        FrameLayout cardContainer;
        String lastInflatedCellKey;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            if (viewType == TYPE_HEADER) {
                headerTextView = itemView.findViewById(R.id.card_catalog_header_label);
                headerTopSpace = itemView.findViewById(R.id.card_catalog_header_top_space);
            } else if (viewType == TYPE_ITEM) {
                cardContainer = itemView.findViewById(R.id.card_catalog_card_container);
            }
        }
    }
}
