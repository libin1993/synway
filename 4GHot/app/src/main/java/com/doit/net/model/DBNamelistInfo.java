package com.doit.net.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Zxc on 2019/5/29.
 */

@Table(name = "NameList")
public class DBNamelistInfo {
    @Column(name = "id", isId = true)
    private int id;

    //NAMELIST_REJECT、NAMELIST_REDIRECT、NAMELIST_BLOCK、NAMELIST_RELEASE、NAMELIST_REST
    @Column(name = "action")
    private String action;

    @Column(name = "list")
    private String list;


}
