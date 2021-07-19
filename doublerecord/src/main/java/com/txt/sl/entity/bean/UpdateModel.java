package com.txt.sl.entity.bean;

import java.io.Serializable;

/**
 * Created by pc on 2017/11/26.
 */

public class UpdateModel implements Serializable{

    /**
     * appName : 易定损
     * packageName : com.txt.picc
     * versionCode : 2017113001
     * downloadLink : https://192.168.1.65:60100/apks/2b/app_v2017112401.apk
     * force : true
     * des : 1.修改已知bug
     2.新增功能
     */

    private String appName;
    private String packageName;
    private int versionCode;
    private String downloadLink;
    private boolean force;
    private String des;
    private String versionName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
