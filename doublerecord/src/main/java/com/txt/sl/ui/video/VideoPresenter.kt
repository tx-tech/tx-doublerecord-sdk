package com.txt.sl.ui.video

import android.app.Activity
import com.txt.sl.base.BasePresenter

/**
 * Created by Justin on 2018/5/22/022.
 * email：WjqJustin@163.com
 * effect：search specific work list
 */

private val TAG = VideoPresenter::class.java.simpleName

class VideoPresenter(val mContext: Activity,val view: VideoContract.View ) : BasePresenter<VideoContract.View>(),VideoContract.Presenter {



}