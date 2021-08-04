package com.txt.sl.config

import android.app.Activity
import com.txt.sl.callback.netResultCallBack
import com.txt.sl.callback.startVideoResultOnListener
import com.txt.sl.callback.onSDKListener
import org.json.JSONObject

/**
 * author ：Justin
 * time ：2021/4/2.
 * des ：
 */

interface ITXManager {
    fun checkPermission(
            context: Activity?,
            agent: String?,
            orgAccount: String?,
            sign: String?,
            businessData: JSONObject?,
            listener: startVideoResultOnListener,
            isAgent: Boolean
    )

    fun checkPermission(
            context: Activity?,
            roomId: String?,
            account: String?,
            userName: String?,
            orgAccount: String?,
            sign: String?,
            businessData: JSONObject?,
            listener: startVideoResultOnListener,
            isAgent: Boolean
    )


    fun gotoOrderDetaisPage(
        context: Activity?, loginName: String, fullName: String, orgCode: String, taskId: String, sign: String, listener: onSDKListener
    )

    fun gotoCreateDetaisPage(
            context: Activity?, loginName: String, fullName: String, orgCode: String, sign: String, listener: onSDKListener
    )

    fun gotoOrderListPage(
            context: Activity?, loginName: String, fullName: String, orgCode: String, sign: String, listener: onSDKListener
    )

    fun gotoVideoUploadPage(
        context: Activity?, loginName: String, fullName: String, orgCode: String, taskId: String,
        sign: String, listener: onSDKListener
    )

    fun freeLogin(
            orgCode: String,
            sign: String,
            loginName: String,
            fullName: String,
            listener: netResultCallBack
    )

    fun getToken() :String

    fun getAgentId() :String

    fun getTenantId() :String

    fun getLoginName():String

    fun getFullName():String

    fun getOrgAccountName():String

}