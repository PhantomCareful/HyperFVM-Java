package com.careful.HyperFVM.utils.ForDesign.Glow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * OOBE 欢迎页 glow 背景容器（移植自 HyperCeiler provision renderengine.RenderViewLayout）。
 * 子 View 按 mChildScale（0.2）尺寸布局、再 setScale(1/scale) 放大铺满——shader 只算
 * 1/25 的像素（降采样省 GPU），放大后低分辨率噪声天然柔化。
 */
public class RenderViewLayout extends ViewGroup {

    float mChildScale = 0.5f;

    public RenderViewLayout(Context context) {
        super(context);
    }

    public RenderViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RenderViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RenderViewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        int iCeil = (int) Math.ceil(getWidth() * mChildScale);
        int iCeil2 = (int) Math.ceil(getHeight() * mChildScale);
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt.getVisibility() != View.GONE) {
                childAt.setScaleX(1.0f / mChildScale);
                childAt.setScaleY(1.0f / mChildScale);
                int width = (int) ((getWidth() - iCeil) * 0.5f);
                int height = (int) ((getHeight() - iCeil2) * 0.5f);
                childAt.layout(width, height, width + iCeil, height + iCeil2);
            }
        }
    }

    public void attachView(View view, float f) {
        this.mChildScale = f;
        addView(view);
        view.setBackgroundColor(-16777216);
    }

    public void attachView(View view, float f, int i) {
        this.mChildScale = f;
        addView(view);
        view.setBackgroundColor(i);
    }

    public int getInternalWidth() {
        return (int) (getWidth() * this.mChildScale);
    }

    public int getInternalHeight() {
        return (int) (getHeight() * this.mChildScale);
    }
}
