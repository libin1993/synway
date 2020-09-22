package com.doit.net.Protocol;

/**
 * Author：Libin on 2020/9/17 14:16
 * Email：1993911441@qq.com
 * Describe：消息编号、信息体编号
 * */
public class GSMMsgType {

    public static final byte GET_COMMON = 0x01;  //查询通用消息
    public static final byte GET_COMMON_ACK = 0x11;  //查询通用消息回应
    public static final byte SET_COMMON = 0x02;  //设置通用
    public static final byte SET_COMMON_ACK = 0x12;  //设置通用回应
    public static final byte GET_NB_CELL = 0x03;  //邻小区查询(GSM制式)
    public static final byte GET_NB_CELL_ACK = 0x13;  //邻小区查询回应
    public static final byte GET_TARGET = 0x04;  //目标请求（包含短寻呼，鉴权）
    public static final byte GET_TARGET_ACK = 0x14;  //目标请求（包含短寻呼，鉴权）回应
    public static final byte OPEN_RF = 0x05;  //开启射频
    public static final byte OPEN_RF_ACK = 0x15;  //开启射频回应
    public static final byte CLOSE_RF = 0x06;  //关闭射频
    public static final byte CLOSE_RF_ACK = 0x16;  //关闭射频回应
    public static final byte IMSI_REPORT = 0x18;  //侦码上报
    public static final byte HEART_BEAT_REPORT = 0x3A;  //心跳上报
    public static final byte NB_CELL_REPORT = 0x3B;  //小区上报


    public static final short DEVICE_VERSION = 0x0001;  //软件版本号
    public static final short DEVICE_STATUS = 0x0002;  //运行状态
    public static final short CONFIGURE_MODE = 0x010A;  //配置模式
    public static final short WORK_MODE = 0x010B;  //工作模式
    public static final short CDMA_FCN1 = 0x0120;  //CDMA频点1
    public static final short CDMA_FCN2 = 0x0128;  //CDMA频点2
    public static final short CDMA_FCN3 = 0x0130;  //CDMA频点3
    public static final short CDMA_FCN4 = 0x0138;  //CDMA频点4
    public static final short GSM_FCN = 0x0150;  //GSM频点
    public static final short DOWN_ATTENUATION = 0x0151;  //下行衰减


}
