package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.careful.HyperFVM.R;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
    private List<CardSuggestion> mData;
    private final OnItemClickListener mListener;
    private final Context mContext; // 用于加载图片资源

    // 点击回调接口：返回选中的CardSuggestion
    public interface OnItemClickListener {
        void onItemClick(CardSuggestion suggestion);
    }

    // 构造方法：传入上下文、数据、点击监听
    public SuggestionAdapter(Context context, List<CardSuggestion> data, OnItemClickListener listener) {
        this.mContext = context;
        this.mData = data;
        this.mListener = listener;
    }

    // 动态更新数据
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CardSuggestion> newData) {
        this.mData = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardSuggestion suggestion = mData.get(position);
        // 设置卡片名称
        holder.tvName.setText(suggestion.getName());
        // 加载卡片图片
        loadImage(suggestion.getImageId(), holder.ivImage);
        // 点击事件：返回完整的CardSuggestion对象
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    // 加载图片：根据image_id从drawable获取资源
    private void loadImage(String imageId, ImageView imageView) {
        if (imageId == null || imageId.isEmpty() || imageId.equals("无")) {
            return;
        }

        // 根据image_id获取drawable资源ID
        @SuppressLint("DiscouragedApi") int imageResId = mContext.getResources().getIdentifier(
                imageId,
                "drawable",
                mContext.getPackageName()
        );

        if (imageResId != 0) {
            // 资源存在：设置图片
            imageView.setImageResource(imageResId);
        }
    }

    // ViewHolder：绑定文字和图片控件
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.suggestion_text);
            ivImage = itemView.findViewById(R.id.suggestion_image);
        }
    }
}