package com.txt.sl.entity.bean

/**
 * Created by JustinWjq
 * @date 2019-12-06.
 * description：
 */
data class SaInfoBean(
        var educationStr: String="",//教育程度
        var maritalStr: String="",//婚姻状态
        var memberStr: String="",//家庭成员
        var memberNumberStr: String="",//家庭成员手机号
        var emergencyNameStr: String="",//紧急联系人姓名
        var emergencyNumberStr: String="",//紧急联系人手机号
        var unitStr: String="",//单位名称
        var industryStr: String="",//行业类别
        var positionStr: String="",//职业
        var incomeStr: String="",//税前收入
        var corpPhone: String="",//单位手机号码
        var isNewPhone: Int=-1//是否为全新手机

)