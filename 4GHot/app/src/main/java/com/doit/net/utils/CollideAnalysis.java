package com.doit.net.utils;

import com.doit.net.bean.CollideTimePeriodBean;
import com.doit.net.bean.DBUeidInfo;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Zxc on 2018/5/22.
 */

public class CollideAnalysis {
    //时间段碰撞
    private static HashMap<String, List<Long>> mapCollideDetectTime = new HashMap<>();

    /* 打点碰撞 */
    private static HashMap<String, List<Long>> mapTPCollideDetectTime = new HashMap<>();

    /* 实时碰撞 */
    private static final int UNREPEAT_TIME = 3*60*1000;  //去重时间
    private static HashMap<String, List<Long>> mapRTCollideDetectTime = new HashMap<>();

    //时间段碰撞
    public static HashMap<String, Integer> collideAnalysis(List<CollideTimePeriodBean> listCollideTimePeriod){
        HashMap<String, Integer> mapImsiWithTimes = new HashMap<>();
        List<List<DBUeidInfo>> listCollideTimePeriodIMSI = new ArrayList<>();
        mapCollideDetectTime.clear();

        //0. 获取各个时间段对应的IMSI
        List<DBUeidInfo> listUeidInPeriod = new ArrayList<>();
        for (CollideTimePeriodBean collideTimePeriodBean : listCollideTimePeriod) {
            try {
                listUeidInPeriod = UCSIDBManager.getDbManager().selector(DBUeidInfo.class)
                        .where("createDate", "BETWEEN",
                                new long[]{DateUtils.convert2long(collideTimePeriodBean.getStartTime(), DateUtils.LOCAL_DATE),
                                        DateUtils.convert2long(collideTimePeriodBean.getEndTime(), DateUtils.LOCAL_DATE)})
                        .findAll();

            } catch (DbException e) {e.printStackTrace();}


            if (listUeidInPeriod == null  || listUeidInPeriod.size() == 0)
                continue;

            listCollideTimePeriodIMSI.add(listUeidInPeriod);
        }

        //1.获取所有非重复的IMSI放入map，key为IMSI，value为次数
        for(List<DBUeidInfo> tmpList:listCollideTimePeriodIMSI) {
            for(DBUeidInfo tmpImsi : tmpList){
                mapImsiWithTimes.put(tmpImsi.getImsi(), 0);
            }
        }

        //2.每个不重复的imsi再次遍历各个时间段对应的list，存在就+1
        for(String tmpIMSI : mapImsiWithTimes.keySet()) {
            List<Long> listDetectTime = new ArrayList<>();
            for(List<DBUeidInfo> tmpList:listCollideTimePeriodIMSI) {
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
            mapCollideDetectTime.put(tmpIMSI, listDetectTime);
        }

        return mapImsiWithTimes;
    }

    //打点碰撞
    public static HashMap<String, Integer> collideAnalysis(List<Long> listCollideTimePoint, long deviation){
        HashMap<Long, List<DBUeidInfo>> mapCollideTimePointWithIMSI = new HashMap<>();
        HashMap<String, Integer> mapImsiWithTimes = new HashMap<>();

        mapTPCollideDetectTime.clear();

        //0.获取所有时间点对应的IMSI
        List<DBUeidInfo> listUeidInPeriod = new ArrayList<>();
        for(long tmpPoint : listCollideTimePoint){
            try {
                listUeidInPeriod = UCSIDBManager.getDbManager().selector(DBUeidInfo.class)
                        .where("createDate", "BETWEEN",
                                new long[]{(tmpPoint-deviation), (tmpPoint+deviation)}).findAll();
            } catch (DbException e) {e.printStackTrace();}

            if(listUeidInPeriod == null || listUeidInPeriod.size() == 0)
                continue;

            mapCollideTimePointWithIMSI.put(tmpPoint, listUeidInPeriod);
        }


        //1.获取所有非重复的IMSI放入map，key为IMSI，value为次数
        for(List<DBUeidInfo> tmpList:mapCollideTimePointWithIMSI.values()) {
            for(DBUeidInfo tmpImsi : tmpList){
                mapImsiWithTimes.put(tmpImsi.getImsi(), 0);
            }
        }

        //2.每个不重复的imsi再次遍历各个时间段对应的list，存在就+1
        for(String tmpIMSI : mapImsiWithTimes.keySet()) {
            List<Long> listDetectTime = new ArrayList<>();
            for(List<DBUeidInfo> tmpList : mapCollideTimePointWithIMSI.values()) {
                //boolean hasAddToMapImsiWithTimes = false;   //一个时间段只加一次
                for (DBUeidInfo tmpDBUeid : tmpList){
                    if (tmpDBUeid.getImsi().equals(tmpIMSI)){
                        mapImsiWithTimes.put(tmpIMSI, mapImsiWithTimes.get(tmpIMSI)+1);
                        listDetectTime.add(tmpDBUeid.getCreateDate());
                        break;
                    }
                }
            }
            //同时记录每次出现的时间
            mapTPCollideDetectTime.put(tmpIMSI, listDetectTime);
        }

        return mapImsiWithTimes;
    }

    //去重
    private static void removeDuplicate(List<String> list) {
        LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
        set.addAll(list);
        list.clear();
        list.addAll(set);
    }


    public static List<String> getCollideDetail(String imsi){
        List<Long> listAllDetectTime = mapCollideDetectTime.get(imsi);
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

    //打点碰撞获取时间
    public static List<String> getTPCollideDetail(String imsi, List<Long> listCollideTimePoint, long deviation){
        List<Long> listAllDetectTime = mapTPCollideDetectTime.get(imsi);
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

    //实时碰撞
    public static void collideAnalysis(HashMap<String, Integer> initialCollide, long endTime, long timePeriod){
        List<DBUeidInfo> listUeidInPeriod = new ArrayList<>();
        LinkedHashSet<DBUeidInfo> setUeidInPeriod = new LinkedHashSet<DBUeidInfo>();
        try {
            listUeidInPeriod = UCSIDBManager.getDbManager().selector(DBUeidInfo.class)
                    .where("createDate", "BETWEEN",
                            new long[]{(endTime -timePeriod), endTime}).findAll();
        } catch (DbException e) {e.printStackTrace();}

        if(listUeidInPeriod == null || listUeidInPeriod.size() == 0)
            return;

        //mapRealTimeAllTimePeriod.put(endTime-timePeriod, endTime);

        setUeidInPeriod.addAll(listUeidInPeriod);   //单次碰撞只能最多+1
        List<Long> listDetectTime;
        for (DBUeidInfo tmpUeid : setUeidInPeriod){
            listDetectTime = mapRTCollideDetectTime.get(tmpUeid.getImsi());
            if (initialCollide.containsKey(tmpUeid.getImsi())){
                if ((tmpUeid.getCreateDate()-listDetectTime.get(listDetectTime.size()-1)) < UNREPEAT_TIME)
                    continue;

                initialCollide.put(tmpUeid.getImsi(), initialCollide.get(tmpUeid.getImsi())+1);
            }else{
                initialCollide.put(tmpUeid.getImsi(), 1);
            }


            if (listDetectTime == null){
                listDetectTime = new ArrayList<>();
            }
            listDetectTime.add(tmpUeid.getCreateDate());
            mapRTCollideDetectTime.put(tmpUeid.getImsi(), listDetectTime);
        }
    }

    public static List<String> getRTCollideDetail(String imsi){
        List<Long> listAllDetectTime = mapRTCollideDetectTime.get(imsi);
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

    public static void restartRealtimeCollide(){
        mapRTCollideDetectTime.clear();
    }
}
