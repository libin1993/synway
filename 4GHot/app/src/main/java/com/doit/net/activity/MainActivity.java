package com.doit.net.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import android.widget.Toast;

import com.doit.net.Sockets.OnSocketChangedListener;
import com.doit.net.Sockets.ServerSocketUtils;
import com.doit.net.Utils.PermissionUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.DeviceState;
import com.doit.net.bean.TabEntity;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.AccountManage;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.FTPManager;
import com.doit.net.Utils.LicenceUtils;
import com.doit.net.Model.PrefManage;
import com.doit.net.Model.VersionManage;
import com.doit.net.Sockets.IServerSocketChange;
import com.doit.net.Sockets.NetConfig;
import com.doit.net.Sockets.ServerSocketManager;
import com.doit.net.Sockets.UtilServerSocketSub;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.FTPServer;
import com.doit.net.Utils.FileUtils;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.NetWorkUtils;
import com.doit.net.Utils.LogUtils;

import com.doit.net.fragment.AppFragment;
import com.doit.net.View.LicenceDialog;
import com.doit.net.fragment.LocationFragment;
import com.doit.net.fragment.NameListFragment;
import com.doit.net.adapter.NetworkChangeReceiver;
import com.doit.net.fragment.StartPageFragment;
import com.doit.net.fragment.UeidFragment;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.BaiduAudio;
import com.doit.net.Utils.SoundUtils;
import com.doit.net.Utils.ToastUtils;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;

@SuppressLint("NewApi")
public class MainActivity extends BaseActivity implements IHandlerFinish, TextToSpeech.OnInitListener, EventAdapter.EventCall {
    private Activity activity = this;

    private ViewPager mViewPager;
    private List<BaseFragment> mTabs = new ArrayList<BaseFragment>();
    private CommonTabLayout tabLayout;
    private MainTabLayoutAdapter adapter;
    boolean[] fragmentsUpdateFlag = {false, false, false};
    private List<String> listTitles = new ArrayList<>();
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    private TextToSpeech textToSpeech; // TTS对象
    private BaiduAudio baiduAudio = null;
    private SoundUtils soundUtils = null;

    private MySweetAlertDialog mProgressDialog;
    private boolean hasSetDefaultParam = false;   //开始全部打开射频标志
    private int heartbeatCount = 0;
    private boolean isCheckDeviceStateThreadRun = true;
    private FTPServer ftpServer = new FTPServer();

    private ImageView ivDeviceState;
    Animation viewAnit = new AlphaAnimation(0, 1);
    private ImageView ivWifiState;
    private ImageView ivBatteryLevel;
    private ImageView ivSyncError;
    private MySweetAlertDialog batteryWarningDialog = null;
    Animation batteryViewAnit = new AlphaAnimation(0, 1);


    //handler消息
//    private final int FOUND_BLACK_NAME = 0;
//    private final int SYS_RPT = 1;
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
    private final int DEVICE_INIT_COMPLETE = 12;
    private final int CHECK_LICENCE = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
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
        NetworkInfo wifiNetInfo = ((ConnectivityManager) activity.
                getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String ssid = NetWorkUtils.getWifiSSID(activity);

        if (wifiNetInfo.isConnected() && ssid.contains("synway")) {
            LogUtils.log("wifi state change——connected");
            if (CacheManager.deviceState.getDeviceState().equals(DeviceState.WIFI_DISCONNECT))  //只有从wifi未连接到连接才出现这种状态
                CacheManager.deviceState.setDeviceState(DeviceState.WAIT_SOCKET);
        } else {
            LogUtils.log("wifi state change——disconnected");
        }
    }


    private void initBlackBox() {
        if (VersionManage.isArmyVer())
            return;

        BlackBoxManger.setCurrentAccount(AccountManage.getCurrentLoginAccount());
        BlackBoxManger.initBlx();
        BlackBoxManger.recordOperation(BlackBoxManger.LOGIN + AccountManage.getCurrentLoginAccount());
    }

    private void initNetWork() {
//        ServerSocketManager.getInstance().setUIServerSocketListener(new ServerSocketChange());
//        ServerSocketManager.getInstance().newServerSocket(Integer.parseInt(NetConfig.MONITOR_PORT));
//        ServerSocketManager.getInstance().startMainListener(NetConfig.MONITOR_PORT);

        ServerSocketUtils.getInstance().startServer(new OnSocketChangedListener() {
            @Override
            public void onConnect() {
                CacheManager.deviceState.setDeviceState(DeviceState.ON_INIT);

                heartbeatCount = 0;    //一旦发现是连接就重置此标志以设置所有配置
                //设备重启（重连）后需要重新检查设置默认参数
                hasSetDefaultParam = false;
                CacheManager.resetState();

            }

            @Override
            public void onDisconnect() {
                CacheManager.deviceState.setDeviceState(DeviceState.ON_INIT);
            }
        });
    }

    private void initFTP() {
        File f = new File(FTPServer.FTP_DIR);
        if (!f.exists())
            f.mkdir();

        ftpServer.copyConfigFile(R.raw.users, FTPServer.FTP_DIR + "users.properties", getBaseContext());
        ftpServer.init();
        ftpServer.startFTPServer();
    }

    private void initProgressDialog() {
        mProgressDialog = new MySweetAlertDialog(activity, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("Loading...");
        mProgressDialog.setCancelable(false);
    }

    private void initSpeech() {
        PrefManage.getPlayType();
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setSpeechRate(1.8f);
        baiduAudio = new BaiduAudio(this);
    }

    private void initWifiChangeReceive() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void initView() {
        setOverflowShowingAlways();
//        getActionBar().setDisplayShowHomeEnabled(false);
        mViewPager = (ViewPager) findViewById(R.id.vpTabPage);
        tabLayout = (CommonTabLayout) findViewById(R.id.tablayout);

        ivWifiState = (ImageView) findViewById(R.id.ivWifiState);
        ivWifiState.setOnClickListener(wifiSystemSetting);
        //ivWifiState.setOnClickListener(showSetParamDialogListener);

        ivDeviceState = (ImageView) findViewById(R.id.ivDeviceState);
        //ivDeviceState.setOnClickListener(showSetParamDialogListener);

        ivSyncError = (ImageView) findViewById(R.id.ivSyncError);

        ivBatteryLevel = (ImageView) findViewById(R.id.ivBatteryLevel);

        initTabs();
        initProgressDialog();
    }

    private void initEvent() {
        EventAdapter.setEvent(EventAdapter.FOUND_BLACK_NAME, this);
        EventAdapter.setEvent("TIP_MSG", this);
        EventAdapter.setEvent("SYS_RPT", this);
        EventAdapter.setEvent("SHOW_PROGRESS", this);
        EventAdapter.setEvent("CLOSE_PROGRESS", this);
        EventAdapter.setEvent(EventAdapter.SPEAK, this);
        EventAdapter.setEvent(EventAdapter.UPDATE_FILE_SYS, this);
        EventAdapter.setEvent(EventAdapter.UPDATE_BATTERY, this);
        EventAdapter.setEvent(EventAdapter.ADD_BLACKBOX, this);
        EventAdapter.setEvent(EventAdapter.CHANGE_TAB, this);
        EventAdapter.setEvent(EventAdapter.WIFI_CHANGE, this);
        EventAdapter.setEvent(EventAdapter.POWER_START, this);
        EventAdapter.setEvent(EventAdapter.CHECK_LICENCE, this);
        //EventAdapter.setEvent(EventAdapter.SYNC_ERROR_RPT,this);

        UIEventManager.register(UIEventManager.KEY_HEARTBEAT_RPT, this);
    }

    private void checkDataDir() {
        String dataDir = Environment.getExternalStorageDirectory() + "/4GHotspot/";
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
        if (PrefManage.play_type == 0) {
            textToSpeech.setPitch(1f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.setSpeechRate(0.7f);
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        } else if (PrefManage.play_type == 1) {
            baiduAudio.speak(text);
        } else if (PrefManage.play_type == 2) {
            text = text.replaceAll("-", "").replaceAll("负", "");
            for (int i = 0; i < text.length(); i++) {
                soundUtils.play(text.substring(i, i + 1));
            }
        }
    }

    private void clearDataDir() {
        AccountManage.deleteAccountFile();
        BlackBoxManger.clearBlxData();
    }

    private void initTabs() {
        List<Integer> listSelectIcon = new ArrayList<>();
        List<Integer> listUnselectIcon = new ArrayList<>();

        if (VersionManage.isArmyVer()) {
            listTitles.add("侦码");
            listTitles.add("搜寻");
            listTitles.add("设置");

            listSelectIcon.add(R.drawable.detect_lable_select);
            listSelectIcon.add(R.drawable.location_lable_select);
            listSelectIcon.add(R.drawable.setting_lable_select);

            listUnselectIcon.add(R.drawable.detect_lable_unselect);
            listUnselectIcon.add(R.drawable.location_lable_unselect);
            listUnselectIcon.add(R.drawable.setting_lable_unselect);

            mTabs.add(new StartPageFragment());
            mTabs.add(new LocationFragment());
            mTabs.add(new AppFragment());
        } else if (VersionManage.isPoliceVer()) {
            listTitles.add("侦码");
            listTitles.add("名单");
            listTitles.add("设置");

            listSelectIcon.add(R.drawable.detect_lable_select);
            listSelectIcon.add(R.drawable.location_lable_select);
            listSelectIcon.add(R.drawable.setting_lable_select);

            listUnselectIcon.add(R.drawable.detect_lable_unselect);
            listUnselectIcon.add(R.drawable.location_lable_unselect);
            listUnselectIcon.add(R.drawable.setting_lable_unselect);

            mTabs.add(new StartPageFragment());
            mTabs.add(new NameListFragment());
            mTabs.add(new AppFragment());
        }

        for (int i = 0; i < listTitles.size(); i++) {
            mTabEntities.add(new TabEntity(listTitles.get(i), listSelectIcon.get(i), listUnselectIcon.get(i)));
        }

        adapter = new MainTabLayoutAdapter(getSupportFragmentManager(), mTabs, listTitles);
        mViewPager.setAdapter(adapter);

        tabLayout.setTabData(mTabEntities);
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
                mTabs.get(position).onFocus();
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
        }

        //BlackBoxManger.uploadCurrentBlxFile(); //会卡顿一段时间，体验很差
        clearDataDir();

        unregisterReceiver(networkChangeReceiver);
//        ServerSocketManager.getInstance().closeMainListener(NetConfig.MONITOR_PORT);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (baiduAudio != null) {
            baiduAudio.release();
        }

        isCheckDeviceStateThreadRun = false;
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

    public class MainTabLayoutAdapter<T extends Fragment> extends FragmentPagerAdapter {
        private List<BaseFragment> mList;
        private List<String> mTitles;
        private FragmentManager fm;

        public MainTabLayoutAdapter(FragmentManager fm, List<BaseFragment> list, List<String> titles) {
            super(fm);
            this.mList = list;
            this.mTitles = titles;
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mTabs.get(position % mTabs.size());
//            UtilBaseLog.printLog("getItem:position=" + position + ",fragment:"
//                    + fragment.getClass().getName() + ",fragment.tag=" + fragment.getTag());
            return mTabs.get(position % mTabs.size());
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles == null ? super.getPageTitle(position) : mTitles.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            String fragmentTag = fragment.getTag();
            if (fragmentsUpdateFlag[position % fragmentsUpdateFlag.length]) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(fragment);
                fragment = mTabs.get(position % mTabs.size());
                //添加新fragment时必须用前面获得的tag，这点很重要
                ft.add(container.getId(), fragment, fragmentTag == null ? fragment.getClass().getName() + position : fragmentTag);
                ft.attach(fragment);
                ft.commit();

                fragmentsUpdateFlag[position % fragmentsUpdateFlag.length] = false;
            } else {
                fragment = mTabs.get(position);

            }
            return fragment;
        }
    }

    private void powerStart() {
        TurnToUeidPage();
    }

    private void TurnToUeidPage() {
        fragmentsUpdateFlag[0] = true;
        mTabs.set(0, new UeidFragment());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onInit(int status) {
        LogUtils.log("TextToSpeech status=" + status);
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            LogUtils.log("TextToSpeech result=" + result);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, R.string.tip_08, Toast.LENGTH_SHORT).show();
                PrefManage.setPlayType(1);
                PrefManage.supportPlay = false;
            } else {
                PrefManage.supportPlay = true;
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
        NetworkInfo wifiNetInfo = ((ConnectivityManager) activity.
                getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        String ssid = NetWorkUtils.getWifiSSID(activity);

        if (wifiNetInfo.isConnected() && ssid.contains("synway")) {
            LogUtils.log("wifi state change——connected");
            CacheManager.isWifiConnected = true;
            if (CacheManager.deviceState.getDeviceState().equals(DeviceState.WIFI_DISCONNECT))  //只有从wifi未连接到连接才出现这种状态
                CacheManager.deviceState.setDeviceState(DeviceState.WAIT_SOCKET);
        } else {
            CacheManager.isWifiConnected = false;
            LogUtils.log("wifi state change——disconnected");
            CacheManager.deviceState.setDeviceState(DeviceState.WIFI_DISCONNECT);
            CacheManager.resetState();
        }
    }


    private void setDeviceWorkMode() {
        String workMode = "";
        if (VersionManage.isPoliceVer()) {
            workMode = "0";
        } else if (VersionManage.isArmyVer()) {
            workMode = "2";
        }

        ProtocolManager.setActiveMode(workMode);
        LogUtils.log("设置默认工作模式：" + workMode);
        CacheManager.currentWorkMode = workMode;
    }

    private void updateStatusBar(String deviceState) {
        switch (deviceState) {
            case DeviceState.WIFI_DISCONNECT:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_disconnect));

                ivBatteryLevel.clearAnimation();
                batteryViewAnit.cancel();
                ivBatteryLevel.setVisibility(View.GONE);

                viewAnit.cancel();
                ivDeviceState.clearAnimation();
                ivDeviceState.setVisibility(View.GONE);
                break;

            case DeviceState.WAIT_SOCKET:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));

                ivBatteryLevel.clearAnimation();
                batteryViewAnit.cancel();
                ivBatteryLevel.setVisibility(View.GONE);

                viewAnit.cancel();
                ivDeviceState.clearAnimation();
                ivDeviceState.setVisibility(View.GONE);

                //ivSyncError.setVisibility(View.GONE);
                break;

            case DeviceState.ON_INIT:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));
                ivBatteryLevel.setVisibility(View.GONE);

                ivDeviceState.setImageDrawable(getDrawable(R.drawable.small_device_icon));
                ivDeviceState.setVisibility(View.VISIBLE);
                if (!viewAnit.hasStarted() || viewAnit.hasEnded()) {
                    viewAnit.setDuration(900);   //时间毫秒
                    viewAnit.setInterpolator(new LinearInterpolator());
                    viewAnit.setRepeatMode(Animation.REVERSE);   //播放次序为倒叙
                    viewAnit.setRepeatCount(-1);   //无限
                    ivDeviceState.startAnimation(viewAnit);
                }

                break;

            case DeviceState.NORMAL:
                ivWifiState.setImageDrawable(getDrawable(R.drawable.wifi_connect));
                ivBatteryLevel.setVisibility(View.VISIBLE);
                ivDeviceState.setVisibility(View.VISIBLE);
                viewAnit.cancel();
                ivDeviceState.setImageDrawable(getDrawable(R.drawable.small_device_icon));
                ivDeviceState.clearAnimation();

                //ivSyncError.setVisibility(View.GONE);
                break;
            default:
        }
    }

    private void lowBatteryWarnning() {
        if (batteryWarningDialog == null) {
            batteryWarningDialog = new MySweetAlertDialog(this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.low_power))
                    .setContentText(getString(R.string.low_power_warning))
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

    private void processBattery(int voltage) {
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

            lowBatteryWarnning();
        }
    }

    private class ServerSocketChange implements IServerSocketChange {
        @Override
        public void onServerStartListener(String mainSocketTag) {
        }

        @Override
        public void onServerReceiveNewLink(String subSocketTag, UtilServerSocketSub utilSocket) {
            LogUtils.log("call onServerReceiveNewLink on main activity.");
            CacheManager.deviceState.setDeviceState(DeviceState.ON_INIT);

            heartbeatCount = 0;    //一旦发现是连接就重置此标志以设置所有配置
            //设备重启（重连）后需要重新检查设置默认参数
            hasSetDefaultParam = false;
            CacheManager.resetState();

            CacheManager.DEVICE_IP = utilSocket.remoteIP;
        }

        @Override
        public void onServerReceiveError(String errorMsg) {
            LogUtils.log("call onServerReceiveError");
            CacheManager.deviceState.setDeviceState(DeviceState.ON_INIT);
            // TODO Auto-generated method stub
        }

        @Override
        public void onServerStopLink(String mainSocketTag) {
            LogUtils.log("call onServerStopLink");
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.KEY_HEARTBEAT_RPT)) {
            if (heartbeatCount == 0) {
                ProtocolManager.setNowTime();
                LogUtils.log("首次下发查询以获取小区信息：");
                ProtocolManager.getEquipAndAllChannelConfig();
                //FtpConfig.setFtpServerIp(NetWorkUtils.getWIFILocalIpAdress(this));
                //ProtocolManager.setFTPConfig();    //目前ftp设置从网页直接配
                ProtocolManager.setBlackList("1", "");  //防止上报其他手机设置的黑名单，就查上来删掉
            }

            if (!hasSetDefaultParam && CacheManager.getChannels().size() > 0) {
                setDeviceWorkMode();
                if (VersionManage.isPoliceVer()) {
                    CacheManager.setCurrentBlackList();
                }
                ProtocolManager.setDefaultArfcnsAndPwr();
                hasSetDefaultParam = true;

                if (CacheManager.hasPressStartButton()) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ProtocolManager.openAllRf();
                        }
                    }, 5000);
                }

                mHandler.sendEmptyMessage(DEVICE_INIT_COMPLETE);
                //2019.9.12号讨论决定，所有版本一开始不使用celluar设置频点，公安版本下的定位开启才使用cellular的频点
            }

            heartbeatCount++;
            if (heartbeatCount >= 1000) {
                heartbeatCount = 0;
            }
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
            powerStart();
        } else if (EventAdapter.CHECK_LICENCE.equals(key)) {
            checkLicence();
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
                    boolean isFileExist = FTPManager.getInstance().downloadFile(LicenceUtils.LOCAL_LICENCE_PATH,
                            LicenceUtils.LICENCE_FILE_NAME);
                    if (isFileExist) {
                        //从设备下载证书校验
                        LicenceUtils.authorizeCode = FileUtils.getInstance().fileToString(
                                LicenceUtils.LOCAL_LICENCE_PATH + LicenceUtils.LICENCE_FILE_NAME);
                        mHandler.sendEmptyMessage(CHECK_LICENCE);
                        FileUtils.getInstance().deleteFile(LicenceUtils.LOCAL_LICENCE_PATH
                                + LicenceUtils.LICENCE_FILE_NAME);
                    } else {
                        //设备无证书，默认生成30天证书上传设备
                        String licence = LicenceUtils.createLicence();
                        LicenceUtils.authorizeCode = licence;
                        boolean isFinish = FileUtils.getInstance().stringToFile(licence,
                                LicenceUtils.LOCAL_LICENCE_PATH + LicenceUtils.LICENCE_FILE_NAME);
                        if (isFinish) {
                            boolean isUploaded = FTPManager.getInstance().uploadFile(LicenceUtils.LOCAL_LICENCE_PATH,
                                    LicenceUtils.LICENCE_FILE_NAME);
                            if (isUploaded) {
                                FileUtils.getInstance().deleteFile(LicenceUtils.LOCAL_LICENCE_PATH
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
            ToastUtils.showMessageLong(this, "App未授权，请联系管理员。");
            LicenceDialog licenceDialog = new LicenceDialog(this);
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
        String nowDate = DateUtils.convert2String(new Date(), DateUtils.LOCAL_DATE_DAY);
        long nowTime = DateUtils.convert2long(nowDate, DateUtils.LOCAL_DATE_DAY);
        if (nowTime >= longDueTime) {
            ToastUtils.showMessageLong(activity, "授权已过期，请联系管理员");
            LicenceDialog licenceDialog = new LicenceDialog(this);
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
                ToastUtils.showMessageLong(activity, "授权码还剩" + dueDay + "天到期，请联系管理员");
            }

            return true;
        }

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIP_MSG) {
                String tip = msg.obj.toString();
                ToastUtils.showMessageLong(MainActivity.this, tip);
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
                processBattery((int) msg.obj);
            } else if (msg.what == ADD_BLACKBOX) {
                BlackBoxManger.recordOperation((String) msg.obj);
            } else if (msg.what == CHANGE_TAB) {
                mViewPager.setCurrentItem((int) msg.obj, true);
            } else if (msg.what == POWER_START) {
                powerStart();
            } else if (msg.what == DEVICE_INIT_COMPLETE) {
//                new MySweetAlertDialog(activity, MySweetAlertDialog.SUCCESS_TYPE)
//                        .setTitleText("提示")
//                        .setContentText("设备初始化已完成！")
//                        .show();
            } else if (msg.what == CHECK_LICENCE) {
                checkAuthorize();
            }
        }
    };
}