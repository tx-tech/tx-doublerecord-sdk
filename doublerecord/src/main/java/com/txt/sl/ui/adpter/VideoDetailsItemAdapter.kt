package com.txt.sl.ui.adpter

import com.common.widget.recyclerviewadapterhelper.base.TxBaseQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.txt.sl.R

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class VideoDetailsItemAdapter(var data: ArrayList<String>) :
    TxBaseQuickAdapter<String, TxBaseViewHolder>(R.layout.tx_rv_item_videodetails,
        data
    ) {
    override fun convert(helper: TxBaseViewHolder, title: String?) {
        helper.setText(R.id.tv_title1,""+(helper.adapterPosition+1)+"、")
        helper.setText(R.id.tv_value1,title)
    }



}