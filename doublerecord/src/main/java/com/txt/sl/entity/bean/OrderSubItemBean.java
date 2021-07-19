package com.txt.sl.entity.bean;

/**
 * author ：Justin
 * time ：4/27/21.
 * des ：
 */
public class OrderSubItemBean {

    /**
     * _id : 60869038db866029a969ba6f
     * type : 传统险
     * name : 爱宝保传统险
     * code : abb
     * isMain : true
     * tenant : {"tenantId":"60598f17eb8ddb4538e47da0"}
     */

    private String _id;
    private String type;
    private String name;
    private String code;
    private boolean isMain;
    private TenantBean tenant;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isIsMain() {
        return isMain;
    }

    public void setIsMain(boolean isMain) {
        this.isMain = isMain;
    }

    public TenantBean getTenant() {
        return tenant;
    }

    public void setTenant(TenantBean tenant) {
        this.tenant = tenant;
    }

    public static class TenantBean {
        /**
         * tenantId : 60598f17eb8ddb4538e47da0
         */

        private String tenantId;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }
}
