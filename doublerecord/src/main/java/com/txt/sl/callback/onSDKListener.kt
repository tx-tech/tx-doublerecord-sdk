package com.txt.sl.callback

/**
 * author ：Justin
 * time ：2021/7/5.
 * des ：
 */
open interface onSDKListener {
    fun onResultSuccess(result:String)
    fun onResultFail(errCode:Int,errMsg:String)
}