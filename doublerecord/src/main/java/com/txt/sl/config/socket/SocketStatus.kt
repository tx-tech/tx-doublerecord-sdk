package com.txt.sl.config.socket

/**
 * Created by Justin on 2018/8/14/014 14:54.
 * email：WjqJustin@163.com
 * effect：
 */
interface SocketStatus {

    fun onConnect(){}

    fun onDisconnect(){}

    fun onConnectError(){}

    fun onConnectTimeoutError(){}

    //event_reconnect
    fun event_reconnect(){}

    fun event_reconnect_error(){}


    fun event_reconnect_attempt(){}

    fun event_reconnecting(){}

    fun event_reconnect_failed(){}



}