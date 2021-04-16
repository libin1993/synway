package com.doit.net.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.utils.FileUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.AccountManage;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.utils.SPUtils;
import com.doit.net.view.LSettingItem;
import com.doit.net.view.MySweetAlertDialog;
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

public class SystemSettingActivity extends BaseActivity implements EventAdapter.EventCall {
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
    private EditText etDeviceIP;
    private BootstrapButton btEditDeviceIP;

    private long lastRefreshParamTime = 0; //防止频繁刷新参数

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

        tvOnOffLocation.setChecked(SPUtils.getBoolean(LOC_PREF_KEY, true));

        tvStaticIp = findViewById(R.id.tv_static_ip);
        tvStaticIp.setChecked(SPUtils.getBoolean(SET_STATIC_IP, true));
        tvStaticIp.setOnLSettingCheckedChange(setStaticIpSwitch);
        tvStaticIp.setmOnLSettingItemClick(setStaticIpSwitch);

        etDeviceIP = findViewById(R.id.et_device_ip);
        btEditDeviceIP = findViewById(R.id.bt_edit_ip);

        etDeviceIP.setText(CacheManager.DEVICE_IP);
        btEditDeviceIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = etDeviceIP.getText().toString().trim();
                if (TextUtils.isEmpty(ip)) {
                    ToastUtils.showMessage("请输入设备IP");
                    return;
                }

                new MySweetAlertDialog(SystemSettingActivity.this, MySweetAlertDialog.WARNING_TYPE)
                        .setTitleText("设备IP")
                        .setContentText("请确认和设备IP保持一致，否则将导致无法连接！")
                        .setCancelText(SystemSettingActivity.this.getString(R.string.cancel))
                        .setConfirmText(SystemSettingActivity.this.getString(R.string.sure))
                        .showCancelButton(true)
                        .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                            @Override
                            public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                SPUtils.setString(SPUtils.DEVICE_IP, ip);
                                CacheManager.DEVICE_IP = ip;

                            }
                        })
                        .show();
            }
        });


        if (CacheManager.checkDevice(SystemSettingActivity.this)) {
            initView();
        } else {
            ToastUtils.showMessageLong("设备未连接，当前展示的设置都不准确，请等待设备连接后重新进入该界面");
        }

        EventAdapter.register(EventAdapter.REFRESH_SYSTEM, this);

    }

    private void initView() {
        if (CacheManager.getLteEquipConfig() != null) {
            etMaxWindSpeed.setText(CacheManager.getLteEquipConfig().getMaxFanSpeed());
            etMinWindSpeed.setText(CacheManager.getLteEquipConfig().getMinFanSpeed());
            etTempThreshold.setText(CacheManager.getLteEquipConfig().getTempThreshold());
        }

        if (CacheManager.getChannels() != null & CacheManager.getChannels().size() > 0) {
            tvIfAutoOpenRF.setChecked(CacheManager.getChannels().get(0).getAutoOpen().equals("1"));
        }
    }

    private LSettingItem.OnLSettingItemClick settingItemLocSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (tvOnOffLocation.isChecked()) {
                SPUtils.setBoolean(LOC_PREF_KEY, true);
            } else {
                SPUtils.setBoolean(LOC_PREF_KEY, false);
            }

            ToastUtils.showMessage("设置成功，重新登陆生效。");
        }
    };

    private LSettingItem.OnLSettingItemClick settingItemAutoRFSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (!CacheManager.checkDevice(SystemSettingActivity.this)) {
                tvIfAutoOpenRF.setChecked(!tvIfAutoOpenRF.isChecked());
                return;
            }

            LTESendManager.setAutoRF(tvIfAutoOpenRF.isChecked());

            ToastUtils.showMessage("下次开机生效");
        }
    };

    private LSettingItem.OnLSettingItemClick setStaticIpSwitch = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            if (tvStaticIp.isChecked()) {
                SPUtils.setBoolean(SET_STATIC_IP, true);
                ToastUtils.showMessage("已开启自动连接，无需配置WIFI静态IP，以后将自动连接设备");
            } else {
                SPUtils.setBoolean(SET_STATIC_IP, false);
                ToastUtils.showMessageLong("已关闭自动连接，请配置WIFI静态IP，否则将无法连接设备");
            }


        }
    };


    private LSettingItem.OnLSettingItemClick generalAdminAccount = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            generalAdmin();
        }
    };

    private void generalAdmin() {
        String accountFullPath = FileUtils.ROOT_PATH + "FtpAccount/";
        String accountFileName = "account";


        File namelistFile = new File(accountFullPath + accountFileName);
        if (namelistFile.exists()) {
            namelistFile.delete();
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accountFullPath + accountFileName, true)));
            bufferedWriter.write("admin" + "," + "admin" + "," + AccountManage.getAdminRemark() + "\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        new Thread() {
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    if (FTPManager.getInstance().uploadFile(false, accountFullPath, accountFileName)) {
                        ToastUtils.showMessage("生成管理员账号成功");
                    } else {
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

    View.OnClickListener setFanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(SystemSettingActivity.this))
                return;

            LTESendManager.setFanControl(etMaxWindSpeed.getText().toString(), etMinWindSpeed.getText().toString()
                    , etTempThreshold.getText().toString());
        }
    };


    View.OnClickListener resetFreqScanFcnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(SystemSettingActivity.this))
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
        if (!CacheManager.checkDevice(this)) {
            return;
        }
        for (int i = 0; i < CacheManager.getChannels().size(); i++) {
            int index = i;

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LteChannelCfg channel = CacheManager.getChannels().get(index);
                    String fcn = LTESendManager.getCheckedFcn(channel.getBand());
                    if (!TextUtils.isEmpty(fcn)) {
                        LTESendManager.setChannelConfig(channel.getIdx(), fcn, "", "", "", "", "", "");
                        channel.setFcn(fcn);
                    }

                }
            }, index * 200);
        }

    }

    View.OnClickListener refreshClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(SystemSettingActivity.this))
                return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshParamTime > 20 * 1000) {
                LTESendManager.getEquipAndAllChannelConfig();
                lastRefreshParamTime = currentTime;
                ToastUtils.showMessage("下发查询参数成功！");
            } else {
                ToastUtils.showMessage("请勿频繁刷新参数！");
            }


        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* 为解决从后台切回来之后重新打开启动屏及登录界面问题，需要设置点击子activity时强制打开MainActivity
         * 否则会出现在子activity点击返回直接将app切到后台(为防止mainActivity重复加载，已将其设置为singleTop启动) */
        switch (item.getItemId()) {
            case android.R.id.home:
                // 点击返回按钮，退回上一层Activity
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void call(String key, Object val) {
        if (EventAdapter.REFRESH_SYSTEM.equals(key)) {
            mHandler.sendEmptyMessage(0);
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                initView();
            }
        }
    };
}
