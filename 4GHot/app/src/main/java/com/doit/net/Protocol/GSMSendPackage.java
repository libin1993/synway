package com.doit.net.Protocol;

import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.UtilDataFormatChange;

import org.apache.http.util.ByteArrayBuffer;

/**
 * Author：Libin on 2020/6/5 13:43
 * Email：1993911441@qq.com
 * Describe：
 */
public class GSMSendPackage {

    private int msgLength;//消息长度 4字节
    private byte msgSequence;//消息序号 1字节
    private byte msgNumber;//消息编号 1字节
    private byte carrierInstruction;//载波指示 1字节
    private byte msgParameter;//消息参数 1字节
    private byte[] msgSubContent;//信息体 可变长度


    //得到整个包的内容
    public byte[] getMsgContent() {
        ByteArrayBuffer byteArray = new ByteArrayBuffer(getMsgLength());

        //拷贝包长度
        byte[] tempMsgLength = UtilDataFormatChange.intToByteArray(getMsgLength());
        byteArray.append(tempMsgLength, 0, tempMsgLength.length);

        //拷贝序号
        byte[] tempMsgSequence = {GSMProtocol.getSequenceID()};
        byteArray.append(tempMsgSequence, 0, tempMsgSequence.length);

        //消息编号
        byte[] tempMsgNumber = {this.msgNumber};
        byteArray.append(tempMsgNumber, 0, tempMsgNumber.length);

        //载波指示
        byte[] tempCarrierInstruction = {this.carrierInstruction};
        byteArray.append(tempCarrierInstruction, 0, tempCarrierInstruction.length);

        //消息参数
        byte[] tempMsgParameter = {this.msgParameter};
        byteArray.append(tempMsgParameter, 0, tempMsgParameter.length);

        //拷贝内容
        if (this.msgSubContent != null) {
            byteArray.append(this.msgSubContent, 0, this.msgSubContent.length);
        }

        return byteArray.toByteArray();
    }


    public int getMsgLength() {
        int packageLength = 8;
        if (this.msgSubContent != null) {
            packageLength += msgSubContent.length;
        }
        return packageLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public byte getMsgSequence() {
        return msgSequence;
    }

    public void setMsgSequence(byte msgSequence) {
        this.msgSequence = msgSequence;
    }

    public byte getMsgNumber() {
        return msgNumber;
    }

    public void setMsgNumber(byte msgNumber) {
        this.msgNumber = msgNumber;
    }

    public byte getCarrierInstruction() {
        return carrierInstruction;
    }

    public void setCarrierInstruction(byte carrierInstruction) {
        this.carrierInstruction = carrierInstruction;
    }

    public byte getMsgParameter() {
        return msgParameter;
    }

    public void setMsgParameter(byte msgParameter) {
        this.msgParameter = msgParameter;
    }

    public byte[] getMsgSubContent() {
        return msgSubContent;
    }

    public void setMsgSubContent(byte[] msgSubContent) {
        this.msgSubContent = msgSubContent;
    }
}
