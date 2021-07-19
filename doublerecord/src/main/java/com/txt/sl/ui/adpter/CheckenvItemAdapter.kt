package com.txt.sl.ui.adpter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.content.ContextCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.common.widget.recyclerviewadapterhelper.base.TxBaseQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.txt.sl.R
import com.txt.sl.entity.bean.CheckEnvItem

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：检测环境
 */
public class CheckenvItemAdapter() :
    TxBaseQuickAdapter<CheckEnvItem, TxBaseViewHolder>(R.layout.tx_rv_item_checkenv
    ) {

    fun startAnim() : Animation {

        return  AnimationUtils.loadAnimation(this.mContext,R.anim.tx_rorate_anim)
    }

    override fun convert(helper: TxBaseViewHolder, item: CheckEnvItem?) {
        val ivUploading = helper.getView<ImageView>(R.id.tv_title)
        helper.setText(R.id.tv_value,item?.value)

        if (item?.isUploading!!) {
            ivUploading.clearAnimation()
            if (item.isPass) {
                ivUploading.setImageDrawable(ContextCompat.getDrawable(helper.itemView.context,R.drawable.tx_pass_icon))
            }else{
                ivUploading.setImageDrawable(ContextCompat.getDrawable(helper.itemView.context,R.drawable.tx_nopass_icon))
            }

        }else{
            ivUploading.setImageDrawable(ContextCompat.getDrawable(helper.itemView.context,R.drawable.tx_icon_refresh))
            ivUploading.startAnimation( startAnim())
        }

    }



}