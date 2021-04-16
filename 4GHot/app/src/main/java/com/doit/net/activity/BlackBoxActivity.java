package com.doit.net.activity;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.doit.net.utils.FTPManager;
import com.doit.net.utils.FileUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.base.BaseActivity;
import com.doit.net.utils.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.bean.BlackBoxBean;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.UCSIDBManager;
import com.doit.net.utils.DateUtils;
import com.doit.net.view.MySweetAlertDialog;
import com.doit.net.utils.ToastUtils;
import com.doit.net.adapter.BlackBoxListAdapter;
import com.doit.net.view.MyTimePickDialog;
import com.doit.net.ucsi.R;

import org.apache.commons.net.ftp.FTPFile;
import org.xutils.DbManager;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BlackBoxActivity extends BaseActivity {

    private ListView lvBlackBox;
    private BlackBoxListAdapter blackBoxListAdapter;
    private EditText etKeyword;
    private Button btSearch;
    private Button btExportSearchResult;
    private EditText etStartTime;
    private EditText etEndTime;
    private DbManager dbManager;

    private List<BlackBoxBean> listBlackBox = new ArrayList<>();

    //handler消息
    private final int UPDATE_BLACKBOX_LIST = 0;
    private final int EXPORT_ERROR = -1;
    private final int HIDE_LOADING = 2;

    private MySweetAlertDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_box);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbManager = UCSIDBManager.getDbManager();
        lvBlackBox = findViewById(R.id.lvBlackBox);
        etKeyword = findViewById(R.id.etKeyword);
        btSearch = findViewById(R.id.btSearch);
        btSearch.setOnClickListener(searchClick);

        etStartTime = findViewById(R.id.etStartTime);
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(BlackBoxActivity.this, etStartTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etStartTime);
            }
        });

        etEndTime = findViewById(R.id.etEndTime);
        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickDialog myTimePicKDialog = new MyTimePickDialog(BlackBoxActivity.this, etEndTime.getText().toString());
                myTimePicKDialog.dateTimePicKDialog(etEndTime);
            }
        });

        btExportSearchResult = findViewById(R.id.btExportSearchResult);
        btExportSearchResult.setOnClickListener(exportClick);

        blackBoxListAdapter = new BlackBoxListAdapter(this, R.layout.layout_black_box_item, listBlackBox);
        lvBlackBox.setAdapter(blackBoxListAdapter);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 1000 * 3600 * 24 * 7;

        etStartTime.setText(DateUtils.convert2String(startTime,DateUtils.LOCAL_DATE));
        etEndTime.setText(DateUtils.convert2String(endTime,DateUtils.LOCAL_DATE));

        mProgressDialog = new MySweetAlertDialog(this, MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("下载中，请耐心等待...");
        mProgressDialog.setCancelable(false);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_BLACKBOX_LIST) {
                if (blackBoxListAdapter != null) {
                    blackBoxListAdapter.notifyDataSetChanged();
                }
            } else if (msg.what == EXPORT_ERROR) {
                new SweetAlertDialog(BlackBoxActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因：" + msg.obj)
                        .show();
            } else if (msg.what == HIDE_LOADING) {
                if (mProgressDialog !=null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
            }
        }
    };


    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CacheManager.isDeviceOk()){
                return;
            }

            String keyword = etKeyword.getText().toString();
            final String startTime = etStartTime.getText().toString().trim();
            final String endTime = etEndTime.getText().toString().trim();

            clearBlackboxList();

            if (("".equals(startTime) && !"".equals(endTime)) || ((!"".equals(startTime) && "".equals(endTime)))) {
                ToastUtils.showMessage("未设置开始时间及结束时间！");
                return;
            } else if (!"".equals(startTime) && startTime.equals(endTime)) {
                ToastUtils.showMessage("开始时间和结束时间一样，请重新设置！");
                return;
            } else if (!"".equals(startTime) && !isStartEndTimeOrderRight(startTime, endTime)) {
                ToastUtils.showMessage("开始时间比结束时间晚，请重新设置！");
                return;
            }

            initQueryBlx(keyword,startTime, endTime);


        }
    };

    private void initQueryBlx(String keyword,String startTime, String endTime) {

        try {
            dbManager.delete(BlackBoxBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (mProgressDialog !=null && !mProgressDialog.isShowing()){
            mProgressDialog.show();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FTPManager.getInstance().connect();
                    boolean uploadSuccess = FTPManager.getInstance().uploadFile(true,BlackBoxManger.LOCAL_FTP_BLX_PATH,
                            BlackBoxManger.currentBlxFileName);
                    if (uploadSuccess) {
                        getBlxFromDevice(keyword,startTime, endTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showMessage("连接设备失败，请保持网络连接");
                    mHandler.sendEmptyMessage(HIDE_LOADING);
                }
            }
        }).start();

    }

    public  void getBlxFromDevice(String keyword,String startTime, String endTime) {

        FTPFile[] files = FTPManager.getInstance().listFiles();
        if (files == null) {
            ToastUtils.showMessage("暂无黑匣子文件");
        } else {
            String tmpFileName = "";
            LogUtils.log("服务器黑匣子文件数量："+files.length);
            if ("".equals(startTime) && "".equals(endTime)){
                for (int i = 0; i < files.length; i++) {
                    tmpFileName = files[i].getName();
                    LogUtils.log("文件名："+tmpFileName);
                    if (tmpFileName.endsWith(".blx")) {
                        try {
                            boolean downloadSuccess = FTPManager.getInstance().downloadFile(BlackBoxManger.LOCAL_FTP_BLX_PATH, tmpFileName);
                            if (downloadSuccess){
                                parseBlxFile(tmpFileName);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                for (int i = 0; i < files.length; i++) {
                    tmpFileName = files[i].getName();
                    LogUtils.log(tmpFileName);
                    if (tmpFileName.endsWith(".blx")) {
                        if (DateUtils.convert2long(getDateByFileName(tmpFileName), DateUtils.LOCAL_DATE_DAY) >= DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE_DAY) &&
                                DateUtils.convert2long(getDateByFileName(tmpFileName), DateUtils.LOCAL_DATE_DAY) <= DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)) {
                            try {
                                LogUtils.log("满足条件，下载该文件：" + tmpFileName);

                                boolean downloadSuccess = FTPManager.getInstance().downloadFile(BlackBoxManger.LOCAL_FTP_BLX_PATH, tmpFileName);
                                if (downloadSuccess){
                                    parseBlxFile(tmpFileName);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            List<BlackBoxBean> searchBlackBox = new ArrayList<>();

            if (TextUtils.isEmpty(startTime)) {
                try {
                    searchBlackBox = dbManager.selector(BlackBoxBean.class).findAll();

                    searchBlackBox = dbManager.selector(BlackBoxBean.class)
                            .where("account", "like", "%" + keyword + "%")
                            .or("operation", "like", "%" + keyword + "%")
                            .orderBy("time", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    searchBlackBox = dbManager.selector(BlackBoxBean.class)
                            .where("time", "BETWEEN", new long[]{DateUtils.convert2long(startTime, DateUtils.LOCAL_DATE), DateUtils.convert2long(endTime, DateUtils.LOCAL_DATE)})
                            .and(WhereBuilder.b("operation", "like", "%" + keyword + "%").or("account", "like", "%" + keyword + "%"))
                            .orderBy("time", true)
                            .findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            if (searchBlackBox == null || searchBlackBox.size() <= 0) {
                ToastUtils.showMessage(R.string.search_not_found);
                mHandler.sendEmptyMessage(HIDE_LOADING);
                return;
            }

            updateBlackboxList(searchBlackBox);
        }

        mHandler.sendEmptyMessage(HIDE_LOADING);
    }

    private static String getDateByFileName(String fileName){
        String[] date = fileName.split("\\.");
        return date[0];
    }

    private  void parseBlxFile(String tmpFileName) {
        File file = new File(BlackBoxManger.LOCAL_FTP_BLX_PATH +tmpFileName);
        if (!file.exists())
            return;

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String readline = "";
            while ((readline = bufferedReader.readLine()) != null) {

                BlackBoxManger.saveOperationToDB(readline);
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {}
            }
        }
    }



    private void clearBlackboxList() {
        listBlackBox.clear();
        mHandler.sendEmptyMessage(UPDATE_BLACKBOX_LIST);
    }

    private void updateBlackboxList(List<BlackBoxBean> searchBlackBox) {
        listBlackBox.clear();
        listBlackBox.addAll(searchBlackBox);

        mHandler.sendEmptyMessage(UPDATE_BLACKBOX_LIST);
    }

    View.OnClickListener exportClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listBlackBox == null || listBlackBox.size() <= 0) {
                ToastUtils.showMessage(R.string.can_not_export_search);
                return;
            }

            String fileName = "BLACKBOX_" + DateUtils.getStrOfDate() + ".csv";
            String fullPath = FileUtils.ROOT_PATH + fileName;
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullPath, true)));
                bufferedWriter.write("用户,操作,时间" + "\r\n");
                for (BlackBoxBean info : listBlackBox) {
                    bufferedWriter.write(info.getAccount() + ",");
                    bufferedWriter.write(info.getOperation() + ",");
                    bufferedWriter.write(DateUtils.convert2String(info.getTime(), DateUtils.LOCAL_DATE));
                    bufferedWriter.write("\r\n");
                }
            } catch (DbException e) {
                //log.error("Export SELECT ERROR",e);
                createExportError("数据查询错误");
            } catch (FileNotFoundException e) {
                //log.error("File Error",e);
                createExportError("文件未创建成功");
            } catch (IOException e) {
                //log.error("File Error",e);
                createExportError("写入文件错误");
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                    }
                }
            }


            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, fullPath);
            new SweetAlertDialog(BlackBoxActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("导出成功")
                    .setContentText("文件导出在：手机存储/" + FileUtils.ROOT_DIRECTORY + "/" + fileName)
                    .show();

            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.EXPORT_BLACKBOX + fullPath);
        }
    };

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


    public boolean isStartEndTimeOrderRight(String startTime, String endTime) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dataStartTime = null;
        try {
            dataStartTime = simpleDateFormat.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date dateEndTime = null;
        try {
            dateEndTime = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dataStartTime.before(dateEndTime);
    }

    private void createExportError(String obj) {
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }
}
