package com.doit.net.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.adapter.UeidListViewAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.LteChannelCfg;
import com.doit.net.bean.UeidBean;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.utils.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilOperator;
import com.doit.net.ucsi.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RealTimeUeidRptFragment extends BaseFragment implements EventAdapter.EventCall {
    private ListView mListView;
    private UeidListViewAdapter mAdapter;
    private Button btClearRealtimeUeid;

    private TextView tvRealtimeCTJCount;
    private TextView tvRealtimeCTUCount;
    private TextView tvRealtimeCTCCount;
    private int realtimeCTJCount = 0;
    private int realtimeCTUCount = 0;
    private int realtimeCTCCount = 0;

    private CheckBox cbDetectSwitch;

    private long lastSortTime = 0;  //为了防止频繁上报排序导致列表错乱，定时排序一次

    //handler消息
    private final int UEID_RPT = 1;
    private final int SHIELD_RPT = 2;
    private final int RF_STATUS_RPT = 3;


    public RealTimeUeidRptFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.doit_layout_ueid_list, container, false);
        mListView = rootView.findViewById(R.id.listview);
        btClearRealtimeUeid = rootView.findViewById(R.id.button_clear);
        btClearRealtimeUeid.setOnClickListener(clearListener);

        tvRealtimeCTJCount = rootView.findViewById(R.id.tvCTJCount);
        tvRealtimeCTUCount = rootView.findViewById(R.id.tvCTUCount);
        tvRealtimeCTCCount = rootView.findViewById(R.id.tvCTCCount);
        cbDetectSwitch = rootView.findViewById(R.id.cbDetectSwitch);
        initView();

        EventAdapter.register(EventAdapter.RF_STATUS_RPT, this);
        EventAdapter.register(EventAdapter.UEID_RPT, this);
        EventAdapter.register(EventAdapter.SHIELD_RPT, this);

        return rootView;
    }

    private void initView() {


        mAdapter = new UeidListViewAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).setClickToClose(true);
            }
        });


        cbDetectSwitch.setOnCheckedChangeListener(null);
        cbDetectSwitch.setChecked(CacheManager.isDeviceOk());
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
    }

    CompoundButton.OnCheckedChangeListener rfDetectSwichtListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (!compoundButton.isPressed()) {
                return;
            }

            if (!CacheManager.checkDevice(getContext())) {
                cbDetectSwitch.setChecked(!isChecked);
                return;
            }

            if (isChecked) {
                ProtocolManager.openAllRf();
                ToastUtils.showMessageLong(R.string.all_rf_open);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.OPEN_ALL_RF);
                EventAdapter.call(EventAdapter.SHOW_PROGRESS, 6000);
            } else {
                if (CacheManager.getLocState()) {
                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
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
                                    ToastUtils.showMessage(R.string.all_rf_close);
                                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 6000);
                                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_RF);
                                }
                            })
                            .setCancelClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(MySweetAlertDialog mySweetAlertDialog) {
                                    mySweetAlertDialog.dismiss();
                                    cbDetectSwitch.setOnCheckedChangeListener(null);
                                    cbDetectSwitch.setChecked(true);
                                    cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
                                }
                            })
                            .show();
                } else {
                    ProtocolManager.closeAllRf();
                    ToastUtils.showMessageLong(R.string.all_rf_close);
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS, 6000);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLOSE_ALL_RF);
                }
            }
        }
    };


    private void addShildRptList(List<UeidBean> ueidList) {
        for (UeidBean ueidBean : ueidList) {
            LogUtils.log("侦码上报: IMSI：" + ueidBean.getImsi() + "强度：" + ueidBean.getSrsp());
            boolean isContain = false;
            for (int i = 0; i < CacheManager.realtimeUeidList.size(); i++) {
                if (CacheManager.realtimeUeidList.get(i).getImsi().equals(ueidBean.getImsi())) {
                    int times = CacheManager.realtimeUeidList.get(i).getRptTimes();
                    if (times > 1000) {
                        times = 0;
                    }
                    CacheManager.realtimeUeidList.get(i).setRptTimes(times + 1);
                    CacheManager.realtimeUeidList.get(i).setSrsp("" + Integer.parseInt(ueidBean.getSrsp()) * 5 / 6);
                    CacheManager.realtimeUeidList.get(i).setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));

                    isContain = true;
                }
            }

            if (!isContain){
                UeidBean newUeid = new UeidBean();
                newUeid.setImsi(ueidBean.getImsi());
                newUeid.setSrsp("" + Integer.parseInt(ueidBean.getSrsp()) * 5 / 6);
                newUeid.setRptTime(DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE));
                newUeid.setRptTimes(1);
                CacheManager.realtimeUeidList.add(newUeid);

                UCSIDBManager.saveUeidToDB(ueidBean.getImsi(), "", "",
                        new Date().getTime(), "", "");
            }

        }

    }


    private void updateUeidCntInOperator(List<UeidBean> realtimeUeidList) {
        realtimeCTJCount = 0;
        realtimeCTUCount = 0;
        realtimeCTCCount = 0;

        for (int i = 0; i < realtimeUeidList.size(); i++) {
            switch (UtilOperator.getOperatorName(realtimeUeidList.get(i).getImsi())) {
                case "CTJ":
                    realtimeCTJCount++;
                    break;
                case "CTU":
                    realtimeCTUCount++;
                    break;
                case "CTC":
                    realtimeCTCCount++;
                    break;
                default:
                    break;
            }
        }
    }


    private void updateView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        updateUeidCntInOperator(CacheManager.realtimeUeidList);

        tvRealtimeCTJCount.setText(String.valueOf(realtimeCTJCount));
        tvRealtimeCTUCount.setText(String.valueOf(realtimeCTUCount));
        tvRealtimeCTCCount.setText(String.valueOf(realtimeCTCCount));
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UEID_RPT:
                    if (CacheManager.currentWorkMode.equals("2"))  //管控模式忽略ftp上报的
                        return;
                    //确保到达这里的采集数据已经去过重，并存到数据库了
                    List<UeidBean> listUeid = (List<UeidBean>) msg.obj;

                    addShildRptList(listUeid);

                    if (new Date().getTime() - lastSortTime >= 3000) {
                        Collections.sort(CacheManager.realtimeUeidList, new Comparator<UeidBean>() {
                            public int compare(UeidBean o1, UeidBean o2) {
                                return (int) (DateUtils.convert2long(o2.getRptTime(),DateUtils.LOCAL_DATE) -
                                        DateUtils.convert2long(o1.getRptTime(),DateUtils.LOCAL_DATE));
                            }
                        });

                        lastSortTime = new Date().getTime();
                    }


                    updateView();
                    break;
                case SHIELD_RPT:
                    List<UeidBean> ueidList = (List<UeidBean>) msg.obj;

                    addShildRptList(ueidList);
                    sortRealtimeRpt();
                    updateView();
                    break;
                case RF_STATUS_RPT:
                    isRFOpen();
                    break;

            }
        }
    };


    //根据强度排序
    private void sortRealtimeRpt() {
        if (!CacheManager.currentWorkMode.equals("2"))
            return;

        if (new Date().getTime() - lastSortTime >= 3000) {
            Collections.sort(CacheManager.realtimeUeidList, new Comparator<UeidBean>() {
                public int compare(UeidBean o1, UeidBean o2) {
                    return Integer.valueOf(o2.getSrsp()).compareTo(Integer.valueOf(o1.getSrsp()));
                }
            });

            lastSortTime = new Date().getTime();
        }
    }


    /**
     * 开启射频耗时操作,此时射频还未收到设备射频开启回复
     */
    @Override
    public void onResume() {
        super.onResume();
        isRFOpen();
    }

    /**
     * 射频是否开启
     */
    private void isRFOpen() {
        boolean rfState = false;

        for (LteChannelCfg channel : CacheManager.getChannels()) {
            if (channel.getRFState()) {
                rfState = true;
                break;
            }
        }

        cbDetectSwitch.setOnCheckedChangeListener(null);
        cbDetectSwitch.setChecked(rfState);
        cbDetectSwitch.setOnCheckedChangeListener(rfDetectSwichtListener);
    }


    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(View v) {
            CacheManager.realtimeUeidList.clear();
            lastSortTime = new Date().getTime();
            updateView();
        }
    };

    @Override
    public void call(String key, Object val) {
        switch (key) {
            case EventAdapter.SHIELD_RPT:
                Message msg = new Message();
                msg.what = SHIELD_RPT;
                msg.obj = val;
                mHandler.sendMessage(msg);
                break;
            case EventAdapter.UEID_RPT:
                Message message = new Message();
                message.what = UEID_RPT;
                message.obj = val;
                mHandler.sendMessage(message);
                break;
            case EventAdapter.RF_STATUS_RPT:
                mHandler.sendEmptyMessage(RF_STATUS_RPT);
                break;
        }

    }
}
