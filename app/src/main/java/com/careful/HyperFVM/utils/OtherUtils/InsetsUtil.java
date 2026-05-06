package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetsUtil {

    /**
     * 动态获取并处理底部导航栏高度
     * @param view 通常是 Activity 的根容器
     * @param heightConsumer 一个用于接收高度的回调
     */
    public static void getNavigationBarHeight(Context context, View view, Consumer<Integer> heightConsumer) {
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
    public static void getStatusBarHeight(Context context, View view, Consumer<Integer> heightConsumer) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            if (heightConsumer != null) {
                heightConsumer.accept(statusBarHeight == 0 ? DensityUtil.dpToPx(context, 36) : statusBarHeight);
            }

            return insets;
        });
    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
