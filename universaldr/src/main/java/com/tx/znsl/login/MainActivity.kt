package com.tx.znsl.login

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import com.tx.znsl.R
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.utils.ApplicationUtils
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.TxSPUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), LoginContract.View {

    val TAG1 = "MainActivity"


    //该activity 会导致页面闪烁
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.isTaskRoot,表示该activity在该栈中，是在最底下（代表是第一个）
        LogUtils.i(TAG1, "当前处在第一个： ${this.isTaskRoot}")
        if (!this.isTaskRoot) {
//            if (ApplicationUtils.isContainVideo) {
//                LogUtils.i("MainActivity", "当前存在视频页面，所以不需要加载这个页面！")
//                finish()
//            } else {
//                LogUtils.i("MainActivity", "当前存在存在其他的页面，所以不需要加载这个页面！")
//
//                finish()
//            }

        }else{
            setContentView(R.layout.activity_main)
            initEvent()
        }



    }



    private var mLoginPresenter: LoginPresenter? = null
     fun initEvent() {
       LogUtils.i("initEvent()")
        mLoginPresenter = LoginPresenter(this, this)
//        var type = when(BuildConfig.isShowOut){
//             //测试
//             "1"->"测试版本："
//             //开发
//             "2"->"开发版本："
//             //正式
//             "3"->"版本："
//
//            else -> ""
//        }
//         tv_version.text = type +"${BuildConfig.VERSION_NAME}"
         val aa = AlphaAnimation(0.2f, 1.0f)
         aa.duration = 500
         image_logo.animation =aa
         aa.setAnimationListener(object :Animation.AnimationListener{
             override fun onAnimationRepeat(animation: Animation?) {

             }

             override fun onAnimationEnd(animation: Animation?) {
                 getData()
             }

             override fun onAnimationStart(animation: Animation?) {
             }

         })

    }

    var loginName =""
    var loginPwd =""
    private fun getData() {
        val isLogin = TxSPUtils.get(this, SPConstant.SP_ISLOGIN, false) as Boolean
        LogUtils.i("initEvent()--$isLogin")
        if (!isLogin) {

            LoginActivity.newActivity(this)
            finish()
        } else {

            loginName = TxSPUtils.get(this, SPConstant.LOGIN_NAME, "") as String
            loginPwd = TxSPUtils.get(this, SPConstant.LOGIN_PWD, "") as String
            if (loginName.isEmpty()||loginPwd.isEmpty()){
                LoginActivity.newActivity(this)
                finish()
            }else{
                if (mLoginPresenter!=null) {
                    mLoginPresenter?.loginReuqest(loginName, loginPwd)
                }else{
                    LoginActivity.newActivity(this)
                    finish()
                }

            }


        }


    }

    override fun LoginSuccess() {
        runOnUiThread {

            MeetingActivity.newActivity(this)
            finish()
        }
    }

    override fun LoginBFail(errCode: Int, err: String?) {
        runOnUiThread {
            Toast.makeText(this,"",Toast.LENGTH_SHORT).show()
            LoginActivity.newActivity(this)
            finish()
        }
    }



    override fun saveNameAndPwd() {
        TxSPUtils.put(this, SPConstant.SP_ISLOGIN, true)
        TxSPUtils.put(this, SPConstant.LOGIN_NAME, loginName)
        TxSPUtils.put(this, SPConstant.LOGIN_PWD, loginPwd)

    }

}
