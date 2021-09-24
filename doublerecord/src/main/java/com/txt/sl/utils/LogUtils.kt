package com.txt.sl.utils

import android.util.Log
import com.txt.sl.TXSdk

val TAG_NAME = "Txlog"
object LogUtils {

    @JvmStatic
    fun v(msg: String) {
        if (TXSdk.getInstance().isDebug) {
            Log.v(TAG_NAME, msg)
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (TXSdk.getInstance().isDebug) {
            Log.v(TAG_NAME, "$tag \n$msg")
        }
    }


    @JvmStatic
    fun i(msg: String) {
        if (TXSdk.getInstance().isDebug) {
            Log.i(TAG_NAME, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (TXSdk.getInstance().isDebug) {

            Log.i(TAG_NAME, "$tag \n$msg")
        }

    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (TXSdk.getInstance().isDebug) {

            Log.d(TAG_NAME, "$tag \n$msg")
        }

    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (TXSdk.getInstance().isDebug) {

            Log.e(TAG_NAME, "$tag \n$msg")
        }

    }

    @JvmStatic
    fun d(msg: String) {
        if (TXSdk.getInstance().isDebug) {

            Log.d(TAG_NAME, msg)
        }

    }



}

fun logD(msg :String){
    Log.d("Txlog", msg)
}
