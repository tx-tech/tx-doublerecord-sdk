package com.txt.sl.ui.adpter;

import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity;
import com.txt.sl.entity.bean.WorkItemBean;

import java.io.Serializable;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/1.
 * description：
 */
public class WorkerItemTypeBean implements MultiItemEntity, Serializable {

    WorkItemBean workItemBean;

    public WorkerItemTypeBean(WorkItemBean workItemBean) {

        this.workItemBean = workItemBean;
    }

    public WorkItemBean getWorkItemBean() {
        return workItemBean;
    }

    public void setWorkItemBean(WorkItemBean workItemBean) {
        this.workItemBean = workItemBean;
    }

    private int itemType= 1;
    public void setItemType(int itemType){
        this.itemType = itemType ;
    }

    @Override
    public int getItemType() {

        return itemType;
    }
}
