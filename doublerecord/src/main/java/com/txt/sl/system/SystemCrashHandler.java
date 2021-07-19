package com.txt.sl.system;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.txt.sl.TXSdk;
import com.txt.sl.utils.ApplicationUtils;
import com.txt.sl.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 2017/5/5.
 */

public class SystemCrashHandler  implements Thread.UncaughtExceptionHandler {
    private static final String TAG = SystemCrashHandler.class.getSimpleName();
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private Map<String, String> infos = new HashMap<String, String>();

    private static volatile SystemCrashHandler singleton = null;

    private SystemCrashHandler() {

    }

    public static SystemCrashHandler getInstance() {
        if (singleton == null) {
            synchronized (SystemCrashHandler.class) {
                if (singleton == null) {
                    singleton = new SystemCrashHandler();
                }
            }
        }
        return singleton;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {


        LogUtils.d(TAG, "uncaughtException: ");
        if (mDefaultHandler != null && !handlerException(throwable)) {
            mDefaultHandler.uncaughtException(thread, throwable);
        } else {
            ApplicationUtils.finishActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
        }


    }

    protected boolean handlerException(Throwable ex) {
        LogUtils.d(TAG, "handlerException");
        if (ex == null) {
            return false;
        } else {
            uploadLogToServer();//奔溃时，log上传
            saveInfoToSD(ex);
            ApplicationUtils.finishActivity();
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
    }

    private void uploadLogToServer() {
        LogUtils.i("uploadLogToServer");
//        getSystem(SystemHttpRequest.class).uploadLogFile(
//                new SystemHttpRequest.onRequestCallBack() {
//                    @Override
//                    public void onSuccess() {
//
//
//                    }
//
//                    @Override
//                    public void onFail(String msg) {
//
//                    }
//                }
//        );
    }


    /**
     * 获取系统未捕捉的错误信息
     *
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }


    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return true;
    }

    private static String PATH_LOGCAT;

    private String saveInfoToSD(Throwable ex) {
        String fileName = null;
        StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, String> entry : obtainSimpleInfo(TXSdk.getInstance().application)
                .entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }

        sb.append(obtainExceptionInfo(ex));
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "txslCrashLog";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = TXSdk.getInstance().application.getFilesDir().getAbsolutePath()
                    + File.separator + "txslCrashLog";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            fileName = file.toString()
                    + File.separator
                    + getAssignTime("yyyy_MM_dd_HH_mm") + "_crash.txt";
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }


    /**
     * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
     *
     * @return
     */
    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        map.put("versionName", mPackageInfo.versionName);
        map.put("versionCode", "" + mPackageInfo.versionCode);
        map.put("MODEL", "" + Build.MODEL);
        map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
        map.put("PRODUCT", "" + Build.PRODUCT);
        map.put("" +
                "", getMobileInfo());
        return map;
    }


    /**
     * 返回当前日期根据格式
     **/
    private String getAssignTime(String dateFormatStr) {
        DateFormat dataFormat = new SimpleDateFormat(dateFormatStr);
        long currentTime = System.currentTimeMillis();
        return dataFormat.format(currentTime);
    }

    /**
     * Cell phone information
     *
     * @return
     */
    public static String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
