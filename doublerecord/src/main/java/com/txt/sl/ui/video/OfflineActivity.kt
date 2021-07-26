package com.txt.sl.ui.video

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.Window
import android.view.WindowManager
import android.widget.*
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.core.BasePopupView
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.glide.TxGlide
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.common.widget.titlebar.sign.SignatureView
import com.common.widget.immersionbar.TxBarHide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.aai.AAIClient
import com.tencent.aai.audio.data.AudioRecordDataSource
import com.tencent.aai.auth.LocalCredentialProvider
import com.tencent.aai.config.ClientConfiguration
import com.tencent.aai.exception.ClientException
import com.tencent.aai.exception.ServerException
import com.tencent.aai.listener.AudioRecognizeResultListener
import com.tencent.aai.model.AudioRecognizeRequest
import com.tencent.aai.model.AudioRecognizeResult
import com.tencent.aai.model.type.AudioRecognizeConfiguration
//import com.tencent.cloud.ai.fr.sdksupport.YTSDKManager
import com.tencent.liteav.TXLiteAVCode
import com.tencent.qcloudtts.LongTextTTS.LongTextTtsController
import com.tencent.qcloudtts.VoiceLanguage
import com.tencent.qcloudtts.VoiceSpeed
import com.tencent.qcloudtts.VoiceType
import com.tencent.qcloudtts.callback.QCloudPlayerCallback
import com.tencent.qcloudtts.callback.TtsExceptionHandler
import com.tencent.qcloudtts.exception.TtsNotInitializedException
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.*
import com.tencent.trtc.TRTCCloudListener
import com.txt.sl.R
import com.txt.sl.TXManagerImpl
import com.txt.sl.config.socket.SocketBusiness
import com.txt.sl.entity.bean.FileBean
import com.txt.sl.entity.bean.LevelItem1
import com.txt.sl.entity.bean.UploadShotPic
import com.txt.sl.entity.bean.UploadSignPic
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.receive.SystemBaiduLocation
import com.txt.sl.screenrecorder.ScreenRecordHelper
import com.txt.sl.system.SystemCommon
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.system.SystemLogHelper
import com.txt.sl.system.SystemSocket
import com.txt.sl.ui.adpter.CheckenvItemAdapter
import com.txt.sl.ui.adpter.ExpandableItem1Adapter
import com.txt.sl.ui.adpter.VideoDetailsItemAdapter
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.video.trtc.BusinessLayout
import com.txt.sl.ui.video.trtc.TRTCVideoLayout
import com.txt.sl.ui.video.trtc.TRTCVideoLayoutManager
import com.txt.sl.utils.*
import com.txt.sl.utils.FileUtils
import com.txt.sl.widget.PointBean
import kotlinx.android.synthetic.main.tx_activity_offline_room.*
import kotlinx.android.synthetic.main.tx_activity_offline_room.room_time
import kotlinx.android.synthetic.main.tx_activity_offline_room.tv_linkname
import kotlinx.android.synthetic.main.tx_activity_offline_room.tv_linknameindex
import kotlinx.android.synthetic.main.tx_activity_offline_room.tv_skip
import kotlinx.android.synthetic.main.tx_activity_offline_room.tv_text_continue
import kotlinx.android.synthetic.main.tx_page_3.*
import kotlinx.android.synthetic.main.tx_page_checkenv.*
import kotlinx.android.synthetic.main.tx_page_welcome1.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.DecimalFormat

/**
 * 线下视频模块
 */

class OfflineActivity : BaseActivity(), View.OnClickListener, SocketBusiness, TRTCVideoLayoutManager.IVideoLayoutListener, TRTCVideoLayout.IVideoLayoutListener {
    //设置为铃声模式
    //private var mLocalPreviewView //【控件】本地画面View
    //      : TXCloudVideoView? = null
    private var mBackButton //【控件】返回上一级页面
            : ImageView? = null
    private var mTRTCCloud // SDK 核心类
            : TRTCCloud? = null
    private var mIsFrontCamera = true // 默认摄像头前置
    private var mRemoteUidList // 远端用户Id列表
            : MutableList<String>? = null
    private var mRemoteViewList // 远端画面列表
            : MutableList<TXCloudVideoView>? = null
    private val mUserCount = 0 // 房间通话人数个数
    private var mRoomId // 房间Id
            : String? = null
    private var mUserId // 用户Id
            : String? = null

    private var mRoomInfo // 房间信息
            : String? = null

    override fun isFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }


    companion object {
        private const val TAG = "OfflineActivity"
        private const val REQ_PERMISSION_CODE = 0x1000
    }

    override fun initView() {

        statusBarConfig.hideBar(TxBarHide.FLAG_HIDE_STATUS_BAR)
        super.initView()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.mode = AudioManager.MODE_NORMAL
        // 算法初始化，并 log 版本信息
        // 模型全局加载必须先于 SDK 实例化
        // 算法初始化，并 log 版本信息
        // 模型全局加载必须先于 SDK 实例化
//        YTSDKManager.loadLibs() //加载动态库 .so
//
//        YTSDKManager.loadModels(assets) //模型全局加载

        handleIntent()
        // 先检查权限再加入通话
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        initView1()

        showPageCheck()
        inflate()
    }

    private fun showPageCheck() {
        hideView()
        tv_skip.visibility(true)
        tv_skip.text = "开始检测"
        ll_welcome.visibility(true)
    }


    override fun getLayoutId(): Int {
        return R.layout.tx_activity_offline_room
    }

    private fun handleIntent() {
        val intent = intent
        if (null != intent) {
            if (intent.getStringExtra(Constant.USER_ID) != null) {
                mUserId = intent.getStringExtra(Constant.USER_ID)
            }
            if (intent.getStringExtra(Constant.ROOM_ID) != null) {
                mRoomId = intent.getStringExtra(Constant.ROOM_ID)
            }
            if (intent.getStringExtra(Constant.ROOM_INFO) != null) {
                mRoomInfo = intent.getStringExtra(Constant.ROOM_INFO)
            }
        }

    }

    private var screenRecordHelper: ScreenRecordHelper? = null

    private fun initView1() {
        getServiceId()
        SystemSocket.instance?.connectSocket()
        SystemSocket.instance?.setonSocketListener(this)
        mBackButton = findViewById(R.id.trtc_ic_back)

        mRemoteUidList = ArrayList()
        mRemoteViewList = ArrayList()

        mBackButton?.setOnClickListener(this)

        initBusiness()
        initAbsCredentialProvider()
        SystemHttpRequest.getInstance().getAllSteps(mServiceId, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                //获取全部节点的数据
                val jsonObject = JSONObject(json)
                val ruleJsonArray = jsonObject.getJSONArray("rule")


            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }

    interface RoomHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
    }

    interface PhotoHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
    }

    //        processIndex: { type: Number },  //当前节点环节下标
    //        stepIndex: { type: Number },  //当前节点下标
    //        videoFrom: { type: Number }, //当前节点从视频第几秒开始
    //        videoTo: { type: Number },//当前节点在视频第几秒结束
    //        autoCheckType: { type: String },//智能检测通过类型，"Button"为用户按钮确认通过，"System"为系统自动质检通过
    //        autoCheck: { type: Boolean }, //智能质检通过与否
    //        check: { type: Boolean } // 该值与autoCheck保持一致
    private fun setCheckJson(processIndexInt: Int,
                             stepIndexInt: Int,
                             videoFromInt: Long,
                             videoTo: Long,
                             autoCheckTypeStr: String,
                             autoCheckBoo: Boolean,
                             checkBoo: Boolean,
                             failType: String,
                             failReason: String
    ): JSONObject {
        val jsonOb = JSONObject().apply {
            put("check", JSONObject().apply {
                put("processIndex", processIndexInt)
                put("stepIndex", stepIndexInt)
                put("videoFrom", videoFromInt)
                put("videoTo", videoTo)
                put("autoCheckType", autoCheckTypeStr)
                put("autoCheck", autoCheckBoo)
                put("check", checkBoo)
                put("autoFailType", failType) //失败类型
                put("autoFailReason", failReason) //失败原因
            })
        }

        return jsonOb
    }

    public fun nextStep(isPassed: Boolean, jsonOb: JSONObject, listener: RoomHttpCallBack) {
        try {
            jsonOb.put("serviceId", mServiceId)
            jsonOb.put("isPassed", isPassed)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        SystemHttpRequest.getInstance().nextStep(jsonOb.toString(), object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                runOnUiThread {
                    listener.onSuccess(json!!)
                }

            }

            override fun onFail(err: String?, code: Int) {
                runOnUiThread {
                    listener.onFail(err, code)
                    ToastUtils.showLong(err!!)
                    if (-3 == code) {

                        SystemSocket.instance?.setMSG(TXManagerImpl.instance?.getLoginName()!!)
                    }
                }
            }

        })
    }

    public fun nextStep(isPassed: Boolean, listener: RoomHttpCallBack) {

        nextStep(isPassed, JSONObject(), listener)
    }


    public fun startRecord(listener: RoomHttpCallBack) {

        val jsonObject = JSONObject()
        try {
            jsonObject.put("serviceId", mServiceId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        SystemHttpRequest.getInstance().startRecord(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                runOnUiThread {

                    listener.onSuccess(json!!)
                }

            }

            override fun onFail(err: String?, code: Int) {
                runOnUiThread {
                    listener.onFail(err, code)
                    ToastUtils.showLong(err!!)
                }
            }

        })
    }

    public fun endRecord(listener: RoomHttpCallBack, disableState: Boolean) {

        val jsonObject = JSONObject()
        try {
            jsonObject.put("serviceId", mServiceId)
            jsonObject.put("disableState", disableState)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (isConnect) {
            SystemHttpRequest.getInstance().endRecord(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {
                        listener.onSuccess(json!!)
                    }

                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        listener.onFail(err, code)
                    }
                }

            })

        } else {
            listener.onSuccess("")
        }
    }

    var mServiceId = ""
    var mFlowId = ""
    var userName = ""
    var agentName = ""
    var insuredName = ""
    var policyholderName = ""
    var roomPerson = ""
    var canCheck = false //判断是否为第一次点击下一步触发定时器
    var jsonObject1: JSONObject? = null
    var mAppid = 0L
    var mSecretId = ""
    var mSecretKey = ""
    fun getServiceId() {
        jsonObject1 = JSONObject(mRoomInfo)
        mServiceId = jsonObject1!!.optString("serviceId", "")
        mFlowId = jsonObject1!!.optString("flowId", "")
        agentName = jsonObject1!!.optString("agentName", "")
        policyholderName = jsonObject1!!.optString("policyholderName", "")
        insuredName = jsonObject1!!.optString("insuredName", "")
        val wxCloudConfJO = jsonObject1!!.getJSONObject("wxCloudConf")
        mSecretId = wxCloudConfJO!!.getString("SecretId")
        mSecretKey = wxCloudConfJO!!.getString("SecretKey")
        mAppid = wxCloudConfJO!!.getString("AppId").toLong()
        initTtsController()

    }

    var longTextTtsController: LongTextTtsController? = null
    fun initTtsController() {
        longTextTtsController = LongTextTtsController()
        longTextTtsController?.init(
                this,
                mAppid,
                mSecretId,
                mSecretKey
        )
        longTextTtsController?.apply {
            setVoiceSpeed(VoiceSpeed.VOICE_SPEED_NORMAL.getNum())
            setVoiceType(VoiceType.VOICE_TYPE_AFFNITY_FEMALE.getNum())
            setVoiceLanguage(VoiceLanguage.VOICE_LANGUAGE_CHINESE.getNum())
            setProjectId(0)
        }
    }

    var orderDetailsItemlists = ArrayList<String>()
    var baseQuickAdapter: VideoDetailsItemAdapter? = null
    var mAllProcessIndex = 0

    @SuppressLint("SetTextI18n")
    fun initRecyclerview(jsonArray: JSONArray) {
        orderDetailsItemlists.clear()
        for (index in 0 until jsonArray.length()) {
            val s = jsonArray.get(index) as String
            orderDetailsItemlists.add(s)
        }
        recyclerview.layoutManager = LinearLayoutManager(this)
        baseQuickAdapter = VideoDetailsItemAdapter(orderDetailsItemlists!!)
        recyclerview.adapter = baseQuickAdapter
        tv_welcome_title.text = "录制过程中包含如下${orderDetailsItemlists.size}个环节："
        mAllProcessIndex = orderDetailsItemlists.size
    }


    fun destroylongTextTtsController() {
        longTextTtsController?.stop()
    }


    fun startTtsController(ttsStr: String, callBack: RoomHttpCallBack) {
        try {
            val startTts = longTextTtsController?.startTts(ttsStr, mTtsExceHandler, object : QCloudPlayerCallback {
                override fun onTTSPlayStart() {

                }

                override fun onTTSPlayProgress(p0: String?, p1: Int) {
                }

                override fun onTTSPlayAudioCachePath(p0: String?) {
                }

                override fun onTTSPlayWait() {
                }

                override fun onTTSPlayNext() {
                }

                override fun onTTSPlayStop() {
                }

                override fun onTTSPlayEnd() {
                    callBack.onSuccess("")
                }

                override fun onTTSPlayResume() {
                }


            })
        } catch (e: TtsNotInitializedException) {
            LogUtils.i("${e.message}")
            callBack.onFail("", 0)
        }


    }

    private var mTtsExceHandler: TtsExceptionHandler = TtsExceptionHandler {

    }

    public fun sendMsg1(jsonObject: JSONObject, listener: RoomHttpCallBack) {


        SystemHttpRequest.getInstance().startAgent1(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                runOnUiThread {
                    listener.onSuccess(json!!)
                }

            }

            override fun onFail(err: String?, code: Int) {
                runOnUiThread {
                    listener.onFail(err, code)
                }
            }

        })
    }

    var isPassed = true
    var isStartRecord = false
    private fun initBusiness() {
//        tv_continue.setOnClickListener {
//
//            TxPopup.Builder(this).maxWidth(700).asConfirm("补充录制", "由于您还有疑问，现对您不通过的\n环节进行补充录制", "取消", "确认", object : OnConfirmListener {
//                override fun onConfirm() {
//                    mCurrentEndTimer = System.currentTimeMillis()
//
//                    mCurrentEndTime = mCurrentStartTime + (mCurrentEndTimer - mCurrentStartTimer) / 1000L
//
//                    LogUtils.i("quickEnterRoom------当前节点第:${mCurrentStartTime}秒开始-----当前节点第:${mCurrentEndTime}秒结束")
//
//                    nextStep(isPassed, object : OfflineActivity.RoomHttpCallBack {
//                        override fun onSuccess(json: String?) {
//                            mCurrentStartTimer = mCurrentEndTimer
//                            mCurrentStartTime = mCurrentEndTime
//                            tv_continue.visibility(visibility = false)
//                            tv_text_continue.visibility(false)
//                        }
//
//                        override fun onFail(err: String?, code: Int) {
//
//                        }
//
//                    })
//                }
//
//            }, null, false).show()
//        }
        tv_skip.setOnClickListener(CheckDoubleClickListener {
            it as TextView
            when (it.text) {
                "开始检测" -> {
                    //点击开始检测
                    showPageTwo()
                }
                "下一步" -> {
                    //开始录屏，先获取权限，然后再开始录制
                    if (startAutoNextStepTimer != null) {
                        startAutoNextStepTimer?.cancel()
                        startAutoNextStepTimer = null
                    }
                    quickEnterRoom(isSystem = true)

                }
                "开始录制" -> {  //当前页面 --双录开始前沟通-2
                    requestRecordPer()

                }
                "确认完成" -> {  //当前页面 --双录开始前沟通-2
                    endRecord(object : RoomHttpCallBack {
                        override fun onSuccess(json: String?) {
                            runOnUiThread {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    screenRecordHelper?.apply {
                                        if (isRecording) {
                                            stopRecord()
                                        }
                                    }
                                }
//
                                val jsonObject1 = JSONObject(mRoomInfo)
                                val flowId = jsonObject1.optString("flowId", "")
                                val df = DecimalFormat("#.000")
                                val mTimeMillis = (mStartRecordNetMillis - mStartRecordTimeMillis) / 1000f
                                var minSize = df.format(mTimeMillis)
                                val jsonObject = JSONObject().apply {
                                    put("flowId", flowId)
                                    put("preTime", minSize)
                                    put("serviceId", mServiceId)
                                    put("path", screenRecordHelper?.getSavaName())
                                }

                                TxSPUtils.put(this@OfflineActivity, flowId, jsonObject.toString())
                                //开始上传视频
                                //上传视频
                                val customDialog = UploadVideoDialog(this@OfflineActivity)
                                customDialog.setFlowId(jsonObject.toString())
                                customDialog.setOnConfirmClickListener(object : UploadVideoDialog.OnConfirmClickListener {
                                    override fun onVideoUpload(isFinish: Boolean) {
                                        if (isFinish) {
                                            finish()
                                        } else {
                                            //该视频上传失败保存下来
                                            TxSPUtils.put(this@OfflineActivity, flowId, jsonObject.toString())
                                            finish()
                                        }
                                    }

                                })
                                TxPopup.Builder(this@OfflineActivity).setPopupCallback(object : XPopupCallback {
                                    override fun onCreated() {

                                    }

                                    override fun beforeShow() {
                                    }

                                    override fun onShow() {

                                    }

                                    override fun onDismiss() {
                                    }

                                    override fun onBackPressed(): Boolean {
                                        return true
                                    }

                                }).asCustom(customDialog).show()
                            }
                        }

                        override fun onFail(err: String?, code: Int) {
                            runOnUiThread {
                                LogUtils.d(err!!)
                                ToastUtils.showShort(err)
                            }
                        }

                    }, disableState = false)


                }
                else -> {
                }
            }
        })


    }

    //    var allocCloudVideoView: TXCloudVideoView? = null
    var trtcvideolayout: TRTCVideoLayout? = null

    @SuppressLint("SetTextI18n")
    private fun enterRoom() {

        ll_showLink.visibility(false)
        offlineVideoLayoutManager.visibility(true)
        val allocCloudVideoView1 = offlineVideoLayoutManager.allocCloudVideoView(mUserId, "agent", TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)

        val findEntityBytype = offlineVideoLayoutManager.findEntityBytype("agent")
        trtcvideolayout = findEntityBytype.layout as TRTCVideoLayout
        trtcvideolayout!!.setLocationStr(
                SystemBaiduLocation.instance?.getLocationInfo()?.city + SystemBaiduLocation.instance?.getLocationInfo()?.province
        )


        mTRTCCloud = TRTCCloud.sharedInstance(applicationContext)

        mTRTCCloud?.setListener(TRTCCloudImplListener(this@OfflineActivity))


        LogUtils.i("width--${allocCloudVideoView1?.width}-----height--${allocCloudVideoView1?.height}")
        // 初始化配置 SDK 参数
        val trtcParams = TRTCParams()
        trtcParams.sdkAppId = jsonObject1!!.getInt("sdkAppId")
        trtcParams.userId = mUserId
        trtcParams.roomId = mRoomId?.toInt()!!
        // userSig是进入房间的用户签名，相当于密码（这里生成的是测试签名，正确做法需要业务服务器来生成，然后下发给客户端）
        trtcParams.userSig = jsonObject1!!.getString("agentSig")
        trtcParams.role = TRTCCloudDef.TRTCRoleAnchor

        // 进入通话
        mTRTCCloud?.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)

        mTRTCCloud?.startLocalPreview(mIsFrontCamera, allocCloudVideoView1)

        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360
        encParam.videoFps = Constant.VIDEO_FPS
        encParam.videoBitrate = Constant.RTC_VIDEO_BITRATE
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE

        mTRTCCloud?.setVideoEncoderParam(encParam)
        trtcvideolayout?.setIVideoLayoutListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {

        destroylongTextTtsController()
        SystemLogHelper.getInstance().stop()
        exitRoom()
        cancelAbsCredentialProvider()
        cancelTitleTimer()
        checkPhotoInRemotVideoTimer?.cancel()
        checkPhotoInRemotVideoTimer = null
        checkPhotoInVideoTimer?.cancel()
        checkPhotoInVideoTimer = null
        checkPhotoInRemotVideoTimer1?.cancel()
        checkPhotoInRemotVideoTimer1 = null

        mCheckLocal = false
        mRemoteCheck = false

        checkLocalTimer?.cancel()
        checkLocalTimer = null
        checkRemoteTimer?.cancel()
        checkRemoteTimer = null
        if (aaiClient != null) {
            try {
                aaiClient?.release()
            } catch (e: java.lang.Exception) {

            }

        }
        super.onDestroy()

    }


    /**
     * 离开通话
     */
    private fun exitRoom() {


        SystemSocket.instance?.disconnectSocket()
        if (mTRTCCloud != null) {
            mTRTCCloud!!.stopLocalAudio()
            mTRTCCloud!!.stopLocalPreview()
            mTRTCCloud!!.exitRoom()
            //销毁 trtc 实例
            if (mTRTCCloud != null) {
                mTRTCCloud!!.setListener(null)
            }
            mTRTCCloud = null
            TRTCCloud.destroySharedInstance()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScreenRecordHelper.REQUEST_CODE -> {
                LogUtils.i("REQUEST_CODE")
                if (data != null) {

                    screenRecordHelper?.onActivityResult(requestCode, resultCode, data!!)
                } else {
                    ToastUtils.showLong("拒绝录屏，退出房间！")
                    finish()
                }


            }
            else -> {
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_page_voice_result_retry -> {
                sendMsg1(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
            }


            R.id.trtc_ic_back -> {
                //当前在录屏
                end()
            }
            else -> {
            }
        }

    }

    private fun end() {

        TxPopup.Builder(this).maxWidth(700).asConfirm("退出", "确认退出？", "取消", "确认", object : OnConfirmListener {
            override fun onConfirm() {

                endRecord(object : RoomHttpCallBack {
                    override fun onSuccess(json: String?) {
                        runOnUiThread {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                screenRecordHelper?.cancelRecord()
                            }
                            finish()
                        }
                    }

                    override fun onFail(err: String?, code: Int) {
                        runOnUiThread {
                            LogUtils.d(err!!)
                            ToastUtils.showShort(err)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                screenRecordHelper?.cancelRecord()
                            }

                            finish()
                        }
                    }

                }, true)
            }

        }, null, false).show()
    }


    var isConnect = true

    private inner class TRTCCloudImplListener(activity: OfflineActivity) : TRTCCloudListener() {
        override fun onEnterRoom(result: Long) {
            super.onEnterRoom(result)
            LogUtils.i("onEnterRoom-----result----$result")
//            tv_local_video.text = "代理人：$agentName"
            mStartRecordTimeMillis = System.currentTimeMillis()
            LogUtils.i("mStartRecordTimeMillis-----$mStartRecordTimeMillis")
            checkPhotoInVideo()
        }

        override fun onExitRoom(reason: Int) {
            super.onExitRoom(reason)
            LogUtils.i("onEnterRoom-----result----$reason")
        }


        private val mContext: WeakReference<OfflineActivity>

        override fun onNetworkQuality(localQuality: TRTCQuality?, remoteQuality: ArrayList<TRTCQuality>?) {
            super.onNetworkQuality(localQuality, remoteQuality)

        }

        override fun onRemoteUserLeaveRoom(p0: String?, p1: Int) {
            super.onRemoteUserLeaveRoom(p0, p1)
            LogUtils.i("RoomActivity", "onRemoteUserLeaveRoom-----p0----$p0,p1---$p1")


        }

        override fun onRemoteUserEnterRoom(p0: String?) {
            super.onRemoteUserEnterRoom(p0)
            LogUtils.i("RoomActivity", "onRemoteUserEnterRoom----p0----$p0")

        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            LogUtils.i("RoomActivity", "onUserVideoAvailable userId $userId, mUserCount $mUserCount,available $available")
            val index = mRemoteUidList!!.indexOf(userId)
            if (available) {
                if (index != -1) { //如果mRemoteUidList有，就不重复添加
                    return
                }
                mRemoteUidList!!.add(userId)
                refreshRemoteVideoViews(true, userId)
                cancleRemoteTimer()
            } else {
                if (index == -1) { //如果mRemoteUidList没有，说明已关闭画面
                    return
                }
                /// 关闭用户userId的视频画面
                if (mTRTCCloud != null) {

                    mTRTCCloud!!.stopRemoteView(userId)
                    mRemoteUidList!!.removeAt(index)
                    refreshRemoteVideoViews(false, userId)
                    //开始记录 如果30s内 改没有提示 则提示退出录屏
                    starCheckRemoteTimer()
                }

            }
        }

        override fun onConnectionRecovery() {
            super.onConnectionRecovery()
            //SDK 跟服务器的连接恢复
            isConnect = true
            LogUtils.i("onConnectionRecovery");
        }

        override fun onConnectionLost() {
            super.onConnectionLost()
            //SDK 跟服务器的连接断开
            isConnect = false
            starLocalTimer()
            LogUtils.i("onConnectionLost");
        }

        override fun onTryToReconnect() {
            super.onTryToReconnect()
            LogUtils.i("onTryToReconnect");
        }

        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            super.onUserAudioAvailable(userId, available)
            LogUtils.i("RoomActivity", "onUserAudioAvailable userId $userId, mUserCount $mUserCount,available $available")
            if (available) {
                if (userId == mInsurantId) {

//                    remotevideo_tv.visibility(false)

                } else if (userId == mInsuredId) {
//                    remotevideo1_tv.visibility(false)

                }

            } else {
                if (userId == mInsurantId) {
                    checkPhotoInRemotVideoTimer?.cancel()
                    checkPhotoInRemotVideoTimer = null
//                    remotevideo_tv.visibility(true)
//                    remotevideo_tv.text = "投保人连接异常\n 正在恢复中请稍后"
                } else if (userId == mInsuredId) {
                    checkPhotoInRemotVideoTimer1?.cancel()
                    checkPhotoInRemotVideoTimer1 = null
//                    remotevideo1_tv.visibility(true)
//                    remotevideo1_tv.text = "被投保人连接异常\n 正在恢复中请稍后"
                }

            }


        }


        var mInsurantId = ""
        var mInsuredId = ""
        private fun refreshRemoteVideoViews(isShow: Boolean, userId: String) {
            if (isShow) {
                //判断哪个个账号
                SystemHttpRequest.getInstance().getRoomInfo(mServiceId, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        //{"errCode":0,"result":{"agentId":"5ee88000c488564bd8621344","insurantId":"opN2-4hPLdm9wVtNkD5_xxH8YUuI"}}
                        LogUtils.i("$json")
                        val resultObject = JSONObject(json)
                        //投保人Id
                        if (resultObject.has("insurantId")) {
                            mInsurantId = resultObject.getString("insurantId")
                            if (mInsurantId.equals(userId)) {
                                runOnUiThread {
                                    mRemoteViewList!![0].visibility = View.VISIBLE
//                                    remotevideo_tv.visibility(false)
                                    // 开始显示用户userId的视频画面
//                                    tv_remote_video.text = "投保人：$userName"
                                    mTRTCCloud!!.startRemoteView(mInsurantId, mRemoteViewList!![0])
                                    mTRTCCloud!!.muteRemoteAudio(mInsurantId, false)
                                }
                            }


                        } else {

                        }
                        //被保人id
                        if (resultObject.has("insuredId")) {
                            mInsuredId = resultObject.getString("insuredId")
                            if (mInsuredId.equals(userId)) {
                                runOnUiThread {
                                    mRemoteViewList!![1].visibility = View.VISIBLE
//                                    remotevideo1_tv.visibility(false)
//                                    tv_remote_video1.text = "被保人：$insuredName"
                                    mTRTCCloud!!.startRemoteView(mInsuredId, mRemoteViewList!![1])
                                    mTRTCCloud!!.muteRemoteAudio(mInsuredId, false)
                                }
                            }


                        } else {

                        }

                    }

                    override fun onFail(err: String?, code: Int) {
                    }

                })
            } else {

                if (userId == mInsurantId) {
                    checkPhotoInRemotVideoTimer?.cancel()
                    checkPhotoInRemotVideoTimer = null
//                    remotevideo_tv.visibility(true)
//                    remotevideo_tv.text = "投保人连接异常\n 正在恢复中请稍后"
                    mRemoteViewList!![0].visibility = GONE
                } else if (userId == mInsuredId) {
                    checkPhotoInRemotVideoTimer1?.cancel()
                    checkPhotoInRemotVideoTimer1 = null
//                    remotevideo1_tv.visibility(true)
//                    remotevideo1_tv.text = "被投保人连接异常\n 正在恢复中请稍后"
                    mRemoteViewList!![1].visibility = GONE
                }


            }


        }

        // 错误通知监听，错误通知意味着 SDK 不能继续运行
        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            Log.d(Companion.TAG, "sdk callback onError")
            val activity = mContext.get()
            if (activity != null) {
                Toast.makeText(activity, "onError: $errMsg[$errCode]", Toast.LENGTH_SHORT).show()
                LogUtils.d("onError: ", "onError: $errMsg[$errCode]")
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom()
                }
            }
        }

        init {
            mContext = WeakReference(activity)
        }
    }

    fun requestRecordPer() {
        if (screenRecordHelper == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                screenRecordHelper = ScreenRecordHelper(this@OfflineActivity, object : ScreenRecordHelper.OnVideoRecordListener {
                    override fun onBeforeRecord() {
                    }

                    override fun onStartRecord() {
                        startTitleTimer()
                        enterRoom() //首次启动，权限都获取到，才能正常进入通话
                        var isSelf = false
                        val jsonObject = JSONObject(mRoomInfo)
                        var agentID = jsonObject.optString("agentID", "")
                        var policyholderID = jsonObject.optString("policyholderID", "")
                        var title = if (agentID.equals(policyholderID)) {
                            //代理人和投保人身份证一致，判断为自保件
                            isSelf = true
                            getString(R.string.tx_title_ready_oneperson)
                        } else {
                            isSelf = false
                            getString(R.string.tx_title_ready)
                        }
                        trtcvideolayout?.setPersonView(isSelf)
                        showReadNextPage("", title)
                        trtcvideolayout?.setll_remote_skip("调整好位置后，请点击【下一步】", View.VISIBLE, View.VISIBLE)
                        trtcvideolayout?.setPersonView(View.VISIBLE)
                    }

                    override fun onCancelRecord() {
                    }

                    override fun onEndRecord() {
                    }

                }, TxPathUtils.getExternalStoragePath() + "/txsl/video")
            }

        }

        screenRecordHelper?.apply {
            if (!isRecording) {
                recordAudio = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startRecord()
                }
            }
        }
    }


    private fun showPageOne() {
        hideView()
        room_time.visibility(true)
        ll_showLink.visibility(true)
        tv_skip.visibility(true)
        tv_skip.text = "开始录制"
        val jsonArray = jsonObject1!!.getJSONArray("process")
        initRecyclerview(jsonArray)

    }

    private var audioManager: AudioManager? = null
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {


        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {

                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP)

                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {

                audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP)
                return true
            }
        }
        return true

    }

    private fun showPageTwo() {
        tv_skip.visibility(false)
        ll_welcome.visibility(false)
        hideView()
        ll_showLink.visibility(false)
        page_checkenv.visibility(true)
        initCheckEnvRecyclerView()

    }

    private fun startCheckEnv(){
        pagetwo_count.visibility(false)
        ll_checkenv.visibility(false)
        tv_checkenvstate.text = "监测中"
        tv_checkenvstate.background = ContextCompat.getDrawable(this@OfflineActivity,R.drawable.tx_checkenv_bg)
        val envData = CheckEnvUtils.getInstance().getEnvData()
        CheckEnvUtils.getInstance().startCheckEnv(this,false)
        checkenvItemAdapter?.setNewData(envData)
        var timer = object : CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                CheckEnvUtils.getInstance().getCheckEnv(this@OfflineActivity,false)
                checkenvItemAdapter?.notifyDataSetChanged()
                CheckEnvUtils.getInstance().stopCheckEnv(this@OfflineActivity)
                if (CheckEnvUtils.getInstance().checkvolumeAndMemory) {
                    //不展示 坚持录制按钮
                    tv_checkenv_start.visibility(true)
                }else{
                    tv_checkenv_start.visibility(false)
                }
                if (CheckEnvUtils.getInstance().checkEnvResult) {
                    //监测成功
                    pagetwo_count.visibility(true)
                    ll_checkenv.visibility(false)
                    tv_checkenvstate.text = "合格"
                    tv_checkenvstate.background = ContextCompat.getDrawable(this@OfflineActivity,R.drawable.tx_checkenv_pass)
                    startTimer1()
                }else{
                    pagetwo_count.visibility(false)
                    ll_checkenv.visibility(true)
                    tv_checkenvstate.text = "不合格"
                    tv_checkenvstate.background = ContextCompat.getDrawable(this@OfflineActivity,R.drawable.tx_checkenv_fail)
                }
            }
        }

        timer!!.start()
    }

    var checkenvItemAdapter : CheckenvItemAdapter ? = null
    private fun initCheckEnvRecyclerView() {

        checkenvItemAdapter = CheckenvItemAdapter()
        checkenv_recyclerview?.layoutManager = LinearLayoutManager(this)
        checkenv_recyclerview?.adapter = checkenvItemAdapter

        tv_checkenv_retry.setOnClickListener {
            startCheckEnv()
        }

        tv_checkenv_start.setOnClickListener {
            showPageOne()
        }
        tv_checkenv_exit.setOnClickListener {
            end()
        }
        startCheckEnv()
    }


    var page_readnextPage: View? = null
    var page_ttsPage: View? = null
    var page_agent_ocrPage: View? = null
    var page_idcaPage: View? = null
    var page_11Page: View? = null
    var page_signPage: View? = null
    var page_asr_resultPage: View? = null
    var page_endPage: View? = null
    var page_asr_userPage: View? = null
    var page_self_signPage: View? = null
    var page_local_signPage: View? = null

    fun inflate() {
        page_readnextPage = layoutInflater.inflate(R.layout.tx_page_readnext, null)
        page_ttsPage = layoutInflater.inflate(R.layout.tx_page_tts, null)
        page_agent_ocrPage = layoutInflater.inflate(R.layout.tx_page_agent_ocr, null)
        page_idcaPage = layoutInflater.inflate(R.layout.tx_page_idcomparison, null)
        page_11Page = layoutInflater.inflate(R.layout.tx_page_textread, null)
        page_signPage = layoutInflater.inflate(R.layout.tx_page_sign, null)
        page_asr_resultPage = layoutInflater.inflate(R.layout.tx_page_asr_result, null)
        page_endPage = layoutInflater.inflate(R.layout.tx_page_end, null)
        page_asr_userPage = layoutInflater.inflate(R.layout.tx_page_asr_user, null)
        page_self_signPage = layoutInflater.inflate(R.layout.tx_page_self_sign, null)
        page_local_signPage = layoutInflater.inflate(R.layout.tx_page_local_sign, null)
        page_asr_userPage!!.findViewById<TextView>(R.id.ll_page_voice_result_retry).setOnClickListener(this)
    }


    var isCacheLeftVideo = false //记录上一次视频位置，默认是左边

    //切换左边的视图到右边
    fun checkLeftVideoToRightScreen(view: View, checkVideoToRight: Boolean) {
        trtcvideolayout?.setPersonView(View.GONE)
        trtcvideolayout?.setll_remote_skip("", View.GONE, View.GONE)
        val findEntityBytype = offlineVideoLayoutManager.findEntityBytype("")
        val businessLayout = findEntityBytype.layout as BusinessLayout
        businessLayout.contentView.removeAllViews()
        businessLayout.contentView.addView(view)
        if (isCacheLeftVideo == checkVideoToRight) {

        } else {
            offlineVideoLayoutManager.makeFullVideoView(1)
        }

        isCacheLeftVideo = checkVideoToRight

    }

    fun showWatingPage(title: String, titleContent: String) {
        startTtsController(title, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {

            }

            override fun onFail(err: String?, code: Int) {
            }

        })
        checkLeftVideoToRightScreen(page_readnextPage!!, false)

        page_readnextPage?.findViewById<TextView>(R.id.tv_readNext_title)?.text = title
        page_readnextPage?.findViewById<TextView>(R.id.tv_readNext_content)?.text = titleContent

        trtcvideolayout?.setll_remote_skip(title, View.VISIBLE, View.VISIBLE)
    }

    //展示纯文本展示  左边视频右边提示
    private fun showReadNextPage(title: String, titleContent: String) {
        page_readnextPage?.visibility(true)
        tv_skip.visibility(false)
        checkLeftVideoToRightScreen(page_readnextPage!!, false)
        page_readnextPage?.findViewById<TextView>(R.id.tv_readNext_title)?.text = title
        page_readnextPage?.findViewById<TextView>(R.id.tv_readNext_content)?.text = titleContent

    }

    //展示纯文本展示  左边视频右边提示
    private fun showTTSNext(contentStr: String) {
        tv_skip.visibility(false)
        checkLeftVideoToRightScreen(page_ttsPage!!, false)
        page_ttsPage?.findViewById<TextView>(R.id.tts_page_content)?.text = contentStr
        //播报
        startTtsController(contentStr, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                //播报完成
                //自动跳到下一个节点
                autoCheckBoolean = true
                setFailType("", "")
                quickEnterRoom(true)
            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }

    //展示语音识别 左边提示右边视频
    private fun showUserASR(fillterData: String) {
        tv_skip.visibility(false)
        checkLeftVideoToRightScreen(page_asr_userPage!!, true)
        page_asr_userPage!!.findViewById<TextView>(R.id.tv_agent_content).text = fillterData
        page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text = ""
        page_asr_userPage!!.findViewById<LinearLayout>(R.id.page_asr_voice).visibility(true)
        page_asr_userPage!!.findViewById<LinearLayout>(R.id.ll_page_voice_result).visibility(false)
        page_asr_userPage!!.findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(false)
        page_asr_userPage!!.findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(false)


        pageVoice()
    }


    fun cancleTimer() {
        llPage8timer?.cancel()
        llPage8timer = null
        llPage9timer?.cancel()
        llPage9timer = null

    }

    var llPage8timer: CountDownTimer? = null
    var handler: Handler? = null

    var llPage9timer: CountDownTimer? = null
    var mWatingArray: JSONArray? = null
    private fun showidComparisonPage(title: String, jsonArray: JSONArray, isRetry: Boolean) {
        mFailCacheArray.clear()
        mSuccessCacheArray.clear()
        if (isRetry) {
            trtcvideolayout?.setll_page_voice_result(View.GONE, false, "")
        }
        mWatingArray = jsonArray
        tv_skip.visibility(false)
        startTtsController(title, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {

            }

            override fun onFail(err: String?, code: Int) {
            }
        })
        handler?.removeCallbacksAndMessages(null)
        llPage8timer?.cancel()
        llPage8timer = null

        checkLeftVideoToRightScreen(page_idcaPage!!, false)
        page_idcaPage!!.findViewById<TextView>(R.id.ll_page9_title).text = title
        trtcvideolayout?.startRoundView()
        trtcvideolayout?.setHollowOutView(View.VISIBLE)
        Handler().postDelayed({
            if (jsonArray.length() == 0) {
                ToastUtils.showShort("作用对象为空，请配置")
                trtcvideolayout?.stopRoundView()
                trtcvideolayout?.setHollowOutView(View.GONE)
            } else {
                mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                    if (jsonArray.length() == 1) {
                        //对单个人进行人脸识别
                        val tagrtOb = jsonArray.getString(0)
                        val byteToBitmap = SystemCommon.getInstance().byteToBitmap(p0)
                        val jsonObject = JSONObject(mRoomInfo)
                        val serviceId = jsonObject.optString("serviceId", "")
                        var name = ""
                        var agentID = ""
                        if ("agent" == tagrtOb) {
                            name = jsonObject.optString("agentName", "")
                            agentID = jsonObject.optString("agentID", "")
                        } else if ("policyholder" == tagrtOb) {
                            name = jsonObject.optString("policyholderName", "")
                            agentID = jsonObject.optString("policyholderID", "")
                        }
                        val encode = Base64.encode(byteToBitmap, Base64.DEFAULT)
                        val bulider = StringBuilder("data:image/png;base64,")
                        bulider.append(String(encode))

                        val replace = bulider.toString().replace("\n", "")
                        val uploadShotPic = UploadShotPic()
                        uploadShotPic.serviceId = serviceId
                        uploadShotPic.facePhoto = replace
                        uploadShotPic.idCardNum = agentID
                        uploadShotPic.name = name
                        SystemHttpRequest.getInstance().agentIdCard(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
                            override fun onSuccess(json: String?) {

                                val jsonObject = JSONObject(json)
                                val status = jsonObject.getString("status")
                                val jsonObject2 = if ("1" == status) {
                                    JSONObject().apply {
                                        put("type", "idComparison")
                                        put("serviceId", serviceId)
                                        put("step", JSONObject().apply {
                                            put("data", JSONObject().apply {
                                                put("roomMessage", "识别失败")
                                            })
                                            put("roomType", "idComparison-fail")
                                            put("userId", tagrtOb)
                                        })

                                    }
                                } else {
                                    JSONObject().apply {
                                        put("type", "idComparison")
                                        put("serviceId", serviceId)
                                        put("step", JSONObject().apply {
                                            put("data", JSONObject().apply {
                                                put("roomMessage", "识别成功")
                                            })
                                            put("roomType", "idComparison-success")
                                            put("userId", tagrtOb)
                                        })

                                    }
                                }
                                sendMsg1(jsonObject2, object : OfflineActivity.RoomHttpCallBack {
                                    override fun onSuccess(json: String?) {
                                        runOnUiThread {
                                        }
                                    }

                                    override fun onFail(err: String?, code: Int) {
                                        runOnUiThread {
                                        }
                                    }
                                })
                                runOnUiThread {
                                    trtcvideolayout?.stopRoundView()
                                    trtcvideolayout?.setHollowOutView(View.GONE)
                                }

                            }

                            override fun onFail(err: String?, code: Int) {
                                runOnUiThread {
                                    trtcvideolayout?.stopRoundView()
                                    trtcvideolayout?.setHollowOutView(View.GONE)
                                }

                            }

                        })


                    } else {
                        SystemCommon.getInstance().cropBitmap(p0, object : SystemCommon.BitmapLister {
                            override fun onByteListener(bytes: ByteArray?, bytes1: ByteArray?) {


                                val jsonObject = JSONObject(mRoomInfo)
                                val serviceId = jsonObject.optString("serviceId", "")
                                val agentName = jsonObject.optString("agentName", "")
                                val agentID = jsonObject.optString("agentID", "")

                                val policyholderName = jsonObject.optString("policyholderName", "")
                                val policyholderID = jsonObject.optString("policyholderID", "")
                                val encode = Base64.encode(bytes, Base64.DEFAULT)
                                val bulider = StringBuilder("data:image/png;base64,")
                                bulider.append(String(encode))

                                val replace = bulider.toString().replace("\n", "")
                                val uploadShotPic = UploadShotPic()
                                uploadShotPic.serviceId = serviceId
                                uploadShotPic.facePhoto = replace
                                uploadShotPic.idCardNum = agentID
                                uploadShotPic.name = agentName
                                SystemHttpRequest.getInstance().agentIdCard(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                        val jsonObject = JSONObject(json)
                                        val status = jsonObject.getString("status")
                                        val jsonObject2 = if ("1" == status) {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "识别失败")
                                                    })
                                                    put("roomType", "idComparison-fail")
                                                    put("userId", "agent")
                                                })

                                            }
                                        } else {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "识别成功")
                                                    })
                                                    put("roomType", "idComparison-success")
                                                    put("userId", "agent")
                                                })

                                            }
                                        }
                                        sendMsg1(jsonObject2, object : OfflineActivity.RoomHttpCallBack {
                                            override fun onSuccess(json: String?) {
                                                runOnUiThread {
                                                }
                                            }

                                            override fun onFail(err: String?, code: Int) {
                                                runOnUiThread {
                                                }
                                            }
                                        })

                                    }

                                    override fun onFail(err: String?, code: Int) {

                                        val jsonObject2 = JSONObject().apply {
                                            put("type", "idComparison")
                                            put("serviceId", serviceId)
                                            put("step", JSONObject().apply {
                                                put("data", JSONObject().apply {
                                                    put("roomMessage", "识别失败")
                                                })
                                                put("roomType", "idComparison-fail")
                                                put("userId", "agent")
                                            })

                                        }
                                        sendMsg1(jsonObject2, object : OfflineActivity.RoomHttpCallBack {
                                            override fun onSuccess(json: String?) {
                                                runOnUiThread {
                                                }
                                            }

                                            override fun onFail(err: String?, code: Int) {
                                                runOnUiThread {
                                                }
                                            }
                                        })
                                    }

                                })

                                val encode1 = Base64.encode(bytes1, Base64.DEFAULT)
                                val bulider1 = StringBuilder("data:image/png;base64,")
                                bulider1.append(String(encode1))
                                val replace1 = bulider1.toString().replace("\n", "")
                                val uploadShotPic1 = UploadShotPic()
                                uploadShotPic1.serviceId = serviceId
                                uploadShotPic1.facePhoto = replace1
                                uploadShotPic1.idCardNum = policyholderID
                                uploadShotPic1.name = policyholderName
                                SystemHttpRequest.getInstance().agentIdCard(uploadShotPic1, object : HttpRequestClient.RequestHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                        val jsonObject = JSONObject(json)
                                        val status = jsonObject.getString("status")
                                        val jsonObject2 = if ("1" == status) {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "识别失败")
                                                    })
                                                    put("roomType", "idComparison-fail")
                                                    put("userId", "policyholder")
                                                })

                                            }
                                        } else {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "识别成功")
                                                    })
                                                    put("roomType", "idComparison-success")
                                                    put("userId", "policyholder")
                                                })

                                            }
                                        }
                                        sendMsg1(jsonObject2, object : OfflineActivity.RoomHttpCallBack {
                                            override fun onSuccess(json: String?) {
                                                runOnUiThread {
                                                }
                                            }

                                            override fun onFail(err: String?, code: Int) {
                                                runOnUiThread {
                                                }
                                            }
                                        })

                                    }

                                    override fun onFail(err: String?, code: Int) {
                                        val jsonObject2 = JSONObject().apply {
                                            put("type", "idComparison")
                                            put("serviceId", serviceId)
                                            put("step", JSONObject().apply {
                                                put("data", JSONObject().apply {
                                                    put("roomMessage", "识别失败")
                                                })
                                                put("roomType", "idComparison-fail")
                                                put("userId", "policyholder")
                                            })

                                        }
                                        sendMsg1(jsonObject2, object : OfflineActivity.RoomHttpCallBack {
                                            override fun onSuccess(json: String?) {
                                                runOnUiThread {
                                                }
                                            }

                                            override fun onFail(err: String?, code: Int) {
                                                runOnUiThread {
                                                }
                                            }
                                        })

                                    }

                                })

                            }

                        })
                    }


                }
            }
        }, 3000)


    }

    private fun showTextReadPage(promtStr: String, fileUrl: String) {

        checkLeftVideoToRightScreen(page_11Page!!, true)
        page_11Page?.findViewById<TextView>(R.id.tv_prompt1)?.text = promtStr

        if (fileUrl.isNotEmpty()) {
            TxGlide.with(this)
                    .load(fileUrl)
                    .into(page_11Page?.findViewById<ImageView>(R.id.iv_textRead)!!)
        }
        page_11Page?.findViewById<View>(R.id.view)?.visibility(false)
        page_11Page?.findViewById<TextView>(R.id.tv_textread_skip)?.visibility(true)
        page_11Page?.findViewById<TextView>(R.id.tv_textread_skip)?.setOnClickListener {
//            autoCheckBoolean = true
            autoCheckBoolean = true
            setFailType("", "")
            quickEnterRoom(false)
        }
    }

    private fun showSignPage(jsonArray: JSONArray) {

        page_local_signPage?.findViewById<LinearLayout>(R.id.ll_clear)?.visibility(true)
        page_local_signPage?.findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(false)
        page_local_signPage?.findViewById<TextView>(R.id.tv_name_sign)!!.text = "签名："
        val checkName = when (jsonArray.getString(0)) {
            "policyholder" -> policyholderName
            "agent" -> agentName
            else -> {
                insuredName
            }
        }

        page_local_signPage?.findViewById<TextView>(R.id.tv_name)!!.text = when (jsonArray.getString(0)) {
            "policyholder" -> "投保人：" + checkName
            "agent" -> "代理人：" + checkName
            else -> {
                "被保人：" + checkName
            }
        }
        val signatureview = page_local_signPage?.findViewById<SignatureView>(R.id.iv_page_local_signatureview)
        checkLeftVideoToRightScreen(page_local_signPage!!, true)
        signatureview?.clear()
        //上传签字文件
        page_local_signPage?.findViewById<TextView>(R.id.tv_sign)?.setOnClickListener {
            upload(signatureview!!, object : PhotoHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {
                        LogUtils.i("upload------$json")
                        page_local_signPage?.findViewById<TextView>(R.id.tv_name_sign)!!.text = "签名：" + json
                        if (checkName == json) {
                            //识别成功，跳过
                            page_local_signPage?.findViewById<LinearLayout>(R.id.ll_clear)?.visibility(false)
                            page_local_signPage?.findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(false)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(true)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(false)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(false)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(false)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(false)
                            startAutoNextStep(true)
                        } else {
                            page_local_signPage?.findViewById<LinearLayout>(R.id.ll_clear)?.visibility(false)
                            page_local_signPage?.findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(true)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(false)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(true)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(true)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(true)
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(true)

                            //重试
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_mark)?.setOnClickListener {
                                quickEnterRoom(true)

                            }
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_jump)?.setOnClickListener {
                                quickEnterRoom(true)
                            }
                            page_local_signPage?.findViewById<TextView>(R.id.ll_page_voice_result_retry)?.setOnClickListener {
                                sendMsg1(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                    }

                                    override fun onFail(err: String?, code: Int) {

                                    }

                                })
                            }

                        }
                    }
                }

                override fun onFail(err: String?, code: Int) {

                }

            })

        }

        //清除
        page_local_signPage?.findViewById<TextView>(R.id.tv_clear)?.setOnClickListener {
            signatureview?.clear()
        }

    }


    fun upload(signatureview: SignatureView, callback: PhotoHttpCallBack) {
        val encode = Base64.encode(signatureview.save(), Base64.DEFAULT)

        val bulider = StringBuilder("data:image/png;base64,")
        bulider.append(String(encode))


        val replace = bulider.toString().replace("\n", "")
        val uploadShotPic = UploadSignPic()
        uploadShotPic.serviceId = mServiceId
        uploadShotPic.flowId = mFlowId
        uploadShotPic.signPic = replace
        SystemHttpRequest.getInstance().signPic(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                //{"errCode":0,"result":"王健泉"}
                callback.onSuccess(json)
            }

            override fun onFail(err: String?, code: Int) {
                callback.onFail(err, code)

            }

        })
    }

    private fun showPage20() {
//        hideView()
//        ll_page11.visibility(false)
//        ll_page12.visibility(true)

    }


    private fun pageVoice() {
        page_asr_userPage!!.apply {
            findViewById<TextView>(R.id.tv_user_content2).visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result).visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_retry).visibility(false)
        }

    }

    //展示录像计时器
    var timer: CountDownTimer? = null

    private fun startTitleTimer() {
        if (timer != null) {

        } else {
            timer = object : CountDownTimer(60000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    room_time.text = "当前时间：" + DateUtils.getCurrentTime()

                }

                override fun onFinish() {
                    timer!!.start()

                }
            }

            timer!!.start()
            room_time.visibility(true)
        }


    }

    fun showPageend() {
        page_endPage?.visibility(true)
        showNextStep("finishRecord")
        checkLeftVideoToRightScreen(page_endPage!!, true)
    }

    var list: java.util.ArrayList<MultiItemEntity>? = null
    var mExpandableItemAdapter: ExpandableItem1Adapter? = null
    fun initEndRecyclerview(jsonArray: JSONArray) {

        list = java.util.ArrayList()
        list?.clear()
        var isFailBoolean = false
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val name = jsonObject.getString("name")
            val stepsJsonArray = jsonObject.getJSONArray("steps")
            val isPubliclist: java.util.ArrayList<MultiItemEntity> = java.util.ArrayList<MultiItemEntity>()
            val isNoPublicItem = LevelItem1(isPubliclist, name)
            for (index1 in 0 until stepsJsonArray.length()) {
                val jsonObject1 = stepsJsonArray.getJSONObject(index1)
                val fileBean = FileBean()
                fileBean.name = jsonObject1.getString("name")
                fileBean.failType = jsonObject1.getString("autoFailType")
                fileBean.failReason = jsonObject1.getString("autoFailReason")
                isFailBoolean = true
                isNoPublicItem.addSubItem(
                        fileBean
                )
            }

            list?.add(isNoPublicItem)
        }
        if (isFailBoolean) {
//            tv_continue.visibility(true)
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI预质检：不合格"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.color_ED6656))
        } else {
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI预质检：合格"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.color_40D4A1))
        }
        LogUtils.i(list.toString())
        val recyclerView = page_endPage?.findViewById<RecyclerView>(R.id.endpage_recyclerview)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        mExpandableItemAdapter = ExpandableItem1Adapter(list!!)
        recyclerView?.adapter = mExpandableItemAdapter
    }

    //取消录像计时器
    private fun cancelTitleTimer() {
        timer?.cancel()
        timer = null
    }

    var timer1: CountDownTimer? = null
    var mStartRecordTimeMillis = 1L
    var mStartRecordNetMillis = 1L

    private fun startTimer1() {
        timer1 = object : CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                pagetwo_count.text = "${millisUntilFinished / 1000 + 1}s正在跳转至下一步"
            }

            override fun onFinish() {
                showPageOne()

            }
        }

        timer1!!.start()

    }


    var voiceTimer: CountDownTimer? = null
    private fun startVoiceTimer(millisInFuture: Long) {
        voiceTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                page_asr_userPage!!.findViewById<TextView>(R.id.tv_count_second).text = "${millisUntilFinished / 1000}S"
            }

            override fun onFinish() {
                //15s 未识别完
                //显示重试
                //识别成功
                cancelAbsCredentialProvider()

                sendMsg1(JSONObject().apply {
                    put("serviceId", mServiceId)
                    put("type", "soundOCR")

                    put("step", JSONObject().apply {
                        put("data", JSONObject().apply {
                            put("roomMessage", strBuffer.toString())
                        })
                        put("roomType", "soundOCR-fail")
                        put("target", targetJSONArray)
                    })
                }, object : RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })
                strBuffer.delete(0, strBuffer.length)
                LogUtils.i("onFinish", stepDataNode.toString())
            }
        }

        voiceTimer!!.start()

    }

    var checkRemoteTimer: CountDownTimer? = null
    private fun starCheckRemoteTimer() {

        if (checkRemoteTimer != null) return
        if (!isStartRecord) return
        checkRemoteTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                TxPopup.Builder(this@OfflineActivity).maxWidth(700).asConfirm("", "网络异常，结束双录！", "", "确定", object : OnConfirmListener {
                    override fun onConfirm() {
                        endRecord(object : RoomHttpCallBack {
                            override fun onSuccess(json: String?) {
                                runOnUiThread {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        screenRecordHelper?.cancelRecord()
                                    }
                                    finish()
                                }
                            }

                            override fun onFail(err: String?, code: Int) {
                                runOnUiThread {
                                    LogUtils.d(err!!)
                                    ToastUtils.showShort(err)
                                }
                            }

                        }, disableState = true)
                    }

                }, null, true).show()
            }
        }

        checkRemoteTimer!!.start()

    }


    var checkLocalTimer: CountDownTimer? = null
    private fun starLocalTimer() {
        checkLocalTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (!isConnect) {
                    TxPopup.Builder(this@OfflineActivity).maxWidth(700).asConfirm("", "网络异常，结束双录！", "", "确定", object : OnConfirmListener {
                        override fun onConfirm() {
                            endRecord(object : RoomHttpCallBack {
                                override fun onSuccess(json: String?) {
                                    runOnUiThread {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            screenRecordHelper?.cancelRecord()
                                        }

                                        finish()
                                    }
                                }

                                override fun onFail(err: String?, code: Int) {
                                    runOnUiThread {
                                        LogUtils.d(err!!)
                                        ToastUtils.showShort(err)
                                    }
                                }

                            }, true)
                        }

                    }, null, true).show()

                }
            }
        }

        checkLocalTimer!!.start()

    }

    private fun cancleRemoteTimer() {
        checkRemoteTimer?.cancel()
        checkRemoteTimer = null
    }


    private var stepDataNode: JSONObject? = null
    private var stepDataNodeType = ""

    private fun showNextStep(buttonStr: String) {

        tv_skip.text = when (buttonStr) {
            "startRecord" -> {
                tv_skip.visibility(true)
                "开始录制"
            }
            "next" -> {
                "下一步"
            }
            "finishRecord" -> {
                tv_skip.visibility(true)
                "确认完成"
            }

            else -> {
                "下一步"
            }
        }
    }

    //显示title的计时器
    var isShowStartRecord = false


    var autoCheckTypeStr = "System" // Button 为用户按钮确认通过 System 为系统自动质检通过
    var autoCheckBoolean = true  //智能质检通过与否
    var failType = ""  //智能质检通过与否
    var failReason = ""  //智能质检通过与否

    //点击保存节点数据
    var mCurrentStartTimer = 0L
    var mCurrentEndTimer = 0L
    var mCurrentStartTime = 0L
    var mCurrentEndTime = 0L

    fun quickEnterRoom(isSystem: Boolean) {
        destroylongTextTtsController()
        if ("idComparison".equals(stepDataNodeType)) {
            //如果是等待页面。
//            mTrtcrightvideolayoutmanager?.makeVideoView(2,1)
//            mTrtcrightvideolayoutmanager?.makeVideoView(1,0)
//            mTrtcrightvideolayoutmanager?.buildLayout()
//            resetVideoLayout()
//            mTrtcrightvideolayoutmanager?.makeFullVideoView(1)
        }
        autoCheckTypeStr = if (isSystem) {
            "System"
        } else {
            "Button"
        }

        mCurrentEndTimer = System.currentTimeMillis()

        mCurrentEndTime = mCurrentStartTime + (mCurrentEndTimer - mCurrentStartTimer) / 1000L

        LogUtils.i("quickEnterRoom------当前节点第:${mCurrentStartTime}秒开始-----当前节点第:${mCurrentEndTime}秒结束")
//        putNodeData(processIndex, stepIndex, mCurrentMsg!!)
        val checkJson = setCheckJson(
                processIndex,
                stepIndex,
                mCurrentStartTime,
                mCurrentEndTime,
                autoCheckTypeStr,
                autoCheckBoo = autoCheckBoolean,
                checkBoo = autoCheckBoolean,
                failType = failType,
                failReason = failReason
        )

        nextStep(isPassed, checkJson, object : OfflineActivity.RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                mCurrentStartTimer = mCurrentEndTimer
                mCurrentStartTime = mCurrentEndTime
            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }

    fun setFailType(failTypeStr: String, failReasonStr: String) {
        failType = failTypeStr
        failReason = failReasonStr

    }

    var startAutoNextStepTimer: CountDownTimer? = null
    private fun startAutoNextStep(isPassed: Boolean) {

        startAutoNextStepTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                LogUtils.i("自动跳过")
                autoCheckBoolean = true
                setFailType("", "")
                quickEnterRoom(true)
                trtcvideolayout?.setll_page_voice_result(View.GONE, true, "质检通过")
            }
        }

        startAutoNextStepTimer!!.start()


    }

    fun fillterData(jsonOb: JSONObject): String {
        LogUtils.i("fillterData---$jsonOb")
        val dataObject2 = jsonOb.getJSONObject("data")
        val stringBuffer = StringBuffer("")
        if (dataObject2.has("textArray")) {
            val jsonArray = dataObject2.getJSONArray("textArray")

            for (index in 0 until jsonArray.length()) {
                val subStr = jsonArray.get(index) as String
                stringBuffer.append("$subStr \n")
            }


        } else {
            ToastUtils.showShort("没有textArray字段！！！")
        }

        return stringBuffer.toString()
    }

    var agentASRPassword = ""
    var processIndex = 0
    var stepIndex = 0
    var targetJSONArray: JSONArray? = null

    //记录当前收到消息，便于下次重试机制

    //成功还是失败都是通过socket下发的消息执行的
    var mCurrentMsg: JSONObject? = null
    override fun onReceiveMSG(data: JSONObject) {

        try {

            if (data.getString("serviceId") == mServiceId) {

                val mType = data.getString("type")
                if (mType == "roomMessage" || mType == "end") { //如果type 为roomMessage 就不处理 为显示是否有无人脸

                } else {
                    LogUtils.i("scSC_Call_Status", "收到消息------$data")

                    var finished = data.optBoolean("finished", false)
                    if (finished) {
                        //完成
                        runOnUiThread {

                            showPageend()
                            initEndRecyclerview(data.optJSONArray("finishedStep"))
                            showLinkName("", "")
                        }

                    } else {
                        runOnUiThread {
                            showNextStep("next")
                        }

                        var stepDataJson: JSONObject? = null
                        if (data.has("step")) {
                            stepDataJson = data.getJSONObject("step")
                        } else {

                        }


                        val roomType = stepDataJson?.optString("roomType")
                        cancleTimer()
                        if (roomType!!.isNotEmpty()) { //房间内自定义type判断
                            when (roomType) {

                                "soundOCR-success" -> {
                                    runOnUiThread {
                                        isPassed = true
                                        cancelAbsCredentialProvider()
                                        voiceTimer?.cancel()
                                        voiceTimer = null
                                        strBuffer.delete(0, strBuffer.length)
                                        val dataJson = stepDataJson!!.getJSONObject("data")
                                        val roomMessage = dataJson.getString("roomMessage")
                                        startAutoNextStep(true)
                                        page_asr_userPage!!.apply {
                                            findViewById<LinearLayout>(R.id.ll_page_voice_result).visibility(false)
                                            findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(false)
                                            findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(true)
                                            findViewById<TextView>(R.id.ll_page_voice_result_retry).visibility(false)
                                            findViewById<TextView>(R.id.page_asr_voice).visibility(false)
                                            findViewById<TextView>(R.id.tv_user_content2).text = roomMessage

                                        }
                                    }
                                }
                                "soundOCR-fail" -> {
                                    runOnUiThread {
                                        isPassed = false
                                        strBuffer.delete(0, strBuffer.length)
                                        page_asr_userPage!!.apply {
                                            findViewById<LinearLayout>(R.id.ll_page_voice_result).visibility(true)
                                            findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(true)
                                            findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(false)
                                            findViewById<TextView>(R.id.ll_page_voice_result_retry).visibility(true)
                                            findViewById<TextView>(R.id.page_asr_voice).visibility(false)
                                            findViewById<TextView>(R.id.ll_page_voice_result_jump).setOnClickListener {
                                                autoCheckBoolean = false
                                                setFailType("", "")
                                                quickEnterRoom(isSystem = false)
                                            }
                                            findViewById<TextView>(R.id.ll_page_voice_result_mark).setOnClickListener {
                                                autoCheckBoolean = true
                                                setFailType("", "")
                                                quickEnterRoom(isSystem = false)
                                            }
                                            findViewById<TextView>(R.id.ll_page_voice_result_retry).setOnClickListener {
                                                sendMsg1(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                                                    override fun onSuccess(json: String?) {

                                                    }

                                                    override fun onFail(err: String?, code: Int) {

                                                    }

                                                })
                                            }
                                        }


                                    }
                                }

                                "idComparison-success" -> {
                                    setFailType("", "")
                                    runOnUiThread {
                                        isPassed = true
                                        val mUserId = stepDataJson?.optString("userId")
                                        //汇总数据
                                        fifterMemberList("", mUserId!!)
                                    }
                                }
                                "idComparison-fail" -> {
                                    setFailType("识别失败", "检测无人脸")
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        isPassed = false
                                        val mUserId = stepDataJson?.optString("userId")
                                        //汇总数据
                                        fifterMemberList(mUserId!!, "")
                                    }
                                }

                                "idComparison-retry" -> {
                                    val dataObject2 = stepDataNode?.getJSONObject("data")

                                    if (dataObject2!!.has("textArray")) {
                                        val jsonArray = dataObject2.getJSONArray("textArray")
                                        val stringBuffer = StringBuffer("")
                                        for (index in 0 until jsonArray.length()) {
                                            val subStr = jsonArray.get(index) as String
                                            stringBuffer.append("$subStr \n")
                                        }
                                        runOnUiThread {
                                            showidComparisonPage(stringBuffer.toString(), stepDataNode!!.getJSONArray("target")!!, true)
                                        }


                                    } else {
                                        ToastUtils.showShort("没有textArray字段！！！")
                                    }

                                }

                                "idComparison-collect-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true

                                        trtcvideolayout?.stopRoundView()
                                        trtcvideolayout?.setHollowOutView(View.GONE)
                                        trtcvideolayout?.setll_page_voice_result(View.VISIBLE, true, "质检通过")
                                        startAutoNextStep(true)
                                    }
                                }
                                "idComparison-collect-fail" -> {
                                    runOnUiThread {
                                        val dataJson = stepDataJson!!.getJSONObject("data")
                                        val roomMessage = dataJson.getString("roomMessage")
                                        autoCheckBoolean = false
                                        trtcvideolayout?.stopRoundView()
                                        trtcvideolayout?.setHollowOutView(View.GONE)
                                        trtcvideolayout?.setll_page_voice_result(View.VISIBLE, false, roomMessage)
                                    }

                                }

                                "onScroll" -> { //滑动中
                                    val stepData = data.getJSONObject("stepData")
                                    val jsonObject = stepData.getJSONObject("data")
                                    val string = jsonObject.getInt("roomMessage")
                                    runOnUiThread {
//                                    ll_page11_content.smoothScrollBy(0, string)
                                    }

                                }
                                "onScrollToUpper" -> {//滑到顶
//                                        if (isshowPage21){
//                                        ll_page13_content.smoothScrollBy(0, string)
//                                    }else{
//                                        ll_page11_content.smoothScrollBy(0, string)
//                                    }

                                }
                                "onScrollToLower" -> { //滑到底
//                                         if (isshowPage21){
//                                        ll_page13_content.smoothScrollBy(0, string)
//                                    }else{
//                                        ll_page11_content.smoothScrollBy(0, string)
//                                    }

                                }
                                "onTouchEnd" -> {
                                    val stepData = data.getJSONObject("stepData")
                                    val dataJson = stepData.getJSONObject("data")
                                    val imageByte = dataJson.getString("roomMessage")
                                    LogUtils.i("imageByte--------------" + imageByte)
                                    var mDataList = ArrayList<PointBean>()
                                    mDataList = Gson().fromJson(imageByte, object : TypeToken<java.util.ArrayList<PointBean>>() {}.type)
                                    runOnUiThread {
//                                    iv_page_sign_12.clear()
//                                    iv_page_sign_12.drawPath(mDataList)
                                    }
                                }
                                "clearCanvas" -> {
                                    runOnUiThread {

                                    }
                                }
                                "signFile-fail" -> {
                                    runOnUiThread {
                                        isPassed = false
                                        val stepData = data.getJSONObject("stepData")
                                        val dataJson = stepData.getJSONObject("data")
                                        val imageByte = dataJson.getString("roomMessage")

                                    }


                                }
                                "signFile-success" -> {
                                    runOnUiThread {
                                        isPassed = true
                                        val stepData = data.getJSONObject("stepData")
                                        val dataJson = stepData.getJSONObject("data")
                                        val imageByte = dataJson.getString("roomMessage")
//                                    page12_sign_result.text = "投保人：${userName}   签名：${imageByte}"
//                                    ll_page12_bottom.visibility(false)
//                                    ll_page12_result.visibility(true)
//                                    ll_page12_result_success.visibility(true)
//                                    ll_page12_result_fail.visibility(false)
                                    }
                                }
                                else -> {
                                }
                            }

                        } else {
                            runOnUiThread {
                                showLinkName(stepDataNode!!.optString("name", ""), "(${processIndex + 1}/${mAllProcessIndex})")
                            }
                            mCurrentMsg = data
                            if (data.has("step")) {
                                stepDataNode = data.getJSONObject("step")
                            } else {

                            }
                            //textTTS 话术播报
                            //soundOCR 语音识别
                            //idComparison 人脸核身
                            //waiting 等待操作
                            //signFile 电子签名
                            //productTTS 产品列表播报
                            //textRead 单证阅读
                            processIndex = stepDataNode!!.optInt("processIndex")
                            stepIndex = stepDataNode!!.optInt("stepIndex")

                            when (mType) {
                                "nextStep", "preStep" -> {

                                    stepDataNodeType = stepDataNode!!.optString("baseType", "")
                                    when (stepDataNodeType) {
                                        "waiting" -> {//纯文字展示,数组第一个元素为标题
                                            runOnUiThread {
                                                val fillterData = fillterData(stepDataNode!!)
                                                showWatingPage(fillterData, "")

                                            }


                                        }
                                        "textTTS", "productTTS" -> { //语音播放,数组每个元素为一个段落
                                            runOnUiThread {
                                                val fillterData = fillterData(stepDataNode!!)
                                                showTTSNext(fillterData)
                                            }

                                        }
                                        "soundOCR" -> { //投保人语音识别
                                            runOnUiThread {
                                                targetJSONArray = stepDataNode!!.getJSONArray("target")!!
                                                val targetOb = targetJSONArray!!.getString(0)
                                                if ("agent" == targetOb) {
                                                    startAudioRecognize()
                                                    startVoiceTimer(15000)

                                                    val jsonArray = stepDataNode?.getJSONObject("data")?.getJSONArray("textArray")
                                                    agentASRPassword = jsonArray!!.getString(1)!!

                                                    showUserASR("请代理人回复:" + jsonArray!!.getString(0)!!)
                                                } else {
                                                    startAudioRecognize()
                                                    startVoiceTimer(5000)
                                                    val jsonArray = stepDataNode?.getJSONObject("data")?.getJSONArray("textArray")
                                                    agentASRPassword = jsonArray!!.getString(1)!!

                                                    showUserASR("请投保人回复:" + jsonArray!!.getString(0)!!)
                                                }

                                            }

                                        }


                                        "idComparison" -> { //人脸核身
                                            val dataObject2 = stepDataNode?.getJSONObject("data")

                                            if (dataObject2!!.has("textArray")) {
                                                val fillterData = fillterData(stepDataNode!!)
                                                runOnUiThread {
                                                    showidComparisonPage(fillterData, stepDataNode!!.getJSONArray("target")!!, false)
                                                }
                                            } else {
                                                ToastUtils.showShort("没有textArray字段！！！")
                                            }

                                        }

                                        "signFile" -> { //投保人签字
                                            runOnUiThread {
                                                startTtsController(fillterData(stepDataNode!!), object : RoomHttpCallBack {
                                                    override fun onSuccess(json: String?) {
                                                    }

                                                    override fun onFail(err: String?, code: Int) {

                                                    }

                                                })
                                                showSignPage(stepDataNode!!.getJSONArray("target")!!)
                                            }

                                        }
                                        "textRead" -> {
                                            runOnUiThread {

                                                val dataObject2 = stepDataNode?.getJSONObject("data")
                                                if (dataObject2!!.has("textArray")) {
                                                    val fillterData = fillterData(stepDataNode!!)
                                                    startTtsController(fillterData, object : RoomHttpCallBack {
                                                        override fun onSuccess(json: String?) {
                                                        }

                                                        override fun onFail(err: String?, code: Int) {

                                                        }

                                                    })

                                                    showTextReadPage(fillterData, stepDataNode!!.optString("fileUrl", ""))
                                                } else {
                                                    ToastUtils.showShort("没有textArray字段！！！")
                                                }


                                            }
                                        }

                                        else -> {
                                        }
                                    }

                                }

                                "sign" -> {
                                    runOnUiThread {
                                        showPage20()
                                    }

                                }
                                "confirmSign" -> {//签名区显示签名
                                    runOnUiThread {
//                                    val stepData = data.getJSONObject("stepData")
//                                    val dataJson = stepData.getJSONObject("data")
//                                    val imageByte = dataJson.getString("image")
//                                    iv_page_sign_12.clear()
//                                    iv_page_sign_12.visibility(false)
//                                    showSignPhoto1(imageByte)
                                    }
                                }
                                "signShow" -> {
                                    runOnUiThread {
                                        showSignPage(stepDataNode!!.getJSONArray("target")!!)
                                        val stepData = data.getJSONObject("stepData")
                                        val dataJson = stepData.getJSONObject("data")
                                        val imageByte = dataJson.getString("image")
                                        showSignPhoto(imageByte)
                                    }
                                }
                                else -> {
                                }
                            }

                        }
                    }

                }


            } else {

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private var mFailCacheArray = ArrayList<String>()
    private var mSuccessCacheArray = ArrayList<String>()

    private fun fifterMemberList(failUserId: String, successUserId: String) {
        LogUtils.i(mWatingArray.toString())
        LogUtils.i(mSuccessCacheArray.toString())
        LogUtils.i(mFailCacheArray.toString())

        if (successUserId.isNotEmpty()) {
            mSuccessCacheArray.add(successUserId)
            if (mWatingArray?.length() == (mSuccessCacheArray.size)) {
                val jsonArray = JSONArray()
                mFailCacheArray.forEach {
                    jsonArray.put(it)
                }
                sendMsg1(JSONObject().apply {
                    put("type", "idComparison")
                    put("serviceId", mServiceId)
                    put("step", JSONObject().apply {
                        put("data", JSONObject().apply {
                            put("roomMessage", "质检成功")
                        })
                        put("roomType", "idComparison-collect-success")
                        put("target", jsonArray)
                    })

                }, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })

            } else {
                if (mWatingArray?.length() == (mFailCacheArray.size + mSuccessCacheArray.size)) {
                    val jsonArray = JSONArray()
                    mFailCacheArray.forEach {
                        jsonArray.put(it)
                    }
                    var str = StringBuffer("")
                    for (index in 0 until mFailCacheArray.size) {
                        val obj = mFailCacheArray.get(index)
                        if ("agent".equals(obj)) {
                            if (index == 0) {
                                str.append("代理人")
                            } else {
                                str.append("和代理人")
                            }

                        } else if ("policyholder".equals(obj)) {
                            if (index == 0) {
                                str.append("投保人")
                            } else {
                                str.append("和投保人")
                            }

                        } else if ("insured".equals(obj)) {
                            if (index == 0) {
                                str.append("被保人")
                            } else {
                                str.append("和被保人")
                            }
                        } else {

                        }
                    }
                    str.append("人脸核身识别失败")
                    sendMsg1(JSONObject().apply {
                        put("type", "idComparison")
                        put("serviceId", mServiceId)
                        put("step", JSONObject().apply {
                            put("data", JSONObject().apply {
                                put("roomMessage", str.toString())
                            })
                            put("roomType", "idComparison-collect-fail")
                            put("target", jsonArray)
                        })

                    }, object : OfflineActivity.RoomHttpCallBack {
                        override fun onSuccess(json: String?) {

                        }

                        override fun onFail(err: String?, code: Int) {

                        }
                    })

                }
            }
        }


        if (failUserId.isNotEmpty()) {
            mFailCacheArray.add(failUserId)

            if (mWatingArray?.length() == (mFailCacheArray.size + mSuccessCacheArray.size)) {
                val jsonArray = JSONArray()
                mFailCacheArray.forEach {
                    jsonArray.put(it)
                }
                var str = StringBuffer("")
                for (index in 0 until mFailCacheArray.size) {
                    val obj = mFailCacheArray.get(index)
                    if ("agent".equals(obj)) {
                        if (index == 0) {
                            str.append("代理人")
                        } else {
                            str.append("和代理人")
                        }

                    } else if ("policyholder".equals(obj)) {
                        if (index == 0) {
                            str.append("投保人")
                        } else {
                            str.append("和投保人")
                        }

                    } else if ("insured".equals(obj)) {
                        if (index == 0) {
                            str.append("被保人")
                        } else {
                            str.append("和被保人")
                        }
                    } else {

                    }
                }
                str.append("人脸核身识别失败")
                sendMsg1(JSONObject().apply {
                    put("type", "idComparison")
                    put("serviceId", mServiceId)
                    put("step", JSONObject().apply {
                        put("data", JSONObject().apply {
                            put("roomMessage", str.toString())
                        })
                        put("roomType", "idComparison-collect-fail")
                        put("target", jsonArray)
                    })

                }, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })

            }
        }


    }

    override fun agentOnline(data: JSONObject) {
        LogUtils.i("room-----agentOnline")
    }

    var credentialProvider: LocalCredentialProvider? = null
    var aaiClient: AAIClient? = null
    var audioRecognizeRequest: AudioRecognizeRequest? = null
    var audioRecognizeConfiguration: AudioRecognizeConfiguration? = null
    private fun initAbsCredentialProvider() {

        credentialProvider = LocalCredentialProvider(mSecretKey)

        // 用户配置
        //        ClientConfiguration.setServerProtocolHttps(false) // 是否启用https，默认启用

        ClientConfiguration.setMaxAudioRecognizeConcurrentNumber(2) // 语音识别的请求的最大并发数

        ClientConfiguration.setMaxRecognizeSliceConcurrentNumber(10) // 单个请求的分片最大并发数

        // 为了方便用户测试，sdk提供了本地签名，但是为了secretKey的安全性，正式环境下请自行在第三方服务器上生成签名。

    }


    var strBuffer = StringBuffer()
    var mOldSegmentStr = ""

    fun startAudioRecognize() {
        val audioRecognizeResultListener: AudioRecognizeResultListener = object : AudioRecognizeResultListener {
            override fun onSuccess(request: AudioRecognizeRequest?, result: String?) {
                LogUtils.i("result-------$result")

            }

            override fun onFailure(request: AudioRecognizeRequest?, clientException: ClientException?, serverException: ServerException?) {
                LogUtils.i("onFailure-------$clientException------$serverException")
            }

            override fun onSliceSuccess(request: AudioRecognizeRequest?, result: AudioRecognizeResult?, order: Int) {
                LogUtils.i("onSliceSuccess-------${result?.text}")
            }

            override fun onSegmentSuccess(request: AudioRecognizeRequest?, result: AudioRecognizeResult?, order: Int) {
                LogUtils.i("onSegmentSuccess-------${result?.text}")

                runOnUiThread {
                    strBuffer.append(result?.text)
                    val replaceTV1 = replaceTV(strBuffer.toString())
                    page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text = "$replaceTV1"
                    page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).visibility(true)
                    mOldSegmentStr = strBuffer.toString()


                    val replaceTV = replaceTV(agentASRPassword)

                    LogUtils.i("replaceTV------$replaceTV")

                    val numJewelsInStones = numJewelsInStones(replaceTV1, replaceTV)

                    if (numJewelsInStones == 0) {
                        //识别成功
                        strBuffer.delete(0, strBuffer.length)
                        cancelAbsCredentialProvider()
                        voiceTimer?.cancel()
                        voiceTimer = null

                        page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text = "$replaceTV1"
                        //自动跳到下一步
                        sendMsg1(JSONObject().apply {
                            put("serviceId", mServiceId)
                            put("type", "soundOCR")
                            put("step", JSONObject().apply {
                                put("data", JSONObject().apply {
                                    put("roomMessage", replaceTV1)
                                })
                                put("roomType", "soundOCR-success")
                                put("target", targetJSONArray)
                            })
                        }, object : RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }
                        })

                    } else {
                        //识别成功
                        voiceTimer?.cancel()
                        voiceTimer = null
                        cancelAbsCredentialProvider()
                        page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text = "$replaceTV1"
                        strBuffer.delete(0, strBuffer.length)


                        //自动跳到下一步
                        sendMsg1(JSONObject().apply {
                            put("serviceId", mServiceId)
                            put("type", "soundOCR")
                            put("step", JSONObject().apply {
                                put("data", JSONObject().apply {
                                    put("roomMessage", replaceTV1)
                                })
                                put("roomType", "soundOCR-fail")
                                put("target", targetJSONArray)
                            })
                        }, object : RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }
                        })
                    }
                }
            }


        }
        try {
            // 1、初始化AAIClient对象。
            if (aaiClient == null) {
                aaiClient = AAIClient(this, mAppid.toInt(), 0, mSecretId, credentialProvider)
            }
            // 2、初始化语音识别请求。

            audioRecognizeRequest = AudioRecognizeRequest.Builder()
                    .pcmAudioDataSource(AudioRecordDataSource()) // 设置语音源为麦克风输入
                    .build()

            // 3、初始化语音识别结果监听器。


            // 自定义识别配置
            audioRecognizeConfiguration = AudioRecognizeConfiguration.Builder()
                    .setSilentDetectTimeOut(true)// 是否使能静音检测，true表示不检查静音部分
                    .audioFlowSilenceTimeOut(5000) // 静音检测超时停止录音
                    .minAudioFlowSilenceTime(2000) // 语音流识别时的间隔时间
                    .minVolumeCallbackTime(80) // 音量回调时间
                    .sensitive(2.5f)
                    .build()

        } catch (e: ClientException) {
            e.printStackTrace()
        }

        // 4、启动语音识别
        Thread(Runnable {
            if (aaiClient != null) {

                LogUtils.i("audioRecognizeRequest-----${audioRecognizeRequest?.requestId}")
                aaiClient?.startAudioRecognize(audioRecognizeRequest, audioRecognizeResultListener, audioRecognizeConfiguration)
            }
        }).start()
    }


    fun cancelAbsCredentialProvider() {
        // 1、获得请求的id

        val requestId = audioRecognizeRequest?.requestId
        // 2、调用cancel方法

        Thread(Runnable {
            if (aaiClient != null) {
                //取消语音识别，丢弃当前任务
                aaiClient!!.cancelAudioRecognize(requestId!!)
            }
        }).start()
    }

    public fun numJewelsInStones(newWord: String, oldWord: String): Int {
        if (newWord.length != oldWord.length) return 12
        LogUtils.i("newWord", newWord)
        LogUtils.i("oldWord", oldWord)
        var count = 0;
        if (newWord.length <= oldWord.length) {
            for (index in 0 until newWord.length) {

                if (newWord[index] != oldWord[index]) {
                    count++
                }
            }
        } else {
            count = 1
        }


        return count;
    }

    public fun replaceTV(newWord: String): String {
        val replace = newWord.replace(
                "，", "")

        val replace2 = replace.replace(
                "。", "")
        return replace2
    }


    private fun showSignPhoto(photoByte: String) {
        val bytes = FileUtils.stringToBitmap(photoByte, 700, 700)
//        Glide.with(this)
//                .load(bytes)
//                .into(iv_page_13)


    }

    private fun showSignPhoto1(photoByte: String) {
        val bytes = FileUtils.stringToBitmap(photoByte, 700, 700)
//        Glide.with(this)
//                .load(bytes)
//                .into(iv_page_12)


    }

    public fun showLinkName(linkName: String, linkIndex: String) {
        tv_linkname.text = linkName
        tv_linknameindex.text = linkIndex
    }


    private var mFailCount = 0;
    private var mCheckLocal = false
    private var checkPhotoInVideoTimer: CountDownTimer? = null
    var showBasePop: BasePopupView? = null
    var showRemoteBasePop: BasePopupView? = null

    var isShowedAgentNoFace = false;
    var isShowedAgentHaveFace = false;
    private fun checkPhotoInVideo() {
        mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->

            val bytes = SystemCommon.getInstance()?.saveBitmap(p0)
            if (bytes != null) {
                val encode = Base64.encode(bytes, Base64.DEFAULT)
                val bulider = StringBuilder("data:image/png;base64,")
                bulider.append(String(encode))


                val replace = bulider.toString().replace("\n", "")
                val jsonObject = JSONObject()
                jsonObject.put("img", replace)
                SystemHttpRequest.getInstance().faceDetection(jsonObject, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        //没有人脸
                        var haveFace = if ("0".equals(json)) {
                            //todo
//                            mTrtcrightvideolayoutmanager?.updateToastStrByType("agent", getString(R.string.tx_str))
                            true
                        } else {//有人脸
                            false
                        }
//                        sendMsg1(JSONObject().apply {
//                            put("serviceId", mServiceId)
//                            put("type", "faceDetection")
//                            put("step", JSONObject().apply {
//                                put("roomType", "faceDetection")
//                                put("haveFace", haveFace)
//                                put("userId", mUserId)
//
//                            })
//                        }, object : OfflineActivity.RoomHttpCallBack {
//                            override fun onSuccess(json: String?) {
//                            }
//
//                            override fun onFail(err: String?, code: Int) {
//                            }
//                        })
                       runOnUiThread {
                           Handler().postDelayed({ checkPhotoInVideo() }, 3000)
                       }

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                }

                )

            }

        }

    }

    private var mRemoteFailCount = 0;
    private var mRemoteCheck = false;
    private var checkPhotoInRemotVideoTimer: CountDownTimer? = null


    private var checkPhotoInRemotVideoTimer1: CountDownTimer? = null
    var showRemoteBasePop1: BasePopupView? = null


    fun hideView() {
        page_checkenv.visibility(false)

    }

    override fun onConnect() {
        super.onConnect()
        LogUtils.i("rooms-onConnect")

        SystemSocket.instance?.setMSG(TXManagerImpl.instance?.getLoginName()!!)
    }

    override fun onBackPressed() {
        end()
    }


    private fun takePhoto(param: PhotoHttpCallBack) {
        LogUtils.i("mUserId---$mUserId")
        Handler().postDelayed({
            mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                val bytes = SystemCommon.getInstance().saveBitmap1(p0)
                if (bytes != null) {
                    val encode = Base64.encode(bytes, Base64.DEFAULT)
                    val jsonObject = JSONObject(mRoomInfo)
                    val flowId = jsonObject.optString("flowId", "")
                    val bulider = StringBuilder("data:image/png;base64,")
                    bulider.append(String(encode))


                    val replace = bulider.toString().replace("\n", "")
                    val uploadShotPic = UploadShotPic()
                    uploadShotPic.idCardNum = flowId
                    uploadShotPic.serviceId = mServiceId
                    uploadShotPic.facePhoto = replace
                    uploadShotPic.name = replace
                    SystemHttpRequest.getInstance().agentIdCard(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
                        override fun onSuccess(json: String?) {
                            runOnUiThread {
                                param.onSuccess(json)
                            }

                        }

                        override fun onFail(err: String?, code: Int) {
                            runOnUiThread {
                                param.onFail(err, code)
                            }

                        }

                    })
                } else {
                    param.onFail("识别错误！！！", 1)
                }
            }
        }, 2000)


    }

    override fun onClickItemFill(userId: String?, streamType: Int, enableFill: Boolean) {
        LogUtils.i("onClickItemFill--userId$userId----enableFill$enableFill")
    }

    override fun onClickItemMuteVideo(userId: String?, streamType: Int, isMute: Boolean) {
    }

    override fun onClickItemMuteAudio(userId: String?, isMute: Boolean) {
    }

    override fun onClickItemMuteInSpeakerAudio(userId: String?, isMute: Boolean) {
    }

    override fun onClickMuteVideo(view: TRTCVideoLayout?, isMute: Boolean) {

    }

    override fun onClickMuteInSpeakerAudio(view: TRTCVideoLayout?, isMute: Boolean) {
        //
        if (isStartRecord) {
            autoCheckBoolean = true
            setFailType("", "")
            quickEnterRoom(false)
        } else {
            startRecord(object : RoomHttpCallBack {
                override fun onSuccess(json: String?) {
                    SystemLogHelper.getInstance().start()
                    mStartRecordNetMillis = System.currentTimeMillis()
                    mCurrentStartTimer = System.currentTimeMillis()
                    mCurrentStartTime = (mStartRecordNetMillis - mStartRecordTimeMillis) / 1000L
                    isStartRecord = true
                    trtcvideolayout?.setll_remote_skip("", View.GONE, View.GONE)
                }

                override fun onFail(err: String?, code: Int) {

                }

            })
        }

    }

    override fun onClickMuteAudio(view: TRTCVideoLayout?, isMute: Boolean) {
    }

    override fun onClickFill(view: TRTCVideoLayout?, enableFill: Boolean) {
    }

    override fun onClickRetry(view: TRTCVideoLayout?, type: String?) {
        when (type) {
            "0" -> {
                trtcvideolayout?.setll_page_voice_result(View.GONE, false, "")
                //标记
                setFailType("", "")
                autoCheckBoolean = true
                quickEnterRoom(isSystem = false)
            }
            "1" -> {
                //跳过
                trtcvideolayout?.setll_page_voice_result(View.GONE, false, "")
                setFailType("识别失败", "送检失败")
                autoCheckBoolean = false
                quickEnterRoom(isSystem = false)
            }
            "2" -> {
                //重试
                trtcvideolayout?.setll_page_voice_result(View.GONE, false, "")
                val jsonArray = JSONArray()
                mFailCacheArray.forEach {
                    jsonArray.put(it)
                }
                mCurrentMsg!!.getJSONObject("step").remove("target")
                mCurrentMsg!!.getJSONObject("step").put("target", jsonArray)
                mCurrentMsg!!.getJSONObject("step").put("roomType", "idComparison-retry")
                sendMsg1(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })


            }
            else -> {
            }
        }
    }


}

