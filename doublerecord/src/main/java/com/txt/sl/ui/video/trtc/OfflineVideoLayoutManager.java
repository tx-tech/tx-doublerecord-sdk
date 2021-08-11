package com.txt.sl.ui.video.trtc;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.txt.sl.R;
import com.txt.sl.utils.RoomVideoUiUtils;

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
 * 3.堆叠布局：{@link OfflineVideoLayoutManager#makeGirdLayout(boolean)} 思路是初始化一系列的 x、y、padding、margin 组合 LayoutParams 直接对 View 进行定位
 * <p>
 * 4.宫格布局：{@link OfflineVideoLayoutManager#makeGirdLayout(boolean)} 思路与堆叠布局一致，也是初始化一些列的 LayoutParams 直接对 View 进行定位
 * <p>
 * 5.如何实现管理：
 * A. 使用{@link TRTCLayoutEntity} 实体类，保存 {@link TRTCVideoLayout} 的分配信息，能够与对应的用户绑定起来，方便管理与更新UI
 * B. {@link TRTCVideoLayout} 专注实现业务 UI 相关的，控制逻辑放在此类中
 * <p>
 * 6.布局切换，见 {@link OfflineVideoLayoutManager#switchMode()}
 * <p>
 * 7.堆叠布局与宫格布局参数，见{@link RoomVideoUiUtils } 工具类
 */
public class OfflineVideoLayoutManager extends RelativeLayout implements TRTCVideoLayout.IVideoLayoutListener {
    public static final int MODE_FLOAT = 1;  // 前后堆叠模式
    public static final int MODE_GRID = 2;  // 九宫格模式
    public int MAX_USER = 3;
    private final static String TAG = OfflineVideoLayoutManager.class.getSimpleName();
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
    public OfflineVideoLayoutManager(Context context) {
        super(context);
        initView(context,MAX_USER);
    }


    public OfflineVideoLayoutManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,MAX_USER);
    }


    public OfflineVideoLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,MAX_USER);
    }

    public void initView(Context context ,int max_user) {
        this.MAX_USER =max_user;
        Log.i(TAG, "initView: ");

        mLayoutEntityList = new ArrayList<TRTCLayoutEntity>();
        // 初始化多个 View，以备用
        for (int i = 0; i < MAX_USER; i++) {
            if (i==0){
                TRTCVideoLayout videoLayout = new TRTCVideoLayout(context);
                videoLayout.setVisibility(View.INVISIBLE);
                videoLayout.setBackgroundColor(Color.WHITE);
                videoLayout.setMoveable(false);
                videoLayout.setIVideoLayoutListener(this);
                // 这里不展示其底部的控制菜单
                videoLayout.setBottomControllerVisibility(View.GONE);
                videoLayout.updateNoVideoLayout("",View.VISIBLE);
                TRTCLayoutEntity entity = new TRTCLayoutEntity();
                entity.layout = videoLayout;
                entity.index = i;
                mLayoutEntityList.add(entity);
            }else{
                BusinessLayout videoLayout = new BusinessLayout(context);
                TRTCLayoutEntity entity = new TRTCLayoutEntity();
                entity.layout = videoLayout;
                entity.index = i;
                entity.userId = ""+i;
                mLayoutEntityList.add(entity);
            }
        }


        this.post(new Runnable() {
            @Override
            public void run() {
                makeGirdLayout(true);
            }
        });
    }

    /**
     * ===============================九宫格布局下点击按钮的事件回调===============================
     */

    @Override
    public void onClickFill(TRTCVideoLayout view, boolean enableFill) {
        OfflineVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemFill(entity.userId, entity.streamType, enableFill);
        }
    }

    @Override
    public void onClickMuteAudio(TRTCVideoLayout view, boolean isMute) {
        OfflineVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteAudio(entity.userId, isMute);
        }
    }

    @Override
    public void onClickMuteVideo(TRTCVideoLayout view, boolean isMute) {
        OfflineVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteVideo(entity.userId, entity.streamType, isMute);
        }
    }

    @Override
    public void onClickMuteInSpeakerAudio(TRTCVideoLayout view, boolean isMute) {
        OfflineVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemMuteInSpeakerAudio(entity.userId, isMute);
        }
    }

    @Override
    public void onClickRetry(TRTCVideoLayout view, String type) {
        OfflineVideoLayoutManager.IVideoLayoutListener listener = mWefListener != null ? mWefListener.get() : null;
        if (listener != null) {
            TRTCLayoutEntity entity = findEntity(view);
            listener.onClickItemRetry(entity.userId, type);
        }
    }

    /**
     * ===============================Manager对外相关方法===============================
     */
    public void setIVideoLayoutListener(OfflineVideoLayoutManager.IVideoLayoutListener listener) {
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
     * 根据 userId 和 视频类型（大、小、辅路）画面分配对应的 com.common.weight.view
     *
     * @param userId
     * @param userType agent insurant  insured
     * @param streamType
     * @return
     */
    public TXCloudVideoView allocCloudVideoView(String userId,String userType, int streamType) {
        if (userId == null) return null;
        TRTCLayoutEntity trtcLayoutEntity;
//        String content = "";
        if (userType =="agent") {
             trtcLayoutEntity = mLayoutEntityList.get(0);
//            content = "代理人等待进入";
        }else if(userType =="2"){
             trtcLayoutEntity = mLayoutEntityList.get(2);
//            content = "投保人等待进入";
        }else{
            trtcLayoutEntity = mLayoutEntityList.get(1);
//            content = "代理人等待进入";
        }

        trtcLayoutEntity.userId = userId;
        trtcLayoutEntity.streamType = streamType;
        trtcLayoutEntity.userType = userType;
        BusinessVideo layout = trtcLayoutEntity.layout;
        if (layout instanceof BusinessLayout) {
            BusinessLayout businessLayout = (BusinessLayout) layout;
            businessLayout.setVisibility(VISIBLE);
            return null;
        }else{
            TRTCVideoLayout businessLayout = (TRTCVideoLayout) layout;
            businessLayout.setVisibility(VISIBLE);
            businessLayout.updateNoVideoLayout("", View.GONE);
            return businessLayout.getVideoView();
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

    public TRTCLayoutEntity findEntityBytype(String userType) {
        for (TRTCLayoutEntity entity : mLayoutEntityList) {
            if (entity.userType.equals(userType)) return entity;
        }
        return null;
    }


    /**
     * 切换到两人模式
     *
     * @param needUpdate 是否需要更新布局
     */
    public void makeGirdLayout(boolean needUpdate) {
        if (mGrid4ParamList == null || mGrid4ParamList.size() == 0 || mGrid9ParamList == null || mGrid9ParamList.size() == 0) {
            mGrid4ParamList = RoomVideoUiUtils.initTwoView(getContext(), getWidth(), getHeight());
        }
        if (needUpdate) {
            for (int i = 0; i < mLayoutEntityList.size(); i++) {
                TRTCLayoutEntity entity = mLayoutEntityList.get(i);
                BusinessVideo layout = entity.layout;

                if (layout instanceof BusinessLayout) {
                    BusinessLayout businessLayout = (BusinessLayout) layout;

                    businessLayout.setOnClickListener(null);
                    // 我自己要放在布局的左上角
                    businessLayout.setVisibility(View.VISIBLE);

                    addView(businessLayout);
                    businessLayout.setLayoutParams(mGrid4ParamList.get(i));
                }else{
                    TRTCVideoLayout businessLayout = (TRTCVideoLayout) layout;

                    businessLayout.setMoveable(false);
                    businessLayout.setOnClickListener(null);
                    // 我自己要放在布局的左上角
                    businessLayout.setVisibility(View.VISIBLE);
                    businessLayout.updateNoVideoLayout("", View.VISIBLE);

                    addView(businessLayout);
                    businessLayout.setLayoutParams(mGrid4ParamList.get(i));
                }



            }
        }
    }

        //切换三人模式
    public void makeGirdLayout1(boolean needUpdate) {
        if (mGrid4ParamList == null || mGrid4ParamList.size() == 0 || mGrid9ParamList == null || mGrid9ParamList.size() == 0) {
            mGrid4ParamList = RoomVideoUiUtils.initThreeView(getContext(), getWidth(), getHeight());
        }
        if (needUpdate) {
            for (int i = 0; i < mLayoutEntityList.size(); i++) {
                TRTCLayoutEntity entity = mLayoutEntityList.get(i);
                BusinessVideo layout = entity.layout;

                if (layout instanceof BusinessLayout) {
                    BusinessLayout businessLayout = (BusinessLayout) layout;

                    businessLayout.setOnClickListener(null);
                    // 我自己要放在布局的左上角
                    businessLayout.setVisibility(View.VISIBLE);

                    addView(businessLayout);
                    businessLayout.setLayoutParams(mGrid4ParamList.get(i));
                }else{
                    TRTCVideoLayout businessLayout = (TRTCVideoLayout) layout;
                    businessLayout.setMoveable(false);
                    businessLayout.setOnClickListener(null);
                    // 我自己要放在布局的左上角
                    businessLayout.setVisibility(View.VISIBLE);
                    businessLayout.updateNoVideoLayout("等待进入", View.VISIBLE);
                    addView(businessLayout);
                    businessLayout.setLayoutParams(mGrid4ParamList.get(i));
                }


            }
        }
    }
    /**
     * 堆叠模式下，将 index 号的 com.common.weight.view 换到 0 号位，全屏化渲染
     *
     * @param index
     */
    int mCurrentViewIndex = 0;
    public void makeFullVideoView(int index) {
        if (index == 0) return;
        mCurrentViewIndex = index;
        // 1 -> 0
        //        if (index <= 0 || mLayoutEntityList.size() <= index) return;
        Log.i(TAG, "makeFullVideoView: from = " + index);
        TRTCLayoutEntity indexEntity = mLayoutEntityList.get(index);

        BusinessVideo indexLayout = indexEntity.layout;

        TRTCLayoutEntity fullEntity = mLayoutEntityList.get(0);

        BusinessVideo fullLayout = fullEntity.layout;
        ViewGroup.LayoutParams indexParams;
        ViewGroup.LayoutParams fullVideoParams ;
        if (indexLayout instanceof BusinessLayout) {
            BusinessLayout businessLayout = (BusinessLayout) indexLayout;
            businessLayout.setVisibility(VISIBLE);
            indexParams = businessLayout.getLayoutParams();
        }else{
            TRTCVideoLayout businessLayout = (TRTCVideoLayout) indexLayout;
            businessLayout.setVisibility(VISIBLE);
            businessLayout.updateNoVideoLayout("", View.GONE);
            indexParams = businessLayout.getLayoutParams();
        }

        if (fullLayout instanceof BusinessLayout) {
            BusinessLayout businessLayout = (BusinessLayout) fullLayout;
            businessLayout.setVisibility(VISIBLE);
            fullVideoParams = businessLayout.getLayoutParams();
        }else{
            TRTCVideoLayout businessLayout = (TRTCVideoLayout) fullLayout;
            businessLayout.setVisibility(VISIBLE);
            businessLayout.updateNoVideoLayout("", View.GONE);
            fullVideoParams = businessLayout.getLayoutParams();
        }

        indexEntity.index = 0;
        fullEntity.index = index;
        if (indexLayout instanceof BusinessLayout) {
            BusinessLayout businessLayout = (BusinessLayout) indexLayout;
            businessLayout.setLayoutParams(fullVideoParams);
        }else{
            TRTCVideoLayout businessLayout = (TRTCVideoLayout) indexLayout;
            businessLayout.setLayoutParams(fullVideoParams);
        }

        if (fullLayout instanceof BusinessLayout) {
            BusinessLayout businessLayout = (BusinessLayout) fullLayout;
            businessLayout.setLayoutParams(indexParams);
        }else{
            TRTCVideoLayout businessLayout = (TRTCVideoLayout) fullLayout;
            businessLayout.setLayoutParams(indexParams);
        }





        // 将 fromView 塞到 0 的位置
        mLayoutEntityList.set(0, indexEntity);
        mLayoutEntityList.set(index, fullEntity);

        for (int i = 0; i < mLayoutEntityList.size(); i++) {
            TRTCLayoutEntity entity = mLayoutEntityList.get(i);
            // 需要对 View 树的 zOrder 进行重排，否则在 RelativeLayout 下，存在遮挡情况
            BusinessVideo layout = entity.layout;
            if (layout instanceof BusinessLayout) {
                BusinessLayout businessLayout = (BusinessLayout) layout;
                bringChildToFront(businessLayout);
            }else{
                TRTCVideoLayout businessLayout  = (TRTCVideoLayout) layout;
                bringChildToFront(businessLayout);
            }

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
        public BusinessVideo layout;
        public int index = -1;
        public String userId = "";
        public int streamType = -1;
        public String  userType = "";
    }
}
