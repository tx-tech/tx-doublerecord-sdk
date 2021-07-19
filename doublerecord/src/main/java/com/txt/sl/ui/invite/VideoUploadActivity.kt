package com.txt.sl.ui.invite

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.txt.sl.R
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_video_upload.*
import org.json.JSONObject
import java.text.DecimalFormat

class VideoUploadActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.tx_activity_video_upload

    companion object {
        var flowIdStr = "flowId"
        fun newActivity(context: Context, flowId:String) {
            val intent = Intent(context, VideoUploadActivity::class.java)
            intent.putExtra(flowIdStr,flowId)
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

    public fun showDialog(){
        TxPopup.Builder(this).asConfirm("退出",
                "确认退出双录视频上传页面？",
                "取消",
                "是的",
                { finish() },
                null,
                false).show()
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
            tv_gotovideo.text = "取消上传"
            SystemHttpRequest.getInstance().uploadLogFile(pathFile, preTime
                    , serviceId, { size, time ->
                val byteToMB = byteToMB(size)
                tv_videosize?.text = "$byteToMB M"
                LogUtils.i("time$time")
                val min = time / 1000 / 60.0
                val df = DecimalFormat("#.00")
                var minSize = df.format(min)
                tv_videotime?.text = "$minSize 分钟"
            }, object : SystemHttpRequest.onRequestCallBack {
                override fun onSuccess() {
                    MainThreadUtil.run(Runnable {
                        ToastUtils.showShort("上传成功！！！")
                        tv_gotovideo.text = "开始上传"
                        finish()
                    })

                }

                override fun onFail(msg: String?) {
                    MainThreadUtil.run(Runnable {
                        ToastUtils.showShort(msg)
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

    // 进度对话框
    open fun initProgressDialog() {

        progressBar?.max = 100
        val mFlowId = intent.extras.getString(VideoUploadActivity.flowIdStr)
        val screenRecordStr = TxSPUtils.get(this, mFlowId, "" ) as String
        if (screenRecordStr.isEmpty()) {
            TxPopup.Builder(this).asConfirm("退出",
                    "视频文件不存在，请从“我的双录”进入找到此单重新录制",
                    "取消",
                    "确认",
                    { finish() },
                    null,
                    false).show()
        }else{

        }

        tv_invite_wx.text = "$mFlowId"
        tv_gotovideo.setOnClickListener {
            if (tv_gotovideo.text =="开始上传") {
                upload(mFlowId,screenRecordStr)
            }else{
                SystemHttpRequest.getInstance().cancelClient()
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

        progressBar?.progress = progressInt

        tvPercent?.text = "$progressInt %"

    }


}