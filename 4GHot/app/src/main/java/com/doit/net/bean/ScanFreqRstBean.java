package com.doit.net.bean;

/**
 * Created by Zxc on 2019/3/20.
 */

public class ScanFreqRstBean {
    private String ScanFreqRst;
    private boolean isSelected;

    public ScanFreqRstBean(String scanFreqRst, boolean isSelected) {
        this.ScanFreqRst = scanFreqRst;
        this.isSelected = isSelected;
    }

    public String getScanFreqRst() {
        return ScanFreqRst;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setScanFreqRst(String scanFreqRst) {
        this.ScanFreqRst = scanFreqRst;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
