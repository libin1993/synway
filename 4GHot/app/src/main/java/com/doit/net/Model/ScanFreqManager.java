package com.doit.net.Model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.doit.net.application.MyApplication;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LogUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by Zxc on 2019/4/11.
 */

public class ScanFreqManager {
    private static int MIN_DISTANCE_RE_SCAN_FREQ = 8000;  //找不到这个距离以内的地点就重新搜网获取频点列表
    private static DbManager dbManager = UCSIDBManager.getDbManager();

    private static double currentLongitude = -1;
    private static double currentLatitude = -1;
    private static ScanFreqInfo currentMinDisScanFreqInfo = null;   //成功获取到的、当前距离最近的搜网列表
    private static boolean hasSetScanFreqList = false;
    private static LocationManager locationManager;
    private static String providerName = LocationManager.GPS_PROVIDER;   //配套手机不带sim卡，所以只考虑GPS定位
    private static final int LOCATION_CODE = 1;

    public static void processLocation(double longitude, double latitude){
        int distance = 0;
        currentLongitude = longitude;
        currentLatitude = latitude;

        try {
            List<ScanFreqInfo> listScanFreqInfo= dbManager.selector(ScanFreqInfo.class)
                    .orderBy("id", true)
                    .findAll();
            distance = getBestScanFreqInfo(listScanFreqInfo, longitude, latitude);

        } catch (DbException e) {
            e.printStackTrace();
        }

        if (distance < 0 || distance > MIN_DISTANCE_RE_SCAN_FREQ) {
            //下发搜网
            startScanFreq();
        }else{
            LogUtils.log("数据库里找到最小距离为" + distance +"， 使用现有搜网频点列表");
            hasSetScanFreqList = true;
            setCurrentScanInfo(currentMinDisScanFreqInfo);
        }
    }

    private static void startScanFreq() {
        new Thread() {
            @Override
            public void run() {
                while(true){
                    if (CacheManager.isDeviceOk()){
                        ProtocolManager.scanFreq();
                        break;
                    }else{
                        try {sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
                    }

                }
            }
        }.start();
    }

    private static void setCurrentScanInfo(final ScanFreqInfo scanFreqInfo) {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (CacheManager.isDeviceOk()){
                        for(LteChannelCfg channel:CacheManager.getChannels()){
                            switch (channel.getBand()){
                                case "1":
                                    if (!channel.getAltFcn().equals(scanFreqInfo.getBand1FcnList()))
                                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                                "","", scanFreqInfo.getBand1FcnList());
                                    break;
                                case "3":
                                    if (!channel.getAltFcn().equals(scanFreqInfo.getBand3FcnList()))
                                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                                "","", scanFreqInfo.getBand3FcnList());
                                    break;
                                case "38":
                                    if (!channel.getAltFcn().equals(scanFreqInfo.getBand38FcnList()))
                                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                                "","", scanFreqInfo.getBand38FcnList());
                                    break;

                                case "39":
                                    if (!channel.getAltFcn().equals(scanFreqInfo.getBand39FcnList()))
                                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                                "","", scanFreqInfo.getBand39FcnList());
                                    break;

                                case "40":
                                    if (!channel.getAltFcn().equals(scanFreqInfo.getBand40FcnList()))
                                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                                "","", scanFreqInfo.getBand40FcnList());
                                    break;
                                default:
                            }
                        }

                        break;
                    }else{
                        try {sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
                    }
                }
            }
        }.start();
    }

    private static int getBestScanFreqInfo(List<ScanFreqInfo> listScanFreqInfo, double longitude, double latitude) {
        int minDistance = 5600000;  //中国距离最长的两个点距离0。0
        int tmpDistance = 0;

        if(listScanFreqInfo == null){
            return -1;
        }

        for (ScanFreqInfo scanFreqInfo : listScanFreqInfo) {
            tmpDistance = getDistance(scanFreqInfo.getLatitude(), scanFreqInfo.getLongitude(), latitude, longitude);
            if (tmpDistance < minDistance){
                minDistance = tmpDistance;
                currentMinDisScanFreqInfo = scanFreqInfo;
            }
        }

        return minDistance;
    }

    public static void saveAndSetScanFreqResult(final String band1FcnList, final String band3FcnList, final String band38FcnList,
                                                final String band39FcnList, final String band40FcnList){
        //理论上获取位置成功后才会搜网才会调用下发设置，但不保证后续要求变化
        if (currentLatitude < 0 || currentLongitude < 0 || hasSetScanFreqList){
            return;
        }

        try {
            dbManager.save(new ScanFreqInfo(currentLongitude, currentLatitude, band1FcnList, band3FcnList, band38FcnList, band39FcnList, band40FcnList));
        } catch (DbException e) {e.printStackTrace();}

        for(LteChannelCfg channel:CacheManager.getChannels()){
            switch (channel.getBand()){
                case "1":
                    if (!channel.getAltFcn().equals(band1FcnList))
                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                "","", band1FcnList);
                    break;
                case "3":
                    if (!channel.getAltFcn().equals(band3FcnList))
                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                "","", band3FcnList);
                    break;
                case "38":
                    if (!channel.getAltFcn().equals(band38FcnList))
                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                "","", band38FcnList);
                    break;

                case "39":
                    if (!channel.getAltFcn().equals(band39FcnList))
                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                "","", band39FcnList);
                    break;

                case "40":
                    if (!channel.getAltFcn().equals(band40FcnList))
                        ProtocolManager.setChannelConfig(channel.getIdx(),"","","","",
                                "","", band40FcnList);
                    break;
                default:
            }
        }

        hasSetScanFreqList = true;
        ToastUtils.showMessageLong(MyApplication.mContext, "已下发配置开机搜网频点列表并存储对应坐标！");
    }


    private static void successGetLocation(Location location) {
        if (location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            LogUtils.log("成功获取到位置信息："+longitude+","+latitude);
            //ToastUtils.showMessageLong(GameApplication.appContext, "成功获取到位置信息"+longitude+","+latitude);
            processLocation(longitude, latitude);
            //sb.append("定位精度：" + location.getAccuracy() + "\n");
            //sb.append("与临安的距离为：" + getDistance(location.getLongitude(), location.getLatitude(), 119.72, 30.23) + "米\n");
            //sb.append("定位方式：" + location.getProvider());
            locationManager.removeUpdates(locationListener);
        }
    }

    private static LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // 当GPS定位信息发生改变时，更新定位
            successGetLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    public static void startGetLocation(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
            LogUtils.log("没有定位权限");
        }

        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(providerName)) {
            ToastUtils.showMessage(activity, " GPS被关闭，为优化下次开机初始化时间，建议开启GPS！");
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);

    }


    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static int getDistance(double lat1, double lng1, double lat2, double lng2) {
        double EARTH_RADIUS = 6378.137;

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return (int) s;
    }
}
