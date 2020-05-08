package com.doit.net.Model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wiker on 2016/4/28.
 */
public class PrefManage {
    public static final String SHIELD_SWITCH_KEY = "SHIELD_SWITCH_KEY";
    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;
    public static void init(Context context){
        settings = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    public static String getIp(){
        return settings.getString("deviceIp","");
    }

    public static int getPort(){
        return settings.getInt("devicePort",0);
    }

    public static void setIp(String ip){
        editor.putString("deviceIp",ip);
        editor.commit();
    }
    public static void setPort(int port){
        editor.putInt("devicePort",port);
        editor.commit();
    }

    public static int play_type = 0;
    public static int getPlayType(){
        play_type = settings.getInt("PlayType",0);
        return play_type;
    }

    public static boolean supportPlay = false;

    public static void setPlayType(int t){
        if(1==1){
            //去掉百度
            return;
        }
        play_type = t;
        editor.putInt("PlayType",t);
        editor.commit();
    }

    public static String getImsi(){
        return settings.getString("IMSI","");
    }

    public static void setImsi(String ip){
        editor.putString("IMSI",ip);
        editor.commit();
    }

    public static void setString(String key,String val){
        editor.putString(key,val);
        editor.commit();
    }

    public static String getString(String key,String defVal){
        return settings.getString(key,defVal);
    }

    public static void setInt(String key,int val){
        editor.putInt(key,val);
        editor.commit();
    }

    public static int getInt(String key, int defVal){
        return settings.getInt(key,defVal);
    }

    public static void setBoolean(String key, boolean val){
        editor.putBoolean(key,val);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defVal){
        return settings.getBoolean(key,defVal);
    }

    public static Long getLong(String key,Long defVal){
        return settings.getLong(key,defVal);
    }

    public static void setLong(String key, long val){
        editor.putLong(key,val);
        editor.commit();
    }
}
