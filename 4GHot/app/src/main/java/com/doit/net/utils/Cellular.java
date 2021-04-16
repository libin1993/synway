package com.doit.net.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.doit.net.application.MyApplication;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.ScanCellInfo;
import com.doit.net.protocol.LTESendManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zxc on 2019/6/25.
 */

public class Cellular {
    private static int maxArfcnCount = 0;  //一个文件里每个组合服务小区和邻区个数之和的最大值
    public static String file_fcns = "";  //测试显示用
    public static String final_fcns = "";  //测试显示用

    @SuppressLint("MissingPermission")
    private static List<List<ScanCellInfo>> getCellFromFile(){
        String ARFCN_FILE_PATH =  Environment.getExternalStorageDirectory()+"/CellularPro/logs/";
        TelephonyManager telManager = (TelephonyManager) MyApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);
         String imsi =  StringUtils.defaultIfBlank(telManager.getDeviceId(), "");
        String cellFileName = ARFCN_FILE_PATH + imsi+"-0.txt";
        //UtilBaseLog.printLog(cellFileName);
        List<List<ScanCellInfo>> listAllCellInfo = new ArrayList<>();
        //中国移动	460	00	S	B40	38950	356	-56	    -82    	 -5	     0	4421
        //中国移动	460	00	N	B40	39148	357	-97.62	-124.19	 -17.5   1	4421
        File namelistFile = new File(cellFileName);
        if (namelistFile.exists()){
            maxArfcnCount = 0;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(namelistFile));
                String readline = "";
                int tmpCnt = 0;
                List<ScanCellInfo> tmpCellInfo = new ArrayList<>();
                while ((readline = bufferedReader.readLine()) != null) {
                    if (StringUtils.getChrCount(readline, "\t") != 11)
                        continue;

                    if (readline.split("\t")[3].equals("S")){
                        if (tmpCellInfo.size() > 0)
                            listAllCellInfo.add(0, tmpCellInfo);  //最新的放最前面

                        if (tmpCnt > maxArfcnCount){
                            maxArfcnCount = tmpCnt;
                        }

                        tmpCnt = 1;
                        tmpCellInfo = new ArrayList<>();
                        tmpCellInfo.add(new ScanCellInfo(readline.split("\t")[4],
                                readline.split("\t")[5], true));
                    }else{
                        tmpCnt ++;
                        tmpCellInfo.add(new ScanCellInfo(readline.split("\t")[4],
                                readline.split("\t")[5], false));
                    }
                }

                if (tmpCnt > maxArfcnCount)
                    maxArfcnCount = tmpCnt;
                listAllCellInfo.add(tmpCellInfo);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {}
                }
            }

        }

        cellFileName = ARFCN_FILE_PATH + imsi+"-1.txt";
        //UtilBaseLog.printLog(cellFileName);
        namelistFile = new File(cellFileName);
        if (namelistFile.exists()){
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(namelistFile));
                String readline = "";
                int tmpCnt = 0;
                List<ScanCellInfo> tmpCellInfo = new ArrayList<>();
                while ((readline = bufferedReader.readLine()) != null) {
                    if (StringUtils.getChrCount(readline, "\t") != 11)
                        continue;

                    if (readline.split("\t")[3].equals("S")){
                        if (tmpCellInfo.size() > 0)
                            listAllCellInfo.add(0, tmpCellInfo);  //最新的放最前面

                        if (tmpCnt > maxArfcnCount){
                            maxArfcnCount = tmpCnt;
                        }

                        tmpCnt = 1;
                        tmpCellInfo = new ArrayList<>();
                        tmpCellInfo.add(new ScanCellInfo(readline.split("\t")[4],
                                readline.split("\t")[5], true));
                    }else{
                        tmpCnt ++;
                        tmpCellInfo.add(new ScanCellInfo(readline.split("\t")[4],
                                readline.split("\t")[5], false));
                    }
                }

                if (tmpCnt > maxArfcnCount)
                    maxArfcnCount = tmpCnt;
                listAllCellInfo.add(tmpCellInfo);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {}
                }
            }
        }


        return listAllCellInfo;
    }

    private static boolean isArfcnsSame(String arfcns1, String arfcns2){
        if (StringUtils.getChrCount(arfcns1,",") != 2 || StringUtils.getChrCount(arfcns2,",") != 2)
            return false;

        String[] split1 = arfcns1.split(",");
        return arfcns2.contains(split1[0]) && arfcns2.contains(split1[1]) && arfcns2.contains(split1[2]);
    }


    public static void adjustArfcnPwrForLocTarget(String locImsi) {
        if ("".equals(locImsi)){
            return;
        }



        String band1FromFile = "";   //xxx,xxx,xxx
        String band3FromFile = "";
        String band38FromFile = "";
        String band39FromFile = "";
        String band40FromFile = "";
        String band41FromFile = "";
        String band1Config = "";
        String band3Config = "";
        String band38Config = "";
        String band39Config = "";
        String band40Config = "";


        /* 1.从文件里提取信息 */
        List<List<ScanCellInfo>> listAllCellInfo = getCellFromFile();
        if (listAllCellInfo == null || listAllCellInfo.size() == 0){
            return;
        }

        /* 2.从list里提取信息到各个band的string */
        //UtilBaseLog.printLog("最大主频+邻区："+ maxArfcnCount);
        List<ScanCellInfo> listTmpCellInfo;
        for (int j = 0; j < maxArfcnCount; j++) {
            for (int i = 0; i < listAllCellInfo.size(); i++) {
                listTmpCellInfo = listAllCellInfo.get(i);
                if(listTmpCellInfo.size() > j){
                    if (listTmpCellInfo.get(j).getBand().equals("B1") && !band1FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band1FromFile, ",") < 3)) {
                        band1FromFile += listTmpCellInfo.get(j).getArfcn();
                        band1FromFile += ",";
                    }else if (listTmpCellInfo.get(j).getBand().equals("B3") && !band3FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band3FromFile, ",") < 3)){
                        band3FromFile += listTmpCellInfo.get(j).getArfcn();
                        band3FromFile += ",";
                    }else if (listTmpCellInfo.get(j).getBand().equals("B38") && !band38FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band38FromFile, ",") < 3)){
                        band38FromFile += listTmpCellInfo.get(j).getArfcn();
                        band38FromFile += ",";
                    }else if (listTmpCellInfo.get(j).getBand().equals("B39") && !band39FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band39FromFile, ",") < 3)){
                        band39FromFile += listTmpCellInfo.get(j).getArfcn();
                        band39FromFile += ",";
                    }else if (listTmpCellInfo.get(j).getBand().equals("B40") && !band40FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band40FromFile, ",") < 3)){
                        band40FromFile += listTmpCellInfo.get(j).getArfcn();
                        band40FromFile += ",";
                    }else if (listTmpCellInfo.get(j).getBand().equals("B41") && !band41FromFile.contains(listTmpCellInfo.get(j).getArfcn())
                            && (StringUtils.getChrCount(band41FromFile, ",") < 3)){
                        band41FromFile += listTmpCellInfo.get(j).getArfcn();
                        band41FromFile += ",";
                    }
                }
            }
        }

        //打印测试
        file_fcns = "文件获取频点值B1:"+ band1FromFile + "B3:"+band3FromFile + "B38:"+ band38FromFile+"B39:"+ band39FromFile + "B40:" + band40FromFile;
        LogUtils.log(file_fcns);

        //删除最后的“，”
        removeCommaInTail(band1FromFile);
        removeCommaInTail(band3FromFile);
        removeCommaInTail(band38FromFile);
        removeCommaInTail(band39FromFile);
        removeCommaInTail(band40FromFile);
        removeCommaInTail(band41FromFile);

        //常用频点
        String band1CommonFcns = "100,375,400";
        String band3CommonFcns = "1825,1650,1506";
        String band38CommonFcns = "37900,38098,38200";
        String band39CommonFcns = "38400,38544,38300";
        String band40CommonFcns = "38950,39148,39300";
        String band41CommonFcns = band38CommonFcns;

        String locTargetOpr = UtilOperator.getOperatorName(locImsi);
        if (locTargetOpr.equals("CTJ")){
            for(LteChannelCfg channel: CacheManager.getChannels()) {
                if (channel.getBand().equals("3")){
                    //band3必须含有1300,但不能有1825
//                    if (!band3FromFile.contains("1300"))
//                        band1CommonFcns = "1300,"+band3FromFile;
//
//                    if (band3FromFile.contains("1825")){
//                        band3FromFile.replaceAll("1825,", "");
//                        band3FromFile.replaceAll(",1825", "");
//                    }

                    //改为直接写死，并只打开1300,直接设置不做策略
                    LTESendManager.setChannelConfig(channel.getIdx(), "1300,1506,1650", "",
                            getMaxPwr(channel, 1)+",-89,-89", "", "","","");
                    generalCfgAndSet(locTargetOpr,band3FromFile, band3CommonFcns, channel);
                }else if (channel.getBand().equals("38")){  //配套手机插的是联通电信卡，理论上不会走到以下分支，但为保持扩展性先写上
                    generalCfgAndSet(locTargetOpr,band38FromFile, band38CommonFcns, channel);
                }else if (channel.getBand().equals("39")){
                    generalCfgAndSet(locTargetOpr,band39FromFile, band39CommonFcns, channel);
                }else if (channel.getBand().equals("40")){
                    generalCfgAndSet(locTargetOpr,band40FromFile, band40CommonFcns, channel);
                }else if (channel.getBand().equals("41")){
                    generalCfgAndSet(locTargetOpr,band41FromFile, band41CommonFcns, channel);
                }
            }
        }else if (locTargetOpr.equals("CTU")){
            for(LteChannelCfg channel: CacheManager.getChannels()) {
                if (channel.getBand().equals("1")){
                    generalCfgAndSet(locTargetOpr,band1FromFile, band1CommonFcns, channel);
                }else if (channel.getBand().equals("3")){
                    generalCfgAndSet(locTargetOpr,band3FromFile, band3CommonFcns, channel);
                }
            }
        }else if (locTargetOpr.equals("CTC")){
            for(LteChannelCfg channel: CacheManager.getChannels()) {
                if (channel.getBand().equals("1")){
                    generalCfgAndSet(locTargetOpr,band1FromFile, band1CommonFcns, channel);
                }else if (channel.getBand().equals("3")){
                    generalCfgAndSet(locTargetOpr,band3FromFile, band3CommonFcns, channel);
                }
            }
        }
    }

    private static void removeCommaInTail(String str) {
        if("".equals(str) || !str.contains(",") || (str.lastIndexOf(",") != str.length()-1))
            return;

        str = str.substring(0, str.length()-1);
    }

    private static void generalCfgAndSet(String targetOpr, String bandInfo, String bandCommonFcns, LteChannelCfg channel) {
        if (!targetOpr.equals("CTJ") && !targetOpr.equals("CTU") && !targetOpr.equals("CTC")){
            LogUtils.log("generalCfgAndSet fail, error targetOpr");
            return;
        }

        if (channel.getPMax().equals("")){
            LogUtils.log("generalCfgAndSet fail, error pmax");
            return;
        }

        String[] arfcns = bandInfo.split(",");
        if (arfcns == null || arfcns.length == 0){
            if (!bandCommonFcns.equals("") && bandCommonFcns.split(",").length == 3){
                LTESendManager.setChannelConfig(channel.getIdx(), bandCommonFcns, "",
                        channel.getPMax()+","+channel.getPMax()+","+channel.getPMax(), "", "","","");
            }
        }else{
            int effectArfcnNum = 0;
            String effectArfcns = "";
            String arfcnCfgContent = "";

            for (int i = 0; i < arfcns.length; i++) {
                if (UtilOperator.isArfcnInOperator(targetOpr,arfcns[i])){
                    effectArfcnNum++;
                    effectArfcns += arfcns[i];
                    effectArfcns += ",";

                }

                if (effectArfcnNum >= 3)
                    break;
            }

            //没有目标运营商的频点就直接跳过了
            if(effectArfcnNum == 0)
                return;

            //补充频点
            if (effectArfcnNum < 3){
                effectArfcns += bandCommonFcns;
                String[] tmpSplitFcn = effectArfcns.split(",");
                for (int i = 0; i < tmpSplitFcn.length; i++){
                    if ((!arfcnCfgContent.contains(tmpSplitFcn[i])) && (StringUtils.getChrCount(arfcnCfgContent, ",") < 3)){
                        arfcnCfgContent += tmpSplitFcn[i];
                        arfcnCfgContent += ",";
                    }
                }
            }

            switch (effectArfcnNum){
                case 1:
                    LTESendManager.setChannelConfig(channel.getIdx(), arfcnCfgContent.substring(0, arfcnCfgContent.length()-1), "",
                            getMaxPwr(channel, 1)+",-89,-89", "", "","","");
                    break;
                case 2:
                    LTESendManager.setChannelConfig(channel.getIdx(), arfcnCfgContent.substring(0, arfcnCfgContent.length()-1), "",
                            getMaxPwr(channel, 2)+","+getMaxPwr(channel, 2)+",-89", "", "","","");
                    break;
                case 3:
                    LTESendManager.setChannelConfig(channel.getIdx(), arfcnCfgContent.substring(0, arfcnCfgContent.length()-1), "",
                            channel.getPMax()+","+channel.getPMax()+","+channel.getPMax(), "", "","","");
                    break;
                default:
                    break;
            }
        }
    }

    /*
    *  在三个全开pmax的情况下，功率都为15dbm
    *  单载波时可在pmax上+5，达到20dbm （Band38和Band40只能+1，达到16dbm）
    *  双载波可在pmax上+2（Band38和Band40不能再加，只能三个开15dbm）
    * */
    private static String getMaxPwr(LteChannelCfg channel, int effectCarrierCount) {
        if (effectCarrierCount == 1){
            if (channel.getBand().equals("38") || channel.getBand().equals("40") || channel.getBand().equals("41")){
                return String.valueOf(Integer.parseInt(channel.getPMax())+1);
            }else{
                return String.valueOf(Integer.parseInt(channel.getPMax())+5);
            }
        }else if (effectCarrierCount == 2){
            if (channel.getBand().equals("38") || channel.getBand().equals("40") || channel.getBand().equals("41")){
                return channel.getPMax();
            }else{
                return String.valueOf(Integer.parseInt(channel.getPMax())+1);
            }
        }

        return channel.getPMax();
    }
}
