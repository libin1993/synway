package com.doit.net.protocol;

/**
 * Author：Libin on 2020/6/5 13:43
 * Email：1993911441@qq.com
 * Describe：
 */
public class GSMProtocol {


    //当前的SequenceID
    private static byte currentSequenceID = 0x01;

    /**
     * 得到会话序号
     */
    public static byte getSequenceID() {
        if(currentSequenceID>=0xFF) {
            currentSequenceID=0x01;
        }
        currentSequenceID+=1;

        return currentSequenceID;
    }

}
