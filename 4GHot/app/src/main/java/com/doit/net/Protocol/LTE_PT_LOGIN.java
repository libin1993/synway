package com.doit.net.Protocol;

import android.text.TextUtils;

import com.doit.net.Data.LTESendManager;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Utils.LicenceUtils;
import com.doit.net.Utils.UtilDataFormatChange;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTE_PT_LOGIN {
	public static final byte PT_LOGIN=0x01;


	public static final byte LOGIN_APPLY=0x01;
	public static final byte LOGIN_RESP=0x02;


	//回复登录协议
	public static boolean loginResp(LTEReceivePackage receivePackage) {
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

		//设备id,校验证书
		String equipId = UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(),0);
		if (!TextUtils.isEmpty(equipId)){
            String str = equipId.substring(equipId.indexOf("x")+1);
            if (!TextUtils.isEmpty(str) && str.length() == 8 && TextUtils.isEmpty(LicenceUtils.machineID)
					|| !TextUtils.equals(LicenceUtils.machineID,str)){
                LicenceUtils.machineID = str;
                EventAdapter.call(EventAdapter.CHECK_LICENCE);
            }
		}

		//获取整体的包
		byte[] tempSendBytes=sendPackage.getPackageContent();
		return LTESendManager.sendData(tempSendBytes);
	}
}
