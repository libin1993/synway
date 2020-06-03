package com.doit.net.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.doit.net.adapter.AnalysisResultAdapter;
import com.doit.net.adapter.CollideTimePeriodAdapter;
import com.doit.net.View.MyTimePickDialog;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.AnalysisResultBean;
import com.doit.net.bean.CollideTimePeriodBean;
import com.doit.net.Data.CollideAnalysis;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.MyCommonDialog;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Zxc on 2018/11/21.
 */

public class CollideAnalysisFragment extends BaseFragment {
//    private View rootView;

    private EditText etCollideStartTime;
    private EditText etCollideEndTime;
    private Button btAddTimePeriod;
    private Button btStartCollide;
    private Button btExportCollideResult;

    private LinearLayout layoutCollideResult;



    private SwipeMenuListView lvCollideTimePeriod;
    private List<CollideTimePeriodBean> listCollideTimePeriod = new ArrayList<>();
    private CollideTimePeriodAdapter collideTimePeriodAdapter;


    private ListView lvCollideResult;
    private List<AnalysisResultBean> listCollideResult = new ArrayList<>();
    private AnalysisResultAdapter analysisResultAdapter;
    private final int MAX_COLLIDE_RESULT_TO_SHOW = 30;   //列表最后显示个数

    //handler消息
    private final int UPDATE_RESULT_LIST = 0;
    private final int EXPORT_ERROR = -1;

    public CollideAnalysisFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        if (rootView != null)
//            return rootView;

        View rootView = inflater.inflate(R.layout.fragment_collide_analysis, null);
        etCollideStartTime = (EditText) rootView.findViewById(R.id.etCollideStartTime);
        etCollideEndTime = (EditText) rootView.findViewById(R.id.etCollideEndTime);
        lvCollideTimePeriod = (SwipeMenuListView)rootView.findViewById(R.id.lvCollideTimePeriod) ;
        btStartCollide = (Button) rootView.findViewById(R.id.btStartCollide);

        btAddTimePeriod = (Button) rootView.findViewById(R.id.btAddTimePeriod);
        btExportCollideResult = (Button) rootView.findViewById(R.id.btExportCollideResult);
        lvCollideResult = (ListView)rootView.findViewById(R.id.lvAnalysisResult) ;
        layoutCollideResult = (LinearLayout) rootView.findViewById(R.id.layoutCollideResult);
        initWidget();

        return rootView;
    }


    private void initWidget() {

        etCollideStartTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(getActivity(), etCollideStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etCollideStartTime);
            }
        });



        etCollideEndTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(getActivity(), etCollideEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etCollideEndTime);
            }
        });

        collideTimePeriodAdapter = new CollideTimePeriodAdapter(getContext(), listCollideTimePeriod);

        lvCollideTimePeriod.setAdapter(collideTimePeriodAdapter);
        lvCollideTimePeriod.setMenuCreator(timePerioCreator);
        lvCollideTimePeriod.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                if (index == 0) {

                    listCollideTimePeriod.remove(position);
                    collideTimePeriodAdapter.updateView();
                }

                return true;
            }
        });

        btAddTimePeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startTime = etCollideStartTime.getText().toString();
                String endTime = etCollideEndTime.getText().toString();
                if ("".equals(startTime) || "".equals(endTime)){
                    ToastUtils.showMessage(getContext(), "还未设置开始时间或者结束时间！");
                    return;
                }else if (startTime.equals(endTime)){
                    ToastUtils.showMessage(getContext(), "开始时间和结束时间一样，请重新设置！");
                    return;
                }else if (!DateUtils.isStartEndTimeOrderRight(startTime, endTime)){
                    ToastUtils.showMessage(getContext(), "开始时间比结束时间晚，请重新设置！");
                    return;
                }else if(checkTimePeriodExist(startTime, endTime)){
                    ToastUtils.showMessage(getContext(), "此时间段已添加过，请勿重复添加！");
                    return;
                }

                listCollideTimePeriod.add(new CollideTimePeriodBean(startTime, endTime));
                collideTimePeriodAdapter.updateView();

            }
        });



        btStartCollide.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (listCollideTimePeriod.size() < 2){
                    ToastUtils.showMessage(getContext(), "请至少选择两个时间段！");
                    return;
                }

                HashMap<String, Integer> mapImsiWithTimes = CollideAnalysis.collideAnalysis(listCollideTimePeriod);
                UpdateCollideResultToList(mapImsiWithTimes);
                if (listCollideResult.size() == 0){
                    layoutCollideResult.setVisibility(View.GONE);
                    ToastUtils.showMessage(getContext(), "无碰撞结果！");
                }else {
                    layoutCollideResult.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessage(UPDATE_RESULT_LIST);
                }

                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.TIME_PERIOD_COLLIDE);
            }
        });



        btExportCollideResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(listCollideResult.size() == 0){
                    ToastUtils.showMessage(getContext(),"无碰撞结果，导出失败！");
                    return;
                }

                ExportCollideResult(listCollideResult);
            }
        });

        analysisResultAdapter = new AnalysisResultAdapter(getContext(), R.layout.analysis_result_item, listCollideResult);

        lvCollideResult.setAdapter(analysisResultAdapter);
        lvCollideResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnalysisResultBean analysisResultBean = listCollideResult.get(position);
                List<String> listCollideDetail = CollideAnalysis.getCollideDetail(analysisResultBean.getImsi());
                showCollideDetailDialog(analysisResultBean.getImsi(), listCollideDetail);
            }
        });

        lvCollideTimePeriod.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                lvCollideTimePeriod.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    private void ExportCollideResult(List<AnalysisResultBean> listCollideResult) {
        boolean isSuccess = true;
        final String COLLIDE_RES_FILE_PATH =  Environment.getExternalStorageDirectory()+"/4GHotspot/export/";

        String fileName = "COLLIDE_"+ DateUtils.getStrOfDate()+".txt";
        String fullPath = COLLIDE_RES_FILE_PATH+fileName;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath,true)));
            bufferedWriter.write("imsi,次数"+"\r\n");
            for (AnalysisResultBean info: listCollideResult) {
                //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                bufferedWriter.write(info.getImsi()+",");
                bufferedWriter.write(info.getTimes());
                bufferedWriter.write("\r\n");
            }
        }  catch (FileNotFoundException e){
            //log.error("File Error",e);
            isSuccess = false;
            e.printStackTrace();
            createExportError("文件未创建成功");
        } catch (IOException e){
            //log.error("File Error",e);
            isSuccess = false;
            e.printStackTrace();
            createExportError("写入文件错误");
        } finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }

        if (isSuccess){
            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, fullPath);
            new MySweetAlertDialog(getContext(), MySweetAlertDialog.TEXT_SUCCESS)
                    .setTitleText("导出成功")
                    .setContentText("文件导出在：手机存储/4GHotspot/"+ fileName)
                    .show();
        }
    }

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    /**
     * @param startTime
     * @param endTime
     * @return  判断重复时间段
     */
    private boolean checkTimePeriodExist(String startTime, String endTime) {
        boolean isExist = false;
        for (CollideTimePeriodBean collideTimePeriodBean : listCollideTimePeriod) {
            if (startTime.equals(collideTimePeriodBean.getStartTime())
                    && endTime.equals(collideTimePeriodBean.getEndTime())){
                isExist = true;
                break;
            }
        }

        return isExist;
    }

    private void showCollideDetailDialog(String imsi, List<String> listAllDetectTime) {
        final String[] stringsAllDetectTime = listAllDetectTime.toArray(new String[listAllDetectTime.size()]);
        MyCommonDialog myCommonDialog = new MyCommonDialog(getActivity());
        myCommonDialog.setTitle("目标："+imsi+"\n"+ "所有出现的时间：");
        myCommonDialog.setItems(stringsAllDetectTime, null);
        myCommonDialog.setNegativeButton("关闭",null);
        myCommonDialog.showDialog();
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



    SwipeMenuCreator timePerioCreator = new SwipeMenuCreator() {
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
            if (msg.what == UPDATE_RESULT_LIST) {
                if (analysisResultAdapter != null) {
                    analysisResultAdapter.updateView();
                }
            }else if(msg.what == EXPORT_ERROR){
                new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }
        }
    };
}

