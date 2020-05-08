package com.doit.net.Sockets;

public class ServerSocketConfig 
{
	
	/**
	 * socket监听端口
	 */
	public int listenerPort =0;
	
	/**
	 * 超时的时间(秒)
	 */
	public int timeOut=6000;
	
	public ServerSocketConfig(int listenerPort, int timeOut)
	{
		this.listenerPort = listenerPort;
		this.timeOut=timeOut;
	}
}
