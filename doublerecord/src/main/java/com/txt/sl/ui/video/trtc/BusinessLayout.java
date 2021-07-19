package com.txt.sl.ui.video.trtc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.txt.sl.R;

import java.lang.ref.WeakReference;

/**
 * Module: TRTCVideoLayout
 * <p>
 * Function:
 * <p>
 * 此 TRTCVideoLayout 封装了{@link TXCloudVideoView} 以及业务逻辑 UI 控件
 * 作用：
 * 1. 实现了手势监听，配合 {@link TRTCVideoLayoutManager} 能够实现自由拖动 View。
 * 详情可见：{@link BusinessLayout#initGestureListener()}
 * 实现原理：利用 RelativeLayout 的 margin 实现了能够在父容器自由定位的特性；需要注意，{@link BusinessLayout} 不能增加约束规则，如 alignParentRight 等，否则无法自由定位。
 * <p>
 * 2. 对{@link TXCloudVideoView} 与逻辑 UI 进行组合，在 muteLocal、音量回调等情况，能够进行 UI 相关的变化。若您的项目中，也相关的业务逻辑，可以参照 Demo 的相关实现。
 */
public class BusinessLayout extends RelativeLayout implements View.OnClickListener ,BusinessVideo{
    public WeakReference<IVideoLayoutListener> mWefListener;
    private Context mContext ;
    private ViewGroup mVgFuc;


    public BusinessLayout(Context context) {
        this(context, null);
        initFuncLayout();
    }

    public BusinessLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext  = context ;
        initFuncLayout();
    }

    public BusinessLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext  = context ;
        initFuncLayout();
    }


    private void initFuncLayout() {
        mVgFuc = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.tx_layout_trtc_func1, null);
        addView(mVgFuc);
    }

    public ViewGroup getContentView(){
        return mVgFuc;
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {

    }



    @Override
    public void onClick(View v) {
        IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener == null) return;
        int id = v.getId();
         if (id == R.id.tv_remote_skip){
            if (listener!=null){
                listener.onClickMuteInSpeakerAudio(this,true);
            }
        }else if (id == R.id.ll_page_voice_result_mark){
            if (listener!=null){
                listener.onClickRetry(this,"0");
            }
        }else if (id == R.id.ll_page_voice_result_jump){
            if (listener!=null){
                listener.onClickRetry(this,"1");
            }
        }else if(id == R.id.ll_page_voice_result_retry){
            if (listener!=null){
                listener.onClickRetry(this,"2");
            }
        }
//        if (id == R.id.trtc_btn_fill) {
//            mEnableFill = !mEnableFill;
//            if (mEnableFill) {
//                v.setBackgroundResource(R.drawable.fill_scale);
//            } else {
//                v.setBackgroundResource(R.drawable.fill_adjust);
//            }
//            listener.onClickFill(this, mEnableFill);
//        } else if (id == R.id.trtc_btn_mute_audio) {
//            mEnableAudio = !mEnableAudio;
//            if (mEnableAudio) {
//                v.setBackgroundResource(R.drawable.remote_audio_enable);
//            } else {
//                v.setBackgroundResource(R.drawable.remote_audio_disable);
//            }
//            listener.onClickMuteAudio(this, !mEnableAudio);
//        } else if (id == R.id.trtc_btn_mute_video) {
//            mEnableVideo = !mEnableVideo;
//            if (mEnableVideo) {
//                v.setBackgroundResource(R.drawable.remote_video_enable);
//            } else {
//                v.setBackgroundResource(R.drawable.remote_video_disable);
//            }
//            listener.onClickMuteVideo(this, !mEnableVideo);
//        } else if (id == R.id.mute_in_speaker) {
//            listener.onClickMuteInSpeakerAudio(this, ((ToggleButton) v).isChecked());
//        }
    }

    public void setIVideoLayoutListener(IVideoLayoutListener listener) {
        if (listener == null) {
            mWefListener = null;
        } else {
            mWefListener = new WeakReference<>(listener);
        }
    }

    public interface IVideoLayoutListener {
        void onClickFill(BusinessLayout view, boolean enableFill);

        void onClickMuteAudio(BusinessLayout view, boolean isMute);

        void onClickMuteVideo(BusinessLayout view, boolean isMute);

        void onClickMuteInSpeakerAudio(BusinessLayout view, boolean isMute);

        void onClickRetry(BusinessLayout view, String type);
    }
}
