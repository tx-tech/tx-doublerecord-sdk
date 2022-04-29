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
    private fun loadUrl() {
        if (null == smartWebDialog) {
            smartWebDialog = SmartWebDialog(this)
        }
        smartWebDialog?.show()
        smartWebDialog?.request(
            "https://sync-fileview.cloud-ins.cn/onlinePreview?syncid=1-lt21101802-3&synctoken=006880b027964924e6ca254b77531c2eaf3IAAznxSNdqqNlMmYMXm8Y84BijRhHNvztGOC3Rwf31JrXL0AKGEAAAAAEACjXG2OwZVnYgEA6APBlWdi&sync=test&dr=true&url=aHR0cHM6Ly9nZHJiLWRpbmdzdW4tdGVzdC0xMjU1MzgzODA2LmNvcy5hcC1zaGFuZ2hhaS5teXFjbG91ZC5jb20vJUU3JTg4JUIxJUU1JUJGJTgzJUU0JUJBJUJBJUU1JUFGJUJGJUU1JUFFJTg4JUU2JThBJUE0JUU3JUE1JTlFMi4wJUU3JUJCJTg4JUU4JUJBJUFCJUU1JUFGJUJGJUU5JTk5JUE5LSVFNSU4NSVCMyVFNCVCQSU4RSVFNSU4NSU4RCVFOSU5OSVBNCVFNCVCRiU5RCVFOSU5OSVBOSVFNCVCQSVCQSVFOCVCNCVBMyVFNCVCQiVCQiVFNiU5RCVBMSVFNiVBQyVCRSVFNyU5QSU4NCVFNCVCOSVBNiVFOSU5RCVBMiVFOCVBRiVCNCVFNiU5OCU4RS5wZGY%3D",            ""
        )

    }
}