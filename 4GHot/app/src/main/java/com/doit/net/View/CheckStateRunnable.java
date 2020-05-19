package com.doit.net.View;

import android.os.Handler;

import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.udp.g4.server.G4UDPServerManager;

/**
 * Created by wiker on 2016/5/5.
 */
public class CheckStateRunnable implements Runnable {

    private final static int CHECK_TIME = 90*1000;
    private Handler mHandler;

    public CheckStateRunnable(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        try {
//            if(!CacheManager.isDeviceOk()){
//                ProtocolManager.getEnbState();
//            }
            if(!G4UDPServerManager.isStart || CacheManager.last_heart_time<=0){
                mHandler.postDelayed(this,10*1000);
                return;
            }
            if(System.currentTimeMillis() - CacheManager.last_heart_time > CHECK_TIME){
                LogUtils.log(CHECK_TIME+" 毫秒秒无心跳响应");
                CacheManager.resetState();
//                G4UDPServerManager.stop();
                UIEventManager.call(UIEventManager.KEY_REFRESH_DEVICE);
            }
            mHandler.postDelayed(this,10*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
