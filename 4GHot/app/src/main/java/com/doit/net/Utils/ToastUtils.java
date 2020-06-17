package com.doit.net.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
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
	 * 显示一个view，居中显示
	 * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
	 * @param act
	 * @param len
	 * @param view
	 */
	public static void showMessageView(final Context act, final int len, final View view) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						synchronized (synObj) {
							if (toast != null) {
								toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
								toast.setView(view);
								toast.setDuration(len);
							} else {
								toast = new Toast(act.getApplicationContext());
								toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
								toast.setView(view);
								toast.setDuration(len);
							}
							toast.show();
						}
					}
				});
			}
		}).start();
	}
	
	/**
	 * 自定义toast，默认Toast.LENGTH_LONG
	 * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
	 * @param c
	 * @param view
	 */
	public static void showMessageViewLong(Context c,View view){
		showMessageView(c.getApplicationContext(),Toast.LENGTH_LONG,view);
	}
	
	/**
	 * 自定义toast，默认Toast.LENGTH_LONG
	 * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
	 * @param c
	 * @param view
	 */
	public static void showMessageViewShort(Context c,View view){
		showMessageView(c.getApplicationContext(),Toast.LENGTH_SHORT,view);
	}
	
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
     * Toast发送消息，默认Toast.LENGTH_SHORT
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:10
     * @param act
     * @param msg
     */
    public static void showMessage(final Context act, final String msg) {
        showMessage(act.getApplicationContext(), msg, Toast.LENGTH_SHORT);
    }
    
    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:10
     * @param act
     * @param msg
     */
    public static void showMessageLong(final Context act, final String msg) {
        showMessage(act.getApplicationContext(), msg, Toast.LENGTH_LONG);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:10
     * @param msg
     */
    public static void showMessageLong(String msg) {
        showMessage(MyApplication.mContext, msg, Toast.LENGTH_LONG);
    }

    /**
     * Toast发送消息，默认Toast.LENGTH_SHORT
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:35
     * @param act
     * @param msg
     */
    public static void showMessage(final Context act, final int msg) {
        showMessage(act.getApplicationContext(), msg, Toast.LENGTH_SHORT);
    }
    
    /**
     * Toast发送消息，默认Toast.LENGTH_LONG
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:13:35
     * @param act
     * @param msg
     */
    public static void showMessageLong(final Context act, final int msg) {
        showMessage(act.getApplicationContext(), msg, Toast.LENGTH_LONG);
    }
    public static void showMessageLong(final Context act, final int msg,int xOffset,int yOffset) {
        showMessage(act.getApplicationContext(), msg, Toast.LENGTH_LONG,xOffset,yOffset);
    }

    /**
     * Toast发送消息
     * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
     * @version 2012-5-22 上午11:14:09
     * @param act
     * @param msg
     * @param len
     */
    public static void showMessage(final Context act, final int msg,
            final int len) {
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
    public static void showMessage(final Context act, final int msg,
            final int len,final int xOffset,final int yOffset) {
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
    public static void showMessage(final Context act, final String msg,
            final int len) {
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
    public static void showMessage(final Context act, final String msg,
            final int len,final int xOffset,final int yOffset) {
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
}
