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
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.glide.TxGlide
import com.common.widget.immersionbar.TxBarHide
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
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
import com.tencent.trtc.TRTCStatistics
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.config.socket.SocketBusiness
import com.txt.sl.entity.bean.PointBean
import com.txt.sl.entity.bean.FileBean
import com.txt.sl.entity.bean.LevelItem1
import com.txt.sl.entity.bean.UploadOcrPic
import com.txt.sl.entity.bean.UploadShotPic
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
import com.txt.sl.ui.video.trtc.TRTCRightVideoLayoutManager
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_remote_room.*
import kotlinx.android.synthetic.main.tx_activity_remote_room.ll_title
import kotlinx.android.synthetic.main.tx_activity_remote_room.room_time
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_continue
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_continue1
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_linkname
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_linknameindex
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_skip
import kotlinx.android.synthetic.main.tx_activity_remote_room.tv_text_continue
import kotlinx.android.synthetic.main.tx_page_asr_user.*
import kotlinx.android.synthetic.main.tx_page_checkenv.*
import kotlinx.android.synthetic.main.tx_page_end.*
import kotlinx.android.synthetic.main.tx_page_envpreview.*
import kotlinx.android.synthetic.main.tx_page_error.*
import kotlinx.android.synthetic.main.tx_page_linkpreview.*
import kotlinx.android.synthetic.main.tx_page_nativesign.*
import kotlinx.android.synthetic.main.tx_page_nativetextread.*
import kotlinx.android.synthetic.main.tx_page_readnext.*
import kotlinx.android.synthetic.main.tx_page_readnext_title.*
import kotlinx.android.synthetic.main.tx_page_sign.*
import kotlinx.android.synthetic.main.tx_page_textread.*
import kotlinx.android.synthetic.main.tx_page_textread.tv_prompt1
import kotlinx.android.synthetic.main.tx_page_textread.tv_textread_skip
import kotlinx.android.synthetic.main.tx_page_tts.*
import kotlinx.android.synthetic.main.tx_page_tts.tts_page_content
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.DecimalFormat

class RoomActivity : BaseActivity(), View.OnClickListener, SocketBusiness,
    TRTCRightVideoLayoutManager.IVideoLayoutListener {
    //?????????????????????
    private var mBackButton //?????????????????????????????????
            : ImageView? = null

    private var mTRTCCloud // SDK ?????????policyholder
            : TRTCCloud? = null
    private var mIsFrontCamera = true // ?????????????????????
    private var mRemoteUidList // ????????????Id??????
            : MutableList<String>? = null

    private val mUserCount = 0 // ????????????????????????
    private var mRoomId // ??????Id
            : String? = null
    private var mUserId // ??????Id
            : String? = null

    private var mRoomInfo // ????????????
            : String? = null

    private var mTaskId // ????????????
            : String? = null
    private var mRecordType = "" // ????????????

    override fun isFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }

    private var audioManager: AudioManager? = null
    override fun initView() {
        statusBarConfig.hideBar(TxBarHide.FLAG_HIDE_STATUS_BAR)
        super.initView()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.mode = AudioManager.MODE_NORMAL
        handleIntent()
        // ??????????????????????????????
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        initView1()
        showPageCheck()
        initTtsController()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                audioManager?.adjustStreamVolume(
                    if (jugeTenantIdIsRemoteRecord()) {
                        AudioManager.STREAM_VOICE_CALL
                    }else{
                        AudioManager.STREAM_MUSIC
                    }

                   ,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP
                )
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                audioManager?.adjustStreamVolume(
                    if (jugeTenantIdIsRemoteRecord()) {
                        AudioManager.STREAM_VOICE_CALL
                    }else{
                        AudioManager.STREAM_MUSIC
                    },
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP
                )

                return true
            }
        }
        return true

    }


    override fun getLayoutId(): Int {
        return R.layout.tx_activity_remote_room
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
            if (intent.getStringExtra(Constant.RECORDTYPE) != null) {
                mRecordType = intent.getStringExtra(Constant.RECORDTYPE)!!
            }
        }

    }

    private var screenRecordHelper: ScreenRecordHelper? = null

    private var mTrtcrightvideolayoutmanager: TRTCRightVideoLayoutManager? = null
    private fun initView1() {
        getServiceId()
        SystemBaiduLocation.instance!!.startLocationService()
        SystemSocket.instance!!.connectSocket()
        SystemSocket.instance!!.setonSocketListener(this)
        mBackButton = findViewById(R.id.trtc_ic_back)

        mTrtcrightvideolayoutmanager = findViewById(R.id.trtcrightvideolayoutmanager)
        mTrtcrightvideolayoutmanager?.initView(this, roomPerson)
        mTrtcrightvideolayoutmanager?.setMySelfUserId(mUserId)
        mTrtcrightvideolayoutmanager?.setIVideoLayoutListener(this)


        mRemoteUidList = ArrayList()


        mBackButton?.setOnClickListener(this)

        initBusiness()
        initAbsCredentialProvider()

    }

    interface RoomHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
    }

    interface PhotoHttpCallBack {
        fun onSuccess(json: String?)
        fun onFail(err: String?, code: Int)
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
                        com.common.widget.toast.ToastUtils.show(err!!)
                    }
                }

            })
    }

    var orderDetailsItemlists = ArrayList<String>()
    var baseQuickAdapter: VideoDetailsItemAdapter? = null
    var mAllProcessIndex = 0
    fun initRecyclerview(jsonArray: JSONArray) {

        orderDetailsItemlists.clear()
        if (null != jsonArray) {
            for (index in 0 until jsonArray.length()) {
                val s = jsonArray.get(index) as String
                orderDetailsItemlists.add(s)
            }
            recyclerview.layoutManager = LinearLayoutManager(this)
            baseQuickAdapter = VideoDetailsItemAdapter(orderDetailsItemlists!!)
            recyclerview.adapter = baseQuickAdapter
            tv_welcome_title.text = "???????????????????????????${orderDetailsItemlists.size}????????????"
            mAllProcessIndex = orderDetailsItemlists.size
        } else {
            showToastMsg("??????????????????")
        }

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
    var userName = ""
    var agentName = ""
    var insuredName = ""
    var roomPerson = 0
    var canCheck = false //??????????????????????????????????????????????????????
    var jsonObject1: JSONObject? = null
    var agentID: String? = null

    var mAppid = 0L
    var mSecretId = ""
    var mSecretKey = ""
    fun getServiceId() {
        jsonObject1 = JSONObject(mRoomInfo)
        mServiceId = jsonObject1!!.optString("serviceId", "")
        userName = jsonObject1!!.optString("policyholderName", "")
        agentName = jsonObject1!!.optString("agentName", "")
        roomPerson = jsonObject1!!.optInt("roomPerson", 0)
        insuredName = jsonObject1!!.optString("insuredName", "")


        agentID = jsonObject1!!.optString("agentID", "")
        val wxCloudConfJO = jsonObject1!!.getJSONObject("wxCloudConf")
        mSecretId = wxCloudConfJO!!.optString("SecretId")
        mSecretKey = wxCloudConfJO!!.optString("SecretKey")
        mAppid = wxCloudConfJO!!.optString("AppId").toLong()
    }

    public fun pushMessage(jsonObject: JSONObject, listener: RoomHttpCallBack) {


        SystemHttpRequest.getInstance()
            .pushMessage(jsonObject.toString(), object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {
                        listener.onSuccess(json!!)
                    }

                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        if (!isFinishing) {
                            showToastMsg(err!!)
                        }

                        listener.onFail(err, code)
                    }
                }

            })
    }

    var isPassed = true
    var isStartRecord = false
    private fun initBusiness() {

        tv_skip.setOnClickListener(CheckDoubleClickListener {
            it as TextView
            when (it.text) {
                "?????????" -> {
                    if (roomPerson == 3) {
                        if (mRemoteUidList!!.size <= 1) {
                            showToastMsg("????????????????????????????????????")
                            return@CheckDoubleClickListener
                        }
                    } else {
                        if (mRemoteUidList!!.size == 0) {
                            showToastMsg("????????????????????????????????????")
                            return@CheckDoubleClickListener
                        }
                    }
                    //??????????????????????????????????????????????????????
                    if (startAutoNextStepTimer != null) {
                        startAutoNextStepTimer?.cancel()
                        startAutoNextStepTimer = null
                    }


                    //???????????????????????????????????????????????????????????????????????????

                    if (!isStartRecord) {
                        pushMessage(JSONObject().apply {
                            put("type", "recordExchange")
                            put("serviceId", mServiceId)
                            put("step", JSONObject().apply {
                                put("roomType", "recordExchange")
                                put("roomMessage", jsonObject1!!.getJSONArray("process"))
                            })
                        }, object : RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }
                        })

                    } else {


                        quickEnterRoom(isSystem = false)
                    }


                }
                "????????????" -> {  //???????????? --?????????????????????-2
                    if (roomPerson == 3) {
                        if (mRemoteUidList!!.size <= 1) {
                            showToastMsg("????????????????????????????????????")
                            return@CheckDoubleClickListener
                        }
                    } else {
                        if (mRemoteUidList!!.size == 0) {
                            showToastMsg("????????????????????????????????????")
                            return@CheckDoubleClickListener
                        }
                    }

                    startRecord(object : RoomHttpCallBack {

                        override fun onSuccess(json: String?) {

                            SystemLogHelper.getInstance().start()
                            mStartRecordNetMillis = System.currentTimeMillis()
                            mCurrentStartTimer = System.currentTimeMillis()
                            mCurrentStartTime =
                                (mStartRecordNetMillis - mStartRecordTimeMillis) / 1000L
                            isStartRecord = true

                        }

                        override fun onFail(err: String?, code: Int) {

                        }

                    })


                }
                getString(R.string.tx_str_endRecord) -> {
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

                                stopCheckPhotoInVideo()
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


                                TxSPUtils.put(this@RoomActivity, mTaskId, jsonObject.toString())
                                //??????????????????
                                //????????????
                                val customDialog = UploadVideoDialog(this@RoomActivity)
                                customDialog.setScreenRecordStr(jsonObject.toString())
                                customDialog.setOnConfirmClickListener(object :
                                    UploadVideoDialog.OnConfirmClickListener {
                                    override fun onVideoUpload(isFinish: Boolean) {
                                        if (!isFinish) {
                                            TxSPUtils.put(
                                                this@RoomActivity,
                                                mTaskId,
                                                jsonObject.toString()
                                            )
                                        }

                                        finish()
                                    }

                                })
                                TxPopup.Builder(this@RoomActivity)
                                    .maxWidth(1200)
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
                                    .asCustom(customDialog).show()
                            }
                        }

                        override fun onFail(err: String?, code: Int) {
                            runOnUiThread {
                                LogUtils.d(err!!)
                                showToastMsg(err)
                            }
                        }

                    }, false)

                }
                "????????????" -> {
                    //??????????????????
                    showPageTwo()
                }
                else -> {
                }
            }
        })


        ll_page_voice_result_retry.isClickable = true
        ll_page_voice_result_retry.setOnClickListener(
            CheckDoubleClickListener {
                pushMessage(mCurrentMsg!!, object : RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
            }

        )
        ll_page_voice_result_jump.setOnClickListener(
            //??????
            CheckDoubleClickListener {
                quickEnterRoom(false)
            }
        )

        ll_page_voice_result_mark.setOnClickListener(
            //??????
            CheckDoubleClickListener {
                setFailType("", "")
                quickEnterRoom(false)
            }

        )

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
                            pushMessage(JSONObject().apply {
                                put("serviceId", mServiceId)
                                put("type", "voiceSpeed")
                                put("step", JSONObject().apply {
                                    put("roomType", "voiceSpeed")
                                    put("voiceSpeed", voiceSpeed)
                                })
                            }, object : RoomHttpCallBack {
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


        tv_continue1.setOnClickListener (
               CheckDoubleClickListener {
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


                        nextStep(isPassed, object : RoomActivity.RoomHttpCallBack {
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
            }
                )


    }

    var autoCheckTypeStr = "System" // Button ??????????????????????????? System ???????????????????????????
    var autoCheckBoolean = false  //????????????????????????
    var failType = ""  //????????????????????????
    var failReason = ""  //????????????????????????

    //????????????????????????
    var mCurrentStartTimer = 0L
    var mCurrentEndTimer = 0L
    var mCurrentStartTime = 0L
    var mCurrentEndTime = 0L

    var preTime = 0

    fun setFailType(failTypeStr: String, failReasonStr: String) {
        failType = failTypeStr
        failReason = failReasonStr
    }


    fun quickEnterRoom(isSystem: Boolean) {
        if ("idComparison".equals(stepDataNodeType)) {
            //????????????????????????
//            mTrtcrightvideolayoutmanager?.makeVideoView(2,1)
//            mTrtcrightvideolayoutmanager?.makeVideoView(1,0)
//            mTrtcrightvideolayoutmanager?.buildLayout()
            resetVideoLayout()
//            mTrtcrightvideolayoutmanager?.makeFullVideoView(1)
            layout_right.visibility = View.VISIBLE
        }
        autoCheckTypeStr = if (isSystem) {
            "System"
        } else {
            "Button"
        }

        mCurrentEndTimer = System.currentTimeMillis()

        mCurrentEndTime = mCurrentStartTime + (mCurrentEndTimer - mCurrentStartTimer) / 1000L

        LogUtils.i("quickEnterRoom------???????????????:${mCurrentStartTime}?????????-----???????????????:${mCurrentEndTime}?????????")
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

        nextStep(isPassed, checkJson, object : RoomActivity.RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                mCurrentStartTimer = mCurrentEndTimer
                mCurrentStartTime = mCurrentEndTime
            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }


    var allocCloudVideoView: TXCloudVideoView? = null

    private fun enterRoom() {
        SystemBaiduLocation.instance!!.requestLocation()
        tv_skip.visibility(true)
        ll_title.setBackgroundColor(ContextCompat.getColor(this, R.color.tx_txwhite))
        mTrtcrightvideolayoutmanager?.visibility(true)
        mTRTCCloud = TRTCCloud.sharedInstance(applicationContext)
        mTRTCCloud?.setListener(TRTCCloudImplListener(this@RoomActivity))
        allocCloudVideoView = mTrtcrightvideolayoutmanager?.allocCloudVideoView(
            mUserId,
            "agent",
            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
        )


        LogUtils.i("width--${allocCloudVideoView?.width}-----height--${allocCloudVideoView?.height}")
        // ??????????????? SDK ??????
        val trtcParams = TRTCParams()
        trtcParams.sdkAppId = jsonObject1!!.getInt("sdkAppId")
        trtcParams.userId = mUserId
        trtcParams.roomId = mRoomId!!.toInt()
        // userSig????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        trtcParams.userSig = jsonObject1!!.getString("agentSig")
        trtcParams.role = TRTCCloudDef.TRTCRoleAnchor
//        mTRTCCloud?.deviceManager?.setAudioRoute(TXDeviceManager.TXAudioRoute.TXAudioRouteSpeakerphone)

        // ?????????????????????????????????

        mTRTCCloud?.startLocalAudio(
            if (jugeTenantIdIsRemoteRecord()) {
                TRTCCloudDef.TRTC_AUDIO_QUALITY_DEFAULT
            }else{
                TRTCCloudDef.TRTC_AUDIO_QUALITY_MUSIC
            })
        // ?????????????????????????????????
        mTRTCCloud?.startLocalPreview(mIsFrontCamera, allocCloudVideoView)

        /**
         * ??????????????????????????????????????????????????????????????????5, ???????????????1???
         * ????????????.?????????????????????0 ?????????  1?????????  2?????????
         * ??????????????????????????????????????????????????????
         */
        val beautyManager = mTRTCCloud?.getBeautyManager()
        beautyManager?.apply {
            setBeautyStyle(Constant.BEAUTY_STYLE_NATURE)
//            setBeautyLevel(0)
//            setWhitenessLevel(0)
        }

        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360
        encParam.videoFps = Constant.VIDEO_FPS
        encParam.videoBitrate = Constant.RTC_VIDEO_BITRATE
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_LANDSCAPE

        mTRTCCloud?.setVideoEncoderParam(encParam)
        // ????????????
        mTRTCCloud?.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        clearLocationMap()
        isSoundOCRException = false
        sendBroadcast(Intent().apply {
            action = HomeActivity.br_action
        })
        SystemBaiduLocation.instance!!.stopLocationService()
        destroylongTextTtsController()
        stopCheckPhotoInVideo()
        mTrtcrightvideolayoutmanager?.hideAllStateView()
        SystemLogHelper.getInstance().stop()
        exitRoom()
        cancelAbsCredentialProvider()
        cancelTitleTimer()
        checkPhotoInVideoTimer?.cancel()
        checkPhotoInVideoTimer = null

        mCheckLocal = false

        checkLocalTimer?.cancel()
        checkLocalTimer = null
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
        SystemSocket.instance!!.disconnectSocket()
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

            R.id.page_8_voice_result_retry -> {
                pushMessage(mCurrentMsg!!, object : RoomHttpCallBack {
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

    private fun requestRecordPer() {
        //??????????????????
        if (screenRecordHelper == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                screenRecordHelper = ScreenRecordHelper(
                    this@RoomActivity,
                    object : ScreenRecordHelper.OnVideoRecordListener {
                        override fun onBeforeRecord() {
                        }

                        override fun onStartRecord() {
                            //????????????
                            //????????????app ??????????????????

                            showPageOne()
                            showNextStep("next")
                            enterRoom() //????????????????????????????????????????????????????????????

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
                recordAudio = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startRecord()
                }
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

    private inner class TRTCCloudImplListener(activity: RoomActivity) : TRTCCloudListener() {
        private val mContext: WeakReference<RoomActivity>
        override fun onEnterRoom(result: Long) {
            super.onEnterRoom(result)
            LogUtils.i("onEnterRoom-----result----$result")
            mTrtcrightvideolayoutmanager?.updateName(mUserId, "????????????$agentName")
            mTrtcrightvideolayoutmanager?.updateLocationStr(
                mUserId,
                SystemBaiduLocation.instance!!.getLocationInfo()?.city + SystemBaiduLocation.instance!!.getLocationInfo()?.province
            )
            mStartRecordTimeMillis = System.currentTimeMillis()
            LogUtils.i("mStartRecordTimeMillis-----$mStartRecordTimeMillis")
            startCheckPhotoInVideo()
        }

        override fun onStartPublishing(p0: Int, p1: String?) {
            super.onStartPublishing(p0, p1)

        }

        override fun onStatistics(p0: TRTCStatistics?) {
            super.onStatistics(p0)

        }

        override fun onExitRoom(reason: Int) {
            super.onExitRoom(reason)
            LogUtils.i("onExitRoom-----result----$reason")
        }


        override fun onNetworkQuality(
            localQuality: TRTCQuality?,
            remoteQuality: ArrayList<TRTCQuality>?
        ) {
            super.onNetworkQuality(localQuality, remoteQuality)

            mTrtcrightvideolayoutmanager?.updateNetworkQuality(
                localQuality?.userId,
                localQuality?.quality!!
            )
            remoteQuality?.forEach {
                mTrtcrightvideolayoutmanager?.updateNetworkQuality(it.userId, it.quality!!)
            }
            LogUtils.i("onNetworkQuality", "${localQuality?.quality}")
        }

        override fun onRemoteUserLeaveRoom(p0: String?, p1: Int) {
            super.onRemoteUserLeaveRoom(p0, p1)
            LogUtils.i("RoomActivity", "onRemoteUserLeaveRoom-----p0----$p0,p1---$p1")

            SystemHttpRequest.getInstance().setServiceRoomStatus(
                mServiceId,
                p0,
                "0",
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {
                    }

                })
        }

        override fun onRemoteUserEnterRoom(p0: String?) {
            super.onRemoteUserEnterRoom(p0)
            LogUtils.i("RoomActivity", "onRemoteUserEnterRoom----p0----$p0")
            SystemHttpRequest.getInstance().setServiceRoomStatus(
                mServiceId,
                p0,
                "1",
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {
                    }

                })
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            LogUtils.i(
                "RoomActivity",
                "onUserVideoAvailable userId $userId, mUserCount $mUserCount,available $available"
            )
            val index = mRemoteUidList!!.indexOf(userId)
            if (available) {
                if (index != -1) { //??????mRemoteUidList????????????????????????
                    return
                }
                mRemoteUidList!!.add(userId)
                refreshRemoteVideoViews(true, userId)
                //cancelRemoteTimer()
            } else {
                if (index == -1) { //??????mRemoteUidList??????????????????????????????
                    return
                }
                /// ????????????userId???????????????
                if (mTRTCCloud != null) {

                    mTRTCCloud!!.stopRemoteView(userId)
                    mRemoteUidList!!.removeAt(index)
                    refreshRemoteVideoViews(false, userId)
                    //???????????? ??????30s??? ??????????????? ?????????????????????
                    //starCheckRemoteTimer()
                }

            }
        }

        override fun onConnectionRecovery() {
            super.onConnectionRecovery()
            //SDK ???????????????????????????
            isConnect = true
            LogUtils.i("onConnectionRecovery")
        }

        override fun onConnectionLost() {
            super.onConnectionLost()
            //SDK ???????????????????????????
            isConnect = false
//            starLocalTimer()
            LogUtils.i("onConnectionLost")
        }

        override fun onTryToReconnect() {
            super.onTryToReconnect()
            LogUtils.i("onTryToReconnect")
        }

        override fun onUserAudioAvailable(userId: String?, available: Boolean) {
            super.onUserAudioAvailable(userId, available)
            LogUtils.i(
                "RoomActivity",
                "onUserAudioAvailable userId $userId, mUserCount $mUserCount,available $available"
            )
            if (available) {
                if (userId == mInsurantId) {
//                    checkPhotoInRemoteVideo()

//                    remotevideo_tv.visibility(false)

                } else if (userId == mInsuredId) {
//                    checkPhotoInRemoteVideo1()
//                    remotevideo1_tv.visibility(false)

                }

            } else {
                if (userId == mInsurantId) {
//                    remotevideo_tv.visibility(true)
//                    remotevideo_tv.text = "?????????????????????\n ????????????????????????"
                } else if (userId == mInsuredId) {
                    //todo
//                    remotevideo1_tv.visibility(true)
//                    remotevideo1_tv.text = "????????????????????????\n ????????????????????????"
                }

            }


        }

        //?????????????????????
        private fun refreshRemoteVideoViews(isShow: Boolean, userId: String) {
            if (isShow) {
                //?????????????????????
                SystemHttpRequest.getInstance()
                    .getRoomInfo(mServiceId, object : HttpRequestClient.RequestHttpCallBack {
                        override fun onSuccess(json: String?) {
                            //{"errCode":0,"result":{"agentId":"5ee88000c488564bd8621344","insurantId":"opN2-4hPLdm9wVtNkD5_xxH8YUuI"}}
                            LogUtils.i("$json")
                            val resultObject = JSONObject(json)
                            //?????????Id

                            if (resultObject.has("policyholderId")) {
                                mInsurantId = resultObject.optString("policyholderId")
                                if (mInsurantId == userId) {
                                    runOnUiThread {
                                        val allocCloudVideoView1 =
                                            mTrtcrightvideolayoutmanager?.allocCloudVideoView(
                                                mInsurantId,
                                                "policyholder",
                                                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                                            )
                                        //???????????????
                                        var name = ""
                                        name =
                                            if (mRecordType.isNotEmpty() && "0".equals(mRecordType)) {
                                                "?????????:$userName" + "\n ?????????:$insuredName"
                                            } else {
                                                "????????????$userName"
                                            }
                                        mTrtcrightvideolayoutmanager?.updateName(
                                            mInsurantId,
                                            name
                                        )
                                        val locationStr = getLocationStr(userId)
                                        TxLogUtils.i("getLocationStr(userId)-------$locationStr")
                                        mTrtcrightvideolayoutmanager?.updateLocationStr(userId, locationStr )
                                        mTRTCCloud!!.startRemoteView(
                                            mInsurantId,
                                            allocCloudVideoView1
                                        )
                                        mTRTCCloud!!.muteRemoteAudio(mInsurantId, false)
                                    }
                                    pushMessage(JSONObject().apply {
                                        put("serviceId", mServiceId)
                                        put("type", "location")
                                        put("step", JSONObject().apply {
                                            put("roomType", "location")
                                            put("userId", mUserId)
                                            put("data", JSONObject().apply {
                                                put(
                                                    "roomMessage",
                                                    SystemBaiduLocation.instance!!.getLocationInfo()?.city
                                                            + SystemBaiduLocation.instance!!.getLocationInfo()?.province
                                                )
                                            })
                                        })
                                    }, object : RoomHttpCallBack {
                                        override fun onSuccess(json: String?) {

                                        }

                                        override fun onFail(err: String?, code: Int) {
                                        }
                                    })

                                }


                            } else {

                            }
                            //?????????id
                            if (resultObject.has("insuredId")) {
                                mInsuredId = resultObject.optString("insuredId")
                                if (mInsuredId.equals(userId)) {
                                    runOnUiThread {
                                        val allocCloudVideoView1 =
                                            mTrtcrightvideolayoutmanager?.allocCloudVideoView(
                                                mInsuredId,
                                                "insured",
                                                TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                                            )
                                        mTrtcrightvideolayoutmanager?.updateName(
                                            mInsuredId,
                                            "????????????$insuredName"
                                        )

                                        val locationStr = getLocationStr(userId)
                                        TxLogUtils.i("getLocationStr(userId)-------$locationStr")
                                        mTrtcrightvideolayoutmanager?.updateLocationStr(userId, locationStr )
                                        mTRTCCloud!!.startRemoteView(
                                            mInsuredId,
                                            allocCloudVideoView1
                                        )
                                        mTRTCCloud!!.muteRemoteAudio(mInsuredId, false)
                                    }
                                }
                                pushMessage(JSONObject().apply {
                                    put("serviceId", mServiceId)
                                    put("type", "location")
                                    put("step", JSONObject().apply {
                                        put("roomType", "location")
                                        put("userId", mUserId)
                                        put("data", JSONObject().apply {
                                            put(
                                                "roomMessage",
                                                SystemBaiduLocation.instance?.getLocationInfo()?.city
                                                        + SystemBaiduLocation.instance?.getLocationInfo()?.province
                                            )
                                        })
                                    })
                                }, object : RoomHttpCallBack {
                                    override fun onSuccess(json: String?) {

                                    }

                                    override fun onFail(err: String?, code: Int) {
                                    }
                                })

                            } else {

                            }

                        }

                        override fun onFail(err: String?, code: Int) {
                        }

                    })
            } else {
                destroylongTextTtsController()
                if (userId == mInsurantId) {
                    mTrtcrightvideolayoutmanager?.updateVideoStatus(
                        userId,
                        false,
                        ""
                    )
                } else if (userId == mInsuredId) {
                    mTrtcrightvideolayoutmanager?.updateVideoStatus(
                        userId,
                        false,
                        ""
                    )
                }


            }


        }

        // ?????????????????????????????????????????? SDK ??????????????????
        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            LogUtils.d(Companion.TAG, "sdk callback onError")
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

    private fun showPageCheck() {
        hideView()
        tv_skip.visibility(true)
        tv_skip.text = "????????????"
        ll_envpreview.visibility(true)
    }

    private fun showPageOne() {
        hideView()
        layout_all.visibility(true)
        room_time.visibility(true)
        page_readnext_title.visibility(true)
        startTitleTimer()
    }

    //???????????????????????????
    private fun showRecordBefore() {
        hideView()
        ll_showLink.visibility(true)
        val jsonArray = jsonObject1!!.optJSONArray("process")
        initRecyclerview(jsonArray)
    }


    private fun showPageTwo() {
        hideView()
        tv_skip.visibility(false)
        page_checkenv.visibility(true)
        initCheckEnvRecyclerView()
    }


    private fun startCheckEnv() {
        pagetwo_count.visibility(false)
        ll_checkenv.visibility(false)
        tv_checkenvstate.text = "?????????"
        tv_checkenvstate.background = ContextCompat.getDrawable(this, R.drawable.tx_checkenv_bg)
        val envData = CheckEnvUtils.getInstance().getEnvData()
        CheckEnvUtils.getInstance().startCheckEnv(this, true)
        checkenvItemAdapter?.setNewData(envData)
        var timer = object : CountDownTimer(3000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                CheckEnvUtils.getInstance().getCheckEnv(this@RoomActivity, if (jugeTenantIdIsRemoteRecord()) {
                    true
                }else{
                    false
                })
                checkenvItemAdapter?.notifyDataSetChanged()
                CheckEnvUtils.getInstance().stopCheckEnv(this@RoomActivity)
                if (CheckEnvUtils.getInstance().checkvolumeAndMemory) {
                    //????????? ??????????????????
                    tv_checkenv_start.visibility(true)
                } else {
                    tv_checkenv_start.visibility(false)
                }
                if (CheckEnvUtils.getInstance().checkEnvResult) {
                    //????????????
                    pagetwo_count.visibility(true)
                    ll_checkenv.visibility(false)
                    tv_checkenvstate.text = "??????"
                    tv_checkenvstate.background =
                        ContextCompat.getDrawable(this@RoomActivity, R.drawable.tx_checkenv_pass)
                    startTimer1()
                } else {
                    pagetwo_count.visibility(false)
                    ll_checkenv.visibility(true)
                    tv_checkenvstate.text = "?????????"
                    tv_checkenvstate.background =
                        ContextCompat.getDrawable(this@RoomActivity, R.drawable.tx_checkenv_fail)
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

        tv_checkenv_start.setOnClickListener(CheckDoubleClickListener {
            requestRecordPer()
        })
        //????????????????????????


        tv_checkenv_exit.setOnClickListener(
            CheckDoubleClickListener {
                end()
            }
        )
        startCheckEnv()
    }


    //?????????????????????
    private fun showReadNextPage(title: String, titleContent: String) {
        hideView()
        hideVideoView()
        page_readnext.visibility(true)
        tv_readNext_content.text = titleContent
    }


    private fun showTTSNext(contentStr: String) {
        hideView()
        hideVideoView()
        page_tts.visibility(true)

        tts_page_content.text = contentStr
        if (jugeTenantIdIsRemoteRecord()) {

        }else{
            startTtsController(contentStr, object : OfflineActivity.RoomHttpCallBack {
            override fun onSuccess(json: String?) {
                autoCheckBoolean = true
                failType = ""
                failReason = ""
                quickEnterRoom(isSystem = true)
            }

            override fun onFail(err: String?, code: Int) {

            }

        })
        }

    }

    private fun showUserASR(prompt: String, fillterData: String, isAgent: Boolean) {
        hideView()
        hideVideoView()
        ll_page_voice.visibility(true)
        tv_agent_content.text = fillterData
        tv_asr_prompt.text = prompt

        tv_user_content2.visibility(false)
        ll_page_voice_result.visibility(false)
        ll_page_voice_result_no.visibility(false)
        ll_page_voice_result_yes.visibility(false)
        if (isAgent) {
            page_asr_voice.visibility(true)
        } else {
            page_asr_voice.visibility(false)
        }


    }


    fun cancelTimer() {
        llPage8timer?.cancel()
        llPage8timer = null
    }

    var mWatingArray: JSONArray? = null

    //???????????????????????????????????????
    private fun showWatingPage(title: String, jsonArray: JSONArray) {
        hideView()
        hideVideoView()
        if (jsonArray.length() > 0) {
            mWatingArray = jsonArray
            val tagrtOb = jsonArray.get(0)

            if (tagrtOb.equals("agent")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeVideoView(1,0)
//                mTrtcrightvideolayoutmanager?.makeVideoView(2,1)
//                mTrtcrightvideolayoutmanager?.buildLayout()
                mTrtcrightvideolayoutmanager?.makeFullVideoView(1)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "agent",
                    title,
                    View.VISIBLE,
                    View.VISIBLE
                )
            } else if (tagrtOb.equals("policyholder")) {
                //?????????????????????
                mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "policyholder",
                    title,
                    View.VISIBLE,
                    View.GONE
                )
            } else {
                if (roomPerson==3) {
                    mTrtcrightvideolayoutmanager?.makeFullVideoView(3)
                    mTrtcrightvideolayoutmanager?.updateSkipLayout(
                        "insured",
                        title,
                        View.VISIBLE,
                        View.GONE
                    )
                }else{
                    mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                    mTrtcrightvideolayoutmanager?.updateSkipLayout(
                        "insured",
                        title,
                        View.VISIBLE,
                        View.GONE
                    )
                }
                //?????????????????????

            }
        } else {
            showToastMsg("????????????????????????")
        }

        layout_right.visibility = View.INVISIBLE
        checkPhotoInVideoTimer?.cancel()
        checkPhotoInVideoTimer = null

    }

    //???????????????????????????????????????
    private fun showOCRPage(title: String, jsonArray: JSONArray) {
        hideView()
        hideVideoView()
        if (jsonArray.length() > 0) {
            mWatingArray = jsonArray
            val tagrtOb = jsonArray.get(0)

            var userType = if (tagrtOb.equals("agent")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeVideoView(1,0)
//                mTrtcrightvideolayoutmanager?.makeVideoView(2,1)
//                mTrtcrightvideolayoutmanager?.buildLayout()
                mTrtcrightvideolayoutmanager?.makeFullVideoView(1)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "agent",
                    title,
                    View.VISIBLE,
                    View.GONE
                )
                "agent"
            } else if (tagrtOb.equals("policyholder")) {
                //?????????????????????
                mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "policyholder",
                    title,
                    View.VISIBLE,
                    View.GONE
                )
                "policyholder"
            } else {
                //?????????????????????
                    if (roomPerson==2){
                        mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                        mTrtcrightvideolayoutmanager?.updateSkipLayout(
                            "insured",
                            title,
                            View.VISIBLE,
                            View.GONE
                        )
                    }else{
                        mTrtcrightvideolayoutmanager?.makeFullVideoView(3)
                        mTrtcrightvideolayoutmanager?.updateSkipLayout(
                            "insured",
                            title,
                            View.VISIBLE,
                            View.GONE
                        )
                    }

                "insured"
            }
            if (jugeTenantIdIsRemoteRecord()) {
                mTrtcrightvideolayoutmanager?.updateOcrLayout(userType, View.VISIBLE)

            }else{
                startTtsController(title, object : OfflineActivity.RoomHttpCallBack {
                    override fun onSuccess(json: String?) {
                        mTrtcrightvideolayoutmanager?.updateOcrLayout(userType, View.VISIBLE)
                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
            }





            if ("agent".equals(userType)) {
                //
                stopCheckPhotoInVideo()
                Handler().postDelayed({
                    takePhotoOcr(object : PhotoHttpCallBack {
                        override fun onSuccess(json: String?) {
                            startCheckPhotoInVideo()
                            pushMessage(JSONObject().apply {
                                put("type", "identityOCR-success")
                                put("serviceId", mServiceId)
                                put("step", JSONObject().apply {
                                    put("data", JSONObject().apply {
                                        put("roomMessage", "????????????")
                                    })
                                    put("roomType", "identityOCR-success")
                                    put("userId", mUserId)
                                })

                            }, object : RoomHttpCallBack {
                                override fun onSuccess(json: String?) {

                                }

                                override fun onFail(err: String?, code: Int) {
                                }
                            })
                        }

                        override fun onFail(err: String?, code: Int) {

                            startCheckPhotoInVideo()
                            pushMessage(JSONObject().apply {
                                put("type", "identityOCR-fail")
                                put("serviceId", mServiceId)
                                put("step", JSONObject().apply {
                                    put("data", JSONObject().apply {
                                        put(
                                            "roomMessage", when (code) {
                                                100 -> {
                                                    "????????????????????????"
                                                }
                                                101 -> {
                                                    "????????????????????????????????????"
                                                }
                                                else -> {
                                                    "?????????????????????????????????????????????"
                                                }
                                            }
                                        )
                                    })
                                    put("roomType", "identityOCR-fail")
                                    put("userId", mUserId)
                                })

                            }, object : RoomHttpCallBack {
                                override fun onSuccess(json: String?) {

                                }

                                override fun onFail(err: String?, code: Int) {
                                }
                            })
                        }

                    }, "agent")
                }, 2000)
            }

        } else {
            showToastMsg("????????????????????????")
        }

        layout_right.visibility = View.INVISIBLE
        checkPhotoInVideoTimer?.cancel()
        checkPhotoInVideoTimer = null


    }


    private var mFailCacheArray = ArrayList<String>()
    private var mSuccessCacheArray = ArrayList<String>()

    private fun fifterMemberList(failUserId: String, successUserId: String) {
        LogUtils.i(mWatingArray.toString())
        LogUtils.i(mSuccessCacheArray.toString())
        LogUtils.i(mFailCacheArray.toString())

        if (successUserId.isNotEmpty()) {
            val successEntity = mTrtcrightvideolayoutmanager?.findEntity(successUserId)
            if (!mSuccessCacheArray.contains(successEntity?.userType!!)) {
                mSuccessCacheArray.add(successEntity?.userType!!)
            }

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

                }, object : RoomHttpCallBack {
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

                    }, object : RoomHttpCallBack {
                        override fun onSuccess(json: String?) {

                        }

                        override fun onFail(err: String?, code: Int) {

                        }
                    })

                }
            }
        }


        if (failUserId.isNotEmpty()) {
            val failEntity = mTrtcrightvideolayoutmanager?.findEntity(failUserId)
            if (!mFailCacheArray.contains(failEntity?.userType!!)) {
                mFailCacheArray.add(failEntity?.userType!!)
            }

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

                }, object : RoomHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })

            }
        }


    }

    var llPage8timer: CountDownTimer? = null
    var handler: Handler? = null //????????????????????????????????????
    private fun showidComparisonPage(
        jsonArray: JSONArray,
        isRetry: Boolean,
        title: String
    ) {

        mSuccessCacheArray.clear()
        mFailCacheArray.clear()
        if (!isRetry) {
            hideView()
            hideVideoView()
            layout_right.visibility = View.INVISIBLE
            Handler().postDelayed({ mTrtcrightvideolayoutmanager?.makeFullVideoView(1) }, 200)
        }
        if (jugeTenantIdIsRemoteRecord()) {

        }else{
            startTtsController(title, object : OfflineActivity.RoomHttpCallBack {
            override fun onSuccess(json: String?) {

            }

            override fun onFail(err: String?, code: Int) {

            }

        })
        }


        mWatingArray = jsonArray
        for (index in 0 until jsonArray.length()) {
            val tagrtOb = jsonArray.getString(index)
            if (tagrtOb.equals("agent")) {
                //?????????????????????
//
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "agent",
                    "?????????...",
                    View.VISIBLE,
                    "123"
                )


                Handler().postDelayed(
                    {
                        mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType(
                            "agent",
                            View.VISIBLE
                        )
                    },
                    500
                )
                LogUtils.i("agent", "mTrtcrightvideolayoutmanager")
                stopCheckPhotoInVideo()
                Handler().postDelayed({
                    takePhoto(object : PhotoHttpCallBack {
                        override fun onSuccess(json: String?) {
                            LogUtils.i("takePhoto-onSuccess", json!!)
                            //{"status":"1","reason":"???????????????????????????"}
                            startCheckPhotoInVideo()
                            val jsonObject = JSONObject(json)
                            val status = jsonObject.optString("status")
                            var roomMessage = ""
                            var roomType = ""
                            roomMessage = if ("1" == status) {
                                roomType = "idComparison-fail"
                                "????????????"

                            } else {
                                roomType = "idComparison-success"
                                "????????????"
                            }


                            val jsonObject2 = JSONObject().apply {
                                put("type", "idComparison")
                                put("serviceId", mServiceId)
                                put("step", JSONObject().apply {
                                    put("data", JSONObject().apply {
                                        put("roomMessage", roomMessage)
                                    })
                                    put("roomType", roomType)
                                    put("userId", mUserId)
                                })

                            }
                            pushMessage(jsonObject2, object : RoomHttpCallBack {
                                override fun onSuccess(json: String?) {

                                }

                                override fun onFail(err: String?, code: Int) {

                                }
                            })
                            runOnUiThread {
                                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId(
                                    mUserId,
                                    View.GONE
                                )
                            }

                        }

                        override fun onFail(err: String?, code: Int) {
                            LogUtils.i("takePhoto-err", err!!)
                            startCheckPhotoInVideo()
                            pushMessage(JSONObject().apply {
                                put("type", "idComparison")
                                put("serviceId", mServiceId)
                                put("step", JSONObject().apply {
                                    put("data", JSONObject().apply {
                                        put("roomMessage", "????????????")
                                    })
                                    put("roomType", "idComparison-fail")
                                    put("userId", mUserId)
                                })

                            }, object : RoomHttpCallBack {
                                override fun onSuccess(json: String?) {

                                }

                                override fun onFail(err: String?, code: Int) {
                                }
                            })
                            runOnUiThread {
                                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId(
                                    mUserId,
                                    View.GONE
                                )
                            }
                        }

                    })
                }, 3000)

            } else if (tagrtOb.equals("policyholder")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "policyholder",
                    "?????????...",
                    View.VISIBLE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType(
                    "policyholder",
                    View.VISIBLE
                )
            } else if (tagrtOb.equals("insured")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeFullVideoView(3)
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "insured",
                    "?????????...",
                    View.VISIBLE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType(
                    "insured",
                    View.VISIBLE
                )
            }
        }


        handler = Handler()
        cancelAbsCredentialProvider()


    }

    var webView :WebView ?= null
    private fun showTextReadPage(promtStr: String, obj: String, url: String,isPdf :Boolean) {
        hideView()
        hideVideoView()
        page_basetype_textread.visibility(true)

        val settings: WebSettings = textreadWebView?.getSettings()!!
        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setAppCacheEnabled(true)
        settings.defaultTextEncodingName = "UTF-8"
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        if (!url.isEmpty()) {
            textreadWebView.loadUrl(url)
        } else {
            showToastMsg("url??????")
        }

        LogUtils.i("promtStr------$promtStr")
        tv_prompt1.text = promtStr
        tv_textread_skip.visibility(!isPdf)
        tv_textread_skip.setOnClickListener(
            CheckDoubleClickListener {
                autoCheckBoolean = true
                quickEnterRoom(false)
            }

        )
    }

    private fun resetWebviewUrl(){
        webView?.loadUrl("")

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
                }

                override fun onFinish() {
                    timer!!.start()

                }
            }

            timer!!.start()
            room_time.visibility(true)
        }


    }

    //?????????????????????
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
                pagetwo_count.text = "${millisUntilFinished / 1000}s????????????????????????"
            }

            override fun onFinish() {
                //????????????????????????
                requestRecordPer()
            }
        }

        timer1!!.start()

    }


    var voiceTimer1: CountDownTimer? = null
    private fun startVoiceTimer(millisInFuture :Long = 15000) {
        TxLogUtils.i("startVoiceTimer")
        voiceTimer1 = object : CountDownTimer(millisInFuture, 1000) {
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

            override fun onTick(millisUntilFinished: Long) {
                tv_count_second.text = "${millisUntilFinished / 1000}S"
            }

        }

        voiceTimer1!!.start()

    }


    var checkLocalTimer: CountDownTimer? = null
    private fun starLocalTimer() {
        checkLocalTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (!isConnect) {
                    TxPopup.Builder(this@RoomActivity).maxWidth(700)
                        .asConfirm("", "??????????????????????????????", "", "??????", object : OnConfirmListener {
                            override fun onConfirm() {
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

                                            finish()
                                        }
                                    }

                                    override fun onFail(err: String?, code: Int) {
                                        runOnUiThread {
                                            LogUtils.d(err!!)
                                            showToastMsg(err)
                                        }
                                    }

                                }, disableState = true)
                            }

                        }, null, true).show()

                }
            }
        }

        checkLocalTimer!!.start()

    }


    companion object {
        private const val TAG = "RoomActivity"
        private const val REQ_PERMISSION_CODE = 0x1000
    }


    private var stepDataNode: JSONObject? = null
    private var failureButtonJSONArray: JSONArray? = null //????????????
    private var keywordsRuleJSONObject: JSONObject? = null //????????????
    private var stepDataNodeType = ""

    private fun showNextStep(buttonStr: String) {

        tv_skip.text = when (buttonStr) {
            "startRecord" -> {
                tv_skip.visibility(true)

                tv_continue.visibility(!jugeTenantIdIsRemoteRecord())
                "????????????"
            }
            "next" -> {
                tv_continue.visibility(false)
                "?????????"
            }
            "finishRecord" -> {
                tv_continue.visibility(false)
                tv_skip.visibility(true)
                getString(R.string.tx_str_endRecord)
            }

            else -> {
                "?????????"
            }
        }


    }

    /***
     *  ?????????????????? remoteRecordPoc1
     */
    public fun jugeTenantId() {
        //getTenantCode
        tv_continue1.visibility(
            !TXManagerImpl.instance?.getTenantCode().equals("remoteRecord")
        )


    }
    /***
     *  ???????????? RemoteRecord
     */
//    public fun jugeTenantIdIsRemoteRecord() :Boolean = TXManagerImpl.instance?.getTenantCode().equals("remoteRecord")
    public fun jugeTenantIdIsRemoteRecord() :Boolean = true

    public fun showLinkName(linkName: String, linkIndex: String) {
        tv_linkname.text = linkName
        tv_linknameindex.text = linkIndex
    }


    //??????title????????????
    var isShowStartRecord = false


    var startAutoNextStepTimer: CountDownTimer? = null
    private fun startAutoNextStep(isPassed: Boolean) {

        startAutoNextStepTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                LogUtils.i("????????????")
                quickEnterRoom(isSystem = true)
            }
        }

        startAutoNextStepTimer!!.start()


    }

    fun fillterData(jsonOb: JSONObject): String {
        LogUtils.i("fillterData---$jsonOb")
        if (jsonOb.has("data")) {
            val dataObject2 = jsonOb.optJSONObject("data")
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
        } else {
            showToastMsg("??????data???????????????")
            return ""
        }
    }


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


    public fun nextStep(
        isPassed: Boolean,
        jsonOb: JSONObject,
        listener: RoomActivity.RoomHttpCallBack
    ) {
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
                        com.common.widget.toast.ToastUtils.show(err)
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

    public fun nextStep(isPassed: Boolean, listener: RoomActivity.RoomHttpCallBack) {

        nextStep(isPassed, JSONObject(), listener)
    }


    public fun showEndPage() {
        showNextStep("finishRecord")
        hideView()
        ll_pageend.visibility(true)

    }

    var list: java.util.ArrayList<MultiItemEntity>? = null
    var mExpandableItemAdapter: ExpandableItem1Adapter? = null
    fun initEndRecyclerview(jsonArray: JSONArray) {

        list = java.util.ArrayList()
        list?.clear()
        var isFailBoolean = false
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val name = jsonObject.optString("name")
            val stepsJsonArray = jsonObject.getJSONArray("steps")
            val isPubliclist: java.util.ArrayList<MultiItemEntity> =
                java.util.ArrayList<MultiItemEntity>()
            val isNoPublicItem = LevelItem1(isPubliclist, name)
            for (index1 in 0 until stepsJsonArray.length()) {
                val jsonObject1 = stepsJsonArray.getJSONObject(index1)
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
            jugeTenantId()
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI?????????????????????"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.tx_txcolor_ED6656))
        } else {
            tv_text_continue.visibility(true)
            tv_text_continue.text = "AI??????????????????"
            tv_text_continue.setTextColor(ContextCompat.getColor(this, R.color.tx_txcolor_40D4A1))
        }
        endpage_recyclerview.layoutManager = LinearLayoutManager(this)
        mExpandableItemAdapter = ExpandableItem1Adapter(list!!)
        endpage_recyclerview.adapter = mExpandableItemAdapter
    }

    var agentASRPassword = ""
    var processIndex = 0
    var stepIndex = 0

    //??????????????????roomtype?????????
    var mCurrentMsg: JSONObject? = null
    var targetJSONArray: JSONArray? = null
    var mInsurantId = "" //?????????
    var mInsuredId = ""  //?????????

    //???????????????
    var failTarget: JSONArray? = null

    var isSoundOCRException  = false //????????????????????????asr????????????
    var locationMsgMap : HashMap<String,String> ?=null

    fun getLocationStr(userId:String) :String{
        var location = ""
        if (null != locationMsgMap) {
         location = if (null == locationMsgMap?.get(userId)) {
              "????????????"
            }else{
               locationMsgMap?.get(userId)!!
            }
        }
        return  location
    }

    fun putLocationMap(userId: String,locationMsg: String){
        if (null == locationMsgMap ){
            locationMsgMap  = HashMap<String,String>()
        }
        locationMsgMap?.put(userId,locationMsg)
    }

    fun clearLocationMap(){
        locationMsgMap?.clear()
    }

    override fun onReceiveMSG(data: JSONObject) {
        try {

            if (data.getString("serviceId") == mServiceId) {
                LogUtils.i("scSC_Call_Status", "????????????------$data")
                val mType = data.optString("type")
                if (mType == "roomMessage") return
                if (mType == "location") { //?????????????????????????????????
                    runOnUiThread {
                        //onUserVideoAvailable
                        var userId = data.optJSONObject("step").optString("userId")
                        var locationMsg = data.optJSONObject("step").optJSONObject("data")
                            .optString("roomMessage")
                        mTrtcrightvideolayoutmanager?.updateLocationStr(userId, locationMsg)
                        putLocationMap(userId,locationMsg)
                    }

                } else if (mType == "faceDetection") {
                    runOnUiThread {
                        val jsonObject = data.getJSONObject("step")
                        var userId = jsonObject.optString("userId")
                        var haveFace = jsonObject.optBoolean("haveFace")
                        var number = jsonObject.optInt("number")
                        val findEntity =
                            mTrtcrightvideolayoutmanager?.findEntity(userId)
                        val facetxColor =
                            if (findEntity?.userType == "policyholder") { //?????????
                                if (haveFace) {
                                    if ("0".equals(mRecordType)) {
                                        //????????????????????????
                                        if (2.equals(number)) {
                                            //????????????????????????
                                            ContextCompat.getColor(
                                                this@RoomActivity,
                                                R.color.tx_txcolor_40D4A1
                                            )
                                        } else {
                                            ContextCompat.getColor(
                                                this@RoomActivity,
                                                R.color.tx_txred
                                            )

                                        }
                                    } else {
                                        if (1.equals(number)) {
                                            //????????????????????????
                                            ContextCompat.getColor(
                                                this@RoomActivity,
                                                R.color.tx_txcolor_40D4A1
                                            )
                                        } else {
                                            ContextCompat.getColor(
                                                this@RoomActivity,
                                                R.color.tx_txred
                                            )

                                        }

                                    }


                                } else {
                                    ContextCompat.getColor(
                                        this@RoomActivity,
                                        R.color.tx_txred
                                    )

                                }
                            } else {
                                if (haveFace) {
                                    if (1.equals(number)) {
                                        //????????????????????????
                                        ContextCompat.getColor(
                                            this@RoomActivity,
                                            R.color.tx_txcolor_40D4A1
                                        )
                                    } else {
                                        ContextCompat.getColor(
                                            this@RoomActivity,
                                            R.color.tx_txred
                                        )

                                    }

                                } else {
                                    ContextCompat.getColor(
                                        this@RoomActivity,
                                        R.color.tx_txred
                                    )

                                }
                            }
                        mTrtcrightvideolayoutmanager?.updateToastStr(
                            userId,
                            "???????????????$number ???",
                            facetxColor
                        )


                    }
                } else if (mType == "error") {

                    //
                    val status = data.optString("status")
                    val userList = data.optJSONArray("user")
                    val stringBuffer = StringBuffer("")
                    if (userList.length() > 0) {

                        for (index in 0 until userList.length()) {
                            val jsonObject = userList.optJSONObject(index)
                            val userId = jsonObject.optString("userId")
                            val theWay = jsonObject.optJSONArray("theWay")
                            val findEntity =
                                mTrtcrightvideolayoutmanager?.findEntity(userId)
                            when (findEntity?.userType) {
                                "agent" -> {
                                    stringBuffer.append("????????????")
                                }
                                "policyholder" -> {
                                    stringBuffer.append("????????????")
                                }
                                "insured" -> {
                                    stringBuffer.append("????????????")
                                }
                                else -> {
                                }
                            }

                        }
                    }

                    when (status) {
                        "disconnectRoom" -> {
                            runOnUiThread {
                                TxPopup.Builder(this).maxWidth(700)
                                    .dismissOnBackPressed(false)
                                    .dismissOnTouchOutside(false)
                                    .asConfirm(
                                        "??????",
                                        "${stringBuffer.toString()}???????????????????????????????????????",
                                        "",
                                        "??????",
                                        {
                                            endRecord(object :
                                                RoomActivity.RoomHttpCallBack {
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
                                        },
                                        null,
                                        true
                                    ).show()
                            }

                        }
                        "connectRoom" -> {
                            runOnUiThread {
                                page_error.visibility(false)
                                var finished = data.optBoolean("finished", false)
                                if (finished) { //??????type ???roomMessage ???????????? ???????????????????????????
                                    runOnUiThread {
                                        showEndPage()
                                        initEndRecyclerview(data.optJSONArray("finishedStep"))
                                        showLinkName("", "")
                                    }
                                } else {
                                    if (data.has("step")) {
                                        stepDataNode = data.getJSONObject("step")
                                        stepDataNodeType = stepDataNode!!.optString("baseType", "")
                                        handlePage(stepDataNodeType)
                                    } else {
                                        //
                                        //??????????????????
                                        if (tv_skip.text == "????????????") {
                                            pushMessage(JSONObject().apply {
                                                put("type", "recordExchange")
                                                put("serviceId", mServiceId)
                                                put("step", JSONObject().apply {
                                                    put("roomType", "recordExchange")
                                                    put(
                                                        "roomMessage",
                                                        jsonObject1!!.getJSONArray("process")
                                                    )
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
                        }
                        "exitRoom" -> {
                            //????????????
                            runOnUiThread {
                                LogUtils.i("exitRoom")
                                cancelAbsCredentialProvider()
                                destroylongTextTtsController()
                                hideVideoView()
                                layout_right.visibility = View.VISIBLE
                                page_error.visibility(true)
                                tv_error.text =
                                    "${stringBuffer.toString()}?????????????????????????????????"

                            }

                        }
                        else -> {
                        }
                    }

                } else if(mType == "syncNotice"){
                    //????????????????????????
                    TxLogUtils.i("?????????syncNotice")
                    runOnUiThread {
                        autoCheckBoolean = true
                        setFailType("", "")
                        quickEnterRoom(isSystem = true)
                    }
                }else {
                    var finished = data.optBoolean("finished", false)
                    if (finished) { //??????type ???roomMessage ???????????? ???????????????????????????
                        runOnUiThread {
                            showEndPage()
                            initEndRecyclerview(data.optJSONArray("finishedStep"))
                            showLinkName("", "")
                        }
                    } else {

                        var stepDataJson: JSONObject? = null
                        if (data.has("step")) {
                            stepDataJson = data.getJSONObject("step")
                            stepDataNode = data.getJSONObject("step")
                        } else {

                        }
                        if (null != stepDataNode) {
                            if (stepDataNode!!.has("processIndex")) {
                                processIndex = stepDataNode!!.optInt("processIndex")
                                stepIndex = stepDataNode!!.optInt("stepIndex")
                            }
                        }

                        val roomType = stepDataJson?.optString("roomType")
                        cancelTimer()
                        if (roomType!!.isNotEmpty()) { //??????????????????type??????
                            when (roomType) {
                                "textTTS-success", "productTTS-success" -> {
                                    runOnUiThread {
                                        setFailType("", "")
                                        autoCheckBoolean = true
                                        quickEnterRoom(isSystem = true)
                                    }
                                }
                                "textTTS-fail", "productTTS-fail" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        quickEnterRoom(isSystem = false)
                                    }
                                }

                                "soundOCR-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        setFailType("", "")
                                        isPassed = true
                                        cancelAbsCredentialProvider()
                                        strBuffer.delete(0, strBuffer.length)
                                        ll_page_voice_result.visibility(false)
                                        ll_page_voice_result_yes.visibility(true)
                                        ll_page_voice_result_no.visibility(false)
                                        page_asr_voice.visibility(false)
                                        val jsonObject = stepDataJson?.getJSONObject("data")
                                        val roomMessage = jsonObject?.optString("roomMessage")
                                        tv_user_content2.visibility(true)
                                        tv_user_content2.text = roomMessage

                                        startAutoNextStep(true)

                                    }
                                }
                                "soundOCR-fail" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        setFailType("????????????", "??????????????????")
                                        isPassed = false
                                        strBuffer.delete(0, strBuffer.length)
                                        ll_page_voice_result.visibility(true)
                                        ll_page_voice_result_no.visibility(true)
                                        ll_page_voice_result_yes.visibility(false)
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
                                                ll_page_voice_result_mark.visibility(checkBoolean)
                                            } else if ("releaseFailure" == key) {
                                                ll_page_voice_result_jump.text = buttonName
                                                ll_page_voice_result_jump.visibility(checkBoolean)
                                            } else if ("retry" == key) {
                                                ll_page_voice_result_retry.text = buttonName
                                                ll_page_voice_result_retry.visibility(checkBoolean)
                                            } else {
                                            }
                                        }

                                        page_asr_voice.visibility(false)
                                        ll_page_voice_result.visibility(true)
                                        val jsonObject = stepDataJson?.optJSONObject("data")
                                        val roomMessage = jsonObject?.getString("roomMessage")
                                        LogUtils.i("roomMessage-----$roomMessage")
                                        tv_user_content2.visibility(true)
                                        tv_user_content2.text = roomMessage
                                    }
                                }

                                "idComparison-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        //???????????????usedId
                                        isPassed = true
                                        val mUserId = stepDataJson?.optString("userId")
                                        val jsonObject = stepDataJson?.optJSONObject("data")
                                        val roomMessage = jsonObject?.getString("roomMessage")
                                        mTrtcrightvideolayoutmanager?.updateOcrStatus(
                                            mUserId,
                                            roomMessage,
                                            View.VISIBLE,
                                            "0"
                                        )
                                        fifterMemberList("", mUserId!!)
                                        mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId(
                                            mUserId,
                                            View.GONE
                                        )
                                    }

                                }

                                "idComparison-fail" -> {
                                    setFailType("????????????", "???????????????")
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        isPassed = false
                                        val mUserId = stepDataJson?.optString("userId")
                                        val jsonObject = stepDataJson?.optJSONObject("data")
                                        val roomMessage = jsonObject?.getString("roomMessage")
                                        mTrtcrightvideolayoutmanager?.updateOcrStatus(
                                            mUserId,
                                            "????????????",
                                            View.VISIBLE,
                                            "1"
                                        )
                                        mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId(
                                            mUserId,
                                            View.GONE
                                        )
                                        //????????????
                                        fifterMemberList(mUserId!!, "")
                                    }
                                }

                                "idComparison-collect-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserType(
                                            "agent",
                                            View.VISIBLE,
                                            true,
                                            "????????????",
                                            null
                                        )
                                        startAutoNextStep(true)
                                    }
                                }
                                "originTextRead-success" -> {
                                    runOnUiThread {
                                        startAutoNextStep(true)
                                    }
                                }
                                "idComparison-collect-fail" -> {
                                    runOnUiThread {
                                        val dataJson = stepDataJson!!.optJSONObject("data")
                                        val roomMessage = dataJson.optString("roomMessage")
                                        failTarget = stepDataJson?.optJSONArray("target")
                                        autoCheckBoolean = false

                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserType(
                                            "agent",
                                            View.VISIBLE,
                                            false,
                                            roomMessage,
                                            failureButtonJSONArray
                                        )
                                    }

                                }
                                "idComparison-retry" -> {
                                    runOnUiThread {
                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserType(
                                            "agent",
                                            View.GONE,
                                            true,
                                            "",
                                            null
                                        )
                                        val jsonArray = stepDataNode!!.optJSONArray("target")!!
                                        val title =
                                            stepDataNode!!.optJSONObject("data")
                                                .optJSONArray("textArray").optString(0)
                                        showidComparisonPage(jsonArray, true, title)
                                    }


                                }
                                "onScroll" -> { //?????????
                                    val stepData = data.optJSONObject("step")
                                    val jsonObject = stepData.optJSONObject("data")
                                    val string = jsonObject.getInt("roomMessage")
                                    runOnUiThread {
                                        ll_page11_content.smoothScrollBy(0, string)
                                    }

                                }
                                "identityOCR-retry" -> {
                                    runOnUiThread {
                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserType(
                                            "agent",
                                            View.GONE,
                                            true,
                                            "",
                                            null
                                        )
                                        val title =
                                            stepDataNode!!.optJSONObject("data")
                                                .optJSONArray("textArray").optString(0)
                                        runOnUiThread {
                                            showOCRPage(
                                                title,
                                                stepDataNode!!.optJSONArray("target")!!
                                            )

                                            tv_skip.visibility(false)

                                        }
                                    }

                                }
                                "identityOCR-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        val dataJson = stepDataJson!!.optJSONObject("data")
                                        val roomMessage = dataJson.optString("roomMessage")
                                        val mUserId = stepDataJson?.optString("userId")
                                        mTrtcrightvideolayoutmanager?.updateOcrLayoutByUserId(
                                            mUserId,
                                            View.GONE
                                        )
                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserId(
                                            mUserId,
                                            View.VISIBLE,
                                            true,
                                            roomMessage,
                                            null
                                        )
                                        startAutoNextStep(true)
                                    }
                                }
                                "identityOCR-fail" -> {
                                    setFailType("????????????", "OCR????????????")
                                    runOnUiThread {
                                        val dataJson = stepDataJson!!.optJSONObject("data")
                                        val roomMessage = dataJson.getString("roomMessage")
                                        val mUserId = stepDataJson?.optString("userId")
                                        failTarget = JSONArray()
                                        val successEntity =
                                            mTrtcrightvideolayoutmanager?.findEntity(mUserId)
                                        failTarget?.put(successEntity?.userType!!)
                                        autoCheckBoolean = false
                                        mTrtcrightvideolayoutmanager?.updateOcrLayoutByUserId(
                                            mUserId,
                                            View.GONE
                                        )

                                        mTrtcrightvideolayoutmanager?.updateSkipLayoutByUserId(
                                            mUserId,
                                            "",
                                            View.GONE,
                                            View.GONE
                                        )
                                        mTrtcrightvideolayoutmanager?.updateRetryLayoutByUserType(
                                            failTarget!!.getString(0),
                                            View.VISIBLE,
                                            false,
                                            roomMessage,
                                            failureButtonJSONArray
                                        )
                                    }
                                }
                                "waiting-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        quickEnterRoom(false)
                                    }

                                }
                                "textRead-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        quickEnterRoom(false)
                                    }
                                }

                                "recordExchange" -> { //???????????????????????????
                                    runOnUiThread {
                                        showRecordBefore()
                                        showNextStep("startRecord")

                                    }
                                }
                                "soundOCR-exception" -> { //asr????????????????????????????????????asr?????????????????????????????????
                                     isSoundOCRException  = true
                                    TxLogUtils.i("soundOCR-exception")
                                }

                                "onTouchEnd" -> {
                                    val stepData = data.optJSONObject("step")
                                    val dataJson = stepData.optJSONObject("data")
                                    val imageByte = dataJson.optString("roomMessage")
                                    LogUtils.i("imageByte--------------" + imageByte)
                                    var mDataList = ArrayList<PointBean>()
                                    mDataList = Gson().fromJson(
                                        imageByte,
                                        object : TypeToken<java.util.ArrayList<PointBean>>() {}.type
                                    )
                                    runOnUiThread {
                                        iv_page_sign_12.clear()
                                        iv_page_sign_12.drawPath(mDataList)
                                    }
                                }
                                "clearCanvas" -> {
                                    runOnUiThread {
                                        //??????
//                                    iv_page_12.invalidate()
//                                    iv_page_12.setImageBitmap(null)
                                        ll_page12_bottom.visibility(true)
                                        ll_page12_result.visibility(false)
                                        iv_page_sign_12.clear()
                                    }
                                }
                                "originSignFile-fail" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = false
                                        setFailType("????????????", "????????????????????????????????????")
                                        isPassed = false
                                        val stepData = data.optJSONObject("step")
                                        val dataJson = stepData.optJSONObject("data")
                                        val imageByte = dataJson.optString("roomMessage")
                                        page12_sign_result.text =
                                            "????????????${userName}   ?????????${imageByte}"
                                        ll_page12_bottom.visibility(false)
                                        ll_page12_result.visibility(true)
                                        ll_page12_result_fail.visibility(true)
                                        tv_page12_sign_nextstep.visibility(true)
                                        tv_page12_sign__retry.visibility(true)
                                        ll_page12_result_success.visibility(false)
                                        tv_page12_sign_nextstep.setOnClickListener(
                                            CheckDoubleClickListener {
                                                quickEnterRoom(isSystem = false)
                                            }
                                        )

                                        tv_page12_sign__retry.setOnClickListener(
                                            CheckDoubleClickListener {
                                                mCurrentMsg!!.optJSONObject("step")
                                                    .put("roomType", "originSignFile-retry")
                                                pushMessage(
                                                    mCurrentMsg!!,
                                                    object : RoomHttpCallBack {
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
                                    }


                                }
                                "originSignFile-retry" -> {
                                    runOnUiThread {
                                        ll_page12_bottom.visibility(true)
                                        ll_page12_result.visibility(false)
                                        ll_page12_result_success.visibility(false)
                                        ll_page12_result_fail.visibility(false)
                                        tv_page12_sign_nextstep.visibility(false)
                                        tv_page12_sign__retry.visibility(false)
                                        iv_page_sign_12.clear()
                                    }

                                }
                                "originSignFile-success" -> {
                                    runOnUiThread {
                                        autoCheckBoolean = true
                                        isPassed = true
                                        val stepData = data.optJSONObject("step")
                                        val dataJson = stepData.optJSONObject("data")
                                        val imageByte = dataJson.optString("roomMessage")
                                        page12_sign_result.text =
                                            "????????????${userName}   ?????????${imageByte}"
                                        ll_page12_bottom.visibility(false)
                                        ll_page12_result.visibility(true)
                                        ll_page12_result_success.visibility(true)
                                        ll_page12_result_fail.visibility(false)
                                        tv_page12_sign_nextstep.visibility(false)
                                        tv_page12_sign__retry.visibility(false)

                                        Handler().postDelayed({
                                            quickEnterRoom(isSystem = true)
                                        }, 2000)

                                    }
                                }
                                else -> {
                                }
                            }

                        } else {

                            mCurrentMsg = data
                            val mType = data.optString("type", "")

                            //textTTS ????????????
                            //soundOCR ????????????
                            //idComparison ????????????
                            //waiting ????????????
                            //signFile ????????????
                            //productTTS ??????????????????
                            //textRead ????????????
                            //{"serviceId":"612dfda621255524478161cc","type":"error","status":"exitRoom","user":[{"userId":"ojcFc46li3GgGOVVv2yJ3cb-HgaE","theWay":2}]}
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
                                    handlePage(stepDataNodeType)

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

    override fun agentOnline(data: JSONObject) {
        LogUtils.i("room-----agentOnline")
    }

    fun handlePage(stepDataNodeType: String) {
        when (stepDataNodeType) {
            "readNext" -> {//???????????????,??????????????????????????????
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)
                    val fillterData = fillterData(stepDataNode!!)
                    showReadNextPage("", fillterData)
                }


            }
            "textTTS", "productTTS" -> { //????????????,?????????????????????????????????
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)
                    val fillterData = fillterData(stepDataNode!!)
                    showTTSNext(fillterData)
                }

            }
            "soundOCR" -> { //?????????????????????
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)
                    targetJSONArray =
                        stepDataNode!!.optJSONArray("target")!!
                    failureButtonJSONArray =
                        stepDataNode!!.optJSONArray("failureButton")!!
                    keywordsRuleJSONObject =
                        stepDataNode!!.optJSONObject("keywordsRule")!!
                    val targetOb = targetJSONArray!!.optString(0)
                    val jsonArray = stepDataNode?.optJSONObject("data")
                        ?.optJSONArray("textArray")
                    agentASRPassword = jsonArray!!.optString(0)!!


                    //asr????????????

                    if ("agent" == targetOb) {
                        showUserASR(
                            "?????????????????????",
                            jsonArray!!.optString(0)!!,
                            true
                        )
                        startAudioRecognize()
                        startVoiceTimer()
                    } else if ("policyholder" == targetOb) {
                        showUserASR(
                            "?????????????????????",
                            jsonArray!!.optString(0)!!,
                            false
                        )
                        if (isSoundOCRException) {
                            startAudioRecognize()
                            startVoiceTimer(7000)
                        }
                    } else {
                        showUserASR(
                            "?????????????????????",
                            jsonArray!!.optString(0)!!,
                            false
                        )
                        if (isSoundOCRException) {
                            startAudioRecognize()
                            startVoiceTimer(5000)
                        }
                    }

                }


            }
            "waiting" -> { //?????????????????????
                val dataObject2 = stepDataNode?.optJSONObject("data")

                if (dataObject2!!.has("textArray")) {
                    val jsonArray =
                        dataObject2.optJSONArray("textArray")
                    val stringBuffer = StringBuffer("")
                    for (index in 0 until jsonArray.length()) {
                        val subStr = jsonArray.get(index) as String
                        stringBuffer.append("$subStr \n")
                    }
                    runOnUiThread {
                        tv_skip.visibility(false)
                        showWatingPage(
                            stringBuffer.toString(),
                            stepDataNode!!.optJSONArray("target")!!
                        )
                    }
                } else {
                    showToastMsg("??????textArray???????????????")
                }

            }
            "idComparison" -> { //????????????
                runOnUiThread {
                    val title =
                        stepDataNode!!.optJSONObject("data").optJSONArray("textArray").optString(0)

                    tv_skip.visibility(false)

                    failureButtonJSONArray =
                        stepDataNode?.optJSONArray("failureButton")!!
                    showidComparisonPage(
                        stepDataNode?.optJSONArray("target")!!,
                        false,
                        title
                    )

                }

            }
//                                        "signFile" -> { //???????????????
//                                            runOnUiThread {
//                                                layout_right.visibility = View.VISIBLE
//                                                tv_skip.visibility(false)
//                                                showSignFilePage( stepDataNode!!.optString("agentUrl")!!)
//                                            }
//
//                                        }
            "signFile", "textRead" -> { //???????????????????????????????????????????????????app??????????????????
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)

                    val dataObject2 =
                        stepDataNode?.optJSONObject("data")
                    if (dataObject2!!.has("textArray")) {
                        val jsonArray =
                            dataObject2.optJSONArray("textArray")
                        val title =
                            jsonArray.optString(0)
                        startTtsController(title, object : OfflineActivity.RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }

                        })
                        showTextReadPage(
                            jsonArray.optString(0) + "??????????????????????????????????????????",
                            stepDataNode!!.optJSONArray("target")!!
                                .optString(0),
                            stepDataNode!!.optString("agentUrl", ""),
                            stepDataNode!!.optBoolean("isPdf", false)
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
                        stepDataNode?.optJSONArray("failureButton")!!
                    runOnUiThread {
                        showOCRPage(
                            stringBuffer.toString(),
                            stepDataNode?.optJSONArray("target")!!
                        )

                        tv_skip.visibility(false)

                    }
                } else {
                    showToastMsg("??????textArray???????????????")
                }
            }
            "originSignFile" -> {
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)

                    val dataObject2 =
                        stepDataNode?.optJSONObject("data")
                    if (dataObject2!!.has("textArray")) {
                        val jsonArray =
                            dataObject2.optJSONArray("textArray")
                        val title =
                            jsonArray.optString(0)
                        startTtsController(title, object : OfflineActivity.RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }

                        })
                        showSignFilePage()
                    } else {
                        showToastMsg("??????textArray???????????????")
                    }
                }
            }
            "originTextRead" -> {
                runOnUiThread {
                    layout_right.visibility = View.VISIBLE
                    tv_skip.visibility(false)
                    val dataObject2 =
                        stepDataNode?.optJSONObject("data")
                    if (dataObject2!!.has("textArray")) {
                        val jsonArray =
                            dataObject2.optJSONArray("textArray")
                        val title =
                            jsonArray.optString(0)
                        startTtsController(title, object : OfflineActivity.RoomHttpCallBack {
                            override fun onSuccess(json: String?) {

                            }

                            override fun onFail(err: String?, code: Int) {

                            }

                        })
                        showOriginTextReadPage(
                            jsonArray.optString(0) + "??????????????????????????????????????????",
                            stepDataNode!!.optJSONArray("target")!!.optString(0)
                        )
                        val fileUrl = stepDataNode!!.optString("fileUrl", "")
                        if (fileUrl.isNotEmpty()) {
                            TxGlide.with(this)
                                .load(fileUrl)
                                .into(iv_textRead)
                        }

                    } else {
                        showToastMsg("??????textArray???????????????")
                    }


                }
            }
            else -> {
            }
        }
    }

    private fun showOriginTextReadPage(promtStr: String, obj: String) {
        hideView()
        hideVideoView()
        tv_origin_textread_skip.visibility = if ("agent".equals(obj)) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        page_basetype_origintextread.visibility(true)
        LogUtils.i("promtStr------$promtStr")
        tv_origin_prompt1.text = promtStr
        ll_page11_content.isEnabled = false
        ll_origin_keepout.isClickable = true
        ll_origin_keepout.isFocusable = true
        tv_origin_textread_skip.setOnClickListener(
            CheckDoubleClickListener {
                autoCheckBoolean = true
                quickEnterRoom(false)
            }
        )
    }

    fun showSignFilePage() {
        hideView()
        hideVideoView()
        ll_page12_bottom.visibility(true)
        ll_page12_result.visibility(false)
        page_basetype_originsign.visibility(true)
        rl_sign.visibility(true)
        iv_page_sign_12.clear()
    }

    var credentialProvider: LocalCredentialProvider? = null
    var aaiClient: AAIClient? = null
    var audioRecognizeRequest: AudioRecognizeRequest? = null
    var audioRecognizeConfiguration: AudioRecognizeConfiguration? = null
    private fun initAbsCredentialProvider() {

        credentialProvider = LocalCredentialProvider(mSecretKey)

        ClientConfiguration.setMaxAudioRecognizeConcurrentNumber(2) // ???????????????????????????????????????

        ClientConfiguration.setMaxRecognizeSliceConcurrentNumber(10) // ????????????????????????????????????


    }


    var strBuffer = StringBuffer("")
    var mOldSegmentStr = ""
    fun startAudioRecognize() {
        val audioRecognizeResultListener: AudioRecognizeResultListener =
            object : AudioRecognizeResultListener {
                override fun onSuccess(request: AudioRecognizeRequest?, result: String?) {
                    LogUtils.i("audioRecognizeResultListener","result-------$result")

                }

                override fun onFailure(
                    request: AudioRecognizeRequest?,
                    clientException: ClientException?,
                    serverException: ServerException?
                ) {
                    LogUtils.i("audioRecognizeResultListener","onFailure-------$clientException------$serverException")
                }

                override fun onSliceSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult?,
                    order: Int
                ) {
                    LogUtils.i("audioRecognizeResultListener","onSliceSuccess-------${result?.text}")
//                    tv_user_content2.visibility(true)
//                    tv_user_content2.text = result?.text
                }

                override fun onSegmentSuccess(
                    request: AudioRecognizeRequest?,
                    result: AudioRecognizeResult?,
                    order: Int
                ) {
                    LogUtils.i("audioRecognizeResultListener","onSegmentSuccess-------${result?.text}")
                    val text = result?.text
                    if (text?.isNotEmpty()!!) {
                        strBuffer.append(result?.text)

                        mOldSegmentStr = strBuffer.toString()
                        val replaceTV1 = replaceTV(result?.text!!)

                        LogUtils.i("replaceTV------$replaceTV1")

                        //??????????????????
                        val filterASRPassword = filterASRPassword(replaceTV1)


                        strBuffer.delete(0, strBuffer.length)
                        cancelAbsCredentialProvider()
                        voiceTimer1?.cancel()

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
                                put("target", replaceTV1)
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
                .audioFlowSilenceTimeOut(2000) // ??????????????????????????????
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

                LogUtils.i("audioRecognizeResultListener","audioRecognizeRequest-----${audioRecognizeRequest?.requestId}")
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
        LogUtils.i("audioRecognizeResultListener","cancelAbsCredentialProvider")

        val requestId = audioRecognizeRequest?.requestId
        // 2?????????cancel??????

        Thread(Runnable {
            if (aaiClient != null) {
                //???????????????????????????????????????
                aaiClient!!.stopAudioRecognize(requestId!!)
            }
        }).start()
    }

    private fun filterASRPassword(word: String): Boolean {
        if (word.isEmpty()) {
            return false
        }
        val notContainArray = keywordsRuleJSONObject!!.optJSONArray("notContain")
        var contain = true
        for (index in 0 until notContainArray.length()) {
            val notContainStr = notContainArray.optString(index)
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
            val containsArray = keywordsRuleJSONObject!!.optJSONArray("contains")
            val stringBuilder = StringBuilder("")
            for (index in 0 until containsArray.length()) {
                val containsStr = containsArray.optJSONObject(index)
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

    public fun numJewelsInStones(newWord: String, oldWord: String): Int {
        if (newWord.length != oldWord.length) return 12
        LogUtils.i("newWord", newWord)
        LogUtils.i("oldWord", oldWord)
        var count = 0;
        if (newWord.length <= oldWord.length) {
            for (index in 0 until newWord.length) {

                if (newWord[index] != oldWord[index]) {
                    count++;
                }
            }
        } else {
            count = 1
        }




        return count;
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


    private var mCheckLocal = false
    private var checkPhotoInVideoTimer: CountDownTimer? = null

    var isShowedAgentHaveFace = false
    var mAgentBytes: ByteArray? = null

    private var startCheckPhotoInVideoTimer: CountDownTimer? = null

    //??????????????????
    private fun stopCheckPhotoInVideo() {
        startCheckPhotoInVideoTimer?.cancel()
    }

    private fun startCheckPhotoInVideo() {

        if (null == startCheckPhotoInVideoTimer) {
            startCheckPhotoInVideoTimer = object : CountDownTimer(600000, 3000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) { p0 ->
                        TxLogUtils.i("checkPhotoInVideo")
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
                                            //????????????
                                            var haveFace = !"0".equals(json)
                                            var qualifiedFace = "1".equals(json)
                                            runOnUiThread {
                                                mTrtcrightvideolayoutmanager?.updateToastStrByType(
                                                    "agent",
                                                    "???????????????$json ???",
                                                    if (qualifiedFace) {
                                                        ContextCompat.getColor(
                                                            this@RoomActivity,
                                                            R.color.tx_txcolor_40D4A1
                                                        )
                                                    } else {
                                                        ContextCompat.getColor(
                                                            this@RoomActivity,
                                                            R.color.tx_txred
                                                        )
                                                    }
                                                )
                                            }
                                            pushMessage(JSONObject().apply {
                                                put("serviceId", mServiceId)
                                                put("type", "faceDetection")
                                                put("step", JSONObject().apply {
                                                    put("roomType", "faceDetection")
                                                    put("haveFace", haveFace)
                                                    put("userId", mUserId)
                                                    put("number", json?.toInt())

                                                })
                                            }, object : RoomHttpCallBack {
                                                override fun onSuccess(json: String?) {

                                                }

                                                override fun onFail(err: String?, code: Int) {

                                                }
                                            })

                                        }

                                        override fun onFail(err: String?, code: Int) {

                                        }
                                    }

                                )

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

    fun hideVideoView() {
        mTrtcrightvideolayoutmanager?.hideAllStateView()
        mTrtcrightvideolayoutmanager?.makeFullVideoView()
    }

    fun hideView() {
        voiceTimer1?.cancel()
        voiceTimer1 = null
        mSuccessCacheArray.clear()
        mFailCacheArray.clear()

        ll_showLink.visibility(false)
        page_checkenv.visibility(false)
        page_readnext.visibility(false)
        page_tts.visibility(false)
        page_readnext_title.visibility(false)
        page_basetype_textread.visibility(false)
        page_basetype_origintextread.visibility(false)
        page_basetype_originsign.visibility(false)


        ll_page_voice.visibility(false)
        ll_envpreview.visibility(false)

        ll_page_voice_result.visibility(false)
        ll_pageend.visibility(false)
        tv_text_continue.visibility(false)
        page_error.visibility(false)
        resetWebviewUrl()
    }

    override fun onConnect() {
        super.onConnect()
        LogUtils.i("rooms-onConnect")

        SystemSocket.instance?.setMSG(TXManagerImpl.instance?.getLoginName()!!, mServiceId)

    }

    override fun event_reconnect_attempt() {
        super.event_reconnect_attempt()
        runOnUiThread {
            LogUtils.i("exitRoom")
            if (page_error.visibility == View.GONE) {
                cancelAbsCredentialProvider()
                destroylongTextTtsController()
                hideVideoView()
                layout_right.visibility = View.VISIBLE
                page_error.visibility(true)
                tv_error.text =
                    "??????????????????????????????????????????"
            }

        }
    }

    override fun event_reconnect() {
        super.event_reconnect()
        runOnUiThread {

        }

    }

    override fun onDisconnect() {
        super.onDisconnect()

    }


    override fun onBackPressed() {
        end()
    }

    private fun takePhoto(param: PhotoHttpCallBack) {
        LogUtils.i("mUserId---$mUserId")
        mTRTCCloud?.snapshotVideo(null, TRTC_VIDEO_STREAM_TYPE_BIG) {
            val saveBitmap1 = SystemCommon.getInstance().byteToBitmap(it)
            if (saveBitmap1 != null) {
                val encode = Base64.encode(saveBitmap1, Base64.DEFAULT)
                val jsonObject = JSONObject(mRoomInfo)
                val agentName = jsonObject.optString("agentName", "")
                val bulider = StringBuilder("data:image/png;base64,")
                bulider.append(String(encode))


                val replace = bulider.toString().replace("\n", "")
                val uploadShotPic = UploadShotPic()
                uploadShotPic.apply {
                    serviceId = mServiceId
                    facePhoto = replace
                    idCardNum = agentID
                    name = agentName
                }
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


    }

    private fun takePhotoOcr(param: PhotoHttpCallBack, role: String) {
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

    override fun onClickItemFill(userId: String?, streamType: Int, enableFill: Boolean) {
        LogUtils.i("onClickItemFill--userId$userId----enableFill$enableFill")

//        mLocalPreviewView?.makeFullVideoView(1)
//        if (!enableFill) {
//            relative_7.visibility = View.VISIBLE
//        }else{
//            relative_7.visibility = View.INVISIBLE
//        }

    }

    override fun onClickItemMuteVideo(userId: String?, streamType: Int, isMute: Boolean) {
    }

    override fun onClickItemMuteAudio(userId: String?, isMute: Boolean) {
    }

    override fun onClickItemRetry(userId: String?, type: String?) {
        when (type) {
            "0" -> {
                //??????
                setFailType("", "")
                quickEnterRoom(isSystem = false)
            }
            "1" -> {
                //??????
                setFailType("????????????", "????????????")
                quickEnterRoom(isSystem = false)
            }
            "2" -> {
                //??????
                if ("idComparison".equals(stepDataNodeType)) {
                    mCurrentMsg!!.optJSONObject("step").remove("target")
                    mCurrentMsg!!.optJSONObject("step").put("target", failTarget)
                    mCurrentMsg!!.optJSONObject("step").put("roomType", "idComparison-retry")
                    pushMessage(mCurrentMsg!!, object : RoomHttpCallBack {
                        override fun onSuccess(json: String?) {

                        }

                        override fun onFail(err: String?, code: Int) {

                        }
                    })
                } else {
                    mCurrentMsg!!.optJSONObject("step").remove("target")
                    mCurrentMsg!!.optJSONObject("step").put("target", failTarget)
                    mCurrentMsg!!.optJSONObject("step").put("roomType", "identityOCR-retry")
                    pushMessage(mCurrentMsg!!, object : RoomHttpCallBack {
                        override fun onSuccess(json: String?) {

                        }

                        override fun onFail(err: String?, code: Int) {

                        }
                    })

                }


            }
            else -> {
            }
        }

    }


    override fun onClickItemMuteInSpeakerAudio(userId: String?, isMute: Boolean) {
        for (index in 0 until mWatingArray!!.length()) {
            val tagrtOb = mWatingArray!!.getString(index)
            if (tagrtOb.equals("agent")) {
                //?????????????????????

                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "agent",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatus(mUserId, "?????????...", View.GONE, "123")
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId("agent", View.GONE)
            } else if (tagrtOb.equals("policyholder")) {
                //?????????????????????

                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "policyholder",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "policyholder",
                    "?????????...",
                    View.GONE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType(
                    "policyholder",
                    View.GONE
                )
            } else {
                //?????????????????????

                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "insured",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "insured",
                    "?????????...",
                    View.GONE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId(
                    "insured",
                    View.GONE
                )
            }
        }
        layout_right.visibility = View.VISIBLE
        Handler().postDelayed({

            autoCheckBoolean = true
            setFailType("", "")
            quickEnterRoom(isSystem = false)
        }, 200)

    }


    fun resetVideoLayout() {
        for (index in 0 until mWatingArray!!.length()) {
            val tagrtOb = mWatingArray!!.getString(index)
            if (tagrtOb.equals("agent")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeFullVideoView(1)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "agent",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatus(mUserId, "?????????...", View.GONE, "123")
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByUserId("agent", View.GONE)
            } else if (tagrtOb.equals("policyholder")) {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeFullVideoView(2)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "policyholder",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "policyholder",
                    "?????????...",
                    View.GONE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType(
                    "policyholder",
                    View.GONE
                )
            } else {
                //?????????????????????
//                mTrtcrightvideolayoutmanager?.makeFullVideoView(3)
                mTrtcrightvideolayoutmanager?.updateSkipLayout(
                    "insured",
                    "title",
                    View.GONE,
                    View.GONE
                )
                mTrtcrightvideolayoutmanager?.updateOcrStatusByType(
                    "insured",
                    "?????????...",
                    View.GONE,
                    "123"
                )
                mTrtcrightvideolayoutmanager?.updateHollowOutViewLayoutByType("insured", View.GONE)
            }
        }

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
                TxSPUtils.get(this@RoomActivity, TXManagerImpl.instance!!.getAgentId(), 2) as Int
            val values = VoiceSpeed.values()
            val voiceSpeed = values[i]
            setVoiceSpeed(voiceSpeed.num)
            setVoiceType(VoiceType.VOICE_TYPE_AFFNITY_FEMALE.num)
            setVoiceLanguage(VoiceLanguage.VOICE_LANGUAGE_CHINESE.num)
            setProjectId(0)
        }
    }


    fun destroylongTextTtsController() {
        longTextTtsController?.stop()
    }


    fun startTtsController(ttsStr: String, callBack: OfflineActivity.RoomHttpCallBack) {
        destroylongTextTtsController()
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
            LogUtils.i("${e.message}")
            callBack.onFail(e.message, 0)
        }

    }

    private var mTtsExceHandler: TtsExceptionHandler = TtsExceptionHandler {

    }

}

fun View.visibility(visibility: Boolean) {
    this.visibility = if (visibility) {
        View.VISIBLE
    } else {
        GONE
    }
}