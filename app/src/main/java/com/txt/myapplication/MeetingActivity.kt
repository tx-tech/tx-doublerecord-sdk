package com.txt.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.txt.sl.TXSdk
import kotlinx.android.synthetic.main.activity_meeting.*

class MeetingActivity : AppCompatActivity() {
    enum class TYPE {
        CREATEROOM, JOINROOM, RESERVATIONROOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

    }

    public fun onMeetClick(v: View) {
        val type = when (v.id) {
            R.id.tv_createroom -> {
                "1"
            }
            R.id.tv_joinroom -> {
                "2"
            }
            R.id.tv_reservationroom -> {
                "3"
            }
            R.id.tv_reservationroom1 -> {
                "4"
            }
            R.id.tv_config -> {
                "5"
            }

            else -> {
                "1"
            }
        }
        if ( "5" == type){
            //配置弹窗
            XPopup.Builder(this)
                    .setPopupCallback(object : SimpleCallback() {
                        override fun onDismiss() {
                            super.onDismiss()
                            changeUI()

                        }
                    })
                    .hasStatusBarShadow(true)
                    .autoOpenSoftInput(true)
                    .asCustom(CustomFullScreenPopup(this))
                    .show()
        }else{
            MainActivity.gotoActivity(this, type)
        }

    }

    @SuppressLint("SetTextI18n")
    fun changeUI() {

        var sdkVersion = "SDK：" + TXSdk.getInstance().sdkVersion
        var appEnv = sdkVersion + "\n" + when (TXSdk.getInstance().environment) {
            TXSdk.Environment.DEV -> "App：开发环境"
            TXSdk.Environment.TEST -> "App：测试环境"
            else -> "App：正式环境"
        }
        tv_dep.text = appEnv + "\n" + when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.DEV -> {
                "小程序：开发版本"
            }
            TXSdk.Environment.TEST -> {
                "小程序：体验版本"
            }
            else -> "小程序：正式版本"
        }

    }

}