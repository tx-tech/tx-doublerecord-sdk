package com.txt.sl.ui.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.txt.sl.R
import com.txt.sl.entity.bean.WorkItemBean
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.video.VideoPlayActivity
import com.txt.sl.ui.adpter.OrderListNodeAdapter
import com.txt.sl.ui.adpter.OrderListNodeAdapter.*
import com.txt.sl.ui.adpter.WorkerItemTypeBean
import com.txt.sl.ui.dialog.CheckRemoteDialog
import com.txt.sl.ui.invite.InviteActivity
import com.txt.sl.ui.order.OrderActivity
import com.txt.sl.ui.order.OrderDetailsActivity
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.ToastUtils
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_fragment_recycler_list_no_toolbar.*
import org.json.JSONObject

class HomeItemListFragment : BaseLazyViewPagerFragment(), CheckRemoteDialog.OnRemoteClickListener {

    private var mLoadingView: LoadingView? = null
    private var applyStatusParams: String? = null
    var api: IWXAPI? = null

    override fun initData() {
        if (arguments != null) {
            applyStatusParams = arguments!!.getString(ARG_PARAM1)
        }
        refreshData()
    }

    override fun getLayoutId(): Int {
        return R.layout.tx_fragment_recycler_list_no_toolbar
    }

    override fun getTitleBarId(): Int {
        return 0
    }


    var mAdapter = OrderListNodeAdapter(null)


    val mDataList = ArrayList<MultiItemEntity>()
    public var customDialog: CheckRemoteDialog? = null
    public fun showDialog() {

        customDialog?.show()
    }


    override fun initView() {

        customDialog = CheckRemoteDialog(_mActivity)
        customDialog?.setOnRemoteClickListener(this)
        mLoadingView = LoadingView(context, "发起录制...", LoadingView.SHOWLOADING)

        swipeRefreshLayout.setOnRefreshListener { refreshData() }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_details -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderActivity.newActivity(_mActivity, bean.workItemBean.flowId)
                }
                R.id.ll_common -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderDetailsActivity.newActivity(_mActivity, bean)
                }
                R.id.tv_item1_sl, R.id.tv_replay -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    customDialog?.setData(
                        bean.workItemBean
                    )
                    showDialog()
                }
                R.id.tv_unupload_play -> { //播放本地视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    val screenRecordStr =
                        TxSPUtils.get(_mActivity, bean.workItemBean.flowId, "") as String
                    LogUtils.i("screenRecordStr---$screenRecordStr")
                    if (!TextUtils.isEmpty(screenRecordStr)) {
                        val jsonObject = JSONObject(screenRecordStr)
                        val pathFile = jsonObject.getString("path")
                        VideoPlayActivity.Builder().setVideoSource(pathFile!!, false)
                            .start(_mActivity)
                    } else {
                        ToastUtils.showShort("没有录屏文件")
                    }

                }
                R.id.tv_playremotevideo -> { //播放远端视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    VideoPlayActivity.Builder().setVideoSource(bean.workItemBean.recordUrl!!, true)
                        .start(_mActivity)
                }
                R.id.tv_item2_play -> { //上传视频
                    val homeActivity = _mActivity as HomeActivity
                    val bean = mDataList[position] as WorkerItemTypeBean
                    homeActivity.upload(bean.workItemBean.flowId)

                }
                else -> {
                }
            }

        }


        recyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter.apply {
            bindToRecyclerView(recyclerView)
            openLoadAnimation()
            setEmptyView(R.layout.tx_adapter_list_pic_empty, recyclerView)
        }
        recyclerView.adapter = mAdapter

    }

    fun refreshData() {
        LogUtils.i("applyStatusParams", applyStatusParams!!)
        tv_unupload.visibility = if (applyStatusParams.equals("UnUploaded")) {
            View.VISIBLE
        } else {
            View.GONE
        }

        SystemHttpRequest.getInstance()
            .list(applyStatusParams, object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    val jsonOb = JSONObject(json)
                    val jsonArray = jsonOb.getJSONArray("list")
                    val arrayList = ArrayList<WorkItemBean>()
                    for (index in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(index)
                        val policyholderObject = jsonObject.optJSONObject("policyholder")
                        val insuredObject = jsonObject.optJSONObject("insured")
                        val insurancesObject = jsonObject.getJSONArray("insurances")
                        val fieldsObject = jsonObject.optJSONObject("fields")
                        val stringBuffer = StringBuffer("")
                        for (index in 0 until insurancesObject.length()) {
                            val jsonObject1 = insurancesObject.getJSONObject(index)
                            val infoObject = jsonObject1.getJSONObject("info")
                            val isMain = infoObject.getString("isMain")
                            if ("1" == isMain) {
                                stringBuffer.append(infoObject.getString("name") + " ")
                            }
                        }
                        var list = ArrayList<String>()
                        val optJSONArray = jsonObject.optJSONArray("membersArray")
                        if (null != optJSONArray && optJSONArray.length() > 0) {
                            for (index in 0 until optJSONArray.length()) {
                                list.add(optJSONArray.getString(index))
                            }
                        }

                        arrayList.add(WorkItemBean().apply {
                            flowId = jsonObject.getString("flowId")
                            recordUrl = jsonObject.optString("videoUrl")
                            membersArray = list
                            insurantName = policyholderObject.optString("name")
                            insurantPhone = policyholderObject.optString("phone")
                            insuredName = insuredObject.optString("name")
                            utime = jsonObject.optString("utime")
                            status = jsonObject.optString("status")
                            taskId = jsonObject.optString("taskId")
                            isIsRemote = jsonObject.optBoolean("isRemote")
                            isSelfInsurance = jsonObject.optBoolean("selfInsurance") //是否自保件
                            insuranceName = stringBuffer.toString()
                            relationship = jsonObject.optString("relationship")
                            policyholderUrl = fieldsObject.optString("policyholderUrl")
                            insuranceUrl = fieldsObject.optString("insuranceUrl")
                            recordingMethod =  jsonObject.optString("recordingMethod")
                        })

                    }

                    _mActivity?.runOnUiThread {
                        mDataList.clear()
                        swipeRefreshLayout?.isRefreshing = false

                        arrayList.forEach {
                            val workerItemTypeBean = WorkerItemTypeBean(it)
                            workerItemTypeBean.itemType = when (it.status) {

                                "Refused" -> {
                                    TYPE_Refused
                                }
                                "UnUploaded" -> {
                                    TYPE_UnUploaded
                                }
                                "UnChecked" -> {
                                    TYPE_UnChecked
                                }
                                "Completed", "Accepted" -> {
                                    TYPE_Completed
                                }
                                else -> {
                                    TYPE_UnRecorded
                                }
                            }

                            mDataList.add(workerItemTypeBean)
                        }



                        mAdapter.setNewData(mDataList)

                    }
                }

                override fun onFail(err: String?, code: Int) {
                    _mActivity?.runOnUiThread {
                        swipeRefreshLayout.isRefreshing = false
                    }
                    LogUtils.i("$err")
                }

            })


    }

    override fun onConfirmClick(
        isRemote: Boolean,
        recordType: String,
        workItemBean: WorkItemBean
    ) {
        InviteActivity.newInstance(
            _mActivity,
            isRemote,
            recordType,
            workItemBean
        )

    }


    companion object {
        private const val ARG_PARAM1 = "applyStatusParams"

        @JvmStatic
        fun newInstance(applyStatusParams: String?): HomeItemListFragment {
            val fragment = HomeItemListFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, applyStatusParams)
            fragment.arguments = args
            return fragment
        }
    }


}