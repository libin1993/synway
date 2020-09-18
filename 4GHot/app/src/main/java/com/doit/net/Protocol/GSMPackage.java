package com.doit.net.Protocol;

import com.doit.net.Utils.FormatUtils;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.UtilDataFormatChange;

import org.apache.http.util.ByteArrayBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * Author：Libin on 2020/6/5 13:43
 * Email：1993911441@qq.com
 * Describe：
 */
public class GSMPackage {

    private String ip;    //设备ip
    private int msgLength;//消息长度 4字节
    private byte msgSequence;//消息序号 1字节
    private byte msgNumber;//消息编号 1字节
    private byte carrierInstruction;//载波指示 1字节
    private byte msgParameter;//消息参数 1字节
    private byte[] subContent;//信息体 可变长度



    //得到整个包的内容
    public byte[] getMsgContent() {
        ByteArrayBuffer byteArray = new ByteArrayBuffer(getMsgLength());

        //包长度
        byte[] tempMsgLength = UtilDataFormatChange.intToByteArray(getMsgLength());
        byteArray.append(tempMsgLength, 0, tempMsgLength.length);

        //序号
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

        //信息体
        if (this.subContent != null) {
            byteArray.append(subContent, 0, subContent.length);
        }

        return byteArray.toByteArray();
    }


    public int getMsgLength() {
        int packageLength = 8;
        if (this.subContent != null) {
            packageLength += subContent.length;
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


    public byte[] getSubContent() {
        return subContent;
    }

    public void setSubContent(byte[] subContent) {
        this.subContent = subContent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "GSMPackage{" +
                "ip='" + ip + '\'' +
                ", msgLength=" + msgLength +
                ", msgSequence=" + msgSequence +
                ", msgNumber=" + msgNumber +
                ", carrierInstruction=" + carrierInstruction +
                ", msgParameter=" + msgParameter +
                ", subContent=" + Arrays.toString(subContent) +
                '}';
    }
}
