package com.tx.znsl

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.tencent.bugly.crashreport.CrashReport
import com.txt.sl.TXSdk
import com.txt.sl.config.TxConfig
import com.umeng.commonsdk.UMConfigure

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：com.github.tx-tech:tx-doublerecord-sdk:1.1.2 这样
 */
public class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val txConfig = TxConfig()
        txConfig.wxKey = "wx1fafe1ec5a12a7a8" //双录测试环境
//        txConfig.wxKey = "wx716f1319f5dd623a"
        //dev 小程序开发版本  TEST 体验版本 RELEASE 正式颁布
        txConfig.miniprogramType = TXSdk.Environment.RELEASE
        //小程序跳转参数
//        txConfig.userName = "gh_fe0a27ed0ba5"
        txConfig.userName = "gh_72cea2621d8b"  //公司测试环境小程序
//        txConfig.userName = "gh_20a61ca39c2f"  //公司测试环境小程序
//        txConfig.userName = "gh_856cb9aba549"  //腾讯测试环境小程序
        txConfig.miniprogramTitle = "智能双录"//显示小程序描述
        txConfig.miniprogramDescription = "智能双录"//显示小程序描述

        TXSdk.getInstance().init(this, TXSdk.Environment.RELEASE, true, txConfig)
        CrashReport.initCrashReport(this, "8351c98a70", true)
//        val txConfig = TxConfig()
//        txConfig.wxKey = "wx1fafe1ec5a12a7a8" //双录测试环境
//
////        txConfig.wxKey = "wx716f1319f5dd623a"
//        //dev 小程序开发版本  TEST 体验版本 RELEASE 正式颁布
//        //        txConfig.wxKey = "wx716f1319f5dd623a"
//        //dev 小程序开发版本  TEST 体验版本 RELEASE 正式颁布
//        txConfig.miniprogramType = TXSdk.Environment.TEST
//        //小程序跳转参数
////        txConfig.userName = "gh_fe0a27ed0ba5"
//        //小程序跳转参数
////        txConfig.userName = "gh_fe0a27ed0ba5"
////        txConfig.userName = "gh_72cea2621d8b" //公司测试环境小程序
//        txConfig.userName = "gh_20a61ca39c2f" //公司测试环境小程序
//
////        txConfig.userName = "gh_856cb9aba549"  //腾讯测试环境小程序
//        //        txConfig.userName = "gh_856cb9aba549"  //腾讯测试环境小程序
//        txConfig.miniprogramTitle = "" //显示小程序描述
//
//        txConfig.miniprogramDescription = "" //显示小程序描述
//
//        TXSdk.getInstance().init(this, TXSdk.Environment.TEST, true, txConfig)
//        CrashReport.initCrashReport(this, "8351c98a70", true)
        UMConfigure.init(this, "622042e8317aa8776078adcc", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}