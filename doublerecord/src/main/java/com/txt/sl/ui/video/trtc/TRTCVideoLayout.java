package com.txt.sl.ui.video.trtc;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloudDef;
import com.txt.sl.R;
import com.txt.sl.utils.CheckDoubleClickListener;
import com.txt.sl.utils.MainThreadUtil;
import com.txt.sl.utils.OnCheckDoubleClick;
import com.txt.sl.widget.HollowDoubleOutView;
import com.txt.sl.widget.HollowOutView;
import com.txt.sl.widget.RoundView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Module: TRTCVideoLayout
 * <p>
 * Function:
 * <p>
 * 此 TRTCVideoLayout 封装了{@link TXCloudVideoView} 以及业务逻辑 UI 控件
 * 作用：
 * 1. 实现了手势监听，配合 {@link TRTCVideoLayoutManager} 能够实现自由拖动 View。
 * 详情可见：{@link TRTCVideoLayout#initGestureListener()}
 * 实现原理：利用 RelativeLayout 的 margin 实现了能够在父容器自由定位的特性；需要注意，{@link TRTCVideoLayout} 不能增加约束规则，如 alignParentRight 等，否则无法自由定位。
 * <p>
 * 2. 对{@link TXCloudVideoView} 与逻辑 UI 进行组合，在 muteLocal、音量回调等情况，能够进行 UI 相关的变化。若您的项目中，也相关的业务逻辑，可以参照 Demo 的相关实现。
 */
public class TRTCVideoLayout extends RelativeLayout implements View.OnClickListener, BusinessVideo, OnCheckDoubleClick {
    public WeakReference<IVideoLayoutListener> mWefListener;
    private Context mContext;
    private TXCloudVideoView mVideoView;
    private OnClickListener mClickListener;
    private CheckDoubleClickListener mDClickListener;
    private GestureDetector mSimpleOnGestureListener;
    private ProgressBar mPbAudioVolume;
    private LinearLayout mLlController;
    private Button mBtnMuteVideo, mBtnMuteAudio, mBtnFill;
    private LinearLayout mLlNoVideo;
    private ImageView mIvNoS;
    private ImageView mTvNoS;
    private ViewGroup mVgFuc;
    private HashMap<Integer, Integer> mNoSMap = null;
    private boolean mMoveable;
    private boolean mEnableFill = false;
    private boolean mEnableAudio = true;
    private boolean mEnableVideo = true;
    private TextView mName;
    private TextView mTvLocation;
    private TextView mTvToast;
    private TextView mTvNovideo;
    private LinearLayout ll_remote_skip;
    private LinearLayout ll_page_voice_result;
    private TextView tv_prompt;
    private TextView tv_remote_skip;
    private TextView tv_ocr, ll_page_voice_result_mark, ll_page_voice_result_jump, ll_page_voice_result_retry, ll_page12_result_fail;
    private HollowOutView mHollowOutView;
    private RoundView mRoundView;
    private HollowDoubleOutView mHollowDoubleOutView;
    private ImageView iv_person;
    private ImageView iv_ocr;
    private ObjectAnimator rotation;

    public TRTCVideoLayout(Context context) {
        this(context, null);
        initFuncLayout();
        initGestureListener();
        initNoS();
    }

    public TRTCVideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initFuncLayout();
        initGestureListener();
        initNoS();
    }

    public TRTCVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initFuncLayout();
        initGestureListener();
        initNoS();
    }

    public TXCloudVideoView getVideoView() {
        return mVideoView;
    }

    public void updateNetworkQuality(int quality) {
        if (quality < TRTCCloudDef.TRTC_QUALITY_Excellent) {
            quality = TRTCCloudDef.TRTC_QUALITY_Excellent;
        }
        if (quality > TRTCCloudDef.TRTC_QUALITY_Down) {
            quality = TRTCCloudDef.TRTC_QUALITY_Down;
        }

        if (mTvNoS != null) {
            Drawable drawable;
            if (quality==6) {
                 drawable = ContextCompat.getDrawable(mContext, R.drawable.tx_net_6);
            }else if (quality==3) {
                drawable = ContextCompat.getDrawable(mContext, R.drawable.tx_net_3);
            }else if (quality>=4&& quality<=5) {
                drawable = ContextCompat.getDrawable(mContext, R.drawable.tx_net_5);
            }else{
                //quality>=2&& quality<=0
                drawable = ContextCompat.getDrawable(mContext, R.drawable.tx_net_2);
            }
            mTvNoS.setImageDrawable(drawable);
        }
    }

    public void setBottomControllerVisibility(int visibility) {
        if (mLlController != null)
            mLlController.setVisibility(visibility);
    }

    public void updateNoVideoLayout(String text, int visibility) {
        if (mTvNovideo != null) {
            mIvNoS.setVisibility(GONE);
            mLlNoVideo.setVisibility(visibility);
        }

    }

    public void updateNoVideoLayoutTv(String text, int visibility) {
        if (mTvNovideo != null) {
            if (text.isEmpty()) {
                mTvNovideo.setVisibility(GONE);
            }else{
                mTvNovideo.setText(text);
                mTvNovideo.setVisibility(visibility);
            }

            mIvNoS.setVisibility(visibility);
            mLlNoVideo.setVisibility(visibility);
        }

    }

    public void setAudioVolumeProgress(int progress) {
        if (mPbAudioVolume != null) {
            mPbAudioVolume.setProgress(progress);
        }
    }

    public void setAudioVolumeProgressBarVisibility(int visibility) {
        if (mPbAudioVolume != null) {
            mPbAudioVolume.setVisibility(visibility);
        }
    }

    //单个人脸识别动画
    public void startRoundView() {
        if (null != mHollowOutView) {
            mHollowOutView.setVisibility(VISIBLE);
            mRoundView.setVisibility(VISIBLE);
            rotation = ObjectAnimator.ofFloat(mRoundView, "rotation", 0f, 359f);
            rotation.setDuration(2000);
            rotation.setRepeatCount(ValueAnimator.INFINITE);
            rotation.setInterpolator(new LinearInterpolator());
            rotation.start();
        }
    }

    public void stopRoundView() {
        if (null != mHollowOutView && mHollowOutView.getVisibility() == VISIBLE) {
            if (null != mHollowOutView) {
                mRoundView.setVisibility(GONE);
                if (null != rotation) {
                    rotation.end();
                }
                mHollowOutView.setVisibility(GONE);
            }
        } else {
            if (null != mHollowDoubleOutView) {
                mHollowDoubleOutView.setVisibility(INVISIBLE);
                mHollowDoubleOutView.stopRoundView();
            }
        }

    }

    public void startTwoRoundView() {
        if (null != mHollowDoubleOutView) {
            mHollowDoubleOutView.setVisibility(VISIBLE);
            MainThreadUtil.INSTANCE.getHANDLER().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHollowDoubleOutView.startRoundView();
                }
            }, 500);

        }
    }

    public void stopTwoRoundView() {
        if (null != mHollowDoubleOutView) {
            mHollowDoubleOutView.setVisibility(GONE);
            mHollowDoubleOutView.stopRoundView();
        }
    }


    public void setHollowOutView(int visibility) {
        if (null != mHollowOutView) {
            mHollowOutView.setVisibility(visibility);
        }
    }

    public void setPersonView(int visibility) {
        if (iv_person != null) {
            iv_person.setVisibility(visibility);
        }
    }

    //如果是自保件
    public void setPersonView(boolean isSelf) {
        if (iv_person != null) {
            if (isSelf) {
                iv_person.setBackground(ContextCompat.getDrawable(this.mContext, R.drawable.tx_icon_checkoneperson));
            } else {
                iv_person.setBackground(ContextCompat.getDrawable(this.mContext, R.drawable.tx_icon_checkperson));
            }
        }
    }

    public void setOcrStatus(String name, int visibility, String type) {
        if (tv_ocr != null) {
            tv_ocr.setText(name);
            tv_ocr.setVisibility(visibility);
            if ("0".equals(type)) {
                //成功
                tv_ocr.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.tx_pass_icon), null, null, null);
                tv_ocr.setCompoundDrawablePadding(5);
            } else if ("1".equals(type)) {
                //失败
                tv_ocr.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.tx_nopass_icon), null, null, null);
                tv_ocr.setCompoundDrawablePadding(5);
            } else {
                //显示识别状态
                tv_ocr.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tv_ocr.setCompoundDrawablePadding(5);
            }
        }
    }

    public void setName(String name) {
        if (mName != null) {
            mName.setText(name);
            mName.setVisibility(VISIBLE);
        }
    }

    public void setLocationStr(String location) {
        if (mTvLocation != null) {
            mTvLocation.setText(location);
            mTvLocation.setVisibility(VISIBLE);
        }
    }

    public void setToastStr(String toast) {
        if (mTvToast != null) {
            mTvToast.setText(toast);
            if (toast.isEmpty()) {
                mTvToast.setVisibility(GONE);
            } else {
                mTvToast.setVisibility(VISIBLE);
            }

        }
    }

    public void setToastStr(String toast, @ColorInt int color) {
        if (mTvToast != null) {
            mTvToast.setText(toast);
            if (toast.isEmpty()) {
                mTvToast.setVisibility(GONE);
            } else {
                mTvToast.setTextColor(color);
                mTvToast.setVisibility(VISIBLE);
            }

        }
    }


    public void setll_remote_skip(String toast, int visibility, int skipVisibility) {
        if (ll_remote_skip != null) {
            if (ll_remote_skip.getVisibility() != visibility) {
                ll_remote_skip.setVisibility(visibility);
            }
            tv_prompt.setText(toast);
            if (tv_remote_skip.getVisibility() != visibility) {
                tv_remote_skip.setVisibility(skipVisibility);
            }

        }
    }

    public void setll_page_voice_result(int visibility, boolean isSuccess, String successStr, JSONArray btArray) {
        if (ll_page_voice_result != null) {
            if (ll_page_voice_result.getVisibility() != visibility) {
                ll_page_voice_result.setVisibility(visibility);
            }
            ll_page12_result_fail.setVisibility(VISIBLE);
            ll_page12_result_fail.setText(successStr);
            ll_page12_result_fail.setCompoundDrawablePadding(5);
            if (isSuccess) {
                ll_page12_result_fail.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.tx_pass_icon), null, null, null);
                ll_page_voice_result_mark.setVisibility(GONE);
                ll_page_voice_result_jump.setVisibility(GONE);
                ll_page_voice_result_retry.setVisibility(GONE);
            } else {
                ll_page12_result_fail.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.tx_nopass_icon), null, null, null);
                try {
                    if (null != btArray) {
                        //显示按钮的值
                        for (int i = 0; i < btArray.length(); i++) {
                            JSONObject btJSONObject = btArray.getJSONObject(i);
                            String key = btJSONObject.optString("key", "");
                            String buttonName = btJSONObject.optString("buttonName", "");
                            boolean check = btJSONObject.optBoolean("check", true);
                            if ("releaseSuccessful".equals(key)) {
                                if (check) {
                                    ll_page_voice_result_mark.setVisibility(VISIBLE);
                                } else {
                                    ll_page_voice_result_mark.setVisibility(GONE);
                                }
                                ll_page_voice_result_mark.setText(buttonName);
                            } else if ("releaseFailure".equals(key)) {
                                if (check) {
                                    ll_page_voice_result_jump.setVisibility(VISIBLE);
                                } else {
                                    ll_page_voice_result_jump.setVisibility(GONE);
                                }
                                ll_page_voice_result_jump.setText(buttonName);
                            } else if ("retry".equals(key)) {
                                if (check) {
                                    ll_page_voice_result_retry.setVisibility(VISIBLE);
                                } else {
                                    ll_page_voice_result_retry.setVisibility(GONE);
                                }
                                ll_page_voice_result_retry.setText(buttonName);
                            } else {

                            }
                        }
                    }
                } catch (JSONException exception) {

                }

            }
        }
    }


    public void setIvOcr(int visibility){
        if (null !=  iv_ocr) {
            if (visibility == GONE) {
                iv_ocr.setVisibility(INVISIBLE);
            }else{
                iv_ocr.setVisibility(visibility);
            }

        }
    }

    private void initNoS() {
        mNoSMap = new HashMap<>();
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Down), Integer.valueOf(R.drawable.signal1));
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Vbad), Integer.valueOf(R.drawable.signal2));
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Bad), Integer.valueOf(R.drawable.signal3));
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Poor), Integer.valueOf(R.drawable.signal4));
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Good), Integer.valueOf(R.drawable.signal5));
//        mNoSMap.put(Integer.valueOf(TRTCCloudDef.TRTC_QUALITY_Excellent), Integer.valueOf(R.drawable.signal6));
    }


    private void initFuncLayout() {
        mVgFuc = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.tx_layout_trtc_func, null);
        addView(mVgFuc);
        mVideoView = (TXCloudVideoView) mVgFuc.findViewById(R.id.trtc_tc_cloud_view);
        mName = (TextView) mVgFuc.findViewById(R.id.tv_name);
        mTvLocation = (TextView) mVgFuc.findViewById(R.id.tv_location);
        mTvToast = (TextView) mVgFuc.findViewById(R.id.tv_local_video_toast);
        ll_remote_skip = (LinearLayout) mVgFuc.findViewById(R.id.ll_remote_skip);
        tv_remote_skip = (TextView) mVgFuc.findViewById(R.id.tv_remote_skip);
        tv_prompt = (TextView) mVgFuc.findViewById(R.id.tv_prompt);
        tv_ocr = (TextView) mVgFuc.findViewById(R.id.tv_ocr);
        iv_person = (ImageView) mVgFuc.findViewById(R.id.iv_person);
        mRoundView = (RoundView) mVgFuc.findViewById(R.id.roundView);
        mHollowOutView = (HollowOutView) mVgFuc.findViewById(R.id.hollowoutview);
        mHollowDoubleOutView = (HollowDoubleOutView) mVgFuc.findViewById(R.id.hollowdoubleoutview);
        ll_page_voice_result = (LinearLayout) mVgFuc.findViewById(R.id.ll_page_voice_result);
        ll_page_voice_result_mark = (TextView) mVgFuc.findViewById(R.id.ll_page_voice_result_mark);
        ll_page_voice_result_jump = (TextView) mVgFuc.findViewById(R.id.ll_page_voice_result_jump);
        ll_page_voice_result_retry = (TextView) mVgFuc.findViewById(R.id.ll_page_voice_result_retry);
        ll_page12_result_fail = (TextView) mVgFuc.findViewById(R.id.ll_page12_result_fail);
        iv_ocr = (ImageView) mVgFuc.findViewById(R.id.iv_ocr);
//        mVideoView.setOnClickListener(this);
        CheckDoubleClickListener checkDoubleClickListener = new CheckDoubleClickListener(this);
        tv_remote_skip.setOnClickListener(checkDoubleClickListener);
        ll_page_voice_result_mark.setOnClickListener(checkDoubleClickListener);
        ll_page_voice_result_jump.setOnClickListener(checkDoubleClickListener);
        ll_page_voice_result_retry.setOnClickListener(checkDoubleClickListener);
//        mPbAudioVolume = (ProgressBar) mVgFuc.findViewById(R.id.trtc_pb_audio);
//        mLlController = (LinearLayout) mVgFuc.findViewById(R.id.trtc_ll_controller);
//        mBtnMuteVideo = (Button) mVgFuc.findViewById(R.id.trtc_btn_mute_video);
//        mBtnMuteVideo.setOnClickListener(this);
//        mBtnMuteAudio = (Button) mVgFuc.findViewById(R.id.trtc_btn_mute_audio);
//        mBtnMuteAudio.setOnClickListener(this);
//        mBtnFill = (Button) mVgFuc.findViewById(R.id.trtc_btn_fill);
//        mBtnFill.setOnClickListener(this);
        mLlNoVideo = (LinearLayout) mVgFuc.findViewById(R.id.trtc_fl_no_video);
        mTvNovideo = (TextView) mVgFuc.findViewById(R.id.tv_no_video);
        mTvNoS = (ImageView) mVgFuc.findViewById(R.id.trtc_iv_nos);
        mIvNoS = (ImageView) mVgFuc.findViewById(R.id.trtc_iv_no_video);
//        ToggleButton muteBtn = (ToggleButton) mVgFuc.findViewById(R.id.mute_in_speaker);
//        muteBtn.setOnClickListener(this);
    }


    private void initGestureListener() {
        mSimpleOnGestureListener = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mDClickListener != null) {
                    mDClickListener.onClick(TRTCVideoLayout.this);
                }
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mMoveable) return false;
                ViewGroup.LayoutParams params = TRTCVideoLayout.this.getLayoutParams();
                // 当 TRTCVideoView 的父容器是 RelativeLayout 的时候，可以实现拖动
                if (params instanceof LayoutParams) {
                    LayoutParams layoutParams = (LayoutParams) TRTCVideoLayout.this.getLayoutParams();
                    int newX = (int) (layoutParams.leftMargin + (e2.getX() - e1.getX()));
                    int newY = (int) (layoutParams.topMargin + (e2.getY() - e1.getY()));

                    layoutParams.leftMargin = newX;
                    layoutParams.topMargin = newY;

                    TRTCVideoLayout.this.setLayoutParams(layoutParams);
                }
                return true;
            }
        });
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mSimpleOnGestureListener.onTouchEvent(event);
            }
        });
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mDClickListener = (CheckDoubleClickListener) l;
    }


    public void setMoveable(boolean enable) {
        mMoveable = enable;
    }

    @Override
    public void onClick(View v) {

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

    @Override
    public void onCheckDoubleClick(View view) {
        IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener == null) return;
        int id = view.getId();
        if (id == R.id.trtc_tc_cloud_view) {
            mEnableFill = !mEnableFill;
            //放大
            if (listener != null) {
                listener.onClickFill(this, mEnableFill);
            }

        } else if (id == R.id.tv_remote_skip) {
            if (listener != null) {
                listener.onClickMuteInSpeakerAudio(this, true);
            }
        } else if (id == R.id.ll_page_voice_result_mark) {
            if (listener != null) {
                listener.onClickRetry(this, "0");
            }
        } else if (id == R.id.ll_page_voice_result_jump) {
            if (listener != null) {
                listener.onClickRetry(this, "1");
            }
        } else if (id == R.id.ll_page_voice_result_retry) {
            if (listener != null) {
                listener.onClickRetry(this, "2");
            }
        }
    }

    public interface IVideoLayoutListener {
        void onClickFill(TRTCVideoLayout view, boolean enableFill);

        void onClickMuteAudio(TRTCVideoLayout view, boolean isMute);

        void onClickMuteVideo(TRTCVideoLayout view, boolean isMute);

        void onClickMuteInSpeakerAudio(TRTCVideoLayout view, boolean isMute);

        void onClickRetry(TRTCVideoLayout view, String type);
    }

    public enum NetQuality {
        UNKNOW(0, "未定义"),
        EXCELLENT(1, "最好"),
        GOOD(2, "好"),
        POOR(3, "一般"),
        BAD(4, "差"),
        VBAD(5, "很差"),
        DOWN(6, "不可用");

        private int code;
        private String msg;

        NetQuality(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public static String getMsg(int code) {
            for (NetQuality item : NetQuality.values()) {
                if (item.code == code) {
                    return item.msg;
                }
            }
            return "未定义";
        }
    }

}
