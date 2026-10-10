package com.careful.HyperFVM.utils.OtherUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;

/**
 * 适配MIUI/HyperOS导航栏沉浸
 */
public class NavigationBarForMIUIAndHyperOS {

    public static void edgeToEdgeForMIUIAndHyperOS(Activity activity) {
        if (activity == null) {
            return;
        }

        Window window = activity.getWindow();
        try {
            //通过反射禁用导航栏对比度增强
            window.getClass().getMethod("setNavigationBarContrastEnforced", boolean.class).invoke(window, false);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception ignored) {
        }
    }

    /**
     * 判断系统是否为MIUI/HyperOS
     * <p>
     * 品牌判断覆盖标准Xiaomi/Redmi设备；另外通过特征系统属性识别移植ROM场景
     * （设备本身非Xiaomi/Redmi，但刷入的系统确实是MIUI/HyperOS，
     * 此时MANUFACTURER仍是原厂品牌，但ro.miui与ro.mi特征属性会被保留）
     */
    public static boolean isMIUIOrHyperOS() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
            return true;
        }
        // 移植ROM：任一MIUI/HyperOS特征属性存在即命中
        return !getSystemProperty("ro.miui.ui.version.name").isEmpty()   // MIUI（如V816）
                || !getSystemProperty("ro.mi.os.version.name").isEmpty() // HyperOS（如OS1.0.2.0）
                || !getSystemProperty("ro.mi.os.version.code").isEmpty();// HyperOS版本号
    }

    /**
     * 反射读取系统属性（android.os.SystemProperties为隐藏API），失败时返回空串
     */
    private static String getSystemProperty(String key) {
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Object value = clazz.getMethod("get", String.class).invoke(null, key);
            return value instanceof String ? (String) value : "";
        } catch (Exception ignored) {
            return "";
        }
    }
}
