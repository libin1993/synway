package com.doit.net.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.View.AddTimePointDialog;
import com.doit.net.adapter.AnalysisResultAdapter;
import com.doit.net.adapter.CollideTimePointAdapter;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.AnalysisResultBean;
import com.doit.net.bean.CollideTimePointBean;
import com.doit.net.Data.CollideAnalysis;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Zxc on 2018/12/17.
 */

public class PointCollideFragment extends BaseFragment {
    private View rootView;

    private BootstrapButton btAddTimePoint;
    private BootstrapButton btStartCollide;
    private BootstrapEditText etDeviation;

    private LinearLayout layoutCollideResult;

    private DbManager dbManager;

    private SwipeMenuListView lvCollideTimePoint;
    private List<CollideTimePointBean> listCollideTimePoint = new ArrayList<>();
    private CollideTimePointAdapter collideTimePointAdapter;

    private ListView lvCollideResult;
    private List<AnalysisResultBean> listCollideResult = new ArrayList<>();
    private AnalysisResultAdapter analysisResultAdapter;
    private List<Long> listCollideTimeLong = new ArrayList<>();
    private long deviation = 0;
    private final int MAX_COLLIDE_RESULT_TO_SHOW = 30;   //列表最后显示个数

    //handler消息
    private final int UPDATE_COLLIDE_RESULT = 0;

    public PointCollideFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;

        rootView = inflater.inflate(R.layout.fragment_point_collide, null);
        initWidget();
        dbManager = UCSIDBManager.getDbManager();

        return rootView;
    }

    private void initWidget() {
        collideTimePointAdapter = new CollideTimePointAdapter(getContext(), listCollideTimePoint);
        lvCollideTimePoint = rootView.findViewById(R.id.lvCollideTimePoint);
        lvCollideTimePoint.setAdapter(collideTimePointAdapter);
        lvCollideTimePoint.setMenuCreator(timePointCreator);
        lvCollideTimePoint.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        listCollideTimeLong.remove(position);
                        listCollideTimePoint.remove(position);
                        collideTimePointAdapter.updateView();
                        break;
                }

                return true;
            }
        });

        //以下为了解决scrollView嵌套listview时，listview无法滑动问题
        lvCollideTimePoint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                lvCollideTimePoint.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        etDeviation = rootView.findViewById(R.id.etDeviation);
        btAddTimePoint = rootView.findViewById(R.id.btAddTimePoint);
        btAddTimePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AddTimePointDialog addTimePointDialog = new AddTimePointDialog(getActivity(),
                    DateUtils.convert2String(new Date().getTime(), DateUtils.LOCAL_DATE), new AddTimePointDialog.IAddTimePointListener() {
                @Override
                public void addTimePoint(String timePoint, String remark) {
                    listCollideTimeLong.add(DateUtils.convert2long(timePoint, DateUtils.LOCAL_DATE));
                    listCollideTimePoint.add(new CollideTimePointBean(timePoint, remark));
                    collideTimePointAdapter.updateView();
                }
            });
            addTimePointDialog.show();
            }
        });


        btStartCollide = rootView.findViewById(R.id.btStartCollide);
        btStartCollide.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (listCollideTimePoint.size() < 2){
                    ToastUtils.showMessage(getContext(), "请至少选择两个时间点！");
                    return;
                }

                if ("".equals(etDeviation.getText().toString())){
                    ToastUtils.showMessage(getContext(), "请请输入时间偏差！");
                    return;
                }

                deviation = Long.parseLong(etDeviation.getText().toString())*60*1000;
                HashMap<String, Integer> mapImsiWithTimes = CollideAnalysis.collideAnalysis(listCollideTimeLong, deviation);
                UpdateCollideResultToList(mapImsiWithTimes);
                if (listCollideResult.size() == 0){
                    layoutCollideResult.setVisibility(View.GONE);
                    ToastUtils.showMessage(getContext(), "无碰撞结果！");
                }else {
                    layoutCollideResult.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessage(UPDATE_COLLIDE_RESULT);
                }

                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.TIME_POINT_COLLIDE);
            }});

        analysisResultAdapter = new AnalysisResultAdapter(getContext(), R.layout.analysis_result_item, listCollideResult);
        lvCollideResult = rootView.findViewById(R.id.lvAnalysisResult);
        lvCollideResult.setAdapter(analysisResultAdapter);
        lvCollideResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnalysisResultBean analysisResultBean = listCollideResult.get(position);
                List<String> listCollideDetail = CollideAnalysis.getTPCollideDetail(analysisResultBean.getImsi(), listCollideTimeLong, deviation);
                showCollideDetailDialog(analysisResultBean.getImsi(), listCollideDetail);
            }
        });

        layoutCollideResult = rootView.findViewById(R.id.layoutCollideResult);
    }

    private void showCollideDetailDialog(String imsi, List<String> listAllDetectTime) {
        final String[] stringsAllDetectTime = listAllDetectTime.toArray(new String[listAllDetectTime.size()]);
        AlertDialog.Builder listDialog = new AlertDialog.Builder(getActivity());
        listDialog.setTitle("IMSI："+imsi+"\n"+ "出现的时间：");
        listDialog.setItems(stringsAllDetectTime, null);
        listDialog.setNegativeButton("关闭",null);
        listDialog.show();
    }

    private void UpdateCollideResultToList(HashMap<String, Integer> mapImsiWithTimes) {
        //按照次数从小到排序
        List<HashMap.Entry<String, Integer>> sortedCollideResult = new ArrayList<>(mapImsiWithTimes.entrySet());
        Collections.sort(sortedCollideResult, new Comparator<HashMap.Entry<String, Integer>>() {
            // 升序排序
            public int compare(HashMap.Entry<String, Integer> o1, HashMap.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //排序后的前几个放入列表
        int count = 0;
        listCollideResult.clear();
        for (HashMap.Entry<String, Integer> tmpCollideResult : sortedCollideResult) {
            count++;
            listCollideResult.add(new AnalysisResultBean(tmpCollideResult.getKey(), String.valueOf(tmpCollideResult.getValue())));
            if (count >= MAX_COLLIDE_RESULT_TO_SHOW)
                break;
            //System.out.println("Key=" + tmpCollideResult.getKey() + ", Value=" + tmpCollideResult.getValue());
        }
    }

    SwipeMenuCreator timePointCreator = new SwipeMenuCreator() {
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
