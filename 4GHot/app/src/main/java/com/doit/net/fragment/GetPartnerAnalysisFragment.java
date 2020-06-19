package com.doit.net.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.doit.net.Utils.FileUtils;
import com.doit.net.adapter.AnalysisResultAdapter;
import com.doit.net.View.MyTimePickDialog;
import com.doit.net.base.BaseFragment;
import com.doit.net.bean.AnalysisResultBean;
import com.doit.net.Data.GettingPartnerAnalysis;
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
 * Created by Zxc on 2018/12/11.
 */

public class GetPartnerAnalysisFragment extends BaseFragment {

    private EditText etStartTime;
    private EditText etEndTime;
    private Button btStartGettPartner;
    private Button btExportGetPartnerRes;
    private EditText etGetPartnerIMSI;
    private EditText etDeviation;
    private TextView tvTargetIMSI;
    private TextView tvTargetIMSITimes;

    private LinearLayout layoutCollideTarget;


    private ListView lvGetPartnerResult;
    private List<AnalysisResultBean> listCollideResult = new ArrayList<>();
    private AnalysisResultAdapter analysisResultAdapter;
    private final int MAX_COLLIDE_RESULT_TO_SHOW = 30;   //列表最后显示个数
    private String saveTargetImsi = "";
    private int saveTargetTimes = 1;
    //handler消息
    private final int UPDATE_RESULT_LIST = 0;
    private final int EXPORT_ERROR = -1;

    public GetPartnerAnalysisFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_get_partner, null);
        etStartTime = rootView.findViewById(R.id.etStartTime);
        etEndTime = rootView.findViewById(R.id.etEndTime);
        etGetPartnerIMSI = rootView.findViewById(R.id.etGetPartnerIMSI);
        etDeviation = rootView.findViewById(R.id.etDeviation);
        btStartGettPartner = rootView.findViewById(R.id.btStartGetPartner);
        btExportGetPartnerRes = rootView.findViewById(R.id.btExportGetPartnerRes);
        lvGetPartnerResult = rootView.findViewById(R.id.lvAnalysisResult);
        tvTargetIMSI = rootView.findViewById(R.id.tvTargetIMSI);
        tvTargetIMSITimes = rootView.findViewById(R.id.tvTargetIMSITimes);
        layoutCollideTarget = rootView.findViewById(R.id.layoutCollideTarget);
        initWidget();
        return rootView;
    }


    private void initWidget() {

        etStartTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(getActivity(), etStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etStartTime);
            }
        });

        etEndTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(getActivity(), etEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etEndTime);
            }
        });

        btStartGettPartner.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String targetImsi = etGetPartnerIMSI.getText().toString();

                if ("".equals(etStartTime.getText().toString()) || "".equals(etEndTime.getText().toString())){
                    ToastUtils.showMessage( "还未选择开始和结束时间！");
                    return;
                }

                if (etStartTime.getText().toString().equals(etEndTime.getText().toString())){
                    ToastUtils.showMessage("开始时间和结束时间一样，请重新设置！");
                    return;
                }
                if (!DateUtils.isStartEndTimeOrderRight(etStartTime.getText().toString(), etEndTime.getText().toString())){
                    ToastUtils.showMessage( "开始时间比结束时间晚，请重新设置！");
                    return;
                }

                if ("".equals(targetImsi)){
                    ToastUtils.showMessage("请输入伴随目标的IMSI!");
                    return;
                }

                if ("".equals(etDeviation.getText().toString())){
                    ToastUtils.showMessage("请输入伴随的时间偏差!");
                    return;
                }

                HashMap<String, Integer> mapImsiWithTimes = GettingPartnerAnalysis.startGetPartnerAnalysis(etStartTime.getText().toString(),
                        etEndTime.getText().toString(), targetImsi, etDeviation.getText().toString());
                if (mapImsiWithTimes == null){
                    listCollideResult.clear();
                    layoutCollideTarget.setVisibility(View.GONE);
                    ToastUtils.showMessage("时间段内没有找到目标IMSI！");
                    return;
                }else if (mapImsiWithTimes.size() == 0){
                    listCollideResult.clear();
                    layoutCollideTarget.setVisibility(View.GONE);
                    ToastUtils.showMessage("无伴随结果！");
                }else{
                    saveTargetImsi = targetImsi;
                    saveTargetTimes = mapImsiWithTimes.get(targetImsi);
                    UpdateResultToList(mapImsiWithTimes, targetImsi);
                    layoutCollideTarget.setVisibility(View.VISIBLE);
                    //mHandler.sendEmptyMessage(UPDATE_RESULT_LIST);
                }

                mHandler.sendEmptyMessage(UPDATE_RESULT_LIST);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.GET_PARTNER);
            }
        });

        btExportGetPartnerRes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(listCollideResult.size() == 0){
                    ToastUtils.showMessage("无伴随分析结果，导出失败！");
                    return;
                }

                exportCollideResult(listCollideResult);
            }
        });

        analysisResultAdapter = new AnalysisResultAdapter(getContext(), R.layout.analysis_result_item, listCollideResult);

        lvGetPartnerResult.setAdapter(analysisResultAdapter);
        lvGetPartnerResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnalysisResultBean analysisResultBean = listCollideResult.get(position);
                List<String> listCollideDetail = GettingPartnerAnalysis.getDetail(analysisResultBean.getImsi());
                showAnalysisDetailDialog(analysisResultBean.getImsi(), listCollideDetail);
            }
        });


        tvTargetIMSI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> listDetail = GettingPartnerAnalysis.getDetail(tvTargetIMSI.getText().toString());
                showAnalysisDetailDialog(tvTargetIMSI.getText().toString(), listDetail);
            }
        });


        tvTargetIMSITimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> listDetail = GettingPartnerAnalysis.getDetail(tvTargetIMSI.getText().toString());
                showAnalysisDetailDialog(tvTargetIMSI.getText().toString(), listDetail);
            }
        });

    }

    private void exportCollideResult(List<AnalysisResultBean> listCollideResult) {
        boolean isSuccess = true;
        final String COLLIDE_RES_FILE_PATH =  FileUtils.ROOT_PATH+"export/";

        String fileName = "PARTNER_"+saveTargetImsi+"_"+ DateUtils.getStrOfDate()+".txt";
        String fullPath = COLLIDE_RES_FILE_PATH+fileName;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath,true)));
            bufferedWriter.write("imsi,次数"+"\r\n");
            bufferedWriter.write(saveTargetImsi+",");
            bufferedWriter.write(String.valueOf(saveTargetTimes));
            bufferedWriter.write("\r\n");
            for (AnalysisResultBean info: listCollideResult) {
                //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                bufferedWriter.write(info.getImsi()+",");
                bufferedWriter.write(info.getTimes());
                bufferedWriter.write("\r\n");
            }
        } catch (FileNotFoundException e){
            //log.error("File Error",e);
            isSuccess = false;
            createExportError("文件未创建成功");
        } catch (IOException e){
            //log.error("File Error",e);
            isSuccess = false;
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
                    .setContentText("文件导出在：手机存储/"+FileUtils.ROOT_DIRECTORY+"/"+ fileName)
                    .show();
        }

    }

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    private void showAnalysisDetailDialog(String imsi, List<String> listAllDetectTime) {
        final String[] stringsAllDetectTime = listAllDetectTime.toArray(new String[listAllDetectTime.size()]);
        MyCommonDialog myCommonDialog = new MyCommonDialog(getActivity());
        myCommonDialog.setTitle("IMSI："+imsi+"\n"+ "出现的时间：");
        myCommonDialog.setItems(stringsAllDetectTime, null);
        myCommonDialog.setNegativeButton("关闭",null);
        myCommonDialog.showDialog();
    }

    private void UpdateResultToList(HashMap<String, Integer> mapImsiWithTimes, String targetImsi) {
        tvTargetIMSI.setText(targetImsi);
        tvTargetIMSITimes.setText(String.valueOf(mapImsiWithTimes.get(targetImsi)));
        mapImsiWithTimes.remove(targetImsi);  //目标IMSI在头部显示后，不再在列表里显示

        //按照次数从大到小排序
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
//            UtilBaseLog.printLog(tmpCollideResult.getKey() + "     " + targetImsi);
//            if (tmpCollideResult.getKey().equals(targetImsi)){
//                tvTargetIMSI.setText(targetImsi);
//                tvTargetIMSITimes.setText(String.valueOf(tmpCollideResult.getValue()));
//                continue;
//            }

            count++;
            listCollideResult.add(new AnalysisResultBean(tmpCollideResult.getKey(), String.valueOf(tmpCollideResult.getValue())));
            if (count >= MAX_COLLIDE_RESULT_TO_SHOW)
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



