package com.doit.net.Protocol;

import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.UtilDataFormatChange;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTESendPackage
{
	short packageLength;//长 2字节
	short packageCheckNum;//校验位2字节
	short packageSequence;//包的序号2字节
	short packageSessionID;//对话ID2字节
	byte packageEquipType;//设备型1字节
	byte packageReserve;//预留1字节
	byte packageMainType;//主类型1字节
	byte packageSubType;//子类型1字节
	byte[] byteSubContent;//子协议内容
	public short getPackageLength() {
		return packageLength;
	}
	public void setPackageLength(short packageLength) {
		this.packageLength = packageLength;
	}
	public short getPackageCheckNum() {
		return packageCheckNum;
	}
	public void setPackageCheckNum(short packageCheckNum) {
		this.packageCheckNum = packageCheckNum;
	}
	public short getPackageSequence() {
		return packageSequence;
	}
	public void setPackageSequence(short packageSequence) {
		this.packageSequence = packageSequence;
	}
	public short getPackageSessionID() {
		return packageSessionID;
	}
	public void setPackageSessionID(short packageSessionID) {
		this.packageSessionID = packageSessionID;
	}
	public byte getPackageEquipType() {
		return packageEquipType;
	}
	public void setPackageEquipType(byte packageEquipType) {
		this.packageEquipType = packageEquipType;
	}
	public byte getPackageReserve() {
		return packageReserve;
	}
	public void setPackageReserve(byte packageReserve) {
		this.packageReserve = packageReserve;
	}
	public byte getPackageMainType() {
		return packageMainType;
	}
	public void setPackageMainType(byte packageMainType) {
		this.packageMainType = packageMainType;
	}
	public byte getPackageSubType() {
		return packageSubType;
	}
	public void setPackageSubType(byte packageSubType) {
		this.packageSubType = packageSubType;
	}
	public byte[] getByteSubContent() {
		return byteSubContent;
	}
	public void setByteSubContent(byte[] byteSubContent) {
		this.byteSubContent = byteSubContent;
	}
	
	//得到包长度
	public short getPackLength() {
		short packageLength=12;
		if(this.byteSubContent!=null) {
			packageLength+=byteSubContent.length;
		}
		return packageLength;
	}

	//得到整个包的内容
	public byte[] getPackageContent() {
		ByteArrayBuffer byteArray=new ByteArrayBuffer(getPackLength());
		
		//拷贝包长度
		byte[] tempByte_Length=UtilDataFormatChange.shortToByteArray(getPackLength());
		byteArray.append(tempByte_Length,0,tempByte_Length.length);

		//拷贝校验位
		byte[] tempByte_CheckNum=UtilDataFormatChange.shortToByteArray(getPackageCheckNum());
		byteArray.append(tempByte_CheckNum,0,tempByte_CheckNum.length);

		//拷贝序号
		byte[] tempByte_Sequence=UtilDataFormatChange.shortToByteArray(this.packageSequence);
		byteArray.append(tempByte_Sequence,0,tempByte_Sequence.length);
		
		//拷贝SessionID
		byte[] tempByte_SessionID=UtilDataFormatChange.shortToByteArray(this.packageSessionID);
		byteArray.append(tempByte_SessionID,0,tempByte_SessionID.length);
		
		//拷贝设备类型
		byte[] tempByte_EquipType={this.packageEquipType};
		byteArray.append(tempByte_EquipType,0,tempByte_EquipType.length);
		
		//拷贝预留
		byte[] tempByte_Reserve={this.packageReserve};
		byteArray.append(tempByte_Reserve,0,tempByte_Reserve.length);
		
		//拷贝主类型
		byte[] tempByte_MainType={this.packageMainType};
		byteArray.append(tempByte_MainType,0,tempByte_MainType.length);
		
		if(this.packageSubType!=-1) {
			//拷贝子类型
			byte[] tempByte_SubType={this.packageSubType};
			byteArray.append(tempByte_SubType,0,tempByte_SubType.length);
		}
		
				
		//拷贝内容
		if(this.byteSubContent!=null) {
			byteArray.append(this.byteSubContent,0,this.byteSubContent.length);
		}

		LogUtils.log("TCP接收：Type:"+packageMainType+";SubType："+packageSubType);
		
		return byteArray.toByteArray();
	}

	//得到校验位
	public short getCheckNum() {
		ByteArrayBuffer byteArray=new ByteArrayBuffer(getPackLength());
		
		//拷贝序号
		byte[] tempByte_Sequence = UtilDataFormatChange.shortToByteArray(this.packageSequence);
		byteArray.append(tempByte_Sequence,0,tempByte_Sequence.length);
		
		//拷贝SessionID
		byte[] tempByte_SessionID=UtilDataFormatChange.shortToByteArray(this.packageSessionID);
		byteArray.append(tempByte_SessionID,0,tempByte_SessionID.length);
		
		//拷贝设备类型
		byte[] tempByte_EquipType={this.packageEquipType};
		byteArray.append(tempByte_EquipType,0,tempByte_EquipType.length);
		
		//拷贝预留
		byte[] tempByte_Reserve={this.packageReserve};
		byteArray.append(tempByte_Reserve,0,tempByte_Reserve.length);
		
		//拷贝主类型
		byte[] tempByte_MainType={this.packageMainType};
		byteArray.append(tempByte_MainType,0,tempByte_MainType.length);
		
		if(this.packageSubType!=-1) {
			//拷贝子类型
			byte[] tempByte_SubType={this.packageSubType};
			byteArray.append(tempByte_SubType,0,tempByte_SubType.length);
		}
			
		//拷贝内容
		if(this.byteSubContent!=null) {
			byteArray.append(this.byteSubContent,0,this.byteSubContent.length);
		}

		return LTE_CRC.xcrc(byteArray.toByteArray());
	}
	
}
