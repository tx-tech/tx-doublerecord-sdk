package com.tx.znsl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import com.common.widget.base.BaseActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback
import com.txt.sl.TXSdk
import com.txt.sl.utils.TxLogUtils
import kotlinx.android.synthetic.main.activity_appmeeting.*

import org.json.JSONObject

class AppMeetingActivity : BaseActivity() {

    companion object {
        fun gotoActivity(context: Activity) {
            context.startActivity(
                Intent(context, AppMeetingActivity::class.java)
            )

        }
    }


    override fun getLayoutId(): Int =  R.layout.activity_appmeeting

    override fun initView() {
        super.initView()
        statusBarConfig
            .statusBarDarkFont(true)
            // 指定导航栏背景颜色
            .navigationBarColor(R.color.white)
            .statusBarColor(R.color.white)
            .init()
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
//            配置弹窗
            XPopup.Builder(this)
                    .setPopupCallback(object : SimpleCallback() {
                        override fun onDismiss() {
                            super.onDismiss()
                            changeUI()

                        }
                    })
                    .hasStatusBarShadow(true)
                    .autoOpenSoftInput(true)
                    .asCustom(com.natchi.base.CustomFullScreenPopup(this))
                    .show()
//            val filterASRPassword = filterASRPassword(et_test.text.toString())
//            LogUtils.i("filterASRPassword"+filterASRPassword)
        }else{
            AvoidTheLoginActivity.gotoActivity(this, type)
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
    var keywordsRuleJSONObject = JSONObject("{\"contains\":[{\"_id\":\"610a462fe935212bc9ff3d10\",\"name\":\"同意\"},{\"_id\":\"610a462fe935212bc9ff3d0f\",\"name\":\"清楚\"},{\"_id\":\"610a462fe935212bc9ff3d0e\",\"name\":\"好的\"},{\"_id\":\"610a462fe935212bc9ff3d0d\",\"name\":\"了解\"}],\"notContain\":[\"不同意\",\"不清楚\",\"不好的\",\"不了解\"]}")
    private fun filterASRPassword(word: String): Boolean {

        val notContainArray = keywordsRuleJSONObject!!.getJSONArray("notContain")
        var contain = true
        for (index in 0 until notContainArray.length()) {
            val notContainStr = notContainArray.getString(index)
            contain = word.contains(notContainStr)
            if (contain) {
                return false
            }
        }
        //如果存在不包含数组中
        if (contain) {
            //不通过
            return false
        } else {
            //
            val containsArray = keywordsRuleJSONObject!!.getJSONArray("contains")
            val stringBuilder = StringBuilder("")
            for (index in 0 until containsArray.length()) {
                val containsStr = containsArray.getJSONObject(index)
                val conditionsStr = containsStr.optString("conditions")
                val nameStr = containsStr.optString("name")
                if (conditionsStr.isEmpty() || conditionsStr == "and") {
                    stringBuilder.append(nameStr + "@")
                } else {
                    stringBuilder.append(nameStr + ",")
                }
            }
            var contain = true
            val split = stringBuilder.split("@")
            if (split.size==1){
                contain = word.contains(split[0])
            }else{
                split.forEach {
                    val split1 = it.split(",")
                    split1.forEach {
                        val contains = word.contains(it)
                        contain = contains
                        if (contains) {
                            return@forEach
                        }
                    }
                }
            }


            TxLogUtils.i("${contain}")
            return contain
        }


    }

}