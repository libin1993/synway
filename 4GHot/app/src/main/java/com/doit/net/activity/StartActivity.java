package com.doit.net.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.doit.net.base.BaseActivity;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.BuildConfig;
import com.doit.net.ucsi.R;

import java.util.ArrayList;
import java.util.List;


public class StartActivity extends BaseActivity {
    View view;
    private Activity activity = this;
    private final int PERMISSION_REQUEST_CODE = 1;
	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestAllPermissions();
	}

	private void startApp(){
        if (BuildConfig.SPLASH_SCREEN){
            getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            view = View.inflate(this, R.layout.start, null);
            setContentView(view);
            loadAnim();
        }else{
            redirectTo();
        }
    }

    private void loadAnim(){
	     // 渐变展示启动屏
        AlphaAnimation aa = new AlphaAnimation(0.8f, 1.0f);
        aa.setDuration(2000);
        view.startAnimation(aa);
        aa.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                redirectTo();
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationStart(Animation animation) {
                //mSoundManager.play(SoundManager.SOUND_LOGO);
            }
            
        });
    }

	private void redirectTo(){
        finish();
        startActivity(new Intent(this, LoginActivity.class));
	}


	/********************* 以下为权限申请相关 *********************/
    private void requestAllPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE};
        String title = "检测到存在未授权的权限";
        String content = "请务必同意授权这些权限，否则程序将无法打开！";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {//for循环把需要授权的权限都添加进来
                if (ContextCompat.checkSelfPermission(activity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {  //未授权就进行授权
                    permissionList.add(permissions[i]);
                }
            }

            if (!permissionList.isEmpty()) {
                showDialogRequestPermissions(permissionList, title, content);
            }else{
                startApp();
            }
        }else{
            startApp();
        }
    }

    private void startRequestPermission(List<String> permissionList) {
        if (!permissionList.isEmpty()) {//不为空就进行授权申请
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void showDialogRequestPermissions(final List<String> permissionList, String title, String content) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission(permissionList);
                    }
                })
                .setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults.length > 0) {//安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        switch (grantResult){
                            case PackageManager.PERMISSION_GRANTED://同意授权0
                                break;
                            case PackageManager.PERMISSION_DENIED://拒绝授权-1
                                ToastUtils.showMessageLong(activity, permissions[i]+"权限获取失败");
                                finish();
                                break;
                        }
                    }

                    startApp();
                }
            }
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
