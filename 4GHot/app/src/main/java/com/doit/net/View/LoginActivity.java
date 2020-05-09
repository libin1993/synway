package com.doit.net.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.doit.net.Model.AccountManage;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.FTPManager;
import com.doit.net.Model.LicenceManage;
import com.doit.net.Model.PrefManage;
import com.doit.net.Utils.DateUtil;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.ucsi.R;

import java.io.File;
import java.util.Date;


public class LoginActivity extends Activity {
    private Activity activity = this;

    private View view;
    private CheckBox ckRememberPass;
    private EditText etUserName;
    private EditText etPassword;
    private Button btLogin;
    ImageView ivUserNameClear;
    ImageView ivdPasswordClear;
    boolean isRemember;

    private Thread getAccountThread;
    private final String TIME_DATUM = "TIME_DATUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = View.inflate(this, R.layout.activity_login, null);
        setContentView(view);

//        checkTimeDatum();
//        checkAuthorize();
        initView();
        checkLocalDir();
        initLog();
    }

    private boolean checkAuthorize() {
        LicenceManage.generateAuthorizeInfo(activity);
        String authorizeCode = LicenceManage.getAuthorizeCode();
        if (authorizeCode.equals("")){
            ToastUtils.showMessageLong(activity, "App未授权，请联系管理员。");
            LicenceDialog licenceDialog = new LicenceDialog(this);
            licenceDialog.show();
            //LicenceManage.investDays(30,activity);

            return false;
        }

        //以下判断是否过期
        String dueTime = LicenceManage.getDueTime();
        long longDueTime = DateUtil.convert2long(dueTime,DateUtil.LOCAL_DATE_DAY);
        String nowDate = DateUtil.convert2String(new Date(),DateUtil.LOCAL_DATE_DAY);
        long nowTime = DateUtil.convert2long(nowDate,DateUtil.LOCAL_DATE_DAY);
        if (nowTime >= longDueTime){
            ToastUtils.showMessageLong(activity, "授权已过期，请联系管理员");
            LicenceDialog licenceDialog = new LicenceDialog(this);
            licenceDialog.show();

            return false;
        }

        return true;
    }

    private void checkLocalDir() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/4GHotspot/");
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdir();
        }

        BlackBoxManger.checkBlackBoxFile();
        AccountManage.checkAccountDir();
    }

    private void initLog(){
        UtilBaseLog.initLog(); //必须在UPDATE_FILE_SYS事件注册后，否则电脑端无法显示
    }

    private void getAccountInfoFormDevice() {
        getAccountThread = new Thread() {
            public void run() {
                try {
                    if (FTPManager.getInstance().connect()){
                        AccountManage.clearCurrentAccountDB();
                        AccountManage.downloadAccountFile();
                        if (AccountManage.UpdateAccountFromFileToDB()){
                            //AccountManage.getAdminAccoutFromPref();
                            AccountManage.setGetAccountFromDevFlag(true);
                        }else{
                            ToastUtils.showMessageLong(getBaseContext(), "从设备获取用户信息失败");
                            AccountManage.setGetAccountFromDevFlag(false);
                        }
                        AccountManage.deleteAccountFile();
                    }else{
                        ToastUtils.showMessageLong(getBaseContext(), "从设备获取用户信息失败");
                    }
                } catch (Exception e) {
                    ToastUtils.showMessageLong(getBaseContext(), "从设备获取用户信息失败，请确保与设备网络连接畅通");
                    e.printStackTrace();
                }
            }};
        getAccountThread.start();
        try {
            getAccountThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTimeDatum() {
        long timeDatum =  PrefManage.getLong(TIME_DATUM, Long.valueOf("0"));
        long nowTime = new Date().getTime();
        if (timeDatum == 0){
            PrefManage.setLong(TIME_DATUM, nowTime);
            return true;
        }

        if (timeDatum >= nowTime){
            new MySweetAlertDialog(this,MySweetAlertDialog.WARNING_TYPE)
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
        }else{
            PrefManage.setLong(TIME_DATUM, nowTime);
            return true;
        }
    }

    private void checkLocMode() {
        boolean ifOpenLocMode = PrefManage.getBoolean("LOC_PREF_KEY", true);

        CacheManager.setLocMode(ifOpenLocMode);
        PrefManage.setBoolean("LOC_PREF_KEY", ifOpenLocMode);
    }

    private void initView(){
        etUserName = (EditText) findViewById(R.id.etImsi);
        etPassword = (EditText) findViewById(R.id.etPassword);
        ivUserNameClear = (ImageView) findViewById(R.id.ivUserNameClear);
        ivdPasswordClear = (ImageView) findViewById(R.id.ivdPasswordClear);
        ckRememberPass = (CheckBox) findViewById(R.id.ckRememberPass);
        btLogin = (Button) findViewById(R.id.btLogin);

        isRemember = PrefManage.getBoolean("remember_password", false);
        if (isRemember) {
            String userName = PrefManage.getString("username", "");
            String Password = PrefManage.getString("password", "");
            etUserName.setText(userName);
            etPassword.setText(Password);
            ckRememberPass.setChecked(true);
        }

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean tmpCheckRes = checkTimeDatum();
//                if (!tmpCheckRes){
//                    return;
//                }
//                tmpCheckRes = checkAuthorize();
//                if (!tmpCheckRes){
//                    return;
//                }

                String userName = etUserName.getText().toString();
                String password = etPassword.getText().toString();

                if ("".equals(userName) || "".equals(password)){
                    ToastUtils.showMessage(LoginActivity.this, "密码或账号为空，请重新输入");
                    return;
                }

                AccountManage.getAdminAccoutFromPref();
                if (!userName.equals(AccountManage.getSuperAccount()) && !userName.equals(AccountManage.getAdminAcount())){
                    //clearLocalUserInfo();
                    getAccountInfoFormDevice();
                    if (!AccountManage.hasGetAccountFromDev() && !userName.equals(AccountManage.getSuperAccount()) ){
                        return;
                    }
                }

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
                    checkLocMode();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    ToastUtils.showMessage(LoginActivity.this, "密码或账号错误,请联系管理员！");
                }
            }
        });

        addClearListener(etUserName,ivUserNameClear);
        addClearListener(etPassword,ivdPasswordClear);
    }

    private void addClearListener(final EditText et , final ImageView iv){
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
                String str = editable + "" ;
                if (editable.length() > 0){
                    iv.setVisibility(View.VISIBLE);
                }else{
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
