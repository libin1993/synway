package com.doit.net.Sockets;

public interface IServerSocketSubChange
{
	void onClientStartReceiveData(String remoteIP, String remotePort);
	void onClientReceiveData(String remoteIP, String remotePort, byte[] bytesReceived, int receiveCount);
	void onClientReceiveError(String remoteIP, String remotePort, String errorMsg);
	void onClientStopLink(String remoteIP, String remotePort);
}
