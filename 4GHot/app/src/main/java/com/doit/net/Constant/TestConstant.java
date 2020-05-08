/*
 * Copyright (C) 2011-2013 dshine.com
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com
 */
package com.doit.net.Constant;


/**
 * @author Wiker Yong Email:<a href="mailto:wikeryong@gmail.com">wikeryong@gmail.com</a>
 * @date 2013-12-17 上午9:29:53
 * @version 1.0-SNAPSHOT
 */
public class TestConstant {
    
    /** 是否测试模式，全局通用 */
    public static boolean TEST_MODE = false;
    
    /**
     * 是否显示失败提示
     */
    public static boolean SHOW_PAY_FAIL = true;
    
    /** 测试购买是否直接成功，商用时应该 为false */
    public static boolean TEST_BUY_SUCCESS = false; 
    
    /** 是否开启日志，即LogUtils是否打印 */
    public static boolean OPEN_LOG = true;
    
    /** 计费类型  true 是双币种  false 为单币种  */
    public static boolean BILLING_TYPE = false;
}
