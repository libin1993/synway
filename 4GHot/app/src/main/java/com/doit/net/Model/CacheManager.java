/*
 * Copyright (C) 2011-2016 dshine.com.cn
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com.cn
 */
package com.doit.net.Model;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.doit.net.application.MyApplication;
import com.doit.net.bean.DeviceState;
import com.doit.net.bean.LocationBean;
import com.doit.net.bean.LocationRptBean;
import com.doit.net.bean.LteCellConfig;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.LteEquipConfig;
import com.doit.net.bean.Namelist;
import com.doit.net.bean.ScanFreqRstBean;
import com.doit.net.bean.UeidBean;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.LogUtils;
import com.doit.net.udp.g4.bean.G4MsgChannelCfg;

import org.apache.commons.lang3.math.NumberUtils;
import org.xutils.ex.DbException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public static String DEVICE_IP = "192.168.4.100";

    public static List<UeidBean> realtimeUeidList = new ArrayList<>();
    public static final int MAX_REALTIME_LIST_SIZE = 300;


    public static List<LocationBean> locations = new ArrayList<>();
    public static LocationBean currentLoction = null;
    public static List<LocationRptBean> locationRpts = new ArrayList<>();

    public static Namelist namelist = new Namelist();

    public static long last_heart_time;

    public static String currentWorkMode = "2";   //0公安侦码  2军队管控
    public static boolean isReportBattery = false;  //是否上报电量

    public static DeviceState deviceState = new DeviceState();

    public static List<ScanFreqRstBean> listLastScanFreqRst = new ArrayList<>();

    public static boolean loc_mode = false;  //是否开启搜寻功能

    public static boolean isWifiConnected = false;

    private static boolean hasPressStartButton = false;  //是否已经在主页面点击开始按钮

    public static boolean checkLicense = false; //连接成功后校验证书

    public static boolean getLocMode() {
        return loc_mode;
    }

    public static LteChannelCfg b3Fcn= new LteChannelCfg();  //b3 移动定位时，强制设置1300

    public static void setLocMode(boolean locMode) {
        loc_mode = locMode;
    }



    public synchronized static void addRealtimeUeidList(List<UeidBean> listUeid) {
        addToList(listUeid);

        /* 如果实时上报界面没加载就有数据上传，就会丢失数据
           所以将存储数据库操作移到processUeidRpt */
//        try {
//            DbManager dbManager = UCSIDBManager.getDbManager();
//            for (int i= 0; i < listUeid.size(); i++){
//                DBUeidInfo info = new DBUeidInfo();
//                info.setImsi(listUeid.get(i).getImsi());
//                info.setTmsi(listUeid.get(i).getTmsi());
//                //info.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(listUeid.get(i).getRptTime()));
//                info.setCreateDate(DateUtil.convert2long(listUeid.get(i).getRptTime(), DateUtil.LOCAL_DATE));
//                info.setLongitude(listUeid.get(i).getLongitude());
//                info.setLatitude(listUeid.get(i).getLatitude());
//                dbManager.save(info);
//            }
//        } catch (DbException e) {
//            log.error("插入UEID 到数据库异常",e);
//        }
    }

    public synchronized static void addToList(List<UeidBean> listUeid) {
        if ((realtimeUeidList.size() + listUeid.size()) >= MAX_REALTIME_LIST_SIZE) {
            for (int i = 0; i < (realtimeUeidList.size() + listUeid.size() - MAX_REALTIME_LIST_SIZE); i++)
                //realtimeUeidList.remove(realtimeUeidList.size()-1);
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
        if (currentLoction == null) {
            currentLoction = new LocationBean();
        }
        PrefManage.setImsi(imsi);
        currentLoction.setImsi(imsi);
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

        String content = "";
        for (DBBlackInfo dbBlackInfo : listBlackList) {
            content += "#";
            content += dbBlackInfo.getImsi();
        }

        ProtocolManager.setBlackList("2", content);
    }


    public static void startLoc(String imsi) {
        ProtocolManager.setActiveMode("1");
        ProtocolManager.setLocImsi(imsi);

        CacheManager.getCurrentLoction().setLocateStart(true);
    }

    public static void changeLocTarget(String imsi) {
        ProtocolManager.setLocImsi(imsi);
    }

    /**
     * 开关管控模式
     */
    public static void setLocalWhiteList(String mode) {
        String imsi0 = getSimIMSI(0);
        String imsi1 = getSimIMSI(1);

        if (imsi0 == null || imsi0.equals("000000000000000"))
            imsi0 = "";

        if (imsi1 == null || imsi1.equals("000000000000000"))
            imsi1 = "";


        String whitelistContent = "";

        if ("".equals(imsi0) && "".equals(imsi1)) {
            whitelistContent = "";
        } else if (!"".equals(imsi0) && "".equals(imsi1)) {
            whitelistContent = imsi0;
        } else if ("".equals(imsi0) && !"".equals(imsi1)) {
            whitelistContent = imsi1;
        } else {
            whitelistContent = imsi0 + "," + imsi1;
        }


        ProtocolManager.setNameList(mode, "", whitelistContent,
                "", "", "block", "", "");

    }

    public static String getSimIMSI(int simid) {
        TelephonyManager telephonyManager = (TelephonyManager) MyApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
            return "";

        int[] subId = null;//SubscriptionManager.getSubId(simid);
        Class<?> threadClazz = null;
        threadClazz = SubscriptionManager.class;

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
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
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
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
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

        ProtocolManager.setBlackList("3", content);
    }

    public static void stopCurrentLoc() {
        ProtocolManager.setLocImsi("0000");
        ProtocolManager.setActiveMode(CacheManager.currentWorkMode);

        ProtocolManager.setChannelConfig(CacheManager.b3Fcn.getIdx(), CacheManager.b3Fcn.getFcn(),
                CacheManager.b3Fcn.getPlmn(), "", "", "", "", "");

        if (CacheManager.getCurrentLoction() != null)
            CacheManager.getCurrentLoction().setLocateStart(false);
    }

    public static boolean getLocState() {
        if (currentLoction == null)
            return false;

        return currentLoction.isLocateStart();
    }

    public static LocationBean getCurrentLoction() {
        return currentLoction;
    }

    public static LocationRptBean getCurrentLocRptBean() {
        if (locationRpts == null) {
            return null;
        }
        if (locationRpts.size() - 1 < 0) {
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
        if (!isDeviceOk()) {
            new MySweetAlertDialog(context, MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText("错误")
                    .setContentText("设备未就绪")
                    .show();
            return false;
        }
        return true;
    }

    public static boolean isDeviceOk() {
        return cellConfig != null && channels.size() != 0 && equipConfig != null;
    }

    /**
     * 重置一下状态，一般设备需要重启时调用
     */
    public static void resetState() {
        LogUtils.log("reset state.");
        cellConfig = null;
        channels.clear();
        equipConfig = null;
    }

    private static LteCellConfig cellConfig;
    private static LteEquipConfig equipConfig;
    public static List<LteChannelCfg> channels = new ArrayList<>();


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

    public static void setNamelist(Namelist list) {
        namelist = list;
    }

    public synchronized static void addChannel(LteChannelCfg cfg) {
        for (LteChannelCfg channel : channels) {
            if (channel.getIdx().equals(cfg.getIdx())) {
                channel.setFcn(cfg.getFcn());
                channel.setBand(cfg.getBand());
                channel.setGa(cfg.getGa());
                channel.setPa(cfg.getPa());
                channel.setPlmn(cfg.getPlmn());
                channel.setRlm(cfg.getRlm());
                channel.setAutoOpen(cfg.getAutoOpen());
                channel.setAltFcn(cfg.getAltFcn());
                channel.setChangeBand(cfg.getChangeBand());
                //channel.setState(cfg.getState());
                return;
            }
        }

        channels.add(cfg);
        Collections.sort(channels, new Comparator<LteChannelCfg>() {
            @Override
            public int compare(LteChannelCfg lhs, LteChannelCfg rhs) {
                return NumberUtils.toInt(lhs.getPlmn()) - NumberUtils.toInt(rhs.getPlmn());
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

    private static Map<String, List<G4MsgChannelCfg>> userChannels = new HashMap<>();

    public static void addUserChannel(G4MsgChannelCfg cfg) {
        if (userChannels.containsKey(cfg.getIdx())) {
            userChannels.get(cfg.getIdx()).add(cfg);
        } else {
            List<G4MsgChannelCfg> list = new ArrayList<>();
            list.add(cfg);
            userChannels.put(cfg.getIdx(), list);
        }
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
                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", "", String.valueOf(Integer.parseInt(channel.getGa()) * 5), "", "", "");
                }
            }
        } else {
            for (LteChannelCfg channel : channels) {
                if (Integer.parseInt(channel.getGa()) > 10) {
                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", "", String.valueOf(Integer.parseInt(channel.getGa()) / 5), "", "", "");
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

    public static void changeBand(final String idx, final String changeBand) {
        ProtocolManager.changeBand(idx, changeBand);

        //下发切换之后，等待生效，设置默认频点
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String band38Fcns = "37900,38098,38200";
                String band40Fcns = "38950,39148,39300";

                switch (changeBand) {
                    case "38":
                        ProtocolManager.setChannelConfig(idx, band38Fcns, "", "", "", "", "", "");
                        break;

                    case "40":
                        ProtocolManager.setChannelConfig(idx, band40Fcns, "", "", "", "", "", "");
                        break;

                    default:
                        break;
                }
            }
        }, 5000);

        //设置完频点，更新参数和界面
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ProtocolManager.getEquipAndAllChannelConfig();
            }
        }, 12000);

        EventAdapter.call(EventAdapter.SHOW_PROGRESS, 13000);
    }


    /*
        删除列表里已存在的ueid
        成功删除返回ture,没有删除（即不存在）返回false
     */
    public static synchronized boolean removeExistUeidInRealtimeList(String imsi) {
        for (int i = 0; i < realtimeUeidList.size(); i++) {
            if (realtimeUeidList.get(i).getImsi().equals(imsi)) {
                realtimeUeidList.remove(i);
                return true;
            }
        }

        return false;
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
