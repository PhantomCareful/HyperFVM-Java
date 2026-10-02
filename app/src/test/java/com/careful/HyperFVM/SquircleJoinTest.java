package com.careful.HyperFVM;

import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.SquircleCornerTreatment;

import org.junit.Test;

import java.lang.reflect.Method;

/**
 * 数值体检：G2 角曲线三个接缝（直边-翼 / 弧 / 翼-直边）的切线方向与曲率连续性，
 * 以及 getCornerPath 的 reset 起点与库生成 cubics 首锚点是否一致
 * （cubicToPoint 会丢弃 c[0..1]，不一致 = 真实几何错位）。
 */
public class SquircleJoinTest {

    private static final float SMOOTHING = 0.4f;

    /** 端点曲率 k = cross(B',B'')/|B'|^3（解析，非数值差分） */
    private static float curvature(float[] c, int b, boolean atStart) {
        float p0x = c[b], p0y = c[b + 1];
        float c0x = c[b + 2], c0y = c[b + 3];
        float c1x = c[b + 4], c1y = c[b + 5];
        float p3x = c[b + 6], p3y = c[b + 7];
        float bx, by, ddx, ddy;
        if (atStart) {
            bx = 3f * (c0x - p0x);
            by = 3f * (c0y - p0y);
            ddx = 6f * (p0x - 2f * c0x + c1x);
            ddy = 6f * (p0y - 2f * c0y + c1y);
        } else {
            bx = 3f * (p3x - c1x);
            by = 3f * (p3y - c1y);
            ddx = 6f * (c0x - 2f * c1x + p3x);
            ddy = 6f * (c0y - 2f * c1y + p3y);
        }
        float cross = bx * ddy - by * ddx;
        float speed3 = (float) Math.pow(bx * bx + by * by, 1.5);
        if (speed3 < 1e-12f) return Float.NaN;
        return cross / speed3;
    }

    private static float[] tangent(float[] c, int b, boolean atStart) {
        float p0x = c[b], p0y = c[b + 1];
        float c0x = c[b + 2], c0y = c[b + 3];
        float c1x = c[b + 4], c1y = c[b + 5];
        float p3x = c[b + 6], p3y = c[b + 7];
        float tx, ty;
        if (atStart) { tx = c0x - p0x; ty = c0y - p0y; }
        else { tx = p3x - c1x; ty = p3y - c1y; }
        float len = (float) Math.hypot(tx, ty);
        return new float[]{tx / len, ty / len};
    }

    private static float degBetween(float[] a, float[] b) {
        double dot = a[0] * b[0] + a[1] * b[1];
        double cross = a[0] * b[1] - a[1] * b[0];
        return (float) Math.toDegrees(Math.atan2(Math.abs(cross), dot));
    }

    @Test
    public void dumpAndCheckSeams() throws Exception {
        SquircleCornerTreatment t = new SquircleCornerTreatment();
        Method m = SquircleCornerTreatment.class.getDeclaredMethod("cornerCubics", float.class);
        m.setAccessible(true);

        float r = 40f;
        float expectStart = (1f + SMOOTHING) * r;
        float[] c = (float[]) m.invoke(t, r);
        if (c == null) {
            System.out.println("cornerCubics -> null (fallback path!)");
            return;
        }

        System.out.println("=== r=" + r + " smoothing=" + SMOOTHING + " ===");
        for (int i = 0; i < 24; i += 8) {
            System.out.printf("cubic%d: P0=(%.4f,%.4f) C0=(%.4f,%.4f) C1=(%.4f,%.4f) P3=(%.4f,%.4f)%n",
                    i / 8, c[i], c[i + 1], c[i + 2], c[i + 3], c[i + 4], c[i + 5], c[i + 6], c[i + 7]);
        }

        System.out.println("--- 起点一致性（reset 起点 vs 库首锚点）---");
        System.out.printf("reset 起点 = (0, %.4f), 库 P0 = (%.4f, %.4f), 偏差 = %.6f%n",
                expectStart, c[0], c[1], Math.hypot(c[0] - 0f, c[1] - expectStart));
        System.out.printf("终锚点 P3 = (%.4f, %.4f), 期望 (%.4f, 0), 偏差 = %.6f%n",
                c[22], c[23], expectStart, Math.hypot(c[22] - expectStart, c[23]));

        System.out.println("--- 锚点连续性（cubic 间是否精确相接）---");
        System.out.printf("c0.P3 vs c1.P0: %.8f%n", Math.hypot(c[6] - c[8], c[7] - c[9]));
        System.out.printf("c1.P3 vs c2.P0: %.8f%n", Math.hypot(c[14] - c[16], c[15] - c[17]));

        System.out.println("--- 接缝切线（相邻段夹角，0 = G1 连续）---");
        // 直边(竖直,方向指向角: 单位向量 (0,-1) 或 (0,+1) 取绝对夹角) -> flank0 t=0
        float[] t0 = tangent(c, 0, true);
        System.out.printf("J0 直边|flank0: 切线=(%.4f,%.4f) 与竖直夹角=%.4f°%n", t0[0], t0[1],
                degBetween(t0, new float[]{0f, 1f}));
        // flank0 t=1 vs arc t=0
        float[] f0e = tangent(c, 0, false);
        float[] a0s = tangent(c, 8, true);
        System.out.printf("J1 flank0|arc  : 夹角=%.6f°%n", degBetween(f0e, a0s));
        // arc t=1 vs flank2 t=0
        float[] a1e = tangent(c, 8, false);
        float[] f2s = tangent(c, 16, true);
        System.out.printf("J2 arc|flank2  : 夹角=%.6f°%n", degBetween(a1e, f2s));
        // flank2 t=1 vs 直边(水平)
        float[] f2e = tangent(c, 16, false);
        System.out.printf("J3 flank2|直边 : 切线=(%.4f,%.4f) 与水平夹角=%.4f°%n",
                f2e[0], f2e[1], degBetween(f2e, new float[]{1f, 0f}));

        System.out.println("--- 接缝曲率（期望: 直边侧 0, 弧侧 1/r=" + (1f / r) + "）---");
        System.out.printf("J0 flank0 t=0 : k=%.6f (期望 0)%n", curvature(c, 0, true));
        System.out.printf("J1 flank0 t=1 : k=%.6f (期望 %.6f)%n", curvature(c, 0, false), 1f / r);
        System.out.printf("J1 arc    t=0 : k=%.6f%n", curvature(c, 8, true));
        System.out.printf("J2 arc    t=1 : k=%.6f%n", curvature(c, 8, false));
        System.out.printf("J2 flank2 t=0 : k=%.6f (期望 %.6f)%n", curvature(c, 16, true), 1f / r);
        System.out.printf("J3 flank2 t=1 : k=%.6f (期望 0)%n", curvature(c, 16, false));
    }
}
