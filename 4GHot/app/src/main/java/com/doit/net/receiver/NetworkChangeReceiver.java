package com.doit.net.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.doit.net.event.EventAdapter;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.ucsi.R;

/**
 * Created by wiker on 2016/4/29.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static boolean isShow = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiNetInfo.isConnected() && !isShow) {
            MySweetAlertDialog dialog = new MySweetAlertDialog(context, MySweetAlertDialog.WARNING_TYPE);
            dialog.setTitleText(context.getString(R.string.wifi_error));
            dialog.setContentText(context.getString(R.string.tip_10));
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isShow = false;
                }
            });

            isShow = true;
            EventAdapter.call(EventAdapter.STOP_LOC);
            CacheManager.resetState();

        }

        EventAdapter.call(EventAdapter.WIFI_CHANGE);

    }
}
