package com.doit.net.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

/**
 * Author：Libin on 2020/5/19 16:27
 * Description：
 */
public class PermissionUtils {
    private static PermissionUtils mInstance;

    private PermissionUtils() {
    }

    public static PermissionUtils getInstance() {
        if (mInstance == null) {
            synchronized (PermissionUtils.class) {
                if (mInstance == null) {
                    mInstance = new PermissionUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 检查特定权限，是否
     *
     * @param context
     * @param permission
     * @return
     */
    public boolean hasPermission(Context context, String permission) {
        //没有权限
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;

    }


    /**
     * 检查特定权限
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean hasPermission(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 权限被拒绝，重新请求或者提示用户去设置开启
     *
     * @param context
     * @param permissionName
     * @param onPermissionListener
     */
    public void showPermissionDialog(final Context context, String permission, String permissionName, final OnPermissionListener onPermissionListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");

        if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            builder.setMessage("未获得" + permissionName + "权限，无法正常使用APP，请点击\"设置\" - \"权限管理\"，开启相关权限。")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                        }
                    });

        } else {
            builder.setMessage("未获得" + permissionName + "权限，是否授予权限？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onPermissionListener.onReQuest();
                        }
                    });
        }

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onPermissionListener.onCancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();

    }


    public interface OnPermissionListener {
        void onReQuest();

        void onCancel();
    }
}


