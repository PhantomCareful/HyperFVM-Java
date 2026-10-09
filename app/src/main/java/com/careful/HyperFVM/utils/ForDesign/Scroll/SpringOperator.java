package com.careful.HyperFVM.utils.ForDesign.Scroll;

/**
 * 弹簧物理运算器（复刻 miuix springback 的 fan.springback.view.SpringOperator）。
 * <p>
 * 半隐式欧拉积分，每帧更新速度：
 *   v = (1 - damping * dt) * v + (target - current) * tension * dt
 * <p>
 * 参数换算（与原版字节码一致）：
 *   tension  = (2 * PI / period) ^ 2
 *   damping  = dampingRatio * 4 * PI / period
 * <p>
 * dampingRatio = 1.0 时为临界阻尼：回位平滑减速、无过冲。
 */
public class SpringOperator {

    private final double tension;
    private final double damping;

    /**
     * @param dampingRatio 阻尼比（原版固定传 1.0）
     * @param period       弹簧周期，越小回位越快（常规 0.55，快速甩动 0.4）
     */
    public SpringOperator(float dampingRatio, float period) {
        double omega = 2 * Math.PI / period;
        tension = omega * omega;
        damping = dampingRatio * 4 * Math.PI / period;
    }

    /**
     * @param velocity 当前速度（px/s）
     * @param dt       本帧时间步长（s，上限 0.016）
     * @param target   目标位置
     * @param current  当前位置
     * @return 更新后的速度
     */
    public double updateVelocity(double velocity, float dt, double target, double current) {
        double next = (1 - damping * dt) * velocity;
        // 与原版一致：弹簧项先截断为 float 再参与运算
        float springStep = (float) ((target - current) * tension * dt);
        return next + springStep;
    }
}
