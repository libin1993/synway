package com.doit.net.bean;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * Author：Libin on 2020/5/22 13:19
 * Email：1993911441@qq.com
 * Describe：
 */
public class SectionBean extends SectionEntity<DBChannel> {
    public SectionBean(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public SectionBean(DBChannel dbChannel) {
        super(dbChannel);
    }
}
