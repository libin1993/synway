/*
 * Copyright (C) 2011-2013 dshine.com
 * All rights reserved.
 * ShangHai Dshine - http://www.dshine.com
 */
package com.doit.net.Constant;

/**
 * @author Wiker Yong Email:<a href="mailto:wikeryong@gmail.com">wikeryong@gmail.com</a>
 * @date 2013-12-13 下午1:35:54
 * @version 1.0-SNAPSHOT
 */
public class URLs {
    
    public static boolean isBak = false;
    
    public static final String HTTP = "http";
    
    /** 商用域名 **/
    public static final String HOST = "http://mrace.bjlxtech.com:9070/m/";
    
    /** 商用域名，备用 **/
    public static final String HOST_BAK = "http://mrace.bjdxdkj.com:9070/m/";
    
    /** 测试域名 **/
    public static final String HOST_TEST = "http://test.dshinepf.com:9070/m/";
    
    
    /** 上传用户异常数据 */
    private static final String UPLOAD_EXCEPTION = "race/k.jsp";
    
    
    
    /** 获取上传用户异常数据接口 */
    public static String getExceptionUrl() {
        if(TestConstant.TEST_MODE){
            return HOST_TEST+UPLOAD_EXCEPTION;
        }
        if(isBak){
            return HOST_BAK + UPLOAD_EXCEPTION;
        }
        return HOST+ UPLOAD_EXCEPTION;
    }
  
}
