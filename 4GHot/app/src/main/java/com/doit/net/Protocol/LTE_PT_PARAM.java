package com.doit.net.Protocol;


import android.text.TextUtils;

import com.doit.net.bean.BatteryBean;
import com.doit.net.bean.DeviceState;
import com.doit.net.bean.LteCellConfig;
import com.doit.net.bean.Namelist;
import com.doit.net.bean.UeidBean;
import com.doit.net.Data.LTESendManager;
import com.doit.net.bean.BlackNameBean;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.LteEquipConfig;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.ImsiMsisdnConvert;
import com.doit.net.Model.ScanFreqManager;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.UtilDataFormatChange;
import com.doit.net.Utils.UtilOperator;
import com.doit.net.ucsi.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTE_PT_PARAM {
    public static final byte PT_PARAM = 0x05;


    public static final byte PARAM_GET_ENB_CONFIG = 0x02;    //获取设备配置
    public static final byte PARAM_GET_ENB_CONFIG_ACK = 0x03;
    public static final byte PARAM_GET_NAMELIST = 0x08;    //获取名单信息
    public static final byte PARAM_GET_NAMELIST_ACK = 0x09;
    public static final byte PARAM_GET_ACTIVE_MODE = 0x0a;    //查询工作模式
    public static final byte PARAM_GET_ACTIVE_MODE_ASK = 0x0b;    //查询工作模式

    public static final byte PARAM_SET_ENB_CONFIG = 0x11;    //设置设备配置
    public static final byte PARAM_SET_ENB_CONFIG_ACK = 0x12;
    public static final byte PARAM_SET_CHANNEL_CONFIG = 0x13;    //设置通道配置
    public static final byte PARAM_SET_CHANNEL_CONFIG_ACK = 0x14;
    public static final byte PARAM_SET_BLACK_NAMELIST = 0x15;     //设置黑名单
    public static final byte PARAM_SET_BLACK_NAMELIST_ACK = 0x16;
    public static final byte PARAM_SET_FTP_CONFIG = 0x17;    //设置ftp
    public static final byte PARAM_SET_FTP_CONFIG_ACK = 0x18;
    public static final byte PARAM_SET_CHANNEL_ON = 0x19;    //设置通道开
    public static final byte PARAM_SET_CHANNEL_ON_ACK = 0x1a;
    public static final byte PARAM_SET_CHANNEL_OFF = 0x1b;    //设置通道关
    public static final byte PARAM_SET_CHANNEL_OFF_ACK = 0x1c;
    public static final byte PARAM_SET_NAMELIST = 0x1d;    //设置名单
    public static final byte PARAM_SET_NAMELIST_ACK = 0x1e;
    public static final byte PARAM_SET_RT_IMSI = 0x21;    //设置是否实时上报黑名单IMSI
    public static final byte PARAM_SET_RT_IMSI_ACK = 0x22;
    public static final byte PARAM_SET_SCAN_FREQ = 0x23; //下发扫频命令
    public static final byte PARAM_SET_SCAN_FREQ_ACK = 0x24;
    public static final byte PARAM_RPT_SCAN_FREQ = 0x25;
    public static final byte PARAM_RPT_UPGRADE_STATUS = 0x26; //设备升级
    public static final byte RPT_SRSP_GROUP = 0x27;    //定位上报
    public static final byte PARAM_RPT_HEATBEAT = 0x31;    //心跳
    public static final byte PARAM_RPT_BLACK_NAME = 0x32;    //黑名单中标上报
    public static final byte PARAM_RPT_RT_IMSI = 0x33;    //中标上报
    public static final byte PARAM_CHANGE_TAG = 0x34;    //更新tac
    public static final byte PARAM_CHANGE_TAG_ACK = 0x35;    //更新tac
    public static final byte PARAM_CHANGE_BAND = 0x36;    //更换band
    public static final byte PARAM_CHANGE_BAND_ACK = 0x37;    //更换band
    public static final byte PARAM_SET_FAN = 0x38;    //设置风扇
    public static final byte PARAM_SET_FAN_ACK = 0x39;    //设置风扇
    public static final byte PARAM_SET_ACTIVE_MODE = 0x3a;    //设置工作模式
    public static final byte PARAM_SET_ACTIVE_MODE_ACK = 0x3b;    //设置工作模式回复
    public static final byte PARAM_SET_LOC_IMSI = 0x3c;    //设置定位
    public static final byte PPARAM_SET_LOC_IMSI_ACK = 0x3d;    //设置定位回复
    public static final byte SET_IMSI_TRANS_OPTIONS = 0x45;    //设置定IMSI翻译
//    public static final byte SET_IMSI_TRANS_OPTIONS_ACK = 0xc5;    //设置IMSI翻译回复

    private static long lastRptSyncErrorTime = 0; //记录每次上报同步状态异常的时间

    //查询参数
    public static boolean queryCommonParam(byte queryType) {

        LTESendPackage sendPackage = new LTESendPackage();
        //设置Sequence ID
        sendPackage.setPackageSequence(LTEProtocol.getSequenceID());
        //设置Session ID
        sendPackage.setPackageSessionID(LTEProtocol.getSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(LTEProtocol.equipType);
        //设置预留
        sendPackage.setPackageReserve((byte) 0xFF);
        //设置主类型
        sendPackage.setPackageMainType(PT_PARAM);
        //设置子类型
        sendPackage.setPackageSubType(queryType);
        //设置内容
        sendPackage.setByteSubContent(null);
        //设置校验位
        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());
        //获取整体的包
        byte[] tempSendBytes = sendPackage.getPackageContent();

        LogUtils.log1("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:" + Integer.toHexString(sendPackage.getPackageSubType()) + "" + ";  Content:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));
        return LTESendManager.sendData(tempSendBytes);
    }

    //解析设备配置查询回复
    public static void processEnbConfigQuery(LTEReceivePackage receivePackage) {
        //IDX:10@BAND:1@FCN:100,400,500@ALT_FCN:@PLMN:46000,46001,46011@PA:-7,-7,-7@GA:35@PW:43.0@RLM:-108@AUTO_OPEN:0@MAX:-5
        //IDX:11@BAND:38@FCN:37900,40936,38098@ALT_FCN:@PLMN:46000,46001,46011@PA:-1,-1,-1@GA:50@PW:44.0@RLM:-100@CHANGE:40@AUTO_OPEN:0@MAX:-1
        //IDX:12@BAND:3@FCN:1650,1506,1825@ALT_FCN:@PLMN:46000,46001,46011@PA:-7,-7,-7@GA:35@PW:43.0@RLM:-100@AUTO_OPEN:0@MAX:-5
        //IDX:13@BAND:39@FCN:38544,38400,38300@ALT_FCN:@PLMN:46000,46001,46011@PA:-13,-13,-13@GA:40@PW:43.0@RLM:-90@AUTO_OPEN:0@MAX:-10
        String enbConfigAck = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        LogUtils.log("查询配置回复:" + enbConfigAck);
        String[] splitStr = enbConfigAck.split("#");

        LteEquipConfig lteEquipConfig = praseEquipConfig(splitStr[0]);
        CacheManager.setEquipConfig(lteEquipConfig);

        LteCellConfig lteCellConfig = praseCellConfig(splitStr[0]);
        CacheManager.setCellConfig(lteCellConfig);

        //通道级的配置
        for (int i = 1; i < splitStr.length; i++) {
            LogUtils.log(splitStr[i]);
            LteChannelCfg lteChannelCfg = praseChannelConfig(splitStr[i]);

            CacheManager.addChannel(lteChannelCfg);
        }



        EventAdapter.call(EventAdapter.UPDATE_BATTERY, Integer.valueOf(CacheManager.getLteEquipConfig().getVoltage12V()));

        EventAdapter.call(EventAdapter.REFRESH_DEVICE);
        EventAdapter.call(EventAdapter.INIT_SUCCESS);
    }

    //解析名单查询回复
    public static void processNamelistQuery(LTEReceivePackage receivePackage) {
        // MODE:on
        // @REDIRECT_CONFIG:46000,4,38400#46001,4,300#46011,4,100#46002,2,98
        // @NAMELIST_REJECT:460001234512345,460011234512345,460111234512345
        // @NAMELIST_REDIRECT:460001234512345,460011234512345,460111234512345
        // @NAMELIST_BLOCK:460001234512345,460011234512345,460111234512345
        // @NAMELIST_RELEASE:460001234512345
        // @NAMELIST_REST_ACTION:block
        String namelistAck = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        LogUtils.log("获取白名单回复:" + namelistAck);
        String[] splitStr = namelistAck.split("@");

        Namelist namelist = new Namelist();


        for (String s : splitStr) {
            String[] split = s.split(":");
            String value = split.length > 1 ? split[1] : "";
            switch (split[0]) {
                case "MODE":
                    namelist.setMode(value);
                    break;
                case "REDIRECT_CONFIG":
                    namelist.setRedirectConfig(value);
                    break;
                case "NAMELIST_REJECT":
                    namelist.setNamelistReject(value);
                    break;
                case "NAMELIST_REDIRECT":
                    namelist.setNamelistRedirect(value);
                    break;
                case "NAMELIST_BLOCK":
                    namelist.setNamelistBlock(value);
                    break;
                case "NAMELIST_REST_ACTION":
                    namelist.setNamelistRestAciton(value);
                    break;
                case "NAMELIST_RELEASE":
                    namelist.setNamelistRelease(value);
                    break;
                case "NAMELIST_FILE":
                    namelist.setNamelistFile(value);
                    break;
            }
        }

        CacheManager.setNamelist(namelist);
        EventAdapter.call(EventAdapter.GET_NAME_LIST);
    }


    //通用设置配置
    public static boolean setCommonParam(byte paramSubType, String paramContent) {
        LTESendPackage sendPackage = new LTESendPackage();
        //设置Sequence ID
        sendPackage.setPackageSequence(LTEProtocol.getSequenceID());
        //设置Session ID
        sendPackage.setPackageSessionID(LTEProtocol.getSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(LTEProtocol.equipType);
        //设置预留
        sendPackage.setPackageReserve((byte) 0xFF);
        //设置主类型
        sendPackage.setPackageMainType(PT_PARAM);
        //设置子类型
        sendPackage.setPackageSubType(paramSubType);
        sendPackage.setByteSubContent(UtilDataFormatChange.stringtoBytesForASCII(paramContent));

        //设置校验位
        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());

        //获取整体的包
        byte[] tempSendBytes = sendPackage.getPackageContent();
        LogUtils.log1("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:" + Integer.toHexString(sendPackage.getPackageSubType()) + ";  Content:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));
        return LTESendManager.sendData(tempSendBytes);
    }


    //处理心跳
    public static void processRPTHeartbeat(LTEReceivePackage receivePackage) {
        // IDX:60@STATE:0#IDX:61@STATE:0#IDX:72@STATE:0#IP:192.168.0.1@ID:BL001@TM:55:66:77,45:55:65@G1:12.123456@G2:12.123456
        // @DATANUM:30@RPTSTATUS:1@ENBSTATUS:1@FTPERRCNT:10@SYNCSTATUS:0
        String heartbeat = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        LogUtils.log("processRPTHeartbeat:" + heartbeat);

        //同步状态
        lastRptSyncErrorTime = System.currentTimeMillis();  //第一次不提示
        if (heartbeat.split("SYNCSTATUS")[1].charAt(1) != '0') {
            if ((int) (System.currentTimeMillis() - lastRptSyncErrorTime) > 5 * 60 * 1000) {
                LogUtils.log("同步状态异常");
                lastRptSyncErrorTime = System.currentTimeMillis();
                //EventAdapter.call(EventAdapter.SYNC_ERROR_RPT);
            }

        }


        //更新射频状态
        String[] splitStr = heartbeat.split("#");
        for (int i = 0; i < splitStr.length; i++) {
            if (splitStr[i].contains("IDX")) {
                CacheManager.updateRFState(splitStr[i].split(":")[1].split("@")[0],
                        splitStr[i].split("STATE")[1].charAt(1) == '1');
            }
        }

        BatteryBean batteryBean = new BatteryBean();
        String[] split = heartbeat.split("@");

        for (String s : split) {
            String[] key = s.split(":");
            switch (key[0]) {
                case "TM":   //温度
                    String temperature = key[1];
                    EventAdapter.call(EventAdapter.UPDATE_TMEPRATURE, temperature);
                    break;
                case "COU_STATE":   //是否正在充电
                    batteryBean.setCharging("1".equals(key[1]));
                    break;
                case "COU_LEFT":   //剩余电量
                    batteryBean.setBatteryQuantity(Integer.parseInt(key[1].split("%")[0]));

                    break;
                case "COU_USE_MIN":   //剩余电量可用分钟
                    batteryBean.setUseTime(Integer.parseInt(key[1]));
                    break;
            }
        }
        CacheManager.isReportBattery = batteryBean.getBatteryQuantity() > 0;
        EventAdapter.call(EventAdapter.BATTERY_STATE, batteryBean);

        EventAdapter.call(EventAdapter.HEARTBEAT_RPT);
        EventAdapter.call(EventAdapter.RF_STATUS_RPT);
    }

    //处理黑名单中标上报
    public static void processRptBlackName(LTEReceivePackage receivePackage) {
        if (!CacheManager.isDeviceOk())
            return;

        String rptBlackName = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        //UtilBaseLog.printLog("processRptBlackName:" + rptBlackName);
        String[] splitStr = rptBlackName.split("#");

        BlackNameBean blackName = new BlackNameBean(splitStr[0], splitStr[1], "", "", new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()));
        if (splitStr.length >= 4) {
            blackName.setLatitude(splitStr[2]);
            blackName.setLatitude(splitStr[3]);
        }

//        LogUtils.log("##################  中标：" + blackName.getIMSI() + "  #########################");
        EventAdapter.call(EventAdapter.BLACK_NAME_RPT, blackName);
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile())
            file.delete();
    }

    //处理Ftp上传的UEID文件
    public static void processUeidRpt(String filePath) {
        if (filePath.contains("19700101")) {
            LogUtils.log("上报采集文件时间无效文件——忽略");
            deleteFile(filePath);
            return;
        }

        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            List<UeidBean> listUeid = new ArrayList<>();
            String[] splitUeid;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String readLine = "";
                String tmpImsi, tmpTmsi, band, tmpRptTime, longitude, latitude;


                while ((readLine = bufferedReader.readLine()) != null) {
                    splitUeid = readLine.split("\t");

                    //如果是定位号码就跳过（目前的情况是在定位就不会有FTP上传）
//                    if (CacheManager.getCurrentLoction() != null && CacheManager.getCurrentLoction().isLocateStart() &&
//                            (CacheManager.currentLoction.getImsi().equals(splitUeid[0])))
//                        continue;

                    //据测试，有时文件最后会多一行且带有/t
                    if (splitUeid.length < 6) {
                        continue;
                    }

                    tmpImsi = splitUeid[0];
                    tmpTmsi = splitUeid[1];
                    band = splitUeid[2];
                    tmpRptTime = splitUeid[3];
                    longitude = splitUeid[4];
                    latitude = splitUeid[5];

                    if (isImsiExistInSimpleRpt(tmpImsi, listUeid)) {
                        continue;
                    }

                    listUeid.add(new UeidBean(tmpImsi, "未获取", tmpTmsi, band, tmpRptTime, longitude, latitude));
//                        UtilBaseLog.printLog(splitUeid[0]+"  "+splitUeid[1]+"  "+splitUeid[2]+"  "+splitUeid[3]+"  "+splitUeid[4]+"  "+
//                                splitUeid[5]);

                    if (!CacheManager.removeExistUeidInRealtimeList(splitUeid[0])) {
                        //
                        /* 1.如果实时上报界面没打开，就只是存到数据库而不显示
                         * 2.存入数据库需要去重，去重的依据是否则已经存在实时列表里
                         * */
                        UCSIDBManager.saveUeidToDB(tmpImsi, ImsiMsisdnConvert.getMsisdnFromLocal(tmpImsi), tmpTmsi,
                                DateUtils.convert2long(tmpRptTime, DateUtils.LOCAL_DATE), longitude, latitude);
                    }
                }
                bufferedReader.close();
                file.delete();   //处理完删除

                EventAdapter.call(EventAdapter.UEID_RPT, listUeid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isImsiExistInSimpleRpt(String imsi, List<UeidBean> listUeid) {
        for (int i = 0; i < listUeid.size(); i++) {
            if (listUeid.get(i).getImsi().equals(imsi)) {
                return true;
            }
        }

        return false;
    }

    //搜网结果上报
    public static void processRPTFreqScan(LTEReceivePackage receivePackage) {
        if (!CacheManager.isDeviceOk())
            return;
        String band1FcnList = "";
        String band3FcnList = "";
        String band38FcnList = "";
        String band39FcnList = "";
        String band40FcnList = "";


        String scanFreqResult = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        LogUtils.log("搜网上报:" + scanFreqResult);
        String tmpFcn = "";
        int tmpBand = 1;
        String[] splitStr = scanFreqResult.split(",");
        for (int i = 0; i < splitStr.length; i++) {
            tmpFcn = splitStr[i].split("@")[1].split(":")[1];
            tmpBand = UtilOperator.getBandByFcn(Integer.parseInt(tmpFcn));
            switch (tmpBand) {
                case 1:
                    if (!band1FcnList.contains(tmpFcn)) {
                        band1FcnList += tmpFcn;
                        band1FcnList += ",";
                    }

                    break;
                case 3:
                    if (!band3FcnList.contains(tmpFcn)) {
                        band3FcnList += tmpFcn;
                        band3FcnList += ",";
                    }
                    break;
                case 38:
                    if (!band38FcnList.contains(tmpFcn)) {
                        band38FcnList += tmpFcn;
                        band38FcnList += ",";
                    }
                    break;
                case 39:
                    if (!band39FcnList.contains(tmpFcn)) {
                        band39FcnList += tmpFcn;
                        band39FcnList += ",";
                    }
                    break;

                case 40:
                    if (!band40FcnList.contains(tmpFcn)) {
                        band40FcnList += tmpFcn;
                        band40FcnList += ",";
                    }
                    break;
            }
        }

        LogUtils.log("搜网结果解析：" + band1FcnList + "/" + band3FcnList + "/" + band38FcnList + "/" + band39FcnList + "/" + band40FcnList);
        ScanFreqManager.saveAndSetScanFreqResult(
                "".equals(band1FcnList) ? "" : band1FcnList.substring(0, band1FcnList.length() - 1),
                "".equals(band3FcnList) ? "" : band3FcnList.substring(0, band3FcnList.length() - 1),
                "".equals(band38FcnList) ? "" : band38FcnList.substring(0, band38FcnList.length() - 1),
                "".equals(band39FcnList) ? "" : band39FcnList.substring(0, band39FcnList.length() - 1),
                "".equals(band40FcnList) ? "" : band40FcnList.substring(0, band40FcnList.length() - 1));
    }


    //处理配置回复
    public static void processSetResp(LTEReceivePackage receivePackage) {
        String respContent = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        switch (receivePackage.getPackageSubType()) {
            case PARAM_SET_CHANNEL_ON_ACK:
                String[] onAsk = respContent.split("#");
                if (onAsk[0].charAt(0) == '0') {
                    for (LteChannelCfg channel : CacheManager.getChannels()) {
                        if (channel.getIdx().equals(onAsk[1])) {
                            LogUtils.log(onAsk[1]+"：射频开启成功");
                            channel.setRFState(true);
                        }
                    }
                }

                EventAdapter.call(EventAdapter.RF_STATUS_LOC);
                EventAdapter.call(EventAdapter.RF_STATUS_RPT);
                EventAdapter.call(EventAdapter.REFRESH_DEVICE);
                break;

            case PARAM_SET_CHANNEL_OFF_ACK:
                String[] offAsk = respContent.split("#");
                //UtilBaseLog.printLog("关");
                if (offAsk[0].charAt(0) == '0') {
                    for (LteChannelCfg channel : CacheManager.getChannels()) {
                        if (channel.getIdx().equals(offAsk[1])) {
                            LogUtils.log(offAsk[1]+"：射频关闭成功");
                            channel.setRFState(false);
                        }
                    }
                }

                EventAdapter.call(EventAdapter.RF_STATUS_LOC);
                EventAdapter.call(EventAdapter.RF_STATUS_RPT);
                EventAdapter.call(EventAdapter.REFRESH_DEVICE);
                break;

            case LTE_PT_PARAM.PARAM_SET_NAMELIST_ACK:
                String setNameListAsk = respContent;
                if (setNameListAsk.charAt(0) == '0') {
                    LogUtils.log("设置名单成功");
                } else if (setNameListAsk.charAt(0) == '1') {
                    LogUtils.log("设置名单失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_CHANGE_TAG_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("更新TAC成功");

                    ToastUtils.showMessage("更新TAC成功");
                } else if (respContent.charAt(0) == 1) {
                    LogUtils.log("更新TAC失败");
                    //ToastUtils.showMessage(GameApplication.appContext,"更新TAC失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_CHANNEL_CONFIG_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置通道成功");
                    ProtocolManager.getEquipAndAllChannelConfig();
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置通道失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_BLACK_NAMELIST_ACK:
                if (respContent.length() != 0) {  //如果黑名单为空，那么查回来的respcContent就为空
                    if (respContent.contains("#") || respContent.length() == 15) {  //程序里默认查询到就删掉，因为协议的问题，可能会会越积越多
                        LogUtils.log("查询到黑名单:" + respContent + " ,将其删除。");
                        ProtocolManager.setBlackList("3", "#" + respContent);
                    } else {
                        if (respContent.charAt(0) == '0') {
                            LogUtils.log("设置黑名单(中标)成功");
                        } else if (respContent.charAt(0) == '1') {
                            LogUtils.log("设置黑名单(中标)失败");
                        }
                    }
                }

                break;

            case LTE_PT_PARAM.PARAM_SET_RT_IMSI_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置上报黑名单(中标)开关成功");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置上报黑名单(中标)开关失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_CHANGE_BAND_ACK:
                if (respContent.charAt(0) == '0') {
                    //ToastUtils.showMessageLong(GameApplication.appContext,"下发切换Band命令成功，请等待设备重启。");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("切换band失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_ENB_CONFIG_ACK:
                if (respContent.charAt(0) == '0') {
                    ToastUtils.showMessageLong(R.string.set_cell_reboot);
                } else if (respContent.charAt(0) == '1') {
                    ToastUtils.showMessageLong(R.string.set_cell_fail);
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_FTP_CONFIG_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置ftp成功");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置ftp失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_FAN_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置风扇成功");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置风速失败失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_SET_SCAN_FREQ_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("下发搜网命令成功，结果稍后上报");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("扫频失败");
                }
                break;
            case LTE_PT_PARAM.PPARAM_SET_LOC_IMSI_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置定位号码成功");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置定位号码失败");
                }
                break;
            case LTE_PT_PARAM.PARAM_SET_ACTIVE_MODE_ACK:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("设置工作模式成功");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设置工作模式失败");
                }
                break;

            case LTE_PT_PARAM.PARAM_RPT_UPGRADE_STATUS:
                if (respContent.charAt(0) == '0') {
                    LogUtils.log("加载升级包成功，设备即将重启");
                    ToastUtils.showMessageLong("加载升级包成功，设备即将（约1分钟后）重启");
                } else if (respContent.charAt(0) == '1') {
                    LogUtils.log("设备获取升级包失败");
                    ToastUtils.showMessageLong("设备获取升级包失败");
                }

                EventAdapter.call(EventAdapter.UPGRADE_STATUS);
                break;
            default:
                break;
        }
    }

    //解析设备级配置
    private static LteEquipConfig praseEquipConfig(String equipConfig) {
        // SW:xxx@EXPIRED: 20171001@ SERVER1:10.10.10.100:10022
        // @ SERVER2:10.10.10.200:10022@BOARD:01030103383940@PROTOCOL:1.3.5@PRODUCT_TYPE:57@SYNC:gps
        String[] splitStr = equipConfig.split("@");
        LteEquipConfig config = new LteEquipConfig();

        for (int i = 0; i < splitStr.length; i++) {
            if (splitStr[i].split(":")[0].equals("HW")) {
                config.setHw(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("SW")) {
                config.setSw(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("EXPIRED")) {
                config.setExpired(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("SERVER1")) {
                config.setServer1(splitStr[i].split(":")[1] + ":" + splitStr[i].split(":")[2]);
            } else if (splitStr[i].split(":")[0].equals("SERVER2")) {
                config.setServer2(splitStr[i].split(":")[1] + ":" + splitStr[i].split(":")[2]);
            } else if (splitStr[i].split(":")[0].equals("BOARD")) {
                config.setBoard(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("PROTOCOL")) {
                config.setProtocol(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("PRODUCT_TYPE")) {
                config.setProtocolType(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("12v")) {
                config.setVoltage12V(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("28v")) {
                config.setVoltage28V(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("MIN_FAN")) {
                config.setMinFanSpeed(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("MAX_FAN")) {
                config.setMaxFanSpeed(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("FAN_TMPT")) {
                config.setTempThreshold(splitStr[i].split(":")[1]);
            }
        }

        return config;
    }

    //解析小区配置
    private static LteCellConfig praseCellConfig(String cellConfig) {
        // PCI:500,11:501,13:502,10|12:503,@GPS_OFFSET:-700,11:-700,13:-700,10|12:0,@SSP:SSP7@TAC_TIMER:120
        // @TAC_RANGE:500:20000@REJECT_PLMN:46000,15,38400:46011,15,1825:46001,15,1650:46002,15,38400
        String[] splitStr = cellConfig.split("@");
        LteCellConfig config = new LteCellConfig();

        for (int i = 0; i < splitStr.length; i++) {
            if (splitStr[i].split(":")[0].equals("PCI")) {
                String pci = "";
                String[] pciSplit = splitStr[i].split(":");
                for (int j = 1; j < pciSplit.length; j++) {
                    pci += pciSplit[j].split(",")[0];
                    pci += ",";
                }
                pci = pci.substring(0, pci.length() - 1);   //删除最后的,
                config.setPci(pci);
            } else if (splitStr[i].split(":")[0].equals("GPS_OFFSET")) {
                //700,60|70|71
                //-700,00|10|11
                //700,50|51
                String[] gpsOffsetSplit = splitStr[i].split(":");
                String gpsOffset = "";
                for (int j = 1; j < gpsOffsetSplit.length; j++) {
                    gpsOffset += gpsOffsetSplit[j].split(",")[0];
                    gpsOffset += ",";
                }
                gpsOffset = gpsOffset.substring(0, gpsOffset.length() - 1);   //删除最后的,
                config.setGpsOffset(gpsOffset);
            } else if (splitStr[i].split(":")[0].equals("SSP")) {
                config.setSsp(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("TAC_TIMER")) {
                config.setTacTimer(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("TAC_RANGE")) {
                config.setTacRange(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("REJECT_PLMN")) {
                config.setRejectPLMN(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("SYNC")) {
                if (splitStr[i].contains("air"))
                    config.setSync(splitStr[i].split(":")[1]);
                else
                    config.setSync(splitStr[i]);
            }
        }

        return config;
    }

    //解析通道配置
    private static LteChannelCfg praseChannelConfig(String channelConfig) {
        //IDX:60@BAND:38@FCN:37900@PLMN:46000@PA:-8@GA:20@PW:37.5@RLM:-106
        String[] splitStr = channelConfig.split("@");
        LteChannelCfg config = new LteChannelCfg();

        for (int i = 0; i < splitStr.length; i++) {
            if (splitStr[i].split(":")[0].equals("IDX")) {
                config.setIdx(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("BAND")) {
                config.setBand(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("FCN")) {
                config.setFcn(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("ALT_FCN")) {
                config.setAltFcn(splitStr[i].split(":").length > 1 ? splitStr[i].split(":")[1] : "");
            } else if (splitStr[i].split(":")[0].equals("PLMN")) {
                config.setPlmn(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("PA")) {
                config.setPa(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("GA")) {
                config.setGa(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("PW")) {
                config.setPw(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("RLM")) {
                config.setRlm(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("CHANGE")) {
                config.setChangeBand(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("AUTO_OPEN")) {
                config.setAutoOpen(splitStr[i].split(":")[1]);
            } else if (splitStr[i].split(":")[0].equals("MAX")) {
                config.setPMax(splitStr[i].split(":")[1]);
            }
        }

        return config;
    }

    //定位上报
    public static void processLocRpt(LTEReceivePackage receivePackage) {
        /* 1.定位过程中app异常重启的后，会有持续定位上报，其中包括了大量的设置的黑名单imsi,此代码块为解决此问题而加
           2.管控模式的号码强度也是从这里上报，要加以区分 */
        String locRpt = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(), 0);
        LogUtils.log("定位上报:" + locRpt);
        if (CacheManager.currentWorkMode.equals("0") && !CacheManager.getLocState()) {

            LogUtils.log("忽略此次srsp上报:" + locRpt);
            //CacheManager.stopCurrentLoc();
            return;
        }


        if ("".equals(locRpt))
            return;

        String SRSP = "";
        if (locRpt.contains("#")) {  /* 一条上报中含有多个IMSI */
            if (CacheManager.currentWorkMode.equals("0")) {
                if (CacheManager.getLocState()) {
                    String[] splitStr = locRpt.split("#");
                    List<UeidBean> listRpt = new ArrayList<>();
                    for (int i = 0; i < splitStr.length; i++) {
                        String[] split = splitStr[i].split(":");
                        if (split.length > 1) {
                            SRSP = split[1];
                            if (SRSP.equals(""))
                                continue;
                        } else {
                            SRSP = "";
                        }

                        String tmpImsi = splitStr[i].split(":")[0];
                        if (tmpImsi.equals(CacheManager.getCurrentLoction().getImsi()) && !TextUtils.isEmpty(SRSP)) {
                            EventAdapter.call(EventAdapter.LOCATION_RPT, SRSP);
                            //break;
                        }

                        if (isImsiExistInSimpleRpt(tmpImsi, listRpt)) {
                            continue;
                        }

                        listRpt.add(new UeidBean(tmpImsi, "", "", "",
                                DateUtils.convert2String(new Date(), DateUtils.LOCAL_DATE), "", ""));

                        if (!CacheManager.removeExistUeidInRealtimeList(tmpImsi)) {
                            UCSIDBManager.saveUeidToDB(tmpImsi, ImsiMsisdnConvert.getMsisdnFromLocal(tmpImsi), "",
                                    new Date().getTime(), "", "");
                        }
                    }

                    if (listRpt.size() > 0) {
                        EventAdapter.call(EventAdapter.UEID_RPT, listRpt);
                    }
                }
            } else if (CacheManager.currentWorkMode.equals("2")) {
                String[] splitStr = locRpt.split("#");
                for (int i = 0; i < splitStr.length; i++) {
                    String[] split = splitStr[i].split(":");
                    if (split.length > 1) {
                        SRSP = split[1];
                        if (SRSP.equals(""))
                            continue;

                        if (CacheManager.getLocState()) {
                            if (splitStr[i].split(":")[0].equals(CacheManager.getCurrentLoction().getImsi())) {
                                EventAdapter.call(EventAdapter.LOCATION_RPT, SRSP);
                                //break;
                            }
                        }
                    }


                    EventAdapter.call(EventAdapter.SHIELD_RPT, splitStr[i]);
                }
            }
        } else { /* 一条上报中只有一个IMSI */
            SRSP = locRpt.split(":")[1];
            if (SRSP.equals(""))
                return;

            if (CacheManager.currentWorkMode.equals("0")) {
                if (CacheManager.getLocState()) {
                    String imsi = locRpt.split(":")[0];


                    if (imsi.equals(CacheManager.getCurrentLoction().getImsi())) {
                        EventAdapter.call(EventAdapter.LOCATION_RPT, SRSP);
                    }

                    if (!CacheManager.removeExistUeidInRealtimeList(imsi)) {
                        UCSIDBManager.saveUeidToDB(imsi, ImsiMsisdnConvert.getMsisdnFromLocal(imsi), "",
                                new Date().getTime(), "", "");
                    }

                    EventAdapter.call(EventAdapter.UEID_RPT,
                            Arrays.asList(new UeidBean(imsi, "", "", "",
                                    DateUtils.convert2String(new Date(), DateUtils.LOCAL_DATE), "", "")));
                }
            } else if (CacheManager.currentWorkMode.equals("2")) {
                if (CacheManager.getLocState()) {
                    if (locRpt.split(":")[0].equals(CacheManager.getCurrentLoction().getImsi())) {
                        EventAdapter.call(EventAdapter.LOCATION_RPT, SRSP);
                    }
                }

                EventAdapter.call(EventAdapter.SHIELD_RPT, locRpt);
            }
        }
    }
}
