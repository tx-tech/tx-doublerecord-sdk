package com.txt.sl.ui.video;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/6.
 * description：
 */
public class IVideoView extends VideoView {
    public IVideoView(Context context) {
        super(context);
    }

    public IVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //此处设置的默认值可随意,因为getDefaultSize中的size是有值的
//        int width = getDefaultSize(0,widthMeasureSpec);
//        int height = getDefaultSize(0,heightMeasureSpec);
//        setMeasuredDimension(width,height);
//        System.out.println("======onMeasure===width==="+width);
//        System.out.println("======onMeasure===height==="+height);
    }

}
