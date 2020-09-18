package com.doit.net.Protocol;

import com.doit.net.Sockets.UDPSocketUtils;

/**
 * Author：Libin on 2020/9/17 14:12
 * Email：1993911441@qq.com
 * Describe：发送指令
 */
public class GSMSendManager {

    /**
     * 临近小区查询
     */
    public static void getNearbyCell(){
        GSMPackage sendPackage = new GSMPackage();
        sendPackage.setMsgNumber(GSMMsgType.GET_NB_CELL);
        sendPackage.setCarrierInstruction((byte) 0x00);
        sendPackage.setMsgParameter((byte) 0x00);

        UDPSocketUtils.getInstance().sendData(UDPSocketUtils.REMOTE_GSM_IP,sendPackage.getMsgContent());
    }


    /**
     * 开启射频
     */
    public static void  openRF(){
        GSMPackage sendPackage = new GSMPackage();
        sendPackage.setMsgNumber(GSMMsgType.OPEN_RF);
        sendPackage.setCarrierInstruction((byte) 0x00);
        sendPackage.setMsgParameter((byte) 0x00);

        UDPSocketUtils.getInstance().sendData(sendPackage.getMsgContent());
    }


    /**
     * 关闭射频
     */
    public static void  closeRF(){
        GSMPackage sendPackage = new GSMPackage();
        sendPackage.setMsgNumber(GSMMsgType.CLOSE_RF);
        sendPackage.setCarrierInstruction((byte) 0x00);
        sendPackage.setMsgParameter((byte) 0x00);

        UDPSocketUtils.getInstance().sendData(sendPackage.getMsgContent());
    }
}
