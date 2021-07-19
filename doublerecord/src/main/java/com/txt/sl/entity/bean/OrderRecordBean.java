package com.txt.sl.entity.bean;

/**
 * author ：Justin
 * time ：4/27/21.
 * des ：
 */
public class OrderRecordBean {

    /**
     * videoUrl : 12313
     * status : Accepted
     * failType : 12313
     * failReason : 1231
     * utime : 123
     */

    private String videoUrl;
    private String status;
    private String failType;
    private String failReason;
    private String utime;
    /**
     * uploadedTime :
     */

    private String uploadedTime;

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    public String getUploadedTime() {
        return uploadedTime;
    }

    public void setUploadedTime(String uploadedTime) {
        this.uploadedTime = uploadedTime;
    }
}
