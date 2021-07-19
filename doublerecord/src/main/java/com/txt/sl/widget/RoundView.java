package com.txt.sl.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.SweepGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.txt.sl.R;
import com.txt.sl.utils.LogUtils;

/**
 * author ：Justin
 * time ：5/25/21.
 * des ：
 */
public class RoundView extends View {
    private Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;
    private Paint mEraser;
    private float mDensity;
    private Context mContext;

    private float mRadius;
    private int mBackgroundColor;
    private float mRx;//默认在中心位置
    private float mRy;

    public RoundView(@NonNull Context context) {
        super(context);

    }

    public RoundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView(context, attrs);
    }


    public RoundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    private void initView(Context context, @Nullable AttributeSet attrs) {
        mContext = context;
        mDensity = mContext.getResources().getDisplayMetrics().density;


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup parent = (ViewGroup) getParent();
        if (null != parent) {
//            mRx =  parent.getWidth()/2;
//            mRy = parent.getHeight()/2;
            LogUtils.i("" + parent.getWidth() / 2);
            LogUtils.i("" + parent.getHeight() / 2);
        }
    }

    SweepGradient sweepGradient;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtils.i("onSizeChanged" + w);
        LogUtils.i("onSizeChanged" + h);
        mRx = w / 2;
        mRy = h / 2;
        int[] colors = {0xff006EFF, 0x00006EFF};
        sweepGradient = new SweepGradient(mRx, mRy, colors, null);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //circleWidth 圆的直径 取中心点
        Paint paint = new Paint();
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(sweepGradient);
        canvas.drawCircle(mRx, mRy, mRy-20, paint);

    }

}

