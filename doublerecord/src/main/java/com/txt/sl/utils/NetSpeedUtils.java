package com.txt.sl.utils;

import android.net.TrafficStats;

/**
 * author ：Justin
 * time ：2021/7/15.
 * des ：
 */
class NetSpeedUtils {
    private static final String TAG = NetSpeedUtils.class.getSimpleName();
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private static volatile NetSpeedUtils singleton = null;

    private NetSpeedUtils() {

    }

    public static NetSpeedUtils getInstance() {
        if (singleton == null) {
            synchronized (NetSpeedUtils.class) {
                if (singleton == null) {
                    singleton = new NetSpeedUtils();
                }
            }
        }
        return singleton;
    }
    public String getNetSpeed(int uid) {
        long nowTotalRxBytes = getTotalRxBytes(uid);
        LogUtils.i(TAG, "nowTotalRxBytes  = " + nowTotalRxBytes);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return String.valueOf(speed) + " kb/s";
    }

    public long getNetSpeedLong(int uid) {
        long nowTotalRxBytes = getTotalRxBytes(uid);
        LogUtils.i(TAG, "nowTotalRxBytes  = " + nowTotalRxBytes);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return speed;
    }

    //getApplicationInfo().uid
    public long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);
    }
}
