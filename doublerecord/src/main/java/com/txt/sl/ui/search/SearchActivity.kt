package com.txt.sl.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.base.AppMVPActivity
import com.txt.sl.entity.bean.WorkItemBean
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderListNodeAdapter
import com.txt.sl.ui.adpter.WorkerItemTypeBean
import com.txt.sl.ui.dialog.CheckRemoteDialog
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.invite.InviteActivity
import com.txt.sl.ui.order.OrderActivity
import com.txt.sl.ui.order.OrderDetailsActivity
import com.txt.sl.ui.video.VideoPlayActivity
import com.txt.sl.utils.LogUtils
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_activity_search.*
import kotlinx.android.synthetic.main.tx_activity_search.recyclerView
import org.json.JSONObject
import java.util.*


private val TAG = SearchActivity::class.java.simpleName

class SearchActivity : AppMVPActivity<SearchContract.View, SearchPresenter>(), SearchContract.View,
    CheckRemoteDialog.OnRemoteClickListener {

    companion object {
        fun newActivity(context: Activity) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivityForResult(intent, 10001)
        }
    }


    override fun initView() {
        super.initView()
        title = "查询"
        initRecyclerview()
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.apply {
            bindToRecyclerView(recyclerView)
            openLoadAnimation()
            setEmptyView(R.layout.tx_adapter_list_pic_empty, recyclerView)
        }
        recyclerView.adapter = mAdapter
        searchView.setOnEditorActionListener { v, actionId, event ->
           if ((actionId == 0 || actionId ==3 )&&event !=null){
               refreshData(searchView.text.toString())
               searchView.postDelayed({
                   hideInput()
               }, 200)
               return@setOnEditorActionListener  true
           }
            false
        }
        finish.setOnClickListener {

            refreshData(searchView.text.toString())
        }
    }

    val mDataList = ArrayList<MultiItemEntity>()
    public var customDialog: CheckRemoteDialog? = null
    public fun showDialog() {

        customDialog?.show()

    }

    private var mLoadingView: LoadingView? = null
    private fun initRecyclerview() {


        customDialog = CheckRemoteDialog(this)
        customDialog?.setOnRemoteClickListener(this)
        mLoadingView = LoadingView(this, "发起录制...", LoadingView.SHOWLOADING)


        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val bean = mDataList[position] as WorkerItemTypeBean
            when (view.id) {
                R.id.tv_details -> {

                    OrderActivity.newActivity(this, bean.workItemBean.flowId)
                }
                R.id.ll_common -> {
                    OrderDetailsActivity.newActivity(this, bean)
                }
                R.id.tv_item1_sl, R.id.tv_replay -> {
                    val workItemBean = bean.workItemBean

                    if (workItemBean?.isSelfInsurance!!) {
                        //如果是自保件
                        InviteActivity.newInstance(
                            this, false, "2", workItemBean!!
                        )
                    } else {
                        customDialog?.setData(
                            workItemBean
                        )
                        showDialog()
                    }

                }
                R.id.tv_unupload_play -> { //播放本地视频
                    val screenRecordStr =
                        TxSPUtils.get(this, bean.workItemBean.taskId, "") as String
                    LogUtils.i("screenRecordStr---$screenRecordStr")
                    if (!TextUtils.isEmpty(screenRecordStr)) {
                        val jsonObject = JSONObject(screenRecordStr)
                        val pathFile = jsonObject.getString("path")
                        VideoPlayActivity.Builder().setVideoSource(pathFile!!, false).start(this)
                    } else {
                        showToastMsg("查不到本地视频")
                    }

                }
                R.id.tv_playremotevideo -> { //播放远端视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    VideoPlayActivity.Builder().setVideoSource(bean.workItemBean.recordUrl!!, true)
                        .start(this)
                }
                R.id.tv_item2_play -> { //上传视频
                    val bean = mDataList[position] as WorkerItemTypeBean
                    upload(bean.workItemBean.taskId)

                }
//                R.id.tv_wx_share -> { //邀请
//                    val homeActivity = _mActivity as HomeActivity
//                    val bean = mDataList[position] as WorkerItemTypeBean
//                    XPopup.Builder(_mActivity).asConfirm("微信邀约", "确定进行微信邀约？",
//                            "取消", "好的",
//                            OnConfirmListener { homeActivity.requestWX(bean.workItemBean.insuredPhone,bean.workItemBean.taskId) }, null, false).show()
//
//                }
                else -> {
                }
            }

        }
    }


    override fun showNoDataLayout() {
        runOnUiThread {


        }


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

            }).dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .asCustom(customDialog).show()
        } else {
            showToastMsg("查不到本地视频")
        }


    }

    override fun showErrorLayout() {
        runOnUiThread {

        }
    }


    fun refreshData(name: String) {
        SystemHttpRequest.getInstance()
            .list("", name, object : HttpRequestClient.RequestHttpCallBack {
                override fun onSuccess(json: String?) {
                    val jsonOb = JSONObject(json)
                    val jsonArray = jsonOb.getJSONArray("list")
                    val arrayList = ArrayList<WorkItemBean>()
                    for (index in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.optJSONObject(index)
                        val policyholderObject = jsonObject.optJSONObject("policyholder")
                        val insurancesObject = jsonObject.optJSONArray("insurances")
                        val fieldsObject = jsonObject.optJSONObject("fields")
                        val stringBuffer = StringBuffer("")
                        for (index in 0 until insurancesObject.length()) {
                            val jsonObject1 = insurancesObject.optJSONObject(index)
                            val infoObject = jsonObject1.optJSONObject("info")
                            stringBuffer.append(infoObject.optString("name") + " ")
                        }
                        var list = ArrayList<String>()
                        val optJSONArray = jsonObject.optJSONArray("membersArray")
                        if (null != optJSONArray && optJSONArray.length() > 0) {
                            for (index in 0 until optJSONArray.length()) {
                                list.add(optJSONArray.optString(index))
                            }
                        }

                        arrayList.add(WorkItemBean().apply {
                            flowId = jsonObject.getString("flowId")
                            recordUrl = jsonObject.optString("videoUrl")
                            membersArray = list
                            insurantName = policyholderObject.optString("name")
                            insuredPhone = policyholderObject.optString("phone")
                            utime = jsonObject.optString("utime")
                            ctime = jsonObject.optString("ctime")
                            status = jsonObject.optString("status")
                            taskId = jsonObject.optString("taskId")
                            isIsRemote = jsonObject.optBoolean("isRemote")
                            isSelfInsurance = jsonObject.optBoolean("selfInsurance") //是否自保件
                            insuranceName = stringBuffer.toString()
                            relationship = jsonObject.optString("relationship")
                            policyholderUrl = jsonObject.optString("policyholderUrl")
                            insuranceUrl = jsonObject.optString("insuranceUrl")
                            insurerQuotationNo = fieldsObject.optString("insurerQuotationNo")
                        })

                    }

                    runOnUiThread {
                        mDataList.clear()

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


                        mAdapter.emptyView.findViewById<TextView>(R.id.tx_emptycontent).text = "暂无工单"
                        mAdapter.setNewData(mDataList)

                    }
                }

                override fun onFail(err: String?, code: Int) {
                    runOnUiThread {
                    }
                    LogUtils.i("$err")
                }

            })


    }

    override fun refreshData(mListData: ArrayList<String>?) {

        runOnUiThread {

        }

    }


    var mAdapter = OrderListNodeAdapter(null)

    override fun createPresenter(): SearchPresenter? = SearchPresenter(this, this)
    override fun getLayoutId(): Int = R.layout.tx_activity_search
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

}
