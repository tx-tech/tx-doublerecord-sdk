package com.txt.sl.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.txt.sl.TXSdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pc on 2017/11/8.
 */

public class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();

    public static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 20;
    public static final File FILE_SDCARD = Environment.getExternalStorageDirectory();
    public static final String PATH = "picctwo";
    public static final File FILE_LOCAL = new File(FILE_SDCARD, PATH);
    public static final File FILE_PIC_ICON = new File(FILE_LOCAL, "icon");
    private static String pathDiv = "/";
    public static final String CACHDIR = "AppCache";
    public static File cacheDir = !isExternalStorageWritable() ? TXSdk.getInstance().application.getFilesDir() :
            TXSdk.getInstance().application.getExternalCacheDir();

    private FileUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    /**
     * 创建临时文件
     *
     * @param type 文件类型
     */
    public static File getTempFile(FileType type) {
        try {
            File file = File.createTempFile(type.toString(), null, cacheDir);
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * 获取缓存文件地址
     */
    public static String getCacheFilePath(String fileName) {
        return cacheDir.getAbsolutePath() + pathDiv + fileName;
    }


    /**
     * 判断缓存文件是否存在
     */
    public static boolean isCacheFileExist(String fileName) {
        File file = new File(getCacheFilePath(fileName));
        return file.exists();
    }


    /**
     * 将图片存储为文件
     *
     * @param bitmap 图片
     */
    public static String createFile(Bitmap bitmap, String filename) {
        File f = new File(cacheDir, filename);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "create bitmap file error" + e);
        }
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return null;
    }

    /**
     * 将数据存储为文件
     *
     * @param data 数据
     */
    public static void createFile(byte[] data, String filename) {
        File f = new File(cacheDir, filename);
        try {
            if (f.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "create bitmap file error" + e);
        }
    }


    /**
     * 从URI获取图片文件地址
     *
     * @param context 上下文
     * @param uri     文件uri
     */
    public static String getImageFilePath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String path = null;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat) {
            if (isMediaDocument(uri)) {
                try {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                } catch (IllegalArgumentException e) {
                    path = null;
                }
            }
        }
        if (path == null) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = ((Activity) context).managedQuery(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
            path = null;
        }
        return path;
    }


    /**
     * 从URI获取文件地址
     *
     * @param context 上下文
     * @param uri     文件uri
     */
    public static String getFilePath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            String[] projection = {MediaStore.Images.Media.DATA};

            cursor = resolver.query(uri, projection, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * 判断外部存储是否可用
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.e(TAG, "ExternalStorage not mounted");
        return false;
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public enum FileType {
        IMG,
        AUDIO,
        VIDEO,
        FILE,
    }

    public static String getCacheApkPath(String fileName, Context context) {
        return cacheDir + pathDiv + CACHDIR + pathDiv + fileName;
    }


    public static void deleteApkFile(String absoulePath) {
        File file = new File(absoulePath);
        if (getExtensionName(file.getName()).equals("apk")) {
            file.delete();
        }
    }


    /**
     * 获得apk文件名
     *
     * @param versionName
     * @return
     */
    public static String getApkFileName(String versionName) {
        return new StringBuffer().append("app_v").append(versionName).append(".apk").toString();
    }


    /*
     * Java文件操作 获取文件扩展名
     *
     * Created on: 2011-8-2 Author: blueeagle
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * Created on: 2011-8-2 Author: blueeagle
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static boolean isEnoughFreeSpace() {

        boolean isEnough = false;

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());

        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
                .getBlockSize()) / 1024 / 1024;

        if (sdFreeMB > FREE_SD_SPACE_NEEDED_TO_CACHE) {
            isEnough = true;
        }

        return isEnough;

    }

    public static boolean isEnoughFreeSpace(Context context) {

        boolean isEnough = false;

        StatFs stat = new StatFs(context.getExternalCacheDir().getPath());

        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
                .getBlockSize()) / 1024 / 1024;

        if (sdFreeMB > FREE_SD_SPACE_NEEDED_TO_CACHE) {
            isEnough = true;
        }

        return isEnough;

    }

    public static File createCacheDirectory() throws Exception {
        String path = Environment.getExternalStorageDirectory()
                + pathDiv + CACHDIR;
        File dir = new File(path);
        if (!dir.exists()) {
            System.out.println("cache dir created");
            dir.mkdir();
        }
        return dir;
    }

    public static File createCacheDirectory(Context context) throws Exception {
        String path = context.getExternalCacheDir()
                + pathDiv + CACHDIR;
        File dir = new File(path);
        if (!dir.exists()) {
            System.out.println("cache dir created");
            dir.mkdir();
        }
        return dir;
    }

    public static void deleteDirectoryFiles(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] list = dir.listFiles();
            if (list == null) return;
            for (File file : list) {
                if (file.isDirectory()) {
                    deleteDirectoryFiles(file);
                } else {
                    file.delete();
                }
            }
        }
    }


    public static File createFileInSd(String fileName) throws Exception {

        String SDPATH = Environment.getExternalStorageDirectory() + File.separator;
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    public static File createFileInSd(String fileName, Context context) throws Exception {

        String SDPATH = context.getExternalCacheDir() + File.separator;
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }


    /**
     * 根据图片的大小设置压缩的比例，提高速度
     *
     * @param imageMB
     * @return
     */
    private static int setSubstractSize(int imageMB) {
        if (imageMB > 1000) {
            return 60;
        } else if (imageMB > 750) {
            return 40;
        } else if (imageMB > 500) {
            return 20;
        } else {
            return 10;
        }
    }
    public static byte[] bitmapTobyte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        LogUtils.i("压缩后",""+baos.toByteArray().length/1024 +"k");
        LogUtils.i("压缩后 Height",""+bitmap.getHeight());
        LogUtils.i("压缩后 Width",""+bitmap.getWidth());

        return baos.toByteArray();
    }

    public static Bitmap compressByResolution1(String imgPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, opts);

        int srcWidth = opts.outWidth;
        int srcHeight = opts.outHeight;
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        int scaleF =1;
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 2000) {
                scaleF= 1;
            } else if (longSide < 6000) {
                scaleF= 2;
            } else if (longSide > 6000 && longSide < 10240) {
                scaleF= 4;
            } else {
                scaleF= longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            scaleF= longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            scaleF= (int) Math.ceil(longSide / (1280.0 / scale));
        }
        opts.inSampleSize = scaleF;
        opts.inJustDecodeBounds = false;

       return BitmapFactory.decodeFile(imgPath, opts);

    }


    /**
     * 根据分辨率压缩图片比例
     *
     * @param imgPath
     * @param
     * @return
     */
    public static Bitmap compressByResolution(String imgPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
       BitmapFactory.decodeFile(imgPath, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;

        LogUtils.i("compressByResolution","opts.outWidth"+width+"-----"+"opts.outHeight"+height);
//        int widthScale = width / bitmap1.getWidth();
//        int heightScale = height / bitmap1.getHeight();

        int scale;
        if (width < height) { //保留压缩比例小的
            scale = height/width;
        } else {
            scale = width/height;
        }

        if (scale < 1) {
            scale = 1;
        }
        Log.i(TAG, "图片分辨率压缩比例：" + scale);
        opts.inSampleSize = scale;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, opts);

        return bitmap;
    }


    /**
     * 根据分辨率压缩
     *
     * @param srcPath   图片路径
     * @param ImageSize 图片大小 单位kb
     * @return
     */
    public static boolean compressBitmap(String srcPath, int ImageSize, String savePath) {
        int subtract;
        Log.i(TAG, "图片处理开始.."); ///external_files/rbCamera/1523606091643.jpg
        Bitmap bitmap = compressByResolution(srcPath); //分辨率压缩
        Log.i(TAG, "图片压缩前大小：" + bitmap.getByteCount() / 1024 + "KB" + "宽度为：" + bitmap.getWidth() + "高度为：" + bitmap.getHeight());

        if (bitmap == null) {
            Log.i(TAG, "bitmap 为空");
            return false;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 70;
        bitmap.compress(Bitmap.CompressFormat.WEBP, options, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        Log.i(TAG, "图片分辨率压缩后：" + baos.toByteArray().length / 1024 + "KB");


        while (baos.toByteArray().length > ImageSize * 1024) { //循环判断如果压缩后图片是否大于ImageSize kb,大于继续压缩
            subtract = setSubstractSize(baos.toByteArray().length / 1024);
            baos.reset();//重置baos即清空baos
            options -= subtract;//每次都减少10
//            if (options < 11){
//                break;
//            }
            bitmap.compress(Bitmap.CompressFormat.WEBP, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            Log.i(TAG, "图片压缩后：" + baos.toByteArray().length / 1024 + "KB");
        }
        Log.i(TAG, "图片处理完成!" + baos.toByteArray().length / 1024 + "KB");
        try {
            File file = new File(savePath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);//将压缩后的图片保存的本地上指定路径中
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            bitmap.recycle();
        }

        return true; //压缩成功返回true
    }

    public static int getFileSize(String path) {
        File file = new File(path);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return inputStream.available() / 1024;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 获取全路径中的最长目录
     *
     * @param file 文件
     * @return filePath 最长目录
     */
    public static String getDirName(final File file) {
        if (file == null) return null;
        return getDirName(file.getPath());
    }

    /**
     * 获取全路径中的最长目录
     *
     * @param filePath 文件路径
     * @return filePath 最长目录
     */
    public static String getDirName(final String filePath) {
        if (isSpace(filePath)) return filePath;
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? "" : filePath.substring(0, lastSep + 1);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将文件转为byte[]
     *
     * @param filePath 文件路径
     * @return
     */
    public static byte[] getBytes(Context context, String filePath) {
        LogUtils.INSTANCE.i("filePath" + filePath);
        byte[] sS = {};
        File file = new File(filePath);
        if (!file.exists()) {
            return sS;
        }

        ByteArrayOutputStream out = null;

        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {

                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sS = out.toByteArray();
        return sS;

    }

    /**
     * 将文件转为byte[]
     *
     * @param filePath 文件路径
     * @return
     */
    public static byte[] getBytesToFile(Context context, File file) {

        byte[] sS = {};

        if (!file.exists()) {
            return sS;
        }

        ByteArrayOutputStream out = null;

        try {
            FileInputStream in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = in.read(b)) != -1) {

                out.write(b, 0, b.length);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sS = out.toByteArray();
        LogUtils.i("size",""+sS.length);
        return sS;

    }

    public static byte[] stringToBitmap(String string, int w, int h) {
        Bitmap bitmap = null;
        byte[] bitmapdata = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            byte[] decode = android.util.Base64.decode(string.split(",")[1], Base64.DEFAULT);
            BitmapFactory.decodeByteArray(decode, 0, decode.length, opts);

            int width = opts.outWidth;
            int height = opts.outHeight;

            int widthScale = width / w;
            int heightScale = height / h;

            int scale;
            if (widthScale < heightScale) { //保留压缩比例小的
                scale = widthScale;
            } else {
                scale = heightScale;
            }

            if (scale < 1) {
                scale = 1;
            }
            Log.i(TAG, "图片分辨率压缩比例：" + scale);
            opts.inSampleSize = scale;
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length, opts);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, bos);
            bitmapdata = bos.toByteArray();
            bitmap.recycle();
        } catch (Exception e) {

        }
        return bitmapdata;
    }



}
