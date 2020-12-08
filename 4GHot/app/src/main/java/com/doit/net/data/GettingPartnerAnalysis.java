package com.doit.net.data;

import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.LogUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Zxc on 2018/12/11.
 */

public class GettingPartnerAnalysis {
    private static DbManager dbManager = UCSIDBManager.getDbManager();
    private static HashMap<String, List<Long>> mapImsiWithDetectTime = new HashMap<>();   //伴随每个IMSI时间
    //private static List<Long> listTargetImsiWithDetectTime = new ArrayList<>();   //目标IMSI出现时间

    public static HashMap<String, Integer> startGetPartnerAnalysis(String startTime, String endTime, String targetImsi, String deviation){
        List<DBUeidInfo> targetUeidInPeriod = new ArrayList<>();
        HashMap<Long, List<DBUeidInfo>> mapPeriodWithIMSI = new HashMap<>();
        HashMap<String, Integer> mapImsiWithTimes = new HashMap<>();
        long timeDev = Long.parseLong(deviation)*60*1000;

        mapImsiWithDetectTime.clear();

        //1.得到时间段内所有目标IMSI,并按照时间排序
        try {
            targetUeidInPeriod = dbManager.selector(DBUeidInfo.class)
                    .where("imsi", "=", targetImsi)
                    .and("createDate", "BETWEEN", new long[]{DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE),
                            DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)})
                    .orderBy("createDate", false).findAll();
        } catch (DbException e) {e.printStackTrace();}

        if (targetUeidInPeriod == null || targetUeidInPeriod.size() == 0){
            return null;
        }

        //2.遍历所有目标IMSI，获取每个时间点的时间差内所有IMSI放入map
        List<DBUeidInfo> imsiInDeviation = new ArrayList<>();
        long tmpLastTimePoint = 0;
        for (DBUeidInfo tmpUeid :targetUeidInPeriod ){
            LogUtils.log(tmpUeid.getCreateDate() + "  " + tmpLastTimePoint + "  " + timeDev);

            /* 考虑时间交叉 */
            if (tmpUeid.getCreateDate() < tmpLastTimePoint){
                LogUtils.log("1");
                continue;
            } else if ((tmpUeid.getCreateDate()-tmpLastTimePoint) >= 2*timeDev){
                LogUtils.log("2");
                try {
                    imsiInDeviation = dbManager.selector(DBUeidInfo.class)
                            .where("createDate", "BETWEEN",
                                    new long[]{tmpUeid.getCreateDate()-timeDev, tmpUeid.getCreateDate()+timeDev})
                            .orderBy("id", true).findAll();
                } catch (DbException e) {e.printStackTrace();}
            }else if ((tmpUeid.getCreateDate()-tmpLastTimePoint) < 2*timeDev){
                LogUtils.log("3");
                try {
                    imsiInDeviation = dbManager.selector(DBUeidInfo.class)
                            .where("createDate", "BETWEEN",
                                    new long[]{tmpLastTimePoint+timeDev, tmpUeid.getCreateDate()+timeDev})
                            .orderBy("id", true).findAll();
                } catch (DbException e) {e.printStackTrace();}
            }

            tmpLastTimePoint = tmpUeid.getCreateDate();

            mapPeriodWithIMSI.put(tmpUeid.getCreateDate(), imsiInDeviation);
        }

        LogUtils.log("mapPeriodWithIMSI:"+mapPeriodWithIMSI.size());

        //3.获取所有非重复的IMSI放入map，key为IMSI，value为次数
        for(List<DBUeidInfo> tmpList: mapPeriodWithIMSI.values()) {
            for(DBUeidInfo tmpImsi : tmpList){
                mapImsiWithTimes.put(tmpImsi.getImsi(), 0);
            }
        }

        //4.每个不重复的imsi再次遍历各个时间段对应的list，存在就+1
        for(String tmpIMSI : mapImsiWithTimes.keySet()) {
            List<Long> listDetectTime = new ArrayList<>();
            for(List<DBUeidInfo> tmpList: mapPeriodWithIMSI.values()) {
                //boolean hasAddToMapImsiWithTimes = false;   //一个时间段只加一次
                for (DBUeidInfo tmpDBUeid : tmpList){
                    if ((tmpDBUeid.getImsi().equals(tmpIMSI)) && (!listDetectTime.contains(tmpDBUeid.getCreateDate()))){
                        mapImsiWithTimes.put(tmpIMSI, mapImsiWithTimes.get(tmpIMSI)+1);
                        listDetectTime.add(tmpDBUeid.getCreateDate());
                        break;
                    }
                }
            }
            //同时记录每次出现的时间
            mapImsiWithDetectTime.put(tmpIMSI, listDetectTime);
        }
        //mapImsiWithTimes.put(targetImsi, targetUeidInPeriod.size());

        return mapImsiWithTimes;
    }

    private static List<DBUeidInfo> removeRepeatImsi(List<DBUeidInfo> listUeid) {
        List<DBUeidInfo>  listNoRepeatUeid = new ArrayList<>();
        boolean tmpIsExist = false;

        if (listUeid == null || listUeid.size() == 0)
            return listNoRepeatUeid;

        listNoRepeatUeid.add(listUeid.get(0));
        for (int i = 1; i < listUeid.size(); i++){
            tmpIsExist = false;
            for (int j = 0; i < listNoRepeatUeid.size(); i++){
                if (listUeid.get(i).getImsi().equals(listNoRepeatUeid.get(j).getImsi())){
                    tmpIsExist = true;
                    break;
                }
            }

            if (!tmpIsExist)
                listNoRepeatUeid.add(listUeid.get(i));
        }

        return listNoRepeatUeid;
    }

    public static List<String> getDetail(String imsi) {
        List<Long> listAllDetectTime = mapImsiWithDetectTime.get(imsi);
        List<String> stringAllDetectTime = new ArrayList<>();

        Collections.sort(listAllDetectTime, new Comparator<Long>() {
            public int compare(Long o1,  Long o2) {
                return (o2 > o1?1:-1);
            }
        });
        for (Long tmpTimeLong : listAllDetectTime){
            stringAllDetectTime.add(DateUtils.convert2String(tmpTimeLong, DateUtils.LOCAL_DATE));
        }

        return stringAllDetectTime;
    }
}
