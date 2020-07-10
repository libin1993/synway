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

public class AddUserDialog extends Dialog {
    private View mView;
    private EditText etUserName;
    private EditText etRemake;
    private EditText etPassword;
    private Button btAddUser;
    private Button btCancel;

    public AddUserDialog(Context context) {
        super(context, R.style.Theme_dialog);
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
        mView = inflater.inflate(R.layout.layout_add_user, null);
        setCancelable(false);

        etUserName = mView.findViewById(R.id.etImsi);
        etRemake = mView.findViewById(R.id.etRemark);
        etPassword = mView.findViewById(R.id.etPassword);
        btAddUser = mView.findViewById(R.id.btAddUser);
        btAddUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = etUserName.getText().toString();
                String remark = etRemake.getText().toString();
                String password = etPassword.getText().toString();

                if ("".equals(name)){
                    ToastUtils.showMessage("请输入账号！");
                    return;
                }else if ("".equals(password)){
                    ToastUtils.showMessage( "请输入密码！");
                    return;
                }

                if (addToLocalUser(name, remark,password) < 0){
                    return;
                }

                dismiss();
                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.ADD_USER+"账号："+name+"密码："+password+"备注："+remark);
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

    private int addToLocalUser(String name, String remake, String password) {
        try {
//            ToastUtils.showMessage(mContext,"index:"+position+", imsi:"+imsi);
            DbManager dbManager = UCSIDBManager.getDbManager();
            long count = dbManager.selector(UserInfo.class)
                    .where("account","=",name).count();
            if(count>0){
                ToastUtils.showMessage(R.string.same_user);
                return -1;
            }

            UserInfo info = new UserInfo();
            info.setAccount(name);
            info.setRemake(remake);
            info.setPassword(password);
            dbManager.save(info);

            if (AccountManage.UpdateAccountToDevice()){
                ToastUtils.showMessage(R.string.add_success);
                return 0;
            }else{
                dbManager.delete(info);
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getContext().getString(R.string.add_user_fail))
                        .setContentText(getContext().getString(R.string.add_user_fail_ftp))
                        .show();
            }

        } catch (DbException e) {
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getContext().getString(R.string.add_user_fail))
                    .show();
            return -1;
        }

        return 0;
    }

}
