package com.doit.net.Data;

import android.util.Log;

import com.doit.net.Protocol.LTE_PT_LOGIN;
import com.doit.net.Protocol.LTE_PT_PARAM;
import com.doit.net.Protocol.LTE_PT_SYSTEM;
import com.doit.net.Protocol.LTEReceivePackage;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.Utils.UtilDataFormatChange;

/**
 * Created by Zxc on 2018/10/18.
 */
import java.util.ArrayList;

public class LTEDataParse {
	//将字节数暂存
	ArrayList<Byte> listReceiveBuffer = new ArrayList<Byte>();
	//包头的长度
	short packageHeadLength=12;
   
	//解析数据
	public void parseData(String IP, String port, byte[] bytesReceived, int receiveCount) {
		//将接收到数据存放在列表中
		for (int i = 0; i < receiveCount; i++) {
			listReceiveBuffer.add(bytesReceived[i]);
		}
		//得到当前缓存中的长度
		int listReceiveCount = listReceiveBuffer.size();
		
		//如果缓存长度小于12说明最小包都没有收完整
		if(listReceiveCount>=packageHeadLength) {
			parseData(listReceiveCount);
		}
	}
	
	//将收到的字节数解析并且组成一个整包
	private void parseData(int listReceiveCount) {
		//循环读取包
		for (int i = 0; i < listReceiveCount; i++) {
			if (listReceiveBuffer.size() <10 ) {
				UtilBaseLog.printLog("GTW丢包-丢字节");
				break;
			}
			//取出长度
			byte[] byteLength={listReceiveBuffer.get(i),listReceiveBuffer.get(i+1)};
			short packageLength =getShortData(byteLength[0],byteLength[1]);
			//UtilBaseLog.printLog("LTE组包的长度:"+packageLength);
			//判断缓存列表中的数据是否达到一个包的数据
			if(listReceiveBuffer.size()<packageLength) {
				UtilBaseLog.printLog("LTE没有达到整包数");
				break;
			}
			
			byte[] tempPackage=new byte[packageLength];
			//取出一个整包
			for(int j=0;j<packageLength;j++) {
				tempPackage[j]=listReceiveBuffer.get(j);
			}
			
			//删除内存列表中的数据
			for (int j = 0; j < packageLength; j++) {
				listReceiveBuffer.remove(0);
			}
			
			//解析包
			parsePackageData(tempPackage);

			//获取缓存列表中的数据
			listReceiveCount = listReceiveBuffer.size();
			//如果有余下的字节数,则说明有余包
			if (listReceiveCount >=packageHeadLength) {
				UtilBaseLog.printLog("LTE余下的字节数:"+listReceiveCount);
				i=-1;
			}else {
				break;
			}
		}
	}


	//解析成包数据
	private void parsePackageData(byte[] tempPackage)
	{
		if(tempPackage.length < 11)
			return;

		LTEReceivePackage receivePackage = new LTEReceivePackage();
		
    	//第一步取出包的长度
		short packageLength =getShortData(tempPackage[0],tempPackage[1]);
		receivePackage.setPackageLength(packageLength);
		//UtilBaseLog.printLog("LTE收到(packageLength):"+receivePackage.getPackageLength());
		
		//第二步取出CheckNum
		short packageCheckNum =getShortData(tempPackage[2],tempPackage[3]);
		receivePackage.setPackageCheckNum(packageCheckNum);
		//UtilBaseLog.printLog("LTE收到(packageCheckNum):"+packageCheckNum);
		
		//第三步取出序号
		short packageSequence =getShortData(tempPackage[4],tempPackage[5]);
		receivePackage.setPackageSequence(packageSequence);
		//UtilBaseLog.printLog("LTE收到(packageSequence):"+packageSequence);
		
		//第四步取出SessionID
		short packageSessionID =getShortData(tempPackage[6],tempPackage[7]);
		receivePackage.setPackageSessionID(packageSessionID);
		//UtilBaseLog.printLog("LTE收到(packageSessionID):"+receivePackage.getPackageSessionID());
		
		//第五步取出主协议类型EquipType
		byte packageEquipType = tempPackage[8];
		receivePackage.setPackageEquipType(packageEquipType);
		//UtilBaseLog.printLog("LTE收到(packageEquipType):"+receivePackage.getPackageEquipType());
		
		//第六步取出预留类型Reserve
		byte packageReserve = tempPackage[9];
		receivePackage.setPackageReserve(packageReserve);
		//UtilBaseLog.printLog("LTE收到(packageReserve):"+receivePackage.getPackageReserve());
		
		//第七步取出主协议类型MainType
		byte packageMainType = tempPackage[10];
		receivePackage.setPackageMainType(packageMainType);
		//UtilBaseLog.printLog("LTE收到(packageMainType):"+receivePackage.getPackageMainType());
		
		//第八步取出主协议类型Type
		byte packageSubType = tempPackage[11];
		receivePackage.setPackageSubType(packageSubType);
		UtilBaseLog.printLog("LTE收到(packageSubType):"+receivePackage.getPackageSubType());
		
		//第九部取出内容
		//1.计算子协议内容包的长度
		int subPacketLength=packageLength-packageHeadLength;
		byte[] byteSubContent = new byte[subPacketLength];
		//2.取出子协议内容
		if(subPacketLength>0) {
			for(int j=0;j<byteSubContent.length;j++) {
				byteSubContent[j]=tempPackage[packageHeadLength+j];
			}
		}
		receivePackage.setByteSubContent(byteSubContent);

		//解析包数据交由上层处理
		parsePackageEvent(receivePackage);
	}

	
		
	// 解析包数据
	public void parsePackageEvent(LTEReceivePackage receivePackage) {
		//实时解析协议
		realTimeRespose(receivePackage);
	}
	
	//实时回复协议
	public void realTimeRespose(LTEReceivePackage receivePackage) {
		switch(receivePackage.getPackageMainType()) {

			case LTE_PT_LOGIN.PT_LOGIN:
				LTE_PT_LOGIN.parseLoginApply(receivePackage);
				break;

			case LTE_PT_SYSTEM.PT_SYSTEM:
			    switch (receivePackage.getPackageSubType()){
                    case LTE_PT_SYSTEM.SYSTEM_REBOOT_ACK:
					case LTE_PT_SYSTEM.SYSTEM_SET_DATETIME_ASK:
					case LTE_PT_SYSTEM.SYSTEM_UPGRADE_ACK:
					case LTE_PT_SYSTEM.SYSTEM_GET_LOG_ACK:
						LTE_PT_SYSTEM.processCommonSysResp(receivePackage);
						break;
                }

				break;
			case LTE_PT_PARAM.PT_PARAM:
                switch (receivePackage.getPackageSubType()){
                    case LTE_PT_PARAM.PARAM_SET_ENB_CONFIG_ACK:
                    case LTE_PT_PARAM.PARAM_SET_CHANNEL_CONFIG_ACK:
                    case LTE_PT_PARAM.PARAM_SET_CHANNEL_ON_ACK:
                    case LTE_PT_PARAM.PARAM_SET_BLACK_NAMELIST_ACK:
                    case LTE_PT_PARAM.PARAM_SET_RT_IMSI_ACK:
                    case LTE_PT_PARAM.PARAM_SET_CHANNEL_OFF_ACK:
                    case LTE_PT_PARAM.PARAM_SET_FTP_CONFIG_ACK:
                    case LTE_PT_PARAM.PARAM_CHANGE_TAG_ACK:
                    case LTE_PT_PARAM.PARAM_SET_NAMELIST_ACK:
                    case LTE_PT_PARAM.PARAM_CHANGE_BAND_ACK:
                    case LTE_PT_PARAM.PARAM_SET_SCAN_FREQ_ACK:
                    case LTE_PT_PARAM.PARAM_SET_FAN_ACK:
                    case LTE_PT_PARAM.PPARAM_SET_LOC_IMSI_ACK:
                    case LTE_PT_PARAM.PARAM_SET_ACTIVE_MODE_ACK:
                    case LTE_PT_PARAM.PARAM_RPT_UPGRADE_STATUS:
                        UtilBaseLog.printLog("设置回复:"+UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(),0));
						LTE_PT_PARAM.processSetResp(receivePackage);
                        break;

                    case LTE_PT_PARAM.PARAM_GET_ENB_CONFIG_ACK:
                        LTE_PT_PARAM.processEnbConfigQuery(receivePackage);
                        break;

					case LTE_PT_PARAM.PARAM_GET_ACTIVE_MODE_ASK:
						UtilBaseLog.printLog("工作模式查询:"+UtilDataFormatChange.bytesToString(receivePackage.getByteSubContent(),0));
                        break;

                    case LTE_PT_PARAM.PARAM_RPT_HEATBEAT:
                        LTE_PT_PARAM.processRPTHeartbeat(receivePackage);
                        break;

					case LTE_PT_PARAM.PARAM_GET_NAMELIST_ACK:
						LTE_PT_PARAM.processNamelistQuery(receivePackage);
						break;
                    case LTE_PT_PARAM.PARAM_RPT_BLACK_NAME:
                        LTE_PT_PARAM.processRptBlackName(receivePackage);
                        break;

                    case LTE_PT_PARAM.PARAM_SET_SCAN_FREQ:
                        LTE_PT_PARAM.processRPTHeartbeat(receivePackage);
                        break;

					case LTE_PT_PARAM.PARAM_RPT_SCAN_FREQ:
						LTE_PT_PARAM.processRPTFreqScan(receivePackage);
						break;
					case LTE_PT_PARAM.RPT_SRSP_GROUP:
						LTE_PT_PARAM.processLocRpt(receivePackage);
						break;
                }

				break;
		}
	}

	//获取short
	private short getShortData(byte tempByte1,byte tempByte2) {
		byte[] tempByteData={tempByte1,tempByte2};
		return UtilDataFormatChange.byteToShort(tempByteData);
	}
	
	public void clearReceiveBuffer(){
	    UtilBaseLog.printLog("clearReceiveBuffer... ...");
		listReceiveBuffer.clear();
	}
}
