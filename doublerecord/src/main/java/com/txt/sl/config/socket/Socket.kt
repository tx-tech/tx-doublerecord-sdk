package com.txt.sl.config.socket

/**
 * Created by Justin on 2018/8/8/008 16:34.
 * email：WjqJustin@163.com
 * effect：
 */
interface SocketI {

    /*
    * 约定接收指令
    * */
    fun onSocket()

    /*
   * 解约接收指令
   * */
    fun offSocket()

    /**
     * 连接socket
     * */

    fun connectSocket()

    /**
     * 断开socket
     * */
    fun disconnectSocket()


}