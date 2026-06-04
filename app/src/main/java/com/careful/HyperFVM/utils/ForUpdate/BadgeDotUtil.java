package com.careful.HyperFVM.utils.ForUpdate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 给BottomNavigationView添加小红点的工具类
 */
public class BadgeDotUtil {
    private static final String TAG = "BadgeDot";
    // 小红点的尺寸（dp）
    private static final int DOT_SIZE_DP = 16;

    // 定义回调接口
    public interface OnUpdateCheckComplete {
        void onComplete(boolean isShowRedDot);
    }

    /**
     * 检查更新然后返回是否显示小红点
     */
    public static void checkUpdateAndShowRedDot(Context context, OnUpdateCheckComplete callback) {
        final boolean[] isShowRedDot = {false};

        long localAppVersionCode = LocalVersionUtil.getAppLocalVersionCode(context);

        Log.d(TAG, "localAppVersionCode = " + localAppVersionCode);

        // 调用UpdaterUtil检查App更新
        AppUpdaterUtil appUpdaterUtil = AppUpdaterUtil.getInstance();
        appUpdaterUtil.checkServerVersion(new AppUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                Log.d(TAG, "serverAppVersionCode = " + serverVersion);

                try {
                    isShowRedDot[0] = serverVersion > localAppVersionCode;
                } catch (Exception e) {
                    isShowRedDot[0] = false;
                }

                callback.onComplete(isShowRedDot[0]);
            }

            @Override
            public void onVersionCheckFailure(String errorMsg) {
                Log.e(TAG, "onVersionCheckFailure: " + errorMsg);

                callback.onComplete(false);
            }

            @Override
            public void onVersionParseError() {
                callback.onComplete(false);
            }
        });
    }

    /**
     * 给指定位置的BottomNavigationItem添加小红点
     * @param navigationView 自定义的NoPaddingBottomNavigationView
     * @param position 目标Item的位置（从0开始）
     */
    public static void showRedDot(BottomNavigationView navigationView, int position) {
        // 1. 确保导航栏已加载完成，获取目标Item的View
        @SuppressLint("RestrictedApi") BottomNavigationItemView itemView = getBottomNavigationItemView(navigationView, position);
        if (itemView == null) return;

        // 2. 先移除已存在的小红点（避免重复添加）
        removeRedDot(itemView);

        // 3. 创建小红点的Shape（圆形、红色）
        ShapeDrawable redDotShape = new ShapeDrawable(new OvalShape());
        redDotShape.getPaint().setColor(0xFFba1a1a);

        // 4. 封装小红点为View
        View redDotView = new View(navigationView.getContext());
        redDotView.setBackground(redDotShape);

        // 5. 设置小红点的布局参数（位置：右上角，尺寸：8dp）
        int dotSize = DensityUtil.dpToPx(navigationView.getContext(), DOT_SIZE_DP);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dotSize, dotSize);
        // 调整margin控制红点与图标的偏移（可根据需求修改）
        params.gravity = Gravity.TOP | Gravity.END;
        params.topMargin = DensityUtil.dpToPx(navigationView.getContext(), 12);
        params.rightMargin = DensityUtil.dpToPx(navigationView.getContext(), 12);

        // 6. 将小红点添加到ItemView中
        itemView.addView(redDotView, params);
    }

    /**
     * 移除指定位置的小红点
     * @param navigationView 自定义的NoPaddingBottomNavigationView
     * @param position 目标Item的位置（从0开始）
     */
    public static void hideRedDot(BottomNavigationView navigationView, int position) {
        @SuppressLint("RestrictedApi") BottomNavigationItemView itemView = getBottomNavigationItemView(navigationView, position);
        if (itemView == null) return;
        removeRedDot(itemView);
    }

    /**
     * 移除单个ItemView内的小红点
     */
    private static void removeRedDot(@SuppressLint("RestrictedApi") BottomNavigationItemView itemView) {
        for (int i = 0; i < itemView.getChildCount(); i++) {
            View child = itemView.getChildAt(i);
            // 通过背景类型判断是否是小红点（避免误删其他View）
            if (child.getBackground() instanceof ShapeDrawable) {
                itemView.removeView(child);
                break;
            }
        }
    }

    /**
     * 获取指定位置的BottomNavigationItemView
     */
    @SuppressLint("RestrictedApi")
    private static BottomNavigationItemView getBottomNavigationItemView(BottomNavigationView navigationView, int position) {
        // BottomNavigationView的子View是FrameLayout，再往下是LinearLayout（包含所有Item）
        ViewGroup navigationMenuView = (ViewGroup) navigationView.getChildAt(0);
        if (navigationMenuView.getChildCount() <= position) {
            return null;
        }
        View itemView = navigationMenuView.getChildAt(position);
        if (itemView instanceof BottomNavigationItemView) {
            return (BottomNavigationItemView) itemView;
        }
        return null;
    }

}
