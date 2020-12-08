package com.doit.net.protocol;

import com.doit.net.sockets.UDPSocketUtils;

import org.apache.http.util.ByteArrayBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Author：Libin on 2020/9/17 14:12
 * Email：1993911441@qq.com
 * Describe：发送指令
 */
public class GSMSendManager {

    /**
     * @param msgNumber
     * @param msgContent
     * @return 获取信息体
     */
    private static GSMSubPackage getSubContent(short msgNumber, byte[] msgContent) {
        GSMSubPackage subPackage = new GSMSubPackage();
        subPackage.setSubMsgLength((byte) (3 + msgContent.length));
        subPackage.setSubMsgNumber(msgNumber);
        subPackage.setSubMsgContent(msgContent);
        return subPackage;
    }


    /**
     * @param msgNumber 消息编号
     * @return 获取消息
     */
    private static byte[] getContent(byte msgNumber) {
        return getContent(msgNumber, (byte) 0x00, (byte) 0x00, null);
    }


    /**
     * @param msgNumber      消息编号
     * @param subPackageList  信息体
     * @return 获取消息
     */
    private static byte[] getContent(byte msgNumber, List<GSMSubPackage> subPackageList) {
        return getContent(msgNumber, (byte) 0x00, (byte) 0x00, subPackageList);
    }


    /**
     * @param msgNumber          消息编号
     * @param carrierInstruction  载波编号
     * @param msgParameter      消息参数
     * @param subPackageList   信息体
     * @return 获取消息
     */
    private static byte[] getContent(byte msgNumber, byte carrierInstruction, byte msgParameter, List<GSMSubPackage> subPackageList) {

        GSMPackage sendPackage = new GSMPackage();
        sendPackage.setMsgNumber(msgNumber);
        sendPackage.setCarrierInstruction(carrierInstruction);
        sendPackage.setMsgParameter(msgParameter);
        if (subPackageList != null && subPackageList.size() > 0) {
            int length = 0;
            for (GSMSubPackage gsmSubPackage : subPackageList) {
                length += gsmSubPackage.getSubMsgLength();
            }

            ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(length);

            for (GSMSubPackage gsmSubPackage : subPackageList) {
                byte[] msgContent = gsmSubPackage.getMsgContent();
                byteArrayBuffer.append(msgContent, 0, msgContent.length);
            }

            sendPackage.setSubContent(byteArrayBuffer.toByteArray());
        }

        return sendPackage.getMsgContent();
    }


    /**
     * 运行状态
     */
    public static void getDeviceVersion() {
        List<GSMSubPackage> subPackageList = new ArrayList<>();
        subPackageList.add(getSubContent(GSMMsgType.DEVICE_VERSION, new byte[20]));
        UDPSocketUtils.getInstance().sendData(getContent(GSMMsgType.GET_COMMON,subPackageList));
    }

    /**
     * 运行状态
     */
    public static void getDeviceStatus() {
        List<GSMSubPackage> subPackageList = new ArrayList<>();
        subPackageList.add(getSubContent(GSMMsgType.DEVICE_STATUS, new byte[4]));
        UDPSocketUtils.getInstance().sendData(getContent(GSMMsgType.GET_COMMON,subPackageList));
    }

    /**
     * 临近小区查询
     */
    public static void getNearbyCell() {
        UDPSocketUtils.getInstance().sendData(getContent(GSMMsgType.GET_NB_CELL));
    }


    /**
     * 开启射频
     */
    public static void openRF() {
        UDPSocketUtils.getInstance().sendData(getContent(GSMMsgType.OPEN_RF));
    }


    /**
     * 关闭射频
     */
    public static void closeRF() {
        UDPSocketUtils.getInstance().sendData(getContent(GSMMsgType.CLOSE_RF));
    }


    /**
     * 获取GSM参数
     */
    public static void getGSMParams() {
        List<GSMSubPackage> subPackageList = new ArrayList<>();
        subPackageList.add(getSubContent(GSMMsgType.DEVICE_STATUS, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.CONFIGURE_MODE, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.WORK_MODE, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.GSM_FCN, new byte[8]));
        subPackageList.add(getSubContent(GSMMsgType.DOWN_ATTENUATION, new byte[8]));
        UDPSocketUtils.getInstance().sendData(UDPSocketUtils.REMOTE_GSM_IP,getContent(GSMMsgType.GET_COMMON,subPackageList));
    }


    /**
     * 获取CDMA参数
     */
    public static void getCDMAParams() {
        List<GSMSubPackage> subPackageList = new ArrayList<>();
        subPackageList.add(getSubContent(GSMMsgType.DEVICE_STATUS, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.CONFIGURE_MODE, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.WORK_MODE, new byte[4]));
        subPackageList.add(getSubContent(GSMMsgType.CDMA_FCN1, new byte[8]));
        subPackageList.add(getSubContent(GSMMsgType.CDMA_FCN2, new byte[8]));
        subPackageList.add(getSubContent(GSMMsgType.CDMA_FCN3, new byte[8]));
        subPackageList.add(getSubContent(GSMMsgType.CDMA_FCN4, new byte[8]));
        subPackageList.add(getSubContent(GSMMsgType.DOWN_ATTENUATION, new byte[8]));
        UDPSocketUtils.getInstance().sendData(UDPSocketUtils.REMOTE_CDMA_IP,getContent(GSMMsgType.GET_COMMON,subPackageList));
    }
}
