package com.natchi.screenrecorddemo

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import android.util.Log
import com.tencent.bugly.crashreport.CrashReport
//import com.tencent.smtt.sdk.QbSdk
//import com.tencent.smtt.sdk.QbSdk.PreInitCallback
//import com.tencent.smtt.sdk.TbsListener
import com.txt.sl.TXSdk
import com.txt.sl.config.TxConfig

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：com.github.tx-tech:tx-doublerecord-sdk:1.1.2 这样
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val txConfig = TxConfig()
        txConfig.wxKey = "wx1fafe1ec5a12a7a8" //双录测试环境

//        txConfig.wxKey = "wx716f1319f5dd623a"
        //dev 小程序开发版本  TEST 体验版本 RELEASE 正式颁布
        //        txConfig.wxKey = "wx716f1319f5dd623a"
        //dev 小程序开发版本  TEST 体验版本 RELEASE 正式颁布
        txConfig.miniprogramType = TXSdk.Environment.TEST
        //小程序跳转参数
//        txConfig.userName = "gh_fe0a27ed0ba5"
        //小程序跳转参数
//        txConfig.userName = "gh_fe0a27ed0ba5"
//        txConfig.userName = "gh_72cea2621d8b" //公司测试环境小程序
        txConfig.userName = "gh_20a61ca39c2f" //公司测试环境小程序

//        txConfig.userName = "gh_856cb9aba549"  //腾讯测试环境小程序
        //        txConfig.userName = "gh_856cb9aba549"  //腾讯测试环境小程序
        txConfig.miniprogramTitle = "" //显示小程序描述

        txConfig.miniprogramDescription = "" //显示小程序描述

        TXSdk.getInstance().init(this, TXSdk.Environment.POC, true, txConfig)
        CrashReport.initCrashReport(this, "8351c98a70", true)
//
//        /* 设置允许移动网络下进行内核下载。默认不下载，会导致部分一直用移动网络的用户无法使用x5内核 */
//
//        /* 设置允许移动网络下进行内核下载。默认不下载，会导致部分一直用移动网络的用户无法使用x5内核 */QbSdk.setDownloadWithoutWifi(true)
//
//        /* SDK内核初始化周期回调，包括 下载、安装、加载 */
//
//        /* SDK内核初始化周期回调，包括 下载、安装、加载 */QbSdk.setTbsListener(object : TbsListener {
//            /**
//             * @param stateCode 110: 表示当前服务器认为该环境下不需要下载；200下载成功
//             */
//            override fun onDownloadFinish(stateCode: Int) {}
//
//            /**
//             * @param i 200、232安装成功
//             */
//            override fun onInstallFinish(i: Int) {}
//
//            /**
//             * 首次安装应用，会触发内核下载，此时会有内核下载的进度回调。
//             * @param progress 0 - 100
//             */
//            override fun onDownloadProgress(progress: Int) {
////                Log.i(com.tencent.tbs.demo.DemoApplication.TAG, "Core Downloading: $progress")
//            }
//        })
//
//        /* 此过程包括X5内核的下载、预初始化，接入方不需要接管处理x5的初始化流程，希望无感接入 */
//
//        /* 此过程包括X5内核的下载、预初始化，接入方不需要接管处理x5的初始化流程，希望无感接入 */QbSdk.initX5Environment(
//            this,
//            object : PreInitCallback {
//                override fun onCoreInitFinished() {
//                    // 内核初始化完成，可能为系统内核，也可能为系统内核
//                }
//
//                /**
//                 * 预初始化结束
//                 * 由于X5内核体积较大，需要依赖wifi网络下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
//                 * 内核下发请求发起有24小时间隔，卸载重装、调整系统时间24小时后都可重置
//                 * @param isX5 是否使用X5内核
//                 */
//                override fun onViewInitFinished(isX5: Boolean) {}
//            })
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}