package com.txt.sl.ui.order

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.txt.sl.R
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.bean.*
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderDetailsItemAdapter
import com.txt.sl.ui.adpter.OrderExpandableItemAdapter1
import com.txt.sl.ui.home.BaseLazyViewPagerFragment
import com.txt.sl.ui.video.visibility
import com.txt.sl.utils.GsonUtils
import com.txt.sl.utils.LogUtils
import kotlinx.android.synthetic.main.tx_activity_new_order.*
import kotlinx.android.synthetic.main.tx_activity_order_details_page.*
import kotlinx.android.synthetic.main.tx_fragment_order_details1.*
import kotlinx.android.synthetic.main.tx_fragment_order_details1.nestedscrollview
import kotlinx.android.synthetic.main.tx_fragment_order_details1.recyclerview
import kotlinx.android.synthetic.main.tx_fragment_order_details1.recyclerview1
import org.json.JSONException
import org.json.JSONObject

class OrderDetailsFragment : BaseLazyViewPagerFragment() {

    private var applyStatusParams: String? = null
    var mFlowId: String? = null
    var api: IWXAPI? = null

    var mDataList: ArrayList<OrderBean>? = null
    var requestOrderBean: RequestOrderBean? = null

    override fun initData() {

        val stringStr = GsonUtils.getJson(_mActivity, "ordertypelist.json")
        val json = JSONObject(stringStr)
        val jsonarr = json.getJSONArray("data")
        mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(jsonarr.toString(), object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type)
        initRecyclerview()
        initRecyclerview1()
        requestData()
    }

    override fun getLayoutId(): Int {
        return R.layout.tx_fragment_order_details1
    }

    override fun getTitleBarId(): Int {
        return 0
    }

    var orderDetailsItemlists = ArrayList<OrderDetailsItem>()
    var baseQuickAdapter: OrderDetailsItemAdapter? = null
    fun initRecyclerview() {

        recyclerview.layoutManager = LinearLayoutManager(_mActivity)
        baseQuickAdapter = OrderDetailsItemAdapter(orderDetailsItemlists!!)
        recyclerview.adapter = baseQuickAdapter


    }

    override fun initView() {
        if (arguments != null) {
            applyStatusParams = arguments!!.getString(ARG_PARAM1)
            mFlowId = arguments!!.getString(ARG_PARAM2)
        }
    }


    var list = ArrayList<OrderDetailsItem>()
    var workItemBean: WorkItemBean? = null
    fun requestData() {
        val orderDetailsActivity = _mActivity as OrderDetailsActivity
        orderDetailsActivity.mLoadingView?.show()
        SystemHttpRequest.getInstance().getFlowDetails(mFlowId, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                LogUtils.i("onSuccess$json")
                _mActivity?.runOnUiThread {
                    orderDetailsItemlists.clear()
                    orderDetailsActivity.mLoadingView?.dismiss()
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


                            relationship = fields.optString("relationship")
                            repordId = fields.optString("reportId")
                            insurance = _id //险种id
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("业务单号", fields.optString("taskId")))
                        val stringBuffer = StringBuffer()
                        val optJSONArray = fields.optJSONArray("institutionNames")
                        if (null != optJSONArray) {
                            for (index in 0 until optJSONArray.length()) {
                                val subStr = optJSONArray.getString(index)
                                stringBuffer.append("$subStr")
                            }
                        }else{
                            stringBuffer.append("暂无")
                        }

                        if (TXManagerImpl.instance!!.getTenantCode()=="remoteRecordPoc") {
                            orderDetailsItemlists.add(OrderDetailsItem("所属区域",TXManagerImpl.instance!!.getOrgAccountName()))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("所属区域",stringBuffer.toString()))
                            orderDetailsItemlists.add(OrderDetailsItem("中介机构", fields.optString("IntermediaryInstitutions")))
                        }


                        orderDetailsItemlists.add(OrderDetailsItem("代理人姓名", agentJSONObject.optString("fullName")))
                        orderDetailsItemlists.add(OrderDetailsItem("代理人编码",agentJSONObject.optString("loginName")) )
                        val filterStr = mDataList?.filter { it.name == "投保人证件类型" }
                        val filterStr1 = filterStr?.get(0)!!.options.filter { it.key == fields.optString("agentCertificateType") }
                        if (filterStr1.isNotEmpty()&&filterStr1.size>0) {
                            orderDetailsItemlists.add(OrderDetailsItem("代理人证件类型", filterStr1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("代理人证件类型", "暂无"))
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("代理人证件号", fields.optString("agentCertificateNo","暂无")))
                        orderDetailsItemlists.add(OrderDetailsItem("投保人姓名", fields.optString("policyholderName")))


                        val filter = mDataList?.filter { it.name == "投保人证件类型" }
                        val filter1 = filter?.get(0)!!.options.filter { it.key == fields.optString("policyholderCertificateType") }
                        if (filter1.isNotEmpty()) {
                            orderDetailsItemlists.add(OrderDetailsItem("投保人证件类型", filter1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("投保人证件类型","暂无"))
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("投保人证件号", fields.optString("policyholderCertificateNo")))
                        orderDetailsItemlists.add(OrderDetailsItem("投保人年龄", fields.optString("policyholderAge")))
                        orderDetailsItemlists.add(OrderDetailsItem("投保人手机号", fields.optString("policyholderPhone")))

                        val policyholderGenderfilter = mDataList?.filter { it.name == "投保人性别" }
                        val policyholderGenderfilter1 = policyholderGenderfilter?.get(0)!!.options.filter { it.key == fields.optString("policyholderGender") }
                        if (policyholderGenderfilter1.isNotEmpty()) {
                            orderDetailsItemlists.add(OrderDetailsItem("投保人性别", policyholderGenderfilter1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("投保人性别", "暂无"))
                        }

                        val relationshipfilter = mDataList?.filter { it.name == "与投保人关系" }
                        val relationshipfilter1 = relationshipfilter?.get(0)!!.options.filter { it.key == fields.optString("relationship") }
                        if (relationshipfilter1.isNotEmpty()) {
                            orderDetailsItemlists.add(OrderDetailsItem("与投保人关系",  relationshipfilter1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("与投保人关系",  "暂无"))
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("被保人姓名", fields.optString("insuredName")))

                        val insuredCertificateTypefilter = mDataList?.filter { it.name == "被保人证件类型" }
                        val insuredCertificateTypefilter1 = insuredCertificateTypefilter?.get(0)!!.options.filter { it.key == fields.optString("insuredCertificateType") }
                        if (insuredCertificateTypefilter1.isNotEmpty()) {
                            orderDetailsItemlists.add(OrderDetailsItem("被保人证件类型", insuredCertificateTypefilter1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("被保人证件类型", "暂无"))
                        }
                        orderDetailsItemlists.add(OrderDetailsItem("被保人证件号", fields.optString("insuredCertificateNo")))
                        orderDetailsItemlists.add(OrderDetailsItem("被保人年龄", fields.optString("insuredAge")))
                        orderDetailsItemlists.add(OrderDetailsItem("被保人手机号", fields.optString("insuredPhone")))
                        val insuredGenderfilter = mDataList?.filter { it.name == "被保人性别" }
                        val insuredGenderfilter1 = insuredGenderfilter?.get(0)!!.options.filter { it.key == fields.optString("insuredGender") }
                        if (insuredGenderfilter1.isNotEmpty()) {
                            orderDetailsItemlists.add(OrderDetailsItem("被保人性别", insuredGenderfilter1[0].name))
                        }else{
                            orderDetailsItemlists.add(OrderDetailsItem("被保人性别","暂无"))
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("整单首期保费", fields.optString("insuranceAllPaymentDown")))
                        val jsonArray = fields.getJSONArray("insuranceIsMain")
                        list1.clear()
                        if (null!=jsonArray&&jsonArray.length()>0) {
                            for (index in 0 until jsonArray.length()){
                                val requestSubOrderBean = RequestSubOrderBean()
                                requestSubOrderBean.insuranceIsMain = fields!!.getJSONArray("insuranceIsMain")!!.getInt(index)
                                requestSubOrderBean.insuranceType = fields!!.getJSONArray("insuranceType")!!.getString(index)
                                requestSubOrderBean.insuranceName = fields!!.getJSONArray("insuranceName")!!.getString(index)
                                requestSubOrderBean.insurancePaymentDown = fields!!.getJSONArray("insurancePaymentDown")!!.getString(index)
                                requestSubOrderBean.insurancePaymentMethod = fields!!.getJSONArray("insurancePaymentMethod")!!.getString(index)
                                requestSubOrderBean.insurancePaymentPeriods = fields!!.getJSONArray("insurancePaymentPeriods")!!.getString(index)
                                requestSubOrderBean.insurancePaymentPrice = fields!!.getJSONArray("insurancePaymentPrice")!!.getString(index)
                                requestSubOrderBean.insurancePaymentYearUnit = fields!!.getJSONArray("insurancePaymentYearUnit")!!.getString(index)
                                requestSubOrderBean.insurancePaymentYear = fields!!.getJSONArray("insurancePaymentYear")!!.getInt(index)
                                requestSubOrderBean.insuranceCode = fields!!.getJSONArray("insuranceCode")!!.getString(index)
                                addProductData(requestSubOrderBean)
                            }

                        }

                    } catch (e: JSONException) {
                        LogUtils.i(e.message!!)
                    }
                    baseQuickAdapter?.setNewData(orderDetailsItemlists)
                }
            }

            override fun onFail(err: String?, code: Int) {

                _mActivity?.runOnUiThread {
                    orderDetailsActivity.mLoadingView?.dismiss()
                }
            }

        })
    }


    var list1 = ArrayList<MultiItemEntity>()
    var baseQuickAdapter1: OrderExpandableItemAdapter1? = null
    fun initRecyclerview1() {

        recyclerview1.layoutManager = LinearLayoutManager(_mActivity)
        baseQuickAdapter1 = OrderExpandableItemAdapter1(list1!!)
        baseQuickAdapter1?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.headerliner -> {
                    LogUtils.i("position$position")
                    val itemBean = baseQuickAdapter1?.data?.get(position)
                    if (itemBean is ProductLevelItem ){
                        if (itemBean.isExpanded) {
                            baseQuickAdapter1?.collapse(position)

                        } else {
                            baseQuickAdapter1?.expand(position)
                            Handler().postDelayed({ nestedscrollview.smoothScrollBy(0,700)},500)
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

    fun addProductData(requestSubOrderBean:RequestSubOrderBean){
        val levelItem1 = ProductLevelItem()
        levelItem1.insuranceName = requestSubOrderBean.insuranceName
        levelItem1.insuranceIsMain = requestSubOrderBean.insuranceIsMain
        levelItem1.addSubItem(requestSubOrderBean)
        baseQuickAdapter1?.addData(levelItem1)
    }



    companion object {
        private const val ARG_PARAM1 = "applyStatusParams"
        private const val ARG_PARAM2 = "bean"

        @JvmStatic
        fun newInstance(applyStatusParams: String?): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, applyStatusParams)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(applyStatusParams: String, flowId: String): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, applyStatusParams)
            args.putString(ARG_PARAM2, flowId)
            fragment.arguments = args
            return fragment
        }
    }
}