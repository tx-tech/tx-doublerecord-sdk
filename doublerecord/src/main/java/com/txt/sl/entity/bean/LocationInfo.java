package com.txt.sl.entity.bean;

/**
 * Created by DELL on 2017/12/29.
 */

public class LocationInfo {
    //经度
    private String altitude;
    //纬度
    private String latitue;
    //位置信息
    private String address;
    //报案所在省
    private String province = "";
    //报案所在市
    private String city = "";

    public String getLatitude() {
        return altitude;
    }

    public void setLatitude(String altitude) {
        this.altitude = altitude;
    }

    public String getLongitude() {
        return latitue;
    }

    public void setLongitude(String latitue) {
        this.latitue = latitue;
    }

    public String getAddress() {
        return address ;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
