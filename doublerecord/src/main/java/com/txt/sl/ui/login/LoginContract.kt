package com.txt.sl.ui.login

/**
 * Created by pc on 2017/10/20.
 */
interface LoginContract {
    interface View {
        fun LoginSuccess()
        fun LoginBFail(err: String?, errCode: Int)
        fun saveNameAndPwd() //保存账号密码
    }

    interface Presenter{

        fun loginReuqest(loginName:String,passWord:String)
    }
}