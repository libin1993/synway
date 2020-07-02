package com.doit.net.bean;

/**
 * Author：Libin on 2020/6/30 15:11
 * Email：1993911441@qq.com
 * Describe：
 */
public class BatteryBean {
    private boolean isCharging;
    private int batteryQuantity;
    private int useTime;

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    public int getBatteryQuantity() {
        return batteryQuantity;
    }

    public void setBatteryQuantity(int batteryQuantity) {
        this.batteryQuantity = batteryQuantity;
    }

    public int getUseTime() {
        return useTime;
    }

    public void setUseTime(int useTime) {
        this.useTime = useTime;
    }
}
