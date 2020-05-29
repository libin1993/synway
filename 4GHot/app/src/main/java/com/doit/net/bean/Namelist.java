package com.doit.net.bean;

/**
 * Created by Zxc on 2018/10/30.
 */

public class Namelist {
    private String mode;
    private String redirectConfig;
    private String namelistReject;
    private String namelistRedirect;
    private String namelistBlock;
    private String namelistRestAciton;
    private String namelistFile;
    private String namelistRelease;

    public String getNamelistRelease() {
        return namelistRelease;
    }

    public void setNamelistRelease(String namelistRelease) {
        this.namelistRelease = namelistRelease;
    }

    public String getNamelistFile() {
        return namelistFile;
    }

    public void setNamelistFile(String namelistFile) {
        this.namelistFile = namelistFile;
    }

    public String getMode() {
        return mode;
    }

    public String getRedirectConfig() {
        return redirectConfig;
    }

    public String getNamelistReject() {
        return namelistReject;
    }

    public String getNamelistRedirect() {
        return namelistRedirect;
    }

    public String getNamelistBlock() {
        return namelistBlock;
    }



    public String getNamelistRestAciton() {
        return namelistRestAciton;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setRedirectConfig(String redirectConfig) {
        this.redirectConfig = redirectConfig;
    }

    public void setNamelistReject(String namelistReject) {
        this.namelistReject = namelistReject;
    }

    public void setNamelistRedirect(String namelistRedirect) {
        this.namelistRedirect = namelistRedirect;
    }

    public void setNamelistBlock(String namelistBlock) {
        this.namelistBlock = namelistBlock;
    }



    public void setNamelistRestAciton(String namelistRestAciton) {
        this.namelistRestAciton = namelistRestAciton;
    }

    @Override
    public String toString() {
        return "Namelist{" +
                "mode='" + mode + '\'' +
                ", redirectConfig='" + redirectConfig + '\'' +
                ", namelistReject='" + namelistReject + '\'' +
                ", namelistRedirect='" + namelistRedirect + '\'' +
                ", namelistBlock='" + namelistBlock + '\'' +
                ", namelistRestAciton='" + namelistRestAciton + '\'' +
                ", namelistFile='" + namelistFile + '\'' +
                ", namelistRelease='" + namelistRelease + '\'' +
                '}';
    }
}
