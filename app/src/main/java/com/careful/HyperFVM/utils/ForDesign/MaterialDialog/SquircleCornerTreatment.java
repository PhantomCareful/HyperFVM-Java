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
 * 1/4 圆弧在此处曲率从 0 突变为 1/r，视觉上有折感）；翼曲线末端切线与圆弧一致（G1），
 * 接缝藏在圆角内部。
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
     * 平滑度 s（0~1）：翼曲线占角的比例。0 = 纯 1/4 圆弧（无 G2 过渡）；越大过渡区越长、
     * 越接近 iOS squircle；1 = 圆弧消失两翼相接。弧半径与圆心不随 s 变化（同心保持），
     * s 仅决定直边被翼曲线"吃掉"的长度 (s·r)。0.4 为初值，装机目测后微调。
     */
    private static final float SMOOTHING = 0.4f;
    /** 3 顶点构造用的邻边长度（相对半径），须 ≥ (1+s)·r 保证 cut 空间检查全额通过 */
    private static final float SIDE_FACTOR = 10f;

    /** 非 90° 角时的回退实现 */
    private final RoundedCornerTreatment fallback = new RoundedCornerTreatment();

    /** 按半径缓存生成的角 cubics（3 条 × 8 float），避免每帧重建 RoundedPolygon */
    private float cachedRadius = Float.NaN;
    private float[] cachedCubics;

    @Override
    public void getCornerPath(@NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
        if (Math.abs(angle - 90f) > 0.01f || radius * interpolation <= 0f) {
            // 非直角（本构造不成立）或零半径：回退标准 1/4 圆弧
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
        float start = (1f + SMOOTHING) * r;
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
                        new CornerRounding(r, SMOOTHING),        // p1 目标角
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
                cachedRadius = r;
                cachedCubics = out;
                return out;
            }
        }
        return null; // 理论不可达：3 顶点 + 中点圆角必产出 3 条角曲线
    }
}
