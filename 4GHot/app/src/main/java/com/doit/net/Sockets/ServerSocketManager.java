/**
 * 此类的作用管理服务端的Socket
 */

package com.doit.net.Sockets;

import com.doit.net.Data.DataCenterManager;
import com.doit.net.Data.LTESendManager;
import com.doit.net.Utils.LogUtils;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

public class ServerSocketManager {
    //存放服务端的socket
    private Hashtable<String, UtilServerSocket> htServerSockets = new Hashtable<String, UtilServerSocket>();

    //事件toUI
    private IServerSocketChange iServerSocketChangeToUI = null;

    //设置监听事件
    public void setUIServerSocketListener(IServerSocketChange tempListener) {
        iServerSocketChangeToUI = tempListener;
    }


    //单例对象
    private static ServerSocketManager INSTANCE = null;

    //获取单例对象
    public static ServerSocketManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerSocketManager();
        }
        return INSTANCE;
    }

    /**
     * 新建一个ServerSocket
     *
     * @param port
     * @throws IOException
     */
    public boolean newServerSocket(int port) {
        boolean backResult = true;
        UtilServerSocket utilServerSocket = new UtilServerSocket(port);
        try {
            LogUtils.log("newServerSocket");
            utilServerSocket.initServerSocket();
            htServerSockets.put(utilServerSocket.monitorPort + "", utilServerSocket);
            backResult = true;
        } catch (IOException ex) {
            utilServerSocket = null;
            backResult = false;
            LogUtils.log("IOException:" + ex.toString());
        }
        return backResult;
    }

    /**
     * 启动监听
     */
    public void startMainListener(String monitorPort) {
        if (htServerSockets.containsKey(monitorPort)) {
            htServerSockets.get(monitorPort).asyncMonitorLink(new IServerSocketChange() {
                @Override
                public void onServerStartListener(String mainSocketTag) {
                    LogUtils.log("onServerStartListener:" + mainSocketTag);
                }

                @Override
                public void onServerReceiveNewLink(String subSocketTag, UtilServerSocketSub utilSocket) {
                    LogUtils.log("onServerReceiveNewLink:" + subSocketTag);
                    if (iServerSocketChangeToUI != null) {
                        iServerSocketChangeToUI.onServerReceiveNewLink(subSocketTag, utilSocket);
                    }
                    //启动返回的连接接收数据
                    StartNewLinkParse(utilSocket);
                }

                @Override
                public void onServerReceiveError(String errorMsg) {
                    LogUtils.log("onServerReceiveError:" + errorMsg);
                }

                @Override
                public void onServerStopLink(String mainSocketTag) {
                    LogUtils.log("onServerStopLink:" + mainSocketTag);
                }
            });
        }
    }

    /**
     * 关闭监听
     *
     * @throws IOException
     */
    public void closeMainListener(String monitorPort) {
        Set<String> keys = htServerSockets.keySet();
        for (String str : keys) {
            try {
                htServerSockets.get(str).CloseMainServerSocket();
                //htServerSockets.remove(str);	//同时移除，以便于后续再次建立
            } catch (IOException ex) {
                LogUtils.log("closeListener:" + ex.toString());
            }
        }
    }

    /**
     * 监听端口
     *
     * @param monitorPort
     */
    public UtilServerSocket getServerSocketObject(String monitorPort) {

        if (htServerSockets.containsKey(monitorPort)) {
            return htServerSockets.get(monitorPort);
        }
        return null;
    }



    //启动返回的连接接收数据
    private void StartNewLinkParse(UtilServerSocketSub utilSocket) {

        utilSocket.startAsynReceiveData(new IServerSocketSubChange() {
            @Override
            public void onClientStartReceiveData(String remoteIP, String remotePort) {

            }

            @Override
            public void onClientReceiveData(String remoteIP, String remotePort, byte[] bytesReceived, int receiveCount) {

            }

            @Override
            public void onClientReceiveError(String remoteIP, String remotePort, String errorMsg) {

            }

            @Override
            public void onClientStopLink(String remoteIP, String remotePort) {
                //UtilBaseLog.printLog("onClientStopLink... ...x");
                LTESendManager.currentLocalAddress = "";
                removeSubSocket(remoteIP, remotePort);
                DataCenterManager.clearDataBuffer(remoteIP);
                //getServerSocketObject(remotePort).CloseSubSocket(remoteIP+":"+remotePort);
            }
        });
    }

    //从容器中删除链接
    private void removeSubSocket(String remoteIP, String remotePort) {
        UtilServerSocket utilServerSocket = getServerSocketObject(NetConfig.MONITOR_PORT);
        if (utilServerSocket != null) {
            utilServerSocket.CloseSubSocket(remoteIP + ":" + remotePort);
        }
    }


}
