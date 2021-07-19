package com.txt.sl.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.support.annotation.NonNull;


import com.txt.sl.screenrecorder.ScreenRecordHelper;
import com.txt.sl.ui.video.RoomActivity;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public final class ApplicationUtils {

    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    static WeakReference<Activity> sTopActivityWeakRef;
    static List<Activity> sActivityList = new LinkedList<>();
    public static boolean isContainVideo = false;

    private static ActivityLifecycleCallbacks mCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
//            ScreenRecordHelper.bapd();
            if (activity instanceof RoomActivity) {
                isContainVideo = true;
            }


            sActivityList.add(activity);

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivityWeakRef(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity instanceof RoomActivity) {
                isContainVideo = false;
            }

            sActivityList.remove(activity);
        }
    };

    private ApplicationUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param app 应用
     */
    public static void init(@NonNull final Application app) {
        ApplicationUtils.sApplication = app;
        app.registerActivityLifecycleCallbacks(mCallbacks);
    }

    /**
     * 获取 Application
     *
     * @return Application
     */
    public static Application getApp() {
        if (sApplication != null) return sApplication;
        throw new NullPointerException("u should init first");
    }

    public static List<Activity> getActivityList() {
        return sActivityList;

    }

    public static void finishActivity() {
        for (Activity activity : sActivityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }

        }

    }

    public static Activity getCurrentActivity(){
        Activity currentActivity = null;
        if (sTopActivityWeakRef!=null){
            currentActivity=  sTopActivityWeakRef.get();
        }
        return currentActivity;
    }

    private static void setTopActivityWeakRef(final Activity activity) {

        if (sTopActivityWeakRef == null || !activity.equals(sTopActivityWeakRef.get())) {
            sTopActivityWeakRef = new WeakReference<Activity>(activity);
        }
    }
}
