package com.txt.sl.callback

/**
 * author ：Justin
 * time ：2021/7/5.
 * des ：
 */
interface startVideoResultOnListener {
    fun onResultSuccess()
    fun onResultFail(errCode:Int,errMsg:String)
}