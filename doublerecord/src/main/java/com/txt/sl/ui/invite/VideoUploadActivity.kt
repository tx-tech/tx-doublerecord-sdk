package com.txt.sl.ui.invite

import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.text.TextUtils
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.tencent.cos.xml.transfer.TransferState
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_video_upload.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.*


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
        TxPopup.Builder(this)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false)
            .asConfirm(
            "退出",
            "确认退出双录视频上传页面？",
            "取消",
            "是的",
            { finish() },
            null,
            false
        ).show()
    }

    fun showExitDialog() {
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
    }

    var mFlowId = ""

    // 进度对话框
    open fun initProgressDialog() {

        progressBar?.max = 100
        mFlowId = intent.extras?.getString(VideoUploadActivity.flowIdStr)!!
        val screenRecordStr = TxSPUtils.get(this, mFlowId, "") as String
        if (screenRecordStr.isEmpty()) {
            showExitDialog()
        } else {
            SystemHttpRequest.getInstance()
                .getFlowDetailsByTaskid(mFlowId, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        runOnUiThread {
                            upload(mFlowId, screenRecordStr)
                        }

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
            if (tv_gotovideo.text == "暂停上传") {

                val pauseSafely = VideoUploadImp.instance.cosxmlUploadTask?.pauseSafely()
                if (pauseSafely!!) {
                    tv_gotovideo.text = "继续上传"
                } else {
                    showToastMsg("暂停失败")
                }

            } else if (tv_gotovideo.text == "继续上传") {
                VideoUploadImp.instance.cosxmlUploadTask?.resume()
                tv_gotovideo.text = "暂停上传"
            }
        }


    }

    fun upload(mFlowId: String?, screenRecordStr: String) {
        if (!TextUtils.isEmpty(screenRecordStr)) {
            val jsonObject = JSONObject(screenRecordStr)
            LogUtils.i("jsonObject---$jsonObject")
            val preTime = jsonObject.getString("preTime")
            val serviceId = jsonObject.getString("serviceId")
            val pathFile = jsonObject.getString("path")
            val uploadId = jsonObject.optString("uploadId")
            SystemHttpRequest.getInstance().getVideoSizeAndDuration(pathFile,
                SystemHttpRequest.onFileCallBack {size,time->
                    val byteToMB = byteToMB(size)
                    tv_videosize?.text = "视频大小：$byteToMB M"
                    LogUtils.i("time$time")
                    val min = time / 1000 / 60.0
                    var minSize = String.format("%.2f", min)
                    tv_videotime?.text = "视频时长：$minSize 分钟"
                })
            LogUtils.i("开始上传")
            tv_gotovideo.text = "暂停上传"
            VideoUploadImp.instance.upload(
                this, uploadId, preTime, serviceId, pathFile, object : OnUploadListener {
                    override fun onProgress(complete: Long, target: Long) {
                        runOnUiThread {
                            updateProgress(target, complete)
                        }
                    }

                    override fun onStateChanged(
                        var1: TransferState,
                        uploadId: String
                    ) {
                        //如果暂停，就重新记录的 uploadI开始传
                        jsonObject.put("uploadId", uploadId)
                        TxSPUtils.put(
                            this@VideoUploadActivity,
                            mFlowId,
                            jsonObject.toString()
                        )
                    }

                    override fun onFail() {

                    }

                    override fun onSuccess() {
                        TxSPUtils.remove(
                            this@VideoUploadActivity,
                            mFlowId
                        )
                        finish()
                    }

                })


        } else {
            showExitDialog()
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