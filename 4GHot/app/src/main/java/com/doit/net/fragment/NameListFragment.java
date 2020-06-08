package com.doit.net.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.adapter.BlacklistAdapter;
import com.doit.net.View.NameListEditDialog;
import com.doit.net.base.BaseFragment;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.StringUtils;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.Event;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class NameListFragment extends BaseFragment implements IHandlerFinish {

    private ListView mListView;
    private BlacklistAdapter blacklistAdapter;
    private EditText etSearchKeyword;
    private Button btSearch;
    private Button btnAddBlacklist;
    private Button btImportNamelist;
    private Button btExportNamelist;
    private Button btClearNamelist;
    private DbManager dbManager;

    private final String BLACKLIST_FILE_PATH =  Environment.getExternalStorageDirectory()+"/4GHotspot/Blacklist.txt";
    private final String BLACKLIST_FILE_HOME_PATH =  "4GHotspot/Blacklist.txt";
    private List<DBBlackInfo> dbBlackInfos = new ArrayList<>();
    private int lastOpenSwipePos = 0;

    //handler消息
    private final int UPDATE_NAMELIST = 1;
    private final int EXPORT_ERROR = -1;

    public NameListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.doit_layout_name_list, container, false);

        dbManager = UCSIDBManager.getDbManager();
        mListView = rootView.findViewById(R.id.listview);
        etSearchKeyword = rootView.findViewById(R.id.editText_keyword);
        btSearch = rootView.findViewById(R.id.button_search);
        btSearch.setOnClickListener(searchClick);
        btnAddBlacklist = rootView.findViewById(R.id.button_add);
        btnAddBlacklist.setOnClickListener(addClick);
        btImportNamelist = rootView.findViewById(R.id.btImportNamelist);
        btImportNamelist.setOnClickListener(importNamelistClick);
        btExportNamelist = rootView.findViewById(R.id.btExportNamelist);
        btExportNamelist.setOnClickListener(exportNamelistClick);
        btClearNamelist = rootView.findViewById(R.id.btClearNamelist);
        btClearNamelist.setOnClickListener(clearNamelistClick);

        blacklistAdapter = new BlacklistAdapter(getActivity());
        blacklistAdapter.setMode(Attributes.Mode.Single);
        mListView.setAdapter(blacklistAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastOpenSwipePos = position - mListView.getFirstVisiblePosition();
                openSwipe(lastOpenSwipePos);
            }
        });

        UIEventManager.register(UIEventManager.KEY_REFRESH_NAMELIST_LIST,this);
        return rootView;
    }

    @Override
    public void onFocus() {
        refreshNamelist();
    }



    private void refreshNamelist() {
        try {
            dbBlackInfos = dbManager.selector(DBBlackInfo.class).findAll();
            if (dbBlackInfos == null) {
                dbBlackInfos = new ArrayList<>();
            }

            blacklistAdapter.setUeidList(dbBlackInfos);

        } catch (DbException e) {
            e.printStackTrace();
        }

        mHandler.sendEmptyMessage(UPDATE_NAMELIST);
    }

    private void openSwipe(int position){
        ((SwipeLayout) (mListView.getChildAt(position))).open(true);
        ((SwipeLayout) (mListView.getChildAt(position))).setClickToClose(true);
    }

    private void closeSwipe(int position){
        SwipeLayout swipe = ((SwipeLayout) (mListView.getChildAt(position)));
        if (swipe != null)
            swipe.close(true);
    }


    View.OnClickListener searchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String keyword = etSearchKeyword.getText().toString();
                try {
                dbBlackInfos = dbManager.selector(DBBlackInfo.class)
                        .where("imsi", "like", "%" + keyword + "%")
                        .or("name", "like", "%" + keyword + "%")
                        .or("remark", "like", "%" + keyword + "%")
                        .orderBy("id", true)
                        .findAll();

                if (dbBlackInfos == null || dbBlackInfos.size() <= 0) {
                    ToastUtils.showMessage(getActivity(), R.string.search_not_found);
                    return;
                }
                blacklistAdapter.setUeidList(dbBlackInfos);
            } catch (DbException e) {
                e.printStackTrace();
            }

            mHandler.sendEmptyMessage(UPDATE_NAMELIST);
        }
    };

    View.OnClickListener addClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NameListEditDialog dialog = new NameListEditDialog(getActivity());
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //btSearch.performClick();如果此时输入框里有东西，应该是刷不出来的0.o
                    refreshNamelist();
                }
            });
        }
    };


    View.OnClickListener exportNamelistClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
                File namelistFile = new File(BLACKLIST_FILE_PATH);
                if (namelistFile.exists()){
                    namelistFile.delete();
                }

                BufferedWriter bufferedWriter = null;
                try {
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(BLACKLIST_FILE_PATH,true)));
                    bufferedWriter.write("IMSI,姓名,备注,创建时间"+"\r\n");
                    if (dbBlackInfos == null || dbBlackInfos.size() == 0){
                        ToastUtils.showMessageLong(getContext(), "当前名单为空，此次导出为模板");
                    }else{
                        for (DBBlackInfo info: dbBlackInfos) {
                            //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                            bufferedWriter.write(info.getImsi()+",");
                            bufferedWriter.write(info.getName()+",");
                            bufferedWriter.write(StringUtils.defaultString(info.getRemark())+",");
                            bufferedWriter.write(DateUtils.convert2String(info.getCreateDate(), DateUtils.LOCAL_DATE));
                            bufferedWriter.write("\r\n");
                        }
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
                        } catch (IOException e) {}
                    }
                }

                EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, BLACKLIST_FILE_PATH);
                new MySweetAlertDialog(getContext(), MySweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("导出成功")
                        .setContentText("文件导出在：手机存储"+ BLACKLIST_FILE_HOME_PATH)
                        .show();

                EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.EXPORT_NAMELIST+ BLACKLIST_FILE_PATH);
            }

    };

    View.OnClickListener importNamelistClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            importBlacklist();
        }
    };

    View.OnClickListener clearNamelistClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (dbBlackInfos.size() == 0){
                ToastUtils.showMessage(getContext(), "当前名单为空");
                return;
            }

            new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                    .setTitleText("提示")
                    .setContentText("手机及设备上的名单都会被清空,确定吗?")
                    .setCancelText(getContext().getString(R.string.cancel))
                    .setConfirmText(getContext().getString(R.string.sure))
                    .showCancelButton(true)
                    .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(MySweetAlertDialog sweetAlertDialog) {
                            CacheManager.clearCurrentBlackList();
                            try {
                                dbManager.delete(DBBlackInfo.class);
                            } catch (DbException e) {e.printStackTrace();}

                            refreshNamelist();
                            sweetAlertDialog.dismiss();
                        }
                    }).show();
        }
    };

    private void importBlacklist() {
        File namelistFile = new File(BLACKLIST_FILE_PATH);
        if (!namelistFile.exists()) {
            new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                    .setTitleText("导入失败")
                    .setContentText("文件不存在，但已生成模板:"+"手机存储"+ BLACKLIST_FILE_HOME_PATH + "，请按模板格式填入名单后导入！")
                    .show();

            BufferedWriter bufferedWriter = null;
            try {
                namelistFile.createNewFile();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(BLACKLIST_FILE_PATH,true)));
                bufferedWriter.write("IMSI,姓名,备注"+"\r\n");
                if (dbBlackInfos != null && dbBlackInfos.size() > 0) {
                    for (DBBlackInfo info: dbBlackInfos) {
                        //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                        bufferedWriter.write(info.getName()+",");
                        bufferedWriter.write(info.getImsi()+",");
                        bufferedWriter.write(StringUtils.defaultString(info.getRemark())+",");
                        bufferedWriter.write(DateUtils.convert2String(info.getCreateDate(), DateUtils.LOCAL_DATE));
                        bufferedWriter.write("\r\n");
                    }
                }

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
                    } catch (IOException e) {}
                }
            }
            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, BLACKLIST_FILE_PATH);
        }else{
            int validImportNum = 0;
            int repeatNum = 0;
            int errorFormatNum = 0;
            String name = "";
            String remark = "";
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(namelistFile));
                List<DBBlackInfo> listValidBlack = new ArrayList<>();
                String readline = "";
                while ((readline = bufferedReader.readLine()) != null) {
                    if (readline.contains("IMSI") && readline.contains("姓名"))
                        continue;

                    if (!isBlacklistFormatRight(readline)){
                        errorFormatNum++;
                        continue;
                    }

                    if (isBlacklistExist(readline.split(",")[0], listValidBlack)){
                        repeatNum ++;
                        continue;
                    }


                    name = "";
                    remark = "";
                    //如果含有创建时间（即有3个，）的话，那split(",")的返回值固定为4
                    if (readline.length() - readline.replace(",", "").length() == 3){
                        name = readline.split(",")[1];
                        remark = readline.split(",")[2];
                    }else if (readline.length() - readline.replace(",", "").length() == 2){
                        if (!readline.endsWith(",")){
                            remark = readline.split(",")[2];
                        }
                        name = readline.substring(readline.indexOf(",")+1, readline.lastIndexOf(","));
                    }else {
                        errorFormatNum++;
                        continue;
                    }

                    validImportNum++;
                    listValidBlack.add(new DBBlackInfo(readline.split(",")[0],
                            name, remark, new Date()));
                    if (validImportNum > 100)
                        break;

                }

                dbManager.save(listValidBlack);
            } catch (FileNotFoundException e){
                //log.error("File Error",e);
                createExportError("文件未创建成功");
            } catch (IOException e){
                //log.error("File Error",e);
                createExportError("写入文件错误");
            } finally {
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {}
                }
            }

            new MySweetAlertDialog(getContext(), MySweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("导入完成")
                    .setContentText("成功导入"+ validImportNum +"个名单，忽略"+
                            repeatNum +"个重复的名单，忽略"+ errorFormatNum +"行格式或号码错误")
                    .show();
            refreshNamelist();
            if (CacheManager.isDeviceOk() && !CacheManager.getLocState()){
                //当导入量相当大时，数据下发是相当慢的，所以放在子线程里发
                new Thread() {@Override public void run() {CacheManager.setCurrentBlackList();}}.start();
            }

            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.IMPORT_NAMELIST+ BLACKLIST_FILE_PATH);
        }
    }


    public static boolean isNumeric(String str){
        Pattern pattern=Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private boolean isBlacklistFormatRight(String readline) {

        //导出会将创建时间导出，但是导入不需要创建时间字段
        return ((readline.length() - readline.replace(",", "").length()) >= 2) &&
                (!readline.startsWith(",")) &&
                (readline.split(",")[0].length() == 15) &&   //判断长度和是否含有非数字
                isNumeric(readline.split(",")[0]);
    }


    private boolean isWhitelistFormatRight(String readline) {
        //先判断行逗号的个数
        if (readline.length() - readline.replace(",", "").length() != 2)
            return false;

        //如果是以，，开头，说明没有IMSI或手机号，也不行
        if (readline.startsWith(",,"))
            return false;

        //判断长度和是否含有非数字
//        if (!"".equals(readline.split(",")[0]) &&
//                (readline.split(",")[0].length() != 15 ||
//                        !isNumeric(readline.split(",")[0]))){
//            return false;
//        }
        String imsi = readline.substring(0, readline.indexOf(","));
        if (!"".equals(imsi) && (imsi.length() != 15 || !isNumeric(imsi))){
            return false;
        }

        String msisdn = readline.substring(readline.indexOf(",")+1, readline.lastIndexOf(","));
        return "".equals(msisdn) || (msisdn.length() == 11 && isNumeric(msisdn));
    }

    private boolean isBlacklistExist(String imsi, List<DBBlackInfo> listFromFile) {
        if (dbBlackInfos != null){
            for (DBBlackInfo tmpdbBalckInfo : dbBlackInfos){
                if(tmpdbBalckInfo.getImsi().equals(imsi))
                    return true;
            }
        }

        if (listFromFile != null){
            for (int i = 0; i < listFromFile.size(); i++) {
                if (listFromFile.get(i).getImsi().equals(imsi))
                    return true;
            }
        }

        return false;
    }

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_NAMELIST){

                if(blacklistAdapter != null) {
                    blacklistAdapter.refreshData();
                }

                closeSwipe(lastOpenSwipePos);
            }else if(msg.what == EXPORT_ERROR){
                new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }
        }
    };

    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.KEY_REFRESH_NAMELIST_LIST)){
            refreshNamelist();
        }
    }
}
