package com.doit.net.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.View.AddWhitelistDialog;
import com.doit.net.adapter.HistoryListViewAdapter;
import com.doit.net.adapter.WhitelistAdapter;
import com.doit.net.base.BaseActivity;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.ImsiMsisdnConvert;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Model.WhiteListInfo;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.StringUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LogUtils;
import com.doit.net.ucsi.R;

//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellValue;
//import org.apache.poi.ss.usermodel.FormulaEvaluator;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class WhitelistManagerActivity extends BaseActivity implements IHandlerFinish, EventAdapter.EventCall {
    private final Activity activity = this;
    private ListView lvWhitelistInfo;
    private WhitelistAdapter mAdapter;
    private Button btAddWhitelist;
    private Button btExportWhitelist;
    private Button btImportWhitelist;
    private Button btClearWhitelist;

    DbManager dbManager = UCSIDBManager.getDbManager();

    private PopupWindow whitelistItemPop;
    View whitelistItemPopView;
    private TextView tvGetImsi;
    private WhiteListInfo selectedWhitelistItem = null;
    private List<WhiteListInfo> listWhitelistInfo = new ArrayList<>();

    private int lastOpenSwipePos = -1;

    private final String WHITELIST_FILE_PATH =  Environment.getExternalStorageDirectory()+"/4GHotspot/Whitelist.csv";

    //handler消息
    private final int REFRESH_LIST = 0;
    private final int UPDATE_LIST = 1;
    private final int UPDATE_WHITELIST = 2;
    private final int EXPORT_ERROR = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_whitelist);
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        updateListFromDB();
        UIEventManager.register(UIEventManager.KEY_REFRESH_WHITE_LIST,this);
        EventAdapter.setEvent(EventAdapter.UPDATE_WHITELIST,this);
    }

    private void initView() {
        lvWhitelistInfo = (ListView) findViewById(R.id.lvWhitelistInfo);
        btAddWhitelist = (Button) findViewById(R.id.btAddWhitelist);
        btAddWhitelist.setOnClickListener(addWhitelistClick);

        btImportWhitelist = (Button) findViewById(R.id.btImportWhitelist);
        btImportWhitelist.setOnClickListener(importWhitelistClick);

        btExportWhitelist = (Button) findViewById(R.id.btExportWhitelist);
        btExportWhitelist.setOnClickListener(exortWhitelistClick);

        btClearWhitelist = (Button) findViewById(R.id.btClearWhitelist);
        btClearWhitelist.setOnClickListener(clearWhitelistClick);

        mAdapter = new WhitelistAdapter(activity);
        lvWhitelistInfo.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        lvWhitelistInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastOpenSwipePos = position - lvWhitelistInfo.getFirstVisiblePosition();
                openSwipe(lastOpenSwipePos);
            }
        });

        whitelistItemPopView = LayoutInflater.from(activity).inflate(R.layout.whitelist_pop_windows, null);
        whitelistItemPop = new PopupWindow(whitelistItemPopView, getResources().getDisplayMetrics().widthPixels/3,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);   //宽度和屏幕成比例
        whitelistItemPop.setContentView(whitelistItemPopView);
        whitelistItemPop.setBackgroundDrawable(new ColorDrawable());  //据说不设在有些情况下会关不掉
        mAdapter.setOnItemLongClickListener(new HistoryListViewAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongClick(MotionEvent motionEvent, int position) {
                selectedWhitelistItem = mAdapter.getWhitelistList().get(position);
                showListPopWindow(lvWhitelistInfo, calcPopWindowPosX((int)motionEvent.getX()), calcPopWindowPosY((int)motionEvent.getY()));
            }
        });

        tvGetImsi = (TextView) whitelistItemPopView.findViewById(R.id.tvGetImsi);
        tvGetImsi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String msisdn = selectedWhitelistItem.getMsisdn();
                String imsi = selectedWhitelistItem.getImsi();
                if (!"".equals(imsi)){
                    ToastUtils.showMessage(activity,"已知IMSI，无需翻译！");
                    return;
                }

                if("".equals(msisdn)){
                    ToastUtils.showMessage(activity,"手机号为空，请确认后点击！");
                    return;
                }

                LogUtils.log("点击了："+ msisdn);
                if (!ImsiMsisdnConvert.isAuthenticated()){
                    ToastUtils.showMessageLong(activity, "尚未通过认证，请先进入“号码翻译设置”进行认证");
                    whitelistItemPop.dismiss();
                    return;
                }

                new Thread() {
                    @Override
                    public void run() {
                        ImsiMsisdnConvert.requestConvertMsisdnToImsi(activity, selectedWhitelistItem.getMsisdn());
                        //ImsiMsisdnConvert.queryMsisdnConvertImsiRes(activity, selectedWhitelistItem.getMsisdn());
                    }
                }.start();

                whitelistItemPop.dismiss();
            }
        });
    }

    private void showListPopWindow(View anchorView, int posX, int posY) {
        whitelistItemPop.showAtLocation(anchorView, Gravity.TOP | Gravity.START, posX, posY);
    }

    private int calcPopWindowPosY(int eventY) {
        int listviewHeight = lvWhitelistInfo.getResources().getDisplayMetrics().heightPixels;
        whitelistItemPop.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWinHeight = whitelistItemPop.getContentView().getMeasuredHeight();

        boolean isNeedShowUpward = (eventY + popWinHeight > listviewHeight);  //超过范围就向上显示
        if (isNeedShowUpward){
            return eventY-popWinHeight;
        } else {
            return eventY;
        }
    }

    private int calcPopWindowPosX(int eventX) {
        int listviewWidth = lvWhitelistInfo.getResources().getDisplayMetrics().widthPixels;
        whitelistItemPop.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int windowWidth = whitelistItemPop.getContentView().getMeasuredWidth();

        boolean isShowLeft = (eventX+windowWidth > listviewWidth);  //超过屏幕的话就向左边显示
        if (isShowLeft) {
            return eventX-windowWidth;
        } else {
            return eventX;
        }
    }

    View.OnClickListener addWhitelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddWhitelistDialog addWhitelistDialog = new AddWhitelistDialog(activity);
            addWhitelistDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateListFromDB();
                }
            });
            addWhitelistDialog.show();
        }
    };

    private boolean isWhitelistExist(String imsi, String msisdn, List<WhiteListInfo> listWhitelistFromFile) {
        if (listWhitelistInfo != null){
            for (WhiteListInfo tmpdbWhiteInfo : listWhitelistInfo){
                if((!"".equals(imsi) && tmpdbWhiteInfo.getImsi().equals(imsi)) || (!"".equals(msisdn) && tmpdbWhiteInfo.getMsisdn().equals(msisdn)))
                    return true;
            }
        }

        if (listWhitelistFromFile != null){
            for (WhiteListInfo tmpdbWhiteInfo : listWhitelistFromFile){
                if((!"".equals(imsi) && tmpdbWhiteInfo.getImsi().equals(imsi)) || (!"".equals(msisdn) && tmpdbWhiteInfo.getMsisdn().equals(msisdn)))
                    return true;
            }
        }

        return false;
    }

    public static boolean isNumeric(String str){
        Pattern pattern=Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private boolean isFormatRight(String readline) {
        //先判断行逗号的个数
        if (readline.length() - readline.replace(",", "").length() != 2)
            return false;

        //如果是以，，开头，说明没有IMSI或手机号，也不行
        if (readline.startsWith(",,"))
            return false;

        //判断长度和是否含有非数字
        if (!"".equals(readline.split(",")[0]) &&
                (readline.split(",")[0].length() != 15 ||
                !isNumeric(readline.split(",")[0]))){
            return false;
        }

        //判断手机号
        if (readline.split(",").length >= 2){
            if (!"".equals(readline.split(",")[1]) &&
                    (readline.split(",")[1].length() != 11 ||
                            !isNumeric(readline.split(",")[1]))){
                return false;
            }
        }

        return true;
    }

    View.OnClickListener importWhitelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File namelistFile = new File(WHITELIST_FILE_PATH);
            if (!namelistFile.exists()) {
                new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导入失败")
                        .setContentText("文件不存在，但已生成模板:"+"手机存储"+WHITELIST_FILE_PATH+ "，请按模板格式填入名单后导入！")
                        .show();

                BufferedWriter bufferedWriter = null;
                try {
                    namelistFile.createNewFile();
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WHITELIST_FILE_PATH,true)));
                    bufferedWriter.write("IMSI,手机号,备注"+"\r\n");
                    if (listWhitelistInfo != null && listWhitelistInfo.size() > 0) {
                        for (WhiteListInfo info: listWhitelistInfo) {
                            //bufferedWriter.write(DateUtil.getDateByFormat(info.getCreateDate(),DateUtil.LOCAL_DATE)+",");
                            bufferedWriter.write(info.getImsi()+"\t,");
                            bufferedWriter.write(info.getMsisdn()+"\t,");
                            bufferedWriter.write(info.getRemark());
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
                        } catch (IOException e) {e.printStackTrace();}
                    }
                }
                EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, WHITELIST_FILE_PATH);
            }else{
                int validImportNum = 0;
                int repeatNum = 0;
                int errorFormatNum = 0;
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(namelistFile));
                    String readline = "";
                    String imsiInLine = "";
                    String msisdnInLine = "";
                    String remark = "";
                    List<WhiteListInfo> listValidWhite = new ArrayList<>();
                    while ((readline = bufferedReader.readLine()) != null) {
                        if (readline.contains("IMSI") && readline.contains("手机号") && readline.contains("备注"))
                            continue;

                        if (!isFormatRight(readline)){
                            errorFormatNum++;
                            continue;
                        }

                        imsiInLine = "";
                        msisdnInLine = "";
                        if (readline.startsWith(",")){
                            imsiInLine = "";
                        }else{
                            imsiInLine = readline.split(",")[0];
                        }
                        msisdnInLine = readline.substring(readline.indexOf(",")+1, readline.lastIndexOf(","));
                        remark = readline.split(",").length ==3?readline.split(",")[2]:"";

                        if (isWhitelistExist(imsiInLine, msisdnInLine, listValidWhite)){
                            repeatNum ++;
                            continue;
                        }

                        validImportNum++;
                        listValidWhite.add(new WhiteListInfo(imsiInLine, msisdnInLine, remark));
                        if (validImportNum > 10000)  //白名单最大10000
                            break;
                    }

                    dbManager.save(listValidWhite);
                } catch (FileNotFoundException e){
                    createExportError("文件未创建成功");
                } catch (IOException e){
                    createExportError("写入文件错误");
                } finally {
                    if(bufferedReader != null){
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {}
                    }
                }

                new MySweetAlertDialog(activity, MySweetAlertDialog.TEXT_SUCCESS)
                        .setTitleText("导入完成")
                        .setContentText("成功导入"+String.valueOf(validImportNum)+"个名单，忽略"+
                                String.valueOf(repeatNum)+"个重复的名单，忽略"+String.valueOf(errorFormatNum)+"行格式或号码错误")
                        .show();

                updateListFromDB();
                if (CacheManager.isDeviceOk()){
                    //当导入量相当大时，数据下发是相当慢的，所以放在子线程里发
                    new Thread() {@Override public void run() {CacheManager.updateWhitelistToDev(activity);}}.start();
                    //CacheManager.updateWhitelistToDev(activity);
                }
            }

            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.IMPORT_WhiteLIST+WHITELIST_FILE_PATH);
        }
    };

    View.OnClickListener exortWhitelistClick = new View.OnClickListener() {
        @Override

        public void onClick(View v) {
            File namelistFile = new File(WHITELIST_FILE_PATH);
            if (namelistFile.exists()){
                namelistFile.delete();
            }

            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WHITELIST_FILE_PATH,true)));
                bufferedWriter.write("IMSI,手机号,备注"+"\r\n");
                if (listWhitelistInfo == null || listWhitelistInfo.size() == 0){
                    ToastUtils.showMessageLong(activity, "当前名单为空，此次导出为模板");
                }else{
                    for (WhiteListInfo info: listWhitelistInfo) {
                        bufferedWriter.write(info.getImsi()+",");
                        bufferedWriter.write(info.getMsisdn()+",");
                        bufferedWriter.write(StringUtils.defaultString(info.getRemark()));
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

            EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, WHITELIST_FILE_PATH);
            new MySweetAlertDialog(activity, MySweetAlertDialog.TEXT_SUCCESS)
                    .setTitleText("导出成功")
                    .setContentText("文件导出在：手机存储"+WHITELIST_FILE_PATH)
                    .show();

            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.EXPORT_WHITELIST+WHITELIST_FILE_PATH);


//            InputStream stream = getResources().openRawResource(R.raw.test1);
//            try {
//                Workbook workbook = WorkbookFactory.create(stream);
//                Sheet sheet = workbook.getSheetAt(0);  //示意访问sheet
//                int rowsCount = sheet.getPhysicalNumberOfRows();
//                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
//                for (int r = 0; r<rowsCount; r++) {
//                    Row row = sheet.getRow(r);
//                    int cellsCount = row.getPhysicalNumberOfCells();
//                    for (int c = 0; c<cellsCount; c++) {
//                        String value = getCellAsString(row, c, formulaEvaluator);
//                        String cellInfo = "r:"+r+"; c:"+c+"; v:"+value;
//                        LogUtils.log(cellInfo);
//                    }
//                }
//            } catch (Exception e) {
//                /* proper exception handling to be here */
//                LogUtils.log(e.toString());
//            }
        }
    };

//    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
//        String value = "";
//        try {
//            Cell cell = row.getCell(c);
//            CellValue cellValue = formulaEvaluator.evaluate(cell);
//            switch (cellValue.getCellType()) {
//                case Cell.CELL_TYPE_BOOLEAN:
//                    value = ""+cellValue.getBooleanValue();
//                    break;
//                case Cell.CELL_TYPE_NUMERIC:
//
//                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
//                        double date = cellValue.getNumberValue();
//                        SimpleDateFormat formatter =
//                                new SimpleDateFormat("dd/MM/yy");
//                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
//                    } else {
//                        DecimalFormat df = new DecimalFormat("#.####");  //去除科学计数法
//                        value = df.format(cell.getNumericCellValue());
//                    }
//                    break;
//                case Cell.CELL_TYPE_STRING:
//                    value = ""+cellValue.getStringValue();
//                    break;
//                default:
//            }
//        } catch (NullPointerException e) {
//            /* proper error handling should be here */
//            LogUtils.log(e.toString());
//        }
//        return value;
//    }

    View.OnClickListener clearWhitelistClick = new View.OnClickListener() {
        @Override

        public void onClick(View v) {
            try {
                dbManager.delete(WhiteListInfo.class);
            } catch (DbException e) {e.printStackTrace();}
            updateListFromDB();
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.CLEAR_WHITELIST);
        }
    };


    void updateListFromDB(){
        try {
            listWhitelistInfo = dbManager.selector(WhiteListInfo.class).findAll();
            if (listWhitelistInfo == null)
                return;

            mAdapter.setUserInfoList(listWhitelistInfo);

            mHandler.sendEmptyMessage(REFRESH_LIST);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void openSwipe(int position){
        if (position < 0)
            return;

        ((SwipeLayout) (lvWhitelistInfo.getChildAt(position))).open(true);
        ((SwipeLayout) (lvWhitelistInfo.getChildAt(position))).setClickToClose(true);
    }

    private void closeSwipe(int position){
        if (listWhitelistInfo == null || listWhitelistInfo.size() == 0)
            return;

        SwipeLayout swipe = (SwipeLayout) (lvWhitelistInfo.getChildAt(position));
        if (swipe != null)
            swipe.close(true);
    }

    @Override
    protected void onDestroy() {
        UIEventManager.unRegister(UIEventManager.KEY_REFRESH_WHITE_LIST, this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, MainActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
            if (msg.what == REFRESH_LIST) {
                if (mAdapter != null) {
                    mAdapter.updateView();
                }
            }else if(msg.what == UPDATE_LIST) {
                updateListFromDB();
                closeSwipe(lastOpenSwipePos);
            }else if(msg.what == UPDATE_WHITELIST){
                updateListFromDB();
                CacheManager.updateWhitelistToDev(activity);
            }else if(msg.what == EXPORT_ERROR){
                new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }
        }
    };


    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.KEY_REFRESH_WHITE_LIST)){
            mHandler.sendEmptyMessage(UPDATE_LIST);
        }
    }

    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.UPDATE_WHITELIST)) {
            try {
                Message msg = new Message();
                msg.what = UPDATE_WHITELIST;
                msg.obj = val;
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
