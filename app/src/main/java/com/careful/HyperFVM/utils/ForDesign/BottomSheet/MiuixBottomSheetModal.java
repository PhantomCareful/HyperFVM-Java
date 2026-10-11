package com.careful.HyperFVM.utils.ForDesign.BottomSheet;

import android.app.Activity;
import android.widget.FrameLayout;

import fan.bottomsheet.BottomSheetBehavior;
import fan.bottomsheet.BottomSheetModal;

/**
 * Miuix BottomSheet 弹层通用配置链（逐步替换现有 M3 BottomSheet 的公共基类）。
 *
 * <p>复刻 HyperCeiler CustomBottomSheetModel.init()：拖拽把手 + 全高展开 + 可拖拽，
 * 跳过半开/收起态。各试点弹层继承本类，setContentView 与 show 由调用方决定；
 * 底部内容布局统一采用 vendored SpringBackLayout（嵌套回弹 + NestedScrollingChild3
 * 向上转发给 Behavior 实现弹层跟手）。
 */
public class MiuixBottomSheetModal extends BottomSheetModal {

    /** 子类挂接的关闭动作（fan OnDismissListener 为单槽，由基类统一占用） */
    private Runnable mDismissAction;

    public MiuixBottomSheetModal(Activity activity) {
        super(activity);
        init();
    }

    public final void init() {
        setDragHandleViewEnabled(true);
        BottomSheetBehavior<FrameLayout> behavior = getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setForceFullHeight(true);
        behavior.setModeConfig(0);
        behavior.setDraggable(true);
        behavior.setSkipHalfExpanded(true);
        behavior.setSkipCollapsed(true);
        setOnDismissListener(() -> {
            if (mDismissAction != null) {
                mDismissAction.run();
            }
        });
    }

    /**
     * 子类挂接关闭回调（如深浅色重建恢复状态清理）。基类已占用 fan 的单槽
     * OnDismissListener 做转发，子类勿再直接调 setOnDismissListener。
     */
    public final void setDismissAction(Runnable action) {
        mDismissAction = action;
    }
}
