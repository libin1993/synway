package com.doit.net.Sockets;

import com.doit.net.Data.LTESendManager;
import com.doit.net.Utils.UtilBaseLog;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


public class UtilServerSocket 
{
	//收到新的连接
	private Hashtable<String,UtilServerSocketSub> htSubSocket=new Hashtable<String,UtilServerSocketSub>();
	//监听端口
	public int monitorPort=0;
	//服务Socket实例
	ServerSocket serverSocket=null;
	//监听线程式
	MonitorThreat monitorThreat=null;
	//线程连接
	boolean isThreadLoop=false;
	//监听服务事件
	IServerSocketChange iServerSocketChange=null;
	///private boolean isServerStart = false;
	
	public UtilServerSocket(ServerSocketConfig config) {
		monitorPort=config.listenerPort;
	}
	
	//初始化
	public void initServerSocket() throws IOException {
		serverSocket = new ServerSocket(monitorPort);
	}

	//是否开始
//	public boolean isServerStart(){
//		return isServerStart;
//	}
//
//	public void setServerStartStop(boolean startStop){
//		isServerStart = startStop;
//	}
	
	//开启监听(异步)
	public void AsynMonitorLink(final IServerSocketChange iServerSocketChange)
	{
		this.iServerSocketChange=iServerSocketChange;
		if(!isThreadLoop)
		{
			//连接成功则创建接收线程
			monitorThreat=new MonitorThreat();
			isThreadLoop=true;
			monitorThreat.setName("MonitorThreat:"+monitorPort);
			monitorThreat.start();
		}
	}
	
	/**
	 * 关闭主监听对象
	 * @throws IOException
	 */
	public void CloseMainServerSocket() throws IOException {
		//先关闭所有子对象
		Set<String> keys=htSubSocket.keySet();
		for(String str : keys)
		{
			CloseSubSocket(str);
		}
		
		if(serverSocket!=null)
		{
			serverSocket.close();
			serverSocket=null;
		}
	}
	
	/**
	 * 关闭子连接
	 * IP:端口 例192.168.0.1:3215
	 */
	public void CloseSubSocket(String socketTag)
	{
		if(htSubSocket.containsKey(socketTag))
		{
			htSubSocket.get(socketTag).closeSubLink();
			htSubSocket.remove(socketTag);      //临时
		}

        //UtilBaseLog.printLog("remove socket size = "+htSubSocket.size());
	}
	
	/**
	 * 获取当前连接的连接
	 * @param localAddress
	 * @return
	 */
  	public UtilServerSocketSub getClientSocket(String localAddress)
  	{
  		if(htSubSocket.containsKey(localAddress))
		{
			return htSubSocket.get(localAddress);
		}
		return null;
  	}
	
	
	//监听线程
    public class MonitorThreat extends Thread {
        @Override
        public void run() {
    		try {
    			while(isThreadLoop) {
    				//开始监听新的连接
        			iServerSocketChange.onServerStartListener("ServerSocket开始监听新的连接:"+monitorPort);
        			//获取当前的连接对象
        			Socket socket = serverSocket.accept();
        			//构建新客户端连接对象
        			UtilServerSocketSub utilSocket = new UtilServerSocketSub(socket);

					String remoteIP=socket.getInetAddress().getHostAddress();
					String remotePort=socket.getPort()+"";

					//UtilBaseLog.printLog("accept 返回："+ remoteIP+":"+remotePort );
					/*
					 *  经过大量的测试，发现某些手机（测试机为华为）锁屏一段时间后会将网络断开，当屏幕亮起
					 *  网络恢复重连时，会出现多次重复建立而出现收发错乱。故这里将已经在容器里IP相同的socket
					 *  断开并移出，再将新的socket存入。
					 *  （主动将wifi断开不会出现此问题，而且大多手机不会出现此问题）
					 *  */
//					if (LTESendManager.currentLocalAddress.startsWith(remoteIP)){
//                     	CloseSubSocket(LTESendManager.currentLocalAddress);
//                    }


        			//发送消息收到一个新的连接
        			iServerSocketChange.onServerReceiveNewLink("收到新的连接:"+remoteIP,utilSocket);

        			//将这个新的连接放入列表中
        			htSubSocket.put(remoteIP+":"+remotePort,utilSocket);
                    LTESendManager.currentLocalAddress = remoteIP+":"+remotePort;
        			//UtilBaseLog.printLog("add socket:"+ remoteIP+":"+remotePort + ", size = "+htSubSocket.size());
            	}
    		}
    		catch (IOException ex)
    		{
    			iServerSocketChange.onServerReceiveError("ServerSocket error:"+ex.toString());
    		}
    		finally
    		{
    			iServerSocketChange.onServerStopLink("ServerSocket finally:"+monitorPort);
    			serverSocket=null;
    			isThreadLoop=false;
    		}
        }
    }
	
}
