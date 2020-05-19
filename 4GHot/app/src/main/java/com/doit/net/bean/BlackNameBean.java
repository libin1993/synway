package com.doit.net.bean;

/**
 * Created by Zxc on 2018/10/20.
 */

public class BlackNameBean {
    private String IMSI;
    private String equipId;
    private String longitude;
    private String latitude;

    //协议外
    private String reportTime;

    public BlackNameBean(String IMSI, String equipId, String longitude, String latitude, String reportTime) {
        this.IMSI = IMSI;
        this.equipId = equipId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.reportTime = reportTime;
    }

    public String getIMSI() {
        return IMSI;
    }

    public String getEquipId() {
        return equipId;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public void setEquipId(String equipId) {
        this.equipId = equipId;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }
}
