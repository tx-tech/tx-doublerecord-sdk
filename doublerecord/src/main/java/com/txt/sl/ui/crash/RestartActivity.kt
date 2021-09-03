package com.txt.sl.ui.crash

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.common.widget.base.BaseActivity
import com.txt.sl.utils.ApplicationUtils


class RestartActivity : BaseActivity() {
    override fun getLayoutId(): Int = 0
    companion object {
        @JvmStatic
        fun restart(context: Context) {
//            val intent: Intent
//            intent = if (true) {
//                // 如果是未登录的情况下跳转到闪屏页
//                Intent(context, SplashActivity::class.java)
//            } else {
//                // 如果是已登录的情况下跳转到首页
//                Intent(context, HomeActivity::class.java)
//            }
//            if (context !is Activity) {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            context.startActivity(intent)
            ApplicationUtils.finishActivity()
        }

        @JvmStatic
        fun start(context: Context
        ) {
            val intent = Intent(context, RestartActivity::class.java)
           if(!(context is Activity )) {
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
           }
            context.startActivity(intent)
        }
    }

    override fun initData() {
        super.initData()
//        restart(this);
        showToastMsg("应用出了点小意外，正在重新启动");
//        ApplicationUtils.finishActivity()
    }
}