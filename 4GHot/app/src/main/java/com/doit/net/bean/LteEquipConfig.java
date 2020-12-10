package com.doit.net.bean;

/**
 * Created by Zxc on 2018/10/19.
 */

public class LteEquipConfig {
    private String hw;
    private String sw;
    private String expired;
    private String server1;
    private String server2;
    private String board;
    private String protocol;
    private String protocolType;
    ///private String sync = "";
    private String voltage12V;
    private String voltage28V;
    private String maxFanSpeed;
    private String minFanSpeed;
    private String tempThreshold;


//    private int bandwidth;
//    private boolean result;
//    private boolean needReboot;

    public LteEquipConfig() {
    }

    public String getHw() {
        return hw;
    }

    public String getSw() {
        return sw;
    }

    public String getExpired() {
        return expired;
    }

    public String getServer1() {
        return server1;
    }

    public String getServer2() {
        return server2;
    }

    public String getBoard() {
        return board;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getProtocolType() {
        return protocolType;
    }

//    public String getSync() {
//        return sync;
//    }

    public String getVoltage12V() {
        return voltage12V;
    }

    public String getVoltage28V() {
        return voltage28V;
    }

    public void setHw(String hw) {
        this.hw = hw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public void setServer1(String server1) {
        this.server1 = server1;
    }

    public void setServer2(String server2) {
        this.server2 = server2;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

//    public void setSync(String sync) {
//        this.sync = sync;
//    }

    public void setVoltage12V(String voltage12V) {
        this.voltage12V = voltage12V;
    }

    public void setVoltage28V(String voltage28V) {
        this.voltage28V = voltage28V;
    }

    public String getMaxFanSpeed() {
        return maxFanSpeed;
    }

    public String getMinFanSpeed() {
        return minFanSpeed;
    }

    public String getTempThreshold() {
        return tempThreshold;
    }

    public void setMaxFanSpeed(String maxFanSpeed) {
        this.maxFanSpeed = maxFanSpeed;
    }

    public void setMinFanSpeed(String minFanSpeed) {
        this.minFanSpeed = minFanSpeed;
    }

    public void setTempThreshold(String tempThreshold) {
        this.tempThreshold = tempThreshold;
    }

    @Override
    public String toString() {
        return "LteEquipConfig{" +
                "hw='" + hw + '\'' +
                ", sw='" + sw + '\'' +
                ", expired='" + expired + '\'' +
                ", server1='" + server1 + '\'' +
                ", server2='" + server2 + '\'' +
                ", board='" + board + '\'' +
                ", protocol='" + protocol + '\'' +
                ", protocolType='" + protocolType + '\'' +
                ", voltage12V='" + voltage12V + '\'' +
                ", voltage28V='" + voltage28V + '\'' +
                ", maxFanSpeed='" + maxFanSpeed + '\'' +
                ", minFanSpeed='" + minFanSpeed + '\'' +
                ", tempThreshold='" + tempThreshold + '\'' +
                '}';
    }
}
