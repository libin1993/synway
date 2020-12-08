package com.doit.net.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;


import com.doit.net.application.MyApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Author：Libin on 2012/5/9 15:08
 * Description：屏幕配置
 */
public class ScreenUtils {
    private static ScreenUtils mInstance;

    private ScreenUtils() {
    }

    public static ScreenUtils getInstance() {
        if (mInstance == null) {
            synchronized (ScreenUtils.class) {
                if (mInstance == null) {
                    mInstance = new ScreenUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 设置状态栏栏高度
     *
     * @param view
     */
    public void setStatusBarHeight(View view) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = getStatusBarHeight();
        view.setLayoutParams(layoutParams);
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = MyApplication.mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = MyApplication.mContext.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }


    /**
     * @return 屏幕宽度
     */
    public int getScreenWidth(Activity activity) {

        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric.widthPixels;
    }


    /**
     * @return 屏幕高度
     */
    public int getScreenHeight(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric.heightPixels;
    }


    public void setNoStatusBar(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }


    /**
     * 设置屏幕的背景透明度
     *
     * @param
     */
    public void setBackgroundAlpha(Context mContext, boolean isShow) {
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        if (isShow) {
            lp.alpha = 0.4f;
            ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            lp.alpha = 1.0f;
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ((Activity) mContext).getWindow().setAttributes(lp);

    }

    public void setStatusBarFontDark(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCommonUI(activity);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            if (OSUtils.getInstance().isMiui()){
                setMiuiUI(activity,true);
            }else if (OSUtils.getInstance().isFlyme()){
                setFlymeUI(activity,true);
            }
        }
    }



    //设置6.0的字体
    private   void setCommonUI(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    //设置Flyme的字体
    private  void setFlymeUI(Activity activity,boolean dark) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置MIUI字体
    private  void setMiuiUI(Activity activity,boolean dark) {
        try {
            Window window = activity.getWindow();
            Class clazz = activity.getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
