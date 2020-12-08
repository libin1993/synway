package com.doit.net.utils;

/**
 * Created by Zxc on 2018/7/5.
 */

public class UtilDelay {

    public static void delayMilis(int milis){
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {}
    }
}
