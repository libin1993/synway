package com.doit.net.Sockets;

import com.doit.net.Utils.LogUtils;

import org.json.JSONObject;

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
        isRunning = true;
        if (mSocket == null || mSocket.isClosed()) {
            try {
                mSocket = new DatagramSocket(UDP_LOCAL_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            new ReceiveThread().start();
        }

    }

    public class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (isRunning) {
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    mSocket.receive(dp);
                    String remoteIP = dp.getAddress().getHostAddress();
                    int remotePort = dp.getPort();
                    String receiveData = new String(dp.getData(), 0, dp.getLength());

                    //tcp有设备连接,关闭socket
                    LogUtils.log("udp来自ip为：" + remoteIP + " 端口为：" + remotePort + "的信息为：" + receiveData);
                }

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.log("udp接收失败: " + e.toString());
            }


        }
    }


    /**
     * 发送数据
     */
    public void sendData(byte[] data) {
        init();
        new SendThread(data).start();

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

        private byte[] data;

        public SendThread(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            try {
                LogUtils.log("UDP开始发送");

                InetAddress inetAddress = InetAddress.getByName(NetConfig.REMOTE_GSM_IP);
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        inetAddress, UDP_REMOTE_PORT); //创建要发送的数据包，然后用套接字发送
                mSocket.send(packet); //用套接字发送数据包

                StringBuffer sb = new StringBuffer();
                for (byte b : data) {
                    sb.append(b).append(",");
                }
                LogUtils.log("udp发送内容：" + sb.toString());
            } catch (Exception e) {
                LogUtils.log("udp发送失败： " + e.toString());
            }
        }
    }
}
