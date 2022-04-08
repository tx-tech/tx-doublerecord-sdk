package com.txt.sl.utils

import android.text.TextUtils
import android.util.Log
import com.txt.sl.TXSdk

/**
 * author ：Justin
 * time ：2021/7/6.
 * des ：
 */
object TxLogUtils {
    private const val DEFAULT_TAG = "Txlog"

    @JvmStatic
    fun d(tag: String?, msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.d(tag, msg!!)
        }

    }

    @JvmStatic
    fun i(tag: String?, msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.i(tag, msg!!)
        }
    }

    @JvmStatic
    fun i(msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.i(DEFAULT_TAG, msg!!)
        }
    }

    @JvmStatic
    fun d(msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            d(
                    DEFAULT_TAG,
                    msg
            )
        }
    }

    @JvmStatic
    fun w(tag: String?, msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.w(tag, msg!!)
        }
    }

    @JvmStatic
    fun e(tag: String?, msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.e(tag, msg!!)
        }
    }

    @JvmStatic
    fun e(msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Log.e(DEFAULT_TAG, msg!!)
        }
    }

    @JvmStatic
    fun w(msg: String?) {
        if (TXSdk.getInstance().isDebug) {
            w(
                    DEFAULT_TAG,
                    msg!!
            )
        }
    }

    /**
     * 这里使用自己分节的方式来输出足够长度的 message
     *
     * @param tag 标签
     * @param msg 日志内容
     */
    @JvmStatic
    fun debugLongInfo(tag: String?, msg: String) {
        if (TXSdk.getInstance().isDebug) {
            var msg = msg
            if (TextUtils.isEmpty(msg)) {
                return
            }
            msg = msg.trim { it <= ' ' }
            var index = 0
            val maxLength = 3500
            var sub: String
            while (index < msg.length) {
                sub = if (msg.length <= index + maxLength) {
                    msg.substring(index)
                } else {
                    msg.substring(index, index + maxLength)
                }
                index += maxLength
                Log.d(tag, sub.trim { it <= ' ' })
            }
        }
    }

    @JvmStatic
    fun debugLongInfo(msg: String) {
        if (TXSdk.getInstance().isDebug) {
            debugLongInfo(
                    DEFAULT_TAG,
                    msg
            )
        }
    }

}