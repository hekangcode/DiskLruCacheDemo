package com.can.disklrucachedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.can.disklrucachedemo.utils.FileCacheDirUtil;
import com.can.disklrucachedemo.utils.MD5Utils;
import com.can.disklrucachedemo.utils.StreamUtils;
import com.can.disklrucachedemo.utils.VersionUtils;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private final long CACHE_MAX_SIZE = 2 * 1024 * 1024;
    private final int CACHE_KEY_FILE_COUNT = 1;
    private final String CACHE_FILE_CATALOG = "bitmap";
    private final String URL = "http://b152.photo.store.qq.com/psb?/V11pttuB0O6JKX/CTIwAMNAt**2bB6Ox7FCVVTDc2Orz6ZlDcH" +
            "CctqNEZU!/b/YbeFolqEDQAAYnaEolrQCgAA&bo=ngL2AQAAAAABBEg!&rf=viewer_4";
    private ImageView iv;
    private DiskLruCache mDiskLruCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);
        initData();
    }

    private void initData() {
        initDiskLruCache();
        if (!getCacheBitmapSucceed(URL)) {
            Glide.with(this).load(URL)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                                glideAnimation) {
                            iv.setImageBitmap(resource);
                            Toast.makeText(MainActivity.this, "没有缓存图片", Toast.LENGTH_SHORT).show();
                            if (!setCacheStream(resource, URL)) {
                                Log.w("MainActivity", "write in cache failure");
                            }
                        }
                    });
        }
    }

    private void initDiskLruCache() {
        try {
            File cacheDir = FileCacheDirUtil.getCacheDir(this, CACHE_FILE_CATALOG);
            if (!cacheDir.exists()) {
                if (cacheDir.mkdirs()) {
                    mDiskLruCache = DiskLruCache.open(cacheDir, VersionUtils.getAppVersion(this),
                            CACHE_KEY_FILE_COUNT, CACHE_MAX_SIZE);
                }
            } else {
                mDiskLruCache = DiskLruCache.open(cacheDir, VersionUtils.getAppVersion(this),
                        CACHE_KEY_FILE_COUNT, CACHE_MAX_SIZE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getCacheBitmapSucceed(String url) {
        Bitmap cacheBitmap = getCacheBitmap(url);
        if (cacheBitmap != null) {
            iv.setImageBitmap(cacheBitmap);
            Toast.makeText(MainActivity.this, "有缓存图片", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    private boolean setCacheStream(Bitmap bitmap, String url) {
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(MD5Utils.setStringEncodeMD5(url));
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (StreamUtils.setBitmapToStream(bitmap, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                outputStream.close();
                // 刷新到日志文件里
                mDiskLruCache.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Bitmap getCacheBitmap(String url) {
        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(MD5Utils.setStringEncodeMD5(url));
            if (snapShot != null) {
                InputStream inputStream = snapShot.getInputStream(0);
                Bitmap cacheBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return cacheBitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
