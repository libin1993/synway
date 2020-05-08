package com.doit.net.Protocol;

import com.doit.net.Data.LTESendManager;
import com.doit.net.Utils.UtilBaseLog;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTE_PT_LOGIN {
	public static final byte PT_LOGIN=0x1;
	public static final byte LOGIN_APPLY=0x1;
	public static final byte LOGIN_RESP=0x2;

	public static void parseLoginApply(LTEReceivePackage receivePackage) {
		LoginResp(receivePackage);
	}

	//回复登录协议
	public static boolean LoginResp(LTEReceivePackage receivePackage) {
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
		sendPackage.setPackageMainType(PT_LOGIN);
		//设置子类型
		sendPackage.setPackageSubType(LOGIN_RESP);
		//设置内容
		sendPackage.setByteSubContent(new byte[]{(byte) 0xFF});
		//设置校验位
		sendPackage.setPackageCheckNum(sendPackage.getCheckNum());
		
		//获取整体的包
		byte[] tempSendBytes=sendPackage.getPackageContent();
		return LTESendManager.sendData(tempSendBytes);
	}
}
