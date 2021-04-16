package com.doit.net.activity;

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

import com.doit.net.utils.FileUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.AccountManage;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.utils.SPUtils;
import com.doit.net.view.MySweetAlertDialog;
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
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Socket socket = new Socket("123.207.136.134", 9010);
//
//                    byte[] bytesReceived = new byte[1024];
//                    //接收到流的数量
//                    int receiveCount;
//
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            OutputStream os = null;
//                            try {
//                                os = socket.getOutputStream();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            PrintWriter pw = new PrintWriter(os);
//                            pw.write("客户端发送信息");
//                            pw.flush();
//                        }
//                    }, 0, 3000);
//
//                    InputStream inputStream = socket.getInputStream();
//                    //循环接收数据
//                    while ((receiveCount = inputStream.read(bytesReceived)) != -1) {
//                        LogUtils.log("socket读取长度：" + receiveCount);
//                    }
//
//
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    LogUtils.log("socket连接失败：" + e.getMessage());
//                }
//            }
//        }).start();

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
        long timeDatum = SPUtils.getLong(TIME_DATUM, Long.valueOf("0"));
        long nowTime = new Date().getTime();
        if (timeDatum == 0) {
            SPUtils.setLong(TIME_DATUM, nowTime);
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
            SPUtils.setLong(TIME_DATUM, nowTime);
            return true;
        }
    }

    /**
     * 是否开启搜寻功能
     */
    private void checkLocMode() {
        boolean ifOpenLocMode = SPUtils.getBoolean(LOC_PREF_KEY, true);

        CacheManager.setLocMode(ifOpenLocMode);
        SPUtils.setBoolean(LOC_PREF_KEY, ifOpenLocMode);
    }

    private void initView() {
        etUserName = findViewById(R.id.etImsi);
        etPassword = findViewById(R.id.etPassword);
        ivUserNameClear = findViewById(R.id.ivUserNameClear);
        ivdPasswordClear = findViewById(R.id.ivdPasswordClear);
        ckRememberPass = findViewById(R.id.ckRememberPass);
        btLogin = findViewById(R.id.btLogin);

        CacheManager.DEVICE_IP = SPUtils.getString(SPUtils.DEVICE_IP,CacheManager.DEFAULT_DEVICE_IP);

        checkLocMode();

        isRemember = SPUtils.getBoolean("remember_password", false);
        if (isRemember) {
            String userName = SPUtils.getString("username", "");
            String Password = SPUtils.getString("password", "");
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
                SPUtils.setBoolean("remember_password", true);
                SPUtils.setString("username", userName);
                SPUtils.setString("password", password);
            } else {
                SPUtils.setBoolean("remember_password", false);
                SPUtils.setString("username", "");
                SPUtils.setString("password", "");
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
