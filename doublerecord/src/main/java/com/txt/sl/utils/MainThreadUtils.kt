package com.txt.sl.utils

import android.os.Handler
import android.os.Looper

/**
 * author ：Justin
 * time ：2021/7/12.
 * des ： 切换到主线程
 */
public object MainThreadUtil {

    private val HANDLER = Handler(Looper.getMainLooper())

    public fun run (runnable : Runnable){
        if (isMainThread()){
            runnable.run()
        }else{
            HANDLER.post(runnable)
        }

    }

    public fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

}