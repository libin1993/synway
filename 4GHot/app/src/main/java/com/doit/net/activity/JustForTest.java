package com.doit.net.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.doit.net.base.BaseActivity;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Protocol.LTE_PT_SYSTEM;
import com.doit.net.Utils.Cellular;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

public class JustForTest extends BaseActivity implements EventAdapter.EventCall {
    private Button test1;
    private Button test2;
    private Button test3;
    private Button btGetDeviceLog;
    private TextView tvTemperature;
    private TextView tvArfcns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_just_for_test);

        test1 = (Button) findViewById(R.id.test1);
        test2 = (Button) findViewById(R.id.test2);
        test3 = (Button) findViewById(R.id.test3);
        btGetDeviceLog = (Button) findViewById(R.id.btGetDeviceLog);
        tvTemperature = (TextView) findViewById(R.id.tvTemperature);
        tvArfcns = (TextView) findViewById(R.id.tvArfcns);

        initView();

        EventAdapter.setEvent(EventAdapter.UPDATE_TMEPRATURE,this);
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
                String content = Cellular.file_fcns+"########"+Cellular.final_fcns;
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
                ToastUtils.showMessage(getBaseContext(), "获取设备命令已下发，请等待上传成功");
                LTE_PT_SYSTEM.commonSystemMsg(LTE_PT_SYSTEM.SYSTEM_GET_LOG);
            }
        });
    }





    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                tvTemperature.setText("温度："+(String)msg.obj);
            }
        }
    };

    @Override
    public void call(String key, Object val) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = val;
        mHandler.sendMessage(msg);
    }
}
