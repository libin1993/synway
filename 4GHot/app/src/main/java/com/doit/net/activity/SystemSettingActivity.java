package com.doit.net.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.model.DBChannel;
import com.doit.net.utils.FileUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.model.AccountManage;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.model.PrefManage;
import com.doit.net.utils.LSettingItem;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class SystemSettingActivity extends BaseActivity {
    public static String LOC_PREF_KEY = "LOC_PREF_KEY";
    public static String SET_STATIC_IP = "STATIC_IP";
    private LSettingItem tvOnOffLocation;
    private LSettingItem tvIfAutoOpenRF;
    private LSettingItem tvGeneralAdmin;
    private LSettingItem tvStaticIp;

    private BootstrapButton btSetFan;
    private BootstrapEditText etMaxWindSpeed;
    private BootstrapEditText etMinWindSpeed;
    private BootstrapEditText etTempThreshold;

    private BootstrapButton btResetFreqScanFcn;
    private BootstrapButton btRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvOnOffLocation = findViewById(R.id.tvOnOffLocation);
        tvOnOffLocation.setOnLSettingCheckedChange(settingItemLocSwitch);
        tvOnOffLocation.setmOnLSettingItemClick(settingItemLocSwitch);  //点击该行开关以外地方也会切换开关，故覆盖其回调

        tvIfAutoOpenRF = findViewById(R.id.tvIfAutoOpenRF);
        tvIfAutoOpenRF.setOnLSettingCheckedChange(settingItemAutoRFSwitch);
        tvIfAutoOpenRF.setmOnLSettingItemClick(settingItemAutoRFSwitch);

        tvGeneralAdmin = findViewById(R.id.tvGeneralAdmin);
        tvGeneralAdmin.setmOnLSettingItemClick(generalAdminAccount);

        etMaxWindSpeed = findViewById(R.id.etMaxWindSpeed);
        etMinWindSpeed = findViewById(R.id.etMinWindSpeed);
        etTempThreshold = findViewById(R.id.etTempThreshold);
        btSetFan = findViewById(R.id.btSetFan);
        btSetFan.setOnClickListener(setFanClickListener);

        btResetFreqScanFcn = findViewById(R.id.btResetFreqScanFcn);
        btResetFreqScanFcn.setOnClickListener(resetFreqScanFcnClickListener);

        btRefresh = findViewById(R.id.btRefresh);
        btRefresh.setOnClickListener(refreshClickListener);

        tvOnOffLocation.setChecked(PrefManage.getBoolean(LOC_PREF_KEY, true));

        tvStaticIp = findViewById(R.id.tv_static_ip);
        tvStaticIp.setChecked(PrefManage.getBoolean(SET_STATIC_IP,true));
        tvStaticIp.setOnLSettingCheckedChange(setStaticIpSwitch);
        tvStaticIp.setmOnLSettingItemClick(setStaticIpSwitch);

        if (CacheManager.checkDevice(this)){
            etMaxWindSpeed.setText(CacheManager.getLteEquipConfig().getMaxFanSpeed());
            etMinWindSpeed.setText(CacheManager.getLteEquipConfig().getMinFanSpeed());
            etTempThreshold.setText(CacheManager.getLteEquipConfig().getTempThreshold());

            tvIfAutoOpenRF.setChecked(CacheManager.getChannels().get(0).getAutoOpen().equals("1"));
        }else{
            ToastUtils.showMessageLong("设备未连接，当前展示的设置都不准确，请等待设备连接后重新进入该界面");
        }

    }

    private LSettingItem.OnLSettingItemClick settingItemLocSwitch = new LSettingItem.OnLSettingItemClick(){
        @Override
        public void click(LSettingItem item) {
            PrefManage.setBoolean(LOC_PREF_KEY, tvOnOffLocation.isChecked());

            ToastUtils.showMessage( "设置成功，重新登陆生效。");
        }
    };

    private LSettingItem.OnLSettingItemClick settingItemAutoRFSwitch = new LSettingItem.OnLSettingItemClick(){
        @Override
        public void click(LSettingItem item) {
            if(!CacheManager.checkDevice(SystemSettingActivity.this)){
                tvIfAutoOpenRF.setChecked(!tvIfAutoOpenRF.isChecked());
                return;
            }

            ProtocolManager.setAutoRF(tvIfAutoOpenRF.isChecked());

            ToastUtils.showMessage( "下次开机生效");
        }
    };

    private LSettingItem.OnLSettingItemClick setStaticIpSwitch = new LSettingItem.OnLSettingItemClick(){
        @Override
        public void click(LSettingItem item) {
            if (tvStaticIp.isChecked()){
                PrefManage.setBoolean(SET_STATIC_IP, true);
                ToastUtils.showMessage("已开启自动连接，无需配置WIFI静态IP，以后将自动连接设备");
            }else {
                PrefManage.setBoolean(SET_STATIC_IP, false);
                ToastUtils.showMessageLong("已关闭自动连接，请配置WIFI静态IP，否则将无法连接设备");
            }


        }
    };


    private LSettingItem.OnLSettingItemClick generalAdminAccount = new LSettingItem.OnLSettingItemClick(){
        @Override
        public void click(LSettingItem item) {
            generalAdmin();
        }
    };

    private void generalAdmin() {
        String accountFullPath = FileUtils.ROOT_PATH+"FtpAccount/";
        String accountFileName = "account";


        File namelistFile = new File(accountFullPath+accountFileName);
        if (namelistFile.exists()){
            namelistFile.delete();
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accountFullPath+accountFileName,true)));
            bufferedWriter.write("admin"+","+"admin"+ "," + AccountManage.getAdminRemark()+"\n");
            bufferedWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {}
            }
        }

        new Thread() {
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    if (FTPManager.getInstance().uploadFile(false,accountFullPath, accountFileName)){
                        ToastUtils.showMessage( "生成管理员账号成功");
                    }else {
                        ToastUtils.showMessage("生成管理员账号出错");
                    }
                    AccountManage.deleteAccountFile();
                } catch (Exception e) {
                    ToastUtils.showMessage("生成管理员账号出错");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    View.OnClickListener setFanClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(!CacheManager.checkDevice(SystemSettingActivity.this))
                return;

            ProtocolManager.setFanControl(etMaxWindSpeed.getText().toString(), etMinWindSpeed.getText().toString()
                    ,etTempThreshold.getText().toString());
        }
    };


    View.OnClickListener resetFreqScanFcnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(!CacheManager.checkDevice(SystemSettingActivity.this))
                return;

            new SweetAlertDialog(SystemSettingActivity.this, WARNING_TYPE)
                    .setTitleText("提示")
                    .setContentText("开机搜网列表将被重置，确定吗?")
                    .setCancelText(SystemSettingActivity.this.getString(R.string.cancel))
                    .setConfirmText(SystemSettingActivity.this.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            resetFreqScanFcn();
                            sweetAlertDialog.dismiss();
                        }
                    }).show();


        }
    };

    private void resetFreqScanFcn() {
        if (!CacheManager.checkDevice(this)){
            return;
        }
        for (int i = 0; i < CacheManager.getChannels().size(); i++) {
            int index = i;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LteChannelCfg channel = CacheManager.getChannels().get(index);
                    String fcn = ProtocolManager.getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn)) {
                        ProtocolManager.setChannelConfig(channel.getIdx(), fcn ,
                                "", "", "", "", "", "");
                        channel.setFcn(fcn);
                    }

                }
            }, index * 200);
        }

    }

    View.OnClickListener refreshClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(!CacheManager.checkDevice(SystemSettingActivity.this))
                return;

            etMaxWindSpeed.setText(CacheManager.getLteEquipConfig().getMaxFanSpeed());
            etMinWindSpeed.setText(CacheManager.getLteEquipConfig().getMinFanSpeed());
            etTempThreshold.setText(CacheManager.getLteEquipConfig().getTempThreshold());

            tvIfAutoOpenRF.setChecked(CacheManager.getChannels().get(0).getAutoOpen().equals("1"));

            if (PrefManage.getBoolean(LOC_PREF_KEY, true)){
                tvOnOffLocation.setChecked(true);
            }else{
                tvOnOffLocation.setChecked(false);
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* 为解决从后台切回来之后重新打开启动屏及登录界面问题，需要设置点击子activity时强制打开MainActivity
         * 否则会出现在子activity点击返回直接将app切到后台(为防止mainActivity重复加载，已将其设置为singleTop启动) */
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
