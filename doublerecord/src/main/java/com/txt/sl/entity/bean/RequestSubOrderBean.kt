package com.txt.sl.entity.bean

import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity
import java.io.Serializable

/**
 * Created by JustinWjq
 * @date 2020/6/17.
 * description：
 */

data class RequestSubOrderBean(
        var insuranceIsMain: Int = -1,
        var insuranceType: String = "",
        var insuranceName: String = "",
        var insurancePaymentDown: String = "",
        var insurancePaymentMethod: String = "",
        var insurancePaymentPeriods: String = "",
        var insurancePaymentPrice: String = "",
        var insurancePaymentYearUnit: String = "",
        var insurancePaymentYear: Int = -1,
        var insuranceCode: String = "",
        var insuranceCompany: String = "",
        var ensureTheRenewal: Boolean = false
) : Serializable, MultiItemEntity {


    override fun getItemType(): Int = 1
    override fun toString(): String {
        return "RequestSubOrderBean(insuranceIsMain=$insuranceIsMain, insuranceType='$insuranceType', insuranceName='$insuranceName', insurancePaymentDown='$insurancePaymentDown', insurancePaymentMethod='$insurancePaymentMethod', insurancePaymentPeriods='$insurancePaymentPeriods', insurancePaymentPrice='$insurancePaymentPrice', insurancePaymentYearUnit='$insurancePaymentYearUnit', insurancePaymentYear=$insurancePaymentYear, insuranceCode='$insuranceCode')"
    }

}

