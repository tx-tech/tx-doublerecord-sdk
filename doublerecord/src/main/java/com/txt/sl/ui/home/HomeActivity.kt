package com.txt.sl.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.design.widget.TabLayout
import android.view.View
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.base.BaseActivity
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.search.SearchActivity
import com.txt.sl.utils.TxSPUtils
import kotlinx.android.synthetic.main.tx_activity_home.tabLayout
import kotlinx.android.synthetic.main.tx_activity_home.viewPager
import org.json.JSONArray

class HomeActivity : BaseActivity() {

    companion object {
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
//        TestActivity.newActivity(this)
    }


    private fun showDialog() {
        TxPopup.Builder(this).asConfirm("退出", "确认退出智能双录？", "取消", "确认",
            {
                if (null != TXSdk.getInstance().onTxPageListener) {
                    TXSdk.getInstance().onTxPageListener.onSuccess()
                }

                finish()
            }, null, false).show()
    }

    override fun initView() {
        super.initView()
        initFragment()

        val get = TxSPUtils.get(this, SPConstant.REPORT_STATESLIST, "") as String
        val applyStatusParams = ArrayList<PagerBean>()
        val jsonArray = JSONArray(get)
        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val code = jsonObject.getString("code")
            val name = jsonObject.getString("name")

            applyStatusParams.add(PagerBean(name, code))
        }
        tabLayout.tabMode = TabLayout.MODE_FIXED
        val applyHomeFragmentAdapter = ApplyHomeFragmentAdapter(this, supportFragmentManager, applyStatusParams)
        viewPager.adapter = applyHomeFragmentAdapter
        tabLayout.setupWithViewPager(viewPager)
    }


    @SuppressLint("CommitTransaction", "WrongConstant")
    fun initFragment() {
        title = "智能双录"
        titleBar?.rightView?.visibility = View.VISIBLE
        titleBar?.leftView?.visibility = View.VISIBLE
        setRightIcon(R.drawable.tx_search_icon)


    }


    override fun onBackPressed() {
        showDialog()
    }


    fun upload(flowId: String) {
        val screenRecordStr = TxSPUtils.get(TXSdk.getInstance().application, flowId, "") as String
        if (!screenRecordStr.isEmpty()) {
            //上传视频
            val customDialog = UploadVideoDialog(this,true)
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

    override fun onDestroy() {

        super.onDestroy()
    }
}