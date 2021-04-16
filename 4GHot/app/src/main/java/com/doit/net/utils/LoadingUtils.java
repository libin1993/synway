package com.doit.net.utils;

import android.content.Context;
import android.os.Handler;

import com.doit.net.view.MySweetAlertDialog;

/**
 * Author：Libin on 2020/7/31 16:32
 * Email：1993911441@qq.com
 * Describe：
 */
public class LoadingUtils {
    public static void loading(Context context){
        MySweetAlertDialog dialog = new MySweetAlertDialog(context,MySweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);
    }
}
