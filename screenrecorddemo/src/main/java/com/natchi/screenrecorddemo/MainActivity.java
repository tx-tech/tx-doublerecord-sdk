package com.natchi.screenrecorddemo;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.txt.sl.screenrecorder.ScreenRecordHelper;
import com.txt.sl.ui.video.MediaPlayControl;
import com.txt.sl.utils.TxPathUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_startrecord).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        findViewById(R.id.tv_stoprecord).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                screenRecordHelper.stopRecord(0L,0L,null);
            }
        });

        findViewById(R.id.tv_starttts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayControl media = new MediaPlayControl();
                media.creatPlayMedia(MainActivity.this);
            }
        });


    }

    ScreenRecordHelper screenRecordHelper;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void startRecord() {
        if (screenRecordHelper == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                screenRecordHelper = new ScreenRecordHelper(
                        MainActivity.this,
                        new ScreenRecordHelper.OnVideoRecordListener() {

                            @Override
                            public void onEndRecord() {

                            }

                            @Override
                            public void onCancelRecord() {

                            }

                            @Override
                            public void onStartRecord() {

                            }

                            @Override
                            public void onBeforeRecord() {

                            }
                        },
                        TxPathUtils.getExternalStoragePath() + "/txsl/video"
                );
            }

        }
        screenRecordHelper.setRecordAudio(true);
        screenRecordHelper.startRecord();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case   ScreenRecordHelper.REQUEST_CODE:

                if (data != null) {
                    screenRecordHelper.onActivityResult(requestCode, resultCode, data);
                } else {
                    finish();
                }


           break;
        }



    }
}