package com.doit.net.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.PrefManage;
import com.doit.net.Model.VersionManage;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.LTE_PT_SYSTEM;
import com.doit.net.Utils.Cellular;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.doit.net.Event.EventAdapter.GET_NAME_LIST;
import static com.doit.net.Event.EventAdapter.UPDATE_TMEPRATURE;

public class TestActivity extends BaseActivity implements EventAdapter.EventCall {
    private Button test1;
    private Button test2;
    private Button test3;
    private Button test4;
    private Button test5;
    private Button test6;
    private Button test7;
    private Button test8;
    private Button btGetDeviceLog;
    private TextView tvTemperature;
    private TextView tvArfcns;
    private TextView tvNameList;
    private EditText etImsi;
    private Button btnLoc;
    private Button btnSpeak;
    private SeekBar sbPitch;
    private SeekBar sbSpeed;
    private TextView tvPitch;
    private TextView tvSpeed;

    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_for_test);

        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        test3 = findViewById(R.id.test3);
        test4 = findViewById(R.id.test4);
        test5 = findViewById(R.id.test5);
        test6 = findViewById(R.id.test6);
        test7 = findViewById(R.id.test7);
        test8 = findViewById(R.id.test8);
        etImsi = findViewById(R.id.et_imsi);
        btnLoc = findViewById(R.id.btn_loc);
        btGetDeviceLog = findViewById(R.id.btGetDeviceLog);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvArfcns = findViewById(R.id.tvArfcns);
        tvNameList = findViewById(R.id.tv_name_list);
        btnSpeak = findViewById(R.id.btn_speak);
        sbPitch = findViewById(R.id.sb_pitch);
        sbSpeed = findViewById(R.id.sb_speed);
        tvPitch = findViewById(R.id.tv_pitch);
        tvSpeed = findViewById(R.id.tv_speed);

        initView();

        EventAdapter.register(UPDATE_TMEPRATURE, this);
        EventAdapter.register(EventAdapter.GET_NAME_LIST, this);
        //EventAdapter.setEvent(EventAdapter.SPEAK,this);
    }

    private void initView() {
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cellular.adjustArfcnPwrForLocTarget("460008274382921");
            }
        });

        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = Cellular.file_fcns + "########" + Cellular.final_fcns;
                tvArfcns.setText(content);

            }
        });

        test3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EventAdapter.call(EventAdapter.UPDATE_BATTERY, 10800);
//                UtilBaseLog.printLog(getSimIMSI((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE),0)+","+
//                        getSimIMSI((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE),1));
            }
        });

        btGetDeviceLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showMessage("获取设备命令已下发，请等待上传成功");
                LTE_PT_SYSTEM.commonSystemMsg(LTE_PT_SYSTEM.SYSTEM_GET_LOG);
            }
        });

        test4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProtocolManager.setActiveMode("0");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CacheManager.setLocalWhiteList("off");

                    }
                }, 1000);

            }
        });

        test5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VersionManage.isPoliceVer()){
                    ProtocolManager.setActiveMode("1");
                }


                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (VersionManage.isArmyVer()) {
                            CacheManager.setLocalWhiteList("on");
                        } else {
                            CacheManager.setLocalWhiteList("off");
                        }

                    }
                },1000);
            }
        });

        test6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProtocolManager.setActiveMode("2");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CacheManager.setLocalWhiteList("on");

                    }
                }, 1000);
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setPitch(1f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setSpeechRate(1f);
                    int result = textToSpeech.setLanguage(Locale.CHINESE);
                    LogUtils.log("语音播报中文：" + result);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        ToastUtils.showMessage(R.string.tip_08);
                    }
                }
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        textToSpeech.speak((int) (Math.random() * 100)+"", TextToSpeech.QUEUE_ADD, null);
                    }
                },0,2000);
            }
        });

        sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double v = (Math.round(progress * 10) / 10.0);
                float v1 = (float) (v / sbPitch.getMax());
                textToSpeech.setPitch(v1);
                tvPitch.setText("音调:"+v1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double v = (Math.round(progress * 10) / 10.0);
                float v1 = (float) (v / sbSpeed.getMax());
                textToSpeech.setSpeechRate(v1);
                tvSpeed.setText("语速:"+v1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        test7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProtocolManager.getNameList();

            }
        });

        test8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(TestActivity.this).inflate(R.layout.layout_input_fcn, null);
                EditText etMobile = view.findViewById(R.id.et_mobile_fcn);
                EditText etUnicom = view.findViewById(R.id.et_unicom_fcn);
                new AlertDialog.Builder(TestActivity.this).setTitle("请输入消息")
                        .setIcon(android.R.drawable.sym_def_app_icon)
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String mobileFcn = etMobile.getText().toString().trim();
                                String unicomFcn = etUnicom.getText().toString().trim();

                                if (TextUtils.isEmpty(mobileFcn)) {
                                    ToastUtils.showMessage("请输入移动2G频点");
                                    return;
                                }

                                if (TextUtils.isEmpty(unicomFcn)) {
                                    ToastUtils.showMessage("请输入联通2G频点");
                                    return;
                                }

                                ToastUtils.showMessage("已下发指派");

                                EventAdapter.call(EventAdapter.STOP_LOC);
                                ProtocolManager.openAllRf();

                                ProtocolManager.setActiveMode("2");

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        ProtocolManager.setNameList("on",
                                                "46000,2," + mobileFcn + "#46002,2," + mobileFcn + "#46007,2," + mobileFcn + "#46001,2," + unicomFcn, "",
                                                "", "", "redirect", "", "");

                                    }
                                }, 1000);

                            }
                        }).setNegativeButton("取消", null).show();


            }
        });


        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String  imsi = etImsi.getText().toString().trim();
                    if(!CacheManager.checkDevice(TestActivity.this)){
                        return;
                    }

                    if (TextUtils.isEmpty(imsi)){
                        ToastUtils.showMessage("请输入IMSI");
                        return;
                    }


                    if (CacheManager.getLocState()){
                        if (CacheManager.getCurrentLoction().getImsi().equals(imsi)){
                            ToastUtils.showMessage( "该号码正在搜寻中");
                            return;
                        }else{
                            EventAdapter.call(EventAdapter.SHOW_PROGRESS,8000);  //防止快速频繁更换定位目标

                            ProtocolManager.exchangeFcn(imsi);

                            CacheManager.updateLoc(imsi);
                            CacheManager.changeLocTarget(imsi);
                            ToastUtils.showMessage( "开始新的搜寻");
                        }
                    }else{
                        EventAdapter.call(EventAdapter.SHOW_PROGRESS,5000);  //防止快速频繁更换定位目标
                        ProtocolManager.exchangeFcn(imsi);

                        CacheManager.updateLoc(imsi);
                        CacheManager.startLoc(imsi);
                        ProtocolManager.openAllRf();
                        ToastUtils.showMessage("搜寻开始");
                    }

                    EventAdapter.call(EventAdapter.CHANGE_TAB, 1);

                    EventAdapter.call(EventAdapter.ADD_LOCATION,imsi);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST+imsi);
                } catch (Exception e) {
                    LogUtils.log("开启搜寻失败"+e);
                }
            }
        });

    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    tvTemperature.setText("温度：" + msg.obj);
                    break;
                case 1:
                    tvNameList.setText("白名单：" + CacheManager.namelist.toString());
                    break;
            }

        }
    };

    @Override
    public void call(String key, Object val) {
        switch (key) {
            case UPDATE_TMEPRATURE:
                Message msg = new Message();
                msg.what = 0;
                msg.obj = val;
                mHandler.sendMessage(msg);
                break;
            case GET_NAME_LIST:
                Message msg1 = new Message();
                msg1.what = 1;
                mHandler.sendMessage(msg1);
                break;
        }

    }
}
