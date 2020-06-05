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
import com.doit.net.Event.AddToWhitelistListner;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Model.WhiteListInfo;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/5/30.
 */

public class ModifyWhitelistDialog extends Dialog {
    private String modifyIMSI;
    private String modifyMsisdn;
    private String modifyRemark;
    private View mView;
    private EditText etIMSI;
    private EditText etMsisdn;
    private EditText etRemark;
    private Button btSave;
    private Button btCancel;

    private Context mContext;

    public ModifyWhitelistDialog(Context context, String imsi,String msisdn, String remark ) {
        super(context, R.style.Theme_dialog);
        modifyIMSI = imsi;
        modifyMsisdn = msisdn;
        modifyRemark = remark;
        mContext = context;
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
        mView = inflater.inflate(R.layout.layout_modify_whitelist_info, null);
        setCancelable(false);

        etMsisdn = mView.findViewById(R.id.etMsisdn);
        etMsisdn.setText(modifyMsisdn);
        etIMSI = mView.findViewById(R.id.etImsi);
        etIMSI.setText(modifyIMSI);
        etRemark = mView.findViewById(R.id.etRemark);
        etRemark.setText(modifyRemark);
        btSave = mView.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String imsi = etIMSI.getText().toString();
                String msisdn = etMsisdn.getText().toString();
                String remark = etRemark.getText().toString();

                if (!"".equals(imsi) && imsi.length() != 15){
                    ToastUtils.showMessage(getContext(), "IMSI长度错误！");
                    return;
                }

                if (!"".equals(msisdn) && msisdn.length() != 11){
                    ToastUtils.showMessage(getContext(), "手机号长度错误！");
                    return;
                }

                //如果修改了imsi
                if (!imsi.equals(modifyIMSI)){
                    DbManager db = UCSIDBManager.getDbManager();
                    WhiteListInfo whiteListInfo = null;
                    try {
                        whiteListInfo = db.selector(WhiteListInfo.class)
                                .where("imsi", "=", modifyIMSI)
                                .findFirst();

                        if (whiteListInfo == null){
                            ToastUtils.showMessage(getContext(),R.string.modify_whitelist_fail);
                            return;
                        }

                        db.delete(whiteListInfo);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }

                    new AddToWhitelistListner(mContext, imsi, msisdn, remark).onClick(null);
                    CacheManager.updateWhitelistToDev(mContext);
                }else{
                    try {
                        WhiteListInfo whiteListInfo = UCSIDBManager.getDbManager().selector(WhiteListInfo.class)
                                .where("imsi", "=", modifyIMSI)
                                .findFirst();

                        if (whiteListInfo == null){
                            ToastUtils.showMessage(getContext(),R.string.modify_whitelist_fail);
                            return;
                        }

                        whiteListInfo.setMsisdn(etMsisdn.getText().toString());
                        whiteListInfo.setRemark(etRemark.getText().toString());
                        UCSIDBManager.getDbManager().update(whiteListInfo, "msisdn", "remark");
                    } catch (DbException e) {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getContext().getString(R.string.modify_whitelist_fail))
                                .show();
                    }
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
