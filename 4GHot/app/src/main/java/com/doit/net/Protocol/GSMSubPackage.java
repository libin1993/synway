package com.doit.net.Protocol;

import com.doit.net.Utils.UtilDataFormatChange;

import org.apache.http.util.ByteArrayBuffer;

/**
 * Author：Libin on 2020/6/5 14:51
 * Email：1993911441@qq.com
 * Describe：
 */
public class GSMSubPackage {
    private byte subMsgLength; //长度 1字节
    private short subMsgNumber; //信息编号 2字节
    private byte[] subMsgContent;  //数据内容 可变长度

    public byte getSubMsgLength() {
        byte packageLength = 8;
        if (this.subMsgContent != null) {
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
        byteArray.append(tempMsgNumber, 0, tempMsgNumber.length);

        //拷贝内容
        if (this.subMsgContent != null) {
            byteArray.append(this.subMsgContent, 0, this.subMsgContent.length);
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
}
