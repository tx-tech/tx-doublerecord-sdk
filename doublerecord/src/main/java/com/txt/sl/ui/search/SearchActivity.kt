package com.txt.sl.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.txt.sl.R
import com.txt.sl.base.AppMVPActivity
import com.txt.sl.entity.bean.WorkItemBean
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.OrderListNodeAdapter
import com.txt.sl.ui.adpter.WorkerItemTypeBean
import com.txt.sl.ui.order.OrderActivity
import com.txt.sl.ui.order.OrderDetailsActivity
import com.txt.sl.utils.LogUtils
import kotlinx.android.synthetic.main.tx_activity_search.*
import org.json.JSONObject
import java.util.*


private val TAG = SearchActivity::class.java.simpleName

class SearchActivity : AppMVPActivity<SearchContract.View,SearchPresenter>(), SearchContract.View {

    companion object {
        fun newActivity(context: Activity) {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivityForResult(intent, 10001)
        }
    }


    override fun initView() {
        super.initView()
        title = "查询"
        initSearchView()
        initRecyclerview()
//        swipeRefreshLayout.setOnRefreshListener { refreshData() }
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_details -> {
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderActivity.newActivity(this,bean.workItemBean.flowId)
                }
                R.id.ll_common->{
                    val bean = mDataList[position] as WorkerItemTypeBean
                    OrderDetailsActivity.newActivity(this, bean)
                }
                else -> {
                }
            }

        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter.apply {
            bindToRecyclerView(recyclerView)
            openLoadAnimation()
            setEmptyView(R.layout.tx_adapter_list_pic_empty, recyclerView)
        }
        recyclerView.adapter = mAdapter

        finish.setOnClickListener {
            finish()
        }
    }
    val mDataList = ArrayList<MultiItemEntity>()
    private fun initSearchView() {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.e(TAG, "onQueryTextSubmit : " + query)
                if (TextUtils.isEmpty(query)) {
                    return false
                }

                refreshData(query.toString())

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                Log.e(TAG, "onQueryTextChange : " + newText)
                if (TextUtils.isEmpty(newText)) {
                }

                return false
            }

        })
        searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.postDelayed({
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(view.findFocus(), 0)
                }, 200)
            }
        }
//        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.onActionViewExpanded()

    }

    private fun initRecyclerview() {

    }


    override fun showNoDataLayout() {
        runOnUiThread {


        }


    }


    override fun showErrorLayout() {
        runOnUiThread {

        }
    }
    fun refreshData(name:String) {
        SystemHttpRequest.getInstance().list("",name, object : HttpRequestClient.RequestHttpCallBack {
            override fun onSuccess(json: String?) {
                LogUtils.i("TravelApplyHomeItemListFragment", "----$json")
                val jsonOb = JSONObject(json)
                val jsonArray = jsonOb.getJSONArray("list")
                val arrayList = ArrayList<WorkItemBean>()
                for (index in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(index)
                    val policyholderObject = jsonObject.getJSONObject("policyholder")
                    arrayList.add(WorkItemBean().apply {
                        flowId = jsonObject.getString("flowId")
                        recordUrl = jsonObject.optString("recordUrl")

                        insurantName = policyholderObject.optString("name")
                        utime = jsonObject.optString("utime")
                        status = jsonObject.optString("status")
                        taskId = jsonObject.optString("taskId")
                    })

                }

                runOnUiThread {
                    mDataList.clear()
                    swipeRefreshLayout.isRefreshing = false

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
                            "Completed","Accepted" -> {
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
                    swipeRefreshLayout.isRefreshing = false
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

    override fun createPresenter(): SearchPresenter?  = SearchPresenter(this,this)
    override fun getLayoutId(): Int  = R.layout.tx_activity_search

}
