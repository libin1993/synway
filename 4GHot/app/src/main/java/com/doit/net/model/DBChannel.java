package com.doit.net.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Author：Libin on 2020/5/22 10:55
 * Email：1993911441@qq.com
 * Describe：
 */

@Table(name = "channel")
public class DBChannel {

    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "band")
    private String band;

    @Column(name = "fcn")
    private String fcn;

    @Column(name = "is_default")
    private int isDefault;

    @Column(name = "is_check")
    private int isCheck;

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getFcn() {
        return fcn;
    }

    public void setFcn(String fcn) {
        this.fcn = fcn;
    }

    public int isDefault() {
        return isDefault;
    }

    public void setDefault(int aDefault) {
        isDefault = aDefault;
    }

    public int isCheck() {
        return isCheck;
    }

    public void setCheck(int check) {
        isCheck = check;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DBChannel(String band, String fcn, int isDefault, int isCheck) {

        this.band = band;
        this.fcn = fcn;
        this.isDefault = isDefault;
        this.isCheck = isCheck;
    }

    public DBChannel() {
    }
}
