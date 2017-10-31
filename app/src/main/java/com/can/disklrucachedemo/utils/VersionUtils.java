package com.can.disklrucachedemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;

/**
 * Created by HEKANG on 2017/3/23.
 */

public class VersionUtils {

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}
