/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import static android.content.Context.WIFI_SERVICE;


/**
 * @author 杨维(wiker)
 * @version 1.0
 * @date 2016-4-26 下午2:53:35
 */
public class NetWorkUtils {

    /* 设置ip地址类型 assign：STATIC/DHCP 静态/动态 */
    private static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    @SuppressWarnings("unchecked")
    /* 设置ip地址 */
    private static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class<?> laClass = Class.forName("android.net.LinkAddress");
        Constructor<?> laConstructor = laClass.getConstructor(new Class[]{
                InetAddress.class, int.class
        });
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList<Object> mLinkAddresses = (ArrayList<Object>) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    @SuppressWarnings("unchecked")
    /* 设置网关 */
    private static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;

        if (android.os.Build.VERSION.SDK_INT >= 14) { // android4.x版本
            Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
            Constructor<?> routeInfoConstructor = routeInfoClass.getConstructor(new Class[]{
                    InetAddress.class
            });
            Object routeInfo = routeInfoConstructor.newInstance(gateway);

            ArrayList<Object> mRoutes = (ArrayList<Object>) getDeclaredField(linkProperties, "mRoutes");
            mRoutes.clear();
            mRoutes.add(routeInfo);
        } else { // android3.x版本
            ArrayList<InetAddress> mGateways = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mGateways");
            mGateways.clear();
            mGateways.add(gateway);
        }

    }

    private static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); // 清除原有DNS设置（如果只想增加，不想清除，词句可省略）mDnses.addRealtimeUeidList(dns);//增加新的DNS
    }

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static void changeWifiConfiguration(Context c,boolean dhcp, String ip, int prefix, String dns1, String gateway) throws Exception {
        WifiManager wm = (WifiManager) c.getSystemService(WIFI_SERVICE);
        if(!wm.isWifiEnabled()) {
            // wifi is disabled
            return;
        }
        // get the current wifi configuration
        WifiConfiguration wifiConf = null;
        WifiInfo connectionInfo = wm.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wm.getConfiguredNetworks();
        if(configuredNetworks != null) {
            for (WifiConfiguration conf : configuredNetworks){
                if (conf.networkId == connectionInfo.getNetworkId()){
                    wifiConf = conf;
                    break;
                }
            }
        }
        if(wifiConf == null) {
            // wifi is not connected
            return;
        }
        Class<?> ipAssignment = wifiConf.getClass().getMethod("getIpAssignment").invoke(wifiConf).getClass();
        Object staticConf = wifiConf.getClass().getMethod("getStaticIpConfiguration").invoke(wifiConf);
        if(dhcp) {
            wifiConf.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConf, Enum.valueOf((Class<Enum>) ipAssignment, "DHCP"));
            if(staticConf != null) {
                staticConf.getClass().getMethod("clear").invoke(staticConf);
            }
        } else {
            wifiConf.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConf, Enum.valueOf((Class<Enum>) ipAssignment, "STATIC"));
            if(staticConf == null) {
                Class<?> staticConfigClass = Class.forName("android.net.StaticIpConfiguration");
                staticConf = staticConfigClass.newInstance();
            }
            // STATIC IP AND MASK PREFIX
            Constructor<?> laConstructor = LinkAddress.class.getConstructor(InetAddress.class, int.class);
            LinkAddress linkAddress = (LinkAddress) laConstructor.newInstance(
                    InetAddress.getByName(ip),
                    prefix);
            staticConf.getClass().getField("ipAddress").set(staticConf, linkAddress);
            // GATEWAY
            staticConf.getClass().getField("gateway").set(staticConf, InetAddress.getByName(gateway));
            // DNS
            List<InetAddress> dnsServers = (List<InetAddress>) staticConf.getClass().getField("dnsServers").get(staticConf);
            dnsServers.clear();
            dnsServers.add(InetAddress.getByName(dns1));
//                dnsServers.addRealtimeUeidList(InetAddress.getByName("8.8.8.8")); // Google DNS as DNS2 for safety
            // apply the new static configuration
            wifiConf.getClass().getMethod("setStaticIpConfiguration", staticConf.getClass()).invoke(wifiConf, staticConf);
        }
        // apply the configuration change
        boolean result = wm.updateNetwork(wifiConf) != -1; //apply the setting
        if(result) result = wm.saveConfiguration(); //Save it
        if(result) wm.reassociate(); // reconnect with the new static IP
    }

    public static void setIpWithTfiStaticIp(Context context, String ip) throws Exception {

        WifiConfiguration wifiConf = null;

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                wifiConf = conf;
                break;
            }
        }

        changeWifiConfiguration(context,false,ip,24,"","10.10.10.100");


        /*
        if (android.os.Build.VERSION.SDK_INT < 11) { // 如果是android2.x版本的话

            ContentResolver ctRes = context.getContentResolver();
            Settings.System
                    .putInt(ctRes, Settings.System.WIFI_USE_STATIC_IP, 1);
            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_IP,
                    ip);
            Settings.System.putString(ctRes,
                    Settings.System.WIFI_STATIC_NETMASK, "255.255.255.0");
//            Settings.System.putString(ctRes,
//                    Settings.System.WIFI_STATIC_GATEWAY, "192.168.0.1");
//            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS1,
//                    "192.168.0.1");
//            Settings.System.putString(ctRes, Settings.System.WIFI_STATIC_DNS2,
//                    "61.134.1.9");

        } else { // 如果是android3.x版本及以上的话
            setIpAssignment("STATIC", wifiConfig);
            setIpAddress(InetAddress.getByName(ip), 24,
                    wifiConfig);
//                setGateway(InetAddress.getByName("192.168.0.1"), wifiConfig);
//                setDNS(InetAddress.getByName("192.168.0.1"), wifiConfig);
            wifiManager.updateNetwork(wifiConfig); // apply the setting
        }
*/


    }

    /***
     * 使用WIFI时，获取本机IP地址
     *
     * @param mContext
     * @return
     */
    public static String getWIFILocalIpAdress(Context mContext) {

        //获取wifi服务  
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        //判断wifi是否开启  
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        return ip;
    }

    private static String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    public static String getWifiSSID(Context context){
        String ssid = "unknow";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){  //android7 及以前使用此方法
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid =  wifiInfo.getSSID();
        }if (Build.VERSION.SDK_INT  == Build.VERSION_CODES.O || Build.VERSION.SDK_INT  == Build.VERSION_CODES.O_MR1) { //android8 使用此方法
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                ssid = info.getExtraInfo();
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){ //android9 及以上使用此方法
            WifiManager my_wifiManager = ((WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE));
            assert my_wifiManager != null;
            WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            int networkId = wifiInfo.getNetworkId();
            List<WifiConfiguration> configuredNetworks = my_wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration:configuredNetworks){
                if (wifiConfiguration.networkId==networkId){
                    ssid=wifiConfiguration.SSID;
                    break;
                }
            }
        }

        UtilBaseLog.printLog("get ssid:"+ssid);
        return ssid;
    }
}
