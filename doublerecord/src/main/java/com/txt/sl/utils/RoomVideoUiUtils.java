package com.txt.sl.utils;

import android.content.Context;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by JustinWjq
 *
 * @date 2020/7/3.
 * description：
 */
public class RoomVideoUiUtils {
    public static int getWindowWidth(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public static int getWindowHeight(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 堆叠布局初始化参数：
     * <p>
     * 大画面在最下面，左右两排各三个小画面
     *
     * @param context
     * @param layoutWidth
     * @param layoutHeight
     * @return
     */
    public static ArrayList<RelativeLayout.LayoutParams> initFloatParamList(Context context, int layoutWidth, int layoutHeight) {
        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<RelativeLayout.LayoutParams>();
        // 底部最大的布局
        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        list.add(layoutParams0);

        final int midMargin = RoomVideoUiUtils.dip2px(context, 10);
        final int lrMargin = RoomVideoUiUtils.dip2px(context, 15);
        final int bottomMargin = RoomVideoUiUtils.dip2px(context, 50);
        final int subWidth = RoomVideoUiUtils.dip2px(context, 120);
        final int subHeight = RoomVideoUiUtils.dip2px(context, 180);

        for (int i = 0; i < 3; i++) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(subWidth, subHeight);
            layoutParams.leftMargin = layoutWidth - lrMargin - subWidth;
            layoutParams.topMargin = layoutHeight - (bottomMargin + midMargin * (i + 1) + subHeight * i) - subHeight;
            list.add(layoutParams);
        }

        for (int i = 0; i < 3; i++) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(subWidth, subHeight);
            layoutParams.leftMargin = lrMargin;
            layoutParams.topMargin = layoutHeight - (bottomMargin + midMargin * (i + 1) + subHeight * i) - subHeight;
            list.add(layoutParams);
        }
        return list;
    }


    /**
     * 四宫格布局参数
     *
     * @param context
     * @param layoutWidth
     * @param layoutHeight
     * @return
     */
    public static ArrayList<RelativeLayout.LayoutParams> initGrid4Param(Context context, int layoutWidth, int layoutHeight) {
        int margin = dip2px(context, 10);
        int bottomMargin = dip2px(context, 50);

        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<>();
        int grid4W = (layoutWidth - margin * 2) / 10;
        int grid4H = (layoutHeight - margin * 2 - bottomMargin) / 3;

        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(grid4W * 7, layoutWidth);
        layoutParams0.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams0.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams0.topMargin = margin;
        layoutParams0.leftMargin = margin;


        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams1.rightMargin = margin;
        layoutParams1.topMargin = margin;

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams2.topMargin = margin + grid4H;
        layoutParams2.rightMargin = margin;

        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams3.topMargin = margin*2 + grid4H*2;
        layoutParams3.rightMargin = margin;

        list.add(layoutParams0);
        list.add(layoutParams1);
        list.add(layoutParams2);
        list.add(layoutParams3);
        return list;
    }

    public static ArrayList<RelativeLayout.LayoutParams> initLocalView(Context context, int layoutWidth, int layoutHeight) {


        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<>();
        int grid4W = layoutWidth / 10;
        int grid4H = layoutHeight  / 2;
        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(grid4W * 7, ViewGroup.LayoutParams.MATCH_PARENT);


        LogUtils.i("layoutWidth-"+layoutWidth+"layoutHeight"+layoutHeight);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);



        list.add(layoutParams0);
        list.add(layoutParams1);
        return list;
    }

    public static ArrayList<RelativeLayout.LayoutParams> initThreeView(Context context, int layoutWidth, int layoutHeight) {

        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<>();
        int grid4W = layoutWidth / 10;
        int grid4H = layoutHeight  / 3;
        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(grid4W * 7, ViewGroup.LayoutParams.MATCH_PARENT);


        LogUtils.i("layoutWidth-"+layoutWidth+"layoutHeight"+layoutHeight);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams2.topMargin = grid4H;
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams3.topMargin = grid4H*2;
        list.add(layoutParams0);
        list.add(layoutParams1);
        list.add(layoutParams2);
        list.add(layoutParams3);
        return list;
    }

    public static ArrayList<RelativeLayout.LayoutParams> initTwoView(Context context, int layoutWidth, int layoutHeight) {

        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<>();
        int grid4W = layoutWidth / 10;
        int grid4H = layoutHeight  / 2;
        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(grid4W * 7, ViewGroup.LayoutParams.MATCH_PARENT);


        LogUtils.i("layoutWidth-"+layoutWidth+"layoutHeight"+layoutHeight);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


        LogUtils.i("layoutWidth-"+layoutWidth+"layoutHeight"+layoutHeight);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(grid4W * 3, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        list.add(layoutParams0);
        list.add(layoutParams1);
        list.add(layoutParams2);
        return list;
    }


    public static ArrayList<RelativeLayout.LayoutParams> initRemoteTwoView(Context context, int layoutWidth, int layoutHeight) {

        ArrayList<RelativeLayout.LayoutParams> list = new ArrayList<>();
        int grid4W = layoutWidth / 10;
        int grid4H = layoutHeight  / 2;
        RelativeLayout.LayoutParams layoutParams0 = new RelativeLayout.LayoutParams(grid4W * 7, ViewGroup.LayoutParams.MATCH_PARENT);


        LogUtils.i("layoutWidth-"+layoutWidth+"layoutHeight"+layoutHeight);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(grid4W * 3, grid4H);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams2.topMargin = grid4H;

        list.add(layoutParams0);
        list.add(layoutParams1);
        list.add(layoutParams2);
        return list;
    }

}
