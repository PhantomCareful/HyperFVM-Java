package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 控制RecyclerView中CardView的首尾margin
 * 首项加topMargin，末项加bottomMargin，中间Item无额外margin
 */
public class CardItemDecoration extends RecyclerView.ItemDecoration {
    private final int topMargin; // 首项top margin（dp转px）
    private final int bottomMargin; // 末项bottom margin（dp转px）
    private final int normalHorizontalMargin; // 所有Item的水平margin（可选）

    // 构造方法：传入dp值，内部自动转px
    public CardItemDecoration(RecyclerView recyclerView, int topMarginDp, int bottomMarginDp) {
        // dp转px（适配不同分辨率）
        float density = recyclerView.getContext().getResources().getDisplayMetrics().density;
        this.topMargin = (int) (topMarginDp * density + 0.5f);
        this.bottomMargin = (int) (bottomMarginDp * density + 0.5f);
        this.normalHorizontalMargin = (int) (5 * density + 0.5f); // 你原有布局的5dp水平margin
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // 获取当前Item的位置
        int position = parent.getChildAdapterPosition(view);
        // 获取Adapter的总Item数
        int itemCount = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();

        // 重置所有margin（避免叠加）
        outRect.top = 0;
        outRect.bottom = 0;
        outRect.left = normalHorizontalMargin;
        outRect.right = normalHorizontalMargin;

        // 首项：加top margin
        if (position == 0) {
            outRect.top = topMargin;
        }
        // 末项：加bottom margin（注意itemCount>0，避免空列表崩溃）
        if (itemCount > 0 && position == itemCount - 1) {
            outRect.bottom = bottomMargin;
        }
    }
}