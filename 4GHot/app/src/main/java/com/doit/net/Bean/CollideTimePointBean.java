package com.doit.net.Bean;

/**
 * Created by Zxc on 2018/12/17.
 */

public class CollideTimePointBean {
    String time;
    String location;

    public CollideTimePointBean(String time, String location) {
        this.time = time;
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
