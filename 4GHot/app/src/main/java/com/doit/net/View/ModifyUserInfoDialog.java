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
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.AccountManage;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Model.UserInfo;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2018/11/27.
 */

public class ModifyUserInfoDialog extends Dialog {

    private String modifyName;
    private String modifyRemake;
    private String modifyPassword;
    private View mView;
    private EditText etUserName;
    private EditText etRemake;
    private EditText etPassword;
    private Button btSave;
    private Button btCancel;
    private Button btDelete;

    public ModifyUserInfoDialog(Context context, String name, String password, String remake ) {
        super(context, R.style.Theme_dialog);
        modifyName = name;
        modifyRemake = remake;
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
        mView = inflater.inflate(R.layout.layout_modify_user_info, null);
        setCancelable(false);

        etUserName = mView.findViewById(R.id.etImsi);
        etUserName.setText(modifyName);
        etRemake = mView.findViewById(R.id.etRemark);
        etRemake.setText(modifyRemake);
        etPassword = mView.findViewById(R.id.etPassword);
        etPassword.setText(modifyPassword);
        btSave = mView.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            if ("".equals(etUserName.getText().toString()) || "".equals(etPassword.getText().toString())){
                ToastUtils.showMessage("账号或密码为空，请确认后输入！");
                return;
            }

            try {
                DbManager db = UCSIDBManager.getDbManager();
                UserInfo tmpUserInfo = db.selector(UserInfo.class)
                        .where("account", "=", modifyName)
                        .findFirst();

                if (tmpUserInfo == null){
                    ToastUtils.showMessage(R.string.modify_user_fail);
                    return;
                }

                if (!modifyName.equals(etUserName.getText().toString())){
                    long count = db.selector(UserInfo.class)
                            .where("account","=",etUserName.getText().toString())
                            .count();
                    if(count>0){
                        ToastUtils.showMessage(R.string.same_user);
                        return;
                    }
                }

                tmpUserInfo.setAccount(etUserName.getText().toString());
                tmpUserInfo.setRemake(etRemake.getText().toString());
                tmpUserInfo.setPassword(etPassword.getText().toString());
                UCSIDBManager.getDbManager().update(tmpUserInfo, "account", "password", "remake");
                if (AccountManage.UpdateAccountToDevice()){
                    ToastUtils.showMessage(R.string.modify_user_success);

                    EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.MODIFY_USER+"修改账户"+modifyName+
                            "为:"+etUserName.getText().toString() + "+" + etPassword.getText().toString() +
                            ("".equals(etRemake.getText().toString())?"":("+"+etRemake.getText().toString())));
                }else{
                    tmpUserInfo.setAccount(modifyName);
                    tmpUserInfo.setRemake(modifyRemake);
                    tmpUserInfo.setPassword(modifyPassword);
                    UCSIDBManager.getDbManager().update(tmpUserInfo, "account", "password", "remake");

                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getContext().getString(R.string.modify_user_fail))
                            .setContentText(getContext().getString(R.string.modify_user_fail_ftp))
                            .show();
                }


            } catch (DbException e) {
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getContext().getString(R.string.modify_user_fail))
                        .show();
            }

            dismiss();
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
