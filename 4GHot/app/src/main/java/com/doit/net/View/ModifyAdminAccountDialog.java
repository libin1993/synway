package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.Model.AccountManage;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2018/11/28.
 */

public class ModifyAdminAccountDialog extends Dialog {

    private String modifyAccount;
    private String modifyPassword;
    private View mView;
    private EditText etAccount;
    private EditText etPassword;
    private Button btSave;
    private Button btCancel;

    public ModifyAdminAccountDialog(Context context, String account, String password) {
        super(context, R.style.Theme_dialog);
        modifyAccount = account;
        modifyPassword = password;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_modify_admin_account, null);
        setCancelable(false);

        etAccount = mView.findViewById(R.id.etAccount);
        etAccount.setText(modifyAccount);
        etPassword = mView.findViewById(R.id.etPassword);
        etPassword.setText(modifyPassword);
        btSave = mView.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AccountManage.saveAccoutToPref(etAccount.getText().toString(), etPassword.getText().toString());
                if (AccountManage.UpdateAccountToDevice()){
                    ToastUtils.showMessage(R.string.modify_admin_success);
                }else{
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getContext().getString(R.string.modify_admin_fail))
                            .setContentText(getContext().getString(R.string.modify_admin_fail_ftp))
                            .show();
                }

                dismiss();

                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.MODIFY_ADMIN_ACCOUNT+etAccount.getText().toString()+"+"+etPassword.getText().toString());
            }
        });


        btCancel = mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
