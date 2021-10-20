package com.txt.sl.ui.order

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import com.txt.sl.R
import com.txt.sl.entity.bean.OrderRecordBean
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.video.VideoPlayActivity
import com.txt.sl.ui.adpter.QualityInspectionItemAdapter
import com.txt.sl.base.BaseLazyViewPagerFragment
import kotlinx.android.synthetic.main.tx_fragment_check_list.*
import org.json.JSONObject

class CheckFragment : BaseLazyViewPagerFragment() {

    var mFlowId = ""
    var mAdapter: QualityInspectionItemAdapter? = null
    var list = ArrayList<OrderRecordBean>()

    override fun initData() {
        if (arguments != null) {
            mFlowId = arguments!!.getString(CheckFragment.ARG_PARAM2,"")
        }
        SystemHttpRequest.getInstance().getRecordTask(mFlowId, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                _mActivity?.runOnUiThread {
                    list.clear()
                    val jsonOb = JSONObject(json)
                    val checkArray = jsonOb.getJSONArray("check")
                    for (index in 0 until checkArray.length()) {
                        val jsonObject = checkArray.getJSONObject(index)
                        val orderRecordBean = OrderRecordBean()
                        list.add(orderRecordBean)
                        orderRecordBean.videoUrl = jsonObject.optString("videoUrl")
                        val statusStr = jsonObject.optString("status")
                        orderRecordBean.uploadedTime =jsonObject.optString("uploadedTime","")
                        val failTypeSB = StringBuffer("")
                        val failReasonSB = StringBuffer("")
                        if (statusStr == "Accepted" || statusStr == "Completed") {

                        }else{
                            val processArray = jsonObject.getJSONArray("process")

                            for (processInd in 0 until processArray.length() ){
                                val jsonObject1 = processArray.getJSONObject(processInd)
                                val stepsArray = jsonObject1.getJSONArray("steps")
                                for (stepIndex in 0 until  stepsArray.length()){
                                    val stepObject = stepsArray.getJSONObject(stepIndex)

                                    val boolean = stepObject.optBoolean("check",true)
                                    if (!boolean){
                                        val failTypeStr = stepObject.optString("failType","暂无")
                                        val failReasonStr = stepObject.optString("failReason","暂无")
                                        failTypeSB.append("$failTypeStr ")
                                        failReasonSB.append("$failReasonStr ")
                                    }

                                }
                            }
                        }
                        orderRecordBean.apply {
                            status = statusStr
                            failType = failTypeSB.toString()
                            failReason = failReasonSB.toString()
                        }

                    }

                    mAdapter?.setNewData(list)
                }
            }

            override fun onFail(err: String?, code: Int) {

            }
        })
    }


    fun initRecyclerView() {
        mAdapter = QualityInspectionItemAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        mAdapter?.apply {
            bindToRecyclerView(recyclerView)
            openLoadAnimation()
            val inflate = layoutInflater.inflate(R.layout.tx_adapter_list_pic_empty, null)
            val tv = inflate.findViewById<TextView>(R.id.tx_emptycontent)
            tv.text="暂无历史记录"
            setEmptyView(inflate)
        }
        recyclerView.adapter = mAdapter
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val orderRecordBean = mAdapter!!.data.get(position)

            VideoPlayActivity.Builder().setVideoSource(orderRecordBean.videoUrl!!,true).start(_mActivity)

        }
    }


    override fun getLayoutId(): Int {
        return R.layout.tx_fragment_check_list
    }

    override fun getTitleBarId(): Int {
        return 0
    }


    override fun initView() {
        initRecyclerView()
    }


    companion object {
        private const val ARG_PARAM1 = "applyStatusParams"
        private const val ARG_PARAM2 = "bean"

        @JvmStatic
        fun newInstance(applyStatusParams: String?): CheckFragment {
            val fragment = CheckFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, applyStatusParams)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(applyStatusParams: String, flowId: String): CheckFragment {
            val fragment = CheckFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, applyStatusParams)
            args.putString(ARG_PARAM2, flowId)
            fragment.arguments = args
            return fragment
        }
    }
}