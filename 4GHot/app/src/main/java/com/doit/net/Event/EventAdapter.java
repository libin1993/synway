package com.doit.net.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wiker on 2016/5/5.
 */
public class EventAdapter {
    private static Map<String,EventCall> event = new HashMap();

    public final static String WIFI_CHANGE = "WIFI_CHANGE";   //wifi状态监控
    public final static String FOUND_BLACK_NAME = "FOUND_BLACK_NAME";
    public final static String BLACK_NAME_RPT = "BLACK_NAME_RPT";
    public final static String LOCATION_RPT = "LOCATION_RPT";
    public final static String SHIELD_RPT = "SHIELD_RPT";  //军队app，侦码
    public final static String UEID_RPT = "UEID_RPT";    //警察app，管控
    public final static String UPDATE_FILE_SYS = "UPDATE_FILE_SYS";
    public final static String SHOW_PROGRESS = "SHOW_PROGRESS";
    public final static String CLOSE_PROGRESS = "CLOSE_PROGRESS";
    public final static String REALTIME_NAMELIST_RPT = "REALTIME_NAMELIST_RPT";
    public final static String CLEAR_REALTIME_NAMELIST_RPT = "CLEAR_REALTIME_NAMELIST_RPT";
    public final static String SPEAK = "SPEAK";
    public final static String UPDATE_BATTERY = "UPDATE_BATTERY";
    public final static String BATTERY_STATE = "BATTERY_STATE";  //电量
    public final static String UPDATE_TMEPRATURE = "UPDATE_TMEPRATURE";
    public final static String RF_ALL_CLOSE = "RF_ALL_CLOSE";
    public final static String ADD_LOCATION = "ADD_LOCATION";
    public final static String SCAN_FREQ_RPT = "SCAN_FREQ_RPT";
    public final static String STOP_LOC = "STOP_LOC";
    public final static String ADD_BLACKBOX = "ADD_BLACKBOX";
    public final static String CHANGE_TAB = "CHANGE_TAB";
    public final static String UPDATE_WHITELIST = "UPDATE_WHITELIST";
    public final static String POWER_START = "POWER_START";
    public final static String SCAN_CODE = "SCAN_CODE"; //扫码结果
    public final static String GET_NAME_LIST = "GET_NAME_LIST"; //获取白名单
    public final static String REFRESH_DEVICE = "REFRESH_DEVICE";  //通道设置
    public final static String RF_STATUS_RPT = "RF_STATUS_RPT";  //射频状态,是否停止侦码
    public final static String RF_STATUS_LOC = "RF_STATUS_LOC";  //射频状态,是否关闭定位
    public final static String HEARTBEAT_RPT = "HEARTBEAT_RPT"; //设备心跳
    public final static String REFRESH_USER_LIST ="REFRESH_USER_LIST"; //用户列表
    public final static String RESEARCH_HISTORY_LIST ="RESEARCH_HISTORY_LIST"; //历史记录
    public final static String REFRESH_WHITELIST = "REFRESH_WHITELIST";  //白名单列表
    public final static String REFRESH_BLACKLIST = "REFRESH_BLACKLIST";  //黑名单列表
    public final static String REFRESH_NAME_LIST_RPT = "REFRESH_NAME_LIST_RPT";
    public final static String UPGRADE_STATUS = "UPGRADE_STATUS";  //升级结果

    public static void register(String key, EventCall call) {
        event.put(key,call);
    }

    public static void call(String key){
        call(key,null);
    }

    public static void call(String key,Object val){
        EventCall e = event.get(key);
        if(e == null){
            return;
        }
        e.call(key,val);
    }

    public interface EventCall{
        void call(String key,Object val);
    }
}
