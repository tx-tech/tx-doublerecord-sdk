package com.txt.sl.entity.bean;


import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/27.
 * description：
 */
public  class FileBean implements MultiItemEntity {

    /**
     * name :
     * failType :
     * failReason :
     */

    private String name;
    private String failType;
    private String failReason;

    @Override
    public int getItemType() {
        return 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFailType() {
        return failType;
    }

    public void setFailType(String failType) {
        this.failType = failType;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
