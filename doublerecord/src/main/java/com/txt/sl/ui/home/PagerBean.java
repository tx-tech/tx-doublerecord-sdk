package com.txt.sl.ui.home;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/1.
 * description：
 */
public class PagerBean {
    String title;
    String status;

    public PagerBean(String title, String status) {
        this.title = title;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
