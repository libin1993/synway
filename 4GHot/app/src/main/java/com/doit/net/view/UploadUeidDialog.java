package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.model.DBUeidInfo;
import com.doit.net.model.UCSIDBManager;
import com.doit.net.ucsi.R;
import com.doit.net.utils.StringUtils;
import com.doit.net.utils.ToastUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by wiker on 2016/4/29.
 */
public class UploadUeidDialog extends Dialog {
    private final static Logger log = LoggerFactory.getLogger(UploadUeidDialog.class);

    private View mView;
    private DbManager dbManager;

    private SweetAlertDialog pDialog = null;

    public UploadUeidDialog(Context context) {
        super(context,R.style.Theme_dialog);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
        dbManager = UCSIDBManager.getDbManager();
        createProcessDialog();
    }
    private void createProcessDialog(){
        pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.setTitleText("Loading...");
        pDialog.setCancelable(false);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.doit_layout_upload_ueid, null);


    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }

    @Event(value = R.id.button_save)
    private void saveClick(View v){
        try {
            String nameVal = name.getText().toString();
            String url = uploadUrl.getText().toString();

            if(StringUtils.isBlank(nameVal)){
                ToastUtils.showMessageLong(R.string.tip_please_input_name);
                return;
            }
            if(StringUtils.isBlank(url) || !url.startsWith("http")){
                ToastUtils.showMessageLong(R.string.tip_please_input_right_url);
                return;
            }

            List<DBUeidInfo> dbUeidInfos = dbManager.selector(DBUeidInfo.class)
                    .orderBy("id", true)
                    .findAll();
            if(dbUeidInfos == null || dbUeidInfos.size()<=0){
                ToastUtils.showMessageLong(R.string.tip_no_upload_data);
                return;
            }
            pDialog.show();
            log.debug("Prepare upload,data num:"+dbUeidInfos.size());
        } catch (DbException e) {
            log.error("Query db error",e);
        }
    }


    @ViewInject(value = R.id.id_upload_url)
    private BootstrapEditText uploadUrl;
    @ViewInject(value = R.id.id_name)
    private BootstrapEditText name;

    @ViewInject(value = R.id.button_cancel)
    private BootstrapButton button_cancel;

    @ViewInject(value = R.id.button_save)
    private BootstrapButton button_save;
}
