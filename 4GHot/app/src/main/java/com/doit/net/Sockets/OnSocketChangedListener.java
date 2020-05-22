package com.doit.net.Sockets;

/**
 * Author：Libin on 2020/5/20 18:27
 * Email：1993911441@qq.com
 * Describe：socket连接状态监听
 */
public interface OnSocketChangedListener {
    void onConnect();
    void onDisconnect();

}
