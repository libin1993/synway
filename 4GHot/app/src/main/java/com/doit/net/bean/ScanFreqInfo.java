package com.doit.net.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Zxc on 2019/4/11.
 */
@Table(name = "ScanFreq")
public class ScanFreqInfo {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "band1_fcn_list")
    private String band1_fcn_list;

    @Column(name = "band3_fcn_list")
    private String band3_fcn_list;

    @Column(name = "band38_fcn_list")
    private String band38_fcn_list;

    @Column(name = "band39_fcn_list")
    private String band39_fcn_list;

    @Column(name = "band40_fcn_list")
    private String band40_fcn_list;

    public ScanFreqInfo(double longitude, double latitude, String band1_fcn_list, String band3_fcn_list, String band38_fcn_list, String band39_fcn_list, String band40_fcn_list) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.band1_fcn_list = band1_fcn_list;
        this.band3_fcn_list = band3_fcn_list;
        this.band38_fcn_list = band38_fcn_list;
        this.band39_fcn_list = band39_fcn_list;
        this.band40_fcn_list = band40_fcn_list;
    }

    public ScanFreqInfo(){
    }

    public int getId() {
        return id;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getBand1FcnList() {
        return band1_fcn_list;
    }

    public String getBand3FcnList() {
        return band3_fcn_list;
    }

    public String getBand38FcnList() {
        return band38_fcn_list;
    }

    public String getBand39FcnList() {
        return band39_fcn_list;
    }

    public String getBand40FcnList() {
        return band40_fcn_list;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setBand1FcnList(String band1_fcn_list) {
        this.band1_fcn_list = band1_fcn_list;
    }

    public void setBand3FcnList(String band3_fcn_list) {
        this.band3_fcn_list = band3_fcn_list;
    }

    public void setBand38FcnList(String band38_fcn_list) {
        this.band38_fcn_list = band38_fcn_list;
    }

    public void setBand39cnList(String band39_fcn_list) {
        this.band39_fcn_list = band39_fcn_list;
    }

    public void setBand40FcnList(String band40_fcn_list) {
        this.band40_fcn_list = band40_fcn_list;
    }
}
