package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.Event.AddToLocalBlackListener;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by wiker on 2016/4/29.
 */
public class NameListEditDialog extends Dialog {

    private View mView;

    public NameListEditDialog(Context context) {
        super(context,R.style.Theme_dialog);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
//        id_imei.setVisibility(View.GONE);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.doit_layout_add_name_list, null);
//        setContentView(R.layout.doit_layout_system_setup);

    }

    @Event(value = R.id.button_cancel)
    private void cancelClick(View v){
        dismiss();
    }

    @Event(value = R.id.button_save)
    private void saveClick(View v){
        String imsi = id_imsi.getText().toString();
        String name = id_name.getText().toString();
        String remake = etRemark.getText().toString();

        if(imsi.length() != 15){
            ToastUtils.showMessage(getContext(),R.string.tip_09);
            return;
        }

        EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.ADD_NAMELIST+imsi+"+"+name);
        AddToLocalBlackListener listener = new AddToLocalBlackListener(getContext(),name,imsi,remake);
        listener.onClick(v);
        dismiss();
    }


    @ViewInject(value = R.id.id_imsi)
    private BootstrapEditText id_imsi;
    @ViewInject(value = R.id.id_name)
    private BootstrapEditText id_name;
    @ViewInject(value = R.id.etRemark)
    private BootstrapEditText etRemark;
}
