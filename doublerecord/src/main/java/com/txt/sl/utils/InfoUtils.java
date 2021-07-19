package com.txt.sl.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by pc on 2017/10/20.
 */

public class InfoUtils {

    public static String getCarrier(Context context) {
        TelephonyManager telephonyManager = getTelephonyManager(context);
        String simOperitor = telephonyManager.getSimOperator();
        if (!TextUtils.isEmpty(simOperitor)) {
            try {
                simOperitor = simOperitor.substring(0, 6);
            } catch (Exception var5) {
            } finally {
            }
            return simOperitor;
        } else {
            simOperitor = "";
            return simOperitor;
        }
    }

    public static String getTerminal() {
        return "ttdb_android";
    }

    public static String getAppv(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                return String.valueOf(packageManager.getPackageInfo(context.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException var3) {
                var3.printStackTrace();
            }
        }
        return "";
    }

    public static String getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                return String.valueOf(packageManager.getPackageInfo(context.getPackageName(), 0).versionCode);
            } catch (PackageManager.NameNotFoundException var3) {
                var3.printStackTrace();
            }
        }
        return "";
    }

    public static String getEquipment() {
        return "android";
    }

//    public static String getPhone(Context context) {
//        TelephonyManager telephonyManager = getTelephonyManager(context);
//        return TextUtils.isEmpty(telephonyManager.getLine1Number())? "" : telephonyManager.getLine1Number();
//    }

    public static TelephonyManager getTelephonyManager(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getMfr() {
        return Build.DEVICE;
    }

    public static String getRes(Context mContext) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int widthPixels = display.getWidth();
        int heightPixels = display.getHeight();
        return heightPixels + "*" + widthPixels;
    }

    public static String getNettype(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        String mNetWorkType = "";
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetWorkType = "WIFI";
            } else if (type.equalsIgnoreCase("MOBILE")) {
                mNetWorkType = "MOBILE";
            }
        } else {
            mNetWorkType = "网络异常";
        }

        return mNetWorkType;
    }


    /**
     * androidId + build.serial 不需要权限 可以获得 除非出厂化设置 那么会改变
     * deviceid 前提需要获得权限 才能拿到
     */
    public static String getDid(Context context) {
//        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceId = telephonyManager.getDeviceId();
//        if(TextUtils.isEmpty(deviceId)){
//            Log.d("wdy", "getDid: "+deviceId);
//            Log.d("wdy", "getDid: "+readUUID());
//            Log.d("wdy", "getDid: "+getIdentity(context));
//            return readUUID() != null ? readUUID() : getIdentity(context);
//        }else{
//            return deviceId;
//        }
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID ;
        return id;
    }

    public static boolean isRunningForeground(Context context) {
        String packageName = context.getPackageName();
        String topActivityClassName = getTopActivityName(context);
        return packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName);
    }

    private static String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = ((ActivityManager.RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
            topActivityClassName = f.getClassName();
        }

        return topActivityClassName;
    }

    private static String getIdentity(Context context) {
        String identity = UUID.randomUUID().toString();
        if (isSdCardExist()) {
            saveUUID(identity);
        }

        return identity;
    }


    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static void saveUUID(String uuid) {
        try {
            File e = new File(Environment.getExternalStorageDirectory(), "identity.value");
            if (!e.exists()) {
                e.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(e);
            fos.write(uuid.getBytes());
            fos.close();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static String readUUID() {
        try {
            File e = new File(Environment.getExternalStorageDirectory(), "identity.value");
            FileInputStream is = new FileInputStream(e);
            byte[] b = new byte[is.available()];
            is.read(b);
            String result = new String(b);
            return result;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
