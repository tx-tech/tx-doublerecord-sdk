package com.txt.sl.ui.invite

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.CosXmlSimpleService
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.transfer.COSXMLUploadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.cos.xml.transfer.TransferState
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import com.tencent.qcloud.core.common.QCloudClientException
import com.txt.sl.config.socket.SocketConfig
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.DateUtils
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.TxLogUtils
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * author ：Justin
 * time ：2021/9/9.
 * des ： 上传任务
 */
public class VideoUploadImp {
    companion object {
        @JvmStatic
        val instance by lazy { VideoUploadImp() }
    }


    // 通过弱引用持有Activity，防止内容泄漏，适用于只在一个Activity创建浮窗的情况
    private var activityWr: WeakReference<Activity>? = null
    public fun upload(
        activity: Context,
        uploadId: String,
        preTime: String,
        serviceId: String,
        pathFile: String,
        onUploadListener: OnUploadListener? = null
    ) {
        if (activity is Activity) activityWr = WeakReference(activity)

        SystemHttpRequest.getInstance()
            .getCosStsToken(object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    val jsonObject = JSONObject(json)
                    val jsonObject1 = jsonObject.getJSONObject("Credentials")
                    val expiredTime = jsonObject.getLong("ExpiredTime")
                    val mToken = jsonObject1.getString("Token")
                    val mTmpSecretId = jsonObject1.getString("TmpSecretId")
                    val mTmpSecretKey = jsonObject1.getString("TmpSecretKey")
                    val region = "ap-shanghai"
                    val serviceConfig = CosXmlServiceConfig.Builder()
                        .setRegion(region)
                        .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                        .builder()

                    val cosXmlService = CosXmlSimpleService(
                        activity,
                        serviceConfig,
                        MySessionCredentialProvider(
                            mTmpSecretId,
                            mTmpSecretKey,
                            mToken,
                            expiredTime
                        )
                    )
                    try {
                        uploadToS3(
                            cosXmlService,
                            uploadId,
                            serviceId,
                            preTime,
                            pathFile,
                            onUploadListener
                        )
                    }catch (e:IllegalArgumentException){
                        //
                        LogUtils.i(e.toString())
                    }


                }

                override fun onFail(err: String?, code: Int) {

                }

            })

    }


   public var cosxmlUploadTask: COSXMLUploadTask? = null
    private fun uploadToS3(
        cosXmlService: CosXmlSimpleService,
        uploadId: String,
        serviceId: String?,
        preTime: String?,
        pathFile: String?,
        onUploadListener: OnUploadListener? = null
    ) {
        val transferConfig = TransferConfig.Builder().build()
        val transferManager = TransferManager(
            cosXmlService,
            transferConfig
        )
        val bucket = "gdrb-dingsun-test-1255383806" //存储桶，格式：BucketName-APPID
        val dateToString = DateUtils.dateToString(Date())
        val cosPath = "${dateToString}/${serviceId}/record.mp4" //对象在存储桶中的位置标识符，即称对象键

        val srcPath: String = File(pathFile)
            .toString() //本地文件的绝对路径
        cosxmlUploadTask = transferManager.upload(
            bucket, cosPath,
            srcPath,if (uploadId.isEmpty()){
                null
            }else{
                uploadId
            }
        )

        cosxmlUploadTask?.setCosXmlProgressListener { complete, target ->
            TxLogUtils.i("complete：" + complete + "----target" + target)
            onUploadListener?.onProgress(complete, target)
        }
        cosxmlUploadTask?.setCosXmlResultListener(object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                result as COSXMLUploadTask.COSXMLUploadTaskResult
                TxLogUtils.i("cOSXMLUploadTaskResult：" + result.accessUrl)
                uploadServiceVideoNew(serviceId!!, preTime!!, result.accessUrl)
                onUploadListener?.onSuccess()
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException,
                serviceException: CosXmlServiceException
            ) {



                try {
                    if (clientException != null) {
                        clientException.printStackTrace()
                    } else {
                        serviceException.printStackTrace()
                    }
                    TxLogUtils.i(
                        "onFail：" + request.bucket + "clientException:"
                                + clientException.message + clientException.errorCode +
                                "serviceException:" + serviceException.message + serviceException.errorCode
                    )
                }catch (e:Exception){

                }finally {
                    onUploadListener?.onFail()
                }

            }
        })
        //设置任务状态回调, 可以查看任务过程，并拿到 uploadId 用于续传
        cosxmlUploadTask?.setTransferStateListener {


            val state = TransferState.getState(it.name)
            if (TransferState.PAUSED == state) {
                //暂停
                onUploadListener?.onStateChanged(it,cosxmlUploadTask?.uploadId)
            }
            TxLogUtils.i("cosxmlUploadTask：" + state)
            TxLogUtils.i("cosxmlUploadTask：" + cosxmlUploadTask?.uploadId)
        }


    }

    private fun uploadServiceVideoNew(serviceId: String, preTime: String, url: String) {
        SystemHttpRequest.getInstance()
            .uploadServiceVideoNew(
                serviceId,
                preTime,
                url,
                object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {

                    }

                    override fun onFail(err: String?, code: Int) {

                    }
                })
    }

    class MySessionCredentialProvider(
        val tmpSecretId: String,
        val tmpSecretKey: String,
        val token: String,
        val ExpiredTime: Long
    ) : BasicLifecycleCredentialProvider() {
        @Throws(QCloudClientException::class)
        override fun fetchNewCredentials(): QCloudLifecycleCredentials {
            return SessionQCloudCredentials(tmpSecretId, tmpSecretKey, token, ExpiredTime)
        }
    }

}