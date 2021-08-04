package com.txt.sl.ui.adpter

import android.view.View
import android.widget.LinearLayout
import com.common.widget.recyclerviewadapterhelper.base.BaseMultiItemQuickAdapter
import com.common.widget.recyclerviewadapterhelper.base.TxBaseViewHolder
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txt.sl.R
import com.txt.sl.entity.bean.*
import com.txt.sl.utils.GsonUtils
import org.json.JSONObject

/**
 * Created by JustinWjq
 * @date 2020/8/31.
 * description：
 */
public class OrderExpandableItemAdapter1(var data: ArrayList<MultiItemEntity>) :
        BaseMultiItemQuickAdapter<MultiItemEntity, TxBaseViewHolder>(
                data
        ) {
    val TYPE_LEVEL_0 = 0
    val TYPE_LEVEL_1 = 1

    init {
        addItemType(TYPE_LEVEL_0, R.layout.tx_layout_item_product_head)
        addItemType(TYPE_LEVEL_1, R.layout.tx_layout_item_product_subdetails)

    }

    var mDataList = ArrayList<OrderBean>()
    override fun convert(helper: TxBaseViewHolder, item: MultiItemEntity?) {
        when (helper.itemViewType) {
            TYPE_LEVEL_0 -> {
                val levelItem1 = item as ProductLevelItem
                var title = if (levelItem1.insuranceIsMain!! == 1) {
                    "主险"
                } else {
                    "附加险"
                }
                helper.setText(R.id.tv_headtitle, title)
                helper.setText(R.id.tv_insuranceName, levelItem1.insuranceName)

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
                helper.addOnClickListener(R.id.tv_edit, R.id.tv_delete)
                val ll_edit = helper.getView<LinearLayout>(R.id.ll_edit)
                ll_edit.visibility = View.GONE
                val stringStr = GsonUtils.getJson(helper.itemView.context, "ordertypelist.json")
                val json = JSONObject(stringStr)
                val jsonarr = json.getJSONArray("data")

                val mRequestSubOrderBean = item as RequestSubOrderBean
                mDataList = Gson().fromJson<java.util.ArrayList<OrderBean>>(jsonarr.toString(), object : TypeToken<java.util.ArrayList<OrderBean>>() {}.type)

                val insurancePaymentMethodfilter = mDataList?.filter { it.name == "缴费频率" }

                val data = insurancePaymentMethodfilter?.get(0)
                val filter1 = data?.options?.filter {
                    it.key == mRequestSubOrderBean.insurancePaymentMethod
                }

                val insurancePaymentYearUnitfilter = mDataList?.filter { it.name == "保险期间单位" }

                val data1 = insurancePaymentYearUnitfilter?.get(0)
                val filter2 = data1?.options?.filter {
                    it.key == mRequestSubOrderBean.insurancePaymentYearUnit
                }
                val insurancePaymentYearUnitName = filter2?.get(0)?.name


                helper.setText(R.id.tv_insurancePaymentDown, mRequestSubOrderBean.insurancePaymentDown)



                helper.setText(R.id.tv_insurancePaymentMethod, filter1?.get(0)?.name)
                helper.setText(R.id.tv_insurancePaymentPeriods, mRequestSubOrderBean.insurancePaymentPeriods)
                helper.setText(R.id.tv_insurancePaymentPrice, mRequestSubOrderBean.insurancePaymentPrice)

                helper.setText(R.id.tv_insurancePaymentYearUnit, if (insurancePaymentYearUnitName == "终身") {
                    "终身"
                } else if (insurancePaymentYearUnitName == "保至多少岁") {

                    "保至${mRequestSubOrderBean.insurancePaymentYear}岁"
                } else {
                    "${mRequestSubOrderBean.insurancePaymentYear}年"
                }

                )

            }
            else -> {

            }
        }
    }


}