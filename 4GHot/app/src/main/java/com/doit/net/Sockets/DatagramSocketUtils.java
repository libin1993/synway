package com.doit.net.Sockets;

import com.doit.net.Model.CacheManager;
import com.doit.net.Utils.LogUtils;
import com.doit.net.Utils.NetWorkUtils;
import com.doit.net.application.MyApplication;
import com.doit.net.bean.DeviceState;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.doit.net.Sockets.NetConfig.BOALINK_LTE_IP;


/**
 * Author：Libin on 2020/5/26 15:11
 * Email：1993911441@qq.com
 * Describe：udp发送ip
 */
public class DatagramSocketUtils {
    private static DatagramSocketUtils mInstance;
    private boolean isRunning = true; //未收到tcp连接请求就循环发送
    private DatagramSocket mSocket;
    public final static int UDP_PORT = 6070; //设备udp端口
    public final static String SEND_LOCAL_IP = "MSG_SET_XA_IP"; //手机发送ip
    public final static String SEND_LOCAL_IP_ACK = "MSG_SET_XA_IP_ACK"; //手机发送ip设备回复

    private DatagramSocketUtils() {

    }

    public static DatagramSocketUtils getInstance() {
        if (mInstance == null) {
            synchronized (DatagramSocketUtils.class) {
                if (mInstance == null) {
                    mInstance = new DatagramSocketUtils();
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
                mSocket = new DatagramSocket();
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
                    JSONObject jsonReceive = new JSONObject(receiveData);
                    String id = jsonReceive.getString("id");
//                        if (NetConfig.BOALINK_LTE_IP.equals(remoteIP) && remotePort == UDP_PORT
//                                && SEND_LOCAL_IP_ACK.equals(id)) {
//
//                            isSend = false;
//                            socket.close();
//                        }

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
    public void sendData(String data) {
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

        private String data;

        public SendThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            try {
                LogUtils.log("UDP开始发送");

                InetAddress inetAddress = InetAddress.getByName(BOALINK_LTE_IP);
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length,
                        inetAddress, UDP_PORT); //创建要发送的数据包，然后用套接字发送
                mSocket.send(packet); //用套接字发送数据包
                LogUtils.log("udp发送：" + data);
            } catch (Exception e) {
                LogUtils.log("udp发送失败： " + e.toString());
            }
        }
    }
}

