package com.doit.net.Bean;

/**
 * Created by wiker on 2016-08-15.
 */
public class UeidBean {
    private String imsi;
    private String tmsi;
    private String band;
    private String number;
    private String rptTime;
    private String longitude;
    private String latitude;

    //为管控而加
    private int rptTimes = 1; //上报次数累积
    private String srsp = "";  //最近一次场强

    public UeidBean(){

    }

    public UeidBean(String imsi, String number, String tmsi, String band, String rptTime, String longitude, String latitude) {
        this.imsi = imsi;
        this.number = number;
        this.tmsi = tmsi;
        this.band = band;
        this.rptTime = rptTime;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getImsi() {
        return imsi;
    }

    public String getNumber() {
        return number;
    }

    public String getTmsi() {
        return tmsi;
    }

    public String getBand() {
        return band;
    }

    public String getRptTime() {
        return rptTime;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public void setTmsi(String tmsi) {
        this.tmsi = tmsi;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public void setRptTime(String rptTime) {
        this.rptTime = rptTime;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getRptTimes() {
        return rptTimes;
    }

    public String getSrsp() {
        return srsp;
    }

    public void setRptTimes(int rptTimes) {
        this.rptTimes = rptTimes;
    }

    public void setSrsp(String srsp) {
        this.srsp = srsp;
    }
}
