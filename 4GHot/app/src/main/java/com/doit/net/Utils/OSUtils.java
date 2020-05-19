package com.doit.net.Utils;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Author：Libin on 2020-05-19 10:18
 * Email：1993911441@qq.com
 * Describe：手机系统
 */
public class OSUtils {
    private static OSUtils mInstance;
    private static final String ROM_MIUI = "MIUI";
    private static final String ROM_FLYME = "FLYME";
    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";

    private static String sName;
    private static String sVersion;
    private OSUtils(){}

    public static OSUtils getInstance() {
        if (mInstance ==null){
            synchronized (OSUtils.class){
                if (mInstance == null){
                    mInstance = new OSUtils();
                }
            }
        }
        return mInstance;
    }

    public  boolean isMiui() {
        return check(ROM_MIUI);
    }

    public  boolean isFlyme() {
        return check(ROM_FLYME);
    }

    public  boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }
        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }

    public  String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}
