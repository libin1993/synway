package com.doit.net.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
/**
 * Created by Zxc on 2018/11/28.
 */

@Table(name = "BlackBox")
public class BlackBoxBean {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "account")
    private String account;

    @Column(name = "operation")
    private String operation;

    @Column(name = "time")
    private long time;  //为后续查找使用整形绝对时间

    public BlackBoxBean(String account, String operation, long time) {
        this.account = account;
        this.operation = operation;
        this.time = time;
    }

    public BlackBoxBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public String getOperation() {
        return operation;
    }

    public long getTime() {
        return time;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

