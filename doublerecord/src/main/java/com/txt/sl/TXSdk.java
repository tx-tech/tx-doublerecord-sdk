package com.txt.sl;

import android.app.Activity;
import android.app.Application;


import com.common.widget.titlebar.TitleBar;
import com.common.widget.base.SystemBase;
import com.common.widget.base.SystemManager;
import com.common.widget.toast.ToastUtils;
import com.txt.sl.callback.onCreateRoomListener;
import com.txt.sl.config.AppBarInitializer;
import com.txt.sl.receive.SystemBaiduLocation;
import com.txt.sl.system.SystemCrashHandler;
import com.txt.sl.system.SystemHttpRequest;
import com.txt.sl.system.SystemLogHelper;
import com.txt.sl.system.SystemSocket;
import com.txt.sl.utils.AppUtils;
import com.txt.sl.utils.ApplicationUtils;
import com.txt.sl.utils.TxLogUtils;


/**
 * Created by JustinWjq
 *
 * @date 2020/8/19.
 * description：暴露功能类
 */
public class TXSdk implements ITXSDKApi {
    private static volatile TXSdk singleton = null;

    private boolean isDebug = true;

    private Environment environment = Environment.TEST;

    private String wxKey = "";

    private TxConfig txConfig;

    private boolean isDemo = false;

    private String SDKVersion = "v1.0.0";

    private String terminal = "android";

    private String wxTransaction = "";

    private String agent;

    private boolean isShare;


    @Override
    public boolean getShare() {
        return isShare;
    }

    @Override
    public void setShare(boolean share) {
        isShare = share;
    }

    @Override
    public String getAgent() {
        return agent;
    }

    @Override
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String getTerminal() {
        return terminal;
    }

    @Override
    public String getSDKVersion() {
        return SDKVersion;
    }

    @Override
    public boolean isDemo() {
        return isDemo;
    }

    @Override
    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    @Override
    public String getWxTransaction() {
        return wxTransaction;
    }

    @Override
    public void setWxTransaction(String wxTransaction) {
        this.wxTransaction = wxTransaction;
    }

    @Override
    public TxConfig getTxConfig() {
        if (txConfig == null) {
            txConfig = new TxConfig();
        }
        return txConfig;
    }

    @Override
    public void setTxConfig(TxConfig txConfig) {
        this.txConfig = txConfig;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean isDebug() {
        return isDebug;
    }

    @Override
    public void setDebug(boolean debug) {
        isDebug = debug;
    }


    private TXSdk() {

    }

    public Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void init(Application application) {
        init(application, environment, isDebug);
    }

    public <T extends SystemBase>T getSystem(Class<T> tClass){
        return SystemManager.getInstance().getSystem(tClass);
    }

    public void init(Application application, Environment en, boolean isDebug) {
        TxLogUtils.i("SDKVersion:"+getSDKVersion());
        this.application = application;
        this.isDebug = isDebug;
        if (txConfig == null) {
            txConfig = new TxConfig();
        }
        checkoutNetEnv(en);
        AppUtils.init(application);
        ApplicationUtils.init(application);
        ToastUtils.init(application);
//        CrashReport.setIsDevelopmentDevice(application, BuildConfig.DEBUG);

        //        Beta.autoCheckUpgrade = false;
//        CrashReport.initCrashReport(application, "5960ee0d93", BuildConfig.DEBUG);

        //设置appbar的样式
        TitleBar.setDefaultInitializer(new AppBarInitializer());
        SystemBaiduLocation.getInstance().initLocation();
    }

    @Override
    public void init(Application application, Environment en, boolean isDebug, TxConfig txConfig) {
        this.txConfig = txConfig;
        init(application, en, isDebug);
    }

    @Override
    public void checkoutNetEnv(Environment en) {
        setEnvironment(en);
        SystemHttpRequest.getInstance().changeIP(en);
    }

    @Override
    public void gotoOrderDetaisPage(Activity context, String loginName, String fullName, String flowId, String org, String sign) {
        TXManagerImpl.getInstance().gotoOrderDetaisPage(context, loginName, fullName, org, flowId, sign, new onCreateRoomListener() {
        });
    }

    @Override
    public void gotoOrderListPage(Activity context, String loginName, String fullName, String org, String sign) {
        TXManagerImpl.getInstance().gotoOrderListPage(context, loginName, fullName, org, sign, new onCreateRoomListener() {
        });
    }

    @Override
    public void gotoVideoUploadPage(Activity context, String loginName, String fullName, String flowId, String org, String sign) {
        TXManagerImpl.getInstance().gotoVideoUploadPage(context, loginName, fullName, org,flowId, sign, new onCreateRoomListener() {
        });
    }

    @Override
    public void gotoCreateDetailsPage(Activity context, String loginName, String fullName, String org, String sign) {
        TXManagerImpl.getInstance().gotoCreateDetaisPage(context, loginName, fullName, org, sign, new onCreateRoomListener() {
        });
    }


    public enum Environment {
        DEV,
        TEST,
        RELEASE
    }

    public interface TXSDKErrorCode {
        int TXSDK_ERROR_INVITENUMBER_INVALID = 1;

    }

    public static TXSdk getInstance() {
        if (singleton == null) {
            synchronized (TXSdk.class) {
                if (singleton == null) {
                    singleton = new TXSdk();
                }
            }
        }
        return singleton;
    }

}
