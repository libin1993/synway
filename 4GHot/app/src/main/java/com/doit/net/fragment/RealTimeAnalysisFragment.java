package com.doit.net.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.doit.net.adapter.AnalysisResultAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.AnalysisResultBean;
import com.doit.net.data.CollideAnalysis;
import com.doit.net.model.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.ToastUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilDelay;
import com.doit.net.ucsi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

/**
 * Created by Zxc on 2018/12/14.
 */

public class RealTimeAnalysisFragment extends BaseFragment {

    private BootstrapButton btStartStop;
    private BootstrapButton btRestart;
    private boolean isStart = false;

    private long collideTempEndTime;   //每个碰撞时间段的结束时间
    private final int COLLIED_TIME_PERIOD = 60*1000;

    HashMap<String, Integer> mapTempCollideResult = new HashMap<>();

    private ListView lvRealtimeAnalysisResult;
    private List<AnalysisResultBean> listAnalysisResult = new ArrayList<>();
    private AnalysisResultAdapter analysisResultAdapter;
    private final int MAX_RESULT_TO_SHOW = 30;   //列表最后显示个数
    private Thread threadRealtimeCollide = null;

    //handler消息
    private final int UPDATE_COLLIDE_RESULT = 0;

    public RealTimeAnalysisFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_realtime_analysis, null);
        btStartStop = rootView.findViewById(R.id.btStartStop);
        lvRealtimeAnalysisResult = rootView.findViewById(R.id.lvAnalysisResult);
        btRestart = rootView.findViewById(R.id.btRestart);
        initWidget();
        return rootView;
    }


    private void initWidget() {

        btStartStop.setOnClickListener(startStopListener);

        btRestart.setOnClickListener(restartListener);

        analysisResultAdapter = new AnalysisResultAdapter(getContext(), R.layout.analysis_result_item, listAnalysisResult);

        lvRealtimeAnalysisResult.setAdapter(analysisResultAdapter);
        lvRealtimeAnalysisResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnalysisResultBean analysisResultBean = listAnalysisResult.get(position);
                List<String> listCollideDetail = CollideAnalysis.getRTCollideDetail(analysisResultBean.getImsi());
                showAnalysisDetailDialog(analysisResultBean.getImsi(), listCollideDetail);
            }
        });
    }

    private void showAnalysisDetailDialog(String imsi, List<String> listAllDetectTime) {
        final String[] stringsAllDetectTime = listAllDetectTime.toArray(new String[listAllDetectTime.size()]);
        AlertDialog.Builder listDialog = new AlertDialog.Builder(getActivity());
        listDialog.setTitle("IMSI："+imsi+"\n"+ "所有上报时间点：");
        listDialog.setItems(stringsAllDetectTime, null);
        listDialog.setNegativeButton("关闭",null);
        listDialog.show();
    }

    private void UpdateCollideResultToList() {
        //按照次数从小到排序
        List<HashMap.Entry<String, Integer>> sortedCollideResult = new ArrayList<>(mapTempCollideResult.entrySet());
        Collections.sort(sortedCollideResult, new Comparator<HashMap.Entry<String, Integer>>() {
            // 升序排序
            public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //排序后的前几个放入列表
        int count = 0;
        listAnalysisResult.clear();
        for (HashMap.Entry<String, Integer> tmpCollideResult : sortedCollideResult) {
            count++;
            listAnalysisResult.add(new AnalysisResultBean(tmpCollideResult.getKey(), String.valueOf(tmpCollideResult.getValue())));
            if (count >= MAX_RESULT_TO_SHOW)
                break;
            //System.out.println("Key=" + tmpCollideResult.getKey() + ", Value=" + tmpCollideResult.getValue());
        }
    }

    SwipeMenuCreator timeCreator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem openItem = new SwipeMenuItem(getContext());
            //openItem.setBackground(getResources().getColor(R.color.red));
            openItem.setWidth(220);
            openItem.setTitle("删除");
            openItem.setTitleSize(14);
            openItem.setTitleColor(Color.RED);
            menu.addMenuItem(openItem);
        }
    };

    View.OnClickListener startStopListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(View v) {
            if (isStart){
                //UtilBaseLog.printLog("关");
                btStartStop.setText("开始跟踪");
                btStartStop.setBootstrapBrand(DefaultBootstrapBrand.PRIMARY);
                isStart = false;

                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.PAUSE_FOLLOWING);
            }else{
                //collideTempEndTime = new Date().getRptTime();
                if (!CacheManager.checkDevice(getContext()))
                    return;

                isStart = true;
                startRealtimeCollide();
                btStartStop.setText("正在跟踪");
                btStartStop.setBootstrapBrand(DefaultBootstrapBrand.WARNING);

                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.START_FOLLOWING);
            }
        }
    };

    View.OnClickListener restartListener = new View.OnClickListener() {
        @Override
        public synchronized void onClick(View v) {
            if(threadRealtimeCollide == null){
                ToastUtils.showMessage("跟踪未开始");
                return;
            }else {
                new SweetAlertDialog(getActivity(), WARNING_TYPE)
                    .setTitleText("提示")
                    .setContentText("当前结果将被清空，确定重新开始吗？")
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            CollideAnalysis.restartRealtimeCollide();
                            mapTempCollideResult.clear();
                            listAnalysisResult.clear();
                            isStart = false;
                            mHandler.sendEmptyMessage(UPDATE_COLLIDE_RESULT);
                            sweetAlertDialog.dismiss();

                            EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.RESTART_FOLLOWING);
                        }
                    }).show();
            }
        }
    };


    private void startRealtimeCollide() {
        if ( threadRealtimeCollide == null){
            threadRealtimeCollide = new Thread(new Runnable() {
                @Override
                public synchronized void run() {
                    while(true){
                        if (isStart){
                            LogUtils.log("实时碰撞当前大小：" + mapTempCollideResult.size());
                            collideTempEndTime = new Date().getTime();
                            CollideAnalysis.collideAnalysis(mapTempCollideResult, collideTempEndTime, COLLIED_TIME_PERIOD);
                            UpdateCollideResultToList();
                            ProtocolManager.changeTac();
                            mHandler.sendEmptyMessage(UPDATE_COLLIDE_RESULT);
                            ToastUtils.showMessage("跟踪结果已刷新");
                            UtilDelay.delayMilis(COLLIED_TIME_PERIOD - 5000);
                        }
                        UtilDelay.delayMilis(5000);
                    }
                }
            });
            threadRealtimeCollide.start();
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_COLLIDE_RESULT) {
                if (analysisResultAdapter != null) {
                    analysisResultAdapter.updateView();
                }
            }
        }
    };
}
