package com.doit.net.Protocol;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTEReceivePackage {
	private short packageLength;//长 2字节
	private short packageCheckNum;//校验位2字节
	private short packageSequence;//包的序号2字节
	private short packageSessionID;//对话ID2字节
	private byte packageEquipType;//主类型1字节
	private byte packageReserve;//预留1字节
	private byte packageMainType;//主类型1字节
	private byte packageSubType;//子类型1字节
	private byte[] byteSubContent;//子协议内容
	public short getPackageLength() 
	{
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
}
