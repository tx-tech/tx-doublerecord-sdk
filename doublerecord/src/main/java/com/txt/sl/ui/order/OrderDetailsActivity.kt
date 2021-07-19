package com.txt.sl.ui.order

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import com.common.widget.base.BaseActivity
import com.txt.sl.R
import com.txt.sl.ui.adpter.MainPageAdapter
import com.txt.sl.ui.adpter.WorkerItemTypeBean
import com.txt.sl.widget.LoadingView
import kotlinx.android.synthetic.main.tx_activity_order_details.*
import java.util.ArrayList

class OrderDetailsActivity : BaseActivity() {

    companion object {
        var param1 = "bean"
        fun newActivity(context: Context, bean: WorkerItemTypeBean) {
            val intent = Intent(context, OrderDetailsActivity::class.java)
            intent.putExtra(param1, bean)
            context.startActivity(intent)
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.tx_activity_order_details;
    }

    private var adapter1: MainPageAdapter? = null
    private var fragments: MutableList<Fragment>? = ArrayList()
    var dataBean: WorkerItemTypeBean? = null
    public var mLoadingView: LoadingView? = null
    override fun initView() {
        super.initView()
        title = "任务详情"

        dataBean = intent.getSerializableExtra(param1) as WorkerItemTypeBean

        fragments?.add(OrderDetailsFragment.newInstance("", dataBean!!.workItemBean?.flowId!!))
        fragments?.add(CheckFragment.newInstance("", dataBean!!.workItemBean?.flowId!!))

        adapter1 = MainPageAdapter(
                supportFragmentManager
        )
        mLoadingView = LoadingView(this, "发起录制...", LoadingView.SHOWLOADING)
        adapter1?.setData(fragments)
        viewPager.adapter = adapter1
        tabLayout.setupWithViewPager(viewPager)
    }



}