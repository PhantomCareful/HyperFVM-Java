package com.careful.HyperFVM.utils.ForUpdate;

import android.content.Context;
import android.content.pm.PackageManager;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

/**
 * 本地版本号工具类
 */
public class LocalVersionUtil {
    /**
     * 获取App的versionCode
     */
    public static long getAppLocalVersionCode(Context context) {
        long localVersionCode = 0;
        try {
            localVersionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .getLongVersionCode();
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return localVersionCode;
    }

    /**
     * 获取App的versionName
     */
    public static String getAppLocalVersionName(Context context) {
        String localVersionName = "0.0.0";
        try {
            localVersionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return  localVersionName;
    }

    /**
     * 获取图片资源的本地版本号
     */
    public static long getImageResourcesVersionCode(Context context) {
        String localImageResourcesVersionStr;
        long localImageResourcesVersionCode = 0;
        try (DBHelper dbHelper = new DBHelper(context)) {
            localImageResourcesVersionStr = dbHelper.getDataStationValue("DataImageResourcesVersionCode");
        }

        if (localImageResourcesVersionStr != null) {
            localImageResourcesVersionCode = Long.parseLong(localImageResourcesVersionStr);
        }

        return localImageResourcesVersionCode;
    }
}
