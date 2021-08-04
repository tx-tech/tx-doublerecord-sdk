package com.txt.myapplication

//import com.txt.video.widget.utils.ToastUtils
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.txt.sl.TXSdk
import com.txt.sl.callback.onSDKListener
import com.txt.sl.callback.onTxPageListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), onTxPageListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG)
            et.setText("wjqdev")
        initView()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    var type = ""
    fun initView() {
        TXSdk.getInstance().addOnTxPageListener(this)
        if (intent != null) {
            type = if (intent?.getStringExtra("type") != null) {
                intent?.getStringExtra("type")!!
            } else {
                ""
            }
            TYPE = type
        } else {
            type = TYPE
        }
        var btStr = when (type) {
            "2" -> {
                group2.visibility = View.VISIBLE
                "投保中的双录"
            }
            "3" -> {
                group2.visibility = View.GONE
                "我的双录"
            }
            "4" -> {
                group2.visibility = View.VISIBLE
                "双录上传"
            }
            else -> {
                group2.visibility = View.GONE
                "创建保单信息"
            }
        }
        bt.text = btStr
        et_account.setText(
            when (TXSdk.getInstance().environment) {
                TXSdk.Environment.DEV, TXSdk.Environment.TEST -> "gscyf_test"
                else -> "gsc_test"
            }
        )
        bt.setOnClickListener {
//            TxLogUtils.i("AndroidSystemUtil.getDevice"+AndroidSystemUtil.getDevice())
            startSDK(type)


        }

        var appEnv1 = when (TXSdk.getInstance().environment) {
            TXSdk.Environment.DEV -> "/开发环境"
            TXSdk.Environment.TEST -> "/测试环境"
            else -> "/正式环境"
        }

        et_account.setText(
                when (TXSdk.getInstance().environment) {
                    TXSdk.Environment.DEV, TXSdk.Environment.TEST -> "remoteRecordOrg"
                    else -> "gsc_test"
                }
        )
        check_bt.text = when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.DEV -> {
                "开发版本" + appEnv1
            }
            TXSdk.Environment.TEST -> {
                "体验版本" + appEnv1
            }
            else -> "正式版本$appEnv1"
        }
    }




    private fun startSDK(pageType: String) {
        val businessData = JSONObject().apply {
            put("latitude", 123.1231231)
            put("longitude", 21.123123)
            put("accuracy", 1000)
            put("province", "上海市")
            put("city", "上海市")
            put("adr", "上海市")
        }
        val loginName = et.text.toString()
        val roomid = et_fullname.text.toString()



        val orgAccount = et_account.text.toString()
        val fullname = et_fullname.text.toString()
//        val orgAccount = "gscjg"
        if (loginName.isEmpty()) {
            Toast.makeText(this@MainActivity, "请填入账号！", Toast.LENGTH_SHORT).show()
            return
        }
        if (orgAccount.isEmpty()) {
            Toast.makeText(this@MainActivity, "请填入组织代码！", Toast.LENGTH_SHORT).show()
            return
        }
        if (fullname.isEmpty()) {
            Toast.makeText(this@MainActivity, "请填入姓名！", Toast.LENGTH_SHORT).show()
            return
        }
        var flowid =""
        when(pageType){
            "2","4"->{
                flowid  = et_flowid.text.toString()
                if (flowid.isEmpty()) {
                    Toast.makeText(this@MainActivity, "请填入业务单号！", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            else->{

            }
        }
        val l = System.currentTimeMillis() / 1000
        Log.i("currentTimeMillis", "" + l)
        val encrypt: String = SignUtils.Encrypt(orgAccount + "" + l)

        when (pageType) {
            "1"-> {//创建保单
                TXSdk.getInstance().gotoCreateDetailsPage(this,loginName,fullname,orgAccount,encrypt,object :onSDKListener{
                    override fun onResultSuccess(result: String) {

                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {

                    }

                })

            }

            "2"-> {//跳到详情页面
                TXSdk.getInstance().gotoOrderDetaisPage(this,loginName,fullname,flowid,orgAccount,encrypt,object :onSDKListener{
                    override fun onResultSuccess(result: String) {

                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {

                    }

                })

            }
            "3"->{//跳到列表页面
                TXSdk.getInstance().gotoOrderListPage(this,loginName,fullname,orgAccount,encrypt,object :onSDKListener{
                    override fun onResultSuccess(result: String) {

                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {

                    }

                })
            }
            "4"->{ //双录上传
                TXSdk.getInstance().gotoVideoUploadPage(this,loginName,fullname,flowid,orgAccount,encrypt,object :onSDKListener{
                    override fun onResultSuccess(result: String) {

                    }

                    override fun onResultFail(errCode: Int, errMsg: String) {

                    }

                })
            }

            else -> {
            }
        }


    }



    companion object {
        var TYPE = ""
        fun gotoActivity(context: Activity, type: String) {
            context.startActivityForResult(
                Intent(context, MainActivity::class.java).apply {
                    putExtra("type", type)
                },
                12300
            )

        }
    }

    override fun onSuccess(taskId: String) {
        Toast.makeText(this@MainActivity,"返回的业务单号为："+ taskId, Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess() {
        Toast.makeText(this@MainActivity,"返回了", Toast.LENGTH_SHORT).show()
    }
    override fun onFail(errCode: Int, errMsg: String) {

    }


}