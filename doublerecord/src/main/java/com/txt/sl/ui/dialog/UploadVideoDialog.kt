package com.txt.sl.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.common.widget.dialog.core.CenterPopupView
import com.common.widget.dialog.util.XPopupUtils
import com.txt.sl.R
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.MainThreadUtil
import com.txt.sl.utils.ToastUtils
import org.json.JSONObject
import java.text.DecimalFormat

/**
 * author ：Justin
 * time ：2021/7/12.
 * des ： 上传视频框
 */
class UploadVideoDialog(context: Context) : CenterPopupView(context) {
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
        fun onVideoUpload(isFinish:Boolean)
    }

    var mOnItemClickListener: OnConfirmClickListener? = null
    fun setOnConfirmClickListener(onItemClickListener: OnConfirmClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onShow() {
        super.onShow()
        upload(mFlowId!!)
    }

    var mFlowId:String?=null

    public fun setFlowId(flowId:String){
        mFlowId= flowId
    }

    public fun upload(screenRecordStr:String) {

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
//                        updateProgress()
            SystemHttpRequest.getInstance().uploadLogFile(pathFile, preTime
                    , serviceId, { size, time ->
                val byteToMB = byteToMB(size)
                tvVideosize?.text = "$byteToMB M"
                LogUtils.i("time$time")
                val min = time / 1000 / 60.0
                val df = DecimalFormat("#.00")
                var minSize = df.format(min)
                tvVideotime?.text = "$minSize 分钟"
            }, object : SystemHttpRequest.onRequestCallBack {
                override fun onSuccess() {
                    MainThreadUtil.run(Runnable {
                        ToastUtils.showShort("上传成功！！！")
                        mOnItemClickListener?.onVideoUpload(true)
                        dismiss()
                    })

                }

                override fun onFail(msg: String?) {
                    MainThreadUtil.run(Runnable {
                        ToastUtils.showShort(msg)
                        mOnItemClickListener?.onVideoUpload(false)
                        dismiss()
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
            ToastUtils.showShort("没有找到对应的录屏！！！")
        }


    }


    var mProgressBar: ProgressBar? = null
    var mTvPercent: TextView? = null
    var mTvSize: TextView? = null
    var tvVideosize: TextView? = null
    var tvVideotime: TextView? = null

    // 进度对话框
    open fun initProgressDialog() {

        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mProgressBar?.setMax(100)
        mTvPercent = findViewById<View>(R.id.tvPercent) as TextView
        mTvSize = findViewById<View>(R.id.cancel) as TextView
        tvVideosize = findViewById<View>(R.id.tv_videosize) as TextView
        tvVideotime = findViewById<View>(R.id.tv_videotime) as TextView

        mTvSize?.setOnClickListener {
            SystemHttpRequest.getInstance().cancelClient()
            dismiss()
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

    }

}