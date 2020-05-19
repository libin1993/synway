package com.doit.net.activity;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.doit.net.adapter.ScanFreqRstAdapter;
import com.doit.net.base.BaseActivity;
import com.doit.net.bean.ScanFreqRstBean;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.adapter.ScanFreqRstAdapter.ViewHolder;
import com.doit.net.Model.CacheManager;
import com.doit.net.ucsi.R;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class ScanFreqConfigActivity extends BaseActivity implements EventAdapter.EventCall {
    private Activity activity = this;

    private List<ScanFreqRstBean> listScanFreqResult;
    private ListView lvScanFreqResult;
    private ScanFreqRstAdapter scanFreqRstAdapter;
    private BootstrapButton btStartScan;
    private BootstrapButton btConfigSelectedFcn;

    private final int SCAN_FREQ_RPT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_freq_config);

        btStartScan = (BootstrapButton) findViewById(R.id.btStartScan);
        btStartScan.setOnClickListener(startScanFreqClick);
        btConfigSelectedFcn = (BootstrapButton) findViewById(R.id.btConfigSelectedFcn);
        btConfigSelectedFcn.setOnClickListener(configSelectedFcnClick);
        lvScanFreqResult = (ListView) findViewById(R.id.lvScanFreqResult);

        listScanFreqResult = CacheManager.listLastScanFreqRst;
        scanFreqRstAdapter = new ScanFreqRstAdapter(listScanFreqResult, this);
        lvScanFreqResult.setAdapter(scanFreqRstAdapter);
        lvScanFreqResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                // 改变CheckBox的状态
                viewHolder.cbIsSelected.toggle();
                // 将CheckBox的选中状况记录下来
                listScanFreqResult.get(position).setSelected(viewHolder.cbIsSelected.isChecked());
            }
        });

        EventAdapter.setEvent(EventAdapter.SCAN_FREQ_RPT,this);
    }

    View.OnClickListener configSelectedFcnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String selectedFcn = "";
            for (ScanFreqRstBean scanFreqRstBean : listScanFreqResult){
                if (scanFreqRstBean.isSelected()){
                    selectedFcn += scanFreqRstBean.getScanFreqRst();
                }
            }

            if ("".equals(selectedFcn)){
                ToastUtils.showMessage(activity, "请从一下搜网结果中选择要配置的频点");
            }

            new SweetAlertDialog(activity, WARNING_TYPE)
                    .setTitleText("提示")
                    .setContentText("当前搜网频点为xxxxxxxxx，确定设置覆盖吗？")
                    .setCancelText(activity.getString(R.string.cancel))
                    .setConfirmText(activity.getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            //下发设置


                            sweetAlertDialog.dismiss();
                        }
                    }).show();
            //UtilBaseLog.printLog("选中的搜网频点："+ selectedFcn);
        }
    };

    View.OnClickListener startScanFreqClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //ProtocolManager.scanFreq();
            List<ScanFreqRstBean> list = new ArrayList<>();
            list.add(new ScanFreqRstBean("频点：380950", false));
            list.add(new ScanFreqRstBean("频点：100", false));
            list.add(new ScanFreqRstBean("频点：400", false));
            list.add(new ScanFreqRstBean("频点：375", false));
            list.add(new ScanFreqRstBean("频点：38300", false));
            EventAdapter.call(EventAdapter.SCAN_FREQ_RPT, list);
        }
    };


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == SCAN_FREQ_RPT){
                listScanFreqResult = (List<ScanFreqRstBean>) msg.obj;
                CacheManager.listLastScanFreqRst = (List<ScanFreqRstBean>) msg.obj;

                if(scanFreqRstAdapter != null && msg.obj != null){
                    scanFreqRstAdapter.setList(listScanFreqResult);
                }
            }
        }
    };

    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.SCAN_FREQ_RPT)) {
            Message msg = new Message();
            msg.what = SCAN_FREQ_RPT;
            msg.obj = val;
            mHandler.sendMessage(msg);
        }
    }
}
