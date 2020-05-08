package com.doit.net.Protocol;

/**
 * Created by Zxc on 2018/10/18.
 */

public class LTEProtocol {
    public static byte equipType = 57;

    //当前的SequenceID
    private static short currentSequenceID = 0x01;

    //当前的SessionID
    private static short currentSessionID = (short) 32769 ;

    /**
     * 得到会话序号
     */
    public static short getSequenceID() {
        if(currentSequenceID>=0xFFFF) {
            currentSequenceID=0x01;
        }
        currentSequenceID+=1;

        return currentSequenceID;
    }

    /**
     * 得到会话ID
     */
    public static short getSessionID() {
        if(currentSessionID>=65535) {
            currentSessionID=(short) 32769;
        }
        currentSessionID+=1;
        return currentSessionID;
    }
}
