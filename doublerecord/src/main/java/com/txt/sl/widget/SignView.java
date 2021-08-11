package com.txt.sl.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.txt.sl.entity.PointBean;

import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/16.
 * description：
 */
public class SignView extends View {
    public SignView(Context context) {
        super(context);
        init();
        this.context = context;
    }

    public SignView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        this.context = context;
    }

    public SignView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        this.context = context;
    }

    public SignView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        this.context = context;
    }

    Bitmap bitmap = null;
    Path path;
    Rect boundary;
    Canvas canvas1;
    boolean isdraw;//用于判断路径是否为空
    public int bound, stroke;//用来提供给Activity设置使用
    private int width, height;
    private Context context;


    private void init() {
        path = new Path();
        isdraw = false;
        stroke = 8;
        bound = 8;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
        if (width > 0&&height>0) {
            bitmap = Bitmap.createBitmap(width - bound, height - bound, Bitmap.Config.ARGB_8888);
            canvas1 = new Canvas(bitmap);
            boundary = new Rect(bound, bound, width - bound, height - bound);
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width > 0&&height>0) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(stroke);
            canvas.drawPath(path, paint);
            canvas1.drawPath(path, paint);
//        canvas.drawRect(boundary,paint);
        }

    }


    public void drawPath(List<PointBean> mData) {
        clear();
        for (int i = 0; i < mData.size(); i++) {
            PointBean pointBean = mData.get(i);
            String operation = pointBean.getOperation();
            PointBean.LineArrBean lineArr = pointBean.getLineArr();
            if (operation.equals("end")) {

                path.moveTo(lineArr.getCurrentX() * 3f, lineArr.getCurrentY() * 2);
            } else {
                path.lineTo(lineArr.getCurrentX() * 3f, lineArr.getCurrentY() * 2);
            }
            invalidate();
        }

    }

    public void clear() {
        path.reset();
//        bitmap =Bitmap.createBitmap(width-bound, height-bound, Bitmap.Config.ARGB_8888);
//        canvas1 = new Canvas(bitmap);
        invalidate();
    }

}
