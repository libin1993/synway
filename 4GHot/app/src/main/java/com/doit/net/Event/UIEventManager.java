package com.doit.net.Event;

import android.content.Context;

import com.doit.net.Model.CacheManager;
import com.doit.net.Model.PrefManage;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;
import com.doit.net.udp.g4.constants.G4MessageID;
import com.doit.net.udp.g4.handler.HandlerScan;
import com.doit.net.udp.g4.server.G4UDPServerManager;
import com.doit.net.udp.g4.service.G4ServiceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiker on 2016-08-15.
 */
public class UIEventManager {
    private static Map<String,List<IHandlerFinish>> callList = new HashMap<>();

    public final static String KEY_UEID_RPT = "KEY_UEID_RPT";
    public final static String KEY_REFRESH_REALTIME_UEID_LIST = "KEY_REFRESH_REALTIME_UEID_LIST";
    public final static String KEY_REFRESH_DEVICE = "KEY_REFRESH_DEVICE";
    public final static String KEY_SET_LOC_RESP = "KEY_SET_LOC_RESP";
    public final static String KEY_LOC_RPT = "KEY_LOC_RPT";
    public final static String KEY_HEARTBEAT_RPT = "KEY_HEARTBEAT_RPT";
    public final static String KEY_REFRESH_USER_LIST = "KEY_REFRESH_USER_LIST";
    public final static String KEY_RESEARCH_HISTORY_LIST = "KEY_RESEARCH_HISTORY_LIST";
    public final static String KEY_REFRESH_WHITE_LIST = "KEY_REFRESH_WHITE_LIST";
    public final static String KEY_REFRESH_NAMELIST_LIST = "KEY_REFRESH_NAMELIST_LIST";
    public final static String KEY_REFRESH_NAMELIST_RPT_LIST = "KEY_REFRESH_NAMELIST_RPT_LIST";
    public final static String SHOW_SET_PARAM_DIALOG = "SHOW_SET_PARAM_DIALOG";
    public final static String RPT_UPGRADE_STATUS = "RPT_UPGRADE_STATUS";


    public static void appInit(Context context){
//        G4UDPServerManager.setHexTrace(false);
//        HandlerScan.put(G4MessageID.MSG_GPS_RPT.toString(),new G4GpsRptHandler());
//        HandlerScan.put(G4MessageID.MSG_HEARTBEAT.toString(),new G4HeartBeatHandler());
//        HandlerScan.put(G4MessageID.MSG_LOCATE_REPORT_IND.toString(),new G4LocateRptHandler());
//        HandlerScan.put(G4MessageID.MSG_REBOOT_ACK.toString(),new G4SetRebootAckHandler());
//        HandlerScan.put(G4MessageID.MSG_THERMAL_RPT.toString(),new G4TemperatureRptHandler());
//        HandlerScan.put(G4MessageID.MSG_INIT.toString(),new G4WorkInitHandler());
//        HandlerScan.put(G4MessageID.MSG_GET_CELL_CONFIG_ACK.toString(),new G4GetCellCfgAckHandler());
//        HandlerScan.put(G4MessageID.MSG_SYS_RPT.toString(),new G4SysRptHandler());
//        HandlerScan.put(G4MessageID.MSG_GET_CHANNEL_CONFIG_ACK.toString(),new G4GetChannelCfgAckHandler());
//        HandlerScan.put(G4MessageID.MSG_SET_CHANNEL_CONFIG_ACK.toString(),new G4SetChannelCfgAckHandler());
//        HandlerScan.put(G4MessageID.MSG_SET_IP_ACK.toString(),new G4SetIpAckHandler());
//        HandlerScan.put(G4MessageID.MSG_GET_ENB_STATE_ACK.toString(),new G4GetEnbStateAckHandler());
//        HandlerScan.put(G4MessageID.MSG_CHG_TAC_ACK.toString(),new G4CommAckHandler());
//        HandlerScan.put(G4MessageID.MSG_GET_TAC_ACK.toString(),new G4GetTacAckHandler());
//        HandlerScan.put(G4MessageID.MSG_SET_CELL_CONFIG_ACK.toString(),new G4CommAckHandler());
//        HandlerScan.put(G4MessageID.MSG_UEID_RPT.toString(),new G4UeidRptHandler());
//
//        G4ServiceManager.addCallBack(G4MessageID.MSG_SET_FCN_ACK,new G4SetCommHandler());
//        G4ServiceManager.addCallBack(G4MessageID.MSG_SET_NCELL_ACK,new G4SetCommHandler());
//        G4ServiceManager.addCallBack(G4MessageID.MSG_SET_GPS_OFFSET_ACK,new G4SetCommHandler());
//        G4ServiceManager.addCallBack(G4MessageID.MSG_SET_PCI_ACK,new G4SetCommHandler());

        CacheManager.deviceInfo.setIp(PrefManage.getString("deviceIp", CacheManager.DEVICE_IP));
        readUserChannels(context);

    }


    public static void readUserChannels(Context context) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open("channels.txt"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            int cnt = 0;
            while ((line = bufReader.readLine()) != null){
                if(cnt == 0){
                    cnt ++;
                    continue;
                }

                String[] arr = line.split(",");
                G4MsgChannelCfg cfg = new G4MsgChannelCfg();
                cfg.setIdx(arr[0]);
                cfg.setPlmn(arr[1]);
//                cfg.setBand(NumberUtils.toInt(arr[2]));
                CacheManager.addUserChannel(cfg);

                cnt ++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 注册UI事件
     * @param key
     * @param callBack
     */
    public static void register(String key,IHandlerFinish callBack){
        if(callList.containsKey(key)){
            callList.get(key).add(callBack);
            return;
        }
        List<IHandlerFinish> list = new ArrayList<>();
        list.add(callBack);
        callList.put(key,list);
    }

    /**
     * 注销所有的相关的key的事件
     * @param key
     */
    public static void unRegisterAll(String key){
        if(callList.containsKey(key)) {
            callList.remove(key);
        }
    }

    /**
     * 注销指定的key的事件
     * @param key
     * @param callBack
     */
    public static void unRegister(String key,IHandlerFinish callBack){
        if(callList.containsKey(key)){
            for(int i=0;i<callList.get(key).size();i++){
                IHandlerFinish finish = callList.get(key).get(i);
                if(finish.getClass().getName().equals(callBack.getClass().getName())){
                    callList.get(key).remove(i);
                }
            }
        }
    }

    /**
     *  调用事件
     * @param key
     */
    public static void call(String key){
        if(callList.containsKey(key)){
            for(int i=0;i<callList.get(key).size();i++){
                IHandlerFinish finish = callList.get(key).get(i);
                if(finish != null){
                    try {
                        finish.handlerFinish(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
