package com.txt.sl.ui.invite

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.view.View
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.util.PermissionConstants
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.trtc.TRTCCloud
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.bean.WorkItemBean
import com.txt.sl.entity.constant.WXApi
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.receive.SystemBaiduLocation
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.video.Constant
import com.txt.sl.ui.video.OfflineActivity
import com.txt.sl.ui.video.RoomActivity
import com.txt.sl.utils.LogUtils
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
        val recordType = intent.getStringExtra(InviteActivity.ARG_PARAM2)
        val mWorkItemBean = intent.getSerializableExtra(InviteActivity.ARG_PARAM3) as WorkItemBean
        val flowId = mWorkItemBean.flowId
        val phone =  mWorkItemBean.insurantPhone
        val taskId = mWorkItemBean.taskId
        val membersArray = mWorkItemBean.membersArray
        val selfInsurance = mWorkItemBean.isSelfInsurance

        val policyholderUrl = mWorkItemBean.policyholderUrl
        val insuranceUrl = mWorkItemBean.insuranceUrl
        //远程
        if (isRemote) {
            tv_title.text = "远程双录前请发送邀约给双录对象"
            tv_invite_wx.text = "微信转发双录邀请"
            tv_invite.text = "邀约发送完成后，可以开始进行双录"
            titleBar?.title = "发送邀约"
        }else{
            tv_title.text = "请先发送电子签名给双录对象"


            if (policyholderUrl.isNotEmpty()) {
                tv_invite_wx.text = "微信转发投保人电子签名"
                tv_invite_wx.visibility = View.VISIBLE
            }else{
                tv_invite_wx.visibility = View.GONE
            }
            if (insuranceUrl.isNotEmpty()) {
                tv_invite_wx1.text = "微信转发被保人电子签名"
                tv_invite_wx1.visibility = View.VISIBLE
            }else{
                tv_invite_wx1.visibility = View.GONE
            }
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
                        LogUtils.i("permissionsGranted"+permissionsGranted)

                        if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains(
                                        "android.permission.RECORD_AUDIO"
                                )&&permissionsGranted.contains("android.permission.ACCESS_FINE_LOCATION")
                        ) {

                            requestRoom(isRemote,flowId,membersArray,selfInsurance,taskId,recordType)
                            SystemBaiduLocation.instance!!.requestLocation()
                        } else {
                            showToastMsg("视频权限或音频权限未申请！")
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
                requestRoom(isRemote, flowId, membersArray, selfInsurance, taskId,recordType)
            }


        }

        tv_invite_wx.setOnClickListener {
            if (isRemote){
                requestWX(phone,taskId)
            }else{
                if (policyholderUrl.isNotEmpty()) {
                    requestWebUrl(mWorkItemBean.insurantName,policyholderUrl)
                }else{
                    showToastMsg("投保人签字链接为空")
                }

            }

        }
        tv_invite_wx1.setOnClickListener {
            requestWebUrl(mWorkItemBean.insuredName,mWorkItemBean.insuranceUrl)
        }

        regToWx()
    }

    private fun startEnterRoom(
        roomId: String,
        userID: String,
        roomInfo: String,
        isRemote: Boolean,
        selfInsurance: Boolean,
        taskId: String,
        recordType: String
    ) {
        val intent =  if (isRemote) {
            Intent(this, RoomActivity::class.java)
        }else{
            Intent(this, OfflineActivity ::class.java)
        }


        intent.putExtra(Constant.ROOM_ID, roomId)
        intent.putExtra(Constant.USER_ID, userID)
        intent.putExtra(Constant.ROOM_INFO, roomInfo)
        intent.putExtra(Constant.SELFINSURANCE, selfInsurance)
        intent.putExtra(Constant.TASKID, taskId)
        intent.putExtra(Constant.RECORDTYPE, recordType)
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

    private fun requestRoom(
        isRemote: Boolean,
        flowId: String,
        membersArray: java.util.ArrayList<String>,
        selfInsurance: Boolean,
        taskId: String,
        recordType:String
    ) {


        val sdkVersion = TRTCCloud.getSDKVersion()
        LogUtils.i("sdkVersion", sdkVersion)
        showLoading()
        SystemHttpRequest.getInstance().startAgent(flowId, isRemote, membersArray ,recordType,
            object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                runOnUiThread {
                   hideLoading()
                    Handler().postDelayed({
                        val jsonObject = JSONObject(json)
                        val roomId = jsonObject.getString("roomId")
                        val agentIdStr = jsonObject.getString("agentId")

                        startEnterRoom(roomId,
                            agentIdStr,
                            jsonObject.toString(),
                            isRemote,
                            selfInsurance,
                            taskId,
                            recordType
                        )

                    }, 80)

                }


            }

            override fun onFail(err: String?, code: Int) {
                runOnUiThread {
                    hideLoading()
                    showToastMsg(err!!)
                }
            }

        })


    }

    var api: IWXAPI? = null

    public fun requestWebUrl(name :String,url :String){
        val wxWebpageObject = WXWebpageObject()
        wxWebpageObject.webpageUrl = url
        val msg = WXMediaMessage(wxWebpageObject).apply {
            title = if (TXSdk.getInstance().txConfig.miniprogramTitle.isNotEmpty()) {
                TXSdk.getInstance().txConfig.miniprogramDescription
            } else {
                "诚邀您完成投保签名"
            }

            description = if (TXSdk.getInstance().txConfig.miniprogramTitle.isNotEmpty()) {
                TXSdk.getInstance().txConfig.miniprogramDescription
            } else {

                "尊敬的客户${name}您好，${TXManagerImpl.instance!!.getFullName()}邀请您完成投保签名动作，感谢您的配合。"
            }
            thumbData = resources.openRawResource(R.raw.tx_icon_miniprogram_weburl).readBytes()
        }
        TXSdk.getInstance().wxTransaction = "miniProgram${System.currentTimeMillis()}"
        val req = SendMessageToWX.Req().apply {
            transaction = TXSdk.getInstance().wxTransaction
            message = msg
            scene = SendMessageToWX.Req.WXSceneSession
        }

        api?.sendReq(req)
    }

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
        private const val ARG_PARAM2 = "recordType"
        private const val ARG_PARAM3 = "workItemBean"

        @JvmStatic
        fun newInstance(context: Context,
                        applyStatusParams: Boolean?,
                        applyStatusParams1:String,
                        workItemBean: WorkItemBean
                        ) {
            val intent = Intent(context, InviteActivity::class.java)
            intent.putExtra(InviteActivity.ARG_PARAM1, applyStatusParams)
            intent.putExtra(InviteActivity.ARG_PARAM2, applyStatusParams1)
            intent.putExtra(InviteActivity.ARG_PARAM3, workItemBean)
            context.startActivity(intent)
        }
    }

}