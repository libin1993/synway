package com.doit.net.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wiker on 2016/3/15.
 */
public class PrintUtils {

    private final static Logger log = Logger.getLogger(PrintUtils.class);

    private static final String TAG = PrintUtils.class.getSimpleName();
    public static boolean isDebug= Logger.logFlag;
    static public void printStr(String tag, String body)
    {
        log.debug(tag+" "+body);
//        if(isDebug)
//            System.out.println(tag+" "+body);
//          Log.i(tag, body);
    }

    public static void printStr(byte[] ba, int len)
    {
        printStr(TAG, ba, len);
    }
    public static void printStr(byte[] ba)
    {
        printStr(TAG, ba, ba.length);
    }
    public static void printStr(Object obj)
    {
        printStr(TAG, String.valueOf(obj));
    }
    public static void printStr(String tag, Object obj)
    {
        printStr(tag, String.valueOf(obj));
    }

    public static void printStr(String tag, byte[] ba, int len)
    {
        if(!isDebug)
            return;
        String s;
        try {
            s = new String(ba, 0, len, "utf-8");
            printStr(tag, s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public static void printHex(byte[] dataStr_cp, int dataLen_n)
    {
        printHex(TAG, dataStr_cp, dataLen_n);
    }
    public static void printHex(byte[] dataStr_cp)
    {
        printHex(TAG, dataStr_cp, dataStr_cp.length);
    }
    public static void printHex(String tag, byte[] dataStr_cp, int dataLen_n)
    {
        printHex(tag, dataStr_cp, 0, dataLen_n);
    }

    public static void printHex(String tag, byte[] dataStr_cp, int start, int dataLen_n)
    {
        if(!isDebug)
            return;
        int i=0, j=0;
        int lineCnt_n = 0;
        try {
            byte[] set=new String("0123456789ABCDEF").getBytes("utf-8");

            if(dataLen_n==0)
                return;

            if(dataLen_n%16!=0)
                lineCnt_n = dataLen_n/16 + 1;
            else
                lineCnt_n = dataLen_n/16;

            String p=new String("------------------ Raw data "+ DateUtil.getStrOfDateTime()+" ------------------");
            printStr(tag, p.getBytes("utf-8"), p.length());
            printStr("HEX    ","   00 01 02 03 04 05 06 07 08 09  A  B  C  D  E  F");

            for(i=0; i<lineCnt_n; i++)
            {
                byte[] lineStr_ca=new byte[128];
                for(j=0; j<16; j++)
                {
                    if(i*16+j<dataLen_n)
                    {
                        if(dataStr_cp[start+i*16+j]<0)
                        {
                            lineStr_ca[j*3]   = set[ (byte)((dataStr_cp[start+i*16+j]&0x7f+128) >> 4) ];
                            lineStr_ca[j*3+1] = set[ (byte)((dataStr_cp[start+i*16+j]&0x7f+128) & 0xF) ];
                        }
                        else
                        {
                            lineStr_ca[j*3]   = set[ ((byte)dataStr_cp[start+i*16+j]) >> 4 ];
                            lineStr_ca[j*3+1] = set[ ((byte)dataStr_cp[start+i*16+j]) & 0xF ];
                        }
                        lineStr_ca[j*3+2] = ' ';
                    }
                    else
                    {
                        lineStr_ca[j*3]   = ' ';
                        lineStr_ca[j*3+1] = ' ';
                        lineStr_ca[j*3+2] = ' ';
                    }
                }
                lineStr_ca[16*3] = ';';
                lineStr_ca[16*3+1] = ' ';

                //cursor pos 16*3+2= 50
                for(j=0; j<16; j++)
                {
                    if(i*16+j<dataLen_n)
                    {
                        if (dataStr_cp[start+i*16+j]>=0x20 && dataStr_cp[start+i*16+j] <= 0x7e && dataStr_cp[start+i*16+j]!='%')
                            lineStr_ca[50+j] = (byte)dataStr_cp[start+i*16+j];
                        else
                            lineStr_ca[50+j] = '.';
                    }
                    else
                        lineStr_ca[50+j] = ' ';
                }
                int l = String.valueOf(i).length();
                String ws = "";
                for(int ss=0;ss<8-l;ss++){
                    ws += "0";
                }
                printStr(ws+i+" |", lineStr_ca, 50+j);
            }
        } catch (Exception e) {
        }
    }

    public static void logException(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        printStr(sw.toString());
    }

    public static void logException(String tag, Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        printStr(tag, sw.toString());
    }

    static public void printDate(String tag, long ms)
    {
        if(!isDebug)
            return;
        Date date = new Date(ms);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        printStr(tag, "printDate="+String.valueOf(ms)+";date="+simpleDateFormat.format(date));
    }

    static public void printFunc()
    {
        StackTraceElement[] ste=new Exception().getStackTrace();
        String methodName =   ste[1].getMethodName();
        String clsName =   ste[1].getClassName();
        clsName=clsName.substring(clsName.lastIndexOf('.')+1);
        printStr(clsName, methodName);
    }
}
