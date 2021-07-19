package com.txt.sl.entity.bean;

import java.io.Serializable;

/**
 * Created by JustinWjq
 *
 * @date 2019-10-22.
 * description：
 */
public class SaStepBean implements Serializable {

    /**
     * idcardSub : front back none
     * idcardName : justin
     * idcardAddr : 火星
     * idcardNum : 123131
     */

    private String idcardSub;
    private String idcardName;
    private String idcardAddr;
    private String idcardNum;
    /**
     * saStep : idcard
     */

    private String saStep;

    /**
     * isSAPilot : 0
     */

    private int isSAPilot;

    public String getIdcardSub() {
        return idcardSub;
    }

    public void setIdcardSub(String idcardSub) {
        this.idcardSub = idcardSub;
    }

    public String getIdcardName() {
        return idcardName;
    }

    public void setIdcardName(String idcardName) {
        this.idcardName = idcardName;
    }

    public String getIdcardAddr() {
        return idcardAddr;
    }

    public void setIdcardAddr(String idcardAddr) {
        this.idcardAddr = idcardAddr;
    }

    public String getIdcardNum() {
        return idcardNum;
    }

    public void setIdcardNum(String idcardNum) {
        this.idcardNum = idcardNum;
    }

    public String getSaStep() {
        return saStep;
    }

    public void setSaStep(String saStep) {
        this.saStep = saStep;
    }


    public int getIsSAPilot() {
        return isSAPilot;
    }

    public void setIsSAPilot(int isSAPilot) {
        this.isSAPilot = isSAPilot;
    }
}
