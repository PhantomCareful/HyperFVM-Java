package com.careful.HyperFVM.Activities.DataCenter;

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
 *   <li>分节标题行（item_card_catalog_header）：显示分节标签，顶部 15dp Space
 *       提供章与章之间的间距（首个分节隐藏该 Space）；</li>
 *   <li>单卡行（item_card_catalog_card）：每张卡独立一个圆角 CardView
 *       （20dp 圆角 + ?attr/GeneralCardViewBackground，与原分组容器同款背景），
 *       绑定时 inflate 该卡的单卡布局并挂点击跳转；</li>
 *   <li>1 个页脚版权行（item_card_catalog_footer）。</li>
 * </ul>
 * 列表位置账目：[节0标题][节0卡×N] … [节K标题][节K卡×M][页脚]，
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
        int position = 0;
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
            case TYPE_HEADER -> R.layout.item_card_catalog_header;
            case TYPE_FOOTER -> R.layout.item_card_catalog_footer;
            default -> R.layout.item_card_catalog_card;
        };
        View itemView = layoutInflater.inflate(layoutRes, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.viewType == TYPE_HEADER) {
            bindHeader(holder, position);
        } else if (holder.viewType == TYPE_ITEM) {
            bindCard(holder, position);
        }
    }

    private void bindHeader(ViewHolder holder, int position) {
        int sectionIndex = Arrays.binarySearch(headerPositions, position);
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            Log.w(TAG, "找不到分节标题 position=" + position);
            return;
        }
        holder.headerTextView.setText(sections.get(sectionIndex).label);
        // 顶部 15dp Space = 章与章之间的间距：首个分节上方无"上一章"，不显示
        holder.headerTopSpace.setVisibility(sectionIndex == 0 ? View.GONE : View.VISIBLE);
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

    /** 由列表位置反查单卡条目（标题行/页脚返回 null） */
    private CardDataLetterCatalogData.CardEntry entryAt(int position) {
        if (position < 0 || position >= itemCount - 1) {
            return null;
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
