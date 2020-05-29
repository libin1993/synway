package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.doit_layout_add_name_list, null);

        EditText etIMSI = mView.findViewById(R.id.id_imsi);
        EditText etName = mView.findViewById(R.id.id_name);
        EditText etRemark = mView.findViewById(R.id.etRemark);
        Button btnSave = mView.findViewById(R.id.button_save);
        Button btnCancel= mView.findViewById(R.id.button_cancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString();
                String name = etName.getText().toString();
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
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
