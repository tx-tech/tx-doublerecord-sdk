package com.txt.sl.ui.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.common.widget.toast.ToastUtils
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.entity.bean.WorkItemBean
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderListNodeAdapter
import com.txt.sl.ui.adpter.WorkerItemTypeBean
import com.txt.sl.ui.dialog.CheckRemoteDialog
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.invite.InviteActivity
import com.txt.sl.ui.order.OrderActivity
import com.txt.sl.ui.order.OrderDetailsActivity
import com.txt.sl.ui.search.SearchActivity
import com.txt.sl.ui.video.VideoPlayActivity
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.TxLogUtils
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_activity_home.*
import org.json.JSONArray
import org.json.JSONObject


class HomeActivity : BaseActivity(), CheckRemoteDialog.OnRemoteClickListener {

    companion object {
        public const val br_action = "action.refreshlist"
        public const val br_action_selectTab = "action.refreshListAndselect"

        @JvmStatic
        fun newActivity(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }

    }


    override fun getLayoutId(): Int {
        return R.layout.tx_activity_home
    }


    override fun onRightClick(view: View?) {
        SearchActivity.newActivity(this)
    }


    private fun showDialog() {
        TxPopup.Builder(this).asConfirm(
            "退出", "确认退出智能双录？", "取消", "确认",
            {
                if (null != TXSdk.getInstance().onTxPageListener) {
                    TXSdk.getInstance().onTxPageListener.onSuccess()
                }

                finish()
            },
            null,
            false
        ).show()
    }

    override fun initView() {
        super.initView()
        initFragment()
        tabLayout.tabMode = TabLayout.MODE_FIXED

        val get = TxSPUtils.get(this, SPConstant.REPORT_STATESLIST, "") as String
        val applyStatusParamsList = ArrayList<PagerBean>()
        val jsonArray = JSONArray(get)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                LogUtils.i("tabLayout.selectedTabPosition ---${tabLayout.selectedTabPosition}")

                applyStatusParams = applyStatusParamsList[tabLayout.selectedTabPosition].getStatus()
                refreshData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val code = jsonObject.optString("code")
            val name = jsonObject.optString("name")

            applyStatusParamsList.add(PagerBean(name, code))

            val newTab = tabLayout.newTab().apply {
                text = name
            }

            tabLayout.addTab(newTab)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(br_action)
        intentFilter.addAction(br_action_selectTab)
        registerReceiver(broadcastReceiver, intentFilter)

    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    @SuppressLint("CommitTransaction", "WrongConstant")
    fun initFragment() {
        title = "智能双录"
        titleBar?.rightView?.visibility = View.VISIBLE
        titleBar?.leftView?.visibility = View.VISIBLE
        setRightIcon(R.drawable.tx_search_icon)
        initView1()

    }


    override fun onBackPressed() {
        showDialog()
    }


    fun upload(flowId: String) {
        val screenRecordStr = TxSPUtils.get(TXSdk.getInstance().application, flowId, "") as String
        if (!screenRecordStr.isEmpty()) {
            //上传视频
            val customDialog = UploadVideoDialog(this, true)
            customDialog.setScreenRecordStr(screenRecordStr)
            TxPopup.Builder(this).setPopupCallback(object : XPopupCallback {
                override fun onCreated() {

                }

                override fun beforeShow() {
                }

                override fun onShow() {

                }

                override fun onDismiss() {

                }

                override fun onBackPressed(): Boolean {
                    return true
                }

            }).asCustom(customDialog).show()
        } else {
            showToastMsg("查不到本地视频")
        }


    }


    override fun onPause() {
        super.onPause()
        TxLogUtils.i("onPause------onPause")
    }

    var mAdapter = OrderListNodeAdapter(null)


    val mDataList = ArrayList<MultiItemEntity>()
    public var customDialog: CheckRemoteDialog? = null
    public fun showDialog1() {

        customDialog?.show()
    }

    private var mLoadingView: LoadingView? = null
    fun initView1() {

        customDialog = CheckRemoteDialog(this)
        customDialog?.setOnRemoteClickListener(this)
        mLoadingView = LoadingView(this, "发起录制...", LoadingView.SHOWLOADING)

        swipeRefreshLayout.setOnRefreshListener { refreshData() }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_details -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderActivity.newActivity(this, bean.workItemBean.flowId)
                }
                R.id.ll_common -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderDetailsActivity.newActivity(this, bean)
                }
                R.id.tv_item1_sl, R.id.tv_replay -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    var workItemBean = bean?.workItemBean
                    if (workItemBean?.isSelfInsurance!!) {
                        //如果是自保件
                        InviteActivity.newInstance(
                            this, false, "2", workItemBean!!
                        )
                    } else {
                        customDialog?.setData(workItemBean)
                        showDialog1()
                    }
                }
                R.id.tv_unupload_play -> { //播放本地视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    val screenRecordStr =
                        TxSPUtils.get(this, bean.workItemBean.taskId, "") as String
                    LogUtils.i("screenRecordStr---$screenRecordStr")
                    if (!TextUtils.isEmpty(screenRecordStr)) {
                        val jsonObject = JSONObject(screenRecordStr)
                        val pathFile = jsonObject.getString("path")
                        VideoPlayActivity.Builder().setVideoSource(pathFile!!, false)
                            .start(this)
                    } else {
                        ToastUtils.show("查不到本地视频")
                    }

                }
                R.id.tv_playremotevideo -> { //播放远端视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    if (bean.workItemBean.recordUrl.isNullOrEmpty()) {
                        ToastUtils.show("视频正在生成中，请稍等")
                    } else {
                        VideoPlayActivity.Builder()
                            .setVideoSource(bean.workItemBean.recordUrl!!, true)
                            .start(this)
                    }

                }
                R.id.tv_item2_play -> { //上传视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    upload(bean.workItemBean.taskId)

                }
                else -> {
                }
            }

        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.apply {
            bindToRecyclerView(recyclerView)
            openLoadAnimation()
            val inflate = layoutInflater.inflate(R.layout.tx_adapter_list_pic_empty, null)
            val tv = inflate.findViewById<TextView>(R.id.tx_emptycontent)
            tv.text = "暂无工单"
            emptyView = inflate
        }
        recyclerView.adapter = mAdapter

    }

    var applyStatusParams  = ""
    fun refreshData() {
        val dialog = TxPopup.Builder(this).asLoading("获取工单中...").show()
        LogUtils.i("applyStatusParams", applyStatusParams!!)
        tv_unupload.visibility = if (applyStatusParams.equals("UnUploaded")) {
            View.VISIBLE
        } else {
            View.GONE
        }
        mAdapter.setNewData(null)
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
                            ctime = jsonObject.optString("ctime")
                            status = jsonObject.optString("status")
                            taskId = jsonObject.optString("taskId")
                            isIsRemote = jsonObject.optBoolean("isRemote")
                            isSelfInsurance = jsonObject.optBoolean("selfInsurance") //是否自保件
                            insuranceName = stringBuffer.toString()
                            relationship = jsonObject.optString("relationship")
                            policyholderUrl = fieldsObject.optString("policyholderUrl")
                            insuranceUrl = fieldsObject.optString("insuranceUrl")
                            recordingMethod = jsonObject.optString("recordingMethod")
                            insurerQuotationNo = fieldsObject.optString("insurerQuotationNo")
                        })

                    }

                    runOnUiThread {
                        dialog.dismiss()
                        mDataList.clear()
                        swipeRefreshLayout?.isRefreshing = false

                        arrayList.forEach {
                            val workerItemTypeBean = WorkerItemTypeBean(it)
                            workerItemTypeBean.itemType = when (it.status) {

                                "Refused" -> {
                                    OrderListNodeAdapter.TYPE_Refused
                                }
                                "UnUploaded" -> {
                                    OrderListNodeAdapter.TYPE_UnUploaded
                                }
                                "UnChecked" -> {
                                    OrderListNodeAdapter.TYPE_UnChecked
                                }
                                "Completed", "Accepted" -> {
                                    OrderListNodeAdapter.TYPE_Completed
                                }
                                else -> {
                                    OrderListNodeAdapter.TYPE_UnRecorded
                                }
                            }

                            mDataList.add(workerItemTypeBean)
                        }



                        mAdapter.setNewData(mDataList)

                    }
                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                        dialog.dismiss()
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
            this,
            isRemote,
            recordType,
            workItemBean
        )

    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (br_action == intent?.action) {
                TxLogUtils.i("收到消息 刷新列表")
                refreshData()
            }else if (br_action_selectTab == intent?.action) {
                TxLogUtils.i("收到消息 刷新列表")
                val tabAt = tabLayout?.getTabAt(2)
                tabAt?.select()
            }
        }

    }

}