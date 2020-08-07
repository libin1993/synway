package com.doit.net.Sockets;

import com.doit.net.Data.LTEDataParse;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.LogUtils;

import org.apache.commons.net.SocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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
    public String currentRemoteAddress = "";       //当前访问ip
    private Map<String, Socket> map = new HashMap<>();


    private ServerSocketUtils() {
        try {
            mServerSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
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
                        String remoteIP = socket.getInetAddress().getHostAddress();  //远程ip
                        int remotePort = socket.getPort();    //远程端口
                        map.put(remoteIP + ":" + remotePort, socket);   //存储socket
                        currentRemoteAddress = remoteIP + ":" + remotePort;   //ip+端口作为key
                        CacheManager.DEVICE_IP = remoteIP;  //当前设备ip

                        if (onSocketChangedListener != null) {
                            onSocketChangedListener.onConnect();
                        }

                        LogUtils.log("TCP收到设备连接,ip：" + remoteIP + "；端口：" + remotePort);


                        ReceiveThread receiveThread = new ReceiveThread(remoteIP, remotePort, onSocketChangedListener);
                        receiveThread.start();


                    } catch (IOException e) {
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
        private OnSocketChangedListener onSocketChangedListener;
        private String remoteIP;
        private int remotePort;

        public ReceiveThread(String remoteIP, int remotePort, OnSocketChangedListener onSocketChangedListener) {
            this.remoteIP = remoteIP;
            this.remotePort = remotePort;
            this.onSocketChangedListener = onSocketChangedListener;
        }

        @Override
        public void run() {
            super.run();
            //数据缓存
            byte[] bytesReceived = new byte[1024];
            //接收到流的数量
            int receiveCount;
            LTEDataParse lteDataParse = new LTEDataParse();
            Socket socket;
            try {
                //获取当前socket
                socket = map.get(remoteIP + ":" + remotePort);
                if (socket == null) {
                    return;
                }

                //获取输入流
                InputStream inputStream = socket.getInputStream();

                //循环接收数据
                while ((receiveCount = inputStream.read(bytesReceived)) != -1) {
                    lteDataParse.parseData(bytesReceived, receiveCount);
                }

                LogUtils.log("socket被关闭，读取长度：" + receiveCount);

            } catch (IOException ex) {
                LogUtils.log("socket异常:" + ex.toString());
            }

            onSocketChangedListener.onDisconnect();
            closeSocket(remoteIP + ":" + remotePort);  //关闭socket
            lteDataParse.clearReceiveBuffer();
        }
    }

    //关闭socket
    public void closeSocket(String ip) {
        Socket socket = map.get(ip);
        if (socket != null && !socket.isClosed()) {
            //关闭输入流
            try {
                socket.shutdownInput();
                socket.close();//临时
                map.remove(ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 发送数据
     *
     * @param tempByte
     * @return
     */
    public void sendData(byte[] tempByte) {

        Socket socket = map.get(currentRemoteAddress);
        if (socket != null && socket.isConnected()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(tempByte);
                        outputStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.log("socket发送失败：" + e.getMessage());
                    }
                }
            }).start();
        }

    }

}
