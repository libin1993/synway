package com.doit.net.bean;

/**
 * Created by Zxc on 2018/10/20.
 */

public class FtpConfig {
    private static String ftpServerIp;
    private static String ftpUser = "sanhui";
    private static String ftpPassword = "123456";
    private static String ftpPort = "2221";
    private static String ftpTimer = "15";   //imsi上传周期
    private static String ftpMaxSize = "10";  //imsi文件上传大小阈值

    public static String getFtpServerIp() {
        return ftpServerIp;
    }

    public static String getFtpUser() {
        return ftpUser;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    public static String getFtpPort() {
        return ftpPort;
    }

    public static String getFtpTimer() {
        return ftpTimer;
    }

    public static String getFtpMaxSize() {
        return ftpMaxSize;
    }

    public static void setFtpServerIp(String ip) {
        ftpServerIp = ip;
    }

    public static void setFtpUser(String user) {
        ftpUser = user;
    }

    public static void setFtpPassword(String password) {
        ftpPassword = password;
    }

    public static void setFtpPort(String port) {ftpPort = port;
    }

    public static void setFtpTimer(String timer) {
        ftpTimer = timer;
    }

    public static void setFtpMaxSize(String maxSize) {
        ftpMaxSize = maxSize;
    }
}
