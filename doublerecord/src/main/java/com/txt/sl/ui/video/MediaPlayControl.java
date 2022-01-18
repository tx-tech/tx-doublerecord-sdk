package com.txt.sl.ui.video;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.txt.sl.R;


/**
 * 排队中的声音
 * Created by DELL on 2017/7/27.
 */

public class MediaPlayControl {
    private MediaPlayer mPlayMedia;
    public MediaPlayControl(){
    }
    public void playCallMp3(Context context){
        pauseAndStopPlay();
        creatPlayMedia(context);
    }

    public void stopMediaPlay(){
        pauseAndStopPlay();
    }

    public void creatPlayMedia(Context context) {
        if (mPlayMedia!=null){
            mPlayMedia=null;
        }
        mPlayMedia = MediaPlayer.create(context, R.raw.incoming);
        mPlayMedia.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mPlayMedia != null) {
            mPlayMedia.setLooping(true);
            mPlayMedia.start();
        }
    }
    public void pauseAndStopPlay() {
        if (mPlayMedia != null) {
            if (mPlayMedia.isPlaying()) {
                if (mPlayMedia.isLooping())
                    mPlayMedia.setLooping(false);
                mPlayMedia.pause();
                mPlayMedia.stop();
                mPlayMedia.reset();
                mPlayMedia.release();
                mPlayMedia = null;
            }
        }
    }


}
