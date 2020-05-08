package com.doit.net.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;



/**
 * 字符串操作工具包
 * 
 * @version 1.0
 * @created 2012-3-21
 */
@SuppressLint("SimpleDateFormat")
public class StringUtils {
    private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    // private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");
    
    private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    
    /**
     * 获取string.xml中的字符串，有参数
     * 
     * @param c
     *            上下文
     * @param resId
     *            字符串资源id <string name="stringName">字 %1$s 字，字 %2$s 字</string>
     * @param args
     *            替换文本
     * @return
     */
    public static String getString(Context c, int resId, Object... args) {
        String sFormat = c.getResources().getString(resId);
        String sFinal = String.format(sFormat, args);
        return sFinal;
    }
    
    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    
    /**
     * <p>
     * Checks if the CharSequence contains only Unicode digits. A decimal point is not a Unicode digit and returns
     * false.
     * </p>
     * <p>
     * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return {@code false}.
     * </p>
     * 
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * </pre>
     * 
     * @param cs
     *            the CharSequence to check, may be null
     * @return {@code true} if only contains digits, and is non-null
     */
    public static boolean isNumeric(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查是否为数字
     * 
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
    
    /**
     * 获取从min(包含min)到max(包含max)的随机数
     * 
     * @param min
     * @param max
     * @returnR
     */
    public static int getRandom(int min, int max) {
        int s = (int) (Math.random() * (max - min + 1) + min);
        return s;
    }
    
    /**
     * 将字符串转位日期类型
     * 
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.get().parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * 以友好的方式显示时间
     * 
     * @param sdate
     * @return
     */
    public static String friendly_time(String sdate) {
        Date time = toDate(sdate);
        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();
        
        // 判断是否是同一天
        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }
        
        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days <= 10) {
            ftime = days + "天前";
        } else if (days > 10) {
            ftime = dateFormater2.get().format(time);
        }
        return ftime;
    }
    
    /**
     * 判断给定字符串时间是否为今日
     * 
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = dateFormater2.get().format(today);
            String timeDate = dateFormater2.get().format(time);
            if (nowDate.equals(timeDate)) {
                b = true;
            }
        }
        return b;
    }
    
    /**
     * 判断给定字符串是否空白串。
     * 空白串是指由空格、制表符、回车符、换行符组成的字符串
     * 若输入字符串为null或空字符串，返回true
     * 
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>
     * Checks if a CharSequence is empty ("") or null.
     * </p>
     * 
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the CharSequence. That functionality is
     * available in isBlank().
     * </p>
     * 
     * @param cs
     *            the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    /**
     * 获取默认字符串，
     * 
     * <pre>
     * StringUtils.defaultString(null) = ""
     * StringUtils.defaultString("") = ""
     * StringUtils.defaultString("bat") = "bat"
     * </pre>
     * 
     * @param str
     * @return
     */
    public static String defaultString(String str) {
        return str == null ? "" : str;
    }
    
    /**
     * 获取默认字符串，
     * 
     * <pre>
     * StringUtils.defaultString(null,"NULL") = "NULL"
     * StringUtils.defaultString("","NULL") = ""
     * StringUtils.defaultString("bat","NULL") = "bat"
     * </pre>
     * 
     * @param str
     * @return
     */
    public static String defaultString(String str, String defaultStr) {
        return str == null ? "" : str;
    }
    
    /**
     * <p>
     * Checks if a CharSequence is whitespace, empty ("") or null.
     * </p>
     * 
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     * 
     * @param cs
     *            the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * <p>
     * Returns either the passed in CharSequence, or if the CharSequence is whitespace, empty ("") or {@code null}, the
     * value of {@code defaultStr}.
     * </p>
     * 
     * <pre>
     * StringUtils.defaultIfBlank(null, "NULL")  = "NULL"
     * StringUtils.defaultIfBlank("", "NULL")    = "NULL"
     * StringUtils.defaultIfBlank(" ", "NULL")   = "NULL"
     * StringUtils.defaultIfBlank("bat", "NULL") = "bat"
     * StringUtils.defaultIfBlank("", null)      = null
     * </pre>
     * 
     * @param <T>
     *            the specific kind of CharSequence
     * @param str
     *            the CharSequence to check, may be null
     * @param defaultStr
     *            the default CharSequence to return
     *            if the input is whitespace, empty ("") or {@code null}, may be null
     * @return the passed in CharSequence, or the default
     * @see StringUtils#defaultString(String, String)
     */
    public static <T extends CharSequence> T defaultIfBlank(T str, T defaultStr) {
        return StringUtils.isBlank(str) ? defaultStr : str;
    }
    
    /**
     * <p>
     * Returns either the passed in CharSequence, or if the CharSequence is empty or {@code null}, the value of
     * {@code defaultStr}.
     * </p>
     * 
     * <pre>
     * StringUtils.defaultIfEmpty(null, "NULL")  = "NULL"
     * StringUtils.defaultIfEmpty("", "NULL")    = "NULL"
     * StringUtils.defaultIfEmpty(" ", "NULL")   = " "
     * StringUtils.defaultIfEmpty("bat", "NULL") = "bat"
     * StringUtils.defaultIfEmpty("", null)      = null
     * </pre>
     * 
     * @param <T>
     *            the specific kind of CharSequence
     * @param str
     *            the CharSequence to check, may be null
     * @param defaultStr
     *            the default CharSequence to return
     *            if the input is empty ("") or {@code null}, may be null
     * @return the passed in CharSequence, or the default
     * @see StringUtils#defaultString(String, String)
     */
    public static <T extends CharSequence> T defaultIfEmpty(T str, T defaultStr) {
        return StringUtils.isEmpty(str) ? defaultStr : str;
    }
    
    /**
     * 判断是不是一个合法的电子邮件地址
     * 
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }
    
    /**
     * 字符串转整数
     * 
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }
    
    /**
     * 对象转整数
     * 
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }
    
    /**
     * 对象转整数
     * 
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }
    
    /**
     * 字符串转布尔值
     * 
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }
    
    /**
     * 将字符串转为单个字符数组，如“我abc”=>{"我","a","b","c"}
     * 
     * @param s
     * @return
     */
    public static String[] toCharArr(String s) {
        char[] c = s.toCharArray();
        String[] arr = new String[c.length];
        for (int i = 0; i < c.length; i++) {
            arr[i] = Character.toString(c[i]);
        }
        return arr;
    }
    
    /**
     * 金额格式化
     * 
     * @param s
     *            金额
     * @param len
     *            小数位数
     * @return 格式后的金额
     */
    public static String insertComma(String s, int len) {
        if (s == null || s.length() < 1) {
            return "";
        }
        NumberFormat formater = null;
        double num = Double.parseDouble(s);
        if (len == 0) {
            formater = new DecimalFormat("###,###");
            
        } else {
            StringBuffer buff = new StringBuffer();
            buff.append("###,###.");
            for (int i = 0; i < len; i++) {
                buff.append("#");
            }
            formater = new DecimalFormat(buff.toString());
        }
        return formater.format(num);
    }
    
    /**
     * 金额去掉“,”
     * 
     * @param s
     *            金额
     * @return 去掉“,”后的金额
     */
    public static String delComma(String s) {
        String formatString = "";
        if (s != null && s.length() >= 1) {
            formatString = s.replaceAll(",", "");
        }
        
        return formatString;
    }
    
    /**
     * 格式化金币<br>
     * >=10000 返回1万
     * 
     * @param gold
     * @return
     */
//    public static String formatGold(long gold, Context context) {
//        return formatGold(gold, context.getApplicationContext().getString(R.string.unit_ten_thousand));
//    }
    
    /**
     * 格式化金币<br>
     * >=10000 返回1万
     * 
     * @param gold
     * @param suffix
     *            后缀如万，w
     * @return
     */
    public static String formatGold(long gold, String suffix) {
        DecimalFormat fmt = new DecimalFormat("##.##");
        if (gold >= 10000) {
            return fmt.format(Double.valueOf(gold) / Double.valueOf(10000)) + suffix;
        }
        return String.valueOf(gold);
    }
    public static String formatMoneyFen(int money) {
        DecimalFormat fmt = new DecimalFormat("##.##");
        return fmt.format(Double.valueOf(money) / Double.valueOf(100));
    }
    
    /**
     * 获取数组某个值
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @param c
     * @param resId
     * @param index
     * @return
     */
    public static String getStrByArray(Context c, int resId, int index) {
        return c.getResources().getStringArray(resId)[index];
    }
    
    /**
     * 获取int型数组的值
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @param c
     * @param resId
     * @param index
     * @return
     */
    public static int getIntByArray(Context c, int resId, int index) {
        return c.getResources().getIntArray(resId)[index];
    }
    
    /**
     * 获取数组
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @param c
     * @param resId
     * @return
     */
    public static String[] getStrArray(Context c, int resId) {
        return c.getResources().getStringArray(resId);
    }
    
    /**
     * 获取随机数字字符串
     * 
     * @param length
     * @return
     */
    public static String randomNumber(int length) {
        int[] array = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        };
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        String s = "";
        for (int i = 0; i < length; i++)
            s += result * 10 + array[i];
        return s;
    }
    
    /**
     * 获取int型数组
     * 
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @param c
     * @param resId
     * @return
     */
    public static int[] getIntegerArray(Context c, int resId) {
        return c.getResources().getIntArray(resId);
    }
    
    /**
     * 
     * 输入ms的时间
     *
     * @param timeCost  时间ms
     * @return 时间格式的字符串  0:00:0 
     */
    public static String  getHourStrFromMs(long timeCost) {
        int timeInvert = (int)timeCost;
        int hour = 0, minute = 0, second = 0;
        String retStr;
        
        if(timeCost < 0){
            timeInvert = 0;
        }
        
        int maxShow = (99*3600 + 59*60 + 59)*1000;
        if(timeInvert > maxShow){
            timeInvert = maxShow;
        }
        
        timeInvert = timeInvert/1000;//得出 秒
        
        hour = timeInvert/(60*60);
        timeInvert = timeInvert%(60*60);
        minute = timeInvert/60;
        second = timeInvert%60;
        
        retStr = "" + String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
        return retStr;
    }
    
    /**
     * 
     * 输入ms的时间
     *
     * @param timeCost  时间ms
     * @return 时间格式的字符串  0:00.0 
     */
    public static String  getMinuteStrFromMs(long timeCost) {
        int timeInvert = (int)timeCost;
        int minute = 0, second = 0, ms = 0;
        String retStr ;
        
        if(timeCost < 0){
            timeInvert = 0;
        }
        
        int maxShow = (99*60 + 59)*1000 + 999;
        if(timeInvert > maxShow){
            timeInvert = maxShow;
        }
        
        timeInvert = timeInvert / 10;
        ms = timeInvert % 100;
        timeInvert /= 100;
        second = timeInvert % 60;
        timeInvert /= 60;
        minute = timeInvert;
        retStr = "" + String.format("%02d", minute) + ":" + String.format("%02d", second) + "." + String.format("%02d", ms);
        return retStr;
    }

    public static int getChrCount(String s, String chr) {
        return  s.length() - s.replace(chr, "").length();
    }
}
