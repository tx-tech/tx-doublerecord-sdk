package com.natchi.screenrecorddemo

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.txt.sl.utils.TxPermissionConstants
import com.txt.sl.utils.TxPermissionUtils
import kotlinx.android.synthetic.main.activity_check_web_view.*

class CheckWebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_web_view)
        initView()
        tv_webView.setOnClickListener {
            loadUrl()
        }
    }

    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TxPermissionUtils.permission(
                TxPermissionConstants.CAMERA,
                TxPermissionConstants.MICROPHONE,
                TxPermissionConstants.PHONE
            ).callback(object : TxPermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    if (permissionsGranted.contains("android.permission.CAMERA") && permissionsGranted.contains(
                            "android.permission.RECORD_AUDIO"
                        )
                    ) {

                    } else {

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
    var smartWebDialog : SmartWebDialog ?= null
    private fun loadUrl(){
        if (null == smartWebDialog) {
            smartWebDialog = SmartWebDialog(this)
        }
        smartWebDialog?.show()
        smartWebDialog?.request("https://webdemo.agora.io/agora_webrtc_troubleshooting/","")
    }


}