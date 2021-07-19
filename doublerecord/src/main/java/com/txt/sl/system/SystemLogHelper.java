package com.txt.sl.system;

import android.os.Environment;

import com.txt.sl.TXSdk;
import com.txt.sl.utils.DateUtils;
import com.txt.sl.utils.LogUtils;
import com.txt.sl.utils.AndroidSystemUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by DELL on 2017/9/4.
 */

public class SystemLogHelper  {
    private final static String TAG = SystemLogHelper.class.getSimpleName();
    private static String PATH_LOGCAT;
    public LogDumper mLogDumper = null;
    private int mPId;
    public String mCurrentLogFile;


    private static volatile SystemLogHelper singleton = null;

    private SystemLogHelper() {
    }

    public static SystemLogHelper getInstance() {
        if (singleton == null) {
            synchronized (SystemLogHelper.class) {
                if (singleton == null) {
                    singleton = new SystemLogHelper();
                }
            }
        }
        return singleton;
    }

    public void init() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "txsl"+"/log";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = TXSdk.getInstance().application.getFilesDir().getAbsolutePath()
                    + File.separator + "txsl"+"/log";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
        mPId = android.os.Process.myPid();
    }

    public void deleteFile(String fileName) {
        try {
            File file = new File(PATH_LOGCAT, fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void start() {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
            mLogDumper.start();
        }
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            String filename = "txsl-" + TXSdk.getInstance().getSDKVersion() + "-" + AndroidSystemUtil.getSystemModel() + "-" + AndroidSystemUtil.getSystemVersion() + "-" + System.currentTimeMillis()+  ".log";
            mCurrentLogFile = dir + "/" + filename;
            LogUtils.d(TAG, "LogDumper: " + mCurrentLogFile);
            try {
                out = new FileOutputStream(new File(dir, filename), true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /**
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             * 显示当前mPID程序的 E和W等级的日志.
             * */
            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
//             cmds = "logcat | grep \"("+"Txlog"+")\"";//打印标签过滤信息
            //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((DateUtils.getDateEN() + "  " + line + "\n")
                                .getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }

            }

        }

    }
}
