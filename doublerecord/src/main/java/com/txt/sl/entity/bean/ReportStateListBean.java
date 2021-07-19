package com.txt.sl.entity.bean;

/**
 * Created by Jutsin on 2018/5/30/030.
 * email：WjqJutin@163.com
 * effect：
 */

public class ReportStateListBean {


    /**
     * _id : 5b0f68acba3917520bfe451d
     * code : unhold
     * color : #000000
     * name : 未接通
     * sortIndex : 1
     */

    private String _id;
    private String code;
    private String color;
    private String name;
    private int sortIndex;
    private String isCall;

    public String getIsCall() {
        return isCall;
    }

    public void setIsCall(String isCall) {
        this.isCall = isCall;
    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}
