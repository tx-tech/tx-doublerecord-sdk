package com.natchi.base

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.lxj.xpopup.impl.FullScreenPopupView
import com.txt.sl.TXSdk

/**
 * Created by JustinWjq
 * @date 2020/12/10.
 * description：
 */
public class CustomFullScreenPopup constructor(context: Context) : FullScreenPopupView(context){

    override fun getImplLayoutId(): Int {
        return R.layout.layout_customfullscreen
    }

    override fun onCreate() {
        super.onCreate()
        val rgMimpro = findViewById<RadioGroup>(R.id.rg_mimpro)
        val rg_tx_app = findViewById<RadioGroup>(R.id.rg_tx_app)
        val btConfirm = findViewById<Button>(R.id.bt_confirm)
        val et_tx_path = findViewById<EditText>(R.id.et_tx_path)
        val et_tx_username = findViewById<EditText>(R.id.et_tx_username)
        btConfirm.setOnClickListener {
            TXSdk.getInstance().txConfig.miniprogramType  =when (rgMimpro.checkedRadioButtonId) {
                R.id.minpro_test -> {
                    TXSdk.Environment.TEST
                }
                R.id.minpro_dev -> {
                    TXSdk.Environment.DEV
                }
                R.id.minpro_rel -> {
                    TXSdk.Environment.RELEASE
                }
                else -> {
                    TXSdk.Environment.TEST
                }
            }
            TXSdk.getInstance().checkoutNetEnv(when (rg_tx_app.checkedRadioButtonId) {
                R.id.app_test -> {
                    TXSdk.Environment.TEST
                }
                R.id.app_dev -> {
                    TXSdk.Environment.DEV
                }
                R.id.app_rel -> {
                    TXSdk.Environment.RELEASE
                }
                else -> {
                    TXSdk.Environment.TEST
                }
            })

            TXSdk.getInstance().txConfig.miniProgramPath = et_tx_path.text.toString()
            TXSdk.getInstance().txConfig.userName = et_tx_username.text.toString()

            dismiss()
        }


        when (TXSdk.getInstance().txConfig.miniprogramType) {
            TXSdk.Environment.TEST-> {
                rgMimpro.check( R.id.minpro_test)
            }
            TXSdk.Environment.DEV-> {
                rgMimpro.check( R.id.minpro_dev)
            }
            TXSdk.Environment.RELEASE-> {
                rgMimpro.check( R.id.minpro_rel)
            }
        }

        when (TXSdk.getInstance().environment) {
            TXSdk.Environment.TEST-> {
                rg_tx_app.check( R.id.app_test)
            }
            TXSdk.Environment.DEV-> {
                rg_tx_app.check( R.id.app_dev)
            }
            TXSdk.Environment.RELEASE-> {
                rg_tx_app.check( R.id.app_rel)
            }
        }

        et_tx_path.setText(TXSdk.getInstance().txConfig.miniProgramPath)
        et_tx_username.setText(TXSdk.getInstance().txConfig.userName)
    }

}