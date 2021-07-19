package com.txt.sl.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DensityUtils
{
    public static int dp2px(Context context, float dpValue)
    {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int px2dp(Context context, float pxValue)
    {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    public static int getScreenWidth(Context context)
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);


        return dm.heightPixels;
    }

    public static int sp2px(Context context, float spValue)
    {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getActionBarHeight(Context context){
        TypedArray actionbarSizeTypeArray = context.obtainStyledAttributes(new int []{android.R.attr.actionBarSize});
        return (int)actionbarSizeTypeArray.getDimension(0,0);
    }
}
