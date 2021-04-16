package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.utils.SPUtils;
import com.doit.net.utils.UCSIDBManager;
import com.doit.net.ucsi.R;
import com.doit.net.utils.ToastUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xutils.DbManager;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by wiker on 2016/4/29.
 */
public class SaveValueDialog extends Dialog {

    private final static Logger log = LoggerFactory.getLogger(SaveValueDialog.class);

    private View mView;
    private DbManager dbManager;
    private SweetAlertDialog pDialog = null;

    private String key;
    private String name;

    public SaveValueDialog(Context context) {
        super(context,R.style.Theme_dialog);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
        tvName = findViewById(R.id.id_name);
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
        mView = inflater.inflate(R.layout.doit_layout_set_value, null);


    }
    public void setKey(String key){
        this.key = key;
    }
    public void setName(String name){
        tvName.setText(name);
    }
    public void setValue(String val){
        uploadUrl.setText(val);
    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }

    @Event(value = R.id.button_save)
    private void saveClick(View v){
        String url = uploadUrl.getText().toString();
        SPUtils.setString(key,url);
        ToastUtils.showMessageLong("设置成功！");
        dismiss();
    }


    @ViewInject(value = R.id.id_upload_url)
    private BootstrapEditText uploadUrl;

    @ViewInject(value = R.id.button_cancel)
    private BootstrapButton button_cancel;

    @ViewInject(value = R.id.button_save)
    private BootstrapButton button_save;

    private TextView tvName;
}
