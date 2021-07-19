package com.txt.sl.ui.login

import android.app.Activity
import com.txt.sl.entity.constant.Constant
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.base.BasePresenter
import com.txt.sl.receive.SystemBaiduLocation
import com.txt.sl.utils.GsonUtils
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.TxSPUtils
import org.json.JSONObject


/**
 * Created by pc on 2017/10/20.
 */

class LoginPresenter(private val mContext: Activity, private val mLoginContract: LoginContract.View) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    private val TAG = LoginPresenter::class.java.simpleName

    override fun loginReuqest(loginName: String, passWord: String) {
        SystemHttpRequest.getInstance().login(loginName, passWord, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {

                LogUtils.i("LoginPresenter", json!!)
                val resultObject = JSONObject(json)
                val agentObject = resultObject.getJSONObject("agent")
                val token = resultObject.optString("token")
                val agentId = agentObject.optString("_id")
                val fullName = agentObject.optString("fullName")
                val cellphone = agentObject.optString("cellphone")
                val idCard = agentObject.optString("idCard")
                val recordInstitutionJson = agentObject.optJSONObject("recordInstitution")

               val  orgName =recordInstitutionJson.optString("name")
                val tenantJB = agentObject.getJSONObject("tenant")
                val orgAccountJB = agentObject.getJSONObject("orgAccount")

//                val orgName = orgAccountJB.optString("orgName")


                val tenantId = tenantJB.optString("_id")
                val orgAccountId = orgAccountJB.optString("_id")


                //获取本地的列表状态
                val stringStr = GsonUtils.getJson(mContext, "reportstates.json")
                val jsonObject = JSONObject(stringStr)
                val reportStatesJB = jsonObject.getJSONArray("reportStates")
                TxSPUtils.put(mContext, SPConstant.REPORT_STATESLIST, reportStatesJB.toString())
                TxSPUtils.put(mContext, SPConstant.SP_ISLOGIN, true)
                TxSPUtils.put(mContext, SPConstant.LOGIN_NAME, loginName)
                TxSPUtils.put(mContext, SPConstant.LOGIN_PWD, passWord)

                TxSPUtils.put(mContext, SPConstant.SP_AGENT_ID, agentId)
                TxSPUtils.put(mContext, SPConstant.SP_TOKEN, token)


                TxSPUtils.put(mContext, SPConstant.TENANT_ID, tenantId)
                TxSPUtils.put(mContext, SPConstant.ORGACCOUNT_ID, orgAccountId)

                Constant.fullName = fullName
                Constant.cellphone = cellphone
                Constant.idCard = idCard
                Constant.orgname = orgName
                Constant.loginname = loginName
                Constant.password = passWord

//                mLoginContract?.saveNameAndPwd()
                mLoginContract?.LoginSuccess()

                SystemHttpRequest.getInstance().getInsuranceData(agentId, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        val resultObject = JSONObject(json)
                        val insurancesLists = resultObject.getString("insurances")
                        TxSPUtils.put(mContext, SPConstant.INSURANCES_LISTS, insurancesLists)
                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })

                SystemHttpRequest.getInstance().getProductData(tenantId, object : HttpRequestClient.RequestHttpCallBack {
                    override fun onSuccess(json: String?) {
                        TxSPUtils.put(mContext, SPConstant.PRODUCT_LISTS, json)
                    }

                    override fun onFail(err: String?, code: Int) {

                    }

                })
                SystemBaiduLocation.instance!!.startLocationService()
            }

            override fun onFail(err: String?, code: Int) {
                mLoginContract?.LoginBFail(err, code)
            }

        })
    }


}
