package com.doit.net.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.doit.net.View.LocateChart;
import com.doit.net.View.LocateCircle;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.VersionManage;
import com.doit.net.Utils.Cellular;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.UtilOperator;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LocationFragment extends BaseFragment implements EventAdapter.EventCall {
    private TextView tvLocatingImsi;
    private LocateChart vLocateChart;
    private LocateCircle vLocateCircle;
    private CheckBox cbGainSwitch;
    private CheckBox cbVoiceSwitch;
    private CheckBox cbLocSwitch;

    private List<Integer> listChartValue = new ArrayList<>();
    private final int LOCATE_CHART_X_AXIS_P_CNT = 15;       //图表横坐标点数
    private final int LOCATE_CHART_Y_AXIS_P_CNT = 25;       //图表纵坐标点数
    private String textContent = "搜寻未开始";

    private int currentSRSP = 0;
    private int lastRptSRSP = 60;//初始平滑地开始
    private static boolean isOpenVoice = true;
    private Timer speechTimer = null;
    private final int BROADCAST_PERIOD = 1900;
    private long lastLocRptTime = 0;
    private int LOC_RPT_TIMEOUT = 5 * 1000;  //多长时间没上报就开始播报“正在搜寻”
    private int UPDATE_ARFCN_TIMEOUT = 2 * 60 * 1000;  //多长时间没上报就更新频点
    private final int MAX_DEVIATION = 16;   //强度与上次上报偏差大于这个值就重新计算

    private String lastLocateIMSI = "";

    //handler消息
    private final int UPDATE_VIEW = 0;
    private final int LOC_REPORT = 1;
    private final int STOP_LOC = 3;
    private final int REFRESH_DEVICE = 4;
    private final int RF_STATUS_LOC = 5;
    private final int ADD_LOCATION = 6;

    public LocationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.doit_layout_location, container, false);
        tvLocatingImsi = rootView.findViewById(R.id.tvLocatingImsi);
        vLocateChart = rootView.findViewById(R.id.vLocateChart);
        vLocateCircle = rootView.findViewById(R.id.vLocateCircle);
        cbVoiceSwitch = rootView.findViewById(R.id.cbVoiceSwitch);
        cbVoiceSwitch.setOnCheckedChangeListener(voiceSwitchListener);
        cbGainSwitch = rootView.findViewById(R.id.cbGainSwitch);

        cbGainSwitch.setOnCheckedChangeListener(gainSwitchListener);
        cbLocSwitch = rootView.findViewById(R.id.cbLocSwitch);

        initView();
        initEvent();
        return rootView;
    }


    private void initEvent() {

        EventAdapter.register(EventAdapter.REFRESH_DEVICE, this);
        EventAdapter.register(EventAdapter.LOCATION_RPT, this);
        EventAdapter.register(EventAdapter.ADD_LOCATION, this);
        EventAdapter.register(EventAdapter.STOP_LOC, this);
        EventAdapter.register(EventAdapter.RF_STATUS_LOC, this);

    }

    private void initView() {

        cbLocSwitch.setOnCheckedChangeListener(rfLocSwitchListener);

        vLocateChart.setCylinderCount(LOCATE_CHART_X_AXIS_P_CNT);
        vLocateChart.setMaxPointCntInClder(LOCATE_CHART_Y_AXIS_P_CNT);
        resetLocateChartValue();
    }


    private void startSpeechBroadcastLoop() {
        if (speechTimer == null) {
            speechTimer = new Timer();
            speechTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if ((int) (System.currentTimeMillis() - lastLocRptTime) > LOC_RPT_TIMEOUT) {
                        currentSRSP = 0;
                        resetLocateChartValue();
                        refreshPage();
                    }

                    if (currentSRSP == 0) {
                        speech("正在搜寻");
                    } else {
                        speech("" + currentSRSP);
                    }
                }
            }, 4000, BROADCAST_PERIOD);
        }

    }

    private void startUpdateArfcn() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (CacheManager.getLocState()) {
                    if ((int) (System.currentTimeMillis() - lastLocRptTime) > UPDATE_ARFCN_TIMEOUT) {
                        LogUtils.log("定位上报超时，尝试重新设置频点和功率... ...");
                        //ToastUtils.showMessage(getContext(),"搜寻上报超时，尝试更新频点和功率");
                        Cellular.adjustArfcnPwrForLocTarget(CacheManager.getCurrentLoction().getImsi());
                    }
                }
            }
        }, 30000, 30000);
    }

    private void stopSpeechBroadcastLoop() {
        if (speechTimer != null) {
            speechTimer.cancel();
            speechTimer = null;
        }
    }

    private void refreshPage() {
        if (CacheManager.getCurrentLoction() == null) {
            return;
        }

        mHandler.sendEmptyMessage(UPDATE_VIEW);
    }

    private void updateLocateChart() {
        int[] chartDatas = new int[LOCATE_CHART_X_AXIS_P_CNT];

        for (int i = 0; i < LOCATE_CHART_X_AXIS_P_CNT; i++) {
            chartDatas[i] = listChartValue.get(i);
        }
        vLocateChart.updateChart(chartDatas);
    }

    private int correctSRSP(int srspRptValue) {
        //srsp = (srspRptValue-234)/10  旧的算法
        int srsp = srspRptValue * 5 / 6;

        if (srsp <= 0)
            srsp = 0;

        if (srsp > 100)
            srsp = 100;

        if (Math.abs(srsp - lastRptSRSP) > MAX_DEVIATION) {
            srsp = (lastRptSRSP + srsp) / 2;
        }

        return srsp;
    }

    void startLoc() {
        if (!CacheManager.getLocState()) {
            startSpeechBroadcastLoop();

            CacheManager.startLoc(CacheManager.getCurrentLoction().getImsi());
            textContent = "正在搜寻："+CacheManager.currentLoction.getImsi();
            refreshPage();
        }
    }

    void addLocation(String imsi) {
        LogUtils.log("开始定位,IMSI:"+imsi);
        if ("".equals(lastLocateIMSI)) {
            if (VersionManage.isPoliceVer()) {
                startUpdateArfcn();
            }
        }

        textContent = "正在搜寻" + imsi;

        if (!"".equals(lastLocateIMSI) && !lastLocateIMSI.equals(imsi)) {   //更换目标
            restartLoc();
        }

        if (VersionManage.isPoliceVer()) {
            Cellular.adjustArfcnPwrForLocTarget(CacheManager.getCurrentLoction().getImsi());
        }
        startSpeechBroadcastLoop();

        lastLocateIMSI = CacheManager.getCurrentLoction().getImsi();

        refreshPage();
    }

    private void restartLoc() {
        speech("搜寻目标更换");
        currentSRSP = 0;
        lastRptSRSP = 0;
        textContent = "正在搜寻：" + CacheManager.getCurrentLoction().getImsi();
        resetLocateChartValue();
        refreshPage();
        startSpeechBroadcastLoop();  //从停止定位的状态添加定位，故语音手动再次开启
    }

    private void stopLoc() {
        LogUtils.log("停止定位");
        if (CacheManager.getLocState()) {
            CacheManager.stopCurrentLoc();
            stopSpeechBroadcastLoop();
            textContent = "搜寻暂停：" + CacheManager.getCurrentLoction().getImsi();
            currentSRSP = 0;
            resetLocateChartValue();
        }

        refreshPage();
    }

    private void resetLocateChartValue() {
        listChartValue.clear();

        for (int i = 0; i < LOCATE_CHART_X_AXIS_P_CNT; i++) {
            listChartValue.add(0);
        }
    }

    void speech(String content) {
        if (isOpenVoice)
            EventAdapter.call(EventAdapter.SPEAK, content);
    }


    CompoundButton.OnCheckedChangeListener rfLocSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(getContext())) {
                cbLocSwitch.setChecked(!isChecked);
                return;
            }

            if (!isChecked) {
                ProtocolManager.closeAllRf();
                stopLoc();
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
                if (CacheManager.currentLoction != null && !CacheManager.currentLoction.getImsi().equals("")) {
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.STOP_LOCALTE + CacheManager.currentLoction.getImsi());
                }
            } else {
                if (CacheManager.currentLoction == null || CacheManager.currentLoction.getImsi().equals("")) {
                    ToastUtils.showMessage(R.string.button_loc_unstart);
                } else {
                    ProtocolManager.openAllRf();
                    startLoc();
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE + CacheManager.currentLoction.getImsi());
                }
            }
        }
    };



    CompoundButton.OnCheckedChangeListener voiceSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (!view.isPressed())
                return;

            isOpenVoice = isChecked;
        }
    };

    CompoundButton.OnCheckedChangeListener gainSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (!view.isPressed())
                return;

            if (!CacheManager.checkDevice(getContext())) {
                cbGainSwitch.setChecked(!cbGainSwitch.isChecked());
                return;
            }

            if (isChecked) {
                CacheManager.setHighGa(true);
            } else {
                CacheManager.setHighGa(false);
            }

            ToastUtils.showMessageLong("增益设置已下发，请等待其生效");
            EventAdapter.call(EventAdapter.SHOW_PROGRESS, 8000);
        }
    };


    private void setLocationRF() {
        if (!CacheManager.getLocState())
            return;

        String operator = UtilOperator.getOperatorName(CacheManager.getCurrentLoction().getImsi());
        String setBand = "";
        List<Integer> listBandInOpr;
        if (operator.equals("CTJ")) {
            listBandInOpr = Arrays.asList(38, 39, 40, 41);
        } else if (operator.equals("CTU") || operator.equals("CTC")) {
            listBandInOpr = Arrays.asList(1, 3);
        } else {
            return;
        }

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (listBandInOpr.contains(Integer.valueOf(channel.getBand()))) {
                if (!channel.getRFState())
                    ProtocolManager.openRf(channel.getIdx());
            } else {
                if (channel.getRFState())
                    ProtocolManager.closeRf(channel.getIdx());
            }
        }
    }

    private void setArfcnPowerByTargetOpr() {
        //军队版本不做功率调整
        if (VersionManage.isArmyVer())
            return;


        String operator = UtilOperator.getOperatorName(CacheManager.getCurrentLoction().getImsi());
        if (operator.equals("CTJ")) {
            //List<Integer> listBandsInCTJ = Arrays.asList(38,39,40,41);
            for (LteChannelCfg channel : CacheManager.getChannels()) {
//                if (listBandsInCTJ.contains(Integer.valueOf(channel.getBand()))){
//                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", tmpPa, "", "", "", "");
//                }

                //对于移动，只要判断band3里有无1300频点，有则拉低其他两个频点
                if (channel.getBand().equals("3") && channel.getFcn().contains("1300")) {
                    String[] tmpFcns;
                    String tmpPa = "";
                    tmpFcns = channel.getFcn().split(",");
                    for (int i = 0; i < tmpFcns.length; i++) {
                        if (tmpFcns[i].equals("1300")) {
                            tmpPa += "-7";
                            tmpPa += ",";
                        } else {
                            tmpPa += "-55,";
                        }
                    }
                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", tmpPa.substring(0, tmpPa.length() - 1), "", "", "", "");
                } else if (channel.getBand().equals("1") || channel.getBand().equals("3")) {
                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", "-7,-7,-7", "", "", "", "");
                }
                /* 无论是什么制式，怎么切换，band38-41一直保持最大，无需调整 */
//                else if (channel.getBand().equals("38") || channel.getBand().equals("40") || channel.getBand().equals("41")){
//                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", "-1,-1,-1", "", "", "", "");
//                }else if (channel.getBand().equals("39")){
//                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", "-13,-13,-13", "", "", "", "");
//                }
            }
        } else if (operator.equals("CTU") || operator.equals("CTC")) {
            String[] tmpFcns;
            String tmpPa = "";
            for (LteChannelCfg channel : CacheManager.getChannels()) {
                if (channel.getBand().equals("1") || channel.getBand().equals("3")) {
                    tmpFcns = channel.getFcn().split(",");
                    for (int i = 0; i < tmpFcns.length; i++) {
                        if (UtilOperator.isArfcnInOperator(operator, tmpFcns[i])) {
                            tmpPa += "-7";
                            tmpPa += ",";
                        } else {
                            tmpPa += "-55,";
                        }
                    }
                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", tmpPa.substring(0, tmpPa.length() - 1), "", "", "", "");
                    tmpPa = "";
                }
            }
        }
    }



    @Override
    public void onFocus() {
        refreshPage();
    }


    /**
     * 射频是否开启
     */
    private void isRFOpen(){

        boolean rfState = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState = true;
                break;
            }
        }

        cbLocSwitch.setOnCheckedChangeListener(null);
        cbLocSwitch.setChecked(rfState);
        cbLocSwitch.setOnCheckedChangeListener(rfLocSwitchListener);

    }



    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.LOCATION_RPT:
                try {
                    Message msg = new Message();
                    msg.what = LOC_REPORT;
                    msg.obj = val;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventAdapter.ADD_LOCATION:
                try {
                    Message msg = new Message();
                    msg.what = ADD_LOCATION;
                    msg.obj = val;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventAdapter.STOP_LOC:
                try {
                    Message msg = new Message();
                    msg.what = STOP_LOC;
                    msg.obj = val;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EventAdapter.REFRESH_DEVICE:
                mHandler.sendEmptyMessage(REFRESH_DEVICE);
                break;
            case EventAdapter.RF_STATUS_LOC:
                mHandler.sendEmptyMessage(RF_STATUS_LOC);
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW:
                    tvLocatingImsi.setText(textContent);
                    vLocateCircle.setValue(currentSRSP);
                    updateLocateChart();

                    if (CacheManager.getLocState()) {
                        cbLocSwitch.setChecked(true);
                    } else {
                        cbLocSwitch.setChecked(false);
                    }
                    break;
                case LOC_REPORT:
                    if (CacheManager.getCurrentLoction() != null && CacheManager.getCurrentLoction().isLocateStart()) {
                        currentSRSP = correctSRSP(Integer.parseInt((String) msg.obj));
                        if (currentSRSP == 0)
                            return;

                        lastLocRptTime = new Date().getTime();
                        lastRptSRSP = currentSRSP;

                        listChartValue.add(currentSRSP / 4);
                        listChartValue.remove(0);
                        textContent = "正在搜寻" + CacheManager.getCurrentLoction().getImsi();

                        refreshPage();
                    }
                    break;
                case STOP_LOC:
                    stopLoc();
                    break;
                case ADD_LOCATION:
                    addLocation((String) msg.obj);
                    break;
                case REFRESH_DEVICE:
                    //ga <= 10为低增益,11-50为高增益
                    if (CacheManager.channels != null && CacheManager.channels.size() > 0) {
                        cbGainSwitch.setOnCheckedChangeListener(null);
                        for (LteChannelCfg channel : CacheManager.channels) {
                            int ga = Integer.parseInt(channel.getGa());
                            if (ga <= 10) {
                                cbGainSwitch.setChecked(false);
                                break;
                            }
                        }
                        cbGainSwitch.setOnCheckedChangeListener(gainSwitchListener);
                    }
                    break;
                case RF_STATUS_LOC:
                    isRFOpen();
                    break;

            }

        }
    };
}