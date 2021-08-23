package com.txt.sl.ui.createorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.impl.LoadingPopupView
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.pickerview.builder.OptionsPickerBuilder
import com.common.widget.pickerview.listener.OnOptionsSelectListener
import com.common.widget.pickerview.view.OptionsPickerView
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txt.sl.R
import com.txt.sl.config.TXManagerImpl
import com.txt.sl.TXSdk
import com.txt.sl.entity.bean.*
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderExpandableItemAdapter
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_new_order.*
import kotlinx.android.synthetic.main.tx_activity_new_order_item1.*
import kotlinx.android.synthetic.main.tx_activity_new_order_item2.*
import org.json.JSONArray
import org.json.JSONObject

class NewOrderActivity : BaseActivity(), View.OnClickListener {

    override fun getLayoutId(): Int = R.layout.tx_activity_new_order

    companion object {
        private const val ARG_PARAM2 = "bean"

        fun newActivity(context: Activity) {
            val intent = Intent(context, NewOrderActivity::class.java)
            context.startActivityForResult(intent, 10001)
        }

        fun newActivity(context: Context, bean: WorkItemBean) {
            val intent = Intent(context, NewOrderActivity::class.java)
            intent.putExtra(ARG_PARAM2, bean)
            context.startActivity(intent)
        }
    }

    override fun onLeftClick(view: View?) {
        hideInput()
        showDialog()
    }
    private fun showDialog(){
        TxPopup.Builder(this@NewOrderActivity)
                .asConfirm("退出",
                        "确认退出新建页面？",
                        "取消",
                        "是的",
                        { finishPage() },
                        null,
                        false).show()
    }

    var workerItemTypeBean: WorkItemBean? = null
    var isOrderFrom = false
    override fun initView() {
        super.initView()
        requestOrderBean = RequestOrderBean()
        val bean = intent.getSerializableExtra(ARG_PARAM2)
        if (bean != null) {
            isOrderFrom = true
            workerItemTypeBean = bean as WorkItemBean
            insuranceId = workerItemTypeBean?.insurance!!
            workerItemTypeBean?.apply {
//                et_insuredName.setText(insuredName)
//                et_insuredIdCard.setText(insuredIdCard)
//                et_insuredPhone.setText(insuredPhone)
//                et_insuredMoney.setText(insuredMoney)
//
//                et_insurantPhone.setText(insurantPhone)
//                et_insurantIdCard.setText(insurantIdCard)
//                et_insurantName.setText(insurantName)

//                et_reportId.setText(repordId)


//                requestOrderBean?.insuredDate = insuredDate
//
//                requestOrderBean?.relationship = relationship
//                requestOrderBean?.payType = payType
            }


        } else {
            isOrderFrom = false
        }


        title = if (isOrderFrom) {
            "编辑双录任务"
        } else {
            "新建双录任务"
        }

        btn_commit.setOnClickListener(CheckDoubleClickListener {
            commit()
        })
        tv_relationship.setOnClickListener(this)
        tv_taskFrom.setOnClickListener(this)
        et_insuranceAllPaymentDown.setOnClickListener(this)
        tv_addproduct.setOnClickListener(this)

        tv_fullName.text = TXManagerImpl.instance!!.getFullName()

        initRecyclerview()
//        getInsuranceData()
    }

    private fun  getInsuranceData(){
        SystemHttpRequest.getInstance().getInsuranceData(TXManagerImpl.instance?.getAgentId(), object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                val resultObject = JSONObject(json)
                val insurancesLists = resultObject.optString("insurances")
                if (insurancesLists.isEmpty()) {
                    showToastMsg("返回参数为空")
                }else{
                    TxSPUtils.put(TXSdk.getInstance().application, SPConstant.INSURANCES_LISTS, insurancesLists)
                }
            }

            override fun onFail(err: String?, code: Int) {

            }

        })

    }


    private fun commit() {
        if (baseQuickAdapter?.getData() == null || baseQuickAdapter?.getData()?.size == 0) {
            showToastMsg("产品没有选择")
            return
        }
        val taskIdStr = et_taskId.text.toString()
        val policyholderNameStr = et_policyholderName.text.toString()
        val policyholderCertificateNoStr = et_policyholderCertificateNo.text.toString()
        val policyholderAgeStr = et_policyholderAge.text.toString()
        if (policyholderAgeStr.isEmpty()) {
            showToastMsg("投保人年龄不能为空")
            return
        }
        val policyholderPhoneStr = et_policyholderPhone.text.toString()
        val insuredNameStr = et_insurancePaymentDown.text.toString()
        val insuredCertificateNoStr = et_insuredCertificateNo.text.toString()
        val insuredAgeStr = et_insuredAge.text.toString()
        if (insuredAgeStr.isEmpty()) {
            showToastMsg("被保人年龄不能为空")
            return
        }
        val insuredPhoneStr = et_insuredPhone.text.toString()
        val insuranceAllPaymentDownStr = et_insuranceAllPaymentDown.text.toString()
        val agentCertificateNoStr = et_agentCertificateNo.text.toString()

        requestOrderBean?.apply {
            agentCertificateNo = agentCertificateNoStr
            taskId = taskIdStr
            policyholderName = policyholderNameStr
            policyholderCertificateNo = policyholderCertificateNoStr
            policyholderAge = policyholderAgeStr.toInt()
            policyholderPhone = policyholderPhoneStr
            insuredName = insuredNameStr
            insuredCertificateNo = insuredCertificateNoStr
            insuredAge = insuredAgeStr.toInt()
            insuredPhone = insuredPhoneStr
            insuranceAllPaymentDown = insuranceAllPaymentDownStr
            institutions = arrayListOf("60efdba39418525ea664d9b9","60efdba39418525ea664da1d","60efdba49418525ea664e146")
            IntermediaryInstitutions = "我是李垒我最帅机构"
            agentCode = "12313"
        }

//        val insurancesLists = TxSPUtils.get(this, SPConstant.INSURANCES_LISTS, "") as String
//        if (insurancesLists.isEmpty()) {
//            showToastMsg("该账号没有配置险种信息")
//            return
//        }
//        val insurancesListsJsonArray = JSONArray(insurancesLists)
//        var mSelectBeanList = ArrayList<SelectBean>()
//        var mSelectBeanStrList = ArrayList<String>()
//        if (insurancesListsJsonArray.length() == 0) {
//            showToastMsg("该账号没有配置险种信息")
//            return
//        } else {
//            for (index in 0 until insurancesListsJsonArray.length()) {
//                val jsonObject = insurancesListsJsonArray.getJSONObject(index)
//                mSelectBeanList?.add(SelectBean(
//                        jsonObject.getString("_id"),
//                        jsonObject.getString("name")
//                ))
//                mSelectBeanStrList?.add(jsonObject.getString("name"))
//            }
//        }
        val checkBean = checkBean(requestOrderBean)

        if (!checkBean) {

            TxPopup.Builder(this).asConfirm("提交", "信息不能为空！！！", "取消", "好的", object : OnConfirmListener {
                override fun onConfirm() {

                }

            }, null, false).show()
        } else {
            baseQuickAdapter?.getData()!!.forEach {
                if (it is ProductLevelItem) {
                    val subItem = it.getSubItem(0)
                    mRequestSubOrderBeanList.add(subItem)
                }
            }

            mRequestSubOrderBeanList.forEach {
                requestOrderBean?.insuranceIsMain?.add(it.insuranceIsMain)
                requestOrderBean?.insuranceType?.add(it.insuranceType)
                requestOrderBean?.insuranceName?.add(it.insuranceName)
                requestOrderBean?.insurancePaymentDown?.add(it.insurancePaymentDown)
                requestOrderBean?.insurancePaymentMethod?.add(it.insurancePaymentMethod)
                requestOrderBean?.insurancePaymentPeriods?.add(it.insurancePaymentPeriods)
                requestOrderBean?.insurancePaymentPrice?.add(it.insurancePaymentPrice)
                requestOrderBean?.insurancePaymentYearUnit?.add(it.insurancePaymentYearUnit)
                requestOrderBean?.insurancePaymentYear?.add(it.insurancePaymentYear)
                requestOrderBean?.insuranceCode?.add(it.insuranceCode)
                requestOrderBean?.insuranceCompany?.add(it.insuranceCompany)
                requestOrderBean?.ensureTheRenewal?.add(it.ensureTheRenewal)

            }
            //     JSONArray insuranceCompanyArray = new JSONArray();
            //            insuranceCompanyArray.put("60ed47deda28f16c75966b0d");
            //            JSONArray ensureTheRenewalArray = new JSONArray();
            //            ensureTheRenewalArray.put(false);
            //            fieldsJsonObject.put("insuranceCompany",insuranceCompanyArray);
            //            fieldsJsonObject.put( "ensureTheRenewal",ensureTheRenewalArray);
            SystemHttpRequest.getInstance().update(
                    "",
                    TXManagerImpl.instance!!.getAgentId(),
                    requestOrderBean, object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    runOnUiThread {
                        hideInput()
                        ToastUtils.showLong("提交成功,可从投保中的双录发起！！！")
                        finishPage()
                    }
                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        hideInput()
                        TxPopup.Builder(this@NewOrderActivity).asConfirm("提交", err, "", "好的", object : OnConfirmListener {
                            override fun onConfirm() {

                            }

                        }, null, true).show()


                    }
                }

            }
            )
        }


    }

    var insuranceId = ""

    var mRequestSubOrderBeanList: ArrayList<RequestSubOrderBean> = ArrayList<RequestSubOrderBean>()
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_relationship -> {
                hideInput()
                relationshipOptions?.show()
            }
            R.id.tv_taskFrom -> {

            }

            R.id.et_insuranceAllPaymentDown -> {
                hideInput()
                saleFromOptions?.show()
            }

            R.id.tv_addproduct -> {
                NewOrderSubActivity.newActivity(this)
            }
            else -> {
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            12000 -> {
                val mRequestSubOrderBean = data?.extras?.getSerializable("requestSubOrderBean") as RequestSubOrderBean

                val postion = data?.extras?.getInt(NewOrderSubActivity.POSTION, -1)
                if (-1 != postion) {
                    //只需要更新数据
                    val mProductLevelItem = baseQuickAdapter?.getData()?.get(postion!! - 1) as ProductLevelItem
                    mProductLevelItem.insuranceName = mRequestSubOrderBean.insuranceName
                    mProductLevelItem.insuranceIsMain = mRequestSubOrderBean.insuranceIsMain
                    mProductLevelItem.subItems[0] = mRequestSubOrderBean
                    baseQuickAdapter?.setData(postion!! - 1, mProductLevelItem)
                    baseQuickAdapter?.setData(postion!!, mRequestSubOrderBean)
                } else {
                    //添加数据
                    val levelItem1 = ProductLevelItem()
                    levelItem1.insuranceName = mRequestSubOrderBean.insuranceName
                    levelItem1.insuranceIsMain = mRequestSubOrderBean.insuranceIsMain
                    levelItem1.addSubItem(mRequestSubOrderBean)
                    baseQuickAdapter?.addData(levelItem1)
                }

            }
            else -> {
            }
        }
    }


    override fun onBackPressed() {
        showDialog()
    }

    private fun finishPage() {
        setResult(10000)
        finish()
    }

    private fun checkBean(requestOrderBean: RequestOrderBean?): Boolean {
        var canCheck = false
        requestOrderBean?.apply {
            canCheck = !taskId.isEmpty() &&
                    !policyholderName.isEmpty() && !policyholderCertificateType.isEmpty()
                    && !policyholderCertificateNo.isEmpty() && policyholderAge != -1
                    && !policyholderGender.isEmpty() && !policyholderPhone.isEmpty()
                    && !relationship.isEmpty() && !insuredName.isEmpty() && !insuredCertificateType.isEmpty()
                    && !insuredCertificateNo.isEmpty() && insuredAge != -1
                    && !insuredGender.isEmpty() && !insuredPhone.isEmpty() && !saleFrom.isEmpty()
                    && !taskFrom.isEmpty() && !insuranceAllPaymentDown.isEmpty()
        }
        LogUtils.i("requestOrderBean", requestOrderBean.toString())
        return canCheck
    }


    var mDataList: ArrayList<OrderBean>? = null

    var requestOrderBean: RequestOrderBean? = null
    override fun initData() {

        val stringStr = GsonUtils.getJson(this, "ordertypelist.json")
        val json = JSONObject(stringStr)
        val jsonarr = json.getJSONArray("data")

        TxLogUtils.i("${jsonarr}")
        mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(jsonarr.toString(), object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type)
        initRelationshipPicker()

        initSaleFromPicker()
        initTaskFromPicker()

        initPolicyholderCertificateTypePicker()
        initPolicyholderGender()
        initInsuredGenderPicker()
        initInsuredCertificateTypePicker()
        initPolicyholderCertificateTypePicker1()
    }


    var relationshipOptions: OptionsPickerView<String>? = null
    fun initRelationshipPicker() {

        val data = mDataList?.get(3)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }


        relationshipOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_relationship.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            if ("本人" == dataList[options1]) {
                et_insurancePaymentDown.setText(et_policyholderName.text)
                et_insuredCertificateNo.setText(et_policyholderCertificateNo.text)
                et_insuredAge.setText(et_policyholderAge.text)
                et_insuredPhone.setText(et_policyholderPhone.text)
                requestOrderBean?.insuredCertificateType = requestOrderBean?.policyholderCertificateType!!
                requestOrderBean?.insuredGender = requestOrderBean?.policyholderGender!!

                tv_insuredCertificateType.apply {
                    text = tv_policyholderCertificateType.text
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
                }

                tv_insuredGender.apply {
                    text = tv_policyholderGender.text
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
                }
            } else {
                et_insurancePaymentDown.setText("")
                et_insuredCertificateNo.setText("")
                et_insuredAge.setText("")
                et_insuredPhone.setText("")

                requestOrderBean?.insuredCertificateType = ""
                requestOrderBean?.insuredGender = ""
                tv_insuredCertificateType.apply {
                    text = "请选择"
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_A5A7AC))
                }
                tv_insuredCertificateType.apply {
                    text = "请选择"
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_A5A7AC))
                }
                tv_insuredGender.apply {
                    text = "请选择"
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_A5A7AC))
                }
            }
            requestOrderBean?.relationship = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        relationshipOptions?.setPicker(dataList)

    }

    var policyholderCertificateTypeOptions1: OptionsPickerView<String>? = null

    //投保人证件类型
    fun initPolicyholderCertificateTypePicker1() {
        val data = mDataList?.get(1)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }
        policyholderCertificateTypeOptions1 = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_agentCertificateType.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.agentCertificateType = data?.options?.get(options1)?.key!!


        })
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        policyholderCertificateTypeOptions1?.setPicker(dataList)
        tv_agentCertificateType.setOnClickListener {
            hideInput()
            policyholderCertificateTypeOptions?.setTitleText("代理人证件类型")
            policyholderCertificateTypeOptions1?.show()
        }


    }


    var policyholderCertificateTypeOptions: OptionsPickerView<String>? = null
    var insuredPerson = true //被保人

    //投保人证件类型
    fun initPolicyholderCertificateTypePicker() {
        val data = mDataList?.get(1)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }
        policyholderCertificateTypeOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            if (insuredPerson) {
                tv_insuredCertificateType.apply {
                    text = dataList[options1]
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
                }
                requestOrderBean?.insuredCertificateType = data?.options?.get(options1)?.key!!
            } else {
                tv_policyholderCertificateType.apply {
                    text = dataList[options1]
                    setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
                }
                requestOrderBean?.policyholderCertificateType = data?.options?.get(options1)?.key!!
            }


        })
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        policyholderCertificateTypeOptions?.setPicker(dataList)
        tv_policyholderCertificateType.setOnClickListener {
            hideInput()
            policyholderCertificateTypeOptions?.setTitleText("投保人证件类型")
            insuredPerson = false
            policyholderCertificateTypeOptions?.show()
        }
        tv_insuredCertificateType.setOnClickListener {
            hideInput()
            policyholderCertificateTypeOptions?.setTitleText("被保人证件类型")
            insuredPerson = true
            policyholderCertificateTypeOptions?.show()
        }

    }

    var saleFromOptions: OptionsPickerView<String>? = null
    fun initSaleFromPicker() {
        val data = mDataList?.get(6)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        saleFromOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_saleFrom.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.saleFrom = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        saleFromOptions?.setPicker(dataList)
        tv_saleFrom.setOnClickListener {
            hideInput()
            saleFromOptions?.show()
        }
    }

    var taskFromOptions: OptionsPickerView<String>? = null
    fun initTaskFromPicker() {
        val data = mDataList?.get(7)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        taskFromOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_taskFrom.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.taskFrom = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        taskFromOptions?.setPicker(dataList)
        tv_taskFrom.setOnClickListener {
            hideInput()
            taskFromOptions?.show()
        }
    }


    var policyholderGenderOptions: OptionsPickerView<String>? = null
    fun initPolicyholderGender() {
        val data = mDataList?.get(2)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        policyholderGenderOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_policyholderGender.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.policyholderGender = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        policyholderGenderOptions?.setPicker(dataList)
        tv_policyholderGender.setOnClickListener {
            hideInput()
            policyholderGenderOptions?.show()
        }
    }

    var insuredGenderOptions: OptionsPickerView<String>? = null
    fun initInsuredGenderPicker() {
        val data = mDataList?.get(5)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        insuredGenderOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insuredGender.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.insuredGender = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        insuredGenderOptions?.setPicker(dataList)
        tv_insuredGender.setOnClickListener {
            hideInput()
            insuredGenderOptions?.show()
        }


    }

    var insuredCertificateTypeOptions: OptionsPickerView<String>? = null
    fun initInsuredCertificateTypePicker() {
        val data = mDataList?.get(4)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        insuredCertificateTypeOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insuredCertificateType.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderActivity, R.color.tx_txcolor_000000))
            }
            requestOrderBean?.insuredCertificateType = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.tx_txgray_text))
                .build()



        insuredCertificateTypeOptions?.setPicker(dataList)
        tv_insuredCertificateType.setOnClickListener {
            hideInput()
            insuredCertificateTypeOptions?.show()
        }


    }


    var list = ArrayList<MultiItemEntity>()
    var baseQuickAdapter: OrderExpandableItemAdapter? = null
    fun initRecyclerview() {

        recyclerview.layoutManager = LinearLayoutManager(this)
        baseQuickAdapter = OrderExpandableItemAdapter(list!!)
        recyclerview.adapter = baseQuickAdapter

        baseQuickAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_edit -> {
                    val mEntity = baseQuickAdapter?.data?.get(position)
                    val productLevelItem = mEntity as RequestSubOrderBean
                    //编辑数据

                    NewOrderSubActivity.newActivity(this, position, productLevelItem)

                }
                R.id.tv_delete -> {
                    baseQuickAdapter?.remove(position - 1)
                }
                else -> {
                }
            }
        }
        baseQuickAdapter?.setNewData(list)
    }


    fun clearFocus() {
//        et_name.clearFocus()
//        et_emergencyphone.clearFocus()
//        et_phone.clearFocus()
//        tv_unit.clearFocus()
//        et_income.clearFocus()
//        et_unit.clearFocus()
    }

    fun hideSkb() {
        hideInput()
        clearFocus()

    }

}
