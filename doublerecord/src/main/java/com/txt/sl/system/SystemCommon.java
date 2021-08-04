package com.txt.sl.system;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.txt.sl.TXSdk;
import com.txt.sl.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import static com.txt.sl.utils.WaterMask.DefWaterMaskParam.Location.right_bottom;

/**
 * Created by DELL on 2017/4/18.
 */
public class SystemCommon  {
    private final String TAG = SystemCommon.class.getSimpleName();
    private CameraManager manager;// 声明CameraManager对象

    public boolean mLoginFirst = true;
    public String mMoblieModle;

    private static volatile SystemCommon singleton = null;

    private SystemCommon() {

    }

    public static SystemCommon getInstance() {
        if (singleton == null) {
            synchronized (SystemCommon.class) {
                if (singleton == null) {
                    singleton = new SystemCommon();
                }
            }
        }
        return singleton;
    }

    public boolean isFADMoblie() {

        if (mMoblieModle == null && mMoblieModle.equals("")) {
            return false;
        }
        if (mMoblieModle.toUpperCase().equals("HONOR") || mMoblieModle.toUpperCase().equals("HUAWEI")) {
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init() {
//        manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camerList = manager.getCameraIdList();

            for (String str : camerList) {
                Log.d(TAG, "init: ");
            }
        } catch (CameraAccessException e) {
            Log.e("error", e.getMessage());
        }

    }

    public void lightSwitch(final boolean lightStatus) {
        if (lightStatus) { // 关闭手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", false);
                } catch (Exception e) {
                    Log.d(TAG, "lightSwitch: " + e);
                    e.printStackTrace();
                }
            } else {

            }
        } else { // 打开手电筒

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", true);
                } catch (Exception e) {
                    Log.d(TAG, "lightSwitch: " + e);
                    e.printStackTrace();
                }
            } else {

            }
        }
    }


    /**
     * 判断Android系统版本是否 >= M(API23)
     */
    private boolean isM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * dip转pix
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static Bitmap savePixels(int x, int y, int w, int h, GL10 gl) {
        int b[] = new int[w * (y + h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(x, 0, w, y + h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        for (int i = 0, k = 0; i < h; i++, k++) {//remember, that OpenGL bitmap is incompatible with Android bitmap
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
        Bitmap bp = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return bp;
    }

    public void screenShot(int width, int heigh, String filepath, ShotScreentCallBack callback) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        Log.d(TAG, "screenShot: egl" + egl.toString());
        GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
        Log.d(TAG, "screenShot: egl" + gl.toString());
        Bitmap bp = savePixels(0, 0, width, heigh, gl);
        savePic(bp, filepath, callback);
    }


    // 保存到sdcard
    public void savePic(Bitmap b, String strFileName, ShotScreentCallBack callback) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                b.recycle();
                fos.flush();
                fos.close();
                if (callback != null) {
                    callback.shotscrren(strFileName);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.shotscrren(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.shotscrren(null);
        }
    }

    public interface ShotScreentCallBack {
        public void shotscrren(String path);
    }


    //退出应用
    public void exitApp() {
//        SystemManager.getInstance().destoryAllSysrtm();
        //android.os.Process.killProcess(android.os.Process.myPid());//获取PID
        System.exit(0);
    }

    public boolean matchCarNumber(String carNumber) {
        String carRule = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[警京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{0,1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$";
        String bigCarNumber = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]{1}[A-HJ-NP-Z]{1}[0-9]{5}[DF]{1}$";
        String smallCarnumber = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]{1}[A-HJ-NP-Z]{1}[DF]{1}[A-Z0-9]{1}[0-9]{4}$";
        String carRule1 = "([0-9A-Z]){17}$";

        if (TextUtils.isEmpty(carNumber)) {
            return false;
        }
        return carNumber.matches(carRule) || carNumber.matches(carRule1) || carNumber.matches(bigCarNumber) || carNumber.matches(smallCarnumber);
    }


    public String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    public String byteToBase64(byte[] bytes) {

        byte[] encode = Base64.encode(bytes, Base64.DEFAULT);
        String result = new String(encode);
        return result;
    }

    public String imgToBase64(String imageptah) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageptah);

        LogUtils.i("compressByResolution", bitmap.getWidth()+"图片分辨率压缩后：" + bitmap.getHeight());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();
       LogUtils.i("compressByResolution", "图片分辨率压缩后：" + baos.toByteArray().length / 1024 + "KB");
        byte[] encode = Base64.encode(bytes, Base64.DEFAULT);
        String result = new String(encode);
        return result;
    }

    public int getScreenWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        return width;
    }

    public int getScreenHigh(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int heigh = metrics.heightPixels;
        return heigh;
    }






    //判断网络的连接状态
    public boolean isNetworkAvailable(final Context context) {
        boolean hasWifoCon = false;
        boolean hasMobileCon = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfos = cm.getAllNetworkInfo();
        for (NetworkInfo net : netInfos) {
            String type = net.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                if (net.isConnected()) {
                    hasWifoCon = true;
                }
            }
            if (type.equalsIgnoreCase("MOBILE")) {
                if (net.isConnected()) {
                    hasMobileCon = true;
                }
            }
        }
        return hasWifoCon || hasMobileCon;
    }




    public boolean isOritation(Context context) {
        int flag = 0;
        try {
            int screenchange = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            flag = screenchange;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return flag == 0 ? false : true;
    }




    private static Bitmap getBitmapFromGL(int w, int h, GL10 gl) {
        int b[] = new int[w * (h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);

        gl.glFlush();
        gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0xffff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.RGB_565);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }
    public  byte[] bitmap2RGB(Bitmap bitmap) {
        if (bitmap==null){
            return null;
        }
        int bytes = bitmap.getByteCount();  //返回可用于储存此位图像素的最小字节数

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
        bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        //Bitmap像素点的色彩通道排列顺序是RGBA
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }
        return pixels;
    }

    public byte saveBitmap(Bitmap bitmap)[] {
//        if (isBlack(bitmap)) {
//            return null;
//        }

        String fileDir;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            fileDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "jiexinPhoto";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            fileDir = TXSdk.getInstance().application.getFilesDir().getAbsolutePath()
                    + File.separator + "jiexinPhoto";
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        File picFile = new File(file,  System.currentTimeMillis() + ".jpeg");

        Log.d(TAG, "saveBitmap: picFile" + picFile.getAbsolutePath());
        byte[] mBytes = null;
        try {

            FileOutputStream out = new FileOutputStream(picFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG
                    , 70, out);
            out.flush();
            out.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            mBytes = baos.toByteArray();

            FileInputStream inputStream = new FileInputStream(picFile);
            int size = inputStream.available() / 1024;
            Log.d(TAG, "saveBitmap: size" + size);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            picFile = null;
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            picFile = null;
        }
        if (picFile == null) {
            return null;
        }
        return mBytes;
    }

    public byte saveBitmap1(Bitmap bitmap)[] {
//        if (isBlack(bitmap)) {
//            return null;
//        }
        if (bitmap==null){
            return null;
        }
        String fileDir;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            fileDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "jiexinPhoto";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            fileDir = TXSdk.getInstance().application.getFilesDir().getAbsolutePath()
                    + File.separator + "jiexinPhoto";
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        File picFile = new File(file,  System.currentTimeMillis() + ".jpeg");

        Log.d(TAG, "saveBitmap: picFile" + picFile.getAbsolutePath());
        byte[] mBytes = null;
        try {


            FileOutputStream out = new FileOutputStream(picFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG
                    , 100, out);
            out.flush();
            out.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mBytes = baos.toByteArray();

            FileInputStream inputStream = new FileInputStream(picFile);
            int size = inputStream.available() / 1024;
            Log.d(TAG, "saveBitmap: size" + size);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            picFile = null;
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            picFile = null;
        }
        if (picFile == null) {
            return null;
        }
        return mBytes;
    }

    public boolean isBlack(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int hight = bitmap.getHeight();
        for (int i = 2; i < 6; i++) {
            int color = bitmap.getPixel(width / i, hight / i);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            Log.d(TAG, "isBlack: r" + r + ";g" + g + ";b" + b);
            if (r != 0 || g != 0 || b != 0) {
                return false;
            }
        }
        return true;
    }

    public byte getAssetFileToByte(Context context)[]{
        AssetManager mAssetManager = TXSdk.getInstance().application.getAssets();
        Bitmap bitmap = null;
        byte[] mBytes = null;
        try {
            InputStream open = mAssetManager.open("selfphoto.jpg");
            bitmap =BitmapFactory.decodeStream(open);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return mBytes;
    }

    public void cropBitmap(Bitmap origin,BitmapLister lister){


        int height = origin.getHeight();
        int width = origin.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(origin, 0, 0, width / 2, height, null, false);
        Bitmap bitmap1 = Bitmap.createBitmap(origin, width / 2, 0, width / 2, height, null, false);
        byte[] bytes = byteToBitmap(bitmap);
        byte[] bytes1 = byteToBitmap(bitmap1);

        if (null != lister) {
            lister.onByteListener(bytes,bytes1);
        }

    }

    public byte byteToBitmap(Bitmap bitmap)[]{
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

   public interface BitmapLister{
       void onByteListener(byte[] bytes,byte[] bytes1);
    }

}
