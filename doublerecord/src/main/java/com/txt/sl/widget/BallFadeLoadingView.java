package com.txt.sl.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;



public class BallFadeLoadingView extends View {


    public static final float SCALE=1.0f;

    public static final int ALPHA=255;
    private ArrayList<ValueAnimator> mAnimators;

    public Paint mPaint=new Paint();

    float[] scaleFloats=new float[]{SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE,
            SCALE};

    int[] alphas=new int[]{ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA};



    public BallFadeLoadingView(Context context) {
        super(context);
        init();
    }

    public BallFadeLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init(){
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        onCreateAnimators();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        draw(canvas,mPaint);
    }


    public void draw(Canvas canvas, Paint paint) {
        float radius=getWidth()/10;
        for (int i = 0; i < 8; i++) {
            canvas.save();
            Point point=circleAt(getWidth(),getHeight(),getWidth()/2-radius,i*(Math.PI/4));
            canvas.translate(point.x,point.y);
            canvas.scale(scaleFloats[i],scaleFloats[i]);
            paint.setAlpha(alphas[i]);
            canvas.drawCircle(0,0,radius,paint);
            canvas.restore();
        }
    }


    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators=new ArrayList<>();
        int[] delays= {0, 120, 240, 360, 480, 600, 720, 780, 840};
        for (int i = 0; i < 8; i++) {
            final int index=i;
            ValueAnimator scaleAnim= ValueAnimator.ofFloat(1,0.4f,1);
            scaleAnim.setDuration(1000);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });

            ValueAnimator alphaAnim= ValueAnimator.ofInt(255, 77, 255);
            alphaAnim.setDuration(1000);
            alphaAnim.setRepeatCount(-1);
            alphaAnim.setStartDelay(delays[i]);
            alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    alphas[index] = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
            animators.add(alphaAnim);
        }
        mAnimators = animators;
        return animators;
    }


    public void startAnimators() {
        if(mAnimators==null){
            onCreateAnimators();
        }
        for (int i = 0; i < mAnimators.size(); i++) {
            ValueAnimator animator = mAnimators.get(i);
            animator.start();
        }
    }

    public void stopAnimators() {
        if (mAnimators!=null){
            for (ValueAnimator animator : mAnimators) {
                if (animator != null && animator.isStarted()) {
//                    animator.removeAllUpdateListeners();
                    animator.end();
                }
            }
        }
    }

    /**
     * 圆O的圆心为(a,b),半径为R,点A与到X轴的为角α.
     *则点A的坐标为(a+R*cosα,b+R*sinα)
     * @param width
     * @param height
     * @param radius
     * @param angle
     * @return
     */
    Point circleAt(int width,int height,float radius,double angle){
        float x= (float) (width/2+radius*(Math.cos(angle)));
        float y= (float) (height/2+radius*(Math.sin(angle)));
        return new Point(x,y);
    }

    final class Point{
        public float x;
        public float y;

        public Point(float x, float y){
            this.x=x;
            this.y=y;
        }
    }
}
