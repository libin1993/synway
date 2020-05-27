package com.doit.net.Protocol;

import android.text.TextUtils;

import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.DBChannel;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.NetWorkUtils;
import com.doit.net.application.MyApplication;
import com.doit.net.bean.FtpConfig;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.LogUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wiker on 2017-06-25.
 */

public class ProtocolManager {
    private static final String HOLD_VALUE = "0";  //设备是否保存配置：“0”不保存，“1”保存


    public static void setNamelist(String redirectList, String rejectList, String blockList, String releaseList, String redirectRejectList, String restAction) {
        //MODE:[on|off]
        // @REDIRECT_CONFIG:46000,4,38400#46001,4,300#46011,4,100#46002,2,98  //重定向
        // @NAMELIST_REJECT:460001234512345,460011234512345   //拒绝
        // @NAMELIST_REDIRECT:460001234512345,460011234512345 //重定向再回公网
        // @NAMELIST_BLOCK:460001234512345,460011234512345   //吸附
        // @NAMELIST_RELEASE:460001233332345,460011235452345   //release
        // @NAMELIST_REST_ACTION:block  //其余手机操作

        String namelist = "";
        namelist += "MODE:on";

        namelist += "@REDIRECT_CONFIG:";
        if (!"".equals(redirectList)) {
            namelist += redirectList;
        }

        namelist += "@NAMELIST_REJECT:";
        if (!"".equals(rejectList)) {
            namelist += rejectList;
        }

        namelist += "@NAMELIST_REDIRECT:";
        if (!"".equals(redirectRejectList)) {
            namelist += redirectRejectList;
        }

        namelist += "@NAMELIST_BLOCK:";
        if (!"".equals(blockList)) {
            namelist += blockList;
        }

        namelist += "@NAMELIST_RELEASE:";
        if (!"".equals(releaseList)) {
            namelist += releaseList;
        }

        namelist += "@NAMELIST_REST_ACTION:";
        if (!"".equals(restAction)) {
            namelist += restAction;
        }

        LogUtils.log(namelist);
        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_NAMELIST, namelist);
    }

    public static void getEquipAndAllChannelConfig() {
        LogUtils.log("获取设备配置");
        LTE_PT_PARAM.queryCommonParam(LTE_PT_PARAM.PARAM_GET_ENB_CONFIG);
    }

    public static void setNowTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        LogUtils.log("当前时间：" + sdf.format(d));

        LTE_PT_SYSTEM.setSystemParam(LTE_PT_SYSTEM.SYSTEM_SET_DATETIME, sdf.format(d));
    }

    public static void changeTac() {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        //UtilBaseLog.printLog("下发更新TAC");

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_CHANGE_TAG, "");
    }

    public static void setCellConfig(String gpsOffset, String pci, String tacPeriod, String sync) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        String configContent = "";
        if (!"".equals(pci)) {
            configContent += "@PCI:";
            configContent += pci.replaceAll(",", ":");
        }
        if (!"".equals(gpsOffset)) {
            configContent += "@GPS_OFFSET:";
            configContent += gpsOffset.replaceAll(",", ":");
        }

        if (!"".equals(tacPeriod)) {
            configContent += "@TAC_TIMER:";
            configContent += tacPeriod;
        }

        if (!"".equals(sync)) {
            configContent += "@SYNC:";
            configContent += sync;
        }

        //删掉最开始的@
        configContent = configContent.replaceFirst("@", "");

        LogUtils.log("set cell config:" + configContent);
        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_ENB_CONFIG, configContent);
        //EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.SET_CELL_CONFIG + configContent);
    }

    public static void reboot() {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LTE_PT_SYSTEM.commonSystemMsg(LTE_PT_SYSTEM.SYSTEM_REBOOT);
        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.REBOOT_DEVICE);
    }

    public static void changeBand(String idx, String changeBand) {
        String content = "IDX:";
        content += idx;
        content += "@BAND:";
        content += changeBand;

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_CHANGE_BAND, content);
    }

    public static void setDetectCarrierOpetation(String carrierOpetation) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        String plnmValue = "46000,46001,46011";
        if (carrierOpetation.equals("detect_ctj")) {
            plnmValue = "46000,46000,46000";
        } else if (carrierOpetation.equals("detect_ctu")) {
            plnmValue = "46001,46001,46001";
        } else if (carrierOpetation.equals("detect_ctc")) {
            plnmValue = "46011,46011,46011";
        }

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            setChannelConfig(channel.getIdx(), "", plnmValue, "", "", "", "", "");
        }
    }

    public static void setChannelConfig(String idx, String fcn, String plmn, String pa,
                                        String ga, String rxlevMin, String atuoOpenRF, String AltFcn) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        if ("".equals(idx))
            return;


        String configContent = "IDX:";
        configContent += idx;

        if (!"".equals(plmn)) {
            configContent += "@PLMN:";
            configContent += plmn;
        }

        if (!"".equals(fcn)) {
            configContent += "@FCN:";
            configContent += fcn;
        }

        if (!"".equals(pa)) {
            configContent += "@PA:";
            configContent += pa;
            //configContent += checkPa(idx, pa);
        }

        if (!"".equals(ga)) {
            configContent += "@GA:";
            configContent += ga;
        }

        if (!"".equals(rxlevMin)) {
            configContent += "@RLM:";
            configContent += rxlevMin;
        }


        if (!"".equals(atuoOpenRF)) {
            configContent += "@AUTO_OPEN:";
            configContent += atuoOpenRF;
        }

        if (!"".equals(AltFcn)) {
            configContent += "@ALT_FCN:";
            configContent += AltFcn;
        }

//        configContent += "@HOLD:";
//        configContent += HOLD_VALUE;


        LogUtils.log("设置通道: " + configContent);

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_CHANNEL_CONFIG, configContent);
        //BlackBoxManger.recordOperation(BlackBoxManger.SET_CHANNEL_CONFIG+configContent);
    }

    //加入定位关闭非目标运营商频点加大目标运营商频点功率策略之后，
    //这个功率判定方法不再好用，就暂时不做对功率限制做判定了
    private static String checkPa(String idx, String ga) {
        String band = getBandByIdx(idx);
        String returnGa = "";

        String[] tmpGa = ga.split(",");
        if (tmpGa == null || tmpGa.length != 3) {
            LogUtils.log("len " + tmpGa.length);
            return ga;
        }


        if (band.equals("1") || band.equals("3")) {
            for (int i = 0; i < tmpGa.length; i++) {
                if (Integer.valueOf(tmpGa[i]) > -7) {
                    returnGa += -7;
                } else {
                    returnGa += tmpGa[i];
                }
                returnGa += ",";
            }
        } else if (band.equals("38") || band.equals("40") || band.equals("41")) {
            for (int i = 0; i < tmpGa.length; i++) {
                if (Integer.valueOf(tmpGa[i]) > -1) {
                    returnGa += -1;
                } else {
                    returnGa += tmpGa[i];
                }
                returnGa += ",";
            }
        } else if (band.equals("39")) {
            for (int i = 0; i < tmpGa.length; i++) {
                if (Integer.valueOf(tmpGa[i]) > -13) {
                    returnGa += -13;
                } else {
                    returnGa += tmpGa[i];
                }
                returnGa += ",";
            }
        }

        return returnGa.substring(0, returnGa.length() - 1);
    }

    private static String getBandByIdx(String idx) {
        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getIdx().equals(idx)) {
                return channel.getBand();
            }
        }

        return "";
    }

    public static void setBlackList(String operation, String content) {
//        if(!CacheManager.isDeviceOk()){
//            return;
//        }

        //operation 1查询  2添加 3删除
        String configContent = operation + content;

        LogUtils.log("设置黑名单(中标)号码: " + configContent);

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_BLACK_NAMELIST, configContent);
    }

    //这个协议是用于rpt_rt_imsi而不是rpt_black_name
    public static void setRTImsi(boolean onOff) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LogUtils.log("设置是否上报中标号码: " + (onOff ? "1" : "0"));

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_RT_IMSI, onOff ? "1" : "0");
    }


    public static void openAllRf() {
        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (!channel.getRFState()) {
                openRf(channel.getIdx());
            }
        }
    }

    public static void openRf(String idx) {
        //UtilBaseLog.printLog("打开射频："+idx);
        //LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_CHANNEL_ON, idx+"@HOLD:"+HOLD_VALUE);
        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_CHANNEL_ON, idx);
        //setChannelConfig(idx,null,null,null,null, FlagConstant.RF_OPEN,null);
    }

    public static void closeRf(String idx) {
        LogUtils.log("关闭射频：" + idx);
        //LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_CHANNEL_OFF, idx+"@HOLD:"+HOLD_VALUE);
        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_CHANNEL_OFF, idx);
    }

    public static void closeAllRf() {
        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                closeRf(channel.getIdx());
            }

        }
    }

    public static void setLocImsi(String imsi) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LogUtils.log("下发开始定位：" + imsi);

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_LOC_IMSI, imsi);
    }


    public static void setActiveMode(String mode) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_ACTIVE_MODE, mode);
    }

    /**
     * 设置ftp
     */
    public static void setFTPConfig() {

        String configContent = NetWorkUtils.getWIFILocalIpAddress(MyApplication.mContext)
                + "#"
                + FtpConfig.ftpUser
                + "#"
                + FtpConfig.ftpPassword
                + "#"
                + FtpConfig.ftpPort
                + "#"
                + FtpConfig.ftpTimer
                + "#"
                + FtpConfig.ftpMaxSize;

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_FTP_CONFIG, configContent);
    }

    /**
     * 设置默认配置
     */
    public static void setDefaultArfcnsAndPwr() {
        String tmpArfcnConfig = "";
        String defaultGa = "";
        String defaultPower = "-7,-7,-7";
        String band1Fcns = "100,375,400";
        String band3Fcns = "1825,1650,1506";//1300
        String band38Fcns = "37900,38098,38200";
        String band39Fcns = "38400,38544,38300";
        String band40Fcns = "38950,39148,39300";
        String band41Fcns = "37900,38098,38200";
        String tmpAllFcns = "";
        String pMax = "";

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            //2019.9.12讨论不再使用过滤筛选方式，直接使用固定常用频点
            switch (channel.getBand()) {
                case "1":
//                    tmpAllFcns = channel.getFcn() + "," + band1Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if ((!tmpArfcnConfig.contains(tmpSplitFcn[i])) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }

                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-7,-7,-7";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }

                    tmpArfcnConfig = band1Fcns;

                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band1Fcns);
                    String fcn1 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn1)) {
                        tmpArfcnConfig = fcn1;
                    }


                    //tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                case "3":
//                    tmpAllFcns = channel.getFcn() + "," + band3Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if (!tmpArfcnConfig.contains(tmpSplitFcn[i]) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }
                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-7,-7,-7";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }

                    //tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    tmpArfcnConfig = band3Fcns;
                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band3Fcns);
                    String fcn3 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn3)) {
                        tmpArfcnConfig = fcn3;
                    }


                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                case "38":
//                    tmpAllFcns = channel.getFcn() + "," + band38Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if (!tmpArfcnConfig.contains(tmpSplitFcn[i]) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }
                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-1,-1,-1";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }

                    //tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    tmpArfcnConfig = band38Fcns;
                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band38Fcns);
                    String fcn38 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn38)) {
                        tmpArfcnConfig = fcn38;
                    }


                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                case "39":
//                    tmpAllFcns = channel.getFcn() + "," + band39Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if (!tmpArfcnConfig.contains(tmpSplitFcn[i]) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }
                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-13,-13,-13";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }

                    //tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    tmpArfcnConfig = band39Fcns;
                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band39Fcns);
                    String fcn39 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn39)) {
                        tmpArfcnConfig = fcn39;
                    }


                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                case "40":
//                    tmpAllFcns = channel.getFcn() + "," + band40Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if (!tmpArfcnConfig.contains(tmpSplitFcn[i]) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }
                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-1,-1,-1";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }
                    ///tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    tmpArfcnConfig = band40Fcns;
                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band40Fcns);
                    String fcn40 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn40)) {
                        tmpArfcnConfig = fcn40;
                    }


                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                case "41":
//                    tmpAllFcns = channel.getFcn() + "," + band38Fcns;
//                    tmpSplitFcn = tmpAllFcns.split(",");
//                    for (int i = 0; i < tmpSplitFcn.length; i++){
//                        if (!tmpArfcnConfig.contains(tmpSplitFcn[i]) && (getCharCount(tmpArfcnConfig, ",") < 3)){
//                            tmpArfcnConfig += tmpSplitFcn[i];
//                            tmpArfcnConfig += ",";
//                        }
//                    }

                    pMax = channel.getPMax();
                    if ("".equals(pMax)) {
                        defaultPower = "-1,-1,-1";
                    } else {
                        defaultPower = pMax + "," + pMax + "," + pMax;
                    }

                    //tmpArfcnConfig = tmpArfcnConfig.substring(0, tmpArfcnConfig.length()-1);
                    tmpArfcnConfig = band41Fcns;
                    saveDefaultFcn(channel.getIdx(), channel.getBand(), band41Fcns);
                    String fcn41 = getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn41)) {
                        tmpArfcnConfig = fcn41;
                    }

                    if (Integer.parseInt(channel.getGa()) <= 8)
                        defaultGa = String.valueOf(Integer.parseInt(channel.getGa()) * 5);
                    break;

                default:
                    break;
            }

            if ("".equals(tmpArfcnConfig)) {
                setChannelConfig(channel.getIdx(), "", "", "", defaultGa, "", "", "");
            } else {
                setChannelConfig(channel.getIdx(), tmpArfcnConfig, "", "", defaultGa, "", "", "");
            }

            LogUtils.log("默认fcn:" + tmpArfcnConfig);

            //setChannelConfig(channel.getIdx(),tmpArfcnConfig, "","", "", "","","");
            tmpArfcnConfig = "";
            defaultPower = "";
            defaultGa = "";
        }
    }

    /**
     * 获取选中fcn
     */
    private static String getCheckedFcn(String band) {
        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            DBChannel channel = dbManager.selector(DBChannel.class)
                    .where("band", "=", band)
                    .and("is_check", "=", 1)
                    .findFirst();
            if (channel != null) {
                return channel.getFcn();
            }

        } catch (DbException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * @param band
     * @param fcn  存入默认fcn
     */
    private static void saveDefaultFcn(String idx, String band, String fcn) {
        try {
            DbManager dbManager = UCSIDBManager.getDbManager();
            DBChannel channel = dbManager.selector(DBChannel.class)
                    .where("band", "=", band)
                    .and("fcn", "=", fcn)
                    .findFirst();
            if (channel == null) {
                dbManager.save(new DBChannel(idx, band, fcn, 1, 1));
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void setFancontrol(String maxFanSpeed, String minFanSpeed, String tempThreshold) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        String configContent = "";

        if (!"".equals(minFanSpeed)) {
            configContent += "MIN_FAN:";
            configContent += minFanSpeed;
        }

        if (!"".equals(maxFanSpeed)) {
            configContent += "@MAX_FAN:";
            configContent += maxFanSpeed;
        }

        if (!"".equals(tempThreshold)) {
            configContent += "@FAN_TMPT:";
            configContent += tempThreshold;
        }

        LogUtils.log("设置风扇控制 " + configContent);

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_FAN, configContent);
    }

    public static void setAutoRF(boolean onOff) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }
        String ifAutoOpen = onOff ? "1" : "0";

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            setChannelConfig(channel.getIdx(), "", "", "", "", "", ifAutoOpen, "");
        }
    }

    public static void scanFreq() {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LTE_PT_PARAM.setCommonParam(LTE_PT_PARAM.PARAM_SET_SCAN_FREQ, "");
    }

    public static void systemUpgrade(String upgradeCommand) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        LTE_PT_SYSTEM.setSystemParam(LTE_PT_SYSTEM.SYSTEM_UPGRADE, upgradeCommand);
    }
}
