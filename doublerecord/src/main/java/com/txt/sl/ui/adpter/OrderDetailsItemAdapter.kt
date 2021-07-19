package com.txt.sl.ui.adpter

import com.common.widget.recyclerviewadapterhelper.base.TxBaseQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.txt.sl.R
import com.txt.sl.entity.bean.OrderDetailsItem

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class OrderDetailsItemAdapter(var data: ArrayList<OrderDetailsItem>) :
    TxBaseQuickAdapter<OrderDetailsItem, TxBaseViewHolder>(R.layout.tx_rv_item_orderdetails,
        data
    ) {
    override fun convert(helper: TxBaseViewHolder, item: OrderDetailsItem?) {
        helper.setText(R.id.tv_title,item?.title)
        helper.setText(R.id.tv_value,item?.value)
    }



}