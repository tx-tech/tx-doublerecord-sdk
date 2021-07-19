package com.txt.sl.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.common.widget.immersionbar.TxBarHide;
import com.common.widget.immersionbar.TxImmersionBar;
import com.common.widget.base.BaseActivity;
import com.txt.sl.R;
import com.txt.sl.config.IntentKey;
import com.txt.sl.widget.PlayerView;

import java.io.File;

/**
 *    author :
 *    time   : 2020/02/16
 *    desc   : 视频播放界面
 */
public final class VideoPlayActivity extends BaseActivity
        implements PlayerView.onPlayListener {

    private PlayerView mPlayerView;
    private Builder mBuilder;

    @Override
    public int getLayoutId() {
        return R.layout.tx_video_play_activity;
    }

    @Override
    public void initData() {
        super.initData();


        mBuilder = getParcelable(IntentKey.VIDEO);
        if (mBuilder == null) {
            throw new IllegalArgumentException("are you ok?");
        }
        mPlayerView.setVideoTitle(mBuilder.getVideoTitle());
        mPlayerView.setVideoSource(mBuilder.getVideoSource(),mBuilder.ismIsNet());
        mPlayerView.setGestureEnabled(mBuilder.isGestureEnabled());
        if (mBuilder.isAutoPlay()) {
            mPlayerView.start();
        }
    }


    @Override
    public void initView() {
        super.initView();
        mPlayerView = findViewById(R.id.pv_video_play_view);
//        mPlayerView.setLifecycleOwner(this);
        mPlayerView.setOnPlayListener(this);
    }


    /**
     * {@link PlayerView.onPlayListener}
     */
    @Override
    public void onClickBack(PlayerView view) {
        onBackPressed();
    }

    @Override
    public void onPlayStart(PlayerView view) {
        int progress = mBuilder.getPlayProgress();
        if (progress > 0) {
            mPlayerView.setProgress(progress);
        }
    }

    @Override
    public void onPlayEnd(PlayerView view) {
        if (mBuilder.isLoopPlay()) {
            mPlayerView.setProgress(0);
            mPlayerView.start();
        } else if (mBuilder.isAutoOver()) {
            finish();
        }
    }

    @NonNull
    @Override
    protected TxImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 隐藏状态栏和导航栏
                .hideBar(TxBarHide.FLAG_HIDE_BAR);
    }

    /**
     * 播放参数构建
     */
    public static final class Builder implements Parcelable {

        /** 视频源 */
        private String mVideoSource;
        private boolean mIsNet;
        /** 视频标题 */
        private String mVideoTitle;
        /** 播放进度 */
        private int mPlayProgress;
        /** 手势开关 */
        private boolean mGestureEnabled = true;
        /** 循环播放 */
        private boolean mLoopPlay = false;
        /** 自动播放 */
        private boolean mAutoPlay = true;
        /** 播放完关闭 */
        private boolean mAutoOver = true;

        public Builder() {}

        public Builder setVideoSource(File file) {
            mVideoSource = file.getPath();
            if (mVideoTitle == null) {
                mVideoTitle = file.getName();
            }
            return this;
        }

        public Builder setVideoSource(String url,boolean isNet) {
            mVideoSource = url;
            mIsNet = isNet;
            return this;
        }

        private String getVideoSource() {
            return mVideoSource;
        }

        public Builder setVideoTitle(String title) {
            mVideoTitle = title;
            return this;
        }

        private String getVideoTitle() {
            return mVideoTitle;
        }

        public Builder setPlayProgress(int progress) {
            mPlayProgress = progress;
            return this;
        }

        private int getPlayProgress() {
            return mPlayProgress;
        }

        public boolean ismIsNet() {
            return mIsNet;
        }

        public Builder setGestureEnabled(boolean enabled) {
            mGestureEnabled = enabled;
            return this;
        }

        private boolean isGestureEnabled() {
            return mGestureEnabled;
        }

        public Builder setLoopPlay(boolean enabled) {
            mLoopPlay = enabled;
            return this;
        }

        private boolean isLoopPlay() {
            return mLoopPlay;
        }

        public Builder setAutoPlay(boolean enabled) {
            mAutoPlay = enabled;
            return this;
        }

        public boolean isAutoPlay() {
            return mAutoPlay;
        }

        public Builder setAutoOver(boolean enabled) {
            mAutoOver = enabled;
            return this;
        }

        private boolean isAutoOver() {
            return mAutoOver;
        }

        public void start(Context context) {
            Intent intent = new Intent(context, VideoPlayActivity.class);
            intent.putExtra(IntentKey.VIDEO, this);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mVideoSource);
            dest.writeByte(this.mIsNet ? (byte) 1 : (byte) 0);
            dest.writeString(this.mVideoTitle);
            dest.writeInt(this.mPlayProgress);
            dest.writeByte(this.mGestureEnabled ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mLoopPlay ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mAutoPlay ? (byte) 1 : (byte) 0);
            dest.writeByte(this.mAutoOver ? (byte) 1 : (byte) 0);
        }

        protected Builder(Parcel in) {
            this.mVideoSource = in.readString();
            this.mIsNet = in.readByte() != 0;
            this.mVideoTitle = in.readString();
            this.mPlayProgress = in.readInt();
            this.mGestureEnabled = in.readByte() != 0;
            this.mLoopPlay = in.readByte() != 0;
            this.mAutoPlay = in.readByte() != 0;
            this.mAutoOver = in.readByte() != 0;
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }
}