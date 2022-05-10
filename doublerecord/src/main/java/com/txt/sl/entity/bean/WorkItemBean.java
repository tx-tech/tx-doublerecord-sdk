package com.txt.sl.entity.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/5.
 * description：
 */
public class WorkItemBean implements Serializable {

    /**
     * _id : 5ed4bd679614cc00182a6ee8
     * flowId : 20200601490006
     * insurance : 5ec4f2d98444541f383e9b2c
     * reminder : 0
     * recordUrl : https://s3.cn-north-1.amazonaws.com.cn/gdrb-dingsun-test/20200605/5ed9a9b0d316ed32d6ee5946/record.mp4
     * insuranceName : 国寿康宁终身重大疾病保险2019版
     * insurantName : 欧阳锋
     * insurantPhone : 13544445555
     * insurantIdCard : -
     * insuredMoney : -
     * insuredDate : -
     * payType : -
     * state : 已完成
     * ctime : 2020-06-01 08:33:42
     */

    private String _id;
    private String flowId;
    private String insurance;
    private int reminder;
    private String recordUrl;
    private String insuranceName;
    private String insurantName;
    private String insurantPhone;
    private String insurantIdCard;
    private String insuredMoney;
    private String insuredDate;
    private String payType;
    private String ctime;
    /**
     * relationship : 0
     * repordId : 12345
     */

    private String relationship;
    private String repordId;
    /**
     * insuredIdCard : uu
     * insuredName : yy
     * insuredPhone : 12
     */

    private String insuredIdCard;
    private String insuredName;
    private String insuredPhone;
    /**
     * canEdit : 1
     */

    private int canEdit;
    /**
     * taskId : 1231321
     */

    private String taskId;
    /**
     * utime :
     */

    private String utime;
    /**
     * status :
     */

    private String status;
    private ArrayList<String> membersArray;
    /**
     * isRemote : true
     */

    private boolean isRemote;
    private boolean selfInsurance;
    private String  policyholderUrl;
    private String insuranceUrl;
    private String recordingMethod;

    private String insurerQuotationNo = "";

    public String getInsurerQuotationNo() {
        return insurerQuotationNo;
    }

    public void setInsurerQuotationNo(String insurerQuotationNo) {
        this.insurerQuotationNo = insurerQuotationNo;
    }

    public String getRecordingMethod() {
        return recordingMethod;
    }

    public void setRecordingMethod(String recordingMethod) {
        this.recordingMethod = recordingMethod;
    }

    public String getPolicyholderUrl() {
        return policyholderUrl;
    }

    public void setPolicyholderUrl(String policyholderUrl) {
        this.policyholderUrl = policyholderUrl;
    }

    public String getInsuranceUrl() {
        return insuranceUrl;
    }

    public void setInsuranceUrl(String insuranceUrl) {
        this.insuranceUrl = insuranceUrl;
    }

    public boolean isSelfInsurance() {
        return selfInsurance;
    }

    public void setSelfInsurance(boolean selfInsurance) {
        this.selfInsurance = selfInsurance;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public String getInsuranceName() {
        return insuranceName;
    }

    public void setInsuranceName(String insuranceName) {
        this.insuranceName = insuranceName;
    }

    public String getInsurantName() {
        return insurantName;
    }

    public void setInsurantName(String insurantName) {
        this.insurantName = insurantName;
    }

    public String getInsurantPhone() {
        return insurantPhone;
    }

    public void setInsurantPhone(String insurantPhone) {
        this.insurantPhone = insurantPhone;
    }

    public String getInsurantIdCard() {
        return insurantIdCard;
    }

    public void setInsurantIdCard(String insurantIdCard) {
        this.insurantIdCard = insurantIdCard;
    }

    public String getInsuredMoney() {
        return insuredMoney;
    }

    public void setInsuredMoney(String insuredMoney) {
        this.insuredMoney = insuredMoney;
    }

    public String getInsuredDate() {
        return insuredDate;
    }

    public void setInsuredDate(String insuredDate) {
        this.insuredDate = insuredDate;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }


    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getRepordId() {
        return repordId;
    }

    public void setRepordId(String repordId) {
        this.repordId = repordId;
    }

    public String getInsuredIdCard() {
        return insuredIdCard;
    }

    public void setInsuredIdCard(String insuredIdCard) {
        this.insuredIdCard = insuredIdCard;
    }

    public String getInsuredName() {
        return insuredName;
    }

    public void setInsuredName(String insuredName) {
        this.insuredName = insuredName;
    }

    public String getInsuredPhone() {
        return insuredPhone;
    }

    public void setInsuredPhone(String insuredPhone) {
        this.insuredPhone = insuredPhone;
    }

    public int getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(int canEdit) {
        this.canEdit = canEdit;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getMembersArray() {
        return membersArray;
    }

    public void setMembersArray(ArrayList<String> membersArray) {
        this.membersArray = membersArray;
    }

    public boolean isIsRemote() {
        return isRemote;
    }

    public void setIsRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }
}
