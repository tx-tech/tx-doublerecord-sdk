package com.txt.sl.widget;

/**
 * author ：Justin
 * time ：5/13/21.
 * des ：
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.txt.sl.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by justin on 2020/3/5.
 * 原生电子签名
 */
public class SignatureView extends View {
    private Context context;
    //X轴起点
    private float x;
    //Y轴起点
    private float y;
    //画笔
    private final Paint paint = new Paint();
    //路径
    private final Path path = new Path();
    //画布
    private Canvas canvas;
    //生成的图片
    private Bitmap bitmap;
    //画笔的宽度
    private int paintWidth = 10;
    //签名颜色
    private int paintColor = Color.BLACK;
    //背景颜色
    private int backgroundColor = Color.TRANSPARENT;
    //是否已经签名
    private boolean isTouched = false;
    //签名开始与结束
    public interface Touch {
        void OnTouch(boolean isTouch);
    }
    private Touch touch;
    public SignatureView(Context context) {
        super(context);
        init(context);
    }
    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(Context context) {
        this.context = context;
        //抗锯齿
        paint.setAntiAlias(true);
        //样式
        paint.setStyle(Paint.Style.STROKE);
        //画笔颜色
        paint.setColor(paintColor);
        //画笔宽度
        paint.setStrokeWidth(paintWidth);
        setBackground(context.getDrawable(R.drawable.bg_sign));
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //创建于view大小一致的bitmap
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundColor);
        isTouched = false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touch != null) touch.OnTouch(true);
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                touchDwon(event);
                break;
            //手指移动
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                if (touch != null) touch.OnTouch(false);
                touchMove(event);
                break;
            //手指抬起
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                break;
        }
        // 更新绘制
        invalidate();
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画此次笔画之前的签名
        canvas.drawBitmap(bitmap, 0, 0, paint);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(path, paint);
    }
    //手指按下的方法
    private void touchDwon(MotionEvent event) {
        //重置绘制路径
        path.reset();
        float downX = event.getX();
        float downY = event.getY();
        x = downX;
        y = downY;
        //绘制起点
        path.moveTo(downX, downY);
    }
    //手指滑动的方法
    private void touchMove(MotionEvent event) {
        //当前的x,y坐标点
        final float moveX = event.getX();
        final float moveY = event.getY();
        //之前的x,y坐标点
        final float previousX = x;
        final float previousY = y;
        //获取绝对值
        final float dx = Math.abs(moveX - previousX);
        final float dy = Math.abs(moveY - previousY);
        if (dx >= 3 || dy >= 3) {
            float cX = (moveX + previousX) / 2;
            float cY = (moveY + previousY) / 2;
            path.quadTo(previousX, previousY, cX, cY);
            x = moveX;
            y = moveY;
        }
    }
    /**
     * 设置画笔颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
        paint.setColor(paintColor);
    }
    /**
     * 设置画笔宽度
     *
     * @param paintWidth
     */
    public void setPaintWidth(int paintWidth) {
        this.paintWidth = paintWidth;
        paint.setStrokeWidth(paintWidth);
    }
    /**
     * 设置画板颜色
     *
     * @param canvasColor
     */
    public void setCanvasColor(int canvasColor) {
        this.backgroundColor = canvasColor;
    }
    /**
     * 清除画板
     */
    public void clear() {
        if (canvas != null) {
            isTouched = false;
            //更新画板
            paint.setColor(paintColor);
            paint.setStrokeWidth(paintWidth);
            canvas.drawColor(backgroundColor, PorterDuff.Mode.CLEAR);
            invalidate();
        }
    }
    /**
     * 获取画板的Bitmap
     *
     * @return
     */
    public Bitmap getBitmap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }
    /**
     * 是否有签名
     *
     * @return
     */
    public Boolean getSigstatus() {
        return isTouched;
    }
    /**
     * 保存画板
     *
     * @param path 保存到路径
     */
    @SuppressLint("WrongThread")
    public byte save() [] throws IOException {
        Bitmap bitmap = this.bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] buffer = bos.toByteArray();
        return buffer;
//        if (buffer != null) {
//            File file = new File(path);
//            if (file.exists()) {
//                file.delete();
//            }
//            OutputStream outputStream = new FileOutputStream(file);
//            outputStream.write(buffer);
//            outputStream.close();
//            return true;
//        } else {
//            return false;
//        }
    }
}