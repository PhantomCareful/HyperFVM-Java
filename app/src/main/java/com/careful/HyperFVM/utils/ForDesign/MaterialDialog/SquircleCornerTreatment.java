package com.careful.HyperFVM.utils.ForDesign.MaterialDialog;

import androidx.annotation.NonNull;
import androidx.graphics.shapes.CornerRounding;
import androidx.graphics.shapes.Cubic;
import androidx.graphics.shapes.Feature;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.RoundedPolygonKt;

import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapePath;

import java.util.Arrays;
import java.util.List;

/**
 * G2（曲率连续）squircle 圆角，基于 AndroidX graphics-shapes 官方库
 * （androidx.graphics:graphics-shapes），替代手写双段三次贝塞尔拼接。
 * <p>
 * 每个 smoothing&gt;0 的角由 3 条三次贝塞尔组成：两侧翼曲线（flanking curve）+ 中间圆弧。
 * 翼曲线首控制点取"曲线起点与锚点末端连线的三等分点"，两端控制向量共线，使起点曲率
 * 精确为 0——与直边曲率相等，即"直边转入圆角处"达到精确 G2 连续（仅 G1 切向连续的
 * 1/4 圆弧在此处曲率从 0 突变为 1/r，视觉上有折感）。库原始翼曲线末端只保证与圆弧
 * 切向一致（G1），曲率却比弧大（s=0.4 时约 1.86/r vs 1/r，接缝处突降 46%），细看会
 * 在翼-弧衔接处看到细微凹陷；本类再对两条翼曲线做闭式 G2 修正（见
 * {@link #correctFlank0} / {@link #correctFlank2}），把接缝端曲率精确调到 1/r，
 * 使直边-翼-弧全程曲率连续。
 * <p>
 * 同心设计保持：圆弧圆心 = 角顶点沿平分线内缩 √(r²+cut²)，90° 角即 (r, r)，与
 * smoothing 无关——弧段圆心、半径和标准 1/4 圆角完全相同，只是两端多了一段平滑过渡，
 * 弹窗/BottomSheet 与屏幕物理圆角的同心换算（DialogCornerRadiusUtil）不受影响。
 * <p>
 * 局部坐标系（material CornerTreatment 约定）：原点为角外顶点，两边沿 x=0 与 y=0。
 * smoothing 使翼曲线起点从切点 (0,r) 外移到 (0,(1+s)·r)（直线边相应变短，仍在同一
 * 直线上，与 material 的角间直线段无缝衔接）。
 * <p>
 * 仅针对 90° 直角构造（弹窗/BottomSheet 均为直角）；其他角度或零半径回退
 * {@link RoundedCornerTreatment} 的 1/4 圆弧。
 * 通过 {@link com.google.android.material.shape.ShapeAppearanceModel.Builder}
 * 挂到 MaterialShapeDrawable 后，elevation 阴影沿用同一 path，轮廓自动同步。
 *
 * @see <a href="https://developer.android.com/jetpack/androidx/releases/graphics-shapes">graphics-shapes</a>
 */
public class SquircleCornerTreatment extends CornerTreatment {

    /**
     * 默认平滑度 s（0~1）：翼曲线占角的比例。0 = 纯 1/4 圆弧（无 G2 过渡）；越大过渡区
     * 越长、越接近 iOS squircle；1 = 圆弧消失两翼相接。弧半径与圆心不随 s 变化（同心
     * 保持），s 仅决定直边被翼曲线"吃掉"的长度 (s·r)。0.4 为初值，装机目测后微调。
     */
    public static final float DEFAULT_SMOOTHING = 0.4f;
    /** 3 顶点构造用的邻边长度（相对半径），须 ≥ (1+s)·r 保证 cut 空间检查全额通过 */
    private static final float SIDE_FACTOR = 10f;

    /**
     * 本实例的平滑度。小于等于 0 时退化为标准 1/4 圆弧（走 fallback）。
     * 小尺寸背景（如按钮）须按 min(w,h)/(2r)-1 自适应下调：翼曲线沿直边延伸到
     * (1+s)·r，相邻两角合计 2(1+s)·r 超过背景短边时曲线在直边上互相重叠，
     * 触发 material ShapePath 的 pathOverlapsCorner → UNION 规范化分支，
     * 在"直边转入圆角处"留下衔接折痕。
     */
    private final float smoothing;

    /** 非 90° 角时的回退实现 */
    private final RoundedCornerTreatment fallback = new RoundedCornerTreatment();

    /** 默认平滑度（{@link #DEFAULT_SMOOTHING}）实例，用于弹窗/BottomSheet 等大尺寸轮廓 */
    public SquircleCornerTreatment() {
        this(DEFAULT_SMOOTHING);
    }

    /** @param smoothing 平滑度 0~1，&lt;=0 时四角退化为标准 1/4 圆弧 */
    public SquircleCornerTreatment(float smoothing) {
        this.smoothing = Math.max(0f, Math.min(1f, smoothing));
    }

    /** 按半径缓存生成的角 cubics（3 条 × 8 float），避免每帧重建 RoundedPolygon */
    private float cachedRadius = Float.NaN;
    private float[] cachedCubics;

    @Override
    public void getCornerPath(@NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
        if (smoothing <= 0f || Math.abs(angle - 90f) > 0.01f || radius * interpolation <= 0f) {
            // s<=0（小背景自适应降为 0，翼退化会使 G2 修正除零）、非直角（本构造不
            // 成立）或零半径：回退标准 1/4 圆弧
            fallback.getCornerPath(shapePath, angle, interpolation, radius);
            return;
        }
        float r = radius * interpolation;
        float[] c = cornerCubics(r);
        if (c == null) {
            fallback.getCornerPath(shapePath, angle, interpolation, radius);
            return;
        }
        // 翼曲线起点在切点外侧 (1+s)·r（沿左边），起点角 180f 即 ShapePath.ANGLE_LEFT
        // （该常量为 protected，material 1.12 中值为 180）；路径画到 ((1+s)·r, 0)（上边）
        float start = (1f + smoothing) * r;
        shapePath.reset(0f, start, 180f, 180f - angle);
        // 3 条 cubic：左边翼曲线 → 中间圆弧 → 上边翼曲线（方向与角遍历方向一致）
        for (int i = 0; i < 24; i += 8) {
            shapePath.cubicToPoint(c[i + 2], c[i + 3], c[i + 4], c[i + 5], c[i + 6], c[i + 7]);
        }
    }

    /**
     * 生成单角（90°，顶点在原点、p0 沿 +y、p2 沿 +x）的 3 条角曲线 cubics。
     * 构造 3 顶点 RoundedPolygon：中间顶点挂 CornerRounding(r, s)，邻顶点不圆角，
     * 从 features 中取恰好 3 条 cubic 的非边特征（即翼-弧-翼）。
     */
    private float[] cornerCubics(float r) {
        if (cachedCubics != null && cachedRadius == r) {
            return cachedCubics;
        }
        float side = SIDE_FACTOR * r;
        RoundedPolygon poly = RoundedPolygonKt.RoundedPolygon(
                new float[]{0f, side, 0f, 0f, side, 0f},
                CornerRounding.Unrounded,
                Arrays.asList(
                        CornerRounding.Unrounded,               // p0 邻顶点：尖角
                        new CornerRounding(r, smoothing),        // p1 目标角
                        CornerRounding.Unrounded),               // p2 邻顶点：尖角
                0f, 0f);
        float[] out = new float[24];
        for (Feature feature : poly.getFeatures()) {
            List<Cubic> cubics = feature.getCubics();
            // 尖角特征为 1 条零长 cubic，圆角特征恰为翼-弧-翼 3 条
            if (!feature.isEdge() && cubics.size() == 3) {
                for (int i = 0; i < 3; i++) {
                    Cubic cubic = cubics.get(i);
                    int b = i * 8;
                    out[b] = cubic.getAnchor0X();
                    out[b + 1] = cubic.getAnchor0Y();
                    out[b + 2] = cubic.getControl0X();
                    out[b + 3] = cubic.getControl0Y();
                    out[b + 4] = cubic.getControl1X();
                    out[b + 5] = cubic.getControl1Y();
                    out[b + 6] = cubic.getAnchor1X();
                    out[b + 7] = cubic.getAnchor1Y();
                }
                // G2 修正：库的翼曲线只保证接缝切向一致（G1），末端曲率比弧大（s=0.4 约
                // 1.86/r vs 1/r），衔接处有细微凹陷；沿边线微调自由控制点，把接缝端曲率
                // 精确调到 1/r。只动控制点，三个锚点与圆弧段不变（同心设计保持）
                correctFlank0(out, r);   // 左边翼：接缝在末端，动 C0（out[2], out[3]）
                correctFlank2(out, r);   // 上边翼：接缝在起点，动 C1（out[20], out[21]）
                cachedRadius = r;
                cachedCubics = out;
                return out;
            }
        }
        return null; // 理论不可达：3 顶点 + 中点圆角必产出 3 条角曲线
    }

    /**
     * 闭式 G2 修正（flank0，接缝在 t=1）：C1 已由 G1 唯一确定（边线 ∩ 接缝处圆切线，
     * 库已算好），沿边线滑动自由控制点 C0 使末端曲率 k(1) = 1/r。B'(1)=3(P3-C1) 与 C0
     * 无关、B''(1)=6(C0-2C1+P3) 对 C0 线性，故 k(1)=1/r 是 C0 的线性方程，闭式可解；
     * C0 沿边线方向滑动不破坏起点共线性，直边侧 k=0（G2）保持。
     */
    private static void correctFlank0(float[] c, float r) {
        float c0x = c[2], c0y = c[3];
        float c1x = c[4], c1y = c[5], p3x = c[6], p3y = c[7];
        float bx = 3f * (p3x - c1x), by = 3f * (p3y - c1y);   // B'(1)，固定
        float ex = c0x - c1x, ey = c0y - c1y;                  // C0 的滑动方向（沿边线）
        // 取与库原始曲线相同的符号分支
        float crossLib = cross(bx, by, 6f * (c0x - 2f * c1x + p3x), 6f * (c0y - 2f * c1y + p3y));
        float cross0 = cross(bx, by, 6f * (p3x - c1x), 6f * (p3y - c1y));
        float slope = 6f * cross(bx, by, ex, ey);
        float speed3 = (float) Math.pow(bx * bx + by * by, 1.5);
        float target = Math.signum(crossLib) * speed3 / r;      // |B'(1)|³/r，带符号
        float v = (target - cross0) / slope;
        c[2] = c1x + v * ex;
        c[3] = c1y + v * ey;
    }

    /**
     * 闭式 G2 修正（flank2，接缝在 t=0，与 flank0 时间反转对称）：C0 已由 G1 固定，
     * 沿边线滑动自由控制点 C1 使起点曲率 k(0) = 1/r。B'(0)=3(C0-P0) 与 C1 无关、
     * B''(0)=6(P0-2C0+C1) 对 C1 线性，同样闭式可解；C1 沿边线滑动保持末端共线性。
     */
    private static void correctFlank2(float[] c, float r) {
        int b = 16; // flank2 在 24 float 数组中的起始偏移
        float p0x = c[b], p0y = c[b + 1], c0x = c[b + 2], c0y = c[b + 3];
        float c1x = c[b + 4], c1y = c[b + 5];
        float bx = 3f * (c0x - p0x), by = 3f * (c0y - p0y);   // B'(0)，固定
        float ex = c1x - c0x, ey = c1y - c0y;                  // C1 的滑动方向（沿边线）
        float crossLib = cross(bx, by, 6f * (p0x - 2f * c0x + c1x), 6f * (p0y - 2f * c0y + c1y));
        float cross0 = cross(bx, by, 6f * (p0x - c0x), 6f * (p0y - c0y));
        float slope = 6f * cross(bx, by, ex, ey);
        float speed3 = (float) Math.pow(bx * bx + by * by, 1.5);
        float target = Math.signum(crossLib) * speed3 / r;
        float w = (target - cross0) / slope;
        c[b + 4] = c0x + w * ex;
        c[b + 5] = c0y + w * ey;
    }

    private static float cross(float ax, float ay, float bx, float by) {
        return ax * by - ay * bx;
    }
}
