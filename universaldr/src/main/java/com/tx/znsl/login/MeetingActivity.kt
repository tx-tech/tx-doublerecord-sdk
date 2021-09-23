package com.tx.znsl.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.common.widget.base.BaseActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxj.xpopup.interfaces.SimpleCallback
import com.tx.znsl.R
import com.txt.sl.TXSdk
import com.txt.sl.callback.onSDKListener
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.utils.TxSPUtils


class MeetingActivity : BaseActivity() {
    enum class TYPE {
        CREATEROOM, JOINROOM, RESERVATIONROOM
    }

    companion object {
        fun newActivity(context: Context) {
            val intent = Intent(context, MeetingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        super.initView()
        statusBarConfig
            .statusBarDarkFont(true)
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.tx_white)
            .statusBarColor(R.color.tx_white)
            .init()
    }


    override fun getLayoutId(): Int = R.layout.activity_meeting

    public fun onMeetClick(v: View) {
        val type = when (v.id) {
            R.id.tv_createroom -> {
                TXManagerImpl.instance!!.directCreateDetaisPage(this, object : onSDKListener {
                    override fun onResultSuccess(result: String) {
                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {
                    }

                })
                "1"
            }

            R.id.tv_reservationroom -> {
                TXManagerImpl.instance!!.directGotoOrderListPage(this, object : onSDKListener {
                    override fun onResultSuccess(result: String) {
                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {
                    }

                })
                "3"
            }
            R.id.tv_exitroom -> {
                XPopup.Builder(this).asConfirm("退出", "确认退出？", "取消", "确认", {
                    val loginName = TxSPUtils.get(this@MeetingActivity, SPConstant.LOGIN_NAME, "")
                    val password = TxSPUtils.get(this@MeetingActivity, SPConstant.LOGIN_PWD, "")
                    TxSPUtils.clear(this@MeetingActivity)
                    TxSPUtils.put(this@MeetingActivity, SPConstant.LOGIN_NAME, loginName)
                    TxSPUtils.put(this@MeetingActivity, SPConstant.LOGIN_PWD, password)

                    LoginActivity.newActivity(this@MeetingActivity)
                }, null, false).show()
            }

            else -> {
                "1"
            }
        }

    }


}