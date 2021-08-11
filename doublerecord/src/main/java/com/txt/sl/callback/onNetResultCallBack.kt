package com.txt.sl.callback

/**
 * author ：Justin
 * time ：2021/7/8.
 * des ：
 */
interface onNetResultCallBack {
    fun onResultSuccess(result : String)
    fun onResultFail(errCode:Int,errMsg:String)
}