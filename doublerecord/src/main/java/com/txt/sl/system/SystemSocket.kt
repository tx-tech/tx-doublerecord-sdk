package com.txt.sl.system

import android.annotation.SuppressLint
import com.txt.sl.config.socket.SocketBusiness
import com.txt.sl.config.socket.SocketConfig
import com.txt.sl.config.socket.SocketI
import com.txt.sl.entity.constant.Constant
import com.txt.sl.entity.constant.SocketEvent
import com.txt.sl.utils.LogUtils
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.EngineIOException
import org.json.JSONObject

/**
 * Created by Justin on 2018/8/8/008 16:31.
 * email：WjqJustin@163.com
 * effect：全局的Socket
 */
open class SystemSocket :  SocketI {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: SystemSocket? = null

        @JvmStatic
        val instance: SystemSocket?
            get() {
                if (singleton == null) {
                    synchronized(SystemSocket::class.java) {
                        if (singleton == null) {
                            singleton =
                                    SystemSocket()
                        }
                    }
                }
                return singleton
            }
    }


    override fun onSocket() {
        mSocket = SocketConfig.instance.getSocket()!!
        mSocket?.apply {
            on(Socket.EVENT_CONNECT, onConnect)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError)
            on(Socket.EVENT_RECONNECT, event_reconnect)
            on(Socket.EVENT_RECONNECTING, event_reconnecting)
            on(Socket.EVENT_RECONNECT_ERROR, event_reconnect_error)
            on(Socket.EVENT_RECONNECT_FAILED, event_reconnect_failed)
            on(Socket.EVENT_RECONNECT_ATTEMPT, event_reconnect_attempt)
            on(SocketEvent.SC_Call_Status, SC_Call_Status)
            on(SocketEvent.agentOnline, agentOnline)

        }


    }

  public  fun setMSG(loginName : String){
      LogUtils.i("setMSG---$loginName")
        val jsonObject = JSONObject()

        jsonObject.put("loginName",loginName)
        mSocket?.emit(SocketEvent.agentOnline, jsonObject)
  }

    override fun offSocket() {
        mSocket?.off(Socket.EVENT_CONNECT, onConnect)

        mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
        mSocket?.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError)
        mSocket?.off(Socket.EVENT_RECONNECT, event_reconnect)
        mSocket?.off(Socket.EVENT_RECONNECTING, event_reconnecting)
        mSocket?.off(Socket.EVENT_RECONNECT_ERROR, event_reconnect_error)
        mSocket?.off(Socket.EVENT_RECONNECT_FAILED, event_reconnect_failed)
        mSocket?.off(Socket.EVENT_RECONNECT_ATTEMPT, event_reconnect_attempt)
        mSocket?.off(Socket.EVENT_PING, event_ping)
        mSocket?.off(Socket.EVENT_PONG, event_pong)
        mSocket?.off(SocketEvent.SC_Call_Status, SC_Call_Status)
        mSocket?.off( SocketEvent.agentOnline, agentOnline)

    }

    override fun disconnectSocket() {
        if (mSocket?.connected()!!)
            mSocket?.disconnect()
    }

    var mSocket: Socket? = null

    override fun connectSocket() {
        this@SystemSocket.onSocket()
        LogUtils.i("SystemSocket", "开始连接socket")
//        receivePongTimeMillis = System.currentTimeMillis()
        mSocket?.connect()

    }



    private val onConnect = Emitter.Listener { args ->
        LogUtils.i("SystemSocket", "onConnect")
        socketStatus?.onConnect()
        receivePongTimeMillis = System.currentTimeMillis()

//        startTimer()
//        sendMSG()
    }
    private val onDisconnect = Emitter.Listener { args ->
        var args0 = args[0] as String
        LogUtils.i("SystemSocket", "onDisconnect:reason$args0")
        Constant.socketConnect = false
        socketStatus?.onDisconnect()
    }
    private val onConnectError = Emitter.Listener { args ->
        Constant.shouldUpload = true
        socketStatus?.onConnectError()

        var args0 = args[0] as EngineIOException

        LogUtils.i("SystemSocket", "Failure：onConnectError+${args0.transport}+${args0.code}")
    }


    private val onConnectTimeoutError = Emitter.Listener { args ->
        Constant.shouldUpload = true
        socketStatus?.onConnectTimeoutError()
        LogUtils.i("SystemSocket", "onConnectTimeoutError")
    }

    //event_reconnect
    private val event_reconnect = Emitter.Listener { args ->

        socketStatus?.event_reconnect()
        LogUtils.i("SystemSocket", "event_reconnect")
    }
    private val event_reconnect_error = Emitter.Listener { args ->
        Constant.shouldUpload = true
        socketStatus?.event_reconnect_error()
        LogUtils.i("SystemSocket", "Failure：event_reconnect_error")
    }


    private val event_reconnect_attempt = Emitter.Listener { args ->

        socketStatus?.event_reconnect_attempt()
        LogUtils.i("SystemSocket", "event_reconnect_attempt")
    }

    private val event_reconnecting = Emitter.Listener { args ->

        socketStatus?.event_reconnecting()
        LogUtils.i("SystemSocket", "event_reconnecting")
    }

    private val event_reconnect_failed = Emitter.Listener { args ->
        socketStatus?.event_reconnect_failed()
        Constant.shouldUpload = true
        LogUtils.i("SystemSocket", "Failure：event_reconnect_failed")
    }


    private val SC_Call_Status = Emitter.Listener { args ->
        val data = args[0] as JSONObject

        socketStatus?.onReceiveMSG(data)
    }
    private val agentOnline = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        socketStatus?.agentOnline(data)
    }


    private val event_ping = Emitter.Listener { args ->
        LogUtils.i("SystemSocket", "event_ping：---${mSocket?.connected()}")
    }


    var eventPongValue: Long? = null
    var receivePongTimeMillis = 0L
    private val event_pong = Emitter.Listener { args ->
        eventPongValue = args[0] as Long

        LogUtils.i("SystemSocket", "event_pong：${args[0] as Long}---${mSocket?.connected()}\n" +
                "$receivePongTimeMillis")
    }


    var socketStatus: SocketBusiness? = null
    fun setonSocketListener(socketStatus: SocketBusiness) {

        this.socketStatus = socketStatus

    }







}