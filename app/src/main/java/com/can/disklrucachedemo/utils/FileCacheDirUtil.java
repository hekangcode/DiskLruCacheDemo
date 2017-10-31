package com.can.disklrucachedemo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author hekang
 */

public class FileCacheDirUtil {

    /**
     * 获取缓存路径
     */
    public static File getCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
