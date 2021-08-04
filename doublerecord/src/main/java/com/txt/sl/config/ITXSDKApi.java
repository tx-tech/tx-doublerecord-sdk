package com.txt.sl.config;

import android.app.Activity;
import android.app.Application;

import com.txt.sl.TXSdk;
import com.txt.sl.callback.onSDKListener;
import com.txt.sl.callback.onTxPageListener;

/**
 * author ：Justin
 * time ：2021/2/25.
 * des ：SDK api说明
 */
public  interface ITXSDKApi {

    /**
     * @note
     */
    boolean getShare();

    /**
     * @note
     */
     void setShare(boolean share);


    /**
     * @note
     */
    String getAgent();

    /**
     * @note
     */
     void setAgent(String agent);


    /**
     * @note
     */
     String getTerminal();

    /**
     * @note
     */
     String getSDKVersion();

    /**
     * @note
     */
     boolean isDemo();

    /**
     * @note
     */
     void setDemo(boolean demo);


    /**
     * @note
     */
     String getWxTransaction();

    /**
     * @param wxTransaction
     *
     * @note
     */
     void setWxTransaction(String wxTransaction);


    /**
     * 获取可配置信息
     *
     * @note
     */
    TxConfig getTxConfig();


    /**
     * 设置可配置信息（详情见TxConfig）
     *
     * @param txConfig
     *
     *
     * @note
     */
    void setTxConfig(TxConfig txConfig);

    /**
     *
     * 获取环境
     * @return   环境
     */
     TXSdk.Environment getEnvironment();

    /**
     * 设置环境
     *
     * @param environment
     *
     * @note
     */
     void setEnvironment(TXSdk.Environment environment);

    /**
     * 获取debug状态
     *
     *
     * @note
     */
     boolean isDebug();

    /**
     * 设置debug状态
     *
     * @param debug
     *
     * @note
     */
    void setDebug(boolean debug);


    /**
     * 初始化
     *
     * @param application
     * @param en
     * @param isDebug
     * @param txConfig
     *
     * @note
     */
    void init(Application application, TXSdk.Environment en, boolean isDebug, TxConfig txConfig);


    /**
     * 切换环境
     *
     * @param en
     *
     * @note
     */
    void checkoutNetEnv(TXSdk.Environment en);

    /**
     * 跳到详情页面
     *
     * @param context
     *
     * @param account
     * @param fullName
     * @param taskId
     * @param org
     * @param sign
     * @note
     */
     void gotoOrderDetaisPage(Activity context , String account, String fullName, String taskId, String org, String sign, onSDKListener onSDKListener);

    /**
     * 跳到列表页
     *
     * @param account
     *
     * @param fullName
     * @param org
     * @param sign
     * @note
     */
     void gotoOrderListPage(Activity context ,String account,String fullName,String org,String sign,onSDKListener onSDKListener);



    /**
     * 跳到上传页面
     *
     * @param context
     *
     * @param account
     *
     *
     * @param fullName
     * @param taskId
     * @param org
     * @param sign
     * @note
     */
     void gotoVideoUploadPage(Activity context , String account, String fullName, String taskId, String org, String sign, onSDKListener onSDKListener);



    /**
     * 跳到创建工单页面，目前仅测试使用
     *
     * @param context
     *
     * @param account
     *
     *
     * @param fullName
     * @param org
     * @param sign
     * @note
     */
    void gotoCreateDetailsPage(Activity context, String account, String fullName, String org, String sign, onSDKListener onSDKListener);


    /**
     *监听返回按钮
     *
     * @param onTxPageListener
     *
     * @return
     */
    public abstract void addOnTxPageListener(onTxPageListener onTxPageListener);

    /**
     *监听返回按钮
     *
     * @param onTxPageListener
     *
     * @return
     */
    public abstract void removeOnTxPageListener(onTxPageListener onTxPageListener);


}
