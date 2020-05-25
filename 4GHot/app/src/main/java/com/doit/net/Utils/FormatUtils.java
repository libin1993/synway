package com.doit.net.Utils;

import android.text.TextUtils;

import com.doit.net.application.MyApplication;

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
                        return false;
                    }
                    break;
                case "3":
                    if (fcn<1200 || fcn > 1949){
                        return false;
                    }
                    break;
                case "38":
                    if (fcn<37750 || fcn > 38250){
                        return false;
                    }
                    break;
                case "39":
                    if (fcn<38250 || fcn > 38650){
                        return false;
                    }
                    break;
                case "40":
                    if (fcn<38650 || fcn > 39650){
                        return false;
                    }
                    break;
                case "41":
                    if (fcn<39650 || fcn > 41589){
                        return false;
                    }
                    break;

            }
        }

        return true;
    }
}
