package com.doit.net.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.doit.net.event.EventAdapter;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.NetWorkUtils;

/**
 * Created by wiker on 2016/4/29.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static boolean isShow = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!NetWorkUtils.getNetworkState()){
            if (!isShow){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        EventAdapter.call(EventAdapter.STOP_LOC);

                        EventAdapter.call(EventAdapter.WIFI_CHANGE);
                    }
                },5000);

            }
        }else {
            EventAdapter.call(EventAdapter.WIFI_CHANGE);
        }

    }
}
