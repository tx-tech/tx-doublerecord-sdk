package com.tx.znsl.login

import android.app.Activity
import com.txt.sl.base.BasePresenter
import com.txt.sl.callback.onNetResultCallBack
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.utils.TxSPUtils


/**
 * Created by pc on 2017/10/20.
 */

class LoginPresenter(private val mContext: Activity, private val mLoginContract: LoginContract.View) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    private val TAG = LoginPresenter::class.java.simpleName

    override fun loginReuqest(loginName: String, passWord: String) {
        TXManagerImpl.instance!!.login(loginName,passWord,object : onNetResultCallBack{
            override fun onResultSuccess(result: String) {

                TxSPUtils.put(mContext, SPConstant.LOGIN_NAME, loginName)
                TxSPUtils.put(mContext, SPConstant.LOGIN_PWD, passWord)
                TxSPUtils.put(mContext, SPConstant.SP_ISLOGIN, true)
                mLoginContract?.LoginSuccess()




            }

            override fun onResultFail(errCode: Int, errMsg: String) {
                mLoginContract?.LoginBFail( errCode!! ,errMsg!!)
            }

        })

    }


}
