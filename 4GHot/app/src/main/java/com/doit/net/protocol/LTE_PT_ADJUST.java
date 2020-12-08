package com.doit.net.protocol;

import com.doit.net.sockets.ServerSocketUtils;
import com.doit.net.utils.LogUtils;
import com.doit.net.utils.UtilDataFormatChange;

/**
 * Author：Libin on 2020/8/6 18:14
 * Email：1993911441@qq.com
 * Describe：校时
 */
public class LTE_PT_ADJUST {

    public static final byte PT_ADJUST=0x03;


    public static final byte ADJUST_APP=0x01;
    public static final byte ADJUST_RESP=0x02;


    //校时回复
    public static void response(LTEReceivePackage receivePackage) {
        LTESendPackage sendPackage = new LTESendPackage();

        //设置Sequence ID
        sendPackage.setPackageSequence(receivePackage.getPackageSequence());
        //设置Session ID
        sendPackage.setPackageSessionID(receivePackage.getPackageSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(receivePackage.getPackageEquipType());
        //设置预留
        sendPackage.setPackageReserve((byte)0xFF);
        //设置主类型
        sendPackage.setPackageMainType(PT_ADJUST);
        //设置子类型
        sendPackage.setPackageSubType(ADJUST_RESP);
        //设置内容
        sendPackage.setByteSubContent(new byte[]{(byte) 0xFF});
        //设置校验位
        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());


        //获取整体的包
        byte[] tempSendBytes=sendPackage.getPackageContent();


        LogUtils.log("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:0x" + Integer.toHexString(receivePackage.getPackageSubType())+ ";  子协议:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));

        ServerSocketUtils.getInstance().sendData(tempSendBytes);
    }


    public static void sendData(byte subType, String paramContent){
        LTESendPackage sendPackage = new LTESendPackage();
        //设置Sequence ID
        sendPackage.setPackageSequence(LTEProtocol.getSequenceID());
        //设置Session ID
        sendPackage.setPackageSessionID(LTEProtocol.getSessionID());
        //设置EquipType
        sendPackage.setPackageEquipType(LTEProtocol.equipType);
        //设置预留
        sendPackage.setPackageReserve((byte) 0xFF);
        //设置主类型
        sendPackage.setPackageMainType(PT_ADJUST);
        //设置子类型
        sendPackage.setPackageSubType(subType);
        sendPackage.setByteSubContent(UtilDataFormatChange.stringtoBytesForASCII(paramContent));

        //设置校验位
        sendPackage.setPackageCheckNum(sendPackage.getCheckNum());

        //获取整体的包
        byte[] tempSendBytes = sendPackage.getPackageContent();
        LogUtils.log("TCP发送：Type:" + sendPackage.getPackageMainType() + ";  SubType:0x" + Integer.toHexString(sendPackage.getPackageSubType()) + ";  子协议:" + UtilDataFormatChange.bytesToString(sendPackage.getByteSubContent(), 0));
        ServerSocketUtils.getInstance().sendData(tempSendBytes);
    }
}
