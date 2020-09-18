package com.doit.net.Protocol;

import android.text.TextUtils;

import com.doit.net.Utils.FormatUtils;
import com.doit.net.Utils.UtilDataFormatChange;

import org.apache.http.util.ByteArrayBuffer;

import java.util.Arrays;

/**
 * Author：Libin on 2020/6/5 14:51
 * Email：1993911441@qq.com
 * Describe：信息体
 */
public class GSMSubPackage {
    private byte subMsgLength; //长度 1字节
    private short subMsgNumber; //信息编号 2字节
    private byte[] subMsgContent;  //数据内容 可变长度

    public byte getSubMsgLength() {
        byte packageLength = 3;
        if (subMsgContent !=null && subMsgContent.length>0) {
            packageLength += subMsgContent.length;
        }
        return packageLength;
    }

    //得到整个包的内容
    public byte[] getMsgContent() {
        ByteArrayBuffer byteArray = new ByteArrayBuffer(getSubMsgLength());

        //拷贝包长度
        byte[] tempMsgLength = {getSubMsgLength()};
        byteArray.append(tempMsgLength, 0, tempMsgLength.length);


        //信息编号
        byte[] tempMsgNumber = UtilDataFormatChange.shortToByteArray(this.subMsgNumber);
        FormatUtils.getInstance().reverseData(tempMsgNumber);
        byteArray.append(tempMsgNumber, 0, tempMsgNumber.length);

        //拷贝内容
        if (subMsgContent !=null && subMsgContent.length>0) {
            byteArray.append(subMsgContent, 0, subMsgContent.length);
        }

        return byteArray.toByteArray();
    }


    public void setSubMsgLength(byte subMsgLength) {
        this.subMsgLength = subMsgLength;
    }

    public short getSubMsgNumber() {
        return subMsgNumber;
    }

    public void setSubMsgNumber(short subMsgNumber) {
        this.subMsgNumber = subMsgNumber;
    }

    public byte[] getSubMsgContent() {
        return subMsgContent;
    }

    public void setSubMsgContent(byte[] subMsgContent) {
        this.subMsgContent = subMsgContent;
    }

    @Override
    public String toString() {
        return "GSMSubPackage{" +
                "subMsgLength=" + subMsgLength +
                ", subMsgNumber=" + subMsgNumber +
                ", subMsgContent=" + Arrays.toString(subMsgContent) +
                '}';
    }
}
