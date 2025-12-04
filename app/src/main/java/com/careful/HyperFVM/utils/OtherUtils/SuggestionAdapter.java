package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.careful.HyperFVM.R;

import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
    private List<String> mData;
    private final OnItemClickListener mListener;

    // 点击回调接口
    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public SuggestionAdapter(List<String> data, OnItemClickListener listener) {
        mData = data;
        mListener = listener;
    }

    // 新增：动态更新数据
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<String> newData) {
        mData = newData;
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
        String item = mData.get(position);
        holder.textView.setText(item);
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.suggestion_text);
        }
    }
}
