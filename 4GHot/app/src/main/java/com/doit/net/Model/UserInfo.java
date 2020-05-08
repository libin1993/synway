package com.doit.net.Model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;

/**
 * Created by Zxc on 2018/11/26.
 */

@Table(name = "UserInfo")
public class UserInfo {
    @Column(name = "id", isId = true)
    private int id;

    @Column(name = "account")
    private String account;

    @Column(name = "password")
    private String password;

    @Column(name = "remake")
    private String remake;


    public UserInfo() {
    }

    public UserInfo(String account, String remake, String password) {
        this.account = account;
        this.remake = remake;
        this.password = password;
    }


    public String getAccount() {
        return account;
    }

    public String getRemake() {
        return remake;
    }

    public String getPassword() {
        return password;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRemake(String type) {
        this.remake = type;
    }
}


