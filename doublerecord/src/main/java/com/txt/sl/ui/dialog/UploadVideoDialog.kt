package com.txt.sl.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.net.TrafficStats
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.common.widget.dialog.core.CenterPopupView
import com.common.widget.dialog.util.XPopupUtils
import com.tencent.cos.xml.transfer.TransferState
import com.tencent.qcloud.core.task.QCloudTask
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.invite.OnUploadListener
import com.txt.sl.ui.invite.VideoUploadImp
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.MainThreadUtil
import com.txt.sl.utils.ToastUtils
import com.txt.sl.utils.TxSPUtils
import kotlinx.android.synthetic.main.tx_activity_video_upload.*
import org.json.JSONObject
import java.text.DecimalFormat

/**
 * author ：Justin
 * time ：2021/7/12.
 * des ： 上传视频框
 */
class UploadVideoDialog(
    context: Context,
    var isShowBt: Boolean = false
) : CenterPopupView(context) {
    override fun onCreate() {
        super.onCreate()
        initProgressDialog()
    }

    override fun getImplLayoutId(): Int {
        return R.layout.tx_dialog_progress
    }

    override fun getMaxHeight(): Int {      //最大高度
        return (XPopupUtils.getWindowHeight(context) * .5f).toInt()
    }

    interface OnConfirmClickListener {
        /**
         * @param ip
         * @param port
         */
        fun onVideoUpload(isFinish: Boolean)
    }

    var mOnItemClickListener: OnConfirmClickListener? = null
    fun setOnConfirmClickListener(onItemClickListener: OnConfirmClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onShow() {
        super.onShow()

        upload(mScreenRecordStr!!)
    }

    var mScreenRecordStr: String? = null

    public fun setScreenRecordStr(screenRecordStr: String) {
        mScreenRecordStr = screenRecordStr
    }

    public fun upload(screenRecordStr: String) {

        //上传
        //flowId
        LogUtils.i("screenRecordStr---$screenRecordStr")
        if (!TextUtils.isEmpty(screenRecordStr)) {
            val jsonObject = JSONObject(screenRecordStr)
            LogUtils.i("jsonObject---$jsonObject")
            val flowId = jsonObject.getString("flowId")
            val preTime = jsonObject.getString("preTime")
            val serviceId = jsonObject.getString("serviceId")
            val pathFile = jsonObject.getString("path")
            val uploadId = jsonObject.optString("uploadId")
            if (isShowBt) {
                tvGotovideo?.text = "暂停上传"
            }
            SystemHttpRequest.getInstance().getVideoSizeAndDuration(pathFile
            ) { size, time ->
                val byteToMB = byteToMB(size)
                tvVideosize?.text = "视频大小：$byteToMB M"
                LogUtils.i("time$time")
                val min = time / 1000 / 60.0
                var minSize = String.format("%.2f", min)
                tvVideotime?.text = "视频时长：$minSize 分钟"
            }
            LogUtils.i("开始上传"+uploadId)
            VideoUploadImp.instance.upload(
                context, uploadId, preTime, serviceId, pathFile, object : OnUploadListener {
                    override fun onProgress(complete: Long, target: Long) {
                        MainThreadUtil.run(Runnable {
                            updateProgress(target, complete)
                        })

                    }

                    override fun onStateChanged(
                        var1: TransferState,
                        uploadId: String
                    ) {
                        //如果暂停，就重新记录的 uploadI开始传 PAUSED
                        jsonObject.put("uploadId", uploadId)
                        TxSPUtils.put(
                            context,
                            flowId,
                            jsonObject.toString()
                        )
                    }

                    override fun onFail() {
                        mOnItemClickListener?.onVideoUpload(isFinish = false)
                    }

                    override fun onSuccess() {
                        mOnItemClickListener?.onVideoUpload(isFinish = true)
                        TxSPUtils.remove(
                            context,
                            flowId
                        )
                        MainThreadUtil.run(Runnable {
                            dismiss()
                        })
                    }

                })

        } else {
            ToastUtils.showShort("没有找到对应的录屏！！！")
        }


    }


    var mProgressBar: ProgressBar? = null
    var mTvPercent: TextView? = null
    var mTvSize: TextView? = null
    var tvVideosize: TextView? = null
    var tvVideotime: TextView? = null
    var tvGotovideo: TextView? = null

    // 进度对话框
    open fun initProgressDialog() {

        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mProgressBar?.max = 100
        mTvPercent = findViewById<View>(R.id.tvPercent) as TextView
        mTvSize = findViewById<View>(R.id.cancel) as TextView
        tvVideosize = findViewById<View>(R.id.tv_videosize) as TextView
        tvVideotime = findViewById<View>(R.id.tv_videotime) as TextView
        tvGotovideo = findViewById<View>(R.id.tv_gotovideo) as TextView

        mTvSize?.setOnClickListener {
            SystemHttpRequest.getInstance().cancelClient()
            dismiss()
        }
        tvGotovideo?.visibility = if (isShowBt) {
            VISIBLE
        } else {
            GONE
        }
        tvGotovideo?.setOnClickListener {
            if (tvGotovideo?.text == "暂停上传") {

                val pauseSafely = VideoUploadImp.instance.cosxmlUploadTask?.pauseSafely()
                if (pauseSafely!!) {
                    tvGotovideo?.text = "继续上传"
                } else {
                    ToastUtils.showShort("暂停失败！！！")
                }

            } else if (tvGotovideo?.text == "继续上传") {
                tvGotovideo?.text = "暂停上传"
                VideoUploadImp.instance.cosxmlUploadTask?.resume()
            }
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

        mProgressBar?.progress = progressInt

        mTvPercent?.text = "$progressInt %"
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
}