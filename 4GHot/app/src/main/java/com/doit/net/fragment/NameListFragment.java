package com.doit.net.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.Utils.FileUtils;
import com.doit.net.Utils.FormatUtils;
import com.doit.net.Utils.LogUtils;
import com.doit.net.adapter.BlacklistAdapter;
import com.doit.net.View.NameListEditDialog;
import com.doit.net.base.BaseFragment;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.DateUtils;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.bean.FileBean;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class NameListFragment extends BaseFragment implements EventAdapter.EventCall {

    private ListView mListView;
    private BlacklistAdapter blacklistAdapter;
    private EditText etSearchKeyword;
    private Button btSearch;
    private Button btnAddBlacklist;
    private Button btImportNamelist;
    private Button btExportNamelist;
    private Button btClearNamelist;
    private DbManager dbManager;

    private final String BLACKLIST_FILE_PATH = FileUtils.ROOT_PATH + "Blacklist.xls";
    private List<DBBlackInfo> dbBlackInfos = new ArrayList<>();
    private int lastOpenSwipePos = 0;

    //handler消息
    private final int UPDATE_NAMELIST = 1;
    private final int EXPORT_ERROR = -1;
    private final static  int IMPORT_SUCCESS = 2;  //导入成功
    private final static  int EXPORT_SUCCESS =  3;  //导出成功

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

        EventAdapter.register(EventAdapter.REFRESH_BLACKLIST, this);
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

    private void openSwipe(int position) {
        ((SwipeLayout) (mListView.getChildAt(position))).open(true);
        ((SwipeLayout) (mListView.getChildAt(position))).setClickToClose(true);
    }

    private void closeSwipe(int position) {
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
                    ToastUtils.showMessage( R.string.search_not_found);
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


    View.OnClickListener exportNamelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(BLACKLIST_FILE_PATH);
                        if (file.exists()) {
                            file.delete();
                        }

                        XSSFWorkbook workbook = new XSSFWorkbook();
                        XSSFSheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName("BlackList"));

                        Row row = sheet.createRow(0);
                        Cell cell1 = row.createCell(0);
                        cell1.setCellValue("IMSI");
                        Cell cell2 = row.createCell(1);
                        cell2.setCellValue("姓名");
                        Cell cell3 = row.createCell(2);
                        cell3.setCellValue("备注");
                        Cell cell4 = row.createCell(3);
                        cell4.setCellValue("创建时间");


                        if (dbBlackInfos == null || dbBlackInfos.size() == 0) {
                            ToastUtils.showMessageLong("当前名单为空，此次导出为模板");
                        } else {
                            for (int i = 0; i < dbBlackInfos.size(); i++) {
                                Row rowi = sheet.createRow(i + 1);
                                rowi.createCell(0).setCellValue(dbBlackInfos.get(i).getImsi() + "");
                                rowi.createCell(1).setCellValue(dbBlackInfos.get(i).getName() + "");
                                rowi.createCell(2).setCellValue(dbBlackInfos.get(i).getRemark() + "");
                                rowi.createCell(3).setCellValue(DateUtils.convert2String(dbBlackInfos.get(i).getCreateDate(), DateUtils.LOCAL_DATE));
                            }
                        }

                        OutputStream outputStream = new FileOutputStream(file);
                        workbook.write(outputStream);
                        outputStream.flush();
                        outputStream.close();


                        EventAdapter.call(EventAdapter.UPDATE_FILE_SYS, BLACKLIST_FILE_PATH);

                        Message message = new Message();
                        message.what = EXPORT_SUCCESS;
                        message.obj = "文件导出在：手机存储/"+FileUtils.ROOT_DIRECTORY+"/"+ file.getName();
                        mHandler.sendMessage(message);


                        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.EXPORT_NAMELIST + BLACKLIST_FILE_PATH);

                    } catch (Exception e) {
                        /* proper exception handling to be here */
                        createExportError("导出名单失败");
                    }
                }
            }).start();

        }

    };

    View.OnClickListener importNamelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            File file = new File(FileUtils.ROOT_PATH);
            if (!file.exists()) {
                ToastUtils.showMessageLong("未找到黑名单，请确认已将黑名单放在\"手机存储/"+FileUtils.ROOT_DIRECTORY+"\"目录下");
                return;
            }

            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                ToastUtils.showMessageLong("未找到黑名单，请确认已将黑名单放在\"手机存储/"+FileUtils.ROOT_DIRECTORY+"\"目录下");
                return;
            }

            List<FileBean> fileList = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                String tmpFileName = files[i].getName();
                if (tmpFileName.endsWith(".xls") || tmpFileName.endsWith(".xlsx")) {
                    FileBean fileBean = new FileBean();
                    fileBean.setFileName(tmpFileName);
                    fileBean.setCheck(false);
                    fileList.add(fileBean);
                }
            }

            if (fileList.size() == 0) {
                ToastUtils.showMessageLong("未找到黑名单，黑名单必须是以\".xls\"或\".xlsx\"为后缀的文件");
                return;
            }


            fileList.get(0).setCheck(true);  //默认选中第一个

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_import_whitelist, null);
            PopupWindow mPopupWindow = new PopupWindow(dialogView, FormatUtils.getInstance().dip2px(300), ViewGroup.LayoutParams.WRAP_CONTENT);

            //设置Popup具体控件
            RecyclerView rvFile = dialogView.findViewById(R.id.rv_file);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_import);
            Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_import);
            TextView tvTitle = dialogView.findViewById(R.id.tv_import_whitelist);
            tvTitle.setText("请选择黑名单文件");


            //设置Popup具体参数
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
            mPopupWindow.setFocusable(true);//点击空白，popup不自动消失
            mPopupWindow.setTouchable(true);//popup区域可触摸
            mPopupWindow.setOutsideTouchable(true);//非popup区域可触摸
            mPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);


            rvFile.setLayoutManager(new LinearLayoutManager(getActivity()));
            BaseQuickAdapter<FileBean, BaseViewHolder> adapter = new BaseQuickAdapter<FileBean, BaseViewHolder>(R.layout.layout_file_item, fileList) {
                @Override
                protected void convert(BaseViewHolder helper, FileBean item) {
                    helper.setText(R.id.tv_file_name, item.getFileName());
                    helper.setVisible(R.id.iv_select_whitelist, item.isCheck());
                }
            };

            rvFile.setAdapter(adapter);


            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    for (int i = 0; i < fileList.size(); i++) {
                        fileList.get(i).setCheck(i == position);
                    }
                    adapter.notifyDataSetChanged();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                }
            });

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();

                    importBlackList(fileList);



                }
            });

        }
    };

    /**
     * @param fileList 导入黑名单
     */
    private void importBlackList(List<FileBean> fileList) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                File file = null;
                for (FileBean fileBean : fileList) {
                    if (fileBean.isCheck()) {
                        file = new File(FileUtils.ROOT_PATH + fileBean.getFileName());
                        break;
                    }
                }
                if (file == null) {
                    return;
                }
                int validImportNum = 0;
                int repeatNum = 0;
                int errorFormatNum = 0;
                try {
                    InputStream stream = new FileInputStream(file);
                    Workbook workbook = WorkbookFactory.create(stream);
                    Sheet sheet = workbook.getSheetAt(0);  //示意访问sheet
                    int rowsCount = sheet.getPhysicalNumberOfRows();
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    List<DBBlackInfo> listValidBlack = new ArrayList<>();
                    for (int r = 0; r < rowsCount; r++) {
                        String imsi = "";
                        String name = "";
                        String remark = "";
                        Row row = sheet.getRow(r);


                        imsi = getCellAsString(row, 0, formulaEvaluator);
                        name = getCellAsString(row, 1, formulaEvaluator);
                        remark = getCellAsString(row, 2, formulaEvaluator);

                        if ("IMSI".equals(imsi) && "姓名".equals(name)) {
                            continue;
                        }


                        if (TextUtils.isEmpty(imsi) || !isNumeric(imsi) || imsi.length() != 15) {
                            errorFormatNum++;
                            continue;
                        }


                        if (isBlacklistExist(imsi, listValidBlack)) {
                            repeatNum++;
                            continue;
                        }

                        if (!TextUtils.isEmpty(name) && name.length() > 8) {
                            name = name.substring(0, 8);
                        }

                        if (!TextUtils.isEmpty(remark) && remark.length() > 8) {
                            remark = remark.substring(0, 8);
                        }

                        validImportNum++;
                        listValidBlack.add(new DBBlackInfo(imsi, name, remark, new Date()));

                        if (validImportNum > 100)  //黑名单最大100
                            break;

                    }
                    stream.close();
                    dbManager.save(listValidBlack);

                    Message message = new Message();
                    message.what = IMPORT_SUCCESS;
                    message.obj = "成功导入" + validImportNum + "个名单，忽略" +
                            repeatNum + "个重复的名单，忽略" + errorFormatNum + "行格式或号码错误";
                    mHandler.sendMessage(message);

                    if (CacheManager.isDeviceOk() && !CacheManager.getLocState()) {
                        //当导入量相当大时，数据下发是相当慢的，所以放在子线程里发
                        CacheManager.setCurrentBlackList();
                    }

                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.IMPORT_NAMELIST + BLACKLIST_FILE_PATH);
                } catch (Exception e) {
                    /* proper exception handling to be here */
                    LogUtils.log("导入黑名单错误："+e.toString());
                    createExportError("写入文件错误");
                }
            }
        }).start();
    }

    /**
     * @param row
     * @param c
     * @param formulaEvaluator
     * @return 获取单元格值
     */
    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:

                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("dd/MM/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else {
                        DecimalFormat df = new DecimalFormat("#.########");  //去除科学计数法
                        value = df.format(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            LogUtils.log("黑名单解析异常："+e.toString());
        }
        return value;
    }


    View.OnClickListener clearNamelistClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (dbBlackInfos.size() == 0) {
                ToastUtils.showMessage("当前名单为空");
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
                            } catch (DbException e) {
                                e.printStackTrace();
                            }

                            refreshNamelist();
                            sweetAlertDialog.dismiss();
                        }
                    }).show();
        }
    };


    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    private boolean isBlacklistExist(String imsi, List<DBBlackInfo> listFromFile) {
        if (dbBlackInfos != null) {
            for (DBBlackInfo tmpdbBalckInfo : dbBlackInfos) {
                if (tmpdbBalckInfo.getImsi().equals(imsi))
                    return true;
            }
        }

        if (listFromFile != null) {
            for (int i = 0; i < listFromFile.size(); i++) {
                if (listFromFile.get(i).getImsi().equals(imsi))
                    return true;
            }
        }

        return false;
    }

    private void createExportError(String obj) {
        Message msg = new Message();
        msg.what = EXPORT_ERROR;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_NAMELIST) {

                if (blacklistAdapter != null) {
                    blacklistAdapter.refreshData();
                }

                closeSwipe(lastOpenSwipePos);
            } else if (msg.what == EXPORT_ERROR) {
                new MySweetAlertDialog(getContext(), MySweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因：" + msg.obj)
                        .show();
            }else if (msg.what == IMPORT_SUCCESS){
                new MySweetAlertDialog(getContext(), MySweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("导入完成")
                        .setContentText(String.valueOf(msg.obj))
                        .show();
                refreshNamelist();
            }else if (msg.what == EXPORT_SUCCESS){
                new MySweetAlertDialog(getActivity(), MySweetAlertDialog.TEXT_SUCCESS)
                        .setTitleText("导出成功")
                        .setContentText(String.valueOf(msg.obj))
                        .show();
            }
        }
    };


    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.REFRESH_BLACKLIST)) {
            refreshNamelist();
        }
    }
}
