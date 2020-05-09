package com.doit.net.Sockets;

import com.doit.net.Data.DataCenterManager;
import com.doit.net.Utils.UtilBaseLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UtilServerSocketSub
{
	//远程IP及端口
	public String remoteIP="";
	public String remotePort="";
	//超时时间(如果无数据传送)即断开
	public int outTime=60000;
	
	//连接对象
	Socket socket=null;
	
	boolean threadLoop=false;
	//数据接收线程
	ReciveThreat receiveThread=null;

	//private Lock lock = new ReentrantLock();
	
	//事件
	IServerSocketSubChange iClientSocketChange=null;
	
	public UtilServerSocketSub(Socket socket)
	{
		this.socket=socket;
		remoteIP=socket.getInetAddress().getHostAddress();
		remotePort=socket.getPort()+"";
	}
	
	//线程接收数据
	public void startAsynReceiveData(IServerSocketSubChange iClientSocketChange)
	{
		this.iClientSocketChange=iClientSocketChange;
		if(!threadLoop)
		{
			//连接成功则创建接收线程
			receiveThread=new ReciveThreat();
			threadLoop=true;
			receiveThread.setName("ReciveThreat:"+remoteIP+":"+remotePort);
			receiveThread.start();
		}
	}

	//关闭连接
	public void closeSubLink()
	{
        //lock.lock();
		if(socket!=null && !socket.isClosed())
		{
			///UtilBaseLog.printLog("shutdown socket");
			//关闭输入流
			try 
			{
			    threadLoop = false; //临时
				socket.shutdownInput();
                socket.close();//临时
                socket=null;//临时
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
        //lock.unlock();
	}

	//数据接收线程
    public class ReciveThreat extends Thread
    {
        //接收线程
        @Override
        public void run()
        {
        	//数据缓存
        	byte[] bytesReceived=new byte[1024];
        	//接收到流的数量
        	int receiveCount = 0;
        	//lock.lock();
        	try
        	{
        		iClientSocketChange.onClientStartReceiveData(remoteIP,remotePort);
        		//设置超时
        		socket.setSoTimeout(outTime);
        		//获取输入流
        		InputStream netInputStream=socket.getInputStream();

				while(threadLoop)
	        	{
					//读取服务端发送给客户端的数据
					receiveCount=netInputStream.read(bytesReceived, 0,bytesReceived.length);
					if (receiveCount <= -1)
                    {
                    	UtilBaseLog.printLog("break read!");
                        break;
                    }
					//将数据交给数据中心管理员处理
					DataCenterManager.parseData(remoteIP, remotePort, bytesReceived, receiveCount);
					//收到数据
					iClientSocketChange.onClientReceiveData(remoteIP,remotePort,bytesReceived,receiveCount);
	        	}
        	}
        	catch (IOException ex) {
        		iClientSocketChange.onClientReceiveError(remoteIP,remotePort,"UtilSocket Error:"+ex.toString());
        		UtilBaseLog.printLog("UtilSocket Error:"+ex.toString());
			}
        	finally
        	{

        		//iClientSocketChange.onClientStopLink(remoteIP,remotePort);
        		threadLoop = false;
        		try
        		{
        			if(socket!=null && !socket.isClosed())
        			{
                        UtilBaseLog.printLog("close socket");
                        //socket.shutdownInput(); //
        				socket.close();
    					socket=null;
        			}
				}
        		catch (IOException e)
        		{
					e.printStackTrace();
				}
                iClientSocketChange.onClientStopLink(remoteIP,remotePort);
        	}
        	//lock.unlock();
        }
    }

    //发送数据
  	public synchronized boolean  sendData(final byte[] tempByte) {
		if (socket == null){
			UtilBaseLog.printLog("send failed, socket is null.");
			return false;
		}


		new Thread() {
			@Override
			public void run() {
				try {
					socket.getOutputStream().write(tempByte);
				}
				catch(Exception ex) {
					ex.printStackTrace();}
			}
		}.start();
		return true;
  	}   
}
