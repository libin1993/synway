package com.doit.net.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.doit.net.Utils.DateUtil;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.Utils.UtilCipher;

import java.util.Date;

/**
 * Created by Zxc on 2020/3/4.
 */

public class LicenceManage {
    //在使用之前必须保证PrefManage已经初始化
    private static String authorizeCode = "";   //IMEI+日期的加密文
    private static String machineID = "";   //IMEI
    private static final String LICENCE_KEY = "Licence";

    @SuppressLint({"MissingPermission", "NewApi"})
    public static void generateAuthorizeInfo(Context context){
        authorizeCode =  PrefManage.getString(LICENCE_KEY, "");

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        machineID = tm.getImei(0);
    }

    public static String getMachineID(){
        return machineID;
    }

    public static String getAuthorizeCode(){
        return authorizeCode;
    }

    public static String getDueTime(){
        if ("".equals(authorizeCode)){
            return "";
        }

        String decryptCode = "";
        try {
            decryptCode = UtilCipher.decrypt(authorizeCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptCode.split("_")[1];
    }


    public static boolean checkAuthorizeCode(String authorizeCode){
        //判断IMEI后8位
        String decryptCode = "";
        try {
            decryptCode = UtilCipher.decrypt(authorizeCode);
            UtilBaseLog.printLog("解密："+decryptCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!decryptCode.contains("_")){
            return false;
        }

        if (!decryptCode.split("_")[0].equals(machineID.substring(machineID.length()/2, machineID.length()))){
            return false;
        }

        long newDueTime = DateUtil.convert2long(decryptCode.split("_")[1],DateUtil.LOCAL_DATE_DAY);

        if("".equals(LicenceManage.authorizeCode)){
            long nowTime = new Date().getTime();
            if (nowTime >= newDueTime){
                return false;
            }else{
                return true;
            }
        }

        long oldDueTime = DateUtil.convert2long(getDueTime(),DateUtil.LOCAL_DATE_DAY);
        return (newDueTime>oldDueTime);
    }

    public static boolean saveAuthorizeCode(String code){
        authorizeCode = code;
        PrefManage.setString(LICENCE_KEY, authorizeCode);
        return false;
    }

    public static void investDays(int days, Context context){
        long dueTime;
        if ("".equals(authorizeCode)){
            dueTime = new Date().getTime();
            new Date().getTime();
        }else{
            dueTime = DateUtil.convert2long(getDueTime(),DateUtil.LOCAL_DATE_DAY);
        }

        dueTime += (long)30*24*60*60*1000;
        String dueTimeStr = DateUtil.convert2String(dueTime,DateUtil.LOCAL_DATE_DAY);
        //Log.d("##############", DateUtil.convert2String(new Date().getTime(),DateUtil.LOCAL_DATE_DAY)+","+dueTimeStr);

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint({"MissingPermission", "NewApi", "LocalSuppress"})
        String imei = tm.getImei(0);
        //Log.d("############1", imei);
        String decryptCode =  imei.substring(imei.length()/2, imei.length())+"_"+dueTimeStr;
        String encryptCode = "";
        try {
            encryptCode = UtilCipher.encrypt(decryptCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveAuthorizeCode(encryptCode);
    }


}
