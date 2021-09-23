package com.txt.sl.ui.dialog

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.widget.dialog.core.CenterPopupView
import com.tencent.qcloudtts.VoiceSpeed
import com.txt.sl.R
import com.txt.sl.utils.TxSPUtils

/**
 * author ：Justin
 * time ：2021/7/12.
 * des ： 上传视频框
 */
public class ChooseSpeedDialog(
    context: Context
) : CenterPopupView(context), View.OnClickListener {
    override fun onCreate() {
        super.onCreate()
        initSpeedBt()
    }

    var ivSpeed: ImageView? = null
    var ivSpeed1: ImageView? = null
    var ivSpeed2: ImageView? = null
    var ivSpeed3: ImageView? = null
    var ivSpeed4: ImageView? = null
    var ivList: ArrayList<ImageView> = ArrayList()
    private fun initSpeedBt() {
        ivSpeed = findViewById<ImageView>(R.id.iv_speed)
        ivSpeed1 = findViewById<ImageView>(R.id.iv_speed1)
        ivSpeed2 = findViewById<ImageView>(R.id.iv_speed2)
        ivSpeed3 = findViewById<ImageView>(R.id.iv_speed3)
        ivSpeed4 = findViewById<ImageView>(R.id.iv_speed4)
        ivSpeed?.setOnClickListener(this)
        ivSpeed1?.setOnClickListener(this)
        ivSpeed2?.setOnClickListener(this)
        ivSpeed3?.setOnClickListener(this)
        ivSpeed4?.setOnClickListener(this)
        ivList.add(ivSpeed!!)
        ivList.add(ivSpeed1!!)
        ivList.add(ivSpeed2!!)
        ivList.add(ivSpeed3!!)
        ivList.add(ivSpeed4!!)
        findViewById<TextView>(R.id.tv_gotovideo).setOnClickListener(this)
        val position = TxSPUtils.get(context, mAgentIdStr, 2) as Int
        selectOneIvForPostion(position)
    }

    override fun getImplLayoutId(): Int {
        return R.layout.tx_dialog_choosespeak
    }


    interface OnConfirmClickListener {
        /**
         * @param voiceSpeed
         * @param content
         */
        fun onSpeedChoose(voiceSpeed: Int, content: String)

        fun onConfirm()
    }

    var mOnItemClickListener: OnConfirmClickListener? = null
    fun setOnConfirmClickListener(onItemClickListener: OnConfirmClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    override fun onShow() {
        super.onShow()

    }

    var mAgentIdStr: String? = null

    public fun setAgentIdStr(agentIdStr: String) {
        mAgentIdStr = agentIdStr
    }

    override fun onDismiss() {
        super.onDismiss()
        ivList?.clear()
    }

    val contentTv = "您将选择当前语速进行双录"
    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.iv_speed) {
            selectOneIv(v)
            mOnItemClickListener?.onSpeedChoose(VoiceSpeed.VOICE_SPEED_VERY_SLOW.num, contentTv)
        } else if (id == R.id.iv_speed1) {
            selectOneIv(v)
            mOnItemClickListener?.onSpeedChoose(VoiceSpeed.VOICE_SPEED_SLOWDOWN.num, contentTv)
        } else if (id == R.id.iv_speed2) {
            selectOneIv(v)
            mOnItemClickListener?.onSpeedChoose(VoiceSpeed.VOICE_SPEED_NORMAL.num, contentTv)
        } else if (id == R.id.iv_speed3) {
            selectOneIv(v)
            mOnItemClickListener?.onSpeedChoose(VoiceSpeed.VOICE_SPEED_ACCELERATE.num, contentTv)
        } else if (id == R.id.iv_speed4) {
            selectOneIv(v)
            mOnItemClickListener?.onSpeedChoose(VoiceSpeed.VOICE_SPEED_VERY_FAST.num, contentTv)
        } else if (id == R.id.tv_gotovideo) {
            TxSPUtils.put(context, mAgentIdStr, position)
            mOnItemClickListener?.onConfirm()
            dismiss()
        }

    }

    var position = 2
    private fun selectOneIv(v: View?) {
        position = ivList.indexOf(v)
        ivList.forEach {
            it.isSelected = v?.id == it.id
        }

    }

    private fun selectOneIvForPostion(position: Int) {
        val imageView = ivList[position]
        ivList.forEach {
            it.isSelected = imageView?.id == it.id
        }
    }


}