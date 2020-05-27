package com.doit.net.Sockets;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Author：Libin on 2020/5/26 15:11
 * Email：1993911441@qq.com
 * Describe：
 */
public class UdpClient {
    private static UdpClient mInstance;
    private boolean isSend = true; //未收到tcp连接请求就循环发送
    private final static int UDP_PORT = 6070; //设备udp端口
    private final static String SEND_LOCAL_IP = "MSG_SET_XA_IP"; //手机发送ip
    private final static String SEND_LOCAL_IP_ACK = "MSG_SET_XA_IP_ACK"; //手机发送ip设备回复

    private UdpClient() {

    }

    public static UdpClient getInstance() {
        if (mInstance == null) {
            synchronized (UdpClient.class) {
                if (mInstance == null) {
                    mInstance = new UdpClient();
                }
            }
        }
        return mInstance;
    }

    /**
     * @param localIP 本机ip
     * @param tcpPort tcp端口
     */
    public void startUdp(String localIP, int tcpPort) {
        isSend = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    Log.i("libin", "UDP服务器已经启动");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("port", tcpPort);
                    jsonObject.put("ip", localIP);
                    jsonObject.put("id", SEND_LOCAL_IP);
                    jsonObject.put("ok", true);

                    String data = jsonObject.toString();
                    Log.d("libin","udp发送："+ jsonObject.toString());

                    while (isSend) {
                        InetAddress inetAddress = InetAddress.getByName(NetConfig.BOALINK_LTE_IP);
                        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.getBytes().length,
                                inetAddress, UDP_PORT); //创建要发送的数据包，然后用套接字发送
                        socket.send(packet); //用套接字发送数据包
                        Log.d("libin","udp发送："+ jsonObject.toString());


                        byte[] buf = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buf, buf.length);
                        socket.receive(dp);
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
                        if (!TextUtils.isEmpty(ServerSocketUtils.getInstance().currentRemoteAddress)){
                            isSend = false;
                            socket.close();
                        }
                        Log.d("libin", "来自ip为：" + remoteIP + " 端口为：" + remotePort + "的信息为：" + receiveData);
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("libin", "run: " + e.toString());
                }
            }
        }).start();
    }
}

