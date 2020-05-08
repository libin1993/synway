package com.doit.net.Bean;

/**
 * Created by Zxc on 2018/10/23.
 */

public class LteCellConfig {
    private String gpsOffset;
    private String pci;
    private String ssp;
    private String tacTimer;  //分钟
    private String tacRange;
    private String rejectPLMN;
    //private String[][] rejectPLMN;
    private String sync = "";

    private boolean isSnycComplete = false;

    public String getGpsOffset() {
        return this.gpsOffset;
    }
    public void setGpsOffset(String gpsOffset) {
        this.gpsOffset = gpsOffset;
    }

    public String getPci() {
        return this.pci;
    }
    public void setPci(String pci) {
        this.pci = pci;
    }

    public String getSsp() {
        return this.ssp;
    }
    public void setSsp(String ssp) {
        this.ssp = ssp;
    }

    public String getTacTimer() {
        return tacTimer;
    }

    public String getTacRange() {
        return tacRange;
    }

    public String getRejectPLMN() {
        return rejectPLMN;
    }

    public void setTacTimer(String tacTimer) {
        this.tacTimer = tacTimer;
    }

    public void setTacRange(String tacRange) {
        this.tacRange = tacRange;
    }

    public void setRejectPLMN(String rejectPLMN) {
        this.rejectPLMN = rejectPLMN;
    }

    public void setSnycCompleteFlag(boolean isComplete) {this.isSnycComplete = isComplete;}

    public boolean isSnycComplete(){ return isSnycComplete;}

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }
}
