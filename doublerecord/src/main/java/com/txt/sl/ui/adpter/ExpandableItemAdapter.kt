package com.txt.sl.ui.adpter

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
public class ExpandableItemAdapter(var data: ArrayList<MultiItemEntity>) :
    BaseMultiItemQuickAdapter<MultiItemEntity, TxBaseViewHolder>(
        data
    ) {
    val TYPE_LEVEL_0 = 0
    val TYPE_LEVEL_1 = 1

    init {
        addItemType(TYPE_LEVEL_0, R.layout.tx_layout_item_head)
        addItemType(TYPE_LEVEL_1, R.layout.tx_adapter_file_list_item)

    }

    override fun convert(helper: TxBaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            TYPE_LEVEL_0 -> {
                val levelItem1 = item as LevelItem1
                helper.setText(R.id.tx_tv_headtitle, levelItem1.title)
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
            helper.setText(R.id.tv, levelItem1.name)
            helper.setText(R.id.tv2, levelItem1.failType)
            helper.setText(R.id.tv3, levelItem1.failReason)

        }
            else -> {

            }
        }
    }



}