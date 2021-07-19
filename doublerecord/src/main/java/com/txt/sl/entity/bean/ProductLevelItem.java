package com.txt.sl.entity.bean;

import com.common.widget.recyclerviewadapterhelper.base.entity.AbstractExpandableItem;
import com.common.widget.recyclerviewadapterhelper.base.entity.MultiItemEntity;

import java.io.Serializable;

/**
 * Created by JustinWjq
 *
 * @date 2020/8/31.
 * description：
 */
public class ProductLevelItem extends AbstractExpandableItem<RequestSubOrderBean> implements MultiItemEntity, Serializable {
    private String title;
    /**
     * insuranceIsMain : -1
     *  insuranceType :
     * insuranceName :
     */

    private int insuranceIsMain;
    private String insuranceType;
    private String insuranceName;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public ProductLevelItem() {
    }


    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getInsuranceName() {
        return insuranceName;
    }

    public void setInsuranceName(String insuranceName) {
        this.insuranceName = insuranceName;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return 0;
    }

    public int getInsuranceIsMain() {
        return insuranceIsMain;
    }

    public void setInsuranceIsMain(int insuranceIsMain) {
        this.insuranceIsMain = insuranceIsMain;
    }
}
