package com.doit.net.sockets;

import com.doit.net.protocol.LTEReceiveManager;
import com.doit.net.model.CacheManager;
import com.doit.net.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;


/**
 * Author：Libin on 2020/5/20 15:43
 * Email：1993911441@qq.com
 * Describe：socket服务端
 */
public class ServerSocketUtils {
    private static ServerSocketUtils mInstance;
    private ServerSocket mServerSocket;

    public final static int PORT = 7003;   //端口
    private final static int READ_TIME_OUT = 60000;  //超时时间
    private Map<String, Socket> map = new HashMap<>();


    private ServerSocketUtils() {
        try {
            mServerSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.log("tcp错误1：" + e.getMessage());
        }
    }

    //获取单例对象
    public static ServerSocketUtils getInstance() {
        if (mInstance == null) {
            synchronized (ServerSocketUtils.class) {
                if (mInstance == null) {
                    mInstance = new ServerSocketUtils();
                }
            }

        }
        return mInstance;
    }


    /**
     * @param onSocketChangedListener 线程接收连接
     */
    public void startTCP(OnSocketChangedListener onSocketChangedListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        Socket socket = mServerSocket.accept();  //获取socket
                        socket.setSoTimeout(READ_TIME_OUT);      //设置超时
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);

                        String remoteIP = socket.getInetAddress().getHostAddress();  //远程ip
                        int remotePort = socket.getPort();    //远程端口

                        LogUtils.log("TCP收到设备连接,ip：" + remoteIP + "；端口：" + remotePort);
                        if (remoteIP.equals(CacheManager.DEVICE_IP)) {
                            map.put(remoteIP, socket);   //存储socket
                            if (onSocketChangedListener != null) {
                                onSocketChangedListener.onChange();
                            }

                            new ReceiveThread(socket,remoteIP).start();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.log("tcp错误：" + e.getMessage());

                    }
                }
            }
        }).start();
    }



    /**
     * 接收线程
     */
    public class ReceiveThread extends Thread {
        private String remoteIP;
        private Socket socket;

        public ReceiveThread(Socket socket,String remoteIP) {
            this.socket = socket;
            this.remoteIP = remoteIP;
        }

        @Override
        public void run() {
            super.run();
            //数据缓存
            byte[] bytesReceived = new byte[1024];
            //接收到流的数量
            int receiveCount;
            LTEReceiveManager lteReceiveManager = new LTEReceiveManager();
            long lastTime = 0; //上次读取时间

            while (true) {
                //获取输入流
                try {
                    InputStream inputStream = socket.getInputStream();
                    //循环接收数据
                    while ((receiveCount = inputStream.read(bytesReceived)) != -1) {
                        lteReceiveManager.parseData(bytesReceived, receiveCount);
                        lastTime = System.currentTimeMillis();
                    }
                    LogUtils.log(remoteIP + "：socket异常，读取长度：" + receiveCount);
                    closeSocket(socket);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.log(remoteIP + "：socket异常:" + e.toString());
                    if (e instanceof SocketTimeoutException) {
                        //捕获读取超时异常，若超时时间内收到数据，不做处理

                        long timeout = System.currentTimeMillis() - lastTime;
                        LogUtils.log(remoteIP + "：socket异常:读取超时" + timeout);
                        if (timeout > READ_TIME_OUT) {
                            closeSocket(socket);
                            break;
                        }
                    } else {
                        closeSocket(socket);
                        break;
                    }
                }

            }

        }
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtils.log( "：socket关闭失败:" + ex.toString());
        }
    }


    /**
     * 发送数据
     *
     * @param tempByte
     * @return
     */
    public void sendData(byte[] tempByte) {

        Socket socket = map.get(CacheManager.DEVICE_IP);
        if (socket != null && socket.isConnected()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(tempByte);
                        outputStream.flush();
                        LogUtils.log("TCP发送："+tempByte.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.log("socket发送失败：" + e.getMessage());
                    }
                }
            }).start();
        }else {
            LogUtils.log("socket未连接");
        }

    }

}
