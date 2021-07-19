package com.txt.sl.ui.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.dialog.util.KeyboardUtils
import com.txt.sl.R
import com.txt.sl.base.AppMVPActivity
import com.txt.sl.entity.constant.Constant
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.HomeActivity
import com.txt.sl.ui.dialog.CustomDialog
import com.txt.sl.utils.InfoUtils
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.utils.ToastUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_activity_login.*
import java.util.*


/**
 * Created by pc on 2017/10/18.
 */
class LoginActivity : AppMVPActivity< LoginContract.View,LoginPresenter>(), View.OnClickListener, LoginContract.View {
    private var mLoadingView: LoadingView? = null
    private var mLoginPresenter: LoginPresenter? = null
    private var mAccount: String? = null
    private var mPassWord: String? = null
    private var mDeviceId = ""
    private val mLoginType = "0"
    internal var mHits = LongArray(COUNTS)

    object PermissionMode {
        const val READ_PHONE_STATE = 100
        const val READ_EXTERNAL_STORAGE = 101
        const val WRITE_EXTERNAL_STORAGE = 102
    }



    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val REQ_PERMISSION_CODE = 0x1000
        internal val COUNTS = 3// 点击次数
        internal val DURATION: Long = 1000// 规定有效时间
        fun newActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun getLayoutId(): Int = R.layout.tx_activity_login



    private fun continuousClick() {
        val customDialog = CustomDialog(this)
        customDialog.setOnConfirmClickListener { ip, port ->
            if (ip.isEmpty()) {
                ToastUtils.showShort("ip不能为空")
                return@setOnConfirmClickListener
            }
            SystemHttpRequest.getInstance().changeIP(ip,port)
            customDialog.dismiss()
            KeyboardUtils.hideSoftInput(customDialog)
        }
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(COUNTS)//重新初始化数组
            //
            TxPopup.Builder(this).asCustom(customDialog).show()
        }
    }


    private var mGrantedCount = 0 // 权限个数计数，获取Android系统权限
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERMISSION_CODE) {
            for (ret in grantResults) {
                if (PackageManager.PERMISSION_GRANTED == ret) mGrantedCount++
            }
            if (mGrantedCount == permissions.size) {

            } else {
                Toast.makeText(this, getString(R.string.tx_rtc_permisson_error_tip), Toast.LENGTH_SHORT).show()
            }
            mGrantedCount = 0
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> {
                mAccount = etAccount!!.text.toString().trim { it <= ' ' }
                mPassWord = etPassWord!!.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(mAccount)||TextUtils.isEmpty(mPassWord)){
                    TxPopup.Builder(this).asConfirm("登录","账号/密码不能为空","","好的",object : OnConfirmListener {
                        override fun onConfirm() {

                        }

                    },null,true).show()
                }else{
                    showLoading()
                    mPresenter?.loginReuqest(mAccount!!,mPassWord!!)
                }




            }

            R.id.image_logo -> continuousClick()

            else -> {
            }
        }
    }


    fun showLoading() {
        if (mLoadingView == null) {
            mLoadingView = LoadingView(this@LoginActivity, resources.getString(R.string.tx_loading_login), LoadingView.SHOWLOADING)
        }
        mLoadingView!!.show()
    }


    fun hideLoading() {
        if (mLoadingView != null)
            mLoadingView!!.dismiss()
    }


    override fun LoginSuccess() {
        runOnUiThread {
            Constant.loginname = mAccount
            Constant.password = mPassWord
            hideLoading()
            HomeActivity.newActivity(this)
            finish()
        }

    }


    override fun LoginBFail(err: String?, errCode: Int) {
        runOnUiThread {
            hideLoading()
            TxPopup.Builder(this).asConfirm("登录",err,"","好的", OnConfirmListener { },null,true).show()
        }

    }



    //保存登录账号和密码
    override fun saveNameAndPwd() {
        TxSPUtils.put(this@LoginActivity, SPConstant.LOGIN_NAME, mAccount)
        TxSPUtils.put(this@LoginActivity, SPConstant.LOGIN_PWD, mPassWord)
        TxSPUtils.put(this@LoginActivity, SPConstant.SP_ISLOGIN, true)

    }

    fun showNameAndPwd() {

        val login_name = TxSPUtils.get(this@LoginActivity, SPConstant.LOGIN_NAME, "") as String
        val login_pwd = TxSPUtils.get(this@LoginActivity, SPConstant.LOGIN_PWD, "") as String

        if (!TextUtils.isEmpty(login_name)) etAccount!!.setText(login_name)
        if (!TextUtils.isEmpty(login_pwd)) etPassWord!!.setText(login_pwd)


    }


    override fun onBackPressed() {
        //退出App
//        finish()
//        exitApp()

    }
    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions: MutableList<String> = ArrayList()
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE)
            }

            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (permissions.size != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toTypedArray(),
                        LoginActivity.REQ_PERMISSION_CODE)
                return false
            }
        }
        return true
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun initView() {
        super.initView()
        statusBarConfig
                .statusBarDarkFont(true)
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.tx_colorwhite)
                .statusBarColor(R.color.tx_colorwhite)
                .init()
        checkPermission()
        mLoadingView = LoadingView(this@LoginActivity, "登录中...", LoadingView.SHOWLOADING)
        btn_login!!.setOnClickListener(this)

        image_logo!!.setOnClickListener(this)
        showNameAndPwd()

        mDeviceId = InfoUtils.getDid(this) // 取唯一标识 用来区别强制下线
    }



    override fun createPresenter(): LoginPresenter? = LoginPresenter(this, this)
}
