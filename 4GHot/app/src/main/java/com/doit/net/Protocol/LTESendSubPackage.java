package com.doit.net.Protocol;

import com.doit.net.Utils.UtilDataFormatChange;

import org.apache.http.util.ByteArrayBuffer;


/**
 * Created by Zxc on 2018/10/18.
 */
public class LTESendSubPackage {
	ByteArrayBuffer byteArray=null;
	//实际增加的长
	private int realCount=0;
	
	//设置子协议所的长度
	public void setSubPackageLength(int length) {
		byteArray=new ByteArrayBuffer(length);
	}
	
	//增加一个byte
	public void appendByte(byte tempValue) {
		byteArray.append(tempValue);
		realCount+=1;
	}
	
	//增加一个short
	public void appendShort(Short tempValue) {
		byte[] tempByte= UtilDataFormatChange.shortToByteArray(tempValue);
		byteArray.append(tempByte,0,tempByte.length);
		realCount+=tempByte.length;
	}
	
	//增加一个int
	public void appendInt(int tempValue) {
		byte[] tempByte=UtilDataFormatChange.IntToByteArray(tempValue);
		byteArray.append(tempByte,0,tempByte.length);
		realCount+=tempByte.length;
	}

	//获取子协议内容
	public byte[] getSubContent()
	{
		return byteArray.toByteArray();
	}
	
}
