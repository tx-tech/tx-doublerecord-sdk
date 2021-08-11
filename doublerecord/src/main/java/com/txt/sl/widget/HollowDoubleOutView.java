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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.txt.sl.R;
import com.txt.sl.utils.LogUtils;

/**
 * author ：Justin
 * time ：5/25/21.
 * des ：
 */
public class HollowDoubleOutView extends FrameLayout {
    private Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;
    private Paint mEraser;
    private float mDensity;
    private Context mContext;

    private float mRadius;
    private int mBackgroundColor;
    private float mRx;//默认在中心位置
    private float mRy;

    public HollowDoubleOutView(@NonNull Context context) {
        super(context);

    }

    public HollowDoubleOutView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView(context, attrs);
    }


    public HollowDoubleOutView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }


    private void initView(Context context, @Nullable AttributeSet attrs) {
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.txFrameLayoutWithHole);
        mBackgroundColor = ta.getColor(R.styleable.txFrameLayoutWithHole_tx_background_color, -1);
        mRadius = ta.getFloat(R.styleable.txFrameLayoutWithHole_tx_hole_radius, 0);
        mRx = ta.getFloat(R.styleable.txFrameLayoutWithHole_tx_radius_x, 0);
        mRy = ta.getFloat(R.styleable.txFrameLayoutWithHole_tx_radius_y, 0);
        init(null, 0);
        ta.recycle();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtils.i("onSizeChanged" + w);
        LogUtils.i("onSizeChanged" + h);

        mRx = w / 4;
        mRy = h / 2;
        mRadius = mRy/2;
        RoundCustomizeView roundCustomizeView = new RoundCustomizeView(mContext, mRx, mRy,mRadius);
        RoundCustomizeView roundCustomizeView1 = new RoundCustomizeView(mContext, mRx*3, mRy,mRadius);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        addView(roundCustomizeView,lp);
        addView(roundCustomizeView1,lp);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setWillNotDraw(false);
        mDensity = mContext.getResources().getDisplayMetrics().density;

        Point size = new Point();
        size.x = mContext.getResources().getDisplayMetrics().widthPixels;
        size.y = mContext.getResources().getDisplayMetrics().heightPixels;


//
//        mRadius = mRadius != 0 ? mRadius : 130;
//
//        mRadius = mRadius * mDensity;

        mBackgroundColor = mBackgroundColor != -1 ? mBackgroundColor : Color.parseColor("#99000000");

        mEraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        mEraserCanvas = new Canvas(mEraserBitmap);


        mEraser = new Paint();
        mEraser.setColor(0xFFFFFFFF);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mEraserBitmap.eraseColor(Color.TRANSPARENT);
        mEraserCanvas.drawColor(mBackgroundColor);
        mEraserCanvas.drawCircle(
                mRx,
                mRy,
                mRadius, mEraser);
        mEraserCanvas.drawCircle(
                mRx*3,
                mRy,
                mRadius, mEraser);
        canvas.drawBitmap(mEraserBitmap, 0, 0, null);

    }

    //先确定两个圆的原点位置
      //先中心点为原点，
}

