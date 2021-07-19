package com.txt.sl.entity.bean;

import java.io.Serializable;

/**
 * Created by pc on 2017/10/20.
 */

public class SearchCarMoudle implements Serializable {


    private String accountId;
    private String keyword;
    private int pageSize;
    private int pageIndex;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Override
    public String toString() {
        return "SearchCarMoudle{" +
                "accountId='" + accountId + '\'' +
                ", keyword='" + keyword + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", pageIndex='" + pageIndex + '\'' +
                '}';
    }
}
