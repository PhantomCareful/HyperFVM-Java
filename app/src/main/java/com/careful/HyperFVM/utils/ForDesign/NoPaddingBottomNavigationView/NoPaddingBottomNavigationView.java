package com.careful.HyperFVM.utils.ForDesign.NoPaddingBottomNavigationView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoPaddingBottomNavigationView extends BottomNavigationView {

    public NoPaddingBottomNavigationView(Context context) {
        super(context);
        init();
    }

    public NoPaddingBottomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoPaddingBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 禁用系统窗口适配
        setFitsSystemWindows(false);

        // 设置WindowInsets监听
        ViewCompat.setOnApplyWindowInsetsListener(this, (view, insets) -> {
            // 不处理底部系统导航栏的insets
            int left = insets.getInsets(WindowInsetsCompat.Type.systemBars()).left;
            int right = insets.getInsets(WindowInsetsCompat.Type.systemBars()).right;
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            // 只消耗左右和顶部的insets，底部设为0
            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(WindowInsetsCompat.Type.navigationBars(),
                            androidx.core.graphics.Insets.of(left, top, right, 0))
                    .build();
        });
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        // 重写此方法，不应用底部insets
        return insets.inset(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                0  // 底部设为0
        );
    }
}