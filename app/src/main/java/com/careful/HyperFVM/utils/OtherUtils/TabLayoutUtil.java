package com.careful.HyperFVM.utils.OtherUtils;

import android.content.Context;
import android.util.TypedValue;

import com.google.android.material.tabs.TabLayout;

/**
 * TabLayout 文字颜色工具类
 */
public class TabLayoutUtil {

    /**
     * 将 TabLayout 文字颜色设置为跟随主题动态色（colorOnSurface）：
     * 选中 = colorOnSurface（100%），未选中 = colorOnSurface 叠 50% 透明
     * <p>
     * 注意：TabLayout 的文字颜色不受 tabTextAppearance 里的 android:textColor 控制
     * （TabView 刷新时会用内部的选中/未选中颜色列表覆盖它），所以这里的颜色必须通过
     * setTabTextColors 设置；且主题是 DynamicColors 动态取色，XML 无法对 ?attr/colorOnSurface
     * 叠加透明度，只能在代码里解析主题色后再计算半透明色。
     */
    public static void setOnSurfaceTextColors(Context context, TabLayout tabLayout) {
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)) {
            return;
        }
        int colorOnSurface = typedValue.data;
        // 未选中颜色 = colorOnSurface 叠 50% 透明（0x80 = 128 ≈ 255 * 0.5）
        int unselectedColor = (colorOnSurface & 0x00FFFFFF) | 0x80000000;
        // setTabTextColors(未选中颜色, 选中颜色)
        tabLayout.setTabTextColors(unselectedColor, colorOnSurface);
    }
}
