package com.doit.net.View;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.doit.net.Model.LicenceManage;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;


/**
 * Created by Zxc on 2018/12/29.
 */

public class LicenceDialog extends Dialog {
    private View mView;
    private EditText etAuthorizeCode;
    private TextView tvMachineId;
    private TextView tvDueTime;
    private Button btAuthorize;
    private Button btCancel;

    Activity activity;

    public LicenceDialog(Activity activity) {
        super(activity, R.style.Theme_dialog);
        this.activity = activity;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);

        tvDueTime.setText(LicenceManage.getDueTime());
        tvMachineId.setText(LicenceManage.getMachineID());
    }

    @Nullable
    @Override
    public ActionBar getActionBar() {
        return super.getActionBar();
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_licence_dialog, null);
        setCancelable(false);

        etAuthorizeCode = mView.findViewById(R.id.etAuthorizeCode);
        tvDueTime = mView.findViewById(R.id.tvDueTime);
        tvMachineId =  mView.findViewById(R.id.tvMachineId);
        btAuthorize = (Button)mView.findViewById(R.id.btAuthorize);
        btAuthorize.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String authorizeCode = etAuthorizeCode.getText().toString();
                if ("".equals(authorizeCode)){
                    ToastUtils.showMessage(getContext(), "请输入授权码");
                    return;
                }

                if(LicenceManage.checkAuthorizeCode(authorizeCode)){
                    LicenceManage.saveAuthorizeCode(authorizeCode);
                    ToastUtils.showMessageLong(getContext(),"授权成功，到期时间："+LicenceManage.getDueTime());
                    dismiss();
                }else{
                    ToastUtils.showMessage(getContext(), "授权码有误，请确认后输入！");
                }
            }
        });

        btCancel = (Button)mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}

