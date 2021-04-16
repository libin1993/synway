package com.doit.net.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doit.net.utils.BlackBoxManger;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.VersionManage;
import com.doit.net.protocol.LTESendManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.LTE_PT_SYSTEM;
import com.doit.net.utils.Cellular;
import com.doit.net.utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.doit.net.event.EventAdapter.GET_ACTIVE_MODE;
import static com.doit.net.event.EventAdapter.GET_NAME_LIST;
import static com.doit.net.event.EventAdapter.UPDATE_TMEPRATURE;

public class TestActivity extends BaseActivity implements EventAdapter.EventCall {
    private Button test1;
    private Button test2;
    private Button test3;
    private Button test6;
    private Button test7;
    private Button test8;
    private Button btGetDeviceLog;
    private TextView tvTemperature;
    private TextView tvArfcns;
    private TextView tvNameList;
    private EditText etImsi;
    private Button btnLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_for_test);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        test1 = findViewById(R.id.test1);
        test2 = findViewById(R.id.test2);
        test3 = findViewById(R.id.test3);
        test6 = findViewById(R.id.test6);
        test7 = findViewById(R.id.test7);
        test8 = findViewById(R.id.test8);
        etImsi = findViewById(R.id.et_imsi);
        btnLoc = findViewById(R.id.btn_loc);
        btGetDeviceLog = findViewById(R.id.btGetDeviceLog);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvArfcns = findViewById(R.id.tvArfcns);
        tvNameList = findViewById(R.id.tv_name_list);


        initView();

        EventAdapter.register(UPDATE_TMEPRATURE, this);
        EventAdapter.register(EventAdapter.GET_NAME_LIST, this);
        EventAdapter.register(EventAdapter.GET_ACTIVE_MODE, this);
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

        test6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LTESendManager.getActiveMode();
            }
        });

        test7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LTESendManager.getNameList();
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
                                LTESendManager.openAllRf();

                                LTESendManager.setActiveMode("2");

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        LTESendManager.setNameList("on",
                                                "46000,2," + mobileFcn + "#46002,2," + mobileFcn + "#46007,2," + mobileFcn + "#46001,2," + unicomFcn, "",
                                                "", "", "redirect",  "");

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
                        if (CacheManager.getCurrentLocation().getImsi().equals(imsi)){
                            ToastUtils.showMessage( "该号码正在搜寻中");
                            return;
                        }else{
                            EventAdapter.call(EventAdapter.SHOW_PROGRESS,8000);  //防止快速频繁更换定位目标

                            LTESendManager.exchangeFcn(imsi);

                            CacheManager.updateLoc(imsi);
                            if (!VersionManage.isArmyVer()){
                                LTESendManager.setLocImsi(imsi);
                            }
                            ToastUtils.showMessage( "开始新的搜寻");
                        }
                    }else{
                        EventAdapter.call(EventAdapter.SHOW_PROGRESS,5000);  //防止快速频繁更换定位目标
                        LTESendManager.exchangeFcn(imsi);

                        CacheManager.updateLoc(imsi);
                        CacheManager.startLoc(imsi);
                        LTESendManager.openAllRf();
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
                    tvNameList.setText("名单：" + msg.obj);
                    break;
                case 2:
                    test6.setText("查询工作模式：" + msg.obj);
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
                msg1.obj = val;
                mHandler.sendMessage(msg1);
                break;
            case GET_ACTIVE_MODE:
                Message msg2 = new Message();
                msg2.what = 2;
                msg2.obj = val;
                mHandler.sendMessage(msg2);
                break;
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
