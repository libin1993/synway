package com.doit.net.Event;

import android.content.Context;
import android.view.View;

import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.ToastUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocalBlackListener implements View.OnClickListener {
    private Context mContext;
    private String imsi;
    private String name;
    private String remark;

    public AddToLocalBlackListener(Context mContext, String name, String imsi, String remark) {
        this.mContext = mContext;
        this.imsi = imsi;
        this.name = name;
        this.remark = remark;
    }

    public AddToLocalBlackListener(Context mContext, String imsi) {
        this.mContext = mContext;
        this.imsi = imsi;
        this.name = "未设置";
    }

    @Override
    public void onClick(View v) {
        try {
//            ToastUtils.showMessage(mContext,"index:"+position+", imsi:"+imsi);
            DbManager dbManager = UCSIDBManager.getDbManager();
            long count = dbManager.selector(DBBlackInfo.class)
                    .where("imsi","=",imsi).count();
            if(count>0){
                ToastUtils.showMessage(mContext, R.string.tip_17);
                return;
            }
            DBBlackInfo info = new DBBlackInfo();
            info.setCreateDate(new Date());
            info.setRemark(remark);
            info.setImsi(imsi);
            info.setName(name);
            dbManager.save(info);

            if (CacheManager.isDeviceOk() && !CacheManager.getLocState()){
                ProtocolManager.setBlackList("2", "#"+imsi);
            }

            ToastUtils.showMessage(mContext,R.string.add_success);
        } catch (DbException e) {
            new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(mContext.getString(R.string.add_black_fail))
                    .show();
        }
    }
}
