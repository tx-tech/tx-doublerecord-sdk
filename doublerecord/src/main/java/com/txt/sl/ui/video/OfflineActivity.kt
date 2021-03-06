package com.txt.sl.ui.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.*
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.immersionbar.TxBarHide
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.common.widget.titlebar.sign.SignatureView
import com.common.widget.toast.ToastUtils
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
import com.txt.sl.TXSdk
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.config.TXManagerImpl.Companion.instance
import com.txt.sl.config.socket.SocketBusiness
import com.txt.sl.entity.bean.*
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
import com.txt.sl.ui.dialog.ChooseSpeedDialog
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.home.HomeActivity
import com.txt.sl.ui.video.trtc.BusinessLayout
import com.txt.sl.ui.video.trtc.TRTCVideoLayout
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_offline_room.*
import kotlinx.android.synthetic.main.tx_page_checkenv.*
import kotlinx.android.synthetic.main.tx_page_envpreview.*
import kotlinx.android.synthetic.main.tx_page_linkpreview.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.DecimalFormat

/**
 * ??????????????????
 */

class OfflineActivity : BaseActivity(), View.OnClickListener, SocketBusiness,
    TRTCVideoLayout.IVideoLayoutListener {
    //?????????????????????
    //private var mLocalPreviewView //????????????????????????View
    //      : TXCloudVideoView? = null
    private var mBackButton //?????????????????????????????????
            : ImageView? = null
    private var mTRTCCloud // SDK ?????????
            : TRTCCloud? = null
    private var mIsFrontCamera = true // ?????????????????????
    private var mRemoteUidList // ????????????Id??????
            : MutableList<String>? = null
    private var mRemoteViewList // ??????????????????
            : MutableList<TXCloudVideoView>? = null
    private val mUserCount = 0 // ????????????????????????
    private var mRoomId // ??????Id
            : String? = null
    private var mUserId // ??????Id
            : String? = null

    private var mRoomInfo // ????????????
            : String? = null

    private var mTaskId // ????????????
            : String? = null

    private var mSelfInsurance // ?????????
            = false

    override fun isFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }


    companion object {
        private const val TAG = "OfflineActivity"
        private const val REQ_PERMISSION_CODE = 0x1000
    }

    override fun initView() {
        SystemLogHelper.getInstance().start()
        TxLogUtils.i("onSuccess------${System.currentTimeMillis()}")
        statusBarConfig.hideBar(TxBarHide.FLAG_HIDE_STATUS_BAR)
        super.initView()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.mode = AudioManager.MODE_NORMAL
        handleIntent()
        // ??????????????????????????????
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        initView1()

        showPageCheck()
        inflate()
    }

    private fun showPageCheck() {
        hideView()
        tv_skip.visibility(true)
        tv_skip.text = "????????????"
        ll_envpreview.visibility(true)
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
            if (intent.getStringExtra(Constant.TASKID) != null) {
                mTaskId = intent.getStringExtra(Constant.TASKID)
            }

            mSelfInsurance = intent.getBooleanExtra(Constant.SELFINSURANCE, false)
        }

    }

    private var screenRecordHelper: ScreenRecordHelper? = null

    private fun initView1() {
        getServiceId()
        SystemBaiduLocation.instance!!.startLocationService()
        SystemSocket.instance?.connectSocket()
        SystemSocket.instance?.setonSocketListener(this)
        mBackButton = findViewById(R.id.trtc_ic_back)

        mRemoteUidList = ArrayList()
        mRemoteViewList = ArrayList()

        mBackButton?.setOnClickListener(this)

        initBusiness()
        initAbsCredentialProvider()
        mTRTCCloud = TRTCCloud.sharedInstance(applicationContext)

        mTRTCCloud?.setListener(TRTCCloudImplListener(this@OfflineActivity))
    }

    interface RoomHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
    }

    interface PhotoHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
    }

    //        processIndex: { type: Number },  //????????????????????????
    //        stepIndex: { type: Number },  //??????????????????
    //        videoFrom: { type: Number }, //????????????????????????????????????
    //        videoTo: { type: Number },//????????????????????????????????????
    //        autoCheckType: { type: String },//???????????????????????????"Button"??????????????????????????????"System"???????????????????????????
    //        autoCheck: { type: Boolean }, //????????????????????????
    //        check: { type: Boolean } // ?????????autoCheck????????????
    private fun setCheckJson(
        processIndexInt: Int,
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
                put("autoFailType", failType) //????????????
                put("autoFailReason", failReason) //????????????
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

        SystemHttpRequest.getInstance()
            .nextStep(jsonOb.toString(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {
                        listener.onSuccess(json!!)
                    }

                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        listener.onFail(err, code)
                        ToastUtils.show(err!!)
                        if (-3 == code) {

                            SystemSocket.instance?.setMSG(
                                TXManagerImpl.instance?.getLoginName()!!,
                                mServiceId
                            )
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

        SystemHttpRequest.getInstance()
            .startRecord(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {

                        listener.onSuccess(json!!)
                    }

                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        listener.onFail(err, code)
                        showToastMsg(err!!)
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
            SystemHttpRequest.getInstance()
                .endRecord(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
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
    var canCheck = false //??????????????????????????????????????????????????????
    var jsonObject1: JSONObject? = null
    var mAppid = 0L
    var mSecretId = ""
    var mSecretKey = ""
    var policyholdeRequalInsured = false
    var agentID = ""
    var policyholderID = ""
    var insuredID = ""
    fun getServiceId() {
        jsonObject1 = JSONObject(mRoomInfo)
        mServiceId = jsonObject1!!.optString("serviceId", "")
        mFlowId = jsonObject1!!.optString("flowId", "")
        agentName = jsonObject1!!.optString("agentName", "")
        policyholderName = jsonObject1!!.optString("policyholderName", "")
        insuredName = jsonObject1!!.optString("insuredName", "")
        val wxCloudConfJO = jsonObject1!!.optJSONObject("wxCloudConf")
        mSecretId = wxCloudConfJO!!.optString("SecretId")
        mSecretKey = wxCloudConfJO!!.optString("SecretKey")
        mAppid = wxCloudConfJO!!.optString("AppId").toLong()

        agentID = jsonObject1!!.optString("agentID", "")
        policyholderID = jsonObject1!!.optString("policyholderID", "")
        insuredID = jsonObject1!!.optString("insuredID", "")
        policyholdeRequalInsured = policyholderID == insuredID
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
            val i =
                TxSPUtils.get(this@OfflineActivity, TXManagerImpl.instance!!.getAgentId(), 2) as Int
            val values = VoiceSpeed.values()
            val voiceSpeed = values[i]
            setVoiceSpeed(voiceSpeed.num)
            setVoiceType(VoiceType.VOICE_TYPE_AFFNITY_FEMALE.num)
            setVoiceLanguage(VoiceLanguage.VOICE_LANGUAGE_CHINESE.num)
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
        tv_welcome_title.text = "???????????????????????????${orderDetailsItemlists.size}?????????"
        mAllProcessIndex = orderDetailsItemlists.size
    }


    fun destroylongTextTtsController() {
        longTextTtsController?.stop()
    }


    fun startTtsController(ttsStr: String, callBack: RoomHttpCallBack) {
        try {
            longTextTtsController?.startTts(
                ttsStr,
                mTtsExceHandler,
                object : QCloudPlayerCallback {
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
            LogUtils.i("TtsException-----${e.message}")
            callBack.onFail(e.message, 0)
        }

    }

    private var mTtsExceHandler: TtsExceptionHandler = TtsExceptionHandler {

        LogUtils.i("TtsException-----${it.errMsg}")

        //?????? ????????????
        //[
        //            {
        //                "_id":"61e54414f21228206948ec13",
        //                "key":"releaseSuccessful",
        //                "buttonName":"????????????",
        //                "check":true
        //            },
        //            {
        //                "_id":"61e54414f21228206948ec12",
        //                "key":"releaseFailure",
        //                "buttonName":"??????",
        //                "check":true
        //            },
        //            {
        //                "_id":"61e54414f21228206948ec11",
        //                "key":"retry",
        //                "buttonName":"??????",
        //                "check":true
        //            }
        //        ]
        val jsonObject = JSONObject()
        jsonObject.put("key", "releaseSuccessful")
        jsonObject.put("buttonName", "????????????")
        jsonObject.put("check", true)

        val jsonObject1 = JSONObject()
        jsonObject1.put("key", "retry")
        jsonObject1.put("buttonName", "??????")
        jsonObject1.put("check", true)


        val jsonArray = JSONArray()
        jsonArray.put(jsonObject)
        jsonArray.put(jsonObject1)
        runOnUiThread {
            mTrtcVideolayout?.setll_page_voice_result(
                View.VISIBLE,
                false,
                "",
                jsonArray
            )
        }


    }

    public fun pushMessage(jsonObject: JSONObject, listener: RoomHttpCallBack) {
        LogUtils.i("pushMessage-----${jsonObject}")
        SystemHttpRequest.getInstance()
            .pushMessage(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    LogUtils.i("pushMessage-----onSuccess${jsonObject}")
                    runOnUiThread {
                        listener.onSuccess(json!!)
                    }

                }

                override fun onFail(err: String?, code: Int) {
                    LogUtils.i("pushMessage-----onFail{jsonObject}")
                    runOnUiThread {
                        listener.onFail(err, code)
                    }
                }

            })
    }

    var isPassed = true
    var isStartRecord = false
    private fun initBusiness() {
        if (!jugeTenantIdIsRemoteRecord()) {
            tv_continue.setOnClickListener(
                CheckDoubleClickListener {
                    //????????????
                    val customDialog = ChooseSpeedDialog(this)
                    customDialog.setAgentIdStr(TXManagerImpl.instance!!.getAgentId())
                    customDialog.setOnConfirmClickListener(object :
                        ChooseSpeedDialog.OnConfirmClickListener {
                        override fun onSpeedChoose(voiceSpeed: Int, content: String) {
                            destroylongTextTtsController()
                            longTextTtsController?.setVoiceSpeed(voiceSpeed)
                            startTtsController(content, object : OfflineActivity.RoomHttpCallBack {
                                override fun onSuccess(json: String?) {
                                }

                                override fun onFail(err: String?, code: Int) {
                                }

                            })
                        }

                        override fun onConfirm() {

                        }

                    })
                    TxPopup.Builder(this).maxWidth(900).dismissOnTouchOutside(false)
                        .dismissOnBackPressed(false).setPopupCallback(object : XPopupCallback {
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
            )
        }

        tv_continue1.setOnClickListener(CheckDoubleClickListener {
            TxPopup.Builder(this).maxWidth(700).asConfirm(
                "????????????",
                "?????????????????????????????????????????????\n????????????????????????",
                "??????",
                "??????",
                {
                    mCurrentEndTimer = System.currentTimeMillis()

                    mCurrentEndTime =
                        mCurrentStartTime + (mCurrentEndTimer - mCurrentStartTimer) / 1000L

                    LogUtils.i("quickEnterRoom------???????????????:${mCurrentStartTime}?????????-----???????????????:${mCurrentEndTime}?????????")


                    nextStep(isPassed, object : OfflineActivity.RoomHttpCallBack {
                        override fun onSuccess(json: String?) {
                            mCurrentStartTimer = mCurrentEndTimer
                            mCurrentStartTime = mCurrentEndTime
                            tv_continue1.visibility(false)
                            tv_text_continue.visibility(false)
                        }

                        override fun onFail(err: String?, code: Int) {

                        }

                    })
                },
                null,
                false
            ).show()
        })


        tv_skip.setOnClickListener(CheckDoubleClickListener {
            it as TextView
            when (it.text) {
                "????????????" -> {
                    //??????????????????
                    showPageTwo()
                }
                "?????????" -> {
                    //??????????????????????????????????????????????????????
                    if (startAutoNextStepTimer != null) {
                        startAutoNextStepTimer?.cancel()
                        startAutoNextStepTimer = null
                    }
                    quickEnterRoom(isSystem = true)

                }
                "????????????" -> {  //???????????? --?????????????????????-2
                    requestRecordPer()

                }
                "????????????" -> {  //???????????? --?????????????????????-2
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

                                val df = DecimalFormat("#.000")
                                val mTimeMillis =
                                    (mStartRecordNetMillis - mStartRecordTimeMillis) / 1000f
                                var minSize = df.format(mTimeMillis)
                                val jsonObject = JSONObject().apply {
                                    put("flowId", mTaskId)
                                    put("preTime", minSize)
                                    put("serviceId", mServiceId)
                                    put("path", screenRecordHelper?.getSavaName())
                                }

                                TxSPUtils.put(this@OfflineActivity, mTaskId, jsonObject.toString())
                                //??????????????????
                                //????????????
                                val customDialog = UploadVideoDialog(this@OfflineActivity)
                                customDialog.setScreenRecordStr(jsonObject.toString())
                                customDialog.setOnConfirmClickListener(object :
                                    UploadVideoDialog.OnConfirmClickListener {
                                    override fun onVideoUpload(isFinish: Boolean) {
                                        if (!isFinish) {
                                            TxSPUtils.put(
                                                this@OfflineActivity,
                                                mTaskId,
                                                jsonObject.toString()
                                            )
                                        }

                                        finish()
                                    }

                                })
                                TxPopup.Builder(this@OfflineActivity).maxWidth(1200)
                                    .setPopupCallback(object : XPopupCallback {
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

                                    })
                                    .dismissOnTouchOutside(false)
                                    .dismissOnBackPressed(false)
                                    .asCustom(customDialog)
                                    .show()
                            }
                        }

                        override fun onFail(err: String?, code: Int) {
                            runOnUiThread {
                                LogUtils.d(err!!)
                                showToastMsg(err)
                            }
                        }

                    }, disableState = false)


                }
                else -> {
                }
            }
        })


    }

    //??????????????????
    var mTrtcVideolayout: TRTCVideoLayout? = null

    //
    var mTextBusinessLayout: BusinessLayout? = null

    @SuppressLint("SetTextI18n")
    private fun enterRoom() {
        SystemBaiduLocation.instance!!.requestLocation()
        ll_showLink.visibility(false)
        offlineVideoLayoutManager.visibility(true)
        val allocCloudVideoView1 = offlineVideoLayoutManager.allocCloudVideoView(
            mUserId,
            "agent",
            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
        )
        offlineVideoLayoutManager.allocCloudVideoView(
            "1",
            "1",
            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
        )

        offlineVideoLayoutManager.allocCloudVideoView(
            "2",
            "2",
            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
        )

        val findEntityBytype = offlineVideoLayoutManager.findEntityBytype("agent")
        val findRightFullEntityBytype = offlineVideoLayoutManager.findEntityBytype("2")

        mTextBusinessLayout = findRightFullEntityBytype?.layout as BusinessLayout
//        mTextBusinessLayout?.visibility = View.GONE
        mTextBusinessLayout?.contentView?.removeAllViews()
        mTextBusinessLayout?.contentView?.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.tx_txcolor_F6F7F9
            )
        )
        mTextBusinessLayout?.contentView?.addView(page_readnextPage1)

        mTrtcVideolayout = findEntityBytype.layout as TRTCVideoLayout
        LogUtils.i("width--${allocCloudVideoView1?.width}-----height--${allocCloudVideoView1?.height}")
        // ??????????????? SDK ??????
        val trtcParams = TRTCParams()
//        trtcParams.sdkAppId = jsonObject1!!.optInt("sdkAppId")
//        trtcParams.userId = mUserId
//        trtcParams.roomId = mRoomId?.toInt()!!
        // userSig????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        trtcParams.userSig = jsonObject1!!.optString("agentSig")
//        trtcParams.role = TRTCCloudDef.TRTCRoleAnchor

        // ????????????
//        mTRTCCloud?.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
        mStartRecordTimeMillis = System.currentTimeMillis()
        mTRTCCloud?.startLocalPreview(mIsFrontCamera, allocCloudVideoView1)

        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360
        encParam.videoFps = Constant.VIDEO_FPS
        encParam.videoBitrate = Constant.RTC_VIDEO_BITRATE
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE

        mTRTCCloud?.setVideoEncoderParam(encParam)
        mTrtcVideolayout?.setIVideoLayoutListener(this)
        startCheckPhotoInVideo()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        sendBroadcast(Intent().apply {
            action = HomeActivity.br_action
        })
        SystemBaiduLocation.instance!!.stopLocationService()
        SystemLogHelper.getInstance().stop()
        stopCheckPhotoInVideo()
        destroylongTextTtsController()
        exitRoom()
        cancelAbsCredentialProvider()
        cancelTitleTimer()
        startCheckPhotoInVideoTimer?.cancel()
        startCheckPhotoInVideoTimer = null

        mCheckLocal = false

        if (aaiClient != null) {
            try {
                aaiClient?.release()
            } catch (e: java.lang.Exception) {

            }

        }

        super.onDestroy()
        val containActivity = ApplicationUtils.isContainActivity(HomeActivity::class.java)
        if (!containActivity) {
            if (null != TXSdk.getInstance().onTxPageListener) {
                TXSdk.getInstance().onTxPageListener.onSuccess(mTaskId!!)
            }
            ApplicationUtils.finishActivity()
        }
    }


    /**
     * ????????????
     */
    private fun exitRoom() {


        SystemSocket.instance?.disconnectSocket()
        if (mTRTCCloud != null) {
            mTRTCCloud!!.stopLocalAudio()
            mTRTCCloud!!.stopLocalPreview()
            mTRTCCloud!!.exitRoom()
            //?????? trtc ??????
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
                    showToastMsg("??????????????????????????????")
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
                pushMessage(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
            }


            R.id.trtc_ic_back -> {
                //???????????????
                end()
            }
            else -> {
            }
        }

    }

    private fun end() {

        TxPopup.Builder(this).maxWidth(700)
            .asConfirm("??????", "?????????????????????????????????????????????????????????", "??????", "??????", object : OnConfirmListener {
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
                                showToastMsg(err)
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

        override fun onSpeedTest(p0: TRTCSpeedTestResult?, p1: Int, p2: Int) {
            super.onSpeedTest(p0, p1, p2)
            LogUtils.i("onSpeedTest------" + p0.toString())

            //4 5 6
            CheckEnvUtils.getInstance().setNetSpeed(p0?.quality!!)
        }

        override fun onEnterRoom(result: Long) {
            super.onEnterRoom(result)
            LogUtils.i("onEnterRoom-----result----$result")
//            tv_local_video.text = "????????????$agentName"
//            mStartRecordTimeMillis = System.currentTimeMillis()
            LogUtils.i("mStartRecordTimeMillis-----$mStartRecordTimeMillis")


        }

        override fun onExitRoom(reason: Int) {
            super.onExitRoom(reason)
            LogUtils.i("onEnterRoom-----result----$reason")
        }


        private val mContext: WeakReference<OfflineActivity>

        override fun onNetworkQuality(
            localQuality: TRTCQuality?,
            remoteQuality: ArrayList<TRTCQuality>?
        ) {
            super.onNetworkQuality(localQuality, remoteQuality)

            mTrtcVideolayout?.updateNetworkQuality(localQuality?.quality!!)

            LogUtils.i("onNetworkQuality-------${localQuality?.quality}")
        }

        override fun onRemoteUserLeaveRoom(p0: String?, p1: Int) {
            super.onRemoteUserLeaveRoom(p0, p1)
            LogUtils.i("onRemoteUserLeaveRoom----$p0,p1---$p1")


        }

        override fun onRemoteUserEnterRoom(p0: String?) {
            super.onRemoteUserEnterRoom(p0)
            LogUtils.i("onRemoteUserEnterRoom----$p0")

        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
        }

        override fun onConnectionRecovery() {
            super.onConnectionRecovery()
            //SDK ???????????????????????????
            isConnect = true
            LogUtils.i("onConnectionRecovery");
        }

        override fun onConnectionLost() {
            super.onConnectionLost()
            //SDK ???????????????????????????
            isConnect = false
            LogUtils.i("onConnectionLost");
        }

        override fun onTryToReconnect() {
            super.onTryToReconnect()
            LogUtils.i("onTryToReconnect");
        }

        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            super.onUserAudioAvailable(userId, available)
        }


        // ?????????????????????????????????????????? SDK ??????????????????
        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            Log.d(Companion.TAG, "sdk callback onError")
            val activity = mContext.get()
            if (activity != null) {
                showToastMsg("onError: $errMsg[$errCode]")
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
                screenRecordHelper = ScreenRecordHelper(
                    this@OfflineActivity,
                    object : ScreenRecordHelper.OnVideoRecordListener {
                        override fun onBeforeRecord() {
                        }

                        override fun onStartRecord() {

                            enterRoom() //????????????????????????????????????????????????????????????
                            var title = if (mSelfInsurance) {
                                //?????????????????????????????????????????????????????????
                                getString(R.string.tx_title_ready_oneperson)
                            } else {
                                getString(R.string.tx_title_ready)
                            }
                            var title1 = getString(R.string.tx_title_ready_test)
                            mTrtcVideolayout?.setPersonView(mSelfInsurance)
                            showReadNextPage("", title1)
                            mTrtcVideolayout?.setll_remote_skip(
                                "?????????????????????????????????????????????",
                                View.VISIBLE,
                                View.VISIBLE
                            )
                            mTrtcVideolayout?.setPersonView(View.VISIBLE)
                        }

                        override fun onCancelRecord() {
                        }

                        override fun onEndRecord() {
                        }

                    },
                    TxPathUtils.getExternalStoragePath() + "/txsl/video"
                )
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
        tv_continue.visibility(!jugeTenantIdIsRemoteRecord())
        tv_skip.visibility(true)
        tv_skip.text = "????????????"
        val jsonArray = jsonObject1!!.optJSONArray("process")
        initRecyclerview(jsonArray)
        startTitleTimer()
    }

    private var audioManager: AudioManager? = null
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {


        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {

                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP
                )

                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {

                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP
                )
                return true
            }
        }
        return true

    }

    private fun showPageTwo() {
        tv_skip.visibility(false)
        tv_continue.visibility(false)
        ll_envpreview.visibility(false)
        hideView()
        ll_showLink.visibility(false)
        page_checkenv.visibility(true)
        initCheckEnvRecyclerView()

    }

    private fun startCheckEnv() {
        pagetwo_count.visibility(false)
        ll_checkenv.visibility(false)
        tv_checkenvstate.text = "?????????"
        tv_checkenvstate.background =
            ContextCompat.getDrawable(this@OfflineActivity, R.drawable.tx_checkenv_bg)
        val envData = CheckEnvUtils.getInstance().getEnvData()
        mTRTCCloud?.startSpeedTest(
            jsonObject1!!.optInt("sdkAppId"),
            mUserId,
            jsonObject1!!.optString("agentSig")
        )
        CheckEnvUtils.getInstance().startCheckEnv(this, false)
        checkenvItemAdapter?.setNewData(envData)
        var timer = object : CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                CheckEnvUtils.getInstance().getCheckEnv(this@OfflineActivity, false)
                checkenvItemAdapter?.notifyDataSetChanged()
                CheckEnvUtils.getInstance().stopCheckEnv(this@OfflineActivity)
                tv_checkenv_start.visibility(
                    CheckEnvUtils.getInstance().checkvolumeAndMemory
                )

                if (CheckEnvUtils.getInstance().checkEnvResult) {
                    //????????????
                    pagetwo_count.visibility(true)
                    ll_checkenv.visibility(false)
                    tv_checkenvstate.text = "??????"
                    tv_checkenvstate.background =
                        ContextCompat.getDrawable(this@OfflineActivity, R.drawable.tx_checkenv_pass)
                    startTimer1()
                } else {
                    pagetwo_count.visibility(false)
                    ll_checkenv.visibility(true)
                    tv_checkenvstate.text = "?????????"
                    tv_checkenvstate.background =
                        ContextCompat.getDrawable(this@OfflineActivity, R.drawable.tx_checkenv_fail)
                }
            }
        }

        timer!!.start()
    }

    var checkenvItemAdapter: CheckenvItemAdapter? = null
    private fun initCheckEnvRecyclerView() {

        checkenvItemAdapter = CheckenvItemAdapter()
        checkenv_recyclerview?.layoutManager = LinearLayoutManager(this)
        checkenv_recyclerview?.adapter = checkenvItemAdapter

        tv_checkenv_retry.setOnClickListener(
            CheckDoubleClickListener {
                startCheckEnv()
            }
        )

        tv_checkenv_start.setOnClickListener(
            CheckDoubleClickListener {
                showPageOne()
            }
        )
        tv_checkenv_exit.setOnClickListener(
            CheckDoubleClickListener {
                end()
            }
        )
        startCheckEnv()
    }


    var page_readnextPage: View? = null
    var page_readnextPage1: View? = null
    var page_ttsPage: View? = null
    var page_agent_ocrPage: View? = null
    var page_idcaPage: View? = null
    var page_11Page: View? = null
    var page_signPage: View? = null
    var page_asr_resultPage: View? = null
    var page_endPage: View? = null
    var page_asr_userPage: View? = null
    var page_local_signPage: View? = null
    var page_local_nativesignPage: View? = null

    fun inflate() {
        page_readnextPage = layoutInflater.inflate(R.layout.tx_page_readnext, null)
        page_readnextPage1 = layoutInflater.inflate(R.layout.tx_page_readnext, null)
        page_ttsPage = layoutInflater.inflate(R.layout.tx_page_tts, null)
        page_agent_ocrPage = layoutInflater.inflate(R.layout.tx_page_agent_ocr, null)
        page_idcaPage = layoutInflater.inflate(R.layout.tx_page_idcomparison, null)
        page_11Page = layoutInflater.inflate(R.layout.tx_page_textread, null)
        page_signPage = layoutInflater.inflate(R.layout.tx_page_sign, null)
        page_asr_resultPage = layoutInflater.inflate(R.layout.tx_page_asr_result, null)
        page_endPage = layoutInflater.inflate(R.layout.tx_page_end, null)
        page_asr_userPage = layoutInflater.inflate(R.layout.tx_page_asr_user, null)
        page_local_signPage = layoutInflater.inflate(R.layout.tx_page_local_sign, null)
        page_local_nativesignPage = layoutInflater.inflate(R.layout.tx_page_local_nativesign, null)
        page_asr_userPage!!.findViewById<TextView>(R.id.ll_page_voice_result_retry)
            .setOnClickListener(this)
    }


    var isCacheLeftVideo = false //?????????????????????????????????????????????

    //??????????????????????????????
    fun checkLeftVideoToRightScreen(view: View, checkVideoToRight: Boolean, content: String) {
        mTrtcVideolayout?.setPersonView(View.GONE)
        mTrtcVideolayout?.setll_remote_skip("", View.GONE, View.GONE)
        val findEntityBytype = offlineVideoLayoutManager.findEntityBytype("1")


        val businessLayout = findEntityBytype.layout as BusinessLayout
        businessLayout.contentView.removeAllViews()
        businessLayout.contentView.addView(view)
        if (isCacheLeftVideo == checkVideoToRight) {

        } else {
            offlineVideoLayoutManager.makeFullVideoView(1)
        }

        if (!checkVideoToRight) {
            // ???????????????view
            page_readnextPage1?.visibility = View.VISIBLE
            page_readnextPage1?.findViewById<TextView>(R.id.tv_readNext_content)?.text = content
        } else {
            page_readnextPage1?.visibility = View.GONE
        }

        isCacheLeftVideo = checkVideoToRight
        resetWebviewUrl()
    }

    fun showWatingPage(title: String, titleContent: String) {
        startTtsController(titleContent, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {

            }

            override fun onFail(err: String?, code: Int) {
            }

        })
        checkLeftVideoToRightScreen(page_readnextPage!!, false, titleContent)


        mTrtcVideolayout?.setll_remote_skip(titleContent, View.VISIBLE, View.VISIBLE)
    }

    //?????????????????????  ????????????????????????
    private fun showReadNextPage(title: String, titleContent: String) {
        page_readnextPage?.visibility(true)
        tv_skip.visibility(false)
        tv_continue.visibility(false)
        checkLeftVideoToRightScreen(page_readnextPage!!, false, titleContent)

    }

    //?????????????????????  ????????????????????????
    private fun showTTSNext(contentStr: String) {
        tv_skip.visibility(false)
        tv_continue.visibility(false)
        checkLeftVideoToRightScreen(page_ttsPage!!, false, contentStr)
        //??????
        startTtsController(contentStr, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                //????????????
                //???????????????????????????
                autoCheckBoolean = true
                setFailType("", "")
                quickEnterRoom(true)
            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }

    //?????????????????? ????????????????????????
    private fun showUserASR(fillterData: String) {
        tv_skip.visibility(false)
        checkLeftVideoToRightScreen(page_asr_userPage!!, true, "")
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
        stopCheckPhotoInVideo()
        mFailCacheArray.clear()
        mSuccessCacheArray.clear()
        if (isRetry) {
            mTrtcVideolayout?.setll_page_voice_result(View.GONE, false, "", null)
        }
        mWatingArray = jsonArray
        tv_skip.visibility(false)

        startTtsController(title, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {

            }

            override fun onFail(err: String?, code: Int) {
                //tts ???????????? ??????
                showToastMsg("??????????????????")

            }
        })
        handler?.removeCallbacksAndMessages(null)
        llPage8timer?.cancel()
        llPage8timer = null

        checkLeftVideoToRightScreen(page_idcaPage!!, false, title)

        if (2 == jsonArray.length()) {
            //?????????
            mTrtcVideolayout?.startTwoRoundView()
        } else if (1 == jsonArray.length()) {
            mTrtcVideolayout?.startRoundView()
        } else {
            showToastMsg("??????????????????????????????")
        }

        Handler().postDelayed({
            if (jsonArray.length() == 0) {

            } else {
                mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                    if (jsonArray.length() == 1) {
                        //??????????????????????????????
                        try {
                            val tagrtOb = jsonArray.getString(0)
                            val byteToBitmap = SystemCommon.getInstance().byteToBitmap(p0)
                            val jsonObject = JSONObject(mRoomInfo)
                            var mName = ""
                            var agentID = ""
                            if ("agent" == tagrtOb) {
                                mName = jsonObject.optString("agentName", "")
                                agentID = jsonObject.optString("agentID", "")
                            } else if ("policyholder" == tagrtOb) {
                                mName = jsonObject.optString("policyholderName", "")
                                agentID = jsonObject.optString("policyholderID", "")
                            }
                            val encode = Base64.encode(byteToBitmap, Base64.DEFAULT)
                            val bulider = StringBuilder("data:image/png;base64,")
                            bulider.append(String(encode))

                            val replace = bulider.toString().replace("\n", "")
                            val uploadShotPic = UploadShotPic()

                            uploadShotPic.apply {
                                serviceId = mServiceId
                                facePhoto = replace
                                idCardNum = agentID
                                name = mName
                            }
                            SystemHttpRequest.getInstance().agentIdCard(
                                uploadShotPic,
                                object : HttpRequestClient.RequestHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                        val jsonObject = JSONObject(json)
                                        val status = jsonObject.getString("status")
                                        val jsonObject2 = if ("1" == status) {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", mServiceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "????????????")
                                                    })
                                                    put("roomType", "idComparison-fail")
                                                    put("userId", tagrtOb)
                                                })

                                            }
                                        } else {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", mServiceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "????????????")
                                                    })
                                                    put("roomType", "idComparison-success")
                                                    put("userId", tagrtOb)
                                                })

                                            }
                                        }
                                        pushMessage(
                                            jsonObject2,
                                            object : OfflineActivity.RoomHttpCallBack {
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
                                            mTrtcVideolayout?.stopRoundView()
                                            mTrtcVideolayout?.setHollowOutView(View.GONE)
                                            startCheckPhotoInVideo()
                                        }

                                    }

                                    override fun onFail(err: String?, code: Int) {
                                        runOnUiThread {
                                            mTrtcVideolayout?.stopRoundView()
                                            mTrtcVideolayout?.setHollowOutView(View.GONE)
                                            startCheckPhotoInVideo()
                                        }

                                    }

                                })
                        } catch (e: Exception) {

                        }


                    } else {
                        SystemCommon.getInstance().cropBitmap(
                            p0
                        ) { bytes, bytes1 ->
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
                            SystemHttpRequest.getInstance().agentIdCard(
                                uploadShotPic,
                                object : HttpRequestClient.RequestHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                        val jsonObject = JSONObject(json)
                                        val status = jsonObject.optString("status")
                                        val jsonObject2 = if ("1" == status) {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "????????????")
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
                                                        put("roomMessage", "????????????")
                                                    })
                                                    put("roomType", "idComparison-success")
                                                    put("userId", "agent")
                                                })

                                            }
                                        }
                                        pushMessage(
                                            jsonObject2,
                                            object : OfflineActivity.RoomHttpCallBack {
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
                                                    put("roomMessage", "????????????")
                                                })
                                                put("roomType", "idComparison-fail")
                                                put("userId", "agent")
                                            })

                                        }
                                        pushMessage(
                                            jsonObject2,
                                            object : OfflineActivity.RoomHttpCallBack {
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
                            SystemHttpRequest.getInstance().agentIdCard(
                                uploadShotPic1,
                                object : HttpRequestClient.RequestHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                        val jsonObject = JSONObject(json)
                                        val status = jsonObject.getString("status")
                                        val jsonObject2 = if ("1" == status) {
                                            JSONObject().apply {
                                                put("type", "idComparison")
                                                put("serviceId", serviceId)
                                                put("step", JSONObject().apply {
                                                    put("data", JSONObject().apply {
                                                        put("roomMessage", "????????????")
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
                                                        put("roomMessage", "????????????")
                                                    })
                                                    put("roomType", "idComparison-success")
                                                    put("userId", "policyholder")
                                                })

                                            }
                                        }
                                        pushMessage(
                                            jsonObject2,
                                            object : OfflineActivity.RoomHttpCallBack {
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
                                                    put("roomMessage", "????????????")
                                                })
                                                put("roomType", "idComparison-fail")
                                                put("userId", "policyholder")
                                            })

                                        }
                                        pushMessage(
                                            jsonObject2,
                                            object : OfflineActivity.RoomHttpCallBack {
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
                    }


                }
            }
        }, 3000)


    }

    var webView: WebView? = null
    private fun showTextReadPage(promtStr: String, url: String, isPdf: Boolean) {
        TxLogUtils.i("showTextReadPage:url-----${url}---isPdf${isPdf}")
        checkLeftVideoToRightScreen(page_11Page!!, true, "")
        page_11Page?.findViewById<TextView>(R.id.tv_prompt1)?.text = promtStr

        page_11Page?.findViewById<TextView>(R.id.tv_textread_skip)?.visibility(!isPdf)
        page_11Page?.findViewById<TextView>(R.id.tv_textread_skip)?.setOnClickListener(
            CheckDoubleClickListener {
                autoCheckBoolean = true
                setFailType("", "")
                quickEnterRoom(isSystem = true)
            }
        )

        webView =
            page_11Page?.findViewById<WebView>(R.id.textreadWebView)
        val settings: WebSettings = webView?.getSettings()!!
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setAppCacheEnabled(true)
        settings.defaultTextEncodingName = "UTF-8"
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        webView?.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY)
        webView?.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    TxLogUtils.i("????????????")
//                    showToastMsg("????????????")
                }
            }
        })
//        webView?.setWebViewClient(object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                view.loadUrl(url)
//                return true
//            }
//        })
        if (!url.isEmpty()) {
            webView?.loadUrl(url)
        } else {
            showToastMsg("url??????")
        }

    }

    private fun resetWebviewUrl() {
        webView?.loadUrl("")

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
        SystemHttpRequest.getInstance()
            .signPic(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    //{"errCode":0,"result":"test"}
                    callback.onSuccess(json)
                }

                override fun onFail(err: String?, code: Int) {
                    callback.onFail(err, code)

                }

            })
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

    //?????????????????????
    var timer: CountDownTimer? = null

    private fun startTitleTimer() {
        if (timer != null) {

        } else {
            timer = object : CountDownTimer(60000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    room_time.text = "???????????????" + DateUtils.getCurrentTime()
                    if (null != mTrtcVideolayout) {
                        if (null == SystemBaiduLocation.instance?.getLocationInfo()?.city && null == SystemBaiduLocation.instance?.getLocationInfo()?.province) {
                            mTrtcVideolayout?.setLocationStr(
                                "?????????????????????"
                            )
                        } else {
                            mTrtcVideolayout?.setLocationStr(
                                SystemBaiduLocation.instance?.getLocationInfo()?.city + SystemBaiduLocation.instance?.getLocationInfo()?.province
                            )
                        }

                    }
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
        checkLeftVideoToRightScreen(page_endPage!!, true, "")
    }

    var list: java.util.ArrayList<MultiItemEntity>? = null
    var mExpandableItemAdapter: ExpandableItem1Adapter? = null
    fun initEndRecyclerview(jsonArray: JSONArray) {

        list = java.util.ArrayList()
        list?.clear()
        var isFailBoolean = false
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.optJSONObject(index)
            val name = jsonObject.optString("name")
            val stepsJsonArray = jsonObject.optJSONArray("steps")
            val isPubliclist: java.util.ArrayList<MultiItemEntity> =
                java.util.ArrayList<MultiItemEntity>()
            val isNoPublicItem = LevelItem1(isPubliclist, name)
            for (index1 in 0 until stepsJsonArray.length()) {
                val jsonObject1 = stepsJsonArray.optJSONObject(index1)
                val fileBean = FileBean()
                fileBean.name = jsonObject1.optString("name")
                fileBean.failType = jsonObject1.optString("autoFailType")
                fileBean.failReason = jsonObject1.optString("autoFailReason")
                isFailBoolean = true
                isNoPublicItem.addSubItem(
                    fileBean
                )
            }

            list?.add(isNoPublicItem)
        }
        if (isFailBoolean) {
//            tv_continue.visibility(true)
            jugeTenantId()
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI?????????????????????"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.tx_txcolor_ED6656))
        } else {
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI??????????????????"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.tx_txcolor_40D4A1))
        }
        LogUtils.i(list.toString())
        val recyclerView = page_endPage?.findViewById<RecyclerView>(R.id.endpage_recyclerview)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        mExpandableItemAdapter = ExpandableItem1Adapter(list!!)
        recyclerView?.adapter = mExpandableItemAdapter
    }


    //?????????????????????
    private fun cancelTitleTimer() {
        TxLogUtils.i("?????????????????????")
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
                pagetwo_count.text = "${millisUntilFinished / 1000 + 1}s????????????????????????"
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
                page_asr_userPage!!.findViewById<TextView>(R.id.tv_count_second).text =
                    "${millisUntilFinished / 1000}S"
            }

            override fun onFinish() {
                //15s ????????????
                //????????????
                //????????????
                cancelAbsCredentialProvider()

                pushMessage(JSONObject().apply {
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


    private var stepDataNode: JSONObject? = null
    private var failureButtonJSONArray: JSONArray? = null //????????????
    private var keywordsRuleJSONObject: JSONObject? = null //????????????
    private var stepDataNodeType = ""

    private fun showNextStep(buttonStr: String) {

        tv_skip.text = when (buttonStr) {
            "startRecord" -> {
                tv_skip.visibility(true)
                "????????????"
            }
            "next" -> {
                tv_continue.visibility(false)
                "?????????"
            }
            "finishRecord" -> {
                tv_skip.visibility(true)
                tv_continue.visibility(false)
                "????????????"
            }

            else -> {
                "?????????"
            }
        }
    }

    //??????title????????????
    var isShowStartRecord = false


    var autoCheckTypeStr = "System" // Button ??????????????????????????? System ???????????????????????????
    var autoCheckBoolean = true  //????????????????????????
    var failType = ""  //????????????????????????
    var failReason = ""  //????????????????????????

    //????????????????????????
    var mCurrentStartTimer = 0L
    var mCurrentEndTimer = 0L
    var mCurrentStartTime = 0L
    var mCurrentEndTime = 0L

    fun quickEnterRoom(isSystem: Boolean) {
        destroylongTextTtsController()
        autoCheckTypeStr = if (isSystem) {
            "System"
        } else {
            "Button"
        }

        mCurrentEndTimer = System.currentTimeMillis()

        mCurrentEndTime = mCurrentStartTime + (mCurrentEndTimer - mCurrentStartTimer) / 1000L

        LogUtils.i("quickEnterRoom------???????????????:${mCurrentStartTime}?????????-----???????????????:${mCurrentEndTime}?????????")
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
                LogUtils.i("????????????")
                autoCheckBoolean = true
                setFailType("", "")
                quickEnterRoom(true)
                mTrtcVideolayout?.setll_page_voice_result(View.GONE, true, "????????????", null)
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
            showToastMsg("??????textArray???????????????")
        }

        return stringBuffer.toString()
    }

    var agentASRPassword = ""
    var processIndex = 0
    var stepIndex = 0
    var targetJSONArray: JSONArray? = null

    //???????????????????????????????????????????????????

    //??????????????????????????????socket????????????????????????
    var mCurrentMsg: JSONObject? = null
    override fun onReceiveMSG(data: JSONObject) {

        try {

            if (data.optString("serviceId") == mServiceId) {

                val mType = data.optString("type")
                if (mType == "roomMessage" || mType == "end") { //??????type ???roomMessage ???????????? ???????????????????????????

                } else if (mType == "syncNotice") {
                    //????????????????????????
                    TxLogUtils.i("?????????syncNotice")
                    runOnUiThread {
                        autoCheckBoolean = true
                        setFailType("", "")
                        quickEnterRoom(isSystem = true)
                    }
                } else {
                    LogUtils.i("????????????------$data")

                    var finished = data.optBoolean("finished", false)
                    if (finished) {
                        //??????
                        runOnUiThread {

                            showPageend()
                            initEndRecyclerview(data.optJSONArray("finishedStep"))
                            showLinkName("", "")
                        }

                    } else {

                        var stepDataJson: JSONObject? = null
                        if (data.has("step")) {
                            stepDataJson = data.getJSONObject("step")
                        } else {

                        }


                        val roomType = stepDataJson?.optString("roomType")
                        cancleTimer()
                        //????????????????????????????????????
                        if (roomType!!.isNotEmpty()) { //??????????????????type??????
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
                                            findViewById<LinearLayout>(R.id.ll_page_voice_result).visibility(
                                                false
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(
                                                false
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(
                                                true
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_retry).visibility(
                                                false
                                            )
                                            findViewById<TextView>(R.id.page_asr_voice).visibility(
                                                false
                                            )
                                            findViewById<TextView>(R.id.tv_user_content2).text =
                                                roomMessage

                                        }
                                    }
                                }
                                "soundOCR-fail" -> {
                                    runOnUiThread {
                                        isPassed = false
                                        strBuffer.delete(0, strBuffer.length)

                                        page_asr_userPage!!.apply {
                                            findViewById<LinearLayout>(R.id.ll_page_voice_result).visibility(
                                                true
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_no).visibility(
                                                true
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_yes).visibility(
                                                false
                                            )
                                            findViewById<TextView>(R.id.ll_page_voice_result_retry).visibility(
                                                true
                                            )
                                            findViewById<TextView>(R.id.page_asr_voice).visibility(
                                                false
                                            )
                                            val ll_page_voice_result_jump =
                                                findViewById<TextView>(R.id.ll_page_voice_result_jump)
                                            val ll_page_voice_result_mark =
                                                findViewById<TextView>(R.id.ll_page_voice_result_mark)
                                            val ll_page_voice_result_retry =
                                                findViewById<TextView>(R.id.ll_page_voice_result_retry)
                                            ll_page_voice_result_jump.setOnClickListener(
                                                CheckDoubleClickListener {
                                                    autoCheckBoolean = false
                                                    setFailType("????????????", "?????????????????????")
                                                    quickEnterRoom(isSystem = false)
                                                }

                                            )
                                            ll_page_voice_result_mark.setOnClickListener(
                                                CheckDoubleClickListener {
                                                    autoCheckBoolean = true
                                                    setFailType("????????????", "?????????????????????")
                                                    quickEnterRoom(isSystem = false)
                                                }

                                            )
                                            ll_page_voice_result_retry.setOnClickListener(
                                                CheckDoubleClickListener {
                                                    pushMessage(
                                                        mCurrentMsg!!,
                                                        object : OfflineActivity.RoomHttpCallBack {
                                                            override fun onSuccess(json: String?) {

                                                            }

                                                            override fun onFail(
                                                                err: String?,
                                                                code: Int
                                                            ) {

                                                            }

                                                        })
                                                }

                                            )

                                            //??????????????????
                                            for (i in 0 until failureButtonJSONArray?.length()!!) {
                                                val btJSONObject: JSONObject =
                                                    failureButtonJSONArray!!.getJSONObject(i)
                                                val key = btJSONObject.optString("key", "")
                                                val buttonName =
                                                    btJSONObject.optString("buttonName", "")
                                                val checkBoolean =
                                                    btJSONObject.optBoolean("check", true)
                                                if ("releaseSuccessful" == key) {
                                                    ll_page_voice_result_mark.text = buttonName
                                                    ll_page_voice_result_mark.visibility(
                                                        checkBoolean
                                                    )
                                                } else if ("releaseFailure" == key) {
                                                    ll_page_voice_result_jump.text = buttonName
                                                    ll_page_voice_result_jump.visibility(
                                                        checkBoolean
                                                    )
                                                } else if ("retry" == key) {
                                                    ll_page_voice_result_retry.text = buttonName
                                                    ll_page_voice_result_retry.visibility(
                                                        checkBoolean
                                                    )
                                                } else {
                                                }
                                            }

                                        }


                                    }
                                }

                                "idComparison-success" -> {
                                    setFailType("", "")
                                    runOnUiThread {
                                        isPassed = true
                                        val mUserId = stepDataJson?.optString("userId")
                                        //????????????
                                        fifterMemberList("", mUserId!!)
                                    }
                                }
                                "idComparison-fail" -> {
                                    setFailType("????????????", "???????????????")
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        isPassed = false
                                        val mUserId = stepDataJson?.optString("userId")
                                        //????????????
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
                                            showidComparisonPage(
                                                stringBuffer.toString(),
                                                stepDataNode!!.getJSONArray("target")!!,
                                                true
                                            )
                                        }


                                    } else {
                                        showToastMsg("??????textArray???????????????")
                                    }

                                }

                                "idComparison-collect-success" -> {
                                    runOnUiThread {
                                        startCheckPhotoInVideo()
                                        autoCheckBoolean = true
                                        val target = stepDataJson!!.optJSONArray("target")
                                        mTrtcVideolayout?.stopRoundView()
                                        mTrtcVideolayout?.setll_page_voice_result(
                                            View.VISIBLE,
                                            true,
                                            "????????????",
                                            null
                                        )
                                        startAutoNextStep(true)
                                    }
                                }
                                "idComparison-collect-fail" -> {
                                    runOnUiThread {
                                        startCheckPhotoInVideo()
                                        val dataJson = stepDataJson!!.getJSONObject("data")
                                        val target = stepDataJson!!.optJSONArray("target")
                                        val roomMessage = dataJson.getString("roomMessage")
                                        autoCheckBoolean = false
                                        mTrtcVideolayout?.stopRoundView()

                                        mTrtcVideolayout?.setll_page_voice_result(
                                            View.VISIBLE,
                                            false,
                                            roomMessage,
                                            failureButtonJSONArray
                                        )
                                    }

                                }
                                else -> {
                                }
                            }

                        } else {

                            mCurrentMsg = data
                            if (data.has("step")) {
                                stepDataNode = data.getJSONObject("step")
                            } else {

                            }
                            //textTTS ????????????
                            //soundOCR ????????????
                            //idComparison ????????????
                            //waiting ????????????
                            //signFile ????????????
                            //productTTS ??????????????????
                            //textRead ????????????
                            processIndex = stepDataNode!!.optInt("processIndex")
                            stepIndex = stepDataNode!!.optInt("stepIndex")

                            when (mType) {
                                "nextStep", "preStep" -> {
                                    runOnUiThread {
                                        showNextStep("next")
                                        showLinkName(
                                            stepDataNode!!.optString("name", ""),
                                            "(${processIndex + 1}/${mAllProcessIndex})"
                                        )
                                    }
                                    stepDataNodeType = stepDataNode!!.optString("baseType", "")
                                    when (stepDataNodeType) {
                                        "waiting" -> {//???????????????,??????????????????????????????
                                            runOnUiThread {
                                                val fillterData = fillterData(stepDataNode!!)
                                                showWatingPage(fillterData, fillterData)

                                            }


                                        }
                                        "textTTS", "productTTS" -> { //????????????,?????????????????????????????????
                                            runOnUiThread {
                                                val fillterData = fillterData(stepDataNode!!)
                                                showTTSNext(fillterData)
                                            }

                                        }
                                        "soundOCR" -> { //?????????????????????
                                            runOnUiThread {
                                                targetJSONArray =
                                                    stepDataNode!!.optJSONArray("target")!!
                                                failureButtonJSONArray =
                                                    stepDataNode!!.optJSONArray("failureButton")!!
                                                keywordsRuleJSONObject =
                                                    stepDataNode!!.optJSONObject("keywordsRule")!!
                                                val targetOb = targetJSONArray!!.optString(0)
                                                if ("agent" == targetOb) {
                                                    startAudioRecognize()
                                                    startVoiceTimer(15000)

                                                    val jsonArray =
                                                        stepDataNode?.getJSONObject("data")
                                                            ?.getJSONArray("textArray")
                                                    //todo
                                                    agentASRPassword = jsonArray!!.getString(0)!!

                                                    showUserASR("??????????????????:" + jsonArray!!.getString(0)!!)
                                                } else {
                                                    startAudioRecognize()
                                                    startVoiceTimer(5000)
                                                    val jsonArray =
                                                        stepDataNode?.getJSONObject("data")
                                                            ?.getJSONArray("textArray")
                                                    agentASRPassword = jsonArray!!.getString(0)!!

                                                    showUserASR("??????????????????:" + jsonArray!!.getString(0)!!)
                                                }

                                            }

                                        }


                                        "idComparison" -> { //????????????
                                            val dataObject2 = stepDataNode?.getJSONObject("data")
                                            failureButtonJSONArray =
                                                stepDataNode!!.optJSONArray("failureButton")!!

                                            if (dataObject2!!.has("textArray")) {
                                                val fillterData = fillterData(stepDataNode!!)
                                                runOnUiThread {
                                                    showidComparisonPage(
                                                        fillterData,
                                                        stepDataNode!!.getJSONArray("target")!!,
                                                        false
                                                    )
                                                }
                                            } else {
                                                showToastMsg("??????textArray???????????????")
                                            }

                                        }
                                        "signFile" -> {
                                            //??????????????????????????????????????????????????????
                                            if (instance!!.getTenantCode() == "remoteRecord") {
                                                runOnUiThread {
                                                    val fillterData = fillterData(stepDataNode!!)
                                                    showWatingPage(fillterData, fillterData)

                                                }
                                            } else {
                                                runOnUiThread {

                                                    val dataObject2 =
                                                        stepDataNode?.optJSONObject("data")
                                                    if (dataObject2!!.has("textArray")) {
                                                        val fillterData =
                                                            fillterData(stepDataNode!!)
                                                        startTtsController(
                                                            fillterData,
                                                            object : RoomHttpCallBack {
                                                                override fun onSuccess(json: String?) {
                                                                }

                                                                override fun onFail(
                                                                    err: String?,
                                                                    code: Int
                                                                ) {

                                                                }

                                                            })

                                                        showTextReadPage(
                                                            fillterData,
                                                            stepDataNode!!.optString(
                                                                "clientUrl",
                                                                ""
                                                            ),
                                                            stepDataNode!!.optBoolean(
                                                                "isPdf",
                                                                false
                                                            )
                                                        )
                                                    } else {
                                                        showToastMsg("??????textArray???????????????")
                                                    }


                                                }
                                            }
                                        }
                                        "textRead" -> {
                                            runOnUiThread {

                                                val dataObject2 =
                                                    stepDataNode?.optJSONObject("data")
                                                if (dataObject2!!.has("textArray")) {
                                                    val fillterData = fillterData(stepDataNode!!)
                                                    startTtsController(
                                                        fillterData,
                                                        object : RoomHttpCallBack {
                                                            override fun onSuccess(json: String?) {
                                                            }

                                                            override fun onFail(
                                                                err: String?,
                                                                code: Int
                                                            ) {

                                                            }

                                                        })

                                                    showTextReadPage(
                                                        fillterData,
                                                        stepDataNode!!.optString("clientUrl", ""),
                                                        stepDataNode!!.optBoolean("isPdf", false)
                                                    )
                                                } else {
                                                    showToastMsg("??????textArray???????????????")
                                                }


                                            }
                                        }
                                        "originSignFile" -> { //???????????????
                                            runOnUiThread {
                                                startTtsController(
                                                    fillterData(stepDataNode!!),
                                                    object : RoomHttpCallBack {
                                                        override fun onSuccess(json: String?) {
                                                        }

                                                        override fun onFail(
                                                            err: String?,
                                                            code: Int
                                                        ) {

                                                        }

                                                    })
                                                showSignPage(stepDataNode!!.getJSONArray("target")!!)
                                            }

                                        }
                                        "originTextRead" -> {
                                            runOnUiThread {

                                                val dataObject2 =
                                                    stepDataNode?.getJSONObject("data")
                                                if (dataObject2!!.has("textArray")) {
                                                    val fillterData = fillterData(stepDataNode!!)
                                                    startTtsController(
                                                        fillterData,
                                                        object : RoomHttpCallBack {
                                                            override fun onSuccess(json: String?) {
                                                            }

                                                            override fun onFail(
                                                                err: String?,
                                                                code: Int
                                                            ) {

                                                            }

                                                        })

                                                    showTextReadPage(
                                                        fillterData,
                                                        stepDataNode!!.optString("fileUrl", ""),
                                                        isPdf = false
                                                    )
                                                } else {
                                                    showToastMsg("??????textArray???????????????")
                                                }


                                            }
                                        }
                                        "identityOCR" -> {
                                            val dataObject2 = stepDataNode?.optJSONObject("data")

                                            if (dataObject2!!.has("textArray")) {
                                                val jsonArray =
                                                    dataObject2.optJSONArray("textArray")
                                                val stringBuffer = StringBuffer("")
                                                for (index in 0 until jsonArray.length()) {
                                                    val subStr = jsonArray.get(index) as String
                                                    stringBuffer.append("$subStr \n")
                                                }
                                                failureButtonJSONArray =
                                                    stepDataNode!!.optJSONArray("failureButton")!!
                                                runOnUiThread {
                                                    showOCRPage(
                                                        stringBuffer.toString(),
                                                        stepDataNode!!.optJSONArray("target")!!
                                                    )

                                                    tv_skip.visibility(false)

                                                }
                                            } else {
                                                showToastMsg("??????textArray???????????????")
                                            }
                                        }
                                        else -> {
                                        }
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

    private fun takePhotoOcr(param: OfflineActivity.PhotoHttpCallBack, role: String) {
        LogUtils.i("mUserId---$mUserId")
        mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) {
            val saveBitmap1 = SystemCommon.getInstance().byteToBitmap(it)
            if (saveBitmap1 != null) {
                val encode = Base64.encode(saveBitmap1, Base64.DEFAULT)
                val jsonObject = JSONObject(mRoomInfo)
                val mflowId = jsonObject.optString("flowId", "")
                val bulider = StringBuilder("data:image/png;base64,")
                bulider.append(String(encode))


                val replace = bulider.toString().replace("\n", "")
                val uploadShotPic = UploadOcrPic()
                uploadShotPic.apply {
                    serviceId = mServiceId
                    flowId = mflowId
                    idCard = replace
                    roole = role
                }
                SystemHttpRequest.getInstance()
                    .ocr(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
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
                param.onFail("?????????????????????", 1)
            }
        }


    }

    //???????????????????????????????????????
    private fun showOCRPage(title: String, jsonArray: JSONArray) {
        mTrtcVideolayout?.setIvOcr(View.VISIBLE)
        hideView()
        var taget = ""
        if (jsonArray.length() > 0) {
            mWatingArray = jsonArray
            taget = jsonArray.getString(0)
        } else {
            showToastMsg("????????????????????????")
        }



        checkLeftVideoToRightScreen(page_readnextPage!!, false, title)

        stopCheckPhotoInVideo()

        startTtsController(title, object : RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                Handler().postDelayed({

                    takePhotoOcr(object : OfflineActivity.PhotoHttpCallBack {
                        override fun onSuccess(json: String?) {
                            startCheckPhotoInVideo()
                            mTrtcVideolayout?.setIvOcr(View.GONE)
                            mTrtcVideolayout?.setll_page_voice_result(
                                View.VISIBLE,
                                true,
                                "",
                                failureButtonJSONArray
                            )
                            quickEnterRoom(true)
                        }

                        override fun onFail(err: String?, code: Int) {
                            //{"errCode":-2,"errInfo":"[OCR??????]????????????"}
                            mTrtcVideolayout?.setIvOcr(View.GONE)
                            startCheckPhotoInVideo()
                            autoCheckBoolean = false

                            mTrtcVideolayout?.setll_page_voice_result(
                                View.VISIBLE,
                                false,
                                when (code) {
                                    100 -> {
                                        "????????????????????????"
                                    }
                                    101 -> {
                                        "????????????????????????????????????"
                                    }
                                    else -> {
                                        "?????????????????????????????????????????????"
                                    }
                                },
                                failureButtonJSONArray
                            )
                        }

                    }, taget)
                }, 2000)
            }

            override fun onFail(err: String?, code: Int) {
            }

        })

    }


    private fun showSignPage(jsonArray: JSONArray) {
        val signatureview =
            page_local_nativesignPage?.findViewById<SignatureView>(R.id.iv_page_local_signatureview)
        page_local_nativesignPage?.apply {
            findViewById<LinearLayout>(R.id.ll_clear)?.visibility(true)
            findViewById<LinearLayout>(R.id.ll_clear)?.setOnClickListener(
                CheckDoubleClickListener {
                    signatureview?.clear()
                }
            )
            findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(false)
            findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(false)
            findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(false)
            findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(false)
            findViewById<TextView>(R.id.tv_name_sign)!!.text = "?????????"
        }
        val checkName = when (jsonArray.getString(0)) {
            "policyholder" -> policyholderName
            "agent" -> agentName
            else -> {
                insuredName
            }
        }

        page_local_nativesignPage?.findViewById<TextView>(R.id.tv_name)!!.text =
            when (jsonArray.getString(0)) {
                "policyholder" -> "????????????" + checkName
                "agent" -> "????????????" + checkName
                else -> {
                    "????????????" + checkName
                }
            }

        checkLeftVideoToRightScreen(page_local_nativesignPage!!, true, "")
        signatureview?.clear()
        //??????????????????
        page_local_nativesignPage?.findViewById<TextView>(R.id.tv_sign)?.setOnClickListener(
            CheckDoubleClickListener {
                upload(signatureview!!, object : PhotoHttpCallBack {
                    override fun onSuccess(json: String?) {
                        runOnUiThread {
                            LogUtils.i("upload------$json")
                            page_local_nativesignPage?.findViewById<TextView>(R.id.tv_name_sign)!!.text =
                                "?????????" + json
                            if (checkName == json) {
                                //?????????????????????
                                page_local_nativesignPage?.apply {
                                    findViewById<LinearLayout>(R.id.ll_clear)?.visibility(false)
                                    findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(
                                        false
                                    )
                                    findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(
                                        true
                                    )
                                    findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(
                                        false
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(
                                        false
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(
                                        false
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(
                                        false
                                    )
                                }
                                startAutoNextStep(true)
                            } else {
                                page_local_nativesignPage?.apply {
                                    findViewById<LinearLayout>(R.id.ll_clear)?.visibility(false)
                                    findViewById<LinearLayout>(R.id.ll_page12_result)?.visibility(
                                        true
                                    )
                                    findViewById<TextView>(R.id.ll_page12_result_success)?.visibility(
                                        false
                                    )
                                    findViewById<TextView>(R.id.ll_page12_result_fail)?.visibility(
                                        true
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_mark)?.visibility(
                                        true
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_jump)?.visibility(
                                        true
                                    )
                                    findViewById<TextView>(R.id.ll_page_voice_result_retry)?.visibility(
                                        true
                                    )

                                    //??????
                                    findViewById<TextView>(R.id.ll_page_voice_result_mark)?.setOnClickListener {
                                        quickEnterRoom(true)

                                    }
                                    findViewById<TextView>(R.id.ll_page_voice_result_jump)?.setOnClickListener {
                                        setFailType("????????????", "????????????????????????????????????")
                                        quickEnterRoom(true)
                                    }
                                    findViewById<TextView>(R.id.ll_page_voice_result_retry)?.setOnClickListener {
                                        pushMessage(
                                            mCurrentMsg!!,
                                            object : OfflineActivity.RoomHttpCallBack {
                                                override fun onSuccess(json: String?) {

                                                }

                                                override fun onFail(err: String?, code: Int) {

                                                }

                                            })
                                    }

                                }

                            }
                        }
                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
            }


        )

        //??????
        page_local_signPage?.findViewById<TextView>(R.id.tv_clear)?.setOnClickListener(
            CheckDoubleClickListener {
                signatureview?.clear()
            }
        )

    }

    private fun filterASRPassword(word: String): Boolean {
        if (word.isEmpty()) {
            return false
        }
        val notContainArray = keywordsRuleJSONObject!!.getJSONArray("notContain")
        var contain = true
        for (index in 0 until notContainArray.length()) {
            val notContainStr = notContainArray.getString(index)
            contain = word.contains(notContainStr)
            if (contain) {
                return false
            }
        }
        //??????????????????????????????
        if (contain) {
            //?????????
            return false
        } else {
            //
            val containsArray = keywordsRuleJSONObject!!.getJSONArray("contains")
            val stringBuilder = StringBuilder("")
            for (index in 0 until containsArray.length()) {
                val containsStr = containsArray.getJSONObject(index)
                val conditionsStr = containsStr.optString("conditions")
                val nameStr = containsStr.optString("name")
                if (conditionsStr == "and") {
                    stringBuilder.append("$nameStr@")
                } else {
                    stringBuilder.append("$nameStr,")
                }
            }

            val split = stringBuilder.split(",")
            if (split.size == 1) {
                contain = word.contains(split[0])
            } else {
                split.forEach {
                    if (it.isNotEmpty()) {
                        val contains = word.contains(it)
                        contain = contains
                        if (contains) {
                            return contain
                        }
                    }
                }
            }

            return contain
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
                pushMessage(JSONObject().apply {
                    put("type", "idComparison")
                    put("serviceId", mServiceId)
                    put("step", JSONObject().apply {
                        put("data", JSONObject().apply {
                            put("roomMessage", "????????????")
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
                                str.append("?????????")
                            } else {
                                str.append("????????????")
                            }

                        } else if ("policyholder".equals(obj)) {
                            if (index == 0) {
                                str.append("?????????")
                            } else {
                                str.append("????????????")
                            }

                        } else if ("insured".equals(obj)) {
                            if (index == 0) {
                                str.append("?????????")
                            } else {
                                str.append("????????????")
                            }
                        } else {

                        }
                    }
                    str.append("????????????????????????")
                    pushMessage(JSONObject().apply {
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
                            str.append("?????????")
                        } else {
                            str.append("????????????")
                        }

                    } else if ("policyholder".equals(obj)) {
                        if (index == 0) {
                            str.append("?????????")
                        } else {
                            str.append("????????????")
                        }

                    } else if ("insured".equals(obj)) {
                        if (index == 0) {
                            str.append("?????????")
                        } else {
                            str.append("????????????")
                        }
                    } else {

                    }
                }
                str.append("????????????????????????")
                pushMessage(JSONObject().apply {
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

        // ????????????
        //        ClientConfiguration.setServerProtocolHttps(false) // ????????????https???????????????

        ClientConfiguration.setMaxAudioRecognizeConcurrentNumber(2) // ???????????????????????????????????????

        ClientConfiguration.setMaxRecognizeSliceConcurrentNumber(10) // ????????????????????????????????????

        // ???????????????????????????sdk????????????????????????????????????secretKey??????????????????????????????????????????????????????????????????????????????

    }


    var strBuffer = StringBuffer()
    var mOldSegmentStr = ""

    fun startAudioRecognize() {
        val audioRecognizeResultListener: AudioRecognizeResultListener =
            object : AudioRecognizeResultListener {
                override fun onSuccess(request: AudioRecognizeRequest?, result: String?) {
                    LogUtils.i("result-------$result")

                }

                override fun onFailure(
                    request: AudioRecognizeRequest?,
                    clientException: ClientException?,
                    serverException: ServerException?
                ) {
                    LogUtils.i("onFailure-------$clientException------$serverException")
                }

                override fun onSliceSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult,
                    order: Int
                ) {
                    runOnUiThread {
                        val replaceTV = replaceTV(result?.text)
                        page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text =
                            "${replaceTV}"
                        page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2)
                            .visibility(true)
                    }
                    LogUtils.i("onSliceSuccess-------${result?.text}")
                }

                override fun onSegmentSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult?,
                    order: Int
                ) {
                    LogUtils.i("onSegmentSuccess-------${result?.text}")

                    runOnUiThread {
                        val text = result?.text
                        if (text?.isNotEmpty()!!) {
                            strBuffer.append(result?.text)
                            val replaceTV1 = replaceTV(strBuffer.toString())
                            LogUtils.i("filterASRPassword--replaceTV1----$replaceTV1")
                            page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2).text =
                                "$replaceTV1"
                            page_asr_userPage!!.findViewById<TextView>(R.id.tv_user_content2)
                                .visibility(true)
                            mOldSegmentStr = strBuffer?.toString()
                            strBuffer.delete(0, strBuffer.length)
                            cancelAbsCredentialProvider()
                            voiceTimer?.cancel()
                            voiceTimer = null
                            //??????????????????
                            val filterASRPassword = filterASRPassword(replaceTV1)

                            LogUtils.i("filterASRPassword------$filterASRPassword")
                            //????????????
                            //?????????????????????
                            pushMessage(JSONObject().apply {
                                put("serviceId", mServiceId)
                                put("type", "soundOCR")
                                put("step", JSONObject().apply {
                                    put("data", JSONObject().apply {
                                        put("roomMessage", replaceTV1)
                                    })
                                    var result = if (filterASRPassword) {
                                        "soundOCR-success"
                                    } else {
                                        "soundOCR-fail"
                                    }
                                    put("roomType", result)
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
            // 1????????????AAIClient?????????
            if (aaiClient == null) {
                aaiClient = AAIClient(this, mAppid.toInt(), 0, mSecretId, credentialProvider)
            }
            // 2?????????????????????????????????

            audioRecognizeRequest = AudioRecognizeRequest.Builder()
                .pcmAudioDataSource(AudioRecordDataSource()) // ?????????????????????????????????
                .build()

            // 3??????????????????????????????????????????


            // ?????????????????????
            audioRecognizeConfiguration = AudioRecognizeConfiguration.Builder()
                .setSilentDetectTimeOut(true)// ???????????????????????????true???????????????????????????
                .audioFlowSilenceTimeOut(5000) // ??????????????????????????????
                .minAudioFlowSilenceTime(2000) // ?????????????????????????????????
                .minVolumeCallbackTime(80) // ??????????????????
                .sensitive(2.5f)
                .build()

        } catch (e: ClientException) {
            e.printStackTrace()
        }

        // 4?????????????????????
        Thread(Runnable {
            if (aaiClient != null) {

                LogUtils.i("audioRecognizeRequest-----${audioRecognizeRequest?.requestId}")
                aaiClient?.startAudioRecognize(
                    audioRecognizeRequest,
                    audioRecognizeResultListener,
                    audioRecognizeConfiguration
                )
            }
        }).start()
    }


    fun cancelAbsCredentialProvider() {
        // 1??????????????????id

        val requestId = audioRecognizeRequest?.requestId
        // 2?????????cancel??????

        Thread(Runnable {
            if (aaiClient != null) {
                //???????????????????????????????????????
                aaiClient!!.cancelAudioRecognize(requestId!!)
            }
        }).start()
    }

    public fun replaceTV(newWord: String): String {
        val replace = newWord.replace(
            "???", ""
        )

        val replace2 = replace.replace(
            "???", ""
        )
        return replace2
    }


    public fun showLinkName(linkName: String, linkIndex: String) {
        tv_linkname.text = linkName
        tv_linknameindex.text = linkIndex
    }

    //judgecount ??????????????????personCount???????????????
    private fun checkColorPerson(judgecount: String, personCount: String): Int =
        if (judgecount == personCount) {
            ContextCompat.getColor(
                this@OfflineActivity,
                R.color.tx_txcolor_40D4A1
            )
        } else {
            ContextCompat.getColor(
                this@OfflineActivity,
                R.color.tx_txred
            )
        }

    private var mCheckLocal = false
    private var startCheckPhotoInVideoTimer: CountDownTimer? = null

    //??????????????????
    private fun stopCheckPhotoInVideo() {
        startCheckPhotoInVideoTimer?.cancel()
    }

    //??????????????????
    private fun startCheckPhotoInVideo() {
        if (null == startCheckPhotoInVideoTimer) {
            startCheckPhotoInVideoTimer = object : CountDownTimer(600000, 3000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                        ThreadPoolManager.getInstance().execute {
                            val bytes = SystemCommon.getInstance()?.byteToBitmap(p0)
                            if (bytes != null) {
                                val encode = Base64.encode(bytes, Base64.DEFAULT)
                                val bulider = StringBuilder("data:image/png;base64,")
                                bulider.append(String(encode))

                                val replace = bulider.toString().replace("\n", "")
                                val jsonObject = JSONObject()
                                jsonObject.put("img", replace)
                                SystemHttpRequest.getInstance()
                                    .faceDetection(
                                        jsonObject,
                                        object : HttpRequestClient.RequestHttpCallBack {
                                            override fun onSuccess(json: String?) {
                                                //???????????? 0 , ???1
                                                LogUtils.i("??????????????????onSuccess$json")
                                                runOnUiThread {
                                                    var color = if (mSelfInsurance!!) {
                                                        checkColorPerson("1", json!!)
                                                    } else {
                                                        if (policyholdeRequalInsured) { //2
                                                            checkColorPerson("2", json!!)
                                                        } else {//3
                                                            checkColorPerson("3", json!!)
                                                        }
                                                    }
                                                    mTrtcVideolayout!!.setToastStr(
                                                        "???????????????${json}???",
                                                        color
                                                    )
                                                }

                                            }

                                            override fun onFail(err: String?, code: Int) {
                                                //???????????? 0 , ???1
                                                LogUtils.i("??????????????????onFail${err}")
                                            }
                                        }

                                    )

                            }
                        }


                    }

                }

                override fun onFinish() {
                    startCheckPhotoInVideoTimer?.start()
                }
            }
        }


        startCheckPhotoInVideoTimer!!.start()

    }

    var isShowedAgentHaveFace = false;


    fun hideView() {
        page_checkenv.visibility(false)

    }

    override fun onConnect() {
        super.onConnect()
        LogUtils.i("rooms-onConnect")

        SystemSocket.instance?.setMSG(TXManagerImpl.instance?.getLoginName()!!, mServiceId)
    }

    override fun onBackPressed() {
        end()
    }


    private fun takePhoto(param: PhotoHttpCallBack) {
        LogUtils.i("mUserId---$mUserId")
        Handler().postDelayed({
            mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                val bytes = SystemCommon.getInstance().byteToBitmap(p0)
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
                    SystemHttpRequest.getInstance()
                        .agentIdCard(uploadShotPic, object : HttpRequestClient.RequestHttpCallBack {
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
                    param.onFail("?????????????????????", 1)
                }
            }
        }, 2000)


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
                    mStartRecordNetMillis = System.currentTimeMillis()
                    mCurrentStartTimer = System.currentTimeMillis()
                    mCurrentStartTime = (mStartRecordNetMillis - mStartRecordTimeMillis) / 1000L
                    isStartRecord = true
                    mTrtcVideolayout?.setll_remote_skip("", View.GONE, View.GONE)
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
                mTrtcVideolayout?.setll_page_voice_result(View.GONE, false, "", null)
                //??????
                if ("idComparison".equals(stepDataNodeType)) {
                    setFailType("????????????", "????????????")
                } else {

                }

                autoCheckBoolean = true
                quickEnterRoom(isSystem = false)
            }
            "1" -> {
                //??????
                mTrtcVideolayout?.setll_page_voice_result(View.GONE, false, "", null)
                setFailType("????????????", "????????????")
                autoCheckBoolean = false
                quickEnterRoom(isSystem = false)
            }
            "2" -> {
                //??????
                mTrtcVideolayout?.setll_page_voice_result(View.GONE, false, "", null)
                if ("idComparison".equals(stepDataNodeType)) {

                    val jsonArray = JSONArray()
                    mFailCacheArray.forEach {
                        jsonArray.put(it)
                    }
                    mCurrentMsg!!.getJSONObject("step").remove("target")
                    mCurrentMsg!!.getJSONObject("step").put("target", jsonArray)
                    mCurrentMsg!!.getJSONObject("step").put("roomType", "idComparison-retry")
                    pushMessage(mCurrentMsg!!, object : OfflineActivity.RoomHttpCallBack {
                        override fun onSuccess(json: String?) {

                        }

                        override fun onFail(err: String?, code: Int) {

                        }
                    })

                } else {
                    val title =
                        stepDataNode!!.getJSONObject("data")
                            .getJSONArray("textArray").getString(0)
                    runOnUiThread {
                        showOCRPage(
                            title,
                            stepDataNode!!.optJSONArray("target")!!
                        )

                        tv_skip.visibility(false)

                    }
                }


            }
            else -> {
            }
        }
    }

    /***
     *  ???????????? remoteRecord
     */
//    public fun jugeTenantIdIsRemoteRecord() :Boolean = TXManagerImpl.instance?.getTenantCode().equals("remoteRecord")
    public fun jugeTenantIdIsRemoteRecord(): Boolean = true


    /***
     *  ?????????????????? remoteRecordPoc1 ???????????? remoteRecord
     */
    public fun jugeTenantId() {
        //getTenantCode
        tv_continue1.visibility(
            !TXManagerImpl.instance?.getTenantCode().equals("remoteRecord")
        )


    }

}

