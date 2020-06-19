package com.doit.net.Utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;


@SuppressLint("SimpleDateFormat")
public class DateUtils {
    
    /** yyyy-MM-dd HH:mm:ss */
    public final static String LOCAL_DATE = "yyyy-MM-dd HH:mm:ss";
    /**
    /** yyyy-MM-dd */
    public final static String LOCAL_DATE_DAY = "yyyy-MM-dd";
    /**
     * 返回日期：yyyyMMddHHmmss格式的字符串
     */
    public static String getStrOfDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 获取当前日期时间 返回日期：yyyy-MM-dd HH:mm:ss
     * 
     * @author WikerYong
     * @version 2012-1-9 上午09:47:39
     * @return
     */
    public static String getStrOfDateTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 比较日期，
     *
     * @param date1
     * @param date2
     * @return -1:date1&lt;date2;0:date1=date2;1:date1&gt;data2
     */
    public static int compareDate(Date date1,Date date2){
        int ss = date1.compareTo(date2);
        return ss;
    }
    
    /**
     * 日期比较，
     *
     * @param date1 格式：yyyy-MM-dd HH:mm:ss
     * @param date2 格式：yyyy-MM-dd HH:mm:ss
     * @return -1:date1&lt;date2;0:date1=date2;1:date1&gt;data2
     */
    public static int compareDate(String date1,String date2){
        DateFormat df = new SimpleDateFormat(LOCAL_DATE);
        try {
            return compareDate(df.parse(date1),df.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 日期比较，
     *
     * @param date1 格式：yyyy-MM-dd HH:mm:ss
     * @param date2 格式：yyyy-MM-dd HH:mm:ss
     * @param fmt
     * @return -1:date1&lt;date2;0:date1=date2;1:date1&gt;data2
     */
    public static int compareDate(String date1,String date2,String fmt){
        DateFormat df = new SimpleDateFormat(fmt);
        try {
            return compareDate(df.parse(date1),df.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 字符串转为日期类型，返回yyyy-MM-dd HH:mm:ss格式
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-7-5 下午04:33:49
     * @param str
     * @return
     */
    public static Date getDateByStr(String str) {
        Date date = new Date();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = formatter.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
   /**
    * 字符串转为日期类型
    *
    * @param str
    * @param sdf
    * @return
    */
    public static Date getDateByStr(String str,String sdf) {
        Date date = new Date();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(sdf);
            date = formatter.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    
    /**
     * 获取当前日期时间 返回日期：yyyy-MM-dd HH:mm
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-1-31 下午02:57:30
     * @return
     */
    public static String getStrOfDateMinute() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回日期：yyyyMMddHHmmssSSS格式的字符串
     * 
     * @author WikerYong
     * @version 2011-11-25 下午07:18:44
     * @return
     */
    public static String getStrOfMs() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回日期：yyyyMM格式的字符串
     */
    public static String getMonthFolder() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回日期：yyyyMMdd格式的字符串
     */
    public static String getYearMonthDay() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 获取前天天，返回yyyyMMdd格式的字符串
     *
     * @param i
     * @return
     */
    public static String getYearMonthDay(int i) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE,i);
        String dateString = formatter.format(cal.getTime());
        return dateString;
    }
    
    /**
     * 根据format将date转成字符串
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDateByFormat(Date date,String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }
    
    /**
     * 将Calendar 转成字符串
     *
     * @param c
     * @param format
     * @return
     */
    public static String getDateByFormat(Calendar c,String format){
        Calendar ca=Calendar.getInstance();    
        Date d = ca.getTime();
        return getDateByFormat(d,format);
    }
    
    /**
     * 获取当前月份
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-4-9 上午10:45:28
     * @return
     */
    public static String getMonth() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 获取当前年份
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-7-5 下午04:31:07
     * @return
     */
    public static String getYear() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回日期：yyyyMMddHH格式的字符串
     * 
     * @author WikerYong
     * @version 2011-12-20 下午03:43:14
     * @return
     */
    public static String getDataOfHour() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回时间：yyyyMMddHHmm格式
     */
    public static String getStrOfTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    
    /**
     * 返回时间：yyyy-MM-dd格式
     */
    public static String getCurrentDay() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 返回时间：yyyy-MM-dd格式
     */
    public static String getCurrentDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }
    
    public static String getLastDay(int day) {
        java.util.Date yestoday = new java.util.Date(new java.util.Date().getTime() - 1000 * 60
                * 60 * 24 * day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(yestoday);
    }
    
    /**
     * 获取昨天、前天的日期
     * 
     * @param currentDate
     * @return
     */
    public static String[] getLastDates(String currentDate) {
        String currYear, currMonth, currDay;
        currYear = currentDate.substring(0, 4);
        currMonth = currentDate.substring(4, 6);
        currDay = currentDate.substring(6);
        
        // 月份或日期首位是0
        String tempMonth, tempDay;
        if (currMonth.substring(0, 1).equals("0")) {
            tempMonth = "0";
        } else {
            tempMonth = "";
        }
        if (currDay.substring(0, 1).equals("0") || currDay.equals("10")) {
            tempDay = "0";
        } else {
            tempDay = "";
        }
        
        String[] returnDays = new String[2];
        
        if (currMonth.equals("01") && currDay.equals("01")) {
            returnDays[0] = (Integer.parseInt(currYear) - 1) + "1231";
            returnDays[1] = (Integer.parseInt(currYear) - 1) + "1230";
        } else if (currMonth.equals("01") && currDay.equals("02")) {
            returnDays[0] = currYear + "0101";
            returnDays[1] = (Integer.parseInt(currYear) - 1) + "1231";
        } else if (Integer.parseInt(currMonth) >= 1 && Integer.parseInt(currDay) > 2) {
            returnDays[0] = currYear + currMonth + tempDay + (Integer.parseInt(currDay) - 1);
            if (currDay.equals("11")) {
                returnDays[1] = currYear + currMonth + "0" + (Integer.parseInt(currDay) - 2);
            } else {
                returnDays[1] = currYear + currMonth + tempDay + (Integer.parseInt(currDay) - 2);
            }
        } else if (Integer.parseInt(currMonth) > 1 && Integer.parseInt(currDay) == 2) {
            returnDays[0] = currYear + currMonth + "01";
            if (currMonth.equals("10")) {
                returnDays[1] = currYear
                        + "0"
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)));
            } else {
                returnDays[1] = currYear
                        + tempMonth
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)));
            }
        } else if (Integer.parseInt(currMonth) > 1 && Integer.parseInt(currDay) == 1) {
            if (currMonth.equals("10")) {
                returnDays[0] = currYear
                        + "0"
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)));
                returnDays[1] = currYear
                        + "0"
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)) - 1);
            } else {
                returnDays[0] = currYear
                        + tempMonth
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)));
                returnDays[1] = currYear
                        + tempMonth
                        + (Integer.parseInt(currMonth) - 1)
                        + (getLastDayOfUpMonth(Integer.parseInt(currYear),
                                Integer.parseInt(currMonth), Integer.parseInt(currDay)) - 1);
            }
        } else {
            returnDays[0] = currYear + currMonth + tempDay + (Integer.parseInt(currDay) - 1);
            returnDays[1] = currYear + currMonth + tempDay + (Integer.parseInt(currDay) - 2);
        }
        
        return returnDays;
    }
    
    public static int getLastDayOfUpMonth(int year, int month, int date) {
        Calendar calendar = new GregorianCalendar(year, month, date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        calendar.add(Calendar.MONTH, -1);// 月增减1天
        calendar.add(Calendar.DAY_OF_MONTH, -1);// 日期倒数一日,既得到本月最后一天
        return calendar.get(Calendar.DATE);
    }
    
    /**
     * 获取当月第一天
     * 
     * @author WikerYong
     * @version 2011-11-21 下午04:45:06
     * @return
     */
    public static String getFirstDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }
    
    /**
     * 获取当月最后一天
     * 
     * @author WikerYong
     * @version 2011-11-21 下午04:46:06
     * @return
     */
    public static String getLastDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        lastDate.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
        lastDate.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
        str = sdf.format(lastDate.getTime());
        return str;
    }
    
    /**
     * 获取去年的年份
     * 
     * @return
     */
    public static String getLastYear() {
        return getYear(-1);
    }
    
    /**
     * 获取过去i年或后面的年份，如今年为2013，i=-1，则获取2012年
     *
     * @param i
     * @return
     */
    public static String getYear(int i) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(currentTime);
        c.add(Calendar.YEAR, i);
        String dateString = formatter.format(c.getTime());
        return dateString;
    }
    
    /**
     * 获取从当年往前递减i年，如：i=5，year=2013，则获取2013，2012，2011，2010，2009
     *
     * @param i
     * @return
     */
    public static String[] getLastYears(int num){
        String[] s = new String[num];
        for(int i=0;i<num;i++){
            s[i]=getYear(-i+1);
        }
        return s;
    }
    
    /**
     * 获取前年的年份
     * 
     * @return
     */
    public static String getBeforeLastYear() {
        return getYear(-2);
    }
    
    /**
     * 获取某月最后一天
     * 
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    /**
     * 获取某月第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
    
    /**
     * 判断日期是否为同一天
     *
     * @param dateA
     * @param dateB
     * @return true 则为同一天，false则为不在同一天
     */
    public static boolean isSameDay(Date dateA,Date dateB) {
        Calendar calDateA = Calendar.getInstance();
        calDateA.setTime(dateA);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTime(dateB);

        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
                && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
                &&  calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
    }
    /**
     * 将日期格式的字符串转换为长整型
     * 
     * @param date
     * @param format
     * @return
     */
    public static long convert2long(String date, String format) {
         try {
           SimpleDateFormat sf = new SimpleDateFormat(format);
           return sf.parse(date).getTime();
         } catch (ParseException e) {
          e.printStackTrace();
         }
         return 0L;
    }
    
    public static String formatMillseconds(long millSeconds){
        int minute = (int)millSeconds/(1000* 60);
        int hour = minute / 60;
        float second = ((float)millSeconds/1000f) % 60;
        minute %= 60;
        DecimalFormat df = new DecimalFormat("0.00");
        String s = " ";
        if(hour>0){
            s+=hour+" hour ";
        }
        if(minute>0){
            s+=minute+" minuete ";
        }
        s+=df.format(second)+" seconds";
        return s;
    }
    
    /**
     * 将长整型数字转换为日期格式的字符串
     * 
     * @param time
     * @param format
     * @return
     */
    public static String convert2String(long time, String format) {
          SimpleDateFormat sf = new SimpleDateFormat(format);
          Date date = new Date(time);
          return sf.format(date);
    }

    public static String convert2String(Date date, String format) {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(date);
    }


    public static boolean isStartEndTimeOrderRight(String startTime, String endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dataStartTime = null;
        try {
            dataStartTime = simpleDateFormat.parse(startTime);
        } catch (ParseException e) {e.printStackTrace();}

        Date dateEndTime = null;
        try {
            dateEndTime = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {e.printStackTrace();}

        return  dataStartTime.before(dateEndTime);
    }
}