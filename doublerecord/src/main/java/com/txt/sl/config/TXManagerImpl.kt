package com.txt.sl.config

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.txt.sl.TXSdk
import com.txt.sl.callback.onNetResultCallBack
import com.txt.sl.callback.startVideoResultOnListener
import com.txt.sl.callback.onSDKListener
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.home.HomeActivity
import com.txt.sl.ui.createorder.NewOrderActivity
import com.txt.sl.ui.invite.VideoUploadActivity
import com.txt.sl.ui.order.OrderDetailsPageActivity
import com.txt.sl.utils.*
import org.json.JSONObject

/**
 * Created by JustinWjq
 *
 * @date 2021/2/23.
 * description：sdk实现类
 */
class TXManagerImpl : ITXManager {
    private val mHandler = Handler(Looper.getMainLooper())

    //快速会议检测
    override fun checkPermission(
            context: Activity?,
            agent: String?,
            orgAccount: String?,
            sign: String?,
            businessData: JSONObject?,
            listener: startVideoResultOnListener,
            isAgent: Boolean
    ) {
        checkPermission(
                context,
                "",
                agent,
                "",
                orgAccount,
                sign,
                businessData,
                listener,
                isAgent
        )
    }

    //检测进入房间需要的权限
    override fun checkPermission(
            context: Activity?,
            roomId: String?,
            account: String?,
            userName: String?,
            orgAccount: String?,
            sign: String?,
            businessData: JSONObject?,
            listener: startVideoResultOnListener,
            isAgent: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TxPermissionUtils.permission(
                    TxPermissionConstants.CAMERA,
                    TxPermissionConstants.MICROPHONE,
                    TxPermissionConstants.PHONE,
                    TxPermissionConstants.STORAGE
            ).callback(object : TxPermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {

                    if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains(
                                    "android.permission.RECORD_AUDIO"
                            )
                    ) {
                        if (isAgent) {
                        } else {

                        }

                    } else {
                        listener.onResultFail(10000, "视频权限或音频权限未申请！")
                    }
                }

                override fun onDenied(
                        permissionsDeniedForever: List<String>,
                        permissionsDenied: List<String>
                ) {
                    TxLogUtils.i("txsdk---permissionsDeniedForever ---- $permissionsDeniedForever")
                    TxLogUtils.i("txsdk---permissionsDenied ---- $permissionsDenied")
                    if (permissionsDenied.contains("android.permission.CAMERA") || permissionsDenied.contains(
                                    "android.permission.RECORD_AUDIO"
                            )
                    ) {
                        listener.onResultFail(10000, "视频权限或音频权限未申请！")
                    } else {
                    }
                }
            }
            ).request()
        } else {
            TxLogUtils.i("txsdk---joinRoom----23以下 ")

        }
    }


    //跳转到详情页面
    override fun gotoOrderDetaisPage(
        context: Activity?, loginName: String, fullName: String, orgCode: String, taskId: String, sign: String, listener: onSDKListener
    ) {
        this.mLoginName = loginName
        freeLogin(orgCode, sign, loginName, fullName, object : onNetResultCallBack {
            override fun onResultSuccess(result: String) {

                if (getAgentId().isEmpty() || getTenantId().isEmpty() || getToken().isEmpty()) {
                    listener?.onResultFail(10000,"返回参数为空")
                } else {
                    listener?.onResultSuccess("")
                    OrderDetailsPageActivity.newActivity(context!!,taskId)
                }

            }

            override fun onResultFail(errCode: Int, errMsg: String) {

            }

        })
    }

    //跳转到创建工单页面
    override fun gotoCreateDetaisPage(context: Activity?, loginName: String, fullName: String, orgCode: String, sign: String, listener: onSDKListener
    ) {
        this.mLoginName = loginName
        freeLogin(orgCode, sign, loginName, fullName, object : onNetResultCallBack {
            override fun onResultSuccess(result: String) {
                val stringStr = GsonUtils.getJson(TXSdk.getInstance().application, "reportstates.json")
                val jsonObject1 = JSONObject(stringStr)
                val reportStatesJB = jsonObject1.getJSONArray("reportStates")
                TxSPUtils.put(TXSdk.getInstance().application, SPConstant.REPORT_STATESLIST, reportStatesJB.toString())

                if (getAgentId().isEmpty() || getTenantId().isEmpty() || getToken().isEmpty()) {
                    MainThreadUtil.run {
                        listener?.onResultFail(10000, "返回参数为空")
                    }

                } else {
                    MainThreadUtil.run {
                        listener?.onResultSuccess("")
                        NewOrderActivity.newActivity(context!!)
                    }
                }

            }

            override fun onResultFail(errCode: Int, errMsg: String) {
                MainThreadUtil.run {
                    listener?.onResultFail(errCode, errMsg)
                }
            }

        })
    }

    //跳转到工单列表页面
    override fun gotoOrderListPage(context: Activity?, loginName: String, fullName: String, orgCode: String, sign: String, listener: onSDKListener
    ) {
        this.mLoginName = loginName
        freeLogin(orgCode, sign, loginName, fullName, object : onNetResultCallBack {
            override fun onResultSuccess(result: String) {
                val stringStr = GsonUtils.getJson(TXSdk.getInstance().application, "reportstates.json")
                val jsonObject1 = JSONObject(stringStr)
                val reportStatesJB = jsonObject1.getJSONArray("reportStates")
                TxSPUtils.put(TXSdk.getInstance().application, SPConstant.REPORT_STATESLIST, reportStatesJB.toString())

                if (getAgentId()!!.isEmpty() || getTenantId()!!.isEmpty() || getToken()!!.isEmpty()) {

                    MainThreadUtil.run {
                        listener?.onResultFail(10000, "返回参数为空")
                    }
                } else {
                    MainThreadUtil.run {
                        listener?.onResultSuccess("")
                        HomeActivity.newActivity(context!!)
                    }
                }

            }

            override fun onResultFail(errCode: Int, errMsg: String) {
                MainThreadUtil.run {
                    listener?.onResultFail(errCode, errMsg)
                }
            }

        })


    }

    //跳转到上传视频页面
    override fun gotoVideoUploadPage(
        context: Activity?, loginName: String, fullName: String, orgCode: String, taskId:String,
        sign: String, listener: onSDKListener
    ) {
        this.mLoginName = loginName
        freeLogin(orgCode, sign, loginName, fullName, object : onNetResultCallBack {
            override fun onResultSuccess(result: String) {
                val stringStr = GsonUtils.getJson(TXSdk.getInstance().application, "reportstates.json")
                val jsonObject1 = JSONObject(stringStr)
                val reportStatesJB = jsonObject1.getJSONArray("reportStates")
                TxSPUtils.put(TXSdk.getInstance().application, SPConstant.REPORT_STATESLIST, reportStatesJB.toString())

                if (getAgentId()!!.isEmpty() || getTenantId()!!.isEmpty() || getToken()!!.isEmpty()) {
                    MainThreadUtil.run {
                        listener?.onResultFail(10000, "返回参数为空")
                    }
                } else {
                    MainThreadUtil.run {
                        listener?.onResultSuccess("")
                        VideoUploadActivity.newActivity(context!!, taskId)
                    }
                }

            }

            override fun onResultFail(errCode: Int, errMsg: String) {
                MainThreadUtil.run {
                    listener?.onResultFail(errCode, errMsg)
                }
            }

        })
    }

    private var mAgentId: String? = null
    private var mTenantId: String? = null
    private var mToken: String? = null
    private var mLoginName: String? = null
    private var mFullName: String? = null
    private var mOrgAccountName: String? = null

    //免登录接口
    override fun freeLogin(orgCode: String, sign: String, loginName: String, fullName: String, netResultCallBack: onNetResultCallBack) {

        SystemHttpRequest.getInstance().passwordFreeLogin(orgCode, sign, loginName, fullName, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                val jsonObject = JSONObject(json)
                val agentInfoJsonObject = jsonObject.getJSONObject("agentInfo")
                val agentId = agentInfoJsonObject.optString("agentId")
                val tenantId = agentInfoJsonObject.optString("tenant")
                val orgAccountName = agentInfoJsonObject.optString("orgAccountName")
                val fullName = agentInfoJsonObject.optString("fullName")
                val token = jsonObject.optString("token")
                mAgentId = agentId
                mTenantId = tenantId
                mToken = token
                mOrgAccountName = orgAccountName
                mFullName = fullName
                netResultCallBack.onResultSuccess(json!!)

            }

            override fun onFail(err: String?, code: Int) {
                netResultCallBack.onResultFail(code, err!!)
            }

        })


    }

    public override fun getToken(): String = mToken!!

    public override fun getAgentId(): String = mAgentId!!

    public override fun getTenantId(): String = mTenantId!!

    override fun getLoginName(): String = mLoginName!!

    override fun getFullName(): String =  mFullName!!

    override fun getOrgAccountName(): String = mOrgAccountName!!

    companion object {
        @Volatile
        private var singleton: TXManagerImpl? = null

        @JvmStatic
        val instance: TXManagerImpl?
            get() {
                if (singleton == null) {
                    synchronized(TXManagerImpl::class.java) {
                        if (singleton == null) {
                            singleton =
                                    TXManagerImpl()
                        }
                    }
                }
                return singleton
            }
    }
}