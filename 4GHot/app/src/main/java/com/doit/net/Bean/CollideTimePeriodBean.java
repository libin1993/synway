package com.doit.net.Bean;

/**
 * Created by Zxc on 2018/5/22.
 */

public class CollideTimePeriodBean {
    private String StartTime;
    private String EndTime;

    public CollideTimePeriodBean(String startTime, String endTime) {
        StartTime = startTime;
        EndTime = endTime;
    }

    public String getStartTime() {
        return StartTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }
}
