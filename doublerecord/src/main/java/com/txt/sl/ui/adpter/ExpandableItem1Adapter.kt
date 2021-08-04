package com.txt.sl.ui.adpter

import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.common.widget.recyclerviewadapterhelper.base.BaseMultiItemQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.txt.sl.R
import com.txt.sl.entity.bean.FileBean
import com.txt.sl.entity.bean.LevelItem1

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class ExpandableItem1Adapter(var data: ArrayList<MultiItemEntity>) :
    BaseMultiItemQuickAdapter<MultiItemEntity, TxBaseViewHolder>(
        data
    ) {
    val TYPE_LEVEL_0 = 0
    val TYPE_LEVEL_1 = 1

    init {
        addItemType(TYPE_LEVEL_0, R.layout.tx_layout_item_head1)
        addItemType(TYPE_LEVEL_1, R.layout.tx_adapter_file_list_item1)

    }

    override fun convert(helper: TxBaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            TYPE_LEVEL_0 -> {
                val levelItem1 = item as LevelItem1
                if (item.subItems==null||item.subItems.size==0) {
                    helper.setVisible(R.id.tx_iv_arrow,false)
                    helper.getView<TextView>(R.id.tv_state).background =ContextCompat.getDrawable(helper.itemView.context,R.drawable.tx_shape_green_oval)
                    helper.getView<TextView>(R.id.tv_state1).text = "通过"
                    helper.getView<TextView>(R.id.tv_state1).setTextColor(ContextCompat.getColor(helper.itemView.context,R.color.tx_txcolor_40D4A1))
                }else{
                    helper.setVisible(R.id.tx_iv_arrow,true)
                    helper.getView<TextView>(R.id.tv_state).background =ContextCompat.getDrawable(helper.itemView.context,R.drawable.tx_shape_red_oval)
                    helper.getView<TextView>(R.id.tv_state1).text = "不通过"
                    helper.getView<TextView>(R.id.tv_state1).setTextColor(ContextCompat.getColor(helper.itemView.context,R.color.tx_txcolor_ED6656))
                }
                helper.setText(R.id.tx_tv_headtitle, ""+(helper.adapterPosition+1)+"、"+levelItem1.title)

                helper.setImageResource(R.id.tx_iv_arrow,
                    if (levelItem1.isExpanded) {
                        R.drawable.tx_up_icon
                    } else {
                        R.drawable.tx_down_icon
                    }
                )

                helper.itemView.setOnClickListener {
                    val adapterPosition = helper.adapterPosition
                    if (levelItem1.isExpanded) {
                        collapse(adapterPosition)
                    } else {
                        expand(adapterPosition)
                    }
                }
            }
            TYPE_LEVEL_1 -> {
            val levelItem1 = item as FileBean
            helper.setText(R.id.tv, "不通过节点："+levelItem1.name)
            helper.setText(R.id.tv1, levelItem1.failType)
            helper.setText(R.id.tv2, levelItem1.failReason)

        }
            else -> {

            }
        }
    }



}