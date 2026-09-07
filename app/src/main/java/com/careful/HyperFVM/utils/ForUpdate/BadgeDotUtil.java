package com.careful.HyperFVM.utils.ForUpdate;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.google.android.material.tabs.TabLayout;

/**
 * 给纯图片TabLayout的指定Tab添加小红点的工具类（主界面底栏更新提醒）
 */
public class BadgeDotUtil {
    private static final String TAG = "BadgeDot";
    // 小红点的尺寸（dp）
    private static final int DOT_SIZE_DP = 8;
    // 红点View的Tag标记（用于可靠识别小红点，避免误删其他View）
    private static final String RED_DOT_TAG = "red_dot";

    // 定义回调接口
    public interface OnUpdateCheckComplete {
        void onComplete(boolean isShowRedDot);
    }

    /**
     * 检查更新然后返回是否显示小红点
     */
    public static void checkUpdateAndShowRedDot(Context context, OnUpdateCheckComplete callback) {
        long localAppVersionCode = LocalVersionUtil.getAppLocalVersionCode(context);

        Log.d(TAG, "localAppVersionCode = " + localAppVersionCode);

        // 调用AppUpdaterUtil检查App更新（其内部回调已切回主线程，可直接操作UI）
        AppUpdaterUtil appUpdaterUtil = AppUpdaterUtil.getInstance();
        appUpdaterUtil.checkServerVersion(new AppUpdaterUtil.OnVersionCheckCallback() {
            @Override
            public void onVersionCheckSuccess(long serverVersion, String updateLog) {
                Log.d(TAG, "serverAppVersionCode = " + serverVersion);
                callback.onComplete(serverVersion > localAppVersionCode);
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
     * 给指定位置的纯图片Tab添加小红点
     * （挂载在Tab的customView容器上：要求自定义视图根布局为FrameLayout）
     * @param tabLayout 纯图片TabLayout（主界面底栏）
     * @param position 目标Tab的位置（从0开始）
     */
    public static void showRedDot(TabLayout tabLayout, int position) {
        FrameLayout container = getTabDotContainer(tabLayout, position);
        if (container == null) return;

        // 先移除已存在的小红点（避免重复添加）
        removeRedDot(container);

        // 创建小红点View并叠加到容器右上角
        View redDotView = createRedDotView(tabLayout.getContext());
        int dotSize = DensityUtil.dpToPx(tabLayout.getContext(), DOT_SIZE_DP);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dotSize, dotSize);
        params.gravity = Gravity.TOP | Gravity.END;
        // 红点挂在Tab容器（约106dp宽×60dp高）的右上角，40dp图标居中放置，故需用margin把红点从角落收向图标：
        // - rightMargin：距容器右缘，越大红点越靠左（贴向图标右缘）
        // - topMargin：距容器顶部，越大红点越靠下（压住图标上缘）
        params.topMargin = DensityUtil.dpToPx(tabLayout.getContext(), 10);
        params.rightMargin = DensityUtil.dpToPx(tabLayout.getContext(), 14);
        container.addView(redDotView, params);
    }

    /**
     * 移除指定位置Tab上的小红点
     * @param tabLayout 纯图片TabLayout（主界面底栏）
     * @param position 目标Tab的位置（从0开始）
     */
    public static void hideRedDot(TabLayout tabLayout, int position) {
        FrameLayout container = getTabDotContainer(tabLayout, position);
        if (container == null) return;
        removeRedDot(container);
    }

    /**
     * 获取Tab的自定义视图容器（纯图片Tab的自定义视图根布局为FrameLayout）
     */
    private static FrameLayout getTabDotContainer(TabLayout tabLayout, int position) {
        if (tabLayout == null) return null;
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab == null) return null;
        View customView = tab.getCustomView();
        if (customView instanceof FrameLayout) {
            return (FrameLayout) customView;
        }
        return null;
    }

    /**
     * 创建小红点View（圆形、红色）
     */
    private static View createRedDotView(Context context) {
        ShapeDrawable redDotShape = new ShapeDrawable(new OvalShape());
        redDotShape.getPaint().setColor(0xFFba1a1a);
        View redDotView = new View(context);
        redDotView.setBackground(redDotShape);
        // 打上Tag标记，供removeRedDot可靠识别
        redDotView.setTag(RED_DOT_TAG);
        return redDotView;
    }

    /**
     * 移除指定容器内的小红点
     */
    private static void removeRedDot(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            // 通过Tag标记识别小红点（避免误删其他View）
            if (RED_DOT_TAG.equals(child.getTag())) {
                container.removeView(child);
                break;
            }
        }
    }

}
