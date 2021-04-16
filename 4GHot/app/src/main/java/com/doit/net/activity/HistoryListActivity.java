package com.doit.net.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.utils.FileUtils;
import com.doit.net.adapter.HistoryListViewAdapter;
import com.doit.net.view.MyTimePickDialog;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.bean.DBUeidInfo;
import com.doit.net.utils.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.view.MySweetAlertDialog;
import com.doit.net.utils.StringUtils;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryListActivity extends BaseActivity implements EventAdapter.EventCall {

    private ListView mListView;
    private HistoryListViewAdapter mAdapter;
    private EditText editText_keyword;
    private Button button_search;
    private Button btExportSearchResult;
    private EditText etStartTime;
    private EditText etEndTime;
    private DbManager dbManager;

    private List<DBUeidInfo> dbUeidInfos = new ArrayList<>();


    //handler消息
    private final int EXPORT_ERROR = -1;
    private final int UPDATE_SEARCH_RESULT = 0;
    private final int SEARCH_HISTORT = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.doit_layout_history_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbManager = UCSIDBManager.getDbManager();
        mListView = findViewById(R.id.lvUeidSearchRes);
        editText_keyword = findViewById(R.id.editText_keyword);
        button_search = findViewById(R.id.button_search);
        button_search.setOnClickListener(searchClick);

        etStartTime = findViewById(R.id.etStartTime);
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(HistoryListActivity.this, etStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etStartTime);
            }
        });
        etEndTime = findViewById(R.id.etEndTime);
        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(HistoryListActivity.this, etEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etEndTime);
            }
        });
        btExportSearchResult = findViewById(R.id.btExportSearchResult);
        btExportSearchResult.setOnClickListener(exportClick);

        mAdapter = new HistoryListViewAdapter(this);
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).setClickToClose(true);
            }
        });

//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                selectedUeidItem = CacheManager.realtimeUeidList.get(position);
//                showListPopWindow(mListView, calcPopWindowPosX((int)motionEvent.getX()), calcPopWindowPosY((int)motionEvent.getY()));
//                return true;
//            }
//        });
//        ueidItemPopView = LayoutInflater.from(activity).inflate(R.layout.realtime_ueid_pop_window, null);
//        ueidItemPop = new PopupWindow(ueidItemPopView, getResources().getDisplayMetrics().widthPixels/3,
//                LinearLayout.LayoutParams.WRAP_CONTENT, true);   //宽度和屏幕成比例
//        ueidItemPop.setContentView(ueidItemPopView);
//        ueidItemPop.setBackgroundDrawable(new ColorDrawable());  //据说不设在有些情况下会关不掉
//        mAdapter.setOnItemLongClickListener(new HistoryListViewAdapter.onItemLongClickListener() {
//            @Override
//            public void onItemLongClick(MotionEvent motionEvent, int position) {
//                selectedUeidItem = mAdapter.getUeidList().get(position);
//                showListPopWindow(mListView, calcPopWindowPosX((int)motionEvent.getX()), calcPopWindowPosY((int)motionEvent.getY()));
//            }
//        });

//        tvGetTelNumber = ueidItemPopView.findViewById(R.id.tvGetTelNumber);
//        tvGetTelNumber.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                LogUtils.log("点击了："+selectedUeidItem.getImsi());
//                if (!ImsiMsisdnConvert.isAuthenticated()){
//                    ToastUtils.showMessageLong( "尚未通过认证，请先进入“号码翻译设置”进行认证");
//                    ueidItemPop.dismiss();
//                    return;
//                }
//
//                new Thread() {
//                    @Override
//                    public void run() {
//                        ImsiMsisdnConvert.requestConvertImsiToMsisdn(activity,selectedUeidItem.getImsi());
//                        //ImsiMsisdnConvert.queryImsiConvertMsisdnRes(activity, selectedUeidItem.getImsi());
//                    }
//                }.start();
//
//                ueidItemPop.dismiss();
//            }
//        });

        EventAdapter.register(EventAdapter.RESEARCH_HISTORY_LIST,this);
    }


//    private void showListPopWindow(View anchorView, int posX, int posY) {
//        ueidItemPop.showAtLocation(anchorView, Gravity.TOP | Gravity.START, posX, posY);
//    }
//
//    private int calcPopWindowPosY(int eventY) {
//        int listviewHeight = mListView.getResources().getDisplayMetrics().heightPixels;
//        ueidItemPop.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        int popWinHeight = ueidItemPop.getContentView().getMeasuredHeight();
//
//        boolean isNeedShowUpward = (eventY + popWinHeight > listviewHeight);  //超过范围就向上显示
//        if (isNeedShowUpward){
//            return eventY-popWinHeight;
//        } else {
//            return eventY;
//        }
//    }
//
//    private int calcPopWindowPosX(int eventX) {
//        int listviewWidth = mListView.getResources().getDisplayMetrics().widthPixels;
//        ueidItemPop.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        int windowWidth = ueidItemPop.getContentView().getMeasuredWidth();
//
//        boolean isShowLeft = (eventX+windowWidth > listviewWidth);  //超过屏幕的话就向左边显示
//        if (isShowLeft) {
//            return eventX-windowWidth;
//        } else {
//            return eventX;
//        }
//    }

    public void getTitleBar() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.doit_layout_history_title);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.doit_layout_history_title);
        TextView textView = findViewById(R.id.head_center_text);
        textView.setText("采集历史记录");
        Button titleBackBtn = findViewById(R.id.btn_back);
        titleBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_BACK);
                onKeyDown(KeyEvent.KEYCODE_BACK, newEvent);
            }
        });
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SEARCH_RESULT) {
                if (mAdapter != null) {
                    mAdapter.refreshData();
                }
            }else if(msg.what == EXPORT_ERROR){
                new MySweetAlertDialog(HistoryListActivity.this, MySweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }else if(msg.what == SEARCH_HISTORT){
                button_search.performClick();
            }
        }
    };


    @Override
    protected void onResume() {
        clearSearchHistory();
        super.onResume();
    }

    private void clearSearchHistory() {
        if (dbUeidInfos == null)
            dbUeidInfos = new ArrayList<>();

        dbUeidInfos.clear();
        mAdapter.setUeidList(dbUeidInfos);
        mHandler.sendEmptyMessage(UPDATE_SEARCH_RESULT);
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

    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String keyword = editText_keyword.getText().toString();
            String startTime = etStartTime.getText().toString();
            String endTime = etEndTime.getText().toString();

            if (("".equals(startTime) && !"".equals(endTime)) || ((!"".equals(startTime) && "".equals(endTime)))){
                ToastUtils.showMessage("未设置开始时间及结束时间！");
                return;
            } else if (!"".equals(startTime) && startTime.equals(endTime)){
                ToastUtils.showMessage("开始时间和结束时间一样，请重新设置！");
                return;
            } else if (!"".equals(startTime) && !"".equals(endTime) && !isStartEndTimeOrderRight(startTime, endTime)){
                ToastUtils.showMessage( "开始时间比结束时间晚，请重新设置！");
                return;
            }

            if ("".equals(startTime)){
                try {
                    dbUeidInfos = dbManager.selector(DBUeidInfo.class)
                            .where("imsi", "like", "%" + keyword + "%")
                            .orderBy("id", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    dbUeidInfos = dbManager.selector(DBUeidInfo.class)
                            .where("imsi", "like", "%" + keyword + "%")
//                        .or("imei", "like", "%" + keyword + "%")
                            .and("createDate", "BETWEEN",
                                    new long[]{DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE), DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)})
                            .orderBy("id", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            if (dbUeidInfos == null || dbUeidInfos.size() <= 0) {
                clearSearchHistory();
                ToastUtils.showMessage(R.string.search_not_found);
                return;
            }

            mAdapter.setUeidList(dbUeidInfos);
            mHandler.sendEmptyMessage(UPDATE_SEARCH_RESULT);
        }
    };

    View.OnClickListener exportClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dbUeidInfos == null || dbUeidInfos.size() <= 0) {
                ToastUtils.showMessage( R.string.can_not_export_search);
                return;
            }

            String fileName = "UEID_"+ DateUtils.getStrOfDate()+".csv";
            String fullPath = FileUtils.ROOT_PATH+fileName;
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath,true)));
                bufferedWriter.write("imsi,tmsi,时间,经度,纬度"+"\r\n");
                for (DBUeidInfo info: dbUeidInfos) {
                    //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                    bufferedWriter.write(info.getImsi()+",");
                    bufferedWriter.write(info.getTmsi()+",");
                    bufferedWriter.write(DateUtils.convert2String(info.getCreateDate(), DateUtils.LOCAL_DATE)+",");
                    bufferedWriter.write(StringUtils.defaultString(info.getLongitude())+",");
                    bufferedWriter.write(StringUtils.defaultString(info.getLatitude()));
                    bufferedWriter.write("\r\n");
                }
            } catch (DbException e) {
                //log.error("Export SELECT ERROR",e);
                createExportError("数据查询错误");
            } catch (FileNotFoundException e){
                //log.error("File Error",e);
                createExportError("文件未创建成功");
            } catch (IOException e){
                //log.error("File Error",e);
                createExportError("写入文件错误");
            } finally {
                if(bufferedWriter != null){
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                    }
                }
            }

            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, fullPath);
            new MySweetAlertDialog(HistoryListActivity.this, MySweetAlertDialog.TEXT_SUCCESS)
                    .setTitleText("导出成功")
                    .setContentText("文件导出在：手机存储/"+FileUtils.ROOT_DIRECTORY+"/"+ fileName)
                    .show();

            EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.EXPORT_HISTORT_DATA+ fullPath);
        }
    };

    public boolean isStartEndTimeOrderRight(String startTime, String endTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dataStartTime = null;
        try {
            dataStartTime = simpleDateFormat.parse(startTime);
        } catch (ParseException e) {e.printStackTrace();}

        Date dateEndTime = null;
        try {
            dateEndTime = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {e.printStackTrace();}

        return dataStartTime.before(dateEndTime);
    }

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }


    @Override
    public void call(String key, Object val) {
        switch (key){
            case EventAdapter.RESEARCH_HISTORY_LIST:
                mHandler.sendEmptyMessage(SEARCH_HISTORT);
                break;
        }

    }

}
