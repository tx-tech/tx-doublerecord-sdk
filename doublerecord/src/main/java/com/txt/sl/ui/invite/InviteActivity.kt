package com.txt.sl.ui.invite

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.util.PermissionConstants
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.trtc.TRTCCloud
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.entity.constant.WXApi
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.receive.SystemBaiduLocation
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.video.Constant
import com.txt.sl.ui.video.OfflineActivity
import com.txt.sl.ui.video.RoomActivity
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.ToastUtils
import com.txt.sl.utils.TxPermissionConstants
import com.txt.sl.utils.TxPermissionUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_activity_invite.*
import org.json.JSONObject

public class InviteActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.tx_activity_invite

    override fun initView() {
        super.initView()

    }

    @SuppressLint("WrongConstant")
    override fun initData() {
        super.initData()
        val isRemote = intent.getBooleanExtra(InviteActivity.ARG_PARAM1, false)
        val flowId = intent.getStringExtra(InviteActivity.ARG_PARAM2)
        val phone = intent.getStringExtra(InviteActivity.ARG_PARAM3)
        val taskId = intent.getStringExtra(InviteActivity.ARG_PARAM4)
        val membersArray = intent.getStringArrayListExtra(InviteActivity.ARG_PARAM5)
        //远程
        if (isRemote) {
            tv_title.text = "远程双录前请发送邀约给双录对象"
            tv_invite_wx.text = "微信转发双录邀请"
            tv_invite.text = "邀约发送完成后，可以开始进行双录"
            titleBar?.title = "发送邀约"
        }else{
            tv_title.text = "请先发送电子签名给双录对象"
            tv_invite_wx.text = "微信转发电子签名"
            tv_invite.text = "电子签名发送完成后，可以开始进行双录"
            titleBar?.title = "发送电子签名"
        }
        tv_gotovideo.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TxPermissionUtils.permission(
                        PermissionConstants.CAMERA,
                        PermissionConstants.MICROPHONE,
                        PermissionConstants.LOCATION,
                    TxPermissionConstants.STORAGE
                ).callback(object : TxPermissionUtils.FullCallback {
                    override fun onGranted(permissionsGranted: List<String>) {

                        if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains(
                                        "android.permission.RECORD_AUDIO"
                                )
                        ) {

                            requestRoom(isRemote,flowId,membersArray)
                        } else if (permissionsGranted.contains("android.permission.LOCATION")) {
                            SystemBaiduLocation.instance!!.requestLocation()

                        }
                    }

                    override fun onDenied(
                            permissionsDeniedForever: List<String>,
                            permissionsDenied: List<String>
                    ) {
                        if (permissionsDenied.contains("android.permission.CAMERA") || permissionsDenied.contains(
                                        "android.permission.RECORD_AUDIO"
                                )
                        ) {
                            showToastMsg("视频权限或音频权限未申请！")
                        } else {
                        }
                    }
                }
                ).request()
            } else {
                requestRoom(isRemote,flowId,membersArray)
            }


        }

        tv_invite_wx.setOnClickListener {
            requestWX(phone,taskId)
        }

        regToWx()
    }

    private fun startEnterRoom(roomId: String, userID: String, roomInfo: String,isRemote :Boolean) {
        val intent =  if (isRemote) {
            Intent(this, RoomActivity::class.java)
        }else{
            Intent(this, OfflineActivity ::class.java)
        }


        intent.putExtra(Constant.ROOM_ID, roomId)
        intent.putExtra(Constant.USER_ID, userID)
        intent.putExtra(Constant.ROOM_INFO, roomInfo)
        startActivity(intent)
        finish()
    }
    private var mLoadingView: LoadingView? = null

    fun showLoading() {
        if (mLoadingView == null) {
            mLoadingView = LoadingView(this, "开始录制", LoadingView.SHOWLOADING)
        }
        mLoadingView!!.show()
    }


    fun hideLoading() {
        if (mLoadingView != null)
            mLoadingView!!.dismiss()
    }

    private fun requestRoom(isRemote: Boolean, flowId: String, membersArray: java.util.ArrayList<String>) {


        val sdkVersion = TRTCCloud.getSDKVersion()
        LogUtils.i("sdkVersion", sdkVersion)
        showLoading()
        SystemHttpRequest.getInstance().startAgent(flowId, isRemote, membersArray ,object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                runOnUiThread {
                   hideLoading()
                    Handler().postDelayed({
                        val jsonObject = JSONObject(json)
                        val roomId = jsonObject.getString("roomId")
                        val agentIdStr = jsonObject.getString("agentId")
//                                    val agentId = jsonObject.getString("customerCode")
                        startEnterRoom(roomId, agentIdStr, jsonObject.toString(),isRemote)

                    }, 80)

                }


            }

            override fun onFail(err: String?, code: Int) {
                runOnUiThread {
                    hideLoading()
                    ToastUtils.showShort(err!!)
                }
            }

        })


    }

    var api: IWXAPI? = null

    public  fun requestWX(phone:String,taskId:String) {

//用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        val webpage = WXMiniProgramObject().apply {
            webpageUrl = "http://www.qq.com"
            miniprogramType = when (TXSdk.getInstance().txConfig.miniprogramType) {
                TXSdk.Environment.DEV -> {
                    WXMiniProgramObject.MINIPROGRAM_TYPE_TEST
                }
                TXSdk.Environment.TEST -> {
                    WXMiniProgramObject.MINIPROGRAM_TYPE_PREVIEW
                }
                TXSdk.Environment.RELEASE -> {
                    WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE
                }
            }

            userName = TXSdk.getInstance().txConfig.userName
            path =
                    "${TXSdk.getInstance().txConfig.miniProgramPath}?taskId=${taskId}"
            LogUtils.i("path-------$path")
        }

        val msg = WXMediaMessage(webpage).apply {
            title = if (TXSdk.getInstance().txConfig.miniprogramTitle.isNotEmpty()) {
                TXSdk.getInstance().txConfig.miniprogramDescription
            } else {
                "智能双录"
            }

            description = if (TXSdk.getInstance().txConfig.miniprogramTitle.isNotEmpty()) {
                TXSdk.getInstance().txConfig.miniprogramDescription
            } else {
                "智能双录"
            }
            thumbData = resources.openRawResource(R.raw.tx_icon_miniprogram).readBytes()
        }
        TXSdk.getInstance().wxTransaction = "miniProgram${System.currentTimeMillis()}"
        val req = SendMessageToWX.Req().apply {
            transaction = TXSdk.getInstance().wxTransaction
            message = msg
            scene = SendMessageToWX.Req.WXSceneSession
        }

        api?.sendReq(req)
    }

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            api?.registerApp(WXApi.APP_ID)
        }

    }

    private fun regToWx() {

        api = WXAPIFactory.createWXAPI(this, WXApi.APP_ID, true)
        api?.registerApp(WXApi.APP_ID)
        registerReceiver(
                broadcastReceiver , IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP)
        )
    }

    private fun buildTransaction(type: String?): String? {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    companion object {
        private const val ARG_PARAM1 = "isRemote"
        private const val ARG_PARAM2 = "flowId"
        private const val ARG_PARAM3 = "phone"
        private const val ARG_PARAM4 = "taskId"
        private const val ARG_PARAM5 = "membersArray"

        @JvmStatic
        fun newInstance(context: Context,
                        applyStatusParams: Boolean?,
                        applyStatusParams1:String,
                        applyStatusParams2:String,
                        applyStatusParams3:String,
                        applyStatusParams4:ArrayList<String>
                        ) {
            val intent = Intent(context, InviteActivity::class.java)
            intent.putExtra(InviteActivity.ARG_PARAM1, applyStatusParams)
            intent.putExtra(InviteActivity.ARG_PARAM2, applyStatusParams1)
            intent.putExtra(InviteActivity.ARG_PARAM3, applyStatusParams2)
            intent.putExtra(InviteActivity.ARG_PARAM4, applyStatusParams3)
            intent.putExtra(InviteActivity.ARG_PARAM5, applyStatusParams4)
            context.startActivity(intent)
        }
    }

}