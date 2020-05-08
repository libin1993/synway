package com.doit.net.Bean;

/**
 * Created by Zxc on 2018/10/19.
 */

public class LteChannelCfg {
    //这里是协议里直接有的
    private String idx;
    private String band;
    private String fcn;
    private String plmn;
    private String pa;
    private String ga;
    private String pw;
    private String rlm;
    private String change = "";
    private String autoopen;
    private String alt_fcn;
    private String pmax;

    //这条协议之外的值
   private boolean RFState;

    //这里暂时还不知道什么意思
    private Integer state;
    public Boolean hold;
    public String info;
    private int[] fcns;
    private int[] pas;
    private float[] pwrs;
    private boolean result;

    public LteChannelCfg() {
    }

    public String getIdx() {
        return this.idx;
    }
    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getBand() {
        return this.band;
    }
    public void setBand(String band) {
        this.band = band;
    }

    public String getFcn() {
        return this.fcn;
    }
    public void setFcn(String fcn) {
        this.fcn = fcn;
    }

    public String getPlmn() {
        return this.plmn;
    }
    public void setPlmn(String plmn) {
        this.plmn = plmn;
    }

    public String getPa() {
        return this.pa;
    }
    public void setPa(String pa) {
        this.pa = pa;
    }

    public boolean getState() {
        return this.RFState;
    }
    public void setState(boolean state) {
        this.RFState = state;
    }

    public String getGa() {
        return this.ga;
    }
    public void setGa(String ga) {
        this.ga = ga;
    }

    public String getPw() {
        return this.pw;
    }
    public void setPw(String pw) {
        this.pw = pw;
    }

    public boolean isRfOpen() {
        return this.state == null?false:this.state.intValue() == 1;
    }

    public String getPMax() {
        return pmax;
    }

    public void setPMax(String max) {
        this.pmax = max;
    }

    public Boolean getHold() {
        return this.hold;
    }
    public void setHold(Boolean hold) {
        this.hold = hold;
    }

    public boolean isResult() {
        return this.result;
    }
    public void setResult(boolean result) {
        this.result = result;
    }

    public String getRlm() {
        return this.rlm;
    }
    public void setRlm(String rxlevmin) {
        this.rlm = rxlevmin;
    }

    public String getInfo() {
        return this.info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public int[] getFcns() {
        return this.fcns;
    }
    public void setFcns(int[] fcns) {
        this.fcns = fcns;
    }

    public int[] getPas() {
        return this.pas;
    }
    public void setPas(int[] pas) {
        this.pas = pas;
    }

    public String getFcnsString() {
        if(this.fcns == null) {
            return null;
        } else {
            StringBuilder s = new StringBuilder();
            int[] var2 = this.fcns;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                int fcn = var2[var4];
                s.append(fcn + ",");
            }

            if(s.length() > 0) {
                s.delete(s.length() - 1, s.length());
            }

            return s.toString();
        }
    }

    public float[] getPwrs() {
        return this.pwrs;
    }
    public void setPwrs(float[] pwrs) {
        this.pwrs = pwrs;
    }


    public Boolean getRFState() {
        return this.RFState;
    }
    public void setRFState(Boolean RFState) {
        this.RFState = RFState;
    }

    public String getChangeBand() {
        return change;
    }

    public void setChangeBand(String change) {
        this.change = change;
    }

    public String getAutoOpen() {
        return autoopen;
    }

    public void setAutoOpen(String autoopen) {
        this.autoopen = autoopen;
    }

    public String getAltFcn() {
        return alt_fcn;
    }

    public void setAltFcn(String alt_fcn) {
        this.alt_fcn = alt_fcn;
    }
}
