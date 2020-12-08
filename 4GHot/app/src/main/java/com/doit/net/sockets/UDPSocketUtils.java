package com.doit.net.sockets;

import com.doit.net.protocol.GSMReceiveManager;
import com.doit.net.utils.LogUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Author：Libin on 2020/6/5 10:53
 * Email：1993911441@qq.com
 * Describe：2G设备通信
 */
public class UDPSocketUtils {

    private static UDPSocketUtils mInstance;
    private boolean isRunning = true; //未收到tcp连接请求就循环发送
    private DatagramSocket mSocket;
    public final static int UDP_LOCAL_PORT = 5557; //本机udp端口
    public final static int UDP_REMOTE_PORT = 5558; //设备udp端口
    public final static String REMOTE_GSM_IP = "192.168.1.202";
    public final static String REMOTE_CDMA_IP = "192.168.1.203";


    private UDPSocketUtils() {

    }

    public static UDPSocketUtils getInstance() {
        if (mInstance == null) {
            synchronized (UDPSocketUtils.class) {
                if (mInstance == null) {
                    mInstance = new UDPSocketUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 开启udp socket
     */
    public void init() {
        if (mSocket == null || mSocket.isClosed()) {
            try {
                mSocket = new DatagramSocket(UDP_LOCAL_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.log("UDP1异常: " + e.toString());
            }

            new ReceiveThread().start();
        }

    }

    public class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isRunning) {
                GSMReceiveManager gsmReceiveManager = new GSMReceiveManager();
                try {

                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    mSocket.receive(dp);
                    String remoteIP = dp.getAddress().getHostAddress();
                    int remotePort = dp.getPort();

//                    LogUtils.log("UDP1来自ip为：" + remoteIP + " 端口为：" + remotePort + "的信息为："
//                            + Arrays.toString(dp.getData())+",长度为："+dp.getLength());
                    gsmReceiveManager.parseData(remoteIP, dp.getData(), dp.getLength());

                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.log("UDP1接收失败: " + e.toString());
                    gsmReceiveManager.clearReceiveBuffer();
                }

            }

        }
    }


    /**
     * 发送数据
     */
    public void sendData(byte[] data) {
        sendData(REMOTE_GSM_IP,data);
        sendData(REMOTE_CDMA_IP,data);
    }

    /**
     * 发送数据
     */
    public void sendData(String ip,byte[] data) {
        new SendThread(ip,data).start();
    }


    public void closeSocket() {
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
        }
    }


    /**
     *
     */
    public class SendThread extends Thread {
        private String ip;
        private byte[] data;

        public SendThread(String ip,byte[] data) {
            this.ip = ip;
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            try {
                LogUtils.log("UDP1开始发送");

                InetAddress inetAddress = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        inetAddress, UDP_REMOTE_PORT); //创建要发送的数据包，然后用套接字发送
                mSocket.send(packet); //用套接字发送数据包

                StringBuffer sb = new StringBuffer();
                for (byte b : data) {
                    sb.append(b).append(",");
                }
                LogUtils.log("UDP1发送内容：" + sb.toString());
            } catch (Exception e) {
                LogUtils.log("UDP1发送失败： " + e.toString());
            }
        }
    }
}
