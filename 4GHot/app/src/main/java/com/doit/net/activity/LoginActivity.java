package com.doit.net.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.doit.net.model.ImsiMsisdnConvert;
import com.doit.net.utils.FileUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.model.AccountManage;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.model.PrefManage;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.ucsi.R;

import java.io.File;
import java.util.Date;

import static com.doit.net.activity.SystemSettingActivity.LOC_PREF_KEY;


public class LoginActivity extends BaseActivity {

    private CheckBox ckRememberPass;
    private EditText etUserName;
    private EditText etPassword;
    private Button btLogin;
    ImageView ivUserNameClear;
    ImageView ivdPasswordClear;
    boolean isRemember;


    private static final String TIME_DATUM = "TIME_DATUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

//        checkTimeDatum();
//        checkAuthorize();
        initView();
        checkLocalDir();
        initLog();
//        ImsiMsisdnConvert.test();
    }

//    private boolean checkAuthorize() {
//        LicenceUtils.generateAuthorizeInfo(activity);
//        String authorizeCode = LicenceUtils.getAuthorizeCode();
//        if (authorizeCode.equals("")){
//            ToastUtils.showMessageLong(activity, "App未授权，请联系管理员。");
//            LicenceDialog licenceDialog = new LicenceDialog(this);
//            licenceDialog.show();
//            //LicenceManage.investDays(30,activity);
//
//            return false;
//        }
//
//        //以下判断是否过期
//        String dueTime = LicenceUtils.getDueTime();
//        long longDueTime = DateUtils.convert2long(dueTime, DateUtils.LOCAL_DATE_DAY);
//        String nowDate = DateUtils.convert2String(new Date(), DateUtils.LOCAL_DATE_DAY);
//        long nowTime = DateUtils.convert2long(nowDate, DateUtils.LOCAL_DATE_DAY);
//        if (nowTime >= longDueTime){
//            ToastUtils.showMessageLong(activity, "授权已过期，请联系管理员");
//            LicenceDialog licenceDialog = new LicenceDialog(this);
//            licenceDialog.show();
//
//            return false;
//        }
//
//        return true;
//    }

    private void checkLocalDir() {
        File dir = new File(FileUtils.ROOT_PATH);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdir();
        }

        BlackBoxManger.checkBlackBoxFile();
        AccountManage.checkAccountDir();
    }

    private void initLog() {
        LogUtils.initLog(); //必须在UPDATE_FILE_SYS事件注册后，否则电脑端无法显示
    }

    private void getAccountInfoFormDevice() {


    }

    private boolean checkTimeDatum() {
        long timeDatum = PrefManage.getLong(TIME_DATUM, Long.valueOf("0"));
        long nowTime = new Date().getTime();
        if (timeDatum == 0) {
            PrefManage.setLong(TIME_DATUM, nowTime);
            return true;
        }

        if (timeDatum >= nowTime) {
            new MySweetAlertDialog(this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("时间异常")
                    .setContentText("当前系统时间异常，请矫正后再打开App！")
                    .setConfirmText("退出")
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(MySweetAlertDialog sDialog) {
                            System.exit(0);
                        }
                    })
                    .show();

            return false;
        } else {
            PrefManage.setLong(TIME_DATUM, nowTime);
            return true;
        }
    }

    /**
     * 是否开启搜寻功能
     */
    private void checkLocMode() {
        boolean ifOpenLocMode = PrefManage.getBoolean(LOC_PREF_KEY, true);

        CacheManager.setLocMode(ifOpenLocMode);
        PrefManage.setBoolean(LOC_PREF_KEY, ifOpenLocMode);
    }

    private void initView() {
        etUserName = findViewById(R.id.etImsi);
        etPassword = findViewById(R.id.etPassword);
        ivUserNameClear = findViewById(R.id.ivUserNameClear);
        ivdPasswordClear = findViewById(R.id.ivdPasswordClear);
        ckRememberPass = findViewById(R.id.ckRememberPass);
        btLogin = findViewById(R.id.btLogin);

        CacheManager.DEVICE_IP = PrefManage.getString(PrefManage.DEVICE_IP,CacheManager.DEFAULT_DEVICE_IP);

        checkLocMode();

        isRemember = PrefManage.getBoolean("remember_password", false);
        if (isRemember) {
            String userName = PrefManage.getString("username", "");
            String Password = PrefManage.getString("password", "");
            etUserName.setText(userName);
            etPassword.setText(Password);
            ckRememberPass.setChecked(true);
        }

        AccountManage.clearCurrentAccountDB();

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = etUserName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if ("".equals(userName) || "".equals(password)) {
                    ToastUtils.showMessage("密码或账号为空，请重新输入");
                    return;
                }

                AccountManage.getAdminAccoutFromPref();
                if (!userName.equals(AccountManage.getSuperAccount()) && !userName.equals(AccountManage.getAdminAcount())) {

                    new Thread() {
                        public void run() {
                            try {
                                if (FTPManager.getInstance().connect()) {
                                    boolean isDownloadSuccess = FTPManager.getInstance().downloadFile(AccountManage.LOCAL_FTP_ACCOUNT_PATH,
                                            AccountManage.ACCOUNT_FILE_NAME);
                                    if (isDownloadSuccess) {
                                        if (AccountManage.UpdateAccountFromFileToDB()) {
                                            checkAccount();
                                            AccountManage.deleteAccountFile();
                                        } else {
                                            ToastUtils.showMessageLong("从设备获取用户信息失败");
                                        }

                                    }else {
                                        ToastUtils.showMessageLong("从设备获取用户信息失败");
                                    }

                                } else {
                                    ToastUtils.showMessageLong("从设备获取用户信息失败");
                                }
                            } catch (Exception e) {
                                ToastUtils.showMessageLong("从设备获取用户信息失败，请确保与设备网络连接畅通");
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    checkAccount();
                }


            }
        });

        addClearListener(etUserName, ivUserNameClear);
        addClearListener(etPassword, ivdPasswordClear);
    }

    private void checkAccount() {
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (AccountManage.checkAccount(userName, password)) {
            if (ckRememberPass.isChecked()) { // 检查复选框是否被选中
                PrefManage.setBoolean("remember_password", true);
                PrefManage.setString("username", userName);
                PrefManage.setString("password", password);
            } else {
                PrefManage.setBoolean("remember_password", false);
                PrefManage.setString("username", "");
                PrefManage.setString("password", "");
            }

            AccountManage.setCurrentLoginAccount(userName);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            ToastUtils.showMessage("密码或账号错误,请联系管理员！");
        }
    }

    private void addClearListener(final EditText et, final ImageView iv) {
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //如果有输入内容长度大于0那么显示clear按钮
                String str = editable + "";
                if (editable.length() > 0) {
                    iv.setVisibility(View.VISIBLE);
                } else {
                    iv.setVisibility(View.INVISIBLE);
                }
            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });
    }
}
