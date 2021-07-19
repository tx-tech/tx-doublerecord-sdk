package com.txt.sl.ui.createorder

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.pickerview.builder.OptionsPickerBuilder
import com.common.widget.pickerview.listener.OnOptionsSelectListener
import com.common.widget.pickerview.view.OptionsPickerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.common.widget.base.BaseActivity
import com.txt.sl.R
import com.txt.sl.TXManagerImpl
import com.txt.sl.entity.bean.*
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.utils.*
import kotlinx.android.synthetic.main.tx_activity_new_order_sub.*
import org.json.JSONObject

class NewOrderSubActivity : BaseActivity() {
    companion object {
        private const val ARG_PARAM2 = "bean"
        public const val POSTION = "postion"

        fun newActivity(context: Activity) {
            val intent = Intent(context, NewOrderSubActivity::class.java)
            context.startActivityForResult(intent, 10001)
        }

        fun newActivity(context: Activity, postion: Int, bean: RequestSubOrderBean) {
            val intent = Intent(context, NewOrderSubActivity::class.java)
            intent.putExtra(ARG_PARAM2, bean)
            intent.putExtra(POSTION, postion)
            context.startActivityForResult(intent, 10001)
        }
    }


    override fun getLayoutId(): Int = R.layout.tx_activity_new_order_sub


    fun getProductData() {
        SystemHttpRequest.getInstance().getProductData(TXManagerImpl.instance!!.getTenantId(), object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                if (json!!.isEmpty()) {
                    ToastUtils.showShort("没有产品")
                    runOnUiThread {
                        finish()
                    }
                } else {
                    runOnUiThread {
                        val insurancesLists = json
                        if (insurancesLists.isEmpty()) {
                            ToastUtils.showShort("没有产品")
                            finish()
                        }
                        orderSubItemBeanList = Gson().fromJson<java.util.ArrayList<OrderSubItemBean>>(insurancesLists.toString(), object : TypeToken<java.util.ArrayList<OrderSubItemBean>>() {}.type)
                        if (orderSubItemBeanList?.isEmpty()!!) {
                            ToastUtils.showShort("没有产品")
                        }
                        //如果有值，说明是编辑按钮过来的
                        val serializableExtra = intent.getSerializableExtra(ARG_PARAM2)
                        if (null != serializableExtra) {
                            mPostion = intent.getIntExtra(POSTION, -1)
                            requestSubOrderBean = serializableExtra as RequestSubOrderBean
                            restoreData()
                        } else {
                            requestSubOrderBean = RequestSubOrderBean()
                        }



                        btn_commit.setOnClickListener {

                            requestSubOrderBean?.apply {
                                insurancePaymentDown = et_insurancePaymentDown.text.toString()
                                insurancePaymentPeriods = et_insurancePaymentPeriods.text.toString()
                                insurancePaymentPrice = et_insurancePaymentPrice.text.toString()
                                val insurancePaymentYearStr = et_insurancePaymentYear.text.toString()
                                insurancePaymentYear = if (insurancePaymentYearStr.isEmpty()) {
                                    -1
                                } else {
                                    insurancePaymentYearStr.toInt()
                                }

                            }
                            if (checkBean(requestSubOrderBean)) {
                                LogUtils.i(requestSubOrderBean.toString())
                                val putExtra = intent.putExtra("requestSubOrderBean", requestSubOrderBean)
                                if (mPostion != -1) {
                                    putExtra.putExtra(POSTION, mPostion)
                                }
                                setResult(12000, putExtra)
                                finish()
                            } else {
                                TxPopup.Builder(this@NewOrderSubActivity).asConfirm("提交", "信息不能为空！！！", "取消", "好的", OnConfirmListener { }, null, false).show()
                            }


                        }

                    }
                }

            }

            override fun onFail(err: String?, code: Int) {

            }

        })
    }

    var mDataList: ArrayList<OrderBean>? = null
    override fun initView() {
        super.initView()
        title = "补充产品信息"

    }

    var requestSubOrderBean: RequestSubOrderBean? = null
    var mPostion = -1
    override fun initData() {
        getProductData()
        val stringStr = GsonUtils.getJson(this@NewOrderSubActivity, "ordertypelist.json")
        val json = JSONObject(stringStr)
        val jsonarr = json.getJSONArray("data")
        TxLogUtils.i("${jsonarr}")
        mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(jsonarr.toString(), object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type)

        initInsuredDatePicker()
        initInsuredDatePicker1()
        initInsuredDatePicker2()
        initInsurances()
        initPolicyholdercertificatetypePicker()
    }

    private fun restoreData() {

        tv_insurance1.apply {
            text = if (requestSubOrderBean?.insuranceIsMain == 1) {
                "主险"
            } else {
                "附加险"
            }

            setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
        }

        tv_policyholdercertificatetype.apply {
            text = requestSubOrderBean?.insuranceName
            setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
        }
        val insurancePaymentMethodfilter = mDataList?.filter { it.name == "缴费频次" }

        val data = insurancePaymentMethodfilter?.get(0)
        val insurancePaymentMethodfilter1 = data?.options?.filter {
            it.key == requestSubOrderBean?.insurancePaymentMethod
        }

        val insurancetypeDatas = mDataList?.get(11)
        val filter = insurancetypeDatas?.options?.filter { it.key == requestSubOrderBean?.insuranceType }
        tv_insurancetype.apply {
            text = filter?.get(0)!!.name
            setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
        }

        filterProductDatas = filterProductDatas(isMainInt = requestSubOrderBean?.insuranceIsMain!!,
                type = requestSubOrderBean?.insuranceType!!
        )
        if (filterProductDatas!!.size > 0) {
            var dataList = ArrayList<String>()
            filterProductDatas?.forEach {
                dataList.add(it.name)
            }
            policyholdercertificatetypeOptions?.setPicker(dataList)
        }

        et_insurancePaymentDown.setText(requestSubOrderBean?.insurancePaymentDown)
        tv_insurancePaymentMethod.apply {
            text = insurancePaymentMethodfilter1?.get(0)!!.name
            setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
        }
        et_insurancePaymentPeriods.setText(requestSubOrderBean?.insurancePaymentPeriods)
        et_insurancePaymentPrice.setText(requestSubOrderBean?.insurancePaymentPrice)

        val insurancePaymentYearUnitfilter = mDataList?.filter { it.name == "保险期间单位" }

        val data1 = insurancePaymentYearUnitfilter?.get(0)
        val filter2 = data1?.options?.filter {
            it.key == requestSubOrderBean?.insurancePaymentYearUnit
        }
        val insurancePaymentYearUnitName = filter2?.get(0)?.name
        tv_insurancePaymentYearUnit.apply {
            text = insurancePaymentYearUnitName
            setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
        }
        if (insurancePaymentYearUnitName == "终身") {
            ll_insurancePaymentYear.visibility = View.GONE
        } else {
            ll_insurancePaymentYear.visibility = View.VISIBLE
            et_insurancePaymentYear.setText("" + requestSubOrderBean?.insurancePaymentYear)
        }


    }

    private fun checkBean(requestOrderBean: RequestSubOrderBean?): Boolean {
        var canCheck = false
        requestOrderBean?.apply {
            canCheck = insuranceIsMain != -1 &&
                    !insuranceType.isEmpty() && !insuranceName.isEmpty()
                    && !insurancePaymentDown.isEmpty() && !insurancePaymentMethod.isEmpty()
                    && !insurancePaymentPeriods.isEmpty() && !insurancePaymentPrice.isEmpty()
                    && !insuranceCode.isEmpty()

        }
        if (!canCheck) {
            return canCheck
        }
        //如果为终身 不需要判断年限
        if (requestOrderBean!!.insurancePaymentYearUnit.isEmpty()) {
            return false
        } else {
            if (requestOrderBean!!.insurancePaymentYearUnit == "LifeLong") {

                return canCheck
            } else {

                return requestOrderBean!!.insurancePaymentYear != -1
            }
        }

        LogUtils.i("requestOrderBean", requestOrderBean.toString())

    }

    var insuredDateOptions: OptionsPickerView<String>? = null
    var filterProductDatas: ArrayList<OrderSubItemBean>? = null
    fun initInsuredDatePicker() {
        val filter = mDataList?.filter { it.name == "主附险类型" }
        val data = filter?.get(0)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }

        insuredDateOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insurance1.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
            }
            requestSubOrderBean?.insuranceName = ""
            requestSubOrderBean?.insuranceCode = ""
            tv_policyholdercertificatetype.apply {
                text = "请选择"
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_A5A7AC))
            }
            val key = data?.options?.get(options1)?.key!!
            requestSubOrderBean?.insuranceIsMain = key.toInt()

            filterProductDatas = filterProductDatas(isMainInt = requestSubOrderBean?.insuranceIsMain!!,
                    type = requestSubOrderBean?.insuranceType!!
            )
            if (filterProductDatas!!.size > 0) {
                var dataList = ArrayList<String>()
                filterProductDatas?.forEach {
                    dataList.add(it.name)
                }
                policyholdercertificatetypeOptions?.setPicker(dataList)
            }

        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.gray_text))
                .build()

        insuredDateOptions?.setPicker(dataList)

    }

    var policyholdercertificatetypeOptions: OptionsPickerView<String>? = null
    fun initPolicyholdercertificatetypePicker() {

        policyholdercertificatetypeOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_policyholdercertificatetype.apply {
                text = filterProductDatas?.get(options1)!!.name
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
            }
            requestSubOrderBean?.insuranceName = filterProductDatas?.get(options1)!!.name
            requestSubOrderBean?.insuranceCode = filterProductDatas?.get(options1)!!.code
        })
                .setTitleText("产品名称")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.gray_text))
                .build()


        tv_policyholdercertificatetype.setOnClickListener {
            if (requestSubOrderBean?.insuranceName!!.isEmpty() && requestSubOrderBean?.insuranceType!!.isEmpty()) {
                ToastUtils.showShort("请选择！！！")
                return@setOnClickListener
            }
            hideInput()
            policyholdercertificatetypeOptions?.show()
        }
    }


    fun filterProductDatas(isMainInt: Int = -1, type: String = ""): ArrayList<OrderSubItemBean> {
        //根据 isMain 和type 筛选 产品列表
        LogUtils.i("isMainInt--$isMainInt----type----$type")
        var needProductList: ArrayList<OrderSubItemBean>? = null
        if (isMainInt != -1 && type.isNotEmpty()) {
            var isMain = isMainInt == 1 // isMain 1 是主险
            needProductList = orderSubItemBeanList?.filter { it.isIsMain == isMain && it.type == type } as ArrayList<OrderSubItemBean>?
        } else {
            needProductList = ArrayList<OrderSubItemBean>()
        }
        LogUtils.i("needProductList--${needProductList}")
        return needProductList!!
    }

    var insuredDateOptions1: OptionsPickerView<String>? = null
    fun initInsuredDatePicker1() {
        val filter = mDataList?.filter { it.name == "缴费频次" }
        val data = filter?.get(0)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }


        insuredDateOptions1 = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insurancePaymentMethod.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
            }
            requestSubOrderBean?.insurancePaymentMethod = data?.options?.get(options1)?.key!!
        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.gray_text))
                .build()

        insuredDateOptions1?.setPicker(dataList)

        tv_insurancePaymentMethod.setOnClickListener {
            hideInput()
            insuredDateOptions1?.show()
        }
    }

    var insuredDateOptions2: OptionsPickerView<String>? = null
    fun initInsuredDatePicker2() {
        val filter = mDataList?.filter { it.name == "保险期间单位" }
        val data = filter?.get(0)
        var dataList = ArrayList<String>()
        data?.options?.forEach {
            dataList.add(it.name)
        }


        insuredDateOptions2 = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insurancePaymentYearUnit.apply {
                text = dataList[options1]
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
            }
            requestSubOrderBean?.insurancePaymentYearUnit = data?.options?.get(options1)?.key!!
            ll_insurancePaymentYear.visibility = if (requestSubOrderBean?.insurancePaymentYearUnit != "LifeLong") {
                View.VISIBLE
            } else {
                View.GONE
            }


        })
                .setTitleText(data?.name)
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.gray_text))
                .build()

        insuredDateOptions2?.setPicker(dataList)

        tv_insurancePaymentYearUnit.setOnClickListener {
            hideInput()
            insuredDateOptions2?.show()
        }
    }

    var orderSubItemBeanList: ArrayList<OrderSubItemBean>? = null
    private fun initInsurances() {

        var dataList = ArrayList<String>()
        val data = mDataList?.get(11)
        data?.options?.forEach {
            dataList.add(it.name)
        }

        var payTypeOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            tv_insurancetype.apply {
                text = dataList!![options1]
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_000000))
            }
            requestSubOrderBean?.insuranceName = ""
            requestSubOrderBean?.insuranceCode = ""
            tv_policyholdercertificatetype.apply {
                text = "请选择"
                setTextColor(ContextCompat.getColor(this@NewOrderSubActivity, R.color.color_A5A7AC))
            }

            requestSubOrderBean?.insuranceType = data?.options!![options1].key
            filterProductDatas = filterProductDatas(isMainInt = requestSubOrderBean?.insuranceIsMain!!,
                    type = requestSubOrderBean?.insuranceType!!
            )
            if (filterProductDatas!!.size > 0) {
                var dataList = ArrayList<String>()
                filterProductDatas?.forEach {
                    dataList.add(it.name)
                }
                policyholdercertificatetypeOptions?.setPicker(dataList)
            }

        })
                .setTitleText("产品类型")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .setCancelColor(ContextCompat.getColor(this, R.color.gray_text))
                .build<String>()


        payTypeOptions?.setPicker(dataList)

        tv_insurancetype.setOnClickListener {
            hideInput()
            payTypeOptions?.show()
        }
    }

    fun txClick(v: View) {
        when (v.id) {
            R.id.tv_insurance1 -> {
                hideInput()
                insuredDateOptions?.show()
            }
            else -> {
            }
        }
    }

}