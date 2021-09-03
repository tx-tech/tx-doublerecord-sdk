package com.txt.sl.ui.invite

import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.text.TextUtils
import android.view.View
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_video_upload.*
import org.json.JSONObject
import java.text.DecimalFormat

class VideoUploadActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.tx_activity_video_upload

    companion object {
        var flowIdStr = "flowId"
        fun newActivity(context: Context, flowId: String) {
            val intent = Intent(context, VideoUploadActivity::class.java)
            intent.putExtra(flowIdStr, flowId)
            context.startActivity(intent)
        }

    }

    override fun initView() {
        super.initView()
        title = "双录视频上传"
        initProgressDialog()

    }

    override fun onBackPressed() {
        showDialog()
    }

    public fun showDialog() {
        TxPopup.Builder(this).asConfirm(
            "退出",
            "确认退出双录视频上传页面？",
            "取消",
            "是的",
            { finish() },
            null,
            false
        ).show()
    }

    public fun upload(flowId: String, screenRecordStr: String) {

        //上传
        //flowId
        LogUtils.i("screenRecordStr---$screenRecordStr")
        if (!TextUtils.isEmpty(screenRecordStr)) {
            val jsonObject = JSONObject(screenRecordStr)
            LogUtils.i("jsonObject---$jsonObject")
            val preTime = jsonObject.getString("preTime")
            val serviceId = jsonObject.getString("serviceId")
            val pathFile = jsonObject.getString("path")

//                        val preTime = "1"
//                        val serviceId = "5edcc228dc18f7001e07be90"
//                        val pathFile = PathUtils.getExternalStoragePath() + "/txsl/"+"txsl_1591525938353.mp4"

            LogUtils.i("开始上传")
            tv_gotovideo.visibility = View.GONE
            SystemHttpRequest.getInstance()
                .uploadLogFile(pathFile, preTime, serviceId, { size, time ->
                    val byteToMB = byteToMB(size)
                    tv_videosize?.text = "$byteToMB M"
                    LogUtils.i("time$time")
                    val min = time / 1000 / 60.0
                    var minSize = String.format("%.2f", min)
                    tv_videotime?.text = "$minSize 分钟"
                }, object : SystemHttpRequest.onRequestCallBack {
                    override fun onSuccess() {
                        MainThreadUtil.run(Runnable {
                            showToastMsg("上传成功")
                            finish()
                        })

                    }

                    override fun onFail(msg: String?) {
                        MainThreadUtil.run(Runnable {
                            showToastMsg("上传失败")
                            tv_gotovideo.visibility = View.VISIBLE
                            tv_gotovideo.text = "开始上传"
                        })

                        LogUtils.i("uploadLogFile$msg")
                    }

                }, { totalLength, currentLength ->


                    MainThreadUtil.run(Runnable {
                        val progress = (currentLength * 100.0 / totalLength)
                        LogUtils.i("progress---$progress")
                        updateProgress(totalLength, currentLength)
                    })

                })


        } else {

        }


    }

    var mFlowId = ""

    // 进度对话框
    open fun initProgressDialog() {

        progressBar?.max = 100
        mFlowId = intent.extras.getString(VideoUploadActivity.flowIdStr)
        val screenRecordStr = TxSPUtils.get(this, mFlowId, "") as String
        if (screenRecordStr.isEmpty()) {
            TxPopup.Builder(this)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .asConfirm(
                "退出",
                "视频文件不存在，请从“我的双录”进入找到此单重新录制",
                "",
                "确认",
                { finish() },
                null,
                true
            ).show()
        } else {
            SystemHttpRequest.getInstance()
                .getFlowDetailsByTaskid(mFlowId, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        upload(mFlowId, screenRecordStr)
                    }

                    override fun onFail(err: String?, code: Int) {
                        runOnUiThread {
                            TxPopup.Builder(this@VideoUploadActivity)
                                .dismissOnBackPressed(false)
                                .dismissOnTouchOutside(false)
                                .asConfirm(
                                    "退出",
                                    "${err}",
                                    "",
                                    "确认",
                                    { finish() },
                                    null,
                                    true
                                ).show()
                        }
                    }
                })

        }

        tv_invite_wx.text = "$mFlowId"
        tv_gotovideo.setOnClickListener {
            upload(mFlowId, screenRecordStr)
        }

    }

    private fun byteToMB(size: Long): String? {
        val fileSize = size / 1024.0 / 1024.0
        val df = DecimalFormat("#.00")
        var mbSize = df.format(fileSize)
        if (mbSize.indexOf(".") == 0) {
            mbSize = "0$mbSize"
        }
        return mbSize
    }

    fun updateProgress(totalLength: Long, currentLength: Long) {

        // 计算下载百分比
        val progress = (currentLength * 100.0 / totalLength)

        var progressInt = progress.toInt()

        progressBar?.progress = progressInt

        tvPercent?.text = "$progressInt %"
        val netSpeed = getNetSpeed(TXSdk.getInstance().application.applicationInfo.uid)
        LogUtils.i("netSpeed:" + netSpeed)
    }

    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0

    fun getNetSpeed(uid: Int): String {
        val nowTotalRxBytes = getTotalRxBytes(uid)
        val nowTimeStamp = System.currentTimeMillis()
        val speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / if (nowTimeStamp == lastTimeStamp)
            nowTimeStamp
        else
            nowTimeStamp - lastTimeStamp// 毫秒转换
        lastTimeStamp = nowTimeStamp
        lastTotalRxBytes = nowTotalRxBytes
        return speed.toString() + " kb/s"
    }

    fun getTotalRxBytes(uid: Int): Long {
        return if (TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED.toLong()) 0 else TrafficStats.getTotalRxBytes() / 1024//转为KB
    }

    override fun onDestroy() {
        if (null != TXSdk.getInstance().onTxPageListener) {
            TXSdk.getInstance().onTxPageListener.onSuccess(mFlowId!!)
        }
        super.onDestroy()
    }

}