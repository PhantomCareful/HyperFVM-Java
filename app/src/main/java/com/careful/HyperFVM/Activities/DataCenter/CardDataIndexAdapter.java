package com.careful.HyperFVM.Activities.DataCenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 防御卡目录页（CardDataIndexActivity）的列表适配器。
 * <p>
 * 将原先“单个巨型 ScrollView + 367 个静态卡片组件”的页面改为虚拟化列表：
 * - 47 个分节标题行（item_card_catalog_header）
 * - 47 个分节卡片组行（item_card_catalog_group：运行时组装圆角 CardView 分组容器，
 *   按数据表顺序 inflate 该分节全部单卡布局并绑定点击，视觉与原先
 *   card_card_data_index_X_Y.xml 的分组背景完全一致）
 * - 1 个页脚版权行（item_card_catalog_footer）
 * <p>
 * 单卡布局文件（card_card_data_index_X_Y_Z.xml）被其他页面（如辅助卡列表页）复用，
 * 必须原样保留；分节容器文件已可由本类运行时组装替代（即原先的 47 个
 * card_card_data_index_X_Y.xml 已不再需要）。增删卡片只需改 CardDataCatalogData
 * 数据表与单卡布局文件，见数据表类注释。
 */
public class CardDataIndexAdapter extends RecyclerView.Adapter<CardDataIndexAdapter.ViewHolder> {

    private static final String TAG = "CardDataIndexAdapter";

    /** 分节标题行 */
    public static final int TYPE_HEADER = 0;
    /** 分节卡片组行 */
    public static final int TYPE_GROUP = 1;
    /** 页脚版权行 */
    public static final int TYPE_FOOTER = 2;

    /** 分组容器圆角（与原分节文件 cardCornerRadius="20dp" 保持一致） */
    private static final int GROUP_CORNER_RADIUS_DP = 20;

    private final Context context;
    /** 47 个分节的标题文案（顺序与 CardDataCatalogData.PREFIXES 一致） */
    private final List<String> sectionTitles;
    /** 单卡布局缓存：布局名 -> 资源 id */
    private final Map<String, Integer> cellLayoutCache = new HashMap<>();

    private final LayoutInflater layoutInflater;

    public CardDataIndexAdapter(Context context, List<String> sectionTitles) {
        this.context = context;
        this.sectionTitles = sectionTitles;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 返回第 i 个分节标题在列表中的位置（供快速滚动使用）。
     */
    public int getHeaderPosition(int sectionIndex) {
        return sectionIndex * 2;
    }

    /**
     * 返回第 i 个分节卡片组在列表中的位置。
     */
    public int getGroupPosition(int sectionIndex) {
        return sectionIndex * 2 + 1;
    }

    /**
     * 返回页脚位置。
     */
    public int getFooterPosition() {
        return getItemCount() - 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = switch (viewType) {
            case TYPE_HEADER -> R.layout.item_card_catalog_header;
            case TYPE_FOOTER -> R.layout.item_card_catalog_footer;
            default -> R.layout.item_card_catalog_group;
        };
        View itemView = layoutInflater.inflate(layoutRes, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.viewType == TYPE_HEADER) {
            bindHeader(holder, position / 2);
        } else if (holder.viewType == TYPE_GROUP) {
            bindGroup(holder, position / 2);
        }
    }

    private void bindHeader(ViewHolder holder, int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= sectionTitles.size()) {
            Log.w(TAG, "找不到分节标题 sectionIndex=" + sectionIndex);
            return;
        }
        holder.headerTextView.setText(sectionTitles.get(sectionIndex));
    }

    /**
     * 绑定一个分节卡片组：分节变化时重新组装整组，否则直接复用（点击事件已在组装时挂好）。
     */
    private void bindGroup(ViewHolder holder, int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= CardDataCatalogData.PREFIXES.length) {
            Log.w(TAG, "找不到分节卡片组 sectionIndex=" + sectionIndex);
            return;
        }
        String prefix = CardDataCatalogData.PREFIXES[sectionIndex];

        View groupView = holder.groupContainer.getChildAt(0);
        if (groupView == null || !prefix.equals(holder.lastInflatedPrefix)) {
            holder.groupContainer.removeAllViews();
            groupView = buildSectionCardGroup(sectionIndex);
            holder.groupContainer.addView(groupView);
            holder.lastInflatedPrefix = prefix;
        }
    }

    /**
     * 创建与原始分节容器同款外观的卡片组：圆角 CardView
     * （?attr/GeneralCardViewBackground 背景 + 20dp 圆角）内竖向排列该分节全部单卡，
     * 单卡数量与顺序以 CardDataCatalogData.NAMES 为准，并给每张卡绑定唯一点击事件。
     */
    private View buildSectionCardGroup(int sectionIndex) {
        // 分组容器：样式与原先 card_card_data_index_X_Y.xml 根 CardView 保持一致
        CardView groupCard = new CardView(context);
        groupCard.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        groupCard.setRadius(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, GROUP_CORNER_RADIUS_DP,
                context.getResources().getDisplayMetrics()));
        groupCard.setCardBackgroundColor(resolveGeneralCardViewBackgroundColor());

        // 原分节文件内部为竖向 LinearLayout 依次 include 各单卡，这里等价实现
        LinearLayout innerLayout = new LinearLayout(context);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        groupCard.addView(innerLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        String prefix = CardDataCatalogData.PREFIXES[sectionIndex];
        String[] cardNames = CardDataCatalogData.NAMES[sectionIndex];
        for (int row = 0; row < cardNames.length; row++) {
            View cellView = layoutInflater.inflate(
                    resolveCellLayoutRes(prefix, row + 1), innerLayout, false);
            String cardName = cardNames[row];
            // 每张卡片都有唯一对应的点击事件：跳转到该卡的详细数据页
            cellView.setOnClickListener(v -> CardDataHelper.selectCardDataByName(context, cardName));
            innerLayout.addView(cellView);
        }
        return groupCard;
    }

    /**
     * 动态解析单卡布局资源：布局名 = "card_" + 分节前缀 + "_" + 行号
     * （如 card_card_data_index_1_1_1，与原 include 体系命名一致）。
     */
    private int resolveCellLayoutRes(String prefix, int row) {
        String layoutName = "card_" + prefix + "_" + row;
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

    /**
     * 解析当前主题下 ?attr/GeneralCardViewBackground 的实际颜色（分组卡片背景）。
     */
    private int resolveGeneralCardViewBackgroundColor() {
        TypedArray typedArray = context.obtainStyledAttributes(
                new int[]{R.attr.GeneralCardViewBackground});
        try {
            return typedArray.getColor(0, 0);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getFooterPosition()) {
            return TYPE_FOOTER;
        }
        return (position % 2 == 0) ? TYPE_HEADER : TYPE_GROUP;
    }

    @Override
    public int getItemCount() {
        return CardDataCatalogData.PREFIXES.length * 2 + 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final int viewType;
        TextView headerTextView;
        FrameLayout groupContainer;
        String lastInflatedPrefix;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            if (viewType == TYPE_HEADER) {
                headerTextView = (TextView) itemView;
            } else if (viewType == TYPE_GROUP) {
                groupContainer = itemView.findViewById(R.id.card_catalog_group_container);
            }
        }
    }
}
