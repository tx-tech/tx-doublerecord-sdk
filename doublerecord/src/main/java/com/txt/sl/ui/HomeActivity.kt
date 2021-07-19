package com.txt.sl.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.design.widget.TabLayout
import android.view.View
import com.common.widget.dialog.TxPopup
import com.common.widget.dialog.interfaces.OnConfirmListener
import com.common.widget.dialog.interfaces.XPopupCallback
import com.common.widget.base.BaseActivity
import com.common.widget.dialog.util.PermissionConstants
import com.txt.sl.R
import com.txt.sl.TXSdk
import com.txt.sl.entity.constant.SPConstant
import com.txt.sl.receive.SystemBaiduLocation
import com.txt.sl.ui.dialog.UploadVideoDialog
import com.txt.sl.ui.home.ApplyHomeFragmentAdapter
import com.txt.sl.ui.home.PagerBean
import com.txt.sl.ui.search.SearchActivity
import com.txt.sl.utils.TxSPUtils
import com.txt.sl.utils.ToastUtils
import com.txt.sl.utils.TxPermissionUtils
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
    }


    private fun showDialog() {
        TxPopup.Builder(this).asConfirm("退出", "确认退出智能双录？", "取消", "确认", object : OnConfirmListener {
            override fun onConfirm() {
                finish()
            }

        }, null, false).show()
    }

    override fun initView() {
        super.initView()
        initFragment()
        //        val initAuth = YTCommonInterface.initAuth(getApplicationContext(), "https://license.youtu.qq.com/youtu/sdklicenseapi/license_generate", "10240298", "HI4dv50kEQM9j1DAs50f5E6pHfmcCXbU", false)
//        Auth.authWithDeviceSn(this, WXApi.YT_ID /*修改APPID为实际的值*/, WXApi.YT_SECRETKEY /*修改SECRET_KEY为实际的值*/)

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TxPermissionUtils.permission(
                    PermissionConstants.CAMERA,
                    PermissionConstants.MICROPHONE,
                    PermissionConstants.PHONE,
                    PermissionConstants.LOCATION
            ).callback(object : TxPermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {

                    if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains(
                                    "android.permission.RECORD_AUDIO"
                            )
                    ) {


                    } else if (permissionsGranted.contains("android.permission.LOCATION")) {
                        SystemBaiduLocation.instance!!.requestLocation()

                    }
                }

                override fun onDenied(
                        permissionsDeniedForever: List<String>,
                        permissionsDenied: List<String>
                ) {
                    if (permissionsDenied.contains("android.permission.CAMERA") || permissionsDenied.contains(
                                    "android.permission.RECORD_AUDIO"
                            )
                    ) {
                    } else {
                    }
                }
            }
            ).request()
        } else {

        }

    }


    override fun onBackPressed() {
        showDialog()
    }


    fun upload(flowId: String) {
        val screenRecordStr = TxSPUtils.get(TXSdk.getInstance().application, flowId, "") as String
        if (!screenRecordStr.isEmpty()) {
            //上传视频
            val customDialog = UploadVideoDialog(this)
            customDialog.setFlowId(screenRecordStr)
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
            ToastUtils.showShort("没有找到对应的录屏文件")
        }


    }


}