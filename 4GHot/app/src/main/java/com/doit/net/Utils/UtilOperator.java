package com.doit.net.Utils;

import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Model.CacheManager;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Zxc on 2019/3/22.
 */

public class UtilOperator {
    private static List<String> listFcnsInCTJ = Arrays.asList("1300","37900", "38098", "38300", "38400", "38544",
            "38950", "39148", "39300", "38200");
    private static List<String> listFcnsInCTU = Arrays.asList("375", "400", "450", "500",  "1506","1650");
    private static List<String> listFcnsInCTC = Arrays.asList("100", "1825");


    public static String getOperatorName(String plmn){
        if(plmn == null){
            return "";
        }
        if (plmn.startsWith("46000") || plmn.startsWith("46002") ||
            plmn.startsWith("46007") || plmn.startsWith("46004")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
            // 中国移动
            return "CTJ";
        } else if (plmn.startsWith("46001") || plmn.startsWith("46006") || plmn.startsWith("46009")) {
            // 中国联通
            return "CTU";
        } else if (plmn.startsWith("46003") || plmn.startsWith("46005") || plmn.startsWith("46011")) {
            // 中国电信
            return "CTC";
        }
        return "";
    }

    public static String getOperatorNameCH(String plmn){
        if(plmn == null){
            return "";
        }
        if (plmn.startsWith("46000") || plmn.startsWith("46002") ||
                plmn.startsWith("46007") || plmn.startsWith("46004")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
            // 中国移动
            return "移动";
        } else if (plmn.startsWith("46001") || plmn.startsWith("46006") || plmn.startsWith("46009")) {
            // 中国联通
            return "联通";
        } else if (plmn.startsWith("46003") || plmn.startsWith("46005") || plmn.startsWith("46011")) {
            // 中国电信
            return "电信";
        }
        return "??";
    }

    /* 判断频点是否属于目标制式内*/
    public static boolean isArfcnInOperator(String operator, String fcn) {
        if ("".equals(fcn))
            return false;

        int intFcn = Integer.parseInt(fcn);
        if (operator.equals("CTJ")){
            return (rangeInDefined(intFcn, 38250, 38600) ||
                    rangeInDefined(intFcn, 38850, 39350) ||
                    rangeInDefined(intFcn, 40440, 41040) || intFcn == 1300);
        }else if (operator.equals("CTU")){   //联通暂不考虑TDD频段,并包括了B3的DCS
            return (rangeInDefined(intFcn, 1350, 1750) ||
                    rangeInDefined(intFcn, 350, 599));
        }else if (operator.equals("CTC")){
            return (rangeInDefined(intFcn, 1750, 1900) ||
                    rangeInDefined(intFcn, 0, 200)||
                    rangeInDefined(intFcn, 41040, 41240));
        }

        return true;
    }


    //根据imsi的制式，关闭所有其他制式的频点以加大目标制式频点的功率
    public static  void checkPower(String imsi) {
        //1.根据imsi获取制式
        String operator = UtilOperator.getOperatorName(imsi);
        if ("".equals(operator))
            return;

        //2.遍历所有band,再遍历每个band里的每个频点，若属于目标制式的里的频点，
        //  就将其功率设到最大，否则设置到最小
        String[] tmpFcns;
        String tmpPa = "";
        for (LteChannelCfg tmpCfg : CacheManager.getChannels()){
            tmpFcns = tmpCfg.getFcn().split(",");
            for (int i = 0; i < tmpFcns.length; i++){
                if (UtilOperator.isArfcnInOperator(operator, tmpFcns[i])){
                    tmpPa += "-5,";
                }else{
                    tmpPa += "-80,";
                }
            }
            ProtocolManager.setChannelConfig(tmpCfg.getIdx(), "", "", tmpPa.substring(0, tmpPa.length()-1),"", "","", "");
            tmpPa = "";
        }
    }

    private static boolean rangeInDefined(int current, int min, int max) {
        return Math.max(min, current) == Math.min(current, max);
    }

    public static int getBandByFcn(int fcn) {
        if (rangeInDefined(fcn, 0, 599)) {
            return 1;
        } else if (rangeInDefined(fcn, 1200, 1949)) {
            return 3;
        } else if (rangeInDefined(fcn, 37750, 38250)) {
            return 38;
        }else if (rangeInDefined(fcn, 38250, 38650)){
            return 39;
        }else if (rangeInDefined(fcn, 38650, 39650)){
            return 40;
        }else if (rangeInDefined(fcn, 39650, 41589))
            return 41;

        return -1;
    }
}
