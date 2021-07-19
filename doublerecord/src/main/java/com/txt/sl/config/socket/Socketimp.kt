package com.txt.sl.config.socket

import org.json.JSONObject

/**
 * Created by Justin on 2019/1/3/003 16:41.
 * email：WjqJustin@163.com
 * effect：
 */
open class Socketimp: SocketBusiness {
    override fun onConnect() {
    }

    override fun onDisconnect() {
    }


    override fun onConnectError() {
    }

    override fun onConnectTimeoutError() {
    }



    override fun event_reconnect() {
    }

    override fun event_reconnect_error() {
    }



    override fun event_reconnect_attempt() {
    }



    override fun event_reconnecting() {
    }

    override fun event_reconnect_failed() {
    }

    override fun onReceiveMSG(data: JSONObject) {
    }

    override fun agentOnline(data: JSONObject) {

    }
}