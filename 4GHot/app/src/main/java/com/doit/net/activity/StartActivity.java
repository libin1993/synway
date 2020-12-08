package com.doit.net.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.doit.net.utils.PermissionUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.ucsi.BuildConfig;
import com.doit.net.ucsi.R;


public class StartActivity extends BaseActivity {
    View view;
    private final int PERMISSION_REQUEST_CODE = 1;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION,};
    private String[][] permissionArray = {{Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储"},{Manifest.permission.READ_PHONE_STATE, "读取手机状态"},{Manifest.permission.ACCESS_COARSE_LOCATION, "定位"}};

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkPermissions();
	}

    @Override
    protected void onRestart() {
        super.onRestart();
        requestPermissions();
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



    private void checkPermissions() {
        if (PermissionUtils.getInstance().hasPermission(this, permissions)) {
            startApp();
        } else {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("检测到存在未授权的权限")
                    .setMessage("请务必同意授权这些权限，否则程序将无法打开！")
                    .setPositiveButton("确 定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(StartActivity.this, permissions,
                                    PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("取 消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setCancelable(false)
                    .show();

        }
    }

    private void requestPermissions() {
        if (PermissionUtils.getInstance().hasPermission(this, permissions)) {
            startApp();
        } else {
            ActivityCompat.requestPermissions(StartActivity.this, permissions,
                    PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (String[] strings : permissionArray) {
                if (!PermissionUtils.getInstance().hasPermission(this, strings[0])) {
                    PermissionUtils.getInstance().showPermissionDialog(StartActivity.this,
                            strings[0], strings[1], new PermissionUtils.OnPermissionListener() {
                                @Override
                                public void onCancel() {
                                    finish();
                                }

                                @Override
                                public void onReQuest() {
                                   requestPermissions();
                                }
                            });
                    return;
                }
            }
            startApp();
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }
}
