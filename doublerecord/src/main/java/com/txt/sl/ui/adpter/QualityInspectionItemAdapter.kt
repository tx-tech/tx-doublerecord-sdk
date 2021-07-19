package com.txt.sl.ui.adpter

import android.support.v7.widget.LinearLayoutCompat
import android.view.View
import com.common.widget.recyclerviewadapterhelper.base.TxBaseQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.txt.sl.R
import com.txt.sl.entity.bean.OrderRecordBean
import com.txt.sl.utils.DateUtils
import java.text.ParseException

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class QualityInspectionItemAdapter(var data: ArrayList<OrderRecordBean>) :
    TxBaseQuickAdapter<OrderRecordBean, TxBaseViewHolder>(R.layout.tx_rv_item_record,
        data
    ) {
    override fun convert(helper: TxBaseViewHolder, item: OrderRecordBean?) {
        helper.addOnClickListener(R.id.tv_play)
        val ll_failType = helper.getView<LinearLayoutCompat>(R.id.ll_failType)
        val ll_failReason = helper.getView<LinearLayoutCompat>(R.id.ll_failReason)
        try {
            val s = DateUtils.UTCToCST(item?.uploadedTime)
            helper.setText(R.id.tv_uploadedtime, s)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        when (item?.status) {
            "Completed","Accepted" -> {
                ll_failType.visibility = View.GONE
                ll_failReason.visibility = View.GONE
                helper.setText(R.id.tv_result,"通过")
            }
            else -> {
                helper.setText(R.id.tv_result,"不通过")
                ll_failType.visibility = View.VISIBLE
                ll_failReason.visibility = View.VISIBLE
                helper.setText(R.id.tv_failType,item?.failType)
                helper.setText(R.id.tv_failReason,item?.failReason)
            }
        }
    }



}