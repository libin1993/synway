package com.doit.net.bean;

/**
 * Created by Zxc on 2018/11/8.
 */

public class DeviceState {
    public static final String WIFI_DISCONNECT = "WIFI未连接";
    public static final String WAIT_SOCKET = "Socket未连接";
    public static final String ON_INIT = "初始化中...";  //依据心跳的同步字段判断
    //public static final String SYNC_ERROR = "失步";   //依据心跳的同步字段判断
    public static final String NORMAL = "正常";   //依据收到心跳

    private String deviceState = WIFI_DISCONNECT;

    public synchronized String getDeviceState(){
        return deviceState;
    }

    public synchronized void setDeviceState(String deviceState){
        this.deviceState = deviceState;

        //UIEventManager.call(UIEventManager.KEY_DEVICE_STATE_CHANGE);
    }
}
