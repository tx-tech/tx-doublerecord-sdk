package com.tx.znsl.login

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.common.widget.dialog.TxPopup
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxj.xpopup.interfaces.SimpleCallback
import com.lxj.xpopup.util.KeyboardUtils
import com.tx.znsl.BuildConfig
import com.tx.znsl.R
import com.txt.sl.base.AppMVPActivity
import com.txt.sl.callback.onSDKListener
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.config.socket.SocketBusiness
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.system.SystemSocket
import com.txt.sl.ui.dialog.CustomDialog
import com.txt.sl.utils.CheckEnvUtils
import com.txt.sl.utils.InfoUtils
import com.txt.sl.utils.TxLogUtils
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*


/** 613980e6e7cf5e50a523ff8c 通用双录
 * Created by pc on 2017/10/18.
 */
class LoginActivity : AppMVPActivity< LoginContract.View, LoginPresenter>(), View.OnClickListener, LoginContract.View,
    SocketBusiness {
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
    override fun getLayoutId(): Int = R.layout.activity_login


    private fun continuousClick() {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(COUNTS)//重新初始化数组
            //
            XPopup.Builder(this)
                .setPopupCallback(object : SimpleCallback() {
                    override fun onDismiss() {
                        super.onDismiss()
//                        changeUI()

                    }
                })
                .hasStatusBarShadow(true)
                .autoOpenSoftInput(true)
                .asCustom(CustomFullScreenPopup(this))
                .show()
        }
    }


    private var mGrantedCount = 0 // 权限个数计数，获取Android系统权限
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_login -> {
                if (BuildConfig.DEBUG) {
                    etAccount!!.setText("aixin-poc")
                    etPassWord!!.setText("ax123456")
                }
                mAccount = etAccount!!.text.toString().trim { it <= ' ' }
                mPassWord = etPassWord!!.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(mAccount)||TextUtils.isEmpty(mPassWord)){
                    XPopup.Builder(this).asConfirm("登录","账号/密码不能为空","","好的",
                        { },null,true).show()
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
            mLoadingView = LoadingView(this@LoginActivity,"登录中", LoadingView.SHOWLOADING)
        }
        mLoadingView!!.show()
    }


    fun hideLoading() {
        if (mLoadingView != null)
            mLoadingView!!.dismiss()
    }


    override fun LoginSuccess() {
        runOnUiThread {
            hideLoading()
            MeetingActivity.newActivity(this)
            finish()
        }

    }


    override fun LoginBFail(errCode: Int, err: String?) {
        runOnUiThread {
            hideLoading()
            XPopup.Builder(this).asConfirm("登录",err,"","好的", OnConfirmListener { },null,true).show()
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





    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun initView() {
        super.initView()
        statusBarConfig
                .statusBarDarkFont(true)
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.tx_white)
                .statusBarColor(R.color.tx_white)
                .init()
        mLoadingView = LoadingView(this@LoginActivity, "登录中...", LoadingView.SHOWLOADING)
        btn_login!!.setOnClickListener(this)

        image_logo!!.setOnClickListener(this)
        showNameAndPwd()

        mDeviceId = InfoUtils.getDid(this) // 取唯一标识 用来区别强制下线
    }



    override fun createPresenter(): LoginPresenter? = LoginPresenter(this, this)
    override fun onReceiveMSG(data: JSONObject) {

    }

    override fun agentOnline(data: JSONObject) {

    }
}
