package com.txt.sl.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;

import com.txt.sl.TXSdk;
import com.txt.sl.entity.bean.CheckEnvItem;
import com.txt.sl.system.SystemActivityManager;
import com.txt.sl.system.SystemCommon;

import java.io.File;
import java.util.ArrayList;

/**
 * author ：Justin
 * time ：6/4/21.
 * des ：检测双录需要的条件（噪音，光线，电量，存储空间，扬声器音量，网络带宽）
 */
public class CheckEnvUtils {
    public static final String TAG = CheckEnvUtils.class.getSimpleName();

    private static volatile CheckEnvUtils singleton = null;

    private CheckEnvUtils() {

    }

    public static CheckEnvUtils getInstance() {
        if (singleton == null) {
            synchronized (CheckEnvUtils.class) {
                if (singleton == null) {
                    singleton = new CheckEnvUtils();
                }
            }
        }
        return singleton;
    }


    Sensor defaultSensor;
    LightSensorListener mLightSensorListener;
    SensorManager mSensorManager;

    //检测光线
    public void startLight(Context context) {
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        defaultSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (null != defaultSensor) {
            mLightSensorListener = new LightSensorListener();
            mSensorManager.registerListener(mLightSensorListener, defaultSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public float getLux() {
        if (null != mLightSensorListener) return mLightSensorListener.lux;
        return -1.0f;
    }

    public void stopLight() {
        if (null != mLightSensorListener) {
            mSensorManager.unregisterListener(mLightSensorListener);
        }

    }

    BroadcastReceiver mBatteryStateBroadcastReceiver;
    int batteryLevel;
    int batteryStatus;

    //检测电量
    public void startBatteryState(Context context) {
        mBatteryStateBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                batteryLevel = intent.getIntExtra("level", -1);
                batteryStatus = intent.getIntExtra("status", -1);
                LogUtils.i("BatteryStateBroadcastReceiver", "level" + batteryLevel);
                LogUtils.i("BatteryStateBroadcastReceiver", "status" + batteryStatus);
            }
//
        };
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(mBatteryStateBroadcastReceiver, intentFilter);
    }

    String batterName = "";

    public boolean getBatterLevelAndStatus() {
        if (batteryLevel >= 30) {
            return true;
        } else {

            if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                batterName = "手机电量不合格，录制时请保持充电状态";
            } else {
                batterName = "手机电量不合格，请连接充电线";
            }

            return false;
        }
    }

    //获取不合格的文案
    public String getBatterName() {
        return batterName;
    }

    public void stopBatteryState(Context context) {
        if (null != mBatteryStateBroadcastReceiver) {
            context.unregisterReceiver(mBatteryStateBroadcastReceiver);
        }
    }

    //检测内存空间
    public long getAvailableInternalMemorySize() {
        File dataDirectory = Environment.getDataDirectory();
        StatFs statFs = new StatFs(dataDirectory.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        return availableBlocksLong * blockSizeLong / 1024 / 1024 ;
    }

    //检测扬声器的声量
    public int getVolume(Context context, boolean isRemote) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        double streamVolume1;
        double streamMaxVolume;
        if (isRemote) {
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
//            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
//                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);

            streamVolume1 = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        } else {
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FX_KEY_CLICK);

            streamVolume1 = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        LogUtils.i("LightSensorListener", "streamVolume : " + streamVolume1);
        LogUtils.i("LightSensorListener", "streamMaxVolume : " + streamMaxVolume);
        double i = streamVolume1 / streamMaxVolume;
        LogUtils.i("LightSensorListener", "i : " + (int) (i * 100));
        return (int) (i * 100);
    }


    private class LightSensorListener implements SensorEventListener {
        private float lux; // 光线强度

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                // 获取光线强度
                lux = event.values[0];
                LogUtils.i("LightSensorListener", "lux : " + lux);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    public void startCheckEnv(Context context, boolean isRemote) {
        mCheckEnvResult = true ;
        //获取噪音

        //获取光线
        startLight(context);
        //获取电量
        startBatteryState(context);
        //获取存储空间
//        long availableInternalMemorySize = getAvailableInternalMemorySize();
//        LogUtils.i(TAG,availableInternalMemorySize+"M");
        //获取扬声器音量

        //获取网络带宽
        NetSpeedUtils.getInstance().getNetSpeed(TXSdk.getInstance().application.getApplicationInfo().uid);

    }

    private boolean mCheckEnvResult = true;
    public boolean  getCheckEnvResult(){
        return mCheckEnvResult;
    }

    private boolean mCheckvolumeAndMemory = true;
    public boolean  getCheckvolumeAndMemory(){
        return mCheckvolumeAndMemory;
    }

    public void getCheckEnv(Context context, boolean isRemote) {
        //噪音值
        noiseItem.setUploading(true);
        noiseItem.setPass(true);
        noiseItem.setValue("环境噪音值合格");
        //获取光线
        float lux = getLux();
        if (lux > 20 && lux < 500) {
            luxItem.setUploading(true);
            luxItem.setPass(true);
            luxItem.setValue("环境光线合格");
        } else {
            luxItem.setUploading(true);
            luxItem.setPass(false);
            luxItem.setValue("环境光线值不合格，请保持光线适中");
            mCheckEnvResult = false ;
        }

        //获取电量
        boolean batterLevelAndStatus = getBatterLevelAndStatus();
        if (batterLevelAndStatus) {
            mBatterLevelAndStatusItem.setUploading(true);
            mBatterLevelAndStatusItem.setPass(true);
            mBatterLevelAndStatusItem.setValue("手机电量合格");
        } else {
            mBatterLevelAndStatusItem.setUploading(true);
            mBatterLevelAndStatusItem.setPass(false);
            mBatterLevelAndStatusItem.setValue(batterName);
            mCheckEnvResult = false ;
        }

        //获取存储空间
        long availableInternalMemorySize = getAvailableInternalMemorySize();
        LogUtils.i(TAG, availableInternalMemorySize + "M");
        if (availableInternalMemorySize>500) {
            mMemoryItem.setUploading(true);
            mMemoryItem.setPass(true);
            mMemoryItem.setValue("手机存储空间合格");
        }else{
            mMemoryItem.setUploading(true);
            mMemoryItem.setPass(false);
            mMemoryItem.setValue("手机存储空间不合格，请预留500M以上的存储空间");
            mCheckEnvResult = false ;
            mCheckvolumeAndMemory = false;
        }
        //获取扬声器音量
        int volume = getVolume(context, isRemote);
        if (volume>=60) {
            volumeItem.setUploading(true);
            volumeItem.setPass(true);
            volumeItem.setValue("手机扬声器音量合格");
        }else{
            volumeItem.setUploading(true);
            volumeItem.setPass(false);
            volumeItem.setValue("手机扬声器音量不合格，请调高扬声器音量");
            mCheckEnvResult = false ;
            mCheckvolumeAndMemory = false;
        }
        //获取网络带宽 128kb/s = 1Mbps带宽
        long netSpeed = NetSpeedUtils.getInstance().getNetSpeedLong(TXSdk.getInstance().application.getApplicationInfo().uid);
        TxLogUtils.i(TAG,""+netSpeed);
        netSpeedItem.setUploading(true);
        netSpeedItem.setPass(true);
        netSpeedItem.setValue("网络带宽合格");
//        if (netSpeed>=128) {
//
//        }else{
//            netSpeedItem.setUploading(true);
//            netSpeedItem.setPass(false);
//            netSpeedItem.setValue("网络带宽不合格，请更换网络环境");
//            mCheckEnvResult = false ;
//        }

    }

    public void stopCheckEnv(Context context) {
        //获取噪音

        //获取光线
        stopLight();
        //获取电量
        stopBatteryState(context);
        //获取存储空间

        //获取扬声器音量

        //获取网络带宽

    }

    CheckEnvItem noiseItem;
    CheckEnvItem luxItem;
    CheckEnvItem mBatterLevelAndStatusItem;
    CheckEnvItem mMemoryItem;
    CheckEnvItem volumeItem;
    CheckEnvItem netSpeedItem;
    ArrayList<CheckEnvItem> checkEnvItems = new ArrayList<>();
    public ArrayList<CheckEnvItem> getEnvData() {
        mCheckvolumeAndMemory = true;
        mCheckEnvResult = true;
        checkEnvItems.clear();
        //获取噪音
        noiseItem = new CheckEnvItem(true, "环境噪音值检测中", false);
        checkEnvItems.add(noiseItem);

        luxItem = new CheckEnvItem(true, "环境光线值检测中", false);
        checkEnvItems.add(luxItem);

        mBatterLevelAndStatusItem = new CheckEnvItem(true, "手机电量检测中", false);
        checkEnvItems.add(mBatterLevelAndStatusItem);
        //获取存储空间

        mMemoryItem = new CheckEnvItem(true, "手机存储空间检测中", false);
        checkEnvItems.add(mMemoryItem);

        volumeItem = new CheckEnvItem(true, "手机扬声器音量检测中", false);
        checkEnvItems.add(volumeItem);


        netSpeedItem = new CheckEnvItem(true, "网络带宽检测中", false);
        checkEnvItems.add(netSpeedItem);


        return checkEnvItems;
    }

}
