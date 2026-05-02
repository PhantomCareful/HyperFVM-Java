package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;

public class DensityUtil {

    /**
     * 将 px 值转换为 dp 值
     * @param context 上下文
     * @param pxValue 像素值（如从 WindowInsets 获取的 bottom）
     * @return 对应的 dp 值
     */
    public static int pxToDp(Context context, int pxValue) {
        if (context == null || context.getResources() == null) return 0;
        int density = (int) context.getResources().getDisplayMetrics().density;
        return pxValue / density;
    }

    public static int dpToPx(Context context, int dpValue) {
        if (context == null || context.getResources() == null) return 0;
        int density = (int) context.getResources().getDisplayMetrics().density;
        return dpValue * density;
    }
}
