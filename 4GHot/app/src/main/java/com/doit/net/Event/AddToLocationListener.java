package com.doit.net.Event;

import android.content.Context;
import android.view.View;

import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Protocol.ProtocolManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.UtilOperator;

/**
 * Created by wiker on 2016/4/27.
 */
public class AddToLocationListener implements View.OnClickListener
{

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


            if (CacheManager.getLocState()){
                if (CacheManager.getCurrentLoction().getImsi().equals(imsi)){
                    ToastUtils.showMessage( "该号码正在搜寻中");
                    return;
                }else{
                    EventAdapter.call(EventAdapter.SHOW_PROGRESS,8000);  //防止快速频繁更换定位目标

                    exchangeFcn();

                    CacheManager.updateLoc(imsi);
                    CacheManager.changeLocTarget(imsi);
                    ToastUtils.showMessage( "开始新的搜寻");
                }
            }else{

                exchangeFcn();

                CacheManager.updateLoc(imsi);
                CacheManager.startLoc(imsi);
                ProtocolManager.openAllRf();
                ToastUtils.showMessage("搜寻开始");
            }

            TurnToLocInterface();

            EventAdapter.call(EventAdapter.ADD_LOCATION,imsi);
            EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.START_LOCALTE_FROM_NAMELIST+imsi);
        } catch (Exception e) {
            LogUtils.log("开启搜寻失败"+e);
        }

    }

    /**
     * 更换fcn
     */
    private void exchangeFcn(){

        if ("CTJ".equals(UtilOperator.getOperatorName(imsi))){
            ProtocolManager.setDetectCarrierOpetation("detect_ctj");


            String[] split = CacheManager.b3Fcn.getFcn().split(",");
            boolean hasExchange = false; //是否已更换
            String fcn="";
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("1825") || split[i].equals("1850")){
                    split[i] = "1300";
                    hasExchange = true;
                    break;
                }
            }

            if (split.length == 3){
                if (!hasExchange){
                    split[0] = "1300";
                }

                fcn = split[0] +","+split[1]+","+split[2];
            }

            if (split.length < 3){
                if (!hasExchange){
                    fcn ="1300,"+CacheManager.b3Fcn.getFcn();
                }else {
                    for (int i = 0; i < split.length; i++) {
                        if (i == split.length -1){
                            fcn += split[i];
                        }else {
                            fcn += split[i]+",";
                        }
                    }
                }

            }

            LogUtils.log("更换的频点："+fcn);
            ProtocolManager.setChannelConfig(CacheManager.b3Fcn.getIdx(), fcn, "", "", "", "", "", "");
        }else {
            ProtocolManager.setDetectCarrierOpetation("detect_all");
            ProtocolManager.setChannelConfig(CacheManager.b3Fcn.getIdx(), CacheManager.b3Fcn.getFcn(),
                    "", "", "", "", "", "");
        }

    }

    private void TurnToLocInterface() {
        EventAdapter.call(EventAdapter.CHANGE_TAB, 1);
    }


    private void checkPower(String imsi){
        String operator = UtilOperator.getOperatorName(imsi);
        if (operator.equals(lastLocOperator)){
            return;
        }else {
            UtilOperator.checkPower(imsi);
        }

        lastLocOperator = operator;
    }
}
