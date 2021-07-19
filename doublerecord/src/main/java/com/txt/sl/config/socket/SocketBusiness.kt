package com.txt.sl.config.socket

import org.json.JSONObject

/**
 * Created by Justin on 2018/8/14/014 15:38.
 * email：WjqJustin@163.com
 * effect：
 */
interface SocketBusiness : SocketStatus {



    //结束会话
    fun onReceiveMSG(data : JSONObject)

    fun agentOnline(data : JSONObject)

}