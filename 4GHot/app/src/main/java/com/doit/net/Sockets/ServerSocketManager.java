/**
 * 此类的作用管理服务端的Socket
 */

package com.doit.net.Sockets;

import com.doit.net.Data.DataCenterManager;
import com.doit.net.Data.LTESendManager;
import com.doit.net.Utils.UtilBaseLog;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

public class ServerSocketManager
{
	//存放服务端的socket
	Hashtable<String,UtilServerSocket> htServerSockets=new Hashtable<String, UtilServerSocket>();
		
	//事件toUI
	IServerSocketChange iServerSocketChangeToUI=null;
	IServerSocketSubChange iClientSocketChangeToUI=null;
	
	//设置监听事件
	public void setUIServerSocketListener(IServerSocketChange tempListener) {
		iServerSocketChangeToUI = tempListener;
	}
	//设置监听事件
	public void setUIClientSocketListener(IServerSocketSubChange tempListener)
	{
		iClientSocketChangeToUI=tempListener;
	}
	
	//单例对象
  	static ServerSocketManager INSTANCE=null;
    //获取单例对象
  	public static ServerSocketManager getInstance()
  	{
  		if(INSTANCE==null)
  		{
  			INSTANCE=new ServerSocketManager();
  		}
  		return INSTANCE;
  	}
	
  	/**
  	 * 新建一个ServerSocket
  	 * @param config
  	 * @throws IOException
  	 */
	public boolean newServerSocket(ServerSocketConfig config) {
		boolean backResult=true;
		UtilServerSocket utilServerSocket=new UtilServerSocket(config);
		try {
			UtilBaseLog.printLog("newServerSocket");
			utilServerSocket.initServerSocket();
			htServerSockets.put(utilServerSocket.monitorPort+"",utilServerSocket);
			backResult = true;
		} catch (IOException ex) {
			utilServerSocket=null;
			backResult=false;
			UtilBaseLog.printLog("IOException:"+ex.toString());
		}
		return backResult;
	}
	
	/**
	 * 启动监听
	 */
	public void startMainListener(String monitorPort) {
		if(htServerSockets.containsKey(monitorPort)) {
			htServerSockets.get(monitorPort).AsynMonitorLink(new IServerSocketChange() {
				@Override
				public void onServerStartListener(String mainSocketTag) {
					UtilBaseLog.printLog("onServerStartListener:"+mainSocketTag);
				}

				@Override
				public void onServerReceiveNewLink(String subSocketTag, UtilServerSocketSub utilSocket) {
					UtilBaseLog.printLog("onServerReceiveNewLink:"+subSocketTag);
					if(iServerSocketChangeToUI != null) {
						iServerSocketChangeToUI.onServerReceiveNewLink(subSocketTag,utilSocket);
					}
					//启动返回的连接接收数据
					StartNewLinkParse(utilSocket);
				}

				@Override
				public void onServerReceiveError(String errorMsg) {
					UtilBaseLog.printLog("onServerReceiveError:"+errorMsg);
				}

				@Override
				public void onServerStopLink(String mainSocketTag) {
					UtilBaseLog.printLog("onServerStopLink:"+mainSocketTag);
				}
			});
		}
	}
	
	/**
	 * 关闭监听
	 * @throws IOException
	 */
	public void closeMainListener(String monitorPort)
	{
		Set<String> keys=htServerSockets.keySet();
		for(String str : keys) {
			try {
				htServerSockets.get(str).CloseMainServerSocket();
				//htServerSockets.remove(str);	//同时移除，以便于后续再次建立
			} 
			catch (IOException ex)
			{
				UtilBaseLog.printLog("closeListener:"+ex.toString());
			}
		}
	}
	
	/**
	 * 监听端口
	 * @param monitorPort
	 */
	public UtilServerSocket getServerSocketObject(String monitorPort)
	{

		if(htServerSockets.containsKey(monitorPort))
		{
			return htServerSockets.get(monitorPort);
		}
		return null;
	}
	
	/**
	 * 获取子连接
	 *
	 */
	public UtilServerSocketSub getUtilServerSocketSub(String monitorPort, String localAddress)
	{
		return getServerSocketObject(monitorPort).getClientSocket(localAddress);
	}

	
	//启动返回的连接接收数据
	private void StartNewLinkParse(UtilServerSocketSub utilSocket){
		final String  port = utilSocket.remotePort;

		utilSocket.startAsynReceiveData(new IServerSocketSubChange() {
			@Override
			public void onClientStartReceiveData(String remoteIP, String remotePort) {
				if(iClientSocketChangeToUI!=null) {
					iClientSocketChangeToUI.onClientStartReceiveData(remoteIP, remotePort);
				}
			}

			@Override
			public void onClientReceiveData(String remoteIP, String remotePort, byte[] bytesReceived, int receiveCount) {
				if(iClientSocketChangeToUI!=null) {
					iClientSocketChangeToUI.onClientReceiveData(remoteIP, remotePort, bytesReceived, receiveCount);
				}
			}

			@Override
			public void onClientReceiveError(String remoteIP, String remotePort, String errorMsg) {
				if(iClientSocketChangeToUI!=null) {
					iClientSocketChangeToUI.onClientReceiveError(remoteIP, remotePort, errorMsg);
				}
			}

			@Override
			public void onClientStopLink(String remoteIP, String remotePort) {
				//UtilBaseLog.printLog("onClientStopLink... ...x");
                LTESendManager.currentLocalAddress = "";
				removeSubSocket(remoteIP, remotePort);
				DataCenterManager.clearDataBuffer(remoteIP);
                //getServerSocketObject(remotePort).CloseSubSocket(remoteIP+":"+remotePort);
				if(iClientSocketChangeToUI!=null) {
					iClientSocketChangeToUI.onClientStopLink(remoteIP, remotePort);
				}


			}
		});
	}

	//从容器中删除链接
    private void removeSubSocket(String remoteIP, String remotePort) {
        UtilServerSocket utilServerSocket = getServerSocketObject(NetConfig.MONITOR_PORT);
        if (utilServerSocket != null) {
            utilServerSocket.CloseSubSocket(remoteIP+":"+remotePort);
        }
    }


}
