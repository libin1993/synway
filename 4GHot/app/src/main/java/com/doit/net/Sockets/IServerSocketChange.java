package com.doit.net.Sockets;


public interface IServerSocketChange
{
	void onServerStartListener(String mainSocketTag);
	void onServerReceiveNewLink(String subSocketTag, UtilServerSocketSub utilSocket);
	void onServerReceiveError(String errorMsg);
	void onServerStopLink(String mainSocketTag);
}
