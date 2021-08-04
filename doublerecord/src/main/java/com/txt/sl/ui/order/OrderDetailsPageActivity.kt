package com.txt.sl.ui.order

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import com.common.widget.dialog.TxPopup
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.trtc.TRTCCloud
import com.common.widget.base.BaseActivity
import com.txt.sl.R
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.entity.bean.*
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderDetailsItemAdapter
import com.txt.sl.ui.adpter.OrderExpandableItemAdapter1
import com.txt.sl.ui.dialog.CheckRemoteDialog
import com.txt.sl.ui.invite.InviteActivity
import com.txt.sl.ui.video.OfflineActivity
import com.txt.sl.ui.video.RoomActivity
import com.txt.sl.utils.GsonUtils
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.ToastUtils
import kotlinx.android.synthetic.main.tx_activity_order_details_page.*
import org.json.JSONException
import org.json.JSONObject

class OrderDetailsPageActivity : BaseActivity(), CheckRemoteDialog.OnRemoteClickListener {


    override fun getLayoutId(): Int  = R.layout.tx_activity_order_details_page

    companion object {
        var taskIdStr = "taskId"
        fun newActivity(context: Context, flowId:String) {
            val intent = Intent(context, OrderDetailsPageActivity::class.java)
            intent.putExtra(taskIdStr,flowId)
            context.startActivity(intent)
        }

    }

    var mDataList: ArrayList<OrderBean>? = null
    var requestOrderBean: RequestOrderBean? = null

    override fun initData() {

        val stringStr = GsonUtils.getJson(this, "ordertypelist.json")
        val json = JSONObject(stringStr)
        val jsonarr = json.getJSONArray("data")
        mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(jsonarr.toString(), object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type)
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
    public  var customDialog : CheckRemoteDialog?=null
    override fun initView() {
        super.initView()
        title = "双录任务"
        customDialog  = CheckRemoteDialog(this)
        customDialog?.setOnRemoteClickListener(this)
        btn_commit.setOnClickListener {
            customDialog?.setData(
                workItemBean?.flowId,
                workItemBean?.insuredPhone,
                workItemBean?.taskId,
                workItemBean?.membersArray as java.util.ArrayList<String>?,
                workItemBean?.isSelfInsurance!!
            )

            showCheckRemoteDialog()
        }
    }
    public fun showCheckRemoteDialog(){

        TxPopup.Builder(this).asCustom(customDialog).show()

    }

    override fun onBackPressed() {
       showDialog()
    }

    private fun  showDialog(){
        TxPopup.Builder(this).asConfirm("退出",
                "确认退出双录任务页面？",
                "取消",
                "是的",
                { finish() },
                null,
                false).show()
    }


    var list = ArrayList<OrderDetailsItem>()
    var workItemBean: WorkItemBean? = null
    fun requestData() {
        val mFlowId = intent.extras.getString(OrderDetailsPageActivity.taskIdStr)
        SystemHttpRequest.getInstance().getFlowDetailsByTaskid(mFlowId, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                LogUtils.i("onSuccess$json")
                //todo 数据为空
                runOnUiThread {
                    orderDetailsItemlists.clear()
                    try {
                        val jsonObject = JSONObject(json)
                        val fields = jsonObject.getJSONObject("fields")
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
                            if (null != optJSONArray &&optJSONArray.length()>0){
                                for (index in 0 until  optJSONArray.length()) {
                                    list.add(optJSONArray.getString(index))
                                }
                            }
                            membersArray =list
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

                            relationship = fields.optString("relationship")
                            repordId = fields.optString("reportId")
                            insurance = _id //险种id
                        }

                        orderDetailsItemlists.add(OrderDetailsItem("业务单号", fields.optString("taskId")))
                        orderDetailsItemlists.add(OrderDetailsItem("所属区域",""))
                        orderDetailsItemlists.add(OrderDetailsItem("中介机构", fields.optString("IntermediaryInstitutions")))
                        orderDetailsItemlists.add(OrderDetailsItem("代理人姓名", TXManagerImpl.instance!!.getFullName()))
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
                    //todo 页面出错
                runOnUiThread {
                    showToastMsg(err)
                    Handler().postDelayed({ finish()},1000)

                }
            }

        })
    }


    var list1 = ArrayList<MultiItemEntity>()
    var baseQuickAdapter1: OrderExpandableItemAdapter1? = null
    fun initRecyclerview1() {

        recyclerview1.layoutManager = LinearLayoutManager(this)
        baseQuickAdapter1 = OrderExpandableItemAdapter1(list1!!)
        baseQuickAdapter1?.setOnItemClickListener { adapter, view, position ->
//            recyclerview1?.smoothScrollToPosition(position+3)
        }
        recyclerview1.adapter = baseQuickAdapter1

        baseQuickAdapter1?.setNewData(list1)
    }

    fun addProductData(requestSubOrderBean: RequestSubOrderBean){
        val levelItem1 = ProductLevelItem()
        levelItem1.insuranceName = requestSubOrderBean.insuranceName
        levelItem1.insuranceIsMain = requestSubOrderBean.insuranceIsMain
        levelItem1.addSubItem(requestSubOrderBean)
        baseQuickAdapter1?.addData(levelItem1)
    }

    override fun onConfirmClick(
        isRemote: Boolean,
        flowId: String,
        phone: String,
        taskId: String,
        membersArray: java.util.ArrayList<String>,
        isSelfInsurance: Boolean,
        recordType: String
    ) {
        InviteActivity.newInstance(
            this,isRemote,
            flowId,phone,taskId,membersArray,
            isSelfInsurance,recordType)
    }




}