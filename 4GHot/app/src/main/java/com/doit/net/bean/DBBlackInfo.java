package com.doit.net.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;

/**
 * Created by wiker on 2016/4/26.
 */
@Table(name = "BlackList")
public class DBBlackInfo {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "name")
    private String name;

    @Column(name = "remark")
    private String remark;

    @Column(name = "createDate")
    private Date createDate;

    public DBBlackInfo(String imsi, String name, String remark, Date createDate) {
        this.imsi = imsi;
        this.name = name;
        this.remark = remark;
        this.createDate = createDate;
    }

    public DBBlackInfo() {
    }

    private boolean isConn;

    public boolean isConn() {
        return isConn;
    }

    public void setConn(boolean conn) {
        isConn = conn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
