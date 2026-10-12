package com.careful.HyperFVM.utils.ForDesign.Blur;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;

import java.lang.reflect.Method;

/**
 * 关于页 logo 的 MIUI/HyperOS 系统级模糊混色工具（借鉴 HyperCeiler VersionCard#setLogoBlur）。
 *
 * <p>通过反射调用小米在 android.view.View 上扩展的隐藏 API：
 * <ul>
 *   <li>setMiViewBlurMode / setBlurMode —— 设置 View 的模糊模式（3 = cross，区域内透出下层模糊）</li>
 *   <li>setMiBackgroundBlurRadius / setBackgroundBlur —— 声明容器的背景模糊半径</li>
 *   <li>addMiBackgroundBlendColor / addBackgroundBlenderColor —— 叠加一层混色</li>
 *   <li>clearMiBackgroundBlendColor / clearBackgroundBlendConfig —— 清除全部混色</li>
 * </ul>
 * HyperOS 使用带 {@code setMi*} 前缀的新名，MIUI 旧版本为无前缀旧名，探测到哪套就用哪套。
 *
 * <p>降级策略：非小米机型、系统"模糊效果"开关关闭、隐藏 API 签名变化（反射失败）时
 * 一律静默跳过，界面回退为普通 logo 显示，不影响任何功能，也不会崩溃。
 */
public final class MiuiLogoBlurUtil {

    private static final String TAG = "MiuiLogoBlurUtil";

    /** cross 模式：logo 区域内透出下层内容的模糊层（对应 HyperCeiler enableTextBlur 的 3） */
    private static final int BLUR_MODE_CROSS = 3;

    /** logo 下层容器的背景模糊半径（dp，借鉴 HyperCeiler VersionCard 的 50dp） */
    private static final int ROOT_BLUR_RADIUS_DP = 50;

    /** 背景模糊模式总闸（fan setBackgroundBlur 内部固定设 1，缺它则半径/混色全部不渲染） */
    private static final int BACKGROUND_BLUR_MODE_ON = 1;

    /** 基础混色层的 blend mode：夜间 18 / 日间 19（MIUI 混合层枚举，见 HyperCeiler mModeValue） */
    private static final int BLEND_MODE_BASE_NIGHT = 18;
    private static final int BLEND_MODE_BASE_DAY = 19;

    /** 第 2、3 层混色的 blend mode（MIUI 固定枚举值，随 HyperCeiler 保持一致） */
    private static final int BLEND_MODE_SECOND = 100;
    private static final int BLEND_MODE_THIRD = 106;

    /** 系统实时模糊总开关的 Settings key（尊重用户在系统设置中关闭模糊） */
    private static final String KEY_BACKGROUND_BLUR_ENABLE = "background_blur_enable";

    /**
     * blend 保持系数：OS1.0（Redmi K70 实测）alpha=1 时系统跳过 blend 渲染（表现为纯色），
     * alpha<1（滚动渐隐一开始）blend 就完整出现；而显式 hardware layer 反而彻底屏蔽 blend
     * （内容进独立 buffer 取样不到下层背景）。故将 alpha 钳在 0.999 使 view 恒处于
     * 半透明渲染路径，blend 持续生效，视觉与 1.0 无感知差异
     */
    public static final float BLEND_ALPHA_CLAMP = 0.999f;

    // ---- 反射句柄：解析结果进程内缓存，四个句柄齐备才算可用 ----
    private static volatile boolean sResolved;
    private static boolean sAvailable;
    private static Method sSetViewBlurMode;
    private static Method sSetBackgroundBlurMode;
    private static Method sSetContainerBlurRadius;
    private static Method sAddBlendColor;
    private static Method sClearBlendColor;

    private MiuiLogoBlurUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 是否可应用该效果：小米机型 + 系统模糊开关开启 + 隐藏 API 可反射，三者缺一返回 false。
     */
    public static boolean isSupported(Context context) {
        if (context == null) return false;
        boolean miuiOrHyperOs = NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS();
        boolean apiAvailable = resolveMethods();
        int secureValue = -1;
        int globalValue = -1;
        try {
            // 实测 HyperOS 4 的开关在 Settings.Secure，旧 MIUI/部分版本在 Settings.Global，两处读取回退
            ContentResolver resolver = context.getContentResolver();
            secureValue = Settings.Secure.getInt(resolver, KEY_BACKGROUND_BLUR_ENABLE, -1);
            globalValue = Settings.Global.getInt(resolver, KEY_BACKGROUND_BLUR_ENABLE, -1);
        } catch (Exception e) {
            Log.d(TAG, "read blur switch failed: " + e);
        }
        boolean supported = miuiOrHyperOs && apiAvailable && (secureValue == 1 || globalValue == 1);
        Log.d(TAG, "check: miui=" + miuiOrHyperOs + " api=" + apiAvailable
                + " secure=" + secureValue + " global=" + globalValue + " => " + supported);
        return supported;
    }

    /**
     * 应用整套 logo 模糊混色效果（照 HyperCeiler VersionCard#setLogoBlur 的顺序）：
     * <ol>
     *   <li>容器声明 50dp 背景模糊并设为 mode 0（模糊源）；</li>
     *   <li>每个 logo 设为 cross 模式 3，透出下方流光背景的模糊；</li>
     *   <li>叠加三层混色：基础层随日夜切换（18/19），另两层为 100、106。</li>
     * </ol>
     *
     * @param context   用于读取混色资源与日夜状态
     * @param container logo 的下层容器（对应 HyperCeiler 的 mRootView，这里传 BlurTarget）
     * @param logoViews 需要效果的 logo 元素（图标、应用名），允许含 null 自动跳过
     */
    public static void applyLogoEffect(Context context, View container, View... logoViews) {
        if (container == null || logoViews.length == 0 || !isSupported(context)) {
            return;
        }
        try {
            // 1. 容器作为模糊源（对齐 fan MiuiBlurUtils.setBackgroundBlur 的三步：
            //    先开 background 模式总闸=1，再设半径，最后 VersionCard 单独把 view 模式置 0）
            int radiusPx = (int) (container.getResources().getDisplayMetrics().density * ROOT_BLUR_RADIUS_DP + 0.5f);
            invoke(sSetBackgroundBlurMode, container, 1);
            invoke(sSetContainerBlurRadius, container, radiusPx);
            invoke(sSetViewBlurMode, container, 0);

            // 2. 三层混色（颜色来自 values/values-night 的日夜两套资源）
            int[] colors = {
                    context.getColor(R.color.about_app_logo_blur_color1),
                    context.getColor(R.color.about_app_logo_blur_color2),
                    context.getColor(R.color.about_app_logo_blur_color3),
            };
            int[] modes = {
                    isNightMode(context) ? BLEND_MODE_BASE_NIGHT : BLEND_MODE_BASE_DAY,
                    BLEND_MODE_SECOND,
                    BLEND_MODE_THIRD,
            };

            // 3. 逐个 logo：cross 模式 + 清旧混色 + 叠三层
            int applied = 0;
            for (View logo : logoViews) {
                if (logo == null) continue;
                // 与 fan enableTextBlur 完全一致：view 模式 3 + 三层混色（不设 background 模式、不选容器）
                invoke(sSetViewBlurMode, logo, BLUR_MODE_CROSS);
                invoke(sClearBlendColor, logo);
                for (int i = 0; i < colors.length; i++) {
                    invoke(sAddBlendColor, logo, colors[i], modes[i]);
                }
                applied++;
            }
            Log.d(TAG, "effect applied to " + applied + " views, baseMode=" + modes[0]);
        } catch (Exception e) {
            Log.d(TAG, "applyLogoEffect failed, fallback to plain logo: " + e);
        }
    }

    /**
     * 为增幅名单各页的卡片名 TextView 批量应用与 logo 相同的 blend 效果：
     * 递归查找 id 形如 {@code card_data_index_background_image_*} 的单卡列容器（复数
     * {@code images_} 为行容器，前缀不匹配自动排除），以列容器为模糊源、其内 TextView 为
     * blend 目标逐个套用三层混色，并按 {@link #BLEND_ALPHA_CLAMP} 钳制 alpha
     * （这些名称不参与滚动渐隐，钳一次即可）。
     * 不满足支持条件时整体静默跳过。
     */
    public static void applyCardNameEffect(Context context, View rootView) {
        if (rootView == null || !isSupported(context)) {
            return;
        }
        applyCardNameEffectToTree(context, rootView);
    }

    /** 遍历视图树，对匹配的单卡列容器套用 blend（列容器内不再嵌套列容器，继续下探无副作用） */
    private static void applyCardNameEffectToTree(Context context, View view) {
        if (!(view instanceof ViewGroup group)) return;

        if (isCardNameColumn(context, view)) {
            TextView nameText = findDirectChildTextView(group);
            if (nameText != null) {
                applyLogoEffect(context, group, nameText);
                if (nameText.getAlpha() > BLEND_ALPHA_CLAMP) {
                    nameText.setAlpha(BLEND_ALPHA_CLAMP);
                }
            }
        }

        for (int i = 0; i < group.getChildCount(); i++) {
            applyCardNameEffectToTree(context, group.getChildAt(i));
        }
    }

    /** 是否为单卡列容器（id 以 card_data_index_background_image_ 开头，行容器 images_ 不匹配） */
    private static boolean isCardNameColumn(Context context, View view) {
        if (view.getId() == View.NO_ID) return false;
        try {
            return context.getResources().getResourceEntryName(view.getId())
                    .startsWith("card_data_index_background_image_");
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }

    /** 取列容器的直接子 TextView（结构固定为 ImageView + TextView），没有则返回 null */
    private static TextView findDirectChildTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof TextView) {
                return (TextView) group.getChildAt(i);
            }
        }
        return null;
    }

    /**
     * 反射解析隐藏 API：HyperOS 新名与 MIUI 旧名各探测一次，命中哪套用哪套；
     * setViewBlurMode / 容器半径 / 加混色 / 清混色 四件齐备才判定可用。
     */
    private static boolean resolveMethods() {
        if (sResolved) return sAvailable;
        synchronized (MiuiLogoBlurUtil.class) {
            if (sResolved) return sAvailable;

            sSetViewBlurMode = find("setMiViewBlurMode", int.class);
            if (sSetViewBlurMode == null) sSetViewBlurMode = find("setBlurMode", int.class);

            sSetContainerBlurRadius = find("setMiBackgroundBlurRadius", int.class);
            if (sSetContainerBlurRadius == null) sSetContainerBlurRadius = find("setBackgroundBlur", int.class);

            sAddBlendColor = find("addMiBackgroundBlendColor", int.class, int.class);
            if (sAddBlendColor == null) sAddBlendColor = find("addBackgroundBlenderColor", int.class, int.class);

            sClearBlendColor = find("clearMiBackgroundBlendColor");
            if (sClearBlendColor == null) sClearBlendColor = find("clearBackgroundBlendConfig");

            // 可选调用：fan 的 setBackgroundBlur 内部第一步就是 setMiBackgroundBlurMode(1)
            sSetBackgroundBlurMode = find("setMiBackgroundBlurMode", int.class);

            sAvailable = sSetViewBlurMode != null && sSetContainerBlurRadius != null
                    && sAddBlendColor != null && sClearBlendColor != null;
            sResolved = true;
            Log.d(TAG, "methods: viewBlur=" + name(sSetViewBlurMode)
                    + " bgBlurMode=" + name(sSetBackgroundBlurMode)
                    + " radius=" + name(sSetContainerBlurRadius)
                    + " blend=" + name(sAddBlendColor)
                    + " clear=" + name(sClearBlendColor));
            return sAvailable;
        }
    }

    /** 方法名或 null，用于诊断日志 */
    private static String name(Method method) {
        return method == null ? "null" : method.getName();
    }

    /** 在 View 类上查找隐藏方法并放开访问，找不到返回 null（不抛异常） */
    private static Method find(String name, Class<?>... paramTypes) {
        try {
            Method method = View.class.getDeclaredMethod(name, paramTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    /** 反射调用，任何异常只记日志不上抛，保证效果失败不影响界面 */
    private static void invoke(Method method, Object receiver, Object... args) {
        if (method == null || receiver == null) return;
        try {
            method.invoke(receiver, args);
        } catch (Exception e) {
            Log.d(TAG, "invoke " + method.getName() + " failed: " + e);
        }
    }

    /** 当前是否处于夜间模式（决定基础混色层用 18 还是 19） */
    private static boolean isNightMode(Context context) {
        int nightMask = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMask == Configuration.UI_MODE_NIGHT_YES;
    }
}
