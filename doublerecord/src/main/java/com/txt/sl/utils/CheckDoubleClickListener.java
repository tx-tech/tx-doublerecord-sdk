package com.txt.sl.utils;

import android.view.View;

import java.util.Calendar;

/**
 * Created by jaydenWang
 * on 2017/9/11.
 * 功能描述： 防止多次点击
 */

public class CheckDoubleClickListener implements View.OnClickListener {
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;
    private OnCheckDoubleClick checkDoubleClick;

    public CheckDoubleClickListener(OnCheckDoubleClick checkDoubleClick) {
        this.checkDoubleClick = checkDoubleClick;
    }

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long time = currentTime - lastClickTime > 0 ? (currentTime - lastClickTime) : -(currentTime - lastClickTime);
        if (time > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            checkDoubleClick.onCheckDoubleClick(v);
        }
    }
}
