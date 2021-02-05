package com.doit.net.utils;

import android.text.TextUtils;

import com.doit.net.application.MyApplication;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Author：Libin on 2020/5/9 17:47
 * Email：1993911441@qq.com
 * Describe：格式转换
 */
public class FormatUtils {
    private static FormatUtils mInstance;

    private FormatUtils() {
    }

    public static FormatUtils getInstance() {
        if (mInstance == null){
            synchronized (FormatUtils.class){
                if (mInstance == null){
                    mInstance = new FormatUtils();
                }
            }
        }
        return mInstance;
    }


    /**
     *  dp转px
     */
    public int dip2px(int dpValue) {

        float scale = MyApplication.mContext.getResources().getDisplayMetrics().density;

        return (int) (dpValue*scale+0.5);

    }

    /**
     *  dp转px
     */
    public int dip2px(float dpValue) {

        float scale = MyApplication.mContext.getResources().getDisplayMetrics().density;

        return (int) (dpValue*scale+0.5);

    }

    /**
     *  px转dp
     */

    public int px2dip(float pxValue) {

        float scale = MyApplication.mContext.getResources().getDisplayMetrics().density;

        return (int) (pxValue*scale+0.5);

    }
    /**
     *  px转dp
     */

    public int px2dip(int pxValue) {

        float scale = MyApplication.mContext.getResources().getDisplayMetrics().density;

        return (int) (pxValue*scale+0.5);

    }
    public boolean plmnRange(String plmn){
        if (!TextUtils.isEmpty(plmn) && (plmn.startsWith(",") || plmn.endsWith(","))){
            return false;
        }
        String[] split = plmn.split(",");
        for (String s : split) {
            if (TextUtils.isEmpty(s) || s.length() != 5){
                return false;
            }
        }
        return true;
    }

    public boolean gpsRange(String gps){
        if (!TextUtils.isEmpty(gps) && (gps.startsWith(",") || gps.endsWith(","))){
            return false;
        }
        String[] split = gps.split(",");

        for (String s : split) {
            if (TextUtils.isEmpty(s)){
                return false;
            }
        }
        return true;
    }

    public boolean pciRange(String pci){
        if (!TextUtils.isEmpty(pci) && (pci.startsWith(",") || pci.endsWith(","))){
            return false;
        }
        String[] split = pci.split(",");

        for (String s : split) {
            if (TextUtils.isEmpty(s) || s.length() > 3){
                return false;
            }
            int pciInt = Integer.parseInt(s);
            if (pciInt > 503){
                return false;
            }
        }
        return true;
    }



    /**
     * @return FCN校验
     */
    public boolean matchFCN(String input){
        return Pattern.matches("^(\\d+)|(\\d+,\\d+)|(\\d+,\\d+,\\d+)", input);
    }

    /**
     * @return FCN校验
     */
    public boolean fcnRange(String band,String input){
        String[] split = input.split(",");
        for (String s : split) {
            int fcn = Integer.parseInt(s);
            switch (band){
                case "1":
                    if (fcn<0 || fcn > 599){
                        ToastUtils.showMessage("请输入0-599范围内数字");
                        return false;
                    }
                    break;
                case "3":
                    if (fcn<1200 || fcn > 1949){
                        ToastUtils.showMessage("请输入1200-1949范围内数字");
                        return false;
                    }
                    break;
                case "38":
                    if (fcn<37750 || fcn > 38250){
                        ToastUtils.showMessage("请输入37750-38250范围内数字");
                        return false;
                    }
                    break;
                case "39":
                    if (fcn<38250 || fcn > 38650){
                        ToastUtils.showMessage("请输入38250-38650范围内数字");
                        return false;
                    }
                    break;
                case "40":
                    if (fcn<38650 || fcn > 39650){
                        ToastUtils.showMessage("请输入38650-39650范围内数字");
                        return false;
                    }
                    break;
                case "41":
                    if (fcn<39650 || fcn > 41589){
                        ToastUtils.showMessage("请输入39650-41589范围内数字");
                        return false;
                    }
                    break;

            }
        }

        return true;
    }


    /**
     *  特殊字符
     */
    public boolean isCommon(String data) {

        return !data.contains(",") && !data.contains("#");
    }

    /**
     * @param data 数组倒序
     */
    public void reverseData(byte[] data) {
        for (int start = 0, end = data.length - 1; start < end; start++, end--) {
            byte temp = data[end];
            data[end] = data[start];
            data[start] = temp;
        }
    }


    /**
     * byte[] 转int
     *
     * @param tempValue
     * @return
     */
    public int byteToInt(byte[] tempValue) {
        int[] tempInt = new int[4];
        tempInt[3] = (tempValue[3] & 0xff) << 0;
        tempInt[2] = (tempValue[2] & 0xff) << 8;
        tempInt[1] = (tempValue[1] & 0xff) << 16;
        tempInt[0] = (tempValue[0] & 0xff) << 24;
        return tempInt[0] + tempInt[1] + tempInt[2] + tempInt[3];
    }


    /**
     * 将传入的字符串转换成byte[]
     *
     * @param tempValue
     * @return
     */
    public  byte[] string2BytesForASCII(String tempValue) {
        try {
            return tempValue.getBytes(StandardCharsets.US_ASCII);
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 将传入的字符串转换成byte[]
     *
     * @param tempValue
     * @return
     */
    public  byte[] string2BytesForUTF(String tempValue) {
        try {
            return tempValue.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 将传入的byte[]转换成字符串
     *
     * @param data
     * @return
     */
    public  String bytes2StringForASCII(byte[] data) {
        return new String(data,StandardCharsets.US_ASCII);
    }


    /**
     * 将传入的byte[]转换成字符串
     *
     * @param data
     * @return
     */
    public  String bytes2StringForUTF(byte[] data) {
        return new String(data,StandardCharsets.UTF_8);
    }


    /**
     * byte[] 转short
     *
     * @param
     * @return
     */
    public short byteToShort(byte[] tempValue) {
        return (short) ((tempValue[0] << 8) + (tempValue[1] & 0xFF));
    }


    /**
     * Byte转Bit
     */
    public String byteToBit(byte b) {
        return "" +(byte)((b >> 7) & 0x1) +
                (byte)((b >> 6) & 0x1) +
                (byte)((b >> 5) & 0x1) +
                (byte)((b >> 4) & 0x1) +
                (byte)((b >> 3) & 0x1) +
                (byte)((b >> 2) & 0x1) +
                (byte)((b >> 1) & 0x1) +
                (byte)((b >> 0) & 0x1);
    }
}
