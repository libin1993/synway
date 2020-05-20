package com.doit.net.Sockets;

import com.doit.net.Data.DataCenterManager;
import com.doit.net.Data.LTESendManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.bean.DeviceState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;


/**
 * Author：Libin on 2020/5/20 15:43
 * Email：1993911441@qq.com
 * Describe：
 */
public class ServerSocketUtils {
    private final static int PORT = 7003;
    private  ServerSocket serverSocket;
    private final static int READ_TIME_OUT=60000;
    private String currentRemoteAddress="";
    private Map<String, Socket> map = new HashMap<>();
    //单例对象
    private static ServerSocketUtils mInstance;
    private OnSocketChangedListener onSocketChangedListener;


    private ServerSocketUtils() {
        try {
            serverSocket = new ServerSocket(PORT);
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


    public void startServer(OnSocketChangedListener onSocketChangedListener) {
        this.onSocketChangedListener = onSocketChangedListener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true){
                        Socket socket = serverSocket.accept();
                        String remoteIP =socket.getInetAddress().getHostAddress();
                        int remotePort = socket.getPort();
                        map.put(remoteIP+":"+remotePort,socket);
                        currentRemoteAddress = remoteIP+":"+remotePort;
                        CacheManager.DEVICE_IP = remoteIP;

                        onSocketChangedListener.onConnect();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //数据缓存
                                byte[] bytesReceived = new byte[1024];
                                //接收到流的数量
                                int receiveCount;
                                try {
                                    //设置超时
                                    Socket socket1 = map.get(currentRemoteAddress);

                                    socket1.setSoTimeout(READ_TIME_OUT);
                                    //获取输入流
                                    InputStream netInputStream = socket1.getInputStream();

                                    while (true) {
                                        //读取服务端发送给客户端的数据
                                        receiveCount = netInputStream.read(bytesReceived, 0, bytesReceived.length);
                                        LogUtils.log(receiveCount+"");
                                        if (receiveCount <= -1) {
                                            LogUtils.log("break read!");
                                            break;
                                        }
                                        //将数据交给数据中心管理员处理
                                        DataCenterManager.parseData(remoteIP, String.valueOf(remotePort),
                                                bytesReceived, receiveCount);
                                        //收到数据
                                    }
                                } catch (IOException ex) {
                                    LogUtils.log("UtilSocket Error:" + ex.toString());
                                    onSocketChangedListener.onDisconnect();

                                    currentRemoteAddress = "";
                                    closeSocket(remoteIP+":"+remotePort);
                                    DataCenterManager.clearDataBuffer(remoteIP);
                                }

                            }
                        }).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //关闭socket
    public void closeSocket(String ip) {
        Socket socket = map.get(ip);
        if (socket != null && !socket.isClosed()) {
            //关闭输入流
            try {
                socket.shutdownInput();
                socket.close();//临时
                socket = null;//临时
                map.remove(ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 发送数据
     * @param tempByte
     * @return
     */
    public  void sendData(byte[] tempByte) {
        StringBuffer sb= new StringBuffer();
        for (byte b : tempByte) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            sb.append(hv).append(",");
        }
        LogUtils.log("sendStr:"+sb.toString());

        Socket socket = map.get(currentRemoteAddress);
        if (socket !=null){
            new Thread() {
                @Override
                public void run() {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(tempByte);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.start();
        }

    }

}
