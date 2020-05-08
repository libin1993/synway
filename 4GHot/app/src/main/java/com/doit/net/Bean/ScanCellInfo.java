package com.doit.net.Bean;

/**
 * Created by Zxc on 2019/6/24.
 */

public class ScanCellInfo {
    private String band;
    private String arfcn;
    private boolean isServiceCell;

    public ScanCellInfo(String band, String arfcn, boolean isServiceCell) {
        this.band = band;
        this.arfcn = arfcn;
        this.isServiceCell = isServiceCell;
    }

    public String getBand() {
        return band;
    }

    public String getArfcn() {
        return arfcn;
    }

    public boolean isServiceCell() {
        return isServiceCell;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public void setArfcn(String arfcn) {
        this.arfcn = arfcn;
    }

    public void setServiceCell(boolean serviceCell) {
        isServiceCell = serviceCell;
    }
}
