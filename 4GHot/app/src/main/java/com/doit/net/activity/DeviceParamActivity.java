package com.doit.net.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.os.Bundle;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.ScreenUtils;
import com.doit.net.view.SystemSetupDialog;
import com.doit.net.adapter.UserChannelListAdapter;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.LogUtils;
import com.doit.net.ucsi.R;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.doit.net.bean.LteChannelCfg;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.CacheManager;

import com.doit.net.utils.ToastUtils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 设备参数
 */
public class DeviceParamActivity extends BaseActivity implements EventAdapter.EventCall {
    private Button btSetCellParam;
    private Button btSetChannelCfg;
    private Button btUpdateTac;
    private Button btRebootDevice;
    private Button btRefreshParam;
    private RecyclerView rvBand;
    private long lastRefreshParamTime = 0; //防止频繁刷新参数

    private RadioGroup rgPowerLevel;

    private RadioGroup rgDetectCarrierOperate;
    private RadioButton rbDetectAll;
    private RadioButton rbCTJ;
    private RadioButton rbCTU;
    private RadioButton rbCTC;
    private RadioButton lastDetectCarrierOperatePress;

    private CheckBox cbRFSwitch;

    private MySweetAlertDialog mProgressDialog;


    //handler消息
    private final int UPDATE_VIEW = 1;
    private final int SHOW_PROGRESS = 2;

    private BaseQuickAdapter<LteChannelCfg, BaseViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_param);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intView();
        initEvent();
    }

    private void initEvent() {
        EventAdapter.register(EventAdapter.REFRESH_DEVICE, this);
    }

    private void intView() {
        btSetCellParam = findViewById(R.id.btSetCellParam);
        btSetCellParam.setOnClickListener(setCellParamClick);
        btSetChannelCfg = findViewById(R.id.btSetChannelCfg);
        btSetChannelCfg.setOnClickListener(setChannelCfgClick);
        btUpdateTac = findViewById(R.id.btUpdateTac);
        btUpdateTac.setOnClickListener(updateTacClick);
        btRebootDevice = findViewById(R.id.btRebootDevice);
        btRebootDevice.setOnClickListener(rebootDeviceClick);
        btRefreshParam = findViewById(R.id.btRefreshParam);
        btRefreshParam.setOnClickListener(refreshParamClick);

        rvBand = findViewById(R.id.rv_band);

        rgPowerLevel = findViewById(R.id.rgPowerLevel);

        rgPowerLevel.setOnCheckedChangeListener(powerLevelListener);

        rgDetectCarrierOperate = findViewById(R.id.rgDetectCarrierOperate);
        rbDetectAll = findViewById(R.id.rbDetectAll);
        rbCTJ = findViewById(R.id.rbCTJ);
        rbCTU = findViewById(R.id.rbCTU);
        rbCTC = findViewById(R.id.rbCTC);
        lastDetectCarrierOperatePress = rbDetectAll;
        rgDetectCarrierOperate.setOnCheckedChangeListener(detectCarrierOperateListener);

        cbRFSwitch = findViewById(R.id.cbRFSwitch);
        cbRFSwitch.setOnCheckedChangeListener(rfCheckChangeListener);


        rvBand.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseQuickAdapter<LteChannelCfg, BaseViewHolder>(R.layout.layout_rv_band_item, CacheManager.channels) {
            @Override
            protected void convert(BaseViewHolder helper, LteChannelCfg item) {
                helper.setText(R.id.tv_band_info, "通道：" + item.getIdx() + "        " + "频段：" + item.getBand() + "\n" +
                        "频点：[" + item.getFcn() + "]");
                ImageView ivRFStatus = helper.getView(R.id.iv_rf_status);
                if (item.getRFState()) {
                    ivRFStatus.setImageResource(R.drawable.switch_open);
                } else {
                    ivRFStatus.setImageResource(R.drawable.switch_close);
                }

                helper.addOnClickListener(R.id.iv_rf_status);
            }
        };
        rvBand.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                    return;
                }
                LteChannelCfg lteChannelCfg = CacheManager.channels.get(position);
                if (lteChannelCfg != null && !TextUtils.isEmpty(lteChannelCfg.getChangeBand())) {
                    changeChannelBandDialog(lteChannelCfg.getIdx(), lteChannelCfg.getBand(),lteChannelCfg.getChangeBand());
                }
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.iv_rf_status) {
                    if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                        return;
                    }

                    LteChannelCfg lteChannelCfg = CacheManager.channels.get(position);

                    if (lteChannelCfg == null) {
                        return;
                    }

                    if (CacheManager.getLocState()) {
                        ToastUtils.showMessageLong("当前正在搜寻中，请确认通道射频变动是否对其产生影响！");
                    }

                    showProcess(6000);
                    if (lteChannelCfg.getRFState()) {
                        ProtocolManager.closeRf(lteChannelCfg.getIdx());
                    } else {
                        ProtocolManager.openRf(lteChannelCfg.getIdx());
                    }

                }

            }
        });

        mProgressDialog = new MySweetAlertDialog(DeviceParamActivity.this, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("Loading...");
        mProgressDialog.setCancelable(false);


        ProtocolManager.getEquipAndAllChannelConfig();
    }

    View.OnClickListener setCellParamClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }
            new SystemSetupDialog(DeviceParamActivity.this).show();
        }
    };

    View.OnClickListener setChannelCfgClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }

            setChannel();
        }
    };


    /**
     * 设置通道
     */
    private void setChannel() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.doit_layout_channels_dialog, null);
        PopupWindow popupWindow = new PopupWindow(dialogView, ScreenUtils.getInstance()
                .getScreenWidth(DeviceParamActivity.this) - FormatUtils.getInstance().dip2px(40),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView rvChannel = dialogView.findViewById(R.id.rv_channel);
        Button btnCancel = dialogView.findViewById(R.id.button_cancel);

        //设置Popup具体参数
        popupWindow.setFocusable(true);//点击空白，popup不自动消失
        popupWindow.setTouchable(true);//popup区域可触摸
        popupWindow.setOutsideTouchable(false);//非popup区域可触摸
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);


        rvChannel.setLayoutManager(new LinearLayoutManager(this));
        BaseQuickAdapter<LteChannelCfg, BaseViewHolder> adapter = new BaseQuickAdapter<LteChannelCfg,
                BaseViewHolder>(R.layout.doit_layout_channel_item, CacheManager.getChannels()) {
            @Override
            protected void convert(BaseViewHolder helper, LteChannelCfg item) {
                helper.setText(R.id.title_text, "通道：" + item.getIdx() + "    " + "频段:" + item.getBand());

                helper.setText(R.id.editText_fcn, item.getFcn() == null ? "" : "" + item.getFcn());
                helper.setText(R.id.editText_plmn, item.getPlmn());
                helper.setText(R.id.editText_ga, item.getGa() == null ? "" : "" + item.getGa());
                helper.setText(R.id.editText_pa, item.getPa() == null ? "" : "" + item.getPa());
                helper.setText(R.id.etRLM, item.getRlm() == null ? "" : "" + item.getRlm());
                helper.setText(R.id.etAltFcn, item.getAltFcn() == null ? "" : "" + item.getAltFcn());

                helper.addOnClickListener(R.id.button_save);
            }
        };
        rvChannel.setAdapter(adapter);

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                if (view.getId() == R.id.button_save) {
                    if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                        return;
                    }

                    EditText etFcn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_fcn);
                    EditText etPlmn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_plmn);
                    EditText etGa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_ga);
                    EditText etPa = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.editText_pa);
                    EditText etRLM = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.etRLM);
                    EditText etAltFcn = (EditText) adapter.getViewByPosition(rvChannel, position, R.id.etAltFcn);

                    String fcn = etFcn.getText().toString().trim();
                    String plmn = etPlmn.getText().toString().trim();
                    String pa = etPa.getText().toString().trim();
                    String ga = etGa.getText().toString().trim();
                    String rlm = etRLM.getText().toString().trim();
                    String alt_fcn = etAltFcn.getText().toString().trim();

                    //不为空校验正则，为空不上传
                    if (!TextUtils.isEmpty(fcn) && !FormatUtils.getInstance().
                            fcnRange(CacheManager.channels.get(position).getBand(), fcn)) {
                        return;
                    }

                    if (!TextUtils.isEmpty(plmn)){
                        if (!FormatUtils.getInstance().plmnRange(plmn)) {
                            ToastUtils.showMessage("plmn格式输入有误,请检查");
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(pa) && (pa.startsWith(",") || pa.endsWith(","))){
                        ToastUtils.showMessage("下行功率格式输入有误,请检查");
                        return;
                    }


                    ToastUtils.showMessage(R.string.tip_15);
                    showProcess(6000);


                    LteChannelCfg channelCfg = CacheManager.channels.get(position);
                    if (!TextUtils.isEmpty(fcn)) {
                        channelCfg.setFcn(fcn);
                    }

                    if (!TextUtils.isEmpty(plmn)) {
                        channelCfg.setPlmn(plmn);
                    }

                    if (!TextUtils.isEmpty(ga)) {
                        channelCfg.setGa(ga);
                    }

                    if (!TextUtils.isEmpty(pa)) {
                        channelCfg.setPa(pa);
                    }
                    if (!TextUtils.isEmpty(rlm)) {
                        channelCfg.setRlm(rlm);
                    }
                    if (!TextUtils.isEmpty(alt_fcn)) {
                        channelCfg.setAltFcn(alt_fcn);
                    }


                    ProtocolManager.setChannelConfig(CacheManager.channels.get(position).getIdx(),
                            fcn, plmn, pa, ga, rlm, "", alt_fcn);

                    refreshViews();

                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    View.OnClickListener updateTacClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }

            ProtocolManager.changeTac();
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANNEL_TAG);
        }
    };

    View.OnClickListener rebootDeviceClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }

            new MySweetAlertDialog(DeviceParamActivity.this, MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("设备重启")
                    .setContentText("确定重启设备")
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            ProtocolManager.reboot();
                        }
                    })
                    .show();
        }
    };

    View.OnClickListener refreshParamClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshParamTime > 20 * 1000) {
                ProtocolManager.getEquipAndAllChannelConfig();
                lastRefreshParamTime = currentTime;
                ToastUtils.showMessage("下发查询参数成功！");
            } else {
                ToastUtils.showMessage("请勿频繁刷新参数！");
            }
        }
    };

    RadioGroup.OnCheckedChangeListener powerLevelListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (!(group.findViewById(checkedId).isPressed())) {
                return;
            }

            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                return;
            }


            if (CacheManager.getLocState()) {
                ToastUtils.showMessage("当前正在搜寻中，请留意功率变动是否对其产生影响！");
            } else {
                ToastUtils.showMessageLong("功率设置已下发，请等待其生效");
            }
            showProcess(6000);

            switch (checkedId) {
                case R.id.rbPowerHigh:
                    setPowerLevel(0);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_ALL_POWER + "高");
                    break;

                case R.id.rbPowerMedium:
                    setPowerLevel(-15);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_ALL_POWER + "中");
                    break;

                case R.id.rbPowerLow:
                    setPowerLevel(-30);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.SET_ALL_POWER + "低");
                    break;
            }

        }
    };

    RadioGroup.OnCheckedChangeListener detectCarrierOperateListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (!(group.findViewById(checkedId).isPressed())) {
                return;
            }

            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                lastDetectCarrierOperatePress.setChecked(true);
                return;
            }

            if (CacheManager.getLocState()) {
                ToastUtils.showMessage("当前正在定位中，无法切换侦码制式！");
                lastDetectCarrierOperatePress.setChecked(true);
                return;
            } else {
                ToastUtils.showMessageLong("侦码制式设置已下发，请等待其生效");
            }

            showProcess(6000);
            switch (checkedId) {
                case R.id.rbDetectAll:
                    ProtocolManager.setDetectCarrierOperation("detect_all");
                    lastDetectCarrierOperatePress = rbDetectAll;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "所有");
                    break;

                case R.id.rbCTJ:
                    ProtocolManager.setDetectCarrierOperation("detect_ctj");
                    lastDetectCarrierOperatePress = rbCTJ;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "移动");
                    break;

                case R.id.rbCTU:
                    ProtocolManager.setDetectCarrierOperation("detect_ctu");
                    lastDetectCarrierOperatePress = rbCTU;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "联通");
                    break;

                case R.id.rbCTC:
                    ProtocolManager.setDetectCarrierOperation("detect_ctc");
                    lastDetectCarrierOperatePress = rbCTC;
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_DETTECT_OPERATE + "电信");
                    break;
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshDetectOperation();
                }
            }, 2000);

        }
    };


    CompoundButton.OnCheckedChangeListener rfCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(DeviceParamActivity.this)) {
                cbRFSwitch.setChecked(!isChecked);
                return;
            }


            if (isChecked) {
                ProtocolManager.openAllRf();
                ToastUtils.showMessageLong(R.string.rf_open);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_RF);
                showProcess(6000);
            } else {
                if (CacheManager.getLocState()) {
                    new MySweetAlertDialog(DeviceParamActivity.this, MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("当前正在搜寻，确定关闭吗？")
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmText(getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {

                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    ProtocolManager.closeAllRf();
                                    ToastUtils.showMessage(R.string.rf_close);
                                    showProcess(6000);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_RF);
                                }
                            })
                            .show();
                } else {
                    ProtocolManager.closeAllRf();
                    ToastUtils.showMessageLong(R.string.rf_close);
                    showProcess(6000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_RF);
                }

            }
        }
    };


    public void setPowerLevel(int powerLevel) {
        if (!CacheManager.isDeviceOk()) {
            return;
        }

        for (int i = 0; i < CacheManager.getChannels().size(); i++) {
            int index = i;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LteChannelCfg channel = CacheManager.getChannels().get(index);
                    int pa = powerLevel + Integer.parseInt(channel.getPMax());

                    ProtocolManager.setChannelConfig(channel.getIdx(), "", "", pa + "," + pa + "," + pa,
                            "", "", "", "");
                    channel.setPa(pa + "," + pa + "," + pa);
                }
            }, index*200);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshPowerLevel();
            }
        }, 2000);
    }

    public void refreshViews() {
        refreshDetectOperation();
        refreshPowerLevel();
        refreshRFSwitch();
        adapter.notifyDataSetChanged();

    }

    private void refreshRFSwitch() {
        boolean rfState = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState = true;
                break;
            }
        }

        cbRFSwitch.setOnCheckedChangeListener(null);
        cbRFSwitch.setChecked(rfState);
        cbRFSwitch.setOnCheckedChangeListener(rfCheckChangeListener);
    }

    private void refreshDetectOperation() {
        if (CacheManager.getChannels().size() > 0) {
            String firstPlnm = CacheManager.getChannels().get(0).getPlmn();  //以第一个作为参考

            if (firstPlnm.contains("46000") && firstPlnm.contains("46001") && firstPlnm.contains("46011")) {
                rbDetectAll.setChecked(true);
            } else if (firstPlnm.equals("46000,46000,46000")) {
                rbCTJ.setChecked(true);
            } else if (firstPlnm.equals("46001,46001,46001")) {
                rbCTU.setChecked(true);
            } else if (firstPlnm.equals("46011,46011,46011")) {
                rbCTC.setChecked(true);
            }
        }
    }

    private void refreshPowerLevel() {
        //定位下有不同频点的功率变动，故不刷新&& !CacheManager.getLocState()
        if (CacheManager.isDeviceOk()) {
            int powerLevel = Integer.parseInt(CacheManager.getChannels().get(0).getPMax()) - Integer.parseInt(CacheManager.getChannels().get(0).getPa().split(",")[0]);
            int index;
            if (powerLevel < 15){
                index = 0;
            }else if (powerLevel < 30){
                index = 1;
            }else{
                index = 2;
            }
            rgPowerLevel.setOnCheckedChangeListener(null);
            ((RadioButton) rgPowerLevel.getChildAt(index)).setChecked(true);
            rgPowerLevel.setOnCheckedChangeListener(powerLevelListener);

        }

    }

    private void showProcess(int keepTime) {
        Message msg = new Message();
        msg.what = SHOW_PROGRESS;
        msg.obj = keepTime;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onResume() {
        refreshViews();
        super.onResume();
    }


    private void changeChannelBandDialog(final String idx, String band,final String changeBand) {
        String[] split = changeBand.split(",");
        List<String> dataList = new ArrayList<>();
        for (String s : split) {
            if (!s.equals(band)){
                dataList.add(s);
            }
        }
        if (dataList.size() == 0){
            return;
        }

        View contentView = LayoutInflater.from(DeviceParamActivity.this).inflate(R.layout.popup_change_band, null);
        PopupWindow mPopupWindow = new PopupWindow(contentView, FormatUtils.getInstance().dip2px(300),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        RecyclerView rvBand = contentView.findViewById(R.id.rv_change_band);
        Button btnCancel = contentView.findViewById(R.id.btn_cancel_change);


        //设置Popup具体参数
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.setFocusable(true);//点击空白，popup不自动消失
        mPopupWindow.setTouchable(true);//popup区域可触摸
        mPopupWindow.setOutsideTouchable(true);//非popup区域可触摸
        mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);


        rvBand.setLayoutManager(new LinearLayoutManager(this));
        BaseQuickAdapter<String,BaseViewHolder> adapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.layout_change_band_item,dataList) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                helper.setText(R.id.tv_change_band,"通道："+idx+"      "+"Band："+item);
            }
        };

        rvBand.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mPopupWindow.dismiss();
                CacheManager.changeBand(idx, dataList.get(position));
                showProcess(8000);
                ToastUtils.showMessageLong("切换Band命令已下发，请等待生效");
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CHANGE_BAND + band);
            }

        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_VIEW) {
                refreshViews();
            } else if (msg.what == SHOW_PROGRESS) {
                int dialogKeepTime = 5000;
                if (msg.obj != null && (int) msg.obj != 0) {
                    dialogKeepTime = (int) msg.obj;
                }
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                }, dialogKeepTime);
            }
        }
    };


    @Override
    public void call(String key, Object val) {
        if (EventAdapter.REFRESH_DEVICE.equals(key)) {
            mHandler.sendEmptyMessage(UPDATE_VIEW);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

