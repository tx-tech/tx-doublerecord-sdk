package com.txt.sl.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/16.
 * description：
 */
public  class AssetUtils {
    /**
     * 从文件中获取字符串
     *
     * @param fileName
     * @param context
     * @return
     */
    public static String getString(String fileName, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 根据字符串返回实体类
     *
     * @param fileName
     * @param context
     * @param tClass
     * @param <T> 实体类
     * @return
     */
    public static <T>  T getJsonObject(String fileName, Context context, Class<T> tClass) {
        String jsonStr = getString(fileName, context);
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, tClass);
    }
}
