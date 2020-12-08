package com.doit.net.protocol;

import com.doit.net.utils.FormatUtils;
import com.doit.net.utils.LogUtils;

import java.util.ArrayList;

/**
 * Author：Libin on 2020/9/15 15:21
 * Email：1993911441@qq.com
 * Describe：
 */
public class GSMReceiveManager {
    //将字节数暂存
    private ArrayList<Byte> listReceiveBuffer = new ArrayList<Byte>();
    //包头的长度
    private short packageHeadLength = 8;


    //解析数据
    public synchronized void parseData(String ip,byte[] bytesReceived, int receiveCount) {
        //将接收到数据存放在列表中
        for (int i = 0; i < receiveCount; i++) {
            listReceiveBuffer.add(bytesReceived[i]);
        }

        while (true) {
            //得到当前缓存中的长度
            int listReceiveCount = listReceiveBuffer.size();

            //如果缓存长度小于包头的长度说明最小包都没有收完整
            if (listReceiveCount < packageHeadLength) {
                break;
            }

            //接收到反序数组，取出长度
            byte[] contentLength = {listReceiveBuffer.get(3), listReceiveBuffer.get(2), listReceiveBuffer.get(1), listReceiveBuffer.get(0)};
            int contentLen = FormatUtils.getInstance().byteToInt(contentLength);


            LogUtils.log("分包大小：" + listReceiveBuffer.size() + "," + contentLen);
            //判断缓存列表中的数据是否达到一个包的数据
            if (listReceiveBuffer.size() < contentLen) {
                LogUtils.log("LTE没有达到整包数:");
                break;
            }

            byte[] tempPackage = new byte[contentLen];
            //取出一个整包
            for (int j = 0; j < contentLen; j++) {
                tempPackage[j] = listReceiveBuffer.get(j);
            }

            //删除内存列表中的数据
            if (contentLen > 0) {
                listReceiveBuffer.subList(0, contentLen).clear();
            }

            //解析包
            parsePackageData(ip,tempPackage);
        }
    }


    //解析成包数据
    private void parsePackageData(String ip,byte[] tempPackage) {
        if (tempPackage.length < packageHeadLength)
            return;

        GSMPackage gsmPackage = new GSMPackage();

        gsmPackage.setIp(ip);

        //包长度
        byte[] tempPackageLength = new byte[4];
        System.arraycopy(tempPackage, 0, tempPackageLength, 0, 4);
        FormatUtils.getInstance().reverseData(tempPackageLength);
        int packageLength = FormatUtils.getInstance().byteToInt(tempPackageLength);
        gsmPackage.setMsgLength(packageLength);

        //消息序号
        gsmPackage.setMsgSequence(tempPackage[4]);

        //消息编号
        gsmPackage.setMsgNumber(tempPackage[5]);

        //载波指示
        gsmPackage.setCarrierInstruction(tempPackage[6]);

        //消息参数
        gsmPackage.setMsgParameter(tempPackage[7]);


        int subPackageLength = packageLength - packageHeadLength;
        if (subPackageLength > 0){
            byte[] tempSubContent = new byte[subPackageLength];
            System.arraycopy(tempPackage,8,tempSubContent,0,subPackageLength);
            gsmPackage.setSubContent(tempSubContent);
        }

//        if (packageLength - packageHeadLength > 0){
//            List<GSMSubPackage> subPackageList = new ArrayList<>();
//
//            int subLength = 0;
//            while (subLength < packageLength - packageHeadLength){
//                byte length = tempPackage[packageHeadLength+subLength];
//                GSMSubPackage gsmSubPackage = new GSMSubPackage();
//                //信息体长度
//                gsmSubPackage.setSubMsgLength(length);
//
//                //信息体编号
//                byte[] contentNumber= {tempPackage[packageHeadLength+subLength+2],tempPackage[packageHeadLength+subLength+1]};
//                gsmSubPackage.setSubMsgNumber(FormatUtils.getInstance().byteToShort(contentNumber));
//
//
//                byte[] tempContent = new byte[length - 3];
//                System.arraycopy(tempPackage, packageHeadLength+subLength+3, tempContent, 0, length - 3);
//
//                gsmSubPackage.setSubMsgContent(FormatUtils.getInstance().bytes2StringForUTF(tempContent));
//
//
//                LogUtils.log(gsmSubPackage.toString());
//                subPackageList.add(gsmSubPackage);
//
//                subLength += length;
//
//            }
//
//
//        }


        LogUtils.log("UDP1接收数据"+gsmPackage.toString());

    }


    public void clearReceiveBuffer() {
        LogUtils.log("clearReceiveBuffer... ...");
        listReceiveBuffer.clear();
    }

}

