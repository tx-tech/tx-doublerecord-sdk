package com.txt.sl.entity.bean

/**
 * Created by JustinWjq
 * @date 2020/6/17.
 * description：
 */

data class RequestOrderBean(
        var taskId: String = "",
        var agentCertificateType: String = "",
        var agentCertificateNo: String = "",
        var policyholderName: String = "",
        var policyholderCertificateType: String = "",
        var policyholderCertificateNo: String = "",
        var policyholderAge: Int = -1,
        var policyholderGender: String = "",
        var policyholderPhone: String = "",
        var relationship: String = "",
        var insuredName: String = "",
        var insuredCertificateType: String = "",
        var insuredCertificateNo: String = "",
        var insuredAge: Int = -1,
        var insuredGender: String = "",
        var insuredPhone: String = "",
        var saleFrom: String = "",
        var taskFrom: String = "",
        var insuranceAllPaymentDown: String = "",
        var insuranceIsMain :ArrayList<Int> =ArrayList(),
        var insuranceType :ArrayList<String> =ArrayList(),
        var insuranceName :ArrayList<String> =ArrayList(),
        var insurancePaymentDown :ArrayList<String> =ArrayList(),
        var insurancePaymentMethod :ArrayList<String> =ArrayList(),
        var insurancePaymentPeriods :ArrayList<String> =ArrayList(),
        var insurancePaymentPrice :ArrayList<String> =ArrayList(),
        var insurancePaymentYearUnit:ArrayList<String> =ArrayList(),
        var insurancePaymentYear:ArrayList<Int> =ArrayList(),
        var insuranceCode:ArrayList<String> =ArrayList()
) {
    override fun toString(): String {
        return "RequestOrderBean(taskId='$taskId', policyholderName='$policyholderName', policyholderCertificateType='$policyholderCertificateType', policyholderCertificateNo='$policyholderCertificateNo', policyholderAge='$policyholderAge', policyholderGender='$policyholderGender', policyholderPhone='$policyholderPhone', relationship='$relationship', insuredName='$insuredName', insuredCertificateType='$insuredCertificateType', insuredCertificateNo='$insuredCertificateNo', insuredAge='$insuredAge', insuredGender='$insuredGender', insuredPhone='$insuredPhone', saleFrom='$saleFrom', taskFrom='$taskFrom', insuranceAllPaymentDown='$insuranceAllPaymentDown', insuranceIsMain=$insuranceIsMain, insuranceType=$insuranceType, insuranceName=$insuranceName, insurancePaymentDown=$insurancePaymentDown, insurancePaymentMethod=$insurancePaymentMethod, insurancePaymentPeriods=$insurancePaymentPeriods, insurancePaymentPrice=$insurancePaymentPrice, insurancePaymentYearUnit=$insurancePaymentYearUnit, insurancePaymentYear=$insurancePaymentYear, insuranceCode=$insuranceCode)"
    }
}

