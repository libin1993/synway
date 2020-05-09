package com.doit.net.Utils;

import com.doit.net.Activity.GameApplication;

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

        float scale = GameApplication.appContext.getResources().getDisplayMetrics().density;

        return (int) (dpValue*scale+0.5);

    }

    /**
     *  dp转px
     */
    public int dip2px(float dpValue) {

        float scale = GameApplication.appContext.getResources().getDisplayMetrics().density;

        return (int) (dpValue*scale+0.5);

    }

    /**
     *  px转dp
     */

    public int px2dip(float pxValue) {

        float scale = GameApplication.appContext.getResources().getDisplayMetrics().density;

        return (int) (pxValue*scale+0.5);

    }
    /**
     *  px转dp
     */

    public int px2dip(int pxValue) {

        float scale = GameApplication.appContext.getResources().getDisplayMetrics().density;

        return (int) (pxValue*scale+0.5);

    }
}
