package com.doit.net.Data;

import com.doit.net.Sockets.NetConfig;
import com.doit.net.Sockets.ServerSocketManager;
import com.doit.net.Sockets.ServerSocketUtils;
import com.doit.net.Sockets.UtilServerSocket;
import com.doit.net.Sockets.UtilServerSocketSub;
import com.doit.net.Utils.LogUtils;

/**
 * Created by Zxc on 2018/10/18.
 */
public class LTESendManager {
	//当前连接上的地址
	public static String currentLocalAddress="";
	
	/**
	 * 发送数据
	 * @param tempByte
	 * @return
	 */
	public static boolean sendData(byte[] tempByte) {


		ServerSocketUtils.getInstance().sendData(tempByte);

		return false;
	}
	
	private static UtilServerSocketSub getClientSocket(String monitorPort)
	{
		UtilServerSocket serverSocket = ServerSocketManager.getInstance().getServerSocketObject(monitorPort);
		if(serverSocket != null) {
			return serverSocket.getClientSocket(currentLocalAddress);
		}
		return null;
	}
	
}
