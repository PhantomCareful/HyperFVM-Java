package com.careful.HyperFVM.Activities.Oobe;

/**
 * OOBE 窗口焦点转发接口（照 HC provision IOnFocusListener）。
 * Fragment 本身没有 onWindowFocusChanged 回调，由 OobeActivity 在
 * {@code onWindowFocusChanged} 中转发给当前 Fragment，保住首帧准时机
 * （入场动画/glow 启动需要窗口已获焦的真实尺寸与位置）。
 */
public interface IOnFocusListener {

    void onWindowFocusChanged(boolean hasFocus);
}
