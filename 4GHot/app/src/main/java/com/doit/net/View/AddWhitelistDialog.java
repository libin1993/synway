package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.doit.net.Event.AddToWhitelistListner;
import com.doit.net.Event.UIEventManager;
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

public class AddWhitelistDialog extends Dialog {
    private View mView;
    private EditText etIMSI;
    private EditText etMsisdn;
    private EditText etRemark;
    private Button btWhitelist;
    private Button btCancel;

    private Context mContext;

    public AddWhitelistDialog(Context context) {
        super(context, R.style.Theme_dialog);
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
        mView = inflater.inflate(R.layout.layout_add_whitelist_dialog, null);
        setCancelable(false);

        etIMSI = (EditText)mView.findViewById(R.id.etIMSI);
        etMsisdn = (EditText)mView.findViewById(R.id.etMsisdn);
        etRemark = (EditText)mView.findViewById(R.id.etRemark);
        btWhitelist = (Button)mView.findViewById(R.id.btWhitelist);
        btWhitelist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String imsi = etIMSI.getText().toString();
                final String msisdn = etMsisdn.getText().toString();
                final String remark = etRemark.getText().toString();

                if ("".equals(imsi) && "".equals(msisdn)){
                    ToastUtils.showMessage(getContext(), "请输入IMSI或手机号");
                    return;
                } else{
                    if (!"".equals(imsi) && imsi.length() != 15){
                        ToastUtils.showMessage(getContext(), "IMSI长度错误，请确认后输入！");
                        return;
                    }

                    if (!"".equals(msisdn) && msisdn.length() != 11){
                        ToastUtils.showMessage(getContext(), "手机长度错误，请确认后输入！");
                        return;
                    }

                    new AddToWhitelistListner(getContext(), imsi, msisdn, remark).onClick(null);
                    if (!"".equals(msisdn) && "".equals(imsi)){
                        ToastUtils.showMessage(mContext, "提示：检测到只输入了手机号，只有翻译成IMSI才可生效！");
                    }else{
                        CacheManager.updateWhitelistToDev(mContext);
                    }
                }

                dismiss();
                UIEventManager.call(UIEventManager.KEY_REFRESH_WHITE_LIST);
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


    private boolean isMsisdnWhitelistExist(String msisdn) {
        if (!"".equals(msisdn)){
            long count = 0;
            try {
                count = UCSIDBManager.getDbManager().selector(WhiteListInfo.class)
                        .where("msisdn","=",msisdn)
                        .count();
            } catch (DbException e) {
                e.printStackTrace();
            }
            if(count>0){
                ToastUtils.showMessage(mContext, R.string.exist_whitelist);
                return true;
            }else{
                return false;
            }
        }

        return true;
    }

    private int addToLocalWhitelist(String imsi, String remark) {
        try {
//            ToastUtils.showMessage(mContext,"index:"+position+", imsi:"+imsi);
            DbManager dbManager = UCSIDBManager.getDbManager();
            long count = dbManager.selector(WhiteListInfo.class)
                    .where("imsi","=", imsi).count();
            if(count>0){
                ToastUtils.showMessage(getContext(), R.string.same_imsi);
                return -1;
            }

            WhiteListInfo whiteListInfo = new WhiteListInfo();
            whiteListInfo.setImsi(imsi);
            whiteListInfo.setRemark(remark);
            dbManager.save(whiteListInfo);

        } catch (DbException e) {
            e.printStackTrace();
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getContext().getString(R.string.add_whitelist_fail))
                    .show();

            return -1;
        }

        return 0;
    }
}
