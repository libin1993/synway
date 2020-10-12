package com.doit.net.Event;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.DBChannel;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.UtilOperator;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocationListener implements View.OnClickListener {

    private int position;
    private Context mContext;
    private String imsi;
    private String remark;
    private DBBlackInfo blackInfo;
    private String lastLocOperator; //上次定位号码制式

    public AddToLocationListener(int position, Context mContext,String imsi,String remark) {
        this.position = position;
        this.mContext = mContext;
        this.remark = remark;
        this.imsi = imsi;
        this.blackInfo = new DBBlackInfo();
        this.blackInfo.setImsi(imsi);
        this.blackInfo.setRemark(remark);
    }


    public AddToLocationListener(int position, Context mContext, DBBlackInfo blackInfo){
        this.position = position;
        this.mContext = mContext;
        this.blackInfo = blackInfo;
        this.remark = blackInfo.getRemark();
        this.imsi = blackInfo.getImsi();

    }
    @Override
    public void onClick(View v) {
        try {
            if(!CacheManager.checkDevice(mContext)){
                return;
            }

            if (TextUtils.isEmpty(imsi)){
                return;
            }


            if (CacheManager.getLocState()){
                if (CacheManager.getCurrentLoction().getImsi().equals(imsi)){
                    ToastUtils.showMessage( "该号码正在搜寻中");
                    return;
                }else{
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS,8000);  //防止快速频繁更换定位目标

                    ProtocolManager.exchangeFcn(imsi);

                    CacheManager.updateLoc(imsi);
                    CacheManager.changeLocTarget(imsi);
                    ToastUtils.showMessage( "开始新的搜寻");
                }
            }else{
                EventAdapter.call(EventAdapter.SHOW_PROGRESS,5000);  //防止快速频繁更换定位目标
                ProtocolManager.exchangeFcn(imsi);

                CacheManager.updateLoc(imsi);
                CacheManager.startLoc(imsi);

                ProtocolManager.openAllRf();

                ToastUtils.showMessage("搜寻开始");
            }

            EventAdapter.call(EventAdapter.CHANGE_TAB, 1);

            EventAdapter.call(EventAdapter.ADD_LOCATION,imsi);
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST+imsi);
        } catch (Exception e) {
            LogUtils.log("开启搜寻失败"+e);
        }

    }


}
