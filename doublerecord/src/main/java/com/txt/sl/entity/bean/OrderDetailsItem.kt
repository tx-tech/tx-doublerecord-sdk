package com.txt.sl.entity.bean

import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.txt.sl.ui.adpter.OrderExpandableItemAdapter1.Companion.TYPE_LEVEL_2

/**
 * author ：Justin
 * time ：4/28/21.
 * des ：
 */
data class OrderDetailsItem(
    val title: String,
    val value: String
): MultiItemEntity {
    override fun getItemType(): Int = TYPE_LEVEL_2
}