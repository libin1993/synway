package com.doit.net.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.utils.BlackBoxManger;
import com.doit.net.event.EventAdapter;
import com.doit.net.utils.AccountManage;
import com.doit.net.utils.CacheManager;
import com.doit.net.utils.UCSIDBManager;
import com.doit.net.bean.UserInfo;
import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.LoadingUtils;
import com.doit.net.utils.ToastUtils;
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
    private Context mContext;

    public AddUserDialog(Context context) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
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
                if (!CacheManager.isDeviceOk()){
                    return;
                }


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

                if (!FormatUtils.getInstance().isCommon(name) || !FormatUtils.getInstance().isCommon(password)){
                    ToastUtils.showMessage( "请不要输入特殊字符");
                    return;
                }

                if (!TextUtils.isEmpty(remark)){
                    if (!FormatUtils.getInstance().isCommon(remark)){
                        ToastUtils.showMessage( "请不要输入特殊字符");
                        return;
                    }
                }

                if (addToLocalUser(name, remark,password) < 0){
                    return;
                }

                dismiss();


                EventAdapter.call(EventAdapter.REFRESH_USER_LIST);
                EventAdapter.call(EventAdapter.ADD_BLACKBOX,BlackBoxManger.ADD_USER+"账号："+name+"密码："+password+"备注："+remark);
                LoadingUtils.loading(mContext);
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
