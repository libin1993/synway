package com.doit.net.Utils;

import android.os.Environment;

import java.util.Date;

/**
 * Created by Zxc on 2020/3/4.
 */

public class LicenceUtils {

    public static String authorizeCode = "";   //授权码，设备id_时间   DES对称加密
    public static String machineID = "";   //设备id
    public static String LOCAL_LICENCE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/4GHotspot/";
    public static String LICENCE_FILE_NAME = "licence.txt";



    public static String getDueTime() {

        String decryptCode = "";
        try {
            decryptCode = CipherUtils.decrypt(authorizeCode);
            LogUtils.log("到期日期："+decryptCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptCode.split("_")[1];
    }


    public static boolean checkAuthorizeCode(String authorizeCode) {
        //判断IMEI后8位
        String decryptCode = "";
        try {
            decryptCode = CipherUtils.decrypt(authorizeCode);
            LogUtils.log("解密：" + decryptCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!decryptCode.contains("_")) {
            return false;
        }

        if (!decryptCode.split("_")[0].equals(machineID)) {
            return false;
        }

        long newDueTime = DateUtils.convert2long(decryptCode.split("_")[1], DateUtils.LOCAL_DATE_DAY);

        return (newDueTime > System.currentTimeMillis());
    }

    /**
     * 生成30天证书
     */
    public static String createLicence() {

        long dueTime = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L;
        String dueTimeStr = DateUtils.convert2String(dueTime, DateUtils.LOCAL_DATE_DAY);

        String decryptCode = machineID + "_" + dueTimeStr;
        String encryptCode = "";
        try {
            encryptCode = CipherUtils.encrypt(decryptCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptCode;
    }

}
