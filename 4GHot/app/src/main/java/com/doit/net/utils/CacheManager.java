/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.doit.net.application.MyApplication;
import com.doit.net.bean.DeviceState;
import com.doit.net.bean.LocationBean;
import com.doit.net.bean.LocationRptBean;
import com.doit.net.bean.LteCellConfig;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.LteEquipConfig;
import com.doit.net.bean.ScanFreqRstBean;
import com.doit.net.bean.UeidBean;
import com.doit.net.bean.DBBlackInfo;
import com.doit.net.bean.DBChannel;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;
import com.doit.net.view.MySweetAlertDialog;

import org.apache.commons.lang3.math.NumberUtils;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author 杨维(wiker)
 * @version 1.0
 * @date 2016-4-26 下午3:37:39
 */
public class CacheManager {
    public static String DEVICE_IP; //设备IP
    public static final String DEFAULT_DEVICE_IP = "192.168.4.100";
    public static List<UeidBean> realtimeUeidList = new ArrayList<>();
    public static final int MAX_REALTIME_LIST_SIZE = 300;


    public static List<LocationBean> locations = new ArrayList<>();
    public static LocationBean currentLocation = null;
    public static List<LocationRptBean> locationRpts = new ArrayList<>();

    public static boolean isReportBattery = false;  //是否上报电量

    public static DeviceState deviceState = new DeviceState();

    public static List<ScanFreqRstBean> listLastScanFreqRst = new ArrayList<>();

    public static boolean loc_mode = true;  //是否开启搜寻功能

    public  static boolean hasSetDefaultParam = false;   //开始全部打开射频标志

    private static boolean hasPressStartButton = false;  //是否已经在主页面点击开始按钮

    public static boolean checkLicense = false; //连接成功后校验证书

    public static boolean getLocMode() {
        return loc_mode;
    }

    private static LteCellConfig cellConfig;
    private static LteEquipConfig equipConfig;
    public static List<LteChannelCfg> channels = new ArrayList<>();



    public static void setLocMode(boolean locMode) {
        loc_mode = locMode;
    }


    public synchronized static void addRealtimeUeidList(List<UeidBean> listUeid) {
        if ((realtimeUeidList.size() + listUeid.size()) >= MAX_REALTIME_LIST_SIZE) {
            for (int i = 0; i < (realtimeUeidList.size() + listUeid.size() - MAX_REALTIME_LIST_SIZE); i++)
                realtimeUeidList.remove(0);
        }

        //最新的放前面
        Collections.reverse(listUeid);
        realtimeUeidList.addAll(0, listUeid);
    }


    public static boolean hasPressStartButton() {
        return hasPressStartButton;
    }

    public static void setPressStartButtonFlag(boolean flag) {
        hasPressStartButton = flag;
    }

    public static void updateLoc(String imsi) {
        if (currentLocation == null) {
            currentLocation = new LocationBean();
        }
        SPUtils.setImsi(imsi);
        currentLocation.setImsi(imsi);
    }

    public static void setCurrentBlackList() {
        List<DBBlackInfo> listBlackList = null;
        try {
            listBlackList = UCSIDBManager.getDbManager().selector(DBBlackInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listBlackList == null || listBlackList.size() == 0)
            return;

        StringBuilder content = new StringBuilder();

        for (DBBlackInfo dbBlackInfo : listBlackList) {
            content.append("#").append(dbBlackInfo.getImsi());
        }

//        ProtocolManager.setBlackList("2", content.toString());
    }


    /**
     * @param imsi 开始定位
     */
    public static void startLoc(String imsi) {
        if (!VersionManage.isArmyVer()){
            LTESendManager.setActiveMode("1");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LTESendManager.setLocImsi(imsi);
                }
            },1000);

        }

        CacheManager.getCurrentLocation().setLocateStart(true);
    }

    /**
     * 开关管控模式
     */
    public static void setLocalWhiteList(String mode) {

        LTESendManager.setNameList(mode, "", "",
                "", "", "block", "");

    }

    public static String getSimIMSI(int simid) {
        TelephonyManager telephonyManager = (TelephonyManager) MyApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
            return "";

        int[] subId = null;//SubscriptionManager.getSubId(simid);
        Class<?>  threadClazz = SubscriptionManager.class;
        try {
            Method method = threadClazz.getDeclaredMethod("getSubId", int.class);
            method.setAccessible(true);
            subId = (int[]) method.invoke(null, simid);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        int sub = -1;
        if (Build.VERSION.SDK_INT >= 24) {
            sub = (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubscriptionId();
        } else {
            try {
                Method method = threadClazz.getDeclaredMethod("getDefaultSubId");
                method.setAccessible(true);
                sub = (subId != null) ? subId[0] : (Integer) method.invoke(null, (Object[]) null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        String IMSI = "";
        if (sub != -1) {
            Class clazz = telephonyManager.getClass();
            try {
                Method method = clazz.getDeclaredMethod("getSubscriberId", int.class);
                method.setAccessible(true);
                IMSI = (String) method.invoke(telephonyManager, sub);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return IMSI;
    }


    public static void clearCurrentBlackList() {
        List<DBBlackInfo> listBlackList = null;
        try {
            listBlackList = UCSIDBManager.getDbManager().selector(DBBlackInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listBlackList == null || listBlackList.size() == 0)
            return;

        String content = "";
        for (DBBlackInfo dbBlackInfo : listBlackList) {
            content += "#";
            content += dbBlackInfo.getImsi();
        }

        LTESendManager.setBlackList("3", content);
    }

    public static void stopCurrentLoc() {
        LTESendManager.setLocImsi("0000");

        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            DBChannel dbChannel = dbManager.selector(DBChannel.class)
                    .where("band", "=", "3")
                    .and("is_check","=","1")
                    .findFirst();
            if (dbChannel !=null){
                String idx="";
                for (LteChannelCfg channel : CacheManager.getChannels()) {
                    if (channel.getBand().equals("3")){
                        idx = channel.getIdx();
                        break;
                    }
                }

                if (!TextUtils.isEmpty(idx)){
                    LTESendManager.setChannelConfig(idx, dbChannel.getFcn(),
                            "46001,46011", "", "", "", "", "");

                    for (LteChannelCfg channel : CacheManager.channels) {
                        if (channel.getIdx().equals(idx)) {
                            channel.setFcn(dbChannel.getFcn());
                            channel.setPlmn("46000,46001");
                            break;
                        }
                    }
                }
            }

        } catch (DbException e) {
            e.printStackTrace();
        }

        if (CacheManager.getCurrentLocation() != null){
            CacheManager.getCurrentLocation().setLocateStart(false);
        }

    }

    public static boolean getLocState() {
        if (currentLocation == null)
            return false;

        return currentLocation.isLocateStart();
    }

    public static LocationBean getCurrentLocation() {
        return currentLocation;
    }

    public static LocationRptBean getCurrentLocRptBean() {
        if (locationRpts == null) {
            return null;
        }
        if (locationRpts.size() < 1) {
            return null;
        }
        return locationRpts.get(locationRpts.size() - 1);
    }


    /**
     * 检查设备是否连接，并提示
     *
     * @param context
     * @return
     */
    public static boolean checkDevice(Context context) {
        if (!isDeviceOk() ) {
            new MySweetAlertDialog(context, MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText("错误")
                    .setContentText("设备未就绪")
                    .show();
            return false;
        }
        return true;
    }

    public static boolean isDeviceOk() {
        return hasSetDefaultParam && cellConfig != null && channels.size() != 0 && equipConfig != null;
    }

    /**
     * @return 射频是否开启
     */
    public static boolean isRFOpen(){
        boolean isOpen = false;
        if (channels !=null && channels.size()>0){
            for (LteChannelCfg channel : channels) {
                if (channel.getRFState()) {
                    isOpen = true;
                    break;
                }
            }
        }

        return isOpen;
    }

    /**
     * 重置一下状态，一般设备需要重启时调用
     */
    public static void resetState() {
        cellConfig = null;
        channels.clear();
        equipConfig = null;
    }



    public static LteCellConfig getCellConfig() {
        return cellConfig;
    }

    public static LteEquipConfig getLteEquipConfig() {
        return equipConfig;
    }

    public static List<LteChannelCfg> getChannels() {
        return channels;
    }

    public static void setCellConfig(LteCellConfig cellConfig) {
        CacheManager.cellConfig = cellConfig;
    }

    public static void setEquipConfig(LteEquipConfig equipConfig) {
        CacheManager.equipConfig = equipConfig;
    }

    public synchronized static void addChannel(LteChannelCfg cfg) {
        for (int i = 0; i < channels.size(); i++) {
            LteChannelCfg channel = channels.get(i);
            if (cfg.getIdx().equals(channel.getIdx())) {
                channel.setFcn(cfg.getFcn());
                channel.setBand(cfg.getBand());
                channel.setGa(cfg.getGa());
                channel.setPa(cfg.getPa());
                channel.setPlmn(cfg.getPlmn());
                channel.setRlm(cfg.getRlm());
                channel.setAutoOpen(cfg.getAutoOpen());
                channel.setAltFcn(cfg.getAltFcn());
                channel.setChangeBand(cfg.getChangeBand());
                return;
            }
        }

        channels.add(cfg);
        Collections.sort(channels, new Comparator<LteChannelCfg>() {
            @Override
            public int compare(LteChannelCfg lhs, LteChannelCfg rhs) {
                return NumberUtils.toInt(lhs.getBand()) - NumberUtils.toInt(rhs.getBand());
            }
        });
    }

    //将RF状态更新到内存
    public synchronized static void updateRFState(String idx, boolean rf) {
        //UtilBaseLog.printLog(idx + "    size:" + channels.size() + "    " + rf);
        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(idx)) {
                channel.setRFState(rf);
                return;
            }
        }
    }

    public static LteChannelCfg getChannelByIdx(String idx) {
        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(idx)) {
                return channel;
            }
        }
        return null;
    }


    public static void updateWhitelistToDev(Context context) {
        /*
        * 考虑到白名单数量巨大时严重影响设备使用，决定不再下发白名单给设备，只做特殊显示
        List<WhiteListInfo> listWhitelist = null;
        String content = "";
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String mobileImsi =  StringUtils.defaultIfBlank(telManager.getSubscriberId(), "");
        //UtilBaseLog.printLog("######" + telManager.getSubscriberId());
        try {
            listWhitelist = UCSIDBManager.getDbManager().selector(WhiteListInfo.class).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (listWhitelist == null || listWhitelist.size() == 0){
            if (!"".equals(mobileImsi)){
                content = mobileImsi;
            }
            //ProtocolManager.setNamelist("", "", "", "","","block");
        }else{
            for (WhiteListInfo whiteListInfo : listWhitelist) {
                if ("".equals(whiteListInfo.getImsi()))
                    continue;

                content += whiteListInfo.getImsi();
                content += ",";
            }

            if ("".equals(mobileImsi)){
                content = content.substring(0, content.length()-1);
            }else{
                content = content+mobileImsi;
            }
        }

        ProtocolManager.setNamelist("", content, "", "","","block");
        */
    }

    public static void setHighGa(boolean on_off) {
        if (on_off) {
            for (LteChannelCfg channel : channels) {
                if (Integer.parseInt(channel.getGa()) <= 10) {
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "",
                            String.valueOf(Integer.parseInt(channel.getGa()) * 5), "", "", "");
                    channel.setGa(String.valueOf(Integer.parseInt(channel.getGa()) * 5));
                }
            }
        } else {
            for (LteChannelCfg channel : channels) {
                if (Integer.parseInt(channel.getGa()) > 10) {
                    LTESendManager.setChannelConfig(channel.getIdx(), "", "", "",
                            String.valueOf(Integer.parseInt(channel.getGa()) / 5), "", "", "");
                    channel.setGa(String.valueOf(Integer.parseInt(channel.getGa()) / 5));
                }
            }
        }
    }

    public static boolean isHighGa() {
        if (!isDeviceOk())
            return true;    //默认高

        int allGa = 0;
        for (LteChannelCfg channel : channels) {
            allGa += Integer.parseInt(channel.getGa());
        }

        return allGa > 32;
    }

    public static void changeBand( String idx,  String changeBand) {

        LTESendManager.changeBand(idx, changeBand);

        //下发切换之后，等待生效，设置默认频点
        String fcn = LTESendManager.getCheckedFcn(changeBand);
        if (!TextUtils.isEmpty(fcn)){
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LTESendManager.setChannelConfig(idx, fcn, "", "", "", "", "", "");
                }
            }, 2000);
        }

    }


    /*

     */
    public static synchronized void saveImsi(String imsi) {
        boolean isContain = false;
        for (UeidBean ueidBean : CacheManager.realtimeUeidList) {
            if (ueidBean.getImsi().equals(imsi)){
                isContain = true;
                break;
            }
        }
        if (!isContain){
            UCSIDBManager.saveUeidToDB(imsi, ImsiMsisdnConvert.getMsisdnFromLocal(imsi), "",
                    new Date().getTime(), "", "");
        }
    }

    public static void clearUeidWhithoutSrsp() {
        for (int i = 0; i < realtimeUeidList.size(); i++) {
            if (realtimeUeidList.get(i).getSrsp().equals("")) {
                realtimeUeidList.remove(i);
                i--;
            }
        }
    }

}
