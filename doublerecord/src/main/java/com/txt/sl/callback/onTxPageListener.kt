package com.txt.sl.callback

/**
 * author ：Justin
 * time ：2021/3/29.
 * des ：
 */
open interface  onTxPageListener{
    fun onSuccess(taskId:String)
    fun onSuccess(){}
    fun onFail(errCode:Int,errMsg:String)
}