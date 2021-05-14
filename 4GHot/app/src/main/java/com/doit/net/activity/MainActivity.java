package com.doit.net.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.TextView;

import com.doit.net.sockets.OnSocketChangedListener;
import com.doit.net.sockets.ServerSocketUtils;
import com.doit.net.sockets.DatagramSocketUtils;
import com.doit.net.utils.PermissionUtils;
import com.doit.net.view.BatteryView;
import com.doit.net.adapter.MainTabLayoutAdapter;
import com.doit.net.application.MyApplication;
import com.doit.net.base.BaseActivity;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.BatteryBean;
import com.doit.net.bean.DeviceState;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.TabEntity;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.utils.AccountManage;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.FTPManager;
import com.doit.net.utils.LicenceUtils;
import com.doit.net.utils.SPUtils;
import com.doit.net.utils.VersionManage;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.FTPServerUtils;
import com.doit.net.utils.FileUtils;
import com.doit.net.view.MySweetAlertDialog;
import com.doit.net.utils.NetWorkUtils;
import com.doit.net.utils.LogUtils;

import com.doit.net.fragment.AppFragment;
import com.doit.net.view.LicenceDialog;
import com.doit.net.fragment.LocationFragment;
import com.doit.net.fragment.NameListFragment;
import com.doit.net.receiver.NetworkChangeReceiver;
import com.doit.net.fragment.StartPageFragment;
import com.doit.net.fragment.UeidFragment;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.doit.net.activity.SystemSettingActivity.SET_STATIC_IP;

public class MainActivity extends BaseActivity implements TextToSpeech.OnInitListener, EventAdapter.EventCall {
    private ViewPager mViewPager;
    private List<BaseFragment> mTabs = new ArrayList<BaseFragment>();
    private CommonTabLayout tabLayout;
    private MainTabLayoutAdapter adapter;

    private List<String> listTitles = new ArrayList<>();
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    private TextToSpeech textToSpeech; // TTS对象

    private MySweetAlertDialog mProgressDialog;

    private boolean heartbeatCount = false;
    private boolean isCheckDeviceStateThreadRun = true;
    private boolean lowBatteryWarn = true;  //低电量提醒


    private ImageView ivDeviceState;
    Animation viewAnim = new AlphaAnimation(0, 1);
    private ImageView ivWifiState;
    private ImageView ivBatteryLevel;
    private BatteryView batteryView;
    private TextView tvBattery;
    private ImageView ivCharging;
    private FrameLayout flBattery;
    private TextView tvRemainTime;
    private MySweetAlertDialog batteryWarningDialog = null;
    Animation batteryViewAnit = new AlphaAnimation(0, 1);


    //handler消息
    private final int TIP_MSG = 2;
    private final int SHOW_PROGRESS = 3;
    private final int CLOSE_PROGRESS = 4;
    private final int UPDATE_DEVICE_STATE = 5;
    private final int REFRESH_FILE_SYS = 6;
    private final int SPEAK = 7;
    private final int UPDATE_BATTERY = 8;
    private final int ADD_BLACKBOX = 9;
    private final int CHANGE_TAB = 10;
    private final int POWER_START = 11;
    private final int CHECK_LICENCE = 13;
    private final int BATTERY_STATE = 14;
    private final int HEARTBEAT_RPT = 15;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            LogUtils.log("没有读取手机权限");
        }

        initView();
        initOtherWork();

    }

    private void initOtherWork() {
        new Thread() {
            @Override
            public void run() {
                checkDataDir();
                initEvent();
                initWifiChangeReceive();
                setData();
                startCheckDeviceState();
                initNetWork();
                initSpeech();
                initFTP();
                initBlackBox();
            }
        }.start();
    }


    private void setData() {
        if (NetWorkUtils.getNetworkState()) {
            LogUtils.log("wifi连接成功");
            if (CacheManager.deviceState.getDeviceState().equals(DeviceState.WIFI_DISCONNECT)) {
                //只有从wifi未连接到连接才出现这种状态
                CacheManager.deviceState.setDeviceState(DeviceState.WAIT_SOCKET);
            }
        } else {
            LogUtils.log("wifi断开连接");
        }
    }


    private void initBlackBox() {

        if (VersionManage.isArmyVer()) {
            return;
        }
        BlackBoxManger.setCurrentAccount(AccountManage.getCurrentLoginAccount());
        BlackBoxManger.initBlx();
        BlackBoxManger.recordOperation(BlackBoxManger.LOGIN + AccountManage.getCurrentLoginAccount());
    }

    private void initNetWork() {

        ServerSocketUtils.getInstance().startTCP(new OnSocketChangedListener() {
            @Override
            public void onChange() {
                CacheManager.deviceState.setDeviceState(DeviceState.ON_INIT);
                heartbeatCount = false;    //一旦发现是连接就重置此标志以设置所有配置
                //设备重启（重连）后需要重新检查设置默认参数
                CacheManager.hasSetDefaultParam = false;
                CacheManager.resetState();
            }
        });

    }

    private void initFTP() {
        File f = new File(FileUtils.ROOT_PATH);
        if (!f.exists())
            f.mkdir();

        FTPServerUtils.getInstance().copyConfigFile(R.raw.users, FileUtils.ROOT_PATH + "users.properties", getBaseContext());
        FTPServerUtils.getInstance().startFTPServer();
    }

    private void initProgressDialog() {
        mProgressDialog = new MySweetAlertDialog(MainActivity.this, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("Loading...");
        mProgressDialog.setCancelable(false);
    }

    private void initSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    private void initWifiChangeReceive() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void initView() {
        setOverflowShowingAlways();
        mViewPager = findViewById(R.id.vpTabPage);
        tabLayout = findViewById(R.id.tablayout);

        ivWifiState = findViewById(R.id.ivWifiState);
        ivWifiState.setOnClickListener(wifiSystemSetting);

        ivDeviceState = findViewById(R.id.ivDeviceState);


        ivBatteryLevel = findViewById(R.id.ivBatteryLevel);
        batteryView = findViewById(R.id.battery_view);
        tvRemainTime = findViewById(R.id.tv_remain_time);
        tvBattery = findViewById(R.id.tv_battery);
        ivCharging = findViewById(R.id.iv_charging);
        flBattery = findViewById(R.id.fl_battery);


        initTabs();
        initProgressDialog();

    }

    private void initEvent() {
        EventAdapter.register(EventAdapter.FOUND_BLACK_NAME, this);
        EventAdapter.register("TIP_MSG", this);
        EventAdapter.register("SYS_RPT", this);
        EventAdapter.register("SHOW_PROGRESS", this);
        EventAdapter.register("CLOSE_PROGRESS", this);
        EventAdapter.register(EventAdapter.SPEAK, this);
        EventAdapter.register(EventAdapter.UPDATE_FILE_SYS, this);
        EventAdapter.register(EventAdapter.UPDATE_BATTERY, this);
        EventAdapter.register(EventAdapter.ADD_BLACKBOX, this);
        EventAdapter.register(EventAdapter.CHANGE_TAB, this);
        EventAdapter.register(EventAdapter.WIFI_CHANGE, this);
        EventAdapter.register(EventAdapter.POWER_START, this);
        EventAdapter.register(EventAdapter.HEARTBEAT_RPT, this);
        EventAdapter.register(EventAdapter.BATTERY_STATE, this);
        EventAdapter.register(EventAdapter.INIT_SUCCESS, this);

    }

    private void checkDataDir() {
        String dataDir = FileUtils.ROOT_PATH;
        File file = new File(dataDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        String upgradePath = dataDir + "upgrade";
        file = new File(upgradePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, upgradePath);

        String exportPath = dataDir + "export";
        file = new File(exportPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, exportPath);
    }


    View.OnClickListener wifiSystemSetting = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        }
    };

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    private void clearDataDir() {
        AccountManage.deleteAccountFile();
    }

    private void initTabs() {
        List<Integer> listSelectIcon = new ArrayList<>();
        List<Integer> listUnselectIcon = new ArrayList<>();

        if (VersionManage.isArmyVer()) {
            listTitles.add("侦码");
            if (CacheManager.getLocMode()) {
                listTitles.add("搜寻");
            }
            listTitles.add("设置");

            listSelectIcon.add(R.drawable.detect_lable_select);
            if (CacheManager.getLocMode()) {
                listSelectIcon.add(R.drawable.location_lable_select);
            }
            listSelectIcon.add(R.drawable.setting_lable_select);

            listUnselectIcon.add(R.drawable.detect_lable_unselect);
            if (CacheManager.getLocMode()) {
                listUnselectIcon.add(R.drawable.location_lable_unselect);
            }
            listUnselectIcon.add(R.drawable.setting_lable_unselect);

            mTabs.add(new StartPageFragment());
            if (CacheManager.getLocMode()) {
                mTabs.add(new LocationFragment());
            }
            mTabs.add(new AppFragment());
        } else {
            listTitles.add("侦码");
            if (CacheManager.getLocMode()) {
                listTitles.add("搜寻");
            }
            listTitles.add("名单");
            listTitles.add("设置");

            listSelectIcon.add(R.drawable.detect_lable_select);
            if (CacheManager.getLocMode()) {
                listSelectIcon.add(R.drawable.location_lable_select);
            }
            listSelectIcon.add(R.mipmap.name_lable_select);
            listSelectIcon.add(R.drawable.setting_lable_select);

            listUnselectIcon.add(R.drawable.detect_lable_unselect);
            if (CacheManager.getLocMode()) {
                listUnselectIcon.add(R.drawable.location_lable_unselect);
            }
            listUnselectIcon.add(R.mipmap.name_lable_unselect);
            listUnselectIcon.add(R.drawable.setting_lable_unselect);

            mTabs.add(new StartPageFragment());
            if (CacheManager.getLocMode()) {
                mTabs.add(new LocationFragment());
            }
            mTabs.add(new NameListFragment());
            mTabs.add(new AppFragment());
        }

        for (int i = 0; i < listTitles.size(); i++) {
            mTabEntities.add(new TabEntity(listTitles.get(i), listSelectIcon.get(i), listUnselectIcon.get(i)));
        }

        adapter = new MainTabLayoutAdapter(getSupportFragmentManager(), mTabs, listTitles);
        mViewPager.setOffscreenPageLimit(mTabEntities.size());
        mViewPager.setAdapter(adapter);

        tabLayout.setIndicatorAnimEnable(false);
        tabLayout.setTabData(mTabEntities);

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.setCurrentTab(position);
                mTabs.get(position).onFocus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new MySweetAlertDialog(this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.exit_app))
                    .setContentText(getString(R.string.tip_07))
                    .showCancelButton(true)
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(MySweetAlertDialog sDialog) {
                            /* 可以选择在onDestroy里做回收工作，但是在这里更稳 */
                            appExit();
                            //finish();   //finish()会导致onDestroy()被调用，exit不会
                            System.exit(0);
                        }
                    })
                    .show();
        }
        return true;
    }

    private void appExit() {
        //程序退出停止掉定位
        if (CacheManager.getLocState()) {
            CacheManager.stopCurrentLoc();
            CacheManager.resetState();
        }

        //BlackBoxManger.uploadCurrentBlxFile(); //会卡顿一段时间，体验很差
        clearDataDir();

        unregisterReceiver(networkChangeReceiver);

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        isCheckDeviceStateThreadRun = false;
        FTPServerUtils.getInstance().stopFTP();
        FTPManager.getInstance().closeFTP();
        LogUtils.unInitLog();
        finish();
        System.exit(0);
    }

    private void startCheckDeviceState() {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (isCheckDeviceStateThreadRun) {
                    Message message = mHandler.obtainMessage();
                    message.what = UPDATE_DEVICE_STATE;
                    mHandler.sendMessage(message);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * 不显示menu菜单键
     */
    private void setOverflowShowingAlways() {
        try {
            // true if a permanent menu key is present, false otherwise.
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NetworkChangeReceiver networkChangeReceiver;


    private void turnToUeidPage() {
        LogUtils.log("开启侦码页面");
        mTabs.set(0, new UeidFragment());
        adapter.exchangeFragment();
    }

    @Override
    public void onInit(int status) {
        LogUtils.log("语音播报初始化：" + status);
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setPitch(0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.setSpeechRate(1f);
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            LogUtils.log("语音播报中文：" + result);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                ToastUtils.showMessage(R.string.tip_08);
                SPUtils.supportPlay = false;
            } else {
                SPUtils.supportPlay = true;
            }
        }
    }

    public void notifyUpdateFileSystem(String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);

        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        this.getApplication().sendBroadcast(intent);
    }

    private void wifiChangeEvent() {
        if (NetWorkUtils.getNetworkState()) {
            if (CacheManager.deviceState.getDeviceState().equals(DeviceState.WIFI_DISCONNECT)) {
                CacheManager.deviceState.setDeviceState(DeviceState.WAIT_SOCKET);
            } //只有从wifi未连接到连接才出现这种状态

            downloadAccount();
            initUDP();  //重连wifi后udp发送ip、端口
        } else {
            ToastUtils.showMessageLong("网络连接已断开！请检查网络是否正常连接！");
            CacheManager.deviceState.setDeviceState(DeviceState.WIFI_DISCONNECT);

            CacheManager.resetState();
        }

    }

    /**
     * 下载账户
     */
    private void downloadAccount() {
        new Thread() {
            public void run() {
                try {
                    if (FTPManager.getInstance().connect()) {
                        boolean isDownloadSuccess = FTPManager.getInstance().downloadFile(AccountManage.LOCAL_FTP_ACCOUNT_PATH,
                                AccountManage.ACCOUNT_FILE_NAME);
                        if (isDownloadSuccess) {
                            AccountManage.UpdateAccountFromFileToDB();
                            AccountManage.deleteAccountFile();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 创建DatagramSocket
     */
    private void initUDP() {

        if (!SPUtils.getBoolean(SET_STATIC_IP, true)) {     //是否设置自动连接
            return;
        }

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!CacheManager.deviceState.getDeviceState().equals(DeviceState.NORMAL)) {
                    sendData();
                } else {
                    DatagramSocketUtils.getInstance().closeSocket();
                    timer.cancel();
                }
            }
        };

        timer.schedule(timerTask,
                0,
                5000);//周期时间

    }

    /**
     * 发送数据
     */
    public void sendData() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ip", NetWorkUtils.getWIFILocalIpAddress(MyApplication.mContext));
            jsonObject.put("port", DatagramSocketUtils.UDP_LOCAL_PORT);
            jsonObject.put("id", DatagramSocketUtils.SEND_LOCAL_IP);
            jsonObject.put("ok", true);

            String data = jsonObject.toString();
            LogUtils.log("发送ip:" + data);
            DatagramSocketUtils.getInstance().sendData(data);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void updateStatusBar(String deviceState) {
        switch (deviceState) {
            case DeviceState.WIFI_DISCONNECT:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_disconnect));

                ivBatteryLevel.clearAnimation();
                batteryViewAnit.cancel();
                ivBatteryLevel.setVisibility(View.GONE);

                flBattery.setVisibility(View.GONE);
                tvBattery.setVisibility(View.GONE);
                tvRemainTime.setVisibility(View.GONE);
                CacheManager.isReportBattery = false;

                viewAnim.cancel();
                ivDeviceState.clearAnimation();
                ivDeviceState.setVisibility(View.GONE);
                break;

            case DeviceState.WAIT_SOCKET:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));

                ivBatteryLevel.clearAnimation();
                batteryViewAnit.cancel();
                ivBatteryLevel.setVisibility(View.GONE);

                flBattery.setVisibility(View.GONE);
                tvBattery.setVisibility(View.GONE);
                tvRemainTime.setVisibility(View.GONE);
                CacheManager.isReportBattery = false;

                viewAnim.cancel();
                ivDeviceState.clearAnimation();
                ivDeviceState.setVisibility(View.GONE);

                break;

            case DeviceState.ON_INIT:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));
                ivBatteryLevel.setVisibility(View.GONE);
                CacheManager.isReportBattery = false;

                flBattery.setVisibility(View.GONE);
                tvBattery.setVisibility(View.GONE);
                tvRemainTime.setVisibility(View.GONE);

                ivDeviceState.setImageDrawable(getDrawable(R.drawable.small_device_icon));
                ivDeviceState.setVisibility(View.VISIBLE);
                if (!viewAnim.hasStarted() || viewAnim.hasEnded()) {
                    viewAnim.setDuration(900);   //时间毫秒
                    viewAnim.setInterpolator(new LinearInterpolator());
                    viewAnim.setRepeatMode(Animation.REVERSE);   //播放次序为倒叙
                    viewAnim.setRepeatCount(-1);   //无限
                    ivDeviceState.startAnimation(viewAnim);
                }
                break;

            case DeviceState.NORMAL:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));

                ivDeviceState.setVisibility(View.VISIBLE);
                viewAnim.cancel();
                ivDeviceState.setImageDrawable(getDrawable(R.drawable.small_device_icon));
                ivDeviceState.clearAnimation();

                //ivSyncError.setVisibility(View.GONE);
                break;
            default:
        }
    }

    private void lowBatteryWarnning(String content) {
        if (batteryWarningDialog == null) {
            batteryWarningDialog = new MySweetAlertDialog(this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.low_power))
                    .setContentText(content)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(MySweetAlertDialog mySweetAlertDialog) {
                            batteryWarningDialog.dismiss();
                        }
                    });
        }

        EventAdapter.call(EventAdapter.SPEAK, "电池电量过低");

        if (!batteryWarningDialog.isShowing()) {
            batteryWarningDialog.show();
        }

    }

    private void processBattery(String level) {
        if (CacheManager.isReportBattery) {
            ivBatteryLevel.setVisibility(View.GONE);
            return;
        } else {
            ivBatteryLevel.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(level)) {
            return;
        }

        int voltage = Integer.parseInt(level);

        final int LEVEL1 = 9112;
        final int LEVEL2 = 9800;
        final int LEVEL3 = 10380;
        final int LEVEL4 = 11026;
        final int LEVEL5 = 11550;

        if (voltage <= 0)
            return;

        if (voltage >= LEVEL5) {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level6));
            //batteryViewAnit.cancel();
            ivBatteryLevel.clearAnimation();
        } else if (voltage >= LEVEL4) {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level5));
            ivBatteryLevel.clearAnimation();
            //batteryViewAnit.cancel();
        } else if (voltage >= LEVEL3) {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level4));
            batteryViewAnit.cancel();
            ivBatteryLevel.clearAnimation();
        } else if (voltage >= LEVEL2) {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level3));
            batteryViewAnit.cancel();
            ivBatteryLevel.clearAnimation();
        } else if (voltage >= LEVEL1) {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level2));
            batteryViewAnit.cancel();
            ivBatteryLevel.clearAnimation();
        } else {
            ivBatteryLevel.setImageDrawable(getResources().getDrawable(R.drawable.battery_level1));

            if (!batteryViewAnit.hasStarted() || batteryViewAnit.hasEnded()) {
                batteryViewAnit.setDuration(900);   //时间毫秒
                batteryViewAnit.setInterpolator(new LinearInterpolator());
                batteryViewAnit.setRepeatMode(Animation.REVERSE);   //播放次序为倒叙
                batteryViewAnit.setRepeatCount(-1);   //无限
                //batteryViewAnit.start();
                ivBatteryLevel.startAnimation(batteryViewAnit);
            }

            lowBatteryWarnning(getString(R.string.low_power_warning));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LicenceDialog.CAMERA_REQUEST_CODE && PermissionUtils.getInstance().
                hasPermission(MainActivity.this, Manifest.permission.CAMERA)) {
            startActivity(new Intent(MainActivity.this, ScanCodeActivity.class));

        }

    }

    @Override
    public void call(String key, final Object val) {
        if ("TIP_MSG".equals(key)) {
            Message msg = new Message();
            msg.what = TIP_MSG;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.SHOW_PROGRESS.equals(key)) {
            Message msg = new Message();
            msg.what = SHOW_PROGRESS;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.CLOSE_PROGRESS.equals(key)) {
            Message msg = new Message();
            msg.what = CLOSE_PROGRESS;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.UPDATE_FILE_SYS.equals(key)) {
            Message msg = new Message();
            msg.what = REFRESH_FILE_SYS;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.SPEAK.equals(key)) {
            Message msg = new Message();
            msg.what = SPEAK;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.UPDATE_BATTERY.equals(key)) {
            Message msg = new Message();
            msg.what = UPDATE_BATTERY;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.ADD_BLACKBOX.equals(key)) {
            Message msg = new Message();
            msg.what = ADD_BLACKBOX;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.CHANGE_TAB.equals(key)) {
            Message msg = new Message();
            msg.what = CHANGE_TAB;
            msg.obj = val;
            mHandler.sendMessage(msg);
        } else if (EventAdapter.WIFI_CHANGE.equals(key)) {
            wifiChangeEvent();
        } else if (EventAdapter.POWER_START.equals(key)) {
            mHandler.sendEmptyMessage(POWER_START);
        } else if (EventAdapter.HEARTBEAT_RPT.equals(key)) {
            if (!heartbeatCount) {
                LogUtils.log("首次下发查询以获取小区信息：");
                LTESendManager.getEquipAndAllChannelConfig();
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        ProtocolManager.setBlackList("1", "");  //防止上报其他手机设置的黑名单，就查上来删掉
//                    }
//                }, 1000);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        LTESendManager.setFTPConfig(); //设置ftp配置
                    }
                }, 1000);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        LTESendManager.setNowTime();
                    }
                }, 2000);


                if (CacheManager.checkLicense) {
                    CacheManager.checkLicense = false;
                    checkLicence();
                }
                heartbeatCount = true;
            }

            if (!CacheManager.hasPressStartButton()) {
                mHandler.sendEmptyMessage(HEARTBEAT_RPT);
            }

        } else if (EventAdapter.INIT_SUCCESS.equals(key)) {
            if (!CacheManager.hasSetDefaultParam && CacheManager.getChannels().size() > 0) {
                CacheManager.hasSetDefaultParam = true;

                if (!CacheManager.getLocState()) {     //已设置定位模式，不能设置别的模式
                    if (VersionManage.isArmyVer()){
                        LTESendManager.setActiveMode("2");
                    }else {
                        LTESendManager.setActiveMode("0");
                    }
                }

                if (VersionManage.isArmyVer()) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            CacheManager.setLocalWhiteList("on");
                        }
                    }, 1000);
                }


//                if (VersionManage.isPoliceVer() && !CacheManager.getLocState()) {
//                    CacheManager.setCurrentBlackList();
//                }


                LTESendManager.saveDefaultFcn();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        LTESendManager.setDefaultArfcnsAndPwr();
                    }
                }, 1500);

                CacheManager.deviceState.setDeviceState(DeviceState.NORMAL);

            }

        } else if (EventAdapter.BATTERY_STATE.equals(key)) {
            Message msg = new Message();
            msg.what = BATTERY_STATE;
            msg.obj = val;
            mHandler.sendMessage(msg);
        }
    }


    /**
     * 校验设备证书是否存在，若不存在，生成30天证书上传；若存在，从设备下载证书校验是否过期
     */
    private void checkLicence() {
        new Thread() {
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    boolean isFileExist = FTPManager.getInstance().downloadFile(FileUtils.ROOT_PATH,
                            LicenceUtils.LICENCE_FILE_NAME);
                    if (isFileExist) {
                        //从设备下载证书校验
                        LicenceUtils.authorizeCode = FileUtils.getInstance().fileToString(
                                FileUtils.ROOT_PATH + LicenceUtils.LICENCE_FILE_NAME);
                        mHandler.sendEmptyMessage(CHECK_LICENCE);
                        FileUtils.getInstance().deleteFile(FileUtils.ROOT_PATH
                                + LicenceUtils.LICENCE_FILE_NAME);
                    } else {
                        //设备无证书，默认生成30天证书上传设备
                        String licence = LicenceUtils.createLicence();
                        LicenceUtils.authorizeCode = licence;
                        boolean isFinish = FileUtils.getInstance().stringToFile(licence,
                                FileUtils.ROOT_PATH + LicenceUtils.LICENCE_FILE_NAME);
                        if (isFinish) {
                            boolean isUploaded = FTPManager.getInstance().uploadFile(false, FileUtils.ROOT_PATH,
                                    LicenceUtils.LICENCE_FILE_NAME);
                            if (isUploaded) {
                                FileUtils.getInstance().deleteFile(FileUtils.ROOT_PATH
                                        + LicenceUtils.LICENCE_FILE_NAME);
                            }

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * @return 验证授权码
     */
    private boolean checkAuthorize() {

        if (TextUtils.isEmpty(LicenceUtils.authorizeCode)) {
            ToastUtils.showMessageLong("App未授权，请联系管理员。");
            LicenceDialog licenceDialog = new LicenceDialog(this, "退出");
            licenceDialog.setOnCloseListener(new LicenceDialog.OnCloseListener() {
                @Override
                public void onClose() {
                    appExit();
                }
            });
            licenceDialog.show();
            return false;
        }

        //以下判断是否过期
        String dueTime = LicenceUtils.getDueTime();
        long longDueTime = DateUtils.convert2long(dueTime, DateUtils.LOCAL_DATE_DAY);
        long nowTime = System.currentTimeMillis();
        if (nowTime >= longDueTime) {
            ToastUtils.showMessageLong("授权已过期，请联系管理员");
            LicenceDialog licenceDialog = new LicenceDialog(this, "退出");
            licenceDialog.setOnCloseListener(new LicenceDialog.OnCloseListener() {
                @Override
                public void onClose() {
                    appExit();
                }
            });
            licenceDialog.show();

            return false;
        } else {
            int dueDay = (int) ((longDueTime - nowTime) / (24 * 60 * 60 * 1000L));
            if (dueDay <= 7) {
                ToastUtils.showMessageLong("授权码还剩" + dueDay + "天到期，请联系管理员");
            }

            return true;
        }

    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIP_MSG) {
                String tip = msg.obj.toString();
                ToastUtils.showMessageLong(tip);
            } else if (msg.what == HEARTBEAT_RPT) {
                boolean rfState = false;

                for (LteChannelCfg channel : CacheManager.getChannels()) {
                    if (channel.getRFState()) {
                        rfState = true;
                        break;
                    }
                }
                if (rfState && !CacheManager.hasPressStartButton()) {
                    CacheManager.setPressStartButtonFlag(true);
                    turnToUeidPage();
                }

            } else if (msg.what == SHOW_PROGRESS) {
                int dialogKeepTime = 5000;
                if (msg.obj != null) {
                    dialogKeepTime = (int) msg.obj;
                }
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                }, dialogKeepTime);
            } else if (msg.what == CLOSE_PROGRESS) {
                try {
                    mProgressDialog.dismiss();
                } catch (Exception e) {
                }
            } else if (msg.what == UPDATE_DEVICE_STATE) {
                try {
                    updateStatusBar(CacheManager.deviceState.getDeviceState());
                } catch (Exception e) {
                }
            } else if (msg.what == REFRESH_FILE_SYS) {
                notifyUpdateFileSystem(msg.obj.toString());
            } else if (msg.what == SPEAK) {
                speak((String) msg.obj);
            } else if (msg.what == UPDATE_BATTERY) {
                processBattery((String) msg.obj);
            } else if (msg.what == ADD_BLACKBOX) {
                BlackBoxManger.recordOperation((String) msg.obj);
            } else if (msg.what == CHANGE_TAB) {
                mViewPager.setCurrentItem((int) msg.obj, true);
            } else if (msg.what == POWER_START) {
                turnToUeidPage();
            } else if (msg.what == CHECK_LICENCE) {
                checkAuthorize();
            } else if (msg.what == BATTERY_STATE) {
                BatteryBean batteryBean = (BatteryBean) msg.obj;
                if (batteryBean.getBatteryQuantity() == 0) {
                    flBattery.setVisibility(View.GONE);
                    tvBattery.setVisibility(View.GONE);
                    tvRemainTime.setVisibility(View.GONE);
                    return;
                }
                flBattery.setVisibility(View.VISIBLE);
                tvBattery.setVisibility(View.VISIBLE);
                tvRemainTime.setVisibility(View.VISIBLE);
                tvBattery.setText(batteryBean.getBatteryQuantity() + "%");
                batteryView.setPower(batteryBean.getBatteryQuantity());
                if (batteryBean.isCharging()) {
                    ivCharging.setVisibility(View.VISIBLE);
                } else {
                    ivCharging.setVisibility(View.GONE);
                }

                tvRemainTime.setText(batteryBean.getUseTime() + "分钟");

                if (batteryBean.getBatteryQuantity() <= 20 && lowBatteryWarn) {
                    lowBatteryWarn = false;
                    lowBatteryWarnning("当前电池电量过低，预计可用" + batteryBean.getUseTime()
                            + "分钟，已无法保证正常工作，请及时更换电池！");
                }

            }
        }
    };
}