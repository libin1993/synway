package com.doit.net.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by wiker on 2016/4/26.
 */
@Table(name = "UeidInfo")
public class DBUeidInfo {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "tmsi")
    private String tmsi;

    @Column(name = "createDate")
    private long createDate;  //为后续查找使用整形绝对时间

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "latitude")
    private String latitude;

    //协议外
    @Column(name = "location")  //归属地
    private String location = "";

    public DBUeidInfo() {
    }

    public DBUeidInfo(String imsi, String msisdn, String tmsi, long createDate, String longitude, String latitude) {
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.tmsi = tmsi;
        this.createDate = createDate;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getTmsi() {
        return tmsi;

    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getLocation() {
        return location;
    }

    public void setTmsi(String tmsi) {
        this.tmsi = tmsi;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}
