/*
 * Copyright (C) 2011-2013 dshine.com
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com
 */
package com.doit.net.Constant;

import java.io.File;

import android.os.Environment;


/**
 * SD相关常量
 * 
 * @author Wiker Yong Email:<a href="mailto:wikeryong@gmail.com">wikeryong@gmail.com</a>
 * @date 2013-12-17 下午7:38:24
 * @version 1.0-SNAPSHOT
 */
public class SDConstant {
    public final static String USER_DATA_FILE = "u.dat";
    public final static String LOG_PATH = "/.android_log/log/";
    public final static String CACHE_PATH = "/.android_log/cache/";
    public final static long MAX_SPACE = 50 * 1024;// 检查最大剩余空间容量，50m
    
    
    public static String getSDPath() {
        File sdDir = null;
        String sdCardExist = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdCardExist)) // 判断sd卡是否挂载
        {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }
        return null;
    }
}
