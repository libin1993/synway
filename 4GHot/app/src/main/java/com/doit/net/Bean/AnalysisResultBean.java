package com.doit.net.Bean;

/**
 * Created by Zxc on 2018/11/22.
 */

public class AnalysisResultBean {
    private String imsi;
    private String times;

    public AnalysisResultBean(String imsi, String times) {
        this.imsi = imsi;
        this.times = times;
    }


    public String getImsi() {
        return imsi;
    }

    public String getTimes() {
        return times;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void setTimes(String times) {
        this.times = times;
    }
}

