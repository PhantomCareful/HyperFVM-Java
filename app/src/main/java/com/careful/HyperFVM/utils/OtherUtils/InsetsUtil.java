package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;

public class InsetsUtil {

    /**
     * 动态获取并处理底部导航栏高度
     * @param view 通常是 Activity 的根容器
     * @param heightConsumer 一个用于接收高度的回调
     */
    public static void setNavigationBarHeight(Context context, View view, Consumer<Integer> heightConsumer) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (heightConsumer != null) {
                heightConsumer.accept(navigationBarHeight == 0 ? DensityUtil.dpToPx(context, 36) : navigationBarHeight + DensityUtil.dpToPx(context, 12));
            }

            return insets;
        });
    }

    /**
     * 动态获取并处理状态栏高度
     * @param view 通常是 Activity 的根容器
     * @param heightConsumer 一个用于接收高度的回调
     */
    public static void setStatusBarHeight(Context context, View view, Consumer<Integer> heightConsumer) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            if (heightConsumer != null) {
                heightConsumer.accept(statusBarHeight == 0 ? DensityUtil.dpToPx(context, 36) : statusBarHeight);
            }

            return insets;
        });
    }

    /**
     * 动态设置水平边距
     * 手机：10dp，PAD：20dp
     * @param view 通常是 Activity 的根容器
     * @param horizontalConsumer 一个用于接收间距的回调
     */
    public static void setMarginHorizontal(Context context, View view, Consumer<Integer> horizontalConsumer) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            if (horizontalConsumer != null) {
                horizontalConsumer.accept(SmallestWidthUtil.getSmallestWidthDp() < 600 ?
                        DensityUtil.dpToPx(context, 10) : DensityUtil.dpToPx(context, 20)
                );
            }

            return insets;
        });
    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
