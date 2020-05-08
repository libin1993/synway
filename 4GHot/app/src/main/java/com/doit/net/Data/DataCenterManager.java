package com.doit.net.Data;


import com.doit.net.Sockets.NetConfig;

/**
 * Created by Zxc on 2018/10/18.
 */
public class DataCenterManager {
	//LTE数据解析
	static LTEDataParse lteDataParse = new LTEDataParse();
		
	//解析数据
	public static synchronized void parseData(String IP, String port, byte[] bytesReceived, int receiveCount) {
		if(NetConfig.BOALINK_LTE_IP.equals(IP)) {
			lteDataParse.parseData(IP, port, bytesReceived, receiveCount);
		}
	}

	//清空缓存里的数据
	public static synchronized void clearDataBuffer(String IP) {
		if(NetConfig.BOALINK_LTE_IP.equals(IP)) {
			lteDataParse.clearReceiveBuffer();
		}
	}
	
}
