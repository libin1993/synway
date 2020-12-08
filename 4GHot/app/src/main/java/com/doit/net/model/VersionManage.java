package com.doit.net.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.doit.net.ucsi.BuildConfig;

/**
 * Created by Zxc on 2019/8/6.
 */

public class VersionManage {
    public static boolean isPoliceVer(){
        return BuildConfig.FLAVOR.equals("police");
    }
    public static boolean isArmyVer(){
        return BuildConfig.FLAVOR.equals("army");
    }

    public static String getVersionName(Context context){
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }
}
