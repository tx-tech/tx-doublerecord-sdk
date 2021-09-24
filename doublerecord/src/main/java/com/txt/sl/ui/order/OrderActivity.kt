package com.txt.sl.ui.order

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.common.widget.base.BaseActivity
import com.txt.sl.R
import com.txt.sl.entity.bean.FileBean
import com.txt.sl.entity.bean.LevelItem1
import com.txt.sl.http.https.HttpRequestClient
import com.txt.sl.system.SystemHttpRequest
import com.txt.sl.ui.adpter.ExpandableItemAdapter
import kotlinx.android.synthetic.main.tx_activity_order.*
import org.json.JSONArray
import java.util.*

class OrderActivity : BaseActivity() {
    companion object {
        var flowIdStr = "flowId"
        fun newActivity(context: Context,flowId:String) {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra(flowIdStr,flowId)
            context.startActivity(intent)
        }

    }



    override fun initView() {
        super.initView()
        title = "质检详情"
    }

    override fun initData() {
        val mFlowId = intent.extras.getString(flowIdStr)
        SystemHttpRequest.getInstance().getRecordTaskProcessDisabled(
                mFlowId,
                object : HttpRequestClient.RequestHttpCallBack{
                    override fun onSuccess(json: String?) {
                        runOnUiThread {

                            val jsonArray = JSONArray(json)

                            list = ArrayList()

                            for (index in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(index)
                                val name = jsonObject.optString("name")
                                val stepsJsonArray = jsonObject.getJSONArray("steps")
                                val isPubliclist: ArrayList<MultiItemEntity> = ArrayList<MultiItemEntity>()
                                val isNoPublicItem = LevelItem1(isPubliclist, name)
                                for (index1 in 0 until stepsJsonArray.length()) {
                                    val jsonObject1 = stepsJsonArray.getJSONObject(index1)
                                    val fileBean = FileBean()
                                    fileBean.name = jsonObject1.optString("name")
                                    fileBean.failType = jsonObject1.optString("failType")
                                    fileBean.failReason = jsonObject1.optString("failReason")

                                    isNoPublicItem.addSubItem(
                                            fileBean
                                    )
                                }

                                list?.add(isNoPublicItem)
                            }
                            initRecyclerview()
                        }
                    }

                    override fun onFail(err: String?, code: Int) {
                        runOnUiThread {
                            showToastMsg(err)
                        }
                    }

                }
        )
    }

    override fun getLayoutId(): Int = R.layout.tx_activity_order

    var list: ArrayList<MultiItemEntity>? = null
    var baseQuickAdapter: ExpandableItemAdapter? = null
    fun initRecyclerview(){

        recyclerview.layoutManager = LinearLayoutManager(this@OrderActivity)
        baseQuickAdapter = ExpandableItemAdapter(list!!)
        recyclerview.adapter = baseQuickAdapter

        baseQuickAdapter?.setOnItemClickListener { adapter, view, position ->
            val data: List<MultiItemEntity> = baseQuickAdapter!!.getData()
            val listBean: MultiItemEntity = data[position]

        }
        baseQuickAdapter?.expand(0)
    }

}