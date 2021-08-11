package com.txt.sl.ui.video.trtc;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloudDef;
import com.txt.sl.utils.RoomVideoUiUtils;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Module:   TRTCVideoViewLayout
 * <p>
 * Function: {@link TXCloudVideoView} 的管理类
 * <p>
 * 1.在多人通话中，您的布局可能会比较复杂，Demo 也是如此，因此需要统一的管理类进行管理，这样子有利于写出高可维护的代码
 * <p>
 * 2.Demo 中提供堆叠布局、宫格布局两种展示方式；若您的项目也有相关的 UI 交互，您可以参考实现代码，能够快速集成。
 * <p>
 * 3.堆叠布局：{@link TRTCRightVideoLayoutManager#makeGirdLayout(boolean)} 思路是初始化一系列的 x、y、padding、margin 组合 LayoutParams 直接对 View 进行定位
 * <p>
 * 4.宫格布局：{@link TRTCRightVideoLayoutManager#makeGirdLayout(boolean)} 思路与堆叠布局一致，也是初始化一些列的 LayoutParams 直接对 View 进行定位
 * <p>
 * 5.如何实现管理：
 * A. 使用{@link TRTCLayoutEntity} 实体类，保存 {@link TRTCVideoLayout} 的分配信息，能够与对应的用户绑定起来，方便管理与更新UI
 * B. {@link TRTCVideoLayout} 专注实现业务 UI 相关的，控制逻辑放在此类中
 * <p>
 * 6.布局切换，见 {@link TRTCRightVideoLayoutManager#switchMode()}
 * <p>
 * 7.堆叠布局与宫格布局参数，见{@link RoomVideoUiUtils } 工具类
 */
public class TRTCRightVideoLayoutManager extends RelativeLayout implements TRTCVideoLayout.IVideoLayoutListener {
    public static final int MODE_FLOAT = 1;  // 前后堆叠模式
    public static final int MODE_GRID = 2;  // 九宫格模式
    public int MAX_USER = 4;
    private final static String TAG = TRTCRightVideoLayoutManager.class.getSimpleName();
    public WeakReference<IVideoLayoutListener> mWefListener;
    private ArrayList<TRTCLayoutEntity> mLayoutEntityList;
    private ArrayList<LayoutParams> mFloatParamList;
    private ArrayList<LayoutParams> mGrid4ParamList;
    private ArrayList<LayoutParams> mGrid9ParamList;
    private int mCount = 0;
    private int mMode;
    private String mSelfUserId;

    /**
     * ===============================View相关===============================
     */
    public TRTCRightVideoLayoutManager(Context context) {
        super(context);
    }


    public TRTCRightVideoLayoutManager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    public TRTCRightVideoLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void initView(Context context, int max_user) {
        this.MAX_USER = max_user + 1;
        Log.i(TAG, "initView: ");

        mLayoutEntityList = new ArrayList<TRTCLayoutEntity>();
        // 初始化多个 View，以备用
        for (int i = 0; i < MAX_USER; i++) {
            TRTCVideoLayout videoLayout = new TRTCVideoLayout(context);
            videoLayout.setVisibility(View.INVISIBLE);
            videoLayout.setBackgroundColor(Color.WHITE);
            videoLayout.setMoveable(false);
            videoLayout.setIVideoLayoutListener(this);
            // 这里不展示其底部的控制菜单
            videoLayout.setBottomControllerVisibility(View.GONE);
            videoLayout.updateNoVideoLayout("", View.VISIBLE);
            TRTCLayoutEntity entity = new TRTCLayoutEntity();
            entity.layout = videoLayout;
            entity.index = i;
            mLayoutEntityList.add(entity);
        }
        // 默认为堆叠模式
//        mMode = MODE_GRID;
        if (max_user == 3) {
            makeThreeLayout();
        } else {
            makeTwoLayout();
        }
    }

    /**
     * ===============================九宫格布局下点击按钮的事件回调===============================
     */

    @Override
    public void onClickFill(TRTCVideoLayout view, boolean enableFill) {
        TRTCRightVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemFill(entity.userId, entity.streamType, enableFill);
        }
    }

    @Override
    public void onClickMuteAudio(TRTCVideoLayout view, boolean isMute) {
        TRTCRightVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteAudio(entity.userId, isMute);
        }
    }

    @Override
    public void onClickMuteVideo(TRTCVideoLayout view, boolean isMute) {
        TRTCRightVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteVideo(entity.userId, entity.streamType, isMute);
        }
    }

    @Override
    public void onClickMuteInSpeakerAudio(TRTCVideoLayout view, boolean isMute) {
        TRTCRightVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteInSpeakerAudio(entity.userId, isMute);
        }
    }

    @Override
    public void onClickRetry(TRTCVideoLayout view, String type) {
        TRTCRightVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemRetry(entity.userId, type);
        }
    }

    /**
     * ===============================Manager对外相关方法===============================
     */
    public void setIVideoLayoutListener(TRTCRightVideoLayoutManager.IVideoLayoutListener listener) {
        if (listener == null) {
            mWefListener = null;
        } else {
            mWefListener = new WeakReference<>(listener);
        }
    }

    public void setMySelfUserId(String userId) {
        mSelfUserId = userId;
    }


    /**
     * 宫格布局与悬浮布局切换
     *
     * @return
     */
    public int switchMode() {
        makeGirdLayout(true);
        if (mMode == MODE_FLOAT) {
            mMode = MODE_GRID;

        } else {
            mMode = MODE_FLOAT;
//            makeFloatLayout(false);
        }
        return mMode;
    }

    /**
     * 宫格布局与悬浮布局切换
     *
     * @return
     */
    public int switchMode(int mode) {
        makeGirdLayout(true);
        if (mMode == MODE_FLOAT) {
            mMode = MODE_GRID;

        } else {
            mMode = MODE_FLOAT;
//            makeFloatLayout(false);
        }
        return mMode;
    }

    /**
     * 根据 userId 和视频类型，找到已经分配的 View
     *
     * @param userId
     * @param streamType
     * @return
     */
    public TXCloudVideoView findCloudViewView(String userId, int streamType) {
        if (userId == null) return null;
        for (TRTCLayoutEntity layoutEntity : mLayoutEntityList) {
            if (layoutEntity.streamType == streamType && layoutEntity.userId.equals(userId)) {
                return layoutEntity.layout.getVideoView();
            }
        }
        return null;
    }

    /**
     * 根据 userId 和 视频类型（大、小、辅路）画面分配对应的 com.common.weight.view
     *
     * @param userId
     * @param userType   agent insurant  insured
     * @param streamType
     * @return
     */
    public TXCloudVideoView allocCloudVideoView(String userId, String userType, int streamType) {
        if (userId == null) return null;
        TRTCLayoutEntity trtcLayoutEntity;
//        String content = "";
        if (userType == "agent") {
            trtcLayoutEntity = mLayoutEntityList.get(1);
//            content = "代理人等待进入";
        } else if (userType == "policyholder") {
            trtcLayoutEntity = mLayoutEntityList.get(2);
//            content = "投保人等待进入";
        } else if (userType == "insured") {
            trtcLayoutEntity = mLayoutEntityList.get(3);
//            content = "被保人等待进入";
        } else {
            trtcLayoutEntity = mLayoutEntityList.get(1);
//            content = "代理人等待进入";
        }

        trtcLayoutEntity.userId = userId;
        trtcLayoutEntity.streamType = streamType;
        trtcLayoutEntity.userType = userType;
        trtcLayoutEntity.layout.setVisibility(VISIBLE);
        trtcLayoutEntity.layout.updateNoVideoLayout("", View.GONE);
        return trtcLayoutEntity.layout.getVideoView();
    }

    /**
     * 根据 userId 和 视频类型，回收对应的 com.common.weight.view
     *
     * @param userId
     * @param streamType
     */
    public void recyclerCloudViewView(String userId, int streamType) {
        if (userId == null) return;
        if (mMode == MODE_FLOAT) {
            TRTCLayoutEntity entity = mLayoutEntityList.get(0);
            // 当前离开的是处于0号位的人，那么需要将我换到这个位置
            if (userId.equals(entity.userId) && entity.streamType == streamType) {
                TRTCLayoutEntity myEntity = findEntity(mSelfUserId);
                if (myEntity != null) {
                    makeFullVideoView(myEntity.index);
                }
            }
        } else {
        }
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.streamType == streamType && userId.equals(entity.userId)) {
                mCount--;
                if (mMode == MODE_GRID) {
                    if (mCount == 4) {
                        makeGirdLayout(true);
                    }
                }
                entity.layout.setVisibility(INVISIBLE);
                entity.userId = "";
                entity.streamType = -1;
                break;
            }
        }
    }

    /**
     * 隐藏所有音量的进度条
     */
    public void hideAllAudioVolumeProgressBar() {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            entity.layout.setAudioVolumeProgressBarVisibility(View.GONE);
        }
    }

    /**
     * 显示所有音量的进度条
     */
    public void showAllAudioVolumeProgressBar() {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            entity.layout.setAudioVolumeProgressBarVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置当前音量
     *
     * @param userId
     * @param audioVolume
     */
    public void updateAudioVolume(String userId, int audioVolume) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setAudioVolumeProgress(audioVolume);
                }
            }
        }
    }

    /**
     * 更新网络质量
     *
     * @param userId
     * @param quality
     */
    public void updateNetworkQuality(String userId, int quality) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.updateNetworkQuality(quality);
                }
            }
        }
    }

    /**
     * 更新名字
     *
     * @param userId
     * @param name
     */
    public void updateName(String userId, String name) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setName(name);
                }
            }
        }
    }


    /**
     * 更新位置信息
     *
     * @param userId
     * @param name
     */
    public void updateLocationStr(String userId, String name) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setLocationStr(name);
                }
            }
        }
    }

    /**
     * 更新提示语
     *
     * @param userId
     * @param name
     */
    public void updateToastStr(String userId, String name) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setToastStr(name);
                }
            }
        }
    }

    public void updateToastStr(String userId, String name, @ColorInt int color) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setToastStr(name, color);
                }
            }
        }
    }

    public void updateToastStrByType(String userType, String name) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setToastStr(name);
                }
            }
        }
    }

    public void updateToastStrByType(String userType, String name, @ColorInt int color) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setToastStr(name, color);
                }
            }
        }
    }

    /**
     * 开始转圈圈
     *
     * @param userId
     */
    public void startRoundView(String userId) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.startRoundView();
                }
            }
        }
    }

    /**
     * 开始转圈圈
     *
     * @param userType
     */
    public void startRoundViewByType(String userType) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.startRoundView();
                }
            }
        }
    }

    /**
     * 暂停转圈圈
     *
     * @param userId
     */
    public void stopRoundView(String userId) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.stopRoundView();
                }
            }
        }
    }

    /**
     * 暂停转圈圈
     *
     * @param userType
     */
    public void stopRoundViewByType(String userType) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.stopRoundView();
                }
            }
        }
    }

    /**
     * 显示识别内容
     *
     * @param userType
     * @param name
     */
    public void updateOcrStatusByType(String userType, String name, int visibility, String statusType) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setOcrStatus(name, visibility, statusType);
                }
            }
        }
    }

    public void updateOcrStatus(String userId, String name, int visibility, String statusType) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setOcrStatus(name, visibility, statusType);
                }
            }
        }
    }

    /**
     * 显示下一步布局
     *
     * @param userType
     * @param pro
     */
    public void updateSkipLayout(String userType, String pro, int visibility, int skipVisibility) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setll_remote_skip(pro, visibility, skipVisibility);
                }
            }
        }
    }

    //隐藏全部状态 布局
    public void hideAllStateView() {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                entity.layout.setll_page_voice_result(View.GONE, true, "", null);
                entity.layout.setOcrStatus("", View.GONE, "0");
                entity.layout.setll_remote_skip("", View.GONE, View.GONE);
            }
        }
    }

    /**
     * 显示重试布局
     *
     * @param userType
     * @param visibility
     */
    public void updateResultLayoutByType(String userType, int visibility, boolean isSuccess, String successStr, JSONArray btArray) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setll_page_voice_result(visibility, isSuccess, successStr, btArray);
                }
            }
        }
    }

    /**
     * 显示识别布局
     *
     * @param userId
     * @param visibility
     */
    public void updateHollowOutViewLayout(String userId, int visibility) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId)) {
                    entity.layout.setHollowOutView(visibility);
                }
            }
        }
    }

    /**
     * 显示识别布局
     *
     * @param userType
     * @param visibility
     */
    public void updateHollowOutViewLayoutByType(String userType, int visibility) {
        if (userType == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userType.equals(entity.userType)) {
                    entity.layout.setHollowOutView(visibility);
                }
            }
        }
    }


    /**
     * 更新当前视频状态
     *
     * @param userId
     * @param bHasVideo
     */
    public void updateVideoStatus(String userId, boolean bHasVideo, String content) {
        if (userId == null) return;
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout.getVisibility() == VISIBLE) {
                if (userId.equals(entity.userId) && entity.streamType == TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG) {
                    entity.layout.updateNoVideoLayout(content, bHasVideo ? GONE : VISIBLE);
                    break;
                }
            }
        }
    }

    private TRTCLayoutEntity findEntity(TRTCVideoLayout layout) {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.layout == layout) return entity;
        }
        return null;
    }

    public TRTCLayoutEntity findEntity(String userId) {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.userId.equals(userId)) return entity;
        }
        return null;
    }

    public String findEntityBytype(String userType) {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.userType.equals(userType)) return entity.userId;
        }
        return null;
    }

    public void hideEntitiyLayout(String userId) {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.userId.equals(userId)) {
                entity.layout.setVisibility(GONE);
                requestLayout();
            }
        }
    }

    /**
     * 切换到两人模式
     *
     * @param needUpdate 是否需要更新布局
     */
    public void makeGirdLayout(boolean needUpdate) {
        if (mGrid4ParamList == null || mGrid4ParamList.size() == 0 || mGrid9ParamList == null || mGrid9ParamList.size() == 0) {
            mGrid4ParamList = RoomVideoUiUtils.initRemoteTwoView(getContext(), getWidth(), getHeight());
        }
        if (needUpdate) {
            ArrayList<LayoutParams> paramList;
            if (mCount <= 4) {
                paramList = mGrid4ParamList;
            } else {
                paramList = mGrid9ParamList;
            }
            int layoutIndex = 1;
            for (int i = 0; i < mGrid4ParamList.size(); i++) {

                TRTCLayoutEntity entity = mLayoutEntityList.get(i);
                TRTCVideoLayout layout = entity.layout;

                layout.setMoveable(false);
                layout.setOnClickListener(null);
                // 我自己要放在布局的左上角
                layout.setVisibility(View.VISIBLE);
                layout.updateNoVideoLayout("", View.VISIBLE);

                addView(layout);
                layout.setLayoutParams(mGrid4ParamList.get(i));

            }
        }
    }

    //切换三人模式
    public void makeGirdLayout1(boolean needUpdate) {
        if (mGrid4ParamList == null || mGrid4ParamList.size() == 0 || mGrid9ParamList == null || mGrid9ParamList.size() == 0) {
            mGrid4ParamList = RoomVideoUiUtils.initThreeView(getContext(), getWidth(), getHeight());
        }
        if (needUpdate) {
            ArrayList<LayoutParams> paramList;
            if (mCount <= 4) {
                paramList = mGrid4ParamList;
            } else {
                paramList = mGrid9ParamList;
            }
            int layoutIndex = 1;
            for (int i = 0; i < mGrid4ParamList.size(); i++) {
                TRTCLayoutEntity entity = mLayoutEntityList.get(i);
                TRTCVideoLayout layout = entity.layout;
                addFloatViewClickListener(layout);
                layout.setMoveable(false);
                layout.setOnClickListener(null);
                // 我自己要放在布局的左上角
                layout.setVisibility(View.VISIBLE);
                layout.updateNoVideoLayout("等待进入", View.VISIBLE);
                addView(layout);
                layout.setLayoutParams(mGrid4ParamList.get(i));

            }
        }
    }


    public void makeTwoLayout() {
        this.post(new Runnable() {
            @Override
            public void run() {
                makeGirdLayout(true);
            }
        });

    }

    public void makeThreeLayout() {
        this.post(new Runnable() {
            @Override
            public void run() {
                makeGirdLayout1(true);
            }
        });
    }

    /**
     * ===============================九宫格布局相关===============================
     */

    /**
     * 两个视图切换成三个视图
     *
     * @param needAddView
     */
    private void makeFloatLayout(boolean needAddView) {
        // 初始化堆叠布局的参数
        if (mFloatParamList == null || mFloatParamList.size() == 0) {
            mFloatParamList = RoomVideoUiUtils.initThreeView(getContext(), getWidth(), getHeight());
        }

        // 根据堆叠布局参数，将每个view放到适当的位置
        for (int i = 0; i < mLayoutEntityList.size(); i++) {
            TRTCLayoutEntity entity = mLayoutEntityList.get(i);
            RelativeLayout.LayoutParams layoutParams = mFloatParamList.get(i);
            entity.layout.setLayoutParams(layoutParams);
            if (i == 0) {
                entity.layout.setMoveable(false);
            } else {
                entity.layout.setMoveable(true);
            }
            addFloatViewClickListener(entity.layout);
            entity.layout.setBottomControllerVisibility(View.GONE);

            if (needAddView) {
                addView(entity.layout);
            }
        }
    }

    /**
     * ===============================堆叠布局相关===============================
     */

    /**
     * 对堆叠布局情况下的 View 添加监听器
     * <p>
     * 用于点击切换两个 View 的位置
     *
     * @param view
     */

    private void addFloatViewClickListener(final TRTCVideoLayout view) {
//        view.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (TRTCLayoutEntity entity : mLayoutEntityList) {
//                    if (entity.layout == v) {
//                        makeFullVideoView(entity.index);
//                        break;
//                    }
//                }
//            }
//        });
    }

    public void makeFullVideoView() {
        Log.i(TAG, "makeFullVideoView:  mCurrentViewIndex from = " + mCurrentViewIndex);
        makeFullVideoView(mCurrentViewIndex);
        mCurrentViewIndex = 0;
    }

    /**
     * 堆叠模式下，将 index 号的 com.common.weight.view 换到 0 号位，全屏化渲染
     *
     * @param index
     */
    int mCurrentViewIndex = 0;

    public void makeFullVideoView(int index) {
        if (index == 0 || index > mLayoutEntityList.size()) return;
        mCurrentViewIndex = index;
        // 1 -> 0
        //        if (index <= 0 || mLayoutEntityList.size() <= index) return;
        Log.i(TAG, "makeFullVideoView: from = " + index);
        TRTCLayoutEntity indexEntity = mLayoutEntityList.get(index);
        ViewGroup.LayoutParams indexParams = indexEntity.layout.getLayoutParams();

        TRTCLayoutEntity fullEntity = mLayoutEntityList.get(0);
        ViewGroup.LayoutParams fullVideoParams = fullEntity.layout.getLayoutParams();

        indexEntity.layout.setLayoutParams(fullVideoParams);
        indexEntity.index = 0;

        fullEntity.layout.setLayoutParams(indexParams);
        fullEntity.index = index;

        indexEntity.layout.setMoveable(false);
        indexEntity.layout.setOnClickListener(null);

        fullEntity.layout.setMoveable(false);
        addFloatViewClickListener(fullEntity.layout);
        // 将 fromView 塞到 0 的位置
        mLayoutEntityList.set(0, indexEntity);
        mLayoutEntityList.set(index, fullEntity);

        for (int i = 0; i < mLayoutEntityList.size(); i++) {
            TRTCLayoutEntity entity = mLayoutEntityList.get(i);
            // 需要对 View 树的 zOrder 进行重排，否则在 RelativeLayout 下，存在遮挡情况
            bringChildToFront(entity.layout);
        }
    }

//排序

    /**
     * 分配视频列表的数据
     *
     * @param fromindex
     */
    public void makeVideoView(int fromindex, int toindex) {
        // 1 -> 0
        //        if (index <= 0 || mLayoutEntityList.size() <= index) return;
        Log.i(TAG, "makeFullVideoView: from = " + fromindex + "makeFullVideoView: to = " + toindex);
        TRTCLayoutEntity indexEntity = mLayoutEntityList.get(fromindex);
        ViewGroup.LayoutParams indexParams = indexEntity.layout.getLayoutParams();

        TRTCLayoutEntity fullEntity = mLayoutEntityList.get(toindex);
        ViewGroup.LayoutParams fullVideoParams = fullEntity.layout.getLayoutParams();

        indexEntity.layout.setLayoutParams(fullVideoParams);
        indexEntity.index = toindex;

        fullEntity.layout.setLayoutParams(indexParams);
        fullEntity.index = fromindex;

        indexEntity.layout.setMoveable(false);
        indexEntity.layout.setOnClickListener(null);

        fullEntity.layout.setMoveable(false);
        addFloatViewClickListener(fullEntity.layout);

        // 将 fromView 塞到 0 的位置
        mLayoutEntityList.set(toindex, indexEntity);
        mLayoutEntityList.set(fromindex, fullEntity);


    }

    public void buildLayout() {
        for (int i = 0; i < mLayoutEntityList.size(); i++) {
            TRTCLayoutEntity entity = mLayoutEntityList.get(i);
            // 需要对 View 树的 zOrder 进行重排，否则在 RelativeLayout 下，存在遮挡情况
            bringChildToFront(entity.layout);
        }
    }

    public interface IVideoLayoutListener {
        void onClickItemFill(String userId, int streamType, boolean enableFill);

        void onClickItemMuteAudio(String userId, boolean isMute);

        void onClickItemMuteVideo(String userId, int streamType, boolean isMute);

        void onClickItemMuteInSpeakerAudio(String userId, boolean isMute);

        void onClickItemRetry(String userId, String type);
    }

    public static class TRTCLayoutEntity {
        public TRTCVideoLayout layout;
        public int index = -1;
        public String userId = "";
        public int streamType = -1;
        public String userType = "";
    }
}
