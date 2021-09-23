package com.txt.sl.config.socket


import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import okhttp3.OkHttpClient
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import com.txt.sl.BuildConfig
import com.txt.sl.TXSdk
import com.txt.sl.utils.LogUtils


/**
 * Created by Justin on 2018/7/18/018 14:22.
 * email：WjqJustin@163.com
 * effect：
 */
class SocketConfig private constructor() {
    companion object {
        val instance by lazy { SocketConfig() }
    }

    private var mSocket: Socket? = null
    private var options: IO.Options? = null

    fun getSocket(): Socket? {
        val xtm: X509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }
        val DO_NOT_VERIFY = HostnameVerifier { hostname, session -> true }
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(xtm), SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }
//        val okHttpClient = OkHttpClient.Builder()
//                .hostnameVerifier(DO_NOT_VERIFY)
//                .sslSocketFactory(sslContext?.getSocketFactory(), xtm)
//                .build()

// default settings for all sockets

// default settings for all sockets
//        IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
//        IO.setDefaultOkHttpCallFactory(okHttpClient)
        try {
            options = IO.Options().apply {
                reconnectionAttempts = 15
                forceNew = false
                reconnection = true
//                callFactory = okHttpClient
//                webSocketFactory = okHttpClient

                transports = arrayOf(WebSocket.NAME)
            }
//            options!!.multiplex = false

            var IP = when (TXSdk.getInstance().environment) {
                TXSdk.Environment.DEV ->
                    "https://developer.ikandy.cn:62727"
                TXSdk.Environment.RELEASE ->
                    "https://video-sells.cloud-ins.cn"
                TXSdk.Environment.POC ->
                    "https://doublerecord.cloud-ins.cn"
                else ->
                    "https://new-2record.ikandy.cn"
            }
            LogUtils.i("socket-----$IP")
            mSocket = IO.socket(IP, options)


        } catch (e: URISyntaxException) {
            throw  e
        }

        return mSocket
    }
}