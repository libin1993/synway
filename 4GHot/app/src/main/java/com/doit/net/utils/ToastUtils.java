package com.doit.net.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.doit.net.application.MyApplication;

/**
 * Toast工具类
 *
 * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
 * @version 2012-5-21 下午9:21:01
 * @copyright Dshine
 */
public class ToastUtils {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Toast toast = null;
    private static Object synObj = new Object();

    /**
     * 关闭当前Toast
     *
     * @author WikerYong Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:14:45
     */
    public static void cancelCurrentToast() {
        if (toast != null) {
            toast.cancel();
        }
    }


    /**
     * Toast发送消息，默认Toast.LENGTH_SHORT
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:10

     * @param msg
     */
    public static void showMessage( String msg) {
        showMessage(MyApplication.mContext, msg, Toast.LENGTH_SHORT);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:10
     * @param msg
     */
    public static void showMessageLong( String msg) {
        showMessage(MyApplication.mContext, msg, Toast.LENGTH_LONG);
    }


    /**
     * Toast发送消息，默认Toast.LENGTH_SHORT
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:35
     * @param msg
     */
    public static void showMessage( int msg) {
        showMessage(MyApplication.mContext, MyApplication.mContext.getString(msg), Toast.LENGTH_SHORT);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:35
     * @param msg
     */
    public static void showMessageLong(int msg) {
        showMessage(MyApplication.mContext, MyApplication.mContext.getString(msg), Toast.LENGTH_LONG);
    }


    public static void showMessageLong(int msg,int xOffset,int yOffset) {
        showMessage(MyApplication.mContext, MyApplication.mContext.getString(msg), Toast.LENGTH_LONG,xOffset,yOffset);
    }

    /**
     * Toast发送消息
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:14:09
     * @param act
     * @param msg
     * @param len
     */
    public static void showMessage(Context act,int msg, int len) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (synObj) {
                            if (toast != null) {
                                toast.setText(msg);
                                toast.setDuration(len);
                            } else {
                                toast = Toast.makeText(act.getApplicationContext(), msg, len);
                            }
                            toast.show();
                        }
                    }
                });
            }
        }).start();
    }
    public static void showMessage(Context act, String msg, int len,int xOffset,int yOffset) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (synObj) {
                            if (toast != null) {
                                toast.setText(msg);
                                toast.setGravity(Gravity.CENTER, xOffset, yOffset);
                                toast.setDuration(len);
                            } else {
                                toast = Toast.makeText(act.getApplicationContext(), msg, len);
                                toast.setGravity(Gravity.CENTER, xOffset, yOffset);
                            }
                            toast.show();
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Toast发送消息
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:14:27
     * @param act
     * @param msg
     * @param len
     */
    public static void showMessage(Context act, String msg, int len) {
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (synObj) {
                            if (toast != null) {
                                toast.setText(msg);
                                toast.setDuration(len);
                            } else {
                                toast = Toast.makeText(act.getApplicationContext(), msg, len);
                            }
                            toast.show();
                        }
                    }
                });
            }
        }).start();
    }

}
