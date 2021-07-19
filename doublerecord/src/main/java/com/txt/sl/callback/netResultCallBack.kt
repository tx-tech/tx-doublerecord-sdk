package com.txt.sl.callback

/**
 * author ：Justin
 * time ：2021/7/8.
 * des ：
 */
interface netResultCallBack {
    fun onResultSuccess(result : String)
    fun onResultFail(errCode:Int,errMsg:String)
}