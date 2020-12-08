package com.doit.net.event;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.doit.net.model.BlackBoxManger;
import com.doit.net.model.CacheManager;
import com.doit.net.protocol.ProtocolManager;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.ToastUtils;
import com.doit.net.activity.MainActivity;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocationListener implements View.OnClickListener {

    private Context mContext;
    private String imsi;

    public AddToLocationListener(Context mContext,String imsi) {
        this.mContext = mContext;
        this.imsi = imsi;
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


            if (!(mContext  instanceof MainActivity)) {
                ((Activity)mContext).finish();
            }

            EventAdapter.call(EventAdapter.CHANGE_TAB, 1);

            EventAdapter.call(EventAdapter.ADD_LOCATION,imsi);
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST+imsi);
        } catch (Exception e) {
            LogUtils.log("开启搜寻失败"+e);
        }

    }


}
