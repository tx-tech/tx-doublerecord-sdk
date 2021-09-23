package com.tx.znsl.login

/**
 * Created by pc on 2017/10/20.
 */
interface LoginContract {
    interface View {
        fun LoginSuccess()
        fun LoginBFail(errCode: Int, err: String?)
        fun saveNameAndPwd() //保存账号密码
    }

    interface Presenter{

        fun loginReuqest(loginName:String,passWord:String)
    }
}