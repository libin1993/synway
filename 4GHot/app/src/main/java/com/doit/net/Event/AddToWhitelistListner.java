package com.doit.net.Event;

import android.content.Context;
import android.view.View;

import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Model.WhiteListInfo;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/7/1.
 */

public class AddToWhitelistListner implements View.OnClickListener {
    private Context mContext;
    private String imsi;
    private String msisdn;
    private String remark;

    public AddToWhitelistListner(Context mContext, String imsi, String msisdn, String remark) {
        this.mContext = mContext;
        this.remark = remark;
        this.msisdn = msisdn;
        this.imsi = imsi;

    }

    @Override
    public void onClick(View v) {
        try {
//            ToastUtils.showMessage(mContext,"index:"+position+", imsi:"+imsi);
            DbManager dbManager = UCSIDBManager.getDbManager();
            if (!"".equals(imsi)){
                long count = dbManager.selector(WhiteListInfo.class)
                        .where("imsi","=",imsi)
                        .count();
                if(count>0){

                    ToastUtils.showMessage(mContext, R.string.exist_whitelist);
                    return;
                }
            }else{
                long count = dbManager.selector(WhiteListInfo.class)
                        .where("msisdn","=",msisdn)
                        .count();
                if(count>0){
                    ToastUtils.showMessage(mContext, R.string.exist_whitelist);
                    return;
                }
            }

            WhiteListInfo info = new WhiteListInfo();
            info.setRemark(remark);
            info.setImsi(imsi);
            info.setMsisdn(msisdn);
            dbManager.save(info);

            ToastUtils.showMessage(mContext,R.string.add_success);
        } catch (DbException e) {
            e.printStackTrace();
            new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(mContext.getString(R.string.add_whitelist_fail))
                    .show();
        }
    }
}
