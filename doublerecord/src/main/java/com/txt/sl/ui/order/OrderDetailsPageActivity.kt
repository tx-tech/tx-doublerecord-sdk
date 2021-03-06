package com.txt.sl.ui.order

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.bean.*
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderDetailsItemAdapter
import com.txt.sl.ui.adpter.OrderExpandableItemAdapter1
import com.txt.sl.ui.dialog.CheckRemoteDialog
import com.txt.sl.ui.invite.InviteActivity
import com.txt.sl.utils.GsonUtils
import com.txt.sl.utils.LogUtils
import kotlinx.android.synthetic.main.tx_activity_order_details_page.*
import org.json.JSONException
import org.json.JSONObject

class OrderDetailsPageActivity : BaseActivity(), CheckRemoteDialog.OnRemoteClickListener {


    override fun getLayoutId(): Int = R.layout.tx_activity_order_details_page

    companion object {
        var taskIdStr = "taskId"
        fun newActivity(context: Context, flowId: String) {
            val intent = Intent(context, OrderDetailsPageActivity::class.java)
            intent.putExtra(taskIdStr, flowId)
            context.startActivity(intent)
        }

    }

    var mDataList: ArrayList<OrderBean>? = null


    override fun initData() {

        val stringStr = GsonUtils.getJson(this, "ordertypelist.json")
        val json = JSONObject(stringStr)
        val jsonarr = json.getJSONArray("data")
        mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(
            jsonarr.toString(),
            object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type
        )
        initRecyclerview()
        initRecyclerview1()
        requestData()
    }

    var orderDetailsItemlists = ArrayList<OrderDetailsItem>()
    var baseQuickAdapter: OrderDetailsItemAdapter? = null
    fun initRecyclerview() {

        recyclerview.layoutManager = LinearLayoutManager(this)
        baseQuickAdapter = OrderDetailsItemAdapter(orderDetailsItemlists!!)
        recyclerview.adapter = baseQuickAdapter


    }


    public var customDialog: CheckRemoteDialog? = null
    override fun initView() {
        super.initView()
        title = "????????????"
        customDialog = CheckRemoteDialog(this)
        customDialog?.setOnRemoteClickListener(this)
        btn_commit.setOnClickListener {
            customDialog?.setData(
                workItemBean
            )
            if (workItemBean?.isSelfInsurance!!) {
                //??????????????????
                InviteActivity.newInstance(
                    this, false, "2", workItemBean!!
                )
            } else {
                showCheckRemoteDialog()
            }


        }
    }

    public fun showCheckRemoteDialog() {

        customDialog?.show()

    }

    override fun onBackPressed() {
        showDialog()
    }

    private fun showDialog() {
        TxPopup.Builder(this)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(false).asConfirm(
                "??????",
                "?????????????????????????????????",
                "??????",
                "??????",
                { finish() },
                null,
                false
            ).show()
    }

    override fun onDestroy() {
        if (null != TXSdk.getInstance().onTxPageListener) {
            TXSdk.getInstance().onTxPageListener.onSuccess(mTaskId!!)
        }
        super.onDestroy()
    }


    var list = ArrayList<OrderDetailsItem>()
    var workItemBean: WorkItemBean? = null
    var mTaskId = ""
    fun requestData() {
        val dialog = TxPopup.Builder(this).asLoading("???????????????...").show()
        mTaskId = intent?.extras?.getString(OrderDetailsPageActivity.taskIdStr)!!
        SystemHttpRequest.getInstance()
            .getFlowDetailsByTaskid(mTaskId, object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    LogUtils.i("onSuccess$json")
                    list1.clear()
                    orderDetailsItemlists.clear()

                    runOnUiThread {
                        dialog.dismiss()

                        try {
                            val jsonObject = JSONObject(json)
                            val fields = jsonObject.getJSONObject("fields")
                            val agentJSONObject = jsonObject.getJSONObject("agent")
                            val _id = jsonObject.getString("insurance")
                            workItemBean = WorkItemBean().apply {
                                flowId = jsonObject.optString("flowId")
                                ctime = jsonObject.optString("ctime")
                                status = jsonObject.optString("state")
                                canEdit = jsonObject.optInt("canEdit", 1)
                                recordUrl = jsonObject.optString("recordUrl")
                                isIsRemote = jsonObject.optBoolean("isRemote")
                                var list = ArrayList<String>()
                                val optJSONArray = jsonObject.optJSONArray("membersArray")
                                if (null != optJSONArray && optJSONArray.length() > 0) {
                                    for (index in 0 until optJSONArray.length()) {
                                        list.add(optJSONArray.getString(index))
                                    }
                                }
                                membersArray = list
                                insuranceName = ""

                                insurantName = fields.optString("insurantName")
                                insurantIdCard = fields.optString("insurantIdCard")
                                insurantPhone = fields.optString("insurantPhone")

                                insuredMoney = fields.optString("insuredMoney")
                                insuredDate = fields.optString("insuredDate")
                                insuredIdCard = fields.optString("insuredIdCard")
                                insuredName = fields.optString("insuredName")
                                insuredPhone = fields.optString("insuredPhone")


                                payType = fields.optString("payType")
                                taskId = fields.optString("taskId")

                                isSelfInsurance = jsonObject.optBoolean("selfInsurance") //???????????????
                                relationship = jsonObject.optString("relationship")
                                repordId = fields.optString("reportId")
                                policyholderUrl = fields.optString("policyholderUrl")
                                insuranceUrl = fields.optString("insuranceUrl")
                                insurance = _id //??????id
                                recordingMethod = jsonObject.optString("recordingMethod")
                            }

                            orderDetailsItemlists.add(
                                if ( TextUtils.isEmpty(fields.optString("insurerQuotationNo"))){
                                    OrderDetailsItem(
                                        "????????????",
                                        fields.optString("taskId")
                                    )
                                }else{

                                    OrderDetailsItem(
                                        "????????????",
                                        fields.optString("insurerQuotationNo")
                                    )
                                }
                            )
                            val stringBuffer = StringBuffer()
                            val optJSONArray = fields.optJSONArray("institutionNames")
                            if (null != optJSONArray) {
                                for (index in 0 until optJSONArray.length()) {
                                    val subStr = optJSONArray.getString(index)
                                    stringBuffer.append("$subStr")
                                }
                            } else {
                                stringBuffer.append("??????")
                            }



                            if (TXManagerImpl.instance!!.getTenantCode() == "remoteRecord") {

                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "????????????",
                                        stringBuffer.toString()
                                    )
                                )
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "????????????",
                                        fields.optString("IntermediaryInstitutions")
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "????????????",
                                        TXManagerImpl.instance!!.getOrgAccountName()
                                    )
                                )

                            }
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    agentJSONObject.optString("fullName")
                                )
                            )

                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    agentJSONObject.optString("loginName")
                                )

                            )

                            val filterStr = mDataList?.filter { it.name == "?????????????????????" }
                            val filterStr1 =
                                filterStr?.get(0)!!.options.filter { it.key == fields.optString("agentCertificateType") }
                            if (filterStr1.isNotEmpty() && filterStr1.size > 0) {
                                list1.add(OrderDetailsItem("?????????????????????", filterStr1[0].name))
                            } else {
                                list1.add(OrderDetailsItem("?????????????????????", "??????"))
                            }

                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("agentCertificateNo", "??????")
                                )
                            )
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    fields.optString("policyholderName")
                                )
                            )

                            val filter = mDataList?.filter { it.name == "?????????????????????" }
                            val filter1 =
                                filter?.get(0)!!.options.filter { it.key == fields.optString("policyholderCertificateType") }
                            if (filter1.isNotEmpty()) {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "?????????????????????",
                                        filter1[0].name
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(OrderDetailsItem("?????????????????????", "??????"))
                            }

                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("policyholderCertificateNo")
                                )
                            )
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    fields.optString("policyholderAge")
                                )
                            )
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("policyholderPhone")
                                )
                            )

                            val policyholderGenderfilter = mDataList?.filter { it.name == "???????????????" }
                            val policyholderGenderfilter1 =
                                policyholderGenderfilter?.get(0)!!.options.filter {
                                    it.key == fields.optString("policyholderGender")
                                }
                            if (policyholderGenderfilter1.isNotEmpty()) {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "???????????????",
                                        policyholderGenderfilter1[0].name
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(OrderDetailsItem("???????????????", "??????"))
                            }

                            val relationshipfilter = mDataList?.filter { it.name == "??????????????????" }
                            val relationshipfilter1 = relationshipfilter?.get(0)!!.options.filter {
                                it.key == fields.optString("relationship")
                            }
                            if (relationshipfilter1.isNotEmpty()) {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "??????????????????",
                                        relationshipfilter1[0].name
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(OrderDetailsItem("??????????????????", "??????"))
                            }

                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    fields.optString("insuredName")
                                )
                            )

                            val insuredCertificateTypefilter =
                                mDataList?.filter { it.name == "?????????????????????" }
                            val insuredCertificateTypefilter1 =
                                insuredCertificateTypefilter?.get(0)!!.options.filter {
                                    it.key == fields.optString("insuredCertificateType")
                                }
                            if (insuredCertificateTypefilter1.isNotEmpty()) {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "?????????????????????",
                                        insuredCertificateTypefilter1[0].name
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(OrderDetailsItem("?????????????????????", "??????"))
                            }
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("insuredCertificateNo")
                                )
                            )
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "???????????????",
                                    fields.optString("insuredAge")
                                )
                            )
                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("insuredPhone")
                                )
                            )
                            val insuredGenderfilter = mDataList?.filter { it.name == "???????????????" }
                            val insuredGenderfilter1 =
                                insuredGenderfilter?.get(0)!!.options.filter {
                                    it.key == fields.optString("insuredGender")
                                }
                            if (insuredGenderfilter1.isNotEmpty()) {
                                orderDetailsItemlists.add(
                                    OrderDetailsItem(
                                        "???????????????",
                                        insuredGenderfilter1[0].name
                                    )
                                )
                            } else {
                                orderDetailsItemlists.add(OrderDetailsItem("???????????????", "??????"))
                            }

                            orderDetailsItemlists.add(
                                OrderDetailsItem(
                                    "??????????????????",
                                    fields.optString("insuranceAllPaymentDown")
                                )
                            )
                            val jsonArray = fields.getJSONArray("insuranceIsMain")
                            if (null != jsonArray && jsonArray.length() > 0) {
                                for (index in 0 until jsonArray.length()) {
                                    val requestSubOrderBean = RequestSubOrderBean()
                                    requestSubOrderBean.apply {
                                        insuranceIsMain =
                                            fields!!.getJSONArray("insuranceIsMain")!!.getInt(index)
                                        insuranceType = fields!!.getJSONArray("insuranceType")!!
                                            .getString(index)
                                        insuranceName = fields!!.getJSONArray("insuranceName")!!
                                            .getString(index)
                                        insurancePaymentDown =
                                            fields!!.getJSONArray("insurancePaymentDown")!!
                                                .getString(index)
                                        insurancePaymentMethod =
                                            fields!!.getJSONArray("insurancePaymentMethod")!!
                                                .getString(index)
                                        insurancePaymentPeriods =
                                            fields!!.getJSONArray("insurancePaymentPeriods")!!
                                                .getString(index)
                                        insurancePaymentPrice =
                                            fields!!.getJSONArray("insurancePaymentPrice")!!
                                                .getString(index)
                                        insurancePaymentYearUnit =
                                            fields!!.getJSONArray("insurancePaymentYearUnit")!!
                                                .getString(index)
                                        insurancePaymentYear =
                                            fields!!.getJSONArray("insurancePaymentYear")!!
                                                .getInt(index)
                                        insuranceCode = fields!!.getJSONArray("insuranceCode")!!
                                            .getString(index)

                                    }
                                    addProductData(requestSubOrderBean)
                                }

                            }
                            baseQuickAdapter1?.setNewData(list1)
                            baseQuickAdapter?.setNewData(orderDetailsItemlists)

                            btn_commit.visibility = View.VISIBLE

                        } catch (e: JSONException) {
                            LogUtils.i(e.message!!)
                            showToastMsg("???????????????${e.message!!}")
                        }
                    }
                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        dialog.dismiss()
                        showToastMsg(err)
                        Handler().postDelayed({ finish() }, 1000)

                    }
                }

            })
    }


    var list1 = ArrayList<MultiItemEntity>()
    var baseQuickAdapter1: OrderExpandableItemAdapter1? = null
    fun initRecyclerview1() {

        recyclerview1.layoutManager = LinearLayoutManager(this)
        baseQuickAdapter1 = OrderExpandableItemAdapter1(list1!!)
        baseQuickAdapter1?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_title -> {
                    LogUtils.i("position$position")
                }
                R.id.headerliner -> {
                    LogUtils.i("position$position")
                    val itemBean = baseQuickAdapter1?.data?.get(position)
                    if (itemBean is ProductLevelItem) {
                        if (itemBean.isExpanded) {
                            baseQuickAdapter1?.collapse(position)
                        } else {
                            baseQuickAdapter1?.expand(position)
                            Handler().postDelayed({ nestedscrollview.smoothScrollBy(0, 700) }, 500)
                        }
                    }
                }
                else -> {
                }
            }


        }
        recyclerview1.adapter = baseQuickAdapter1

        baseQuickAdapter1?.setNewData(list1)

    }

    fun addProductData(requestSubOrderBean: RequestSubOrderBean) {
        val levelItem1 = ProductLevelItem()
        levelItem1.apply {
            insuranceName = requestSubOrderBean.insuranceName
            insuranceIsMain = requestSubOrderBean.insuranceIsMain
            addSubItem(requestSubOrderBean)
        }
        list1.add(levelItem1)
    }

    override fun onConfirmClick(
        isRemote: Boolean,
        recordType: String,
        workItemBean: WorkItemBean
    ) {
        InviteActivity.newInstance(
            this, isRemote, recordType, workItemBean
        )
    }


}