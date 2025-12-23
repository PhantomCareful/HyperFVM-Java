package com.careful.HyperFVM.utils.OtherUtils;

import android.app.Activity;
import android.os.Build;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;

//工具类：适配MIUI/HyperOS导航栏沉浸
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

    //判断是否为Xiaomi/REDMI设备
    public static boolean isMIUIOrHyperOS() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        return manufacturer.contains("xiaomi") || manufacturer.contains("redmi");
    }
}
